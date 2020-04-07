package es.ehu.flexmansys.test;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import static es.ehu.utilities.MasReconOntologies.*;

import java.io.File;

import javax.print.Doc;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * This agent send different type of messages to some OrderAgents, checking that
 * the response for these messages are the correct ones.
 * 
 * @author Brais Fortes (@fortes23)
 * @author Mikel Lopez (@lopeziglesiasmikel)
 */
public class TestOrder extends Agent {

	private static final long serialVersionUID = 1L;
	static final Logger LOGGER = LogManager.getLogger(TestBatch.class.getName());
	private static int period = 5000;
	private static int numBatches = 2;
	private ACLMessage requestMsg;
	private ACLMessage traceinfoMsg;
	private ACLMessage received;
	private int countTaskID;
	private AID receivers;
	private static final String[] tracePaths = new String[] { "/home/mikel/Escritorio/HalogenHeadlightProductModel.xml",
			"/home/mikel/Escritorio/LEDHeadlightProductModel.xml" };

	private class testingOrder extends CyclicBehaviour {

		private static final long serialVersionUID = 579221321446822226L;
		private int i = 0;
		MessageTemplate recTemplate;
		Document dom = null;

		public testingOrder(Agent a) {
			super(a);
			recTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DATA),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		}

		public void action() {
			LOGGER.entry();
			if (i < numBatches) {
				try {
					traceinfoMsg.setContentObject((Serializable)createContent(tracePaths[i]));
				} catch (Exception e) {
					LOGGER.info("Content cannot be added");
				}
				LOGGER.info(traceinfoMsg);
				send(traceinfoMsg);
				i++;
			}
			if (i >= (numBatches-1)) {
				period = 0;
			}

			received = myAgent.receive(recTemplate);
			if (received != null) {
				try {
					dom = (Document) received.getContentObject();
				} catch (Exception e) {
					LOGGER.info("Not expected content");
				}
				saveToXML(dom, "orderTraceModel");
			}

			block(period);
			LOGGER.exit();
		}
	}

	protected void setup() {
		testingOrder test = new testingOrder(this);
		addBehaviour(test);
		receivers = new AID("order1", AID.ISLOCALNAME);
		requestMsg = new ACLMessage(ACLMessage.REQUEST);
		requestMsg.setOntology(ONT_DATA);
		requestMsg.addReceiver(receivers);
		traceinfoMsg = new ACLMessage(ACLMessage.INFORM);
		traceinfoMsg.setOntology(ONT_DATA);
		traceinfoMsg.addReceiver(receivers);
	}

	/**
	 * Get trace model
	 * 
	 * @return traceModel
	 */
	private Document createContent(String path) {
		LOGGER.entry(path);
		File XML = new File(path);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		try {
			dBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.out.println("Document can not be generated");
			e.printStackTrace();
		}
		Document productModel = null;
		try {
			productModel = dBuilder.parse(XML);
		} catch (Exception e) {
			System.out.println("Parse can not generate documents");
			e.printStackTrace();
		}
		return LOGGER.exit(productModel);
	}

	private static void saveToXML(Document dom, String docName) {
		LOGGER.entry(docName);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (Exception e) {
			LOGGER.info("Transformer creation failed");
		}
		DOMSource source = new DOMSource(dom);
		StreamResult result = new StreamResult(new File("/home/mikel/Escritorio/" + docName + ".xml"));

		try {
			transformer.transform(source, result);
		} catch (Exception e) {
			LOGGER.info("Transform failed");
		}

		LOGGER.exit("XML file saved");
	}
}