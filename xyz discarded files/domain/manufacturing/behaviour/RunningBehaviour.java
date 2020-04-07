package es.ehu.domain.manufacturing.behaviour;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.domain.manufacturing.agent.MC_Agent;
import es.ehu.domain.manufacturing.libConcentrador.DataIsoTCP;
import jade.core.behaviours.*;

/**
 * This behaviour is a simple implementation of a message receiver.
 * It check if a satate is recive and stores it.
 * If the timeout expires before any message arrives, the behaviour
 * transition into a negotiation.
 * 
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/
public class RunningBehaviour extends SimpleBehaviour {
	
	private static final long serialVersionUID = 5211311085804151394L;	
	static final Logger LOGGER = LogManager.getLogger(RunningBehaviour.class.getName());
	
		
	private int transitionFlag;
	private long nextActivation;
	private long realActivation;
	private MC_Agent myAgent;
	private boolean finished = false;
	
	public RunningBehaviour(MC_Agent a) {
		super(a);
		this.myAgent = a;
		LOGGER.debug("***************** inicio RunningBehaviour");
	}

	public void onStart(){
	  LOGGER.entry();
	  
	  transitionFlag = 0;
    nextActivation = 0;
    realActivation=0; 
    finished = false;

		try {
		//mandar el mensaje al concentrador para cambiar los mensajes de entradas y salidas al controlador correspondiente 
      DataIsoTCP.cambiarControl(myAgent.getContainerController().getContainerName(),myAgent.MC_ID);
      this.myAgent.functionalityInstance.startMCcode();
      LOGGER.debug("Execution of the mechatronic componet "+ myAgent.cmpID+" started");
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

        myAgent.sendState((Serializable)myAgent.functionalityInstance.readState());
        

      } // end es periódica y toca activarse

      else if (nextActivation - System.currentTimeMillis() >= 0) { // es periódica pero no toca activarse
        /** Es periódica pero no toca activarse. Preparo bloqueo junto a la esporádica en ms hasta la siguiente activación **/
        /** calculo tiempo de siguiente activación */
        t = nextActivation - System.currentTimeMillis();
        block (t);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
	  LOGGER.exit();
	
	}
//
	public int onEnd() {
		return transitionFlag;
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return finished;
	}
}