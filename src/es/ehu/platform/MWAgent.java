package es.ehu.platform;

/**
 * Agente base en el cual contien las funciones necesarias para interactuar con el middleware 
 */

import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static es.ehu.platform.utilities.MasReconOntologies.*;
import static es.ehu.platform.utilities.MWMCommands.*;
import es.ehu.platform.template.interfaces.BasicFunctionality;

public class MWAgent extends Agent {
	private static final long serialVersionUID = 8505462503088901786L;
	
	static final Logger LOGGER = LogManager.getLogger(MWAgent.class.getName()) ;

	
	/**
	 *  Tabla para almacenar los AIDs de los recptores
	 */
	
	/**
	 * instancia AID
	 */

	/**
	 *  Instancia en running del componente (actualizarla cuando al MWM llega un setState)
	 */
	//TODO refresh local cache
	//public Hashtable<String, String> runningInstance = new Hashtable<String, String>();
	public static ConcurrentHashMap<String, String> runningMwm = new ConcurrentHashMap<String, String>();
	
	/**
	 *  Instancias en tracking del componente (arraylist para actualiza el estado, actualizarla cuando al MWM llega un setState)
	 */
//TODO refresh local cache
	//public ArrayList<String> trackingInstances = new ArrayList<String>();
	
	public BasicFunctionality functionalityInstance;
	public String[] targetComponentIDs, sourceComponentIDs;
	public int period = -1;
//	public int deadline = -1;

	public String cmpID = null;
	//public String nodeID = null; Rafa: el nodo ya lo tienes en: myAgent.getContainerController().getContainerName();
	
	public int initTransition;
	public String conversationId;
	public Object initialExecutionState = null;
	
	// Par�metros de configuraci�n
	public boolean mwmStoresExecutionState = true;
	
	/**
	 * Primera transicion a realizar
	 */
	

	public MWAgent() {	
	}


	protected void setup() {
		LOGGER.entry(getArguments());
		Object[] args = (Object[]) getArguments();
		
		// getCmp(cmpins), node, String.valueOf(initialFSMState), period, executionState, conversationId }));
		if (args.length>0) {//getCmp(cmpins)
		  LOGGER.debug("this.cmpID = "+args[0]+";");
		  this.cmpID = args[0].toString();		  
		}
		if (args.length>1) { //node
		  LOGGER.debug("doMove(new ContainerID("+args[1]+", null));"); //TODO comprobar que el nodo args[1] existe
		  doMove(new ContainerID(args[1].toString(), null));
		  //this.nodeID = args[1]; Rafa: el nodo ya lo tienes en: myAgent.getContainerController().getContainerName();
		}
		if (args.length>2 && (args[2]!=null)) { //initialFSMState
		  LOGGER.debug("initTransition=Integer.parseInt("+args[2]+");");
		  initTransition=(Integer)args[2];
		}
    if ((args.length>3) && (args[3]!=null)) { //period
      LOGGER.debug("period=Integer.parseInt("+args[3]+");");
      period=(Integer)args[3];
    }
    if (args.length>4) {
      LOGGER.debug("executionState = "+((args[4]==null)?"null":args[4].getClass().getName())+";");
      initialExecutionState = args[4]; 
    }
		if (args.length>5) {
		  LOGGER.debug("conversationId = "+args[5]+";");
		  conversationId = args[5].toString(); 
		}		
		
		if (args.length>6) {
		  this.sourceComponentIDs = (String[]) args[6] ;
		  LOGGER.info("sourceComponentIDs = "+((sourceComponentIDs.length>0)?sourceComponentIDs[0]:"")+";");
		}
		if (args.length>7) {
		  this.targetComponentIDs = (String[]) args[7] ;
		  LOGGER.info("targetComponentIDs = "+((targetComponentIDs.length>0)?targetComponentIDs[0]:"")+";");
		}

		// Anadir una tarea de apagado
		try {
			Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
			LOGGER.info("Anadida tarea apagado");
		} catch (Throwable t) { 
			LOGGER.warn(" *** Error: No se ha podido anadir tarea de apagado"); 
			LOGGER.warn(t.getLocalizedMessage());}

		setupContent();
		LOGGER.exit();
	}


