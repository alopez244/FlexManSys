package es.ehu.domain.manufacturing.agents.functionality;

import com.google.gson.Gson;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.behaviour.NegotiatingBehaviour;
import es.ehu.platform.behaviour.TrackingBehaviour;
import es.ehu.platform.template.interfaces.AvailabilityFunctionality;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.Traceability;
import es.ehu.platform.utilities.Cmd;
import es.ehu.platform.utilities.XMLReader;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import javafx.application.Platform;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.core.config.Order;
import es.ehu.platform.template.interfaces.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

public class Batch_Functionality extends DomApp_Functionality implements BasicFunctionality, AvailabilityFunctionality{

    private volatile ArrayList<String> itemreference=new ArrayList<String>();
    private MessageTemplate templateFT;
    private static final long serialVersionUID = 1L;
    private MWAgent myAgent;
    private ACLMessage batchName=new ACLMessage();
    private String productID, batchNumber;
    private ArrayList<String> actionList = new ArrayList<>();
    private ArrayList<ArrayList<ArrayList<String>>> productInfo;
    private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> productsTraceability = new ArrayList<>();
    private HashMap<String, String> machinesForOperations = new HashMap<>();
    private String itemsID;
    private Boolean firstTime = true;
    private volatile String batchreference=null;
    private int chatID = 0; // Numero incremental para crear conversationID
    private String firstState;
    private String redundancy;
    private String parentAgentID;
    private volatile String finish_times_of_batch=null;
    private String mySeType;
    private MessageTemplate echotemplate=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchOntology("Acknowledge"));
    private MessageTemplate QoStemplate=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchOntology("acl_error"));
