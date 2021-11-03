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
    public String control="automatic";


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
                        if(control.equals("automatic")){
                            if(!msg.getContent().contains("ControlGatewayCont")){
                                LOGGER.error(msg.getContent()+" is either dead or isolated.");
                                MessageTemplate t=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                                        MessageTemplate.MatchOntology("askrelationship"));
                                if(msg.getContent().contains("batchagent")){
                                    sendACL(16,"QoSManagerAgent" , "askrelationship", msg.getContent());
                                    ACLMessage machineobatch= blockingReceive(t, 500);
                                    if(machineobatch!=null){
                                        LOGGER.info(machineobatch.getContent());
                                        sendACL(16,machineobatch.getContent() , "control", "setstate idle");
                                    }else{
                                        LOGGER.error("QoSManagerAgent did not answer on time");
                                    }
                                    sendACL(16,"machine1" , "control", "setstate idle");//pdte

                                }else if(msg.getContent().contains("machine")){
                                    sendACL(16,"QoSManagerAgent" , "askrelationship", msg.getContent());
                                    ACLMessage batchofmachine= blockingReceive(t, 500);
                                    if(batchofmachine!=null){
                                        LOGGER.info(batchofmachine.getContent());
                                    }else{
                                        LOGGER.error("QoSManagerAgent did not answer on time");
                                    }
                                }

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
                        }else{
                            LOGGER.error(msg.getContent()+" is either dead or isolated.");
                            LOGGER.warn("Programmed response wont execute because D&D is on manual mode");
                        }

                    } else if (msg.getOntology().equals("msg_lost")&&msg.getSender().getLocalName().equals("QoSManagerAgent")) {
                        LOGGER.warn("Message lost. Bridge message");
                        String[] msgparts=msg.getContent().split("/div/");
                        String performative=msgparts[0];
                        String ontology=msgparts[1];
                        String convID=msgparts[2];
                        String receiver=msgparts[3];
                        String intercepted_msg=msgparts[4];
                        if(control.equals("automatic")){
                            ACLMessage bridgedmsg=new ACLMessage(Integer.parseInt(performative));
                            bridgedmsg.setOntology(ontology);
                            AID receiverID = new AID(receiver, false);
                            bridgedmsg.addReceiver(receiverID);
                            bridgedmsg.setConversationId(convID);
                            bridgedmsg.setContent(intercepted_msg);
                            send(bridgedmsg);
                        }else{
                            LOGGER.warn("Programmed response wont execute because D&D is on manual mode");
                        }

                        //TODO añadir aquí bridge de msg

                    } else if (msg.getOntology().equals("timeout")&&msg.getSender().getLocalName().equals("QoSManagerAgent")) {
                        //idle aqui
                        if(control.equals("automatic")){
                            LOGGER.error("Timeout thrown. Machine agent "+msg.getContent()+" idling");
                            sendACL(16,msg.getContent(),"control","setstate idle");
                        }else{
                            LOGGER.error("Timeout thrown. Machine agent "+msg.getContent()+" should idle");
                            LOGGER.warn("Programmed response wont execute because D&D is on manual mode");
                        }
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
