package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.AvailabilityFunctionality;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.IExecManagement;
import jade.core.Agent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class MPlan_Functionality extends DomApp_Functionality implements BasicFunctionality, AvailabilityFunctionality, IExecManagement {
  /**
   * 
   */
  private static final long serialVersionUID = -4078504089052783841L;
  static final Logger LOGGER = LogManager.getLogger(MPlan_Functionality.class.getName()) ;
  
  private Agent myAgent;

  private List<String> myOrders;
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
      // Le añadimos un comportamiento para que consiga todos los mensajes que le van a enviar los orders cuando se arranquen correctamente

      //Aqui cuiado con el myOrders, si utilizamos elementsToCreate en seStart aqui tambien hay que meterlo
      myReplicasID = processACLMessages(myAgent, mySeType, myOrders, conversationId, redundancy, parentAgentID);

    } else {
      // Si su estado es tracking
      trackingOnBoot(myAgent, mySeType, conversationId);
    }

    return null;

    //TODO El planAgent tiene que esperar a la confirmacion de que todos sus hijos se han creado para notificar que pasa al estado running (o tracking si es una réplica)
  }

  @Override
  public String seStart(String seID, Hashtable<String, String> attribs, String conversationId){

    this.myOrders = getAllElements(myAgent, seID, conversationId);  // Antes al mySeType le hemos quitado la parte de Agent, se la añadimos para este metodo
    
    this.elementsClasses = getMyElementsClasses(myAgent, myOrders);

    String creationCategory = "order";  // Aqui decidiremos que tipos de elementos queremos crear --> Order, Batch, las dos...
    attribs.put("seClass", elementsClasses.get(creationCategory));
    List<String> elementsToCreate = getELementsToCreate(myAgent, myOrders, creationCategory);

    chatID = createAllElementsAgents(myAgent, elementsToCreate, attribs, conversationId, redundancy, chatID);


    return null;
  }

  @Override
  public String seStop(String... seID) {
    return null;
  }

  @Override
  public Object execute(Object[] input) {
    return null;
  }


}
