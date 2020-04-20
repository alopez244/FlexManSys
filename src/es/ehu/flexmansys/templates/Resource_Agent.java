package es.ehu.flexmansys.templates;

import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.*;
import es.ehu.platform.utilities.StateParallel;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviourMW;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

/**
 * Resource Agent template used by machines and transports in the FLEXMANSYS
 * architecture.
 * 
 * @author Mikel Lopez (@lopeziglesiasmikel) - Universidad del Pais Vasco
 * @author Brais Fortes (@fortes23) - Universidad del Pais Vasco
 */
public class Resource_Agent extends MWAgent {

	private static final long serialVersionUID = 2476743710831028702L;
	static final Logger LOGGER = LogManager.getLogger(Resource_Agent.class.getName());

	private static final String ST_BOOT = "boot";

	/**
	 * Resource Name
	 */
	public String resourceName;

	/**
	 * Resource Model DOM
	 */
	public Document resourceModel;

	@Override
	protected void setup() {

		this.initTransition = ControlBehaviour.RUNNING;

		/** Comportamiento Agente FSM **/
		FSMBehaviourMW behaviourFSM = new FSMBehaviourMW(this);

		MessageTemplate runTemplates = variableInitialization(getArguments(), behaviourFSM);

		/** Comportamiento boot **/
		Behaviour boot = new BootBehaviour(this);

		/** Comportamiento running **/
		Behaviour running = null;
		if (runTemplates == null) {
			running = new RunningBehaviour(this);
		} else {
			running = new RunningBehaviour(this, runTemplates);
		}

		/** Comportamiento behaviour **/
		Behaviour negotiating = new NegotiatingBehaviour(this);

		/** Comportamiento standby **/
		Behaviour standby = new PausedBehaviour(this);

		/** Comportamiento end **/
		Behaviour end = new EndBehaviour(this);

		/** FSM state definition **/
		behaviourFSM.registerFirstState(boot, ST_BOOT);
		behaviourFSM.registerState(new StateParallel(this, behaviourFSM, running, negotiating),
				ControlBehaviour.ST_RUNNING);
		behaviourFSM.registerState(new StateParallel(this, behaviourFSM, standby), ControlBehaviour.ST_PAUSE);
		behaviourFSM.registerLastState(end, ControlBehaviour.ST_STOP);

		/** FSM transition **/
		behaviourFSM.registerTransition(ST_BOOT, ControlBehaviour.ST_RUNNING, ControlBehaviour.RUNNING,
				new String[] { ST_BOOT });
		behaviourFSM.registerTransition(ST_BOOT, ControlBehaviour.ST_PAUSE, ControlBehaviour.PAUSED,
				new String[] { ST_BOOT });
		behaviourFSM.registerTransition(ST_BOOT, ControlBehaviour.ST_STOP, ControlBehaviour.STOP,
				new String[] { ST_BOOT });

		behaviourFSM.registerTransition(ControlBehaviour.ST_RUNNING, ControlBehaviour.ST_PAUSE, ControlBehaviour.PAUSED,
				new String[] { ControlBehaviour.ST_RUNNING });
		behaviourFSM.registerTransition(ControlBehaviour.ST_RUNNING, ControlBehaviour.ST_STOP, ControlBehaviour.STOP,
				new String[] { ControlBehaviour.ST_RUNNING });

		behaviourFSM.registerTransition(ControlBehaviour.ST_PAUSE, ControlBehaviour.ST_RUNNING,
				ControlBehaviour.RUNNING, new String[] { ControlBehaviour.ST_PAUSE });
		behaviourFSM.registerTransition(ControlBehaviour.ST_PAUSE, ControlBehaviour.ST_STOP, ControlBehaviour.STOP,
				new String[] { ControlBehaviour.ST_PAUSE });

		this.addBehaviour(behaviourFSM);
		LOGGER.debug("FSM start");
	}

	/**
	 * Allows variable initialization
	 */
	protected MessageTemplate variableInitialization(Object[] arguments, Behaviour behaviour) {
		return null;
	}

	protected void takeDown() {
		try {
			LOGGER.info("Agent: " + this.getAID().getName() + "has ended");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
