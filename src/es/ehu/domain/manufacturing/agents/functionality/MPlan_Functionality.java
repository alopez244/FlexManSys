package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.template.interfaces.AvailabilityFunctionality;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.IExecManagement;
import es.ehu.platform.utilities.XMLReader;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
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
  private Object myReplicasID = new HashMap<>();
  private MessageTemplate template;
  private ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<String>>>>>> ordersTraceability = new ArrayList<>();

  @Override
  public Object getState() {
    return null;
  }

  @Override
  public void setState(Object state) {

  }

  @Override
  public Void init(MWAgent myAgent) {

    this.template = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchOntology("Information")),MessageTemplate.MatchConversationId("BatchInfo"));
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
      myReplicasID = processACLMessages(myAgent, mySeType, elementsToCreate, conversationId, redundancy, parentAgentID);

      notifyMachinesToStartOperations(myAgent, conversationId);

    } else {
      // Si su estado es tracking
      trackingOnBoot(myAgent, mySeType, conversationId);

      myAgent.initTransition = ControlBehaviour.TRACKING;
    }


    return null;

    //TODO El planAgent tiene que esperar a la confirmacion de que todos sus hijos se han creado para notificar que pasa al estado running (o tracking si es una réplica)
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

    ACLMessage msg = myAgent.receive(template);
    if (msg != null) {

      ordersTraceability.add(deserializeMsg(msg.getContent()));
    }
    return false;
  }

  private void notifyMachinesToStartOperations(Agent agent, String conversationId) {

    // Avisar a todas las maquinas de mi plan que el plan ya esta listo para que emmpiece a hacer las operaciones

  }

  public ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<String>>>>> deserializeMsg(String msgContent) { //metodo que deserializa el mensage recibido desde el order agent

    ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<String>>>>> batchTraceability = new ArrayList<>();
    String letter = "";
    String data = "";
    int index1 = 0;
    int index2 = 0;
    int index3 = 0;
    int index4 = 0;

    for (int m = 0; m < msgContent.length(); m++) {
      letter = Character.toString(msgContent.charAt(m));
      if (letter.equals("[")) {
        batchTraceability.add(new ArrayList<>());
        for (int i = m + 1; i < msgContent.length(); i++) {
          letter = Character.toString(msgContent.charAt(i));
          if (letter.equals("[")) {
            batchTraceability.get(index1).add(new ArrayList<>());
            for (int j = i + 1; j < msgContent.length(); j++) {
              letter = Character.toString(msgContent.charAt(j));
              if (letter.equals("[")) {
                batchTraceability.get(index1).get(index2).add(new ArrayList<>());
                for (int k = j + 1; k < msgContent.length(); k++) {
                  letter = Character.toString(msgContent.charAt(k));
                  if (letter.equals("[")) {
                    batchTraceability.get(index1).get(index2).get(index3).add(new ArrayList<>());
                    for (int l = k + 1; l < msgContent.length(); l++) {
                      letter = Character.toString(msgContent.charAt(l));
                      if (letter.equals("]")) {
                        batchTraceability.get(index1).get(index2).get(index3).get(index4).add(data);
                        data = "";
                        index4++;
                        k = l;
                        break;
                      } else if (letter.equals(",")) {
                        batchTraceability.get(index1).get(index2).get(index3).get(index4).add(data);
                        data = "";
                        l++;
                      } else {
                        data = data.concat(letter);
                      }
                    }
                  } else if (letter.equals(",")) { //cuando la variable letter es igual a una coma, el siguiente caracter siempre sera un espacio, por lo que se salta
                    k++;
                  } else if (letter.equals("]")) {
                    index3++;
                    index4 = 0;
                    j = k;
                    break;
                  }
                }
              } else if (letter.equals(",")) {
                j++;
              } else if (letter.equals("]")) {
                index2++;
                index3 = 0;
                i = j;
                break;
              }
            }
          } else if (letter.equals(",")) {
            i++;
          } else if (letter.equals("]")) {
            index1++;
            index2 = 0;
            m = i;
            break;
          }
        }
      }
    }
    return batchTraceability;
  }
}
