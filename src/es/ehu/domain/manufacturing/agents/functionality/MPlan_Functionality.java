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

public class MPlan_Functionality implements BasicFunctionality, AvailabilityFunctionality, IExecManagement {
  /**
   * 
   */
  private static final long serialVersionUID = -4078504089052783841L;
  static final Logger LOGGER = LogManager.getLogger(MPlan_Functionality.class.getName()) ;
  
  private Agent myAgent;

  private List<String> myOrders;
  private int chatID = 0; // Numero incremental para crear conversationID

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

    Hashtable<String, String> attributes = new Hashtable<String, String>();
    attributes.put("seClass", "es.ehu.domain.manufacturing.agents.OrderAgent");

    String status = seStart(myAgent.getLocalName(), attributes, null);
    if (status == null)
      System.out.println("OrderAgents created");
    else if (status == "-1")
      System.out.println("ERROR creating OrderAgent -> No existe el ID del plan");
    else if (status == "-4")
      System.out.println("ERROR creating OrderAgent -> No targets");

    // Le añadimos un comportamiento para que consiga todos los mensajes que le van a enviar los orders cuando se arranquen correctamente
    myAgent.addBehaviour(new SimpleBehaviour() {
      @Override
      public void action() {
        boolean moreMsg = true;
        ACLMessage msg = myAgent.receive();
        if(msg != null) {
          if ((msg.getPerformative() == 7) && (msg.getContent().equals("Order created successfully"))) {
            System.out.println("\tYa se ha creado el agente " + msg.getSender().getLocalName() + " - hay que borrarlo de la lista --> " + myOrders);
            // Primero vamos a conseguir el ID del order (ya que el mensaje nos lo envia su agente)
            String senderOrderID = null;
            try {
              ACLMessage reply = sendCommand("get " + msg.getSender().getLocalName() + " attrib=parent");
              if (reply != null)
                senderOrderID = reply.getContent();
            } catch (Exception e) {
              e.printStackTrace();
            }

            // Si la order es uno de los hijos (que solo los hijos nos enviaran los mensaje, pero por se acaso), lo borramos de la lista
            if(myOrders.contains(senderOrderID))
              myOrders.remove(senderOrderID);
            // Si la lista esta vacia, todos los orders se han creado correctamente, y tendremos que pasar del estado BOOT al RUNNING
            if (myOrders.isEmpty()) {
              moreMsg = false;
              // Pasar a estado running
              System.out.println("\tEl agente " + myAgent.getLocalName() + " ha finalizado su estado BOOT y pasará al estado RUNNING");

              System.out.println("Estado del agente " + myAgent.getLocalName() + ": " + myAgent.getState());

              // SystemModelAgent linea 1422
              String query = "set " + myAgent.getLocalName() + " state=running";
              try {
                ACLMessage reply = sendCommand(query);
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

    return null;

    //TODO El planAgent tiene que esperar a la confirmacion de que todos sus hijos se han creado para notificar que pasa al estado running (o tracking si es una réplica)
  }

  @Override
  public String seStart(String seID, Hashtable<String, String> attribs, String conversationId){

    // seID --> ID del mPlanAgent
    String parentQuery = "get " + seID + " attrib=parent";
    ACLMessage reply = null;

    try {
      reply = sendCommand(parentQuery);
      // ID del plan con el cual el agente está relacionado
      String planID = null;
      if (reply == null)   // Si no existe el id en el registro devuelve error
        return "-1";
      else
        planID = reply.getContent();

      String query = "get order* parent="+ planID;  // Busco todos los order de los que el plan es parent (no se buscan todos los elementos porque pueden existir otros mPlanAgent en tracking)
      reply = sendCommand(query);
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

    this.myOrders = items;
    for (String orderID: items) {
      // Creamos los agentes para cada order
      try {
        String redundancy = "1";
        if ((attribs!=null) && (attribs.containsKey("redundancy")))
          redundancy = attribs.get("redundancy");

        reply = sendCommand("get (get * parent=(get * parent=" + orderID + " category=restrictionList)) attrib=attribValue");
        String refServID = null;
        if (reply != null)
          refServID = reply.getContent();

        reply = sendCommand("get * category=pNodeAgent" + ((refServID.length()>0)?" refServID=" + refServID:""));
        String targets = null;
        if (reply != null) {
          targets = reply.getContent();
          if (targets.length() <= 0)
            return "-4";
        }

        reply = sendCommand("get " + orderID + " attrib=category");
        String seCategory = null;
        if (reply != null)
          seCategory = reply.getContent();
        String seClass = attribs.get("seClass");

        // Orden de negociacion a todos los nodos
        for (int i=0; i<Integer.parseInt(redundancy); i++) {
          // Crear un nuevo conversationID
          conversationId = myAgent.getLocalName() + "_" + chatID++;
          System.out.println("\tCONVERSATIONID for order " + orderID + " and plan " + myAgent.getLocalName() + ": " + conversationId);

          //reply = sendCommand("localneg " + targets + " action=start " + orderID + " criterion=max mem externaldata=" + orderID + "," + seCategory + "," + seClass + "," + ((i == 0) ? "running" : "tracking"));
          negotiate(targets, "max mem", "start", orderID + "," + seCategory + "," + seClass + "," + ((i == 0) ? "running" : "tracking"), conversationId);
        }

      } catch (Exception e) {
        e.printStackTrace();
      }


    }

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

  public ACLMessage sendCommand(String cmd) throws Exception {

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

}
