package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.template.interfaces.AvailabilityFunctionality;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.IExecManagement;
import jade.core.Agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class Order_Functionality extends DomApp_Functionality implements BasicFunctionality, AvailabilityFunctionality, IExecManagement {

    private static final long serialVersionUID = 1L;
    private Agent myAgent;

    private List<String> myElements;
    private List<String> elementsToCreate = new ArrayList<>();
    private HashMap<String, String> elementsClasses;
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
            // Le añadimos un comportamiento para que consiga todos los mensajes que le van a enviar los batch cuando se arranquen correctamente

            myReplicasID = processACLMessages(myAgent, mySeType, elementsToCreate, conversationId, redundancy, parentAgentID);

        } else {
            // Si su estado es tracking
            trackingOnBoot(myAgent, mySeType, conversationId);

            myAgent.initTransition = ControlBehaviour.TRACKING;
        }

        return null;
    }

    @Override
    public String seStart(String seID, Hashtable<String, String> attribs, String conversationId) {

        ArrayList<String> creationCategories = new ArrayList<>();
        creationCategories.add("batch");  // Aqui decidiremos que tipos de elementos queremos crear --> Order, Batch, las dos...

        List result = seStart(seID, attribs, conversationId, creationCategories, chatID, redundancy);
        elementsToCreate = (List<String>) result.get(0);
        chatID = (int) result.get(1);

        return null;
    }

    @Override
    public String seStop(String... seID) {
        return null;
    }

    @Override
    public Object execute(Object[] input) {
        System.out.println("El agente " + myAgent.getLocalName() + " esta en el metodo execute de su estado running");
        return null;
    }
}
