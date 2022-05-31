package es.ehu.platform.behaviour;

import jade.core.AID;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import es.ehu.platform.MWAgent;
import jade.core.behaviours.*;
import jade.lang.acl.*;

public class PingBehaviour extends SimpleBehaviour{

    private MessageTemplate template;
    private MWAgent myAgent;
    static final Logger LOGGER = LogManager.getLogger(PingBehaviour.class.getName());

    public PingBehaviour(MWAgent a){
        super(a);
        LOGGER.entry(a);
        LOGGER.debug("*******Ping behaviour started*******");
        this.myAgent = a;
        this.template = MessageTemplate.and(MessageTemplate.or(MessageTemplate.MatchOntology("ping"),MessageTemplate.MatchOntology("ping_PLC")),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST)); //hay 3 tipos de ping: ping a agente, ping hasta el PLC y checkeo de mensajes recibidos
        LOGGER.exit();
    }
@Override
    public void action(){
        LOGGER.entry();
        ACLMessage msg = myAgent.receive(template);
        if(msg!=null) {
            CircularFifoQueue msgFIFO =null;
            LOGGER.info(msg.getSender().getLocalName()+" sent a ping. Answering.");

            if(msg.getContent().equals("")){
                ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                reply.addReceiver(msg.getSender());
                reply.setOntology("ping");

                if(myAgent.getLocalName().contains("machine")&&msg.getOntology().equals("ping_PLC")){ //si soy agente máquina y he recibido un ping PLC le pido al GW

                    ACLMessage PLC_ping=new ACLMessage(ACLMessage.REQUEST);
                    AID myGW=new AID(myAgent.gatewayAgentName,false);
                    PLC_ping.addReceiver(myGW);
                    //primero hay que chekcear el estado del GW con un ping
                    ACLMessage GW_ping=new ACLMessage(ACLMessage.REQUEST);
                    GW_ping.setOntology("ping"); //primero se le envia al GW un ping para evaluar su estado. No se envía el "check_asset" directamente porque los tiempos de espera de respuesta se incrementan
                    GW_ping.setContent("");
                    GW_ping.addReceiver(myGW);
                    myAgent.send(GW_ping);
                    ACLMessage GW_state= myAgent.blockingReceive(MessageTemplate.and(MessageTemplate.MatchOntology("ping"),MessageTemplate.MatchPerformative(7)),300);
                    if(GW_state==null){
                        reply.setContent(myAgent.gatewayAgentName+":DOWN & PLC:?");
                    }else { //si ha contestado el GW le pregutamos el estado del PLC
                        PLC_ping.setOntology("check_asset");
                        PLC_ping.setContent("ask_state");
                        myAgent.send(PLC_ping);
                        ACLMessage answer = myAgent.blockingReceive(MessageTemplate.MatchOntology("asset_state"), 300);
                        if (answer != null) {
                            if (answer.getContent().equals("Working")) { //PLC esta ejecutando algun plan y no tiene errores
                                reply.setContent(myAgent.gatewayAgentName + ":OK & PLC:W");
                            } else if (answer.getContent().equals("Not working")) { //PLC esta a la espera de recibir plan
                                reply.setContent(myAgent.gatewayAgentName + ":OK & PLC:NW");
                            } else if (answer.getContent().equals("Error while working")) { //Error mientras se encontraba trabajando
                                reply.setContent(myAgent.gatewayAgentName + ":OK & PLC:EW");
                            } else if (answer.getContent().equals("Error while not working")) { //Error sin estar trabajando
                                reply.setContent(myAgent.gatewayAgentName + ":OK & PLC:ENW");
                            } else {
                                reply.setContent(myAgent.gatewayAgentName + ":OK & PLC:?");
                            }
                        } else {
                            reply.setContent(myAgent.gatewayAgentName + ":OK & PLC:?"); //con el tecnomatix en pausa siempre se devuelve esto porque no contesta
                        }
                    }
                }else{
                    reply.setContent("Alive");
                }
                myAgent.send(reply);
            }else{ //si el mensaje de ping tiene content, es una consulta de si hemos recibido un mensaje anteriormente "te ha llegado este mensaje:...?"
                msgFIFO = myAgent.recieved_msgs;
                boolean found=false;
                if(msgFIFO!=null) {
                    for (int i = 0; i < msgFIFO.size(); i++) {
                        if (msg.getContent().equals(msgFIFO.get(i).toString())) {
                            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                            reply.addReceiver(msg.getSender());
                            reply.setOntology(msg.getOntology());
                            reply.setContent("Y");
                            myAgent.send(reply);
                            found = true;
                            break;
                        }
                    }
                    if(!found){
                        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                        reply.addReceiver(msg.getSender());
                        reply.setOntology(msg.getOntology());
                        reply.setContent("N");
                        myAgent.send(reply);
                    }
                }else{  //si es un ping sin content simplemente contestamos
                    ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                    reply.addReceiver(msg.getSender());
                    reply.setOntology(msg.getOntology());
                    reply.setContent("pong");
                    myAgent.send(reply);
                }
            }
        }
    }


@Override
    public boolean done(){return false;}
}
