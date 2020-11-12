package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.template.interfaces.AvailabilityFunctionality;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.IExecManagement;
import jade.core.Agent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class MPlan_Functionality extends DomApp_Functionality implements BasicFunctionality, AvailabilityFunctionality {
  /**
   * 
   */
  private static final long serialVersionUID = -4078504089052783841L;
  static final Logger LOGGER = LogManager.getLogger(MPlan_Functionality.class.getName()) ;
  
  private Agent myAgent;

  private List<String> myElements;
  private List<String> elementsToCreate = new ArrayList<>();
  private HashMap<String, String> elementsClasses;
  private int chatID = 0; // Numero incremental para crear conversationID

  private String firstState;
  private String redundancy;
  private String parentAgentID;
  private String mySeType;
  private ArrayList<String> myReplicasID = new ArrayList<>();

  @Override
  public Object getState() {
    return null;
  }

  @Override
  public void setState(Object state) {

  }

  @Override
  public Void init(MWAgent myAgent) {

    this.myAgent = myAgent;

    // Crear un nuevo conversationID
    String conversationId = myAgent.getLocalName() + "_" + chatID;

    // Conseguir los datos de los parametros del agente
    firstState = getArgumentOfAgent(myAgent, "firstState");
    redundancy = getArgumentOfAgent(myAgent, "redundancy");
    parentAgentID = getArgumentOfAgent(myAgent, "parentAgent");
    mySeType = getMySeType(myAgent, conversationId);

    // TODO PRUEBA --> BORRAR
    try {
      sendCommand(myAgent, "sestart " +myAgent.getLocalName()+ " category=" + mySeType, conversationId);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (firstState.equals("running")) {

      // Cambiar a estado bootToRunning para que los tracking le puedan enviar mensajes
      String query = "set " + myAgent.getLocalName() + " state=bootToRunning";
      try {
          sendCommand(myAgent, query, conversationId);
      } catch (Exception e) {
          e.printStackTrace();
      }

      Hashtable<String, String> attributes = new Hashtable<String, String>();
      // TODO ponerlo en DomApp --> parametro para la clase
      //attributes.put("seClass", "es.ehu.domain.manufacturing.agents.OrderAgent");

      seStart(myAgent.getLocalName(), attributes, conversationId);

      // TODO primero comprobara que todas las replicas (tracking) y los orders se han creado correctamente
      // Es decir, antes de avisar a su padre que esta creado, comprueba las replicas y los orders
      // Le a�adimos un comportamiento para que consiga todos los mensajes que le van a enviar los orders cuando se arranquen correctamente

      //Aqui cuiado con el myOrders, si utilizamos elementsToCreate en seStart aqui tambien hay que meterlo
      myReplicasID = processACLMessages(myAgent, mySeType, elementsToCreate, conversationId, redundancy, parentAgentID);

    } else {
      // Si su estado es tracking
      trackingOnBoot(myAgent, mySeType, conversationId);

      myAgent.initTransition = ControlBehaviour.TRACKING;
    }

    // TODO PRUEBA --> BORRAR
    try {
      sendCommand(myAgent, "set " +myAgent.getLocalName()+ " execution_phase=started", conversationId);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;

    //TODO El planAgent tiene que esperar a la confirmacion de que todos sus hijos se han creado para notificar que pasa al estado running (o tracking si es una r�plica)
  }


  public String seStart(String seID, Hashtable<String, String> attribs, String conversationId){

    ArrayList<String> creationCategories = new ArrayList<>();
    creationCategories.add("order");  // Aqui decidiremos que tipos de elementos queremos crear --> Order, Batch, las dos...

    List result = seStart(myAgent, seID, attribs, conversationId, creationCategories, chatID, redundancy);
    elementsToCreate = (List<String>) result.get(0);
    chatID = (int) result.get(1);

    return null;
  }


  public String seStop(String... seID) {
    return null;
  }

  @Override
  public Object execute(Object[] input) {
    System.out.println("El agente " + myAgent.getLocalName() + " esta en el metodo execute de su estado running");
    return null;
  }


}
