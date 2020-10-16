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
    //Aquí hay que arrancar los agentes Order asociados
    //Para eso, hay que buscarlos en el modelo en primer lugar
    this.myAgent = myAgent;

    // Crear un nuevo conversationID
    String conversationId = myAgent.getLocalName() + "_" + chatID;

    /*
    String[] firstArgument = myAgent.getArguments()[0].toString().split("=");
    if (firstArgument[0].equals("firstState"))
      firstState = firstArgument[1];

    String[] secondArgument = myAgent.getArguments()[1].toString().split("=");
    if (secondArgument[0].equals("redundancy"))
      redundancy = secondArgument[1];

     */

      // DOMAPP
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

      // TODO primero comprobara que todas las replicas (tracking) se han creado correctamente, y despues comprobara los orders
      // Es decir, antes de avisar a su padre que esta creado, comprueba las replicas y despues los orders

      // DOMAPP
      //myReplicasID = behaviourToGetMyElementsMessages(myAgent, "MPlan", myOrders, conversationId, redundancy, "Order");



      // Le añadimos un comportamiento para que consiga todos los mensajes que le van a enviar los orders cuando se arranquen correctamente

      myAgent.addBehaviour(new SimpleBehaviour() {
        @Override
        public void action() {
          boolean moreMsg = true;
          ACLMessage msg = myAgent.receive();
          if (msg != null) {
            // TODO COMPROBAR TAMBIEN LOS TRACKING si esta bien programado (sin probar)
            if ((msg.getPerformative() == 7)) {
              if (msg.getContent().equals("Order created successfully")) {
                System.out.println("\tYa se ha creado el agente " + msg.getSender().getLocalName() + " - hay que borrarlo de la lista --> " + myOrders);
                // Primero vamos a conseguir el ID del order (ya que el mensaje nos lo envia su agente)
                String senderOrderID = null;
                try {
                  ACLMessage reply = sendCommand(myAgent,"get " + msg.getSender().getLocalName() + " attrib=parent", conversationId);
                  if (reply != null)
                    senderOrderID = reply.getContent();
                } catch (Exception e) {
                  e.printStackTrace();
                }

                // Si la order es uno de los hijos (que solo los hijos nos enviaran los mensaje, pero por se acaso), lo borramos de la lista
                if (myOrders.contains(senderOrderID))
                  myOrders.remove(senderOrderID);

              } else if (msg.getContent().equals("MPlan replica created successfully")) {
                System.out.println("\tYa se ha creado la replica " + msg.getSender().getLocalName());
                myReplicasID.add(msg.getSender().getLocalName());
              }

              // Si la lista esta vacia, todos los orders se han creado correctamente, y tendremos que pasar del estado BOOT al RUNNING
              if ((myOrders.isEmpty() && (myReplicasID.size() == Integer.parseInt(redundancy) - 1))) {
                moreMsg = false;
                // Pasar a estado running
                System.out.println("\tEl agente " + myAgent.getLocalName() + " ha finalizado su estado BOOT y pasará al estado RUNNING");

                System.out.println("Estado del agente " + myAgent.getLocalName() + ": " + myAgent.getState());

                // SystemModelAgent linea 1422
                String query = "set " + myAgent.getLocalName() + " state=" + firstState;
                try {
                  ACLMessage reply = sendCommand(myAgent, query, conversationId);
                  System.out.println(reply.getContent());
                } catch (Exception e) {
                  e.printStackTrace();
                }

                //LOGGER.exit();
                // TODO Mirar a ver como se puede hacer el cambio de estado (si lo hace la maquina de estados o hay que hacerlo desde aqui)
                // ControlBehaviour --> cambio de estado

                System.out.println("Estado del agente " + myAgent.getLocalName() + ": " + myAgent.getState());

              }
            }
          } else {
            if (moreMsg)
              // Se queda a la espera para cuando le envien mas mensajes
              block();
          }
        }

        @Override
        public boolean done() {
          return false;
        }
      });



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

          /*
          ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
          msg.addReceiver(new AID(runningAgentID, AID.ISLOCALNAME));
          msg.setContent("MPlan replica created successfully");
          myAgent.send(msg);

           */
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

    //TODO borrarlo
    if(firstState.equals("running")) {
        System.out.println("\t############################################ ALL REPLICAS OF " + myAgent.getLocalName() + ":");
        if (myReplicasID != null) {
          for (String a : myReplicasID) {
            System.out.println(a);
          }
        }
    }



    return null;

    //TODO El planAgent tiene que esperar a la confirmacion de que todos sus hijos se han creado para notificar que pasa al estado running (o tracking si es una réplica)
  }

  @Override
  public String seStart(String seID, Hashtable<String, String> attribs, String conversationId){


    // DOMAPP
    this.myOrders = getAllElements(myAgent, seID, "order", conversationId);

    chatID = createAllElementsAgents(myAgent, myOrders, attribs, conversationId, redundancy, chatID);

    /*
    // seID --> ID del mPlanAgent
    String parentQuery = "get " + seID + " attrib=parent";
    ACLMessage reply = null;

    try {
      reply = sendCommand(parentQuery, conversationId);
      // ID del plan con el cual el agente está relacionado
      String planID = null;
      if (reply == null)   // Si no existe el id en el registro devuelve error
        return "-1";
      else
        planID = reply.getContent();

      String query = "get order* parent="+ planID;  // Busco todos los order de los que el plan es parent (no se buscan todos los elementos porque pueden existir otros mPlanAgent en tracking)
      reply = sendCommand(query, conversationId);
    } catch (Exception e) {
      e.printStackTrace();
    }

    String allOrders;
    if (reply == null)    // Si no existen orders en el registro devuelve error
      return "-1";
    else
      allOrders = reply.getContent();

    //List<String> items = Arrays.asList(allOrders.split("\\s*,\\s*"));
    List<String> items = new ArrayList<>();
    String[] aux = allOrders.split(",");
    for (String a:aux)
      items.add(a);

     */

    /*
    this.myOrders = items;
    for (String orderID: items) {
      // Creamos los agentes para cada order
      try {
        reply = sendCommand("get (get * parent=(get * parent=" + orderID + " category=restrictionList)) attrib=attribValue", conversationId);
        String refServID = null;
        if (reply != null)
          refServID = reply.getContent();

        reply = sendCommand("get * category=pNodeAgent" + ((refServID.length()>0)?" refServID=" + refServID:""), conversationId);
        String targets = null;
        if (reply != null) {
          targets = reply.getContent();
          if (targets.length() <= 0)
            return "-4";
        }

        reply = sendCommand("get " + orderID + " attrib=category", conversationId);
        String seCategory = null;
        if (reply != null)
          seCategory = reply.getContent();
        String seClass = attribs.get("seClass");

        // Orden de negociacion a todos los nodos
        for (int i=0; i<Integer.parseInt(redundancy); i++) {
          System.out.println("\tCONVERSATIONID for order " + orderID + " and plan " + myAgent.getLocalName() + ": " + conversationId);

          //reply = sendCommand("localneg " + targets + " action=start " + orderID + " criterion=max mem externaldata=" + orderID + "," + seCategory + "," + seClass + "," + ((i == 0) ? "running" : "tracking"));
          negotiate(targets, "max mem", "start", orderID + "," + seCategory + "," + seClass + "," + ((i == 0) ? "running" : "tracking")+","+redundancy, conversationId);
        }

      } catch (Exception e) {
        e.printStackTrace();
      }


    }
     */

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

  /*
  public ACLMessage sendCommand(String cmd, String conversationId) throws Exception {

    DFAgentDescription dfd = new DFAgentDescription();
    ServiceDescription sd = new ServiceDescription();

    sd.setType("sa");
    dfd.addServices(sd);
    String mwm;

    while (true) {
      DFAgentDescription[] result = DFService.search(myAgent,dfd);

      if ((result != null) && (result.length > 0)) {
        dfd = result[0];
        mwm = dfd.getName().getLocalName();
        break;
      }
      LOGGER.info(".");
      Thread.sleep(100);

    } //end while (true)

    LOGGER.entry(mwm, cmd);
    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
    msg.addReceiver(new AID(mwm, AID.ISLOCALNAME));
    msg.setConversationId(conversationId);
    msg.setOntology("control");
    msg.setContent(cmd);
    msg.setReplyWith(cmd);
    myAgent.send(msg);
    ACLMessage reply = myAgent.blockingReceive(
            MessageTemplate.and(
                    MessageTemplate.MatchInReplyTo(msg.getReplyWith()),
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM))
            , 1000);

    return LOGGER.exit(reply);
  }

   */

  /*
  // TODO Mirar para meter el metodo negotiate en una interfaz
  private String negotiate(String targets, String negotiationCriteria, String action, String externalData, String conversationId) {

    //Request de nueva negociación
    ACLMessage msg = new ACLMessage(ACLMessage.CFP);

    for (String target: targets.split(","))
      msg.addReceiver(new AID(target, AID.ISLOCALNAME));
    msg.setConversationId(conversationId);
    msg.setOntology(es.ehu.platform.utilities.MasReconOntologies.ONT_NEGOTIATE );

    msg.setContent("negotiate " +targets+ " criterion=" +negotiationCriteria+ " action=" +action+ " externaldata=" +externalData);
    myAgent.send(msg);

    return "Negotiation message sent";
  }

   */

}
