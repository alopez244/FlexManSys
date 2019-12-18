package es.ehu.domain.eHealth;

/**
 * @author Aintzane Armentia, Rafael Priego
 */

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.FSMBehaviourMW;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.utilities.StateParallel;
import es.ehu.domain.eHealth.functionality.RepositoryStorage;
import es.ehu.domain.eHealth.message.MsgHR_Acquisition2HR_RepStorage;

public class HR_RepStorage extends MWAgent {
	private static final long serialVersionUID = 8505462503088901786L;

	public boolean log = false;
	private RepositoryStorage repStorage;

	public HR_RepStorage() {
	}

	protected void setupContend() {
		/** Definir el agente **/
		final String[] sourceComponentIDs = { "compon104" };
		final String[] targetComponentIDs = {};

		/** Comportamiento Agente FSM **/
		FSMBehaviourMW comportamientoFSM = new FSMBehaviourMW(this);

		/** Comportamiento initialized **/
		Behaviour boot = new OneShotBehaviour(this) {
			private static final long serialVersionUID = 8225746656000342984L;

			public void action() {
				try {
					// --------------- inicializacion ----------------
					repStorage = new RepositoryStorage("./src/es/ehu/eHealth/data/");

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
					MsgHR_Acquisition2HR_RepStorage receivedData_1 = (MsgHR_Acquisition2HR_RepStorage) receivedMsg_1.getContentObject();
					System.out.println(getLocalName() + " - Dato1 (Pulso): " + receivedData_1.getdato_1());
					System.out.println(getLocalName() + " - Dato2 (TimeStamp): " + receivedData_1.getdato_2().toString());
					System.out.println(getLocalName() + " - Dato3 (PatientID): " + receivedData_1.getdato_3());
					// -------------- ejecutar funcionalidad --------------
					repStorage.pulseStorage(receivedData_1.getdato_1(), receivedData_1.getdato_2(), receivedData_1.getdato_3());

					// ------------ env�a resultado a destino -------------

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
