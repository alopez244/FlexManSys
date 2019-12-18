package es.ehu.flexmansys.agents;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ros.jade.RosAgent;

import es.ehu.flexmansys.templates.Monitor_Template;
import es.ehu.flexmansys.functionality.Monitor_Functionality;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import es.ehu.behaviour.ControlBehaviour;

import static es.ehu.utilities.MasReconOntologies.*;

public class Monitor_Agent extends Monitor_Template {

	private static final long serialVersionUID = -6798384710308698436L;
	static final Logger LOGGER = LogManager.getLogger(Monitor_Agent.class.getName());

	// TODO: Read this data from a file
	// Event List
	private static final String KILL = "kill";
	private static final String REQ_ORDER = "req_order";
	private static final String REQ_PLAN = "req_plan";

	public RosAgent monitorROS;

	@Override
	protected MessageTemplate variableInitialization(Object[] arguments, Behaviour behaviour) {
		LOGGER.entry(arguments);

		MessageTemplate informData = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DATA),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		MessageTemplate informLog = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_LOG),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		MessageTemplate informControl = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_CONTROL),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));


		Set<String> eventList = new HashSet<String>();
		eventList.add(KILL);
		eventList.add(REQ_ORDER);
		eventList.add(REQ_PLAN);
		monitorROS = new RosAgent(this, behaviour, null, eventList);
	
		if ((arguments != null) && (arguments.length >= 1)) {
			this.monitorName = arguments[0].toString();
		} else {
			LOGGER.info("There are not sufficient arguments to start");
			this.initTransition = ControlBehaviour.STOP;
		}

		functionalityInstance = new Monitor_Functionality(this);
		return LOGGER.exit(MessageTemplate.or(informData, MessageTemplate.or(informLog, informControl)));
	}
}
