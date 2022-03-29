package es.ehu.domain.manufacturing.agents.functionality;

import com.google.gson.Gson;
import es.ehu.domain.manufacturing.utilities.StructBatchAgentState;
import es.ehu.domain.manufacturing.utilities.StructOrderAgentState;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.template.interfaces.AvailabilityFunctionality;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.sql.SQLOutput;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

public class Order_Functionality extends DomApp_Functionality implements BasicFunctionality, AvailabilityFunctionality {

    private static final long serialVersionUID = 1L;
    private MWAgent myAgent;
    private ArrayList<ACLMessage> posponed_msgs_to_mplan=new ArrayList<ACLMessage>();
    private List<String> elementsToCreate = new ArrayList<>();
    private int chatID = 0; // Numero incremental para crear conversationID
    private ACLMessage orderName=new ACLMessage();
    private String firstState;
    private String redundancy;
    private String parentAgentID, orderNumber;
//    private ArrayList<String> myReplicasID = new ArrayList<>();
//    private ArrayList<AID> sonAgentID = new ArrayList<>(); // lista con los nombres de los agentes de los que es padre
    private ArrayList<String> sonAgentID = new ArrayList<>();
    private Integer batchIndex = 1;
    private Boolean newBatch = true, firstTime = true;
    private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> batchTraceability = new ArrayList<>();
    private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> deserializedMessage = new ArrayList<>(); // Mensaje recibido desde el batchAgent deserializado
    private String mySeType;
    private MessageTemplate template, template2,templateFT,template3,template4,template5, template6,template7;
    private volatile String orderreference=null;
    private volatile String raw_ft=null;
    private volatile AID QoSID = new AID("QoSManagerAgent", false);
    private volatile ArrayList<String> online_batches=new ArrayList<String>();
    private volatile String batch_to_take_down="";
    private volatile Date new_expected_finish_time=null;
    private volatile boolean QoSresponse_flag=false;
    private volatile String batch_to_update=null;
    private ArrayList<ArrayList<String>> batch_last_items_ft=new ArrayList<ArrayList<String>>();
    private int convIDcnt=0;


    class Ordertimeout extends Thread{

        private boolean exit_timeout=false,QoSreported=false;
        private Date expected_finish_time;
        private String batch;

        public Ordertimeout(Date expected_finish_time, String batch){
            this.expected_finish_time=expected_finish_time;
            this.batch=batch;
        }

        public void run() {
            System.out.println("***********************************"+batch+" batch expected finish time: "+expected_finish_time+"***********************************");
            while (!exit_timeout) {
                while (expected_finish_time.after(getactualtime()) && !exit_timeout) {

                    if (batch_to_take_down.equals(batch)) {
                        exit_timeout = true;
                    }
                    if (new_expected_finish_time != null&&batch_to_update!=null) { //comprueba que se actualice
                        if(batch_to_update.equals(batch)) {
                            expected_finish_time = new_expected_finish_time;
                            System.out.println("BATCH FINISH-TIME UPDATED");
                            System.out.println("***********************************"+batch+" batch expected finish time: "+expected_finish_time+"***********************************");
                            //System.out.println("Batch " + batch + " finish time updated to: " + expected_finish_time);
                            new_expected_finish_time = null;
                            batch_to_update=null;
                        }
                    }
                }
                if (!expected_finish_time.after(getactualtime()) && !exit_timeout) {
                    System.out.println("Order has thrown a timeout on batch " + batch + ". Checking with QoS.");
                    ACLMessage timeout_report= sendACLMessage(ACLMessage.FAILURE, QoSID, "timeout", "order_timeout_"+batch, batch.substring(0, 2) + "/" + batch, myAgent); //avisa al QoS de fallo por timeout
                    myAgent.AddToExpectedMsgs(timeout_report);
                    exit_timeout=true;
                }
                if (batch_to_take_down.equals(batch)) { //cuando la variable volatil batch_to_take_down coincide con el batch del timeout el timeout finaliza porque el batch ha terminado
                    exit_timeout = true;
                    batch_to_take_down=""; //resetea la variable para el siguiente batch (evita bugs)
                }
            }
            batch_to_take_down = "";
            System.out.println("Batch "+batch+" timeout finished.");
        }
    }


