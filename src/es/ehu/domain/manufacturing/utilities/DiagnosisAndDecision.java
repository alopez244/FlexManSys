package es.ehu.domain.manufacturing.utilities;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;


public class DiagnosisAndDecision extends Agent{
//    private volatile AID QoSID = new AID("QoSManagerAgent", false);
    private int convIDCounter=1;
    static final Logger LOGGER = LogManager.getLogger(DiagnosisAndDecision.class.getName());
    private Agent myAgent=this;
    public String control="automatic";
    public MessageTemplate expected_senders=MessageTemplate.or(MessageTemplate.MatchSender(new AID("planner",AID.ISLOCALNAME)),
                                            MessageTemplate.MatchSender(new AID("QoSManagerAgent",AID.ISLOCALNAME)));

    protected void setup(){
        LOGGER.entry();

        LOGGER.info("Diagnosis and Decision Agent started");
        addBehaviour(new DDEventManager() );
        LOGGER.exit();
    }

    class DDEventManager extends CyclicBehaviour {

        private String control="automatic";

        public void action() {
                ACLMessage msg=receive(expected_senders);  //solo lee mensajes de los agentes indicados en el template
                if(msg!=null) {
                    if (msg.getOntology().equals("not_found")&&msg.getSender().getLocalName().equals("QoSManagerAgent")) { //Se reporta un agente aislado o muerto
                        if(control.equals("automatic")){ //solo toma decisiones si está en modo automatico
                            if(!msg.getContent().contains("ControlGatewayCont")){ //no es un agente GW
                                LOGGER.error(msg.getContent()+" is either dead or isolated.");

                                if(msg.getContent().contains("batchagent")||msg.getContent().contains("orderagent")||msg.getContent().contains("mplanagent")){ //si es agente de aplicacion
                                    try {
                                        ACLMessage state= sendCommand(myAgent, "get "+msg.getContent()+" attrib=state", msg.getContent()+"_State_"+convIDCounter); //consigue el estado de la replcia caida
                                        LOGGER.warn(msg.getContent()+" was in "+state.getContent()+" state.");
                                        if(!state.getContent().equals("tracking")){ //si no estaba en tracking estaría en running o bootToRunning y requiere una acción
                                            if (msg.getContent().contains("batchagent")) { //en caso de ser batch habría que hibernar el agente máquina hasta recuperar la replica
                                                String machine = get_relationship(msg.getContent());
                                                LOGGER.info(machine + " is changing to idle state");
                                                sendACL(16, machine, "control", "setstate idle");
                                            }
                                        }
                                            ACLMessage parent= sendCommand(myAgent, "get "+msg.getContent()+" attrib=parent", msg.getContent()+"_parent_"+convIDCounter);
                                            ACLMessage hosting_node=sendCommand(myAgent, "get "+msg.getContent()+" attrib=node", msg.getContent()+"_Hosting_PNode_"+convIDCounter);
//                                            sendACL(7, reply3.getContent(), "delete_replica", msg.getContent()); //TODO innecesario con el nuevo sistema porque esta centralizado en el SA. ELIMINAR esta parte de los agentes de aplicación
                                            sendCommand(myAgent, "del "+msg.getContent(),"Unregister_"+msg.getContent()+"_"+convIDCounter);
                                            if(!PingAgent("pnodeagent"+hosting_node.getContent())){
                                                //TODO reiniciar todas las replicas de este nodo
                                                sendCommand(myAgent, "del pnodeagent"+hosting_node.getContent(),"Unregister_pnodeagent"+hosting_node.getContent()+convIDCounter); //nodo caido, se desregistra del SA
                                            }else{  //nodo no caido, pero replica sí
                                                ACLMessage hosted_elements=sendCommand(myAgent, "get "+"pnodeagent"+hosting_node.getContent()+" attrib=HostedElements", "pnodeagent"+hosting_node.getContent()+"_HE_"+convIDCounter);
                                                String[] HE=new String[1];
                                                if(hosted_elements.getContent().contains(",")){
                                                    HE=hosted_elements.getContent().split(",");
                                                }else{
                                                    HE[0]=hosted_elements.getContent();
                                                }
                                                String new_HE="";
                                                ArrayList<String> updated_hosted_elements=new ArrayList<String>();
                                                for(int i=0;i<HE.length;i++){
                                                    updated_hosted_elements.add(HE[i]);
                                                }
                                                for(int i=0;i< updated_hosted_elements.size();i++){
                                                    if(updated_hosted_elements.get(i).contains(parent.getContent())){
                                                        updated_hosted_elements.remove(i);
                                                    }else{
                                                        if(i==0){
                                                            new_HE=updated_hosted_elements.get(i);
                                                        }else{
                                                            new_HE=new_HE+","+updated_hosted_elements.get(i);
                                                        }
                                                    }
                                                }
                                                ACLMessage set= sendCommand(myAgent, "set pnodeagent"+hosting_node.getContent()+" HostedElements="+new_HE, "pnodeagent"+hosting_node.getContent()+"_set_hosting_elements_"+convIDCounter);
                                            }
                                            boolean done=restart_replica(parent.getContent(),state.getContent());
                                            if(done){
                                                //TODO si estaba en running hay que despertar al machine agent
                                            }


                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }else if(msg.getContent().contains("machine")){
                                    String batch=get_relationship(msg.getContent());
                                    //TODO machine agent  aislado
                                }

                            }else { //agente GW no encontrado
                                String msgparts[] = msg.getContent().split("/div/"); //el agente GW siempre se reporta con su machine responsable
                                String GW = msgparts[0];
                                LOGGER.error(GW + " is either dead or isolated. Manually reset GW.");
                                if (msgparts[1] != null) {
//                                    LOGGER.info(msgparts[1] + " is the machine assigned to "+GW);
                                    sendACL(16,msgparts[1] , "control", "setstate idle");
                                    LOGGER.info(msgparts[1] + " is now idling");
                                }else{
                                    LOGGER.error("QoS should have sent which machine is assigned to GW.");
                                }
                            }
                        }else{
                            LOGGER.error(msg.getContent()+" is either dead or isolated.");
                            LOGGER.warn("MANUAL MODE: User must take a decision to solve the issue");
                        }
                        convIDCounter++;
                    } else if (msg.getOntology().equals("msg_lost")&&msg.getSender().getLocalName().equals("QoSManagerAgent")) {
                        LOGGER.warn("Inform operator: Message lost."+"\n"); //si se pierde un mensaje no se puede hacer nada. Se avisa al operador para que actue en consecuencia.

                        String[] msgparts=msg.getContent().split("/div/");
                        String msgreceiver = msgparts[0];
                        String intercepted_msg = msgparts[1];
                        System.out.println("Receiver: "+msgreceiver+"\n");
                        System.out.println("Content: "+intercepted_msg+"\n");
//                        String performative=msgparts[0];
//                        String ontology=msgparts[1];
//                        String convID=msgparts[2];
//                        String receiver=msgparts[3];
//                        String intercepted_msg=msgparts[4];
                        if(control.equals("automatic")){
//                            ACLMessage bridgedmsg=new ACLMessage(Integer.parseInt(performative));
//                            bridgedmsg.setOntology(ontology);
//                            AID receiverID = new AID(receiver, false);
//                            bridgedmsg.addReceiver(receiverID);
//                            bridgedmsg.setConversationId(convID);
//                            bridgedmsg.setContent(intercepted_msg);
//                            send(bridgedmsg);
                        }else{
                            LOGGER.warn("MANUAL MODE: User must take a decision to solve the issue");
                        }
                        LOGGER.warn("Consider taking actions to solve the issue if needed");
                    } else if (msg.getOntology().equals("timeout")&&msg.getSender().getLocalName().equals("QoSManagerAgent")) {
                        //idle aqui
                        if(control.equals("automatic")){
                            LOGGER.error("Double timeout thrown. Machine agent "+msg.getContent()+" idling");
                            sendACL(16,msg.getContent(),"control","setstate idle");
                        }else{
                            LOGGER.error("Timeout thrown. Machine agent "+msg.getContent()+" should idle");
                            LOGGER.warn("MANUAL MODE: User must take a decision to solve the issue");
                        }
                    } else if(msg.getOntology().equals("man/auto")){
                        control=msg.getContent();
                        LOGGER.info("Changed to "+control+" mode");
                        sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),msg.getOntology(),"ack");
                    }
                }

            }
        }

    private void sendACL(int performative,String receiver,String ontology,String content){ //Funcion estándar de envío de mensajes
        AID receiverAID=new AID(receiver,false); //pasamos la máquina a estado idle
        ACLMessage msg=new ACLMessage(performative);
        msg.addReceiver(receiverAID);
        msg.setOntology(ontology);
        msg.setContent(content);
        send(msg);
    }
    public ACLMessage sendCommand(Agent agent, String cmd, String conversationId) throws Exception {

        this.myAgent = agent;

        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType("sa");
        dfd.addServices(sd);
        String mwm;

        while (true) {
            DFAgentDescription[] result = DFService.search(myAgent, dfd);

            if ((result != null) && (result.length > 0)) {
                dfd = result[0];
                mwm = dfd.getName().getLocalName();
                break;
            }
            LOGGER.info(".");
            Thread.sleep(100);

        } //end while (true)

        LOGGER.entry(mwm, cmd);
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID(mwm, AID.ISLOCALNAME));
        msg.setConversationId(conversationId);
        msg.setOntology("control");
        msg.setContent(cmd);
        msg.setReplyWith(cmd);
        myAgent.send(msg);
        ACLMessage reply = myAgent.blockingReceive(
                MessageTemplate.and(
                        MessageTemplate.MatchInReplyTo(msg.getReplyWith()),
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM))
                , 1000);

        return LOGGER.exit(reply);
    }
    private int SearchAgent (String agent){
        int found=0;
        AMSAgentDescription[] agents = null;

        try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults ( new Long(-1) );
            agents = AMSService.search(myAgent, new AMSAgentDescription (), c );
        }
        catch (Exception e) {
            System.out.println(e);
        }
        for (int i=0; i<agents.length;i++){
            AID agentID = agents[i].getName();
            String agent_to_check=agentID.getLocalName();
//            System.out.println(agent_to_check);
            if(agent_to_check.contains(agent)){
                found++;
            }
        }
        return found;
    }
    private String get_relationship(String agent){
        MessageTemplate t=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("askrelationship"));
        sendACL(16,"QoSManagerAgent" , "askrelationship", agent);
        ACLMessage received_agent= blockingReceive(t, 2000);
        if(received_agent!=null){
            LOGGER.info(received_agent.getContent()+" is assigned to "+ agent);
            return received_agent.getContent();
        }else{
            LOGGER.error("QoSManagerAgent did not answer on time");
            return "error";
        }
    }
    private boolean restart_replica(String parent, String state){

        try {
            ACLMessage process_nodes = sendCommand(myAgent, "get * category=pNodeAgent","GetPNodes");

        ArrayList<String> NegotiatingPnodes = new ArrayList<>();
        if (process_nodes != null) {
            String[] AllPnode=new String[1];
            if(process_nodes.getContent().contains(",")){
                AllPnode=process_nodes.getContent().split(",");
            }else{
                AllPnode[0]=process_nodes.getContent();
            }
            for(int i=0;i<AllPnode.length;i++){
                NegotiatingPnodes.add(AllPnode[i]);
            }
            for(int i=0;i<NegotiatingPnodes.size();i++){
                ACLMessage valid_nodes  = sendCommand(myAgent, "get "+NegotiatingPnodes.get(i)+ " attrib=HostedElements","CheckIfValidNode");
                LOGGER.info(NegotiatingPnodes.get(i)+" hosts "+valid_nodes.getContent());
                if(valid_nodes.getContent().contains(parent)){
                    LOGGER.info(NegotiatingPnodes.get(i)+" is not valid because it already hosts "+parent);
                    NegotiatingPnodes.remove(i);
                    i--;
                }else{
                    LOGGER.info(NegotiatingPnodes.get(i)+" could host "+parent);
                }
            }

            String ToNegotiate="";
            for(int i=0; i<NegotiatingPnodes.size();i++){
                if(i==0){
                    ToNegotiate=NegotiatingPnodes.get(i);
                }else{
                    ToNegotiate=ToNegotiate+","+NegotiatingPnodes.get(i);
                }
            }
            LOGGER.debug("Participating nodes: "+ToNegotiate);
            if(ToNegotiate.equals("")){
                LOGGER.warn("There is no node available to store a replica");
                return false;
            }else{
                ACLMessage category = sendCommand(myAgent, "get " + parent + " attrib=category","GetReplicaCategory");
                String seClass="";
                if(parent.contains("batch")){
                    seClass="es.ehu.domain.manufacturing.agents.BatchAgent";
                }else if(parent.contains("order")){
                    seClass="es.ehu.domain.manufacturing.agents.OrderAgent";
                }else{
                    seClass="es.ehu.domain.manufacturing.agents.MPlanAgent";
                }
                String criteria="";
                if(state.equals("tracking")){
                    criteria="max mem"; //si la replica esta en tracking interesa negociar con la memoria de los nodos
                }else{
                    criteria="cpu use"; //si la replica esta en running interesa negociar con el uso del CPU de los nodos
                }
                String negotationdata="localneg "+ToNegotiate+ " criterion="+criteria+" action=start externaldata=" + parent + "," + category.getContent() + "," + seClass + "," + state + "," + "1" + "," + myAgent.getLocalName();
                sendCommand(myAgent,negotationdata , "Restore_"+state+"_Replica");
                return true;
            }
        }else{
            LOGGER.error("Something went wrong restarting a replica");
        }
        } catch (Exception e) {
            LOGGER.error("Something went wrong restarting a replica");
            e.printStackTrace();
        }
        return false;
    }
    private boolean PingAgent (String name){  //checkea el estado de los agentes de aplicación, recurso y gateway
        MessageTemplate pingtemplate=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("ping"));
        boolean state;
        AID Agent_to_ping_ID=new AID(name,false);
        ACLMessage ping=new ACLMessage(ACLMessage.REQUEST);
        ping.setOntology("ping");
        ping.addReceiver(Agent_to_ping_ID);
        ping.setContent("");
        send(ping);
        ACLMessage echo=blockingReceive(pingtemplate,500);
        if(echo!=null) {
            LOGGER.info(name+" answered on time.");
            state=true;
        }else{
            LOGGER.error(name+" did not answer on time. Confirming failure.");
            state=false;
        }
        return state;
    }

}
