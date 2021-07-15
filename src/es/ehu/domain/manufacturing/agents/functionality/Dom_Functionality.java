package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.domain.manufacturing.test.timeout;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.*;


public class Dom_Functionality {

    static final Logger LOGGER = LogManager.getLogger(DomApp_Functionality.class.getName());

    private String QoSManagerName="QoSManagerAgent";

    private String recieverName;

    private String SenderName;

    private String msgErrContent;

    private Agent myAgent;

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

    public void sendACLMessage(int performative, AID reciever, String ontology, String conversationId, String content, Agent agent) {

                this.myAgent = agent;
                ACLMessage msg = new ACLMessage(performative); //envio del mensaje
                msg.addReceiver(reciever);
                msg.setOntology(ontology);
                msg.setConversationId(conversationId);
                msg.setContent(content);
                myAgent.send(msg);

                /*AID QoSAgentID = new AID(QoSManagerName, false);
                ACLMessage msgErr = new ACLMessage(ACLMessage.INFORM);
                recieverName = reciever.getName();
                SenderName = agent.getName();
                msgErrContent = "Error in communication between agents " + SenderName + " and " + recieverName + "\n" + "Message content: " + content + "\n" + "Performative: " + performative + "\n" + "Ontology: " + ontology + "\n" + "Conversation ID: " + conversationId;
                msgErr.addReceiver(QoSAgentID);
                msgErr.setOntology(ontology);
                msgErr.setConversationId(conversationId);
                msgErr.setContent(msgErrContent);
                myAgent.send(msgErr);*/

        }
    }


