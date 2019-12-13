package es.ehu.domain.manufacturing.behaviour;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.domain.manufacturing.agent.MC_Agent;
import jade.core.behaviours.OneShotBehaviour;

public class EndBehaviour extends OneShotBehaviour {
  private static final long serialVersionUID = -2673578185687045396L;
  static final Logger LOGGER = LogManager.getLogger(EndBehaviour.class.getName());
  
  MC_Agent myAgent;
  
  public EndBehaviour(MC_Agent a) {
    super(a);
    myAgent = a;
    
  }

  @Override
  public void action() {
    try {
      ((MC_Agent)myAgent).deregisterAgent(myAgent.getLocalName());
      myAgent.functionalityInstance.stopMCcode();
      myAgent.doDelete();
    } catch (Exception e) {
      e.printStackTrace();
    }
  } // end action

  public int onEnd() {
    return 1;
  }

 

}
