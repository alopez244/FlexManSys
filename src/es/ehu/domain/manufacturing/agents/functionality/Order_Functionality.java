package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.template.interfaces.AvailabilityFunctionality;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.IExecManagement;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class Order_Functionality extends DomApp_Functionality implements BasicFunctionality, AvailabilityFunctionality {

    private static final long serialVersionUID = 1L;
    private Agent myAgent;

    private List<String> elementsToCreate = new ArrayList<>();
    private int chatID = 0; // Numero incremental para crear conversationID

    private String firstState;
    private String redundancy;
    private String parentAgentID, orderNumber;
    private ArrayList<String> myReplicasID = new ArrayList<>();
    private ArrayList<AID> sonAgentID = new ArrayList<>();
    private Integer batchIndex = 1;
    private Boolean newBatch = true, firstTime = true;
    private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> batchTraceability = new ArrayList<>();
    private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> deserializedMessage = new ArrayList<>();
    private String mySeType;
    private MessageTemplate template, template2;

    @Override
    public Object getState() {
        return null;
    }

    @Override
    public void setState(Object state) {

    }

    @Override
    public Void init(MWAgent myAgent) {

        this.template = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("Information")),MessageTemplate.MatchConversationId("ItemsInfo"));
        this.template2 = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId("Shutdown"));
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

            Object[] result = processACLMessages(myAgent, mySeType, elementsToCreate, conversationId, redundancy, parentAgentID);
            sonAgentID = (ArrayList<AID>) result[1];
            myReplicasID = (ArrayList<String>) result[0];

        } else {
            // Si su estado es tracking
            trackingOnBoot(myAgent, mySeType, conversationId);

            myAgent.initTransition = ControlBehaviour.TRACKING;
        }

        return null;
    }

    // TODO quitamos el atributo attribs y lo creamos dentro del metodo seStart ???
    public String seStart(String seID, Hashtable<String, String> attribs, String conversationId) {

        ArrayList<String> creationCategories = new ArrayList<>();
        creationCategories.add("batch");  // Aqui decidiremos que tipos de elementos queremos crear --> Order, Batch, las dos...

        List result = seStart(myAgent, seID, attribs, conversationId, creationCategories, chatID, redundancy);
        elementsToCreate = (List<String>) result.get(0);
        chatID = (int) result.get(1);

        return null;
    }

    public String seStop(String... seID) {
        return null;
    }

    @Override
    public Object execute(Object[] input) {

        System.out.println("El agente " + myAgent.getLocalName() + " esta en el metodo execute de su estado running");

        ACLMessage msg = myAgent.receive(template);
        if (msg != null) {
            if (firstTime) {
                deserializedMessage = deserializeMsg(msg.getContent());
                batchTraceability = addNewLevel(batchTraceability, deserializedMessage, true); //añade el espacio para la informacion de la orden en primera posicion, sumando un nivel mas a los datos anteriores
                batchTraceability.get(0).get(0).get(0).add("OrderLevel"); // en ese espacio creado, se añade la informacion del order
                batchTraceability.get(0).get(0).get(2).add("orderReference");
                String batchNumber = batchTraceability.get(1).get(0).get(3).get(0);
                orderNumber = batchNumber.substring(0,2);
                batchTraceability.get(0).get(0).get(3).add(orderNumber);
                firstTime = false;
            } else {
                if (newBatch == false) {
                    for (int i = batchTraceability.size() - 1; i >= batchIndex; i--) {
                        batchTraceability.remove(i); //se elimina el ultimo batch añadido para poder sobreescribirlo
                    }
                }
                deserializedMessage = deserializeMsg(msg.getContent());
                batchTraceability = addNewLevel(batchTraceability, deserializedMessage,false);
            }
            newBatch = false; // hasta que el batch añadido no se complete, cada vez que se reciba un mensaje el dato se sobrescribira
        }
        ACLMessage msg2 = myAgent.receive(template2);
        // Recepcion de mensajes para eliminar de la lista de agentes hijo los agentes batch que ya han enviado toda la informacion
        if (msg2 != null) {
            AID sender = msg2.getSender();
            if (msg2.getContent().equals("Batch completed")){
                String msgSender = msg2.getOntology();
                for (int i = 0; i < sonAgentID.size(); i++) {
                    if (sonAgentID.get(i).getName().split("@")[0].equals(msgSender)) {
                        sonAgentID.remove(i);
                    }
                }
                String aux = "";
                String msgToMPLan = "";
                for (ArrayList<ArrayList<ArrayList<String>>>a : batchTraceability) { //serializacion de los datos a enviar
                    aux = a.toString();
                    msgToMPLan = msgToMPLan.concat(aux);
                }
                AID Agent = new AID(parentAgentID, false);
                sendACLMessage(7, Agent, "Information", "OrderInfo", msgToMPLan, myAgent);

                if (sonAgentID.size() == 0) { // todos los batch agent de los que es padre ya le han enviado la informacion
                    sendACLMessage(7, myAgent.getAID(), "Information", "Shutdown", "Shutdown", myAgent); // autoenvio de mensaje para asegurar que el agente de desregistre y se apague
                    return true;
                }
                batchIndex = batchTraceability.size() - 1;
                newBatch = true;
            }
        }
        return false;
    }

    @Override
    public Void terminate(MWAgent myAgent) {
        this.myAgent = myAgent;
        String parentName = "";

        try {
            ACLMessage reply = sendCommand(myAgent, "get * reference=" + orderNumber, "parentAgentID");
            //returns the names of all the agents that are sons
            if (reply != null)   // Si no existe el id en el registro devuelve error
                parentName = reply.getContent(); //gets the name of the agent´s parent
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            AID Agent = new AID(parentAgentID, false);
            sendACLMessage(7, Agent, myAgent.getLocalName(), "Shutdown", "Order completed", myAgent); // Informa al Mplan Agent que ya ha finalizado su tarea
            myAgent.deregisterAgent(parentName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