    @Override
    public void setState(String state) {

        Gson gson = new Gson();
        StructOrderAgentState OAstate = gson.fromJson(state, StructOrderAgentState.class);
        batchTraceability=OAstate.getbatchTraceability();
        sonAgentID=OAstate.getsonAgentID();
        firstTime=OAstate.getfirstTime();
        parentAgentID=OAstate.getparenAgentID();
        myAgent.replicas=OAstate.getreplicas();
        batch_last_items_ft=OAstate.getFT();
        newBatch=OAstate.getnewBatch();
        batchIndex=OAstate.getbatchIndex();

//
//        ArrayList<ArrayList<ArrayList<ArrayList<String>>>> Traceability=new ArrayList<>();
//        ArrayList<String> remaining=new ArrayList<String>();
//        ArrayList<ArrayList<String>> FT=new ArrayList<>();
//        ArrayList<String> replicas=new ArrayList<String>();
//        String parts1[] =state.split("/div0/"); //el divisor 0 divide los argumentos y el resto se usan para los arraylist
//        String productTraceabilityConc = parts1[0]; //trazabilidad concatenada
//        String remainingConc = null;
//        if (parts1[1] != null&&parts1[1] != "") {
//            remainingConc = parts1[1]; //solo si quedan acciones/SonAgentIDs
//        }
//        String firstimeString = parts1[2]; //primera vez
//
//        String FinishTimesConc=parts1[3]; //finish times concatenados (cada agente de aplicaci�n lleva un formato)
//        parentAgentID=parts1[4]; 					//parent
//        String replicasConc=parts1[5];		//replicas del agente
//        String Snewbatch=parts1[6];
//        String Sbatchindex=parts1[7];
//        newBatch=Boolean.parseBoolean(Snewbatch);
//        batchIndex=Integer.parseInt(Sbatchindex);
//
//
//        String parts2[] = productTraceabilityConc.split("/div1/"); //construye la trazabilidad
//        for (int i = 0; i < parts2.length; i++) {
//            Traceability.add(i, new ArrayList<ArrayList<ArrayList<String>>>());
//            String parts3[] = parts2[i].split("/div2/");
//            for (int j = 0; j < parts3.length; j++) {
//                Traceability.get(i).add(j, new ArrayList<ArrayList<String>>());
//                String parts4[] = parts3[j].split("/div3/");
//                for (int k = 0; k < parts4.length; k++) {
//                    Traceability.get(i).get(j).add(k, new ArrayList<String>());
//                    String parts5[] = parts4[k].split("/div4/");
//                    for (int l = 0; l < parts5.length; l++) {
//                        Traceability.get(i).get(j).get(k).add(parts5[l]);
//                    }
//                }
//            }
//        }
//        firstTime = Boolean.parseBoolean(firstimeString);
//        if(!firstTime){
//            batchTraceability=Traceability;
//        }
//
//        if (remainingConc != null) {    //construye los sonagentID o actionlist
//            String parts6[] = remainingConc.split("/div1/");
//            for (int i = 0; i < parts6.length; i++) {
//                remaining.add(parts6[i]);
//            }
//        }
//        sonAgentID=remaining;
//
//        String parts7[]=FinishTimesConc.split("/div1/");
//        for(int i=0;i<parts7.length;i++) {
//            FT.add(i, new ArrayList<String>());
//            String parts8[] = parts7[i].split("/div2/");
//            for (int j = 0; j < parts8.length; j++) {
//                FT.get(i).add(parts8[j]);
//            }
//        }
//        batch_last_items_ft=FT;
//        String parts9[]=replicasConc.split("/div1/");
//        for(int k=0;k<parts9.length;k++){
//            if(!parts9[k].equals(myAgent.getLocalName())){
//                replicas.add(parts9[k]);
//            }
//        }
//        myAgent.replicas=replicas;

    }
    @Override
    public String getState(){
//        myAgent.antiloopflag=true;
//        String state="";
//
//        for(int i=0;i<batchTraceability.size();i++){
//            if(i!=0){
//                state=state+"/div1/";
//            }
//            for(int j=0;j<batchTraceability.get(i).size();j++){
//                if(j!=0){
//                    state=state+"/div2/";
//                }
//                for(int k=0;k<batchTraceability.get(i).get(j).size();k++){
//                    if(k!=0){
//                        state=state+"/div3/";
//                    }
//                    for(int l=0;l<batchTraceability.get(i).get(j).get(k).size();l++){
//                        if(l!=0){
//                            state=state+"/div4/";
//                        }
//                        state=state+batchTraceability.get(i).get(j).get(k).get(l);
//                    }
//                }
//            }
//        }
//        state=state+"/div0/";
//        for(int i=0;i<sonAgentID.size();i++){
//            if(i!=0){
//                state=state+"/div1/";
//            }
//            state=state+sonAgentID.get(i);
//        }
//        state=state+"/div0/"+String.valueOf(firstTime)+"/div0/";
//
//        for(int i=0;i<batch_last_items_ft.size();i++){ //concatena los FT de los item
//            if(i!=0){
//                state=state+"/div1/";
//            }
//            for(int j=0;j<batch_last_items_ft.get(i).size();j++){
//                if(j==0){
//                    state=state+batch_last_items_ft.get(i).get(j);
//                }else{
//                    state=state+"/div2/"+batch_last_items_ft.get(i).get(j);
//                }
//            }
//        }
//        state=state+"/div0/"+parentAgentID+"/div0/";
//
//        try {   //realiza la consulta al sa para tener la lista de replicas actualizada.
//
//            String[] replicas=new String[1];
//            if(redundancy.equals("1")){
//                replicas[0] =" ";
//            }else{
//                ACLMessage parent = sendCommand(myAgent, "get " + myAgent.getLocalName() + " attrib=parent", "GetOrderParent");
//                ACLMessage replicasACL = sendCommand(myAgent, "get * state=tracking parent=" + parent.getContent(), "GetOrderUpdatedReplicas");
//                if(replicasACL.equals("")){
//                    replicas[0] =" ";
//                }else{
//                    if(replicasACL.getContent().contains(",")){
//                        replicas= replicasACL.getContent().split(",");
//                    }else{
//                        replicas[0] = replicasACL.getContent();
//                    }
//                }
//            }
//
//            for(int i=0; i<replicas.length;i++){
//                if(i==0){
//                    state=state+replicas[i];
//                }else{
//                    state=state+"/div1/"+replicas[i];
//                }
//            }
//            state=state+"/div0/"+String.valueOf(newBatch);
//            state=state+"/div0/"+String.valueOf(batchIndex);
//
//
//        }catch  (Exception e) {
//            e.printStackTrace();
//        }
//        myAgent.antiloopflag=false;
//
//        String state=null;

        myAgent.replicas=update_tracking_replicas();
        StructOrderAgentState OAstate=new StructOrderAgentState();
        OAstate.setbatchTraceability(batchTraceability);
        OAstate.setsonAgentID(sonAgentID);
        OAstate.setfirstTime(firstTime);
        OAstate.setparenAgentID(parentAgentID);
        OAstate.setreplicas(myAgent.replicas);
        OAstate.setFT(batch_last_items_ft);
        OAstate.setnewBatch(newBatch);
        OAstate.setbatchIndex(batchIndex);

        Gson gson = new Gson();
        String state=gson.toJson(OAstate);

        return state;
    }

