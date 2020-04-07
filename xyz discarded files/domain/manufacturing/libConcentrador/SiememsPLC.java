package es.ehu.domain.manufacturing.libConcentrador;

/**
 * Clase de gestionar la comunicacion con el concentrador
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/

public class SiememsPLC
{
	public void controller(int controlador, int estacion){
		try
		{
			System.out.println("prov ar");
			if ( DataIsoTCP.Connection == false )
			{
				DataIsoTCP.Start("192.168.0.101");
				if ( DataIsoTCP.Connection == true )
				{
					System.out.println("Connected S7");
				}

			}
			if ( DataIsoTCP.Connection == true )
			{
//				System.out.println("Estacion " + estacion
//						+ " controlado por Controlador " + controlador);
				DataIsoTCP.WriteDataDB(controlador, 115, estacion - 1);
			}
			if ( DataIsoTCP.Connection == true )
			{
				DataIsoTCP.StopConnection();
				//System.out.println("Desconectado");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
