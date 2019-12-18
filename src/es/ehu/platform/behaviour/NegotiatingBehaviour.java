package es.ehu.platform.behaviour;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.SystemModelAgent;
import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.NegFunctionality;
import es.ehu.platform.utilities.Cmd;
import es.ehu.platform.utilities.MasReconAgent;
import es.ehu.platform.utilities.MsgNegotiation;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import static es.ehu.platform.utilities.MasReconOntologies.ONT_NEGOTIATE;


/**
 * Manages several simultaneous negotiations, differentiating the execution
 * status of each one by the conversationID.
 **/
public class NegotiatingBehaviour extends SimpleBehaviour {
  

  public static final int NEG_LOST=0, NEG_PARTIAL_WON=1, NEG_WON=2, NEG_RETRY=-1, NEG_FAIL=-2;
  
	private static class NegotiationData {

		/** Identifier of the task which is being negotiated. */
		private String taskId;

		/** Identifier of the agents which request the negotiation */
		private AID requester;

		private MsgNegotiation negotiationMsg;

		/** Scalar value used to negotiate. */
		private long scalarValue;

		/** Agent counter. Checks the number of agents that have already negotiated. */
		private int repliesCnt;
		
		//TODO: hora de mensaje
		private long timeStamp = 0;

		/** Flag to indicate if the negotiation is tied */
		private boolean flagTie;

		public NegotiationData(AID requester, MsgNegotiation negotiationMsg) {
			this.requester = requester;
			this.negotiationMsg = negotiationMsg;
			this.repliesCnt = 0;
			this.scalarValue = -1;
			this.flagTie = false;
			this.taskId = negotiationMsg.getTaskID();
			
			this.timeStamp = System.currentTimeMillis();
		}

		// Setter methods
		public void setScalarValue(long a) {
			this.scalarValue = a;
		}

		// Getter methods
		public long getScalarValue() {
			return this.scalarValue;
		}

		public String getTaskId() {
			return this.taskId;
		}

		public AID getRequester() {
			return this.requester;
		}

		public AID[] getTargets() {
			return this.negotiationMsg.getTargets();
		}

		public String getAction() {
			return this.negotiationMsg.getNegAction();
		}

		public String getCriterion() {
			return this.negotiationMsg.getCriterion();
		}

		public Object[] getExternalData() {
			return this.negotiationMsg.getExternalData();
		}

		public boolean getFlagTie() {
			return this.flagTie;
		}

		// Other methods
		public void cntReplies() {
			this.repliesCnt++;
		}

		// count a reply and returns true if every target for the current negotiation has sent a proposal
		public boolean checkReplies() {
			return (repliesCnt >= (negotiationMsg.getTargets().length - 1)) ? true : false;
		}

		public void activateFlagTie() {
			this.flagTie = true;
		}

	}

	private static final long serialVersionUID = 5211311085804151394L;
	static final Logger LOGGER = LogManager.getLogger(NegotiatingBehaviour.class.getName());
	private static final String CMD_REFUSE = "Refuse";

	private MWAgent myAgent;
	private NegFunctionality aNegFunctionality;

	/**
	 * Pattern of ACL messages that this agent uses to filter its
	 * {@code MessageQueue}.
	 */
	private MessageTemplate template;

	/**
	 * Object to store all the information needed to execute simultaneous
	 * negotiation at the same time, using the key value to differenciate them.
	 * <p>
	 * <ul>
	 * <li><b>key</b>: ConversationID to identify the negotiation in process.</li>
	 * <li><b>value</b>: Data needed to process the negotiation.</li>
	 * </ul>
	 */
	private ConcurrentHashMap<String, NegotiationData> negotiationRuntime = new ConcurrentHashMap<String, NegotiationData>();

