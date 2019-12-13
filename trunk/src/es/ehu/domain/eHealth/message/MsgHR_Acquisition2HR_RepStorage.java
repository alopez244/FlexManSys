/*
 * GENERACI�N AUTOM�TICA DE C�DIGO POR CADA CONECTOR ASOCIADO AL PUERTO DE SALIDA
 * Al agente HR_RepStorage se le env�a: pulso, timeStamp y patientID
 */		

package es.ehu.domain.eHealth.message;

import java.io.Serializable;
import java.util.Date;

public class MsgHR_Acquisition2HR_RepStorage implements Serializable {
	private static final long serialVersionUID = 1L;
	private int dato_1;
	private Date dato_2;
	private int dato_3;
		
	public MsgHR_Acquisition2HR_RepStorage (int dato_1, Date dato_2, int dato_3) {
		this.dato_1 = dato_1;
		this.dato_2 = dato_2;
		this.dato_3 = dato_3;			
	}
	
	public int getdato_1() {
		return this.dato_1;
	}
	public Date getdato_2() {
		return this.dato_2;
	}
	public int getdato_3() {
		return this.dato_3;
	}
}
