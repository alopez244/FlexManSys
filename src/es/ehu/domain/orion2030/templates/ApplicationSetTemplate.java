package es.ehu.domain.orion2030.templates;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.MessageTemplate;
import es.ehu.domain.orion2030.templates.functionality.ApplicationSet_Functionality;
import es.ehu.platform.template.ApplicationAgentTemplate;

public class ApplicationSetTemplate extends ApplicationAgentTemplate{

  /**
   * 
   */
  private static final long serialVersionUID = -3774619604569763790L;

  @Override
  protected MessageTemplate variableInitialization(Object[] arguments, Behaviour behaviour) {
    System.out.println("es.ehu.platform.template.ResourceAgentTemplate.variableInitialization()");
    this.functionalityInstance = new ApplicationSet_Functionality();
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
