package es.ehu.platform.behaviour;

import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.AvailabilityFunctionality;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static es.ehu.platform.utilities.MasReconOntologies.ONT_NEGOTIATE;

//import jade.util.leap.ArrayList;

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

	private MessageTemplate template,template2,confirmation_required;
	private MWAgent myAgent;
	private int PrevPeriod;
	private long NextActivation;
	private Boolean endFlag=false;
	private AID QoSID = new AID("QoSManagerAgent", false);
	private AID DDID = new AID("D&D", false);
	private boolean agent_block_flag =false;
	private boolean update_replicas=false;
	// Constructor. Create a default template for the entry messages
	public RunningBehaviour(MWAgent a) {
		super(a);
		LOGGER.debug("*** Constructing RunningBehaviour ***");
		this.myAgent = a;
//		this.template = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DATA),
//				MessageTemplate.MatchPerformative(ACLMessage.INFORM));



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
		this.template=
				//templates batch
				MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.or(MessageTemplate.MatchOntology("askdelay"),
								MessageTemplate.or(MessageTemplate.MatchContent("reset_timeout"),
										MessageTemplate.or(MessageTemplate.MatchOntology("data"),
											MessageTemplate.or(MessageTemplate.MatchOntology("rebuild_finish_times"),
													//templates order
													MessageTemplate.or(MessageTemplate.and(MessageTemplate.MatchOntology("Information"),MessageTemplate.MatchConversationId("ItemsInfo")),
															MessageTemplate.or(MessageTemplate.and(MessageTemplate.MatchContent("Batch completed"),MessageTemplate.MatchConversationId("Shutdown")),
																	MessageTemplate.or(MessageTemplate.MatchOntology("update_timeout"),
																			MessageTemplate.or(MessageTemplate.MatchOntology("delay"),
																				MessageTemplate.or(MessageTemplate.MatchOntology("take_down_order_timeout"),
																						//templates mplan
																						MessageTemplate.or(MessageTemplate.and(MessageTemplate.MatchOntology("Information"),MessageTemplate.MatchConversationId("OrderInfo")),
																								MessageTemplate.and(MessageTemplate.MatchContent("Order completed"),MessageTemplate.MatchConversationId("Shutdown"))
																						)))))))))));

		this.template2 = MessageTemplate.and(MessageTemplate.MatchOntology("release_buffer"),
				MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),MessageTemplate.MatchSender(DDID)));
		this.confirmation_required=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.or(MessageTemplate.and(MessageTemplate.MatchOntology("data"),MessageTemplate.MatchConversationId("PLCdata")),
						MessageTemplate.or(MessageTemplate.and(MessageTemplate.MatchOntology("Information"),MessageTemplate.MatchConversationId("ItemsInfo")),
								MessageTemplate.and(MessageTemplate.MatchOntology("Information"),MessageTemplate.MatchConversationId("OrderInfo")))));
		if(!myAgent.ExecTimeStamped){
			myAgent.get_timestamp(myAgent,"ExecutionTime");
			myAgent.ExecTimeStamped=true;
		}

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

		//****************** 1) Generación de acknowledges
		ACLMessage msg_asking_confirmation=myAgent.receive(confirmation_required);
		if(msg_asking_confirmation!=null){ //si se recibe un mensaje que sea necesario contestar mandamos un acknowledge y volvemos a meter el mensaje a la cola
			myAgent.Acknowledge(msg_asking_confirmation,myAgent);
			DateFormat simple = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			System.out.print("Answered: ");
			Date result = new Date(System.currentTimeMillis());
			System.out.println(simple.format(result)); //printea el instante de respuesta
			myAgent.putBack(msg_asking_confirmation);
		}
		//****************** Fin de generación de acknowledge

		//****************** 2) Etapa de checkeo para mensajes retenidos por falta de disponibilidad de agentes
		//comprueba si hay mensajes que permitan liberar los mensajes retenidos por el agente

		ACLMessage new_target= myAgent.receive(template2);
		if(new_target!=null){     //D&D avisa de que ya se puede vaciar el buffer de mensajes
			if(new_target.getContent().contains("orderagent")||new_target.getContent().contains("mplanagent")){ //nuevo agente de aplicación disponible para recibir el aviso
				ACLMessage parent= myAgent.sendCommand("get "+new_target.getContent()+" attrib=parent"); //se ha registrado el parent como key para los agentes de aplicación
				ArrayList<ACLMessage> postponed_msgs=new ArrayList<ACLMessage>();
				postponed_msgs=myAgent.msg_buffer.get(parent.getContent()); //se obtiene el listado de mensajes pendientes para el agente
				if(postponed_msgs!=null){
					for(int i=0; i<postponed_msgs.size();i++){
						ACLMessage msg_to_release=new ACLMessage(postponed_msgs.get(i).getPerformative());
						msg_to_release.setContent(postponed_msgs.get(i).getContent());
						msg_to_release.setOntology(postponed_msgs.get(i).getOntology());
						msg_to_release.setConversationId(postponed_msgs.get(i).getConversationId());
						msg_to_release.addReceiver(new AID(new_target.getContent(),false));
						myAgent.send(msg_to_release);
					}
					myAgent.msg_buffer.remove(parent.getContent());
				}else{
					LOGGER.error("Received a release buffer petition while not having the target registered on msg buffer");
				}
			}else{   //si no es un agente de apliación será de otro tipo
				LOGGER.error("No programmed function for this agent");
			}
		}
		//****************** Fin de etapa de checkeo para mensajes retenidos por falta de disponibilidad de agentes


		//****************** 3) Etapa de checkeo de mensajes de acknowledge
		//Se recorre el queue de mensajes para comprobar si todos los mensajes que esperaban respuesta la han obtenido a tiempo
		for(int i=0;i<myAgent.expected_msgs.size();i++){
			Object[] exp_msg;
			exp_msg=myAgent.expected_msgs.get(i);
			ACLMessage complete_msg=(ACLMessage) exp_msg[0];
			jade.util.leap.Iterator itor = complete_msg.getAllReceiver();
			AID exp_msg_sender= (AID)itor.next();   //usa el iterador para obtener el AID de el receptor original del mensaje (solo debería ser uno)
			String convID=complete_msg.getConversationId();
			String content=complete_msg.getContent();
			long timeout=(long) exp_msg[1]; //obtiene el tiempo limite para recibir la confirmación de mensaje
			Date date = new Date();
			long instant = date.getTime();
			MessageTemplate ack_template=MessageTemplate.and(MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchConversationId(convID),
					MessageTemplate.MatchSender(exp_msg_sender)),
					MessageTemplate.MatchContent(content)),MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));
			ACLMessage ack= myAgent.receive(ack_template);
			if(ack==null){
				if(instant>timeout){
					System.out.println("No confirmation from: "+exp_msg_sender.getLocalName()+"\n"+"Sended message content: "+content);
					if(exp_msg_sender.getLocalName().equals(QoSID.getLocalName())){
						LOGGER.info("QoS did not answer on time. THIS AGENT MIGHT BE ISOLATED.");
						if(myAgent.getLocalName().contains("batchagent")||myAgent.getLocalName().contains("orderagent")||myAgent.getLocalName().contains("mplanagent")){
							System.exit(0); //es un agente de aplicacion, por lo que se "suicida" si esta aislado. El nodo completo es eliminado.
						}else{ //TODO añadir aquí agentes no contemplados cuando proceda

						}
					}else{
						String sender_on_msgbuffer="";
						LOGGER.info("Expected answer did not arrive on time.");
						if(exp_msg_sender.getLocalName().contains("batchagent")||exp_msg_sender.getLocalName().contains("orderagent")||exp_msg_sender.getLocalName().contains("mplanagent")){  //si el mensaje era para el agente batch se asigna el parent en el buffer
							ACLMessage parent_name= myAgent.sendCommand("get "+exp_msg_sender.getLocalName()+" attrib=parent");
							sender_on_msgbuffer=parent_name.getContent();
						}else{
							sender_on_msgbuffer=exp_msg_sender.getLocalName();
						}
						ArrayList<ACLMessage> postponed_msgs=new ArrayList<ACLMessage>();
						postponed_msgs=myAgent.msg_buffer.get(sender_on_msgbuffer); //por si habia algún mensaje anteriormente
						if(postponed_msgs==null){
							postponed_msgs=new ArrayList<ACLMessage>();
							postponed_msgs.add(complete_msg);  //como vamos a denunciar a este agente debemos guardar el mensaje para enviarselo a su sustituto
						}else{
							postponed_msgs.add(complete_msg);
						}
						myAgent.msg_buffer.put(sender_on_msgbuffer,postponed_msgs);  //se añade el mensaje a la lista de espera para enviarlo cuando el D&D nos confirme que existe un nuevo destinatario
						LOGGER.debug("Añadido mensaje a la lista de espera: "+complete_msg);
						String report=exp_msg_sender.getLocalName()+"/div/"+content;
						ACLMessage reported_msg= sendACLMessage(6, QoSID, "acl_error", convID, report, myAgent); //se denuncia al QoS el agnete que no respondía.
						myAgent.AddToExpectedMsgs(reported_msg); //se esperará tambien respuesta del QoS.
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

		//***************** 5) Etapa de ejecución de funtionality
		ACLMessage msg = myAgent.receive(template);
		if(msg!=null){
			update_replicas=true;
			myAgent.msgFIFO.add((String) msg.getContent());
		}
		receivedMsgs = manageReceivedMsg(msg);

		Object result = myAgent.functionalityInstance.execute(receivedMsgs);
		if(!agent_block_flag){ //Si previamente se ha puesto el flag de agent_block_flag en 1 significa que tenemos mensajes pendientes de recibir o enviar no debemos tocar el flag de end
			endFlag =Boolean.valueOf(result.toString());
			manageExecutionResult(result);
		}else{
			if(myAgent.msg_buffer.isEmpty()&&myAgent.expected_msgs.size()==0){
				endFlag=true;
				result=true;
				agent_block_flag =false;
				manageExecutionResult(result);
			}
		}
		if(endFlag&&(!myAgent.msg_buffer.isEmpty()||myAgent.expected_msgs.size()!=0)){ //en caso de que el agente haya terminado pero aun conserve mensajes pendientes de recibir o enviar hay que evitar que desaparezca
			//endFlag=false;
//			result=false;
			agent_block_flag =true;
		}

		//****************** 4) Etapa de actualización de replicas
		// Consigue el estado actual de la replica cuando se recibe cualquier mensaje y se devuelve al queue.
//		String currentState = null;
//		ACLMessage any_msg = myAgent.receive();
//		if(any_msg!=null){
//			myAgent.msgFIFO.add((String) any_msg.getContent());
////			System.out.println("Peeked msg: "+any_msg.getContent()); //para visualizar que mensaje es el que dispara el getstate
//			System.out.println("From: "+any_msg.getSender().getLocalName());
//			if(!any_msg.getContent().equals("done")&&!any_msg.getOntology().equals("trigger_getState")){ //"flushea" mensajes de tipo done y de trigger para evitar bucles porque estos nadie los lee
//				myAgent.putBack(any_msg);  //en caso de no serlo, se devuelve al queue de mensajes ACL
//			}
//			if(!myAgent.antiloopflag) { //el flag de antiloop evita bucles infinitos acotando un tramo de código
//				currentState = (String) ((AvailabilityFunctionality) myAgent.functionalityInstance).getState();
//				if (currentState != null) {
//					LOGGER.debug("Send state");
//					myAgent.sendStateToTracking(currentState);
//				}
//			}
//		}
		ACLMessage msg_to_flush= myAgent.receive(MessageTemplate.or(MessageTemplate.MatchContent("done"),MessageTemplate.MatchOntology("trigger_getState")));
		if(msg_to_flush!=null){
			if(msg_to_flush.getOntology().equals("trigger_getState")){
				update_replicas=true;
			}
		}

		String currentState = null;
		if(update_replicas){

			if(!myAgent.antiloopflag) { //el flag de antiloop evita bucles infinitos acotando un tramo de código con él
				currentState = (String) ((AvailabilityFunctionality) myAgent.functionalityInstance).getState();
				if (currentState != null) {
					LOGGER.debug("Send state");
					if(myAgent.getLocalName().contains("batchagent")){
						myAgent.sendStateToTracking(currentState,"batch");
					}else if(myAgent.getLocalName().contains("orderagent")){
						myAgent.sendStateToTracking(currentState,"order");
					}else{
						myAgent.sendStateToTracking(currentState,"mplan");
					}
				}
			}
			update_replicas=false;
		}

		//****************** Fin de etapa de actualización de replicas

		//***************** Fin de etapa de ejecución de funtionality

		long t = manageBlockingTimes();

		if (msg == null&&myAgent.expected_msgs.size()==0) {
			LOGGER.debug("Block time: " + t);
			block(t);
		}

		// TODO prueba para ver el cambio de estados --> luego borrar
//		System.out.println("El agente " + myAgent.getLocalName() + " esta en el metodo action del RunningBehaviour");

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
//			LOGGER.debug("Execute result is not an ACLMessage");
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
//				LOGGER.debug("Send Message to source components");
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
	public ACLMessage sendACLMessage(int performative, AID reciever, String ontology, String conversationId, String content, Agent agent) {

		ACLMessage msg = new ACLMessage(performative); //envio del mensaje
		msg.addReceiver(reciever);
		msg.setOntology(ontology);
		msg.setConversationId(conversationId);
		msg.setContent(content);
		myAgent.send(msg);
		return msg;
	}


}