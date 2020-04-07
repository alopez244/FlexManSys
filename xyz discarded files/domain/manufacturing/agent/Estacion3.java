package es.ehu.domain.manufacturing.agent;

public class Estacion3 extends MC_Agent {
	private static final long serialVersionUID = -2508355434242241746L;

	@Override
	protected void variableInitialization(){
	  
		XMLState="Y:/PLC_Recon/Estados_Info/StateVariables_estacion3.xml";
		codeExecutionFlag="Wrapper_MC3.start";
		stateArray="Wrapper_MC3.StoreState.datos";
		XMLDiagnosis="Y:/PLC_Recon/Estados_Info/Diagnosis_estacion3.xml";
		MC_ID="3";
	}
}
