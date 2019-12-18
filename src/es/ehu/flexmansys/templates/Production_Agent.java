package es.ehu.flexmansys.templates;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviourMW;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import es.ehu.MWAgent;
import es.ehu.behaviour.BootBehaviour;
import es.ehu.behaviour.ControlBehaviour;
import es.ehu.behaviour.EndBehaviour;
import es.ehu.behaviour.RunningBehaviour;
import es.ehu.behaviour.TrackingBehaviour;
import es.ehu.platform.utilities.StateParallel;
import static es.ehu.utilities.MasReconOntologies.*;

/**
 * Production Agent template used by orders and batches in the FLEXMANSYS
 * architecture.
 * 
 * @author Mikel Lopez (@lopeziglesiasmikel) - Universidad del Pais Vasco
 * @author Brais Fortes (@fortes23) - Universidad del Pais Vasco
 */

public class Production_Agent extends MWAgent {

	private static final long serialVersionUID = -3415727708050658595L;
	static final Logger LOGGER = LogManager.getLogger(Production_Agent.class.getName());

	private static final String ST_BOOT = "boot";

	/**
	 * Production Agent Name
	 */
	public String productionName;

	/**
	 * Product Model DOM
	 */
	public Document productModel;

	@Override
	protected void setup() {

		this.initTransition = ControlBehaviour.RUNNING;

		variableInitialization(getArguments());

		MessageTemplate requestTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DATA),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		MessageTemplate operationTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DATA),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		MessageTemplate negTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_NEGOTIATE),
				MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
						MessageTemplate.MatchPerformative(ACLMessage.INFORM)));
		MessageTemplate runTemplate = MessageTemplate.or(requestTemplate,
				MessageTemplate.or(operationTemplate, negTemplate));

		/** Comportamiento Agente FSM **/
		FSMBehaviourMW behaviourFSM = new FSMBehaviourMW(this);

		/** Boot behaviour **/
		Behaviour boot = new BootBehaviour(this);

		/** Running behaviour **/
		Behaviour running = new RunningBehaviour(this, runTemplate);

		/** Tracking Behaviour **/
		Behaviour tracking = new TrackingBehaviour(this);

		/** End Behaviour **/
		Behaviour end = new EndBehaviour(this);

		/** FSM state definition **/
		behaviourFSM.registerFirstState(boot, ST_BOOT);
		behaviourFSM.registerState(new StateParallel(this, behaviourFSM, running), ControlBehaviour.ST_RUNNING);
		behaviourFSM.registerState(new StateParallel(this, behaviourFSM, tracking), ControlBehaviour.ST_TRACKING);
		behaviourFSM.registerLastState(end, ControlBehaviour.ST_STOP);

		/** FSM transition **/
		behaviourFSM.registerTransition(ST_BOOT, ControlBehaviour.ST_RUNNING, ControlBehaviour.RUNNING,
				new String[] { ST_BOOT });
		behaviourFSM.registerTransition(ST_BOOT, ControlBehaviour.ST_TRACKING, ControlBehaviour.TRACKING,
				new String[] { ST_BOOT });
		behaviourFSM.registerTransition(ST_BOOT, ControlBehaviour.ST_STOP, ControlBehaviour.STOP,
				new String[] { ST_BOOT });

		behaviourFSM.registerTransition(ControlBehaviour.ST_RUNNING, ControlBehaviour.ST_TRACKING,
				ControlBehaviour.TRACKING, new String[] { ControlBehaviour.ST_RUNNING });
		behaviourFSM.registerTransition(ControlBehaviour.ST_RUNNING, ControlBehaviour.ST_STOP, ControlBehaviour.STOP,
				new String[] { ControlBehaviour.ST_RUNNING });

		behaviourFSM.registerTransition(ControlBehaviour.ST_TRACKING, ControlBehaviour.ST_RUNNING,
				ControlBehaviour.RUNNING, new String[] { ControlBehaviour.ST_TRACKING });
		behaviourFSM.registerTransition(ControlBehaviour.ST_TRACKING, ControlBehaviour.ST_STOP, ControlBehaviour.STOP,
				new String[] { ControlBehaviour.ST_TRACKING });

		this.addBehaviour(behaviourFSM);
		LOGGER.debug("FSM start");
	}

	/**
	 * Allows variable initialization
	 */
	protected void variableInitialization(Object[] arguments) {
	}

	protected void takeDown() {
		try {
			LOGGER.info("Agent: " + this.getAID().getName() + "has ended");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}