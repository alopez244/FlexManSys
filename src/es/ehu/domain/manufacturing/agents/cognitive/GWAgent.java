package es.ehu.domain.manufacturing.agents.cognitive;

import es.ehu.domain.manufacturing.utilities.StructMessage;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.gateway.GatewayAgent;
import org.apache.commons.collections4.queue.CircularFifoQueue;

public class GWAgent extends GatewayAgent {

    public String msgRecv;
    public AID machineAgentName;
    public static final int bufferSize =6;
    CircularFifoQueue msgInFIFO = new CircularFifoQueue(bufferSize);


    protected void processCommand(java.lang.Object command) {
        System.out.println("-->Gateway processes execute");
        if(!(command instanceof StructMessage)){
            System.out.println("---Error, unexpected type");
            releaseCommand(command);
        }
        StructMessage msgStruct = (StructMessage) command;
        if(msgStruct.readAction()=="receive") {
            System.out.println("---GW, recv function");
            msgRecv = (String) msgInFIFO.poll();
            if ( msgRecv != null) {
                System.out.println("---GW, new message to read");
                ((StructMessage) command).setMessage(msgRecv);
                ((StructMessage) command).setNewData(true);
            } else {
                ((StructMessage) command).setNewData(false);
                System.out.println("---GW, message queue is empty");
            }
        }else if(msgStruct.readAction()=="send") {
            System.out.println("---Gateway send command");
            ACLMessage msgToAgent = new ACLMessage(msgStruct.readPerformative());
            msgToAgent.addReceiver(machineAgentName);
            msgToAgent.setOntology("negotiation");
            msgToAgent.setConversationId("PLCdata");
            msgToAgent.setContent(msgStruct.readMessage());
            send(msgToAgent);
        }
        System.out.println("<--Gateway processes execute");
        releaseCommand(command);
    }

    public void setup() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.and(MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),MessageTemplate.MatchPerformative(ACLMessage.INFORM)),
                MessageTemplate.MatchOntology("negotiation")),MessageTemplate.MatchConversationId("PLCdata"));

        addBehaviour(new CyclicBehaviour() {

            public void action() {
                ACLMessage msgToFIFO = receive(template);
                if (msgToFIFO != null) {
                    System.out.println("GWagent, message received from Machine Agent");
                    machineAgentName = msgToFIFO.getSender();
                    if(msgInFIFO.isAtFullCapacity()) {
                        System.out.println("buffer full, old message lost");
                    }
                    msgInFIFO.add((String) msgToFIFO.getContent());
                } else {
                    block();
                }
            }
        });
        super.setup();
    }

}
