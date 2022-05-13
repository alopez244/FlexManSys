package es.ehu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
 * This behaviour is a simple implementation of a message receiver.
 * It check if a satate is recive and stores it.
 * If the timeout expires before any message arrives, the behaviour
 * transition into a negotiation.
 * 
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/
public class ThreadedCommandProcessor extends SimpleBehaviour {


  private static final long serialVersionUID = 1L;
  static final Logger LOGGER = LogManager.getLogger(ThreadedCommandProcessor.class.getName()) ;

  public String key = null;
  private SystemModelAgent myAgent= null;
  public ACLMessage msg = null;
  public String condition = null;
  private String result = null; 
  private boolean done = false;

  /* Constructor */
  public ThreadedCommandProcessor(String key , Agent a, ACLMessage msg) {
    super(a);
    LOGGER.entry(key, a);
    this.key = key;
    this.myAgent = (SystemModelAgent)a;
    this.msg=msg;
    LOGGER.exit();
  }

  // For persistence service
  protected ThreadedCommandProcessor() {
  }
  
  public void onStart() {
    LOGGER.entry();


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

     if (result.startsWith("threaded#")) { //la tarea espera a que llegue un mensaje y lo recoge del ds con la condición de finalización
       LOGGER.debug("condition="+condition);
       String data = "";
       do {       
         LOGGER.trace("tbf.getThread(this).suspend();");
         myAgent.tbf.getThread(this).suspend();
         LOGGER.trace("tbf.getThread(this).resume();");
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