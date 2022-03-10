package es.ehu.domain.manufacturing.test;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class REST_Test_Agent extends Agent {

    @Override
    protected void setup() {
        // When this method is called the agent has been already registered with the Agent Platform AMS and is able to send and receive messages.
        // However, the agent execution model is still sequential and no behaviour scheduling is active yet.
        // This method can be used for ordinary startup tasks such as DF registration, but is essential to add at least a Behaviour object to the agent.

        addBehaviour(new CyclicBehaviour() {

            Integer i = 0;

            public void action() {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                AID GWagentROS = new AID("ControlGatewayCont1", false);
                msg.addReceiver(GWagentROS);
                msg.setOntology("data");
                msg.setConversationId("1234");
                msg.setContent(i.toString());
                send(msg);
		i++;

                try {
                    Thread.sleep(5000);
                } catch(Exception e) {
                    ;
                }
            }
        });

    }

    @Override
    protected void takeDown() {
        // When this method is called the agent has not deregistered itself with the Agent Platform AMS and is still able to exchange messages with other agents.
        // However, no behaviour scheduling is active anymore and the Agent Platform Life Cycle state is already set to deleted.
        // This method can be used for ordinary cleanup tasks such as DF deregistration, but explicit removal of all agent behaviours is not needed.
        System.out.println("##### takeDown() #####");
    }
}