	/**
	 * Funcion para definir los comportamientos a ejecutar dentro del contenedos
	 */
	protected void setupContent() {
	  LOGGER.entry();
	  LOGGER.exit();	
	}
	
	
	protected void afterMove() {
	  try { resolveMWM(); } catch (Exception e) {e.printStackTrace();}
	  String container="";
	  try {  container=this.getContainerController().getContainerName(); } catch (Exception e) {e.printStackTrace();}
	  String cmd = CMD_SET + " " + this.getLocalName()+" node="+container;
	  String response = sendCommand(cmd).getContent();
	  this.getContainerController().getName();
    LOGGER.debug(cmd + " > " + response);
    LOGGER.exit();
	}
	
	protected void takeDown() {
	  LOGGER.entry();
	  try { resolveMWM(); } catch (Exception e) {e.printStackTrace();}
    //String container="";
    //try {  container=this.getContainerController().getContainerName(); } catch (Exception e) {e.printStackTrace();}
	  
    String cmd = CMD_DELETE + " "+this.getLocalName();
    String response = sendCommand(cmd).getContent();
    this.getContainerController().getName();
    LOGGER.debug(cmd + " > " + response);
		LOGGER.exit();
	}

	/**
	 * Esperara a que todos los componetes que le siguen esten registrados en el
	 * MM
	 * 
	 * @param componentIDs
	 *            : array con nombre de componentes
	 * @param type
	 *            : tipo de componentes "receivers" o "sources"
	 * @param state
	 *            : estado en el que deen estar los componentes
	 * @param myAgent
	 * @throws Exception
	 */

	private void waitForComponents(String[] componentIDs, String type, String state, Agent myAgent) throws Exception {
	  LOGGER.entry(componentIDs, type, state, myAgent);

	  try { resolveMWM(); } catch (Exception e) {e.printStackTrace();}
		
		// espero hasta que en MM est�n dados de alta
		for (String componentID : componentIDs) {
			// targetComponentIDs
			LOGGER.debug("Esperando a " + componentID);
			
			String result = getInstances(componentID, state);
//			String cmd = CMD_GETINSTANCES + " " + componentID;
//      if (state!=null) cmd+=" state="+state;
      
			while (result.equals("")) {
				result = getInstances(componentID, state);
				Thread.sleep(100);
			}
			
			//TODO: refresh local cache
			//if (state.equals("running"))  runningInstance.put(componentID, result);
			
		} // end for targetIDs
		LOGGER.exit();
	}

	/**
	 * Register an agent in the repository of Middleware Manager
	 * 
	 * @param attribs Registry attributes
	 * @return MWM response (ID of the agent if there were no errors)
	 * @throws Exception
	 */
	public String registerAgent(String attribs) throws Exception {
		LOGGER.entry(attribs);

		String cmd = CMD_REGISTER + " " + attribs;
		LOGGER.debug("*** Registering agent ***");

		String response = sendCommand(cmd).getContent();
		cmpID = response;
		
		LOGGER.info(cmd + " > " + response);  

		return LOGGER.exit(cmpID);
	}

	/**
	 * Eliminar un agente del registro del Middleware Manager
	 * 
	 * @param localName
	 *            : nombre del agente
	 * @throws Exception
	 */
	public void deregisterAgent(String localName) throws Exception {
	  LOGGER.entry(localName);
		
		// guardo AID en el mm
		// String cmd = CMD_SET + " " + myAgent.getLocalName() + " state=running AID="
		// + myAgent.getAID().getName();
		String cmd = CMD_DELETE + "  " + this.getLocalName();
		String response = sendCommand(cmd).getContent();
		LOGGER.info(cmd + " > " + response);
		
		LOGGER.exit();

	}

