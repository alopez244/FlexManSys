package es.ehu.domain.sputnik;

import es.ehu.platform.template.ResourceAgentTemplate;

//public class ProcessingNode extends ResourceAgentTemplate {
//
//  private static final long serialVersionUID = -3415727708050658595L;
//  
//  
//  private long startTime = 0;
//
//  public ProcessingNode() {
//    functionalityInstance  = new es.ehu.domain.sputnik.FunctionalityProcesador();
//  }


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.lang.management.ManagementFactory;
import java.util.Hashtable;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/*
 cd C:\Users\bcsgaguu\workspace\sede.ws\JadeMW\JadeMiddleware
 java -cp bin;lib\jade.jar jade.Boot -container -container-name node101 na_node101:es.ehu.NodeAgent("System=syst101","description=description")

 cd C:\Users\bcsgaguu\workspace\sede.ws\JadeMW\JadeMiddleware
 java -cp bin;lib\jade.jar jade.Boot -container -container-name node102 na_node102:es.ehu.NodeAgent("System=syst101","description=description")

 cd C:\Users\bcsgaguu\workspace\sede.ws\JadeMW\JadeMiddleware
 java -cp bin;lib\jade.jar jade.Boot -container -container-name node103 na_node103:es.ehu.NodeAgent("System=syst101","description=description")
 
 cd C:\Users\bcsgaguu\workspace\sede.ws\JadeMW\JadeMiddleware
 java -cp bin;lib\jade.jar jade.Boot -container -container-name node104 na_node104:es.ehu.NodeAgent("System=syst101","description=description")

 */

public class ProcessingNode extends ResourceAgentTemplate{
  private static final long serialVersionUID = 1L;  
  static final Logger LOGGER = LogManager.getLogger(ProcessingNode.class.getName()) ;
  
  private String nodeID; 
  private String system = "";
  private String description = "";
  private MessageTemplate template;
  private String regTemplate;
  private Runtime rt;
  
  private String mwm;
  
  
  public ProcessingNode() {
    
    functionalityInstance  = new es.ehu.domain.sputnik.FunctionalityProcessingNode();
  }
  
//  protected void setup() {
//    LOGGER.entry();
//    
//    try {
//         Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
//         System.out.println("Añadida tarea de apagado...");
//       } catch (Throwable t) {
//         LOGGER.warn(" *** Error: No se ha podido aï¿½adir tarea de apagado");
//    }
//    
//    String[] args = (String [])getArguments();
//    for (int i=0; i<args.length; i++) {
//      if (args[i].toString().toLowerCase().startsWith("system=")) {
//        this.system = args[i].substring("system=".length());
//        LOGGER.debug("this.system="+this.system);
//      } else if (args[i].toString().toLowerCase().startsWith("description=")) { 
//        this.description = args[i].substring("description=".length());
//        LOGGER.debug("this.description="+this.description);
//      }
//      System.out.println("ss");
//    } 
//    
//    addBehaviour(new NodeAgentBehaviour(this));
//    extraBehaviour(this);
//    LOGGER.exit();
//  }
  
  class NodeAgentBehaviour extends SimpleBehaviour 
    {
    private static final long serialVersionUID = 821356306729805840L;
    boolean finished = false;
    boolean registered = false;   
    
    
    public NodeAgentBehaviour(Agent agent) { 
            super(agent);  
        }
        
    public void onStart(){
      LOGGER.entry();
      LOGGER.warn("warning output sample");
      rt = Runtime.getRuntime(); 
      if (mwm == null) try { resolveMWM(); } catch (Exception e) {e.printStackTrace();}
      regTemplate = registerNode();
      LOGGER.debug("regTemplate="+regTemplate);
      template = MessageTemplate.and(MessageTemplate.MatchOntology("control"), //registro y lo añado al template
            MessageTemplate.MatchInReplyTo(regTemplate));
      LOGGER.exit();
    }
  
