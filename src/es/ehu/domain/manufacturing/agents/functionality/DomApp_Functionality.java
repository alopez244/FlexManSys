package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class DomApp_Functionality {

    /**
     * Class for DomainApplication_Functionality
     * Methods to Mplan, Order, Batch_Functionality
     */

    static final Logger LOGGER = LogManager.getLogger(DomApp_Functionality.class.getName()) ;

    private Agent myAgent;

    private static boolean moreMsg = true;

    //////////////////////////
    //  BOOT STATE METHODS
    //////////////////////////

    public ArrayList<String> processACLMessages(MWAgent agent, String seType, List<String> myElements, String conversationId, String redundancy, String parentAgentID, String... elementType) {

        this.myAgent = agent;
        ArrayList<String> replicasID = new ArrayList<>();

        if (myElements.isEmpty()) {
            // Si myElements esta vacio significa que es el elemento del ultimo nivel (p.e: Batch)
            if (Integer.parseInt(redundancy) == 1) {
                sendElementCreatedMessage(myAgent, parentAgentID, seType, false);

                // Cambiar el estado del elemento de BOOT a RUNNING
                String query = "set " + myAgent.getLocalName() + " state=" + getArgumentOfAgent(agent, "firstState");
                try {
                    sendCommand(myAgent, query, conversationId);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

        }

        moreMsg = true;

        myAgent.addBehaviour(new SimpleBehaviour() {
            @Override
            public void action() {

                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    // TODO COMPROBAR TAMBIEN LOS TRACKING si esta bien programado (sin probar)
                    if ((msg.getPerformative() == ACLMessage.INFORM)) {
                        if (msg.getContent().equals(seType + " replica created successfully")) {     // La replica sera del mismo tipo que el del agente
                            System.out.println("\tYa se ha creado la replica " + msg.getSender().getLocalName());
                            replicasID.add(msg.getSender().getLocalName());
                        }
                        // TODO Hacer un for para cada tipo de elemento? --> Porque elementType es String...
                         if (elementType != null) {
                            if (msg.getContent().equals(elementType[0] + " created successfully")) {    //ElementType puede contener mas de un atributo, de momento cogemos el primero
                                System.out.println("\tYa se ha creado el agente " + msg.getSender().getLocalName() + " - hay que borrarlo de la lista --> " + myElements);

                                // Primero vamos a conseguir el ID del elemento (ya que el mensaje nos lo envia su agente)
                                String senderID = null;
                                try {
                                    ACLMessage reply = sendCommand(myAgent, "get " + msg.getSender().getLocalName() + " attrib=parent", conversationId);
                                    if (reply != null)
                                        senderID = reply.getContent();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                // Si el elemento es uno de los hijos (que solo los hijos nos enviaran los mensaje, pero por se acaso), lo borramos de la lista
                                if (myElements.contains(senderID))
                                    myElements.remove(senderID);
                            }
                        }

                        // Si la lista esta vacia, todos los elementos se han creado correctamente, y tendremos que pasar del estado BOOT al RUNNING
                        if ((myElements.isEmpty()) && (replicasID.size() == Integer.parseInt(redundancy) - 1)) {
                            moreMsg = false;
                            // Pasar a estado running
                            System.out.println("\tEl agente " + myAgent.getLocalName() + " ha finalizado su estado BOOT y pasar� al estado RUNNING");

                            String query = "set " + myAgent.getLocalName() + " state=" + getArgumentOfAgent(agent, "firstState");
                            try {
                                ACLMessage reply = sendCommand(myAgent, query, conversationId);
                                System.out.println(reply.getContent());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (!parentAgentID.equals("sa"))
                                sendElementCreatedMessage(myAgent, parentAgentID, seType, false);
                        }

                    }
                } else {
                    if (moreMsg)
                        // Se queda a la espera para cuando le envien mas mensajes
                       block();
                }
            }

            @Override
            public boolean done() {
                return false;
            }
        });

        if(!moreMsg)
            return replicasID;
        return null;
    }

    public String getArgumentOfAgent(MWAgent agent, String argumentName) {

        Object[] allArguments = agent.getArguments();

        for (int i = 0; i < allArguments.length; i++) {
            String[] argument = allArguments[i].toString().split("=");
            if (argument[0].equals(argumentName))
                return argument[1];
        }
        return null;

    }

    public void sendElementCreatedMessage(Agent agent, String receiver, String seType, boolean isReplica) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        if (isReplica)
            msg.setContent(seType + " replica created successfully");
        else
            msg.setContent(seType + " created successfully");
        agent.send(msg);
    }

    public List<String> getAllElements(Agent agent, String seID, String myElementType, String conversationId){

        this.myAgent = agent;
        // myElementType --> Tipo del hijo (p.e. En el caso de MPlan seria order)

        // seID --> ID del Agent
        String parentQuery = "get " + seID + " attrib=parent";
        ACLMessage reply = null;

        // Get parent
        try {
            reply = sendCommand(myAgent, parentQuery, conversationId);
            // ID del plan con el cual el agente est� relacionado
            String planID = null;
            if (reply != null)   // Si no existe el id en el registro devuelve error
                planID = reply.getContent();

            String query = "get " + myElementType + "* parent="+ planID;  // Busco todos los elementos de los que es parent (no se buscan todos los elementos porque pueden existir otros en tracking)
            reply = sendCommand(myAgent, query, conversationId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String allElements = null;
        if (reply != null)
            allElements = reply.getContent();

        List<String> items = new ArrayList<>();
        String[] aux = allElements.split(",");
        for (String a:aux)
            items.add(a);

        return items;

    }

    public int createAllElementsAgents(Agent seAgent, List<String> allElementsID, Hashtable<String, String> attribs, String conversationId, String redundancy, int chatID) {

        this.myAgent = seAgent;

        ACLMessage reply = null;
        for (String elementID: allElementsID) {
            // Creamos los agentes para cada elemento
            try {
                reply = sendCommand(myAgent, "get (get * parent=(get * parent=" + elementID + " category=restrictionList)) attrib=attribValue", conversationId);
                String refServID = null;
                if (reply != null)
                    refServID = reply.getContent();

                reply = sendCommand(myAgent, "get * category=pNodeAgent" + ((refServID.length()>0)?" refServID=" + refServID:""), conversationId);
                String targets = null;
                if (reply != null) {
                    targets = reply.getContent();
                }

                reply = sendCommand(myAgent, "get " + elementID + " attrib=category", conversationId);
                String seCategory = null;
                if (reply != null)
                    seCategory = reply.getContent();
                String seClass = attribs.get("seClass");

                // Orden de negociacion a todos los nodos
                for (int i=0; i<Integer.parseInt(redundancy); i++) {

                    conversationId = myAgent.getLocalName() + "_" + chatID++;

                    negotiate(myAgent, targets, "max mem", "start", elementID + "," + seCategory + "," + seClass + "," + ((i == 0) ? "running" : "tracking")+","+redundancy+","+myAgent.getLocalName(), conversationId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return chatID;
    }

    public void trackingOnBoot(MWAgent agent, String seType, String conversationId) {

        this.myAgent = agent;

        String runningAgentID = null;
        String seCategory = null;
        try {
            String parentID = null;
            ACLMessage reply = sendCommand(myAgent, "get " + myAgent.getLocalName() + " attrib=parent", conversationId);
            if (reply != null)
                parentID = reply.getContent();
            reply = sendCommand(myAgent, "get " + myAgent.getLocalName() + " attrib=category", conversationId);
            if (reply != null)
                seCategory = reply.getContent();

            reply = sendCommand(myAgent, "get * parent=" + parentID + " category=" + seCategory + " state=bootToRunning", conversationId);
            if (reply !=null) {
                runningAgentID = reply.getContent();
                sendElementCreatedMessage(myAgent, runningAgentID, seType, true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Una vez mande el mensaje, registra que su estado es el tracking
        try {
            sendCommand(myAgent, "set " + myAgent.getLocalName() + " state=" + getArgumentOfAgent(agent, "firstState"), conversationId);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getMySeType(MWAgent agent, String conversationId) {

        this.myAgent = agent;

        String seCategory = null;
        ACLMessage reply = null;
        try {
            reply = sendCommand(myAgent, "get " + myAgent.getLocalName() + " attrib=category", conversationId);
            if (reply != null)
                seCategory = reply.getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(seCategory.charAt(0)).toUpperCase() + seCategory.substring(1).replace("Agent","");
    }

    //////////////////////////
    //  RUNNING STATE METHODS
    //////////////////////////


    //////////////////////////
    //  MULTI STATE METHODS
    //////////////////////////

    public ACLMessage sendCommand(Agent agent, String cmd, String conversationId) throws Exception {

        this.myAgent = agent;

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

    public String negotiate(Agent agent, String targets, String negotiationCriteria, String action, String externalData, String conversationId) {

        //Request de nueva negociaci�n
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);

        for (String target: targets.split(","))
            msg.addReceiver(new AID(target, AID.ISLOCALNAME));
        msg.setConversationId(conversationId);
        msg.setOntology(es.ehu.platform.utilities.MasReconOntologies.ONT_NEGOTIATE );

        msg.setContent("negotiate " +targets+ " criterion=" +negotiationCriteria+ " action=" +action+ " externaldata=" +externalData);
        agent.send(msg);

        return "Negotiation message sent";
    }

}
