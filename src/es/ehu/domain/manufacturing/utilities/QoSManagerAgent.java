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


public class QoSManagerAgent extends ErrorHandlerAgent {


    private Agent myAgent=this;
    private int agent_found_qty=0;
    private int gateway_found_qty=0;
    private ArrayList<String> ActualBatch = new ArrayList<String>();
    private ArrayList<ArrayList<String>> ErrorList=new ArrayList<ArrayList<String>>();
    private int i=0;
    private ArrayList<ArrayList<String>> allDelays = new ArrayList<ArrayList<String>>();
    private int j=0;
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

    public class QoS extends CyclicBehaviour {

        public void action() {
            LOGGER.entry();

            ACLMessage msg = myAgent.blockingReceive();
            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.FAILURE && !msg.getSender().getLocalName().equals("ams")) { //Se recibe un posible error no perteneciente al ams
                    if (msg.getOntology().equals("acl_error")) { //error de tipo comunicación
                        String[] msgparts = msg.getContent().split("/div/");
//                        String performative = msgparts[0];
//                        String ontology = msgparts[1];
//                        String convID = msgparts[2];
                        String receiver = msgparts[0];
                        String intercepted_msg = msgparts[1];

                        LOGGER.warn(msg.getSender().getLocalName() + " reported a failure while trying to communicate with " + receiver);
                        if (CheckNotFoundRegistry(receiver) && CheckNotFoundRegistry(msg.getSender().getLocalName())) { //comprueba que el denunciante y el denunciado no esten en la blacklist de agentes no encontrados
                            get_timestamp(myAgent,receiver,"DeadAgentDetection");
                            LOGGER.info("Checking if " + receiver + " is alive and if the agent received the reported msg.");
                            String command = CheckMsgFIFO(receiver, intercepted_msg); //checkMsgFIFo se puede usar con order, batch y machine por ahora, el resto responde como un ping
                            System.out.println(command);
                            if (command.equals("msg_lost")) {         //Receiver vivo pero mensaje perdido
                                report_back(msg); //responde al fallo de comunicacion
                                if (!PingAgent(msg.getSender().getLocalName(),myAgent)) { //checks if the reporting agent is actually isolated.
                                    LOGGER.error(msg.getSender().getLocalName() + " agent who reported the error is isolated");
                                    sendACL(ACLMessage.INFORM, "D&D", "msg_lost", msg.getContent(),myAgent);
                                    String msgtoDD = msg.getSender().getLocalName();
//                                    if (msg.getSender().getLocalName().contains("ControlGatewayCont")) {
//                                        msgtoDD = msgtoDD + "/div/" + receiver;
//                                    }
                                    add_to_error_list("not_found", msg.getSender().getLocalName(), "", "", "");
                                    sendACL(ACLMessage.INFORM, "D&D", "not_found", msgtoDD,myAgent);
                                } else {
                                    LOGGER.info("Receiver and sender are online, although message is lost.");
                                    add_to_error_list("communication", msg.getSender().getLocalName(), receiver, intercepted_msg, "");
                                    sendACL(ACLMessage.INFORM, "D&D", "msg_lost", msg.getContent(),myAgent);
                                }
                            } else if (command.equals("msg_received")) {
                                report_back(msg);
                                LOGGER.info("Message arrived to agent. Ignore error.");

                            } else { //si no responde el receiver, se trata de un agente aislado o muerto
                                LOGGER.warn("No answer from " + receiver + ". Confirming error.");
                                report_back(msg);
                                String msgtoDD = receiver;
//                                if (receiver.contains("ControlGatewayCont")) {  //en algunos casos necesitamos saber el agente que ha denunciado el fallo para que pase a idle a traves del D&D
//                                    msgtoDD = msgtoDD + "/div/" + msg.getSender().getLocalName();
//                                }
                                get_timestamp(myAgent,receiver,"DeadAgentConfirmation");
                                add_to_error_list("not_found", receiver, "", "", "");
                                sendACL(ACLMessage.INFORM, "D&D", "not_found", msgtoDD,myAgent);
                            }

                        } else {
                            report_back(msg);//si ya estan denunciados no hace nada
                        }

                    } else if (msg.getOntology().equals("timeout")) { //error de tipo timeout

                        if (msg.getSender().getLocalName().contains("batch")) { //timeout enviado por un batch
                            report_back(msg); //confirmacion de recepcion de mensaje

                            String[] parts = msg.getContent().split("/");
                            String timeout_batch_id = parts[0];
                            String timeout_item_id = parts[1];

                                argument1 = "timeout";
                                LOGGER.warn(timeout_batch_id + " batch has thrown a timeout on item " + timeout_item_id);
                                if (!CheckNotFoundRegistry(msg.getSender().getLocalName())&&timeout_batch_id!=null) {
                                    LOGGER.warn("Timeout coming from a thread from " + msg.getSender().getLocalName() + " who is dead. Ignoring timeout");
                                } else {
                                    if (batch_and_machine.size() > 0) { //buscamos el batch en el listado y conseguimos el ID del machine agent responsable
                                        timeout_handler(msg, timeout_batch_id);
                                    } else {
                                        LOGGER.warn("No data available to check failure because process has either not started or already has finish. Most likely a dead agent reported timeout. Ignoring");
//                                        add_to_error_list(argument1,timeout_batch_id,"","","");
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
                                    LOGGER.error("Batch " + timeout_batch_id + " has no running replicas by now. No need to do nothing.");

                                } else {
                                    for (int k = 0; k < ErrorList.size(); k++) {
                                        if (ErrorList.get(k).get(0).equals("timeout") && ErrorList.get(k).get(2).equals(timeout_batch_id)) { //checkea si ha habido timeout en
                                            LOGGER.info("Batch " + timeout_batch_id + " has already reported a timeout. Ignoring error.");
                                            sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), "timeout_confirmed", timeout_batch_id,myAgent);
                                            add_timeout_error_flag = false; //no se añade el error porque ya existe por parte de batch
                                        } else if (ErrorList.get(k).get(0).equals("not_found") && ErrorList.get(k).get(1).equals(running_batch.getContent())) {
                                            LOGGER.info("Batch " + timeout_batch_id + " already reported as not found. Ignoring error.");
                                            sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), "timeout_confirmed", timeout_batch_id,myAgent);
                                            add_timeout_error_flag = false;
                                        }
                                    }
                                    if (add_timeout_error_flag) {
                                        LOGGER.info("No timeout errors found for batch " + timeout_batch_id + ". Pinging batch.");
                                        String batch_to_ping = running_batch.getContent();
                                        if (batch_to_ping != "") {

                                            if (PingAgent(batch_to_ping,myAgent)) {
                                                LOGGER.info("Batch " + batch_to_ping + " with reference " + timeout_batch_id + " found alive. Timeout of order registered.");
                                                argument1 = "timeout";
                                                if (batch_and_machine.size() > 0) { //buscamos el batch en el listado y conseguimos el ID del machine agent responsable
                                                    timeout_handler(msg, timeout_batch_id);
                                                }
                                            } else {
                                                get_timestamp(myAgent,batch_to_ping,"DeadAgentDetection");
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
                        for (int n = 0; n < allDelays.size(); n++) { //Se elimina de la lista de los delays el batch que ha terminado
                            if (allDelays.get(n).get(0).equals(finishing_batch)) {
                                allDelays.remove(n);
                                n--;
                                i--;
                            }
                        }
                        for (int o = 0; o < batch_and_machine.size(); o++) {
                            if (batch_and_machine.get(o).get(0).equals(finishing_batch)) { //se elimina la asignación batch-máquina
                                batch_and_machine.remove(o);
                                o--;
                                j--;
                            }
                        }
                        LOGGER.info("Batch " + finishing_batch + " finished.");
                    } else if (msg.getOntology().equals("delay")) {
                        String[] parts2 = msg.getContent().split("/");
                        String batch_id = parts2[0];
                        String ms_of_delay = parts2[1];
                        LOGGER.info("Batch " + batch_id + " started with " + ms_of_delay + " ms of delay");
                        ActualBatch = getDelays(msg.getContent()); //Añade el batch especificado con su correspondiente delay a la lista de delays

                        for (int l = 0; l < delay_asking_queue.size(); l++) {
                            if (ActualBatch.get(0).equals(delay_asking_queue.get(l))) {
                                sendACL(ACLMessage.INFORM, delay_asking_queue.get(l + 1), "askdelay", ActualBatch.get(1),myAgent);
                                delay_asking_queue.remove(l + 1);
                                delay_asking_queue.remove(l);
                                LOGGER.info("Informing batch " + msg.getContent());
                            }
                        }
                        allDelays.add(i, ActualBatch);
                        i++;
                        String sender = msg.getSender().getLocalName();
                        ArrayList<String> temp = BatchAndMachines(msg.getContent(), sender); //Crea un listado de agentes maquina con los batch que tengan asignados
                        batch_and_machine.add(j, temp);
                        j++;
                    } else if (msg.getOntology().equals("asset_state")) { //recibe ping de vuelta del asset (solo para testing)
                        LOGGER.info("Recieved asset state out of the timeout: " + msg.getContent());
                    } else if (msg.getOntology().equals("reported_on_dead_node")) {
                        get_timestamp(myAgent,msg.getContent(),"DeadAgentConfirmation");
                        add_to_error_list("not_found", msg.getContent(), "", "", "");
                        LOGGER.info("D&D reported a dead agent (" + msg.getContent() + "). Added to error list.");
                    }
                }
                if (msg.getPerformative() == ACLMessage.REQUEST) { //Se recibe algun tipo de petición
                    if (msg.getOntology().equals("askrelationship")) {
                        if (msg.getContent().contains("machine")) {
                            for (int u = 0; u < batch_and_machine.size(); u++) {
                                if (batch_and_machine.get(u).get(1).equals(msg.getContent())) {
                                    try {
                                        ACLMessage reply = sendCommand(myAgent, "get * category=batch reference=" + batch_and_machine.get(u).get(0), "QoS");
                                        ACLMessage reply2 = sendCommand(myAgent, "get * category=batchAgent parent=" + reply.getContent() + " state=running", "QoS");
                                        sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), msg.getOntology(), reply2.getContent(),myAgent);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            try {
                                String parent = "";
                                if (msg.getContent().contains("batchagent")) { //si es un batchagent hay que buscar primero su parent
                                    ACLMessage reply = sendCommand(myAgent, "get " + msg.getContent() + " attrib=parent", "QoS");
                                    parent = reply.getContent();
                                } else if (msg.getContent().contains("batch")) {  //con el parent podemos obtener la referencia directamente
                                    parent = msg.getContent();
                                }
                                ACLMessage reference = sendCommand(myAgent, "get " + parent + " attrib=reference", "QoS");
                                for (int u = 0; u < batch_and_machine.size(); u++) {
                                    if (batch_and_machine.get(u).get(0).equals(reference.getContent())) {
                                        sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), msg.getOntology(), batch_and_machine.get(u).get(1),myAgent);
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
                            for (int k = 0; k < allDelays.size(); k++) {
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
                            myAgent.send(reply);
                            LOGGER.info("Informing batch " + asking_batch);
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
                            sendACL(7, msg.getSender().getLocalName(), msg.getOntology(), concatenated_errors,myAgent);
                        }
                    }
                }
            }
        }

        private void timeout_handler(ACLMessage msg, String timeout_batch_id) {
            for (int k = 0; k < batch_and_machine.size(); k++) {
                if (batch_and_machine.get(k).get(0).equals(timeout_batch_id)) {
                    String MA = batch_and_machine.get(k).get(1);
                    argument2 = timeout_batch_id;
                    argument3 = "";
                    if (CheckNotFoundRegistry(MA)) { //comprobamos que no este denunciado ya

                        String ping_result = PingAsset(MA);
                        if (!ping_result.contains("plc") && ping_result.contains("_down")) { //GW o machine caidos, se denuncia a D&D
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
                                add_to_error_list("not_found", dead_agent[0], "", "", ""); //añadimos agente caido
                                sendACL(ACLMessage.INFORM, "D&D", "not_found", dead_agent[0],myAgent);
                            }

                        } else if (ping_result.equals("working")) { //el sistema funciona correctamente, se puede haber retrasado por un fallo. Registramos el timeout, lo reseteamos y no hacemos nada más.
                            LOGGER.info("Everything OK theoretically. Reset timeout.");
                            sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), msg.getOntology(), "reset_timeout",myAgent);
                            argument4 = MA + "->OK";
                            argument5 = "GW->OK";
                            add_to_error_list("timeout", argument2, argument3, argument4, argument5);  //aunque este todoo ok hay que añadir el timeout a la lista de errores
                        } else { //el sistema está parado o en error
                            sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), msg.getOntology(), "confirmed_timeout",myAgent);
                            argument4 = MA + "->OK";
                            argument5 = "GW->OK";
                            add_to_error_list("timeout", argument2, argument3, argument4, argument5);
                        }

                    } else {
                        argument4 = MA + "->NO OK";
                        argument5 = "?";
                        add_to_error_list("timeout", argument2, argument3, argument4, argument5);
                        sendACL(ACLMessage.INFORM, msg.getSender().getLocalName(), msg.getOntology(), "confirmed_timeout",myAgent);
                    }
                }
            }
        }

        private ArrayList<String> getDelays(String data) {

            ArrayList<String> batchdelay = new ArrayList<String>();//Creamos un Arraylist para los delay de cada batch.
            String[] parts = data.split("/");
            String part1 = parts[0]; // BatchID
            String part2 = parts[1]; // Delay in minutes
            batchdelay.add(part1);
            batchdelay.add(part2);
            return batchdelay;
        }

        private ArrayList<String> BatchAndMachines(String data, String sender) {
            ArrayList<String> MachineAgentList = new ArrayList<String>(); //Creamos un Arraylist para los machine agent que ejecutan cada batch.
            String[] parts = data.split("/");
            String batch = parts[0]; // BatchID
            MachineAgentList.add(batch);
            MachineAgentList.add(sender);


            return MachineAgentList;
        }

