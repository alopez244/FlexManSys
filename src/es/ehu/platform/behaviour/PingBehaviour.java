package es.ehu.platform.behaviour;

import jade.core.AID;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.platform.MWAgent;
import es.ehu.domain.manufacturing.agents.functionality.Batch_Functionality;
import es.ehu.domain.manufacturing.agents.functionality.Order_Functionality;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import java.util.ArrayList;


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
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
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

                if(myAgent.getLocalName().contains("machine")&&msg.getOntology().equals("ping_PLC")){

                    ACLMessage PLC_ping=new ACLMessage(ACLMessage.REQUEST);
                    AID myGW=new AID(myAgent.gatewayAgentName,false);
                    PLC_ping.addReceiver(myGW);
                    //primero hay que chekcear el estado del GW con un ping
                    ACLMessage GW_ping=new ACLMessage(ACLMessage.REQUEST);
                    GW_ping.setOntology("ping");
                    GW_ping.setContent("");
                    GW_ping.addReceiver(myGW);
                    myAgent.send(GW_ping);
                    ACLMessage GW_state= myAgent.blockingReceive(MessageTemplate.and(MessageTemplate.MatchOntology("ping"),MessageTemplate.MatchPerformative(7)),300);
                    if(GW_state==null){
                        reply.setContent(myAgent.gatewayAgentName+":DOWN\n"+"PLC:?");
                    }else {
                        PLC_ping.setOntology("check_asset");
                        PLC_ping.setContent("ask_state");
                        myAgent.send(PLC_ping);
                        ACLMessage answer = myAgent.blockingReceive(MessageTemplate.MatchOntology("asset_state"), 300);
                        if (answer != null) {
                            if (answer.getContent().equals("Working")) {
                                reply.setContent(myAgent.gatewayAgentName + ":OK\n" + "PLC:W");
                            } else if (answer.getContent().equals("Not working")) {
                                reply.setContent(myAgent.gatewayAgentName + ":OK\n" + "PLC:NW");
                            } else if (answer.getContent().equals("Error while working")) {
                                reply.setContent(myAgent.gatewayAgentName + ":OK\n" + "PLC:EW");
                            } else if (answer.getContent().equals("Error while not working")) {
                                reply.setContent(myAgent.gatewayAgentName + ":OK\n" + "PLC:ENW");
                            } else {
                                reply.setContent(myAgent.gatewayAgentName + ":OK\n" + "PLC:?");
                            }
                        } else {
                            reply.setContent(myAgent.gatewayAgentName + ":OK\n" + "PLC:?"); //con el tecnomatix en pausa siempre se devuelve esto aunque el GW este bien
                        }
                    }
                }else{
                    reply.setContent("Alive");
                }
                myAgent.send(reply);
            }else{
                msgFIFO = myAgent.msgFIFO;
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
                        }
                    }
                    if(!found){
                        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                        reply.addReceiver(msg.getSender());
                        reply.setOntology(msg.getOntology());
                        reply.setContent("N");
                        myAgent.send(reply);
                    }
                }else{
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
