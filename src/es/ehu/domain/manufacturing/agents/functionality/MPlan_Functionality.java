package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.SystemModelAgent;
import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.AvailabilityFunctionality;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.IExecManagement;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MPlan_Functionality implements BasicFunctionality, AvailabilityFunctionality, IExecManagement {
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
    //Aqu� hay que arrancar los agentes Order asociados
    //Para eso, hay que buscarlos en el modelo en primer lugar
    this.myAgent = myAgent;

    Hashtable<String, String> attributes = new Hashtable<String, String>();
    attributes.put("seClass", "es.ehu.domain.manufacturing.agents.OrderAgent");

    String status = seStart(myAgent.getLocalName(), attributes, null);
    if (status == null)
      System.out.println("OrderAgent created");
    else if (status == "-1")
      System.out.println("ERROR creating OrderAgent -> No existe el ID del plan");
    else if (status == "-4")
      System.out.println("ERROR creating OrderAgent -> No targets");

    return null;

    //TODO El planAgent tiene que esperar a la confirmacion de que todos sus hijos se han creado para notificar que pasa al estado running (o tracking si es una r�plica)
  }

  @Override
  public String seStart(String seID, Hashtable<String, String> attribs, String conversationId){

    // seID --> ID del mPlanAgent
    String parentQuery = "get " + seID + " attrib=parent";
    ACLMessage reply = null;

    try {
      reply = sendCommand(parentQuery);
      String planID = reply.getContent();
      // ID del plan con el cual el agente est� relacionado
      if (planID == null)   // Si no existe el id en el registro devuelve error
        return "-1";

      String query = "get order* parent="+ planID;  // Busco todos los order de los que el plan es parent (no se buscan todos los elementos porque pueden existir otros mPlanAgent en tracking)
      reply = sendCommand(query);
    } catch (Exception e) {
      e.printStackTrace();
    }

    String allOrders = reply.getContent();
    if(allOrders == null)   // Si no existen orders en el registro devuelve error
      return "-1";

    List<String> items = Arrays.asList(allOrders.split("\\s*,\\s*"));
    for (String orderID: items) {
      // Creamos los agentes para cada order
      try {
        String redundancy = "1";
        if ((attribs!=null) && (attribs.containsKey("redundancy")))
          redundancy = attribs.get("redundancy");

        reply = sendCommand("get (get * parent=(get * parent=" + orderID + " category=restrictionList)) attrib=attribValue");
        String refServID = reply.getContent();

        reply = sendCommand("get * category=pNodeAgent" + ((refServID.length()>0)?" refServID=" + refServID:""));
        String targets = reply.getContent();
        if (targets.length()<=0)
          return "-4";

        reply = sendCommand("get " + orderID + " attrib=category");
        String seCategory = reply.getContent();
        String seClass = attribs.get("seClass");

        // Orden de negociacion a todos los nodos
        for (int i=0; i<Integer.parseInt(redundancy); i++)
          reply = sendCommand("localneg " + targets + " action=start " + orderID + " criterion=max mem externaldata=" + orderID + "," + seCategory + "," + seClass + "," + ((i==0)?"running":"tracking"));

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
}
