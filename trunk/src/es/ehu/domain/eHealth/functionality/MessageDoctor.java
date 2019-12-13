package es.ehu.domain.eHealth.functionality;

import java.util.Date;

public class MessageDoctor {

	public void mandarMensajeMedico(int patientID, Date timeStamp, int pulseValue){
		System.out.println("********************************************");
		System.out.println("El paciente "+patientID+" NO SE RELAJA.");
		System.out.println("El pulos es:"+pulseValue);
		System.out.println("Menasje llego "+timeStamp);
		System.out.println("********************************************");
	}
	
}
