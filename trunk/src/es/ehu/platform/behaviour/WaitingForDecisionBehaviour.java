package es.ehu.platform.behaviour;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.platform.MWAgent;
import jade.core.behaviours.OneShotBehaviour;

public class WaitingForDecisionBehaviour extends OneShotBehaviour {
  private static final long serialVersionUID = -2673578185687045396L;
  static final Logger LOGGER = LogManager.getLogger(WaitingForDecisionBehaviour.class.getName()) ;
  
  MWAgent myAgent;
  
  public WaitingForDecisionBehaviour(MWAgent a) {
    super(a);
    myAgent = a;
    
  }

  @Override
  public void action() {
    
  } // end action

  public int onEnd() {
    return 1;
  }

 

}
