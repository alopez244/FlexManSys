package es.ehu.domain.manufacturing.behaviour;

import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.AssetManagement;
import jade.core.behaviours.SimpleBehaviour;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SendTaskBehaviour extends SimpleBehaviour {

    static final Logger LOGGER = LogManager.getLogger(SendTaskBehaviour.class.getName());

    private MWAgent myAgent;
    private AssetManagement aAssetManagement;

    public SendTaskBehaviour(MWAgent a) {
        super(a);
        LOGGER.debug("*** Constructing RunningBehaviour ***");
        this.myAgent = a;
    }

    @Override
    public void action() {
        LOGGER.entry();

 //       this.aAssetManagement.sendDataToPLC();

        LOGGER.exit();
    }

    @Override
    public boolean done() {
        return false;
    }
}
