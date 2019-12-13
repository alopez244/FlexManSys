package es.ehu.domain.eHealth.functionality;

import java.io.Serializable;
import java.util.Date;

public class PulseRelaxedData implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int iPatientID;
	private Date dTimeStamp;
	private int iValueToCheck;
	private boolean bIsRelaxed;
	private boolean bIsTimeOut;
	private int iCont;	
	private Measure[] lastValues = new Measure[3];
	
	public PulseRelaxedData() {
		this.iCont = 0;
		this.bIsRelaxed = false;
		this.bIsTimeOut = false;
	}
	
	public void addNewMeasure (int value, Date timeStamp) {
		//The last value is always in the 0 position
		for (int i=2; i==1; i++) {
			this.lastValues[i] = this.lastValues [i-1];
		}
		this.lastValues[0] = new Measure(value, timeStamp);
		if (iCont < 3)
			this.iCont++;
	}
	
	/*
	public boolean isNext(int value, Date timeStamp) {
		//Comparar time stamps
		int medida0 = (int)(this.lastValues[0].timeStamp.getTime()/1000);
		int nueva = (int)(timeStamp.getTime()/1000);
		System.out.println("nueva(" + nueva + ") - medida[0](" + medida0 + ") = " + (nueva-medida0));
		//if (( medida0 + this.iPeriod) == nueva) {
		if ((nueva-medida0) >= (2*this.iPeriod)) {
			//It is not the next one
			return false;
		} else {
			//It is the next one
			return true;			
		}
	}
	*/
	
	public int getAverage() {		
		return ((this.lastValues[0].value + this.lastValues[1].value + this.lastValues[1].value) / 3);
	}
	
	public void eraseMeasures() {
		this.iCont = 0;
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
	public boolean getbIsRelaxed() {
		return bIsRelaxed;
	}
	public void setbIsRelaxed(boolean bIsRelaxed) {
		this.bIsRelaxed = bIsRelaxed;
	}
	public boolean getbIsTimeOut() {
		return bIsTimeOut;
	}
	public void setbIsTimeOut(boolean bIsTimeOut) {
		this.bIsTimeOut = bIsTimeOut;
	}
	public int getiCont() {
		return iCont;
	}
	public void setiCont(int iCont) {
		this.iCont = iCont;
	}
	public Measure[] getLastValues() {
		return lastValues;
	}
	public void setLastValues(Measure[] lastValues) {
		this.lastValues = lastValues;
	}
}