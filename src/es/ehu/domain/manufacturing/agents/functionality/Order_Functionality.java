package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.IExecManagement;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class Order_Functionality extends DomApp_Functionality implements BasicFunctionality, IExecManagement {

    private static final long serialVersionUID = 1L;
    private Agent myAgent;

    private String parentAgentID;
    private List<String> myBatches;
    private int chatID = 0; // Numero incremental para crear conversationID

    private String firstState;
    private String redundancy;
    private ArrayList<String> myReplicasID = new ArrayList<>();

    @Override
    public Void init(MWAgent myAgent) {

        this.myAgent = myAgent;

        // Crear un nuevo conversationID
        String conversationId = myAgent.getLocalName() + "_" + chatID++;

        firstState = getArgumentOfAgent(myAgent, "firstState");
        redundancy = getArgumentOfAgent(myAgent, "redundancy");

        if (firstState.equals("running")) {

            // Cambiar a estado bootToRunning para que los tracking le puedan enviar mensajes
            String query = "set " + myAgent.getLocalName() + " state=bootToRunning";
            try {
                sendCommand(myAgent, query, conversationId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Hashtable<String, String> attributes = new Hashtable<String, String>();
            attributes.put("seClass", "es.ehu.domain.manufacturing.agents.BatchAgent");

            String status = seStart(myAgent.getLocalName(), attributes, conversationId);
            if (status == null)
                System.out.println("BatchAgents created");
            else if (status == "-1")
                System.out.println("ERROR creating BatchAgent -> No existe el ID del plan");
            else if (status == "-4")
                System.out.println("ERROR creating BatchAgent -> No targets");

            // TODO primero comprobara que todas las replicas (tracking) se han creado correctamente, y despues comprobara los batches
            // Es decir, antes de avisar a su padre que esta creado, comprueba las replicas y despues los batches
            // Le añadimos un comportamiento para que consiga todos los mensajes que le van a enviar los batch cuando se arranquen correctamente

            myReplicasID = behaviourToGetMyElementsMessages(myAgent, "Order", myBatches, conversationId, redundancy, "Batch");

        } else {
            // Si su estado es tracking

            String runningAgentID = null;
            try {
                String parentID = null;
                ACLMessage reply = sendCommand(myAgent, "get " + myAgent.getLocalName() + " attrib=parent", conversationId);
                if (reply != null)
                    parentID = reply.getContent();
                reply = sendCommand(myAgent, "get * parent=" + parentID + " category=orderAgent state=bootToRunning", conversationId);
                if (reply !=null) {
                    runningAgentID = reply.getContent();
                    sendElementCreatedMessage(myAgent, runningAgentID, "Order", true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Una vez mande el mensaje, registra que su estado es el tracking
            try {
                sendCommand(myAgent, "set " + myAgent.getLocalName() + " state=" + firstState, conversationId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public String seStart(String seID, Hashtable<String, String> attribs, String conversationId) {

        this.myBatches = getAllElements(myAgent, seID, "batch", conversationId);

        chatID = createAllElementsAgents(myAgent, myBatches, attribs, conversationId, redundancy, chatID);

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
}
