package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Batch_Functionality implements BasicFunctionality {

    private static final long serialVersionUID = 1L;
    private Agent myAgent;

    private String parentAgentID;

    @Override
    public Void init(MWAgent myAgent) {

        this.myAgent = myAgent;


        // Envio un mensaje a mi parent diciendole que me he creado correctamente
        parentAgentID = getParentAgentID(myAgent.getLocalName());
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(parentAgentID, AID.ISLOCALNAME));
        msg.setContent("Batch created successfully");
        myAgent.send(msg);


        return null;
    }


    @Override
    public Object execute(Object[] input) {
        return null;
    }

    public ACLMessage sendCommand(String cmd) throws Exception {

        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType("sa");
        dfd.addServices(sd);
        String mwm;

        while (true) {
            DFAgentDescription[] result = DFService.search(myAgent,dfd);

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

    private String getParentAgentID(String seID) {
        String parentAgID = null;
        String parentQuery = "get " + seID + " attrib=parent";
        ACLMessage reply = null;

        try {
            reply = sendCommand(parentQuery);
            // ID del batch con el cual el agente está relacionado
            String batchID;
            if (reply == null)  // Si no existe el id en el registro devuelve error
                return "-1";
            else
                batchID = reply.getContent();

            reply = sendCommand("get " + batchID + " attrib=parent");
            if (reply != null) {  // ID del plan
                String orderID = reply.getContent();     // Con el ID del order conseguir su agente
                reply = sendCommand("get * parent=" + orderID + " category=orderAgent");
                if (reply != null) {
                    parentAgID = reply.getContent();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parentAgID;
    }
}
