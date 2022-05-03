package es.ehu.platform.behaviour;

import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.NegFunctionality;
import es.ehu.platform.utilities.Cmd;
import es.ehu.platform.utilities.MsgNegotiation;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import static es.ehu.platform.utilities.MasReconOntologies.*;

/**
 * Manages several simultaneous negotiations, differentiating the execution
 * status of each one by the conversationID.
 **/
public class NegotiatingBehaviour extends SimpleBehaviour {

    public static final int NEG_LOST=0, NEG_PARTIAL_WON=1, NEG_WON=2, NEG_RETRY=-1, NEG_FAIL=-2;

    private static class NegotiationData {

        /** Identifier of the task which is being negotiated. */
        private String taskId;

        /** Identifier of the agents which request the negotiation */
        private AID requester;

        private MsgNegotiation negotiationMsg;

        /** Scalar value used to negotiate. */
        private long scalarValue;

        /** Agent counter. Checks the number of agents that have already negotiated. */
        private int repliesCnt;

        private long timeStamp = 0;

        /** Flag to indicate if the negotiation is tied */
        private boolean flagTie;

        /** **/
        private boolean partialWinner = true;

        public NegotiationData(AID requester, MsgNegotiation negotiationMsg) {
            this.requester = requester;
            this.negotiationMsg = negotiationMsg;
            this.repliesCnt = 0;
            this.scalarValue = -1;
            this.flagTie = false;
            this.taskId = negotiationMsg.getTaskID();
            this.timeStamp = System.currentTimeMillis();
        }

        public boolean isPartialWinner () {
            return partialWinner;
        }

        public void isNotPartialWinner(){
            this.partialWinner=false;
        }

        // Setter methods
        public void setScalarValue(long a) {
            this.scalarValue = a;
        }

        // Getter methods
        public long getScalarValue() {
            return this.scalarValue;
        }

        public String getTaskId() {
            return this.taskId;
        }

        public AID getRequester() {
            return this.requester;
        }

        public AID[] getTargets() {
            return this.negotiationMsg.getTargets();
        }

        public String getAction() {
            return this.negotiationMsg.getNegAction();
        }

        public String getCriterion() {
            return this.negotiationMsg.getCriterion();
        }

        public Object[] getExternalData() {
            return this.negotiationMsg.getExternalData();
        }

        public boolean getFlagTie() {
            return this.flagTie;
        }

        // Other methods
        public void cntReplies() {
            this.repliesCnt++;
        }

        // count a reply and returns true if every target for the current negotiation has sent a proposal
        public boolean checkReplies() {
            return (repliesCnt >= (negotiationMsg.getTargets().length - 1)) ? true : false;
        }

        public void activateFlagTie() {
            this.flagTie = true;
        }

    }

    private static final long serialVersionUID = 5211311085804151394L;
    static final Logger LOGGER = LogManager.getLogger(NegotiatingBehaviour.class.getName());
    private static final String CMD_REFUSE = "Refuse";
    private boolean busy=false;
    private MWAgent myAgent;
    private boolean direct_win=false; //evita realizar calculos de valor de negociacion cuando una entidad ha ganado ya por ser la unica en participar.
    private NegFunctionality aNegFunctionality;

    /**
     * Pattern of ACL messages that this agent uses to filter its
     * {@code MessageQueue}.
     */
    private MessageTemplate template;

    /**
     * Object to store all the information needed to execute simultaneous
     * negotiation at the same time, using the key value to differenciate them.
     * <p>
     * <ul>
     * <li><b>key</b>: ConversationID to identify the negotiation in process.</li>
     * <li><b>value</b>: Data needed to process the negotiation.</li>
     * </ul>
     */
    private ConcurrentHashMap<String, NegotiationData> negotiationRuntime = new ConcurrentHashMap<String, NegotiationData>();
    private CircularFifoQueue CFP_FIFO = new CircularFifoQueue(10000); //fifo para anotar los CFP recibidos hasta poder leerlos

    private String actionValue = null;
    private String actionValue_temp = null;


