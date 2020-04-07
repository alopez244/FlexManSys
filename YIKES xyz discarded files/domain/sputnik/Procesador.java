package es.ehu.domain.sputnik;

import es.ehu.platform.template.ApplicationAgentTemplate;

public class Procesador extends ApplicationAgentTemplate {

	private static final long serialVersionUID = -3415727708050658595L;
	
	
	private long startTime = 0;

	public Procesador() {
	  functionalityInstance  = new es.ehu.domain.sputnik.FunctionalityProcesador();
	}

}