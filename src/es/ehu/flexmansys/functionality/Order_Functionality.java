package es.ehu.flexmansys.functionality;

import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.flexmansys.agents.Order_Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import static es.ehu.platform.utilities.MasReconOntologies.*;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.*;

import static es.ehu.flexmansys.utilities.FmsNegotiation.*;

/**
 * Order Agent Functionality
 * 
 * @author Brais Fortes (@fortes23) - Euskal Herriko Unibersitatea
 * @author Mikel López (@lopeziglesiasmikel) - Euskal Herriko Unibersitatea
 */

public class Order_Functionality implements BasicFunctionality {

	private static final long serialVersionUID = 1L;
	static final Logger LOGGER = LogManager.getLogger(Order_Functionality.class.getName());

	/** Order state. */
	private static class orderState implements Serializable {

		private static final long serialVersionUID = -5275119671326659890L;

		/** Identifier of the number of product rejected. */
		private int rejectedProducts;

		/** Identifier of the number of finished batches. */
		private int finishedBatches;

		/** Identifier of the agents which request the negotiation */
		private Document traceabilityModel;

		public orderState(String a) {
			this.rejectedProducts = 0;
			try {
				this.traceabilityModel = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				Element rootElement = this.traceabilityModel.createElement("order");
				rootElement.setAttribute("id", a);
				this.traceabilityModel.appendChild(rootElement);

			} catch (Exception e) {
				LOGGER.info("orderState cannot init the traceability model");
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
			this.traceabilityModel = dBuilder.newDocument();
			this.traceabilityModel.appendChild(this.traceabilityModel.importNode(a.getDocumentElement(), true));
		}

		public void setRejectedProducts(int a) {
			this.rejectedProducts = a;
		}

		public void setFinishedBatches(int a) {
			this.finishedBatches = a;
		}

		public void setAll(orderState a) {
			this.setRejectedProducts(a.getRejectedProducts());
			this.setTraceModel(a.getTraceModel());
			this.setFinishedBatches(a.getFinishedBatches());
		}

		// Getter methods
		public Document getTraceModel() {
			return this.traceabilityModel;
		}

		public int getRejectedProducts() {
			return this.rejectedProducts;
		}

		public int getFinishedBatches() {
			return this.finishedBatches;
		}

		@Override
		public boolean equals(Object o) {

			if (o == this) {
				return true;
			}
			if (!(o instanceof orderState)) {
				return false;
			}
			orderState c = (orderState) o;
			Element cur = traceabilityModel.getDocumentElement();
			Element out = c.getTraceModel().getDocumentElement();
			return ((rejectedProducts == c.getRejectedProducts()) && (finishedBatches == c.getFinishedBatches())
					&& cur.isEqualNode(out));
		}

		@Override
		public int hashCode() {
			return Objects.hash(rejectedProducts, finishedBatches, traceabilityModel);
		}
	}

	/** Identifier of the agent. */
	Order_Agent myAgent;

	/** Identifier of the monitor request template. */
	MessageTemplate inforequestTemplate;

	/** Identifier of the baych traceability template. */
	MessageTemplate traceabilityTemplate;

	/** Identifier of the ddra response template. */
	MessageTemplate ddraTemplate;

	/** Identifier of the current state of the batch. */
	orderState curState;

	/** Identifier of the previous state of the batch. */
	orderState prevState;

	public Order_Functionality(Order_Agent agent) {
		LOGGER.entry("*** Constructing Order_Functionality ***");
		this.myAgent = agent;

		inforequestTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DATA),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		traceabilityTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DATA),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ddraTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_FAILURE),
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

		curState = new orderState(myAgent.productionName);
		prevState = new orderState(myAgent.productionName);

		String regParams = "order " + "name=" + (myAgent.productionName);
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
			return LOGGER.exit(null);
		} else {
			LOGGER.debug("State has changed");
			prevState.setAll(curState);
			return LOGGER.exit(curState);
		}
	}

	/**
	 * Updates the state of the agent.
	 */
	public void setState(Object state) {
		LOGGER.entry();
		try {
			orderState aux = (orderState) state;
			this.curState.setAll(aux);
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

		if (msg == null) {
			LOGGER.debug("No messages");
			return LOGGER.exit(null);
		} else if (traceabilityTemplate.match(msg)) {
			LOGGER.debug(msg);
			Document content = null;
			try {
				content = (Document) msg.getContentObject();
			} catch (UnreadableException e) {
				LOGGER.info("Incorrect msg content. object expected");
				return LOGGER.exit(null);
			}

			Element batchRoot = content.getDocumentElement();
			Node importProduct = curState.traceabilityModel.importNode(batchRoot, true);
			curState.traceabilityModel.getDocumentElement().appendChild(importProduct);
			curState.setFinishedBatches(curState.getFinishedBatches() + 1);

		} else if (inforequestTemplate.match(msg)) {
			return LOGGER.exit(createMsg(ONT_DATA, msg.getConversationId(), ACLMessage.INFORM, curState.getTraceModel(),
					msg.getSender()));
		} else if (ddraTemplate.match(msg)) {
			LOGGER.debug(msg);
			curState.setRejectedProducts(0);
			myAgent.batchNumber = myAgent.batchNumber + 1;
		}
		LOGGER.debug("Finished batches: " + curState.getFinishedBatches() + "/" + myAgent.batchNumber);
		if (curState.getFinishedBatches() == myAgent.batchNumber) {
			if (curState.getRejectedProducts() == 0) {
				LOGGER.info("Rejected " + curState.getRejectedProducts() + " batches");
				return LOGGER.exit(createMsg(ONT_CONTROL, ACLMessage.REQUEST,
						ControlBehaviour.CMD_SETSTATE + " " + ControlBehaviour.ST_STOP, myAgent.getAID()));
			} else {
				LOGGER.info("Inform DDRA");
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
				return LOGGER
						.exit(createMsg(ONT_CONTROL, ACLMessage.REQUEST, curState.getRejectedProducts(), targetAID));
			}
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
		String target = null;
		// If there are no response, repeat request.
		while (target == null) {
			target = myAgent.getInfoMWM("*", "service=" + ACT_PLANNER);
		}
		AID receiver = new AID(target, AID.ISLOCALNAME);
		return LOGGER.exit(createMsg(ONT_DATA, ACLMessage.INFORM, curState.getTraceModel(), receiver));
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

}
