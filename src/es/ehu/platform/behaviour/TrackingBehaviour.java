package es.ehu.platform.behaviour;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.platform.MWAgent;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import es.ehu.platform.template.interfaces.*;

import java.util.ArrayList;

/**
 * This behaviour is a simple implementation of a message receiver.
 * It check if a satate is recive and stores it.
 * If the timeout expires before any message arrives, the behaviour
 * transition into a negotiation.
 * 
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/
public class TrackingBehaviour extends SimpleBehaviour {

  private static final long serialVersionUID = -1354067344628941600L;
  static final Logger LOGGER = LogManager.getLogger(TrackingBehaviour.class.getName()) ;

  private MWAgent myAgent;
	
	protected MessageTemplate template;
	protected MessageTemplate traceabilitybatch;
	protected MessageTemplate traceabilityorder;
	protected MessageTemplate traceabilitymplan;
	public ArrayList<String> Traceabilitybatch = new ArrayList<String>();
	public ArrayList<String> Traceabilityorder = new ArrayList<String>();
	public ArrayList<String> Traceabilitymplan = new ArrayList<String>();
	protected long timeout;

	private boolean expired;
	private int transitionFlag;
		
	/**
	 * Constructor.
	 * 
	 * @param a
	 *            a reference to the Agent
//	 * @param timeout
	 *            a timeout for waiting until a message arrives. It must
	 *            be expressed as an absolute time, as it would be returned by <code>System.currentTimeMillisec()</code>
	 **/
	public TrackingBehaviour(MWAgent a) {
		super(a);
		LOGGER.entry(a);
		myAgent = a;
		LOGGER.exit();
		
	}

	// For persistence service
	protected TrackingBehaviour() {
	}

	public void onStart(){
		template = MessageTemplate.MatchOntology("state");
		traceabilitybatch = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				MessageTemplate.MatchOntology("negotiation")),MessageTemplate.MatchConversationId("PLCdata"));
		traceabilityorder = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchOntology("Information")),MessageTemplate.MatchConversationId("ItemsInfo"));
		traceabilitymplan = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchOntology("Information")),MessageTemplate.MatchConversationId("OrderInfo"));
		timeout = 0;
		expired = false;
		transitionFlag = -1;
		LOGGER.debug("***************** inicio RunningBehaviour");
		LOGGER.debug("myAgent.period="+myAgent.period);
		
		
	}
	public void action() {
		LOGGER.entry();
			ACLMessage msg = myAgent.receive(template);
			ACLMessage msg2 = myAgent.receive(traceabilitybatch);
			ACLMessage msg3 = myAgent.receive(traceabilityorder);
			ACLMessage msg4 = myAgent.receive(traceabilitymplan);

		if(msg4!=null) {
			Traceabilitymplan.add(msg4.getContent());
			doAcknowledge(msg4.getSender());
		}else if(msg3!=null) {
			Traceabilityorder.add(msg3.getContent());
			doAcknowledge(msg3.getSender());
		}else if(msg2!=null){
			Traceabilitybatch.add(msg2.getContent());
			doAcknowledge(msg2.getSender());
		}else if (msg != null) {
			if (myAgent.period>0) timeout = System.currentTimeMillis()+(int)(myAgent.period*1.5); // se producirá timeout si se excede de (horaActual + D)
				// TODO el deadline sólo se debe calcular para componentes periódicos
				LOGGER.trace(" trackingBehaviour.onStart() msg != null");
				try {
					String stateClassName = (msg.getContentObject()==null || msg.getContentObject().getClass()==null)?"null"
					    :msg.getContentObject().getClass().getSimpleName();
					LOGGER.info("functionalityInstance.setState("+stateClassName+"="+
					    ((msg.getContentObject()==null)?"null":msg.getContentObject().toString())+
					        ")");
					
					((AvailabilityFunctionality)myAgent.functionalityInstance).setState(msg.getContentObject());
					LOGGER.debug(myAgent.cmpID+"("+((MWAgent) myAgent).getLocalName()+") < " + myAgent.cmpID+"("+msg.getSender().getLocalName()+"):"
							+ "state("+stateClassName+")");
				} catch (UnreadableException e) {
					LOGGER.warn(e.getLocalizedMessage());
					e.printStackTrace();
				}
			} else {
				if (timeout > 0) { //he recibido el primer estado y es periódico

					long blockTime = timeout - System.currentTimeMillis(); // me bloqueo hasta que se produzca posible timeout (horaTimeout-horaActual)
					if (blockTime <= 0) { //timeout expired 
						//expired = true;
						//transitionFlag = ControlBehaviour.PAUSED;
					  timeout = 0;
					  //TODO: el report error deberá ir al healthmonitor 
						myAgent.sendCommand("report " + myAgent.cmpID+ " type=error desc=state refreshing timeout");
						
//  HEALTHMONITOR DEBERÁ:
						//pedir al am lista NA que contienen los comp tracking > am los marca negociando (si alguno mas pregunta le respondo que espere) 
// lanza negociación nodo ganador y avisa al am
// am pasa de tracking a running el ganador 
// el running anterior a failure
						
						/**CompInst->ApplicationManager: timeout
							alt not(negotiation) 
							   ApplicationManager-->CompInst: NA list
							else negotiation
							   ApplicationManager-->CompInst: pause
							end
							
							note over CompInst,NA:negotiation
							CompInst->NA: condicion de negociacion
							NA-->CompInst: valor
							CompInst-->ApplicationManager: winner (NAx)
							
							alt NAx
							  ApplicationManager->CompInst: run
							else not(NAx)
							  ApplicationManager->CompInst: tracking
							end
						 	**/
						
					
						
						
//		nego an tienen compont track
// al que gana cambio de estado

						LOGGER.warn("Deadline excedido: "+(int)(myAgent.period*1.5)+" ms");
					} else {
						block(blockTime);
					}
				} else { // no he recibido ningun estado, timeout a partir del primero
					LOGGER.info("tracking.beh.block()");
					block();
				}
			}

			// TODO prueba para ver el cambio de estados --> luego borrar
			System.out.println("El agente " + myAgent.getLocalName() + " esta en el metodo action del TrackingBehaviour");
			
			LOGGER.exit();
		
	}

	public boolean done() {
		return expired;
	}

	public void doAcknowledge(AID name){ //responde al emisor del mensaje instanciado en name
		ACLMessage ack= new ACLMessage(7);
		ack.setOntology("Acknowledge");
		ack.setContent("Received");
		ack.addReceiver(name);
		myAgent.send(ack);
		LOGGER.debug("TRACKING "+myAgent.getLocalName()+" AGENT GOT FEEDBACK CORRECTLY");
	}
	
	public int onEnd() {
		return transitionFlag;
	}

	/**
	 * This method allows modifying the deadline
	 **/
//	public void setDeadline(long period) {
//		this.deadline = period + System.currentTimeMillis();
//		this.timeout = period;
//	}

}