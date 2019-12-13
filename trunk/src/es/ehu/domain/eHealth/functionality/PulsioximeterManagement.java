/**
 * This class manages the pulsioximeter
 */
package es.ehu.domain.eHealth.functionality;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.Date;

import javax.swing.Timer;

import ehu.mas.eHealthNativeMethods;

public class PulsioximeterManagement {
	private Timer waitTimer; // Timer for waiting control
	private PulsioximeterData data;
	private boolean isFirstTime; // If this is the first time a measurement is asked

	/**
	 * Initialization tasks. Sensor has to be initialized as well as the counter that indicates if the waiting time is out.
	 */
	public PulsioximeterManagement(int waitTimeMs, int patientID) {
		// Set the "java.library.path" system library for the JNI
		try {
			System.setProperty("java.library.path", "/home/pi/JNIlib/");
			Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
			sysPathsField.setAccessible(true);
			sysPathsField.set(null, null);

			this.isFirstTime = true; // Initially, true.
			this.data = new PulsioximeterData(patientID);

			eHealthNativeMethods.j_initPulsioximeter();

			// How much time (at most) waits the application until a valid measurement.
			waitTimer = new WatchDog(waitTimeMs);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets pulse rate (0 position) and timeOut (1 position)
	 * 
	 */
	public PulsioximeterData getPulse() {
		firstTime();
		int iPulse = eHealthNativeMethods.j_getBPM();
		this.data.setdTimeStamp(new Date());
		if (iPulse == 0) {
			this.data.setbIsValid(false);
		} else {
			this.data.setbIsValid(true);
			waitTimer.restart();
		}
		this.data.setiPulse(iPulse);

		return data;
	}

	/**
	 * Gets oxygen saturation (0 position) and timeOut (1 position)
	 * 
	 */
	public PulsioximeterData getSPO2() {
		firstTime();
		int iSPO2 = eHealthNativeMethods.j_getOxygenSaturation();
		this.data.setdTimeStamp(new Date());
		if (iSPO2 == 0) {
			this.data.setbIsValid(false);
		} else {
			this.data.setbIsValid(true);
			waitTimer.restart();
		}
		this.data.setiSPO2(iSPO2);

		return data;
	}

	/**
	 * Gets pulse rate (0 position), oxygen saturation (1 position) and timeOut (2 position)
	 * 
	 */
	public PulsioximeterData getPulseSPO2() {
		firstTime();
		int iSPO2 = eHealthNativeMethods.j_getOxygenSaturation();
		int iPulse = eHealthNativeMethods.j_getBPM();
		this.data.setdTimeStamp(new Date());
		if (iSPO2 == 0 || iPulse == 0) {
			this.data.setbIsValid(false);
		} else {
			this.data.setbIsValid(true);
			waitTimer.restart();
		}
		this.data.setiPulse(iPulse);
		this.data.setiSPO2(iSPO2);

		return data;
	}

	/**
	 * In the first access, the timer has to be started.
	 */
	private void firstTime() {
		System.out.println("Arrancamos timer por primera vez");
		if (this.isFirstTime) {
			waitTimer.start();
			this.isFirstTime = false;
		}
	}

	private class WatchDog extends Timer {
		private static final long serialVersionUID = -4208011866630959521L;

		public WatchDog(int delay) {
			super(delay, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// Por si ha habido una �ltima medida buena
					if (data.getbIsValid()) {
						data.setIsTimeOut(false);
						waitTimer.restart();
					} else {
						data.setIsTimeOut(true);
						waitTimer.stop();
					}
				}
			});
		}

	}
}
