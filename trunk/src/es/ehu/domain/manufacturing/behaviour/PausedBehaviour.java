package es.ehu.domain.manufacturing.behaviour;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.domain.manufacturing.agent.MC_Agent;
import es.ehu.platform.MWAgent;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
 * This behaviour is a simple implementation of a message receiver.
 * It check if a satate is recive and stores it.
 * If the timeout expires before any message arrives, the behaviour
 * transition into a negotiation.
 * 
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/
public class PausedBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = -1354067344628941600L;
	static final Logger LOGGER = LogManager.getLogger(PausedBehaviour.class.getName());

	
	private MC_Agent myAgent;
	
	protected MessageTemplate template;
	protected long deadline = 0;
	protected long timeout = 0;

	private boolean finished = false;
	private int transitionFlag = -1;
		
	/**
	 * Constructor.
	 * 
	 * @param a
	 *            a reference to the Agent
	 * @param timeout
	 *            a timeout for waiting until a message arrives. It must
	 *            be expressed as an absolute time, as it would be returned by <code>System.currentTimeMillisec()</code>
	 **/
	public PausedBehaviour(MC_Agent a) {
		super(a);
		LOGGER.entry(a);
		myAgent = a;
		LOGGER.exit();
	}

	// For persistence service
	protected PausedBehaviour() {
	}

	public void onStart(){
	  LOGGER.entry();
		template = MessageTemplate.MatchOntology("state");
		//log.warning("********************* trackingBehaviour.onStart()");
		
    
		LOGGER.exit();
		
	}
	public void action() {
	  LOGGER.entry();

	  ACLMessage msg = myAgent.receive(template);

	  if (msg != null) {

	    try {
	      String stateClassName = (msg.getContentObject().getClass()==null)?"null":msg.getContentObject().getClass().getSimpleName();
	      LOGGER.info("functionalityInstance.setState("+stateClassName+")");
	      myAgent.functionalityInstance.setState(msg.getContentObject());
	      LOGGER.info(myAgent.cmpID+"("+((MWAgent) myAgent).getLocalName()+") < " + myAgent.cmpID+"("+msg.getSender().getLocalName()+"):"
	          + "state("+stateClassName+")");
	    } catch (UnreadableException e) {
	      LOGGER.warn(e.getLocalizedMessage());
	      e.printStackTrace();
	    }

	  } else {
	    LOGGER.info("paused.beh.block()");
	    block();
	  }
	  LOGGER.exit();
	}

	public boolean done() {
	  LOGGER.entry();
		return LOGGER.exit(finished);
	}	
	
	public int onEnd() {
	  LOGGER.entry();
		return LOGGER.exit(transitionFlag);
	}

	
}