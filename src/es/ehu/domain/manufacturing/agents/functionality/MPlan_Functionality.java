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
  public Void init(MWAgent myAgent) {
    LOGGER.entry(myAgent);
    this.myAgent = myAgent;

    // lanzo todos mis hijos
    //String hijos = ((MWAgent)myAgent).sendCommand("sestart (get * parent=(get "+myAgent.getLocalName()+" attrib=parent) category=application)").getContent();
    
        
    // localneg (entre quienes: con restricción en el xml, criterio (piñon), acción (newInstance, cambiar firstState)
        // añadir al array cada las negociaciones (hijos) 
    // entre quienes: del xml hay que buscar las restricciones cada aplicación: restrictionList se=procNode
    // filtro a los procnodes que tengan la restrictionList < service
    
    // recibir respuestas de negociación y cambios de estado de agentes :
       // - NEG_FAIL 
          //algo habrá que hacer con esto > healthMonitor
       // - NEG_RETRY:
          // lanzar de nueva negociación (añadiéndola al array)
          // remove (actual)
       // - NEG_WON
          // remove (actual)
          // comprobar si han terminado todas 
    
      //  - Agente pasa a running
      //   - START_OK comprobar si todos en running > si true > salgo al firstState
       //  - START_FAIL
    
    LOGGER.debug("Espero mensajes");
//    while (true) {
//      ACLMessage msg = myAgent.blockingReceive();
//      if (msg != null) {
//        LOGGER.debug("Recibo mensaje");
//        //String conversationId = msg.getConversationId();
//
//        if (msg.getContent().equals("")) break;
//      } 
//      
//    }

    
    
    return null;
  }

  
  
  
  @Override
  public Object execute(Object[] input) {
    System.out.println("hola-execute!");
    // recibir aviso de caidas > health monitor, ajusta el número de réplicas
    // recibe actualización de los estados de los hijos > no va al system model
    // actualizar modelo de datos a los trackings
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object getState() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setState(Object state) {
    // TODO Auto-generated method stub
    
  }

}
