package es.ehu.domain.eHealth.functionality;

import java.util.Date;

import jade.util.leap.Serializable;

public class PulseCheckingData implements Serializable {	
	private static final long serialVersionUID = 1L;
	
	private int iPatientID;
	private Date dTimeStamp;
	private int iValueToCheck;
	private boolean bIsInside;
	
	public PulseCheckingData (int patientID, Date timeStamp, int value) {
		this.iPatientID = patientID;
		this.dTimeStamp = timeStamp;
		this.iValueToCheck = value;
		this.bIsInside = false;
	}
	
	public int getiPatientID() {
		return iPatientID;
	}
	public void setiPatientID(int iPatientID) {
		this.iPatientID = iPatientID;
	}
	public Date getdTimeStamp() {
		return dTimeStamp;
	}
	public void setdTimeStamp(Date dTimeStamp) {
		this.dTimeStamp = dTimeStamp;
	}
	public int getiValueToCheck() {
		return iValueToCheck;
	}
	public void setiValueToCheck(int iValueToCheck) {
		this.iValueToCheck = iValueToCheck;
	}
	public boolean getbIsInside() {
		return bIsInside;
	}
	public void setbIsInside(boolean bIsInside) {
		this.bIsInside = bIsInside;
	}
}
