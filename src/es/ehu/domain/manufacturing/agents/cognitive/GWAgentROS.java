package es.ehu.domain.manufacturing.agents.cognitive;

import es.ehu.domain.manufacturing.behaviour.SendTaskBehaviour;
import es.ehu.domain.manufacturing.utilities.StructMessage;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.gateway.GatewayAgent;
import jade.wrapper.gateway.GatewayListener;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.ros.node.topic.Subscriber;
import social_msgs.social;

import java.util.concurrent.ConcurrentHashMap;

public class GWAgentROS extends GatewayAgent {  //ROS

    private GatewayListener listener;
    public String msgRecv;
    public AID TransportAgentName;
    public static final int bufferSize = 6;
    CircularFifoQueue msgInFIFO = new CircularFifoQueue(bufferSize);
    Ros_Jade_Dummy dummy= new Ros_Jade_Dummy(this);  // instanciar Nodo dummy

    protected void processCommand(java.lang.Object command) { //The method is called each time a request to process a command is received from the JSP Gateway. receive strmessage


        //MENSAJE DESDE NODE-KOKUKI-ROS

        System.out.println("-->Gateway processes execute");
        if (!(command instanceof StructMessage)) {
            System.out.println("---Error, unexpected type");
            releaseCommand(command);
        }
        StructMessage msgStruct = (StructMessage) command;
        String action = msgStruct.readAction();
        if (action.equals("receive")) {     // JadeGateway.execute command was called for new message reading (Agent -> PLC)
            System.out.println("---GW, recv function");
            msgRecv = (String) msgInFIFO.poll();    //reads the oldest message from FIFO
            if (msgRecv != null) {
                System.out.println("---GW, new message to read");
                ((StructMessage) command).setMessage(msgRecv);  //message is saved in StructMessage data structure, then ROSJADEgw class will read it from there
                ((StructMessage) command).setNewData(true);
            } else {
                ((StructMessage) command).setNewData(false);
                System.out.println("---GW, message queue is empty");
            }
        } else if (action.equals("send")) {

           // ConcurrentHashMap<String, Ros_Jade_Msg> chm= dummy.getMessageStorage();


            System.out.println("---Gateway send command");
            ACLMessage msgToAgent = new ACLMessage(msgStruct.readPerformative()); //reads the performative saved in StructMessage data structure
            msgToAgent.addReceiver(TransportAgentName);
            msgToAgent.setOntology("data");
            msgToAgent.setConversationId(msgToAgent.getConversationId()); //numbers
            msgToAgent.setContent(msgStruct.readMessage()); //reads the message saved in StructMessage data structure
            send(msgToAgent);

        } else if (action.equals("init")) {

            System.out.println("---Gateway init command called");
            System.out.println("---Hello, I am a Gateway Agent");
        }else{ //Check if any msg is received
            System.out.println("---Gateway recv function");
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


    protected void setup(){ //agent already registered and is able to send and receive messages. Necessary to add behaviour in order to to anything.

        MessageTemplate template = MessageTemplate.and(MessageTemplate.and(MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),MessageTemplate.MatchPerformative(ACLMessage.INFORM)),
                MessageTemplate.MatchOntology("negotiation")),MessageTemplate.MatchConversationId("PLCdata"));


        // MENSAJE DESDE TRANSPORT AGENT

        addBehaviour(new CyclicBehaviour() { //keep executing constantly

            public void action() {
                System.out.println("Entering CyclicBehaviour");
                ACLMessage msgToFIFO = receive(template); //recivir mensaje desde Transport Agent
                if (msgToFIFO != null) {
                    System.out.println("GWagent, message received from Transport Agent");
                    TransportAgentName = msgToFIFO.getSender();   //saves the sender ID for a later reply
                    if(msgInFIFO.isAtFullCapacity()) {
                        System.out.println("buffer full, old message lost");
                    }
                    msgInFIFO.add((String) msgToFIFO.getContent());//adds the message to be send in the buffer (max capacity = 6)

                    Ros_Jade_Msg msg =new Ros_Jade_Msg(msgToFIFO.getConversationId(),msgToFIFO.getOntology(),msgToFIFO.getContent());  //PREPARING MSG TO KOBUKI
                    dummy.send(msg);  // publicar mensaje

                } else {
                    System.out.println("Block the agent");
                    block();
                }
            }
        });
        super.setup();
    }
    protected void takeDown(){
        if(listener!=null){
            listener.handleGatewayDisconnected();
        }
    }
}














