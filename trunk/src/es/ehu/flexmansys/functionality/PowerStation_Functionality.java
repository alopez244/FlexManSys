package es.ehu.flexmansys.functionality;

import es.ehu.NegFunctionality;
import es.ehu.flexmansys.agents.PowerStation_Agent;
import es.ehu.flexmansys.utilities.Position;
import es.ehu.flexmansys.utilities.Timeout;
import es.ehu.flexmansys.utilities.MsgOperation;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import static es.ehu.utilities.MasReconOntologies.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.*;
import org.apache.commons.lang3.tuple.Pair;

import static es.ehu.flexmansys.utilities.FmsNegotiation.*;

/**
 * Power Station Agent Functionality
 * 
 * @author Brais Fortes (@fortes23) - Euskal Herriko Unibersitatea
 * @author Mikel López (@lopeziglesiasmikel) - Euskal Herriko Unibersitatea
 */

public class PowerStation_Functionality implements NegFunctionality {

	private static final long serialVersionUID = -4594533066118045700L;
	static final Logger LOGGER = LogManager.getLogger(PowerStation_Functionality.class.getName());

	/** Batch state. */
	private static class pwsState implements Serializable {
		private static final long serialVersionUID = 7118791771780393851L;

		private ConcurrentHashMap<String, Timeout> timeoutTask;

		private ConcurrentHashMap<Position, Boolean> pwsPositions;

		public pwsState() {
			this.timeoutTask = new ConcurrentHashMap<String, Timeout>();
			this.pwsPositions = new ConcurrentHashMap<Position, Boolean>();
		}

		public pwsState(ConcurrentHashMap<Position, Boolean> a) {
			this.timeoutTask = new ConcurrentHashMap<String, Timeout>();
			this.setPwsPosition(a);
		}

		// Getter methods
		public ConcurrentHashMap<String, Timeout> getTimeoutTask() {
			return this.timeoutTask;
		}

		public ConcurrentHashMap<Position, Boolean> getPwsPosition() {
			return this.pwsPositions;
		}

		// Setter methods
		public void setTimeoutTask(ConcurrentHashMap<String, Timeout> a) {
			this.timeoutTask = new ConcurrentHashMap<String, Timeout>(a);
		}

		public void setPwsPosition(ConcurrentHashMap<Position, Boolean> a) {
			this.pwsPositions = new ConcurrentHashMap<Position, Boolean>(a);
		}

		public void putTimeoutTask(String key, Timeout value) {
			this.timeoutTask.put(key, value);
		}

		public void putPwsPosition(Position key, Boolean value) {
			this.pwsPositions.put(key, value);
		}

		public void removeTask(String key) {
			this.timeoutTask.remove(key);
		}

		public void removePwsPosition(Position key) {
			this.pwsPositions.remove(key);
		}