	public NegotiatingBehaviour(MWAgent a) {
		super(a);
		LOGGER.entry(a);
		this.myAgent = a;
		this.aNegFunctionality = (NegFunctionality) a.functionalityInstance;
		template = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_NEGOTIATE), //es negociación Y (
		    MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CFP), //o es cfp
		        MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE), // o fp
		            //MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), // o req
		                MessageTemplate.MatchPerformative(ACLMessage.FAILURE)))); // o fail
		LOGGER.exit();
	}

	/**
	 * Prepares the negotiation value.
	 */
	public void onStart() {
		LOGGER.entry();

		LOGGER.exit();
	}

	public void action() {
		LOGGER.entry();
	
		ACLMessage msg = myAgent.receive(template);
		
		if (msg != null) {
		  
		  String conversationId = msg.getConversationId();

		  // recibo petición de negociación
		  if (msg.getPerformative() == ACLMessage.CFP) {
			  LOGGER.info("msg="+msg.getContent());
			  Cmd cmd = new Cmd(msg.getContent());     
	       //TODO copiar a punto común el processAttibs
			  MsgNegotiation negMsg = new MsgNegotiation((Iterator<AID>) msg.getAllReceiver(), conversationId, cmd.attribs.get("action"), cmd.attribs.get("criterion"));
	      initNegotiation(conversationId, msg.getSender(), negMsg);

		  } else 
			  // Recibo propuestas
			  if (msg.getPerformative() == ACLMessage.PROPOSE) {
				if (negotiationRuntime.containsKey(conversationId)) {
					Long receivedVal = new Long(0);
					try {
						receivedVal = (Long) msg.getContentObject();
						LOGGER.debug("Proposal content: " + receivedVal);
					} catch (Exception e) {
						LOGGER.debug("Received value is not a number");
					}
					
					LOGGER.info(msg.getSender().getLocalName()+"("+receivedVal+") ");
					negotiationRuntime.get(conversationId).cntReplies();
					
//					if (receivedVal>negotiationRuntime.get(conversationId).getScalarValue()) { //recibo valor mayor (mejor) > pierdo
//					  System.out.println("> "+myAgent.getLocalName()+"("+negotiationRuntime.get(conversationId).getScalarValue()+") pierde negociación "+conversationId);

					// TODO: parámetro de desempate - deberá estar dentro del checkNegotiation (debe ser plataforma)
					boolean tieBreak = msg.getSender().getLocalName().compareTo(myAgent.getLocalName())>0;

					// TODO: Vienen en el mensaje de negociación o los consulto al sA?
					String seID = "applic101";//(String)negExternalData[0];//
					String seType = "applicationSet";//(String)negExternalData[1];//seType
			    String seClass = "es.ehu.domain.orion2030.templates.ApplicationSetTemplate";//(String)negExternalData[2];//
			    String seFirstTransition = "running";//(String)negExternalData[3]; //
			    
					switch (aNegFunctionality.checkNegotiation(conversationId, negotiationRuntime.get(conversationId).getAction(), receivedVal, 
              negotiationRuntime.get(conversationId).getScalarValue(),tieBreak, negotiationRuntime.get(conversationId).checkReplies(),
              seID, seType, seClass, seFirstTransition)) {
					
    					case NEG_LOST: //he perido la negociación  
    					  LOGGER.info("> "+myAgent.getLocalName()+"("+negotiationRuntime.get(conversationId).getScalarValue()+") pierde negociación "+conversationId);
    					  negotiationRuntime.remove(conversationId); //salgo de esta negociación
    					  break;
    					  
    					case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
    					  //TODO: mandar repetir al que lo invoca
    					  negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
    					  break;
    					  
    					case NEG_WON: //he ganado la negociación y termina correctamente
    					// informar al del cfp-er que la negociación ha finalizado y hemos ganado
    					  negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
    					  
    					case NEG_FAIL:
    					  // informar al del cfp-er que la negociación no ha podido ser completada
    					  negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
    					  
    					  break;
					} 
					
				} else // !negotiationRuntime.containsKey(conversationId) 
				  LOGGER.debug("message " + msg.getConversationId() + "is not for me"); //estoy fuera de esta negociación porque ya la he perdido

			} else if (msg.getPerformative() == ACLMessage.FAILURE) {
				LOGGER.info("Received FAILURE message with convID:" + conversationId);
				if (negotiationRuntime.get(conversationId) != null) {
				  negotiationRuntime.get(conversationId).cntReplies();
				  
//  Remove this negotiationRuntimeData
//					if (checkNegotiation(negotiationRuntime.get(conversationId), conversationId)) {
//						negotiationRuntime.remove(conversationId);
//					}
				  
				}
				try {
					String name = msg.getContent().substring(msg.getContent().indexOf(":name ", msg.getContent().indexOf("MTS-error"))+ ":name ".length());
					LOGGER.warn(name.substring(0, name.indexOf('@')) + " FAILURE");
				} catch (Exception e) {
				}
			}
		} else {
			block();
		}
		LOGGER.exit();
	}

	/**
	 * Create a newNegotiationData, calculate the own negotiationValue and saves
	 * NewData in the negotiationRuntime Create the Propose msg with the calculated
	 * scalarValue and send it to other targets.
	 * 
	 * @param negId     ConversationId (negotiationID)
	 * @param requester AID of the requester agent
	 * @param negMsg    necessary information to start the negotiation
	 */
	private void initNegotiation(String negId, AID requester, MsgNegotiation negMsg) {
		LOGGER.entry();

		ACLMessage cfp = new ACLMessage(ACLMessage.PROPOSE);
		cfp.setOntology(ONT_NEGOTIATE);
		cfp.setConversationId(negId);
		LOGGER.debug("Targets lenght: " + negMsg.getTargets().length);
		for (AID id : negMsg.getTargets()) {
			// Removes the own agent from the list
			if (!id.getLocalName().equals(myAgent.getLocalName())) {
				cfp.addReceiver(id);
			}
		}

		long value = aNegFunctionality.calculateNegotiationValue(negMsg.getNegAction(), negMsg.getCriterion(), negMsg.getExternalData());
		
		
		
		LOGGER.info("Negotiation value: " + value);
		
		try {
			cfp.setContentObject(new Long(value));
		} catch (Exception e) {
			LOGGER.error("Negotiation content in " + myAgent.getLocalName() + " could not be sent!! - " + negId);
		}
		myAgent.send(cfp);
		LOGGER.debug("Sent Negotiation Propose msg");
		NegotiationData newNegotiation = new NegotiationData(requester, negMsg);
		newNegotiation.setScalarValue(value);
		negotiationRuntime.put(negId, newNegotiation);
		
		// Check if the agent is alone in the negotiation and has won
//		if (checkNegotiation(negotiationRuntime.get(negId), negId)) {
//			negotiationRuntime.remove(negId);
//		}
		LOGGER.exit();
	}

	/**
	 * Check negotiation result: 1- check if every target responded 2- check
	 * negotiation result: if(tie || lost) send refuse else send inform (won)
	 * 
	 * @param neg   NegotiationData linked to a defined negotiation id
	 * @param negId ConversationId (negotiationID)
	 * @return
	 */