    @Override
    public Void init(MWAgent myAgent) {

        this.template = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("Information")),MessageTemplate.MatchConversationId("ItemsInfo"));
        this.template2 = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId("Shutdown"));
        this.template3 = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("timeout_confirmed"));
        this.template4 = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("update_timeout"));
        this.template5=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("delay"));
        this.myAgent = myAgent;

        myAgent.get_timestamp(myAgent,"CreationTime");
        // Crear un nuevo conversationID
        String conversationId = myAgent.getLocalName() + "_" + chatID++;

        firstState = getArgumentOfAgent(myAgent, "firstState");
        redundancy = getArgumentOfAgent(myAgent, "redundancy");
        parentAgentID = getArgumentOfAgent(myAgent, "parentAgent");
        mySeType = getMySeType(myAgent, conversationId);

        if (firstState.equals("running")) {

            // Cambiar a estado bootToRunning para que los tracking le puedan enviar mensajes
            String query = "set " + myAgent.getLocalName() + " state=bootToRunning";
            try {
                sendCommand(myAgent, query, conversationId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Hashtable<String, String> attributes = new Hashtable<String, String>();
            attributes.put("seClass", "es.ehu.domain.manufacturing.agents.BatchAgent");

            seStart(myAgent.getLocalName(), attributes, conversationId);

            // TODO primero comprobara que todas las replicas (tracking) se han creado correctamente, y despues comprobara los batches
            // Es decir, antes de avisar a su padre que esta creado, comprueba las replicas y despues los batches
            // Le a�adimos un comportamiento para que consiga todos los mensajes que le van a enviar los batch cuando se arranquen correctamente

            Object[] result = processACLMessages(myAgent, mySeType, elementsToCreate, conversationId, redundancy, parentAgentID);
            sonAgentID = (ArrayList<String>) result[1];

            myAgent.replicas = (ArrayList<String>) result[0];

            templateFT=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchOntology("Ftime_order_ask"));
            try {
                orderName = sendCommand(myAgent, "get " + myAgent.getLocalName() + " attrib=parent", "name"); //consigue el nombre del order
                ACLMessage reference = sendCommand(myAgent, "get " + orderName.getContent() + " attrib=reference", "Reference"); //consigue la referencia del order
                AID plannerID = new AID("planner", false);
                orderreference=reference.getContent();
                sendACLMessage(16, plannerID,"Ftime_order_ask", "finnish_time", reference.getContent(), myAgent ); //pide el finish time de cada item al planner
                ACLMessage finishtime= myAgent.blockingReceive(templateFT); //recibe los finish times concatenados
                myAgent.msgFIFO.add((String) finishtime.getContent());
                System.out.println(finishtime.getContent());
                raw_ft=finishtime.getContent();
                batch_last_items_ft=batch_finish_times(raw_ft);

            }catch(Exception e) {
                System.out.println(myAgent.getLocalName()+ " ERROR. Something happened at some point during initialization");
                e.printStackTrace();
            }
            String currentState = (String) ((AvailabilityFunctionality) myAgent.functionalityInstance).getState(); //se actualiza el estado de las replicas
            if (currentState != null) {
                System.out.println("Send state");
                myAgent.sendStateToTracking(currentState,"order"); //comunicamos a las replicas nuestro estado
            }
        } else {
            // Si su estado es tracking
            trackingOnBoot(myAgent, mySeType, conversationId);

            myAgent.initTransition = ControlBehaviour.TRACKING;
        }

        return null;
    }

    // TODO quitamos el atributo attribs y lo creamos dentro del metodo seStart ???
    public String seStart(String seID, Hashtable<String, String> attribs, String conversationId) {

        ArrayList<String> creationCategories = new ArrayList<>();
        creationCategories.add("batch");  // Aqui decidiremos que tipos de elementos queremos crear --> Order, Batch, las dos...

        List result = seStart(myAgent, seID, attribs, conversationId, creationCategories, chatID, redundancy);
        elementsToCreate = (List<String>) result.get(0);
        chatID = (int) result.get(1);

        return null;
    }

    public String seStop(String... seID) {
        return null;
    }

    @Override
    public Object execute(Object[] input) {
        if (input!= null) {
            if (input[0] != null) {
                ACLMessage msg = (ACLMessage) input[0];
                System.out.println("El agente " + myAgent.getLocalName() + " esta en el metodo execute de su estado running");

//        ACLMessage msg = myAgent.receive(template);
//        if (msg != null) {
                myAgent.msgFIFO.add((String) msg.getContent());

            if(msg.getPerformative()==ACLMessage.INFORM&&msg.getOntology().equals("Information")&&msg.getConversationId().equals("ItemsInfo")){
//                Acknowledge(msg, myAgent);
                if (firstTime) {
                    deserializedMessage = deserializeMsg(msg.getContent());
                    batchTraceability = addNewLevel(batchTraceability, deserializedMessage, true); //a�ade el espacio para la informacion de la orden en primera posicion, sumando un nivel mas a los datos anteriores
                    batchTraceability.get(0).get(0).get(0).add("OrderLevel"); // en ese espacio creado, se a�ade la informacion del order
                    batchTraceability.get(0).get(0).get(2).add("orderReference");
                    String batchNumber = batchTraceability.get(1).get(0).get(3).get(0);
//                    while (!batch_to_take_down.equals("")) {
//                    }
                    batch_to_take_down = batchNumber;
                    orderNumber = batchNumber.substring(0, 2);
                    batchTraceability.get(0).get(0).get(3).add(orderNumber);
                    firstTime = false;
                } else {

                    if (newBatch == false) {
                        for (int i = batchTraceability.size() - 1; i >= batchIndex; i--) {
                            batchTraceability.remove(i); //se elimina el ultimo batch a�adido para poder sobreescribirlo
                        }
                    }
                    deserializedMessage = deserializeMsg(msg.getContent());
                    String batchNumber = deserializedMessage.get(0).get(0).get(3).get(0);
//                    while (!batch_to_take_down.equals("")) {
//                    } //espera al reseteo de la variable para escribir el batch (evita algunos bugs)
                    batch_to_take_down = batchNumber;
                    batchTraceability = addNewLevel(batchTraceability, deserializedMessage, false);
                }
                newBatch = false; // hasta que el batch a�adido no se complete, cada vez que se reciba un mensaje el dato se sobrescribira

            }else if(msg.getPerformative()==ACLMessage.INFORM&&msg.getConversationId().equals("Shutdown")){  // Recepcion de mensajes para eliminar de la lista de agentes hijo los agentes batch que ya han enviado toda la informacion
                if (msg.getContent().equals("Batch completed")) {
                    String msgSender = msg.getOntology();

                    for (int i = 0; i < sonAgentID.size(); i++) {
                        if (sonAgentID.get(i).equals(msgSender)) {
                            sonAgentID.remove(i);
                            i--;
                        }
                    }

                    String aux = "";
                    String msgToMPLan = "";
                    for (ArrayList<ArrayList<ArrayList<String>>> a : batchTraceability) { //serializacion de los datos a enviar
                        aux = a.toString();
                        msgToMPLan = msgToMPLan.concat(aux);
                    }
//                AID Agent = new AID(parentAgentID, false);
                    try {
                        ACLMessage orde_parent = sendCommand(myAgent, "get " + myAgent.getLocalName() + " attrib=parent", myAgent.getLocalName() + "_parent");
                        ACLMessage mplan_parent = sendCommand(myAgent, "get " + orde_parent.getContent() + " attrib=parent", myAgent.getLocalName() + "_parent_parent");
                        posponed_msgs_to_mplan = myAgent.msg_buffer.get(mplan_parent.getContent());
                        if (posponed_msgs_to_mplan == null) {  //si no se encuentra el parent en el listado de mensajes postpuestos entonces el receptor ha confirmado la recepcion de todos los mensajes hasta ahora. Seguimos con la ejecuci�n normal.
                            ACLMessage running_replica = sendCommand(myAgent, "get * parent=" + mplan_parent.getContent() + " state=running", myAgent.getLocalName() + "_parent_running_replica");
                            myAgent.msgFIFO.add((String) running_replica.getContent());
                            if (!running_replica.getContent().equals("")) {
                                AID mplanAgentID = new AID(running_replica.getContent(), false);
                                ACLMessage msg_to_mplan = sendACLMessage(7, mplanAgentID, "Information", "OrderInfo", msgToMPLan, myAgent);
                                myAgent.AddToExpectedMsgs(msg_to_mplan);
                            } else {
                                posponed_msgs_to_mplan = new ArrayList<ACLMessage>();
                                ACLMessage msg_to_buffer = new ACLMessage(ACLMessage.INFORM);
                                msg_to_buffer.setConversationId("OrderInfo");
                                msg_to_buffer.setContent(msgToMPLan);
                                msg_to_buffer.setOntology("Information");
                                posponed_msgs_to_mplan.add(msg_to_buffer);
                                myAgent.msg_buffer.put(mplan_parent.getContent(), posponed_msgs_to_mplan);
                            }
                        } else {  //si no es null significa que el agente sigue denunciado y aun no se ha resuelto el problema.

                            System.out.println("Added message to buffer" + "To: " + mplan_parent.getContent() + "\n Still waiting for a solution.");
                            ACLMessage msg_to_buffer = new ACLMessage(ACLMessage.INFORM);
                            msg_to_buffer.setConversationId("OrderInfo");
                            msg_to_buffer.setContent(msgToMPLan);
                            msg_to_buffer.setOntology("Information");
                            posponed_msgs_to_mplan.add(msg_to_buffer); //se a�ade el mensaje a la lista de mensajes retenidos
                            myAgent.msg_buffer.put(mplan_parent.getContent(), posponed_msgs_to_mplan);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


//                ACLMessage msg_to_mplan= sendACLMessage(7, Agent, "Information", "OrderInfo", msgToMPLan, myAgent);
//                myAgent.AddToExpectedMsgs(msg_to_mplan);
                    if (sonAgentID.size() == 0) { // todos los batch agent de los que es padre ya le han enviado la informacion
//                        sendACLMessage(7, myAgent.getAID(), "Information", "Shutdown", "Shutdown", myAgent); // autoenvio de mensaje para asegurar que el agente de desregistre y se apague
                        return true;
                    }
                    batchIndex = batchTraceability.size() - 1;
                    newBatch = true;
                }

            }else if(msg.getPerformative()==ACLMessage.INFORM&&msg.getOntology().equals("update_timeout")){  //Actualiza el finish time del batch recibido (por petici�n de reset del QoS)
                String[] parts = msg.getContent().split("/");
                String timeout_batch_id = parts[0];
                String s_difference = parts[1];
                String ft_of_batch = "";
                for (int m = 0; m < batch_last_items_ft.size(); m++) {
                    if (batch_last_items_ft.get(m).get(0).equals(timeout_batch_id)) {
                        ft_of_batch = batch_last_items_ft.get(m).get(2);
                    }
                }
                if (ft_of_batch != "") {
                    long difference = Long.parseLong(s_difference);
                    try {
                        Date tdate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(ft_of_batch);
                        batch_to_update = timeout_batch_id;
                        new_expected_finish_time = convertToDateViaSqlTimestamp(convertToLocalDateTimeViaSqlTimestamp(tdate).plusSeconds(difference / 1000));

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else {
                    System.out.println("Not my batch, something went wrong");
                }
            }else if(msg.getPerformative()==ACLMessage.INFORM&&msg.getOntology().equals("delay")){
                String rawdelay = msg.getContent();
                String[] parts = rawdelay.split("/");
                String batchref = parts[0];
                String delay = parts[1];
                Date expected_FT = null;
                String batchFT = null;
                int temp = 0;
                for (int p = 0; p < batch_last_items_ft.size(); p++) {
                    if (batch_last_items_ft.get(p).get(0).equals(batchref)) {
                        batchFT = batch_last_items_ft.get(p).get(1);
                        temp = p;
                    }
                }
                if (batchFT != null) {
                    try {
                        expected_FT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(batchFT);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long startime = getactualtime().getTime() - (Long.parseLong(delay));
//                    Date d = new Date(startime); //para debug
                    LocalDateTime new_expected_FT = convertToLocalDateTimeViaSqlTimestamp(getactualtime());
                    new_expected_FT = new_expected_FT.plusSeconds(((expected_FT.getTime() - startime) / 1000));
                    expected_FT = convertToDateViaSqlTimestamp(new_expected_FT);
//                System.out.println(batchref + " batch expected finish time: " + expected_FT);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    String FT_string = sdf.format(expected_FT);
                    batch_last_items_ft.get(temp).add(FT_string);
                    Ordertimeout t = new Ordertimeout(expected_FT, batchref);
                    t.start();
                } else {
                    System.out.println("**ERROR**. No timeout generated for batch " + batchref);
                }
            }

            }
//            ACLMessage msg3 = myAgent.receive(template3);
//            if (msg3 != null) {            //confirmaci�n de timeout
//                myAgent.msgFIFO.add((String) msg3.getContent());
////            QoSresponse_flag=true;
//                batch_to_take_down = msg3.getContent();
//            }
//            ACLMessage msg4 = myAgent.receive(template4);
//            if (msg4 != null) {
//
//            }

//            ACLMessage msg5 = myAgent.receive(template5);
//            if (msg5 != null) {                                 //Genera un timeout para cada batch cuando recibe el delay. (siempre hay delay, sea 0 o no)
//
//
//            }
        }

        return false;
    }

    @Override
    public Void terminate(MWAgent myAgent) {
        this.myAgent = myAgent;
        String parentName = "";
        unregister_from_node();
        myAgent.get_timestamp(myAgent,"FinishTime");
        if(myAgent.ActualState=="running"){ //para filtrar las replicas ejecutando terminate
            try {
                ACLMessage reply = sendCommand(myAgent, "get * reference=" + orderNumber, "parentAgentID");

                if (reply != null) {   // Si no existe el id en el registro devuelve error
                    parentName = reply.getContent(); //gets the name of the agent�s parent
                }

            try {

                KillReplicas(myAgent);
//                sendACLMessage(7, Agent, parentName, "Shutdown", "Order completed", myAgent); // Informa al Mplan Agent que ya ha finalizado su tarea

                ACLMessage mplan_parent= sendCommand(myAgent, "get "+parentName+" attrib=parent", myAgent.getLocalName()+"_parent_parent");
                ACLMessage running_replica = sendCommand(myAgent, "get * parent=" + mplan_parent.getContent()+" state=running", myAgent.getLocalName()+"_parent_running_replica");
                AID Agent = new AID(running_replica.getContent(), false);
                sendACLMessage(7, Agent, parentName, "Shutdown", "Order completed", myAgent); // Informa al Mplan Agent que ya ha finalizado su tarea
                myAgent.deregisterAgent(parentName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    protected ArrayList<ArrayList<String>> batch_finish_times(String rawdata){

        ArrayList<ArrayList<String>> batchFT= new ArrayList<ArrayList<String>>();
        String[] parts1=rawdata.split("&");
        rawdata=parts1[1];
        ArrayList<String> data= new ArrayList<String>(Arrays.asList(rawdata.split("_")));
        for(int i=0;i<data.size();i++){
            String temp=data.get(i);
            String[] parts=temp.split("/");
            batchFT.add(i,new ArrayList<String>());
            batchFT.get(i).add(parts[0]);
            batchFT.get(i).add(parts[1]);
        }

        return batchFT;
    }
}
