package es.ehu.domain.manufacturing.agents.functionality;

import com.google.gson.Gson;
import es.ehu.SystemModelAgent;
import es.ehu.domain.manufacturing.agents.MachineAgent;
import es.ehu.domain.manufacturing.behaviour.ReceiveTaskBehaviour;
import es.ehu.domain.manufacturing.behaviour.SendTaskBehaviour;
import es.ehu.domain.manufacturing.utilities.Position;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.NegotiatingBehaviour;
import es.ehu.platform.template.interfaces.AssetManagement;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.NegFunctionality;
import es.ehu.platform.template.interfaces.Traceability;
import es.ehu.platform.utilities.Cmd;
import es.ehu.platform.utilities.XMLReader;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.tools.gui.ACLPerformativesRenderer;
import jade.wrapper.AgentController;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import std_msgs.Bool;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

public class Machine_Functionality extends DomApp_Functionality implements BasicFunctionality, NegFunctionality, AssetManagement, Traceability {

    private static final long serialVersionUID = -4307559193624552630L;
    static final Logger LOGGER = LogManager.getLogger(Machine_Functionality.class.getName());

    private ArrayList<ArrayList<String>> productInfo;
    private HashMap<String, String> operationsWithBatchAgents = new HashMap<>();
    private HashMap PLCmsgIn = new HashMap();
    private HashMap PLCmsgOut = new HashMap();
    private String BathcID = "";
    private Integer NumOfItems = 0;
    private Integer machinePlanIndex = 0;
    private Boolean sendingFlag = false;
    private Boolean matReqDone = false;
    private Boolean requestMaterial = false;
    private Boolean orderQueueFlag = false;
    private String gatewayAgentName;
    private MessageTemplate template;

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

