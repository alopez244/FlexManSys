package es.ehu.platform.agents;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.MessageTemplate;

import es.ehu.platform.agents.functionality.ProcNode_Functionality;
import es.ehu.platform.behaviour.NegotiatingBehaviour;
import es.ehu.platform.template.*;

public class ProcNodeAgent extends ResourceAgentTemplate  {
    static final Logger LOGGER = LogManager.getLogger(ProcNodeAgent.class.getName());

    /**
     *
     */

    private static final long serialVersionUID = 6653164212927368081L;

    @Override
    protected MessageTemplate variableInitialization(Object[] arguments, Behaviour behaviour) {
        LOGGER.entry(arguments, behaviour);
        this.functionalityInstance = new ProcNode_Functionality();
        return LOGGER.exit(null);
    }

    protected void takeDown() {
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

