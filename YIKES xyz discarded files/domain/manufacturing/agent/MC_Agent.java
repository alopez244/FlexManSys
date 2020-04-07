package es.ehu.domain.manufacturing.agent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviourMW;
import es.ehu.domain.manufacturing.behaviour.BootBehaviour;
import es.ehu.domain.manufacturing.behaviour.EndBehaviour;
import es.ehu.domain.manufacturing.behaviour.MovingBehaviour;
import es.ehu.domain.manufacturing.behaviour.NegotiatingBehaviour;
import es.ehu.domain.manufacturing.behaviour.PausedBehaviour;
import es.ehu.domain.manufacturing.behaviour.RecoverBehaviour;
import es.ehu.domain.manufacturing.behaviour.RunningBehaviour;
import es.ehu.domain.manufacturing.behaviour.TrackingBehaviour;
import es.ehu.domain.manufacturing.lib.FunctionalityPLC;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.utilities.StateParallel;

/**
 * Agenet base del cual cuelgan todos los agentes asocuados a componentes mecatronidos del PLC
 * 
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 */
public class MC_Agent extends MWAgent {

	private static final long serialVersionUID = 2476743710831028702L;
	static final Logger LOGGER = LogManager.getLogger(MC_Agent.class.getName());
	
	public FunctionalityPLC functionalityInstance;
	/**
	 * XML con las variables que conforman al estado
	 */
	public String XMLState = "";
	/**
	 * Nombre de la variables que permite la ejecución del codigo de la funcionalidad
	 */
	public String codeExecutionFlag = "";
	/**
	 * Nombre del array que contienes los valores del estado
	 */
	public String stateArray = "";
	/**
	 * XML con los datos necesarios para hacer el diagnostico.
	 */
	public String XMLDiagnosis = "";
	/**
   *  ID del componente mecatronico
   */
  public String MC_ID;