    public void action() {  
      LOGGER.entry();
      
      ACLMessage msg = receive(template);
      if (msg != null) {
        LOGGER.debug(msg.getSender().getLocalName()+":"+msg.getContent());
        if ((msg.getInReplyTo()!=null) && (msg.getInReplyTo().equals(regTemplate))) {
          LOGGER.trace("respuesta del registro");
          template = MessageTemplate.MatchOntology("control");
          nodeID=msg.getContent();
          } else { 
          LOGGER.trace("mensaje de control");

          StringBuilder result = new StringBuilder();
          String cmd = msg.getContent();
          
          boolean dentroDeComilla = false;
          char[] cCmd = cmd.toCharArray();
          for (int i=0; i<cCmd.length; i++) { 
            if (cCmd[i]=='\"') dentroDeComilla = !dentroDeComilla;
            if (dentroDeComilla) {
              if (cCmd[i]=='(') cCmd[i]='{';
              else if (cCmd[i]==')') cCmd[i]='}';
              else if (cCmd[i]=='=') cCmd[i]='#';
            }
          } //end forCCmd;
          cmd = new String(cCmd);
          
          if (dentroDeComilla) result.append("No closed \"");
            
//          while (cmd.contains("(")) {
//            String subCmd = cmd.substring(cmd.lastIndexOf('(', cmd.indexOf(')'))+1, cmd.indexOf(')'));
//            String subCmdResult = processCmd(subCmd, conversationId);
//            cmd=cmd.replace('('+subCmd+')', (subCmdResult.isEmpty()?"null":subCmdResult));     
//          } 
          

          while (cmd.contains("  ")) cmd=cmd.replace("  ", " ");
          while (cmd.contains(" =")) cmd=cmd.replace(" =", "=");
          
          String[] cmds = cmd.split(" ");
          Hashtable<String, String> attribs = processAttribs(cmds);
          
          
          if (cmds[0].equals("get")) {
            result=result.append(String.valueOf(nodeParametes(cmds[1])));
          } else if (cmds[0].equals("negotiate")) {
            LOGGER.debug("action="+attribs.get("action"));
            myAgent.addBehaviour(new NodeAgentNegotiation (myAgent, cmds[1], attribs.get("negotiationCriteria"), attribs.get("action"), msg.getConversationId()));
            try{ Thread.sleep(100); } catch (Exception e){}
          } else if (cmds[0].equals("exit")) {
            result.append("agur");
            finished=true;
          }

          if (result.length()>0) { //devuelvo respuesta
            LOGGER.debug("sendReply");
            ACLMessage reply = msg.createReply();
            reply.setOntology(msg.getOntology());
            reply.setContent(result.toString().trim());
            if (msg.getPerformative()==ACLMessage.CFP)
              reply.setPerformative(ACLMessage.PROPOSE);
            else 
              reply.setPerformative(ACLMessage.INFORM);
            // TODO: si no cumple el mínimo: REFUSE
            myAgent.send(reply);
            LOGGER.info(result + " < " + msg.getSender().getLocalName()+"("+msg.getContent()+")");
        } // !msg.getReplyWith().equals(regTemplate)
        }
        
      } else {
        LOGGER.trace("block();");
        block();
      }
      LOGGER.exit();
    } //end action
        
