package es.ehu.domain.manufacturing.agents.functionality;

import com.google.gson.Gson;
import es.ehu.SystemModelAgent;
import es.ehu.domain.manufacturing.agents.MachineAgent;
import es.ehu.domain.manufacturing.behaviour.SendTaskBehaviour;
import es.ehu.domain.manufacturing.utilities.Position;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.NegotiatingBehaviour;
import es.ehu.platform.template.interfaces.AssetManagement;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.NegFunctionality;
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


public class Machine_Functionality implements BasicFunctionality, NegFunctionality, AssetManagement {

    private static final long serialVersionUID = -4307559193624552630L;
    static final Logger LOGGER = LogManager.getLogger(Machine_Functionality.class.getName());

    private ArrayList<ArrayList<ArrayList<String>>> productInfo;
    private HashMap<String, String> operationsWithBatchAgents = new HashMap<>();
    private HashMap PLCmsgIn = new HashMap();
    private HashMap PLCmsgOut = new HashMap();
    private String BathcID = "";
    int NumOfItems = 0;
    private Boolean sendingFlag = false;
    private Boolean orderQueueFlag = false;
    private String gatewayAgentName;

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
        gatewayAgentName = "ControlGatewayCont" + machineName.substring(3,4); //Se genera el nombre del Gateway Agent con el que se tendra que comunicar

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

        // Conseguimos toda la informacion del producto utilizando su ID
        //productInfo = getProductInfo(productID);
        //System.out.println("ID del producto asociado al agente " + myAgent.getLocalName() + ": " + productInfo.get(0).get(3).get(1) + " - " + productID);

        sendOperationsInfoToBatches();

