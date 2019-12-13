package es.ehu.domain.manufacturing.behaviour;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.domain.manufacturing.agent.MC_Agent;
import es.ehu.domain.manufacturing.libConcentrador.DataIsoTCP;
import es.ehu.platform.behaviour.ControlBehaviour;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This behaviour is a simple implementation that send the execution state, at the same time that it
 * perform a diagnosis of the state in order to determine mechatronic component is in a state in
 * which it can be reconfigure. If the MC is in a direct recovery state it informs the other
 * instance that they need to recover
 * 
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/
public class MovingBehaviour extends SimpleBehaviour {
  // TODO cambiar el nombre de negotiation por algo como finalrunning

  private static final long serialVersionUID = 5211311085804151394L;
  static final Logger LOGGER = LogManager.getLogger(MovingBehaviour.class.getName());

  private int transitionFlag;
  private long nextActivation;
  private long realActivation;
  private MC_Agent myAgent;
  private boolean finished = false;
  private MessageTemplate template;
  private String activedIntace;

  public MovingBehaviour(MC_Agent a) {
    super(a);
    this.myAgent = a;
  }

  public void onStart() {
    LOGGER.entry();
    LOGGER.debug("***************** inicio MovingBehaviour");
    transitionFlag = 0;
    nextActivation = 0;
    realActivation = 0;
    finished = false;
    //Informacion para el movimiento de la aplicacion
    //Templatedel mensaje de movimiento
    template = MessageTemplate.MatchOntology("move");
    //Intancia a la cual se deve mover el sistema
    activedIntace = "";
    
    //Ensender el controlador en caso de que no este funcionando.
    try {
      // mandar el mensaje al concentrador para cambiar los mensajes de entradas y salidas al
      // controlador correspondiente
      DataIsoTCP.cambiarControl(myAgent.getContainerController().getContainerName(), myAgent.MC_ID);
      this.myAgent.functionalityInstance.startMCcode();
      LOGGER.debug("Execution of the mechatronic componet " + myAgent.cmpID + " started");
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    LOGGER.exit();
  }

  public void action() {
    LOGGER.entry();
    try {
      long t = 0;
      
      if (nextActivation - System.currentTimeMillis() <= 0) {
        /** toca activarse la ejecucion periodica: refresca estado a trackers **/
        // Si es periódica y estamos en activación calculamos la siguiente activación
        realActivation = System.currentTimeMillis();
        if (nextActivation == 0)
          nextActivation = realActivation + myAgent.period;
        else
          nextActivation = nextActivation + myAgent.period; // calculo siguiente activación

        //Obtener el estado y enviarlo
        byte[] state = myAgent.functionalityInstance.readState();
        myAgent.sendState((Serializable) state);
        
        // Comprovar si he recivido el mensaje que me dice que instancia debe de recuperarse
        if (activedIntace.equals("")) {
          ACLMessage msg = myAgent.receive(template);
          if (msg != null) {
            activedIntace = msg.getContent();
            LOGGER.info("== cmpinstance gandor: " + activedIntace + " Nombre local: " + myAgent.getLocalName());
          }
        } else {
          //Comprobar que la instancia a la cual no tengo que pasar es sea yo mismo
          if(myAgent.getLocalName().equals(activedIntace)){
            LOGGER.debug("== Tengo que pasar a Running");
            // Si tengo el mismo nombre que la instancia que me mandaron. Hay que cambiar a running y poner todos las demas instancins (pause) en tracking
            myAgent.sendCommand("localcmd " + myAgent.getInstances(myAgent.cmpID, "paused") + " cmd=setstate tracking");
            transitionFlag = ControlBehaviour.RUNNING;
            finished = true;
          }else{
            //Si tengo que lazar otra instancias
            //Comporvar que estoy en un estado directamente recuperable
            String value = myAgent.functionalityInstance.diagnosis(state);
            LOGGER.debug("== Valor del diagnostico: " + value);
            if(value.equals("0")){
            //Si es directamente recuperable 
              LOGGER.debug("== Comensando reconfiguracion");
              
              //Deterner la ejecucion del codigo
              this.myAgent.functionalityInstance.stopMCcode();
              
              //Obtener las instancias en pause
              String pausedIns = myAgent.getInstances(myAgent.cmpID, "paused");
              LOGGER.debug("== Instancias en pause: "+pausedIns);
              if (pausedIns.length() > 0) {
                //Quitar de las lista de pausadas la intncia que  va arecuperar la ejecuacion
                pausedIns = pausedIns.replace(activedIntace + ",", "").replace("," + activedIntace, "").replace(activedIntace, "");
                LOGGER.debug("== Instancias en para pasar a traking: "+pausedIns);
                //Cambiar las instancais en la lista de pausadas a tracking
                myAgent.sendCommand("localcmd " + pausedIns + " cmd=setstate tracking");
                
                //Cambiar la instancia a recovery
                myAgent.sendCommand("localcmd " + activedIntace + " cmd=setstate recovering");
                
                // Teminar el estado de negociacion y cambiar a traking
                transitionFlag = ControlBehaviour.TRACKING;
                finished = true;
              }
            }
          } 
        }
      } // end es periódica y toca activarse

      else if (nextActivation - System.currentTimeMillis() >= 0) { // es periódica pero no toca activarse
        /*
         * Es periódica pero no toca activarse. Preparo bloqueo junto a la esporádica en ms hasta la
         * siguiente activación
         */
        // calculo tiempo de siguiente activación
        t = nextActivation - System.currentTimeMillis();
        block(t);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    LOGGER.exit();

  }

  public int onEnd() {
    return transitionFlag;
  }

  @Override
  public boolean done() {
    return finished;
  }
}
