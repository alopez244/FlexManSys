package es.ehu.platform.behaviour;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.platform.MWAgent;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import es.ehu.platform.template.interfaces.*;

import java.util.ArrayList;

/**
 * This behaviour is a simple implementation of a message receiver.
 * It check if a satate is recive and stores it.
 * If the timeout expires before any message arrives, the behaviour
 * transition into a negotiation.
 * 
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/
public class TrackingBehaviour extends SimpleBehaviour {

  private static final long serialVersionUID = -1354067344628941600L;
  static final Logger LOGGER = LogManager.getLogger(TrackingBehaviour.class.getName()) ;

  private MWAgent myAgent;

  	public ArrayList<ArrayList<ArrayList<ArrayList<String>>>> Traceability=new ArrayList<>();
  	public ArrayList<String> remaining=new ArrayList<String>();
	public ArrayList<String> FinishTimes=new ArrayList<String>();
	public ArrayList<String> Replicas=new ArrayList<String>();
  	public String parent;
	public boolean firstime;
	protected MessageTemplate template,template2;


	protected long timeout;

	private boolean expired;
	private int transitionFlag;
		
	/**
	 * Constructor.
	 * 
	 * @param a
	 *            a reference to the Agent
//	 * @param timeout
	 *            a timeout for waiting until a message arrives. It must
	 *            be expressed as an absolute time, as it would be returned by <code>System.currentTimeMillisec()</code>
	 **/
	public TrackingBehaviour(MWAgent a) {
		super(a);
		LOGGER.entry(a);
		myAgent = a;
		LOGGER.exit();
		
	}

	// For persistence service
	protected TrackingBehaviour() {
	}

	public void onStart(){
		template = MessageTemplate.MatchOntology("state");
		template2=MessageTemplate.MatchOntology("info_to_tracking");
		myAgent.ActualState="tracking";
		timeout = 0;
		expired = false;
		transitionFlag = -1;
		LOGGER.debug("***************** inicio TrackingBehaviour");
		LOGGER.debug("myAgent.period="+myAgent.period);
		
		
	}
	public void action() {
		LOGGER.entry();
			ACLMessage msg = myAgent.receive(template);
			ACLMessage msg2 = myAgent.receive(template2);

		if(msg2!=null){
			sendACL(7,msg2.getSender().getLocalName(),"Acknowledge","received");
			if(msg2.getOntology().equals("static_info")){
				String parts1[]= msg2.getContent().split("/div0/");
				String FinishTimesConc=parts1[0];
				parent=parts1[1];
				String replicasConc=parts1[2];
				String parts3[]=FinishTimesConc.split("/div1/");
				for(int i=0;i<parts3.length;i++){
					FinishTimes.add(parts3[i]);
				}
				String parts4[]=replicasConc.split("/div1/");
				for(int i=0;i<parts4.length;i++){
					if(!parts4[i].equals(myAgent.getLocalName())){
						Replicas.add(parts4[i]);
					}
				}
				LOGGER.debug(myAgent.getLocalName()+ " replica finished constructing static data");

			}else if(msg2.getOntology().equals("info_to_tracking")) {
				String parts1[] = msg2.getContent().split("/div0/"); //el divisor 0 divide los argumentos y el resto se usan para los arraylist
				String productTraceabilityConc = parts1[0]; //trazabilidad concatenada
				String remainingConc = null;
				if (parts1[1] != null) {
					remainingConc = parts1[1]; //solo si quedan acciones/SonAgentIDs
				}
				String firstimeString = parts1[2]; //primera vez

				String FinishTimesConc=parts1[3]; //finish times concatenados (cada agente de aplicación lleva un formato)
				parent=parts1[4]; 					//parent
				String replicasConc=parts1[5];		//replicas del agente

				String parts2[] = productTraceabilityConc.split("/div1/"); //construye la trazabilidad
				for (int i = 0; i < parts2.length; i++) {
					Traceability.add(i, new ArrayList<ArrayList<ArrayList<String>>>());
					String parts3[] = parts2[i].split("/div2/");
					for (int j = 0; j < parts3.length; j++) {
						Traceability.get(i).add(j, new ArrayList<ArrayList<String>>());
						String parts4[] = parts3[j].split("/div3/");
						for (int k = 0; k < parts4.length; k++) {
							Traceability.get(i).get(j).add(k, new ArrayList<String>());
							String parts5[] = parts4[k].split("/div4/");
							for (int l = 0; l < parts5.length; l++) {
								Traceability.get(i).get(j).get(k).add(parts5[l]);
							}
						}
					}
				}
				if (remainingConc != null) {    //construye bien los sonagentID o actionlist
					String parts6[] = remainingConc.split("/div1/");
					for (int i = 0; i < parts6.length; i++) {
						remaining.add(parts6[i]);
					}
				}
				firstime = Boolean.parseBoolean(firstimeString);
				String parts7[]=FinishTimesConc.split("/div1/");
				for(int i=0;i<parts7.length;i++){
					FinishTimes.add(parts7[i]);
				}

				String parts8[]=replicasConc.split("/div1/");
				for(int i=0;i<parts8.length;i++){
					if(!parts8[i].equals(myAgent.getLocalName())){
						Replicas.add(parts8[i]);
					}
				}
				LOGGER.debug(myAgent.getLocalName() + " replica finished constructing state");
			}


		}else if (msg != null) {
			if (myAgent.period>0) timeout = System.currentTimeMillis()+(int)(myAgent.period*1.5); // se producirá timeout si se excede de (horaActual + D)
				// TODO el deadline sólo se debe calcular para componentes periódicos
				LOGGER.trace(" trackingBehaviour.onStart() msg != null");
//				try {
//					String stateClassName = (msg.getContentObject()==null || msg.getContentObject().getClass()==null)?"null"  //anterior

					String stateClassName = (msg.getContent()==null || msg.getContent().getClass()==null)?"null"

//					    :msg.getContentObject().getClass().getSimpleName();  //anterior

							:msg.getContent().getClass().getSimpleName();

//					LOGGER.info("functionalityInstance.setState("+stateClassName+"="+
//					    ((msg.getContentObject()==null)?"null":msg.getContentObject().toString())+
////					        ")"); //anterior

					LOGGER.info("functionalityInstance.setState("+stateClassName+"="+
							((msg.getContent()==null)));
					
//					((AvailabilityFunctionality)myAgent.functionalityInstance).setState(msg.getContentObject());  //anterior

					((AvailabilityFunctionality)myAgent.functionalityInstance).setState(msg.getContent());
					LOGGER.debug(myAgent.cmpID+"("+((MWAgent) myAgent).getLocalName()+") < " + myAgent.cmpID+"("+msg.getSender().getLocalName()+"):"
							+ "state("+stateClassName+")");

//				} catch (UnreadableException e) {
//					LOGGER.warn(e.getLocalizedMessage());
//					e.printStackTrace();
//				}
			} else {
				if (timeout > 0) { //he recibido el primer estado y es periódico

					long blockTime = timeout - System.currentTimeMillis(); // me bloqueo hasta que se produzca posible timeout (horaTimeout-horaActual)
					if (blockTime <= 0) { //timeout expired 
						//expired = true;
						//transitionFlag = ControlBehaviour.PAUSED;
					  timeout = 0;
					  //TODO: el report error deberá ir al healthmonitor 
						myAgent.sendCommand("report " + myAgent.cmpID+ " type=error desc=state refreshing timeout");
						
//  HEALTHMONITOR DEBERÁ:
						//pedir al am lista NA que contienen los comp tracking > am los marca negociando (si alguno mas pregunta le respondo que espere) 
// lanza negociación nodo ganador y avisa al am
// am pasa de tracking a running el ganador 
// el running anterior a failure
						
						/**CompInst->ApplicationManager: timeout
							alt not(negotiation) 
							   ApplicationManager-->CompInst: NA list
							else negotiation
							   ApplicationManager-->CompInst: pause
							end
							
							note over CompInst,NA:negotiation
							CompInst->NA: condicion de negociacion
							NA-->CompInst: valor
							CompInst-->ApplicationManager: winner (NAx)
							
							alt NAx
							  ApplicationManager->CompInst: run
							else not(NAx)
							  ApplicationManager->CompInst: tracking
							end
						 	**/
						
					
						
						
//		nego an tienen compont track
// al que gana cambio de estado

						LOGGER.warn("Deadline excedido: "+(int)(myAgent.period*1.5)+" ms");
					} else {
						block(blockTime);
					}
				} else { // no he recibido ningun estado, timeout a partir del primero
					LOGGER.info("tracking.beh.block()");
					block();
				}
			}

			// TODO prueba para ver el cambio de estados --> luego borrar
			System.out.println("El agente " + myAgent.getLocalName() + " esta en el metodo action del TrackingBehaviour");
			
			LOGGER.exit();
	}

	public boolean done() {
		return expired;
	}

	public void doAcknowledge(AID name){ //responde al emisor del mensaje instanciado en name
		ACLMessage ack= new ACLMessage(7);
		ack.setOntology("Acknowledge");
		ack.setContent("Received");
		ack.addReceiver(name);
		myAgent.send(ack);
		LOGGER.debug("TRACKING "+myAgent.getLocalName()+" AGENT GOT FEEDBACK CORRECTLY");
	}

	public void setState(ACLMessage msg){


	}

	
	public int onEnd() {

		return transitionFlag;
	}

	/**
	 * This method allows modifying the deadline
	 **/
//	public void setDeadline(long period) {
//		this.deadline = period + System.currentTimeMillis();
//		this.timeout = period;
//	}
	public void sendACL(int performative,String receiver,String ontology,String content){ //Funcion estándar de envío de mensajes
		AID receiverAID=new AID(receiver,false);
		ACLMessage msg=new ACLMessage(performative);
		msg.addReceiver(receiverAID);
		msg.setOntology(ontology);
		msg.setContent(content);
		myAgent.send(msg);
	}
}