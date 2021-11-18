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

    @Override
    public void onStart() {
        super.onStart();
        myAgent.ActualState="idle";
    }

    public IdleBehaviour(MWAgent a){
        super(a);
        LOGGER.entry(a);
        LOGGER.debug("*******Idle behaviour started*******");
        this.myAgent = a;
        this.template = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchOntology("negotiation"),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST)),MessageTemplate.MatchConversationId("PLCdata"));
        LOGGER.exit();
    }
    @Override
    public void action(){
        LOGGER.entry();

        ACLMessage msg= myAgent.receive(template);
        if(msg!=null){
            LOGGER.info(msg.getContent());
        }

        LOGGER.exit();
    }


    @Override
    public boolean done(){return false;}
}

