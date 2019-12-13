package es.ehu.domain.sputnik;

import jade.core.Agent;
import jade.util.Logger;

import java.io.Serializable;
import java.util.Date;

import es.ehu.platform.template.interfaces.BasicFunctionality;

//inicia nodo0 Generador "(es.ehu.FuncionalidadGenerador,1000,Procesador1|Procesador2)"
public class FunctionalityGenerador implements BasicFunctionality {
	int state = 0;
	Dato msg = null;
	//Agent myAgent = null;
	
	public String init(){
		return "Ok";
		
	}
	
	public FunctionalityGenerador () {
	}
	
	//public FuncionalidadGenerador (Agent myAgent) {
	//	this.myAgent = myAgent;
	//} 
	
	public Object getState () {
		return new Integer(state);
	}
	
	public void setState (Object state) {
		this.state = ((Integer)state).intValue();
	}
	
	public Object execute(Object[] input) {
		this.state=this.state+2;
		this.msg = new Dato(this.state);
		LOGGER.info("    Genera: "+((Dato)msg).toString());
		
		return msg;
	}
	
	
	
}
