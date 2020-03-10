package es.ehu.flexmansys.functionality;

import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.flexmansys.agents.Monitor_Agent;
import es.ehu.flexmansys.utilities.Position;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import org.ros.jade.RosMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static es.ehu.flexmansys.utilities.FmsNegotiation.*;
import static es.ehu.platform.utilities.MasReconOntologies.*;

/**
 * Monitor Agent Functionality
 * 
 * @author Brais Fortes (@fortes23) - Euskal Herriko Unibersitatea
 * @author Mikel López (@lopeziglesiasmikel) - Euskal Herriko Unibersitatea
 */

public class Monitor_Functionality implements BasicFunctionality {

	private static final long serialVersionUID = -8778994273760536207L;
	static final Logger LOGGER = LogManager.getLogger(Monitor_Functionality.class.getName());

	private static final String SHOW_MACHINE = "show_machine";
	private static final String SHOW_TRANSPORT = "show_transport";
	private static final String SHOW_ORDER = "show_order";
	private static final String SHOW_PLAN = "show_plan";
	private static final String KILL = "kill";
	private static final String REQ_ORDER = "req_order";
	private static final String REQ_PLAN = "req_plan";

	/** Identifier of the agent. */
	private Monitor_Agent myAgent;

	public ConcurrentHashMap<String, Hashtable<String, String>> elements;

	/** Template where the data requested by this agent is received. */
	private MessageTemplate informData;

	/**
	 * Template where the data of the system is received systematically (set state).
	 */
	private MessageTemplate informLog;

	/**
	 * Template where the data of the system is received systematically (set
	 * register).
	 */
	private MessageTemplate informControl;

	// Constructor
	public Monitor_Functionality(Monitor_Agent agent) {
		LOGGER.entry("*** Constructing Monitor_Functionality ***");
		this.myAgent = agent;
		try {
			// Wait while the ROS Agent is initialized
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}
		informData = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DATA),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		informLog = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_LOG),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		informControl = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_CONTROL),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));

		LOGGER.exit();
	}

	/**
	 * Initialize the functional properties
	 * 
	 * @return Message for registering the agent in the MWM (or SM)
	 */
	public String init() {
		LOGGER.entry(myAgent.monitorName);
		elements = new ConcurrentHashMap<String, Hashtable<String, String>>();
		String regParams = CAT_MONITOR + " " + REG_NAME + "=" + (myAgent.monitorName) + " " + REG_ID + "="
				+ (myAgent.getLocalName()) + " " + REG_SERVICES + "=" + ACT_LOG;
		return LOGGER.exit(regParams);
	}

	/**
	 * Checks if the state changed
	 * 
	 * @return State if there were changes or null otherwise.
	 */
	public Object getState() {
		LOGGER.entry();
		return LOGGER.exit(null);
	}

	/**
	 * Updates the state of the agent.
	 */
	public void setState(Object state) {
		LOGGER.entry();
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

		ACLMessage eventMsg = eventManagement();
		if (eventMsg != null) {
			if (msg != null) {
				myAgent.putBack(msg);
			}
			return LOGGER.exit(eventMsg);
		}

		// Check message templates
		if (msg == null) {
			return LOGGER.exit(null);
		}
		if (informData.match(msg)) {
			// Send order Information to Montitor ROS node
		} else if (informLog.match(msg)) {
			try {
				String id = msg.getSender().getLocalName();
				if (elements.get(id).get(REG_CATEGORY).equals(CAT_TRANSPORT)) {
					Position posTransport = (Position) msg.getContentObject();
					String posX = Float.toString(posTransport.getxPos());
					String posY = Float.toString(posTransport.getyPos());
					elements.get(id).put(REG_POSX, posX);
					elements.get(id).put(REG_POSY, posY);
					sendData(id);
				}
			} catch (Exception e) {
				LOGGER.info("INFORMLOG: Impossible casting to hashmap");
			}

		} else if (informControl.match(msg)) {
			try {
				HashMap<String, Hashtable<String, String>> aux = new HashMap<String, Hashtable<String, String>>();
				aux = (HashMap<String, Hashtable<String, String>>) msg.getContentObject();
				for (String id : aux.keySet()) {
					if (elements.get(id) != null) {
						for (String attrib : aux.get(id).keySet()) {
							elements.get(id).put(attrib, aux.get(id).get(attrib));
						}
					} else {
						elements.put(id, aux.get(id));
					}
					sendData(id);
				}
			} catch (Exception e) {
				LOGGER.info("Impossible casting to hashmap");
			}
		}
		return LOGGER.exit(null);
	}

	/**
	 * Analyze the information and send a request to the ROS GUI.
	 * 
	 * @param id identifier of the element.
	 */
	private void sendData(String id) {
		LOGGER.entry();
		String name = elements.get(id).get(REG_NAME);
		String state = elements.get(id).get(REG_STATE);
		String posX = elements.get(id).get(REG_POSX);
		String posY = elements.get(id).get(REG_POSY);
		if (state == null) {
			state = "running";
		}
		String[] content;
		if ((posX == null) || (posY == null)) {
			content = new String[] { name, state };
		} else {
			content = new String[] { name, state, posX, posY };
		}
		RosMsg rosmsg = null;
		LOGGER.debug("Received data with category: " + elements.get(id).get(REG_CATEGORY));
		switch (elements.get(id).get(REG_CATEGORY)) {
		case CAT_MACHINE:
			rosmsg = new RosMsg("0", SHOW_MACHINE, content);
			break;
		case CAT_TRANSPORT:
			rosmsg = new RosMsg("0", SHOW_TRANSPORT, content);
			break;
		case CAT_ORDER:
			rosmsg = new RosMsg("0", SHOW_ORDER, new String[] { name });
			break;
		case CAT_PLAN:
			rosmsg = new RosMsg("0", SHOW_PLAN, new String[] { name });
			break;
		}
		if (rosmsg != null) {
			LOGGER.debug("Sending msg: " + rosmsg);
			myAgent.monitorROS.send(rosmsg);
		}
		LOGGER.exit();
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
	 * Check if there are new events in the cognitive layer of the robot. In this
	 * case, it is managed.
	 * 
	 * @return Msg to send.
	 */
	private ACLMessage eventManagement() {
		LOGGER.entry();
		ConcurrentHashMap<String, RosMsg> events = new ConcurrentHashMap<String, RosMsg>(
				myAgent.monitorROS.getEvents());
		myAgent.monitorROS.removeAllEvents();
		if (events == null) {
			return LOGGER.exit(null);
		}
		for (String key : events.keySet()) {
			LOGGER.debug("Analyzing event " + events.get(key).getOntology());
			String[] content = events.get(key).getContent();
			String idSystem = null;
			if (content != null && content.length > 0) {
				String name = content[0];
				for (String id : elements.keySet()) {
					if (elements.get(id).get(REG_NAME).equals(name)) {
						idSystem = id;
						break;
					}
				}
			}
			if (idSystem == null) {
				continue;
			}
			switch (events.get(key).getOntology()) {
			case KILL:
				return LOGGER.exit(createMsg(ONT_KILL, ACLMessage.INFORM, KILL, new AID(idSystem, AID.ISLOCALNAME)));
			case REQ_ORDER:
				return LOGGER
						.exit(createMsg(ONT_DATA, ACLMessage.REQUEST, REQ_ORDER, new AID(idSystem, AID.ISLOCALNAME)));
			case REQ_PLAN:
				LOGGER.info("Request for new plan: " + idSystem);
				break;
			default:
				LOGGER.info("Event " + events.get(key).getConversationID() + " is not being read");
			}
		}
		return LOGGER.exit(null);
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
}