	/**
	 * Devuelve el componente a partir la instancia/implementaci�n. 
	 * 
	 * @param id id de la instancia/implementaci�n
	 * @return
	 * @throws FIPAException
	 */
	public String getComponent(String id) {
	    return (sendCommand(CMD_GETCOMPONENTS + " " + id)).getContent();
  }
	
	/**
	 * Devuelve las instacias de un componente en un estado.
	 * @param cmpID id del componente del que requerimos las instancias
	 * @param state puede ser running/tracking/...
	 * @return
	 * @throws FIPAException
	 */
	public String getInstances(String cmpID, String state)  {
		String msg = CMD_GETINSTANCES + " " +cmpID+" state="+state;
		ACLMessage reply = sendCommand(msg);
		String response = (reply==null)?"":reply.getContent();
		LOGGER.info(msg+">"+response);
		return response;
	}

	/**
	 * Sends a {@code get} command to the MWM under the criteria established
	 * by the {@code params}.
	 * 
	 * @param cmpID ID of the element or {@code *} in case of searching in all.
	 * @param filter Search filter.
	 * @return List of agents separated by commas; null if the request could not be completed;
	 * or empty string if there is none.
	 */
	public String getInfoMWM(String cmpID, String filter)  {
		if (cmpID == null) {
			return null;
		}
		String msg = CMD_GET + " " + cmpID + " " + filter;
		ACLMessage reply = sendCommand(msg);
		String response = (reply != null) ? reply.getContent() : null;
		return response;
	}

	/**
	 * Enviar mensaje de control al Middleware Manager
	 * 
	 * @param cmd
	 *            : mensaje a en viar al MM
	 * @return: respuseta del manager
	 * @throws Exception
	 */
	public ACLMessage sendCommand(String cmd)  {
		LOGGER.entry(cmd);		
		System.out.println("cmd="+cmd);
		
		try { resolveMWM(); } catch (Exception e) {e.printStackTrace();}
		
//		if (!runningMwm.contains("sa")){
//		//esto no se deber�a hacer siempre
//		  try {resolveMWM(); } catch (Exception e) {}
//		}
		
		ACLMessage aMsg = new ACLMessage(ACLMessage.REQUEST);
		
		aMsg.setContent(cmd);
		aMsg.setOntology(ONT_CONTROL);
		aMsg.addReceiver(new AID("sa", AID.ISLOCALNAME));
		
		aMsg.setReplyWith(cmd+"_"+System.currentTimeMillis());
		// el conversationId lo utiliza el MWM para despertar la tarea que interveniene en esta conversaci�n (el id conincide con el id de la tarea)
		aMsg.setConversationId(conversationId);
		
		send(aMsg);
		
		
		MessageTemplate mt = MessageTemplate.MatchInReplyTo(aMsg.getReplyWith());

		ACLMessage reply = blockingReceive(mt, 100);
		//ACLMessage reply = blockingReceive(mt);
		
		//ACLMessage reply = receive(mt);
		
		int i=0;
		while ((reply == null) && (i<100)){

			try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
			reply = receive(mt);
			LOGGER.debug("reply = receive(mt);");
			i++;
			
		}
		if (reply == null) 
			LOGGER.warn("mwm tarda en responder");
		else
			LOGGER.info("mm("+aMsg.getContent()+ ") > " + reply.getContent());		
		
		
		return LOGGER.exit(reply);
	}

	/**
	 * Realiza los procesos necesarios para inicailaizar el middleware como
	 * obtenere las direciones reales de los componentes y registrar el agente en
	 * el MM
	 * 
	 * @param targetComponentIDs
	 *            : lista con los componentes a los cuales se les envia el
	 *            mensaje
	 * @param sourceComponentIDs
	 *            : lista con los componentes de los cuales se reciben mensajes
	 * @param myAgent
	 * @throws Exception
	 */

