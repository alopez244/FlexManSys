package es.ehu.domain.sputnik;

import java.io.Serializable;
import java.util.Date;

import es.ehu.platform.MWAgent;
import jade.wrapper.*;
import jade.core.*;
import es.ehu.platform.template.interfaces.BasicFunctionality;

//java -cp lib\jade.jar jade.Boot -gui
//inicia nodo0 Procesador1 "(es.ehu.FuncionalidadProcesador,0,Consumidor)"
//inicia nodo0 Procesador2 "(es.ehu.FuncionalidadProcesador,0,Consumidor)"
public class FunctionalityProcesador implements BasicFunctionality {
	int state = 0;
	Dato msg = null;
	
	
	public FunctionalityProcesador () {
	}

	public String init(MWAgent myAgent) {
		return "done";
	}

	public Object getState () {
		return new Integer(state);
	}

	public void setState (Object state) {
		this.state = ((Integer)state).intValue();
		
	}

	public Object execute(Object[] input) {
		this.state++;
		this.msg = (Dato)input[0];
		msg.incrementa(this.state);
		long delay = System.currentTimeMillis()-msg.getCreationMillis();
		LOGGER.info("    Proces: "+(msg.getValor()-1)+"+"+this.state+"="+msg.getValor()+"("+delay+")");
		
		
		return this.msg;
	}
		
	
	

}