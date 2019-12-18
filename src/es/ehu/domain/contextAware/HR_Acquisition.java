package es.ehu.domain.contextAware;

/**
 * @author Aintzane Armentia, Rafael Priego
 */

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.FSMBehaviourMW;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;

import java.io.Serializable;

import es.ehu.platform.MWAgent;
import es.ehu.StateParallel;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.domain.eHealth.functionality.PulsioximeterData;
import es.ehu.domain.eHealth.functionality.PulsioximeterManagement;
import es.ehu.domain.eHealth.message.MsgHR_Acquisition2HR_CheckInterval;
import es.ehu.domain.eHealth.message.MsgHR_Acquisition2HR_RepStorage;

public class HR_Acquisition extends MWAgent {
	private static final long serialVersionUID = 8505462503088901786L;

	public boolean log = false;
	private PulsioximeterManagement pulsMang;

	public HR_Acquisition() {
	}

	protected void setupContend() {
		/** Definir el agente **/
		final String[] sourceComponentIDs = {};
		final String[] targetComponentIDs = { "compon105", "compon106" };

		/** Comportamiento Agente FSM **/
		FSMBehaviourMW comportamientoFSM = new FSMBehaviourMW(this);

		/** Comportamiento initialized **/
		Behaviour boot = new OneShotBehaviour(this) {
			private static final long serialVersionUID = 8225746656000342984L;

			public void action() {
				try {
					// --------------- inicializacion ----------------
					// Tiempo m�ximo de espera = 1 minuto, identificador
					// del paciente = 1 (QoS de aplicaci�n????)
					pulsMang = new PulsioximeterManagement(60000, 1);

					MWInit(targetComponentIDs, sourceComponentIDs, myAgent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} // end action

			public int onEnd() {
				return initTransition;
			}

		}; // end boot

		/** Comportamiento periodico execution **/
		Behaviour running = new TickerBehaviour(this, 15000) {
			private static final long serialVersionUID = 5211311085804151394L;
			int transitionFlag = 1;

			public void onTick() {
				try {
					// --------------- recibir informacion ----------------

					// -------------- ejecutar funcionalidad --------------
					PulsioximeterData data = pulsMang.getPulse();
					System.out.println(getLocalName() + "-----" + "     "
							+ data.getdTimeStamp().toString() + "     -----");
					System.out.println(getLocalName() + " - PULSO: " + data.getiPulse());
					System.out.println(getLocalName() + " - SPO2: " + data.getiSPO2());
					System.out.println(getLocalName() + " - isTimeOut: " + data.getIsTimeOut());

					// ------------ env�a resultado a destino -------------
					if (data.getIsTimeOut()) {
						// Reconfiguracion
						System.out.println(getLocalName() + " - TO DO: Se debe lanzar un evento "
								+ "de reconfiguraci�n para parar la "
								+ "aplicaci�n, porque ha habido un fallo.");
					} else {
						if (data.getbIsValid()) {
							// Enviar mensaje a los siguientes
							// Destino 1: pulso, timeStamp y patientID
							Serializable msg_1 = new MsgHR_Acquisition2HR_RepStorage(
									data.getiPulse(), data.getdTimeStamp(),
									data.getiPatientID());
							// sendMesage(msg_1, new
							// String[]{"HR_RepStorage"});
							sendMessage(msg_1, new String[] { "compon105" });

							// Destino 2: patientID, timeStamp y pulso
							Serializable msg_2 = new MsgHR_Acquisition2HR_CheckInterval(
									data.getiPatientID(), data.getdTimeStamp(),
									data.getiPulse());
							// sendMesage(msg_2, new
							// String[]{"HR_CheckInterval"});
							sendMessage(msg_2, new String[] { "compon106" });
						} else {
							System.out.println(getLocalName() + " - NADA");
						}
					}

					// --------------- enviar el estado ----------------

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			public int onEnd() {
				return transitionFlag;
			}

		};

		Behaviour end = new OneShotBehaviour(this) {
			private static final long serialVersionUID = 4550696608000730211L;

			public void action() {
				try {
					((MWAgent) myAgent).deregisterAgent(myAgent.getLocalName());
					myAgent.doDelete();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} // end action

			public int onEnd() {
				return 1;
			}

		}; // end boot

		Behaviour tracking = new CyclicBehaviour() {
			private static final long serialVersionUID = -7305601005900363590L;

			@Override
			public void action() {
				// TODO Auto-generated method stub

			}
		};

		// FSM state definition
		comportamientoFSM.registerFirstState(new StateParallel(this, boot), "boot");
		comportamientoFSM.registerState(new StateParallel(this, running), "running");
		comportamientoFSM.registerState(new StateParallel(this, tracking), "tracking");
		comportamientoFSM.registerLastState(end, "end");
		// FSM transition
		comportamientoFSM.registerTransition("boot", "end", ControlBehaviour.STOP);
		comportamientoFSM.registerTransition("boot", "running", ControlBehaviour.RUNNING);
		comportamientoFSM.registerTransition("boot", "tracking", ControlBehaviour.TRACKING);
		comportamientoFSM.registerTransition("running", "end", ControlBehaviour.STOP);
		comportamientoFSM.registerTransition("tracking", "end", ControlBehaviour.STOP);

		this.addBehaviour(comportamientoFSM);
	}
}
