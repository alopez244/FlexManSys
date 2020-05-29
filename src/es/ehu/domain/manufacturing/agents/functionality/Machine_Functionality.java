package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.domain.manufacturing.agents.MachineAgent;
import es.ehu.domain.manufacturing.utilities.Position;
import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.NegFunctionality;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import static es.ehu.platform.utilities.MasReconOntologies.ONT_NEGOTIATE;
import static es.ehu.domain.manufacturing.utilities.FmsNegotiation.ONT_DEBUG;

public class Machine_Functionality implements BasicFunctionality, NegFunctionality {

    private static final long serialVersionUID = -4307559193624552630L;
    static final Logger LOGGER = LogManager.getLogger(Machine_Functionality.class.getName());

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
        LOGGER.entry("*** Constructing Machine_Functionality ***");
        this.myAgent = agent;
        operationTimeout = Long.MAX_VALUE;
        Element dompalletIn = (Element) myAgent.resourceModel.getElementsByTagName("palletIn").item(0);
        Element dompalletOut = (Element) myAgent.resourceModel.getElementsByTagName("palletOut").item(0);
        palletIn = Pair.of(new Position(Float.valueOf(dompalletIn.getAttribute("xPos")),
                Float.valueOf(dompalletIn.getAttribute("yPos"))), false);
        palletOut = Pair.of(new Position(Float.valueOf(dompalletOut.getAttribute("xPos")),
                Float.valueOf(dompalletOut.getAttribute("yPos"))), false);

        newTaskTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_NEGOTIATE),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        debugPalletArrivedTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DEBUG),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        LOGGER.exit();
    }

    /**
     * Initialize the functional properties
     *
     * @return The name used for registering the agent in the MWM (or SM)
     */
    @Override
    public String init(MWAgent mwAgent) {

        //First of all, the connection with the asset must be checked


        //Later, if the previous condition is accomplished, the agent is registered
        //this.mwAgent = myAgent;
        LOGGER.entry();

        String attribs = "";
        String [] args = (String[]) mwAgent.getArguments();

        for (int i=0; i<args.length; i++){
            if (!args[i].toString().toLowerCase().startsWith("id=")) attribs += " "+args[i];
            if (args[i].toString().toLowerCase().startsWith("id=")) return "";
        }

        String cmd = "reg machine parent=system"+attribs;

        ACLMessage reply = null;
        try {
            reply = mwAgent.sendCommand(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String respuesta = reply.getContent();

        LOGGER.info(mwAgent.getLocalName()+" ("+cmd+")"+" > mwm < "+respuesta);
        return LOGGER.exit(respuesta);
    }

    @Override
    public Object execute(Object[] input) {
        return null;
    }

    @Override
    public long calculateNegotiationValue(String negAction, String negCriterion, Object... negExternalData) {
        return 0;
    }

    @Override
    public int checkNegotiation(String negId, String winnerAction, double negReceivedValue, long negScalarValue, boolean tieBreaker, boolean checkReplies, Object... negExternalData) {
        return 0;
    }
}
