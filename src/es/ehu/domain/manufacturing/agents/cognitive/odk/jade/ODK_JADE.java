package es.ehu.domain.manufacturing.agents.cognitive.odk.jade;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;

public class ODK_JADE {

    /** JADE Agent represented in the ROS platform */
    private Agent myAgent;
    /** Behaviour to wake up the agent if there are new events */
    private Behaviour controlledBehaviour;

    //Constructor
    public ODK_JADE (Agent a){

        this.myAgent = a;
        this.controlledBehaviour = null;

    }
}
