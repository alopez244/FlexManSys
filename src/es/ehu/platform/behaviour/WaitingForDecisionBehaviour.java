package es.ehu.platform.behaviour;

import jade.core.behaviours.SimpleBehaviour;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.platform.MWAgent;
import jade.core.behaviours.OneShotBehaviour;

public class WaitingForDecisionBehaviour extends SimpleBehaviour {
  private static final long serialVersionUID = -2673578185687045396L;
  static final Logger LOGGER = LogManager.getLogger(WaitingForDecisionBehaviour.class.getName()) ;
  
  MWAgent myAgent;

  @Override
  public void onStart() {
    super.onStart();
    LOGGER.info("Waiting for negotiation result.");
    myAgent.ActualState="waitingfordecision";
  }

  public WaitingForDecisionBehaviour(MWAgent a) {
    super(a);
    myAgent = a;
    
  }

  @Override
  public void action() {
    
  } // end action

//  public int onEnd() {
//    return 1;
//  }
@Override
public boolean done(){return false;}
 

}
