package es.ehu.platform.behaviour;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.platform.MWAgent;

import jade.core.behaviours.OneShotBehaviour;

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
        try {
            ((MWAgent)myAgent).deregisterAgent(myAgent.getLocalName());
            myAgent.doDelete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // end action

    public int onEnd() {
        return 1;
    }



}