    public NegotiatingBehaviour(MWAgent a) {
        super(a);
        LOGGER.entry(a);
        this.myAgent = a;
        this.aNegFunctionality = (NegFunctionality) a.functionalityInstance;
        template = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_NEGOTIATE), //es negociación Y (
                MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CFP), //o es cfp
                        MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),// o es proposal
                                MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.FAILURE),// o es failure
                                        MessageTemplate.MatchPerformative(ACLMessage.INFORM))))); //o es inform.
        LOGGER.exit();
    }

    /**
     * Prepares the negotiation value.
     */
    public void onStart() {
        LOGGER.entry();
        LOGGER.exit();
    }

    public void action() {
        LOGGER.entry();

        ACLMessage msg = myAgent.receive(template);

        if (msg != null) {

            String conversationId = msg.getConversationId();

            // recibo petición de negociación
            if (msg.getPerformative() == ACLMessage.CFP) {
                    LOGGER.info("message " + msg.getConversationId() + "="+msg.getContent());
                    Cmd cmd = new Cmd(msg.getContent());
                    System.out.println("externaldata="+cmd.attribs.get("externaldata"));

                    StringTokenizer externaldata = new StringTokenizer(cmd.attribs.get("externaldata"),",");
                    actionValue_temp = cmd.attribs.get("action");
                MsgNegotiation negMsg = null;

                        actionValue_temp = cmd.attribs.get("action");
                        if (actionValue_temp.equals("start")) {
                            negMsg = new MsgNegotiation((Iterator<AID>) msg.getAllReceiver(), conversationId, cmd.attribs.get("action"), cmd.attribs.get("criterion"),
                                    externaldata.nextElement().toString(), externaldata.nextElement().toString(), externaldata.nextElement().toString(), externaldata.nextElement().toString(), externaldata.nextElement().toString(), externaldata.nextElement().toString());
                            if(negMsg.getTargets().length<=1){
                                actionValue=actionValue_temp;
                                switch (aNegFunctionality.checkNegotiation(conversationId, cmd.attribs.get("action"), 0,
                                        1, true, true, true, negMsg.getExternalData()[0], negMsg.getExternalData()[1], negMsg.getExternalData()[2], negMsg.getExternalData()[3], negMsg.getExternalData()[4], negMsg.getExternalData()[5])) {

                                    case NEG_LOST: //he perdido la negociación
                                        LOGGER.info("> " + myAgent.getLocalName() + " lost nego" + conversationId);
                                        break;

                                    case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
                                        break;

                                    case NEG_WON: //he ganado la negociación y termina correctamente
                                        System.out.println("WON!");
                                        direct_win=true;
                                    case NEG_FAIL:
                                        break;
                                }
                            }
                        } else if (actionValue_temp.equals("recover_tracking")) {
                                negMsg = new MsgNegotiation((Iterator<AID>) msg.getAllReceiver(), conversationId, cmd.attribs.get("action"), cmd.attribs.get("criterion"),
                                        externaldata.nextElement().toString(), externaldata.nextElement().toString(), externaldata.nextElement().toString(), externaldata.nextElement().toString(), externaldata.nextElement().toString(), externaldata.nextElement().toString());
                                if(negMsg.getTargets().length<=1){
                                    actionValue=actionValue_temp;
                                    switch (aNegFunctionality.checkNegotiation(conversationId, cmd.attribs.get("action"), 0,
                                            1, true, true, true, negMsg.getExternalData()[0], negMsg.getExternalData()[1], negMsg.getExternalData()[2], negMsg.getExternalData()[3], negMsg.getExternalData()[4], negMsg.getExternalData()[5])) {

                                        case NEG_LOST: //he perdido la negociación
                                            LOGGER.info("> " + myAgent.getLocalName() + " lost nego" + conversationId);
                                            break;

                                        case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
                                            break;

                                        case NEG_WON: //he ganado la negociación y termina correctamente
                                            System.out.println("WON!");
                                            direct_win=true;
                                        case NEG_FAIL:
                                            break;
                                    }
                                }
                        }else if (actionValue_temp.equals("execute")) {
                            if(cmd.attribs.get("criterion").equals("finish_time")){
                                negMsg = new MsgNegotiation((Iterator<AID>) msg.getAllReceiver(), conversationId, cmd.attribs.get("action"), cmd.attribs.get("criterion"),
                                        externaldata.nextElement().toString());
                                if(negMsg.getTargets().length<=1){
                                    actionValue=actionValue_temp;
                                    switch (aNegFunctionality.checkNegotiation(conversationId, cmd.attribs.get("action"), 1,
                                            0, true, true, true, negMsg.getExternalData()[0])) {

                                        case NEG_LOST: //he perdido la negociación
                                            LOGGER.info("> " + myAgent.getLocalName() + " lost nego" + conversationId);
                                            break;

                                        case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
                                            break;

                                        case NEG_WON: //he ganado la negociación y termina correctamente
                                            System.out.println("WON!");
                                            direct_win=true;
                                        case NEG_FAIL:
                                            break;
                                    }
                                }
                            }else{
                                negMsg = new MsgNegotiation((Iterator<AID>) msg.getAllReceiver(), conversationId, cmd.attribs.get("action"), cmd.attribs.get("criterion"),
                                        externaldata.nextElement().toString(), externaldata.nextElement().toString(), externaldata.nextElement().toString());
                                if(negMsg.getTargets().length<=1){
                                    actionValue=actionValue_temp;
                                    switch (aNegFunctionality.checkNegotiation(conversationId, cmd.attribs.get("action"), 1,
                                            0, true, true, true, negMsg.getExternalData()[0], negMsg.getExternalData()[1], negMsg.getExternalData()[2])) {

                                        case NEG_LOST: //he perdido la negociación
                                            LOGGER.info("> " + myAgent.getLocalName() + " lost nego" + conversationId);
                                            break;

                                        case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
                                            break;

                                        case NEG_WON: //he ganado la negociación y termina correctamente
                                            System.out.println("WON!");
                                            direct_win=true;
                                        case NEG_FAIL:
                                            break;
                                    }
                                }
                            }
                        }else if (actionValue_temp.equals("supplyConsumables")) {
                            negMsg = new MsgNegotiation((Iterator<AID>) msg.getAllReceiver(), conversationId, cmd.attribs.get("action"), cmd.attribs.get("criterion"),
                                    externaldata.nextElement().toString(), externaldata.nextElement().toString());
                            if(negMsg.getTargets().length<=1){
                                actionValue=actionValue_temp;
                                switch (aNegFunctionality.checkNegotiation(conversationId, cmd.attribs.get("action"), 15000,
                                        1, true, true, true, negMsg.getExternalData()[0], negMsg.getExternalData()[1])) {

                                    case NEG_LOST: //he perdido la negociación
                                        LOGGER.info("> " + myAgent.getLocalName() + " lost nego" + conversationId);
                                        break;

                                    case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
                                        break;

                                    case NEG_WON: //he ganado la negociación y termina correctamente
                                        System.out.println("WON!");
                                        direct_win=true;
                                    case NEG_FAIL:
                                        break;
                                }
                            }

                        }else if(actionValue_temp.equals("restore")){                    //replica que quiere pasar a running
                            negMsg = new MsgNegotiation((Iterator<AID>) msg.getAllReceiver(), conversationId, cmd.attribs.get("action"), cmd.attribs.get("criterion"));
                            if(negMsg.getTargets().length<=1){
                                actionValue=actionValue_temp;
                                switch (aNegFunctionality.checkNegotiation(conversationId, cmd.attribs.get("action"), 100,
                                        1, true, true, true)) {

                                    case NEG_LOST: //he perdido la negociación
                                        LOGGER.info("> " + myAgent.getLocalName() + " lost nego" + conversationId);
                                        break;

                                    case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
                                        break;

                                    case NEG_WON: //he ganado la negociación y termina correctamente

                                        ACLMessage inform_winner = new ACLMessage(ACLMessage.INFORM);
                                        inform_winner.setOntology(ONT_NEGOTIATE);
                                        inform_winner.setConversationId(conversationId);

                                        AID DDId = new AID("D&D", false);
                                        inform_winner.addReceiver(DDId); //para este caso solo hay que avisar al D&D pues no hay replicas
                                        myAgent.send(inform_winner);
                                        System.out.println("WON!");
                                        direct_win=true;

                                    case NEG_FAIL:
                                        break;
                                }
                            }
                        }
                    if(!direct_win){ //significa que solo habia una instancia para negociar por lo que no tiene
                        regNegotiation(conversationId, msg.getSender(), negMsg);
                        initNegotiation();
                    }
                    direct_win=false;

            } else if (msg.getPerformative() == ACLMessage.INFORM){
                busy=false;
                actionValue=null; //ya esta disponible para un nuevo CFP
            } else{
                // Recibo propuestas
                if ((msg.getPerformative() == ACLMessage.PROPOSE)) {
                    if(actionValue!=null) { //recibo propose pero no estoy en un CFP por lo que mi actionvalue puede ser null
                        if (actionValue.equals("start")) {
                            if (negotiationRuntime.containsKey(conversationId)) {
                                Long receivedVal = new Long(0);
                                try {
                                    receivedVal = Long.parseLong(msg.getContent());
                                } catch (Exception e) {
                                    LOGGER.debug("Received value is not a number");
                                }

                                LOGGER.info("message " + msg.getConversationId() + " from agent " + msg.getSender().getLocalName() + "(" + receivedVal + ") ");
                                negotiationRuntime.get(conversationId).cntReplies();
                                boolean tieBreak = msg.getSender().getLocalName().compareTo(myAgent.getLocalName()) > 0;

                                String seID = (String) negotiationRuntime.get(conversationId).getExternalData()[0];
                                String seType = (String) negotiationRuntime.get(conversationId).getExternalData()[1];
                                String seClass = (String) negotiationRuntime.get(conversationId).getExternalData()[2];
                                String parentAgentID = (String) negotiationRuntime.get(conversationId).getExternalData()[3];
                                String redundancy = (String) negotiationRuntime.get(conversationId).getExternalData()[4];
                                String seFirstTransition = (String) negotiationRuntime.get(conversationId).getExternalData()[5];

                                switch (aNegFunctionality.checkNegotiation(conversationId, negotiationRuntime.get(conversationId).getAction(), receivedVal,
                                        negotiationRuntime.get(conversationId).getScalarValue(), tieBreak, negotiationRuntime.get(conversationId).checkReplies(), negotiationRuntime.get(conversationId).isPartialWinner(),
                                        seID, seType, seClass, parentAgentID, redundancy, seFirstTransition)) {

                                    case NEG_LOST: //he perdido la negociación
                                        LOGGER.info("Negotiation (id: " + conversationId + ") loser " + myAgent.getLocalName() + " (value:" + negotiationRuntime.get(conversationId).getScalarValue() + ") dropped");//                                negotiationRuntime.remove(conversationId); //salgo de esta negociación
                                        negotiationRuntime.get(conversationId).isNotPartialWinner();
                                        break;

                                    case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
                                        LOGGER.info("Retry negotiation");
//                                negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                        break;

                                    case NEG_WON: //he ganado la negociación y termina correctamente
                                        LOGGER.info(myAgent.getLocalName() + " WON negotiation(id:" + conversationId + ")!");

//                                        try {
//                                            Thread.sleep(100);
//                                        } catch (InterruptedException e) {
//                                            e.printStackTrace();
//                                        }

                                        ACLMessage inform_winner = new ACLMessage(ACLMessage.INFORM);
                                        inform_winner.setOntology(ONT_NEGOTIATE);
                                        inform_winner.setConversationId(conversationId);
                                        LOGGER.debug("Targets lenght: " + negotiationRuntime.get(conversationId).getTargets().length);
                                        for (AID id : negotiationRuntime.get(conversationId).getTargets()) {
                                            // Removes the own agent from the list
                                            if (!id.getLocalName().equals(myAgent.getLocalName())) {
                                                inform_winner.addReceiver(id);
                                            }
                                        }

                                        myAgent.send(inform_winner);
                                        busy = false;
//                                negotiationRuntime.remove(conversationId); // borrar negotiationRuntime

                                    case NEG_FAIL:
//                                negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                        break;
                                }
                                if (negotiationRuntime.get(conversationId).checkReplies()) { //si he pasado por todos las pujas y sea ganador o perdedor
                                    negotiationRuntime.remove(conversationId);
                                }

                            } else {// !negotiationRuntime.containsKey(conversationId)

                                // todavía no he entrado a esa negociación o ¿la he perdido?
//                        myAgent.putBack(msg);
                                initNegotiation();
                                myAgent.postMessage(msg);
                                LOGGER.debug("negotiation " + msg.getConversationId() + "is not for me");
                            }
                        } if (actionValue.equals("recover_tracking")) {
                            if (negotiationRuntime.containsKey(conversationId)) {
                                Long receivedVal = new Long(0);
                                try {
                                    receivedVal = Long.parseLong(msg.getContent());
                                } catch (Exception e) {
                                    LOGGER.debug("Received value is not a number");
                                }

                                LOGGER.info("message " + msg.getConversationId() + " from agent " + msg.getSender().getLocalName() + "(" + receivedVal + ") ");
                                negotiationRuntime.get(conversationId).cntReplies();
                                boolean tieBreak = msg.getSender().getLocalName().compareTo(myAgent.getLocalName()) > 0;

                                String seID = (String) negotiationRuntime.get(conversationId).getExternalData()[0];
                                String seType = (String) negotiationRuntime.get(conversationId).getExternalData()[1];
                                String seClass = (String) negotiationRuntime.get(conversationId).getExternalData()[2];
                                String parentAgentID = (String) negotiationRuntime.get(conversationId).getExternalData()[3];
                                String redundancy = (String) negotiationRuntime.get(conversationId).getExternalData()[4];
                                String seFirstTransition = (String) negotiationRuntime.get(conversationId).getExternalData()[5];

                                switch (aNegFunctionality.checkNegotiation(conversationId, negotiationRuntime.get(conversationId).getAction(), receivedVal,
                                        negotiationRuntime.get(conversationId).getScalarValue(), tieBreak, negotiationRuntime.get(conversationId).checkReplies(), negotiationRuntime.get(conversationId).isPartialWinner(),
                                        seID, seType, seClass, parentAgentID, redundancy, seFirstTransition)) {

                                    case NEG_LOST: //he perdido la negociación
                                        LOGGER.info("Negotiation (id: " + conversationId + ") loser " + myAgent.getLocalName() + " (value:" + negotiationRuntime.get(conversationId).getScalarValue() + ") dropped");//                                negotiationRuntime.remove(conversationId); //salgo de esta negociación
                                        negotiationRuntime.get(conversationId).isNotPartialWinner();
                                        break;

                                    case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
                                        LOGGER.info("Retry negotiation");
//                                negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                        break;

                                    case NEG_WON: //he ganado la negociación y termina correctamente
                                        LOGGER.info(myAgent.getLocalName() + " WON negotiation(id:" + conversationId + ")!");

//                                        try {
//                                            Thread.sleep(100);
//                                        } catch (InterruptedException e) {
//                                            e.printStackTrace();
//                                        }

                                        ACLMessage inform_winner = new ACLMessage(ACLMessage.INFORM);
                                        inform_winner.setOntology(ONT_NEGOTIATE);
                                        inform_winner.setConversationId(conversationId);
                                        LOGGER.debug("Targets lenght: " + negotiationRuntime.get(conversationId).getTargets().length);
                                        for (AID id : negotiationRuntime.get(conversationId).getTargets()) {
                                            // Removes the own agent from the list
                                            if (!id.getLocalName().equals(myAgent.getLocalName())) {
                                                inform_winner.addReceiver(id);
                                            }
                                        }
                                        myAgent.send(inform_winner);
                                        busy = false;
//                                negotiationRuntime.remove(conversationId); // borrar negotiationRuntime

                                    case NEG_FAIL:
//                                negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                        break;
                                }
                                if (negotiationRuntime.get(conversationId).checkReplies()) { //si he pasado por todos las pujas y sea ganador o perdedor
                                    negotiationRuntime.remove(conversationId);
                                }

                            } else {// !negotiationRuntime.containsKey(conversationId)

                                // todavía no he entrado a esa negociación o ¿la he perdido?
//                        myAgent.putBack(msg);
                                initNegotiation();
                                myAgent.postMessage(msg);
                                LOGGER.debug("negotiation " + msg.getConversationId() + "is not for me");
                            }
                        }else if (actionValue.equals("execute")) {
                            if (negotiationRuntime.containsKey(conversationId)) {
                                Long receivedVal = new Long(0);
                                try {
//                            receivedVal = (Long) msg.getContentObject();
                                    receivedVal = Long.parseLong(msg.getContent());
                                } catch (Exception e) {
                                    LOGGER.debug("Received value is not a number");
                                }
                                String Criterion = (String) negotiationRuntime.get(conversationId).getCriterion();
                                if(Criterion.equals("finish_time")){
                                    LOGGER.info(msg.getSender().getLocalName() + "(" + receivedVal + ") ");
                                    negotiationRuntime.get(conversationId).cntReplies();

                                    boolean tieBreak = msg.getSender().getLocalName().compareTo(myAgent.getLocalName()) > 0;

                                    String Operations = (String) negotiationRuntime.get(conversationId).getExternalData()[0];

                                    switch (aNegFunctionality.checkNegotiation(conversationId, negotiationRuntime.get(conversationId).getAction(), receivedVal,
                                            negotiationRuntime.get(conversationId).getScalarValue(), tieBreak, negotiationRuntime.get(conversationId).checkReplies(), negotiationRuntime.get(conversationId).isPartialWinner(),
                                            Operations)) {

                                        case NEG_LOST: //he perdido la negociación
                                            LOGGER.info("Negotiation (id: " + conversationId + ") loser " + myAgent.getLocalName() + " (value:" + negotiationRuntime.get(conversationId).getScalarValue() + ") dropped");
//                                negotiationRuntime.remove(conversationId); //salgo de esta negociación
                                            negotiationRuntime.get(conversationId).isNotPartialWinner();
                                            break;

                                        case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
//                                            negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                            break;

                                        case NEG_WON: //he ganado la negociación y termina correctamente
                                            LOGGER.info(myAgent.getLocalName() + " WON negotiation(id:" + conversationId + ")!");
//                                            negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                            ACLMessage inform_winner = new ACLMessage(ACLMessage.INFORM);
                                            inform_winner.setOntology(ONT_NEGOTIATE);
                                            inform_winner.setConversationId(conversationId);
                                            LOGGER.debug("Targets lenght: " + negotiationRuntime.get(conversationId).getTargets().length);
                                            for (AID id : negotiationRuntime.get(conversationId).getTargets()) {
                                                // Removes the own agent from the list
                                                if (!id.getLocalName().equals(myAgent.getLocalName())) {
                                                    inform_winner.addReceiver(id);
                                                }
                                            }
                                            myAgent.send(inform_winner);

//                                            ACLMessage new_operations=new ACLMessage(ACLMessage.REQUEST);
//                                            new_operations.setOntology(ONT_RUN);
//                                            new_operations.setContent(Operations);
//                                            new_operations.addReceiver(myAgent.getAID());
//                                            myAgent.send(new_operations);
                                            busy = false;

                                        case NEG_FAIL:
//                                            negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                            break;
                                    }
                                    if (negotiationRuntime.get(conversationId).checkReplies()) { //si he pasado por todos las pujas y sea ganador o perdedor
                                        negotiationRuntime.remove(conversationId);
                                    }
                                }else{
                                    LOGGER.info(msg.getSender().getLocalName() + "(" + receivedVal + ") ");
                                    negotiationRuntime.get(conversationId).cntReplies();

                                    boolean tieBreak = msg.getSender().getLocalName().compareTo(myAgent.getLocalName()) > 0;

                                    String seID = (String) negotiationRuntime.get(conversationId).getExternalData()[0];
                                    String seNumOfItems = (String) negotiationRuntime.get(conversationId).getExternalData()[1];
                                    String seOperationID = (String) negotiationRuntime.get(conversationId).getExternalData()[2];

                                    switch (aNegFunctionality.checkNegotiation(conversationId, negotiationRuntime.get(conversationId).getAction(), receivedVal,
                                            negotiationRuntime.get(conversationId).getScalarValue(), tieBreak, negotiationRuntime.get(conversationId).checkReplies(), negotiationRuntime.get(conversationId).isPartialWinner(),
                                            seID, seNumOfItems, seOperationID)) {

                                        case NEG_LOST: //he perdido la negociación
                                            LOGGER.info("Negotiation (id: " + conversationId + ") loser " + myAgent.getLocalName() + " (value:" + negotiationRuntime.get(conversationId).getScalarValue() + ") dropped");
//                                negotiationRuntime.remove(conversationId); //salgo de esta negociación
                                            negotiationRuntime.get(conversationId).isNotPartialWinner();
                                            break;

                                        case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
//                                            negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                            break;

                                        case NEG_WON: //he ganado la negociación y termina correctamente
                                            LOGGER.info(myAgent.getLocalName() + " WON negotiation(id:" + conversationId + ")!");
//                                            negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                            ACLMessage inform_winner = new ACLMessage(ACLMessage.INFORM);
                                            inform_winner.setOntology(ONT_NEGOTIATE);
                                            inform_winner.setConversationId(conversationId);
                                            LOGGER.debug("Targets lenght: " + negotiationRuntime.get(conversationId).getTargets().length);
                                            for (AID id : negotiationRuntime.get(conversationId).getTargets()) {
                                                // Removes the own agent from the list
                                                if (!id.getLocalName().equals(myAgent.getLocalName())) {
                                                    inform_winner.addReceiver(id);
                                                }
                                            }
                                            myAgent.send(inform_winner);
//
//                                            ACLMessage new_operations=new ACLMessage(ACLMessage.REQUEST);
//                                            new_operations.setOntology(ONT_RUN);
//                                            new_operations.setContent(Operations);
//                                            new_operations.addReceiver(myAgent.getAID());
//                                            myAgent.send(new_operations);
                                            busy = false;

                                        case NEG_FAIL:
//                                            negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                            break;
                                    }
                                    if (negotiationRuntime.get(conversationId).checkReplies()) { //si he pasado por todos las pujas y sea ganador o perdedor
                                        negotiationRuntime.remove(conversationId);
                                    }
                                }

                            } else {// !negotiationRuntime.containsKey(conversationId)

                                // todavía no he entrado a esa negociación o ¿la he perdido?
//                        myAgent.putBack(msg);
                                initNegotiation();
                                myAgent.postMessage(msg);
                                LOGGER.debug("negotiation " + msg.getConversationId() + "is not for me");
                            }

                        } else if (actionValue.equals("supplyConsumables")) {
                            if (negotiationRuntime.containsKey(conversationId)) {
                                Long receivedVal = new Long(0);
                                try {
                                    receivedVal = Long.parseLong(msg.getContent());
                                } catch (Exception e) {
                                    LOGGER.debug("Received value is not a number");
                                }

                                LOGGER.info(msg.getSender().getLocalName() + "(" + receivedVal + ") ");
                                negotiationRuntime.get(conversationId).cntReplies();

                                boolean tieBreak = msg.getSender().getLocalName().compareTo(myAgent.getLocalName()) > 0;

                                String externalData = (String) negotiationRuntime.get(conversationId).getExternalData()[0];
                                String machineAgentName = (String) negotiationRuntime.get(conversationId).getExternalData()[1];

                                switch (aNegFunctionality.checkNegotiation(conversationId, negotiationRuntime.get(conversationId).getAction(), receivedVal,
                                        negotiationRuntime.get(conversationId).getScalarValue(), tieBreak, negotiationRuntime.get(conversationId).checkReplies(), negotiationRuntime.get(conversationId).isPartialWinner(),
                                        externalData, machineAgentName)) {

                                    case NEG_LOST: //he perdido la negociación
                                        LOGGER.info("Negotiation (id: " + conversationId + ") loser " + myAgent.getLocalName() + " (value:" + negotiationRuntime.get(conversationId).getScalarValue() + ") dropped");
//                                negotiationRuntime.remove(conversationId); //salgo de esta negociación
                                        negotiationRuntime.get(conversationId).isNotPartialWinner();
                                        break;

                                    case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
                                        negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                        break;

                                    case NEG_WON: //he ganado la negociación y termina correctamente
                                        LOGGER.info(myAgent.getLocalName() + " WON negotiation(id:" + conversationId + ")!");
                                        negotiationRuntime.remove(conversationId); // borrar negotiationRuntime

                                    case NEG_FAIL:
                                        negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                        break;
                                }
                            } else {// !negotiationRuntime.containsKey(conversationId)

                                // todavía no he entrado a esa negociación o ¿la he perdido?
//                        myAgent.putBack(msg);
                                initNegotiation();
                                myAgent.postMessage(msg);
                                LOGGER.debug("negotiation " + msg.getConversationId() + " is not for me by now");
                            }
                        } else if (actionValue.equals("restore")) {
                            if (negotiationRuntime.containsKey(conversationId)) {
                                Long receivedVal = new Long(0);
                                try {
//                            receivedVal = (Long) msg.getContentObject();
                                    receivedVal = Long.parseLong(msg.getContent());
                                } catch (Exception e) {
                                    LOGGER.debug("Received value is not a number");
                                }

                                LOGGER.info("message " + msg.getConversationId() + " from agent " + msg.getSender().getLocalName() + "(" + receivedVal + ") ");
                                negotiationRuntime.get(conversationId).cntReplies();
                                boolean tieBreak = msg.getSender().getLocalName().compareTo(myAgent.getLocalName()) > 0;


                                switch (aNegFunctionality.checkNegotiation(conversationId, negotiationRuntime.get(conversationId).getAction(), receivedVal,
                                        negotiationRuntime.get(conversationId).getScalarValue(), tieBreak, negotiationRuntime.get(conversationId).checkReplies(), negotiationRuntime.get(conversationId).isPartialWinner()
                                )) {

                                    case NEG_LOST: //he perdido la negociación
                                        LOGGER.info("Negotiation (id: " + conversationId + ") loser " + myAgent.getLocalName() + " (value:" + negotiationRuntime.get(conversationId).getScalarValue() + ") dropped");//                                negotiationRuntime.remove(conversationId); //salgo de esta negociación
                                        negotiationRuntime.get(conversationId).isNotPartialWinner();
                                        break;

                                    case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
//                                negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                        break;

                                    case NEG_WON: //he ganado la negociación y termina correctamente
                                        LOGGER.info(myAgent.getLocalName() + " WON negotiation(id:" + conversationId + ")!");

                                        ACLMessage inform_winner = new ACLMessage(ACLMessage.INFORM);
                                        inform_winner.setOntology(ONT_NEGOTIATE);
                                        inform_winner.setConversationId(conversationId);
                                        LOGGER.debug("Targets lenght: " + negotiationRuntime.get(conversationId).getTargets().length);
                                        for (AID id : negotiationRuntime.get(conversationId).getTargets()) {
                                            // Removes the own agent from the list
                                            if (!id.getLocalName().equals(myAgent.getLocalName())) {
                                                inform_winner.addReceiver(id);
                                            }
                                        }
                                        AID DDId = new AID("D&D", false);
                                        inform_winner.addReceiver(DDId); //para este caso hay que avisar tambien al D&D para que se encargue de avisar a quien corresponda de que hay un nuevo encargado en running
                                        myAgent.send(inform_winner);

                                        ACLMessage recover_tracking_state = new ACLMessage(ACLMessage.REQUEST); //devuelve las replicas que han perdido a estado de tracking
                                        recover_tracking_state.setOntology("control");
                                        recover_tracking_state.setContent("setstate tracking");
                                        recover_tracking_state.setConversationId(conversationId);
                                        LOGGER.debug("Targets back to tracking state: " + negotiationRuntime.get(conversationId).getTargets().length);
                                        for (AID id : negotiationRuntime.get(conversationId).getTargets()) {
                                            // Removes the own agent from the list
                                            if (!id.getLocalName().equals(myAgent.getLocalName())) {
                                                recover_tracking_state.addReceiver(id);
                                            }
                                        }
                                        myAgent.send(recover_tracking_state);

                                        busy = false;
//                                negotiationRuntime.remove(conversationId); // borrar negotiationRuntime

                                    case NEG_FAIL:
//                                negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                        break;
                                }
                                if (negotiationRuntime.get(conversationId).checkReplies()) { //si he pasado por todos las pujas y sea ganador o perdedor
                                    negotiationRuntime.remove(conversationId);
                                }

                            } else {// !negotiationRuntime.containsKey(conversationId)

                                // todavía no he entrado a esa negociación o ¿la he perdido?
//                        myAgent.putBack(msg);
                                initNegotiation();
                                myAgent.postMessage(msg);
                                LOGGER.debug("negotiation " + msg.getConversationId() + "is not for me");
                            }
                        }
                    }else{
                        initNegotiation();
                        myAgent.postMessage(msg);
                    }
                } else if (msg.getPerformative() == ACLMessage.FAILURE) {
                    LOGGER.info("Received FAILURE message with convID:" + conversationId);
                    if (negotiationRuntime.get(conversationId) != null) {
                        negotiationRuntime.get(conversationId).cntReplies();
                    }
                    try {
                        String name = msg.getContent().substring(msg.getContent().indexOf(":name ", msg.getContent().indexOf("MTS-error")) + ":name ".length());
                        LOGGER.warn(name.substring(0, name.indexOf('@')) + " FAILURE");

                    } catch (Exception e) {
                    }
                }
            }
        } else {
            initNegotiation();
            block();
        }
        LOGGER.exit();
    }

    /**
     * Create a newNegotiationData, calculate the own negotiationValue and saves
     * NewData in the negotiationRuntime Create the Propose msg with the calculated
     * scalarValue and send it to other targets.
     *
//     * @param negId     ConversationId (negotiationID)
//     * @param requester AID of the requester agent
//     * @param negMsg    necessary information to start the negotiation
     */
