package es.ehu.platform.behaviour;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.platform.MWAgent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import static es.ehu.platform.utilities.MasReconOntologies.*;

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
public class ResourceRunningBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 3456578696375317772L;

	static final Logger LOGGER = LogManager.getLogger(ResourceRunningBehaviour.class.getName());

	private MessageTemplate template;
	private MWAgent myAgent;
	private int PrevPeriod;
	private long NextActivation;

	// Constructor. Create a default template for the entry messages
	public ResourceRunningBehaviour(MWAgent a) {
		super(a);
		LOGGER.debug("*** Constructing RunningBehaviour ***");
		this.myAgent = a;
		
		this.template = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_RUN),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
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
		
		if (msg!=null) {
		 
		  //lo que haga en el running
    
		
		
		
		
		}
		
		long t = manageBlockingTimes();

		if (msg == null) {
			LOGGER.debug("Block time: " + t);
			block(t); // cada cierto tiempo comprobar recursos/alarmas
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