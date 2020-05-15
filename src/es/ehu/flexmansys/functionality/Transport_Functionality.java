package es.ehu.flexmansys.functionality;

import es.ehu.flexmansys.agents.Transport_Agent;
import es.ehu.flexmansys.utilities.MsgOperation;
import es.ehu.flexmansys.utilities.MsgTransport;
import es.ehu.flexmansys.utilities.Position;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.template.interfaces.NegFunctionality;
import es.ehu.platform.utilities.MsgNegotiation;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ros.jade.RosMsg;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static es.ehu.flexmansys.utilities.FmsNegotiation.*;
import static es.ehu.platform.utilities.MasReconOntologies.*;

/**
 * Transport Agent Functionality
 * 
 * @author Brais Fortes (@fortes23) - Euskal Herriko Unibersitatea
 * @author Mikel López (@lopeziglesiasmikel) - Euskal Herriko Unibersitatea
 */

public class Transport_Functionality implements NegFunctionality {

	private static final long serialVersionUID = -8778994273760536207L;
	static final Logger LOGGER = LogManager.getLogger(Transport_Functionality.class.getName());

	/** Information stored by the plan of a transport agent. */
	private class planTask {

		/** Action to be done by the robot (Ex: DELIVERY, REPLENISHMENT, etc.). */
		String action;

		/** Data needed to execute the {@code action}. */
		Object content;

		/** Estimated time to execute the {@code action}. */
		long estimatedTime;

		/** Agent requester of this service. */
		AID requester;

		// Constructor
		public planTask(String ont, Object content, long estTime, AID requester) {
			this.action = ont;
			this.content = content;
			this.estimatedTime = estTime;
			this.requester = requester;
		}

		// Getter methods
		public String getAction() {
			return this.action;
		}

		public Object getContent() {
			return this.content;
		}

		public long getEstimatedTime() {
			return this.estimatedTime;
		}

		public AID getRequester() {
			return this.requester;
		}
	}

	/** Events/Reports names. */
	private static final String TRANS_STATE = "state";
	private static final String TRANS_BATTERY = "battery";
	private static final String TRANS_STARTORDER = "start_order";
	private static final String TRANS_FINISHORDER = "finish_order";
	private static final String TRANS_ALARMBATTERY = "alarm_battery";

	/** Current state of the transport robot (position). */
	private Position curState;

	/** Last state before update of the transport robot (position). */
	private Position prevState;

	/** Identifier of the agent. */
	private Transport_Agent myAgent;

	/** Ordered plan of the robot. */
	private LinkedHashMap<String, planTask> transportPlan;

	/**
	 * Identifier representing if the robot is in a power station.
	 * <ul>
	 * <li>True: It is in power station or this task is planned.</li>
	 * <li>False: It is not in power station or planned</li>
	 * </ul>
	 */
	private boolean powerStation;

	/** Identifier representing if the robot is doing a task. */
	private boolean runningTask;

	/** Table of values with the negotiation values and times of the transport. */
	private ConcurrentHashMap<Position, ConcurrentHashMap<Position, Long>> tablePointsLayout;

	/**
	 * Pair of values usable by the negotiation. It is composed by two fields:
	 * <ul>
	 * <li>Long: estimated Time of tasks already included in the plan.</li>
	 * <li>Position: Last engaged position of the robot</li>
	 * </ul>
	 */
	private Pair<Long, Position> negInfo;

	/**
	 * TaskID of the negotiation request made by the robot. It is only one value
	 * because it will only request for a power Station one time.
	 * <p>
	 * Its usefulness is to know if a response message that comes from a power
	 * station is of interest or not.
	 */
	private String taskInfo;

	/** TaskID counter. This value is increased, using {@code generateTaskID} */
	private int countTaskID = 0;

	/** Identifier of the power station accepted template. */
	private MessageTemplate powerStationAccTemplate;

	/** Identifier of the power station refuse template. */
	private MessageTemplate powerStationRefTemplate;

	/** Debug template for killing machines/transports */
	private MessageTemplate killMachineTemplate;