//        private boolean PingAgent (String name){  //checkea el estado de los agentes de aplicación, recurso y gateway
//
//            boolean state;
//
//            int n=SearchAgent(name);
//            if(n>0){
//                AID Agent_to_ping_ID=new AID(name,false);
//                ACLMessage ping=new ACLMessage(ACLMessage.REQUEST);
//                ping.setOntology("ping");
//                ping.addReceiver(Agent_to_ping_ID);
//                ping.setContent("");
//                send(ping);
//                sendACL();
//                ACLMessage echo=blockingReceive(pingtemplate,500);
//                if(echo!=null) {
//                    LOGGER.info(name+" answered on time.");
//                    state=true;
//                }else{
//                    LOGGER.error(name+" did not answer on time. Confirming failure.");
//                    state=false;
//                }
//            }else{
//                state=false;
//            }
//            return state;
//        }

        public String PingAsset(String machine) {

            int n = SearchAgent(machine,myAgent);
            if (n > 0) {
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
                    String[] content_div = echo.getContent().split("\n");
                    String[] gw_state = content_div[0].split(":");
                    System.out.println("Ping result: ");
                    if (gw_state[1].contains("DOWN")) {
                        System.out.println(myAgent.getLocalName() + "->" + machine + "-/>" + content_div[0] + "->?");
                        return gw_state[0] + "_down";
                    } else {
                        String[] plc_state = content_div[1].split(":");
                        if (plc_state[1].contains("ENW")) {
                            System.out.println(myAgent.getLocalName() + "->" + machine + "->" + content_div[0] + "->PLC(IDLE)-/>Process");
                            return "error_while_not_working";
                        } else if (plc_state[1].contains("EW")) {
                            System.out.println(myAgent.getLocalName() + "->" + machine + "->" + content_div[0] + "->PLC(RUN)-/>Process");
                            return "error_while_working";
                        } else if (plc_state[1].contains("NW")) {
                            System.out.println(myAgent.getLocalName() + "->" + machine + "->" + content_div[0] + "->PLC(IDLE)->Process");
                            return "not_working";
                        } else if (plc_state[1].contains("W")) {
                            System.out.println(myAgent.getLocalName() + "->" + machine + "->" + content_div[0] + "->PLC(RUN)->Process");
                            return "working";
                        } else {
                            System.out.println(myAgent.getLocalName() + "->" + machine + "->" + content_div[0] + "-/>PLC(?)->?");
                            return "plc_down";
                        }
                    }
                } else {
                    System.out.println(myAgent.getLocalName() + "-/>" + machine + "->?");
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

        public void add_to_error_list(String type, String arg2, String arg3, String arg4, String arg5) {
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


        public Date getactualtime() {
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

        public boolean CheckNotFoundRegistry(String Agent) {  //comprueba que el agente indicado no ha sido haya reportado como no encontrado.
//        boolean flag=true;
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