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

    private List<String> myElements;
    private List<String> elementsToCreate = new ArrayList<>();
    private HashMap<String, String> elementsClasses;
    private int chatID = 0; // Numero incremental para crear conversationID

    private String firstState;
    private String redundancy;
    private String parentAgentID;
    private ArrayList<AID> sonAgentID = new ArrayList<>();
    private ArrayList<String> myReplicasID = new ArrayList<>();
    private String batchNumber;
    private String serviceTimeStamp;
    private ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<String>>>>> batchTraceability = new ArrayList<>();
    private String mySeType;
    private Bundle result;
    private MessageTemplate template;

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

            result = processACLMessages(myAgent, mySeType, elementsToCreate, conversationId, redundancy, parentAgentID);
            sonAgentID = result.senderAgentsID;
            myReplicasID = result.replicasID;

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

            AID msgSender = msg.getSender();
            batchTraceability.add(deserializeMsg(msg.getContent()));

            for (int i = 0; i < sonAgentID.size(); i++) {
                if (sonAgentID.get(i).getName() == msgSender.getName()) {
                    sonAgentID.remove(i);
                }
            }
            if (sonAgentID.size() == 0) { // todos los batch agent de los que es padre ya le han enviado la informacion
                String aux = "";
                String msgToMPLan = "";

                for (ArrayList<ArrayList<ArrayList<ArrayList<String>>>>a : batchTraceability) { //serializacion de los datos a enviar
                    aux = a.toString();
                    msgToMPLan = msgToMPLan.concat(aux);
                }

                ACLMessage msgToPLC = new ACLMessage(ACLMessage.INFORM); //envio del mensaje
                AID Agent = new AID(parentAgentID, false);
                msgToPLC.addReceiver(Agent);
                msgToPLC.setOntology("Information");
                msgToPLC.setConversationId("BatchInfo");
                msgToPLC.setContent(msgToMPLan);
                myAgent.send(msgToPLC);
                System.out.println(msgToMPLan);
                return true;
            }
        }
        return false;
    }

    public ArrayList<ArrayList<ArrayList<ArrayList<String>>>> deserializeMsg(String msgContent) { //metodo que deserializa el mensage recibido desde el batch agent

        ArrayList<ArrayList<ArrayList<ArrayList<String>>>> productsTraceability = new ArrayList<>();
        String letter = "";
        String data = "";
        int index1 = 1;
        int index2 = 0;
        int index3 = 0;

        for (int i = 0; i < msgContent.length(); i++) {
            letter = Character.toString(msgContent.charAt(i));
            if (letter.equals("[")) {
                productsTraceability.add(new ArrayList<>());
                for (int j = i + 1; j < msgContent.length(); j++) {
                    letter = Character.toString(msgContent.charAt(j));
                    if (letter.equals("[")) {
                        productsTraceability.get(index1).add(new ArrayList<>());
                        for (int k = j + 1; k < msgContent.length(); k++) {
                            letter = Character.toString(msgContent.charAt(k));
                            if (letter.equals("[")) {
                                productsTraceability.get(index1).get(index2).add(new ArrayList<>());
                                for (int l = k + 1; l < msgContent.length(); l++) {
                                    letter = Character.toString(msgContent.charAt(l));
                                    if (letter.equals("]")) {
                                        productsTraceability.get(index1).get(index2).get(index3).add(data);
                                        data = "";
                                        index3++;
                                        k = l;
                                        break;
                                    } else if (letter.equals(",")) {
                                        productsTraceability.get(index1).get(index2).get(index3).add(data);
                                        data = "";
                                        l++;
                                    } else {
                                        data = data.concat(letter);
                                    }
                                }
                            } else if (letter.equals(",")) { //cuando la variable letter es igual a una coma, el siguiente caracter siempre sera un espacio, por lo que se salta
                                k++;
                            } else if (letter.equals("]")) {
                                index2++;
                                index3 = 0;
                                j = k;
                                break;
                            }
                        }
                    } else if (letter.equals(",")) {
                        j++;
                    } else if (letter.equals("]")) {
                        index1++;
                        index2 = 0;
                        i = j;
                        break;
                    }
                }
            } else if (letter.equals(",")) {
                productsTraceability.add(new ArrayList<>());
                productsTraceability.get(0).add(new ArrayList<>());
                productsTraceability.get(0).get(0).add(new ArrayList<>());
                productsTraceability.get(0).get(0).get(0).add("Batch ID");
                productsTraceability.get(0).get(0).get(0).add("Data_Service_Time_Stamp");
                batchNumber = data;
                productsTraceability.get(0).get(0).add(new ArrayList<>());
                productsTraceability.get(0).get(0).get(1).add(data);
                data = "";
            } else if (letter.equals(".")) {
                serviceTimeStamp = data;
                productsTraceability.get(0).get(0).get(1).add(data);
                data = "";
            } else {
                data = data.concat(letter);
            }
        }

        return productsTraceability;
    }

}
