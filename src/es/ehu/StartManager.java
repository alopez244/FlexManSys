package es.ehu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;



public class StartManager extends Agent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static final Logger LOGGER = LogManager.getLogger(StartManager.class.getName()) ;
	/**
	 * 
	 */
	
	private String appID = ""; 
	private String description = "";
	
	private MessageTemplate template;
	
	private String mwm;
	
	
	protected void takeDown(){
    //TODO implementar apagado
  }
  
  protected void afterMove(){
    //TODO implementar disponibilidad en AM
    // actualizar el nodo en el que me encuentro
  }
  
  //TODO Implementar registro derregistro, getinfo, setinfo
  //atiende comunicación con CAs
	
	
	protected void setup() {
		LOGGER.entry();
		try {
			   Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
			   LOGGER.info("Aï¿½adida tarea de apagado...");
			 } catch (Throwable t) {
			   LOGGER.warn(" *** Error: No se ha podido aï¿½adir tarea de apagado");
		}
		
		String[] args = (String [])getArguments();
		
		// recibir dirección del MM y el aaID al que represento
		for (int i=0; i<args.length; i++) {
			LOGGER.info(args[i]);
			if (args[i].toString().toLowerCase().startsWith("appid=")) 
				this.appID = args[i].substring("appid=".length());
			else if (args[i].toString().toLowerCase().startsWith("description=")) 
				this.description = args[i].substring("description=".length());
		}	
		
		addBehaviour(new AppManagerBehaviour(this));

		LOGGER.exit();
	}
	class AppManagerBehaviour extends SimpleBehaviour 
    {
		
		boolean finished = false;
		boolean registered = false;


		public AppManagerBehaviour(Agent agent) {
            super(agent);
        }

		
		  
		public void onStart(){
			LOGGER.entry();
			
			if (mwm == null) try { resolveMWM(); } catch (Exception e) {e.printStackTrace();}
			template = MessageTemplate.MatchOntology("control");

			/********************************** MASRECON - START TEMPLATE  *********************************/
			
			
			// HealthCare, arrencamos aplicaciones en caso de escenarios necesitamos un get por delante
			String [] components = sendCommand(mwm, "getApplicationComponents "+appID, true).getContent().split(",");
			
			for (String component: components)
				sendCommand(mwm, "start "+component, false);
			
			/********************************** MASRECON - START TEMPLATE / *********************************/
			
			LOGGER.exit();	
		}
		
    public void action() {

      LOGGER.entry();
      ACLMessage msg = receive(template);
    		        		
    	if (msg == null) {
    			LOGGER.trace("block();");
    			block();
  		} else {
    			
  		  LOGGER.debug(msg.getContent()+" from:"+msg.getSender().getLocalName());
  			String result = "";
  			//reply = msg.createReply();
  			String[] cmds = msg.getContent().split(" ");
  			
  			if (cmds[0].equals("get")) {
    			if (cmds[2].equals("id")) {
    				result=appID;
    			}
  			} else if (cmds[0].equals("exit")) {
  				result = "agur";
  				finished=true;
  			}
      			
  			if (result.length()>0) { //devuelvo respuesta
  				LOGGER.trace("sendReply");
  				ACLMessage reply = msg.createReply();
  				reply.setOntology(msg.getOntology());
  				reply.setContent(result.trim());
  				reply.setPerformative(ACLMessage.INFORM);
  				// TODO: si no cumple el mínimo: REFUSE
  				LOGGER.debug("PROPOSE: "+reply.getContent());
  				myAgent.send(reply);
  			} // !msg.getReplyWith().equals(regTemplate)
      			
  		}
    		
		  String response = "";
			
			String appInsID=myAgent.getLocalName();
		
			LOGGER.exit();
		  } //end action
        
        /**
         * lkdjflkdjfkdjfkdjfkdjfkdf
         * @param mwm dkfjd
         * @param cmd dfdf
         * @param sync dfdf
         * @return
         */
    private ACLMessage sendCommand(String mwm, String cmd, boolean sync) {
      LOGGER.entry(mwm, cmd, sync);
    	ACLMessage aMsg = new ACLMessage(ACLMessage.REQUEST);
			aMsg.addReceiver(new AID(mwm, AID.ISLOCALNAME));
			//if (log) System.out.print("sendCommand ("+cmd+"): ");
			aMsg.setContent(cmd);
			aMsg.setOntology("control");
			
			aMsg.setReplyWith(cmd+"_"+System.currentTimeMillis());
			LOGGER.debug("appManager.send("+cmd+")");
			send(aMsg);
			
			if (!sync) return null;
			
			ACLMessage aReply = null;
			MessageTemplate mt = MessageTemplate.MatchInReplyTo(aMsg.getReplyWith());
			
			while (aReply == null){
				try {Thread.sleep(10);} catch (Exception e) {e.printStackTrace();}
				aReply = receive(mt);
			}
			
			LOGGER.info("mm("+cmd+ ") > " + aReply.getContent());
			// if (log) System.out.println(msg.getContent());
			
			return LOGGER.exit(aReply);
   }
        
    private void resolveMWM () throws Exception{
  		LOGGER.entry();

  		DFAgentDescription dfd = new DFAgentDescription();  
  		ServiceDescription sd = new ServiceDescription();
  		sd.setType("sa");
  		dfd.addServices(sd);
    		    
  		while (true) {
  		  //System.out.print(".");
        DFAgentDescription[] result = DFService.search(myAgent,dfd);
        if ((result != null) && (result.length > 0)) {
  				dfd = result[0]; 
  				mwm = dfd.getName().getLocalName();
  				LOGGER.debug("mwm="+mwm);
  				break;
        }
        System.out.print(".");
        Thread.sleep(100);
  	      
	    } //end while (true)
  		LOGGER.exit();
    }
        
    public boolean done() {  
        return finished;  
    }
		
  } // ----------- End myBehaviour
	
	
	
	class ShutdownThread extends Thread {
		  private Agent myAgent = null;
		  
		  public ShutdownThread(Agent myAgent) {
		    super();
		    this.myAgent = myAgent;
		  }
		   
		  public void run() {
		    LOGGER.entry();

		    try { 
		    	
		    	//DFService.deregister(myAgent);
		    	ACLMessage aMsg = new ACLMessage(ACLMessage.INFORM);
  				aMsg.addReceiver(new AID(mwm, AID.ISLOCALNAME));
  				aMsg.setLanguage("JavaSerialization");
  
  				//if (log) System.out.print("sendCommand ("+cmd+"): ");
  
  				aMsg.setContent("del "+appID);
  				LOGGER.debug("appManager().shutdownThread().send()");
  				send(aMsg);
  				ACLMessage msg = blockingReceive();
  		    	
  				LOGGER.info("Nodo derregistrado.");
				
  				myAgent.doDelete();
	    	} catch (Exception e) {}
		    LOGGER.exit();
		  }
		}

}
