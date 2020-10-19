package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.AvailabilityFunctionality;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.IExecManagement;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
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
  private int chatID = 0; // Numero incremental para crear conversationID

  private String firstState;
  private String redundancy;
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

    firstState = getArgumentOfAgent(myAgent, "firstState");
    redundancy = getArgumentOfAgent(myAgent, "redundancy");

    // TODO SOLO HACER TODO ESTO SI NO ES UNA REPLICA?
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
      attributes.put("seClass", "es.ehu.domain.manufacturing.agents.OrderAgent");

      String status = seStart(myAgent.getLocalName(), attributes, conversationId);
      if (status == null)
        System.out.println("OrderAgents created");
      else if (status == "-1")
        System.out.println("ERROR creating OrderAgent -> No existe el ID del plan");
      else if (status == "-4")
        System.out.println("ERROR creating OrderAgent -> No targets");

      // TODO primero comprobara que todas las replicas (tracking) y los orders se han creado correctamente
      // Es decir, antes de avisar a su padre que esta creado, comprueba las replicas y los orders
      // Le añadimos un comportamiento para que consiga todos los mensajes que le van a enviar los orders cuando se arranquen correctamente

      myReplicasID = behaviourToGetMyElementsMessages(myAgent, "MPlan", myOrders, conversationId, redundancy, "Order");

    } else {
      // Si su estado es tracking


      String runningAgentID = null;
      try {
        String parentID = null;
        ACLMessage reply = sendCommand(myAgent,"get " + myAgent.getLocalName() + " attrib=parent", conversationId);
        if (reply != null)
          parentID = reply.getContent();
        reply = sendCommand(myAgent, "get * parent=" + parentID + " category=mPlanAgent state=bootToRunning", conversationId);
        if (reply !=null) {
          runningAgentID = reply.getContent();
          sendElementCreatedMessage(myAgent, runningAgentID, "MPlan", true);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      // Una vez mande el mensaje, registra que su estado es el tracking
      try {
        sendCommand(myAgent, "set " + myAgent.getLocalName() + " state=" + firstState, conversationId);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return null;

    //TODO El planAgent tiene que esperar a la confirmacion de que todos sus hijos se han creado para notificar que pasa al estado running (o tracking si es una réplica)
  }

  @Override
  public String seStart(String seID, Hashtable<String, String> attribs, String conversationId){

    this.myOrders = getAllElements(myAgent, seID, "order", conversationId);

    chatID = createAllElementsAgents(myAgent, myOrders, attribs, conversationId, redundancy, chatID);


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
