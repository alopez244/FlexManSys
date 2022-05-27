package es.ehu.domain.manufacturing.utilities;

import es.ehu.platform.utilities.XMLReader;
import es.ehu.platform.template.interfaces.DDInterface;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;


public class DiagnosisAndDecision extends ErrorHandlerAgent implements DDInterface {

    private int convIDCounter=1;
    private String xmlplan="MPlan1.xml";
    static final Logger LOGGER = LogManager.getLogger(DiagnosisAndDecision.class.getName());
    private Agent myAgent=this;
    private String reported_agent="";
    public static final String DATE_FORMAT="yyyy-MM-dd'T'HH:mm:ss";
    public static final String TIME_FORMAT="HH:mm:ss";
    private SimpleDateFormat formatter_date = new SimpleDateFormat(DATE_FORMAT);
    private SimpleDateFormat formatter_time = new SimpleDateFormat(TIME_FORMAT);
    public String control="automatic"; //variable que conmutamos cuando la interfaz (planner) lo pide
    private MessageTemplate expected_senders=MessageTemplate.or(MessageTemplate.MatchSender(new AID("planner",AID.ISLOCALNAME)),
                                            MessageTemplate.MatchSender(new AID("QoSManagerAgent",AID.ISLOCALNAME)));
    private MessageTemplate neg_template=MessageTemplate.and(MessageTemplate.or(MessageTemplate.MatchOntology("redistributed_operations"),MessageTemplate.MatchOntology("restored_functionality")),
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

                ACLMessage negotiation_result=myAgent.receive(neg_template); //comprueba si alguna replica en tracking ha cambiado a running
                if(negotiation_result!=null) {
                    if (negotiation_result.getOntology().equals("restored_functionality")) {
                        recover_redundancy(negotiation_result); //si un agente en tracking a pasado a running, necesitamos recuperar ese agente en tracking para mantener la redundancia
                    }
                }
                ACLMessage msg=myAgent.receive(expected_senders);  //solo lee mensajes de los agentes indicados en el template
                if(msg!=null) {
                    if (msg.getOntology().equals("not_found")) { //Se reporta un agente aislado o muerto
                        if(control.equals("automatic")){ //solo toma decisiones si est� en modo automatico
                            actions_after_not_found(msg);
                        }else{
                            LOGGER.error(msg.getContent()+" is either dead or isolated.");
                            LOGGER.warn("MANUAL MODE: Consider taking actions to solve the issue");
                        }
                    } else if(msg.getOntology().equals("redistribute")){
                        redistribute_machine_operations(msg);
                    } else if (msg.getOntology().equals("msg_lost")) {
                        actions_after_msg_lost(msg);
                    } else if(msg.getOntology().equals("man/auto")){
                        change_DD_state(msg);
                    }else if(msg.getOntology().equals("plan_id")){
                        xmlplan=msg.getContent();
                    }
                }
            }
        }

    public void recover_redundancy(ACLMessage msg){
        LOGGER.info("New agent started: "+msg.getSender().getLocalName());
        String convID="negotiation_winner_";
        String winner=msg.getSender().getLocalName();
        if(winner.contains("batchagent")){
            LOGGER.info("New batch agent is in running state: "+winner);
            sendACL(7,winner,"restart_timeout","reset_timeout",myAgent); //si es batch debe resetear el timeout
            try {
                ACLMessage parent=sendCommand(myAgent,"get "+winner+" attrib=parent");
                ACLMessage reference = sendCommand(myAgent, "get " + parent.getContent() + " attrib=reference");

                ArrayList<String> targets=get_relationship(reference.getContent()); //devuelve el agente m�quina que cuelga del batch


                if(targets!=null){
                    for(int i=0;i<targets.size();i++){
                        sendACL(ACLMessage.INFORM,targets.get(i),"release_buffer",winner,myAgent); //pide al machine que vacie el buffer de mensajes retenidos
                    }
                }
                restart_replica(parent.getContent()); //hay que generar una replica en tracking si es posible para mantener el numero de replicas constante
            } catch (Exception e) {
                e.printStackTrace();
            }
//            get_timestamp(myAgent,msg.getSender().getLocalName(),"RunningAgentRecovery");
        }else if(winner.contains("orderagent")||winner.contains("mplanagent")){ //en caso de ser un order o mplan hay que buscar a los sons responsables de informar
            try {
                String[] category=winner.split("agent");
                LOGGER.info("New "+category[0]+" agent is in running state: "+winner);
                ACLMessage parent_of_dead_SE=sendCommand(myAgent,"get "+winner+" attrib=parent"); //parent del winner
                String son_category="";
                if(category[0].equals("order")){
                    son_category="batch";
                }else{
                    son_category="order";
                }

                ACLMessage parent_of_targets=sendCommand(myAgent,"get * parent="+parent_of_dead_SE.getContent()+" category="+son_category); //devuelve todos los batchs que colgaban de order o los order que colgaban del mplan
                String[] target_parents=new String[1];
                if(parent_of_targets.getContent().contains(",")){ //podr�a haber varios y todos necesitar�n vaciar el buffer
                    target_parents=parent_of_targets.getContent().split(",");
                }else{
                    target_parents[0]=parent_of_targets.getContent();
                }
                for(int i=0; i<target_parents.length;i++){
                    ACLMessage target=sendCommand(myAgent,"get * parent="+target_parents[i]+" state=running");
                    while(target.getContent().equals("")){ // es posible no tener una replica en running disponible por lo que hay que esperar hasta poder informarla
                        LOGGER.debug("Running replica not found for "+target_parents[i]+". Retrying.");
                        target=sendCommand(myAgent,"get * parent="+target_parents[i]+" state=running");
                        Thread.sleep(1000);
                    }
                    sendACL(ACLMessage.INFORM,target.getContent(),"release_buffer",winner,myAgent);
                }
                restart_replica(parent_of_dead_SE.getContent()); //hay que generar una replica en tracking si es posible para mantener el numero de replicas constante

            } catch (Exception e) {
                e.printStackTrace();
            }
//            get_timestamp(myAgent,msg.getSender().getLocalName(),"RunningAgentRecovery");
        }else {
            //TODO
        }
    }

    public void actions_after_not_found(ACLMessage msg){
        reported_agent= msg.getContent();

            if(msg.getContent().contains("batchagent")||msg.getContent().contains("orderagent")||msg.getContent().contains("mplanagent")){ //Es agente de aplicacion
                try {

                    ACLMessage state= sendCommand(myAgent, "get "+msg.getContent()+" attrib=state"); //consigue el estado de la replcia caida
                    if(state.getContent().equals("")){
                        LOGGER.warn(msg.getContent()+" reported by QoS but no data available. Already solved."); //si el QoS denuncia un agente ya detectado mientras se ha estado trabajando en ello no se debe hacer nada
                    }else{
                        LOGGER.warn(msg.getContent()+" is dead or isolated and it was in "+state.getContent()+" state.");
                        ACLMessage parent= sendCommand(myAgent, "get "+msg.getContent()+" attrib=parent");

                        ACLMessage hosting_node=sendCommand(myAgent, "get "+msg.getContent()+" attrib=node"); //devuelve el n�mero de nodo que hostea a la replica

                        if(!PingAgent("pnodeagent"+hosting_node.getContent(),myAgent)){  //checkea el estado del nodo para saber si hay que desregistrarlo o puede participar en la negociacion

                            //Si esta caido el nodo se aprovecha para restaurar todas las replicas que este hosteaba

                            ACLMessage HEofDeadPnode= sendCommand(myAgent,"get pnodeagent"+hosting_node.getContent()+" attrib=HostedElements"); //consigue los parent de los agentes hosteados
                            String[] parts1=new String[1];
                            if(HEofDeadPnode.getContent().contains(",")){ //si son varios
                                parts1=HEofDeadPnode.getContent().split(",");
                            }else{
                                parts1[0]=HEofDeadPnode.getContent();
                            }

                            for(int i=0;i<parts1.length;i++){ //comprobamos de cada parent que estaba registrado de que agentes se trataba
                                ACLMessage Dead_SE= sendCommand(myAgent,"get * parent="+parts1[i]+" node="+hosting_node.getContent());
                                if(!Dead_SE.getContent().equals("")){
                                    ACLMessage se_state= sendCommand(myAgent,"get "+Dead_SE.getContent()+" attrib=state");
                                    add_to_restart_queue(se_state.getContent(),Dead_SE.getContent(),hosting_node.getContent()); //a�ade a la lista los agentes caido. pej:

                                }else{
                                    LOGGER.error("No system element found for node "+hosting_node.getContent()+" and parent "+parts1[i]);
                                }
                            }
                            sendCommand(myAgent, "del pnodeagent"+hosting_node.getContent()); //ya se puede desregistrar del SMA el nodo para que no participe en la negociacion

                        }else{  //nodo no caido, pero replica s�. Posible fallo de agente
                            //No tendr�a sentido intentar "matar" al agente en fallo porque no responder�a a un setstate stop.
                            //Simplemente lo borramos del sistema para que el resto de agetes lo ignore
                            LOGGER.info("Posible malfunction of agent. Isolating it from the system. Node can still be used");
                            add_to_restart_queue(state.getContent(),msg.getContent(),null); //se a�ade a la lista para reiniciar agentes el denunciado
                            ACLMessage hosted_elements=sendCommand(myAgent, "get "+"pnodeagent"+hosting_node.getContent()+" attrib=HostedElements");
                            String[] HE=new String[1];
                            if(hosted_elements.getContent().contains(",")){
                                HE=hosted_elements.getContent().split(",");
                            }else{
                                HE[0]=hosted_elements.getContent();
                            }
                            String new_HE=""; //se actualiza el atributo HostedElements
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
                            }  //Actualiza los atributos del nodo, eliminando el agente ca�do, para que pueda participar en la negociacion
                            sendCommand(myAgent, "set pnodeagent"+hosting_node.getContent()+" HostedElements="+new_HE);
                        }

                        //Ahora hay que restaurar todos los agentes caidos, haya fallado el nodo o no. Se corregir� siguiendo la lista de prioridad:
                        //1) Batch running
                        //2) Order running
                        //3) Mplan running
                        //4) Batch tracking
                        //5) Order tracking
                        //6) Mplan tracking

                        HashMap<String, ArrayList<String>> agents_in_running_state= agents_sorted_by_state.get("running"); //empezamos por los agentes denunciados que estaban el running
                        if(agents_in_running_state!=null){
                            ArrayList<String> dead_batchs=agents_in_running_state.get("batchagent");
                            if (dead_batchs != null) {
                                for(int i=0; i<dead_batchs.size();i++){
                                    ACLMessage Dead_SE_parent= sendCommand(myAgent,"get "+dead_batchs.get(i)+" attrib=parent");
                                    sendCommand(myAgent, "del "+dead_batchs.get(i)); //llegado este punto ya se puede desregistrar el agente
                                    tracking_to_running(Dead_SE_parent.getContent());
                                }
                                agents_in_running_state.remove("batchagent"); //elimina de la lista de pendientes
                            }
                            ArrayList<String> dead_orders=agents_in_running_state.get("orderagent");
                            if (dead_orders != null) {
                                for(int i=0; i<dead_orders.size();i++){
                                    ACLMessage Dead_SE_parent= sendCommand(myAgent,"get "+dead_orders.get(i)+" attrib=parent");
                                    sendCommand(myAgent, "del "+dead_orders.get(i));
                                    tracking_to_running(Dead_SE_parent.getContent());
                                }
                                agents_in_running_state.remove("orderagent");
                            }
                            ArrayList<String> dead_mplans=agents_in_running_state.get("mplanagent");
                            if (dead_mplans != null) {
                                for(int i=0; i<dead_mplans.size();i++){
                                    ACLMessage Dead_SE_parent= sendCommand(myAgent,"get "+dead_mplans.get(i)+" attrib=parent");
                                    sendCommand(myAgent, "del "+dead_mplans.get(i));
                                    tracking_to_running(Dead_SE_parent.getContent());
                                }
                                agents_in_running_state.remove("mplanagent");
                            }
                            agents_sorted_by_state.remove("running"); //ya no quedan agentes en running por iniciar
                        }
                        HashMap<String, ArrayList<String>> agents_in_tracking_state= agents_sorted_by_state.get("tracking"); //tras recuperar los agentes prioritarios pasamos a recuperar los agentes en tracking
                        if(agents_in_tracking_state!=null){
                            ArrayList<String> dead_batchs=agents_in_tracking_state.get("batchagent");
                            if (dead_batchs != null) {
                                for(int i=0; i<dead_batchs.size();i++){
                                    ACLMessage Dead_SE_parent= sendCommand(myAgent,"get "+dead_batchs.get(i)+" attrib=parent");
                                    sendCommand(myAgent, "del "+dead_batchs.get(i));
                                    restart_replica(Dead_SE_parent.getContent());
                                }
                                agents_in_tracking_state.remove("batchagent");
                            }
                            ArrayList<String> dead_orders=agents_in_tracking_state.get("orderagent");
                            if (dead_orders != null) {
                                for(int i=0; i<dead_orders.size();i++){
                                    ACLMessage Dead_SE_parent= sendCommand(myAgent,"get "+dead_orders.get(i)+" attrib=parent");
                                    sendCommand(myAgent, "del "+dead_orders.get(i));
                                    restart_replica(Dead_SE_parent.getContent());
                                }
                                agents_in_tracking_state.remove("orderagent");
                            }
                            ArrayList<String> dead_mplans=agents_in_tracking_state.get("mplanagent");
                            if (dead_mplans != null) {
                                for(int i=0; i<dead_mplans.size();i++){
                                    ACLMessage Dead_SE_parent= sendCommand(myAgent,"get "+dead_mplans.get(i)+" attrib=parent");
                                    sendCommand(myAgent, "del "+dead_mplans.get(i));
                                    restart_replica(Dead_SE_parent.getContent());
                                }
                                agents_in_tracking_state.remove("mplanagent");
                            }
                            agents_sorted_by_state.remove("tracking");  //ya no quedan agentes en tracking por iniciar
                        }
                    }

//                agents_sorted_by_state.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        reported_agent= "";
    }

    public void redistribute_machine_operations(ACLMessage msg){
        String[] inf=msg.getContent().split("/");
        String lost_machine=inf[0];
        String lost_machine_id=inf[1];
        String batch=inf[2];
        String item=inf[3];

        try { //a la hora de rehacer el timeout para el batch, si no eliminamos el timeout del order previamente, este se duplicar�
            ACLMessage batch_parent = sendCommand(myAgent, "get * category=batch reference=" + batch);
            ACLMessage order_parent = sendCommand(myAgent, "get "+batch_parent.getContent()+" attrib=parent");
            ACLMessage orderagent_running = sendCommand(myAgent, "get * category=orderAgent state=running parent=" + order_parent.getContent());
            sendACL(ACLMessage.INFORM,orderagent_running.getContent(),"take_down_order_timeout",batch,myAgent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        LOGGER.warn(lost_machine+" operations must be redistributed. "+batch+" stoped on item "+item);
        String appPath="classes/resources/AppInstances/";

        XMLReader fileReader = new XMLReader();
        String uri=appPath+xmlplan;
        String new_operations="";
        ArrayList<ArrayList<ArrayList<String>>> xmlelements = fileReader.readFile(uri);
        int batch_position=0;
        ArrayList<String> needed_operations=new ArrayList<String >();
        if(!item.equals("?")){    //podemos saber o no en que item se ha parado la m�quina
            //si sabemos en que item se qued�, rehacemos el plan a partir del item
            boolean first_item_found=false;
            for(int i=0;i<xmlelements.size();i++){
                if(xmlelements.get(i).get(0).get(0).equals("batch")&&xmlelements.get(i).get(3).get(2).equals(batch)){
                    batch_position=i;
                    break;
                }
            }
            for(int i=batch_position+1;i<xmlelements.size()&&!xmlelements.get(i).get(0).get(0).equals("batch");i++){
                if(xmlelements.get(i).get(0).get(0).equals("PlannedItem")){
                    if(xmlelements.get(i).get(3).get(1).equals(item)||first_item_found){  //solo queremos a�adir los items a partir del timeout
                        for(int j=i+1;j<xmlelements.size()&&!xmlelements.get(j).get(0).get(0).equals("PlannedItem");j++){
                            if(xmlelements.get(j).get(0).get(0).contains("Operation")&&xmlelements.get(j).get(3).get(3).equals(lost_machine_id)){
                                if(!needed_operations.contains(xmlelements.get(j).get(3).get(1))){
                                    needed_operations.add(xmlelements.get(j).get(3).get(1));  //a�ade al listado las operaciones que vamos a necesitar hacer para comprobra la compatibilidad entre maquinas
                                }
                                new_operations=new_operations+ "id*"+xmlelements.get(j).get(3).get(1)+" plannedFinishTime*"+xmlelements.get(j).get(3).get(2)+ " plannedStartTime*"+xmlelements.get(j).get(3).get(4)+ " batch_ID*"+xmlelements.get(i).get(3).get(0)+" item_ID*"+xmlelements.get(i).get(3).get(1)+" order_ID*"+xmlelements.get(i).get(3).get(2)+" productType*"+xmlelements.get(i).get(3).get(3)+"&"; //hay que codificar los "=" como otro caracter para evitar malinterpretaciones por parte del SMA y del mensaje de negociacion
                            }
                        }
                        first_item_found=true;
                    }
                }
            }
        }else{ //si no sabemos en que item se ha quedado, rehacemos el lote por completo
            for(int i=0;i<xmlelements.size();i++){
                if(xmlelements.get(i).get(0).get(0).equals("batch")&&xmlelements.get(i).get(3).get(2).equals(batch)){
                    batch_position=i;
                    break;
                }
            }
            for(int i=batch_position+1;i<xmlelements.size()&&!xmlelements.get(i).get(0).get(0).equals("batch");i++){
                if(xmlelements.get(i).get(0).get(0).equals("PlannedItem")){
                    for(int j=i+1;j<xmlelements.size()&&!xmlelements.get(j).get(0).get(0).equals("PlannedItem");j++){
                        if(xmlelements.get(j).get(0).get(0).contains("Operation")&&xmlelements.get(j).get(3).get(3).equals(lost_machine_id)){
                            if(!needed_operations.contains(xmlelements.get(j).get(3).get(1))){
                                needed_operations.add(xmlelements.get(j).get(3).get(1));
                            }
                            //el finish time es provisional, del plan original. El real depender� de qu� m�quina asuma la negociaci�n
                            new_operations=new_operations+ "id*"+xmlelements.get(j).get(3).get(1)+" plannedFinishTime*"+xmlelements.get(j).get(3).get(2)+ " plannedStartTime*"+xmlelements.get(j).get(3).get(4)+ " batch_ID*"+xmlelements.get(i).get(3).get(0)+" item_ID*"+xmlelements.get(i).get(3).get(1)+" order_ID*"+xmlelements.get(i).get(3).get(2)+" productType*"+xmlelements.get(i).get(3).get(3)+"&";//hay que codificar los "=" como otro caracter para evitar malinterpretaciones por parte del SMA y del mensaje de negociacion
                        }
                    }
                }
            }
        }
        //operaciones listas para a�adirlas como external data en la negociaci�n

        //Ahora hay que checkear que m�quinas pueden participar
        ArrayList<String> participating_machines=new ArrayList<String >();
        try {
            String[] machine_list=new String[1];
            ACLMessage machines= sendCommand(myAgent,"get * category=machine");
            if(machines.getContent().contains(",")){
                machine_list=machines.getContent().split(",");
            }else{
                machine_list[0]=machines.getContent();
            }
            String OPL="";
            for(int i=0;i<machine_list.length;i++){ //comprueba que m�quinas pueden participar en la negociaci�n
                ArrayList<String>this_machine_op_list=new ArrayList<String>();
                ArrayList<String>temp=needed_operations;
                ACLMessage operationList= sendCommand(myAgent,"get "+machine_list[i]+" attrib=simpleOperations");
                OPL=operationList.getContent();
                String[] splited_OPs=new String[1];
                if(operationList.getContent().contains("complexOperations")){
                    splited_OPs=operationList.getContent().split("complexOperations");
                }else{
                    splited_OPs[0]=operationList.getContent();
                }
                String[] OP_list=new String[1];
                if(splited_OPs[0].contains(",")){
                    OP_list=splited_OPs[0].split(",");
                }else{
                    OP_list[0]=splited_OPs[0];
                }
                for(int j=0;j<OP_list.length;j++){ //crea un listado de operaciones para la maquina
                    this_machine_op_list.add(OP_list[j]);
                }
                if(this_machine_op_list.containsAll(temp)){
                    if(!machine_list[i].equals(lost_machine)){
                        participating_machines.add(machine_list[i]);
                        System.out.println(machine_list[i]+" could potentially take over operations "+splited_OPs[0]);
                    }
                }
            }
            if(new_operations.equals("")){
                LOGGER.error("New operations could not be found on XML");
            }else{
                if(participating_machines.size()>0){
                    String targets="";
                    for(int i=0;i<participating_machines.size();i++){
                        targets=targets+participating_machines.get(i)+",";
                    }
                    String negotationdata="localneg "+targets+ " criterion=finish_time action=execute externaldata=" + new_operations;
                    LOGGER.debug(new_operations);
                    get_timestamp(myAgent,targets,"NegotiationStart");
                    sendCommand(myAgent,negotationdata);
                    sendCommand(myAgent,"del "+lost_machine); //eliminamos la m�quina del SMA para que no participe en negociaciones futuras
                    LOGGER.info(lost_machine+" has been erased from system");
                }else{
                    LOGGER.warn("No machines available to take over operations "+OPL);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void add_to_restart_queue(String state, String Dead_SE, String Dead_PN){ //se va a ejecutar una vez por cada agente detectado en el nodo muerto
        //Ejemplo de estructura del hashmap:
        //Running ->  batchagent -> batchagent1
        //                          batchagent10
        //            orderagent -> orderagent1
        //            mplanagent -> mplanagent1
        //Tracking -> batchagent -> batchagent2
        //                          batchagent3
        //            orderagent -> orderagent2
        //            mplanagent -> mplanagent2

        HashMap<String,ArrayList<String>> agents_sorted_by_category=agents_sorted_by_state.get(state);
        if(agents_sorted_by_category==null) {
            agents_sorted_by_category=new HashMap<String,ArrayList<String>>();
        }
        if(Dead_SE.contains("batchagent")){
            ArrayList<String>a=agents_sorted_by_category.get("batchagent");
            if(a==null){
                a=new ArrayList<String>();
            }
            if(Dead_PN!=null){ //en caso de que el nodo este muerto no sera nulo el valor de la variable
                LOGGER.info(Dead_SE+" found to be dead on node "+Dead_PN);
                if(!reported_agent.equals(Dead_SE)){  //si no se traya del agente denunciado originalmente por el QoS hace falta avisarle
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    get_defined_timestamp(myAgent,Dead_SE,"DeadAgentDetection",timestamp);
                    get_defined_timestamp(myAgent,Dead_SE,"DeadAgentConfirmation",timestamp);
                    sendACL(ACLMessage.INFORM, "QoSManagerAgent","reported_on_dead_node",Dead_SE,myAgent); //se avisa al QoS por si otro agente se lo denuncia
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


    private ArrayList<String> get_relationship(String agent){ //obtiene la relacion entre agente m�quina y batch. TODO Har�a falta un sistema mejor.
        MessageTemplate t=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("askrelationship"));
        sendACL(ACLMessage.REQUEST,"QoSManagerAgent" , "askrelationship", agent,myAgent);
        ACLMessage received_agent= myAgent.blockingReceive(t, 1000);
        if(received_agent!=null){
            LOGGER.info(received_agent.getContent()+" is/are assigned to agent "+ agent);
            String[] agents=received_agent.getContent().split("/");
            ArrayList<String> relationed_agents=new ArrayList<String>();
            for(int i=0;i<agents.length;i++){
                if(!agents[i].equals("")){
                    relationed_agents.add(agents[i]);
                }
            }

            return relationed_agents;
        }else{
            LOGGER.error("QoSManagerAgent did not answer on time");
            return null;
        }
    }
    private boolean tracking_to_running(String parent){
        LOGGER.info(parent+" needs a new running replica");
        ACLMessage tracking_instances= null;
        try {  //necesitamos obtener las replicas en tracking asignadas a un parent para la negociaci�n
            tracking_instances = sendCommand(myAgent, "get * state=tracking parent="+parent);
            if (tracking_instances != null) {
                if(tracking_instances.getContent().equals("")){
                    LOGGER.error("No tracking instances found for "+parent+". Traceability lost");
                }else{
                    String[] TrackingReplicas=new String[1];
                    ACLMessage SetReplicasWFD=new ACLMessage(ACLMessage.REQUEST);
                    SetReplicasWFD.setContent("setstate waitingfordecision");   //debemos poner en waiting for decision a las replicas en tracking antes de mandarlas negociar
                    SetReplicasWFD.setOntology("control");
                    if(tracking_instances.getContent().contains(",")){
                        TrackingReplicas=tracking_instances.getContent().split(",");
                    }else{
                        TrackingReplicas[0]=tracking_instances.getContent();
                    }
                    for(int i=0;i<TrackingReplicas.length;i++) {
                        AID Replica=new AID(TrackingReplicas[i],false);  //a�ade todas las replicas para la negociaciones
                        SetReplicasWFD.addReceiver(Replica);
                    }
                    SetReplicasWFD.setConversationId("D&D_"+convIDCounter++);
                    myAgent.send(SetReplicasWFD);
                    String negotationdata="localneg "+tracking_instances.getContent()+ " criterion=CPU_usage action=restore externaldata=" + parent; //se lanza negociacion entre las replicas en waiting for decision
                    sendCommand(myAgent,negotationdata);
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
            ACLMessage process_nodes = sendCommand(myAgent, "get * category=pNodeAgent");

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
                ACLMessage valid_nodes  = sendCommand(myAgent, "get "+NegotiatingPnodes.get(i)+ " attrib=HostedElements");
                LOGGER.info(NegotiatingPnodes.get(i)+" hosts "+valid_nodes.getContent());
                if(valid_nodes.getContent().contains(parent)){
                    NegotiatingPnodes.remove(i);  //este nodo ya alberga alguna replica de este parent. Se excluye de la negociaci�n porque no tendria sentido inciarlo aqui.
                    i--;
                }else{
                    LOGGER.debug(NegotiatingPnodes.get(i)+" could host a tracking replica from "+parent);
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

            if(ToNegotiate.equals("")){
                LOGGER.warn("There is no node available to store a tracking replica"); //si todos los nodos est�n ocupados no se hace nada
                return false;
            }else{
                LOGGER.info("Participating nodes: "+ToNegotiate);
                ACLMessage category = sendCommand(myAgent, "get " + parent + " attrib=category");
                String seClass="";
                if(parent.contains("batch")){ //se elige la clase del agente a iniciar
                    seClass="es.ehu.domain.manufacturing.agents.BatchAgent";
                }else if(parent.contains("order")){
                    seClass="es.ehu.domain.manufacturing.agents.OrderAgent";
                }else{
                    seClass="es.ehu.domain.manufacturing.agents.MPlanAgent";
                }
                String criteria="";
                criteria="max mem"; //mismo criterio que al inicio del plan, seg�n memoria
                String negotationdata="localneg "+ToNegotiate+ " criterion="+criteria+" action=recover_tracking externaldata=" + parent + "," + category.getContent() + "," + seClass + "," + myAgent.getLocalName() + "," + "1" + "," + "tracking";
                sendCommand(myAgent,negotationdata);
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
