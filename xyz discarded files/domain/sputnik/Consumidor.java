package es.ehu.domain.sputnik;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviourMW;
import jade.core.behaviours.OneShotBehaviour;
import es.ehu.*;
import es.ehu.platform.*;
import es.ehu.platform.behaviour.*;
import es.ehu.platform.utilities.StateParallel;
import es.ehu.platform.template.ApplicationAgentTemplate;;

public class Consumidor extends ApplicationAgentTemplate {

	private static final long serialVersionUID = -3415727708050658595L;

	private long startTime = 0;

	public Consumidor() {
	  functionalityInstance = new es.ehu.domain.sputnik.FunctionalityConsumidor();
	}

	
}