/**
 * Comportamiento que recive las ordenes del supervisor del alto nivel
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/
package es.ehu.platform.behaviour;

import static es.ehu.platform.utilities.MWMCommands.CMD_GETCOMPONENTS;
import static es.ehu.platform.utilities.MWMCommands.CMD_REPORT;
import static es.ehu.platform.utilities.MWMCommands.CMD_SET;
import static es.ehu.platform.utilities.MasReconOntologies.ONT_CONTROL;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.MWAgent;
import jade.core.ContainerID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.core.behaviours.FSMBehaviour;

public class ControlBehaviour extends SimpleBehaviour {
  private static final long serialVersionUID = -4041584544631810983L;
  static final Logger LOGGER = LogManager.getLogger(ControlBehaviour.class.getName()) ;
  private static final String CMD_INCORRECTSTATE = "incorrect state";
  public static final String CMD_SETSTATE = "setstate";
  public static final int STOP=0, RUNNING=1, TRACKING=2,  WAITINGFORDECISION=3, RECOVERING=4, NEGOTIATING=5;
  public static final String ST_STOP = "stop";
  public static final String ST_RUNNING = "running";
  public static final String ST_TRACKING = "tracking";
  public static final String ST_WAITINGFORDECISION = "waitingfordecision";
  public static final String ST_RECOVERING = "recovering";
  public static final String ST_NEGOTIATING = "negotiating";

  private MWAgent myAgent;
  private FSMBehaviour fsm;
  protected MessageTemplate template;
  int exitValue = 0;
  boolean exit = false;

  public ControlBehaviour(MWAgent a) {
    super(a);
    LOGGER.entry(a);
    LOGGER.trace("***************** inicio ControlBehaviour");
    myAgent = a;
    template =  MessageTemplate.or(
                          MessageTemplate.MatchPerformative(ACLMessage.FAILURE),
                          MessageTemplate.and(
                              MessageTemplate.MatchOntology(ONT_CONTROL),
                              MessageTemplate.MatchPerformative(ACLMessage.REQUEST))
                                    );
        
                  
    LOGGER.exit();
  }

  public ControlBehaviour(MWAgent a, Behaviour fsmBeh) {
    super(a);
    LOGGER.entry(a);
    LOGGER.trace("***************** init ControlBehaviour");
    myAgent = a;
    
    fsm = (FSMBehaviour)fsmBeh;
    template =  MessageTemplate.or(
                          MessageTemplate.MatchPerformative(ACLMessage.FAILURE),
                          MessageTemplate.and(
                              MessageTemplate.MatchOntology(ONT_CONTROL),
                              MessageTemplate.MatchPerformative(ACLMessage.REQUEST))
                                    );
        
                  
    LOGGER.exit();
  }

  @Override
  public void action()  {
    LOGGER.entry();
    this.exitValue=0;
    this.exit=false;
    
    LOGGER.debug(myAgent.cmpID+"("+myAgent.getLocalName()+"): SupervisorControl.action()");
    ACLMessage msg = myAgent.receive(template);

    /*
     *  Comandos que recibe el agente
     *  UPDATE_RUNNING CMP INS
     *  CHANGE_STATE stop
     */
    //TODO: Utilizar los tipos en ontologia
    if (msg != null) {
      if (msg.getContent() == null) {
        LOGGER.info("message content null!");
        return;
      }
      if (msg.getPerformative()==ACLMessage.FAILURE) { // if FAILURE
        LOGGER.info("****************ACLMessage.FAILURE (control):"+msg.getContent());
        String name=msg.getContent().substring(msg.getContent().indexOf(":name ", msg.getContent().indexOf("MTS-error"))+":name ".length());
        name=name.substring(0, name.indexOf('@'));
        LOGGER.info("msg.getPerformative()==ACLMessage.FAILURE (sender="+name+")");
        try { LOGGER.info(myAgent.sendCommand(CMD_REPORT + " (" + CMD_GETCOMPONENTS + " "+name+") type=notFound cmpins="+name));} catch (Exception e) {e.printStackTrace();}

      } else {
        LOGGER.info("ACLMessage.REQUEST (control):"+msg.getContent());
        String result = "";
        LOGGER.info("* Received control msg:"+msg.getContent());
        LOGGER.info(myAgent.cmpID+"("+myAgent.getLocalName()+")" + " < control:"+msg.getContent()+" < "+msg.getSender().getLocalName());
        String [] cmd = msg.getContent().split(" ");
        if (cmd.length <= 1) {
          return;
        }
        boolean sendReply=false;
        
        
// TODO refresh local cache
//        if (cmd[0].equals("update_running")) {
//          // Recibo orden de actualizaci�n de la instancia en running
//          LOGGER.info("control:update_running()");
//          myAgent.runningInstance.put(cmd[1], cmd[2]);
//          if (myAgent.trackingInstances.contains(cmd[2])) myAgent.trackingInstances.remove(cmd[2]);
//
//          result="done";
//
//
//        } else if (cmd[0].equals("update_tracking")) {
//          // Recibo orden de actualizaci�n de la instancia en running
//          LOGGER.info("control:update_tracking()");
//          if (!myAgent.trackingInstances.contains(cmd[1])) myAgent.trackingInstances.add(cmd[1]);
//          result="done";
//
//
//        } else 
//        if (cmd[0].equals("set")) {
//          
//        } else 
//          if (cmd[0].equals("get")) {
//          
//          String prm = (cmd.length>2)?cmd[2]:((cmd.length>1)?cmd[1]:"");
//          LOGGER.debug("control:get("+prm+")");
//
//          // TODO refresh local cache
////          if (prm.equalsIgnoreCase("runningInstance"))
////            for (String key: myAgent.runningInstance.keySet())
////              result+=key+":"+ myAgent.runningInstance.get(key)+"\n";
////          //        
////          //        /**
////          //         *  Instancias en tracking del componente (arraylist para actualiza el estado, actualizarla cuando al MWM llega un setState)
////          //         */
////          //        public ArrayList<String> trackingInstances = new ArrayList<String>();
////
////          else if (prm.equalsIgnoreCase("trackingInstances"))
////            for (Iterator<String> iter = myAgent.trackingInstances.iterator(); iter.hasNext();) {
////              String cmpins = iter.next();
////              result+=cmpins+(iter.hasNext()?",":"");
////            }
////
////          //        
////          //        public Functionality functionalityInstance = null;
////          else 
//            
//            if (prm.equalsIgnoreCase("functionalityInstance"))
//            result=myAgent.functionalityInstance.getClass().getName();
//          //        
//          //        protected String cmpID = null;
//          else if (prm.equalsIgnoreCase("cmpID"))
//            result=myAgent.cmpID;
//
//          else result="not found";
//
//          sendReply=true;
//
//        } else 
        if (cmd[0].equals("set")) {
          sendReply=true;

          if (cmd[1].equals("period")) {
            LOGGER.debug("myAgent.period = "+ Integer.parseInt(cmd[2]));
            myAgent.period = Integer.parseInt(cmd[2]);            
            myAgent.sendCommand(CMD_SET + " " + myAgent.cmpID + " period="+cmd[2]);

            result="done";
          } else if (cmd[1].equals("mwmStoresExecutionState")) { 
            myAgent.mwmStoresExecutionState = Boolean.parseBoolean(cmd[2]);
          }
        } else if (cmd[0].equals(CMD_SETSTATE)) { 
          
          sendReply=true;
          LOGGER.info("Set State ---------------");
          if (cmd[1].equals(ST_RUNNING)) {
            exitValue = RUNNING;
          } else if (cmd[1].equals(ST_TRACKING)) {
            exitValue = TRACKING;
          } else if (cmd[1].equals(ST_STOP)) {
            exitValue = STOP;
          } else if (cmd[1].equals(ST_RECOVERING)) {
            exitValue = RECOVERING;
          } else if (cmd[1].equals(ST_NEGOTIATING)) {
              exitValue = NEGOTIATING;
          } else {
            result = CMD_INCORRECTSTATE;
          }
//          if (fsm != null) {
//            if (!fsm.hasCurrentTransition(exitValue)) {
//              exitValue = 0;
//              result = CMD_INCORRECTSTATE;
//              LOGGER.info("Set state to" + cmd[1] + "not allowed");
//            }
//          }
          
          if (!result.equals(CMD_INCORRECTSTATE)) {
            exit = true;
            result="done";
          }
        } else if (cmd[0].equals("move")) {
          LOGGER.debug("doMove(new ContainerID("+cmd[1]+", null));"); //TODO comprobar que el nodo args[1] existe
          myAgent.doMove(new ContainerID(cmd[1], null));
          //myAgent.nodeID = cmd[1]; no es correcto hacerlo aqu�. Es posible que el agente no viaje (por ejemplo si no existe el nodo destino). Se debe hacer en el afterMove
        }
        
        if (sendReply) { //devuelvo respuesta
          ACLMessage aReply = msg.createReply();
          aReply.setOntology(msg.getOntology());
          aReply.setContent(result.trim());
          aReply.setPerformative(ACLMessage.INFORM);
          LOGGER.info("controlBehaviour().send("+aReply.getContent()+")");
          myAgent.send(aReply);
        }


        /*ACLMessage aReply = msg.createReply();
        aReply.setInReplyTo(msg.getContent());
        aReply.setContent(result);
        myAgent.send(aReply);*/
      }
    } else {
      LOGGER.trace("ControlBehaviour.beh.block()");
      block();

    }

    LOGGER.exit();
  }

  @Override
  public boolean done() {
    return exit;
  }

  public int onEnd() {
    return exitValue;
  }

}