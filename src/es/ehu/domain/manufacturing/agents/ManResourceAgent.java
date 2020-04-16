package es.ehu.domain.manufacturing.agents;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.MessageTemplate;

import es.ehu.domain.manufacturing.agents.functionality.ManResource_Functionality;
import es.ehu.platform.template.ResourceAgentTemplate;

public class ManResourceAgent extends ResourceAgentTemplate {
    static final Logger LOGGER = LogManager.getLogger(ManResourceAgent.class.getName());

    /**
     *
     */

    private static final long serialversionUID = 6653164212927368152L;

    @Override
    protected MessageTemplate variableInitialization(Object[] arguments, Behaviour behaviour) {
        LOGGER.entry(arguments, behaviour);
        this.functionalityInstance = new ManResource_Functionality();
        return LOGGER.exit(null);
    }

    protected void takeDown() {
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
