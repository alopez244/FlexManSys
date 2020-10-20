package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.AvailabilityFunctionality;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.IExecManagement;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class Order_Functionality extends DomApp_Functionality implements BasicFunctionality, AvailabilityFunctionality, IExecManagement {

    private static final long serialVersionUID = 1L;
    private Agent myAgent;

    private List<String> myBatches;
    private int chatID = 0; // Numero incremental para crear conversationID

    private String firstState;
    private String redundancy;
    private String parentAgentID;
    private String mySeType;
    private ArrayList<String> myReplicasID = new ArrayList<>();

    @Override
    public Object getState() {
        return null;
    }

    @Override
    public void setState(Object state) {

    }

    @Override
    public Void init(MWAgent myAgent) {

        this.myAgent = myAgent;

        // Crear un nuevo conversationID
        String conversationId = myAgent.getLocalName() + "_" + chatID++;

        firstState = getArgumentOfAgent(myAgent, "firstState");
        redundancy = getArgumentOfAgent(myAgent, "redundancy");
        parentAgentID = getArgumentOfAgent(myAgent, "parentAgent");
        mySeType = getMySeType(myAgent, conversationId);

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

            seStart(myAgent.getLocalName(), attributes, conversationId);

            // TODO primero comprobara que todas las replicas (tracking) se han creado correctamente, y despues comprobara los batches
            // Es decir, antes de avisar a su padre que esta creado, comprueba las replicas y despues los batches
            // Le a�adimos un comportamiento para que consiga todos los mensajes que le van a enviar los batch cuando se arranquen correctamente

            myReplicasID = processACLMessages(myAgent, mySeType, myBatches, conversationId, redundancy, parentAgentID, "Batch");

        } else {
            // Si su estado es tracking
            trackingOnBoot(myAgent, mySeType, conversationId);
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
