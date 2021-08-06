package es.ehu.domain.manufacturing.agents;

import es.ehu.domain.manufacturing.agents.functionality.Batch_Functionality;
import es.ehu.platform.template.ApplicationAgentTemplate;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BatchAgent extends ApplicationAgentTemplate {

    private static final long serialVersionUID = -3774619604569763790L;

    @Override
    protected MessageTemplate variableInitialization(Object[] arguments, Behaviour behaviour) {
        System.out.println("es.ehu.platform.template.ApplicationAgentTemplate.variableInitialization()");

        this.functionalityInstance = new Batch_Functionality();  //TODO
        return null;
    }

    protected void takeDown() {
        try {
            //LOGGER.info("Agent: " + this.getAID().getName() + "has ended");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
