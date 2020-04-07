package es.ehu.platform.behaviour;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.platform.MWAgent;
import jade.core.*;
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
public class RunningState extends SimpleBehaviour {
	
	private static final long serialVersionUID = 5211311085804151394L;	
	static final Logger LOGGER = LogManager.getLogger(RunningState.class.getName()) ;
	
		
	private int transitionFlag;
	private long nextActivation;
	private long firstActivation;
	private long realActivation;
	private long startTime;
	private MessageTemplate template;
	private int remainingSources;
	private MWAgent myAgent;
	private boolean esPeriodica,tocaActivarPeriodica,tocaActivarEsporadica;
	
	
	private boolean finished = false;
	
	//para log de tiempos
	private long time0 = 0;
	private long time1 = 0;
	
	private Object[] receivedMsgs;
	
	public RunningState(MWAgent a) {
		super(a);
		template = MessageTemplate.and(MessageTemplate.MatchOntology("data"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
	}

	public void onStart(){
	}
	
	public void action() {
	  // run functionality
	  // send result
    // send execution state to AM			
    // delay until next activation		
	}

	public int onEnd() {
		return transitionFlag;
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return finished;
	}
}