  @Override
  protected void setupContent() {
		/** Definir el agente **/
	  sourceComponentIDs = new String[] {};
		targetComponentIDs = new String[] {};
		
		variableInitialization();
		
		LOGGER.debug("Variables iniciales:\n"+
		    "XMLState="+XMLState+"\n"+
		    "codeExecutionFlag="+codeExecutionFlag+"\n"+
		    "stateArray="+stateArray+"\n"+
		    "XMLDiagnosis="+XMLDiagnosis+"\n"+
		    "MC_ID="+MC_ID);

		/** Comportamiento Agente FSM **/
		FSMBehaviourMW comportamientoFSM = new FSMBehaviourMW(this);

		/** Comportamiento initialized **/
		Behaviour boot = new BootBehaviour(this,XMLState, codeExecutionFlag,stateArray,XMLDiagnosis); // end boot

		/** Comportamiento running **/
		Behaviour running = new RunningBehaviour(this);

		/** Comportamiento tracking **/
		Behaviour tracking = new TrackingBehaviour(this);

		/** Comportamiento negotiation **/
		Behaviour moving = new MovingBehaviour(this);
		
		/** Comportamiento pause **/
    Behaviour paused = new PausedBehaviour(this);

		/** Comportamiento recovery **/
		Behaviour recovering = new RecoverBehaviour(this);

		/** Comportamiento end **/
		Behaviour end = new EndBehaviour(this); // end end

		
		/** FSM state definition **/
    comportamientoFSM.registerFirstState(new StateParallel(this, boot), "boot");
    comportamientoFSM.registerState(new StateParallel(this, running), "running");
    comportamientoFSM.registerState(new StateParallel(this, tracking), "tracking");
    comportamientoFSM.registerState(new StateParallel(this, moving), "moving");
    comportamientoFSM.registerState(new StateParallel(this, paused), "paused");
    comportamientoFSM.registerState(new StateParallel(this, recovering), "recovering");
    comportamientoFSM.registerLastState(new StateParallel(this, end), "end");
    
    /** FSM transition **/  
    comportamientoFSM.registerTransition("boot", "running", ControlBehaviour.RUNNING, new String [] {"boot"});
    comportamientoFSM.registerTransition("boot", "tracking", ControlBehaviour.TRACKING, new String [] {"boot"});
//    comportamientoFSM.registerTransition("boot", "paused", ControlBehaviour.PAUSED, new String [] {"boot"});
    comportamientoFSM.registerTransition("boot", "moving", ControlBehaviour.NEGOTIATING, new String [] {"boot"});
    comportamientoFSM.registerTransition("boot", "end", ControlBehaviour.STOP, new String [] {"boot"});

    comportamientoFSM.registerTransition("running", "tracking", ControlBehaviour.TRACKING, new String [] {"running"});
//    comportamientoFSM.registerTransition("running", "paused", ControlBehaviour.PAUSED, new String [] {"running"});
    comportamientoFSM.registerTransition("running", "moving", ControlBehaviour.NEGOTIATING, new String [] {"running"});
    comportamientoFSM.registerTransition("running", "end", ControlBehaviour.STOP, new String [] {"running"});
    
    //comportamientoFSM.registerTransition("tracking", "running", ControlBehaviour.RUNNING, new String [] {"tracking"});
    comportamientoFSM.registerTransition("tracking", "recovering", ControlBehaviour.RUNNING, new String [] {"tracking"});//TODO se ha modificado el cambio a recovering para que funcione con el MW
    comportamientoFSM.registerTransition("tracking", "recovering", ControlBehaviour.RECOVERING, new String [] {"tracking"});
//    comportamientoFSM.registerTransition("tracking", "paused", ControlBehaviour.PAUSED, new String [] {"tracking"});
    comportamientoFSM.registerTransition("tracking", "negotiating", ControlBehaviour.NEGOTIATING, new String [] {"tracking"});
    comportamientoFSM.registerTransition("tracking", "end", ControlBehaviour.STOP, new String [] {"tracking"});

    //comportamientoFSM.registerTransition("paused", "running", ControlBehaviour.RUNNING, new String [] {"paused"});
    comportamientoFSM.registerTransition("paused", "recovering", ControlBehaviour.RUNNING, new String [] {"paused"});//TODO se ha modificado el cambio a recovering para que funcione con el MW
    comportamientoFSM.registerTransition("paused", "recovering", ControlBehaviour.RECOVERING, new String [] {"paused"});
    comportamientoFSM.registerTransition("paused", "tracking", ControlBehaviour.TRACKING, new String [] {"paused"});
    comportamientoFSM.registerTransition("paused", "moving", ControlBehaviour.NEGOTIATING, new String [] {"paused"});
    comportamientoFSM.registerTransition("paused", "end", ControlBehaviour.STOP, new String [] {"paused"});

    comportamientoFSM.registerTransition("recovering", "running", ControlBehaviour.RUNNING, new String [] {"recovering"});
//    comportamientoFSM.registerTransition("recovering", "paused", ControlBehaviour.PAUSED, new String [] {"recovering"});
    comportamientoFSM.registerTransition("recovering", "tracking", ControlBehaviour.TRACKING, new String [] {"recovering"});
    comportamientoFSM.registerTransition("recovering", "moving", ControlBehaviour.NEGOTIATING, new String [] {"recovering"});
    comportamientoFSM.registerTransition("recovering", "end", ControlBehaviour.STOP, new String [] {"recovering"});
    
    comportamientoFSM.registerTransition("moving", "running", ControlBehaviour.RUNNING, new String [] {"moving"});
    comportamientoFSM.registerTransition("moving", "recovering", ControlBehaviour.RECOVERING, new String [] {"moving"});
//    comportamientoFSM.registerTransition("moving", "paused", ControlBehaviour.PAUSED, new String [] {"moving"});
    comportamientoFSM.registerTransition("moving", "tracking", ControlBehaviour.TRACKING, new String [] {"moving"});
    comportamientoFSM.registerTransition("moving", "end", ControlBehaviour.STOP, new String [] {"moving"});
    
		this.addBehaviour(comportamientoFSM);
		LOGGER.debug("inicializada la maquina de estados");
	}
	
	/**
	 * Permite la inicalizacion de las variables del estado y diagnostico
	 */
	protected void variableInitialization(){
		
	}
	
	protected void takeDown() {
    try {
      System.out.println("Se ha terminado - " + this.getAID().getName());
      // Apagar el programa
      this.functionalityInstance.stopMCcode();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