//    private void initNegotiation(String negId, AID requester, MsgNegotiation negMsg) {
//        LOGGER.entry();
//
//        ACLMessage cfp = new ACLMessage(ACLMessage.PROPOSE);
//        cfp.setOntology(ONT_NEGOTIATE);
//        cfp.setConversationId(negId);
//        LOGGER.debug("Targets lenght: " + negMsg.getTargets().length);
//        for (AID id : negMsg.getTargets()) {
//            // Removes the own agent from the list
//            if (!id.getLocalName().equals(myAgent.getLocalName())) {
//                cfp.addReceiver(id);
//            }
//        }
//            //get distance of transport to X.
//        long value = aNegFunctionality.calculateNegotiationValue(negMsg.getNegAction(), negMsg.getCriterion(), negMsg.getExternalData());
//
//
//        try {
//            cfp.setContentObject(new Long(value));
//        } catch (Exception e) {
//            LOGGER.error("Negotiation content in " + myAgent.getLocalName() + " could not be sent!! - " + negId);
//        }
//        myAgent.send(cfp);
//        LOGGER.debug("Sent Negotiation Propose msg");
//        NegotiationData newNegotiation = new NegotiationData(requester, negMsg);
//        newNegotiation.setScalarValue(value);
//        negotiationRuntime.put(negId, newNegotiation);
//        LOGGER.exit();
//    }


