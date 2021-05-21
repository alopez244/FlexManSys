package es.ehu.domain.manufacturing.behaviour;

import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.AssetManagement;
import es.ehu.platform.template.interfaces.NegFunctionality;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SendTaskBehaviour extends SimpleBehaviour {

    // TODO ESTE COMPORTAMIENTO NO SE UTILIZA PARA NADA. SE PUEDE ELIMINAR.

    static final Logger LOGGER = LogManager.getLogger(SendTaskBehaviour.class.getName());

    private MessageTemplate template;
    private MWAgent myAgent;
    private AssetManagement aAssetManagement;

    public SendTaskBehaviour(MWAgent a) {
        super(a);
        LOGGER.debug("*** Constructing RunningBehaviour ***");
        this.myAgent = a;
        this.aAssetManagement = (AssetManagement) a.functionalityInstance;

    }

    @Override
    public void action() {
        LOGGER.entry();

            block();

        LOGGER.exit();
    }

    @Override
    public boolean done() {
        return false;
    }
}
