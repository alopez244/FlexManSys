package es.ehu.domain.manufacturing.behaviour;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.domain.manufacturing.agent.MC_Agent;
import es.ehu.domain.manufacturing.lib.FunctionalityPLC;
import jade.core.behaviours.OneShotBehaviour;

public class BootBehaviour extends OneShotBehaviour {
  private static final long serialVersionUID = -2673578185687045396L;
  static final Logger LOGGER = LogManager.getLogger(BootBehaviour.class.getName());
  
  MC_Agent myAgent;
  String XMLState, codeExecutionFlag, stateArray, XMLDiagnosis;
  
  public BootBehaviour(MC_Agent a, String XMLState, String codeExecutionFlag, String stateArray, String XMLDiagnosis) {
    super(a);
    myAgent = a;
    myAgent.functionalityInstance = new FunctionalityPLC();
    this.XMLState=XMLState;
    this.codeExecutionFlag=codeExecutionFlag;
    this.stateArray=stateArray;
    this.XMLDiagnosis=XMLDiagnosis;
  }

  @Override
  public void action() {
    LOGGER.entry();
    try {
      // --------------- inicializacion ----------------
      myAgent.MWInit(myAgent.targetComponentIDs, myAgent.sourceComponentIDs, myAgent);
      
      myAgent.functionalityInstance.initialization(XMLState, codeExecutionFlag, stateArray, XMLDiagnosis);
      
    } catch (Exception e) {
      e.printStackTrace();
      LOGGER.warn(e.getMessage());
    }
    LOGGER.exit();
  } // end action

  public int onEnd() {
    return myAgent.initTransition;
  }

}
