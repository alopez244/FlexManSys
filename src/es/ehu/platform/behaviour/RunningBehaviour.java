package es.ehu.platform.behaviour;

import java.io.Serializable;

//import jade.util.leap.ArrayList;

import java.util.ArrayList;
import java.util.Date;

import jade.core.AID;
import jade.core.Agent;
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
	private Boolean endFlag;
	private AID QoSID = new AID("QoSManagerAgent", false);
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
		myAgent.ActualState="running";
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


		//****************** Etapa de checkeo de mensajes de acknowledge
		for(int i=0;i<myAgent.expected_msgs.size();i++){
			Object[] exp_msg;
			exp_msg=myAgent.expected_msgs.get(i);
			String sender=(String) exp_msg[0];
			AID exp_msg_sender=new AID(sender,false);
			String convID=(String) exp_msg[1];
			String content=(String) exp_msg[2];
			long timeout=(long) exp_msg[3];
			Date date = new Date();
			long instant = date.getTime();
			MessageTemplate ack_template=MessageTemplate.and(MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchConversationId(convID),
					MessageTemplate.MatchSender(exp_msg_sender)),
					MessageTemplate.MatchContent(content)),MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));
			ACLMessage ack= myAgent.receive(ack_template);
			if(ack==null){
				if(instant>timeout){
					if(exp_msg_sender.getLocalName().equals(QoSID.getLocalName())){
						LOGGER.info("QoS did not answer on time. THIS AGENT MIGHT BE ISOLATED.");
						if(myAgent.getLocalName().contains("batchagent")||myAgent.getLocalName().contains("orderagent")||myAgent.getLocalName().contains("mplanagent")){
							System.exit(0); //es un agente de aplicacion, por lo que se "suicida" si esta aislado
						}else{ //TODO añadir aquí agentes no contemplados cuando proceda
							LOGGER.debug("Condición no programada ");
						}
					}else{
						LOGGER.info("Expected answer did not arrive on time.");
						String report=sender+"/div/"+content;
						sendACLMessage(6, QoSID, "acl_error", convID, report, myAgent);
						AddToExpectedMsgs(QoSID.getLocalName(),convID,report);
					}
					myAgent.expected_msgs.remove(i);
					i--;
				}
			}else{
				myAgent.expected_msgs.remove(i);
				i--;
			}
		}
		//****************** Fin de etapa de checkeo de mensajes de acknowledge

		//****************** Etapa de actualización de replicas
		// Consigue el estado actual de la replica cuando se recibe cualquier mensaje. A través de un template se pueden filtrar.
		String currentState = null;
		ACLMessage any_msg = myAgent.receive();
		if(any_msg!=null){
			myAgent.putBack(any_msg);
			if(!myAgent.antiloopflag) { //el flag de antiloop evita bucles infinitos acotando un tramo de código
				currentState = (String) ((AvailabilityFunctionality) myAgent.functionalityInstance).getState();
				if (currentState != null) {
					LOGGER.debug("Send state");
					myAgent.sendStateToTracking(currentState);
				}
			}
		}
		//****************** Fin de etapa de actualización de replicas


		//***************** Etapa de ejecución de funtionality
		ACLMessage msg = myAgent.receive(template);
		receivedMsgs = manageReceivedMsg(msg);
		Object result = myAgent.functionalityInstance.execute(receivedMsgs);
		endFlag = Boolean.valueOf(result.toString());
		manageExecutionResult(result);
		//***************** Fin de etapa de ejecución de funtionality

//		Serializable state = null;
//		try {
//			state = (Serializable) ((AvailabilityFunctionality)myAgent.functionalityInstance).getState();
//
//		} catch (Exception e) {
//			LOGGER.debug("GetState is returning a non-serializable object");
//		}
//		if (state != null) {
//			LOGGER.debug("Send state");
//			myAgent.sendState(state);
//		}

		long t = manageBlockingTimes();

		if (msg == null&&myAgent.expected_msgs.size()==0) {
			LOGGER.debug("Block time: " + t);
			block(t);
		}

		// TODO prueba para ver el cambio de estados --> luego borrar
		System.out.println("El agente " + myAgent.getLocalName() + " esta en el metodo action del RunningBehaviour");

		LOGGER.exit();
	}

	public int onEnd() {

		return 0;
	}

	@Override
	public boolean done() {
		return endFlag;
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
				LOGGER.debug("Received message in an agent without sourceComponentIDs");
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
	public void sendACLMessage(int performative, AID reciever, String ontology, String conversationId, String content, Agent agent) {

		ACLMessage msg = new ACLMessage(performative); //envio del mensaje
		msg.addReceiver(reciever);
		msg.setOntology(ontology);
		msg.setConversationId(conversationId);
		msg.setContent(content);
		myAgent.send(msg);
	}
	public void AddToExpectedMsgs(String sender, String convID, String content){
		Object[] ExpMsg=new Object[4];
		ExpMsg[0]=sender;
		ExpMsg[1]=convID;
		ExpMsg[2]=content;
		Date date = new Date();
		long instant = date.getTime();
		instant=instant+2000; //añade una espera de 2 seg
		ExpMsg[3]=instant;
		myAgent.expected_msgs.add(ExpMsg);
	}

}