package es.ehu.platform.behaviour;

import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.NegFunctionality;
import es.ehu.platform.utilities.Cmd;
import es.ehu.platform.utilities.MsgNegotiation;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import static es.ehu.platform.utilities.MasReconOntologies.ONT_NEGOTIATE;

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

        public NegotiationData(AID requester, MsgNegotiation negotiationMsg) {
            this.requester = requester;
            this.negotiationMsg = negotiationMsg;
            this.repliesCnt = 0;
            this.scalarValue = -1;
            this.flagTie = false;
            this.taskId = negotiationMsg.getTaskID();
            this.timeStamp = System.currentTimeMillis();
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

    private MWAgent myAgent;
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

    private static String actionValue = null;
    //private static String QoSAgentName="QoSManagerAgent";

    public NegotiatingBehaviour(MWAgent a) {
        super(a);
        LOGGER.entry(a);
        this.myAgent = a;
        this.aNegFunctionality = (NegFunctionality) a.functionalityInstance;
        template = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_NEGOTIATE), //es negociación Y (
                MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CFP), //o es cfp
                        MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE), // o es proposal
                                MessageTemplate.MatchPerformative(ACLMessage.FAILURE)))); // o es failure)
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
                LOGGER.info("msg="+msg.getContent());
                Cmd cmd = new Cmd(msg.getContent());
                System.out.println("externaldata="+cmd.attribs.get("externaldata"));

                StringTokenizer externaldata = new StringTokenizer(cmd.attribs.get("externaldata"),",");
                actionValue = cmd.attribs.get("action");

                MsgNegotiation negMsg = null;
                if (actionValue.equals("start")) {
                    negMsg = new MsgNegotiation((Iterator<AID>) msg.getAllReceiver(), conversationId, cmd.attribs.get("action"), cmd.attribs.get("criterion"),
                            externaldata.nextElement().toString(), externaldata.nextElement().toString(), externaldata.nextElement().toString(), externaldata.nextElement().toString(), externaldata.nextElement().toString(), externaldata.nextElement().toString());
                    if(negMsg.getTargets().length<=1){
                        switch (aNegFunctionality.checkNegotiation(conversationId, cmd.attribs.get("action"), 0,
                               1, true, true, negMsg.getExternalData()[0], negMsg.getExternalData()[1], negMsg.getExternalData()[2], negMsg.getExternalData()[3], negMsg.getExternalData()[4], negMsg.getExternalData()[5])) {

                            case NEG_LOST: //he perdido la negociación
                                LOGGER.info("> " + myAgent.getLocalName() + " lost nego" + conversationId);
                                break;

                            case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
                                break;

                            case NEG_WON: //he ganado la negociación y termina correctamente
                                System.out.println("WON!");

                            case NEG_FAIL:
                                break;
                        }
                    }
                }else if (actionValue.equals("execute")) {
                    negMsg = new MsgNegotiation((Iterator<AID>) msg.getAllReceiver(), conversationId, cmd.attribs.get("action"), cmd.attribs.get("criterion"),
                            externaldata.nextElement().toString(), externaldata.nextElement().toString(), externaldata.nextElement().toString());
                    if(negMsg.getTargets().length<=1){
                        switch (aNegFunctionality.checkNegotiation(conversationId, cmd.attribs.get("action"), 1,
                                0, true, true, negMsg.getExternalData()[0], negMsg.getExternalData()[1], negMsg.getExternalData()[2])) {

                            case NEG_LOST: //he perdido la negociación
                                LOGGER.info("> " + myAgent.getLocalName() + " lost nego" + conversationId);
                                break;

                            case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
                                break;

                            case NEG_WON: //he ganado la negociación y termina correctamente
                                System.out.println("WON!");

                            case NEG_FAIL:
                                break;
                        }
                    }
                }else if (actionValue.equals("supplyConsumables")) {
                    negMsg = new MsgNegotiation((Iterator<AID>) msg.getAllReceiver(), conversationId, cmd.attribs.get("action"), cmd.attribs.get("criterion"),
                            externaldata.nextElement().toString(), externaldata.nextElement().toString());
                    if(negMsg.getTargets().length<=1){
                        switch (aNegFunctionality.checkNegotiation(conversationId, cmd.attribs.get("action"), 15000,
                                1, true, true, negMsg.getExternalData()[0], negMsg.getExternalData()[1])) {

                            case NEG_LOST: //he perdido la negociación
                                LOGGER.info("> " + myAgent.getLocalName() + " lost nego" + conversationId);
                                break;

                            case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
                                break;

                            case NEG_WON: //he ganado la negociación y termina correctamente
                                System.out.println("WON!");

                            case NEG_FAIL:
                                break;
                        }
                    }
                }

                initNegotiation(conversationId, msg.getSender(), negMsg);

            } else {
                // Recibo propuestas
                if ((msg.getPerformative() == ACLMessage.PROPOSE) && (actionValue.equals("start"))) {
                    if (negotiationRuntime.containsKey(conversationId)) {
                        Long receivedVal = new Long(0);
                        try {
                            receivedVal = (Long) msg.getContentObject();
                            LOGGER.debug("Proposal content: " + receivedVal);
                        } catch (Exception e) {
                            LOGGER.debug("Received value is not a number");
                        }

                        LOGGER.info(msg.getSender().getLocalName() + "(" + receivedVal + ") ");
                        negotiationRuntime.get(conversationId).cntReplies();

                        boolean tieBreak = msg.getSender().getLocalName().compareTo(myAgent.getLocalName()) > 0;

                        String seID = (String) negotiationRuntime.get(conversationId).getExternalData()[0];
                        String seType = (String) negotiationRuntime.get(conversationId).getExternalData()[1];
                        String seClass = (String) negotiationRuntime.get(conversationId).getExternalData()[2];
                        String seFirstTransition = (String) negotiationRuntime.get(conversationId).getExternalData()[3];
                        String redundancy = (String) negotiationRuntime.get(conversationId).getExternalData()[4];
                        String parentAgentID = (String) negotiationRuntime.get(conversationId).getExternalData()[5];

                        switch (aNegFunctionality.checkNegotiation(conversationId, negotiationRuntime.get(conversationId).getAction(), receivedVal,
                                negotiationRuntime.get(conversationId).getScalarValue(), tieBreak, negotiationRuntime.get(conversationId).checkReplies(),
                                seID, seType, seClass, seFirstTransition, redundancy, parentAgentID)) {

                            case NEG_LOST: //he perdido la negociación
                                LOGGER.info("> " + myAgent.getLocalName() + "(" + negotiationRuntime.get(conversationId).getScalarValue() + ") lost nego" + conversationId);
                                negotiationRuntime.remove(conversationId); //salgo de esta negociación
                                break;

                            case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
                                negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                break;

                            case NEG_WON: //he ganado la negociación y termina correctamente
                                System.out.println("WON!");
                                negotiationRuntime.remove(conversationId); // borrar negotiationRuntime

                            case NEG_FAIL:
                                negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                break;
                        }

                    } else { // !negotiationRuntime.containsKey(conversationId)
                        LOGGER.debug("message " + msg.getConversationId() + "is not for me"); //estoy fuera de esta negociación porque ya la he perdido
                    }
                } else if ((msg.getPerformative() == ACLMessage.PROPOSE) && (actionValue.equals("execute"))) {
                    if (negotiationRuntime.containsKey(conversationId)) {
                        Long receivedVal = new Long(0);
                        try {
                            receivedVal = (Long) msg.getContentObject();
                            LOGGER.debug("Proposal content: " + receivedVal);
                        } catch (Exception e) {
                            LOGGER.debug("Received value is not a number");
                        }

                        LOGGER.info(msg.getSender().getLocalName() + "(" + receivedVal + ") ");
                        negotiationRuntime.get(conversationId).cntReplies();

                        boolean tieBreak = msg.getSender().getLocalName().compareTo(myAgent.getLocalName()) > 0;

                        String seID = (String) negotiationRuntime.get(conversationId).getExternalData()[0];
                        String seNumOfItems = (String) negotiationRuntime.get(conversationId).getExternalData()[1];
                        String seOperationID = (String) negotiationRuntime.get(conversationId).getExternalData()[2];

                        switch (aNegFunctionality.checkNegotiation(conversationId, negotiationRuntime.get(conversationId).getAction(), receivedVal,
                                negotiationRuntime.get(conversationId).getScalarValue(), tieBreak, negotiationRuntime.get(conversationId).checkReplies(),
                                seID, seNumOfItems, seOperationID)) {

                            case NEG_LOST: //he perdido la negociación
                                LOGGER.info("> " + myAgent.getLocalName() + "(" + negotiationRuntime.get(conversationId).getScalarValue() + ") lost nego" + conversationId);
                                negotiationRuntime.remove(conversationId); //salgo de esta negociación
                                break;

                            case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
                                negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                break;

                            case NEG_WON: //he ganado la negociación y termina correctamente
                                System.out.println("WON!");
                                negotiationRuntime.remove(conversationId); // borrar negotiationRuntime

                            case NEG_FAIL:
                                negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                break;
                        }
                    }

                } else if ((msg.getPerformative() == ACLMessage.PROPOSE) && (actionValue.equals("supplyConsumables"))) {
                    if (negotiationRuntime.containsKey(conversationId)) {
                        Long receivedVal = new Long(0);
                        try {
                            receivedVal = (Long) msg.getContentObject();
                            LOGGER.debug("Proposal content: " + receivedVal);
                        } catch (Exception e) {
                            LOGGER.debug("Received value is not a number");
                        }

                        LOGGER.info(msg.getSender().getLocalName() + "(" + receivedVal + ") ");
                        negotiationRuntime.get(conversationId).cntReplies();

                        boolean tieBreak = msg.getSender().getLocalName().compareTo(myAgent.getLocalName()) > 0;

                        String externalData = (String) negotiationRuntime.get(conversationId).getExternalData()[0];
                        String machineAgentName = (String) negotiationRuntime.get(conversationId).getExternalData()[1];

                        switch (aNegFunctionality.checkNegotiation(conversationId, negotiationRuntime.get(conversationId).getAction(), receivedVal,
                                negotiationRuntime.get(conversationId).getScalarValue(), tieBreak, negotiationRuntime.get(conversationId).checkReplies(),
                                externalData, machineAgentName)) {

                            case NEG_LOST: //he perdido la negociación
                                LOGGER.info("> " + myAgent.getLocalName() + "(" + negotiationRuntime.get(conversationId).getScalarValue() + ") lost nego" + conversationId);
                                negotiationRuntime.remove(conversationId); //salgo de esta negociación
                                break;

                            case NEG_RETRY: //he ganado la negociación pero había ganado otra por lo que pido al que la ha iniciado que repita
                                negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                break;

                            case NEG_WON: //he ganado la negociación y termina correctamente
                                System.out.println("WON!");
                                negotiationRuntime.remove(conversationId); // borrar negotiationRuntime

                            case NEG_FAIL:
                                negotiationRuntime.remove(conversationId); // borrar negotiationRuntime
                                break;
                        }

                    } else { // !negotiationRuntime.containsKey(conversationId)
                        LOGGER.debug("message " + msg.getConversationId() + "is not for me"); //estoy fuera de esta negociación porque ya la he perdido
                    }
                }else if (msg.getPerformative() == ACLMessage.FAILURE) {
                    LOGGER.info("Received FAILURE message with convID:" + conversationId);
                    if (negotiationRuntime.get(conversationId) != null) {
                        negotiationRuntime.get(conversationId).cntReplies();
                    }
                    try {
                        String name = msg.getContent().substring(msg.getContent().indexOf(":name ", msg.getContent().indexOf("MTS-error")) + ":name ".length());
                        LOGGER.warn(name.substring(0, name.indexOf('@')) + " FAILURE");

                        /*************************** Modificaciones Diego*/
//                        AID QoSID = new AID("QoSManagerAgent", false);
//                        ACLMessage inform_qos=new ACLMessage(ACLMessage.FAILURE);
//                        inform_qos.setContent(name.substring(0, name.indexOf('@')));
//                        inform_qos.addReceiver(QoSID);
//                        inform_qos.setOntology("acl_error");
//                        myAgent.send(inform_qos);
                        /************************** Fin Modificaciones Diego*/

                    } catch (Exception e) {
                    }
                }
            }
        } else {
            block();
        }
        LOGGER.exit();
    }

    /**
     * Create a newNegotiationData, calculate the own negotiationValue and saves
     * NewData in the negotiationRuntime Create the Propose msg with the calculated
     * scalarValue and send it to other targets.
     *
     * @param negId     ConversationId (negotiationID)
     * @param requester AID of the requester agent
     * @param negMsg    necessary information to start the negotiation
     */
    private void initNegotiation(String negId, AID requester, MsgNegotiation negMsg) {
        LOGGER.entry();

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
            //get distance of transport to X.
        long value = aNegFunctionality.calculateNegotiationValue(negMsg.getNegAction(), negMsg.getCriterion(), negMsg.getExternalData());


        try {
            cfp.setContentObject(new Long(value));
        } catch (Exception e) {
            LOGGER.error("Negotiation content in " + myAgent.getLocalName() + " could not be sent!! - " + negId);
        }
        myAgent.send(cfp);
        LOGGER.debug("Sent Negotiation Propose msg");
        NegotiationData newNegotiation = new NegotiationData(requester, negMsg);
        newNegotiation.setScalarValue(value);
        negotiationRuntime.put(negId, newNegotiation);
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
