package es.ehu.domain.sputnik;

import java.io.Serializable;
import java.util.Date;

import es.ehu.platform.MWAgent;
import jade.wrapper.*;
import jade.core.*;
import es.ehu.domain.sputnik.ProcessingNode.NodeAgentBehaviour;
import es.ehu.domain.sputnik.ProcessingNode.ShutdownThread;
import es.ehu.platform.template.interfaces.BasicFunctionality;
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

//java -cp lib\jade.jar jade.Boot -gui
//inicia nodo0 Procesador1 "(es.ehu.FuncionalidadProcesador,0,Consumidor)"
//inicia nodo0 Procesador2 "(es.ehu.FuncionalidadProcesador,0,Consumidor)"
public class FunctionalityProcessingNode implements BasicFunctionality {
	/**
   * 
   */
  private static final long serialVersionUID = 1L;
  int state = 0;
	Dato msg = null;
	
	
	public FunctionalityProcessingNode () {
	}

	public String init(MWAgent myAgent) {
	  System.out.println("ejecuto init en functionalityProcessingNode");
//	  LOGGER.entry();
//    
//    try {
//         Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
//         LOGGER.info("Añadida tarea de apagado...");
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
//    } 
//    
//    addBehaviour(new NodeAgentBehaviour(this));
//    extraBehaviour(this);
//    LOGGER.exit();
	  
		return "running";
	}

	public Object getState () {
		return new Integer(state);
	}

	public void setState (Object state) {
		this.state = ((Integer)state).intValue();
		
	}

	public Object execute(Object[] input) {
		System.out.println("entro en execute");
		try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
		
		return "ddd";
	}
		
	
	

}