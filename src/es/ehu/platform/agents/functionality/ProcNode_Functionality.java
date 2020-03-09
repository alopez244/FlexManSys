package es.ehu.platform.agents.functionality;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import es.ehu.flexmansys.utilities.MsgOperation;
import es.ehu.flexmansys.utilities.MsgTransport;
import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.*;
import es.ehu.platform.utilities.Cmd;
import es.ehu.platform.behaviour.*;

public class ProcNode_Functionality implements BasicFunctionality, NegFunctionality{

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private Agent myAgent;

  @Override
  public String init(MWAgent myAgent) {
    this.myAgent = myAgent;
    LOGGER.entry();

    //log.info("CPU benchmark...");
//    long start=System.currentTimeMillis();
//          for (int i=0;i<1000000;i++)
//            Math.random();
//          //un i7m2660 tarda 46ms
//          float score=46/(System.currentTimeMillis()-start);
//    Runtime rt = Runtime.getRuntime(); 

    String attribs = "";
    String [] args = (String[]) myAgent.getArguments(); 
    
    for (int i=0; i<args.length; i++){ 
      if (!args[i].toString().toLowerCase().startsWith("id=")) attribs += " "+args[i];
      if (args[i].toString().toLowerCase().startsWith("id=")) return "";
    }
    
      

    String cmd = "reg procNode parent=system"+attribs;
//        + " platform="+System.getProperty("java.version")
//        + " coreNumber="+rt.availableProcessors()
//        + " memory="+rt.maxMemory()/1024/1024+"MB"
//        + " network=gigabit" //TODO: calcular
//        + " CPUScore="+score
//        + " storage=0" //TODO calcular
//        + " operatingSystem="+System.getProperty("os.name");

    
    ACLMessage reply = null;
    try {
      reply = myAgent.sendCommand(cmd);
    } catch (Exception e) {
      e.printStackTrace();
    }
    String respuesta = reply.getContent();
    
    LOGGER.info(myAgent.getLocalName()+" ("+cmd+")"+" > mwm < "+respuesta);
    
    return LOGGER.exit(respuesta);
    
  
  }


  @Override
  public long calculateNegotiationValue(String negCriterion, Object... negExternalData) {
    // approximation to the total amount of memory currently available for future allocated objects, measured in bytes
    return Runtime.getRuntime().freeMemory();

  }


  @Override
  public int checkNegotiation(String conversationId, String sAction, double negReceivedValue, long negScalarValue, boolean tieBreak, 
      boolean checkReplies, Object... negExternalData) {
    LOGGER.entry(conversationId, sAction, negReceivedValue, negScalarValue);
    
    String seID = (String)negExternalData[0];
    String seType = (String)negExternalData[1];
    String seClass = (String)negExternalData[2];
    String seFirstTransition = (String)negExternalData[3]; 
    
    if (negReceivedValue>negScalarValue) return NegotiatingBehaviour.NEG_LOST; //pierde negociaci�n
    if ((negReceivedValue==negScalarValue) && !tieBreak ) return NegotiatingBehaviour.NEG_LOST; //empata negocicaci�n pero no es quien fija desempate
    
    LOGGER.info("es el ganador ("+negScalarValue+")");
    if (!checkReplies) return NegotiatingBehaviour.NEG_PARTIAL_WON; // es ganador parcial, faltan negociaciones por finalizar 
    
    //TODO: si este agente ha ganado algo posterior al timestamp del cfp.
    // si es que no actualizar este tiempo y ok
    // si ha ganado hay que pedir que se repita la actual> return NegotiatingBehaviour.NEG_NULL
    
    LOGGER.info("ejecutar "+sAction); 

    Cmd action = new Cmd(sAction);
    
    if (action.cmd.equals("start")) {
      LOGGER.info("id="+action.who);

      try{
        // Registro el agente egistrar y el id>appagn101. seTypeAgent ASA, APA
        String agnID = sendCommand("reg "+seType+"Agent parent="+seID).getContent();
        // Instancio nuevo agente
        AgentController ac = ((AgentController) myAgent.getContainerController().createNewAgent(agnID, seClass, new Object[] { "firstState="+seFirstTransition }));
        ac.start();
      }catch (Exception e) {e.printStackTrace();}
    }
   //if (noTengoRecursos) return NegotiationBehaviour.NEG_FAIL
  return NegotiatingBehaviour.NEG_WON;
    
  }


  @Override
  public Object execute(Object[] input) {
    // TODO Auto-generated method stub
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