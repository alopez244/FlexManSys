package es.ehu.domain.manufacturing.utilities;
import es.ehu.platform.template.interfaces.DDInterface;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;


public class DiagnosisAndDecision extends ErrorHandlerAgent implements DDInterface {
//    private volatile AID QoSID = new AID("QoSManagerAgent", false);
    private int convIDCounter=1;
    static final Logger LOGGER = LogManager.getLogger(DiagnosisAndDecision.class.getName());
    private Agent myAgent=this;
    private String reported_agent="";
    public String control="automatic";
    private MessageTemplate expected_senders=MessageTemplate.or(MessageTemplate.MatchSender(new AID("planner",AID.ISLOCALNAME)),
                                            MessageTemplate.MatchSender(new AID("QoSManagerAgent",AID.ISLOCALNAME)));
    private MessageTemplate neg_template=MessageTemplate.and(MessageTemplate.MatchOntology("negotiation"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM));
    private HashMap<String,HashMap<String,ArrayList<String>>> agents_sorted_by_state=new HashMap<String,HashMap<String,ArrayList<String>>>();
    protected void setup(){
        LOGGER.entry();

        LOGGER.info("Diagnosis and Decision Agent started");
        addBehaviour(new DDEventManager() );
        LOGGER.exit();
    }

    class DDEventManager extends CyclicBehaviour {

        private String control="automatic";

        public void action() {

                ACLMessage negotiation_result=myAgent.receive(neg_template); //comprueba si ha
                if(negotiation_result!=null){
                    actions_after_negotiation(negotiation_result);
                }
                ACLMessage msg=myAgent.receive(expected_senders);  //solo lee mensajes de los agentes indicados en el template
                if(msg!=null) {
                    if (msg.getOntology().equals("not_found")) { //Se reporta un agente aislado o muerto
                        if(control.equals("automatic")){ //solo toma decisiones si está en modo automatico
                            actions_after_not_found(msg);
                        }else{
                            LOGGER.error(msg.getContent()+" is either dead or isolated.");
                            LOGGER.warn("MANUAL MODE: Cosider taking actions to solve the issue");
                        }
                    } else if (msg.getOntology().equals("msg_lost")) {
                        actions_after_msg_lost(msg);
                    } else if(msg.getOntology().equals("man/auto")){
                        change_DD_state(msg);
                    }
                }
            }
        }

    public void actions_after_negotiation(ACLMessage msg){
        LOGGER.info("New agent started: "+msg.getSender().getLocalName());
        String convID="negotiation_winner_";
        String winner=msg.getSender().getLocalName();
        if(winner.contains("batchagent")){
            LOGGER.info("New batch agent is in running state: "+winner);
            sendACL(7,winner,"restart_timeout","reset_timeout",myAgent); //si es batch debe resetear el timeout
            try {
                ACLMessage parent=sendCommand(myAgent,"get "+winner+" attrib=parent",convID+String.valueOf(convIDCounter++));
                String target=get_relationship(parent.getContent());
                sendACL(ACLMessage.INFORM,target,"release_buffer",winner,myAgent);
                restart_replica(parent.getContent()); //hay que generar una replica en tracking si es posible para mantener el numero de replicas constante
            } catch (Exception e) {
                e.printStackTrace();
            }
            get_timestamp(myAgent,msg.getSender().getLocalName(),"RunningAgentRecovery");
        }else if(winner.contains("orderagent")||winner.contains("mplanagent")){
            try {
                String[] category=winner.split("agent");
                LOGGER.info("New "+category[0]+" agent is in running state: "+winner);
                ACLMessage parent_of_dead_SE=sendCommand(myAgent,"get "+winner+" attrib=parent",convID+String.valueOf(convIDCounter++)); //parent del order o batch
                String son_category="";
                if(category[0].equals("order")){
                    son_category="batch";
                }else{
                    son_category="order";
                }

                ACLMessage parent_of_targets=sendCommand(myAgent,"get * parent="+parent_of_dead_SE.getContent()+" category="+son_category,convID+String.valueOf(convIDCounter)); //devuelve todos los batchs que colgaban de order
                String[] target_parents=new String[1];
                if(parent_of_targets.getContent().contains(",")){ //podría haber varios
                    target_parents=parent_of_targets.getContent().split(",");
                }else{
                    target_parents[0]=parent_of_targets.getContent();
                }
                for(int i=0; i<target_parents.length;i++){
                    ACLMessage target=sendCommand(myAgent,"get * parent="+target_parents[i]+" state=running",convID+String.valueOf(convIDCounter));
                    while(target.getContent().equals("")){ //hasta no tener la replica en running no podemos informarla
                        LOGGER.debug("Running replica not found for "+target_parents[i]+". Retrying.");
                        target=sendCommand(myAgent,"get * parent="+target_parents[i]+" state=running",convID+String.valueOf(convIDCounter));
                        Thread.sleep(1000);
                    }
                    sendACL(ACLMessage.INFORM,target.getContent(),"release_buffer",winner,myAgent);
                }
                restart_replica(parent_of_dead_SE.getContent()); //hay que generar una replica en tracking si es posible para mantener el numero de replicas constante

            } catch (Exception e) {
                e.printStackTrace();
            }
            get_timestamp(myAgent,msg.getSender().getLocalName(),"RunningAgentRecovery");
        }else {
            //TODO
        }
    }

