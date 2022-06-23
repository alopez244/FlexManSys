package packet.mqtt;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.gateway.GatewayAgent;

public class GWagentMQTT extends GatewayAgent {

    private String JSONmsg = null;
    public AID machineAgentName;

    @Override
    protected void processCommand(java.lang.Object _command) {
        StructMessage command = (StructMessage) _command;
        String action = command.getAction();

        if(action.equals("init")) {
            System.out.println("--- GWagentMQTT init() command called.");
            System.out.println("--- Hi, I am a Gateway Agent!");

        } else if(action.equals("recv")) {
            //System.out.println("--- GWagentMQTT recv() command called.");
            if(JSONmsg != null) {
                //System.out.println("--- GWagentMQTT is returning the message.");
                command.setMessage(JSONmsg);
                JSONmsg = null;
            } else {
                //System.out.println("--- GWagentMQTT has no message to return.");
            }

        } else if(action.equals("send")) {
            System.out.println("--- GWagentMQTT send() command called.");
            ACLMessage msg2MachineAgent = new ACLMessage(ACLMessage.INFORM);
            msg2MachineAgent.addReceiver(machineAgentName);
            msg2MachineAgent.setContent(command.getMessage());
            send(msg2MachineAgent);
        }

        releaseCommand(command);
    }

    @Override
    public void setup() {
        super.setup();

        MessageTemplate matchPerformative = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        MessageTemplate matchOntology = MessageTemplate.MatchOntology("data");
        MessageTemplate matchConversationID = MessageTemplate.MatchConversationId("1234");
        final MessageTemplate messageTemplate = MessageTemplate.and(MessageTemplate.and(matchPerformative, matchOntology), matchConversationID);

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive(messageTemplate);
                if (msg != null) {
                    System.out.println("--- GWagentMQTT has received a message from MachineAgent");
                    JSONmsg = msg.getContent();

                    ACLMessage ack = new ACLMessage(ACLMessage.CONFIRM);
                    ack.setOntology(msg.getOntology());
                    ack.setConversationId(msg.getConversationId());
                    ack.setContent(msg.getContent());
                    ack.addReceiver(msg.getSender());
                    send(ack);

                    machineAgentName = msg.getSender();
                } else {
                    System.out.println("--- No messages from MachineAgent. The GWagentMQTT is blocking.");
                    block();
                }
            }
        });
    }

}
