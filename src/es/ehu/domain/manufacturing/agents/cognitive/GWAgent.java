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
    private String stateasker="";
    CircularFifoQueue msgInFIFO = new CircularFifoQueue(bufferSize);
    CircularFifoQueue msgInFIFO2 = new CircularFifoQueue(bufferSize);
    public AID MAID = new AID("machine1", false);


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

        } else if(action.equals("ask_state")){
            msgRecv = (String) msgInFIFO2.poll();    //reads the oldest message from FIFO
            if ( msgRecv != null ) {
                System.out.println("---Someone asked to check asset state");
                ((StructMessage) command).setMessage(msgRecv);  //message is saved in StructMessage data structure, then ExternalJADEgw class will read it from there
                ((StructMessage) command).setNewData(true);
            } else {
                ((StructMessage) command).setNewData(false);
                System.out.println("---State checking queue is empty");
            }

        }else if(action.equals("rcv_state")) {
            System.out.println("---Asset answered with his state");
            AID ID=new AID(stateasker,false);
            ACLMessage msg = new ACLMessage(msgStruct.readPerformative());
            msg.addReceiver(ID);
            msg.setOntology("asset_state");
            msg.setContent(msgStruct.readMessage());
            send(msg);

        }else {
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
        MessageTemplate templateping = MessageTemplate.and(MessageTemplate.MatchOntology("ping"),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        MessageTemplate checkasset = MessageTemplate.and(MessageTemplate.MatchOntology("check_asset"),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));


        addBehaviour(new CyclicBehaviour() {

            public void action() {
                System.out.println("Entering CyclicBehaviour");

                ACLMessage ping = receive(templateping);

                if(ping!=null) {
                    ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                    reply.addReceiver(ping.getSender());
                    reply.setOntology(ping.getOntology());
                    reply.setContent("Alive");
                    System.out.println(ping.getSender().getLocalName()+" sent a ping. Answering.");
                    send(reply);
                }
                ACLMessage check_asset_state=receive(checkasset);
                if(check_asset_state!=null) {
                    if(msgInFIFO2.isAtFullCapacity()) {
                        System.out.println("buffer full, old message lost");
                    }
                    msgInFIFO2.add((String) check_asset_state.getContent()); //adds the message to be send in the buffer (max capacity = 6)
                    stateasker= check_asset_state.getSender().getLocalName();
                }

                ACLMessage msgToFIFO = receive(template);

               if (msgToFIFO != null) {

                    System.out.println("GWagent, message received from Machine Agent");
                    machineAgentName = msgToFIFO.getSender();//saves the sender ID for a later reply
                    ACLMessage ack=new ACLMessage(4);
                    ack.setOntology(msgToFIFO.getOntology());
                    ack.setConversationId(msgToFIFO.getConversationId());
                    ack.setContent(msgToFIFO.getContent());
                    ack.addReceiver(msgToFIFO.getSender());
                    send(ack);
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
