package es.ehu.domain.manufacturing.agents.cognitive.ros.jade;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import es.ehu.domain.manufacturing.agents.cognitive.ros.jade.RosNodeMsg;
import org.ros.RosCore;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.*;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import org.ros.message.Time;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;

import social_msgs.social;

/**
 * Class to represent a JADE Agent in the Robotic Operating System middelware.
 * 
 * @author Brais Fortes (@fortes23) - Euskal Herriko Unibersitatea
 * @see AbstractNodeMain
 */

public class RosNodeAgent extends AbstractNodeMain {
	// Constant variables
	private static final String PREFIX_TOPICS = "/social";
	private static final String PERCEPTION_TOPIC = PREFIX_TOPICS + "/perception";
	private static final String ORDER_TOPIC = PREFIX_TOPICS + "/order";
	private static final int MAX_SIZE_BACKUP = 5000;

	/** List of interesting values of the agent */
	private Map<String, String> reportList;
	/** List of low-level events of the agent */
	private Map<String, String> eventList;

	/** Database of all the reports received in ROS (and not cleaned yet) */
	private ConcurrentHashMap<String, RosNodeMsg> reportStorage;
	/** Database of all the events received in ROS (and not cleaned yet) */
	private ConcurrentHashMap<String, RosNodeMsg> eventStorage;
	/** Database of the last messages received by this ROS node */
	private RosNodeMsg[] backupStorage;
	/** Value to point to the current position of the {@code backupStorage} */
	private int backupPosePointer;

	private ConnectedNode connectedNode;
	private boolean connected;

	/** Publisher in the {@code ORDER_TOPIC} topic */
	private Publisher<social> orderPublisher;
	/** Subscriber in the {@code PECEPTION_TOPIC/<agent_name>} topic */
	private Subscriber<social> ownChSubscriber;
	/** Subscriber in the {@code PERCEPTION_TOPIC} topic */
	private Subscriber<social> perceptionSubscriber;

	/** JADE Agent represented in the ROS platform */
	private Agent myAgent;
	/** Behaviour to wake up the agent if there are new events */
	private Behaviour controlledBehaviour;

	// Constructor
	public RosNodeAgent(Agent a) {

		this.myAgent = a;
		this.controlledBehaviour = null;
		this.reportList = new HashMap<String, String>();
		this.eventList = new HashMap<String, String>();
		this.reportStorage = new ConcurrentHashMap<String, RosNodeMsg>();
		this.eventStorage = new ConcurrentHashMap<String, RosNodeMsg>();
		this.connected = false;
		this.restartBackup();

		RosCore rosCore = null;
		try {
			rosCore.newPublic(11311);
			rosCore.start();
			rosCore.awaitStart(1, TimeUnit.SECONDS);
		} catch (Exception e) {
			rosCore = null;
		}
		
		NodeMain nodeMain = (NodeMain) this;
		NodeConfiguration nodeConfiguration = NodeConfiguration.newPrivate();
		nodeConfiguration.setNodeName(myAgent.getLocalName());
		if (rosCore != null) {
			nodeConfiguration.setMasterUri(rosCore.getUri());
		}
		NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
		nodeMainExecutor.execute(nodeMain, nodeConfiguration);
	}

	// Constructor
	public RosNodeAgent(Agent a, Behaviour b) {
		this(a);
		this.controlledBehaviour = b;
	}

	// Constructor
	public RosNodeAgent(Agent a, Set<String> reportList, Set<String> eventList) {
		this(a);
		modifyReportList(reportList);
		modifyEventList(eventList);
	}

	// Constructor
	public RosNodeAgent(Agent a, Behaviour b, Set<String> reportList, Set<String> eventList) {
		this(a, reportList, eventList);
		this.controlledBehaviour = b;
	}