        this.template = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("data")),MessageTemplate.MatchConversationId("ProvidedConsumables"));

        //First of all, the connection with the asset must be checked

        //Later, if the previous condition is accomplished, the agent is registered
        this.myAgent = (MachineAgent) mwAgent;
        LOGGER.entry();

        String machineName = myAgent.resourceName;
        Integer machineNumber = Integer.parseInt(machineName.split("_")[1]);
        gatewayAgentName = "ControlGatewayCont" + machineNumber.toString(); //Se genera el nombre del Gateway Agent con el que se tendra que comunicar
        //First, the Machine Model is read

        String [] args = (String[]) myAgent.getArguments();

        for (int i=0; i<args.length; i++){
            if (args[i].toLowerCase().startsWith("id=")) return null;
        }

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


        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("El agente recurso " + myAgent.getLocalName() + " ya esta en el metodo execute");

        if (input[0] != null) {
            ACLMessage msg = (ACLMessage) input[0];
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
    public int checkNegotiation(String conversationId, String sAction, double negReceivedValue, long negScalarValue, boolean tieBreak, boolean checkReplies, Object... negExternalData) {

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

    public void rcvDataFromDevice(ACLMessage msg) {

        this.PLCmsgIn = new Gson().fromJson(msg.getContent(), HashMap.class);   //Data type conversion Json->Hashmap class
        if(PLCmsgIn.containsKey("Received")){   //Checks if it is a confirmation message
            if(PLCmsgIn.get("Received").equals(true)){
                System.out.println("<--PLC reception confirmation");
            }else{
                System.out.println("<--Problem receiving the message");
            }
        }else{
            recvBatchInfo(msg);   // sends item information to batch agent
            if(PLCmsgIn.containsKey("Control_Flag_Service_Completed")) {    //At least the first field is checked
                if (PLCmsgIn.get("Control_Flag_Service_Completed").equals(true)) {  //If service has been completed, the operation is deleted from machine plan variable

                    HashMap confirmation = new HashMap();
                    confirmation.put("Received", true);
                    sendMessage(new Gson().toJson(confirmation), 7, gatewayAgentName); //Send confirmation message to PLC

                    BathcID = String.valueOf(PLCmsgIn.get("Id_Batch_Reference"));
                    BathcID = BathcID.split("\\.")[0];

                    for (int i = 0; i <myAgent.machinePlan.size(); i++){    //searching the expected batch to be manufactured in machine plan arraylist
                        for (int j = 0; j < myAgent.machinePlan.get(i).size(); j++){
                            if (myAgent.machinePlan.get(i).get(j).get(0).equals("operation")){
                                if (NumOfItems != 0) {
                                    if (myAgent.machinePlan.get(i).get(j + 3).get(4).equals(BathcID)) { //The manufactured batch is compared with the expected batch
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
                        SimpleBehaviour sendingBehaviour = new ReceiveTaskBehaviour(myAgent);
                        sendingBehaviour.action();
                    }
                }
            }
        }
    }

    @Override
    public void recvBatchInfo(ACLMessage msg) {

        ACLMessage reply = null;
        String targets = "";
        ArrayList<String> actionList = new ArrayList<String>();
        ArrayList<String> consumableList = new ArrayList<String>();
        String  batchAgentName = "";
        String neededMaterial = "";
        Integer neededConsumable = 0;
        HashMap msgToBatch = new HashMap();

        // Se crea el array list con las keys que se necesitaran para eliminar el .0 de los datos que se pasen de a tipo string
        ArrayList<String> replace = new ArrayList<String>( Arrays.asList("Id_Machine_Reference", "Id_Order_Reference", "Id_Batch_Reference", "Id_Ref_Subproduct_Type", "Id_Item_Number") );

        msgToBatch = new Gson().fromJson(msg.getContent(), HashMap.class);  //Data type conversion Json->Hashmap class

        if(msgToBatch.containsKey("Control_Flag_Item_Completed")) {
            if (msgToBatch.get("Control_Flag_Item_Completed").equals(true)) {   //checks if the item has been manufactured

                for (int i = 0; i < replace.size(); i++) {  //for loop to remove the .0 of the data that contains the keys defined in replace variable
                    String newValue = String.valueOf(msgToBatch.get(replace.get(i)));
                    newValue = newValue.split("\\.")[0];
                    msgToBatch.remove(replace.get(i));
                    msgToBatch.put(replace.get(i), newValue);
                }

                if (msgToBatch.get("Control_Flag_Service_Completed").equals(false)) {   //If the batch has not yet been completed, this method is in charge of sending the confirmation message

                    msgToBatch.remove("Data_Service_Time_Stamp");    //remove unnecessary data from message
                    HashMap confirmation = new HashMap();
                    confirmation.put("Received", true);
                    sendMessage(new Gson().toJson(confirmation), 7, gatewayAgentName);  //Sends confirmation message to PLC
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

                                neededMaterial = "";
                                matReqDone = true; // Flag que señala si la peticion de material se ha realizado
                                requestMaterial = false;
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
                    if (reply != null)   // If the id does not exist, it returns error
                        batchAgentName = reply.getContent();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    reply = sendCommand(myAgent, "get * parent=" + batchAgentName, "BatchAgentID");
                    //returns the names of all the agents that are sons
                    if (reply != null)   // Si no existe el id en el registro devuelve error
                        batchAgentName = reply.getContent(); //gets the name of the batch agent to which the message should be sent
                } catch (Exception e) {
                    e.printStackTrace();
                }

                sendMessage(MessageContent, 16, batchAgentName); //Sends item information to the batch agent

            }
        }

    public void sendDataToDevice() {

        String targets = "";
        // Se reciven los mensajes ACL que corresponden al material que se ha repuesto
        ACLMessage msg = myAgent.receive(template);
        if (msg != null) {
            ArrayList<ArrayList<String>> newConsumables = new ArrayList<>();
            newConsumables.add(new ArrayList<>()); newConsumables.add(new ArrayList<>());
            String content = msg.getContent();
            String [] contentSplited = content.split(";");
            for (int i = 0; i < contentSplited.length ; i++) {
                newConsumables.get(0).add(contentSplited[i].split(":")[0]);
                newConsumables.get(1).add(contentSplited[i].split(":")[1]);
            }
            for (int i = 0; i < newConsumables.get(0).size(); i++){
                for (int j = 0; j < myAgent.availableMaterial.size(); j++){
                    if (newConsumables.get(0).get(i).equals(myAgent.availableMaterial.get(j).get("consumable_id"))) {
                        Integer currentConsumable = Integer.parseInt(myAgent.availableMaterial.get(j).get("current"));
                        Integer addedconsumable = Integer.parseInt(newConsumables.get(1).get(i));
                        myAgent.availableMaterial.get(j).put("current", Integer.toString(currentConsumable + addedconsumable));
                    }
                }
            }
            matReqDone = false;
        }

        if(sendingFlag == true) {     //It is checked if the method is correctly activated and that orders do not overlap
            ArrayList<String> consumableList = new ArrayList<String>();
            Boolean consumableShortage = false;
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
                            consumableShortage = true;
                        }
                    }
                }
                // Solo se envia la operación si hay material suficiente
                if (!consumableShortage) {
                    PLCmsgOut.remove("Index");
                    String MessageContent = new Gson().toJson(PLCmsgOut);
                    sendMessage(MessageContent, 16, gatewayAgentName);
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
                        if (!matReqDone) {
                            String neededMaterial = "";
                            Integer neededConsumable = 0;
                            for (int j = 0; j < myAgent.availableMaterial.size(); j++){
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
                System.out.println("No operations defined");
                PLCmsgOut.put("Control_Flag_New_Service", false);
                sendingFlag = false;
            }
        }
    }

    private void sendMessage(String data, int performative, String agentName) {       //ACLMessage template for message sending to gateway agent
        ACLMessage msgToPLC = new ACLMessage(performative);
        AID gwAgent = new AID(agentName, false);
        msgToPLC.addReceiver(gwAgent);
        msgToPLC.setOntology("negotiation");
        msgToPLC.setConversationId("PLCdata");
        msgToPLC.setContent(data);
        myAgent.send(msgToPLC);
    }

}
