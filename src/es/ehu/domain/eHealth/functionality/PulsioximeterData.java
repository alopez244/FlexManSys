package es.ehu.domain.eHealth.functionality;

import java.io.Serializable;
import java.util.Date;

public class PulsioximeterData implements Serializable {	
	private static final long serialVersionUID = 1L;
	private int iPulse; //patient's pulse
	private int iSPO2; //patient's oxygenation
	private boolean bIsTimeOut; //If the timer has reached its limit (true) or not (false) 
	private Date dTimeStamp; //Time of measurement
	private int iPatientID; //Patient's identifier
	private boolean bIsValid; //If the measurement is valid (true, when != 0) or not (false)
	
	public PulsioximeterData (int patientID) {
		this.iPulse = 0;
		this.iSPO2 = 0;
		this.bIsTimeOut = false;
		this.iPatientID = patientID;
		this.bIsValid = false;
	}
	
	public boolean getbIsValid() {
		return this.bIsValid;
	}
	public void setbIsValid(boolean isValid) {
		this.bIsValid = isValid;
	}
	public int getiPulse() {
		return iPulse;
	}
	public void setiPulse(int iPulse) {
		this.iPulse = iPulse;
	}
	public int getiSPO2() {
		return iSPO2;
	}
	public void setiSPO2(int iSPO2) {
		this.iSPO2 = iSPO2;
	}
	public boolean getIsTimeOut() {
		return bIsTimeOut;
	}
	public void setIsTimeOut(boolean bIsTimeOut) {
		this.bIsTimeOut = bIsTimeOut;
	}
	public Date getdTimeStamp() {
		return dTimeStamp;
	}
	public void setdTimeStamp(Date dTimeStamp) {
		this.dTimeStamp = dTimeStamp;
	}
	//No hay set, porque s�lo se puede cambiar en la construcci�n de la clase
	public int getiPatientID() {
		return iPatientID;
	}	
}
