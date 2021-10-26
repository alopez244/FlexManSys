package es.ehu.domain.manufacturing.utilities;
import es.ehu.domain.manufacturing.agents.functionality.Batch_Functionality;
import es.ehu.domain.manufacturing.test.QoSManagerAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class DiagnosisAndDecision extends Agent{
//    private volatile AID QoSID = new AID("QoSManagerAgent", false);

    static final Logger LOGGER = LogManager.getLogger(DiagnosisAndDecision.class.getName()) ;
    private Agent myAgent=this;
    public volatile String control="automatic";
    private final Object lock = new Object();


    protected void setup(){
        LOGGER.entry();
        LOGGER.info("Diagnosis and Decision Agent started");

        addBehaviour(new DDEventManager() );

        LOGGER.exit();
    }

    class DDEventManager extends CyclicBehaviour {

        private String control="automatic";

        public void action() {

                ACLMessage msg=receive();
                if(msg!=null) {
                    if (msg.getOntology().equals("not_found")&&msg.getSender().getLocalName().equals("QoSManagerAgent")) {
                        if(!msg.getContent().contains("ControlGatewayCont")){
                            LOGGER.error(msg.getContent()+" is either dead or isolated.");

                        }else { //agente GW no encontrado
                            String msgparts[] = msg.getContent().split("/div/");
                            String GW = msgparts[0];
                            LOGGER.error(GW + " is either dead or isolated. Manually reset GW.");
                            if (msgparts[1] != null) {
                                LOGGER.info(msgparts[1] + " is the machine assigned to "+GW);
                                sendACL(16,msgparts[1] , "control", "setstate idle");
                            }else{
                                LOGGER.error("QoS should have sent which machine is assigned to GW.");
                            }
                        }
                        sendACL(7,"QoSManagerAgent","report",msg.getContent()); //pdte de modificaciones
                    } else if (msg.getOntology().equals("msg_lost")) {
                        LOGGER.warn("Message lost. Bridge message");
                        String[] msgparts=msg.getContent().split("/div/");
                        String performative=msgparts[0];
                        String ontology=msgparts[1];
                        String convID=msgparts[2];
                        String receiver=msgparts[3];
                        String intercepted_msg=msgparts[4];
                        //pendiente, no desarrollado aun por ser un caso altamente improbable.

                    } else if (msg.getOntology().equals("timeout")) {
                        //idle aqui
                        sendACL(16,msg.getContent(),"control","setstate idle");
                    } else if(msg.getOntology().equals("man/auto")){
                        control=msg.getContent();
                        LOGGER.info("Changed to "+control+" mode");
                        sendACL(ACLMessage.INFORM,msg.getSender().getLocalName(),msg.getOntology(),"ack");
                    }
                }
            }
        }


    private void sendACL(int performative,String receiver,String ontology,String content){ //Funcion estándar de envío de mensajes
        AID receiverAID=new AID(receiver,false); //pasamos la máquina a estado idle
        ACLMessage msg=new ACLMessage(performative);
        msg.addReceiver(receiverAID);
        msg.setOntology(ontology);
        msg.setContent(content);
        send(msg);
    }


}
