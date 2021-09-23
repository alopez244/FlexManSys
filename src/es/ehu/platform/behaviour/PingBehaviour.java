package es.ehu.platform.behaviour;

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
        this.template = MessageTemplate.and(MessageTemplate.MatchOntology("ping"),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        LOGGER.exit();
    }
@Override
    public void action(){
        LOGGER.entry();
        ACLMessage msg = myAgent.receive(template);
        if(msg!=null) {
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.addReceiver(msg.getSender());
            reply.setOntology(msg.getOntology());
            reply.setContent("Yes :)");
            LOGGER.warn(msg.getSender().getLocalName()+" sent a ping. Answering.");
            myAgent.send(reply);
        }
    }


@Override
    public boolean done(){return false;}
}
