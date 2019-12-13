package es.ehu.flexmansys.functionality;

import es.ehu.MsgNegotiation;
import es.ehu.NegFunctionality;
import es.ehu.behaviour.ControlBehaviour;
import es.ehu.flexmansys.agents.Machine_Agent;
import es.ehu.flexmansys.utilities.Position;
import es.ehu.flexmansys.utilities.MsgOperation;
import es.ehu.flexmansys.utilities.MsgReqOperation;
import es.ehu.flexmansys.utilities.MsgTransport;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.tuple.Pair;

import static es.ehu.flexmansys.utilities.FmsNegotiation.*;
import static es.ehu.utilities.MasReconOntologies.*;

/**
 * Machine Agent Functionality
 * 
 * @author Brais Fortes (@fortes23) - Euskal Herriko Unibersitatea
 * @author Mikel López (@lopeziglesiasmikel) - Euskal Herriko Unibersitatea
 */

public class Machine_Functionality implements NegFunctionality {

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
	private Machine_Agent myAgent;

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

	/** Identifier of new task template. */
	private MessageTemplate debugPalletArrivedTemplate;

	// Constructor
	public Machine_Functionality(Machine_Agent agent) {
		LOGGER.entry("*** Constructing Machine_Functionality ***");
		this.myAgent = agent;
		operationTimeout = Long.MAX_VALUE;
		Element dompalletIn = (Element) myAgent.resourceModel.getElementsByTagName("PalletIn").item(0);
		Element dompalletOut = (Element) myAgent.resourceModel.getElementsByTagName("PalletOut").item(0);
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
	 * @return Message for registering the agent in the MWM (or SM)
	 */
	public String init() {
		LOGGER.entry(myAgent.resourceName, myAgent.resourceModel);
		curState = new machineState();
		prevState = new machineState();
		runningTask = false;
		String regParams = "machine " + "name=" + (myAgent.resourceName) + " " + REG_CATEGORY + "=" + CAT_MACHINE + " "
				+ REG_ID + "=" + myAgent.resourceName + " " + REG_SERVICES + "=" + myAgent.machineServices;
		return LOGGER.exit(regParams);
	}

	/**
	 * Checks if the state changed
	 * 
	 * @return State if there were changes or null otherwise.
	 */
	public Object getState() {
		LOGGER.entry();
		if (curState.equals(prevState)) {
			LOGGER.debug("State has not changed");
			return null;
		} else {
			LOGGER.debug("State has changed");
			prevState.setAll(curState);
		}
		return LOGGER.exit(curState);
	}

	/**
	 * Updates the state of the agent.
	 */
	public void setState(Object state) {
		LOGGER.entry();
		try {
			machineState aux = (machineState) state;
			this.curState.setAll(aux);
			LOGGER.debug("State was updated");
		} catch (Exception e) {
			LOGGER.debug("State received was not modifiable");
		}
		LOGGER.exit();
	}

	public Object execute(Object input) {
		LOGGER.entry(input);
		ACLMessage msg = null;

		if (input != null) {
			try {
				msg = (ACLMessage) input;
				LOGGER.debug(msg);
			} catch (Exception e) {
				LOGGER.debug("Execution input was not an ACLMessage");
			}
		}

		ACLMessage auxMsg = timeManagement();
		if (auxMsg != null) {
			if (msg != null) {
				myAgent.putBack(msg);
			}
			return LOGGER.exit(auxMsg);
		}

		auxMsg = planManagement();
		if (auxMsg != null) {
			if (msg != null) {
				myAgent.putBack(msg);
			}
			return LOGGER.exit(auxMsg);
		}

		if (msg == null) {
			return LOGGER.exit(null);
		}

		// Check message templates
		if (newTaskTemplate.match(msg)) {

		} else if (debugPalletArrivedTemplate.match(msg)) {
			palletIn = Pair.of(palletIn.getKey(), true);
		}
		return LOGGER.exit(null);
	}

	/**
	 * Secure ending
	 * 
	 * @return Message or null
	 */
	public Object endFunctionality() {
		LOGGER.entry();

		return LOGGER.exit(null);
	}

	/**
	 * TODO Reconfiguration
	 *
	 */
	public long calculateNegotiation(String action, String criterion, Object... negExternalData) {
		return LOGGER.exit(null);
	}

	/**
	 * TODO Reconfiguration
	 */
	public boolean checkNegotiation(String negTaskId, String negAction, String negCriterion, long negScalarValue,
			AID requester, Object... negExternalData) {
		return LOGGER.exit(null);
	}

	private ACLMessage timeManagement() {
		if (runningTask) {
			if (operationTimeout < System.currentTimeMillis()) {
				palletOut = Pair.of(palletOut.getKey(), true);
				removeTimer();
			}
		} else {
			if (operationTimeout < System.currentTimeMillis()) {
				MsgReqOperation requestMsgContent = new MsgReqOperation(curState.getOperation(),
						curState.getSubproductID(), palletIn.getKey());
				runningTask = true;
				removeTimer();
				return LOGGER.exit(createMsg(ONT_DATA, ACLMessage.REQUEST, requestMsgContent, curState.getBatch()));
			}
		}
		return LOGGER.exit(null);
	}

	private void removeTimer() {
		myAgent.period = -1;
		operationTimeout = Long.MAX_VALUE;
	}

	private void addTimer(long t) {
		myAgent.period = (int) t;
		operationTimeout = System.currentTimeMillis() + t;
	}

	/**
	 * Check the state of the plan in each cycle and take decisions.
	 * 
	 * @return msg
	 */
	private ACLMessage planManagement() {
		ACLMessage response = null;
		if (runningTask) {
			if (palletIn.getValue()) {
				// Start opertion timer
				addTimer(curState.getOperationTimer());
				// Inform operation start
				MsgOperation informStartContent = new MsgOperation(MsgOperation.MACHINE, true, curState.getOperation(),
						curState.getTaskID(), curState.getOperationTimer());
				response = (createMsg(ONT_DATA, ACLMessage.INFORM, informStartContent, curState.getBatch()));
				palletIn = Pair.of(palletIn.getKey(), false);
				return LOGGER.exit(response);
			} else if (palletOut.getValue()) {
				// Inform operation finished
				MsgOperation informFinishContent = new MsgOperation(MsgOperation.MACHINE, false,
						curState.getOperation(), curState.getTaskID(), palletOut.getKey());
				response = (createMsg(ONT_DATA, curState.getTaskID(), ACLMessage.INFORM, informFinishContent,
						curState.getBatch()));
				runningTask = false;
				palletOut = Pair.of(palletOut.getKey(), false);
				return LOGGER.exit(response);
			}
		} else if (myAgent.machinePlan.getElementsByTagName("Operation").getLength() > 0) {
			// Take next op and save as curState and deletes it from the plan dom
			NodeList remainingOp = myAgent.machinePlan.getElementsByTagName("Operation");
			Element nextTask = (Element) remainingOp.item(0);
			try {
				Date nextStartTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.parse(nextTask.getAttribute("startTime"));
				Date nextFinishTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.parse(nextTask.getAttribute("finishTime"));
				curState.setAll(nextTask.getAttribute("id"), new AID(nextTask.getAttribute("batchid"), AID.ISLOCALNAME),
						nextTask.getAttribute("subproductid"), nextStartTime, nextFinishTime, generateTaskID());
				addTimer(curState.getStartTime().getTime() - System.currentTimeMillis());
				nextTask.getParentNode().removeChild(nextTask);
			} catch (Exception e) {
				LOGGER.info("Dates could not be parsed");
			}
		} else {
			// TODO a standby
		}
		return LOGGER.exit(response);
	}

	/**
	 * Generate ACLMessage
	 * 
	 * @param ontology
	 * @param performative
	 * @param conversationID
	 * @param content
	 * @param targets
	 * @return
	 */
	private ACLMessage createMsg(String ontology, String conversationID, int performative, Object content,
			AID... targets) {
		LOGGER.entry(ontology, performative, conversationID, content, targets);
		ACLMessage msg = createMsg(ontology, performative, content, targets);
		msg.setConversationId(conversationID);
		return LOGGER.exit(msg);
	}

	/**
	 * Create general ACLMessage
	 * 
	 * @param ontology
	 * @param performative
	 * @param content
	 * @param targets
	 * @return ACLMessage
	 */
	private ACLMessage createMsg(String ontology, int performative, Object content, AID... targets) {
		LOGGER.entry(ontology, performative, content, targets);
		ACLMessage msg = new ACLMessage(performative);
		msg.setOntology(ontology);
		msg.setPostTimeStamp();
		for (AID target : targets) {
			msg.addReceiver(target);
		}
		try {
			if (content instanceof String) {
				msg.setContent((String) content);
			} else {
				msg.setContentObject((Serializable) content);
			}
		} catch (Exception e) {
			LOGGER.info("Content object cannot be added");
			return LOGGER.exit(null);
		}
		return LOGGER.exit(msg);
	}

	/**
	 * Calculates an UID for any operation.
	 * 
	 * @return Universal TaskID
	 */
	private String generateTaskID() {
		LOGGER.entry();
		countTaskID++;
		return LOGGER.exit(myAgent.getLocalName() + countTaskID);
	}

}
