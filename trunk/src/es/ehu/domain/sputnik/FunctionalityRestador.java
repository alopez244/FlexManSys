package es.ehu.domain.sputnik;

import java.io.Serializable;
import java.util.Date;

import jade.wrapper.*;
import jade.core.*;
import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.BasicFunctionality;

//java -cp lib\jade.jar jade.Boot -gui
//inicia nodo0 Procesador1 "(es.ehu.FuncionalidadProcesador,0,Consumidor)"
//inicia nodo0 Procesador2 "(es.ehu.FuncionalidadProcesador,0,Consumidor)"
public class FunctionalityRestador implements BasicFunctionality {
	Object state = null;
	Dato msg = null;
	Agent myAgent = null;
	
	public FunctionalityRestador () {
	}
	
	public String init () {
		return "done";
	}
	
	public FunctionalityRestador (Agent myAgent) {
		this.myAgent = myAgent;
	} 
	
	public Object getState () {
		return null;
	}
	
	public void setState (Object state) {
		this.state = state;
	}
	
	public Object execute(Object[] input) {
		
		msg = (Dato)input[0];
		msg.decrementa(((Dato)input[1]).getValor());
		
		
		
		
		/*if ((myAgent!=null) && (this.msg.getValor()>950)) {
			//inicio otra aplicaci�n
			System.out.println("Inicio otra aplicaci�n");
			Object[] argumentsConsumidor = {"es.ehu.FuncionalidadConsumidor","0"};
			
			try {
				startNewAgent("es.ehu.Rocher", "Consumidor2", argumentsConsumidor);
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	*/
		
		
		return msg;
	}
		
	
	private void startNewAgent(String className,String agentName,Object[] arguments) throws StaleProxyException {
		
		((AgentController)myAgent.getContainerController().createNewAgent(agentName,className, arguments)).start();
	}

  @Override
  public String init(MWAgent myAgent) {
    // TODO Auto-generated method stub
    return null;
  }

}