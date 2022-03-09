//package es.ehu.domain.manufacturing.agents.cognitive;
//
//import com.google.gson.Gson;
//import es.ehu.domain.manufacturing.utilities.StructCommandMsg;
//import es.ehu.domain.manufacturing.utilities.StructTranspResults;
//import es.ehu.domain.manufacturing.utilities.StructTranspState;
//import jade.core.AID;
//import jade.core.behaviours.CyclicBehaviour;
//import jade.lang.acl.ACLMessage;
//import jade.lang.acl.MessageTemplate;
//import jade.wrapper.gateway.GatewayAgent;
//
//public class GWagentROSmsg extends GatewayAgent {
//
//    private String JSONmsg_content = null;
//    private AID TransportAgentName = null;
//    private int conversationId = 0;
//
//    @Override
//    protected void processCommand(Object _command) {
//        StructCommandMsg command = (StructCommandMsg) _command;
//        String action = command.getAction();
//
//        if(action.equals("init")) {
//            System.out.println("--- GWagentROS init() command called.");
//            System.out.println("--- Hi, I am a Gateway Agent!");
//
//        } else if(action.equals("recv")) {
//            if(JSONmsg_content != null) {
//                System.out.println("--- The Gateway Agent is returning the message.");
//
//                //TODO: Cambiar el tipo de mensaje que se procesa (tiene que ser un request)
//                Gson gson = new Gson();
//                StructTranspState javaTranspState = gson.fromJson(JSONmsg_content, StructTranspState.class);
//                ((StructCommandMsg) command).setContent(javaTranspState);
//
//                JSONmsg_content = null;
//            } else {
//                System.out.println("--- The Gateway Agent has no message to return.");
//            }
//        } else if(action.equals("sendState")) {
//            System.out.println("--- The Gateway Agent is reporting a state message to the TransportAgent.");
//            ACLMessage msgToTransportAgent = new ACLMessage(ACLMessage.CONFIRM);
//            msgToTransportAgent.addReceiver(TransportAgentName);
//            msgToTransportAgent.setOntology("data");
//            msgToTransportAgent.setConversationId(String.valueOf(conversationId++));
//
//            Gson gson = new Gson();
//            StructTranspState javaTranspState = (StructTranspState) command.getContent();
//            msgToTransportAgent.setContent( gson.toJson(javaTranspState));
//
//            send(msgToTransportAgent);
//        } else if(action.equals("sendResults")) {
//            System.out.println("--- The Gateway Agent is reporting a results message to the TransportAgent.");
//            ACLMessage msgToTransportAgent = new ACLMessage(ACLMessage.INFORM);
//            msgToTransportAgent.addReceiver(TransportAgentName);
//            msgToTransportAgent.setOntology("data");
//            msgToTransportAgent.setConversationId(String.valueOf(conversationId++));
//
//            Gson gson = new Gson();
//            StructTranspResults javaTranspResults = (StructTranspResults) command.getContent();
//            msgToTransportAgent.setContent( gson.toJson(javaTranspResults));
//
//            send(msgToTransportAgent);
//        }
//
//        releaseCommand(command);
//    }
//
//    @Override
//    public void setup() {
//        super.setup();
//
//        MessageTemplate matchPerformative = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
//        MessageTemplate matchOntology = MessageTemplate.MatchOntology("data");
//        final MessageTemplate messageTemplate =  MessageTemplate.and(matchPerformative, matchOntology);
//
//        addBehaviour(new CyclicBehaviour() {
//            public void action() {
//                ACLMessage msg = receive(messageTemplate);
//                if(msg != null) {
//                    System.out.println("--- The Gateway Agent has received a message from TA!");
//                    JSONmsg_content = msg.getContent();
//                    TransportAgentName =msg.getSender();
//                } else {
//                    System.out.println("--- No messages from TA. The Gateway Agent is blocking.");
//                    block();
//                }
//            }
//        });
//    }
//}
//
//
//
