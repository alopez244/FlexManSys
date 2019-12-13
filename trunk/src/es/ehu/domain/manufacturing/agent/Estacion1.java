package es.ehu.domain.manufacturing.agent;

public class Estacion1 extends MC_Agent {
	private static final long serialVersionUID = -2508355434242241746L;

	@Override
	protected void variableInitialization(){
	  
		XMLState="Y:/PLC_Recon/Estados_Info/StateVariables_estacion1.xml";
		codeExecutionFlag="Wrapper_MC1.start";
		stateArray="Wrapper_MC1.StoreState.datos";
		XMLDiagnosis="Y:/PLC_Recon/Estados_Info/Diagnosis_estacion1.xml";
		MC_ID="1";
	}
}
