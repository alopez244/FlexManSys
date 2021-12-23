package es.ehu.platform.behaviour;

import jade.core.AID;
import jade.core.Agent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.platform.MWAgent;

import jade.core.behaviours.*;
import jade.lang.acl.*;

import java.util.Date;

import static es.ehu.platform.utilities.MasReconOntologies.*;

/**
 * This behaviour receives messages from the templates used in the constructor
 * and execute the functionality to perform its activity.
 * <p>
 * There are two possible cases:
 * <ul>
 * <li>If the MWAgent has any {@code sourceComponentIDs}, then it will send to
 * the execute functionality the contentObject. Finally, the returned value is
 * sent as content to the {@code targetComponentsIDs}</li>
 * <li>If the MWAgent does not have any {@code sourceComponentIDs}, then it will
 * send to the execute functionality the ACLMessage. Finally, the returned value
 * (ACLMessage) is sent.</li>
 * </ul>
 * <p>
 * <b>NOTE:</b> The transition to another state is done using a message to a
 * {@code ControlBehaviour}
 *
 * @author Brais Fortes (@fortes23) - Euskal Herriko Unibersitatea
 * @author Mikel Lopez (@lopeziglesiasmikel) - Euskal Herriko Unibersitatea
 **/
public class ResourceRunningBehaviour extends SimpleBehaviour {

    private static final long serialVersionUID = 3456578696375317772L;

    static final Logger LOGGER = LogManager.getLogger(ResourceRunningBehaviour.class.getName());

    private MessageTemplate template;
    private MWAgent myAgent;
    private int PrevPeriod;
    private long NextActivation;
    private AID QoSID = new AID("QoSManagerAgent", false);
    private MessageTemplate QoStemplate=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchOntology("acl_error"));
    // Constructor. Create a default template for the entry messages
    public ResourceRunningBehaviour(MWAgent a) {
        super(a);
        LOGGER.debug("*** Constructing RunningBehaviour ***");
        this.myAgent = a;
        this.template = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_RUN),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
    }

    public void onStart() {
        LOGGER.entry();
        this.PrevPeriod = myAgent.period;
        if (myAgent.period < 0) {
            this.NextActivation = -1;
        } else {
            this.NextActivation = myAgent.period + System.currentTimeMillis();
        }
        LOGGER.exit();
    }

    public void action() {
        LOGGER.entry();

        for(int i=0;i<myAgent.expected_msgs.size();i++){
            Object[] exp_msg;
            exp_msg=myAgent.expected_msgs.get(i);
            String sender=(String) exp_msg[0];
            AID exp_msg_sender=new AID(sender,false);
            String convID=(String) exp_msg[1];
            String content=(String) exp_msg[2];
            long timeout=(long) exp_msg[3];
            Date date = new Date();
            long instant = date.getTime();
            MessageTemplate ack_template=MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchConversationId(convID),
                    MessageTemplate.MatchSender(exp_msg_sender)),
                    MessageTemplate.MatchContent(content));
            ACLMessage ack= myAgent.receive(ack_template);
            if(ack==null){
                if(instant>timeout){
                    myAgent.expected_msgs.remove(i);
                    if(exp_msg_sender.equals(QoSID.getLocalName())){
                        LOGGER.info("QoS did not answer on time. THIS AGENT MIGHT BE ISOLATED.");
                        if(myAgent.getLocalName().contains("machine")){
                            myAgent.state="idle";	//es un agente máquina por lo que transiciona a idle
                            myAgent.change_state=true;
                        }else if(myAgent.getLocalName().contains("batchagent")||myAgent.getLocalName().contains("orderagent")||myAgent.getLocalName().contains("mplanagent")){
                            System.exit(0); //es un agente de aplicacion, por lo que se "suicida" si esta aislado
                        }else{ //TODO añadir aquí agentes no contemplados cuando proceda
                            LOGGER.debug("Condición no programada ");
                        }
                    }else{
                        LOGGER.info("Expected answer did not arrive on time.");
                        String report=sender+"/div/"+content;
                        sendACLMessage(6, QoSID, "acl_error", convID, report, myAgent);
                        AddToExpectedMsgs(QoSID.getLocalName(),convID,report);
                    }
                }
            }else{
                myAgent.expected_msgs.remove(i);
            }

        }


        ACLMessage msg = myAgent.receive(template);

        if (msg!=null) {
            //lo que haga en el running

            // Esto deberia ir dentro del if anterior, pero de momento no se le mandan mensajes de ese tipo (ontology: ONT_RUN)
            // TODO CUIDADO --> Se ha copiado de RunningBehaviour
            Object[] receivedMsgs = manageReceivedMsg(msg);
            Object result = myAgent.functionalityInstance.execute(receivedMsgs);
        }


