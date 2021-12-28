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

  	public ArrayList<ArrayList<ArrayList<ArrayList<String>>>> Traceability=new ArrayList<>();
  	public ArrayList<String> remaining=new ArrayList<String>();
	public ArrayList<String> FinishTimes=new ArrayList<String>();
	public ArrayList<String> Replicas=new ArrayList<String>();
  	public String parent;
	public boolean firstime;
	protected MessageTemplate template;


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
		myAgent.ActualState="tracking";
		timeout = 0;
		expired = false;
		transitionFlag = -1;
		LOGGER.debug("***************** inicio TrackingBehaviour");
		LOGGER.debug("myAgent.period="+myAgent.period);
		
		
	}
	public void action() {
		LOGGER.entry();
			ACLMessage msg = myAgent.receive(template);


		if (msg != null) {
			if (myAgent.period>0) timeout = System.currentTimeMillis()+(int)(myAgent.period*1.5); // se producirá timeout si se excede de (horaActual + D)
				// TODO el deadline sólo se debe calcular para componentes periódicos
				LOGGER.trace(" trackingBehaviour.onStart() msg != null");
//				try {
//					String stateClassName = (msg.getContentObject()==null || msg.getContentObject().getClass()==null)?"null"  //anterior

					String stateClassName = (msg.getContent()==null || msg.getContent().getClass()==null)?"null"

//					    :msg.getContentObject().getClass().getSimpleName();  //anterior

							:msg.getContent().getClass().getSimpleName();

//					LOGGER.info("functionalityInstance.setState("+stateClassName+"="+
//					    ((msg.getContentObject()==null)?"null":msg.getContentObject().toString())+
////					        ")"); //anterior

					LOGGER.info("functionalityInstance.setState("+stateClassName+"="+
							((msg.getContent()==null)));
					
//					((AvailabilityFunctionality)myAgent.functionalityInstance).setState(msg.getContentObject());  //anterior
					Acknowledge(msg);
					((AvailabilityFunctionality)myAgent.functionalityInstance).setState(msg.getContent());
					LOGGER.debug(myAgent.cmpID+"("+((MWAgent) myAgent).getLocalName()+") < " + myAgent.cmpID+"("+msg.getSender().getLocalName()+"):"
							+ "state("+stateClassName+")");

//				} catch (UnreadableException e) {
//					LOGGER.warn(e.getLocalizedMessage());
//					e.printStackTrace();
//				}
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

	public void setState(ACLMessage msg){


	}
	public void Acknowledge(ACLMessage msg){
		sendACLMessage(ACLMessage.CONFIRM,msg.getSender(),msg.getOntology(),msg.getConversationId(),msg.getContent());
	}
	public void sendACLMessage(int performative, AID reciever, String ontology, String conversationId, String content) {
		ACLMessage msg = new ACLMessage(performative); //envio del mensaje
		msg.addReceiver(reciever);
		msg.setOntology(ontology);
		msg.setConversationId(conversationId);
		msg.setContent(content);
		myAgent.send(msg);
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