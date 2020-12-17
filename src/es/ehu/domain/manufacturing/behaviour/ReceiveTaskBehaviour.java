package es.ehu.domain.manufacturing.behaviour;

import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.AssetManagement;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.HashMap;

public class ReceiveTaskBehaviour extends SimpleBehaviour {

    static final Logger LOGGER = LogManager.getLogger(SendTaskBehaviour.class.getName());

    private MessageTemplate template;
    private MWAgent myAgent;
    private AssetManagement aAssetManagement;

    public ReceiveTaskBehaviour(MWAgent a) {
        super(a);
        LOGGER.debug("*** Constructing RunningBehaviour ***");
        this.myAgent = a;
        this.aAssetManagement = (AssetManagement) a.functionalityInstance;
        this.template = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchOntology("negotiation")),MessageTemplate.MatchConversationId("PLCdata"));
    }

    @Override
    public void action() {
        LOGGER.entry();
        ACLMessage msg = myAgent.receive(template);
        if (msg != null) {
            System.out.println("-->Received message: " + msg.getContent());

            this.aAssetManagement.rcvData(msg);

        } else {
            block();
        }

        LOGGER.exit();
    }

    @Override
    public boolean done() {
        return false;
    }
}
