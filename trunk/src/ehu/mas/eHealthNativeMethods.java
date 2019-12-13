/**
 * Native methods for the functions that manage the health sensor kit on the Raspberry Pi.
 * It includes all the public methods of the eHealth library.
 */
package ehu.mas;

public class eHealthNativeMethods {
	/*
	 * Initializes the position sensor and configure some values.
	 * 
	 * param void return void
	 */
	private native void initPositionSensor();

	/**
	 * Initializes the position sensor and configure some values.
	 */
	public static void j_initPositionSensor() {
		new eHealthNativeMethods().initPositionSensor();
	}

	/*
	 * Initializes the BloodPressureSensor sensor and configure some values
	 * 
	 * param void return void
	 */
	private native void readBloodPressureSensor();

	/**
	 * Initializes the BloodPressureSensor sensor and configure some values
	 */
	public static void j_readBloodPressureSensor() {
		new eHealthNativeMethods().readBloodPressureSensor();
	}

	/*
	 * Initializes the pulsioximeter sensor and configure some values. También
	 * se realizará la inicialización de la interrupción en el pin 6.
	 * 
	 * param void return void
	 */
	private native void initPulsioximeter();

	/**
	 * Initializes the pulsioximeter sensor and configure some values.
	 */
	public static void j_initPulsioximeter() {
		new eHealthNativeMethods().initPulsioximeter();
	}

	/*
	 * Returns the corporal temperature.
	 * 
	 * param void return float : The corporal temperature value.
	 */
	private native float getTemperature();

	/**
	 * Returns the corporal temperature.
	 * 
	 * @return float : The corporal temperature value.
	 */
	public static float j_getTemperature() {
		return new eHealthNativeMethods().getTemperature();
	}

	/*
	 * Returns the oxygen saturation in blood in percent.
	 * 
	 * param void return int : The oxygen saturation value. Normal values betwen
	 * 95-99%
	 */
	private native int getOxygenSaturation();

	/**
	 * Returns the oxygen saturation in blood in percent.
	 * 
	 * @return int : The oxygen saturation value. Normal values betwen 95-99%
	 */
	public static int j_getOxygenSaturation() {
		return new eHealthNativeMethods().getOxygenSaturation();
	}

	/*
	 * Returns the heart beats per minute.
	 * 
	 * param void return int : The beats per minute.
	 */
	private native int getBPM();

	/**
	 * Returns the heart beats per minute.
	 * 
	 * @return int : The beats per minute.
	 */
	public static int j_getBPM() {
		return new eHealthNativeMethods().getBPM();
	}

	/*
	 * Returns the value of skin conductance.
	 * 
	 * param void return float : The skin conductance value.
	 */
	private native float getSkinConductance();

	/**
	 * Returns the value of skin conductance.
	 * 
	 * @return float : The skin conductance value.
	 */
	public static float j_getSkinConductance() {
		return new eHealthNativeMethods().getSkinConductance();
	}

	/*
	 * Returns the value of skin resistance.
	 * 
	 * param void return float : The skin resistance value.
	 */
	private native float getSkinResistance();

	/**
	 * Returns the value of skin resistance.
	 * 
	 * @return float : The skin resistance value.
	 */
	public static float j_getSkinResistance() {
		return new eHealthNativeMethods().getSkinResistance();
	}

	/*
	 * Returns the value of skin conductance in voltage.
	 * 
	 * param void return float : The skin conductance value in voltage (0-5v).
	 */
	private native float getSkinConductanceVoltage();

	/**
	 * Returns the value of skin conductance in voltage.
	 * 
	 * @return float : The skin conductance value in voltage (0-5v).
	 */
	public static float j_getSkinConductanceVoltage() {
		return new eHealthNativeMethods().getSkinConductanceVoltage();
	}

	/*
	 * Returns an analogic value to represent the Electrocardiography.
	 * 
	 * param void return float : The analogic value (0-5V).
	 */
	private native float getECG();

	/**
	 * Returns an analogic value to represent the Electrocardiography.
	 * 
	 * @return float : The analogic value (0-5V).
	 */
	public static float j_getECG() {
		return new eHealthNativeMethods().getECG();
	}

	/*
	 * Returns an analogic value to represent the Electromyography.
	 * 
	 * param void return float : The analogic value (0-5V).
	 */
	private native int getEMG();

	/**
	 * Returns an analogic value to represent the Electromyography.
	 * 
	 * @return float : The analogic value (0-5V).
	 */
	public static int j_getEMG() {
		return new eHealthNativeMethods().getEMG();
	}

	/*
	 * Returns the body position.
	 * 
	 * param void return uint8_t : the position of the pacient. 1 == Supine
	 * position. 2 == Left lateral decubitus. 3 == Rigth lateral decubitus. 4 ==
	 * Prone position. 5 == Stand or sit position
	 */
	private native byte getBodyPosition();