    public void actions_after_not_found(ACLMessage msg){
        reported_agent= msg.getContent();
        if(!msg.getContent().contains("ControlGatewayCont")){ //no es un agente GW por lo que el D&D puede realizar alguna acción
            if(msg.getContent().contains("batchagent")||msg.getContent().contains("orderagent")||msg.getContent().contains("mplanagent")){ //Es agente de aplicacion
                try {

                    ACLMessage state= sendCommand(myAgent, "get "+msg.getContent()+" attrib=state", "D&D_"+convIDCounter++); //consigue el estado de la replcia caida
                    //TODO condicion de agente repetido
                    LOGGER.warn(msg.getContent()+" is dead or isolated and was in "+state.getContent()+" state.");
                    ACLMessage parent= sendCommand(myAgent, "get "+msg.getContent()+" attrib=parent", "D&D_"+convIDCounter++);

                    ACLMessage hosting_node=sendCommand(myAgent, "get "+msg.getContent()+" attrib=node", "D&D_"+convIDCounter++); //devuelve el número de nodo que hostea a la replica

                    if(!PingAgent("pnodeagent"+hosting_node.getContent(),myAgent)){  //checkea el estado del nodo para saber si hay que desregistrarlo o puede participar en la negociacion

                        //Si esta caido el nodo hay que reiniciar todas las replicas que este albergaba. Se priorizan las replicas en running y las de tipo batch

                        ACLMessage HEofDeadPnode= sendCommand(myAgent,"get pnodeagent"+hosting_node.getContent()+" attrib=HostedElements","D&D_"+convIDCounter++); //consigue los batch order y mplan
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

                                add_to_restart_queue(se_state.getContent(),Dead_SE.getContent(),hosting_node.getContent()); //añade a la lista ordenada los agentes caidos

                            }else{
                                LOGGER.error("No system element found for node "+hosting_node.getContent()+" and parent "+parts1[i]);
                            }
                        }
                        sendCommand(myAgent, "del pnodeagent"+hosting_node.getContent(),"D&D_"+convIDCounter++); //ya se puede desregistrar del SMA el nodo

                    }else{  //nodo no caido, pero replica sí
                        add_to_restart_queue(state.getContent(),msg.getContent(),null); //se añade a la lista para reiniciar agentes el denunciado
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

                    //Ahora hay que restaurar todos los agentes caidos, haya caido el nodo o no.
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
                        agents_sorted_by_state.remove("running"); //ya no quedan agentes en running por iniciar
                    }
                    HashMap<String, ArrayList<String>> agents_in_tracking_state= agents_sorted_by_state.get("tracking");
                    if(agents_in_tracking_state!=null){
                        ArrayList<String> dead_batchs=agents_in_tracking_state.get("batchagent");
                        if (dead_batchs != null) {
                            for(int i=0; i<dead_batchs.size();i++){
                                ACLMessage Dead_SE_parent= sendCommand(myAgent,"get "+dead_batchs.get(i)+" attrib=parent","D&D_"+convIDCounter++);
                                sendCommand(myAgent, "del "+dead_batchs.get(i),"D&D_"+convIDCounter++);
                                restart_replica(Dead_SE_parent.getContent());
                            }
                            agents_in_tracking_state.remove("batchagent");
                        }
                        ArrayList<String> dead_orders=agents_in_tracking_state.get("orderagent");
                        if (dead_orders != null) {
                            for(int i=0; i<dead_orders.size();i++){
                                ACLMessage Dead_SE_parent= sendCommand(myAgent,"get "+dead_orders.get(i)+" attrib=parent","D&D_"+convIDCounter++);
                                sendCommand(myAgent, "del "+dead_orders.get(i),"D&D_"+convIDCounter++);
                                restart_replica(Dead_SE_parent.getContent());
                            }
                            agents_in_tracking_state.remove("orderagent");
                        }
                        ArrayList<String> dead_mplans=agents_in_tracking_state.get("mplanagent");
                        if (dead_mplans != null) {
                            for(int i=0; i<dead_mplans.size();i++){
                                ACLMessage Dead_SE_parent= sendCommand(myAgent,"get "+dead_mplans.get(i)+" attrib=parent","D&D_"+convIDCounter++);
                                sendCommand(myAgent, "del "+dead_mplans.get(i),"D&D_"+convIDCounter++);
                                restart_replica(Dead_SE_parent.getContent());
                            }
                            agents_in_tracking_state.remove("mplanagent");
                        }
                        agents_sorted_by_state.remove("tracking");  //ya no quedan agentes en tracking por iniciar
                    }
//                agents_sorted_by_state.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(msg.getContent().contains("machine")){
                String batch=get_relationship(msg.getContent());
                //TODO poner a negociar otras maquinas para asumir el mando
            }

        }else { //agente GW no encontrado
            //TODO poner a negociar otras maquinas para asumir el mando del batch
        }
        reported_agent= "";
    }

    public void actions_after_msg_lost(ACLMessage msg){
        LOGGER.warn("Inform operator: Message lost."+"\n"); //si se pierde un mensaje no se puede hacer nada. Se avisa al operador para que actue en consecuencia.
        String[] msgparts=msg.getContent().split("/div/");
        String msgreceiver = msgparts[0];
        String intercepted_msg = msgparts[1];
        System.out.println("Receiver: "+msgreceiver+"\n");
        System.out.println("Content: "+intercepted_msg+"\n");
        LOGGER.warn("Consider taking actions to solve the issue if needed");
    }

    public void change_DD_state(ACLMessage msg){
        control=msg.getContent();
        LOGGER.info("Changed to "+control+" mode");
        sendACL(ACLMessage.CONFIRM,msg.getSender().getLocalName(),msg.getOntology(),"ack",myAgent);
    }

    private void add_to_restart_queue(String state, String Dead_SE, String Dead_PN){
        HashMap<String,ArrayList<String>> agents_sorted_by_category=agents_sorted_by_state.get(state);
        if(agents_sorted_by_category==null) {
            agents_sorted_by_category=new HashMap<String,ArrayList<String>>();
        }
        if(Dead_SE.contains("batchagent")){
            ArrayList<String>a=agents_sorted_by_category.get("batchagent");
            if(a==null){
                a=new ArrayList<String>();
            }
            if(Dead_PN!=null){
                LOGGER.info(Dead_SE+" found to be dead on node "+Dead_PN);
                if(!reported_agent.equals(Dead_SE)){
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    get_defined_timestamp(myAgent,Dead_SE,"DeadAgentDetection",timestamp);
                    get_defined_timestamp(myAgent,Dead_SE,"DeadAgentConfirmation",timestamp);
                    sendACL(ACLMessage.INFORM, "QoSManagerAgent","reported_on_dead_node",Dead_SE,myAgent);
                }
            }
            a.add(Dead_SE);
            agents_sorted_by_category.put("batchagent",a);
        }else if(Dead_SE.contains("orderagent")){
            ArrayList<String>a=agents_sorted_by_category.get("orderagent");
            if(a==null){
                a=new ArrayList<String>();
            }
            a.add(Dead_SE);
            if(Dead_PN!=null){
                LOGGER.info(Dead_SE+" found to be dead on node "+Dead_PN);
                if(!reported_agent.equals(Dead_SE)){
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    get_defined_timestamp(myAgent,Dead_SE,"DeadAgentDetection",timestamp);
                    get_defined_timestamp(myAgent,Dead_SE,"DeadAgentConfirmation",timestamp);
                    sendACL(ACLMessage.INFORM, "QoSManagerAgent","reported_on_dead_node",Dead_SE,myAgent);
                }
            }
            agents_sorted_by_category.put("orderagent",a);
        }else{
            ArrayList<String>a=agents_sorted_by_category.get("mplanagent");
            if(a==null){
                a=new ArrayList<String>();
            }
            a.add(Dead_SE);
            if(Dead_PN!=null){
                LOGGER.info(Dead_SE+" found to be dead on node "+Dead_PN);
                if(!reported_agent.equals(Dead_SE)){
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    get_defined_timestamp(myAgent,Dead_SE,"DeadAgentDetection",timestamp);
                    get_defined_timestamp(myAgent,Dead_SE,"DeadAgentConfirmation",timestamp);
                    sendACL(ACLMessage.INFORM, "QoSManagerAgent","reported_on_dead_node",Dead_SE,myAgent);
                }
            }
            agents_sorted_by_category.put("mplanagent",a);
        }
        agents_sorted_by_state.put(state,agents_sorted_by_category);
    }


    private String get_relationship(String agent){
        MessageTemplate t=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("askrelationship"));
        sendACL(16,"QoSManagerAgent" , "askrelationship", agent,myAgent);
        ACLMessage received_agent= myAgent.blockingReceive(t, 1000);
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
            if (tracking_instances != null) {
                if(tracking_instances.getContent().equals("")){
                    LOGGER.error("No tracking instances found for "+parent+". Start a node and then start manually the running replica.");
                }else{
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
                        AID Replica=new AID(TrackingReplicas[i],false);  //añade todas las replicas para la negociaciones
                        SetReplicasWFD.addReceiver(Replica);
                    }
                    SetReplicasWFD.setConversationId("D&D_"+convIDCounter++);
                    myAgent.send(SetReplicasWFD);
                    String negotationdata="localneg "+tracking_instances.getContent()+ " criterion=CPU_usage action=restore externaldata=" + parent; //se lanza negociacion entre las replicas en tracking
                    sendCommand(myAgent,negotationdata , "D&D_"+convIDCounter++);

                }
            }else{
                LOGGER.error("No tracking instances found");
            }

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
//                    LOGGER.debug(NegotiatingPnodes.get(i)+" is not valid because it already hosts "+parent);
                    NegotiatingPnodes.remove(i);
                    i--;
                }else{
//                    LOGGER.debug(NegotiatingPnodes.get(i)+" could host "+parent);
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
                LOGGER.warn("There is no node available to store a tracking replica");
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
                String negotationdata="localneg "+ToNegotiate+ " criterion="+criteria+" action=recover_tracking externaldata=" + parent + "," + category.getContent() + "," + seClass + "," + myAgent.getLocalName() + "," + "1" + "," + "tracking";
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


}
