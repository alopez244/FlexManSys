package es.ehu.domain.manufacturing.agents.functionality;

import com.google.gson.Gson;
import es.ehu.domain.manufacturing.agents.MachineAgent;
import es.ehu.domain.manufacturing.behaviour.AssetManagementBehaviour;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.NegotiatingBehaviour;
import es.ehu.domain.manufacturing.template.interfaces.AssetManagement;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.NegFunctionality;
import es.ehu.platform.utilities.Cmd;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Machine_Functionality extends DomRes_Functionality implements BasicFunctionality, NegFunctionality, AssetManagement {
    private boolean firstItemFlag=false;
    private static final long serialVersionUID = -4307559193624552630L;
    static final Logger LOGGER = LogManager.getLogger(Machine_Functionality.class.getName());
    private ArrayList<ArrayList<String>> productInfo;
    private HashMap<String, String> operationsWithBatchAgents = new HashMap<>();
    private HashMap PLCmsgIn = new HashMap(); // Estructura de datos que se envia al PLC
    private HashMap PLCmsgOut = new HashMap(); // Estructura de datos que se recibe del PLC
    private String BathcID = ""; // Variable que guarda el identificador del lote que se esta fabricando
    private Integer NumOfItems = 0; // Representa el numero de intems que se estan fabricando (todos perteneciente al mismo lote)
    private Integer machinePlanIndex = 0; // Indice dentro de la estructura de datos "MachinePlan" hasta donde se ha analizado
    private Boolean sendingFlag = false; // Flag que se activa solo cuando la maquina este preparado para recibir una nueva orden
    private Boolean matReqDone = false; // Flag que se mantiene activo desde que se hace la peticion de consumibles hasta que se reponen
    private Boolean requestMaterial = false; // Flag que se activa cuando se necesita hacer una peticion de consumibles
    private Boolean orderQueueFlag = false; // Flag que se activa cuando existen nuevas ordenes en cola para la maquina
    private AID gatewayAgentID =null;

    private MessageTemplate QoStemplate=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchOntology("acl_error"));

    private AID QoSID = new AID("QoSManagerAgent", false);
    public ArrayList<ACLMessage> posponed_msgs_to_batch=new ArrayList<ACLMessage>();
    public ArrayList<ACLMessage> posponed_msgs_to_gw=new ArrayList<ACLMessage>();
