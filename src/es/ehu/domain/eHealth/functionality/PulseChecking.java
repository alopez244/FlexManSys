package es.ehu.domain.eHealth.functionality;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.HashMap;

import javax.swing.Timer;

public class PulseChecking {
	private HashMap<Object, Object> patientPropList;
	private boolean isFirstTime;
	private Timer relaxedWaitTimer; // Timer for waiting to be relaxed
	private PulseRelaxedData relaxedData;
	private String repositoryFile = "";

	public PulseChecking(String repository) {
		this.isFirstTime = true;
		repositoryFile=repository;
	}

	/*
	 * public PulseChecking (int period) {
	 * this.isFirstTime = true;
	 * relaxedData = new PulseRelaxedData(period);
	 * }
	 */

	public PulseChecking(int waitTimeMs, String repository) {
		this.isFirstTime = true;
		// How much time (at most) waits the application until the patient is relaxed.
		relaxedWaitTimer = new WatchDog(waitTimeMs);
		relaxedData = new PulseRelaxedData();
		repositoryFile=repository;
	}

	/*
	 * public PulseChecking (int waitTimeMs, int period) {
	 * this.isFirstTime = true;
	 * //How much time (at most) waits the application until the patient is relaxed.
	 * relaxedWaitTimer = new WatchDog(waitTimeMs);
	 * relaxedData = new PulseRelaxedData (period);
	 * }
	 */

	public PulseCheckingData checkPulseInterval(int patientID, Date timeStamp, int pulseValue) {
		PulseCheckingData data = new PulseCheckingData(patientID, timeStamp, pulseValue);

		isFirstTime(patientID);
		int min = Integer.parseInt((String) this.patientPropList.get("FCMin"));
		int max = Integer.parseInt((String) this.patientPropList.get("FCMax"));

		if (pulseValue >= min && pulseValue <= max) {
			data.setbIsInside(true);
		} else {
			data.setbIsInside(false);
		}

		return data;
	}

	public void checkUnsuitablePulse(int patientID, Date timeStamp, int pulseValue) {
		isFirstTime(patientID);

		if (relaxedData.getiCont() == 0) {
			System.out.println("Primera medida al array");
			relaxedData.addNewMeasure(pulseValue, timeStamp);
		} else {
			System.out.println("No es la primer medida");
			System.out.println("A�adir medida al array");
			relaxedData.addNewMeasure(pulseValue, timeStamp);
			// If there are three measurements. Medical staff is warned.
			if (relaxedData.getiCont() == 3) {
				System.out.println("ENVIAR AVISO AL PERSONAL M�DICO");
				String cadena = "WARNING MESSAGE:\n\n" + "The patient number '" + patientID + "' (" + this.patientPropList.get("Name") + " " +
						this.patientPropList.get("SurName") + ") is having unsuitable pulse rate, of about " + pulseValue + ".";
				System.out.println(cadena);
			}
		}
	}

	/*
	 * public void checkUnsuitablePulse (int patientID, Date timeStamp, int pulseValue) {
	 * isFirstTime(patientID);
	 * 
	 * if (relaxedData.getiCont() == 0) {
	 * System.out.println ("Primera medida al array");
	 * relaxedData.addNewMeasure(pulseValue, timeStamp);
	 * } else {
	 * System.out.println ("No es la primer medida");
	 * if (relaxedData.isNext(pulseValue, timeStamp)) {
	 * System.out.println ("A�adir medida al array");
	 * relaxedData.addNewMeasure(pulseValue, timeStamp);
	 * //If there are three measurements. Medical staff is warned.
	 * if (relaxedData.getiCont() == 3) {
	 * System.out.println ("ENVIAR AVISO AL PERSONAL M�DICO");
	 * String cadena = "WARNING MESSAGE:\n\n" + "The patient number '" + patientID + "' (" + this.patientPropList.get("Name") + " " +
	 * this.patientPropList.get("SurName") + ") is having unsuitable pulse rate, of about " + pulseValue + ".";
	 * System.out.println (cadena);
	 * }
	 * } else {
	 * //The array is emptied
	 * relaxedData.eraseMeasures();
	 * System.out.println ("No es la siguiente. Vaciar array");
	 * }
	 * }
	 * }
	 */

