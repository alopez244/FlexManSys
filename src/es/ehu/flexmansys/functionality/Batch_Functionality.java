package es.ehu.flexmansys.functionality;

import jade.core.AID;
import jade.lang.acl.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.tuple.Pair;

import org.w3c.dom.*;

import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.utilities.MsgNegotiation;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.flexmansys.agents.Batch_Agent;
import es.ehu.flexmansys.utilities.*;

import java.util.concurrent.ConcurrentHashMap;

import static es.ehu.flexmansys.utilities.FmsNegotiation.*;
import static es.ehu.platform.utilities.MasReconOntologies.*;

/**
 * Batch Agent Functionality
 * 
 * @author Brais Fortes (@fortes23) - Euskal Herriko Unibersitatea
 * @author Mikel López (@lopeziglesiasmikel) - Euskal Herriko Unibersitatea
 */

public class Batch_Functionality implements BasicFunctionality {

	private static final long serialVersionUID = -5826950526310902398L;
	static final Logger LOGGER = LogManager.getLogger(Batch_Functionality.class.getName());

	/** Batch state. */
	private static class batchState implements Serializable {

		private static final long serialVersionUID = 1232069377953096834L;

		/** Identifier of the task which is being negotiated. */
		private ConcurrentHashMap<String, Timeout> timeoutTask;

		/** Identifier of the task which is being negotiated. */
		private ConcurrentHashMap<String, MsgNegotiation> infoTask;

		/** Identifier of the agents which request the negotiation */
		private Document traceabilityModel;

		public batchState(Document traceModel) {
			this.timeoutTask = new ConcurrentHashMap<String, Timeout>();
			this.infoTask = new ConcurrentHashMap<String, MsgNegotiation>();
			this.traceabilityModel = traceModel;
		}

		public batchState() {
			this(null);
			try {
				this.traceabilityModel = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			} catch (Exception e) {
				LOGGER.info("batchState cannot init the traceability model");
				this.traceabilityModel = null;
			}
		}

		// Setter methods
		public void setTraceModel(Document a) {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = null;
			try {
				dBuilder = docFactory.newDocumentBuilder();
			} catch (Exception e) {
				LOGGER.info("Document can not be generated");
				e.printStackTrace();
			}
			// this.traceabilityModel = a;
			this.traceabilityModel = dBuilder.newDocument();
			this.traceabilityModel.appendChild(this.traceabilityModel.importNode(a.getDocumentElement(), true));
		}

		public void setTimeoutTask(ConcurrentHashMap<String, Timeout> a) {
			this.timeoutTask = new ConcurrentHashMap<String, Timeout>(a);
		}

		public void putTimeoutTask(String key, Timeout value) {
			this.timeoutTask.put(key, value);
		}

		public void putInfoTask(String key, MsgNegotiation value) {
			this.infoTask.put(key, value);
		}

		public void removeTask(String key) {
			this.timeoutTask.remove(key);
			this.infoTask.remove(key);
		}

		public void setAll(batchState a) {
			this.setTimeoutTask(a.getTimeoutTask());
			this.setTraceModel(a.getTraceModel());
		}

		// Getter methods
		public Document getTraceModel() {
			return this.traceabilityModel;
		}

		public ConcurrentHashMap<String, MsgNegotiation> getInfoTask() {
			return this.infoTask;
		}

		public ConcurrentHashMap<String, Timeout> getTimeoutTask() {
			return this.timeoutTask;
		}

		@Override
		public boolean equals(Object o) {

			if (o == this) {
				return true;
			}
			if (!(o instanceof batchState)) {
				return false;
			}
			batchState c = (batchState) o;
			Element cur = traceabilityModel.getDocumentElement();
			Element out = c.getTraceModel().getDocumentElement();
			return (Objects.equals(timeoutTask, c.getTimeoutTask()) && Objects.equals(infoTask, c.getInfoTask())
					&& cur.isEqualNode(out));
		}

		@Override
		public int hashCode() {
			return Objects.hash(timeoutTask, infoTask, traceabilityModel);
		}
	}

	// Traceability data names.
	private static final String TRACE_MACHINE = "machine";
	private static final String TRACE_STARTTIME = "startTime";
	private static final String TRACE_FINISHTIME = "finishTime";
	private static final String TRACE_XPOS = "xPos";
	private static final String TRACE_YPOS = "yPos";
	private static final String MSG_DELAY = "delay";
	private static final String MAT_ID = "id";