//	private boolean checkNegotiation(NegotiationData neg, String negId) {
//		LOGGER.entry();
//		if (neg.checkReplies()) {
//			ACLMessage cfp;
//
//			if (neg.getFlagTie()) {
//				cfp = new ACLMessage(ACLMessage.REFUSE);
//				cfp.setContent(CMD_REFUSE);
//				LOGGER.info("Won negotiation" + negId + ", but the negotiation is refused due to TIE");
//			} else {
//				Object cmdPar = null;//aNegFunctionality.checkNegotiation(neg.getTaskId(), neg.getAction(), neg.getCriterion(),
//						//neg.getScalarValue(), neg.getRequester(), neg.getExternalData());
//				if (cmdPar == null) {
//					cfp = new ACLMessage(ACLMessage.REFUSE);
//					cmdPar = (Object) CMD_REFUSE;
//					LOGGER.info("Won negotiation" + negId + ", but the negotiation is refused with msg: " + CMD_REFUSE);
//				} else {
//					cfp = new ACLMessage(ACLMessage.INFORM);
//					LOGGER.info("Won negotiation " + negId + " and sent information: " + cmdPar);
//				}
//				try {
//					cfp.setContentObject((Serializable) cmdPar);
//				} catch (Exception e) {
//					LOGGER.debug("Negotiation content in " + myAgent.getLocalName() + " could not be sent!!");
//				}
//			}
//			cfp.setOntology(ONT_NEGOTIATE);
//			cfp.setConversationId(negId);
//			cfp.addReceiver(neg.getRequester());
//			myAgent.send(cfp);
//			return LOGGER.exit(true);
//		}
//		return LOGGER.exit(false);
//	}

	/**
	 * Called when new negotiation propose arrives. Check if a negotiation was lost
	 * or Tie
	 * 
	 * @param receivedVal Proposed value from another agent
	 * @param neg         NegotiationData linked to a defined negotiation id
	 * @param negId       ConversationId (negotiationID)
	 * @return
	 */
//	private boolean negotiation(Double receivedVal, NegotiationData neg, String negId) {
//		LOGGER.entry();
//		neg.cntReplies();
//		long myVal = neg.getScalarValue();
//		if (receivedVal < myVal) {
//			LOGGER.info("Negotiation: " + negId + " was lost");
//			return true;
//		} else if (receivedVal == myVal) {
//			neg.activateFlagTie();
//		}
//
//		String result = (String)aNegFunctionality.checkNegotiation(negId, "winnerAction", receivedVal, neg.getScalarValue(), "class", "ft", "seType");
//		
//		if (result!=null) {
//		  System.out.println(result);
//		  return true; 
//		}
//		else System.out.println("returns null");
//		
//		return false;
//		
//		//return LOGGER.exit(checkNegotiation(neg, negId));
//	}
	


	@Override
	public boolean done() {
		return false;
	}

	public int onEnd() {
		return 0;
	}
}