    private String registerNode(){
      LOGGER.entry();

      //log.info("CPU benchmark...");
      //long start=System.currentTimeMillis();
      //      for (int i=0;i<1000000;i++)
      //        Math.random();
      //      //un i7m2660 tarda 46ms
      //      float score=46/(System.currentTimeMillis()-start);
      float score=1;
      //if (true) System.out.println(System.currentTimeMillis()-start);

      //ACLMessage aMsg = new ACLMessage(ACLMessage.INFORM);
      //aMsg.addReceiver(mwm);
      //if (!registered) aMsg.setContent("add nod "+myAgent.getContainerController().getContainerName());
      //Runtime rt = Runtime.getRuntime(); 

      //rt.maxMemory() memoria que puede utilizar JVM
      //rt.totalMemory() memoria que actualmente ha reservado la JVM
      //rt.freeMemory() memoria libre de lo actualmente reservado



      long freeMem = freeMemory();
      String container = "";
      try { 
        container = myAgent.getContainerController().getContainerName();
      } catch (Exception e) {e.printStackTrace();}

//      String cmd = "reg node ID="+ container
//          + " parent="+system
//          + " description="+description
//          + " platform="+System.getProperty("java.version")
//          + " coreNumber="+rt.availableProcessors()
//          + " memory="+rt.maxMemory()/1024/1024+"MB"
//          + " network=gigabit" //TODO: calcular
//          + " CPUScore="+score
//          + " storage=0" //TODO calcular
//          + " operatingSystem="+System.getProperty("os.name")
//
//          //RUNTIME
//          + " freeMemory="+freeMem/1024/1024+"MB"
//          + " freeStorage="+freeMem/1024/1024+"MB"
//          + " systemLoad="+ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
      
      String cmd = "reg node parent=system";
      //+ " aid=" + myAgent.getAID().getName();
      
      ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
      msg.addReceiver(new AID(mwm, AID.ISLOCALNAME));

      msg.setOntology("control");
      msg.setReplyWith("reg_" + container + ";" + System.currentTimeMillis());

      msg.setContent(cmd);
      send(msg);
      //ACLMessage msg = blockingReceive();
      LOGGER.info(myAgent.getLocalName()+"("+cmd+")"+" > mwm");

      return LOGGER.exit(msg.getReplyWith());
    }
    


    public ACLMessage sendCommand(String cmd) {
      ACLMessage aMsg = new ACLMessage(ACLMessage.REQUEST);
      aMsg.addReceiver(new AID(mwm, AID.ISLOCALNAME));

      aMsg.setOntology("control");
      aMsg.setReplyWith(cmd+System.currentTimeMillis());

      //if (log) System.out.print("sendCommand ("+cmd+"): ");

      aMsg.setContent(cmd);
      send(aMsg);
      //ACLMessage msg = blockingReceive();
      LOGGER.info(myAgent.getLocalName()+"("+cmd+")"+" > mwm");

      ACLMessage aReply = null; 
      MessageTemplate mt = MessageTemplate.MatchInReplyTo(aMsg.getReplyWith());

      aReply = blockingReceive(mt, 200);

      //      while (aReply == null){
      //        this.block();
      //        aReply = receive(mt);
      //      }

      LOGGER.info(myAgent.getLocalName()+" < mwm ("+aReply.getContent()+")");
      return aReply;
    }
    
   
    
//    public Hashtable<String, String> processAttribs(String... cmdLine){
//
//      LOGGER.entry((Object[])cmdLine);
//
//      Hashtable<String, String> attribs = new Hashtable<String, String>();
//      String attrib = "";
//      if (cmdLine.length > 2)
//        for (int i = 2; i < cmdLine.length; i++) {
//          if (cmdLine[i].contains("=")) { // encuentro otro atributo
//            String[] attribDef = cmdLine[i].split("=");
//            attrib = attribDef[0];
//            attribs.put(attrib, attribDef[1]);
//
//          } else
//            attribs.put(attrib, attribs.get(attrib)+" "+cmdLine[i]);
//          //value += " " + cmds[i];
//        }
//      return LOGGER.exit(attribs);
//    }
    
    
    public Hashtable<String, String> processAttribs(String... cmdLine){
      LOGGER.entry((Object[])cmdLine);
      
      if (cmdLine.length < 3) return null; //no hay atributos

      Hashtable<String, String> attribs = new Hashtable<String, String>();
      String attrib = "attrib";

      for (int i = 2; i < cmdLine.length; i++) {
        if (cmdLine[i].contains("=")) { // encuentro otro atributo
          String[] attribDef = cmdLine[i].split("=");
          attrib = attribDef[0];
          attribs.put(attrib, attribDef[1]);
        } else
          attribs.put(attrib, attribs.get(attrib)+" "+cmdLine[i]);
       String attribValue = attribs.get(attrib);
       while (attribValue.contains("{")) attribValue = attribValue.replace("{", "(");
       while (attribValue.contains("}")) attribValue = attribValue.replace("}", ")");
       while (attribValue.contains("#")) attribValue = attribValue.replace("#", "=");
       attribs.put(attrib, attribValue); 
       
      }
      return LOGGER.exit(attribs);
    }
    
    public boolean done() {  
      return finished;  
    }
    
    } // ----------- End myBehaviour
  
