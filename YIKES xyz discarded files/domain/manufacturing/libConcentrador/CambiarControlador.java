package es.ehu.domain.manufacturing.libConcentrador;

/**
 * Comportamiento que extiende el comportamiento “CyclicMsgReceiver” para recibir 
 * el mensaje de cambio de controlador. este no tiene time out
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/

import es.ehu.domain.manufacturing.lib.CyclicMsgReceiver;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class CambiarControlador extends CyclicMsgReceiver {

	private static final long serialVersionUID = 150713643885892079L;

	public CambiarControlador(Agent a, MessageTemplate mt, DataStore s,
			Object msgKey) {
		super(a, mt, CyclicMsgReceiver.INFINITE, s, msgKey);
	}

	protected void handleMessage(ACLMessage msg) {
		//  cominicarle al concentrado que PLC va a controlar la estacion
		if (msg != null) {
			try {
				if (DataIsoTCP.Connection == false) {
					DataIsoTCP.Start("192.168.0.101");
				}
				if (DataIsoTCP.Connection == true) {
					System.out.println("Cambiar al "
							+ msg.getSender().getName() + " controlador: "+msg.getContent());
					DataIsoTCP.WriteDataDB(Integer.valueOf(msg.getContent()), 115, 0);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
