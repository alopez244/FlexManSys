package es.ehu.domain.manufacturing.agents;

import es.ehu.domain.manufacturing.agents.functionality.Batch_Functionality;
import es.ehu.domain.manufacturing.agents.functionality.Transport_Functionality;
import es.ehu.domain.manufacturing.template.DomResAgentTemplate;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.MessageTemplate;



public class TransportAgent extends DomResAgentTemplate {

    @Override
    protected MessageTemplate variableInitialization(Object[] arguments, Behaviour behaviour) {

        functionalityInstance = new Transport_Functionality();
        return null;

    }


}
