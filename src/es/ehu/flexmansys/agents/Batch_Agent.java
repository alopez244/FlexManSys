package es.ehu.flexmansys.agents;

import es.ehu.flexmansys.templates.Production_Agent;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;

import es.ehu.platform.behaviour.ControlBehaviour;

public class Batch_Agent extends Production_Agent {

	private static final long serialVersionUID = -6798384710308698436L;
	
	static final Logger LOGGER = LogManager.getLogger(Production_Agent.class.getName());

	public Document positionList;

	@Override
	protected void variableInitialization(Object[] arguments) {
		LOGGER.entry(arguments);
		if (arguments.length >= 2) {
			this.productionName = arguments[0].toString();
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
				this.productModel = dBuilder.parse(xmlFile);
			} catch (Exception e) {
				LOGGER.info("Parse can not generate documents");
				this.initTransition = ControlBehaviour.STOP;
			}
			File xmlFile2 = new File(arguments[2].toString());
			
			try {
				dBuilder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				LOGGER.info("Document can not be generated");
				this.initTransition = ControlBehaviour.STOP;
			}
			try {
				positionList = dBuilder.parse(xmlFile2);
			} catch (Exception e) {
				LOGGER.info("Parse can not generate documents");
				this.initTransition = ControlBehaviour.STOP;
			}
		} else {
			LOGGER.info("There are not sufficient arguments to start");
			this.initTransition = ControlBehaviour.STOP;
		}

		functionalityInstance = new es.ehu.flexmansys.functionality.Batch_Functionality(this);
		LOGGER.exit();
	}
}
