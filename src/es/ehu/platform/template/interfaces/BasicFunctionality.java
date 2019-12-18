package es.ehu.platform.template.interfaces;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.platform.MWAgent;

public interface BasicFunctionality extends Serializable {
  
  static final Logger LOGGER = LogManager.getLogger(BasicFunctionality.class.getName()) ;
	
	public String init(MWAgent myAgent);
		
	public Object execute(Object[] input); //ejecutar funcionalidad y devuelve un objeto
	
	//public void stop();
	

}
