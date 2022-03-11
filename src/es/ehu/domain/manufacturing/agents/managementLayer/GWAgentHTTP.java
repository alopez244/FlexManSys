package es.ehu.domain.manufacturing.agents.managementLayer;

import es.ehu.domain.manufacturing.utilities.StructMessage;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.gateway.GatewayAgent;

public class GWAgentHTTP extends GatewayAgent {

    public String msgRecv;
    public String msgRecvState;
    public AID machineAgentName;
    public AID stateasker;

    @Override
    protected void processCommand(Object _command) {

        //Se procesa la estructura recibida al invocar el agente y se lee la acción a realizar
        StructMessage command = (StructMessage) _command;
        String action = command.readAction();

        switch (action) {
            case "init":

                //Se printea un mensaje por pantalla
                System.out.println("--- GWagentHTTP init() command called.");
                break;
            case "receive":

                //Se comprueba si se ha recibido algún mensaje
                if (msgRecv != null) {

                    //En caso afirmativo, se guarda en la estructura de datos
                    command.setMessage(msgRecv);
                    msgRecv = null;
                }
                break;
            case "send":

                //Se declara un nuevo mensaje ACL con la performativa y el contenido recibidos en la estructura
                //También se definen el receptor (el agente que me escribió primero) y la ontología (assetdata)
                ACLMessage msgToAgent = new ACLMessage(command.readPerformative());
                msgToAgent.addReceiver(machineAgentName);
                msgToAgent.setOntology("assetdata");
                msgToAgent.setContent(command.readMessage());
                send(msgToAgent);
                break;
        }

        //Se ejecuta el método releaseCommand para finalizar la ejecución
        releaseCommand(command);
    }

    @Override
    public void setup() {

        super.setup();

        //Se definen tres templates: uno para el ping (se comprueba si el gatewayAgent está vivo)
        MessageTemplate templatePing = MessageTemplate.and(MessageTemplate.MatchOntology("ping"),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        //Un segundo template para comprobar el estado del asset
        MessageTemplate templateCheckAsset = MessageTemplate.and(MessageTemplate.MatchOntology("check_asset"),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        //Y un tercero para el intercambio normal de mensajes
        MessageTemplate templateWork = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),MessageTemplate.MatchOntology("data"));

        addBehaviour(new CyclicBehaviour() {
            public void action() {

                //Se procesa cualquier mensaje
                ACLMessage msg = receive();

                //Se comprueba si se ha recibido algún mensaje o no
                if (msg != null){

                    //Si hay mensaje, se establecen las acciones a realizar dependiendo del tipo de mensaje
                    if (templatePing.match(msg)){

                        //Si se ha recibido un mensaje de este tipo, se responde al ping
                        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                        reply.addReceiver(msg.getSender());
                        reply.setOntology(msg.getOntology());
                        reply.setContent("Alive");
                        System.out.println(msg.getSender().getLocalName()+" sent a ping. Answering.");
                        send(reply);

                    } else if (templateCheckAsset.match(msg)){

                        //Si se ha recibido un mensaje de este tipo, se guarda el contenido del mensaje
                        // También se guarda el AID del solicitante
                        msgRecvState = msg.getContent();
                        stateasker= msg.getSender();

                    } else if (templateWork.match(msg)){

                        //Si se ha recibido un mensaje de este tipo, se responde con un acknowledge
                        ACLMessage ack=new ACLMessage(ACLMessage.CONFIRM);
                        ack.setOntology(msg.getOntology());
                        ack.setConversationId(msg.getConversationId());
                        ack.setContent(msg.getContent());
                        ack.addReceiver(msg.getSender());
                        send(ack);

                        // Además, se guardan el contenido del mensaje y el AID de la máquina
                        msgRecv=msg.getContent();
                        machineAgentName = msg.getSender();

                    }

                } else { //Si no hay mensaje, se bloquea el agente

                    block();
                }
            }
        });
    }
}
