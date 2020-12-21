package es.ehu.domain.manufacturing.agents.functionality;

import com.google.gson.Gson;
import es.ehu.domain.manufacturing.agents.MachineAgent;
import es.ehu.domain.manufacturing.behaviour.SendTaskBehaviour;
import es.ehu.domain.manufacturing.utilities.Position;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.NegotiatingBehaviour;
import es.ehu.platform.template.interfaces.AssetManagement;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.NegFunctionality;
import es.ehu.platform.utilities.Cmd;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.tools.gui.ACLPerformativesRenderer;
import jade.wrapper.AgentController;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;


public class Machine_Functionality implements BasicFunctionality, NegFunctionality, AssetManagement {

    private static final long serialVersionUID = -4307559193624552630L;
    static final Logger LOGGER = LogManager.getLogger(Machine_Functionality.class.getName());

    private HashMap<String, String> operationsWithBatchAgents = new HashMap<>();
    private HashMap PLCmsgIn= new HashMap();
    private HashMap PLCmsgOut = new HashMap();
    String BathcID = "";

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
                for (int i = 0; i < allOperations.length - 1; i++) {

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
            }
        }

        sendOperationsInfoToBatches();

        return LOGGER.exit(null);
    }

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

        this.PLCmsgIn = new Gson().fromJson(msg.getContent(), HashMap.class);
        if(PLCmsgIn.containsKey("Received")){
            if(PLCmsgIn.get("Received").equals(true)){
                System.out.println("<--PLC reception confirmation");
            }else{
                System.out.println("<--Problem receiving the message");
            }
        }else{

            if(PLCmsgIn.containsKey("Flag_Service_Completed")) {
                if (PLCmsgIn.get("Flag_Service_Completed").equals(true)) {

                    sendMessage("Message Received", 7); //Send confirmation message to PLC

                    BathcID = (String) PLCmsgIn.get("Batch_Reference");

                    for (int i = 0; i <myAgent.machinePlan.size(); i++){
                        for (int j = 0; j < myAgent.machinePlan.get(i).size(); j++){
                            if (myAgent.machinePlan.get(i).get(j).get(0).equals("operation")){
                                if (myAgent.machinePlan.get(i).get(j+3).get(4).equals(BathcID)){
                                    myAgent.machinePlan.remove(i);
                                    i--;
                                }
                            }
                        }
                    }
                    if (myAgent.machinePlan.size() < 3){
                        myAgent.machinePlan.remove(1);
                        myAgent.machinePlan.remove(2);
                    } else{
                        SimpleBehaviour sendingBehaviour = new SendTaskBehaviour(myAgent);

                        sendingBehaviour.restart();
                    }
                }
            }
        }
    }

    public void sendDataToPLC() {

        ArrayList<String> auxiliar = new ArrayList<>();
        Boolean ItemContFlag = true;
        int NumOfItems = 0;
        if (myAgent.machinePlan != null) {
            for (int j = 0; j < myAgent.machinePlan.size(); j++) {
                for (int k = 0; k < myAgent.machinePlan.get(j).size(); k++) {
                    auxiliar = myAgent.machinePlan.get(j).get(k);
                    if (auxiliar.get(0).equals("station")) {
                        PLCmsgOut.put("Machine_Reference", myAgent.machinePlan.get(j).get(k + 3).get(0));
                    }
                    if (auxiliar.get(0).equals("operation")) {
                        ArrayList<String> auxiliar2 = myAgent.machinePlan.get(j).get(k + 3);
                        if (ItemContFlag == true) {
                            BathcID = auxiliar2.get(4);
                            PLCmsgOut.put("Batch_Reference", BathcID);
                            PLCmsgOut.put("Order_Reference", auxiliar2.get(6));
                            PLCmsgOut.put("Ref_Subproduct_Type", auxiliar2.get(7));
                            PLCmsgOut.put("Ref_Service_Type", auxiliar2.get(3));
                            PLCmsgOut.put("Flag_New_Service", true);
                            ItemContFlag = false;
                        }
                        if (ItemContFlag == false && auxiliar2.get(4).equals(BathcID)) {
                            NumOfItems++;
                        }
                    }
                }
            }
            PLCmsgOut.put("No_of_Items",NumOfItems);
            String MessageContent = new Gson().toJson(PLCmsgOut);
            sendMessage(MessageContent,16);
        } else {
            System.out.println("No operations defined");
            PLCmsgOut.put("Flag_New_Service", false);
        }

    }

    private void sendMessage(String data, int performative) {
        ACLMessage msgToPLC = new ACLMessage(performative);
        AID gwAgent = new AID("ControlGatewayCont", false);
        msgToPLC.addReceiver(gwAgent);
        msgToPLC.setOntology("negotiation");
        msgToPLC.setConversationId("PLCdata");
        msgToPLC.setContent(data);
        myAgent.send(msgToPLC);
    }

}
