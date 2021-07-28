package es.ehu.domain.manufacturing.agents.cognitive;

import es.ehu.domain.manufacturing.utilities.StructMessage;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.gateway.GatewayAgent;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.*;
import java.util.Iterator;

public class GWAgent extends GatewayAgent {

    public String msgRecv;
    public AID machineAgentName;
    public static final int bufferSize =6;
    CircularFifoQueue msgInFIFO = new CircularFifoQueue(bufferSize);


    protected void processCommand(java.lang.Object command) {   //this method will be executed when the externalJAde class executes the command JadeGateway.execute
        System.out.println("-->Gateway processes execute");
        if(!(command instanceof StructMessage)){
            System.out.println("---Error, unexpected type");
            releaseCommand(command);
        }
        StructMessage msgStruct = (StructMessage) command;
        String action = msgStruct.readAction();
        if(action.equals("receive")) {     // JadeGateway.execute command was called for new message reading (Agent -> PLC)
            System.out.println("---GW, recv function");
            msgRecv = (String) msgInFIFO.poll();    //reads the oldest message from FIFO
            if ( msgRecv != null ) {
                System.out.println("---GW, new message to read");
                ((StructMessage) command).setMessage(msgRecv);  //message is saved in StructMessage data structure, then ExternalJADEgw class will read it from there
                ((StructMessage) command).setNewData(true);
            } else {
                ((StructMessage) command).setNewData(false);
                System.out.println("---GW, message queue is empty");
            }
        } else if(action.equals("send")) {      // JadeGateway.execute command was called for new message sending (PLC -> Agent)
            System.out.println("---Gateway send command");
            ACLMessage msgToAgent = new ACLMessage(msgStruct.readPerformative()); //reads the performative saved in StructMessage data structure
            msgToAgent.addReceiver(machineAgentName);   //for a correct data exchanging, agent must send a message to the PLC first
            msgToAgent.setOntology("negotiation");
            msgToAgent.setConversationId("PLCdata");
            msgToAgent.setContent(msgStruct.readMessage()); //reads the message saved in StructMessage data structure
            send(msgToAgent);
        } else if(action.equals("init")) {      // JadeGateway.execute command was called for new message sending (PLC -> Agent)
            System.out.println("---Gateway init command");
            System.out.println("---Hello, I am a Gateway Agent");
        } else {
            System.out.println("---GW, recv function");
            msgRecv = (String) msgInFIFO.poll();    //reads the oldest message from FIFO
            if ( msgRecv != null ) {
                System.out.println("---GW, new message to read");
                ((StructMessage) command).setMessage(msgRecv);  //message is saved in StructMessage data structure, then ExternalJADEgw class will read it from there
                ((StructMessage) command).setNewData(true);
            } else {
                ((StructMessage) command).setNewData(false);
                System.out.println("---GW, message queue is empty");
            }
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
                System.out.println("Entering CyclicBehaviour");

                ACLMessage msgToFIFO = receive(template);

               if (msgToFIFO != null) {
                    System.out.println("GWagent, message received from Machine Agent");
                    machineAgentName = msgToFIFO.getSender();//saves the sender ID for a later reply
                    System.out.println("MachineAgentName :"+machineAgentName);

                    if(msgInFIFO.isAtFullCapacity()) {
                        System.out.println("buffer full, old message lost");
                    }
                    msgInFIFO.add((String) msgToFIFO.getContent()); //adds the message to be send in the buffer (max capacity = 6)
                } else {
                    System.out.println("Block the agent");
                    block();
                }
            }
        });
        super.setup();
    }

}
