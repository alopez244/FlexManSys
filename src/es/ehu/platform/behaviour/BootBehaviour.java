package es.ehu.platform.behaviour;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.platform.MWAgent;
import jade.core.behaviours.OneShotBehaviour;

public class BootBehaviour extends OneShotBehaviour {
  private static final long serialVersionUID = -2673578185687045396L;
  static final Logger LOGGER = LogManager.getLogger(BootBehaviour.class.getName()) ;
  
  MWAgent myAgent;


  public BootBehaviour(MWAgent a) {
    super(a);
    myAgent = a;
    
  }

  @Override
  public void action() {
    LOGGER.entry();
    try {
      // --------------- inicializacion ----------------

      myAgent.functionalityInstance.init(myAgent);
      myAgent.MWInit(myAgent.targetComponentIDs, myAgent.sourceComponentIDs, myAgent);

      //if (myAgent.initialExecutionState!=null) myAgent.functionalityInstance.setState(myAgent.initialExecutionState);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    LOGGER.exit();
  } // end action

  public int onEnd() {
    LOGGER.info("devuelvo "+myAgent.initTransition);

    return myAgent.initTransition;
  }

}
