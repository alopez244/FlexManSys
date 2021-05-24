package es.ehu.domain.manufacturing.agents;

import es.ehu.domain.manufacturing.agents.functionality.Batch_Functionality;
import es.ehu.domain.manufacturing.agents.functionality.Transport_Functionality;
import es.ehu.domain.manufacturing.template.DomResAgentTemplate;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.MessageTemplate;




public class TransportAgent extends DomResAgentTemplate  {

    private static final long serialVersionUID = -214426101233212079L;

    @Override
    protected MessageTemplate variableInitialization(Object[] arguments, Behaviour behaviour) {
        System.out.println("es.ehu.platform.template.ApplicationAgentTemplate.variableInitialization()");
        functionalityInstance = new Transport_Functionality();
        return null;  // return LOGGER.exit(null); //

    }
    protected void takeDown() {
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