  class NodeAgentNegotiation extends SimpleBehaviour {
    
    private static final long serialVersionUID = 821356306729805840L;

    boolean finished = false;
    private String[] targets;
    private String comparator;
    private String parameter;
    private String conversationId;
    private int step = 0;
    private long myValue;
    private MessageTemplate template;
    private int repliesCnt; // The counter of replies from seller agents
    private String action;

    public NodeAgentNegotiation(Agent agent, String targets, String negotiationCriteria, String action, String conversationId) { 
      super(agent);  
      LOGGER.entry(agent, targets, negotiationCriteria, conversationId);
      this.targets = targets.split(",");
      String [] cond = negotiationCriteria.split(" ");
      this.comparator = cond[0];
      this.parameter = cond[1];
      this.conversationId=conversationId;
      repliesCnt=0;
      this.action=action;
      LOGGER.exit();
    }

    public void onStart(){
      myValue= comparator.equals("max")? nodeParametes(parameter) : -nodeParametes(parameter);
      LOGGER.info("myValue=" + myValue+"("+conversationId+")");
    }

    public void action() {    
      LOGGER.entry("action()");
      
      switch (step) {
            case 0:
             
              ACLMessage cfp = new ACLMessage(ACLMessage.PROPOSE); // Propose
              cfp.setOntology("negotiation");
              cfp.setConversationId(conversationId);
              for (String id: targets)
                if (!id.equals(myAgent.getLocalName()))            //el propio agente descartado del envío
                  cfp.addReceiver(new AID(id, AID.ISLOCALNAME));

              try {
              cfp.setContentObject(new Double(nodeParametes(parameter)));
              } catch (Exception e) {}
              //cfp.setContent(String.valueOf(myValue));

              myAgent.send(cfp);
              
              template = MessageTemplate.and(MessageTemplate.MatchOntology("negotiation"),
                  MessageTemplate.MatchConversationId(conversationId));
              //TODO template para que admita varias negociaciones. usar conversationID como replyWith
              step = 1;
              break;
            case 1:
                  // Recibir el mensaje de la negociacion
                  ACLMessage reply = myAgent.receive(template);
                  if (reply != null) {
                    
                    if (reply.getPerformative() == ACLMessage.PROPOSE) {
                      // This is an offer
                      //double received = Double.parseDouble(reply.getContent());
                      
                      Double received = null;
                      try { received = (Double)reply.getContentObject(); } catch (Exception e) {}
                      LOGGER.debug(reply.getSender().getLocalName()+" PROPOSE "+received);
                      received=(comparator.equals("max")?received:-received);
                      LOGGER.debug("compares "+received+">"+myValue);
                      if (received>myValue) {
                        step=3;  //si alguno de los recibidos es mayor que el mío (si estamos buscando MAX)
                        LOGGER.debug("pierdo");
                        //break;
                      }
                      
//                      if (winner == null || value > bestValue) {
//                        bestValue = value;
//                        winner = reply.getSender().getLocalName();
//                      }
                  } else if (reply.getPerformative() == ACLMessage.FAILURE) {                         
                    String name=reply.getContent().substring(reply.getContent().indexOf(":name ", reply.getContent().indexOf("MTS-error"))
                        +":name ".length());
                    LOGGER.warn(name.substring(0, name.indexOf('@'))+" FAILURE");
                  }
                    
                    repliesCnt++; // TODO: tener en cuenta los que no respondan, mirar el código con FAILURE
                    if (repliesCnt >= targets.length-1) {
                      // El ganador ha recibido todos los mensajes
                      LOGGER.debug("gano");
                      step = (step==3)?3 : 2;
                      //step = 2;
                      if (step == 2) LOGGER.debug("gano");
                      else LOGGER.debug("pierdo"); 
                      break;
                    }
              } else 
                block();              
              break;
            case 2:
              LOGGER.debug(myAgent.getLocalName() + " he ganado ***********");
              String report = "report "+myAgent.getLocalName()+" action="+action+" winner="+myAgent.getLocalName(); 
              LOGGER.info("winner: "+report);
              
              sendCommand(report, conversationId); //en el conversationId me llega el compon TODO esto habría que mejorarlo
              //Cambiar a traking los agentes que ha perdido y el ganador a running
              // TODO: report winner
              /*for (int i=0; i<nodes.length; i++){
                if(nodes[i].contains(myAgent.getLocalName())){//el ganador
                  sendCommand("localcmd "+cmpinss[i]+" cmd=setstate running",conversationId);//TODO o tiene que pasar a recovery
                  // TODO "localcmd (getins compon104 node=node101) cmd=setstate running) //TODO qué ocurre si hay varios tracking en el mismo nodo
                } else{ //Los perdedores
                  sendCommand("localcmd "+cmpinss[i]+" cmd=setstate tracking",conversationId);
                  // localcmd (getins compon104 node=node101 compins!=compins105) cmd=setstate running)
                }
              }*/
              step = 4;
              break;
            case 3:
              LOGGER.info(myAgent.getLocalName() + " he perdido");
              // Terminar la negociacion
              step = 4;
              break;
      
            default:
              break;
      }//end step
    } //end action

