package es.ehu.flexmansys.templates;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviourMW;
import jade.lang.acl.MessageTemplate;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.BootBehaviour;
import es.ehu.platform.behaviour.EndBehaviour;
import es.ehu.platform.behaviour.RunningBehaviour;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.utilities.StateParallel;

/**
 * Monitor Agent template used by the control panel in the FLEXMANSYS
 * architecture.
 * 
 * @author Mikel Lopez (@lopeziglesiasmikel) - Universidad del Pais Vasco
 * @author Brais Fortes (@fortes23) - Universidad del Pais Vasco
 */
public class Monitor_Template extends MWAgent {

	private static final long serialVersionUID = -249640734709572077L;

	static final Logger LOGGER = LogManager.getLogger(Monitor_Template.class.getName());

	private static final String ST_BOOT = "boot";

	/**
	 * Monitor Name
	 */
	public String monitorName;

	@Override
	protected void setup() {

		this.initTransition = ControlBehaviour.RUNNING;

		/** Comportamiento Agente FSM **/
		FSMBehaviourMW behaviourFSM = new FSMBehaviourMW(this);

		/** Agent definition **/
		MessageTemplate runTemplate = variableInitialization(getArguments(), behaviourFSM);

		/** Comportamiento boot **/
		Behaviour boot = new BootBehaviour(this);

		/** Comportamiento running **/
		Behaviour running;
		if (runTemplate != null) {
			running = new RunningBehaviour(this, runTemplate);
		} else {
			running = new RunningBehaviour(this);
		}

		/** Comportamiento end **/
		Behaviour end = new EndBehaviour(this);

		/** FSM state definition **/
		behaviourFSM.registerFirstState(boot, ST_BOOT);
		behaviourFSM.registerState(new StateParallel(this, behaviourFSM, running), ControlBehaviour.ST_RUNNING);
		behaviourFSM.registerLastState(end, ControlBehaviour.ST_STOP);

		/** FSM transition **/
		behaviourFSM.registerTransition(ST_BOOT, ControlBehaviour.ST_RUNNING, ControlBehaviour.RUNNING,
				new String[] { ST_BOOT });
		behaviourFSM.registerTransition(ST_BOOT, ControlBehaviour.ST_STOP, ControlBehaviour.STOP,
				new String[] { ST_BOOT });

		behaviourFSM.registerTransition(ControlBehaviour.ST_RUNNING, ControlBehaviour.ST_STOP, ControlBehaviour.STOP,
				new String[] { ControlBehaviour.ST_RUNNING });

		this.addBehaviour(behaviourFSM);
		LOGGER.debug("FSM start");
	}

	/**
	 * Allow the initialization of the particular variables of each agent.
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
