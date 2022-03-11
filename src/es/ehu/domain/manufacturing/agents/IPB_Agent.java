package es.ehu.domain.manufacturing.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class IPB_Agent extends Agent {

    @Override
    protected void setup() {
        // When this method is called the agent has been already registered with the Agent Platform AMS and is able to send and receive messages.
        // However, the agent execution model is still sequential and no behaviour scheduling is active yet.
        // This method can be used for ordinary startup tasks such as DF registration, but is essential to add at least a Behaviour object to the agent.

        addBehaviour(new CyclicBehaviour() {

            String assetName = getLocalName();
            AID requester;

            public void action() {

                //Recibo el mensaje del Operator Agent y se lo paso al GatewayAgent (hay que hacerlo con un nuevo mensaje)
                ACLMessage request = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                if (request != null){

                    ACLMessage requestToGW = new ACLMessage(ACLMessage.REQUEST);
                    AID GWagentHTTP = new AID("ControlGatewayCont"+assetName, false);
                    requestToGW.addReceiver(GWagentHTTP);
                    requestToGW.setOntology(request.getOntology());
                    requestToGW.setContent(request.getContent());
                    send(requestToGW);
                    requester = request.getSender();
                }

                //Recibo la respuesta y la devuelvo al Operator Agent
                ACLMessage responseFromGW = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                if (responseFromGW != null){

                    ACLMessage response = new ACLMessage(ACLMessage.INFORM);
                    response.addReceiver(requester);
                    response.setContent(responseFromGW.getContent());
                    send(response);
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

