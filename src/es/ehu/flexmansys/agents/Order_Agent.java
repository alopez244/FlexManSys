package es.ehu.flexmansys.agents;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.platform.behaviour.ControlBehaviour;

public class Order_Agent extends Production_Agent {

	private static final long serialVersionUID = 1L;

	static final Logger LOGGER = LogManager.getLogger(Production_Agent.class.getName());

	/** Identifier of the onumber of batched managed by the order. */
	public int batchNumber;

	@Override
	protected void variableInitialization(Object[] arguments) {
		LOGGER.entry(arguments);
		if ((arguments != null) && (arguments.length >= 2)) {
			this.productionName = arguments[0].toString();
			try {
				batchNumber = Integer.valueOf((String)arguments[1]);
			} catch (Exception e) {
				batchNumber = 0;
				LOGGER.info("Not expected batch number");
				this.initTransition = ControlBehaviour.STOP;
			}
		} else {
			LOGGER.info("There are not sufficient arguments to start");
			this.initTransition = ControlBehaviour.STOP;
		}

		functionalityInstance = new es.ehu.flexmansys.functionality.Order_Functionality(this);
		LOGGER.exit();
	}
}
