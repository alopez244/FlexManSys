package es.ehu.platform.template;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.lang.acl.MessageTemplate;

import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.*;
import es.ehu.platform.utilities.*;
import org.w3c.dom.Document;

public class ResourceAgentTemplate extends MWAgent {

    private static final long serialVersionUID = 2476743710831028702L;
    static final Logger LOGGER = LogManager.getLogger(ResourceAgentTemplate.class.getName());

    private static final String ST_BOOT = "boot";

    /**
     * Resource Name
     */
    public String resourceName;

    /**
     * Resource Model DOM
     */
    public Document resourceModel;

    @Override
    protected void setup() {

        this.initTransition = ControlBehaviour.RUNNING;

        /** Comportamiento Agente FSM **/
        FSMBehaviour behaviourFSM = new FSMBehaviour(this);

        MessageTemplate runTemplates = variableInitialization(getArguments(), behaviourFSM);

        /** Comportamiento boot **/
        Behaviour boot = new ResourceBootBehaviour(this);

        /** Comportamiento running **/
        Behaviour running = new ResourceRunningBehaviour(this);

        /** Comportamiento negociación **/
        Behaviour negotiating = new NegotiatingBehaviour(this);

        /** Comportamiento end **/
        Behaviour end = new EndBehaviour(this);

        /** FSM state definition **/
        behaviourFSM.registerFirstState(new StateParallel(this, behaviourFSM, boot), ST_BOOT);
        behaviourFSM.registerState(new StateParallel(this, behaviourFSM, running, negotiating), ControlBehaviour.ST_RUNNING);
        behaviourFSM.registerLastState(new StateParallel(this, behaviourFSM, end), ControlBehaviour.ST_STOP);

        /** FSM transition **/
        behaviourFSM.registerTransition(ST_BOOT, ControlBehaviour.ST_RUNNING, ControlBehaviour.RUNNING, new String[] { ST_BOOT });

        behaviourFSM.registerTransition(ST_BOOT, ControlBehaviour.ST_STOP, ControlBehaviour.STOP, new String[] { ST_BOOT });

        behaviourFSM.registerTransition(ControlBehaviour.ST_RUNNING, ControlBehaviour.ST_STOP, ControlBehaviour.STOP, new String[] { ControlBehaviour.ST_RUNNING });

        this.addBehaviour(behaviourFSM);

        LOGGER.debug("FSM start");
    }

    /**
     * Allows variable initialization
     */
    protected MessageTemplate variableInitialization(Object[] arguments, Behaviour behaviour) {
        return null;
    }

    protected void takeDown() {
        try {
            LOGGER.info("Agent: " + this.getAID().getName() + "has ended");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

