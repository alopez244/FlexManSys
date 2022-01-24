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
import java.util.HashMap;


public class DiagnosisAndDecision extends Agent{
//    private volatile AID QoSID = new AID("QoSManagerAgent", false);
    private int convIDCounter=1;
    static final Logger LOGGER = LogManager.getLogger(DiagnosisAndDecision.class.getName());
    private Agent myAgent=this;
    public String control="automatic";
    private MessageTemplate expected_senders=MessageTemplate.or(MessageTemplate.MatchSender(new AID("planner",AID.ISLOCALNAME)),
                                            MessageTemplate.MatchSender(new AID("QoSManagerAgent",AID.ISLOCALNAME)));
    private MessageTemplate neg_template=MessageTemplate.and(MessageTemplate.MatchOntology("negotiation"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM));
    protected void setup(){
        LOGGER.entry();

        LOGGER.info("Diagnosis and Decision Agent started");
        addBehaviour(new DDEventManager() );
        LOGGER.exit();
    }

    class DDEventManager extends CyclicBehaviour {

        private String control="automatic";

        public void action() {

                ACLMessage negotiation_result=receive(neg_template);
                if(negotiation_result!=null){

                    String convID="negotiation_winner_";
                    String winner=negotiation_result.getSender().getLocalName();
                    if(winner.contains("batchagent")){
                        LOGGER.info("New batch agent is in running state: "+winner);
                        sendACL(7,winner,"restart_timeout","reset_timeout"); //si es batch debe resetear el timeout
                        try {
                            ACLMessage parent=sendCommand(myAgent,"get "+winner+" attrib=parent",convID+String.valueOf(convIDCounter++));
                            String target=get_relationship(parent.getContent());
                            sendACL(ACLMessage.INFORM,target,"release_buffer",winner);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else if(winner.contains("orderagent")){
                        try {
                            LOGGER.info("New order agent is in running state: "+winner);
//                            ACLMessage parent=sendCommand(myAgent,"get "+winner+" attrib=parent",convID+String.valueOf(convIDCounter));
                            //TODO corregir
                            ACLMessage parent_of_dead_SE=sendCommand(myAgent,"get "+winner+" attrib=parent",convID+String.valueOf(convIDCounter++)); //parent del order


                            ACLMessage parent_of_target=sendCommand(myAgent,"get * parent="+parent_of_dead_SE.getContent()+" category=batch",convID+String.valueOf(convIDCounter));
                            ACLMessage target=sendCommand(myAgent,"get * parent="+parent_of_target.getContent()+" category=batch",convID+String.valueOf(convIDCounter));
                            sendACL(ACLMessage.INFORM,target.getContent(),"release_buffer",winner);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else if(winner.contains("mplanagent")){
                        //TODO
                    }
                }
                ACLMessage msg=receive(expected_senders);  //solo lee mensajes de los agentes indicados en el template
                if(msg!=null) {
                    if (msg.getOntology().equals("not_found")&&msg.getSender().getLocalName().equals("QoSManagerAgent")) { //Se reporta un agente aislado o muerto
                        if(control.equals("automatic")){ //solo toma decisiones si está en modo automatico
                            if(!msg.getContent().contains("ControlGatewayCont")){ //no es un agente GW por lo que el D&D puede realizar alguna acción
                                LOGGER.error(msg.getContent()+" is either dead or isolated.");
                                if(msg.getContent().contains("batchagent")||msg.getContent().contains("orderagent")||msg.getContent().contains("mplanagent")){ //Es agente de aplicacion
                                    try {
                                        ACLMessage state= sendCommand(myAgent, "get "+msg.getContent()+" attrib=state", "D&D_"+convIDCounter++); //consigue el estado de la replcia caida
                                        LOGGER.warn(msg.getContent()+" was in "+state.getContent()+" state.");
                                        ACLMessage parent= sendCommand(myAgent, "get "+msg.getContent()+" attrib=parent", "D&D_"+convIDCounter++);

                                            ACLMessage hosting_node=sendCommand(myAgent, "get "+msg.getContent()+" attrib=node", "D&D_"+convIDCounter++); //devuelve el número de nodo que hostea a la replica
                                            sendCommand(myAgent, "del "+msg.getContent(),"D&D_"+convIDCounter++);

                                        HashMap<String,HashMap<String,ArrayList<String>>> agents_sorted_by_state=new HashMap<String,HashMap<String,ArrayList<String>>>();
                                            if(!PingAgent("pnodeagent"+hosting_node.getContent())){  //checkea el estado del nodo para saber si hay que desregistrarlo o puede participar en la negociacion

                                                //Si esta caido el nodo hay que reiniciar las replicas que este albergaba. Se priorizan las replicas en running y las de tipo batch

                                                ACLMessage HEofDeadPnode= sendCommand(myAgent,"get pnodeagent"+hosting_node.getContent()+" attrib=HostedElements","D&D_"+convIDCounter++);
                                                String[] parts1=new String[1];
                                                if(HEofDeadPnode.getContent().contains(",")){
                                                    parts1=HEofDeadPnode.getContent().split(",");
                                                }else{
                                                    parts1[0]=HEofDeadPnode.getContent();
                                                }

                                                for(int i=0;i<parts1.length;i++){
                                                    ACLMessage Dead_SE= sendCommand(myAgent,"get * parent="+parts1[i]+" node="+hosting_node.getContent(),"D&D_"+convIDCounter++);
                                                    if(!Dead_SE.getContent().equals("")){
                                                        ACLMessage se_state= sendCommand(myAgent,"get "+Dead_SE.getContent()+" attrib=state","D&D_"+convIDCounter++);
                                                        HashMap<String,ArrayList<String>> agents_sorted_by_category=agents_sorted_by_state.get(se_state);
                                                        if(agents_sorted_by_category==null) {
                                                            agents_sorted_by_category=new HashMap<String,ArrayList<String>>();
                                                        }
                                                            if(Dead_SE.getContent().contains("batchagent")){
                                                                ArrayList<String>a=agents_sorted_by_category.get("batchagent");
                                                                if(a==null){
                                                                    a=new ArrayList<String>();
                                                                }
                                                                LOGGER.info(Dead_SE.getContent()+" found to be dead on node "+hosting_node.getContent());
                                                                sendACL(ACLMessage.INFORM, "QoSManagerAgent","reported_on_dead_node",Dead_SE.getContent());
                                                                a.add(Dead_SE.getContent());
                                                                agents_sorted_by_category.put("batchagent",a);
                                                            }else if(Dead_SE.getContent().contains("orderagent")){
                                                                ArrayList<String>a=agents_sorted_by_category.get("orderagent");
                                                                if(a==null){
                                                                    a=new ArrayList<String>();
                                                                }
                                                                a.add(Dead_SE.getContent());
                                                                LOGGER.info(Dead_SE.getContent()+" found to be dead on node "+hosting_node.getContent());
                                                                sendACL(ACLMessage.INFORM, "QoSManagerAgent","reported_on_dead_node",Dead_SE.getContent());
                                                                agents_sorted_by_category.put("orderagent",a);
                                                            }else{
                                                                ArrayList<String>a=agents_sorted_by_category.get("mplanagent");
                                                                if(a==null){
                                                                    a=new ArrayList<String>();
                                                                }
                                                                a.add(Dead_SE.getContent());
                                                                LOGGER.info(Dead_SE.getContent()+" found to be dead on node "+hosting_node.getContent());
                                                                sendACL(ACLMessage.INFORM, "QoSManagerAgent","reported_on_dead_node",Dead_SE.getContent());
                                                                agents_sorted_by_category.put("mplanagent",a);
                                                            }
                                                        agents_sorted_by_state.put(se_state.getContent(),agents_sorted_by_category);

                                                        //TODO avisar al QoS para que añada a su listado de caidos el agente

                                                    }else{
                                                        LOGGER.error("No system element found for node "+hosting_node.getContent()+" and parent "+parts1[i]);
                                                    }
                                                }
                                                sendCommand(myAgent, "del pnodeagent"+hosting_node.getContent(),"D&D_"+convIDCounter++); //ya se puede desregistrar del SMA el nodo

                                            }else{  //nodo no caido, pero replica sí
                                                ACLMessage hosted_elements=sendCommand(myAgent, "get "+"pnodeagent"+hosting_node.getContent()+" attrib=HostedElements", "D&D_"+convIDCounter);
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
                                                }  //Actualiza los atributos del nodo para que pueda participar en la negociacion
                                               sendCommand(myAgent, "set pnodeagent"+hosting_node.getContent()+" HostedElements="+new_HE, "D&D_"+convIDCounter);
                                            }

                                        if(!state.getContent().equals("tracking")){ //si no estaba en tracking estaría en running o bootToRunning
                                            tracking_to_running(parent.getContent());
                                        }else{
                                            restart_replica(parent.getContent()); //si esta en tracking simplemente recuperamos la replica si hay nodos disponibles
                                        }
                                        //Ahora hay que comprobar el listado si el nodo había caido.
                                        HashMap<String, ArrayList<String>> agents_in_running_state= agents_sorted_by_state.get("running"); //los agentes en running tienen prioridad
                                        if(agents_in_running_state!=null){
                                            ArrayList<String> dead_batchs=agents_in_running_state.get("batchagent"); //los agentes batch tienen prioridad
                                            if (dead_batchs != null) {
                                                for(int i=0; i<dead_batchs.size();i++){
                                                    ACLMessage Dead_SE_parent= sendCommand(myAgent,"get "+dead_batchs.get(i)+" attrib=parent","D&D_"+convIDCounter++);
                                                    sendCommand(myAgent, "del "+dead_batchs.get(i),"D&D_"+convIDCounter++);
                                                    tracking_to_running(Dead_SE_parent.getContent());
                                                }
                                                agents_in_running_state.remove("batchagent");
                                            }
                                            ArrayList<String> dead_orders=agents_in_running_state.get("orderagent");
                                            if (dead_orders != null) {
                                                for(int i=0; i<dead_orders.size();i++){
                                                    ACLMessage Dead_SE_parent= sendCommand(myAgent,"get "+dead_orders.get(i)+" attrib=parent","D&D_"+convIDCounter++);
                                                    sendCommand(myAgent, "del "+dead_orders.get(i),"D&D_"+convIDCounter++);
                                                    tracking_to_running(Dead_SE_parent.getContent());
                                                }
                                                agents_in_running_state.remove("orderagent");
                                            }
                                            ArrayList<String> dead_mplans=agents_in_running_state.get("mplanagent");
                                            if (dead_mplans != null) {
                                                for(int i=0; i<dead_mplans.size();i++){
                                                    ACLMessage Dead_SE_parent= sendCommand(myAgent,"get "+dead_mplans.get(i)+" attrib=parent","D&D_"+convIDCounter++);
                                                    sendCommand(myAgent, "del "+dead_mplans.get(i),"D&D_"+convIDCounter++);
                                                    tracking_to_running(Dead_SE_parent.getContent());
                                                }
                                                agents_in_running_state.remove("mplanagent");
                                            }
                                        }
                                        HashMap<String, ArrayList<String>> agents_in_tracking_state= agents_sorted_by_state.get("tracking");
                                        if(agents_in_tracking_state!=null){
                                            ArrayList<String> dead_batchs=agents_in_tracking_state.get("batchagent");
                                            if (dead_batchs != null) {
                                                for(int i=0; i<dead_batchs.size();i++){
                                                    ACLMessage Dead_SE_parent= sendCommand(myAgent,"get "+dead_batchs.get(i)+" attrib=parent","D&D_"+convIDCounter++);
                                                    restart_replica(Dead_SE_parent.getContent());
                                                }
                                                agents_in_tracking_state.remove("batchagent");
                                            }
                                            ArrayList<String> dead_orders=agents_in_tracking_state.get("orderagent");
                                            if (dead_orders != null) {
                                                for(int i=0; i<dead_orders.size();i++){
                                                    ACLMessage Dead_SE_parent= sendCommand(myAgent,"get "+dead_orders.get(i)+" attrib=parent","D&D_"+convIDCounter++);
                                                    restart_replica(Dead_SE_parent.getContent());
                                                }
                                                agents_in_tracking_state.remove("orderagent");
                                            }
                                            ArrayList<String> dead_mplans=agents_in_tracking_state.get("mplanagent");
                                            if (dead_mplans != null) {
                                                for(int i=0; i<dead_mplans.size();i++){
                                                    ACLMessage Dead_SE_parent= sendCommand(myAgent,"get "+dead_mplans.get(i)+" attrib=parent","D&D_"+convIDCounter++);
                                                    restart_replica(Dead_SE_parent.getContent());
                                                }
                                                agents_in_tracking_state.remove("mplanagent");
                                            }
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

                    } else if (msg.getOntology().equals("msg_lost")&&msg.getSender().getLocalName().equals("QoSManagerAgent")) {
                        LOGGER.warn("Inform operator: Message lost."+"\n"); //si se pierde un mensaje no se puede hacer nada. Se avisa al operador para que actue en consecuencia.

                        String[] msgparts=msg.getContent().split("/div/");
                        String msgreceiver = msgparts[0];
                        String intercepted_msg = msgparts[1];
                        System.out.println("Receiver: "+msgreceiver+"\n");
                        System.out.println("Content: "+intercepted_msg+"\n");

                        if(control.equals("automatic")){

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
                , 2000);

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
    private boolean tracking_to_running(String parent){
        LOGGER.info(parent+" needs a new running replica");
        ACLMessage tracking_instances= null;
        try {
            tracking_instances = sendCommand(myAgent, "get * state=tracking parent="+parent, "D&D_"+convIDCounter);

        String[] TrackingReplicas=new String[1];
        ACLMessage SetReplicasWFD=new ACLMessage(ACLMessage.REQUEST);
        SetReplicasWFD.setContent("setstate waitingfordecision");   //debemos poner en waiting for decision a las replicas en tracking primero
        SetReplicasWFD.setOntology("control");
        if(tracking_instances.getContent().contains(",")){
            TrackingReplicas=tracking_instances.getContent().split(",");
        }else{
            TrackingReplicas[0]=tracking_instances.getContent();
        }
        for(int i=0;i<TrackingReplicas.length;i++) {
            AID Replica=new AID(TrackingReplicas[i],false);
            SetReplicasWFD.addReceiver(Replica);
        }
        SetReplicasWFD.setConversationId("D&D_"+convIDCounter++);
        myAgent.send(SetReplicasWFD);

        String negotationdata="localneg "+tracking_instances.getContent()+ " criterion=CPU_usage action=restore externaldata=" + parent; //se lanza negociacion entre las replicas en tracking
        sendCommand(myAgent,negotationdata , "D&D_"+convIDCounter++);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    private boolean restart_replica(String parent){
        LOGGER.info(parent+" needs a new tracking replica");
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
                criteria="max mem"; //mismo criterio que al inicio del plan
                String negotationdata="localneg "+ToNegotiate+ " criterion="+criteria+" action=start externaldata=" + parent + "," + category.getContent() + "," + seClass + "," + myAgent.getLocalName() + "," + "1" + "," + "tracking";
                sendCommand(myAgent,negotationdata , "D&D_"+convIDCounter++);
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
