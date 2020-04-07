package es.ehu.domain.sputnik;

import java.io.Serializable;

import es.ehu.platform.MWAgent;
import jade.core.Agent;
import es.ehu.platform.template.interfaces.BasicFunctionality;

//inicia nodo1 Consumidor "(es.ehu.FuncionalidadConsumidor,0)"
public class FunctionalityConsumidor implements BasicFunctionality {
	Object state = null;
	Dato msg = null;
//	Agent myAgent = null;

	public String init(MWAgent myAgent){
		return "done";
	}

	public FunctionalityConsumidor () {
	}

	//public FunctionalityConsumidor (Agent myAgent) {
	//	this.myAgent = myAgent;
	//}
	
	public Object getState () {
		return null;
	}
	
	public void setState (Object state) {
		this.state = state;
	}
	
	public Object execute(Object[] input) {
		this.msg = ((Dato)input[0]);
		long delay = System.currentTimeMillis()-msg.getCreationMillis();
		LOGGER.info("    Consum: "+msg.getValor()+"("+delay+")");
		return null;
	}
	
}