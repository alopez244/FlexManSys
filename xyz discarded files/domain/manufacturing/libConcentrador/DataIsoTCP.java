package es.ehu.domain.manufacturing.libConcentrador;

/**
 *
 * @author Alex
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import es.ehu.domain.manufacturing.libConcentrador.driver.Nodave;
import es.ehu.domain.manufacturing.libConcentrador.driver.PLCinterface;
import es.ehu.domain.manufacturing.libConcentrador.driver.TCPConnection;



public class DataIsoTCP
{
	public static boolean Connection = false;
	public static int i, j;
	public static long a, b, c;
	public static float d, e, f;
	public static char buf[];
	public static byte buf1[];
	public static PLCinterface di;
	public static TCPConnection dc;
	public static Socket sock;
	public static int slot;
	public static byte[] by;
	public static String IP;

	// IP 192.168.1.101
	DataIsoTCP(String host)
	{
		IP = host;
		// Nodave.Debug=Nodave.DEBUG_ALL;
		buf = new char[Nodave.OrderCodeSize];
		buf1 = new byte[Nodave.PartnerListSize];
		try
		{
			sock = new Socket(host, 102);
		}
		catch (IOException e)
		{
			System.out.println(e);
		}
	}

	public static void StartConnection()
	{
		Connection = false;
		OutputStream oStream = null;
		InputStream iStream = null;
		slot = 2;

		if ( sock != null )
		{
			try
			{
				oStream = sock.getOutputStream();
			}
			catch (IOException e)
			{
			}
			try
			{
				iStream = sock.getInputStream();
			}
			catch (IOException e)
			{
			}
			di = new PLCinterface(oStream, iStream, "IF1", 0,
					Nodave.PROTOCOL_ISOTCP);

			dc = new TCPConnection(di, 0, slot);
			int res = dc.connectPLC();
			if ( 0 == res )
			{
				Connection = true;
				System.out.println("Connection OK ");
			}
			else
			{
				System.out.println("No connection");
			}
		}
	}

	public static void StopConnection()
	{
		if ( Connection == true )
		{
			Connection = false;
			dc.disconnectPLC();
			di.disconnectAdapter();
		}
	}

	// read 4 bytes from MD 100
	public static long ReadData()
	{
		dc.readBytes(Nodave.FLAGS, 0, 100, 4, null);
		a = dc.getU32();
		return (long) a;
	}

	// write 4 bytes to MD 100
	public static void WriteData(long a)
	{
		by = Nodave.bswap_32(a);
		dc.writeBytes(Nodave.FLAGS, 0, 100, 4, by);
	}
	
	/**
	 * Escribir un Byte en el DB que se elija en la posicion que se elija
	 * @param data: informacion a meter
	 * @param DBnumbre: DB en el que se quiere escribir
	 * @param start: Posicion en que se va a escribir
	 */
	public static void WriteDataDB(int data,int DBnumbre,int start)
	{
		by = Nodave.bswap_8(data);
		dc.writeBytes(Nodave.DB, DBnumbre, start, 1, by);
	}
	
	/**
	 * Escribir un array de byte en el DB que se elija desde la posicion que se elija
	 * @param data: informacion a meter
	 * @param DBnumbre: DB en el que se quiere escribir
	 * @param start: Posicion en que se va a iniciar la escritura
	 */
	public static void WriteDataDBArray(int[] data,int DBnumbre,int start)
	{
		byte[] envio = new byte[data.length];
		//comvertir los integer a bytes
		for(int i=0; i<data.length;i++){
			envio[i]=Nodave.bswap_8(data[i])[0];
		}
		dc.writeBytes(Nodave.DB, DBnumbre, start, envio.length, envio);
	}
	
	
	/**
	 * Leer un Byte en el DB que se elija en la posicion que se elija
	 * @param DBnumbre: DB en el que se quiere leer
	 * @param start: Posicion en que se va a iniciar la lectura
	 * @return
	 */
		public static byte ReadDataDB(int DBnumbre,int start)
		{
			dc.readBytes(Nodave.DB, DBnumbre, start, 1, null);
			return (byte) dc.getBYTE();
		}
	

	@SuppressWarnings("static-access")
	public static void Start(String adres)
	{

//		Nodave.Debug = Nodave.DEBUG_ALL
//				^ (Nodave.DEBUG_IFACE | Nodave.DEBUG_SPECIALCHARS);

		DataIsoTCP tp = new DataIsoTCP(adres);
		tp.StartConnection();
	}
	
	/**
	 * Cambiar en el concentrador el controlaor en el cual escribe los datos del MC 
	 * @param ControladorID numero del controlador (ej. "1")
	 * @param MC_ID numero de la estacion (ej. "1")
	 */
	public static  void cambiarControl(String nodeID, String MC_ID){
		if (Connection == false) {
			Start("192.168.0.101");
		}
		if (Connection == true) {
			
			int numeroEstacion =Integer.valueOf(MC_ID)-1;
			int controladorId=Integer.valueOf(nodeID.substring(5));
			System.out.println("numero del controlador: "+nodeID +" traduccion: "+controladorId);
			System.out.println("numero de la estacion: "+ MC_ID +" - "+numeroEstacion);
			WriteDataDB(controladorId-20, 115, numeroEstacion);
		}
	}

}
