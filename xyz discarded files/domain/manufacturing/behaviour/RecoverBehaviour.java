
package es.ehu.domain.manufacturing.behaviour;

/**
 * Comportamiento  OneShot del estado de Recover. 
 * Este comportamiento se hacen las labores de recuperacion
 * 
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 * @author Tomás Vergel Corada - EHU
 **/

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.domain.manufacturing.agent.MC_Agent;
import es.ehu.platform.behaviour.ControlBehaviour;
import jade.core.behaviours.OneShotBehaviour;

public class RecoverBehaviour extends OneShotBehaviour {
  static final Logger LOGGER = LogManager.getLogger(RecoverBehaviour.class.getName());
	private static final long serialVersionUID = -7722364363807986926L;
	private MC_Agent myAgent;
	private int exitVariable;

	public RecoverBehaviour(MC_Agent a) {
		super(a);
		myAgent = a;
	}

	@Override
	public void action() {
	  LOGGER.entry();
		try {
			// Do the reconfiguration and diagnosis
			myAgent.functionalityInstance.diagnosisRecover();
			
			LOGGER.debug("recuperado");
			//Change to running
			exitVariable=ControlBehaviour.RUNNING;
		} catch (Exception e) {
			e.printStackTrace();
			//Theres an error go to stop the execution
			exitVariable=ControlBehaviour.STOP;
		}
		LOGGER.exit(exitVariable);
	}

	public int onEnd() {
		return exitVariable;
	}

}
