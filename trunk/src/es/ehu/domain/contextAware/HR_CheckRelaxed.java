package es.ehu.domain.contextAware;

/**
 * @author Aintzane Armentia, Rafael Priego
 */

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.FSMBehaviourMW;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.StaleProxyException;

import java.io.Serializable;

import es.ehu.platform.MWAgent;
import es.ehu.StateParallel;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.domain.eHealth.functionality.PulseChecking;
import es.ehu.domain.eHealth.functionality.PulseRelaxedData;
import es.ehu.domain.eHealth.message.MsgHR_CheckInterval2HR_CheckRelaxed;
import es.ehu.domain.eHealth.message.MsgHR_CheckRelaxed2HR_NoRelaxedWarning;

public class HR_CheckRelaxed extends MWAgent {
	private static final long serialVersionUID = 8505462503088901786L;

	public boolean log = false;
	/**
	 * Funcion general
	 */
	private PulseChecking check;
	String reconfiguredAppID_1; // Un ID por cada aplicación para la cual se puede lanzar una reconfiguración
	String currentAppID; // ID de la aplicación actual. En caso de que haga falta un replace o un destroy de la actual, sería necesario conocerlo.

	public HR_CheckRelaxed() {
	}

	protected void setupContend() {
		/** Definir el agente **/
		final String[] sourceComponentIDs = { "compon106" };
		final String[] targetComponentIDs = { "compon109" };

		/** Comportamiento Agente FSM **/
		FSMBehaviourMW comportamientoFSM = new FSMBehaviourMW(this);

		/** Comportamiento initialized **/
		Behaviour boot = new OneShotBehaviour(this) {
			private static final long serialVersionUID = 8225746656000342984L;

			public void action() {
				try {
					// --------------- inicializacion ----------------
					check = new PulseChecking(180000,"./src/es/ehu/eHealth/data/");

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
		Behaviour running = new CyclicBehaviour(this) {
			private static final long serialVersionUID = 5211311085804151394L;
			int transitionFlag = 1;

			public void action() {
				try {
					// --------------- recibir informacion ----------------
					ACLMessage receivedMsg_1 = receiveMessage();
					System.out.println(getLocalName() + " - Se han recibido datos");
					MsgHR_CheckInterval2HR_CheckRelaxed receivedData_1 =
							(MsgHR_CheckInterval2HR_CheckRelaxed) receivedMsg_1.getContentObject();
					// -------------- ejecutar funcionalidad --------------
					PulseRelaxedData data = check.checkPulseRelaxed(receivedData_1.getparam_1(),
							receivedData_1.getparam_2(), receivedData_1.getparam_3());

					// ------------ env�a resultado a destino -------------
					if (data.getbIsRelaxed()) {
						// Relaxed patient. Replace the current application by a new one.
						try {
							/**
							 * Unai, esta parte es la que hay que cambiar por un mensaje al CF. Se le indicaría la aplicación actual, y
							 * la que se quiere arrancar.
							 * En este caso, reconfiguredAppID_1 sería "Blood Pressure Monitoring" application identifier.
							 * De momento lo pongo como Strings y una llaamda a un método que no hace nada.
							 */
							Object[] arguments = { currentAppID, reconfiguredAppID_1 }; // "Blood Pressure Monitoring" application identifier
							//replaceApplication(arguments);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						Serializable msg_1 = new MsgHR_CheckRelaxed2HR_NoRelaxedWarning(data.getiPatientID(),
								data.getdTimeStamp(), data.getiValueToCheck());
						sendMessage(msg_1, new String[] { "compon109" });
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