	/**
	 * Name of the ROS node.
	 * 
	 * @return String of the name
	 */
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of(myAgent.getLocalName());
	}

	/**
	 * Initialization of the ROS variables themselves.
	 * 
	 * @param connectedNode Node defined in the ROS graph and connected to MASTER.
	 */
	@Override
	public void onStart(final ConnectedNode connectedNode) {
		this.connectedNode = connectedNode;
		this.connected = true;

		this.orderPublisher = connectedNode.newPublisher(ORDER_TOPIC, social._TYPE);
		this.perceptionSubscriber = connectedNode.newSubscriber(PERCEPTION_TOPIC, social._TYPE);
		this.ownChSubscriber = connectedNode
				.newSubscriber(PERCEPTION_TOPIC + "/" + this.getDefaultNodeName().toString(), social._TYPE);

		perceptionSubscriber.addMessageListener(new MessageListener<social>() {
			@Override
			public void onNewMessage(social msg) {
				manageReceivedMsg(msg);
			}
		});

		ownChSubscriber.addMessageListener(new MessageListener<social>() {
			@Override
			public void onNewMessage(social msg) {
				manageReceivedMsg(msg);
			}
		});
	}

	/**
	 * Analyze the message received by the ROS topics and checks if they are mapped
	 * in the {@code reportList} or in the {@code eventList}. In these cases, it
	 * will store the value in teh corresponding database.
	 * 
	 * @param msg message received in the topics.
	 */
	private void manageReceivedMsg(social msg) {
		RosNodeMsg aux = new RosNodeMsg(msg.getConversationID(), msg.getOntology(), msg.getContent().toArray(new String[0]));
		if (eventList.get(msg.getOntology()) != null) {
			eventStorage.put(msg.getOntology(), aux);
			if (controlledBehaviour != null) {
				controlledBehaviour.restart();
			}
		}
		if (reportList.get(msg.getOntology()) != null) {
			reportStorage.put(msg.getOntology(), aux);
		}
		addBackUpInfo(aux);
	}

	/**
	 * Insert a new message in the {@code backupStorage}.
	 * 
	 * @param msg message received in the topics.
	 */
	private void addBackUpInfo(RosNodeMsg msg) {
		backupStorage[backupPosePointer] = msg;
		backupPosePointer++;
		if (backupPosePointer >= MAX_SIZE_BACKUP) {
			backupPosePointer = 0;
		}
	}

	/**
	 * Get the state of the Node
	 * 
	 * @return TRUE if the node has started.
	 */
	public boolean isConnected() {
		return this.connected;
	}

	/**
	 * Send the info received in {@code data}, using the defined topic
	 * {@code ORDER_TOPIC}.
	 * 
	 * @param data Message written by the agent.
	 */
	public void send(RosNodeMsg data) {
		social msg = connectedNode.getTopicMessageFactory().newFromType(social._TYPE);
		msg.setOntology(data.getOntology());
		msg.setContent(Arrays.asList(data.getContent()));
		msg.setConversationID(data.getConversationID());
		msg.setSender(this.getDefaultNodeName().toString());
		msg.getHeader().setStamp(Time.fromMillis(System.currentTimeMillis()));
		orderPublisher.publish(msg);
	}

	/**
	 * Modify the behaviour used to restart the agent.
	 * 
	 * @param b Agent behaviour
	 */
	public void setControlledBehaviour(Behaviour b) {
		this.controlledBehaviour = b;
	}

	/**
	 * Get all the reports stored in the database.
	 * 
	 * @return Map of values.
	 */
	public ConcurrentHashMap<String, RosNodeMsg> getReports() {
		return this.reportStorage;
	}

	/**
	 * Get all the events stored in the database.
	 * 
	 * @return Map of values.
	 */
	public ConcurrentHashMap<String, RosNodeMsg> getEvents() {
		return this.eventStorage;
	}

	/**
	 * Get all the backup data stored in the database.
	 * 
	 * @return Map of values.
	 */
	public RosNodeMsg[] getBackUp() {
		return this.backupStorage;
	}

	/** Remove all the reports from the database. */
	public void removeAllReports() {
		this.reportStorage.clear();
	}

	/** Remove all the events from the database. */
	public void removeAllEvents() {
		this.eventStorage.clear();
	}

	/** Remove all the backup data from the database. */
	public void restartBackup() {
		this.backupStorage = new RosNodeMsg[MAX_SIZE_BACKUP];
		this.backupPosePointer = 0;
	}

	/**
	 * Modify report values to filter new messages.
	 * 
	 * @param reports Values saved into {@code reportList}
	 */
	public void modifyReportList(Set<String> reports) {
		this.removeAllReports();
		if (reports != null) {
			for (String report : reports) {
				this.reportList.put(report, report);
			}
		}
	}

	/**
	 * Modify events values to filter new messages.
	 * 
	 * @param events Values saved into {@code eventList}
	 */
	public void modifyEventList(Set<String> events) {
		this.removeAllEvents();
		if (events != null) {
			for (String event : events) {
				this.eventList.put(event, event);
			}
		}
	}

}