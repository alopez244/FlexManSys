/**
 * Comportamiento paralelo 
 * 
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/
package es.ehu.platform.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.MWAgent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;

public class StateParallel extends ParallelBehaviour {
  
	private static final long serialVersionUID = 3392465385445419545L;
	static final Logger LOGGER = LogManager.getLogger(StateParallel.class.getName()) ;

	private ControlBehaviour _supControl;
	private Behaviour _executComp;

	public StateParallel(MWAgent a, Behaviour executComp) {
		super(a, ParallelBehaviour.WHEN_ANY);
		LOGGER.entry(a, executComp);
		// Agregar comportamieto de control superior
		_supControl = new ControlBehaviour(a);
		addSubBehaviour(_supControl);

		_executComp = executComp;
		addSubBehaviour(_executComp);
		LOGGER.exit();
	}

	public StateParallel(MWAgent a, Behaviour fsmBeh, Behaviour... executComp) {
		super(a, ParallelBehaviour.WHEN_ANY);
		LOGGER.entry(a, executComp);

		// Agregar comportamieto de control superior
		_supControl = new ControlBehaviour(a,fsmBeh);
		addSubBehaviour(_supControl);
		for (int i = 0; i < executComp.length; i++) {
			_executComp = executComp[i];
			addSubBehaviour(_executComp);
		}
		
		LOGGER.exit();
	}

	public int onEnd() {
	  LOGGER.entry();
		if (_executComp.done()) return LOGGER.exit(_executComp.onEnd());
		if (_supControl.done()) return LOGGER.exit(_supControl.onEnd());
		return LOGGER.exit(0);
	}
}