//    String negId, AID requester, MsgNegotiation negMsg
    private void initNegotiation() {
        LOGGER.entry();
        //En la primera parte del método tengo que calcular el valor, y registrar la negociación
        if(!CFP_FIFO.isEmpty()&&!busy){ //si el agente se encuentra ocupado en una negociación o no ha recibido CFPs se ignora
            Object data[] = (Object[]) CFP_FIFO.poll();

            String negId=(String) data[0];
            AID requester=(AID) data[1];
            MsgNegotiation negMsg=(MsgNegotiation) data[2];

            actionValue=negMsg.getNegAction();
//            actionValue = cmd.attribs.get("action");


            long value = aNegFunctionality.calculateNegotiationValue(negMsg.getNegAction(), negMsg.getCriterion(), negMsg.getExternalData());
            NegotiationData newNegotiation = new NegotiationData(requester, negMsg);
            newNegotiation.setScalarValue(value);

            negotiationRuntime.put(negId, newNegotiation);
//            negotiationRuntime.put(negId, newNegotiation);
            //Segundo, enviar un mensaje al resto de agentes con mi valor de negociación
            ACLMessage cfp = new ACLMessage(ACLMessage.PROPOSE);
            cfp.setOntology(ONT_NEGOTIATE);
            cfp.setConversationId(negId);
            LOGGER.debug("Targets lenght: " + negMsg.getTargets().length);
            for (AID id : negMsg.getTargets()) {
                // Removes the own agent from the list
                if (!id.getLocalName().equals(myAgent.getLocalName())) {
                    cfp.addReceiver(id);
                }
            }
            try {
//                cfp.setContentObject(new Long(value));
                cfp.setContent(String.valueOf(value));
            } catch (Exception e) {
                LOGGER.error("Negotiation content in " + myAgent.getLocalName() + " could not be sent!! - " + negId);
            }
            myAgent.send(cfp);
            LOGGER.debug("Sent Negotiation Propose msg");
            busy=true;
        }

        LOGGER.exit();
    }
    private void regNegotiation(String negId, AID requester, MsgNegotiation negMsg){
        Object data[]= new Object[3];
        LOGGER.entry();
        data[0]=negId;
        data[1]=requester;
        data[2]=negMsg;
//        long value = aNegFunctionality.calculateNegotiationValue(negMsg.getNegAction(), negMsg.getCriterion(), negMsg.getExternalData());
//        NegotiationData newNegotiation = new NegotiationData(requester, negMsg);        //mantener
//        newNegotiation.setScalarValue(value);
//        negotiationRuntime.put(negId, newNegotiation);       //mantener
        CFP_FIFO.add((Object[]) data);
        LOGGER.exit();
    }

    @Override
    public boolean done() {
        return false;
    }

    public int onEnd() {
        return 0;
    }
}