		public void setAll(pwsState a) {
			this.setTimeoutTask(a.getTimeoutTask());
			this.setPwsPosition(a.getPwsPosition());
		}
		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof pwsState)) {
				return false;
			}
			pwsState c = (pwsState) o;
			return (timeoutTask.equals(c.getTimeoutTask())) && (pwsPositions.equals(c.getPwsPosition()));
		}

		@Override
		public int hashCode() {
			return Objects.hash(timeoutTask, pwsPositions);
		}
	}

	/** Identifier of the current state of the powerStation. */
	private pwsState curState;

	/** Identifier of the previous state of the powerStation. */
	private pwsState prevState;

	/** Identifier of the agent. */
	private PowerStation_Agent myAgent;

	/** Identifier of the response to won negotiation */
	private MessageTemplate infNegTemplate;

	/** Identifier of the message when a robot arrives to the power station */
	private MessageTemplate infDataTemplate;

	/** Identifier of the messages that substitute the use of sensors */
	private MessageTemplate debugFreePosTemplate;

	private Pair<String, Long> smallTimeout;

	private long lastExecMillis;

	public PowerStation_Functionality(PowerStation_Agent agent) {
		LOGGER.entry("*** Constructing PowerStation_Functionality ***");
		this.myAgent = agent;
		ConcurrentHashMap<Position,Boolean> auxPos = new ConcurrentHashMap<Position,Boolean>();
		NodeList auxPWSPos = myAgent.Positions.getElementById(myAgent.resourceName).getChildNodes();
		for (int i = 0; i < auxPWSPos.getLength() - 1; i++){
			if (auxPWSPos.item(i).getNodeType() == Node.ELEMENT_NODE){
				Element newElem = (Element) auxPWSPos.item(i);
				Position newPos = new Position(Float.valueOf(newElem.getAttribute("posX")), Float.valueOf(newElem.getAttribute("posY")));
				auxPos.put(newPos, false);
			}
		}
		curState = new pwsState(auxPos);
		prevState = new pwsState(auxPos);

		infNegTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_NEGOTIATE),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		infDataTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DATA),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		debugFreePosTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DEBUG),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		LOGGER.exit();
	}

	/**
	 * Initialize the functional properties
	 * 
	 * @return Message for registering the agent in the MWM (or SM)
	 */
	public String init() {
		LOGGER.entry();
		smallTimeout = Pair.of(null, Long.MAX_VALUE);
		String regParams = "powerstation " + "name=" + (myAgent.resourceName) + " ID=" + (myAgent.resourceName) + " " + REG_SERVICES + "=" + ACT_CHARGE;
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
		LOGGER.entry(state);
		pwsState auxState;
		try {
			auxState = (pwsState) state;
			curState.setAll(auxState);
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
			} catch (Exception e) {
				LOGGER.debug("Execution input was not an ACLMessage");
			}
		}

		ACLMessage timeMsg = timeManagement();

		if (msg == null) {
			return LOGGER.exit(null);
		}

		if (infNegTemplate.match(msg)) {
			try {
				Long estimatedTime = (Long) msg.getContentObject();
				addTimeout(msg.getConversationId(), new Timeout(msg.getSender(), estimatedTime));
				LOGGER.debug("Added TRANSPORT to timers");
			} catch (Exception e) {
				LOGGER.info("Received INFORM Negotiation message not valid");
			}
		} else if (infDataTemplate.match(msg)) {
			if (curState.getTimeoutTask().get(msg.getConversationId()) != null) {
				try {
					MsgOperation recMsg = (MsgOperation) msg.getContentObject();
					if ((recMsg.getResourceType() == MsgOperation.TRANSPORT) && !recMsg.getStart()) {
						LOGGER.debug("Removing timeout due to arrival");
						removeTimeout(msg.getConversationId());
					}
				} catch (Exception e) {
					LOGGER.info("Received INFORM " + ONT_DATA + " message not valid");
				}
			}
		} else if (debugFreePosTemplate.match(msg)) {
			try {
				Position freePos = (Position) msg.getContentObject();
				if (curState.getPwsPosition().get(freePos)) {
					curState.getPwsPosition().put(freePos, false);
					LOGGER.debug("Removing occupied position");
				}
			} catch (Exception e) {
				LOGGER.debug("Received INFORM " + ONT_DEBUG + " message not valid");
			}
		}
		return LOGGER.exit(timeMsg);
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
	 * Calculate negotiation value
	 * 
	 * @return negotiation value
	 */
	public long calculateNegotiation(String action, String criterion, Object... negExternalData) {
		LOGGER.entry(action, criterion, negExternalData);
		if (action.equals(ACT_CHARGE)) {
			if ((negExternalData != null) && (negExternalData.length > 0)) {
				try {
					Position robotPos = (Position) negExternalData[0];
					long minVal = Long.MAX_VALUE;
					for (Position key : curState.getPwsPosition().keySet()) {
						if (!curState.getPwsPosition().get(key)) {
							long aux = calculateDistance(robotPos, key);
							if (aux < minVal) {
								minVal = aux;
							}
						}
					}
					return LOGGER.exit(minVal);
				} catch (Exception e) {
					LOGGER.info("Negotiation external data was not as expected");
				}
			}
		}
		return LOGGER.exit(Long.MAX_VALUE);
	}

	/**
	 * Checks if negotiation was correct
	 * 
	 * @return Object
	 */
	public Object checkNegotiation(String negTaskId, String negAction, String negCriterion, long negScalarValue,
			AID requester, Object... externalData) {
		LOGGER.entry(negTaskId, negAction, negCriterion, negScalarValue, requester, externalData);
		long value = calculateNegotiation(negAction, negCriterion, externalData);
		if (value <= negScalarValue) {
			if ((externalData != null) && (externalData.length > 0)) {
				try {
					Position robotPos = (Position) externalData[0];
					long minVal = Long.MAX_VALUE;
					Position bestPos = null;
					for (Position key : curState.getPwsPosition().keySet()) {
						if (!curState.getPwsPosition().get(key)) {
							long aux = calculateDistance(robotPos, key);
							if (aux < minVal) {
								minVal = aux;
								bestPos = key;
							}
						}
					}
					return LOGGER.exit(bestPos);
				} catch (Exception e) {
					LOGGER.info("Negotiation external data was not as expected");
				}
			}
		}
		return LOGGER.exit(null);
	}

	public long calculateDistance(Position a, Position b) {
		LOGGER.entry(a, b);
		double x = (double) a.getxPos() - b.getxPos();
		double y = (double) a.getyPos() - b.getyPos();
		return LOGGER.exit((long) Math.sqrt(x * x + y * y));
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
	 * Removes the taskID in the {@code curState.timeoutTask} and update
	 * {@code smallTimeout} if it was necessary.
	 * 
	 * @param taskID identifier of the task.
	 */
	private void removeTimeout(String taskID) {
		LOGGER.entry(taskID);
		if (taskID.equals(smallTimeout.getKey())) {
			String minKey = null;
			long minValue = Long.MAX_VALUE;
			for (String key : curState.getTimeoutTask().keySet()) {
				long value = curState.getTimeoutTask().get(key).getTimeout();
				if ((value < minValue) && key != smallTimeout.getKey()) {
					minValue = value;
					minKey = key;
				}
			}
			if (minKey != null) {
				smallTimeout = Pair.of(minKey, curState.getTimeoutTask().get(minKey).getTimeout());
				myAgent.period = smallTimeout.getValue().intValue();
				LOGGER.debug("New timeout: " + myAgent.period);
			} else {
				smallTimeout = Pair.of(null, Long.MAX_VALUE);
				myAgent.period = -1;
				LOGGER.debug("Empty map of timeouts");
			}
		}
		curState.removeTask(taskID);
		LOGGER.exit();
	}

	/**
	 * Store the new timeout in {@code curState.timeoutTask} and update the
	 * {@code smallTimeout} if was necessary.
	 * 
	 * @param taskID identifier of the task.
	 * @param timer  resource which offers the task and the estimated time.
	 */
	private void addTimeout(String taskID, Timeout timer) {
		LOGGER.entry(taskID, timer.getTimeout());
		long inputTime = timer.getTimeout();
		if (timer == null || taskID == null) {
			return;
		}
		if (smallTimeout.getKey() == null) {
			LOGGER.debug("First timeout: " + timer.getTimeout());
			smallTimeout = Pair.of(taskID, timer.getTimeout());
			myAgent.period = (int) timer.getTimeout();
		} else if (inputTime < smallTimeout.getValue()) {
			Timeout aux = curState.getTimeoutTask().get(smallTimeout.getKey());
			aux.setTimeout(smallTimeout.getValue() - inputTime);
			curState.putTimeoutTask(smallTimeout.getKey(), aux);
			smallTimeout = Pair.of(taskID, timer.getTimeout());
			myAgent.period = (int) timer.getTimeout();
			LOGGER.debug("New timeout: " + myAgent.period);
		} else {
			inputTime -= smallTimeout.getValue();
			timer.setTimeout(inputTime);
			LOGGER.debug("Relative time stored: " + inputTime);
		}
		curState.putTimeoutTask(taskID, timer);
		LOGGER.exit();
	}

	/**
	 * Manage the timers, checking if the smaller has finished. If it is finished,
	 * the DDRA is informed and the following timer is activated.
	 * 
	 * @return ACLMessage with the data needed by the DDRA.
	 */
	private ACLMessage timeManagement() {
		LOGGER.entry();
		ACLMessage DDRAMsg = null;
		if (smallTimeout.getKey() == null) {
			myAgent.period = -1;
			lastExecMillis = System.currentTimeMillis();
			return LOGGER.exit(DDRAMsg);
		}

		long timer = System.currentTimeMillis() - lastExecMillis;
		long remainingTime = smallTimeout.getValue() - timer;

		if (remainingTime > 0) {
			LOGGER.debug("Remaining time: " + remainingTime);
			smallTimeout = Pair.of(smallTimeout.getKey(), remainingTime);
		} else {
			LOGGER.debug("Finished time: " + remainingTime);
			String[] targets = null;
			while (targets == null) {
				targets = myAgent.getInfoMWM("*", "service=" + ACT_DDRA).split(",");
			}
			AID[] targetAID = new AID[targets.length];
			for (int i = 0; i < targets.length; i++) {
				if (targets[i].length() > 0) {
					targetAID[i] = new AID(targets[i], AID.ISLOCALNAME);
				}
			}
			DDRAMsg = createMsg(ONT_CONTROL, ACLMessage.REQUEST,
					curState.getTimeoutTask().get(smallTimeout.getKey()).getResourceAID(), targetAID);
			removeTimeout(smallTimeout.getKey());
		}

		lastExecMillis = System.currentTimeMillis();
		return LOGGER.exit(DDRAMsg);
	}

}
