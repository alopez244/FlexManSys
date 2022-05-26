package es.ehu.domain.manufacturing.agents.functionality;

import com.google.gson.Gson;
import es.ehu.domain.manufacturing.utilities.StructBatchAgentState;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.template.interfaces.AvailabilityFunctionality;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.utilities.XMLReader;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.commons.io.FilenameUtils;
import java.io.File;
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
    private Object[] myReplicasID  = new Object[2];
    private volatile Date now=null;
    private volatile Date date_when_delay_was_asked=null;
    private volatile Date expected_finish_date=null;
    private volatile ArrayList<String> items_finish_times=new ArrayList<String>();
    private volatile int actual_item_number=0;
    private volatile long delaynum=0;
    private volatile boolean update_timeout_flag=false;
    private volatile AID QoSID = new AID("QoSManagerAgent", false);
    private volatile boolean reset_flag=false;
    private volatile boolean delay_already_incremented=false;
    private ArrayList<ACLMessage> posponed_msgs_to_order=new ArrayList<ACLMessage>();
    public boolean operations_redistributed_flag=false;


    @Override
    public void setState(String state) {

        Gson gson = new Gson();
        StructBatchAgentState BAstate = gson.fromJson(state, StructBatchAgentState.class);
        productsTraceability=BAstate.getproductsTraceability();
        actionList=BAstate.getactionList();
        firstTime=BAstate.getfirstTime();
        parentAgentID=BAstate.getparenAgentID();
        myAgent.replicas=BAstate.getreplicas();
        finish_times_of_batch=BAstate.getFT();
        actual_item_number=BAstate.getitemNbr();
        date_when_delay_was_asked=BAstate.getDateOfDelayAsk();
        if(date_when_delay_was_asked==null){
            expected_finish_date=null;
            System.out.println("Timeout won't be able to start");
        }else{
            expected_finish_date=BAstate.getExpFinishDate();
        }
        delaynum=BAstate.getdelaynum();
        itemreference=take_item_references(finish_times_of_batch);
        items_finish_times=take_finish_times(finish_times_of_batch);

    }
    @Override
    public String getState(){

        myAgent.replicas=update_tracking_replicas();
        StructBatchAgentState BAstate=new StructBatchAgentState();
        BAstate.setproductsTraceability(productsTraceability);
        BAstate.setactionList(actionList);
        BAstate.setfirstTime(firstTime);
        BAstate.setparenAgentID(parentAgentID);
        BAstate.setreplicas(myAgent.replicas);
        BAstate.setFT(finish_times_of_batch);
        BAstate.setitemNbr(actual_item_number);
        BAstate.setDateOfDelayAsk(date_when_delay_was_asked);
        BAstate.setdelaynum(delaynum);
        BAstate.setExpFinishDate(expected_finish_date);

        Gson gson = new Gson();
        String state=gson.toJson(BAstate);

        return state;

    }


    class timeout extends Thread{
    private boolean timeout=false;
        public void run() {

            System.out.println("Item timeout initialized");

            items_finish_times = take_finish_times(finish_times_of_batch); //extrae la información útil que se usa en el timeout
            itemreference=take_item_references(finish_times_of_batch);//nos devuelve las referencias de los item para este batch, ordenados como los tiempos extraidos en la anterior función
            if(!reset_flag) { //si no ha sido un reset pedido externamente debemos construir los FT, si es un reset por replica caida etc., ya los tenemos
                try {
                    expected_finish_date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(items_finish_times.get(actual_item_number));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            while (actual_item_number < items_finish_times.size() && !timeout) {  //Mientras que no estemos en la ultima pieza o se haya lanzado timeout
                if(getactualtime().after(expected_finish_date)&& !timeout){ //Si el tiempo actual es superior a la fecha de FT hay que hacer algo
                    if(!delay_already_incremented){ //en caso de no haberse incrementado el timeout con el delay aun, se realiza
                        expected_finish_date=FIUpdateFinishTimes();

                        //Actualiza el estado de las replicas tras aumentar el expected finish date con el delay
                        String currentState = (String) ((AvailabilityFunctionality) myAgent.functionalityInstance).getState(); //se actualiza el estado de las replicas
                        if (currentState != null) {
                            System.out.println("Send state");
                            myAgent.sendStateToTracking(currentState,"batch"); //comunicamos a las replicas nuestro estado
                        }
                    }else { //si ya ha sido incrementado el delay no queda otra que considerarlo timeout y el thread desaparece
                        System.out.println(getactualtime() + " " + myAgent.getLocalName() + " WARN " + batchreference + " batch has thrown a timeout on item number " + itemreference.get(actual_item_number) + " Checking failure with QoS Agent...");
                        ACLMessage report_to_QoS= sendACLMessage(ACLMessage.FAILURE, QoSID, "timeout", "timeout " + batchreference, batchreference + "/" + itemreference.get(actual_item_number), myAgent); //avisa al QoS de fallo por timeout
                        myAgent.AddToExpectedMsgs(report_to_QoS);
                        timeout = true;
                    }
                }

                while (getactualtime().before(expected_finish_date)&&actual_item_number<items_finish_times.size()&&!timeout) {  //se queda a la espera siempre que no se supere la fecha de finishtime ya incrementada por el delay

                    if (update_timeout_flag) { //el flag se pone a 1 al recibir la trazabilidad de un item completo
                        actual_item_number++; //incrementamos el item actual
                        if(actual_item_number<items_finish_times.size()) {
                            expected_finish_date = UpdateFinishTimes(actual_item_number); //actualiza la fecha esperada de finalizacion a la del siguiente item
                            update_timeout_flag = false;
                        }
                    }
                }
            }
            if(!timeout){
                System.out.println("Batch finished");
            }else{
                System.out.println("Timeout thrown");
            }
            System.out.println("Item timeout finished.");
        }
    }

    @Override
    public Void init(MWAgent myAgent) {

        this.myAgent = myAgent;
         myAgent.get_timestamp(myAgent,"CreationTime");
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
                sendACLMessage(16, plannerID,"Ftime_batch_ask", "finish_time", reference.getContent(), myAgent ); //pide el finish time de cada item al planner
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
        if (input!= null) {
            if (input[0] != null) {

                HashMap infoForTraceability = new HashMap();

                System.out.println("El agente " + myAgent.getLocalName() + " esta en el metodo execute de su estado running");

                System.out.println("Ahora el agente " + myAgent.getLocalName() + " se va a quedar a la espera de la informacion de las operaciones");

                ACLMessage msg = (ACLMessage) input[0];

                if (msg.getPerformative() == ACLMessage.INFORM && msg.getOntology().equals("askdelay")) { //si es un mensaje con info del delay creamos el timeout
                    date_when_delay_was_asked = getactualtime();
                    String delay = msg.getContent();
                    delaynum = Long.parseLong(delay);
                    AID OrderAgent = new AID(parentAgentID, false);

                    try {
                        batchName = sendCommand(myAgent, "get " + myAgent.getLocalName() + " attrib=parent", "name"); //consigue el nombre del batch
                        ACLMessage reference = sendCommand(myAgent, "get " + batchName.getContent() + " attrib=reference", "Reference");
                        sendACLMessage(ACLMessage.INFORM, OrderAgent, "delay", "inform delay", reference.getContent() + "/" + delay, myAgent); //informa al parent
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    timeout thread = new timeout();
                    thread.start(); //se inicia el timeout tras recibir delay
                    if(operations_redistributed_flag){
                        myAgent.get_timestamp(myAgent,"RecoveredTimeoutBatch");
                        operations_redistributed_flag=false;
                    }
                } else if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().equals("reset_timeout")) {

                    reset_flag = true;
                    System.out.println("Timeout has been asked to be reset");
                    if (date_when_delay_was_asked == null || delaynum == 0 || expected_finish_date == null) {
                        System.out.println("Error starting timeout. Some data has been lost.");
                    } else {
                        Date last_expected_finish_time = expected_finish_date;
                        if (actual_item_number == 0) {
                            expected_finish_date = FIUpdateFinishTimes();
                        } else {
                            expected_finish_date = UpdateFinishTimes(actual_item_number);
                        }
                        long difference = expected_finish_date.getTime() - last_expected_finish_time.getTime();
                        AID OrderAgent = new AID(parentAgentID, false);
                        try {
                            batchName = sendCommand(myAgent, "get " + myAgent.getLocalName() + " attrib=parent", "name"); //consigue el nombre del batch
                            ACLMessage reference = sendCommand(myAgent, "get " + batchName.getContent() + " attrib=reference", "Reference");
                            sendACLMessage(ACLMessage.INFORM, OrderAgent, "update_timeout", "timeout", reference.getContent() + "/" + difference, myAgent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        timeout thread = new timeout(); //reinicio de timeout
                        thread.start();
                    }

                } else if(msg.getPerformative() == ACLMessage.INFORM && msg.getOntology().equals("rebuild_finish_times")){ //Si una maquina asume el trabajo de otra, el timeout hace falta rehacerlo
                    System.out.println("New machine is assigned to batch "+batchreference+". Timeouts must be rebuilt for new operation times.");
                    //se resetean las variables para emular el estado inicial del agente antes de crear el threat de timeout
                    delay_already_incremented=false;
                    date_when_delay_was_asked=null;
                    delaynum=0;
                    //empezaria por el item 0 bajo el criterio del timeout pej: Plan original ->1111 (item 0), 1112 (item 1), 1113 (item 2) / Plan recuperado tras el primer item -> 1112 (item 0), 1113 (item 1)
                    actual_item_number=0;
                    reset_flag=false;
                    finish_times_of_batch=msg.getContent(); //los finishtime se reciben del agente maquina segun el tiempo que tarde en ejecutarlos
                    sendACLMessage(16,QoSID,"askdelay","delayasking",batchreference,myAgent); //pide el delay al QoS y de aquí en adelante es como al inicio del plan
                    operations_redistributed_flag=true;
                } else if (msg.getPerformative() == ACLMessage.INFORM && msg.getOntology().equals("data")) {

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
                    update_timeout_flag = true; //se pide al timeout que actualice el item
                    itemreference = take_item_references(finish_times_of_batch);
                    for (int i = 0; i < productsTraceability.size(); i++) {
                        // Se identifica la estructura de datos correspondiente al item que se ha fabricado
                        if (productsTraceability.get(i).get(0).get(3).size() > 2) { //se comprueba que contenga mas de dos elementos, ya que en la siguiente linea se accede al tercero
                            if (productsTraceability.get(i).get(0).get(3).get(3).equals(GetItemIDForTraceability(idItem, itemreference))) {
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
                        sendACLMessage(7, QoSID, "batch_finish", batchNumber + " finish", batchNumber, myAgent);
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
//                        System.out.println(msgToOrder);
                        try {
//                        ACLMessage order_parent= sendCommand(myAgent, "get "+parentAgentID+" attrib=parent", "OrderAgentID");
                            ACLMessage batch_parent = sendCommand(myAgent, "get " + myAgent.getLocalName() + " attrib=parent", myAgent.getLocalName() + "_parent");
                            ACLMessage order_parent = sendCommand(myAgent, "get " + batch_parent.getContent() + " attrib=parent", myAgent.getLocalName() + "_parent_parent");

                            posponed_msgs_to_order = myAgent.msg_buffer.get(order_parent.getContent()); //se comprueba si hay mensajes a la espera de ser enviados al receptor
                            if (posponed_msgs_to_order == null) {  //si no se encuentra el parent en el listado de mensajes postpuestos entonces el receptor ha confirmado la recepcion de todos los mensajes hasta ahora. Seguimos con la ejecución normal.
                                ACLMessage running_replica = sendCommand(myAgent, "get * parent=" + order_parent.getContent() + " state=running", myAgent.getLocalName() + "_parent_running_replica");
                                if (!running_replica.getContent().equals("")) {
                                    AID orderAgentID = new AID(running_replica.getContent(), false);
                                    ACLMessage msg_to_order = sendACLMessage(7, orderAgentID, "Information", "ItemsInfo", msgToOrder, myAgent);
                                    myAgent.AddToExpectedMsgs(msg_to_order);
                                } else { //si el SMA nos devuelve "" significa que actualmente no hay una replica en running. Se guarda el mensaje apra cuando haya uno
                                    posponed_msgs_to_order = new ArrayList<ACLMessage>();
                                    ACLMessage msg_to_buffer = new ACLMessage(ACLMessage.INFORM);
                                    msg_to_buffer.setConversationId("ItemsInfo");
                                    msg_to_buffer.setContent(msgToOrder);
                                    msg_to_buffer.setOntology("Information");
                                    posponed_msgs_to_order.add(msg_to_buffer);
                                    myAgent.msg_buffer.put(order_parent.getContent(), posponed_msgs_to_order);
                                }
                            } else {  //si el receptor tenia algun mensaje retenido significa que el agente sigue denunciado y aun no se ha resuelto el problema. Se añade el mensaje al listado.

                                System.out.println("Added message to buffer:\nContent" + "\nTo: " + order_parent.getContent() + "\n Still waiting for a solution.");
                                ACLMessage msg_to_buffer = new ACLMessage(ACLMessage.INFORM);
                                msg_to_buffer.setConversationId("ItemsInfo");
                                msg_to_buffer.setContent(msgToOrder);
                                msg_to_buffer.setOntology("Information");
                                posponed_msgs_to_order.add(msg_to_buffer); //se añade el mensaje a la lista de mensajes retenidos
                                myAgent.msg_buffer.put(order_parent.getContent(), posponed_msgs_to_order);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (actionList.size() == 0) { // cuando todas las acciones se han completado, se elimina el batch agent
                        return true; //Batch agent a terminado su funcion y pasa a STOP
                    }
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
        myAgent.get_timestamp(myAgent,"FinishTime");
        if(myAgent.ActualState=="running"){ //para filtrar las replicas ejecutando terminate
            try {
                ACLMessage reply = sendCommand(myAgent, "get * reference=" + batchNumber, "parentAgentID");
                //returns the names of all the agents that are sons
                if (reply != null)   // Si no existe el id en el registro devuelve error
                    parentName = reply.getContent(); //gets the name of the agent´s parent

                KillReplicas(myAgent);
//            sendACLMessage(7, Agent, myAgent.getLocalName(), "Shutdown", "Batch completed", myAgent);

                ACLMessage order_parent= sendCommand(myAgent, "get "+parentName+" attrib=parent", myAgent.getLocalName()+"_parent_parent");
                ACLMessage running_replica = sendCommand(myAgent, "get * parent=" + order_parent.getContent()+" state=running", myAgent.getLocalName()+"_parent_running_replica");
                AID Agent = new AID(running_replica.getContent(), false);
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
//        System.out.println("PRODUCT TRACEABILITY OF " + myAgent.getLocalName() + ":\n" + productsTraceability);
//        System.out.println("\n");
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