	// Constructor
	public Transport_Functionality(Transport_Agent agent) {
		LOGGER.entry("*** Constructing Transport_Functionality ***");
		this.myAgent = agent;

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		tablePointsLayout = new ConcurrentHashMap<Position, ConcurrentHashMap<Position, Long>>();

		Element PositionList = (Element) myAgent.transportPositionList.getElementsByTagName("positionList").item(0);
		NodeList positionA = PositionList.getChildNodes();
		for (int i = 0; i < positionA.getLength() - 1; i++) {
			if (positionA.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element auxElem = (Element) positionA.item(i);
				Position auxPosA = new Position(Float.valueOf(auxElem.getAttribute("posX")),
						Float.valueOf(auxElem.getAttribute("posY")));
				NodeList positionB = auxElem.getChildNodes();
				ConcurrentHashMap<Position, Long> auxMap = new ConcurrentHashMap<Position, Long>();
				for (int j = 0; j < positionB.getLength() - 1; j++) {
					if (positionB.item(j).getNodeType() == Node.ELEMENT_NODE) {
						Element auxBelem = (Element) positionB.item(j);
						Position auxBPos = new Position(Float.valueOf(auxBelem.getAttribute("posX")),
								Float.valueOf(auxBelem.getAttribute("posY")));
						auxMap.put(auxBPos, Long.parseLong(auxBelem.getAttribute("time")));
					}
				}
				tablePointsLayout.put(auxPosA, new ConcurrentHashMap<Position, Long>(auxMap));
			}
		}

		powerStationAccTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_NEGOTIATE),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		powerStationRefTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_NEGOTIATE),
				MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
		killMachineTemplate = MessageTemplate.MatchOntology(ONT_KILL);
		LOGGER.exit();
	}

	/**
	 * Initialize the functional properties
	 * 
	 * @return Message for registering the agent in the MWM (or SM)
	 */
	public String init() {
		LOGGER.entry(myAgent.resourceName, myAgent.resourceModel);
		curState = new Position(0, 0);
		prevState = new Position(0, 0);
		taskInfo = null;
		negInfo = Pair.of(new Long(0), new Position(curState.getxPos(), curState.getyPos()));
		transportPlan = new LinkedHashMap<String, planTask>();
		String regParams = CAT_TRANSPORT + " " + REG_CATEGORY + "=" + CAT_TRANSPORT + " " + REG_NAME + "="
				+ (myAgent.resourceName) + " " + REG_ID + "=" + myAgent.getLocalName() + " " + REG_SERVICES + "="
				+ myAgent.transportServices;
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
			prevState.setPos(curState);
		}
		return LOGGER.exit(curState);
	}

	/**
	 * Updates the state of the agent.
	 */
	public void setState(Object state) {
		LOGGER.entry();
		try {
			Position aux = (Position) state;
			this.curState.setPos(aux);
			LOGGER.debug("State was updated");
		} catch (Exception e) {
			LOGGER.debug("State received was not modifiable");
		}
		LOGGER.exit();
	}

	public Object execute(Object input) {
		LOGGER.entry(input);
		myAgent.period = -1;
		ACLMessage msg = null;

		if (input != null) {
			try {
				msg = (ACLMessage) input;
				LOGGER.debug(msg);
			} catch (Exception e) {
				LOGGER.debug("Execution input was not an ACLMessage");
			}
		}

		ACLMessage eventMsg = eventManagement();
		if (eventMsg != null) {
			if (msg != null) {
				myAgent.putBack(msg);
			}
			return LOGGER.exit(eventMsg);
		}

		ACLMessage planMsg = planManagement();
		if (planMsg != null) {
			if (msg != null) {
				myAgent.putBack(msg);
			}
			return LOGGER.exit(planMsg);
		}

		if (msg == null) {
			return LOGGER.exit(null);
		}

		// Check message templates
		if (powerStationAccTemplate.match(msg)) {
			LOGGER.debug(msg);
			// Adds move to power station to the plan
			if (taskInfo.equals(msg.getConversationId())) {
				try {
					Position psPos = (Position) msg.getContentObject();
					long estTime = calculateTime(negInfo.getValue(), psPos);
					planTask newPSTask = new planTask(ACT_MOVE, (Object) psPos, estTime, msg.getSender());
					addTaskPlan(msg.getConversationId(), newPSTask, psPos);

					return LOGGER.exit(createMsg(msg.getOntology(), msg.getConversationId(), ACLMessage.INFORM,
							calculateTime(negInfo.getValue(), psPos), msg.getSender()));
				} catch (Exception e) {
					LOGGER.info("Not expected power station content");
				}
			}
		} else if (powerStationRefTemplate.match(msg)) {
			return LOGGER.exit(createNegMsg(ACT_CHARGE, negInfo.getValue()));
		} else if (killMachineTemplate.match(msg)) {
			return LOGGER.exit(createMsg(ONT_CONTROL, ACLMessage.REQUEST,
					ControlBehaviour.CMD_SETSTATE + " " + ControlBehaviour.ST_STOP, myAgent.getAID()));
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
	 * Calculates the negotiation action with the static table of positions in the
	 * layout and the remaining time of other negotiations. Currently, it will use
	 * only static values.
	 * 
	 * @return Negotiation value
	 */
	public long calculateNegotiation(String action, String criterion, Object... negExternalData) {
		long value = Long.MAX_VALUE;

		try {
			MsgTransport aux = (MsgTransport) negExternalData[0];
			Position initPos = (Position) aux.getInitPos();
			Position finalPos = (Position) aux.getFinalPos();
			value = negInfo.getKey() + calculateTime(negInfo.getValue(), initPos) + calculateTime(initPos, finalPos);
			LOGGER.debug("Negotiation value: " + value);
		} catch (Exception e) {
			LOGGER.info("Not possible to calculate negotiation value");
		}

		return LOGGER.exit(value);
	}

	/**
	 * Checking if the negotiation value was correct and, therefore, the agent has
	 * won. In case of winning, the task is stored in the plan.
	 * 
	 * @return content of the message to send to the requester or null, if the
	 *         negotiation is refused.
	 */
	public Object checkNegotiation(String negTaskId, String negAction, String negCriterion, long negScalarValue,
                                   AID requester, Object... negExternalData) {

		LOGGER.entry(negTaskId, negAction, negCriterion, negScalarValue, requester, negExternalData);
		MsgOperation winMsg = null;
		long curNegValue = calculateNegotiation(negAction, negCriterion, negExternalData);
		if (curNegValue <= negScalarValue) {
			LOGGER.debug("Transport " + myAgent.getLocalName() + " is the winner");
			if ((negExternalData != null) && (negExternalData.length > 0)) {
				try {
					MsgTransport negExtData = (MsgTransport) negExternalData[0];
					planTask newTask = new planTask(negAction, negExtData, curNegValue, requester);
					addTaskPlan(negTaskId, newTask, negExtData.getFinalPos());
					// Creates inform win negotitation msg content
					winMsg = new MsgOperation(MsgOperation.TRANSPORT, true, negExtData.getPalletID(), negTaskId,
							curNegValue);
					restart();
				} catch (Exception e) {
					LOGGER.info("Not expected check negotiation external data");
				}
			}
		}
		return LOGGER.exit(winMsg);
	}

	/**
	 * Check if there are new events in the cognitive layer of the robot. In this
	 * case, it is managed.
	 * 
	 * @return Msg to send.
	 */
	private ACLMessage eventManagement() {
		ACLMessage response = null;
		ConcurrentHashMap<String, RosMsg> events = new ConcurrentHashMap<String, RosMsg>(
				myAgent.robotTransport.getEvents());
		myAgent.robotTransport.removeAllEvents();
		if (events == null) {
			return LOGGER.exit(null);
		}
		for (String key : events.keySet()) {
			LOGGER.debug("Analyzing event " + events.get(key).getOntology());
			switch (events.get(key).getOntology()) {
			case TRANS_STATE:
				try {
					float xPos = Float.parseFloat(events.get(key).getContent()[0]);
					float yPos = Float.parseFloat(events.get(key).getContent()[1]);
					curState.setPos(xPos, yPos);
				} catch (Exception e) {
					LOGGER.info("Not expected " + TRANS_STATE + "content");
				}
				break;
			case TRANS_STARTORDER:
				break;
			case TRANS_FINISHORDER:
				// Force restart after sending message
				restart();
				planTask finishedTask = transportPlan.get(events.get(key).getConversationID());
				if (finishedTask == null) {
					continue;
				}
				try {
					MsgOperation msgOpContent = null;
					if (finishedTask.getContent() instanceof Position) {
						Position finishTaskContent = (Position) finishedTask.getContent();
						msgOpContent = new MsgOperation(MsgOperation.TRANSPORT, false,
								null, key, finishTaskContent);
					} else {
						MsgTransport finishTaskContent = (MsgTransport) finishedTask.getContent();
						msgOpContent = new MsgOperation(MsgOperation.TRANSPORT, false,
								finishTaskContent.getPalletID(), key, finishTaskContent.getFinalPos());
					}
					response = createMsg(ONT_DATA, events.get(key).getConversationID(), ACLMessage.INFORM, msgOpContent,
							finishedTask.getRequester());
					removeTaskPlan(events.get(key).getConversationID());
				} catch (Exception e) {
					LOGGER.info("Not expected finishorder content. MsgTransport expected");
				}
				break;
			case TRANS_ALARMBATTERY:
			case TRANS_BATTERY:
				break;
			default:
				LOGGER.info("Event " + events.get(key).getConversationID() + " is not being read");
			}
		}
		return LOGGER.exit(response);

	}

	/**
	 * Check the state of the plan in each cycle and take decisions.
	 * 
	 * @return msg
	 */
	private ACLMessage planManagement() {
		if (runningTask) {
			return LOGGER.exit(null);
		}
		if (!transportPlan.isEmpty()) {
			Map.Entry<String, planTask> aux = transportPlan.entrySet().iterator().next();
			String cnvID = aux.getKey();
			planTask task = aux.getValue();
			RosMsg nextTask = null;
			LOGGER.info("New task: " + task.getAction());
			switch (task.getAction()) {
			case ACT_DELIVERY:
			case ACT_REPLENISHMENT:
				try {
					MsgTransport content = (MsgTransport) task.getContent();
					nextTask = new RosMsg(cnvID, task.getAction(), content.getInitPos().toString(),
							content.getFinalPos().toString());
				} catch (Exception e) {
					LOGGER.info("Content of the task was not as expected");
				}
				break;
			case ACT_RESCUE:
			case ACT_EXPLORATION:
				break;
			case ACT_MOVE:
				try {
					Position content = (Position) task.getContent();
					nextTask = new RosMsg(cnvID, task.getAction(), Float.toString(content.getxPos()),
							Float.toString(content.getyPos()));
				} catch (Exception e) {
					LOGGER.info("Content of the task was not as expected");
				}
				break;
			default:
				LOGGER.info("Action is not being done");
				removeTaskPlan(cnvID);
				break;
			}
			executeNewTask(cnvID, nextTask);
		} else if (!powerStation) {
			powerStation = true;
			return LOGGER.exit(createNegMsg(ACT_CHARGE, negInfo.getValue()));
		}
		return LOGGER.exit(null);
	}

	/**
	 * Manage a finished task in the plan. Then, the plan and all the variables that
	 * control it, are updated.
	 * 
	 * @param id taskID to remove from the plan.
	 */
	private void removeTaskPlan(String id) {
		LOGGER.entry(id);
		if (transportPlan.get(id) != null) {
			long newTime = negInfo.getKey() - transportPlan.get(id).getEstimatedTime();
			negInfo = Pair.of(newTime, negInfo.getValue());
			runningTask = false;
			transportPlan.remove(id);
			LOGGER.debug("Removed correctly " + id + " from the plan");
		}
		LOGGER.exit();
	}

	/**
	 * Add task to the plan and update {@code negInfo} variable.
	 */
	private void addTaskPlan(String id, planTask task, Position lastPos) {
		LOGGER.entry(id, task, lastPos);
		transportPlan.put(id, task);
		negInfo = Pair.of(task.getEstimatedTime(), lastPos);
		LOGGER.exit();
	}

	/**
	 * Send an order to the robotic elements.
	 * 
	 * @param id       taskId of the action.
	 * @param nextTask msg for the robotic elements (cognitive layer).
	 */
	private void executeNewTask(String id, RosMsg nextTask) {
		if ((nextTask != null) && (transportPlan.get(id) != null)) {
			myAgent.robotTransport.send(nextTask);
			runningTask = true;
			String requester = null;
			while (requester == null) {
				requester = myAgent.getInfoMWM(transportPlan.get(id).getRequester().getLocalName(),
						REG_SERVICES + "=" + ACT_CHARGE);
			}
			if (requester.equals(transportPlan.get(id).getRequester().getLocalName())) {
				powerStation = true;
			} else {
				powerStation = false;
			}
		}
	}

	/**
	 * Calculate time, using the {@code tablePointsLayout}.
	 * 
	 * @param initPos  first position to search in the table.
	 * @param finalPos second position to search in the table.
	 * @return result.
	 */
	private long calculateTime(Position initPos, Position finalPos) {
		long value = Long.MAX_VALUE;
		try {
			value = tablePointsLayout.get(initPos).get(finalPos);
		} catch (Exception e) {
			LOGGER.info("Not possible to calculate time");
		}
		return LOGGER.exit(value);
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
	 * Creates a message to request a {@code negAction} to any
	 * 
	 * @param negAction    Service requested.
	 * @param externalData Values for negotiating.
	 * 
	 * @return Request message for negotiating
	 */
	private ACLMessage createNegMsg(String negAction, Object... externalData) {
		LOGGER.entry(negAction);
		String[] targets = null;

		// If there are no response, repeat request.
		while (targets == null) {
			targets = myAgent.getInfoMWM("*", REG_SERVICES + "=" + negAction).split(",");
		}

		AID[] targetAID = new AID[targets.length];
		for (int i = 0; i < targets.length; i++) {
			if (targets[i].length() > 0) {
				targetAID[i] = new AID(targets[i], AID.ISLOCALNAME);
			}
		}

		String taskID = generateTaskID();
		MsgNegotiation content = new MsgNegotiation(targetAID, taskID, negAction, CRIT_MINTIME, externalData);

		return LOGGER.exit(createMsg(ONT_NEGOTIATE, taskID, ACLMessage.REQUEST, content, targetAID));
	}

	/**
	 * Calculates an UID for any operation.
	 * 
	 * @return Universal TaskID
	 */
	private String generateTaskID() {
		LOGGER.entry();
		countTaskID++;
		String value = myAgent.getLocalName() + countTaskID;
		taskInfo = value;
		return LOGGER.exit(value);
	}

	/**
	 * Introduce a period in the agent to use a {@code block(t)} and waking up again
	 */
	private void restart() {
		myAgent.period = 1;
	}

}
