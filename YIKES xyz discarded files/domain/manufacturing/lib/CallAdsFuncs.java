package es.ehu.domain.manufacturing.lib;

/**
 * Clase la gestionar la conexion con el PLC
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.beckhoff.jni.Convert;
import de.beckhoff.jni.JNIByteBuffer;
import de.beckhoff.jni.JNILong;
import de.beckhoff.jni.tcads.*;

public class CallAdsFuncs {
  static final Logger LOGGER = LogManager.getLogger(CallAdsFuncs.class.getName());
  
	public CallAdsFuncs() {
	}

	long port = 0;
	AmsAddr addr = new AmsAddr();
	long hUser = 0;
	JNILong pNotification = new JNILong();

	public long openPort() {
		if (port == 0) {
			AdsVersion lAdsVersion = AdsCallDllFunction.adsGetDllVersion();
			LOGGER.debug("AdsVersion: "
					+ new Integer(lAdsVersion.getVersion()) + "."
					+ new Integer(lAdsVersion.getRevision()) + "."
					+ lAdsVersion.getBuild());
			port = AdsCallDllFunction.adsPortOpen();
			LOGGER.debug("Port: " + port);

			long nErr = AdsCallDllFunction.getLocalAddress(addr);// local netid
			if (nErr != 0) {
			  LOGGER.warn("getLocalAddress() failed with " + nErr);
				AdsCallDllFunction.adsPortClose();
				return 0;
			}
			addr.mPort = AdsCallDllFunction.AMSPORT_R0_PLC_RTS1; // PLC port 801
		}
		return port;
	}

	public long closePort() {
		if (port != 0) {
			long nErr = AdsCallDllFunction.adsPortClose();
			// LOGGER.debug("Ads port closed");

			port = 0;

			if (nErr != 0)
			  LOGGER.warn("adsPortClose() failed with " + nErr);
		}
		return port;
	}

	public long readState(AdsState adsStateBuff, AdsState devStateBuff) {
		long nErr = 0;

		// read state
		if (port != 0)
			nErr = AdsCallDllFunction.adsSyncReadStateReq(addr, adsStateBuff,
					devStateBuff);
		else
			nErr = 1864; // error 1864 (0x748) ads-port not opened

		return nErr;
	}

	public long readDeviceInfo(AdsDevName devName, AdsVersion adsVersion) {
		long nErr = 0;

		// read state
		if (port != 0)
			nErr = AdsCallDllFunction.adsSyncReadDeviceInfoReq(addr, devName,
					adsVersion);
		else
			nErr = 1864; // error 1864 (0x748) ads-port not opened

		return nErr;
	}

	public long writeControl(int adsState, int devState, JNIByteBuffer databuff) {
		long nErr = 0;

		// read state
		if (port != 0)
			nErr = AdsCallDllFunction.adsSyncWriteControlReq(addr, adsState,
					devState, databuff.getUsedBytesCount(), databuff);
		else
			nErr = 1864; // error 1864 (0x748) ads-port not opened

		return nErr;
	}

	public long setAdsTimeout(long adsTimeout) {
		long nErr = 0;

		// read state
		if (port != 0)
			nErr = AdsCallDllFunction.adsSyncSetTimeout(adsTimeout);
		else
			nErr = 1864; // error 1864 (0x748) ads-port not opened

		return nErr;
	}

	public long readByIGrpOffs(JNIByteBuffer databuff, long lj_idxGrp,
			long lj_idxOffs) {
		long nErr = 0;

		// read by IndexGroup and IndexOffset
		if (port != 0)
			nErr = AdsCallDllFunction.adsSyncReadReq(addr, lj_idxGrp,
					lj_idxOffs, databuff.getUsedBytesCount(), databuff);
		else
			nErr = 1864; // error 1864 (0x748) ads-port not opened

		return nErr;
	}

	public long writeByIGrpOffs(JNIByteBuffer databuff, long lj_idxGrp,
			long lj_idxOffs) {
		long nErr = 0;

		// write by IndexGroup and IndexOffset
		if (port != 0)
			nErr = AdsCallDllFunction.adsSyncWriteReq(addr, lj_idxGrp,
					lj_idxOffs, databuff.getUsedBytesCount(), databuff);
		else
			nErr = 1864; // error 1864 (0x748) ads-port not opened

		return nErr;
	}

	public long getHandleBySymbol(JNIByteBuffer hdlbuff, JNIByteBuffer symbuff) {
		long nErr = 0;

		// get handle by symbol name (symbol like "MAIN.iCounter" name in
		// symbuff)
		if (port != 0)
			nErr = AdsCallDllFunction.adsSyncReadWriteReq(addr, 0xF003, 0x0,
					hdlbuff.getUsedBytesCount(), hdlbuff, // buffer for getting
															// handle
					symbuff.getUsedBytesCount(), symbuff); // buffer containg
															// symbolpath
		else
			nErr = 1864; // error 1864 (0x748) ads-port not opened

		return nErr;
	}

	public long getHandleBySymbol(String Name) throws Exception {
		// Modificados rafa
		byte[] btSymName = Name.getBytes();
		JNIByteBuffer symbuff = new JNIByteBuffer(btSymName.length);
		symbuff.setByteArray(btSymName);
		JNIByteBuffer hdlbuff = new JNIByteBuffer(4);
		byte[] byteArr;
		long nErr = 0;
		long writeHandleReset = 0;

		// get handle by symbol name (symbol like "MAIN.iCounter" name in
		// symbuff)
		if (port != 0) {
			nErr = AdsCallDllFunction.adsSyncReadWriteReq(addr, 0xF003, 0x0,
					hdlbuff.getUsedBytesCount(), hdlbuff, // buffer for getting
															// handle
					symbuff.getUsedBytesCount(), symbuff); // buffer containg
															// symbolpath
		} else {
			nErr = 1864; // error 1864 (0x748) ads-port not opened
		}
		if (nErr == 0) { // get handle
			byteArr = new byte[4];
			byteArr = hdlbuff.getByteArray();
			writeHandleReset = Convert.ByteArrToInt(byteArr); // (2)
																// save
																// handle
		} else {
			throw new Exception("Get handler for " + Name
					+ " by Symbol error : " + nErr);
		}
		return writeHandleReset;
	}

	public long readByHandle(JNIByteBuffer databuff, long symHandle) {
		long nErr = 0;

		if (port != 0) { // read variable by handle
			if (symHandle != 0)
				nErr = AdsCallDllFunction.adsSyncReadReq(addr, 0xF005,
						symHandle, databuff.getUsedBytesCount(), databuff);
			else
				nErr = 1809; // error 1809 (0x711) invalid symbol handle
		} else
			nErr = 1864; // error 1864 (0x748) ads-port not opened

		return nErr;
	}

	public byte[] readByHandle(long symHandle, int buffsize) throws Exception {
		// Modificados rafa
		JNIByteBuffer databuff = new JNIByteBuffer(buffsize);
		byte[] byteArr = null;

		long nErr = 0;

		if (port != 0) { // read variable by handle
			if (symHandle != 0) {
				nErr = AdsCallDllFunction.adsSyncReadReq(addr, 0xF005,
						symHandle, databuff.getUsedBytesCount(), databuff);
			} else {
				nErr = 1809; // error 1809 (0x711) invalid symbol handle
			}
		} else {
			nErr = 1864; // error 1864 (0x748) ads-port not opened
		}
		if (nErr == 0) { // convert data from byte[] to int
			byteArr = new byte[buffsize];
			byteArr = databuff.getByteArray();
		} else {
			throw new Exception("Read by Symbol error : " + nErr);
		}
		return byteArr;
	}

	public long writeByHandle(JNIByteBuffer databuff, long symHandle) {
		long nErr = 0;

		if (port != 0) { // write variable by handle
			if (symHandle != 0)
				nErr = AdsCallDllFunction.adsSyncWriteReq(addr, 0xF005,
						symHandle, databuff.getUsedBytesCount(), databuff);
			else
				nErr = 1809; // error 1809 (0x711) invalid symbol handle
		} else
			nErr = 1864; // error 1864 (0x748) ads-port not opened

		return nErr;
	}

	public void writeByHandle(long symHandle, byte[] data) throws Exception {
		// Modificados rafa
		JNIByteBuffer databuff = new JNIByteBuffer(data.length);
		databuff.setByteArray(data);

		long nErr = 0;

		if (port != 0) { // write variable by handle
			if (symHandle != 0) {
				nErr = AdsCallDllFunction.adsSyncWriteReq(addr, 0xF005,
						symHandle, databuff.getUsedBytesCount(), databuff);
			} else {
				nErr = 1809; // error 1809 (0x711) invalid symbol handle
			}
		} else {
			nErr = 1864; // error 1864 (0x748) ads-port not opened
		}
		if (nErr == 0) {

		} else {
			throw new Exception("Write by Symbol error : " + nErr);
		}
	}

	public long readBySymbol(JNIByteBuffer databuff, JNIByteBuffer symbuff) {
		JNIByteBuffer hdlbuff = new JNIByteBuffer(4);
		long nErr = 0;
		long symHandle = 0;

		if (port != 0) {
			// get handle by symbol name (symbol like "MAIN.iCounter" name in
			// symbuff)
			nErr = AdsCallDllFunction.adsSyncReadWriteReq(addr, 0xF003, 0x0,
					hdlbuff.getUsedBytesCount(), hdlbuff, // buffer for getting
															// handle
					symbuff.getUsedBytesCount(), symbuff); // buffer containg
															// symbolpath

			if (nErr != 0)
				return nErr;

			// get handle
			byte[] byteArr = new byte[4];
			byteArr = hdlbuff.getByteArray();
			symHandle = Convert.ByteArrToInt(byteArr);

			// read variable by handle
			nErr = AdsCallDllFunction.adsSyncReadReq(addr, 0xF005, symHandle,
					databuff.getUsedBytesCount(), databuff);
		} else
			nErr = 1864; // error 1864 (0x748) ads-port not opened

		return nErr;
	}

	public long writeBySymbol(JNIByteBuffer databuff, JNIByteBuffer symbuff) {
		JNIByteBuffer hdlbuff = new JNIByteBuffer(4);
		long nErr = 0;
		long symHandle = 0;

		if (port != 0) {
			// get handle by symbol name (symbol like "MAIN.iCounter" name in
			// symbuff)
			nErr = AdsCallDllFunction.adsSyncReadWriteReq(addr, 0xF003, 0x0,
					hdlbuff.getUsedBytesCount(), hdlbuff, // buffer for getting
															// handle
					symbuff.getUsedBytesCount(), symbuff); // buffer containg
															// symbolpath

			if (nErr != 0)
				return nErr;

			// get handle
			byte[] byteArr = new byte[4];
			byteArr = hdlbuff.getByteArray();
			symHandle = Convert.ByteArrToInt(byteArr);

			// write variable by handle
			nErr = AdsCallDllFunction.adsSyncWriteReq(addr, 0xF005, symHandle,
					databuff.getUsedBytesCount(), databuff);
		} else
			nErr = 1864; // error 1864 (0x748) ads-port not opened

		return nErr;
	}
}