	public void MWInit(String[] targetComponentIDs, String[] sourceComponentIDs, Agent myAgent) throws Exception {
	  LOGGER.entry(targetComponentIDs, sourceComponentIDs, myAgent);
		// esperar a que todos los targets est�n en el MM
		
		LOGGER.info("waitForComponents(receivers)");
		if (targetComponentIDs != null) waitForComponents(targetComponentIDs, "receivers", "running|paused", myAgent);

		// instancias. TODO: refrescaresto cuando hay cambios en el MM
		LOGGER.info("waitForComponents(sources)");
		if (sourceComponentIDs != null) waitForComponents(sourceComponentIDs, "sources", "boot|running|paused", myAgent); //TODO: pensar esta parte como encaja el estado
		
		LOGGER.exit();
	}

	
	/**
	 * Proceso de recibir mensaje. Este bloquea el comportamiento hasta recibir un mensaje de datos
	 * 
	 * @param behaviour
	 *            : comportamiento a padre
	 * @return el mensaje recibido
	 * @throws Exception
	 */
	public ACLMessage receiveMessage() throws Exception {
		LOGGER.entry();
		ACLMessage aMsg = null;
		
		MessageTemplate mt = MessageTemplate.MatchOntology("data");

		while (aMsg == null){
			//beh.block(); /********* ESTE BLOCK NO EST� BIEN UTILIZADO !!! ***/
			try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
			aMsg = receive(mt);
			LOGGER.info("receiveMessage()");
		}
		
		return LOGGER.exit(aMsg);

	}
	
	/**
	 * Metodo para enviar el mensaje a los distintos receptores.
	 * 
	 * @param msg
	 *            : mensaje a enviar
	 * @param targets
	 *            : lista de los componentes a los que se les envia el mensaje.
	 */
	

	
	/**
	 * Procsoso de recibir mensaje. Este bloquea el comportamiento hasta recibir un mensaje de datos
	 * 
	 * @param behaviour
	 *            : comportamiento a padre
	 * @return el mensaje recibido
	 * @throws FIPAException 
	 * @throws Exception
	 */
	
