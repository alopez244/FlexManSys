package es.ehu.flexmansys.agents;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import es.ehu.platform.behaviour.ControlBehaviour;
import static es.ehu.flexmansys.utilities.FmsNegotiation.ONT_DEBUG;
import static es.ehu.platform.utilities.MasReconOntologies.*;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class PowerStation_Agent extends Resource_Agent {

	private static final long serialVersionUID = -8274164310662945089L;
	static final Logger LOGGER = LogManager.getLogger(PowerStation_Agent.class.getName());
	public Document Positions;

	@Override
	protected MessageTemplate variableInitialization(Object[] arguments, Behaviour behaviour) {
		LOGGER.entry(arguments);

		MessageTemplate informNeg = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_NEGOTIATE),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		MessageTemplate informData = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DATA),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		MessageTemplate informDebug = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DEBUG),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));

		if ((arguments != null) && (arguments.length >= 2)) {
			this.resourceName = arguments[0].toString();
			File xmlFile = new File(arguments[1].toString());
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = null;
			try {
				dBuilder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				LOGGER.info("Document can not be generated");
				this.initTransition = ControlBehaviour.STOP;
			}
			try {
				this.Positions = dBuilder.parse(xmlFile);
			} catch (Exception e) {
				LOGGER.info("Parse can not generate documents");
				this.initTransition = ControlBehaviour.STOP;
			}
		} else {
			LOGGER.info("There are not sufficient arguments to start");
			this.initTransition = ControlBehaviour.STOP;
		}

		functionalityInstance = new PowerStation_Functionality(this);

		return LOGGER.exit(MessageTemplate.or(informNeg, MessageTemplate.or(informData, informDebug)));
	}
}
