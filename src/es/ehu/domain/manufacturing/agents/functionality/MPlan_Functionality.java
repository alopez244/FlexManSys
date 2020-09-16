package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.AvailabilityFunctionality;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import jade.core.Agent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MPlan_Functionality implements BasicFunctionality, AvailabilityFunctionality {
  /**
   * 
   */
  private static final long serialVersionUID = -4078504089052783841L;
  static final Logger LOGGER = LogManager.getLogger(MPlan_Functionality.class.getName()) ;
  
  private Agent myAgent;

  @Override
  public Object getState() {
    return null;
  }

  @Override
  public void setState(Object state) {

  }

  @Override
  public Void init(MWAgent myAgent) {
    //Aquí hay que arrancar los agentes Order asociados
    //Para eso, hay que buscarlos en el modelo en primer lugar
    return null;
  }

  @Override
  public Object execute(Object[] input) {
    return null;
  }
}