    @Override
    public boolean done() {
      return step == 4;
    }

    public int onEnd() {  
      return 0;
    }

    public <T> String join(T[] array, String cement) {
      StringBuilder builder = new StringBuilder();

      if(array == null || array.length == 0) 
        return null;

      for (T t : array) 
        builder.append(t).append(cement);

      builder.delete(builder.length() - cement.length(), builder.length());

      return builder.toString();
    }

    public void sendCommand(String cmd, String conversationID) {
      ACLMessage aMsg = new ACLMessage(ACLMessage.REQUEST);
      aMsg.addReceiver(new AID(mwm, AID.ISLOCALNAME));

      aMsg.setOntology("control");
      aMsg.setConversationId(conversationID);
      aMsg.setReplyWith(cmd+System.currentTimeMillis());

      //if (log) System.out.print("sendCommand ("+cmd+"): ");
      aMsg.setContent(cmd);
      send(aMsg);
      //ACLMessage msg = blockingReceive();
      LOGGER.debug(myAgent.getLocalName()+"("+cmd+")"+" > mwm");

    }
    
  } // ----------- End myBehaviour
  
  class ShutdownThread extends Thread {
      private Agent myAgent = null;
      
      public ShutdownThread(Agent myAgent) {
        super();
        this.myAgent = myAgent;
      }
       
      public void run() {
        //System.out.println("Iniciando tarea de apagado...");
        try { 

          //DFService.deregister(myAgent);
          ACLMessage aMsg = new ACLMessage(ACLMessage.REQUEST);
          aMsg.addReceiver(new AID("sa", AID.ISLOCALNAME));
          aMsg.setOntology("control");
          aMsg.setLanguage("JavaSerialization");

          //if (log) System.out.print("sendCommand ("+cmd+"): ");

          aMsg.setContent("del " + nodeID);
          send(aMsg);
          //ACLMessage msg = blockingReceive();

          LOGGER.info("Nodo derregistrado.");
          Thread.sleep(500);
          myAgent.doDelete();}
        catch (Exception e) {}

      }
    }

  /**
   * Leer los valeres de los parametros del nodo
   * @param attrib momnre del parametro
   * @return
   */
  private long nodeParametes(String attrib){
    LOGGER.entry(attrib);
    long result=0;
    if (attrib.equals("freeMem")) result=freeMemory();
    if (attrib.equals("systemLoad")) result=(long)(systemLoad()*1000);
    if (attrib.equals("coreNumber")) result=coreNumber();
    return LOGGER.exit(result);
  }
  
  /**
   * Calcular la memoria libre del nodo
   * 
   * @return la memoria libre del sistema
   */
  protected long freeMemory(){
    return rt.maxMemory()-(rt.totalMemory()-rt.freeMemory());
  }

  /**
   * Calcular la carga del sistema
   * 
   * @return
   */
  protected double systemLoad(){
    return ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
  }

  /**
   * Devuelve el numero de corres del sistema
   * @return
   */
  protected int coreNumber(){
    return rt.availableProcessors();
  }

  /**
   * Permite añadir otros comportamientos al agente
   */
  protected void extraBehaviour(Agent a){
  }

}

