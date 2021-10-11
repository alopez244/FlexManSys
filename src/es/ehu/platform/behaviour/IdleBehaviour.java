package es.ehu.platform.behaviour;

import es.ehu.platform.MWAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IdleBehaviour extends SimpleBehaviour {

    private MessageTemplate template;
    private MWAgent myAgent;
    static final Logger LOGGER = LogManager.getLogger(IdleBehaviour.class.getName());

    public IdleBehaviour(MWAgent a){
        super(a);
        LOGGER.entry(a);
        LOGGER.debug("*******Idle behaviour started*******");
        this.myAgent = a;
//        this.template = MessageTemplate.and(MessageTemplate.MatchOntology("recover"),
//                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        LOGGER.exit();
    }
    @Override
    public void action(){
//        LOGGER.entry();
//
//
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        LOGGER.debug("********Idle********");

    }


    @Override
    public boolean done(){return false;}
}

