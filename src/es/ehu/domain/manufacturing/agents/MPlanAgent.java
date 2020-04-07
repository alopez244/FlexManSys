package es.ehu.domain.manufacturing.agents;

import es.ehu.domain.manufacturing.agents.functionality.MPlan_Functionality;
import es.ehu.platform.template.ApplicationAgentTemplate;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.MessageTemplate;

public class MPlanAgent extends ApplicationAgentTemplate{

  /**
   * 
   */
  private static final long serialVersionUID = -3774619604569763790L;

  @Override
  protected MessageTemplate variableInitialization(Object[] arguments, Behaviour behaviour) {
    System.out.println("es.ehu.platform.template.ResourceAgentTemplate.variableInitialization()");
    this.functionalityInstance = new MPlan_Functionality();
    return null;
  }

  protected void takeDown() {
    try {
      //LOGGER.info("Agent: " + this.getAID().getName() + "has ended");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
