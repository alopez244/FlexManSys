package es.ehu.platform.template;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.lang.acl.MessageTemplate;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.*;
import es.ehu.platform.utilities.*;


public class ApplicationAgentTemplate extends MWAgent {

  private static final long serialVersionUID = 2476743710831028702L;
  static final Logger LOGGER = LogManager.getLogger(ApplicationAgentTemplate.class.getName());

  private static final String ST_BOOT = "boot";

  /**
   * Application Name
   */
  public String applicationName;

  /**
   * Application Model DOM
   */
  public Document applicationModel;

  @Override
  protected void setup() {

    this.initTransition = ControlBehaviour.RUNNING;

    /** Comportamiento Agente FSM **/
    FSMBehaviour behaviourFSM = new FSMBehaviour(this);

    MessageTemplate runTemplates = variableInitialization(getArguments(), behaviourFSM);

    /** Comportamiento boot **/
    Behaviour boot = new BootBehaviour(this);

    /** Comportamiento running **/
    Behaviour running = new RunningBehaviour(this);

    /** Comportamiento de ping **/

    Behaviour ping = new PingBehaviour(this);
    
    /** Tracking Behaviour **/
    Behaviour tracking = new TrackingBehaviour(this);

    /** Comportamiento negociación **/
    Behaviour negotiating = new NegotiatingBehaviour(this);
    Behaviour waitingForDecision = new WaitingForDecisionBehaviour(this);

    /** Comportamiento end **/
    Behaviour end = new EndBehaviour(this);

    /** FSM state definition **/
    behaviourFSM.registerFirstState(new StateParallel(this, behaviourFSM, boot), ST_BOOT);
    behaviourFSM.registerState(new StateParallel(this, behaviourFSM, running, ping),ControlBehaviour.ST_RUNNING);
    behaviourFSM.registerState(new StateParallel(this, behaviourFSM, tracking, ping),ControlBehaviour.ST_TRACKING);
    behaviourFSM.registerState(new StateParallel(this, behaviourFSM, waitingForDecision, ping, negotiating),ControlBehaviour.ST_WAITINGFORDECISION);
    behaviourFSM.registerLastState(end, ControlBehaviour.ST_STOP);

    /** FSM transition **/
    behaviourFSM.registerTransition(ST_BOOT, ControlBehaviour.ST_RUNNING, ControlBehaviour.RUNNING, new String[] { ST_BOOT });
    behaviourFSM.registerTransition(ST_BOOT, ControlBehaviour.ST_TRACKING, ControlBehaviour.TRACKING, new String[] { ST_BOOT });
    behaviourFSM.registerTransition(ST_BOOT, ControlBehaviour.ST_STOP, ControlBehaviour.STOP, new String[] { ST_BOOT });
    behaviourFSM.registerTransition(ST_BOOT, ControlBehaviour.ST_WAITINGFORDECISION, ControlBehaviour.WAITINGFORDECISION, new String[] { ST_BOOT });

    behaviourFSM.registerTransition(ControlBehaviour.ST_RUNNING, ControlBehaviour.ST_STOP, ControlBehaviour.STOP, new String[] { ControlBehaviour.ST_RUNNING });
    behaviourFSM.registerTransition(ControlBehaviour.ST_RUNNING, ControlBehaviour.ST_WAITINGFORDECISION, ControlBehaviour.WAITINGFORDECISION, new String[] { ControlBehaviour.ST_RUNNING });
    
    behaviourFSM.registerTransition(ControlBehaviour.ST_TRACKING, ControlBehaviour.ST_STOP, ControlBehaviour.STOP, new String[] { ControlBehaviour.ST_TRACKING });
    behaviourFSM.registerTransition(ControlBehaviour.ST_TRACKING, ControlBehaviour.ST_WAITINGFORDECISION, ControlBehaviour.WAITINGFORDECISION, new String[] { ControlBehaviour.ST_TRACKING });

    behaviourFSM.registerTransition(ControlBehaviour.ST_WAITINGFORDECISION, ControlBehaviour.ST_STOP, ControlBehaviour.STOP, new String[] { ControlBehaviour.ST_WAITINGFORDECISION });
    behaviourFSM.registerTransition(ControlBehaviour.ST_WAITINGFORDECISION, ControlBehaviour.ST_RUNNING, ControlBehaviour.RUNNING, new String[] { ControlBehaviour.ST_WAITINGFORDECISION });
    behaviourFSM.registerTransition(ControlBehaviour.ST_WAITINGFORDECISION, ControlBehaviour.ST_TRACKING, ControlBehaviour.TRACKING, new String[] { ControlBehaviour.ST_WAITINGFORDECISION });

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

