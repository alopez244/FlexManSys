package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.IExecManagement;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class Order_Functionality implements BasicFunctionality, IExecManagement {

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
        msg.setContent("Order created successfully");
        myAgent.send(msg);

        Hashtable<String, String> attributes = new Hashtable<String, String>();
        attributes.put("seClass", "es.ehu.domain.manufacturing.agents.BatchAgent");

        String status = seStart(myAgent.getLocalName(), attributes, null);
        if (status == null)
            System.out.println("BatchAgents created");
        else if (status == "-1")
            System.out.println("ERROR creating BatchAgent -> No existe el ID del plan");
        else if (status == "-4")
            System.out.println("ERROR creating BatchAgent -> No targets");

        return null;
    }

    @Override
    public String seStart(String seID, Hashtable<String, String> attribs, String conversationId) {

        // seID --> ID del mPlanAgent
        String parentQuery = "get " + seID + " attrib=parent";
        ACLMessage reply = null;

        try {
            reply = sendCommand(parentQuery);
            // ID del order con el cual el agente está relacionado
            String orderID = null;
            if (reply == null)  // Si no existe el id en el registro devuelve error
                return "-1";
            else
                orderID = reply.getContent();

            String query = "get batch* parent="+ orderID;  // Busco todos los batch de los que el order es parent (no se buscan todos los elementos porque pueden existir otros orderAgent en tracking)
            reply = sendCommand(query);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String allBatches;
        if (reply == null)  // Si no existen orders en el registro devuelve error
            return "-1";
        else
            allBatches = reply.getContent();

        List<String> items = Arrays.asList(allBatches.split("\\s*,\\s*"));
        for (String batchID: items) {
            // Creamos los agentes para cada batch
            try {
                String redundancy = "1";
                if ((attribs!=null) && (attribs.containsKey("redundancy")))
                    redundancy = attribs.get("redundancy");

                reply = sendCommand("get (get * parent=(get * parent=" + batchID + " category=restrictionList)) attrib=attribValue");
                String refServID = null;
                if (reply != null)
                    refServID = reply.getContent();

                reply = sendCommand("get * category=pNodeAgent" + ((refServID.length()>0)?" refServID=" + refServID:""));
                String targets = null;
                if (reply != null) {
                    targets = reply.getContent();
                    if (targets.length()<=0)
                        return "-4";
                }


                reply = sendCommand("get " + batchID + " attrib=category");
                String seCategory = null;
                if (reply != null)
                    seCategory = reply.getContent();
                String seClass = attribs.get("seClass");

                // Orden de negociacion a todos los nodos
                for (int i=0; i<Integer.parseInt(redundancy); i++)
                    reply = sendCommand("localneg " + targets + " action=start " + batchID + " criterion=max mem externaldata=" + batchID + "," + seCategory + "," + seClass + "," + ((i==0)?"running":"tracking"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public String seStop(String... seID) {
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
            // ID del order con el cual el agente está relacionado
            String orderID;
            if (reply == null)  // Si no existe el id en el registro devuelve error
                return "-1";
            else
                orderID = reply.getContent();

            reply = sendCommand("get " + orderID + " attrib=parent");
            if (reply != null) {  // ID del plan
                String planID = reply.getContent();     // Con el ID del plan conseguir su agente
                reply = sendCommand("get * parent=" + planID + " category=mPlanAgent");
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
