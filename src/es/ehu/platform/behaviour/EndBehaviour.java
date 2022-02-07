package es.ehu.platform.behaviour;

import es.ehu.platform.MWAgent;
import jade.core.behaviours.OneShotBehaviour;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EndBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = -2673578185687045396L;
    static final Logger LOGGER = LogManager.getLogger(EndBehaviour.class.getName()) ;

    MWAgent myAgent;

    public EndBehaviour(MWAgent a) {
        super(a);
        myAgent = a;
    }

    @Override
    public void action() {
        myAgent.functionalityInstance.terminate(myAgent);
        myAgent.doDelete();

    } // end action

    public int onEnd() {
        return 1;
    }


}