        return LOGGER.exit(null);
    }

    @Override
    public Void terminate(MWAgent myAgent) { return null;}

    private void sendOperationsInfoToBatches() {
        if (!operationsWithBatchAgents.isEmpty()) {

            //String operation = (String) input[0];
            //String agentID = (String) input[1];
            Map.Entry<String, String> data = operationsWithBatchAgents.entrySet().iterator().next();
            String operation = data.getKey();
            String agentID = data.getValue();

            System.out.println("I have the operation: " + operation
                    + " and the batchAgent: " + agentID);

            PLCInformation info = new PLCInformation(true, "batch1", 1, 3, LocalDateTime.now(), LocalDateTime.of(2020, Month.NOVEMBER, 04, 11, 30), false);

            String plcInfoString = info.toString();
            System.out.println("Informacion del PLC--> " + plcInfoString);

            System.out.println("El agente recurso " + myAgent.getLocalName() + " le va a enviar la informacion de la operacion "
                    + operation + " al agente " + agentID);

            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID(agentID, AID.ISLOCALNAME));
            if (!operation.equals("C_01")) {
                //msg.setConversationId("PLC information about operations");
                msg.setConversationId(operation + " " + Math.random());
                msg.setContent(operation + " operation INFO--> " + plcInfoString);
            } else {
                msg.setConversationId("Hola");
                msg.setContent("Ahora va o no");
            }
            myAgent.send(msg);

            operationsWithBatchAgents.remove(operation);
        }
    }

    private class PLCInformation  {

        private boolean flagItemCompleted;
        private String batchReference;
        private int refSubproductType;
        private int itemNumber;
        private LocalDateTime initialTimeStamp;
        private LocalDateTime finalTimeStamp;
        private boolean flagServiceCompleted;

        public PLCInformation(boolean flagItemCompleted, String batchReference, int refSubproductType, int itemNumber, LocalDateTime initialTimeStamp, LocalDateTime finalTimeStamp, boolean flagServiceCompleted) {
            this.flagItemCompleted = flagItemCompleted;
            this.batchReference = batchReference;
            this.refSubproductType = refSubproductType;
            this.itemNumber = itemNumber;
            this.initialTimeStamp = initialTimeStamp;
            this.finalTimeStamp = finalTimeStamp;
            this.flagServiceCompleted = flagServiceCompleted;
        }

        public boolean isFlagItemCompleted() {
            return flagItemCompleted;
        }

        public String getBatchReference() {
            return batchReference;
        }

        public int getRefSubproductType() {
            return refSubproductType;
        }

        public int getItemNumber() {
            return itemNumber;
        }

        public LocalDateTime getInitialTimeStamp() {
            return initialTimeStamp;
        }

        public LocalDateTime getFinalTimeStamp() {
            return finalTimeStamp;
        }

        public boolean isFlagServiceCompleted() {
            return flagServiceCompleted;
        }

        @Override
        public String toString() {
            return "flagItemCompleted=" + flagItemCompleted +
                    ", batchReference=" + batchReference +
                    ", refSubproductType=" + refSubproductType +
                    ", itemNumber=" + itemNumber +
                    ", initialTimeStamp=" + initialTimeStamp +
                    ", finalTimeStamp=" + finalTimeStamp +
                    ", flagServiceCompleted=" + flagServiceCompleted;
        }
    }

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

    public void rcvDataFromPLC(ACLMessage msg) {

        this.PLCmsgIn = new Gson().fromJson(msg.getContent(), HashMap.class);   //Data type conversion Json->Hashmap class
        if(PLCmsgIn.containsKey("Received")){   //Checks if it is a confirmation message
            if(PLCmsgIn.get("Received").equals(true)){
                System.out.println("<--PLC reception confirmation");
            }else{
                System.out.println("<--Problem receiving the message");
            }
        }else{
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
//                        myAgent.machinePlan.remove(1);
//                        myAgent.machinePlan.remove(2);
                    } else{
                        sendingFlag = true; //if there is any operation left, send behavior is called
                        SimpleBehaviour sendingBehaviour = new SendTaskBehaviour(myAgent);
                        sendingBehaviour.action();
                    }
                }
            }
        }
    }

    @Override
    public void recvBatchInfo(ACLMessage msg) {

        ACLMessage reply = null;
        ArrayList<String> auxiliar = new ArrayList<>();
        ArrayList<String> actionList = new ArrayList<String>();
        String  batchAgentName = "";
        HashMap msgToBatch = new HashMap();
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

                for (int j = 0; j < myAgent.resourceModel.size(); j++) {  // Knowing Ref_Service_Type, identification of the actions of each item
                    for (int k = 0; k < myAgent.resourceModel.get(j).size(); k++) {
                        auxiliar = myAgent.resourceModel.get(j).get(k);
                        if (auxiliar.get(0).equals("simple_operation")) {
                            if (myAgent.resourceModel.get(j).get(k+3).get(1).equals(ServiceType)) {
                                for (int l = j + 1; l < myAgent.resourceModel.size(); l++)  {
                                    if (myAgent.resourceModel.get(l).get(0).get(0).equals("action")){
                                        actionList.add(myAgent.resourceModel.get(l).get(3).get(2)); // When actions are identified, they are added to a new variable
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
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
    }

    public void sendDataToPLC() {

        if(sendingFlag == true) {     //It is checked if the method is correctly activated and that orders do not overlap
            ArrayList<String> auxiliar = new ArrayList<>();
            List<String> itemNumbers = new ArrayList<String>(); //to track each of the items that are added to the operation
            Boolean ItemContFlag = true;
            Boolean newItem = false;
            if (myAgent.machinePlan.size() > 2) {   //checks that there are operations in the machine plan
                for (int j = 0; j < myAgent.machinePlan.size(); j++) {  //Looks for the operation to be manufactured in the machine plan
                    for (int k = 0; k < myAgent.machinePlan.get(j).size(); k++) {
                        auxiliar = myAgent.machinePlan.get(j).get(k);
                        if (auxiliar.get(0).equals("station")) {
                            PLCmsgOut.put("Id_Machine_Reference", Integer.parseInt(myAgent.machinePlan.get(j).get(k + 3).get(0)));
                        }
                        if (auxiliar.get(0).equals("operation")) {
                            ArrayList<String> auxiliar2 = myAgent.machinePlan.get(j).get(k + 3);

                            if (ItemContFlag == true) {         //saves the information of the operation only when founds the first item, then just increments the item counter
                                BathcID = auxiliar2.get(4);     //saves the information of the operation in PLCmsgOut
                                PLCmsgOut.put("Control_Flag_New_Service", true);
                                PLCmsgOut.put("Id_Batch_Reference", Integer.parseInt(BathcID));
                                PLCmsgOut.put("Id_Order_Reference", Integer.parseInt(auxiliar2.get(6)));
                                PLCmsgOut.put("Id_Ref_Subproduct_Type", Integer.parseInt(auxiliar2.get(7)));
                                PLCmsgOut.put("Operation_Ref_Service_Type", Integer.parseInt(auxiliar2.get(0)));
                                ItemContFlag = false;
                            }

                            if (!itemNumbers.contains(auxiliar2.get(5)) && auxiliar2.get(4).equals(BathcID)){   //if item number already exists, it is not added
                                itemNumbers.add(auxiliar2.get(5));  //adds new item numbers to array
                                newItem = true;     //the item is counted
                            }

                            if (ItemContFlag == false && auxiliar2.get(4).equals(BathcID) && newItem == true) { //counts all the items with the same batch number
                                NumOfItems++;
                                newItem = false;
                            }

                        }
                    }
                }
                PLCmsgOut.put("Operation_No_of_Items", NumOfItems); //when all the items of the same batch have been counted, the request to the PLC is send
                String MessageContent = new Gson().toJson(PLCmsgOut);
                sendMessage(MessageContent, 16, gatewayAgentName);
                sendingFlag = false;
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

    public ACLMessage sendCommand(Agent agent, String cmd, String conversationId) throws Exception {


        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType("sa");
        dfd.addServices(sd);
        String mwm;

        while (true) {
            DFAgentDescription[] result = DFService.search(myAgent,dfd);

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