	/**
	 * Returns the body position.
	 * 
	 * @return byte: the position of the pacient. 1 == Supine position. 2 ==
	 *         Left lateral decubitus. 3 == Rigth lateral decubitus. 4 == Prone
	 *         position. 5 == Stand or sit position
	 */
	public static byte j_getBodyPosition() {
		return new eHealthNativeMethods().getBodyPosition();
	}

	/*
	 * Returns the value of the systolic pressure.
	 * 
	 * param void return int : The systolic pressure.
	 */
	private native int getSystolicPressure(int i);

	/**
	 * Returns the value of the systolic pressure.
	 * 
	 * @param i
	 * @return int : The systolic pressure.
	 */
	public static int j_getSystolicPressure(int i) {
		return new eHealthNativeMethods().getSystolicPressure(i);
	}

	/*
	 * Returns the value of the diastolic pressure.
	 * 
	 * param void return int : The diastolic pressure.
	 */
	private native int getDiastolicPressure(int i);

	/**
	 * Returns the value of the diastolic pressure.
	 * 
	 * @param i
	 * @return int : The diastolic pressure.
	 */
	public static int j_getDiastolicPressure(int i) {
		return new eHealthNativeMethods().getDiastolicPressure(i);
	}

	/*
	 * Returns an analogic value to represent the air flow.
	 * 
	 * param void return int : The value (0-1023) read from the analogic in.
	 */
	private native int getAirFlow();

	/**
	 * Returns an analogic value to represent the air flow.
	 * 
	 * @return int : The value (0-1023) read from the analogic in.
	 */
	public static int j_getAirFlow() {
		return new eHealthNativeMethods().getAirFlow();
	}

	/*
	 * Prints the current body position
	 * 
	 * param uint8_t position : the current body position. return void
	 */
	private native void printPosition(byte position);

	/**
	 * Prints the current body position
	 * 
	 * @param position
	 *            : the current body position.
	 */
	public static void j_printPosition(byte position) {
		new eHealthNativeMethods().printPosition(position);
	}

	/*
	 * It reads a value from pulsioximeter sensor.
	 * 
	 * param void return void
	 */
	private native void readPulsioximeter();

	/**
	 * It reads a value from pulsioximeter sensor.
	 */
	public static void j_readPulsioximeter() {
		new eHealthNativeMethods().readPulsioximeter();
	}

	/*
	 * Prints air flow wave form in the serial monitor
	 * 
	 * param int air : analogic value to print. return void
	 */
	private native void airFlowWave(int air);

	/**
	 * Prints air flow wave form in the serial monitor
	 * 
	 * @param air
	 *            : analogic value to print.
	 */
	public static void j_airFlowWave(int air) {
		new eHealthNativeMethods().airFlowWave(air);
	}

	/*
	 * Read the values stored in the glucometer.
	 * 
	 * param void return void
	 */
	private native void readGlucometer();

	/**
	 * Read the values stored in the glucometer.
	 */
	public static void j_readGlucometer() {
		new eHealthNativeMethods().readGlucometer();
	}

	/*
	 * Returns the number of data stored in the glucometer.
	 * 
	 * param void return int : length of data
	 */
	private native int getGlucometerLength();

	/**
	 * Returns the number of data stored in the glucometer.
	 * 
	 * @return int : length of data
	 */
	public static int j_getGlucometerLength() {
		return new eHealthNativeMethods().getGlucometerLength();
	}

	/*
	 * Returns the number of data stored in the blood pressure sensor.
	 * 
	 * param void return int : length of data
	 */
	private native int getBloodPressureLength();

	/**
	 * Returns the number of data stored in the blood pressure sensor.
	 * 
	 * @return int : length of data
	 */
	public static int j_getBloodPressureLength() {
		return new eHealthNativeMethods().getBloodPressureLength();
	}

	/*
	 * Returns the library version
	 * 
	 * param void return int : The library version.
	 */
	private native int version();

	/**
	 * Returns the library version
	 * 
	 * @return int : The library version.
	 */
	public static int j_version() {
		return new eHealthNativeMethods().version();
	}

	/*
	 * Convert month variable from numeric to character.
	 * 
	 * param int month in numerical format. return String with the month
	 * characters (January, February...).
	 */
	private native String numberToMonth(int month);

	/**
	 * Convert month variable from numeric to character.
	 * 
	 * @param month
	 *            : in numerical format
	 * @return String with the month characters (January, February...).
	 */
	public static String j_numberToMonth(int month) {
		return new eHealthNativeMethods().numberToMonth(month);
	}

	// Static library
	static {
		System.loadLibrary("eHealthNativeMethods");
	}
}
