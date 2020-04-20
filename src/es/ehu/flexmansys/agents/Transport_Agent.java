package es.ehu.flexmansys.agents;

import es.ehu.behaviour.ControlBehaviour;
import es.ehu.flexmansys.functionality.Transport_Functionality;
import es.ehu.flexmansys.templates.Resource_Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ros.jade.RosAgent;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static es.ehu.flexmansys.utilities.FmsNegotiation.ONT_KILL;
import static es.ehu.utilities.MasReconOntologies.ONT_NEGOTIATE;

public class Transport_Agent extends Resource_Agent {

	private static final long serialVersionUID = 8181455528174740258L;
	static final Logger LOGGER = LogManager.getLogger(Resource_Agent.class.getName());
	public RosAgent robotTransport;
	public String transportServices;
	public Document transportPositionList;
	private static final String TRANS_STATE = "state";
	private static final String TRANS_BATTERY = "battery";
	private static final String TRANS_FINISHORDER = "finish_order";
	private static final String TRANS_ALARMBATTERY = "alarm_battery";

	@Override
	protected MessageTemplate variableInitialization(Object[] arguments, Behaviour behaviour) {
		LOGGER.entry(arguments);

		MessageTemplate informNeg = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_NEGOTIATE),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		MessageTemplate refuseNeg = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_NEGOTIATE),
				MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
		MessageTemplate killResource = MessageTemplate.MatchOntology(ONT_KILL);

		Set<String> eventList = new HashSet<String>();
		eventList.add(TRANS_STATE);
		eventList.add(TRANS_BATTERY);
		eventList.add(TRANS_FINISHORDER);
		eventList.add(TRANS_ALARMBATTERY);
		robotTransport = new RosAgent(this, behaviour, null, eventList);

		if ((arguments != null) && (arguments.length >= 2)) {
			this.resourceName = arguments[0].toString();
			transportServices = arguments[1].toString();
			File xmlFile = new File(arguments[2].toString());
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = null;
			try {
				dBuilder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				LOGGER.info("Document can not be generated");
				this.initTransition = ControlBehaviour.STOP;
			}
			try {
				transportPositionList = dBuilder.parse(xmlFile);
			} catch (Exception e) {
				LOGGER.info("Parse can not generate documents");
				this.initTransition = ControlBehaviour.STOP;
			}
		} else {
			LOGGER.info("There are not sufficient arguments to start");
			this.initTransition = ControlBehaviour.STOP;
		}

		functionalityInstance = new Transport_Functionality(this);
		return LOGGER.exit(MessageTemplate.or(informNeg, MessageTemplate.or(refuseNeg, killResource)));
	}
}
