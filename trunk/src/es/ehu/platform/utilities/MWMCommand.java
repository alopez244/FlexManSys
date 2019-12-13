package es.ehu.platform.utilities;

import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
 * This behaviour is a simple implementation of a message receiver.
 * It check if a satate is recive and stores it.
 * If the timeout expires before any message arrives, the behaviour
 * transition into a negotiation.
 * 
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/
public class MWMCommand extends SimpleBehaviour {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  static final Logger LOGGER = LogManager.getLogger(MWMCommand.class.getName()) ;
	
	private MessageTemplate template;
	private Hashtable<String, String> attribs;
	private String[] targets;
	private boolean done = false;
	Agent myAgent = null;
	
	
	/**
	 * Constructor.
	 * 
	 * @param a
	 *            a reference to the Agent
	 **/
	public MWMCommand(Agent a, Hashtable<String, String> attribs, String... targets) {
		super(a);
		LOGGER.entry(a, attribs, targets);
		this.attribs = attribs;
		this.targets = targets;
		myAgent = a;
		
		//this.replies = new ACLMessage[targets.length];
		LOGGER.exit();
	}

	// For persistence service
	protected MWMCommand() {
	}

	public void action() {
	  LOGGER.entry();
		
		if (template == null) { //todavía no se ha enviado
		  LOGGER.info("template == null");
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
   		// ejemplo de ejecución command=setstate param=debug
			String cmd = attribs.get("command") + " " + attribs.get("param");
			msg.setContent(cmd);
			msg.setOntology("control");
			
			msg.setReplyWith(cmd+"_"+System.currentTimeMillis());
			
			String sTargets = "";
			for (String target: targets) { 
		    	msg.addReceiver(new AID(target, AID.ISLOCALNAME));
		    	sTargets += target + ",";
			}
			
			myAgent.send(msg);
			LOGGER.info("mwm(cb) > control:"+cmd+ " > "+ sTargets);
			
			
			template = MessageTemplate.and(MessageTemplate.and(
			    MessageTemplate.MatchOntology("control"), 
			    MessageTemplate.MatchPerformative(ACLMessage.INFORM)),
			    MessageTemplate.MatchInReplyTo(msg.getReplyWith()
			));
			
			ACLMessage reply = myAgent.blockingReceive(template);
			
			//template = ;
			
		} else {
			
		  LOGGER.debug("myAgent.receive("+template+")");
			ACLMessage reply = myAgent.receive(template);

			if (reply == null) {
			  LOGGER.trace("block()");
				block();
			} else {
			  LOGGER.trace("reply !== null");
				//replies[repliesCnt]=reply;
				getDataStore().put(reply.getSender().getLocalName(), reply);
				if (getDataStore().size()>=targets.length) {
				  LOGGER.debug("done = true");
					done=true;
					myAgent.doWake();
				}
			} // end msg != null
		} // end template !=null
		LOGGER.exit();
	}

	public final boolean done() {
		LOGGER.entry();
		return LOGGER.exit(done);
	}

	public void onStart() {
	  LOGGER.entry();
		LOGGER.exit();
	}
	
	public int onEnd() {
	  LOGGER.entry();
		return LOGGER.exit(0);
	}
}