package es.ehu;

import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jade.core.behaviours.DataStore;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.core.behaviours.ThreadedBehaviourFactory.ThreadedBehaviourWrapper;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.wrapper.AgentController;

/**
 * This behaviour is a simple implementation of a message receiver.
 * It check if a satate is recive and stores it.
 * If the timeout expires before any message arrives, the behaviour
 * transition into a negotiation.
 * 
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/
public class ThreadedCommandProcessor extends SimpleBehaviour {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  static final Logger LOGGER = LogManager.getLogger(ThreadedCommandProcessor.class.getName()) ;

  public String key = null;
  private MessageTemplate template = null;
  private SystemModelAgent myAgent= null;
  public ACLMessage msg = null;
  public String condition = null;
  private String result = null; 
  private boolean done = false;
  
  //log
  //private long startTime = System.currentTimeMillis();
   

  /**
   * Constructor.
   * 
   * @param a
   *            a reference to the Agent
   **/
  public ThreadedCommandProcessor(String key , Agent a) {
    super(a);
    LOGGER.entry(key, a);
    this.key = key;
    this.myAgent = (SystemModelAgent)a;
    LOGGER.exit();
  }

  // For persistence service
  protected ThreadedCommandProcessor() {
  }
  
  public void onStart() {
    LOGGER.entry();
    
    
    
    // recojo y borro del ds el mensaje que contiene el comando
    msg = (ACLMessage)myAgent.ds.get(this.key);
    myAgent.ds.remove(this.key);
    // proceso el comando
    result = myAgent.processCmd(msg.getContent(), key);
    if (result.startsWith("threaded#")) this.condition = result.substring(result.indexOf("#")+1);
    
    myAgent.threadLog.put(key, ((this.result.length()>20)?this.result.substring(0,20)+"...-":this.result) + " < " 
              + ((msg.getContent().length()<=60)?msg.getContent():msg.getContent().substring(0,60)+"...-")+" ("
              + "cond: "+this.condition+")"+" ("+(System.currentTimeMillis()-SystemModelAgent.startTime)+")");
    
    LOGGER.debug(result + "< processCmd("+msg.getContent()+", "+key+")");
    
    LOGGER.exit();
  }

  public void action() {
    
    LOGGER.entry();
//    for (int i=1;i<1000; i++) {
//      System.out.println(i);
//      try {Thread.sleep(1000); } catch (InterruptedException e) {e.printStackTrace(); }
//    }
    
     if (result.startsWith("threaded#")) { //la tarea espera a que llegue un mensaje y lo recoge del ds con la condición de finalización
       LOGGER.debug("condition="+condition);
       String data = "";
       do {       
         LOGGER.trace("tbf.getThread(this).suspend();");
         myAgent.tbf.getThread(this).suspend();
         LOGGER.trace("tbf.getThread(this).resume();");
         if (myAgent.ds.containsKey(key)) {
           data = ((ACLMessage)myAgent.ds.get(this.key)).getContent();
           myAgent.ds.remove(key);
         } 
         LOGGER.debug("data="+data);
         
       } while (!data.matches(condition));
       LOGGER.debug("se cumple condicion:\""+condition+"\" en "+data);
       result=data;
     } else  {
      ACLMessage reply = msg.createReply();
      reply.setPerformative(ACLMessage.INFORM);
      reply.setContent(result);
      reply.setOntology("control");;
      LOGGER.debug("send("+reply+");");
      myAgent.send(reply);
      LOGGER.info(result.replaceAll("\n", " ")+" < "+msg.getSender().getLocalName()+"("+msg.getContent()+")");
      this.done=true;  
    }
    
     //espero a nueva activación
    
//      myAgent.tbf.getThread(this).suspend();
//      ACLMessage status = (ACLMessage)myAgent.ds.get(this.key);
//      myAgent.ds.remove(this.key);
    
    
    
    //(String cmpID, String conversationId)
    
//    if (cmds[0].equals("start") && (cmds[1].startsWith("compon"))) {
//      //(String cmpID, String conversationId)
//      LOGGER.debug("myAgent.startComponent("+cmds[1]+", "+key+")");
//      String respuesta = myAgent.startComponent(cmds[1], key);
//
//      
//      ACLMessage msgRespuesta = null;
//      do {
//        LOGGER.trace("tbf.getThread(this).suspend();");
//        myAgent.tbf.getThread(this).suspend();
//
//        LOGGER.trace("tbf.resume(this);");
//        msgRespuesta = (ACLMessage)myAgent.ds.get(this.key);
//        myAgent.ds.remove(this.key);
//
//      } while (msg.getContent()!="dsds"); //(msgRespuesta.getContent().contains("state=running")); 
//
//      ACLMessage reply = msg.createReply();
//      reply.setContent(cmds[1] + " running");
//      myAgent.send(reply);
//      //(String cmpID, String conversationId)
//    }


   // myAgent.tbf.suspend(this);


//    ACLMessage reply = msg.createReply();     
//    reply.setPerformative(ACLMessage.INFORM);
//    reply.setContent("resultado de "+msg.getContent());
//    myAgent.send(reply);
//
//    myAgent.ds.put(key+";reply", reply);

    //			template = MessageTemplate.and(MessageTemplate.and(
    //			    MessageTemplate.MatchOntology("control"), 
    //			    MessageTemplate.MatchPerformative(ACLMessage.INFORM)),
    //			    MessageTemplate.MatchInReplyTo(msg.getReplyWith()
    //			));

    //			ACLMessage reply = myAgent.blockingReceive(template);

    //template = ;

    //		} else {
    //			
    //		  LOGGER.debug("myAgent.receive("+template+")");
    //			ACLMessage reply = myAgent.receive(template);
    //
    //			if (reply == null) {
    //			  LOGGER.trace("block()");
    //				block();
    //			} else {
    //			  LOGGER.trace("reply !== null");
    //				//replies[repliesCnt]=reply;
    //				getDataStore().put(reply.getSender().getLocalName(), reply);
    //				if (getDataStore().size()>=targets.length) {
    //				  LOGGER.debug("done = true");
    //					done=true;
    //					myAgent.doWake();
    //				}
    //			} // end msg != null
    //		} // end template !=null
    //		LOGGER.exit();
  }




  public final boolean done() {
    LOGGER.entry();
    return LOGGER.exit(done);
  }

 

  public int onEnd() {
    LOGGER.entry();
    this.result = result.replaceAll("\n", " ");
    myAgent.threadLog.replace(key, ((this.result.length()>20)?this.result.substring(0,20)+"...":this.result) + " < " 
        + ((msg.getContent().length()<=60)?msg.getContent():msg.getContent().substring(0,60)+"...")
       + ((this.condition!=null)?(" (cond: "+this.condition+")"):"" ) +" ("+(System.currentTimeMillis()-SystemModelAgent.startTime)+")");
    myAgent.behaviours.remove(key);
    return LOGGER.exit(0);
  }
}