	/** Identifier of the taskID counter. */
	private int countTaskID = 0;

	/** Identifier of the current state of the batch. */
	batchState curState;

	/** Identifier of the previous state of the batch. */
	batchState prevState;

	/** Identifier of the agent. */
	Batch_Agent myAgent;

	/** Identifier of the request operaion template. */
	MessageTemplate requestTemplate;

	/** Identifier of the operation template. */
	MessageTemplate operationTemplate;

	/** Identifier of the negotiation refused template. */
	MessageTemplate negRefuseTemplate;

	/** Identifier of the negotiation template. */
	MessageTemplate negTemplate;

	/** Identifier of the warehouse position. */
	Position warehousePos;

	/** Identifier of the warehouse position. */
	Position finalwarehousePos;

	private Pair<String, Long> smallTimeout;

	private long lastExecMillis;

	public Batch_Functionality(Batch_Agent agent) {
		LOGGER.entry("*** Constructing Batch_Functionality ***");
		this.myAgent = agent;

		Element initWarehouse = myAgent.positionList.getElementById("WH1");
		Element finalWarehouse = myAgent.positionList.getElementById("WH2");
		Element iwhPos = (Element) initWarehouse.getElementsByTagName("position").item(0);
		Element fwhPos = (Element) finalWarehouse.getElementsByTagName("position").item(0);
		warehousePos = new Position(Float.valueOf(iwhPos.getAttribute("posX")),
				Float.valueOf(iwhPos.getAttribute("posY")));
		finalwarehousePos = new Position(Float.valueOf(fwhPos.getAttribute("posX")),
				Float.valueOf(fwhPos.getAttribute("posY")));

		requestTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DATA),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		operationTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DATA),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		negRefuseTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_NEGOTIATE),
				MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
		negTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_NEGOTIATE),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		LOGGER.exit();
	}

	/**
	 * Initialize the functional properties
	 * 
	 * @return Message for registering the agent in the MWM (or SM)
	 */
	public String init() {
		LOGGER.entry(myAgent.productionName, myAgent.productModel);
		curState = new batchState(myAgent.productModel);
		prevState = new batchState();
		prevState.setAll(curState);
		lastExecMillis = System.currentTimeMillis();
		smallTimeout = Pair.of(null, Long.MAX_VALUE);

		String regParams = "batch " + "name=" + (myAgent.productionName) + " " + REG_CATEGORY + "=" + CAT_BATCH + " "
				+ REG_ID + "=" + myAgent.productionName;
		return LOGGER.exit(regParams);
	}

	/**
	 * Checks if the state changed
	 * 
	 * @return State if there were changes or null otherwise.
	 */
	public Object getState() {
		LOGGER.entry();
		// if (curState.equals(prevState)) {
		// LOGGER.debug("State has not changed");
		// return LOGGER.exit(null);
		// } else {
		// LOGGER.debug("State has changed");
		// prevState.setAll(curState);
		// }
		return LOGGER.exit(null);
	}

	/**
	 * Updates the state of the agent.
	 */
	public void setState(Object state) {
		LOGGER.entry();
		try {
			batchState aux = (batchState) state;
			this.curState.setAll(aux);
			myAgent.productModel = curState.getTraceModel();
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
		if (timeMsg != null) {
			if (msg != null) {
				myAgent.putBack(msg);
			}
			return LOGGER.exit(timeMsg);
		}

		if (msg == null) {
			return LOGGER.exit(null);
		}

		if (requestTemplate.match(msg)) {
			// If msg matches transportTemplate either transport is needed or production is
			// delayed
			MsgReqOperation content = null;
			try {
				content = (MsgReqOperation) msg.getContentObject();
			} catch (UnreadableException e) {
				LOGGER.info("Incorrect msg content. MsgReqOperation object expected");
				return LOGGER.exit(null);
			}
			if (isRawMaterial(content.getOperationID(), content.getSubproductID())) {
				MsgTransport msgReqContent = new MsgTransport(content.getSubproductID(), warehousePos,
						content.getFinalPosition());
				return LOGGER.exit(createNegMsg(ACT_DELIVERY, msgReqContent));
			} else if (curState.getTraceModel().getElementById(content.getSubproductID()).hasAttribute(TRACE_XPOS)) {
				Position init_pos = getLastPos(content.getSubproductID());
				if (init_pos != null) {
					return LOGGER.exit(createNegMsg(ACT_DELIVERY,
							new MsgTransport(content.getSubproductID(), init_pos, content.getFinalPosition())));
				}
			} else {
				// Product is delayed
				return LOGGER.exit(createMsg(msg.getOntology(), msg.getConversationId(), ACLMessage.REJECT_PROPOSAL,
						MSG_DELAY, msg.getSender()));
			}

		} else if (operationTemplate.match(msg)) {
			// If msg matches operationTemplate either an operation started or
			MsgOperation content = null;
			try {
				content = (MsgOperation) msg.getContentObject();
			} catch (UnreadableException e) {
				LOGGER.info("Incorrect msg content. MsgOperation object expected");
			}
			if (content != null) {
				if (content.getResourceType() == MsgOperation.MACHINE && content.getStart()) {
					// Save in traceability machine and timestamp
					if (addAttrib(content.getOperationID(), TRACE_MACHINE, msg.getSender().getLocalName())) {
						Date date = new Date(msg.getPostTimeStamp());
						addAttrib(content.getOperationID(), TRACE_STARTTIME, date.toString());
						addTimeout(content.getTaskID(), new Timeout(msg.getSender(), content.getEstimatedTime()));
					}
				} else if (content.getResourceType() == MsgOperation.TRANSPORT && content.getStart()) {
					LOGGER.info("Transport operation has started");
				} else if (content.getResourceType() == MsgOperation.MACHINE && !content.getStart()) {
					removeTimeout(content.getTaskID());
					Date date = new Date(msg.getPostTimeStamp());
					if (addAttrib(content.getOperationID(), TRACE_FINISHTIME, date.toString())) {
						addLastPos(content.getResourceType(), content.getOperationID(), content.getPosition());
					}
				} else if (content.getResourceType() == MsgOperation.TRANSPORT && !content.getStart()) {
					removeTimeout(content.getTaskID());
					addLastPos(content.getResourceType(), content.getOperationID(), content.getPosition());
				} else {
					LOGGER.info("Not expected operation message");
				}
			}
		} else if (negTemplate.match(msg)) {
			MsgOperation content = null;
			try {
				content = (MsgOperation) msg.getContentObject();
			} catch (UnreadableException e) {
				LOGGER.info("Incorrect msg content. MsgOperation object expected");
			}
			if (content != null) {
				addTimeout(content.getTaskID(), new Timeout(msg.getSender(), content.getEstimatedTime()));
			} else {
				LOGGER.info("Not expected operation message");
			}
		} else if (negRefuseTemplate.match(msg)) {
			if (curState.getInfoTask().get(msg.getConversationId()) != null) {
				ACLMessage response = createNegMsg(curState.getInfoTask().get(msg.getConversationId()).getNegAction(),
						curState.getInfoTask().get(msg.getConversationId()).getExternalData());
				curState.removeTask(msg.getConversationId());
				return LOGGER.exit(response);
			}
			LOGGER.debug("Refuse Negotiation Msg invalid");
		}

		return LOGGER.exit(checkBatchFinished());
	}

	/**
	 * Secure ending
	 */
	public Object endFunctionality() {
		return null;
	}

	/**
	 * Finds if a subproduct is rawMaterial
	 * 
	 * @param operation  Service requested.
	 * @param subproduct .
	 * 
	 * @return true if the subproduct is rawMaterial
	 */
	private boolean isRawMaterial(String operation, String subproduct) {
		LOGGER.entry(operation, subproduct);
		final String MAT_TYPE = "rawMaterial";
		Boolean rawMaterial = false;
		NodeList aux = curState.getTraceModel().getElementById(operation).getParentNode().getChildNodes();
		for (int i = 0; i < aux.getLength(); i++) {
			Node nodo = aux.item(i);
			if (nodo.getNodeType() == Node.ELEMENT_NODE) {
				try {
					rawMaterial = (nodo.getNodeName().equals(MAT_TYPE))
							&& (nodo.getAttributes().getNamedItem(MAT_ID).getNodeValue().equals(subproduct));

					NodeList aux2 = nodo.getChildNodes();
					for (int j = 0; j < aux2.getLength(); j++) {
						if (aux2.item(j).getNodeType() == Node.ELEMENT_NODE) {
							rawMaterial |= (aux2.item(j).getNodeName().equals(MAT_TYPE)) && (aux2.item(j)
									.getAttributes().getNamedItem(MAT_ID).getNodeValue().equals(subproduct));
							break;
						}
					}
				} catch (Exception e) {
					LOGGER.debug("FAULT " + nodo.getAttributes().getNamedItem(MAT_ID).getNodeValue());
				}
			}
		}
		return LOGGER.exit(rawMaterial);
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
		curState.putInfoTask(taskID, content);

		return LOGGER.exit(createMsg(ONT_NEGOTIATE, taskID, ACLMessage.REQUEST, content, targetAID));
	}

	private boolean addAttrib(String opID, String attName, String attValue) {
		return addAttrib(opID, attName, attValue, false);
	}

	private boolean addAttrib(String opID, String attName, String attValue, boolean parent) {
		LOGGER.entry(opID, attName, attValue, parent);
		boolean success = false;
		try {
			Element aux;
			if (parent) {
				aux = (Element) curState.getTraceModel().getElementById(opID).getParentNode();
			} else {
				aux = (Element) curState.getTraceModel().getElementById(opID);
			}
			aux.setAttribute(attName, attValue);
			success = true;
			LOGGER.debug("Traceability data added");
		} catch (Exception e) {
			LOGGER.debug("Attribute does not exist and cannot be added");
		}
		return LOGGER.exit(success);
	}

	private void addLastPos(int resourceType, String operation, Position position) {
		LOGGER.entry(resourceType, operation, position);
		String xPos = String.valueOf(position.getxPos());
		String yPos = String.valueOf(position.getyPos());

		if ((resourceType == MsgOperation.MACHINE) || (resourceType == MsgOperation.TRANSPORT)) {
			boolean parent = (resourceType == MsgOperation.MACHINE) ? true : false;
			addAttrib(operation, TRACE_XPOS, xPos, parent);
			addAttrib(operation, TRACE_YPOS, yPos, parent);
		} else {
			LOGGER.info("Not expected resource");
		}
	}

	/**
	 * Searchs the last position of a {@code subproduct}.
	 * 
	 * @param subproduct Value saved in the product model.
	 * @return last position or null if it does not exist
	 */
	private Position getLastPos(String subproduct) {
		LOGGER.entry();
		try {
			float xPos = Float.parseFloat(curState.getTraceModel().getElementById(subproduct).getAttribute(TRACE_XPOS));
			float yPos = Float.parseFloat(curState.getTraceModel().getElementById(subproduct).getAttribute(TRACE_YPOS));
			LOGGER.info("Last posistion found");
			return LOGGER.exit(new Position(xPos, yPos));
		} catch (Exception e) {
			LOGGER.debug("There is no position");
		}
		return LOGGER.exit(null);
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
				targets = myAgent.getInfoMWM("*", REG_SERVICES + "=" + ACT_DDRA).split(",");
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

	private ACLMessage checkBatchFinished() {
		LOGGER.entry();
		ACLMessage msg = null;
		NodeList aux = curState.getTraceModel().getChildNodes();

		for (int i = 0; i < aux.getLength(); i++) {
			if (aux.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element aux2 = (Element) aux.item(i);
				if (aux2.hasAttribute(TRACE_XPOS)) {
					float xPos = Float.valueOf(aux2.getAttribute(TRACE_XPOS));
					float yPos = Float.valueOf(aux2.getAttribute(TRACE_YPOS));
					Position pos = new Position(xPos, yPos);
					msg = createNegMsg(ACT_DELIVERY,
							new MsgTransport(aux2.getAttribute(MAT_ID), pos, finalwarehousePos));
					if (pos == finalwarehousePos) {
						msg = createMsg(ONT_CONTROL, ACLMessage.REQUEST,
								ControlBehaviour.CMD_SETSTATE + " " + ControlBehaviour.ST_STOP, myAgent.getAID());
						break;
					}
				}
			}
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
