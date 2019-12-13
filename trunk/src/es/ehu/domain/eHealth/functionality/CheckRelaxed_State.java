package es.ehu.domain.eHealth.functionality;

import java.io.Serializable;
import java.util.Date;

public class CheckRelaxed_State implements Serializable {
	private static final long serialVersionUID = 1L;
	private int iPatientID;
	private Date dTimeStamp;
	private int iValueToCheck;
	private boolean bIsRelaxed;
	private boolean bIsTimeOut;
	private int iCont;	
	private Measure[] lastValues = new Measure[3];
	
	/**
	 * In order to provide the component's state, a new CheckRelaxed_State object is created
	 * @param data
	 */
	public CheckRelaxed_State (PulseRelaxedData data) {
		this.iPatientID = data.getiPatientID();
		this.dTimeStamp = data.getdTimeStamp();
		this.iValueToCheck = data.getiValueToCheck();
		this.bIsRelaxed = data.getbIsRelaxed();
		this.bIsTimeOut = data.getbIsTimeOut();
		this.iCont = data.getiCont();
		this.lastValues = data.getLastValues();		
	}

	public int getiPatientID() {
		return iPatientID;
	}

	public Date getdTimeStamp() {
		return dTimeStamp;
	}

	public int getiValueToCheck() {
		return iValueToCheck;
	}

	public boolean isbIsRelaxed() {
		return bIsRelaxed;
	}

	public boolean isbIsTimeOut() {
		return bIsTimeOut;
	}

	public int getiCont() {
		return iCont;
	}

	public Measure[] getLastValues() {
		return lastValues;
	}		
}