//        long t = manageBlockingTimes();
//
//        if (msg == null) {
//            LOGGER.debug("Block time: " + t);
//            block(t); // cada cierto tiempo comprobar recursos/alarmas
//        }
        LOGGER.exit();
    }

    public int onEnd() {
        return 0;
    }

    @Override
    public boolean done() {
        return false;
    }

    /**
     * Calculates the blocking times, checking if the agent is periodic.
     *
     * @return blocking time (periodic) or 0 (not periodic).
     */
    private long manageBlockingTimes() {
        LOGGER.entry();
        long t = 0;
        if (PrevPeriod != myAgent.period) {
            NextActivation = System.currentTimeMillis() + myAgent.period;
            PrevPeriod = myAgent.period;
            LOGGER.debug("Restarting period due to change of period");
            return (long) LOGGER.exit(myAgent.period);
        }
        if ((myAgent.period < 0)) {
            return (long) LOGGER.exit(0);
        } else {
            t = NextActivation - System.currentTimeMillis();
            if (t <= 0) {
                LOGGER.debug("Restarting period due to cycle");
                NextActivation = System.currentTimeMillis() + myAgent.period;
                t = myAgent.period;
            }
        }
        return LOGGER.exit(t);
    }


    // TODO mirarlo bien --> Metodo conseguido de RunningBehaviour
    private Object[] manageReceivedMsg(ACLMessage msg) {
        LOGGER.entry(msg);
        if (msg != null) {
            LOGGER.debug("Message received from: " + msg.getSender().getLocalName());
            if (myAgent.sourceComponentIDs != null && myAgent.sourceComponentIDs.length > 0) {
                String senderCmp = myAgent.getComponent(msg.getSender().getLocalName());
                LOGGER.debug("senderCmp = " + senderCmp);
                buscar: for (int i = 0; i < myAgent.sourceComponentIDs.length; i++) {
                    if (senderCmp == null) {
                        break buscar;
                    }
                    LOGGER.info(senderCmp + " checked with " + myAgent.sourceComponentIDs[i]);
                    if ((myAgent.sourceComponentIDs[i]).contains(senderCmp)) {
                        LOGGER.trace("found " + senderCmp);
                        try {
                            return new Object[] {msg.getContentObject()};
                        } catch (UnreadableException e) {
                            LOGGER.debug("Received message without an object in its content");
                            e.printStackTrace();
                        }
                        break buscar;
                    }
                }
            } else {
                LOGGER.debug("Received message in an agent withou sourceComponentIDs");
                return LOGGER.exit(new Object[] {msg});
            }
        }
        return LOGGER.exit(null);
    }
    public void sendACLMessage(int performative, AID reciever, String ontology, String conversationId, String content, Agent agent) {

        ACLMessage msg = new ACLMessage(performative); //envio del mensaje
        msg.addReceiver(reciever);
        msg.setOntology(ontology);
        msg.setConversationId(conversationId);
        msg.setContent(content);
        myAgent.send(msg);
    }
    public void AddToExpectedMsgs(String sender, String convID, String content){
        Object[] ExpMsg=new Object[4];
        ExpMsg[0]=sender;
        ExpMsg[1]=convID;
        ExpMsg[2]=content;
        Date date = new Date();
        long instant = date.getTime();
        instant=instant+1000; //añade una espera de 1 seg
        ExpMsg[3]=instant;
        myAgent.expected_msgs.add(ExpMsg);
    }
}