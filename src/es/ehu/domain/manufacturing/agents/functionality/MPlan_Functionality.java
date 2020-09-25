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

  private String smaID;

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


    seStart(myAgent.getLocalName(), null, null);

    return null;
  }

  @Override
  public String seStart(String seID, Hashtable<String, String> attribs, String conversationId){

    String parentQuery = "get " + seID + " attrib=parent";
    System.out.println("ID agent > " + seID);
    ACLMessage reply = null;

    try {
      reply = sendCommand(parentQuery);
      String planID = reply.getContent();
      System.out.println("PlanID: " + planID);  // ID del plan con el cual el agente está relacionado

      String query = "get * parent="+ planID;
      System.out.println("Query > " + query);
      reply = sendCommand(query);
    } catch (Exception e) {
      e.printStackTrace();
    }

    String allOrders = reply.getContent();
    allOrders = allOrders.replace(seID + ",", "");
    System.out.println("All orders> " + allOrders);

    List<String> items = Arrays.asList(allOrders.split("\\s*,\\s*"));
    for (String order: items) {
      System.out.println("Creating agent for order > " + order);
      StringBuilder startOrder = new StringBuilder("sestart "+ order);

      ConcurrentHashMap<String, String> attributes = new ConcurrentHashMap<String, String>();
      attributes.put("seClass", "es.ehu.domain.manufacturing.agents.OrderAgent");

      if (attributes!=null)
        attributes.entrySet().stream().forEach(entry -> startOrder.append(" " + entry.getKey() + "=" + entry.getValue()));

      try {
        reply = sendCommand(startOrder.toString());
        System.out.println("Start order> " + reply.getContent());
      } catch (Exception e) {
        e.printStackTrace();
      }

    }

    return null;  //TODO
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
