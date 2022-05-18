package es.ehu.domain.manufacturing.agents.functionality;

import com.google.gson.Gson;
import es.ehu.domain.manufacturing.utilities.StructMplanAgentState;
import es.ehu.domain.manufacturing.utilities.StructOrderAgentState;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.template.interfaces.AvailabilityFunctionality;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.utilities.XMLWriter;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class MPlan_Functionality extends DomApp_Functionality implements BasicFunctionality, AvailabilityFunctionality {
  /**
   * 
   */
  private static final long serialVersionUID = -4078504089052783841L;
  static final Logger LOGGER = LogManager.getLogger(MPlan_Functionality.class.getName()) ;
  
  private MWAgent myAgent;

  private List<String> myElements;
  private List<String> elementsToCreate = new ArrayList<>();
  private HashMap<String, String> elementsClasses;
  private int chatID = 0; // Numero incremental para crear conversationID
  private AID QoSID = new AID("QoSManagerAgent", false);
  private String firstState, redundancy, parentAgentID, planNumber;
  private Boolean newOrder = true, firstTime = true;
//  private ArrayList<AID> sonAgentID = new ArrayList<>();
  private ArrayList<String> sonAgentID = new ArrayList<>();
//  private ArrayList<String> myReplicasID = new ArrayList<>();
  private ArrayList<ArrayList<String>> finishtimes = new ArrayList<>();
  private String mySeType;
  private MessageTemplate template,template2,template3;
  private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> ordersTraceability = new ArrayList<>();
  private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> deserializedMessage = new ArrayList<>();
  private Integer orderIndex = 1;

  private int convIDcnt=0;



  @Override
  public void setState(String state) {

    Gson gson = new Gson();
    StructMplanAgentState MPAstate = gson.fromJson(state, StructMplanAgentState.class);
    ordersTraceability=MPAstate.getordersTraceability();
    sonAgentID=MPAstate.getsonAgentID();
    firstTime=MPAstate.getfirstTime();
    parentAgentID=MPAstate.getparenAgentID();
    myAgent.replicas=MPAstate.getreplicas();
    newOrder=MPAstate.getnewOrder();
    orderIndex=MPAstate.getorderIndex();

  }
  @Override
  public String getState(){

    myAgent.replicas=update_tracking_replicas();
    StructMplanAgentState MPAstate=new StructMplanAgentState();
    MPAstate.setordersTraceability(ordersTraceability);
    MPAstate.setsonAgentID(sonAgentID);
    MPAstate.setfirstTime(firstTime);
    MPAstate.setparenAgentID(parentAgentID);
    MPAstate.setreplicas(myAgent.replicas);
    MPAstate.setnewOrder(newOrder);
    MPAstate.setorderIndex(orderIndex);

    Gson gson = new Gson();
    String state=gson.toJson(MPAstate);

    return state;
  }

  @Override
  public Void init(MWAgent myAgent) {

    this.myAgent = myAgent;
    this.template = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchOntology("Information")),MessageTemplate.MatchConversationId("OrderInfo"));
    this.template2=MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchContent("Order completed")),MessageTemplate.MatchConversationId("Shutdown"));

    myAgent.get_timestamp(myAgent,"CreationTime");
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
      Object[] result = processACLMessages(myAgent, mySeType, elementsToCreate, conversationId, redundancy, parentAgentID);
      sonAgentID = (ArrayList<String>) result[1];
      myAgent.replicas = (ArrayList<String>) result[0];

      notifyMachinesToStartOperations(myAgent, conversationId);

      String currentState = (String) ((AvailabilityFunctionality) myAgent.functionalityInstance).getState(); //se actualiza el estado de las replicas
      if (currentState != null) {
        System.out.println("Send state");
        myAgent.sendStateToTracking(currentState,"mplan"); //comunicamos a las replicas nuestro estado
      }

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

    if (input!= null) {
      if (input[0] != null) {
//        System.out.println("El agente " + myAgent.getLocalName() + " esta en el metodo execute de su estado running");
        ACLMessage msg = (ACLMessage) input[0];
        myAgent.msgFIFO.add((String) msg.getContent());
        if(msg.getPerformative()==ACLMessage.INFORM&&msg.getOntology().equals("Information")&&msg.getConversationId().equals("OrderInfo")){
//          Acknowledge(msg,myAgent);
          if (firstTime) { //solo se quiere añadir el nuevo nivel la primera vez
            deserializedMessage = deserializeMsg(msg.getContent());
            ordersTraceability = addNewLevel(ordersTraceability, deserializedMessage, true); //añade el espacio para la informacion de la orden en primera posicion, sumando un nivel mas a los datos anteriores
            ordersTraceability.get(0).get(0).get(0).add("PlanLevel"); // en ese espacio creado, se añade la informacion
            ordersTraceability.get(0).get(0).get(2).add("planReference");
            String orderNumber = ordersTraceability.get(1).get(0).get(3).get(0);  //se obtiene la referencia de la orden
            planNumber = orderNumber.substring(0,1);  // se obtiene la referencia del plan
            ordersTraceability.get(0).get(0).get(3).add(planNumber);
            firstTime = false;
          } else {
            if (newOrder == false) {
              for (int i = ordersTraceability.size() - 1; i >= orderIndex; i--) {
                ordersTraceability.remove(i); //se elimina el ultimo order añadido para poder sobreescribirlo
              }
            }
            deserializedMessage = deserializeMsg(msg.getContent());
            ordersTraceability = addNewLevel(ordersTraceability, deserializedMessage, false); //añade el espacio para la informacion del plan en primera posicion, sumando un nivel mas a los datos anteriores
          }
          newOrder = false;
        }else if(msg.getPerformative()==ACLMessage.INFORM&&msg.getContent().equals("Order completed")&&msg.getConversationId().equals("Shutdown")){
          String msgSender = msg.getOntology();
          for (int i = 0; i < sonAgentID.size(); i++) {
//          if (sonAgentID.get(i).getName().split("@")[0].equals(msgSender)) {
            if (sonAgentID.get(i).equals(msgSender)) {
              sonAgentID.remove(i);
              i--;
            }
          }
          if (sonAgentID.size() == 0) { // todos los batch agent de los que es padre ya le han enviado la informacion
            //se adecuan los datos antes de llamar al metodo XMLwrite
            ArrayList<ArrayList<ArrayList<String>>> toXML = new ArrayList<>();
            for (int j = 0; j < ordersTraceability.size(); j++) {
              for (int k = 0; k < ordersTraceability.get(j).size(); k++) {
                toXML.add(ordersTraceability.get(j).get(k));
              }
            }
            //XMLWriter.writeFile(toXML, planNumber);//se introducen como entrada los datos a convertir y el identificador del MPlan
//          sendACLMessage(7, myAgent.getAID(), "Information", "Shutdown", "Shutdown", myAgent); // autoenvio de mensaje para asegurar que el agente de desregistre y se apague
            return true;
          }
          orderIndex = ordersTraceability.size() - 1; //se actualiza el valor para borrar en el nuevo rango
          newOrder = true; // aun quedan orders por añadir
        }


      }
    }

    return false;
  }

  @Override
  public Void terminate(MWAgent myAgent) {
    this.myAgent = myAgent;
    String parentName = "";
    unregister_from_node();
    myAgent.get_timestamp(myAgent,"FinishTime");
  if(myAgent.ActualState=="running"){ //para filtrar las replicas ejecutando terminate
    try {
      String planName = "MPlan" + planNumber;
      ACLMessage reply = sendCommand(myAgent, "get * name=" + planName, "parentAgentID");
      //returns the names of all the agents that are sons
      if (reply != null)   // Si no existe el id en el registro devuelve error
        parentName = reply.getContent(); //gets the name of the agent´s parent
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      AID Agent = new AID(parentAgentID, false);
//      KillReplicas(myAgent.replicas);
      KillReplicas(myAgent);
      sendACLMessage(7, Agent, "Information", "Shutdown", "Manufacturing Plan has been completed", myAgent);
      myAgent.deregisterAgent(parentName);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }


    return null;
  }

  protected ArrayList<String> get_local_names(ArrayList<AID> SonAIDs){

    ArrayList<String> SonLocalNames= new ArrayList<String>();
    for(int i=0; i<SonAIDs.size();i++){
      SonLocalNames.add(SonAIDs.get(i).getLocalName());
    }
    return SonLocalNames;
  }
  private void notifyMachinesToStartOperations(Agent agent, String conversationId) {

    // Avisar a todas las maquinas de mi plan que el plan ya esta listo para que emmpiece a hacer las operaciones

  }
}
