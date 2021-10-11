package es.ehu.domain.manufacturing.test;

import es.ehu.domain.manufacturing.utilities.DiagnosisAndDecision;
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
import java.util.concurrent.TimeoutException;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.AMSService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class QoSManagerAgent extends Agent {

    private Agent myAgent=this;
    private int agent_found_qty=0;
    private int gateway_found_qty=0;
    private ArrayList<String> ActualBatch = new ArrayList<String>();
    private ArrayList<ArrayList<String>> ErrorList=new ArrayList<ArrayList<String>>();
    private int i=0;
    private ArrayList<ArrayList<String>> allDelays = new ArrayList<ArrayList<String>>();
    private MessageTemplate delaytemplate;
    private int j=0,n=0;
    private int l=0;
    private ArrayList<ArrayList<String>> batch_and_machine = new ArrayList<ArrayList<String>>();
    private ArrayList<String> delay_asking_queue=new ArrayList<String>();
    private ACLMessage machinenbr=null;
    static final Logger LOGGER = LogManager.getLogger(QoSManagerAgent.class.getName());
    private boolean add_timeout_error_flag=true,pong=false,add_communication_error_flag=true,confirmed_isol=false;
    private String argument1="",argument2="",argument3="",argument4="",argument5="",argument6="",now="";
    public AID DDid=new AID("D&D",false);
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

                if(msg.getPerformative()==ACLMessage.FAILURE){ //Se recibe un posible error

                    if(msg.getOntology().equals("acl_error")){ //error de comunicación
                        String[] msgparts=msg.getContent().split("/div/");
                        String receiver=msgparts[0];
                        String intercepted_msg=msgparts[1];

                        LOGGER.warn(msg.getSender().getLocalName()+" reported a failure while trying to communicate with "+ receiver);
                        add_communication_error_flag=true;
                        confirmed_isol=false;

                            for(int o=0;o<ErrorList.size();o++){ //checkea si el error de comunicación se ha repetido
                                if (ErrorList.get(o).get(0).equals("communication")) { //comprueba si se ha repetido un error de comunicación entre los dos agentes y en dicho caso confirma el error.
                                    if(ErrorList.get(o).get(2).equals(msg.getSender().getLocalName())&&ErrorList.get(o).get(3).equals(receiver)){
                                        LOGGER.info("Communication error repeated between this two agents. Confirm isolated agent.");
                                        sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),msg.getOntology(),receiver+"/confirmed");
                                        confirmed_isol=true;
                                        add_to_error_list("communication",msg.getSender().getLocalName(),receiver,intercepted_msg,"");
                                    }
                                }
                            }
                            if(!confirmed_isol) { //si es la primera vez que se lanza el error de comunicación
                                LOGGER.info("Checking if " + receiver + " is alive and if the agent received the reported msg.");
                                String command=CheckMsgFIFO(msg.getSender().getLocalName(),msg.getContent());
                                System.out.println(command);
                                n=SearchAgent(receiver);
                                if (command.equals("msg_lost")&&n==1) {//Receiver alive but msg lost

                                    sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),msg.getOntology(),receiver+"/confirmed");
                                    pong=PingAgent(msg.getSender().getLocalName());
                                    n=SearchAgent(msg.getSender().getLocalName());
                                    if(!pong&&n!=1){ //is posible that the reporting agent is actually isolated. Checking
                                        LOGGER.error(msg.getSender().getLocalName()+" agent who reported an error is probably isolated");
                                        LOGGER.warn("Receiver must ignore sender messages. D&D should bridge.");
                                        //programar respuesta aqui (BRIDGE)
                                    }else{
                                        LOGGER.info("Receiver and sender are online, although message is lost. Report error and ask human intervention.");

                                        //programar respuesta aqui(AVISO)
                                    }
                                } else if(command.equals("msg_received")&&n==1){

                                    sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),msg.getOntology(),receiver+"/ignore");
                                    LOGGER.info("Message arrived to agent. Ignore error.");

                                }else { //si no responde el receiver, se trata de un agente aislado

                                    LOGGER.warn("No answer from "+receiver+". Confirming error.");
                                    sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),msg.getOntology(),receiver+"/confirmed");
                                    if(msg.getSender().getLocalName().contains("machine")&&receiver.contains("ControlGatewayCont")){
                                        LOGGER.warn(msg.getSender().getLocalName()+ " agent should idle.");
                                        sendACL(ACLMessage.REQUEST,msg.getSender().getLocalName(),"control","setstate idle");
                                    }
                                }
                                add_to_error_list("communication",msg.getSender().getLocalName(),receiver,intercepted_msg,"");
                            }


                    } else if(msg.getOntology().equals("timeout")){ //Se recibe aviso de que ha habido un timeout

                        if(msg.getSender().getLocalName().contains("batch")){ //timeout enviado por un batch
                            String[] parts=msg.getContent().split("/");
                            String timeout_batch_id=parts[0];
                            String timeout_item_id=parts[1];
                            for(int m=0; m<ErrorList.size();m++) { //Primero comprueba que el error no esté repetido
                                if (ErrorList.get(m).get(0).equals("timeout")) {
                                    if (ErrorList.get(m).get(2).equals(timeout_batch_id)) {
                                        if(ErrorList.get(m).get(3).equals(timeout_item_id)){
                                            LOGGER.error("Timeout repeated on same batch and item, confirming failure this time");
                                            sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),msg.getOntology(),"confirmed_timeout");
                                            add_timeout_error_flag=false;

                                            for (int p = 0; p < batch_and_machine.size(); p++) {
                                                if (batch_and_machine.get(p).get(0).equals(timeout_batch_id)) {
                                                    String MA = batch_and_machine.get(p).get(1);
                                                    sendACL(ACLMessage.REQUEST,MA,"control","setstate idle");
                                                    //Poner en modo idle el agente máquina responsable del batch para que no ordene la ejecución de más batchs
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if(add_timeout_error_flag) {
//                                ErrorList.add(l, new ArrayList<>());  //Añade al listado de errores el timeout
//                                ErrorList.get(l).add(0, "timeout");
                                argument1="timeout";
                                LOGGER.warn(timeout_batch_id + " batch has thrown a timeout on item " + timeout_item_id);
                                if (batch_and_machine.size() > 0) { //buscamos el batch en el listado y conseguimos el ID del machine agent responsable
                                    for (int k = 0; k < batch_and_machine.size(); k++) {
                                        if (batch_and_machine.get(k).get(0).equals(timeout_batch_id)) {
                                            String MA = batch_and_machine.get(k).get(1);

                                            argument2=timeout_batch_id;
                                            argument3=timeout_item_id;
                                            pong = PingAgent(MA); // hacemos ping al agente máquina para comprobar su estado

                                            if (pong) {  //Si pong es 1 el agente ha contestado
//                                                ErrorList.get(l).add(3, MA + "->OK");
                                                argument4=MA + "->OK";
                                            } else {
//                                                System.out.println("The machine agent " + MA + " which was executing batch number " + timeout_batch_id + " is not alive"); //Si no hay coincidencias el agente máquina no esta activo
//                                                ErrorList.get(l).add(3, MA + "->NO OK");
                                                argument4=MA + "->NO OK";
                                            }
                                            try {
                                                machinenbr = sendCommand(myAgent, "get " + MA + " attrib=id", "name"); //consultamos la id de la estacion al SA para saber que gateway agent le corresponde. Si es 41 seria ControlGatewayCont4.
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            char[] ch = new char[machinenbr.getContent().length()];
                                            ch[0] = machinenbr.getContent().charAt(0);
                                            pong=PingAgent("ControlGatewayCont" + ch[0]);
//                                        gateway_found_qty = SearchAgent("ControlGatewayCont" + ch[0]); //Buscamos a su vez que el agente gateway este vivo.
                                            if (pong) {

                                                argument5="ControlGatewayCont" + ch[0] + "->OK";
                                            } else {

                                                argument5="ControlGatewayCont" + ch[0] + "->NO OK";
                                            }
                                            if (argument4.equals(MA + "->OK") && argument5.equals("ControlGatewayCont" + ch[0] + "->OK")) {
                                                LOGGER.info("All agents online, asking asset state...");
                                                sendACL(ACLMessage.REQUEST,"ControlGatewayCont" + ch[0],"check_asset","How are you feeling PLC?");
                                                ACLMessage state=blockingReceive(MessageTemplate.MatchOntology("asset_state"),500);
                                                if(state!=null&&!state.getContent().contains("Error")){
                                                        LOGGER.info(state.getContent());
                                                        System.out.println("Everything OK theoretically. Lengthening timeout.");
                                                        sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),msg.getOntology(),"reset_timeout");
                                                }else{
                                                    LOGGER.error("Timeout confirmed");
                                                    sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),msg.getOntology(),"confirmed_timeout");
                                                }
                                            } else {
                                                LOGGER.error("Timeout confirmed");
                                                sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),msg.getOntology(),"confirmed_timeout");
                                            }

                                            add_to_error_list(argument1,argument2,argument3,argument4,argument5);

                                        }
                                    }
                                } else {
                                    LOGGER.warn("No data available to check failure");

                                    add_to_error_list(argument1,timeout_batch_id,"","","");

                                }

                            }
                            add_timeout_error_flag=true;

                            /**
                             * En caso de lanzar un timeout en el ORDER pueden darse 3 casos:
                             *
                             * -> Que el batch ya haya lanzado timeout, en cuyo caso se confirma timeout y no se hace nada más porque la máquina ya debería estar idle
                             * -> Que el batch no haya lanzado timeout y no responda a ping, en cuyo caso se anota como error genérico
                             * -> Que el batch no haya lanzado timeout y responda a ping, en cuyo caso se anota como error de timeout
                             * */

                        } else if(msg.getSender().getLocalName().contains("order")){ //es un timeout enviado por un order agent.
                            String[] parts=msg.getContent().split("/");
                            String timeout_order_id=parts[0];
                            String timeout_batch_id=parts[1];
                            LOGGER.warn(timeout_order_id+" order has thrown timeout on batch "+timeout_batch_id);
                            for(int k=0;k<ErrorList.size();k++){
                                if(ErrorList.get(k).get(0).equals("timeout")&&ErrorList.get(k).get(3).equals(timeout_batch_id)){ //checkea si ha habido timeout en
                                    LOGGER.info("Batch "+timeout_batch_id+" has already reported a timeout. Ignoring error.");
                                    sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),"timeout_confirmed",timeout_batch_id);
                                    add_timeout_error_flag=false; //no se añade porque ya existe
                                }
                            }
                            if(add_timeout_error_flag){
                                LOGGER.info("No timeout errors found for batch "+timeout_batch_id+". Pinging batch.");
                                int found=SearchAgent("batchagent");
                                String batch_to_ping="";
                                for(int n=1;n<=found;n++) {
                                    try {
                                        ACLMessage t = sendCommand(myAgent, "get " + "batchagent" + n + " attrib=parent", "name");
                                        ACLMessage b_reference=sendCommand(myAgent, "get " + t.getContent()+ " attrib=reference", "Reference");
                                        if (b_reference.getContent().equals(timeout_batch_id)) {
                                            batch_to_ping = "batchagent" + n;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                    if(batch_to_ping!=""){
                                        boolean pong= PingAgent(batch_to_ping);
                                        if (pong) {
                                            LOGGER.info("Batch "+ batch_to_ping+" with reference "+timeout_batch_id+ " found alive. Timeout of order registered.");
//                                            ErrorList.add(l, new ArrayList<>());
//                                            ErrorList.get(l).add(0, "timeout");
                                                argument1="timeout";
                                            if (batch_and_machine.size() > 0) { //buscamos el batch en el listado y conseguimos el ID del machine agent responsable
                                                for (int k = 0; k < batch_and_machine.size(); k++) {
                                                    if (batch_and_machine.get(k).get(0).equals(timeout_batch_id)) {

                                                        String MA = batch_and_machine.get(k).get(1);
                                                        pong = PingAgent(MA); // hacemos ping al agente máquina para comprobar su estado
                                                        argument2=timeout_batch_id;
                                                        argument3="not known";
//                                                        ErrorList.get(l).add(1, timeout_batch_id);
//                                                        ErrorList.get(l).add(2, "not known");

                                                        if (pong) {  //Si pong es 1, el agente ha contestado
//                                                            ErrorList.get(l).add(3, MA + "->OK");
                                                            argument4=MA + "->OK";
                                                        } else {
//                                                            ErrorList.get(l).add(3, MA + "->NO OK");
                                                            argument4=MA + "->NO OK";
                                                        }
                                                        try {
                                                            machinenbr = sendCommand(myAgent, "get " + MA + " attrib=id", "name"); //consultamos la id de la estacion al SA para saber que gateway agent le corresponde. Si es 41 seria ControlGatewayCont4.
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        char[] ch = new char[machinenbr.getContent().length()];
                                                        ch[0] = machinenbr.getContent().charAt(0);
                                                        pong = PingAgent("ControlGatewayCont" + ch[0]);

                                                        if (pong) {
//                                                            System.out.println("Gateway agent of " + MA + " found alive: " + "ControlGatewayCont" + ch[0]);
//                                                            ErrorList.get(l).add(4, "ControlGatewayCont" + ch[0] + "->OK");
                                                            argument5="ControlGatewayCont" + ch[0] + "->OK";
                                                        } else {
//                                                            System.out.println("No gateway agent found for " + MA + ": ControlGatewayCont" + ch[0]);
//                                                            ErrorList.get(l).add(4, "ControlGatewayCont" + ch[0] + "->NO OK");
                                                            argument5="ControlGatewayCont" + ch[0] + "->NO OK";
                                                        }
                                                        if (argument4.equals(MA + "->OK") && argument5.equals("ControlGatewayCont" + ch[0] + "->OK")) {
                                                            System.out.println(MA + "->OK");
                                                            System.out.println("ControlGatewayCont" + ch[0] + "->OK");
                                                            LOGGER.warn("Batch agent is not throwing timeouts. Probably last item.");

                                                        } else {
                                                            LOGGER.error("Timeout confirmed");
                                                        }

                                                        add_to_error_list(argument1,argument2,argument3,argument4,argument5);
                                                    }
                                                }
                                            }
                                        }else{
                                            LOGGER.error("Batch agent found by AMS but not responding to pong. Possible zombie agent.");
                                            add_to_error_list("zombie",batch_to_ping,"","","");
                                        }
                                    }else{
                                        LOGGER.error("Batch agent not found by AMS. Probably dead or isolated long ago.");
                                        add_to_error_list("not_found",batch_to_ping,"","","");
                                    }
                                sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),"timeout_confirmed",timeout_batch_id);
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
                        System.out.println("Batch "+finishing_batch+" finished.");
                    }
                    if (msg.getOntology().equals("delay")) {
                        String[] parts2=msg.getContent().split("/");
                        String batch_id=parts2[0];
                        String ms_of_delay=parts2[1];
                        LOGGER.info("Batch" +batch_id+" started with "+ms_of_delay+" ms of delay");
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
                            flag = true;
                            reply.addReceiver(msg.getSender());
                            reply.setOntology(msg.getOntology());
                            send(reply);
                            LOGGER.info("Informing batch");
                        }
                    }else if(msg.getOntology().equals("errorlist")&&msg.getSender().getLocalName().equals("D&D")){
                        String concatenated_errors="";
                        for(int q=0;q<ErrorList.size();q++){
                            if(q!=0){
                                concatenated_errors = concatenated_errors +"/err/";
                            }
                            for(int r=q;r<ErrorList.get(q).size();r++) {
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
        ErrorList.get(l).add(2, arg3);
        ErrorList.get(l).add(4, arg4);
        ErrorList.get(l).add(5, arg5);
        l++;

    }
    protected Date getactualtime(){
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

}