	public String sendMessage(Serializable msg, String[] cmpIDs) {
		if (cmpIDs == null) {
			return LOGGER.exit("null cmpID");
		}
		LOGGER.entry(msg, cmpIDs);

		String response = "";
		
		//if (targets.length==0) return response;
		//try {

			ACLMessage aMsg = new ACLMessage(ACLMessage.INFORM);
			aMsg.setOntology(ONT_DATA);
			try { aMsg.setContentObject(msg); } catch (IOException e) { e.printStackTrace(); }
			aMsg.setReplyWith("data_"+System.currentTimeMillis());

			StringBuilder sTargets = new StringBuilder(); 
			boolean separador=false;
			
			for (String cmpID: cmpIDs) {
				//log.info("Sending data to "+cmpID);
				
				//String cmpins = runningInstance.get(cmpID);
			  String cmpins = this.getInstances(cmpID, "running"); 

				LOGGER.debug("this.getInstances("+cmpID+", \"running\"))" + cmpins);
				if (separador) sTargets.append(",");
				else separador=true;
				
				sTargets.append(cmpID+"("+cmpins+")");
				if (cmpins.isEmpty()) cmpins="null";  // si no hay instancia env�o "null" > desencadena negociaci�n.

				aMsg.addReceiver(new AID (cmpins, AID.ISLOCALNAME));
			}
			
//			Properties prop = aMsg.getAllUserDefinedParameters();
//			prop.setProperty("SF_TIMEOUT", "0");
//			for (Object key: prop.keySet()) System.out.println(key+"="+prop.get(key));
//			aMsg.setAllUserDefinedParameters(prop);
//			System.out.println("*********");
			
			send(aMsg);	
			
			
			
			//ACLMessage msg = blockingReceive(MessageTemplate.);
			LOGGER.info(cmpID+"("+getLocalName() + "):data("+
					((msg.getClass()==null)?"null":msg.getClass().getSimpleName())+  
					") > "+sTargets );
			
			/*MessageTemplate mt = MessageTemplate.MatchInReplyTo(aMsg.getReplyWith());
			
			int repliesCnt = 0;
			while (repliesCnt<cmpIDs.length) {
				ACLMessage aReply = receive(mt);
				if (aReply != null){
					if (aReply.getPerformative() == ACLMessage.FAILURE) {                        	
	                	String name=aReply.getContent().substring(aReply.getContent().indexOf(":name ", aReply.getContent().indexOf("MTS-error"))+":name ".length());
	                	name=name.substring(0, name.indexOf('@'));                        
	                	System.out.println(name+" FAILURE");
					}
					repliesCnt++;
				}
			  System.out.println("sendMessage.rcvReply.block()");

			  try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
			  ACLMessage test = receive();
			  if (test == null) System.out.println("*********** desbloqueado con mensaje nulo�?");
			  else System.out.println("*************** "+test.getContent());
			  
			} */
			
			//sendMessage(msg, ontology, targets);

//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return LOGGER.exit(response);
	}
	
	
	public String sendState(Serializable msg, final String sTargets) {
	  LOGGER.entry(msg, sTargets);
	  String [] cmpinss = sTargets.split(",");
	  if (cmpinss == null) return null;
	  if (msg==null) msg = "null";

	  String response = "";
	  //if (targets.length==0) return response;
	  try {

	    ACLMessage aMsg = new ACLMessage(ACLMessage.INFORM);

	    aMsg.setOntology(ONT_STATE);
	    aMsg.setContentObject(msg);
	    for (String cmpins: cmpinss) 
	      aMsg.addReceiver(new AID(cmpins, AID.ISLOCALNAME));
	    
	    send(aMsg);	
	    
	    LOGGER.debug("sendState().send("+sTargets+"):"+aMsg);

	    LOGGER.info(cmpID+"("+getLocalName() + "):state("+ ((msg.getClass()==null)?"null":msg.getClass().getSimpleName())+ ") > "+cmpID+"("+sTargets+")" );

	  } catch (Exception e) {
	    e.printStackTrace();
	  }
	  return LOGGER.exit(response);
	}
	
	
	
	public void triggerEvent(final String eventId){
	    LOGGER.entry(eventId);
	    sendCommand("start "+eventId);
	    //return LOGGER.exit(sendCommand("start "+eventId).getContent());
	    LOGGER.exit();
	  }
	
	/**
	 * Search trackings of a component and the MWM to send them the state.
	 * If there are not any tracking, the state is only sent to the MWM.
	 * @param msg State information
	 */
	public void sendState(final Serializable msg){
	LOGGER.entry(msg);
	String sTracking = this.getInstances(this.cmpID, "tracking");
	LOGGER.info("Tracking: " + sTracking);
	if ((sTracking != null) && (sTracking.length() > 0) && (!sTracking.equals("type not found"))) {
		sTracking += "," + runningMwm.get("tmwm");
	} else {
		sTracking = runningMwm.get("tmwm");
	}

	if (sTracking.length() > 0) {
		LOGGER.info("Refresh state to tracking instances:"+ sTracking);
		this.sendState(msg, sTracking);
	} else {
		LOGGER.info("No tracking instances:");
	}
	LOGGER.exit();
	}
	
	

	/**
	 * Informa al middleware manager que la instancia de componente ha cambiado
	 * de estado
	 * 
	 * @param InstanciaComponente
	 *            : identificador de la instancia de componente
	 * @param estado
	 *            : Nombre del estado al que se ha cambiado.
	 */