	public PulseRelaxedData checkPulseRelaxed(int patientID, Date timeStamp, int pulseValue) {
		relaxedData.setiPatientID(patientID);
		relaxedData.setdTimeStamp(timeStamp);
		relaxedData.setiValueToCheck(pulseValue);

		if (this.isFirstTime) {
			isFirstTime(patientID);
			// start timer
			System.out.println("Iniciamos timer");
			relaxedWaitTimer.start();
		}

		if (relaxedData.getiCont() == 0) {
			System.out.println("Primera medida al array");
			relaxedData.addNewMeasure(pulseValue, timeStamp);
		} else {
			System.out.println("No es la primer medida");
			// A�adir al array
			relaxedData.addNewMeasure(pulseValue, timeStamp);
			// If there are three measurements. Check for relaxed.
			if (relaxedData.getiCont() == 3) {
				System.out.println("Comprobar media");
				int average = relaxedData.getAverage();
				int FCR = Integer.parseInt((String) this.patientPropList.get("FCR"));
				if ((average >= (FCR - 5)) && (average <= (FCR + 5))) {
					System.out.println("Est� relajado");
					relaxedData.setbIsRelaxed(true);
				} else {
					System.out.println("No est� relajado");
				}
			} else
				System.out.println("No hay 3 medidas. No se calcula la media");
		}

		return relaxedData;
	}

	/*
	 * public PulseRelaxedData checkPulseRelaxed (int patientID, Date timeStamp, int pulseValue) {
	 * relaxedData.setiPatientID(patientID);
	 * relaxedData.setdTimeStamp(timeStamp);
	 * relaxedData.setiValueToCheck(pulseValue);
	 * 
	 * if (this.isFirstTime) {
	 * isFirstTime(patientID);
	 * //start timer
	 * System.out.println ("Iniciamos timer");
	 * relaxedWaitTimer.start();
	 * }
	 * 
	 * if (relaxedData.getiCont() == 0) {
	 * System.out.println ("Primera medida al array");
	 * relaxedData.addNewMeasure(pulseValue, timeStamp);
	 * } else {
	 * System.out.println ("No es la primer medida");
	 * if (relaxedData.isNext(pulseValue, timeStamp)) {
	 * System.out.println ("Es siguiente.");
	 * //A�adir al array
	 * relaxedData.addNewMeasure(pulseValue, timeStamp);
	 * //If there are three measurements. Check for relaxed.
	 * if (relaxedData.getiCont() == 3) {
	 * System.out.println ("Comprobar media");
	 * int average = relaxedData.getAverage();
	 * int FCR = Integer.parseInt((String)this.patientPropList.get("FCR"));
	 * if ((average >= (FCR-5)) && (average <= (FCR+5))) {
	 * System.out.println ("Est� relajado");
	 * relaxedData.setbIsRelaxed(true);
	 * } else {
	 * System.out.println ("No est� relajado");
	 * }
	 * } else
	 * System.out.println ("No hay 3 medidas. No se calcula la media");
	 * } else {
	 * //Vaciar array
	 * System.out.println ("No es siguiente. Vaciar array");
	 * relaxedData.eraseMeasures();
	 * }
	 * }
	 * 
	 * return relaxedData;
	 * }
	 */
	public CheckRelaxed_State getPulseRelaxedState() {
		return new CheckRelaxed_State(relaxedData);
	}

	public void setPulseRelaxedState(CheckRelaxed_State executionState) {
		relaxedData.setiPatientID(executionState.getiPatientID());
		relaxedData.setdTimeStamp(executionState.getdTimeStamp());
		relaxedData.setiValueToCheck(executionState.getiValueToCheck());
		relaxedData.setbIsRelaxed(executionState.isbIsRelaxed());
		relaxedData.setbIsTimeOut(executionState.isbIsTimeOut());
		relaxedData.setiCont(executionState.getiCont());
		relaxedData.setLastValues(executionState.getLastValues());
	}

	private void isFirstTime(int patientID) {
		if (this.isFirstTime) {
			System.out.println("Primera Vez");
			this.isFirstTime = false;
			// Leer las propiedades del paciente
			String fichName = repositoryFile + patientID + ".txt";
			PatientProperties patientProps = new PatientProperties(fichName);
			this.patientPropList = patientProps.getProperties();
		}
	}

	private class WatchDog extends Timer {
		private static final long serialVersionUID = -4208011866630959521L;

		public WatchDog(int delay) {
			super(delay, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// Comprobamos una �ltima vez
					System.out.println("WatchDog dice que Time Out");
					if (relaxedData.getbIsRelaxed()) {
						relaxedData.setbIsTimeOut(false);
					} else {
						relaxedData.setbIsTimeOut(true);
					}
					relaxedWaitTimer.stop();
				}
			});
		}
	}
}
