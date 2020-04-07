package es.ehu.domain.manufacturing.agent;

public class Estacion2 extends MC_Agent {
	private static final long serialVersionUID = -2508355434242241746L;

	@Override
	protected void variableInitialization(){
	  
		XMLState="Y:/PLC_Recon/Estados_Info/StateVariables_estacion2.xml";
		codeExecutionFlag="Wrapper_MC2.start";
		stateArray="Wrapper_MC2.StoreState.datos";
		XMLDiagnosis="Y:/PLC_Recon/Estados_Info/Diagnosis_estacion2.xml";
		MC_ID="2";
	}
}