	public void setState(String cmpIns, String state) throws Exception {
	  LOGGER.entry(cmpIns, state);
	  
	  try { resolveMWM(); } catch (Exception e) {e.printStackTrace();}
		String cmd = CMD_SET + " " + cmpIns + " state=" + state;
		String response = sendCommand(cmd).getContent();
		LOGGER.info(response +" < " + cmd);
		// si el estado es running, adem�s actualizo el estado en 
		// de la instancia a la implementaci�n y de implementaci�n a componente
//		if (state.equals("running")) {
//			System.out.println("**********paso estado a running");
//			cmd = CMD_GET + " " + cmpIns + " attrib=parent";
//			String cmpImp = sendCommand(cmd).getContent();
//			cmd = CMD_GET + " " + cmpImp + " attrib=parent";
//			String cmpID = sendCommand(cmd).getContent();
//			System.out.println("**********actualizado a running "+cmpID+" con "+cmpIns);
//			runningInstance.put(cmpID, cmpIns);
//		}
		
		LOGGER.exit();
	}

	/**
	 * Devuelve una lista de las instancias de un componente que s encuentran en
	 * un determinado estado.
	 * 
	 * @param componente
	 *            : identificador del componente
	 * @param estado
	 *            : estado de lasinstancias que se desean buscar
	 * @return
	 */
	public AID[] getComponets(String componente, String estado) throws Exception {
	  LOGGER.entry(componente, estado);

		String cmd = "getInstance " + componente + " state=" + estado;
		LOGGER.debug("cmd=" + cmd);
		String result = sendCommand(cmd).getContent();

		String[] aux = result.split(",");
		AID[] exit = new AID[aux.length];

		for (int i = 0; i < aux.length; i++) {
			exit[i] = new AID(aux[i], true);
		}

		return LOGGER.exit(exit);
	}


	/**
	 *Obtener la direccion del Middleware Manager
	 *
	 * @throws Exception
	 */

	public void resolveMWM() throws FIPAException {
	  LOGGER.entry();
		
	  
	  
	  if (runningMwm.containsKey("tmwm"))	    return;
	  
	  
	  System.out.println("es.ehu.platform.resolveMWM()");
	  
		DFAgentDescription dfd = new DFAgentDescription();  
		ServiceDescription sd = new ServiceDescription();
		sd.setType("sa");
		dfd.addServices(sd);
		    
		while (true) {
		  //System.out.print(".");
	      DFAgentDescription[] result = DFService.search(this,dfd);
	      if ((result != null) && (result.length > 0)) {
    			dfd = result[0]; 
    			runningMwm.put("tmwm", dfd.getName().getLocalName());
    			System.out.println("sa > "+dfd.getName().getLocalName());
    			//AIDsCache.put("mm", dfd.getName());
			break;
	      }
	      System.out.print(",");
	      try { Thread.sleep(100); } catch (InterruptedException e) {}
	      
	    } //end while (true)
		
		LOGGER.exit();
	} // end resolveMM
	
	public <T> String join(T[] array, String cement) {
	    StringBuilder builder = new StringBuilder();
	    
	    if(array == null || array.length == 0) 
	        return null;
	    
	    for (T t : array) 
	        builder.append(t).append(cement);

	    builder.delete(builder.length() - cement.length(), builder.length());

	    return builder.toString();
	}

	class ShutdownThread extends Thread {
		private MWAgent myAgent = null;
		

		public ShutdownThread(MWAgent myAgent) {
			super();
			this.myAgent = myAgent;
			
		}

		public void run() {
		  LOGGER.entry();
			LOGGER.debug("Tarea de apagado");
			try {

				ACLMessage aMsg = new ACLMessage(ACLMessage.INFORM);
				String cmd = CMD_DELETE + " "+myAgent.getLocalName();
				aMsg.setContent(cmd);
				aMsg.setOntology(ONT_CONTROL);
				aMsg.addReceiver(new AID("tmwm", AID.ISLOCALNAME));
				
				LOGGER.info("shutdownThread().send()");
				send(aMsg);		 
				 
				LOGGER.info(myAgent.getLocalName()+"("+cmd+") > mwm");
				
				myAgent.doDelete();
			} catch (Exception e) {
			}
			// System.out.println("Agente borrado");
			LOGGER.exit();
		}
	}
}
