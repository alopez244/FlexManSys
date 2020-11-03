package es.ehu.domain.manufacturing.agents.functionality;

import FIPA.DateTime;
import es.ehu.domain.manufacturing.agents.MachineAgent;
import es.ehu.domain.manufacturing.utilities.Position;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.behaviour.NegotiatingBehaviour;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.NegFunctionality;
import es.ehu.platform.utilities.Cmd;
import es.ehu.platform.utilities.XMLReader;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.security.Timestamp;
import java.util.*;

import static es.ehu.platform.utilities.MasReconOntologies.ONT_NEGOTIATE;
import static es.ehu.domain.manufacturing.utilities.FmsNegotiation.ONT_DEBUG;

public class Machine_Functionality implements BasicFunctionality, NegFunctionality {

    private static final long serialVersionUID = -4307559193624552630L;
    static final Logger LOGGER = LogManager.getLogger(Machine_Functionality.class.getName());

    private HashMap<String, String> operationsWithBatchAgents = new HashMap<>();

    /** Class to represent the state of a machine. */
    private class machineState implements Serializable {

        private static final long serialVersionUID = -3535413924926110816L;

        private String taskID;

        private String operation;

        private AID batch;

        private String subproductID;

        private Date startTime;

        private Date finishTime;

        private long operationTimer;

        // Constructor
        public machineState() {
            this.operation = null;
            this.batch = null;
            this.subproductID = null;
            this.startTime = null;
            this.finishTime = null;
            this.operationTimer = Long.MAX_VALUE;
            this.taskID = null;
        }

        // Getter methods
        public String getOperation() {
            return this.operation;
        }

        public AID getBatch() {
            return this.batch;
        }

        public String getSubproductID() {
            return this.subproductID;
        }

        public Date getStartTime() {
            return this.startTime;
        }

        public Date getFinishTime() {
            return this.finishTime;
        }

        public long getOperationTimer() {
            return this.operationTimer;
        }

        public String getTaskID() {
            return this.taskID;
        }

        // Setter methods
        public void setAll(machineState a) {
            this.operation = a.getOperation();
            this.batch = a.getBatch();
            this.subproductID = a.getSubproductID();
            this.startTime = a.getStartTime();
            this.finishTime = a.getFinishTime();
            this.taskID = a.getTaskID();
            this.operationTimer = finishTime.getTime() - startTime.getTime();
        }

        public void setAll(String op, AID batch, String subproductID, Date startTime, Date finishTime, String taskID) {
            this.operation = op;
            this.batch = batch;
            this.subproductID = subproductID;
            this.startTime = startTime;
            this.finishTime = finishTime;
            this.taskID = taskID;
            this.operationTimer = finishTime.getTime() - startTime.getTime();
        }

        @Override
        public boolean equals(Object o) {

            if (o == this) {
                return true;
            }
            if (!(o instanceof machineState)) {
                return false;
            }
            machineState c = (machineState) o;
            return (operation.equals(c.getOperation()) && subproductID.equals(c.getSubproductID())
                    && taskID.equals(c.getTaskID()) && Objects.equals(batch, c.getBatch())
                    && Objects.equals(startTime, c.getStartTime()) && Objects.equals(finishTime, c.getFinishTime())
                    && (Long.compare(operationTimer, c.getOperationTimer()) == 0));
        }

        @Override
        public int hashCode() {
            return Objects.hash(operation, batch, subproductID, startTime, finishTime, taskID, operationTimer);
        }

    }

    /** Current state of the machine agent (lastOperation). */
    private machineState curState;

    /** Last state before update of the machine agent (lastOperation). */
    private machineState prevState;

    /** Identifier of the agent. */
    private MachineAgent myAgent;

    /** Class name to switch on the agent */
    private String className;

    /** Position and state (empty or not) of the machine palletin station. */
    private Pair<Position, Boolean> palletIn;

    /** Position and state (empty or not) of the machine palletout station. */
    private Pair<Position, Boolean> palletOut;

    /** Identifier representing if the machine is doing a task. */
    private boolean runningTask;

    /** Identifier representing if current operationTimer. */
    private long operationTimeout;

    /** TaskID counter. This value is increased, using {@code generateTaskID} */
    private int countTaskID = 0;

    /** Identifier of new task template. */
    private MessageTemplate newTaskTemplate;

    /** Identifier of debug pallet arrived template. */
    private MessageTemplate debugPalletArrivedTemplate;

    // Constructor
    public Machine_Functionality(MachineAgent agent) {
//        LOGGER.entry("*** Constructing Machine_Functionality ***");
//        this.myAgent = agent;
//        operationTimeout = Long.MAX_VALUE;
//        Element dompalletIn = (Element) myAgent.resourceModel.getElementsByTagName("palletIn").item(0);
//        Element dompalletOut = (Element) myAgent.resourceModel.getElementsByTagName("palletOut").item(0);
//        palletIn = Pair.of(new Position(Float.valueOf(dompalletIn.getAttribute("xPos")),
//                Float.valueOf(dompalletIn.getAttribute("yPos"))), false);
//        palletOut = Pair.of(new Position(Float.valueOf(dompalletOut.getAttribute("xPos")),
//                Float.valueOf(dompalletOut.getAttribute("yPos"))), false);
//
//        newTaskTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_NEGOTIATE),
//                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
//        debugPalletArrivedTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DEBUG),
//                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
//        LOGGER.exit();
    }

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
        // TODO mirarlo, de momento peta porque el input es null
        /*
        LOGGER.entry(input);
        ACLMessage msg = null;

        for (int k =0; k<input.length; k=k+1){

            if (input != null) {
                try {
                    msg = (ACLMessage) input[k];
                    LOGGER.debug(msg);
                } catch (Exception e) {
                    LOGGER.debug("Execution input was not an ACLMessage");
                }
            }

            if (msg == null) {
                return LOGGER.exit(null);
            }

        }
         */

        System.out.println("El agente recurso " + myAgent.getLocalName() + " ya esta en el metodo execute");

        if (!operationsWithBatchAgents.isEmpty())
            System.out.println("I have the operation: " + operationsWithBatchAgents.entrySet().iterator().next().getKey()
                    + " and the batchAgent: " + operationsWithBatchAgents.entrySet().iterator().next().getValue());

        PLCInformation info = new PLCInformation(true, "batch1", 1, 3, new DateTime(), new DateTime(), false);

        String batchAgentID = null;  // Tiene que tener el ID del agente batch
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(batchAgentID, AID.ISLOCALNAME));
        msg.setConversationId("info a mano");
        msg.setContent(info.toString());
        myAgent.send(msg);

        return LOGGER.exit(null);
    }

    private class PLCInformation  {

        private boolean flagItemCompleted;
        private String batchReference;
        private int refSubproductType;
        private int itemNumber;
        private DateTime initialTimeStamp;
        private DateTime finalTimeStamp;
        private boolean flagServiceCompleted;

        public PLCInformation(boolean flagItemCompleted, String batchReference, int refSubproductType, int itemNumber, DateTime initialTimeStamp, DateTime finalTimeStamp, boolean flagServiceCompleted) {
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

        public DateTime getInitialTimeStamp() {
            return initialTimeStamp;
        }

        public DateTime getFinalTimeStamp() {
            return finalTimeStamp;
        }

        public boolean isFlagServiceCompleted() {
            return flagServiceCompleted;
        }

        @Override
        public String toString() {
            return "flagItemCompleted=" + flagItemCompleted +
                    ", batchReference='" + batchReference +
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
        }

        return NegotiatingBehaviour.NEG_WON;
    }
}