//    public static String state ="";
//    public static boolean change_state=false;



    private MessageTemplate template = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchOntology("data")),MessageTemplate.MatchConversationId("ProvidedConsumables"));;

    /** Identifier of the agent. */
    private MachineAgent myAgent;


    /** Class name to switch on the agent */
    private String className;

    /**
     * Initialize the functional properties
     *
     * @return The name used for registering the agent in the MWM (or SM)
     */
    @Override
    public Void init(MWAgent mwAgent) {

        //First of all, the connection with the asset must be checked

        //Later, if the previous condition is accomplished, the agent is registered
        this.myAgent = (MachineAgent) mwAgent;
        LOGGER.entry();

        String machineName = myAgent.resourceName;
        Integer machineNumber = Integer.parseInt(machineName.split("_")[1]);
        myAgent.gatewayAgentName = "ControlGatewayCont" + machineNumber.toString(); //Se genera el nombre del Gateway Agent con el que se tendrá que comunicar
        //First, the Machine Model is read
        gatewayAgentID = new AID(myAgent.gatewayAgentName, false);



        String [] args = (String[]) myAgent.getArguments();

        for (int i=0; i<args.length; i++){
            if (args[i].toLowerCase().startsWith("id=")) return null;
        }
        //******************************Checkeo del estado del GW y PLC. Agente maquina no iniciará hasta tenerlos disponibles
//        sendACLMessage(16,gatewayAgentID,"ping","","",myAgent);
//        ACLMessage answer_gw = myAgent.blockingReceive(MessageTemplate.MatchOntology("ping"), 300);
//        if(answer_gw==null){
//            System.out.println("GW is not online. Start GW and repeat.");
//            System.exit(0);
//        }
//
//        sendACLMessage(16, gatewayAgentID, "check_asset","check_asset_on_boot_"+convIDcnt++,"ask_state",myAgent); //primero antes de nada debemos comprobar si el agente GW y el PLC están disponibles
//        ACLMessage answer = myAgent.blockingReceive(MessageTemplate.MatchOntology("asset_state"), 300);
//        if(answer!=null){
//            if(!answer.getContent().equals("Working")&&!answer.getContent().equals("Not working")){
//                System.out.println("PLC is not prepared to work.");
//                System.exit(0); //si el PLC o el GW no están disponible no tiene sentido que iniciemos el agente máquina
//            }else{
//                System.out.println("PLC is "+answer.getContent());
//            }
//        }else{
//            System.out.println("PLC is not prepared to work.");
//            System.exit(0); //si el PLC o el GW no están disponible no tiene sentido que iniciemos el agente máquina
//        }
        //*************************************
        //First, the machine attributes are included
        String attribs = "";
        for (int j = 0; j < myAgent.resourceModel.get(0).get(2).size(); j++){
            attribs += " "+myAgent.resourceModel.get(0).get(2).get(j)+"="+myAgent.resourceModel.get(0).get(3).get(j);
        }
        //Secondly, the machine operations are appended to the attribs string
        attribs = attribs + " simpleOperations=";
        for (int j = 0; j < myAgent.resourceModel.size(); j++){
            if (myAgent.resourceModel.get(j).get(0).get(0).startsWith("simple")){
                for (int k = 0; k < myAgent.resourceModel.get(j).get(2).size();k++){
                    if (myAgent.resourceModel.get(j).get(2).get(k).startsWith("id")) attribs += myAgent.resourceModel.get(j).get(3).get(k)+",";
                }
            }
        }
        attribs=attribs.substring(0,attribs.length()-1);

        attribs = attribs + " complexOperations=";
        for (int j = 0; j < myAgent.resourceModel.size(); j++){
            if (myAgent.resourceModel.get(j).get(0).get(0).startsWith("complex")){
                for (int k = 0; k < myAgent.resourceModel.get(j).get(2).size();k++){
                    if (myAgent.resourceModel.get(j).get(2).get(k).startsWith("id")) attribs += myAgent.resourceModel.get(j).get(3).get(k)+",";
                }
            }
        }
        attribs=attribs.substring(0,attribs.length()-1);

        //Thirdly, the ProcessNodeAgent is registered in the System Model

        String cmd = "reg machine parent=system"+attribs;

        ACLMessage reply = null;
        try {
            reply = myAgent.sendCommand(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String seId = reply.getContent();


        LOGGER.info(myAgent.getLocalName()+" ("+cmd+")"+" > mwm < "+seId);

//        Fourthly, the material available in the station is counted
        int index = 0;
        for (int i = 0; i < myAgent.resourceModel.size() - 1; i++) {
            if (myAgent.resourceModel.get(i).get(0).get(0).equals("buffer")){
                myAgent.availableMaterial.add(new HashMap<>());   //Se añaden niveles nuevos para poder ser rellenados con datos
                myAgent.availableMaterial.get(index).put("consumable_id", myAgent.resourceModel.get(i).get(3).get(0));   // Valor de consumable_id
                myAgent.availableMaterial.get(index).put("current", myAgent.resourceModel.get(i).get(3).get(1));   // Valor de piezas disponibles
                myAgent.availableMaterial.get(index).put("max", myAgent.resourceModel.get(i).get(3).get(4));   // Valor de capacidad maxima
                myAgent.availableMaterial.get(index).put("warning", myAgent.resourceModel.get(i).get(3).get(5));   // Valor de warning
                index++;
            }
        }

        //Finally, the MachineAgent is started.

        try {
            // Agent generation
            className = myAgent.getClass().getName();
            String [] args2 = {"ID="+seId, "description=description" };
            args = ArrayUtils.addAll(args,args2);
            ((AgentController)myAgent.getContainerController().createNewAgent(seId,className, args)).start();

            Thread.sleep(1000);
        } catch (Exception e1) {
            e1.printStackTrace();
        }


        //myAgent.initTransition = ControlBehaviour.RUNNING;

        return null;

        }

    @Override
    public Object execute(Object[] input) {

//        try { Thread.sleep(5000);} catch (InterruptedException e) {e.printStackTrace();}  //con el buffer de mensajes esta espera es innecesaria

        System.out.println("El agente recurso " + myAgent.getLocalName() + " ya esta en el metodo execute");

        if (input[0] != null) {
            ACLMessage msg = (ACLMessage) input[0];
            myAgent.Acknowledge(msg,myAgent);

            if (msg.getContent().equals("All manufacturing plan ready to run")) {

                System.out.println("Ya estoy listo para empezar a hacer mis operaciones");
                // La maquina y los agentes del plan ya estarian listos, pero habria que comprobar la hora de la operacion mas cercana
                // Asi, si la maquina esta lista pero la operacion empieza en 30 minutos, la maquina tendra que esperar ese tiempo
                // Si la maquina esta lista y la hora de la primera operacion ya ha pasado, podrá comenzar de seguido

            } else {
                String[] allOperations = msg.getContent().split("&");
                for (int i = 0; i < allOperations.length; i++) {

                    ArrayList<ArrayList<String>> operationInfo = new ArrayList<>();

                    ArrayList<String> names = new ArrayList<>();
                    ArrayList<String> values = new ArrayList<>();

                    String[] AllInformation = allOperations[i].split(" ");
                    for (String info : AllInformation) {
                        String attrName = info.split("=")[0];
                        String attrValue = info.split("=")[1];

                        names.add(attrName);
                        values.add(attrValue);
                    }

                    ArrayList<String> aux = new ArrayList<>();
                    ArrayList<String> aux2 = new ArrayList<>();
                    aux.add("operation");
                    aux2.add("3");
                    operationInfo.add(0, aux);
                    operationInfo.add(1, aux2);
                    operationInfo.add(2, names);
                    operationInfo.add(3, values);

                    myAgent.machinePlan.add(operationInfo);

                }

                if(orderQueueFlag == false) {   //flags update to avoid order overlaps
                    sendingFlag = true;
                    orderQueueFlag = true;
                }
            }
        }

        return LOGGER.exit(null);
    }

    @Override
    public Void terminate(MWAgent myAgent) { return null;}

    @Override
    public long calculateNegotiationValue(String negAction, String negCriterion, Object... negExternalData) {
        // TODO
        // negExternalData --> batchAgentID, numOfItems
        String seID = (String)negExternalData[0];
        String seNumOfItems = (String) negExternalData[1];
        int numItems = Integer.parseInt(seNumOfItems);
        String seOperationID = (String)negExternalData[2];

        Random r = new Random();
        return r.nextInt(1000)*numItems;
    }

    @Override
    public int checkNegotiation(String conversationId, String sAction, double negReceivedValue, long negScalarValue, boolean tieBreak, boolean checkReplies, boolean isPartialWinner, Object... negExternalData) {

        LOGGER.entry(conversationId, sAction, negReceivedValue, negScalarValue);

        String seID = (String)negExternalData[0];
        String seNumOfItems = (String)negExternalData[1];
        String seOperationID = (String)negExternalData[2];

        if (negReceivedValue<negScalarValue) return NegotiatingBehaviour.NEG_LOST; //pierde negociación
        if ((negReceivedValue==negScalarValue) && !tieBreak ) return NegotiatingBehaviour.NEG_LOST; //empata negocicación pero no es quien fija desempate

        LOGGER.info("es el ganador ("+negScalarValue+")");
        if (!checkReplies) return NegotiatingBehaviour.NEG_PARTIAL_WON; // es ganador parcial, faltan negociaciones por finalizar

        LOGGER.info("ejecutar "+sAction);

        Cmd action = new Cmd(sAction);

        if (action.cmd.equals("execute")) {
            operationsWithBatchAgents.put(seOperationID, seID);

            // Envio un mensaje al BatchAgent para avisarle de que soy el ganador para asociarme esa operacion
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID(seID, AID.ISLOCALNAME));
            msg.setContent("I am the winner of:" + seOperationID);
            msg.setConversationId(conversationId);
            myAgent.send(msg);

            System.out.println("\tI am the winner to get operation " +seOperationID+ " from batch " + seID + ". NumItems: " + seNumOfItems);

            Object[] data = new Object[2];
            data[0]=seOperationID;
            data[1]=seID;
            execute(data);

        }

        return NegotiatingBehaviour.NEG_WON;
    }

    public void sendDataToDevice() {

        String targets = "";
        // Se reciben los mensajes ACL que corresponden al material que se ha repuesto
        // El codigo implementado dentro de la condicion IF se encarga de añadir al contador de consumibles los nuevos consumibles que han sido repuestos
        ACLMessage msg = myAgent.receive(template);
        if (msg != null) {
            myAgent.msgFIFO.add((String) msg.getContent());
            ArrayList<ArrayList<String>> newConsumables = new ArrayList<>();
            newConsumables.add(new ArrayList<>()); newConsumables.add(new ArrayList<>());
            String content = msg.getContent();
            String [] contentSplited = content.split(";");
            for (int i = 0; i < contentSplited.length ; i++) {  //Se deserializa el mensaje y se guardan los datos en un arraylist
                newConsumables.get(0).add(contentSplited[i].split(":")[0]);
                newConsumables.get(1).add(contentSplited[i].split(":")[1]);
            }
            // bucle para sumar los nuevos consumibles en el contador de material
            for (int i = 0; i < newConsumables.get(0).size(); i++){
                for (int j = 0; j < myAgent.availableMaterial.size(); j++){
                    if (newConsumables.get(0).get(i).equals(myAgent.availableMaterial.get(j).get("consumable_id"))) {
                        Integer currentConsumable = Integer.parseInt(myAgent.availableMaterial.get(j).get("current"));
                        Integer addedconsumable = Integer.parseInt(newConsumables.get(1).get(i));
                        myAgent.availableMaterial.get(j).put("current", Integer.toString(currentConsumable + addedconsumable));
                    }
                }
            }
            System.out.println("El transporte ha terminado de reponer el material pedido");
            System.out.println(myAgent.availableMaterial);
            matReqDone = false; // Una vez repuesto el material se resetea el flag
        }

        if(sendingFlag == true) {     //It is checked if the method is correctly activated and that orders do not overlap
            ArrayList<String> consumableList = new ArrayList<String>();
            Boolean consumableShortage = false; // Flag que se activa cuando no hay material suficiente para realizar una nueva orden
            String serviceType;

            if (myAgent.machinePlan.size() > 2) {   //checks that there are operations in the machine plan
                PLCmsgOut = createOperationHashmap(myAgent.machinePlan, machinePlanIndex); //Looks for the operation to be manufactured in the machine plan
                NumOfItems = (Integer) PLCmsgOut.get("Operation_No_of_Items");
                BathcID = Integer.toString((Integer) PLCmsgOut.get("Id_Batch_Reference"));
                for (int j = 0; j < myAgent.machinePlan.size(); j++) {  // saves the machine´s reference
                    if (myAgent.machinePlan.get(j).get(0).get(0).equals("station")) {
                        PLCmsgOut.put("Id_Machine_Reference", Integer.parseInt(myAgent.machinePlan.get(j).get(3).get(0)));
                        break;
                    }
                }
                // Knowing Ref_Service_Type, identification of the consumables that will be needed for manufacturing
                serviceType = Integer.toString((Integer) PLCmsgOut.get("Operation_Ref_Service_Type"));
                consumableList = defineConsumableList(serviceType, myAgent.resourceModel);

                // Se comprueba que se disponga de material suficiente para poder fabricar el lote
                for (int i = 0; i < myAgent.availableMaterial.size(); i++) {
                    if (consumableList.contains(myAgent.availableMaterial.get(i).get("consumable_id"))) {
                        if (Integer.parseInt(myAgent.availableMaterial.get(i).get("current")) < NumOfItems) {
                            consumableShortage = true; // No hay material suficiente, por lo que se activa el flag para hacer una petición de material y añalizar operaciones en cola
                        }
                    }
                }
                // Solo se envia la operación si hay material suficiente
                if (!consumableShortage) {

                    for(int j = 0 ; j < myAgent.machinePlan.size()&&firstItemFlag==false;j++) {    //Buscamos de todos los plannedStartTime el primero (se asume que estan ordenados)
                        if (myAgent.machinePlan.get(j).get(0).get(0).equals("operation")) {
                            for (int k = 0; k < myAgent.machinePlan.get(j).get(2).size() && firstItemFlag == false; k++) {
                                if (myAgent.machinePlan.get(j).get(2).get(k).equals("plannedStartTime")) {
                                    String starttime = myAgent.machinePlan.get(j).get(3).get(k);
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); //Se usa el formato XS:DateTime
                                    SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"); //Se usa el formato con milisegundos agregados para mayor precision
                                    Date date2 = getactualtime();
//                                    System.out.println("Hora actual: " + hora + ":" + minutos + ":" + segundos);
                                    Date date1 = null;

                                    try {
                                        date1 = formatter.parse(starttime);

                                    } catch (ParseException e) {
                                        System.out.println("ERROR dando formato a una fecha");
                                        e.printStackTrace();
                                    }
                                    while (date1.after(date2)) { //Se queda actualizando la fecha hasta que se alcance la fecha definida en el plan de fabricación
                                        date2 = getactualtime();
                                    }
                                    long diferencia = ((date2.getTime() - date1.getTime())); //calculamos el retraso en iniciar en milisegundos
                                    AID QoSID = new AID("QoSManagerAgent", false);
                                    String content = BathcID;
                                    String delay = String.valueOf(diferencia);
                                    content = content + "/" + delay;
                                    sendACLMessage(ACLMessage.INFORM, QoSID, "delay", "batch_delay", content, myAgent);
                                    firstItemFlag = true;
                                }
                            }
                        }
                    }
                }
                firstItemFlag = false;

                PLCmsgOut.remove("Index");
                String MessageContent = new Gson().toJson(PLCmsgOut);
                AID gatewayAgentID = new AID(myAgent.gatewayAgentName, false);
                ACLMessage msg_to_gw=sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "data", "PLCdata", MessageContent, myAgent);

                myAgent.AddToExpectedMsgs(msg_to_gw);

                sendingFlag = false;
                machinePlanIndex = 0;

            } else { // en caso contrario, se analizan las operaciones en cola para poder ser enviados
                System.out.println("El lote " + BathcID + " no se puede fabricar por falta de material");
                machinePlanIndex = (Integer) PLCmsgOut.get("Index");
                if (machinePlanIndex <= myAgent.machinePlan.size() - 1) {
                    sendDataToDevice();
                } else {
                    System.out.println("No es posible fabricar ninguna orden en cola por falta de material");
                    machinePlanIndex = 0;
                    if (!matReqDone) { // Si aun no se ha hecho la petición de material se procede a hacerlo
                        String neededMaterial = "";
                        Integer neededConsumable = 0;
                        for (int j = 0; j < myAgent.availableMaterial.size(); j++){ // Cálculo del material necesario para llenar el alimentador de piezas al maximo
                            int currentConsumables = Integer.parseInt(myAgent.availableMaterial.get(j).get("current"));
                            neededConsumable = Integer.parseInt(myAgent.availableMaterial.get(j).get("max")) - currentConsumables;
                            neededMaterial = neededMaterial.concat(myAgent.availableMaterial.get(j).get("consumable_id") + ":" + Integer.toString(neededConsumable) + ";");
                        }

                        //Peticion negociacion entre los agentes transporte disponibles
                        try {
                            ACLMessage reply2 = sendCommand(myAgent, "get * category=transport", "TransportAgentID");
                            if (reply2 != null) {   // If the id does not exist, it returns error
                                targets = reply2.getContent();
                            }
                            String negotiationQuery = "localneg " + targets + " criterion=position action=" +
                                    "supplyConsumables externaldata=" + neededMaterial + "," + myAgent.getLocalName();
                            ACLMessage result = sendCommand(myAgent, negotiationQuery, "TransportAgentNeg");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        matReqDone = true;
                    }
                }
            }
        } else {
            //System.out.println("No operations defined");
            PLCmsgOut.put("Control_Flag_New_Service", false);
            sendingFlag = false;
        }

    }

    public void rcvDataFromDevice(ACLMessage msg2) {

        myAgent.msgFIFO.add((String) msg2.getContent());
        this.PLCmsgIn = new Gson().fromJson(msg2.getContent(), HashMap.class);   //Data type conversion Json->Hashmap class
        if(PLCmsgIn.containsKey("Received")){   //Checks if it is a confirmation message
            if(PLCmsgIn.get("Received").equals(true)){

                System.out.println("<--PLC reception confirmation");
            }else{
                System.out.println("<--Problem receiving the message");
            }
//            LOGGER.debug("Received a confirmation message out of the timeout. Timeout might be increased.");
        }else{
            recvBatchInfo(msg2);   // sends item information to batch agent
            if(PLCmsgIn.containsKey("Control_Flag_Service_Completed")) {    //At least the first field is checked
                if (PLCmsgIn.get("Control_Flag_Service_Completed").equals(true)) {  //If service has been completed, the operation is deleted from machine plan variable

                    BathcID = String.valueOf(PLCmsgIn.get("Id_Batch_Reference"));
                    BathcID = BathcID.split("\\.")[0];

                    for (int i = 0; i <myAgent.machinePlan.size(); i++){    //searching the expected batch to be manufactured in machine plan arraylist
                        for (int j = 0; j < myAgent.machinePlan.get(i).size(); j++){
                            if (myAgent.machinePlan.get(i).get(j).get(0).equals("operation")){
                                if (NumOfItems != 0) {
                                    if (myAgent.machinePlan.get(i).get(j + 3).get(3).equals(BathcID)) { //The manufactured batch is compared with the expected batch ***With new XML is trying to compare item ID with batch ID, get(4) changed to get(3)
                                        myAgent.machinePlan.remove(i);
                                        i--;
                                        NumOfItems--;   //only the references to the items that were expected to be manufactured are deleted, that's why it is counted how many remains to be deleted
                                    }
                                }
                            }
                        }
                    }
                    if (myAgent.machinePlan.size() < 3){  //checking that there is no more operation to send
                        orderQueueFlag = false;
                    } else{
                        sendingFlag = true; //if there is any operation left, send behavior is called
                        SimpleBehaviour sendingBehaviour = new AssetManagementBehaviour(myAgent);
                        sendingBehaviour.action();
                    }
                }
            }
        }
    }

    // El metodo recvBatchInfo se encarga de enviar al agente batch la informacion con la trazabilidad de cada item fabricado
    public void recvBatchInfo(ACLMessage msg) {
        myAgent.msgFIFO.add((String) msg.getContent());
        ACLMessage reply = null;
        String targets = "";
        ArrayList<String> actionList = new ArrayList<String>(); // Lista de acciones que componen el servicio actual
        ArrayList<String> consumableList = new ArrayList<String>(); // Lista de consumibles que se utilizan para el servicio actual
        String  batchName = "";// Nombre del batch agent al que se le enviara el mensaje
        ArrayList<String> batchlist= new ArrayList<String>(); //nombres de los batch que deben recibir mensajes, replicas o no
        String neededMaterial = ""; // String que contendra  ID + cantidad de consumibles para hacer la peticion a los transportes
        Integer neededConsumable = 0; // variable que se utiliza para contar los consumibles necesarios (max - current)
        HashMap msgToBatch = new HashMap(); // Estructura de datos que se enviara al agente batch

        // Se crea el array list con las keys que se necesitaran para eliminar el .0 de los datos que se pasen de a tipo string
        ArrayList<String> replace = new ArrayList<String>( Arrays.asList("Id_Machine_Reference", "Id_Order_Reference", "Id_Batch_Reference", "Id_Ref_Subproduct_Type", "Id_Item_Number") );

        msgToBatch = new Gson().fromJson(msg.getContent(), HashMap.class);  //Data type conversion Json->Hashmap class

        if(msgToBatch.containsKey("Control_Flag_Item_Completed")) {
            if (msgToBatch.get("Control_Flag_Item_Completed").equals(true)) {   //checks if the item has been manufactured

                // Se extraen los datos necesarios del mensaje recibido
                // Cada mensaje contiene informacion del item fabricado
                String itemNumber = String.valueOf(msgToBatch.get("Id_Item_Number"));
                String batchNumber = String.valueOf(msgToBatch.get("Id_Batch_Reference"));
                String idItem = batchNumber + itemNumber; //Se compone el ID del item. Ejemplo -> batchNumber = 121 + itemNumber = 2 -> itemID = 1212
                msgToBatch.put("Id_Item_Number", itemNumber);

                // Al haber recibido el mensaje desde la maquina, se envia el mensaje de confirmacion
                HashMap confirmation = new HashMap();
                confirmation.put("Received", true);

                sendACLMessage(7, gatewayAgentID,"", "", new Gson().toJson(confirmation), myAgent); //Send confirmation message to PLC

                for (int i = 0; i < replace.size(); i++) {  //for loop to remove the .0 of the data that contains the keys defined in replace variable
                    String newValue = String.valueOf(msgToBatch.get(replace.get(i)));
                    newValue = newValue.split("\\.")[0];
                    msgToBatch.remove(replace.get(i));
                    msgToBatch.put(replace.get(i), newValue);
                }

                if (msgToBatch.get("Control_Flag_Service_Completed").equals(false)) {
                    msgToBatch.remove("Data_Service_Time_Stamp");    //remove unnecessary data from message
                }

                msgToBatch.remove("Control_Flag_Service_Completed");    //remove unnecessary data from message
                msgToBatch.remove("Control_Flag_Item_Completed");   //remove unnecessary data from message
                String ServiceType = String.valueOf(msgToBatch.get("Id_Ref_Service_Type"));
                ServiceType = ServiceType.split("\\.")[0];

                //Bucle for para identificar las acciones que se han completado conociendo Ref_Service_Type
                for (int j = 0; j < myAgent.resourceModel.size(); j++) {  // Knowing Ref_Service_Type, identification of the actions of each item
                    if (myAgent.resourceModel.get(j).get(0).get(0).equals("simple_operation")) {
                        if (myAgent.resourceModel.get(j).get(3).get(1).equals(ServiceType)) {
                            for (int k = j + 1; k < myAgent.resourceModel.size(); k++)  {
                                if (myAgent.resourceModel.get(k).get(0).get(0).equals("action")){
                                    actionList.add(myAgent.resourceModel.get(k).get(3).get(2)); // When actions are identified, they are added to a new variable
                                    consumableList.add(myAgent.resourceModel.get(k+1).get(3).get(1)); //The used consumable is saved to later discount it
                                } else if (myAgent.resourceModel.get(k).get(0).get(0).equals("simple_operation")) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            // Se restan los consumibles utilizados y se comparan con el valor de warning para pedir más material
            for (int i = 0; i < consumableList.size(); i++){
                for (int j = 0; j < myAgent.availableMaterial.size(); j++){
                    if (myAgent.availableMaterial.get(j).get("consumable_id").equals(consumableList.get(i))){
                        int currentConsumables = Integer.parseInt(myAgent.availableMaterial.get(j).get("current"));
                        currentConsumables--; //una vez identificado el nombre del consumible deseado, se descuenta
                        myAgent.availableMaterial.get(j).put("current", Integer.toString(currentConsumables));
                        int warningConsumable = Integer.parseInt(myAgent.availableMaterial.get(j).get("warning"));
                        if (currentConsumables <= warningConsumable && !matReqDone){
                            neededConsumable = Integer.parseInt(myAgent.availableMaterial.get(j).get("max")) - currentConsumables;
                            neededMaterial = neededMaterial.concat(myAgent.availableMaterial.get(j).get("consumable_id") + ":" + Integer.toString(neededConsumable) + ";");
                            requestMaterial = true;
                        }

                        // Se inicia el proceso de peticion siempre y cuando el flag requestMaterial este activado y se haya comprobado el estado de los cuatro tipos de consumibles
                        if (i == consumableList.size()-1 && requestMaterial) {
                            //Se lanza la negociacion para decidir cual sera el transporte que reponga el material
                            try {
                                ACLMessage reply2 = sendCommand(myAgent, "get * category=transport", "TransportAgentID");
                                if (reply2 != null) {   // If the id does not exist, it returns error
                                    targets = reply2.getContent();
                                }
                                String negotiationQuery = "localneg " + targets + " criterion=position action=" +
                                        "supplyConsumables externaldata=" + neededMaterial + "," + myAgent.getLocalName();
                                ACLMessage result = sendCommand(myAgent, negotiationQuery, "TransportAgentNeg");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            neededMaterial = ""; // Una vez hecha la peticion, se reinicializa la variable
                            matReqDone = true; // Flag que señala si la peticion de material se ha realizado
                            requestMaterial = false; // Una vez hecha la peticion se desactiva el flag
                        }
                    }
                }
            }
            System.out.println(myAgent.availableMaterial);

            msgToBatch.remove("Id_Ref_Service_Type");   // when all actions are identified, the Ref_Service_Type data is unnecessary
            msgToBatch.put("Id_Action_Type", actionList);   // Actions are added to the message
            String MessageContent = new Gson().toJson(msgToBatch);  //creates the message to be send
            System.out.println(MessageContent);

            try {
                BathcID = String.valueOf(PLCmsgIn.get("Id_Batch_Reference"));   //gets the batch reference from the received message
                BathcID = BathcID.split("\\.")[0];
                reply = sendCommand(myAgent, "get * reference=" + BathcID, "BatchAgentID");
                //returns the id of the element that matches with the reference of the required batch
                if (reply != null) {   // If the id does not exist, it returns error
                    myAgent.msgFIFO.add((String) reply.getContent());
                    batchName = reply.getContent();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                posponed_msgs_to_batch= myAgent.msg_buffer.get(batchName);
                if(posponed_msgs_to_batch==null){ //si no se encuentra el parent en el listado de mensajes postpuestos entonces el receptor ha confirmado la recepcion de todos los mensajes hasta ahora. Todoo OK.
                    ACLMessage running_replica = sendCommand(myAgent, "get * parent=" + batchName +" state=running", "BatchAgentID");
                    if (running_replica != null) {
                        String batchAgentName = running_replica.getContent();
                        AID batchAgentID = new AID(batchAgentName, false);
                        if(!running_replica.getContent().equals("")){   //encontrada replica en running para este batch
                            ACLMessage msg_to_batchagent=sendACLMessage(ACLMessage.INFORM, batchAgentID, "data", "PLCdata", MessageContent, myAgent);
                            myAgent.AddToExpectedMsgs(msg_to_batchagent);

                        }else{    //No encontrada replica en running para este batch. Puede que otro agente lo haya denunciado previamente o que el batch aun no se haya iniciado
                            posponed_msgs_to_batch = new ArrayList<ACLMessage>();
                            ACLMessage msg_to_buffer=new ACLMessage(ACLMessage.INFORM);
                            msg_to_buffer.setConversationId("PLCdata");
                            msg_to_buffer.setContent(MessageContent);
                            msg_to_buffer.setOntology("data");
                            posponed_msgs_to_batch.add(msg_to_buffer);
                            myAgent.msg_buffer.put(batchName,posponed_msgs_to_batch); //guardamos el mensaje hasta que el D&D me informe de que ya tenemos disponible otro receptor
                        }
                    }
                }else{  //habia algun mensaje pendiente de envíar a un receptor aun no definido
//                        posponed_msgs_to_batch=new ArrayList<ACLMessage>();
                    System.out.println("Added message to buffer:\nContent: "+MessageContent+"\nTo: "+batchName);
                    ACLMessage msg_to_buffer=new ACLMessage(ACLMessage.INFORM);
                    msg_to_buffer.setConversationId("PLCdata");
                    msg_to_buffer.setContent(MessageContent);
                    msg_to_buffer.setOntology("data");
                    posponed_msgs_to_batch.add(msg_to_buffer);
                    myAgent.msg_buffer.put(batchName,posponed_msgs_to_batch);

                    ACLMessage running_replica = sendCommand(myAgent, "get * parent=" + batchName +" state=running", "BatchAgentID");
                    if(!running_replica.getContent().equals("")){ //nos aseguramos de que el receptor aun no exista. Si existe ya, vaciamos el cajón con un automensaje.
                        sendACLMessage(7, myAgent.getAID(), "release_buffer","new_replica_detected",running_replica.getContent(),myAgent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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



