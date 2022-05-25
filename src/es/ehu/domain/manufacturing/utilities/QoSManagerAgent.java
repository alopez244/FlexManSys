package es.ehu.domain.manufacturing.utilities;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;


public class QoSManagerAgent extends ErrorHandlerAgent {

    private ArrayList<ArrayList<String>> ErrorList=new ArrayList<ArrayList<String>>();
    private int l=0;
//    private HashMap<String, String> batch_machine=new HashMap<String,String >();
    private HashMap<String,HashMap<String, String>> batch_op_machine=new HashMap<String,HashMap<String, String>>();
    private HashMap<String,String> delay_asking_queue =new HashMap<String,String >();
    private HashMap<String,String> delay_list=new HashMap<String,String >();
    static final Logger LOGGER = LogManager.getLogger(QoSManagerAgent.class.getName());
    private boolean add_timeout_error_flag=true,pong=false;
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

    public class QoS extends CyclicBehaviour {

        public void action() {
            LOGGER.entry();

            ACLMessage msg = myAgent.blockingReceive();
            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.FAILURE && !msg.getSender().getLocalName().equals("ams")) { //Se recibe un posible error no perteneciente al ams
                    if (msg.getOntology().equals("acl_error")) { //error de tipo comunicación
                        String[] msgparts = msg.getContent().split("/div/");

                        String receiver = msgparts[0];
                        String intercepted_msg = msgparts[1];
                        report_back(msg);
                        LOGGER.warn(msg.getSender().getLocalName() + " reported a failure while trying to communicate with " + receiver);
                        if (CheckNotFoundRegistry(receiver) && CheckNotFoundRegistry(msg.getSender().getLocalName())) { //comprueba que el denunciante y el denunciado no esten en la blacklist de agentes no encontrados
//                            get_timestamp(myAgent,receiver,"DeadAgentDetection");
                            LOGGER.info("Checking if " + receiver + " is alive and if the agent received the reported msg.");
                            String command = CheckMsgFIFO(receiver, intercepted_msg); //checkMsgFIFo se puede usar con order, batch y machine por ahora, el resto responde como un ping
                            System.out.println(command);
                            if (command.equals("msg_lost")) {         //Receiver vivo pero mensaje perdido
                                if (!PingAgent(msg.getSender().getLocalName(),myAgent)) { //checks if the reporting agent is actually isolated.
                                    LOGGER.error(msg.getSender().getLocalName() + " agent who reported the error is isolated");
                                    sendACL(ACLMessage.INFORM, "D&D", "msg_lost", msg.getContent(),myAgent);
                                    String msgtoDD = msg.getSender().getLocalName();

                                    add_to_error_list("not_found", msg.getSender().getLocalName(), "", "", "");
                                    sendACL(ACLMessage.INFORM, "D&D", "not_found", msgtoDD,myAgent);
                                } else {
                                    LOGGER.info("Receiver and sender are online, although message is lost.");
                                    add_to_error_list("communication", msg.getSender().getLocalName(), receiver, intercepted_msg, "");
                                    sendACL(ACLMessage.INFORM, "D&D", "msg_lost", msg.getContent(),myAgent);
                                }
                            } else if (command.equals("msg_received")) {

                                LOGGER.info("Message arrived to agent. Ignore error.");

                            } else { //si no responde el receiver, se trata de un agente aislado o muerto
                                LOGGER.warn("No answer from " + receiver + ". Confirming error.");
                                String msgtoDD = receiver;
//
//                                get_timestamp(myAgent,receiver,"DeadAgentConfirmation");
                                if(receiver.contains("ControlGatewayCont")){ //si el agente denunciado es un GW hay que redistribuir las tareas de su máquina

                                    for(Entry<String, HashMap<String,String>> batches: batch_op_machine.entrySet()){
                                        for(Entry<String,String> operations: batch_op_machine.get(batches.getKey()).entrySet()){
                                            if(operations.getValue().equals(msg.getSender().getLocalName())){ //el unico posible denunciante del GW es la misma maquina
                                                sendACL(ACLMessage.INFORM,"D&D","redistribute",msg.getSender().getLocalName()+"/"+get_machine_id(msg.getSender().getLocalName())+"/"+batches.getKey()+"/"+"?",myAgent);
                                            }
                                        }
                                    }

                                } else if(receiver.contains("machine")){

                                    for(Entry<String, HashMap<String,String>> batches: batch_op_machine.entrySet()){
                                        for(Entry<String,String> operations: batch_op_machine.get(batches.getKey()).entrySet()){
                                            if(operations.getValue().equals(receiver)){ //el unico posible denunciante del GW es la misma maquina
                                                sendACL(ACLMessage.INFORM,"D&D","redistribute",receiver+"/"+get_machine_id(receiver)+"/"+batches.getKey()+"/"+"?",myAgent);
                                            }
                                        }
                                    }

                                }else{
                                    add_to_error_list("not_found", receiver, "", "", "");
                                    sendACL(ACLMessage.INFORM, "D&D", "not_found", msgtoDD,myAgent);
                                }
                            }
                        }

                    } else if (msg.getOntology().equals("timeout")) { //error de tipo timeout

                        if (msg.getSender().getLocalName().contains("batch")) { //timeout enviado por un batch
                            get_timestamp(myAgent,msg.getSender().getLocalName(),"DetectionTime");
                            report_back(msg); //confirmacion de recepcion de mensaje
                            String[] parts = msg.getContent().split("/");
                            String timeout_batch_id = parts[0];
                            String timeout_item_id = parts[1];

                                argument1 = "timeout";
                                LOGGER.warn(timeout_batch_id + " batch has thrown a timeout on item " + timeout_item_id);
                                if (!CheckNotFoundRegistry(msg.getSender().getLocalName())&&timeout_batch_id!=null) { //es posible que un agente muerto lance timeout porque estos se ejecutan en hilos separados
                                    LOGGER.warn("Timeout coming from a thread from " + msg.getSender().getLocalName() + " who is dead. Ignoring timeout");
                                } else {
                                    if (batch_op_machine.containsKey(timeout_batch_id)) { //buscamos el batch en el listado y conseguimos el ID del machine agent responsable
                                        timeout_handler(msg, timeout_batch_id, timeout_item_id);
                                    } else {
                                        LOGGER.warn("No data available to check failure because process has either not started or already has finish. Most likely a dead agent reported timeout. Ignoring");
                                    }
                                }
                        } else if (msg.getSender().getLocalName().contains("order")) { //es un timeout enviado por un order agent.
                            report_back(msg);
                            add_timeout_error_flag = true;
                            String[] parts = msg.getContent().split("/");
                            String timeout_order_id = parts[0];
                            String timeout_batch_id = parts[1];
                            LOGGER.warn(timeout_order_id + " order has thrown timeout on batch " + timeout_batch_id);
                            try {
                                ACLMessage batch_parent = sendCommand(myAgent, "get * category=batch reference=" + timeout_batch_id, "QoS");
                                ACLMessage running_batch = sendCommand(myAgent, "get * category=batchAgent parent=" + batch_parent.getContent() + " state=running", "QoS");
                                if (running_batch.getContent().equals("")) {
                                    LOGGER.error("Batch " + timeout_batch_id + " has no running replicas by now. Nothing to do.");

                                } else {
                                    for (int k = 0; k < ErrorList.size(); k++) {
                                        if (ErrorList.get(k).get(0).equals("timeout") && ErrorList.get(k).get(2).equals(timeout_batch_id)) { //checkea si ha habido timeout en
                                            LOGGER.info("Batch " + timeout_batch_id + " has already reported a timeout. Ignoring error.");
                                            sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), "timeout_confirmed", timeout_batch_id,myAgent);
                                            add_timeout_error_flag = false; //no se añade el error porque ya existe por parte de batch
                                            break;
                                        } else if (ErrorList.get(k).get(0).equals("not_found") && ErrorList.get(k).get(1).equals(running_batch.getContent())) {
                                            LOGGER.info("Batch " + timeout_batch_id + " already reported as not found. Ignoring error.");
                                            sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), "timeout_confirmed", timeout_batch_id,myAgent);
                                            add_timeout_error_flag = false;
                                            break;
                                        }
                                    }
                                    if (add_timeout_error_flag) {
                                        LOGGER.info("No timeout errors found for batch " + timeout_batch_id + ". Pinging batch.");
                                        String batch_to_ping = running_batch.getContent();
                                        if (batch_to_ping != "") {
                                            if (PingAgent(batch_to_ping,myAgent)) {
                                                LOGGER.info("Batch " + batch_to_ping + " with reference " + timeout_batch_id + " found alive. Timeout of order registered.");
                                                argument1 = "timeout";

                                                if (batch_op_machine.containsKey(timeout_batch_id)) { //buscamos el batch en el listado y conseguimos el ID del machine agent responsable
                                                    timeout_handler(msg, timeout_batch_id,"?"); //al ser un timeout de order no sabemos que item se estaba produciendo
                                                }
                                            } else {
//                                                get_timestamp(myAgent,batch_to_ping,"DeadAgentDetection");
                                                add_to_error_list("not_found", batch_to_ping, "", "", "");
                                                sendACL(ACLMessage.INFORM, "D&D", "not_found", batch_to_ping,myAgent);
                                            }
                                        } else {
                                            LOGGER.error("Batch agent not found by AMS. Probably dead or isolated long ago.");
                                            add_to_error_list("not_found", batch_to_ping, "", "", "");
                                            //informar a D&D par que ponga  anegociar las replicas
                                        }
                                        sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), "timeout_confirmed", timeout_batch_id,myAgent);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (msg.getPerformative() == ACLMessage.INFORM) { //Se recibe algun tipo de info
                    if (msg.getOntology().equals("batch_finish")) { //se recibe cuando acaba el batch
                        String finishing_batch = msg.getContent();
                        delay_list.remove(finishing_batch);
                        batch_op_machine.remove(finishing_batch);
                        LOGGER.info("Batch " + finishing_batch + " finished.");

                    } else if (msg.getOntology().equals("remove_relation")) { //se recibe cuando una máquina acaba sus operaciones
                        LOGGER.info("Removed relation "+msg.getContent().split("/")[0]+" -> "+msg.getContent().split("/")[1]+" -> "+msg.getSender().getLocalName());
                        if(batch_op_machine.containsKey(msg.getContent().split("/")[0])){
                            batch_op_machine.get(msg.getContent().split("/")[0]).remove(msg.getContent().split("/")[1]);
                        }
                    } else if (msg.getOntology().equals("delay")) {     //ejemplo: 221/133234000
                        String[] parts2 = msg.getContent().split("/");
                        String batch_id = parts2[0];
                        String ms_of_delay = parts2[1];
                        LOGGER.info("Batch " + batch_id + " started with " + ms_of_delay + " ms of delay on machine "+msg.getSender().getLocalName());
                        //simula fallo en la maquina 3
//                        if(msg.getSender().getLocalName().equals("machine3")){
//                            sendACL(7,"NodeKiller","killpls",msg.getSender().getLocalName(),myAgent);
//                        }

                        delay_list.put(batch_id,ms_of_delay); //Añade el batch especificado con su correspondiente delay a la lista de delays
                        if(delay_asking_queue.containsKey(batch_id)){  //si anteriormente han pedido el delay que acabamos de registrar debemos informar
                            sendACL(ACLMessage.INFORM, delay_asking_queue.get(batch_id), "askdelay", delay_list.get(batch_id),myAgent);
                            delay_list.remove(batch_id);
                            delay_asking_queue.remove(batch_id);
                            LOGGER.info("Informing batch " + msg.getContent());
                        }

                    } else if(msg.getOntology().equals("add_relation")){ //ej:   machine1/221/4
                        String machine= msg.getContent().split("/")[0];
                        String batch= msg.getContent().split("/")[1];
                        String operation= msg.getContent().split("/")[2];
                        if(!batch_op_machine.containsKey(batch)){
                            batch_op_machine.put(batch,new HashMap<String,String>());
                        }
                        batch_op_machine.get(batch).put(operation,machine);  //se asume que un batch puede disponer de varias maquinas asignadas y viceversa, pero si tiene diferentes maquinas será para ejecutar difernetes operaciones

                        LOGGER.info("Added relation "+batch+" -> "+operation+" -> "+machine);
                    } else if (msg.getOntology().equals("asset_state")) { //recibe ping de vuelta del asset fuera de tiempo de espera de mensaje (solo para testing, no deberia ocurrir en funcionamiento normal)
                        LOGGER.warn("Recieved asset state out of the timeout: " + msg.getContent());
                    } else if (msg.getOntology().equals("reported_on_dead_node")) {
//                        get_timestamp(myAgent,msg.getContent(),"DeadAgentConfirmation");
                        add_to_error_list("not_found", msg.getContent(), "", "", "");
                        LOGGER.info("D&D reported a dead agent (" + msg.getContent() + "). Added to error list.");
                    }
                }
                if (msg.getPerformative() == ACLMessage.REQUEST) { //Se recibe algun tipo de petición
                    if (msg.getOntology().equals("askrelationship")) {  //devuelve la relacion máquina-batch
                        if (msg.getContent().contains("machine")) {
                            String content="";

                            for(Entry<String, HashMap<String,String>> batches: batch_op_machine.entrySet()){
                                for(Entry<String,String> operations: batch_op_machine.get(batches.getKey()).entrySet()){
                                    if(operations.getValue().equals(msg.getContent())){
                                        content=content+operations.getValue()+"/";
                                    }
                                }
                            }

                            sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),msg.getOntology(),content,myAgent);
                        }else{

                            String content="";
                            for(Entry<String,String> operations: batch_op_machine.get(msg.getContent()).entrySet()){
                                content=content+operations.getValue()+"/";
                            }
                            sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),msg.getOntology(),content,myAgent);
                        }

                    }
                    if (msg.getOntology().equals("askdelay")) { //Se recibe consulta del delay

                        String asking_batch = msg.getContent();

                        if(delay_list.containsKey(msg.getContent())){
                            if(msg.getSender().getLocalName().contains("batchagent")){ //
                                sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),msg.getOntology(),delay_list.get(msg.getContent()),myAgent);
                                LOGGER.info("Informing batch " + asking_batch);
                            }
                            delay_list.remove(msg.getContent());
                        }else{
                            if(msg.getSender().getLocalName().contains("batchagent")){
                                delay_asking_queue.put(asking_batch,msg.getSender().getLocalName());
                            }
                        }

                    } else if (msg.getOntology().equals("command")) { //peticiones provenientes del D&D
                        if (msg.getContent().equals("errorlist")) {
                            LOGGER.info(msg.getSender().getLocalName() + " asked to send error list");
                            String concatenated_errors = "";
                            for (int q = 0; q < ErrorList.size(); q++) {
                                if (q != 0) {
                                    concatenated_errors = concatenated_errors + "/err/";
                                }
                                for (int r = 0; r < ErrorList.get(q).size(); r++) {
                                    if (r == 0) {
                                        concatenated_errors = concatenated_errors + ErrorList.get(q).get(r);
                                    } else {
                                        concatenated_errors = concatenated_errors + "/inf/" + ErrorList.get(q).get(r);
                                    }
                                }
                            }
                            sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), msg.getOntology(), concatenated_errors,myAgent);
                        }
                    }
                }
            }
        }

        private void timeout_handler(ACLMessage msg, String timeout_batch_id, String timeout_item_id) {
//                    String MA = batch_machine.get(timeout_batch_id);
            for(Entry<String,String> operations: batch_op_machine.get(timeout_batch_id).entrySet()){ //hay que checkear cada maquina asignada al batch
                String MA = operations.getValue();
                argument2 = timeout_batch_id;
                argument3 = timeout_item_id;
                if (CheckNotFoundRegistry(MA)) { //comprobamos que no este denunciado ya
                    String ping_result = PingAsset(MA);
                    if (!ping_result.contains("plc") && ping_result.contains("_down")) {     //GW o machine caidos, se denuncia a D&D
                        String[] dead_agent = ping_result.split("_down");
                        if (dead_agent[0].contains("machine")) {
                            argument4 = MA + "->NO OK";
                            argument5 = "GW->?";
                        } else {
                            argument4 = MA + "->OK";
                            argument5 = "GW->NO OK";
                        }
                        add_to_error_list("timeout", argument2, argument3, argument4, argument5); //añadimos timeout
                        if (!CheckNotFoundRegistry(dead_agent[0])) {
                            LOGGER.info("Agent " + dead_agent[0] + " has already been reported");
                        } else {
                            LOGGER.warn(MA+" operations must be redistributed to finish batch "+timeout_batch_id);
                            add_to_error_list("not_found", dead_agent[0], "", "", ""); //añadimos agente caido
                            get_timestamp(myAgent,msg.getSender().getLocalName(),"ConfirmationTime");
                            for(Entry<String, HashMap<String,String>> batches: batch_op_machine.entrySet()){  //tras confirmar que esta máquina está mal se redistribuyen las tareas asignadas a esta maquina
                                for(Entry<String,String> operations2: batch_op_machine.get(batches.getKey()).entrySet()){
                                    if(operations2.getValue().equals(MA)){
                                        if(batches.getKey().equals(timeout_batch_id)){ //si se trata del batch del timeout se le pasa el item
                                            sendACL(ACLMessage.INFORM,"D&D","redistribute",MA+"/"+get_machine_id(MA)+"/"+batches.getKey()+"/"+timeout_item_id,myAgent);
                                        }else{   //si no es el batch del timeout solo puede ser un batch que esta pendiente de ejecución, es decir se debe rehacer por completo
                                            sendACL(ACLMessage.INFORM,"D&D","redistribute",MA+"/"+get_machine_id(MA)+"/"+batches.getKey()+"/"+"?",myAgent);
                                        }
                                    }
                                }
                            }
                        }

                    } else if (ping_result.equals("working")||ping_result.equals("not_working")) { //el maquina no ha comenzado el batch o se ha retrasado. Registramos el timeout, lo reseteamos y no hacemos nada más.
                        LOGGER.info("Everything OK theoretically. Reset timeout.");
                        sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), msg.getOntology(), "reset_timeout",myAgent);
                        argument4 = MA + "->OK";
                        argument5 = "GW->OK";
                        add_to_error_list("timeout", argument2, argument3, argument4, argument5);  //aunque este todoo ok hay que añadir el timeout a la lista de errores
                    } else { //el sistema está parado o en error
                        get_timestamp(myAgent,msg.getSender().getLocalName(),"ConfirmationTime");
                        LOGGER.warn(MA+" operations must be redistributed to finish batch "+timeout_batch_id);
                        for(Entry<String, HashMap<String,String>> batches: batch_op_machine.entrySet()){  //tras confirmar que esta máquina está mal se redistribuyen las tareas asignadas a esta maquina
                            for(Entry<String,String> operations2: batch_op_machine.get(batches.getKey()).entrySet()){
                                if(operations2.getValue().equals(MA)){
                                    if(batches.getKey().equals(timeout_batch_id)){ //si se trata del batch del timeout se le pasa el item
                                        sendACL(ACLMessage.INFORM,"D&D","redistribute",MA+"/"+get_machine_id(MA)+"/"+batches.getKey()+"/"+timeout_item_id,myAgent);
                                    }else{   //si no es el batch del timeout solo puede ser un batch que esta pendiente de ejecución, es decir se debe rehacer por completo
                                        sendACL(ACLMessage.INFORM,"D&D","redistribute",MA+"/"+get_machine_id(MA)+"/"+batches.getKey()+"/"+"?",myAgent);
                                    }
                                }
                            }
                        }

                        argument4 = MA + "->OK";
                        argument5 = "GW->OK";
                        add_to_error_list("timeout", argument2, argument3, argument4, argument5);
                    }
                } else {  //si ya esta denunciado no hay que ejecutar acciones de redistribuir de tareas
                    argument4 = MA + "->NO OK";
                    argument5 = "?";
                    add_to_error_list("timeout", argument2, argument3, argument4, argument5);

                }
            }
        }


        public String PingAsset(String machine) {  //Ping completo hasta PLC. Devuelve estado del agente maquina, del gateway, y del proceso del PLC (trabajando, parado , error trabajando, error parado)
            get_timestamp(myAgent,machine,"StartSearch");
            int n = SearchAgent(machine,myAgent);
            get_timestamp(myAgent,machine,"FinishSearch");
            if (n > 0) {
                get_timestamp(myAgent,machine,"StartPing");
                sendACL(ACLMessage.REQUEST, machine, "ping_PLC", "",myAgent);
                ACLMessage echo = myAgent.blockingReceive(pingtemplate, 600);
                //Estructura de datos:
                //nombre_de_GW: estado (OK,DOWN)
                //PLC: estado (W, NW, EW, ENW)
                //      W -> working
                //      NW -> not working but no errors
                //      EW -> error while working
                //      ENW -> error while not working
                if (echo != null) {
                    LOGGER.debug(echo.getContent());
                    String[] content_div = echo.getContent().split(" & ");
                    String[] gw_state = content_div[0].split(":");
                    System.out.println("Ping result: ");
                    if (gw_state[1].contains("DOWN")) {
                        System.out.println(myAgent.getLocalName() + "->" + machine + "-/>" + gw_state[0] + "->?");

                        get_timestamp(myAgent,machine,"FinishPing");

                        return gw_state[0] + "_down";
                    } else {
                        String[] plc_state = content_div[1].split(":");
                        if (plc_state[1].contains("ENW")) {
                            System.out.println(myAgent.getLocalName() + "->" + machine + "->" + gw_state[0] + "->PLC(IDLE)-/>Process");

                            get_timestamp(myAgent,machine,"FinishPing");

                            return "error_while_not_working";
                        } else if (plc_state[1].contains("EW")) {
                            System.out.println(myAgent.getLocalName() + "->" + machine + "->" + gw_state[0] + "->PLC(RUN)-/>Process");

                            get_timestamp(myAgent,machine,"FinishPing");

                            return "error_while_working";
                        } else if (plc_state[1].contains("NW")) {
                            System.out.println(myAgent.getLocalName() + "->" + machine + "->" + gw_state[0] + "->PLC(IDLE)->Process");

                            get_timestamp(myAgent,machine,"FinishPing");

                            return "not_working";
                        } else if (plc_state[1].contains("W")) {
                            System.out.println(myAgent.getLocalName() + "->" + machine + "->" + gw_state[0] + "->PLC(RUN)->Process");

                            get_timestamp(myAgent,machine,"FinishPing");

                            return "working";
                        } else {
                            System.out.println(myAgent.getLocalName() + "->" + machine + "->" + gw_state[0] + "-/>PLC(?)->?");

                            get_timestamp(myAgent,machine,"FinishPing");

                            return "plc_down";
                        }
                    }
                } else {
                    System.out.println(myAgent.getLocalName() + "-/>" + machine + "->?");

                    get_timestamp(myAgent,machine,"FinishPing");

                    return machine + "_down";
                }
            } else {
                return machine + "_down";
            }
        }


        private String CheckMsgFIFO(String name, String msg) { //Para el error de ACL. Consulta al receptor si ha recibido el msg.

            AID Agent_to_ping_ID = new AID(name, false);
            ACLMessage sping = new ACLMessage(ACLMessage.REQUEST);
            sping.setOntology("ping");
            sping.addReceiver(Agent_to_ping_ID);
            sping.setContent(msg);
            myAgent.send(sping);
            ACLMessage reply = myAgent.blockingReceive(pingtemplate, 500);
            if (reply != null) {
                if (reply.getContent().equals("Y")) {
                    return "msg_received";
                } else {
                    return "msg_lost";
                }
            } else {
                return "no_answer";
            }
        }

        public void report_back(ACLMessage msg) { //Funcion especial de respuesta confirmando error de comunicacion habilitado para acknowledge nuevo
            ACLMessage confirm = new ACLMessage(ACLMessage.CONFIRM);
            confirm.addReceiver(msg.getSender());
            confirm.setOntology(msg.getOntology());
            confirm.setContent(msg.getContent());
            confirm.setConversationId(msg.getConversationId());
            myAgent.send(confirm);
        }


        public void takeDown() {
            LOGGER.entry();
            LOGGER.warn("QoS manager shutting down");
            LOGGER.exit();
        }

        public void add_to_error_list(String type, String arg2, String arg3, String arg4, String arg5) {  //añade un error personalizable a la lista de errores
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            now = dateFormat.format(getactualtime());
            ErrorList.add(l, new ArrayList<>());
            ErrorList.get(l).add(0, type);
            ErrorList.get(l).add(1, now);
            ErrorList.get(l).add(2, arg2);
            ErrorList.get(l).add(3, arg3);
            ErrorList.get(l).add(4, arg4);
            ErrorList.get(l).add(5, arg5);
            l++;
        }


        public Date getactualtime() {  //obtiene un timestamp del instante actual
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

        public String get_machine_id(String MA){  //Obtiene la id de la máquina pej: 11, 21, etc
            String lost_machine_id="";
            String[] id=new String[1];
            try {
                ACLMessage LM_id= sendCommand(myAgent,"get "+MA+" attrib=id", MA+" id");
                lost_machine_id=LM_id.getContent();
                id=lost_machine_id.split("");
                //obtenemos el número de la máquina: id=21 -> máquina nº2
            } catch (Exception e) {
                e.printStackTrace();
            }
            return id[0];
        }

        public boolean CheckNotFoundRegistry(String Agent) {  //comprueba que el agente indicado no ha sido haya reportado como no encontrado.

            for (int t = 0; t < ErrorList.size(); t++) {
                if (ErrorList.get(t).get(0).equals("not_found")) {
                    if (ErrorList.get(t).get(2).equals(Agent)) {
                        LOGGER.info(Agent + " has already been reported as not found.");
                        return false;
                    }
                }
            }
            return true;
        }
    }
}