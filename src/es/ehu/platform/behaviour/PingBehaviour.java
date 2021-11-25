package es.ehu.platform.behaviour;

import es.ehu.domain.manufacturing.agents.functionality.Machine_Functionality;
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
        this.template = MessageTemplate.and(MessageTemplate.MatchOntology("ping"),
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
                reply.setOntology(msg.getOntology());
                reply.setContent("Alive");
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
