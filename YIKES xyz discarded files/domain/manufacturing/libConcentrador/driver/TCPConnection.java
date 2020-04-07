/*
 Part of Libnodave, a free communication library for Siemens S7 300/400 via
*/
package es.ehu.domain.manufacturing.libConcentrador.driver;

public class TCPConnection extends S7Connection {
	int rack;
	int slot;
	
	public TCPConnection(PLCinterface ifa, int rack, int slot) {
		super(ifa);
		this.rack=rack;
		this.slot=slot;
		PDUstartIn = 7;
		PDUstartOut = 7;
	}
	
	protected int readISOPacket() {
		int res = iface.read(msgIn, 0, 4);
		if (res == 4) {
		    int len = 0x100 * msgIn[2] + msgIn[3];
		    res += iface.read(msgIn, 4, len);
		} else return 0;
		return res;
	}
	
	protected int sendISOPacket(int size) {
		size += 4;
		msgOut[0] = (byte)0x03;
		msgOut[1] = (byte)0x0;
		msgOut[2] = (byte) (size / 0x100);
		msgOut[3] = (byte) (size % 0x100);
		if ((Nodave.Debug & Nodave.DEBUG_EXCHANGE) != 0) {
			Nodave.dump(" send packet", msgOut, 0, size);
		}
		iface.write(msgOut, 0, size);
		return 0;
	}
	public int exchange(PDU p1) {
		if ((Nodave.Debug & Nodave.DEBUG_EXCHANGE) != 0) {
			System.out.println(" enter TCP.Exchange");
		}
		msgOut[4] = (byte)0x02;
		msgOut[5] = (byte)0xf0;
		msgOut[6] = (byte)0x80;
		sendISOPacket(3 + p1.hlen + p1.plen + p1.dlen);
		readISOPacket();
		return 0;
	}
	/**
	 * We have our own connectPLC(), but no disconnect()
	 *   Open connection to a PLC. This assumes that dc is initialized by
	 *  daveNewConnection and is not yet used.
	 * (or reused for the same PLC ?)
	*/
	public int connectPLC() {
		byte[] b4 ={
			(byte)0x11,
			(byte)0xE0,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x01,
			(byte)0x00,
			(byte)0xC1,
			(byte)0x02,
			(byte)0x01,
			(byte)0x00,
			(byte)0xC2,
			(byte)0x02,
			(byte)0x01,
			(byte)0x02,
			(byte)0xC0,
			(byte)0x01,
			(byte)0x09 };
						
		if ((Nodave.Debug & Nodave.DEBUG_CONNECT) != 0)
			System.out.println("daveConnectPLC() step 1. rack:"+rack+" slot:"+slot);
		System.arraycopy(b4, 0, msgOut, 4, b4.length);
		msgOut[17]=(byte)(rack+1);
		msgOut[18]=(byte)slot;
		sendISOPacket(b4.length);
		readISOPacket();
		if ((Nodave.Debug & Nodave.DEBUG_CONNECT) != 0)
			System.out.println("daveConnectPLC() step 1.");
		return negPDUlengthRequest();
	}
}