//    private Object myReplicasID  = new HashMap<>();
    private Object[] myReplicasID  = new Object[2];
    public volatile int wait=0;
    private volatile Date now=null;
    private volatile Date date_when_delay_was_asked=null;
    private volatile Date expected_finish_date=null;
    private volatile ArrayList<String> items_finish_times=new ArrayList<String>();
    private volatile int actual_item_number=0;
    private volatile long delaynum=0;
    private volatile boolean update_timeout_flag=false, delay_already_asked=false;
    private volatile AID QoSID = new AID("QoSManagerAgent", false);
    private volatile boolean reset_flag=false;
    private volatile boolean delay_already_incremented=false,QoSresponse_flag=false;
    public ArrayList<ACLMessage> posponed_msgs_to_batch=new ArrayList<ACLMessage>();




    @Override
    public void setState(String state) {

        ArrayList<ArrayList<ArrayList<ArrayList<String>>>> Traceability=new ArrayList<>();
        ArrayList<String> remaining=new ArrayList<String>();
        ArrayList<String> FT=new ArrayList<String>();
        ArrayList<String> replicas=new ArrayList<String>();
        String parts1[] =state.split("/div0/"); //el divisor 0 divide los argumentos y el resto se usan para los arraylist
        String productTraceabilityConc = parts1[0]; //trazabilidad concatenada
        String remainingConc = null;
        if (parts1[1] != null&&parts1[1] != "") {
            remainingConc = parts1[1]; //solo si quedan acciones/SonAgentIDs
        }
        String firstimeString = parts1[2]; //primera vez

        String FinishTimesConc=parts1[3]; //finish times concatenados (cada agente de aplicación lleva un formato)
        parentAgentID=parts1[4]; 					//parent
        String replicasConc=parts1[5];		//replicas del agente
        finish_times_of_batch=parts1[6];   //FT de cada item
        itemreference=take_item_references(finish_times_of_batch); //construimos la variable itemreference
        actual_item_number=Integer.parseInt(parts1[7]);  //el item que se esta produciendo actualmente
        String data_string=parts1[8];             //la fecha cuando se pidió el delay
        delaynum=Long.parseLong(parts1[9]);                 //el delay devuelto por el machine
        String s_expected_finish_date=parts1[10];

        if(data_string.equals("")){
            System.out.println("Date when delay was asked is lost. Timeout wont be able to start");
            date_when_delay_was_asked=null;
        }else{
            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                date_when_delay_was_asked = formato.parse(data_string);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if(s_expected_finish_date.equals("*")){
            System.out.println("Date when delay was asked is lost. Timeout thread cannot be start");
            expected_finish_date=null;
        }else{
            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                expected_finish_date = formato.parse(s_expected_finish_date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        String parts2[] = productTraceabilityConc.split("/div1/"); //construye la trazabilidad
        for (int i = 0; i < parts2.length; i++) {
            Traceability.add(i, new ArrayList<ArrayList<ArrayList<String>>>());
            String parts3[] = parts2[i].split("/div2/");
            for (int j = 0; j < parts3.length; j++) {
                Traceability.get(i).add(j, new ArrayList<ArrayList<String>>());
                String parts4[] = parts3[j].split("/div3/");
                for (int k = 0; k < parts4.length; k++) {
                    Traceability.get(i).get(j).add(k, new ArrayList<String>());
                    String parts5[] = parts4[k].split("/div4/");
                    for (int l = 0; l < parts5.length; l++) {
                        Traceability.get(i).get(j).get(k).add(parts5[l]);
                    }
                }
            }
        }
        productsTraceability=Traceability;
        if (remainingConc != null) {    //construye bien los sonagentID o actionlist
            String parts6[] = remainingConc.split("/div1/");
            for (int i = 0; i < parts6.length; i++) {
                remaining.add(parts6[i]);
            }
        }
        actionList=remaining;

        firstTime = Boolean.parseBoolean(firstimeString);
        String parts7[]=FinishTimesConc.split("/div1/");
        for(int i=0;i<parts7.length;i++){
            FT.add(parts7[i]);
        }
        items_finish_times=FT;
        String parts8[]=replicasConc.split("/div1/");
        for(int i=0;i<parts8.length;i++){
            if(!parts8[i].equals(myAgent.getLocalName())){
                replicas.add(parts8[i]);
            }
        }
        myAgent.replicas=replicas;
    }
    @Override
    public String getState(){
     //div0 dividen las diferentes variables
     //div1 y posteriores se usan para dividir arraylist de multiples dimensiones

    myAgent.antiloopflag=true;
        String state="";
        ArrayList<String> updated_replicas=new ArrayList<String>();
        ArrayList<String> Finishtimes=take_finish_times(finish_times_of_batch);

        for(int i=0;i<productsTraceability.size();i++){
            if(i!=0){
                state=state+"/div1/";
            }
            for(int j=0;j<productsTraceability.get(i).size();j++){
                if(j!=0){
                    state=state+"/div2/";
                }
                for(int k=0;k<productsTraceability.get(i).get(j).size();k++){
                    if(k!=0){
                        state=state+"/div3/";
                    }
                    for(int l=0;l<productsTraceability.get(i).get(j).get(k).size();l++){
                        if(l!=0){
                            state=state+"/div4/";
                        }
                        state=state+productsTraceability.get(i).get(j).get(k).get(l);
                    }
                }
            }
        }
        state=state+"/div0/";
        for(int i=0;i<actionList.size();i++){
            if(i!=0){
                state=state+"/div1/";
            }
            state=state+actionList.get(i);
        }
        state=state+"/div0/"+String.valueOf(firstTime)+"/div0/";

        for(int i=0;i<Finishtimes.size();i++){ //concatena los FT de los item
            if(i==0){
                state=state+Finishtimes.get(i);
            }else{
                state=state+"/div1/"+Finishtimes.get(i);
            }
        }
        state=state+"/div0/"+parentAgentID+"/div0/";

        try {   //realiza la consulta al sa para tener la lista de replicas actualizada.
            String[] replicas=new String[1];
            if(redundancy.equals("1")){
                replicas[0] =" ";
            }else{
                ACLMessage parent = sendCommand(myAgent, "get " + myAgent.getLocalName() + " attrib=parent", "GetBatchParent");
                ACLMessage replicasACL = sendCommand(myAgent, "get * state=tracking parent=" + parent.getContent(), "GetBatchUpdatedReplicas");

                if(replicasACL.getContent().contains(",")){
                    replicas= replicasACL.getContent().split(",");
                }else{
                    replicas[0] = replicasACL.getContent();
                }
            }
            for(int i=0; i<replicas.length;i++){
                if(i==0){
                    state=state+replicas[i];
                }else{
                    state=state+"/div1/"+replicas[i];
                }
            }
            for(int i=0;i<replicas.length;i++){ //actualiza las replicas de este agente
                updated_replicas.add(replicas[i]);
            }
            myAgent.replicas=updated_replicas;
        } catch (Exception e) {
            e.printStackTrace();
        }

        state=state+"/div0/"+finish_times_of_batch;
        state=state+"/div0/"+String.valueOf(actual_item_number);
        String string_date ="";
        if(date_when_delay_was_asked!=null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            string_date = sdf.format(date_when_delay_was_asked);
        }
        state=state+"/div0/"+string_date;

        state=state+"/div0/"+String.valueOf(delaynum);

        String s_exp_fd="*";
        if(expected_finish_date!=null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            s_exp_fd = sdf.format(expected_finish_date);
        }
        state=state+"/div0/"+s_exp_fd;

        myAgent.antiloopflag=false;  //baja el flag de antiloop
        return state;

    }


    class timeout extends Thread{
    private boolean timeout=false;
        public void run() {


            System.out.println("Item timeout initialized");
            while (finish_times_of_batch == null) {} //tramo de espera de seguridad hasta tener los finish times
            items_finish_times = take_finish_times(finish_times_of_batch); //extrae la información útil que se usa en el timeout
            itemreference=take_item_references(finish_times_of_batch);//nos devuelve las referencias de los item para este batch, ordenados como los tiempos extraidos en la anterior función
            if(!reset_flag) { //si no ha sido un reset pedido con el QoS debemos construir los FT
                try {
                    expected_finish_date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(items_finish_times.get(actual_item_number));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            while (actual_item_number < items_finish_times.size() && !timeout) {  //Mientras que no estemos en la ultima pieza o se haya lanzado timeout
                if(getactualtime().after(expected_finish_date)&& !timeout){
                    if(!delay_already_incremented){
                        expected_finish_date=FIUpdateFinishTimes();

                        //Actualiza el estado de las replicas tras obtener el delay
                        String currentState = (String) ((AvailabilityFunctionality) myAgent.functionalityInstance).getState(); //disponemos de delaynum, date_when_delay_was_asked y expected_finish_date
                        if (currentState != null) {
                            System.out.println("Send state");
                            myAgent.sendStateToTracking(currentState); //comunicamos a las replicas nuestro estado
                        }

                    }else {
                        System.out.println(getactualtime() + " " + myAgent.getLocalName() + " WARN " + batchreference + " batch has thrown a timeout on item number " + itemreference.get(actual_item_number) + " Checking failure with QoS Agent...");
                        QoSresponse_flag = false;
                        ACLMessage report_to_QoS= sendACLMessage(ACLMessage.FAILURE, QoSID, "timeout", "timeout " + batchreference, batchreference + "/" + itemreference.get(actual_item_number), myAgent); //avisa al QoS de fallo por timeout
                        myAgent.AddToExpectedMsgs(report_to_QoS);
                        timeout = true;
                    }
                }

                while (getactualtime().before(expected_finish_date)&&actual_item_number<items_finish_times.size()&&!timeout) {  //se queda a la espera siempre que no se supere la fecha de finishtime

                    if (update_timeout_flag) {
                        actual_item_number++;
                        if(actual_item_number<items_finish_times.size()) {
//                            System.out.println("Next item started");
                            expected_finish_date = UpdateFinishTimes(actual_item_number); //actualiza la fecha de finish time
                            update_timeout_flag = false;
                        }
                    }
                }
            }
            if(!timeout){
                System.out.println("Batch finished");
            }
            System.out.println("Item timeout finished.");
        }
    }

    @Override
    public Void init(MWAgent myAgent) {

        this.myAgent = myAgent;

        String conversationId = myAgent.getLocalName() + "_" + chatID++;

        firstState = getArgumentOfAgent(myAgent, "firstState");
        redundancy = getArgumentOfAgent(myAgent, "redundancy");
        parentAgentID = getArgumentOfAgent(myAgent, "parentAgent");

        mySeType = getMySeType(myAgent, conversationId);

        // Hay que leer el modelo de trazabilidad que estara en alguna carpeta
        // Tanto el running como los tracking tendran que leerlo
        // Cuando tenga la informacion la guardara en productsTraceability

        if(firstState.equals("running")) {

            // Cambiar a estado bootToRunning para que los tracking le puedan enviar mensajes
            String query = "set " + myAgent.getLocalName() + " state=bootToRunning";
            try {
                sendCommand(myAgent, query, conversationId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            myReplicasID = processACLMessages(myAgent, mySeType, new ArrayList<>(), conversationId, redundancy, parentAgentID);
            myAgent.replicas = (ArrayList) myReplicasID[0];

            // TODO esta comentado ya que peta al estar en el init --> La ejecucion sigue adelante antes de recoger todos los mensajes y despues da problemas
            // sendPlan method of interface ITraceability
            createPlan(myAgent, conversationId);

            templateFT=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchOntology("Ftime_batch_ask"));
            try {
                batchName=sendCommand(myAgent,"get "+myAgent.getLocalName()+" attrib=parent","name"); //consigue el nombre del batch
                ACLMessage reference=sendCommand(myAgent,"get "+batchName.getContent()+" attrib=reference","Reference"); //consigue la referencia del batch
                AID plannerID = new AID("planner", false);
                batchreference=reference.getContent();
                sendACLMessage(16, plannerID,"Ftime_batch_ask", "finnish_time", reference.getContent(), myAgent ); //pide el finish time de cada item al planner
                ACLMessage finishtime= myAgent.blockingReceive(templateFT); //recibe los finish times concatenados
                myAgent.msgFIFO.add((String) finishtime.getContent());
                System.out.println(finishtime.getContent());
                finish_times_of_batch=finishtime.getContent();

                sendACLMessage(16,QoSID,"askdelay","delayasking",batchreference,myAgent);

            } catch (Exception e) {
                System.out.println("ERROR. Something happened asking for finish time to planner");
                e.printStackTrace();
            }


        } else {
            // Si su estado es tracking
            trackingOnBoot(myAgent, mySeType, conversationId);

            myAgent.initTransition = ControlBehaviour.TRACKING;
        }

        return null;
    }

    @Override
    public Object execute(Object[] input) {

        HashMap infoForTraceability = new HashMap();

        System.out.println(input);

        System.out.println("El agente " + myAgent.getLocalName() + " esta en el metodo execute de su estado running");

        System.out.println("Ahora el agente " + myAgent.getLocalName() + " se va a quedar a la espera de la informacion de las operaciones");

        MessageTemplate batch_f_template=MessageTemplate.or(MessageTemplate.or(MessageTemplate.or(
                MessageTemplate.and(MessageTemplate.MatchOntology("askdelay"),MessageTemplate.MatchPerformative(ACLMessage.INFORM)),
                MessageTemplate.and(MessageTemplate.MatchContent("reset_timeout"),MessageTemplate.MatchPerformative(ACLMessage.INFORM))),
                MessageTemplate.and(MessageTemplate.MatchContent("confirmed_timeout"),MessageTemplate.MatchPerformative(ACLMessage.INFORM))),
                MessageTemplate.and(MessageTemplate.MatchOntology("negotiation"),MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));

        ACLMessage msg = myAgent.receive(batch_f_template);
        if (msg != null) {
            myAgent.msgFIFO.add((String) msg.getContent()); //se añade en buffer de listado de mensajes recibidos

            if(msg.getPerformative()==ACLMessage.INFORM&&msg.getOntology().equals("askdelay")){ //si es un mensaje con info del delay creamos el timeout
                date_when_delay_was_asked = getactualtime();
                String delay = msg.getContent();

                delaynum = Long.parseLong(delay);
                AID OrderAgent = new AID(parentAgentID, false);

                try {
                    batchName=sendCommand(myAgent,"get "+myAgent.getLocalName()+" attrib=parent","name"); //consigue el nombre del batch
                    ACLMessage reference=sendCommand(myAgent,"get "+batchName.getContent()+" attrib=reference","Reference");
                    sendACLMessage(7, OrderAgent,"delay", "inform delay", reference.getContent()+"/"+delay, myAgent ); //informa al parent
                    //TODO timeout de ACL aquí
                } catch (Exception e) {
                    e.printStackTrace();
                }


                delay_already_asked = true;
                timeout thread=new timeout();
                thread.start(); //se inicia el timeout tras recibir delay
            }else if(msg.getPerformative()==ACLMessage.INFORM&&msg.getContent().equals("reset_timeout")){
//                QoSresponse_flag=true;
                reset_flag=true;
                System.out.println("Timeout has been asked to be reset");
                if(date_when_delay_was_asked==null||delaynum==0||expected_finish_date==null){
                    System.out.println("Error starting timeout. Some data has been lost.");
                }else{
                    Date last_expected_finish_time=expected_finish_date;
                    if(actual_item_number==0) {
                        expected_finish_date = FIUpdateFinishTimes();
                    }else{
                        expected_finish_date = UpdateFinishTimes(actual_item_number);
                    }
                    long difference=expected_finish_date.getTime()- last_expected_finish_time.getTime();
                    AID OrderAgent = new AID(parentAgentID, false);
                    try {
                        batchName=sendCommand(myAgent,"get "+myAgent.getLocalName()+" attrib=parent","name"); //consigue el nombre del batch
                        ACLMessage reference=sendCommand(myAgent,"get "+batchName.getContent()+" attrib=reference","Reference");
                        //TODO timeout de ACL aquí
                        sendACLMessage(ACLMessage.INFORM,OrderAgent,"update_timeout","timeout",reference.getContent()+"/"+ difference,myAgent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    timeout thread=new timeout(); //reinicio de timeout
                    thread.start();
                }

            }else if(msg.getPerformative()==ACLMessage.INFORM&&msg.getContent().equals("confirmed_timeout")){
//                QoSresponse_flag=true;
                System.out.println("Timeout confirmed by QoS.");
            } else if (msg.getPerformative() == ACLMessage.REQUEST&&msg.getOntology().equals("negotiation")) {

                Acknowledge(msg,myAgent);
                System.out.println("Mensaje con la informacion del PLC");
                System.out.println("Quien envia el mensaje: " + msg.getSender());
                System.out.println("Contenido: " + msg.getContent());
                System.out.println("ConversationId: " + msg.getConversationId());

                infoForTraceability = new Gson().fromJson(msg.getContent(), HashMap.class);  //Data type conversion Json->Hashmap class
                // Se extraen los datos necesarios del mensaje recibido
                // Cada mensaje contiene informacion del  fabricado
                String idItem = String.valueOf(infoForTraceability.get("Id_Item_Number"));
                batchNumber = String.valueOf(infoForTraceability.get("Id_Batch_Reference"));
                String ActionTypes = String.valueOf(infoForTraceability.get("Id_Action_Type"));
                update_timeout_flag=true;
                itemreference=take_item_references(finish_times_of_batch);
                for (int i=0; i < productsTraceability.size(); i++) {
                    // Se identifica la estructura de datos correspondiente al item que se ha fabricado
                    if (productsTraceability.get(i).get(0).get(3).size() > 2) { //se comprueba que contenga mas de dos elementos, ya que en la siguiente linea se accede al tercero
                        if (productsTraceability.get(i).get(0).get(3).get(3).equals(GetItemIDForTraceability(idItem,itemreference))) {
                            for (int j = 0; j < productsTraceability.get(i).size(); j++) {
                                if (productsTraceability.get(i).get(j).get(0).get(0).equals("action")) {    // Dentro del action, se registran los datos de fabricacion
                                    if (ActionTypes.contains(productsTraceability.get(i).get(j).get(3).get(1))) {   //solo se escribe en los actions que esten definidos en el ActionTypes
                                        productsTraceability.get(i).get(j).get(3).set(3, String.valueOf(infoForTraceability.get("Id_Machine_Reference")));
                                        productsTraceability.get(i).get(j).get(3).set(4, String.valueOf(infoForTraceability.get("Id_Machine_Reference")));
                                        productsTraceability.get(i).get(j).get(3).set(5, String.valueOf(infoForTraceability.get("Data_Initial_Time_Stamp")));
                                        productsTraceability.get(i).get(j).get(3).set(6, String.valueOf(infoForTraceability.get("Data_Final_Time_Stamp")));
                                    }
                                }
                            }
                        }
                    }
                }
                // Inicialización de las variables donde se guardaran los datos serializados
                String aux = "";
                String msgToOrder = "";

                if (infoForTraceability.containsKey("Data_Service_Time_Stamp")) { //El lote ha terminado de fabricarse y se envian los datos al order agent
                    sendACLMessage(7,QoSID,"batch_finish",batchNumber+" finish",batchNumber,myAgent);
                    for (int k = 0; k < actionList.size(); k++) { // Se eliminan las acciones que ya se han realizado
                        if (ActionTypes.contains(actionList.get(k))) {
                            actionList.remove(k);
                            k--;
                        }
                    }
                    if (firstTime) { //solo se añade la informacion la primera vez
                        ArrayList<ArrayList<ArrayList<ArrayList<String>>>> traceability = new ArrayList<>();
                        productsTraceability = addNewLevel(traceability, productsTraceability, true); //añade el espacio para la informacion del lote en primera posicion, sumando un nivel mas a los datos anteriores
                        productsTraceability.get(0).get(0).get(0).add("BatchLevel"); // en ese espacio creado, se añade la informacion del lote
                        productsTraceability.get(0).get(0).get(2).add("batchReference");
                        productsTraceability.get(0).get(0).get(2).add("Data_Service_Time_Stamp");
                        productsTraceability.get(0).get(0).get(3).add(batchNumber);
                        productsTraceability.get(0).get(0).get(3).add(String.valueOf(infoForTraceability.get("Data_Service_Time_Stamp")));
                        firstTime = false;
                    }

                    for (ArrayList<ArrayList<ArrayList<String>>> a : productsTraceability) { //serializacion de los datos a enviar
                        aux = a.toString();
                        msgToOrder = msgToOrder.concat(aux);
                    }
                    System.out.println(msgToOrder);
                    try {
                        ACLMessage order_parent= sendCommand(myAgent, "get "+parentAgentID+" attrib=parent", "OrderAgentID");
                        myAgent.msgFIFO.add((String) order_parent.getContent());
                        posponed_msgs_to_batch= myAgent.msg_buffer.get(batchName);
                        if(posponed_msgs_to_batch==null){  //si no se encuentra el parent en el listado de mensajes postpuestos entonces el receptor ha confirmado la recepcion de todos los mensajes hasta ahora. Seguimos con la ejecución normal.
                            ACLMessage running_replica = sendCommand(myAgent, "get * parent=" + order_parent.getContent()+" state=running", "OrderAgentID");
                            myAgent.msgFIFO.add((String) running_replica.getContent());
                            if (!running_replica.getContent().equals("")) {
                                AID orderAgentID = new AID(running_replica.getContent(), false);
                                ACLMessage msg_to_order= sendACLMessage(7, orderAgentID,"Information", "ItemsInfo", msgToOrder, myAgent );
                                myAgent.AddToExpectedMsgs(msg_to_order);
                            }else{
                                ACLMessage msg_to_buffer=new ACLMessage(ACLMessage.INFORM);
                                msg_to_buffer.setConversationId("ItemsInfo");
                                msg_to_buffer.setContent(msgToOrder);
                                msg_to_buffer.setOntology("Information");
                                posponed_msgs_to_batch.add(msg_to_buffer);
                                myAgent.msg_buffer.put(order_parent.getContent(),posponed_msgs_to_batch);
                            }
                        }else{  //si no es null significa que el agente sigue denunciado y aun no se ha resuelto el problema.

                            System.out.println("Added message to buffer:\nContent: "+msgToOrder+"\nTo: "+order_parent.getContent()+"\n Still waiting for a solution.");
                            ACLMessage msg_to_buffer=new ACLMessage(ACLMessage.INFORM);
                            msg_to_buffer.setConversationId("ItemsInfo");
                            msg_to_buffer.setContent(msgToOrder);
                            msg_to_buffer.setOntology("Information");
                            posponed_msgs_to_batch.add(msg_to_buffer); //se añade el mensaje a la lista de mensajes retenidos
                            myAgent.msg_buffer.put(order_parent.getContent(),posponed_msgs_to_batch);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (actionList.size() == 0){ // cuando todas las acciones se han completado, se elimina el batch agent
                    return true; //Batch agent a terminado su funcion y pasa a STOP
                }
            }
        }
        return false;
    }

    //====================================================================
    //ITRACEABILITY INTERFACE
    //====================================================================

    @Override
    public Void terminate(MWAgent myAgent) {
        this.myAgent = myAgent;
        String parentName = "";
        unregister_from_node();

        if(myAgent.ActualState=="running"){ //para filtrar las replicas ejecutando terminate
            try {
                ACLMessage reply = sendCommand(myAgent, "get * reference=" + batchNumber, "parentAgentID");
                //returns the names of all the agents that are sons
                if (reply != null)   // Si no existe el id en el registro devuelve error
                    parentName = reply.getContent(); //gets the name of the agent´s parent

            AID Agent = new AID(parentAgentID, false);
            KillReplicas(myAgent.replicas);
//            sendACLMessage(7, Agent, myAgent.getLocalName(), "Shutdown", "Batch completed", myAgent);
            sendACLMessage(7, Agent, parentName, "Shutdown", "Batch completed", myAgent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                myAgent.deregisterAgent(parentName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return null;
    }

    private void createPlan(MWAgent myAgent, String conversationId) {

        // Conseguir la referencia del producto
        productID = getProductID(myAgent.getLocalName(), conversationId);
        System.out.println("La referencia de producto del batch del agente " + myAgent.getLocalName() + " es: " + productID);

        // Conseguimos toda la informacion del producto utilizando su ID
        productInfo = getProductInfo(productID);
        System.out.println("ID del producto asociado al agente " + myAgent.getLocalName() + ": " + productInfo.get(0).get(3).get(1) + " - " + productID);

        // Conseguimos la lista de acciones que componen la fabricacion de las piezas del lote
        actionList = getProductActions(productInfo);

        // Conseguir la cantidad de productos
        itemsID = getItemsID(myAgent.getLocalName(), conversationId);

        // Ahora podremos proceder a conseguir la trazabilidad de los productos
        getProductsTraceability();

    }

    private void readPlan() {
        // TODO
    }

    //====================================================================


    private String getProductID(String seID, String conversationId) {
        String productID = null;
        String query = "get " + seID + " attrib=parent";
        ACLMessage reply = null;

        try {
            reply = sendCommand(myAgent, query, conversationId);
            // ID del batch con el cual el agente está relacionado
            // Ya que es este objeto el que tiene la referencia del producto
            String batchID = null;
            if (reply != null) {
                batchID = reply.getContent();
                query = "get " + batchID + " attrib=refProductID";
                reply = sendCommand(myAgent, query, conversationId);
                if (reply != null)
                    productID = reply.getContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productID;
    }

    private String getItemsID(String seID, String conversationId) {
        String itemsID = null;
        String query = "get " + seID + " attrib=parent";
        ACLMessage reply = null;

        try {
            reply = sendCommand(myAgent, query, conversationId);
            // ID del batch con el cual el agente está relacionado
            // Ya que es este objeto el que tiene la cantidad de productos
            String batchID = null;
            if (reply != null) {
                batchID = reply.getContent();
                query = "get " + batchID + " attrib=numberOfItems";
                reply = sendCommand(myAgent, query, conversationId);
                if (reply != null)
                    itemsID = reply.getContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemsID;
    }

    // Metodo para extraer las acciones que componen la fabricacion de cada pieza
    private ArrayList<String> getProductActions (ArrayList<ArrayList<ArrayList<String>>> productInfo) {

        ArrayList<String> actionList = new ArrayList<>();

        for (int i = 0; i < productInfo.size(); i++) {
            if (productInfo.get(i).get(0).get(0).equals("action")) {
                actionList.add(productInfo.get(i).get(3).get(1));
            }
        }
        return actionList;
    }

    private HashMap<String,String> getNegotiationWinners() {

        HashMap<String,String> operationsWithMachines = new HashMap<>();

        while (!machinesForOperations.isEmpty()) {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                myAgent.msgFIFO.add((String) msg.getContent());
                // TODO COMPROBAR TAMBIEN LOS TRACKING si esta bien programado (sin probar)
                if ((msg.getPerformative() == ACLMessage.INFORM) && (msg.getContent().contains("I am the winner"))) {

                    String operationID = msg.getContent().split(":")[1];
                    // Con la ID de la operacion lo borramos de la lista que teniamos, y añadimos a la nueva lista la maquina que se le ha asociado
                    machinesForOperations.remove(operationID);
                    operationsWithMachines.put(operationID, msg.getSender().getLocalName());

                }
            }
        }

        // Si se han borrado todas las operaciones es que ya tenemos todas las maquinas asociadas a alguna operacion
        System.out.println("Todas las operaciones tienen asociada una maquina");

        Iterator it =operationsWithMachines.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println("Operation " + pair.getKey() + " is for machine " + pair.getValue());
        }

        // Ahora podremos proceder a conseguir la trazabilidad de los productos
        getProductsTraceability();

        return operationsWithMachines;

    }

    private ArrayList<ArrayList<ArrayList<String>>> getProductInfo(String productID) {

        ArrayList<ArrayList<ArrayList<String>>> allProductInfo = null;

        String productsURL = "classes/resources/ProductInstances";
        //String path = getClass().getResource(productsURL).getPath();
        XMLReader fileReader = new XMLReader();
        ArrayList<ArrayList<ArrayList<String>>> xmlelements = null;
        File directory = new File(productsURL);

        if (directory.isDirectory()) {
            // Recorremos toda la carpeta
            for (File productXML: directory.listFiles()) {
                // Miramos solo los archivos tipo XML
                if (FilenameUtils.getExtension(productXML.getPath()).equals("xml")) {
                    xmlelements = fileReader.readFile(productXML.getPath());
                    String idValue = null;
                    // Buscamos el atributo id para conseguir su valor
                    for (int i = 0; i < xmlelements.get(0).get(2).size(); i++) {
                        if (xmlelements.get(0).get(2).get(i).equals("productType"))
                            idValue = xmlelements.get(0).get(3).get(i);
                    }
                    // Si el id coincide, es el producto que buscamos
                    if (idValue.equals(productID))
                        allProductInfo = xmlelements;
                }
            }
        }

        return allProductInfo;
    }

    private HashMap<String, String> getMachines(String localName, ArrayList<ArrayList<ArrayList<String>>> productInfo, String conversationId) {

        // Teniendo toda la informacion del producto vamos a conseguir las maquinas que vayan a realizar todas las operaciones
        // Por cada operacion que exista en el producto, vamos a buscar las maquinas que puedan realizar esa operacion
        // y guardaremos esa informacion en el hashmap --> <"S_01", "machine1, machine3">
        HashMap<String, String> machinesForOperations = new HashMap<>();

        String operationID = null;
        String operationType = null;
        for (int i=0; i < productInfo.size(); i++) {
            // Solo analizaremos cuando el atributo contenga la palabra operation
            if (productInfo.get(i).get(0).get(0).contains("_operation")) {
                operationType = productInfo.get(i).get(0).get(0);
                // Teniendo el tipo de operacion conseguiremos su valor
                for (int j=0; j < productInfo.get(i).get(2).size(); j++) {
                    if (productInfo.get(i).get(2).get(j).equals("id"))
                        operationID = productInfo.get(i).get(3).get(j);
                }
                System.out.println("\t\tOPERACION --> " + operationType + " y el ID:" + operationID);

                // Ahora, primero conseguiremos todas las maquinas (sus ID)
                String getAllMachines = "get * category=machine";
                ACLMessage reply = null;
                try {
                    reply = sendCommand(myAgent, getAllMachines, conversationId);
                    if (reply != null) {
                        String allMachines = reply.getContent();
                        System.out.println("ALL MACHINES: " + allMachines);
                        List<String> items = new ArrayList<>();
                        String[] aux = allMachines.split(",");
                        // Ahora por cada maquina miraremos si contiene nuestra operacion
                        for (String machineID:aux) {
                            String simpleQuery = "get " + machineID + " attrib=simpleOperations";
                            String complexQuery = "get " + machineID + " attrib=complexOperations";
                            // Dependiendo del tipo de operacion miraremos en un atributo o en otro
                            if (operationType.equals("simple_operation"))
                                reply = sendCommand(myAgent, simpleQuery, conversationId);
                            else
                                reply = sendCommand(myAgent, complexQuery, conversationId);
                            if (reply != null) {
                                System.out.println("All "+operationType+"s of " +machineID+ ": " +reply.getContent());
                                if (reply.getContent().contains(operationID)) {
                                    // Si todavia no se ha añadido ninguna maquina a esa operacion, la meteremos directamente
                                    if (machinesForOperations.get(operationID) == null)
                                        machinesForOperations.put(operationID, machineID);
                                    // En el caso de que tenga alguna maquina, sumaremos la nueva al final y la añadiremos
                                    else {
                                        String newMachine = machinesForOperations.get(operationID) + "," + machineID;
                                        machinesForOperations.put(operationID, newMachine);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        // Al final tendremos un HashMap con todas las operaciones, y las maquinas que las pueden realizar
        System.out.println("All operations and the machines of each operation of the batch " +myAgent.getLocalName() + " --> "+machinesForOperations);
        System.out.println();

        return machinesForOperations;

    }

    private void getProductsTraceability() {

        // Cogeremos como base la informacion del producto que ya hemos conseguido y añadiremos las nuevas variables

        ArrayList<ArrayList<ArrayList<String>>> aux = new ArrayList<ArrayList<ArrayList<String>>>();
        // Para evitar la vinculacion entre productInfo y la varible aux, se clona cada uno de los datos que componen productInfo
        for (ArrayList<ArrayList<String>> listGrande : productInfo) {
            aux.add(new ArrayList<>());
            for (ArrayList<String> listPeq : listGrande) {
                aux.get(aux.size()-1).add((ArrayList<String>) listPeq.clone());
            }
        }

        for (int i=0; i < aux.size(); i++) {
            // Solo analizaremos cuando el atributo contenga la palabra action
            if (aux.get(i).get(0).get(0).contains("action")) { // Dentro de cada action, se inicializan los datos necesarios para el register de fabricacion de cada pieza
                aux.get(i).get(2).add("actualMachineId");
                aux.get(i).get(2).add("actualStationId");
                aux.get(i).get(2).add("startTime");
                aux.get(i).get(2).add("finishTime");

                aux.get(i).get(3).add(" ");  // Hasta que la pieza sea fabricada se desconocen los datos
                aux.get(i).get(3).add(" ");
                aux.get(i).get(3).add(" ");
                aux.get(i).get(3).add(" ");
            }
        }

        // Se identifica el ID de cada Item y se añade en el registro de datos productsTraceability
        String[] separatedItemsID = itemsID.split(",");
        // Para evitar la vinculacion entre items y no se multipliquen los datos, en cada iteracion se reinicia la variable aux2
        for (int i = 0; i < separatedItemsID.length; i++) {
            //
            ArrayList<ArrayList<ArrayList<String>>> aux2 = new ArrayList<ArrayList<ArrayList<String>>>();
            for (ArrayList<ArrayList<String>> listGrande : aux) {
                aux2.add(new ArrayList<>());
                for (ArrayList<String> listPeq : listGrande) {
                    aux2.get(aux2.size()-1).add((ArrayList<String>) listPeq.clone());
                }
            }
            productsTraceability.add(aux2); // Se añadira tantas veces como el numero de items que componen el batch
            productsTraceability.get(i).get(0).get(2).add("itemID");
            productsTraceability.get(i).get(0).get(3).add(separatedItemsID[i]); //A cada item se le añade su ID para despues poder ser identificado

        }
        System.out.println("PRODUCT TRACEABILITY OF " + myAgent.getLocalName() + ":\n" + productsTraceability);
        System.out.println("\n");
    }

    protected ArrayList<String> take_item_references(String rawdata){
        ArrayList<String> data= new ArrayList<String>(Arrays.asList(rawdata.split("_")));
        ArrayList<String> item_references= new ArrayList<String>();
        for(int i=0;i<data.size();i++){
            String temp=data.get(i);
            String[] parts=temp.split("/");

            if(parts[0].contains("&")){
                String[] parts2=parts[0].split("&");
                item_references.add(parts2[1]);
            }else{
                item_references.add(parts[0]);
            }
        }

        return item_references;
    }

    protected ArrayList<String> take_finish_times(String rawdata){
        ArrayList<String> data= new ArrayList<String>(Arrays.asList(rawdata.split("_")));
        ArrayList<String> itemFT= new ArrayList<String>();
        for(int i=0;i<data.size();i++){
            String temp=data.get(i);
            String[] parts=temp.split("/");
            itemFT.add(parts[1]);     //Por ahora solo se coge el dato del tiempo, el cual asumimos que está bien ordenado en el plan
        }

        return itemFT;
    }

    protected Date FIUpdateFinishTimes(){  //Actualiza el finish time para el primer item cuando ha habido delay
        Date expected_FT=null;
            try {
                expected_FT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(items_finish_times.get(actual_item_number));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        long startime = date_when_delay_was_asked.getTime() - (delaynum);    //Para calcular el tiempo de operación se necesita calcular el start time de la primera operacion
//                            Date d = new Date(startime); //para debug
        LocalDateTime new_expected_finish_time = convertToLocalDateTimeViaSqlTimestamp(getactualtime());
        new_expected_finish_time = new_expected_finish_time.plusSeconds(((expected_FT.getTime() - startime) / 1000));
        expected_FT = convertToDateViaSqlTimestamp(new_expected_finish_time);   //el finish time se calcula segun el tiempo de operacion y la fecha actual
        delay_already_incremented = true;
        System.out.println("ITEM FINISH-TIME UPDATED");
        System.out.println("***********************************"+itemreference.get(actual_item_number)+" item expected finish time: "+expected_FT+"***********************************");
        return expected_FT;
    }


    protected Date UpdateFinishTimes(int itemnumber){ //Actualiza el finish time para los item que no son el primero
        now = getactualtime();
        Date finish_date_last_item = null;
        try {
            finish_date_last_item = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(items_finish_times.get(itemnumber - 1));
            expected_finish_date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(items_finish_times.get(itemnumber));
            LocalDateTime new_expected_finish_time = convertToLocalDateTimeViaSqlTimestamp(now);
            new_expected_finish_time = new_expected_finish_time.plusSeconds(((expected_finish_date.getTime() - finish_date_last_item.getTime()) / 1000));
            expected_finish_date = convertToDateViaSqlTimestamp(new_expected_finish_time);
            System.out.println("***********************************"+"New expected finish time on item "+itemreference.get(actual_item_number)+": "+expected_finish_date+"***********************************");

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return expected_finish_date;
    }

    public String GetItemIDForTraceability(String numberofitem, ArrayList<String> Ireferences) {
        String itemreference;
        int index=Integer.parseInt(numberofitem);
        itemreference=Ireferences.get(index-1);
        return itemreference;
    }




}




