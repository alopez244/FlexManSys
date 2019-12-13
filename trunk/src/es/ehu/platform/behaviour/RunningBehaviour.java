package es.ehu.platform.behaviour;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.platform.MWAgent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import es.ehu.platform.template.interfaces.*;

import static es.ehu.platform.utilities.MasReconOntologies.ONT_DATA;

/**
 * This behaviour receives messages from the templates used in the constructor
 * and execute the functionality to perform its activity.
 * <p>
 * There are two possible cases:
 * <ul>
 * <li>If the MWAgent has any {@code sourceComponentIDs}, then it will send to
 * the execute functionality the contentObject. Finally, the returned value is
 * sent as content to the {@code targetComponentsIDs}</li>
 * <li>If the MWAgent does not have any {@code sourceComponentIDs}, then it will
 * send to the execute functionality the ACLMessage. Finally, the returned value
 * (ACLMessage) is sent.</li>
 * </ul>
 * <p>
 * <b>NOTE:</b> The transition to another state is done using a message to a
 * {@code ControlBehaviour}
 * 
 * @author Brais Fortes (@fortes23) - Euskal Herriko Unibersitatea
 * @author Mikel Lopez (@lopeziglesiasmikel) - Euskal Herriko Unibersitatea
 **/
public class RunningBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 3456578696375317772L;

	static final Logger LOGGER = LogManager.getLogger(RunningBehaviour.class.getName());

	private MessageTemplate template;
	private MWAgent myAgent;
	private int PrevPeriod;
	private long NextActivation;

	// Constructor. Create a default template for the entry messages
	public RunningBehaviour(MWAgent a) {
		super(a);
		LOGGER.debug("*** Constructing RunningBehaviour ***");
		this.myAgent = a;
		this.template = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DATA),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
	}

	// Constructor. Use the template received as parameter
	public RunningBehaviour(MWAgent a, MessageTemplate template) {
		super(a);
		this.myAgent = a;
		LOGGER.debug("*** Constructing RunningBehaviour ***");
		this.template = template;
	}

	public void onStart() {
		LOGGER.entry();
		this.PrevPeriod = myAgent.period;
		if (myAgent.period < 0) {
			this.NextActivation = -1;
		} else {
			this.NextActivation = myAgent.period + System.currentTimeMillis();
		}
		LOGGER.exit();
	}

	public void action() {
		LOGGER.entry();
		Object[] receivedMsgs = null;

		ACLMessage msg = myAgent.receive(template);
		receivedMsgs = manageReceivedMsg(msg);

		Object result = myAgent.functionalityInstance.execute(receivedMsgs);

		manageExecutionResult(result);

		Serializable state = null;
		try {
			state = (Serializable) ((AvailabilityFunctionality)myAgent.functionalityInstance).getState();
		} catch (Exception e) {
			LOGGER.debug("GetState is returning a non-serializable object");
		}
		if (state != null) {
			LOGGER.debug("Send state");
			myAgent.sendState(state);
		}

		long t = manageBlockingTimes();

		if (msg == null) {
			LOGGER.debug("Block time: " + t);
			block(t);
		}
		LOGGER.exit();
	}

	public int onEnd() {
		return 0;
	}

	@Override
	public boolean done() {
		return false;
	}

	/**
	 * Analyze the message and check which condition has the agent.
	 * <ul>
	 * <li>If it has {@code sourceComponentIDs}, the message should be from one of
	 * these agents.</li>
	 * <li>Otherwise, the message will be returned</li>
	 * </ul>
	 * 
	 * @param msg ACLMessage received in the {@code MessageQueue}
	 * @return Content of the message if there are any {@code sourceComponentIDs},
	 *         ACLMessage if {@code msg} is different from null or null otherwise
	 * 
	 */
	private Object[] manageReceivedMsg(ACLMessage msg) {
		LOGGER.entry(msg);
		if (msg != null) {
			LOGGER.debug("Message received from: " + msg.getSender().getLocalName());
			if (myAgent.sourceComponentIDs != null && myAgent.sourceComponentIDs.length > 0) {
				String senderCmp = myAgent.getComponent(msg.getSender().getLocalName());
				LOGGER.debug("senderCmp = " + senderCmp);
				buscar: for (int i = 0; i < myAgent.sourceComponentIDs.length; i++) {
					if (senderCmp == null) {
						break buscar;
					}
					LOGGER.info(senderCmp + " checked with " + myAgent.sourceComponentIDs[i]);
					if ((myAgent.sourceComponentIDs[i]).contains(senderCmp)) {
						LOGGER.trace("found " + senderCmp);
						try {
							return new Object[] {msg.getContentObject()};
						} catch (UnreadableException e) {
							LOGGER.debug("Received message without an object in its content");
							e.printStackTrace();
						}
						break buscar;
					}
				}
			} else {
				LOGGER.debug("Received message in an agent withou sourceComponentIDs");
				return LOGGER.exit(new Object[] {msg});
			}
		}
		return LOGGER.exit(null);
	}

	/**
	 * Manages a result value, checking if it is serializable or an
	 * {@code ACLMessage}. In the first case, message is sent to the
	 * {@code targeComponentIDs} of the MWAgent. In the second one, message is sent.
	 * 
	 * @param result Value to send to other agents.
	 */
	private void manageExecutionResult(Object result) {
		LOGGER.entry(result);
		MessageTemplate templateAny = MessageTemplate.MatchAll();
		ACLMessage resultMsg = null;
		try {
			resultMsg = (ACLMessage) result;

		} catch (Exception e) {
			LOGGER.debug("Execute result is not an ACLMessage");
		}

		if (resultMsg != null && templateAny.match(resultMsg)) {
			try {
				myAgent.send(resultMsg);
				LOGGER.debug("Send ACLMessage received in the manageExecutionResult");
			} catch (Exception e) {
				LOGGER.info("ACLMessage is not complete and generates errors");
			}
		} else {
			try {
				Serializable resultSer = (Serializable) result;
				LOGGER.debug("Send Message to source components");
				myAgent.sendMessage(resultSer, myAgent.targetComponentIDs);
			} catch (Exception e) {
				LOGGER.debug("Execute result is not serializable");
			}
		}
		LOGGER.exit();
	}

	/**
	 * Calculates the blocking times, checking if the agent is periodic.
	 * 
	 * @return blocking time (periodic) or 0 (not periodic).
	 */
	private long manageBlockingTimes() {
		LOGGER.entry();
		long t = 0;
		if (PrevPeriod != myAgent.period) {
			NextActivation = System.currentTimeMillis() + myAgent.period;
			PrevPeriod = myAgent.period;
			LOGGER.debug("Restarting period due to change of period");
			return (long) LOGGER.exit(myAgent.period);
		}
		if ((myAgent.period < 0)) {
			return (long) LOGGER.exit(0);
		} else {
			t = NextActivation - System.currentTimeMillis();
			if (t <= 0) {
				LOGGER.debug("Restarting period due to cycle");
				NextActivation = System.currentTimeMillis() + myAgent.period;
				t = myAgent.period;
			}
		}
		return LOGGER.exit(t);
	}
}