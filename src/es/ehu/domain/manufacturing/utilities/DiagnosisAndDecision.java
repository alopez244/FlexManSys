package es.ehu.domain.manufacturing.utilities;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class DiagnosisAndDecision extends Agent{
//    private volatile AID QoSID = new AID("QoSManagerAgent", false);

    static final Logger LOGGER = LogManager.getLogger(DiagnosisAndDecision.class.getName());
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
                                if(msg.getContent().contains("batchagent")||msg.getContent().contains("orderagent")||msg.getContent().contains("mplanagent")){
                                    try {
                                        ACLMessage reply= sendCommand(myAgent, "get "+msg.getContent()+" attrib=state", "ApplicationAgentState");
                                        if(reply.getContent().equals("tracking")){
                                            LOGGER.warn(msg.getContent()+" is in tracking state. Informing running replica.");
                                            ACLMessage reply2= sendCommand(myAgent, "get "+msg.getContent()+" attrib=parent", "ApplicationAgentParent");
                                            ACLMessage reply3= sendCommand(myAgent, "get * parent="+reply2.getContent()+" state=running" , "ApplicationAgentRunning");
                                            sendACL(7, reply3.getContent(), "delete_replica", msg.getContent());
                                        }else{
                                            if(msg.getContent().contains("batchagent")){
                                                sendACL(16,"QoSManagerAgent" , "askrelationship", msg.getContent());
                                                ACLMessage machineofbatch= blockingReceive(t, 2000);
                                                if(machineofbatch!=null){
                                                    LOGGER.info(machineofbatch.getContent()+" is assigned to "+ msg.getContent());
                                                    LOGGER.info(machineofbatch.getContent()+" is changing to idle state");
                                                    sendACL(16,machineofbatch.getContent() , "control", "setstate idle");
                                                }else{
                                                    LOGGER.error("QoSManagerAgent did not answer on time");
                                                }
                                            }
                                            //TODO casos de order y mplan aislados
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
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
                                String msgparts[] = msg.getContent().split("/div/"); //el agente GW siempre se reporta con su machine responsable
                                String GW = msgparts[0];
                                LOGGER.error(GW + " is either dead or isolated. Manually reset GW.");
                                if (msgparts[1] != null) {
//                                    LOGGER.info(msgparts[1] + " is the machine assigned to "+GW);
                                    sendACL(16,msgparts[1] , "control", "setstate idle");
                                    LOGGER.info(msgparts[1] + " is now idling");
                                }else{
                                    LOGGER.error("QoS should have sent which machine is assigned to GW.");
                                }
                            }
                        }else{
                            LOGGER.error(msg.getContent()+" is either dead or isolated.");
                            LOGGER.warn("MANUAL MODE: User must take a decision to solve the issue");
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
                            LOGGER.warn("MANUAL MODE: User must take a decision to solve the issue");
                        }

                        //TODO añadir aquí bridge de msg

                    } else if (msg.getOntology().equals("timeout")&&msg.getSender().getLocalName().equals("QoSManagerAgent")) {
                        //idle aqui
                        if(control.equals("automatic")){
                            LOGGER.error("Timeout thrown. Machine agent "+msg.getContent()+" idling");
                            sendACL(16,msg.getContent(),"control","setstate idle");
                        }else{
                            LOGGER.error("Timeout thrown. Machine agent "+msg.getContent()+" should idle");
                            LOGGER.warn("MANUAL MODE: User must take a decision to solve the issue");
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
    public ACLMessage sendCommand(Agent agent, String cmd, String conversationId) throws Exception {

        this.myAgent = agent;

        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType("sa");
        dfd.addServices(sd);
        String mwm;

        while (true) {
            DFAgentDescription[] result = DFService.search(myAgent, dfd);

            if ((result != null) && (result.length > 0)) {
                dfd = result[0];
                mwm = dfd.getName().getLocalName();
                break;
            }
            LOGGER.info(".");
            Thread.sleep(100);

        } //end while (true)

        LOGGER.entry(mwm, cmd);
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID(mwm, AID.ISLOCALNAME));
        msg.setConversationId(conversationId);
        msg.setOntology("control");
        msg.setContent(cmd);
        msg.setReplyWith(cmd);
        myAgent.send(msg);
        ACLMessage reply = myAgent.blockingReceive(
                MessageTemplate.and(
                        MessageTemplate.MatchInReplyTo(msg.getReplyWith()),
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM))
                , 1000);

        return LOGGER.exit(reply);
    }


}
