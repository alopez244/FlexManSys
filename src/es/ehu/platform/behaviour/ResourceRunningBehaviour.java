package es.ehu.platform.behaviour;

import jade.core.AID;
import jade.core.Agent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.platform.MWAgent;

import jade.core.behaviours.*;
import jade.lang.acl.*;

import java.util.ArrayList;
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

    private MessageTemplate template,template2;
    private MWAgent myAgent;
    private int PrevPeriod;
    private long NextActivation;
    private AID QoSID = new AID("QoSManagerAgent", false);
    private AID DDID = new AID("D&D", false);

    // Constructor. Create a default template for the entry messages
    public ResourceRunningBehaviour(MWAgent a) {
        super(a);
        LOGGER.debug("*** Constructing RunningBehaviour ***");
        this.myAgent = a;
        this.template = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_RUN),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        this.template2 = MessageTemplate.and(MessageTemplate.MatchOntology("release_buffer"),
                MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),MessageTemplate.MatchSender(DDID)));
    }

    public void onStart() {
        LOGGER.entry();
        myAgent.ActualState="running";
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

        ACLMessage new_target= myAgent.receive(template2);
        if(new_target!=null){     //D&D avisa de que ya se puede vaciar el cbuffer de mensajes
            if(new_target.getContent().contains("batchagent")){ //nuevo batch disponble para registrar la trazabilidad.
                ACLMessage parent= myAgent.sendCommand("get "+new_target.getContent()+" attrib=parent"); //se ha registrado el parent como key
                ArrayList<ACLMessage> postponed_msgs=new ArrayList<ACLMessage>();
                postponed_msgs=myAgent.msg_buffer.get(parent.getContent()); //se obtiene el listado de mensajes pendientes para el batch
                for(int i=0; i<postponed_msgs.size();i++){
                    postponed_msgs.get(i).addReceiver(new AID(new_target.getContent(),false));
                    myAgent.send(postponed_msgs.get(i));
                }
            }else{   //si no es el batch será que el GW ya esta disponible

            }
        }


        for(int i=0;i<myAgent.expected_msgs.size();i++){         //realiza el checkeo de mensajes de acknowledge
            Object[] exp_msg;
            exp_msg=myAgent.expected_msgs.get(i);
            ACLMessage complete_msg=(ACLMessage) exp_msg[0];     //el mensaje que se ha enviado
            jade.util.leap.Iterator itor = complete_msg.getAllReceiver();
            AID exp_msg_sender= (AID)itor.next();   //usa el iterador para obtener el AID de el receptor original del mensaje
            String convID=complete_msg.getConversationId();
            String content=complete_msg.getContent();
            long timeout=(long) exp_msg[1];        //el instante para el cual ya se deberia haber obtenido respuesta
            Date date = new Date();
            long instant = date.getTime();
            MessageTemplate ack_template=MessageTemplate.and(MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchConversationId(convID),
                    MessageTemplate.MatchSender(exp_msg_sender)),
                    MessageTemplate.MatchContent(content)),MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));
            ACLMessage ack= myAgent.receive(ack_template);
            if(ack==null){
                if(instant>timeout){
                    if(exp_msg_sender.getLocalName().equals(QoSID.getLocalName())){ //si no contesta el QoS este agente se asume que esta aislado
                        LOGGER.info("QoS did not answer on time. THIS AGENT MIGHT BE ISOLATED.");
                        if(myAgent.getLocalName().contains("machine")){
                            myAgent.state="idle";	//es un agente máquina por lo que transiciona a idle
                            myAgent.change_state=true;
                        }else{ //TODO añadir aquí agentes no contemplados cuando proceda
                            LOGGER.debug("Condición no programada ");
                        }
                    }else{
                        String sender_on_drawer="";  //para guardar el mensaje en el buffer hay que asignarle un receptor
                        LOGGER.error("Expected answer did not arrive on time from "+exp_msg_sender.getLocalName()); //el agente no ha respondido a tiempo, se denuncia al QoS
                        if(exp_msg_sender.getLocalName().contains("batchagent")){  //si el mensaje era para el agente batch se asigna el parent en el buffer
                            ACLMessage parent_name= myAgent.sendCommand("get "+exp_msg_sender.getLocalName()+" attrib=parent");
                            sender_on_drawer=parent_name.getContent();
                        }else{
                            sender_on_drawer=exp_msg_sender.getLocalName();
                        }
                        ArrayList<ACLMessage> postponed_msgs=new ArrayList<ACLMessage>();
                        postponed_msgs=myAgent.msg_buffer.get(sender_on_drawer); //por si habia algún mensaje anteriormente
                        if(postponed_msgs==null){
                            postponed_msgs=new ArrayList<ACLMessage>();
                            postponed_msgs.add(complete_msg);
                        }else{
                            postponed_msgs.add(complete_msg);
                        }
                        myAgent.msg_buffer.put(sender_on_drawer,postponed_msgs);  //se añade el mensaje a la lista de espera para enviarlo cuando el D&D nos confirme que existe un nuevo destinatario
                        String report=exp_msg_sender.getLocalName()+"/div/"+content;
                        ACLMessage report_to_QoS=sendACLMessage(6, QoSID, "acl_error", convID, report, myAgent);
                        AddToExpectedMsgs(report_to_QoS);  //se espera una respuesta del QoS también
                    }
                    myAgent.expected_msgs.remove(i);
                    i--;
                }
            }else{
                myAgent.expected_msgs.remove(i);
                i--;
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


        long t = manageBlockingTimes();

        if (msg == null&&myAgent.expected_msgs.size()==0) {
            LOGGER.debug("Block time: " + t);
            block(t); // cada cierto tiempo comprobar recursos/alarmas
        }
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
    public ACLMessage sendACLMessage(int performative, AID reciever, String ontology, String conversationId, String content, Agent agent) {

        ACLMessage msg = new ACLMessage(performative); //envio del mensaje
        msg.addReceiver(reciever);
        msg.setOntology(ontology);
        msg.setConversationId(conversationId);
        msg.setContent(content);
        myAgent.send(msg);
        return msg;
    }
    public void AddToExpectedMsgs(ACLMessage msg){
        Object[] ExpMsg=new Object[2];
//        ExpMsg[0]=sender;
//        ExpMsg[1]=convID;
//        ExpMsg[2]=content;
        ExpMsg[0]=msg;
        Date date = new Date();
        long instant = date.getTime();
        instant=instant+2000; //añade una espera de 2 seg
        ExpMsg[1]=instant;
        myAgent.expected_msgs.add(ExpMsg);
    }
}