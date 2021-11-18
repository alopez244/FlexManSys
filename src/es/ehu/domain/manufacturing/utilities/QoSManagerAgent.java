package es.ehu.domain.manufacturing.utilities;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.AMSService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.text.DateFormat;

public class QoSManagerAgent extends Agent {

    private Agent myAgent=this;
    private int agent_found_qty=0;
    private int gateway_found_qty=0;
    private ArrayList<String> ActualBatch = new ArrayList<String>();
    private ArrayList<ArrayList<String>> ErrorList=new ArrayList<ArrayList<String>>();
    private int i=0;
    private ArrayList<ArrayList<String>> allDelays = new ArrayList<ArrayList<String>>();
    private int j=0,n=0;
    private int l=0;
    private ArrayList<ArrayList<String>> batch_and_machine = new ArrayList<ArrayList<String>>();
    private ArrayList<String> delay_asking_queue=new ArrayList<String>();
    private ACLMessage machinenbr=null;
    static final Logger LOGGER = LogManager.getLogger(QoSManagerAgent.class.getName());
    private boolean add_timeout_error_flag=true,pong=false,add_communication_error_flag=true,confirmed_isol=false;
    private String argument1="",argument2="",argument3="",argument4="",argument5="",argument6="",now="";
    public boolean automatic=true;
    public MessageTemplate pingtemplate=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchOntology("ping"));

    protected void setup(){
        LOGGER.entry();
        LOGGER.info("QoS manager started");
        addBehaviour(new QoS() );
        LOGGER.exit();
    }

    class QoS extends CyclicBehaviour{

        public void action(){
            LOGGER.entry();

            ACLMessage msg = blockingReceive();
            if(msg!=null){
                if(msg.getPerformative()==ACLMessage.FAILURE&&!msg.getSender().getLocalName().equals("ams")){ //Se recibe un posible error no perteneciente al ams
                    if(msg.getOntology().equals("acl_error")) { //error de tipo comunicación
                        String[] msgparts = msg.getContent().split("/div/");
                        String performative = msgparts[0];
                        String ontology = msgparts[1];
                        String convID = msgparts[2];
                        String receiver = msgparts[3];
                        String intercepted_msg = msgparts[4];
                        LOGGER.warn(msg.getSender().getLocalName() + " reported a failure while trying to communicate with " + receiver);
                        if (CheckNotFoundRegistry(receiver) && CheckNotFoundRegistry(msg.getSender().getLocalName())) { //comprueba que el denunciante y el denunciado no esten en la blacklist de agentes no encontrados
                            LOGGER.info("Checking if " + receiver + " is alive and if the agent received the reported msg.");
                            String command = CheckMsgFIFO(receiver, intercepted_msg); //checkMsgFIFo se puede usar con order, batch y machine por ahora, el resto responde como un ping
                            System.out.println(command);
                            n = SearchAgent(receiver);
                            if (command.equals("msg_lost") && n == 1) {         //Receiver vivo pero mensaje perdido
                                sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), msg.getOntology(), receiver + "/confirmed");
                                pong = PingAgent(msg.getSender().getLocalName());
                                n = SearchAgent(msg.getSender().getLocalName());
                                if (!pong || n != 1) { //checks if the reporting agent is actually isolated.
                                    LOGGER.error(msg.getSender().getLocalName() + " agent who reported the error is isolated");
                                    sendACL(ACLMessage.INFORM, "D&D", "msg_lost", msg.getContent());
                                    String msgtoDD = msg.getSender().getLocalName();
                                    if (msg.getSender().getLocalName().contains("ControlGatewayCont")) { //si el denunciante es el GW necesitamos saber su MA para que el D&D lo pase a idle
                                        msgtoDD = msgtoDD + "/div/" + receiver;
                                    }
                                    add_to_error_list("not_found", msg.getSender().getLocalName(), "", "", "");
                                    sendACL(ACLMessage.INFORM, "D&D", "not_found", msgtoDD);
                                } else {
                                    LOGGER.info("Receiver and sender are online, although message is lost. Bridge by D&D.");
                                    sendACL(ACLMessage.INFORM, "D&D", "msg_lost", msg.getContent());
                                }
                            } else if (command.equals("msg_received") && n == 1) {
                                sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), msg.getOntology(), receiver + "/ignore");
                                LOGGER.info("Message arrived to agent. Ignore error.");

                            } else { //si no responde el receiver, se trata de un agente aislado o muerto
                                LOGGER.warn("No answer from " + receiver + ". Confirming error.");

                                sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), msg.getOntology(), receiver + "/confirmed");
                                String msgtoDD = receiver;
                                if (receiver.contains("ControlGatewayCont")) {  //en algunos casos necesitamos saber el agente que ha denunciado el fallo para que pase a idle a traves del D&D
                                    msgtoDD = msgtoDD + "/div/" + msg.getSender().getLocalName();
                                }
                                add_to_error_list("not_found", receiver, "", "", "");
                                sendACL(ACLMessage.INFORM, "D&D", "not_found", msgtoDD);
                            }
                            add_to_error_list("communication", msg.getSender().getLocalName(), receiver, intercepted_msg, "");
                        }
                    } else if(msg.getOntology().equals("ctrlbhv_failure")){ //error redundante para casos no contemplados por acknowledge. Aporta menos información
                        LOGGER.warn(msg.getSender().getLocalName()+ " reported a communication failure with "+msg.getContent());
                        if(CheckNotFoundRegistry(msg.getContent())){
                            boolean pong=PingAgent(msg.getContent());
                            if(!pong){
                                add_to_error_list("not_found", msg.getContent(), "", "", "");
                                sendACL(ACLMessage.INFORM, "D&D", "not_found", msg.getContent());
                            }else{
                                LOGGER.info(msg.getContent()+ " did answer to ping.");
                                if(msg.getContent().contains("batchagent")||msg.getContent().contains("orderagent")||msg.getContent().contains("mplanagent")){
                                    String n[]=msg.getContent().split("agent");
                                    if(msg.getSender().getLocalName().contains(n[0]+"agent")){ // si llegamos aquí se trata de una replica en modo tracking que habría que restaurar
                                        sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), "restore_replica", msg.getContent());
                                    }
                                }
                            }
                        }

                    } else if(msg.getOntology().equals("timeout")){ //error de tipo timeout

                        if(msg.getSender().getLocalName().contains("batch")){ //timeout enviado por un batch
                            add_timeout_error_flag=true;
                            String[] parts=msg.getContent().split("/");
                            String timeout_batch_id=parts[0];
                            String timeout_item_id=parts[1];
                            for(int m=0; m<ErrorList.size();m++) { //Primero comprueba que el error no esté repetido
                                if (ErrorList.get(m).get(0).equals("timeout")) {
                                    if (ErrorList.get(m).get(2).equals(timeout_batch_id)) {
                                        if(ErrorList.get(m).get(3).equals(timeout_item_id)){
                                            LOGGER.error("Timeout repeated on same batch and item, confirming failure.");
                                            sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),msg.getOntology(),"confirmed_timeout");
                                            add_timeout_error_flag=false;
                                            for (int p = 0; p < batch_and_machine.size(); p++) {
                                                if (batch_and_machine.get(p).get(0).equals(timeout_batch_id)) {
                                                    String MA = batch_and_machine.get(p).get(1);
                                                    if(CheckNotFoundRegistry(MA)){ //comprueba que el agente maquina no este en la blacklist de agentes no encontrados
                                                        sendACL(ACLMessage.INFORM,"D&D","timeout",MA); //Se informa al D&D para que el agente máquina pase a idle.
                                                    }else{
                                                        LOGGER.warn(MA+" is on not found registry. Ignoring error.");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if(add_timeout_error_flag) {
                                argument1="timeout";
                                LOGGER.warn(timeout_batch_id + " batch has thrown a timeout on item " + timeout_item_id);
                                if (batch_and_machine.size() > 0) { //buscamos el batch en el listado y conseguimos el ID del machine agent responsable
                                    for (int k = 0; k < batch_and_machine.size(); k++) {
                                        if (batch_and_machine.get(k).get(0).equals(timeout_batch_id)) {
                                            String MA = batch_and_machine.get(k).get(1);
                                            argument2 = timeout_batch_id;
                                            argument3 = timeout_item_id;
                                            if (CheckNotFoundRegistry(MA)) {
                                                pong = PingAgent(MA); // hacemos ping al agente máquina para comprobar su estado
                                                int found = SearchAgent(MA); //Buscamos a su vez que el agente gateway este vivo.
                                                if (found == 1 && pong) {  //Si pong es 1 el agente ha contestado
                                                    argument4 = MA + "->OK";
                                                } else {
                                                    argument4 = MA + "->NO OK";
                                                    LOGGER.error("Error while trying to communicate with machine agent");
                                                    add_to_error_list("not_found", MA, "", "", "");
                                                    sendACL(ACLMessage.INFORM, "D&D", "not_found", MA);
                                                }
                                                try {
                                                    machinenbr = sendCommand(myAgent, "get " + MA + " attrib=id", "name"); //consultamos la id de la estacion al SA para saber que gateway agent le corresponde. Si es 41 seria ControlGatewayCont4.
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                char[] ch = new char[machinenbr.getContent().length()];
                                                ch[0] = machinenbr.getContent().charAt(0);
                                                if (CheckNotFoundRegistry("ControlGatewayCont" + ch[0])) {
                                                    pong = PingAgent("ControlGatewayCont" + ch[0]); //comprobamos si responde al ping
                                                    found = SearchAgent("ControlGatewayCont" + ch[0]); //Buscamos a su vez que el agente gateway este vivo en el AMS.
                                                    if (found == 1 && pong) {
                                                        argument5 = "ControlGatewayCont" + ch[0] + "->OK";
                                                    } else { //si cualquiera de las dos comprobaciones no se cumple asumimos que el agente se ha desconectado
                                                        argument5 = "ControlGatewayCont" + ch[0] + "->NO OK";
                                                        add_to_error_list("not_found", "ControlGatewayCont" + ch[0], "", "", "");
                                                        sendACL(ACLMessage.INFORM, "D&D", "not_found", "ControlGatewayCont" + ch[0] + "/div/" + MA);
                                                        LOGGER.error("Error while trying to communicate with gateway");
                                                    }
                                                    if (argument4.equals(MA + "->OK") && argument5.equals("ControlGatewayCont" + ch[0] + "->OK")) {
                                                        LOGGER.info("All agents online, asking asset state...");
                                                        sendACL(ACLMessage.REQUEST, "ControlGatewayCont" + ch[0], "check_asset", "How are you feeling PLC?");
                                                        ACLMessage state = blockingReceive(MessageTemplate.MatchOntology("asset_state"), 500);

                                                        if (state == null) {
                                                            LOGGER.error("Asset did not answer on time.");
                                                        } else {
                                                            LOGGER.info("Asset retrieved his state: " + state.getContent());
                                                            if (state.getContent().equals("Working")) {
                                                                LOGGER.info("Everything OK theoretically. Lengthening timeout.");
                                                                sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), msg.getOntology(), "reset_timeout");
                                                            } else {
                                                                LOGGER.error("Timeout confirmed");
                                                                sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), msg.getOntology(), "confirmed_timeout");
                                                            }
                                                        }
                                                    } else {
                                                        LOGGER.error("Timeout confirmed");
                                                        sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), msg.getOntology(), "confirmed_timeout");
                                                    }
                                                    add_to_error_list(argument1, argument2, argument3, argument4, argument5);
                                                }else{
                                                    argument5 = "ControlGatewayCont" + ch[0] + "->NO OK";
                                                    add_to_error_list(argument1, argument2, argument3, argument4, argument5);
                                                    sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), msg.getOntology(), "confirmed_timeout");
                                                }
                                            }else{
                                                argument4 = MA + "->NO OK";
                                                argument5 = "?";
                                                add_to_error_list(argument1, argument2, argument3, argument4, argument5);
                                                sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), msg.getOntology(), "confirmed_timeout");
                                            }
                                        }
                                    }
                                } else {
                                    LOGGER.warn("No data available to check failure");
                                    add_to_error_list(argument1,timeout_batch_id,"","","");
                                }
                            }

                        } else if(msg.getSender().getLocalName().contains("order")){ //es un timeout enviado por un order agent.
                            add_timeout_error_flag=true;
                            String[] parts=msg.getContent().split("/");
                            String timeout_order_id=parts[0];
                            String timeout_batch_id=parts[1];
                            LOGGER.warn(timeout_order_id+" order has thrown timeout on batch "+timeout_batch_id);
                            try{
                                ACLMessage reply =sendCommand(myAgent,"get * category=batch reference="+timeout_batch_id,"QoS");
                                ACLMessage reply2 =sendCommand(myAgent,"get * category=batchAgent parent="+reply.getContent()+" state=running","QoS");

                            for(int k=0;k<ErrorList.size();k++){
                                if(ErrorList.get(k).get(0).equals("timeout")&&ErrorList.get(k).get(2).equals(timeout_batch_id)){ //checkea si ha habido timeout en
                                    LOGGER.info("Batch "+timeout_batch_id+" has already reported a timeout. Ignoring error.");
                                    sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),"timeout_confirmed",timeout_batch_id);
                                    add_timeout_error_flag=false; //no se añade el error porque ya existe por parte de batch
                                }
                                if(ErrorList.get(k).get(0).equals("not_found")&&ErrorList.get(k).get(1).equals(reply2.getContent())){
                                    LOGGER.info("Batch "+timeout_batch_id+" already reported as not found. Ignoring error.");
                                    sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),"timeout_confirmed",timeout_batch_id);
                                    add_timeout_error_flag=false;
                                }
                            }
                            if(add_timeout_error_flag){
                                LOGGER.info("No timeout errors found for batch "+timeout_batch_id+". Pinging batch.");
                                    String batch_to_ping=reply2.getContent();
                                    if(batch_to_ping!=""){
                                        boolean pong= PingAgent(batch_to_ping);
                                        int found=SearchAgent(reply2.getContent());
                                        if (pong&&found>0) {
                                            LOGGER.info("Batch "+ batch_to_ping+" with reference "+timeout_batch_id+ " found alive. Timeout of order registered.");
                                            argument1="timeout";
                                            if (batch_and_machine.size() > 0) { //buscamos el batch en el listado y conseguimos el ID del machine agent responsable
                                                for (int k = 0; k < batch_and_machine.size(); k++) {
                                                    if (batch_and_machine.get(k).get(0).equals(timeout_batch_id)) {

                                                        String MA = batch_and_machine.get(k).get(1);
                                                        if (CheckNotFoundRegistry(MA)) {
                                                            pong = PingAgent(MA); // hacemos ping al agente máquina para comprobar su estado
                                                            argument2 = timeout_batch_id;
                                                            argument3 = "not known";
                                                            found = SearchAgent(MA);
                                                            if (pong && found > 0) {  //Si pong es 1, el agente ha contestado
                                                                argument4 = MA + "->OK";
                                                            } else {
                                                                argument4 = MA + "->NO OK";
                                                                add_to_error_list("not_found", MA, "", "", "");
                                                                sendACL(ACLMessage.INFORM, "D&D", "not_found", MA);
                                                            }
                                                            try {
                                                                machinenbr = sendCommand(myAgent, "get " + MA + " attrib=id", "name"); //consultamos la id de la estacion al SA para saber que gateway agent le corresponde. Si es 41 seria ControlGatewayCont4.
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                            char[] ch = new char[machinenbr.getContent().length()];
                                                            ch[0] = machinenbr.getContent().charAt(0);
                                                            if (CheckNotFoundRegistry("ControlGatewayCont" + ch[0])){
                                                                pong = PingAgent("ControlGatewayCont" + ch[0]);
                                                                if (pong) {
                                                                    argument5 = "ControlGatewayCont" + ch[0] + "->OK";
                                                                } else {
                                                                    argument5 = "ControlGatewayCont" + ch[0] + "->NO OK";
                                                                    add_to_error_list("not_found", "ControlGatewayCont" + ch[0], "", "", "");
                                                                    String msgtoDD = "ControlGatewayCont" + ch[0] + "/div/" + MA;
                                                                    sendACL(ACLMessage.INFORM, "D&D", "not_found", msgtoDD);
                                                                }
                                                                if (argument4.equals(MA + "->OK") && argument5.equals("ControlGatewayCont" + ch[0] + "->OK")) {
                                                                    System.out.println(MA + "->OK");
                                                                    System.out.println("ControlGatewayCont" + ch[0] + "->OK");
                                                                    LOGGER.warn("Batch agent is not throwing timeouts.");
                                                                } else {
                                                                    LOGGER.error("Timeout confirmed");
                                                                }
                                                                add_to_error_list(argument1, argument2, argument3, argument4, argument5);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }else{
                                            add_to_error_list("not_found",batch_to_ping,"","","");
                                            sendACL(ACLMessage.INFORM, "D&D", "not_found", batch_to_ping);
                                        }
                                    }else{
                                        LOGGER.error("Batch agent not found by AMS. Probably dead or isolated long ago.");
                                        add_to_error_list("not_found",batch_to_ping,"","","");
                                        //informar a D&D par que ponga  anegociar las replicas
                                    }
                                sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),"timeout_confirmed",timeout_batch_id);
                                }
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if(msg.getPerformative()==ACLMessage.INFORM) { //Se recibe algun tipo de info
                    if(msg.getOntology().equals("batch_finish")){ //se recibe cuando acaba el batch
                        String finishing_batch=msg.getContent();
                        for(int n=0;n<allDelays.size();n++){ //Se elimina de la lista de los delays el batch que ha terminado
                            if(allDelays.get(n).get(0).equals(finishing_batch)){
                                allDelays.remove(n);
                                i--;
                            }
                        }
                        for(int o=0;o<batch_and_machine.size();o++){
                            if(batch_and_machine.get(o).get(0).equals(finishing_batch)){ //se elimina la asignación batch-máquina
                                batch_and_machine.remove(o);
                                j--;
                            }
                        }
                        LOGGER.info("Batch "+finishing_batch+" finished.");
                    }
                    if (msg.getOntology().equals("delay")) {
                        String[] parts2=msg.getContent().split("/");
                        String batch_id=parts2[0];
                        String ms_of_delay=parts2[1];
                        LOGGER.info("Batch " +batch_id+" started with "+ms_of_delay+" ms of delay");
                        ActualBatch = getDelays(msg.getContent()); //Añade el batch especificado con su correspondiente delay a la lista de delays

                        for (int l = 0; l < delay_asking_queue.size(); l++) {
                            if (ActualBatch.get(0).equals(delay_asking_queue.get(l))) {
                                sendACL(ACLMessage.INFORM,delay_asking_queue.get(l + 1),"askdelay",ActualBatch.get(1));
                                delay_asking_queue.remove(l + 1);
                                delay_asking_queue.remove(l);
                                LOGGER.info("Informing batch");
                            }
                        }
                        allDelays.add(i, ActualBatch);
                        i++;
                        String sender = msg.getSender().getLocalName();
                        ArrayList<String> temp = BatchAndMachines(msg.getContent(), sender); //Crea un listado de agentes maquina con los batch que tengan asignados
                        batch_and_machine.add(j, temp);
                        j++;
                    }
                    if(msg.getOntology().equals("asset_state")){ //recibe ping de vuelta del asset (solo para testing)

                        LOGGER.info("Recieved asset state out of the timeout: "+msg.getContent());
                    }
                }
                if(msg.getPerformative()==ACLMessage.REQUEST) { //Se recibe algun tipo de petición
                    if(msg.getOntology().equals("askrelationship")) {
                        if(msg.getContent().contains("machine")){
                            for(int u=0;u<batch_and_machine.size();u++){
                                if(batch_and_machine.get(u).get(1).equals(msg.getContent())){
                                    try {
                                        ACLMessage reply =sendCommand(myAgent,"get * category=batch reference="+batch_and_machine.get(u).get(0),"QoS");
                                        ACLMessage reply2 =sendCommand(myAgent,"get * category=batchAgent parent="+reply.getContent()+" state=running","QoS");
                                        sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),msg.getOntology(),reply2.getContent());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }else if(msg.getContent().contains("batchagent")){
                            try {
                                ACLMessage reply =sendCommand(myAgent,"get "+msg.getContent()+" attrib=parent","QoS");
                                ACLMessage reply2 =sendCommand(myAgent,"get "+reply.getContent()+ " attrib=reference","QoS");
                                for(int u=0;u<batch_and_machine.size();u++){
                                    if(batch_and_machine.get(u).get(0).equals(reply2.getContent())){
                                        sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),msg.getOntology(),batch_and_machine.get(u).get(1));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (msg.getOntology().equals("askdelay")) { //Se recibe consulta del delay

                        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                        String asking_batch = msg.getContent();
                        reply.setContent("0"); //si no encuentra ningun batch que coincida en la lista predefine un valor de 0
                        boolean flag = true;
                        if (allDelays.size() != 0) {
                            for (int k = 0; k < allDelays.size() || flag; k++) {
                                if (allDelays.get(k).get(0).equals(asking_batch)) {
                                    reply.setContent(allDelays.get(k).get(1));
                                    flag = false;
                                }
                            }
                        }
                        if (flag) {  //Es posible que se pida el delay antes de tenerlo, por lo que se anotan en la lista de espera hasta recibirlo del machine agent.
                            LOGGER.info("Batch " + asking_batch + " asked delay before having it");
                            delay_asking_queue.add(asking_batch);
                            delay_asking_queue.add(msg.getSender().getLocalName());

                        } else {
                            reply.addReceiver(msg.getSender());
                            reply.setOntology(msg.getOntology());
                            send(reply);
                            LOGGER.info("Informing batch");
                        }
                    }else if(msg.getOntology().equals("command")){
                        if(msg.getContent().equals("errorlist")){
                            LOGGER.info(msg.getSender().getLocalName()+" asked to send error list");
                            String concatenated_errors="";
                            for(int q=0;q<ErrorList.size();q++){
                                if(q!=0){
                                    concatenated_errors = concatenated_errors +"/err/";
                                }
                                for(int r=0;r<ErrorList.get(q).size();r++) {
                                    if(r==0){
                                        concatenated_errors = concatenated_errors +ErrorList.get(q).get(r);
                                    }else {
                                        concatenated_errors = concatenated_errors + "/inf/" + ErrorList.get(q).get(r);
                                    }
                                }
                            }
                            sendACL(7,msg.getSender().getLocalName(),msg.getOntology(),concatenated_errors);
                        }
                    }
                }
            }
        }

        private ArrayList<String> getDelays(String data){

            ArrayList<String> batchdelay= new ArrayList<String>();//Creamos un Arraylist para los delay de cada batch.
            String[] parts = data.split("/");
            String part1 = parts[0]; // BatchID
            String part2 = parts[1]; // Delay in minutes
            batchdelay.add(part1);
            batchdelay.add(part2);
            return batchdelay;
        }

        private ArrayList<String> BatchAndMachines(String data, String sender){
            ArrayList<String> MachineAgentList= new ArrayList<String>(); //Creamos un Arraylist para los machine agent que ejecutan cada batch.
            String[] parts=data.split("/");
            String batch = parts[0]; // BatchID
            MachineAgentList.add(batch);
            MachineAgentList.add(sender);


            return MachineAgentList;
        }

        private boolean PingAgent (String name){  //checkea el estado de los agentes de aplicación, recurso y gateway

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
        private String CheckMsgFIFO(String name,String msg){ //Para el error de ACL. Consulta al receptor si ha recibido el msg.

            AID Agent_to_ping_ID=new AID(name,false);
            ACLMessage sping=new ACLMessage(ACLMessage.REQUEST);
            sping.setOntology("ping");
            sping.addReceiver(Agent_to_ping_ID);
            sping.setContent(msg);
            send(sping);
            ACLMessage reply=blockingReceive(pingtemplate,500);
            if(reply!=null){
                if(reply.getContent().equals("Y")){
                    return "msg_received";
                }else{
                    return "msg_lost";
                }
            }else{
                return "no_answer";
            }
        }

        public void sendACL(int performative,String receiver,String ontology,String content){ //Funcion estándar de envío de mensajes
            AID receiverAID=new AID(receiver,false);
            ACLMessage msg=new ACLMessage(performative);
            msg.addReceiver(receiverAID);
            msg.setOntology(ontology);
            msg.setContent(content);
            send(msg);
        }

        private int SearchAgent (String agent){
            int found=0;
            AMSAgentDescription [] agents = null;

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

        private ACLMessage sendCommand(Agent agent, String cmd, String conversationId) throws Exception {

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
    }

    public void takeDown(){
        LOGGER.entry();
        LOGGER.warn("QoS manager shutting down");
        LOGGER.exit();
    }

    public void add_to_error_list(String type, String arg2, String arg3, String arg4, String arg5){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        now = dateFormat.format(getactualtime());ErrorList.add(l, new ArrayList<>());
        ErrorList.get(l).add(0, type);
        ErrorList.get(l).add(1, now);
        ErrorList.get(l).add(2, arg2);
        ErrorList.get(l).add(3, arg3);
        ErrorList.get(l).add(4, arg4);
        ErrorList.get(l).add(5, arg5);
        l++;
    }


    public Date getactualtime(){
        String actualTime;
        int ano, mes, dia, hora, minutos, segundos;
        Calendar calendario = Calendar.getInstance();
        ano = calendario.get(Calendar.YEAR);
        mes = calendario.get(Calendar.MONTH) + 1;
        dia = calendario.get(Calendar.DAY_OF_MONTH);
        hora = calendario.get(Calendar.HOUR_OF_DAY);
        minutos = calendario.get(Calendar.MINUTE);
        segundos = calendario.get(Calendar.SECOND);
        actualTime = ano + "-" + mes + "-" + dia + "T" + hora + ":" + minutos + ":" + segundos;
        Date actualdate = null;
        try {
            actualdate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(actualTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return actualdate;
    }

    public boolean CheckNotFoundRegistry(String Agent){
        boolean flag=true;
        for(int t=0;t<ErrorList.size();t++){
            if(ErrorList.get(t).get(0).equals("not_found")){
                if(ErrorList.get(t).get(2).equals(Agent)){
                    LOGGER.info(Agent +" has already been reported as not found.");
                    flag=false;
                }
            }
        }
        return flag;
    }

}