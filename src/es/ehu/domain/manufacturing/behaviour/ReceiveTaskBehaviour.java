package es.ehu.domain.manufacturing.behaviour;

import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.AssetManagement;
import es.ehu.platform.template.interfaces.Traceability;
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
    private Traceability traceability;

    public ReceiveTaskBehaviour(MWAgent a) {
        super(a);
        LOGGER.debug("*** Constructing RunningBehaviour ***");
        this.myAgent = a;
        this.aAssetManagement = (AssetManagement) a.functionalityInstance;
        this.template = MessageTemplate.and(MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),MessageTemplate.MatchPerformative(ACLMessage.INFORM)),
               MessageTemplate.or(MessageTemplate.MatchOntology("negotiation"),MessageTemplate.MatchOntology("data")));
    }

    @Override
    public void action() {

        System.out.println("ReceiveTaskBehaviour Initilized");
        LOGGER.entry();

        aAssetManagement.sendDataToDevice();
        ACLMessage acl;
        aAssetManagement.rcvDataFromDevice( acl = new ACLMessage(16));
        System.out.println("sendDataeginda");

        ACLMessage msg = myAgent.receive(template); // If ACL Message template matches, rcvDataFromPLC and recvBatchInfo methods are called
        if (msg != null) {

            this.aAssetManagement.rcvDataFromDevice(msg);  // processes the information of the received message and updates the machine plan

        } else {
            System.out.println("msg == null");
            block();
        }

        LOGGER.exit();
    }

    @Override
    public boolean done() {
        return false;
    }
}
