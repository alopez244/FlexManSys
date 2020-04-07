package es.ehu.flexmansys.test;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static es.ehu.utilities.MasReconOntologies.*;

/**
 * This agent send different type of messages to some BatchAgents, checking that
 * the response for these messages are the correct ones.
 * 
 * @author Brais Fortes (@fortes23)
 * @author Mikel Lopez (@lopeziglesiasmikel)
 */
public class TestBatch extends Agent {

	private static final long serialVersionUID = -8461556203741298268L;
	static final Logger LOGGER = LogManager.getLogger(TestBatch.class.getName());
	private static int period = 3000;
	private ACLMessage requestMsg;
	private ACLMessage operationMsg;
	private ACLMessage negMsg;
	private int countTaskID;
	private AID receivers;
	private static final String[] machOperations = new String[] { "ASM001", "ASM002", "ASM003", "ASM004", "ASM005",
			"DRY001", "FLA001", "INS001", "INS002", "MOU001", "MOU002", "PHO001", "SCR001", "TIG001" };

	private class testingBatch extends CyclicBehaviour {

		private static final long serialVersionUID = 579221321446822226L;

		public testingBatch(Agent a) {
			super(a);
		}

		public void action() {
			LOGGER.entry();
			MsgOperation content = new MsgOperation(MsgOperation.MACHINE, true, getOperation(), generateTaskID(),
					generateEstimatedTime());
			operationMsg.setPostTimeStamp();
			try {
				operationMsg.setContentObject(content);
			} catch (Exception e) {
				LOGGER.info("Msg could not be sent");
			}
			LOGGER.info(operationMsg);
			send(operationMsg);
			block(period);
			LOGGER.exit();
		}
	}

	protected void setup() {
		testingBatch test = new testingBatch(this);
		addBehaviour(test);
		receivers = new AID("batch1", AID.ISLOCALNAME);
		requestMsg = new ACLMessage(ACLMessage.REQUEST);
		requestMsg.setOntology(ONT_DATA);
		requestMsg.addReceiver(receivers);
		operationMsg = new ACLMessage(ACLMessage.INFORM);
		operationMsg.setOntology(ONT_DATA);
		operationMsg.addReceiver(receivers);
		negMsg = new ACLMessage(ACLMessage.INFORM);
		negMsg.setOntology(ONT_NEGOTIATE);
		negMsg.addReceiver(receivers);
		countTaskID = 0;
	}

	/**
	 * Calculates an UID for any operation.
	 * 
	 * @return Universal TaskID
	 */
	private String generateTaskID() {
		LOGGER.entry();
		countTaskID++;
		return LOGGER.exit(this.getLocalName() + countTaskID);
	}

	/**
	 * Calculates a random number between 1 and 10000.
	 * 
	 * @return Universal TaskID
	 */
	private long generateEstimatedTime() {
		LOGGER.entry();
		final double max = 10000;
		final double min = 1;
		double val = Math.random() * max + min;
		long rand = (long) val;
		return LOGGER.exit(rand);
	}

	/**
	 * Get a random operation from {@code machOperations}
	 * 
	 * @return Operation ID
	 */
	private String getOperation() {
		LOGGER.entry();
		double val = Math.random() * machOperations.length;
		int i = (int) val;
		return LOGGER.exit(machOperations[i]);
	}
}
