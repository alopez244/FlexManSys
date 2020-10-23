package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.ControlBehaviour;
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

import java.util.*;

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

    public ArrayList<String> processACLMessages(MWAgent agent, String seType, List<String> myElements, String conversationId, String redundancy, String parentAgentID) {

        this.myAgent = agent;
        ArrayList<String> replicasID = new ArrayList<>();

        if ((myElements.isEmpty()) && (Integer.parseInt(redundancy) == 1)) {
            // Si myElements esta vacio significa que es el elemento del ultimo nivel (p.e: Batch)

            // Cambiar el estado del elemento de BOOT a RUNNING
            String query = "set " + myAgent.getLocalName() + " state=" + getArgumentOfAgent(agent, "firstState");
            try {
                sendCommand(myAgent, query, conversationId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            sendElementCreatedMessage(myAgent, parentAgentID, seType, false);

            System.out.println("\tEl agente " + myAgent.getLocalName() + " ha finalizado su estado BOOT y pasar� al estado RUNNING");
            agent.initTransition = ControlBehaviour.RUNNING;

            return null;
        }

        moreMsg = true;

        myAgent.addBehaviour(new SimpleBehaviour() {
            @Override
            public void action() {

                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    // TODO COMPROBAR TAMBIEN LOS TRACKING si esta bien programado (sin probar)
                    if ((msg.getPerformative() == ACLMessage.INFORM)) {
                        String senderParentID = null;
                        String myParentID = null;
                        try {
                            ACLMessage reply = sendCommand(myAgent, "get " + msg.getSender().getLocalName() + " attrib=parent", conversationId);
                            if (reply != null)
                                senderParentID = reply.getContent();
                            reply = sendCommand(myAgent, "get " + myAgent.getLocalName() + " attrib=parent", conversationId);
                            if (reply != null)
                                myParentID = reply.getContent();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (myParentID.equals(senderParentID)) {
                            // Si el padre del emisor y el receptor son iguales, el que le envia el mensaje es una replica
                            System.out.println("\tYa se ha creado la replica " + msg.getSender().getLocalName());
                            replicasID.add(msg.getSender().getLocalName());
                        } else {
                            // Si los padres son diferentes, se trata de un hijo
                            if (myElements.contains(senderParentID))
                                myElements.remove(senderParentID);
                        }

                        // Si la lista esta vacia, todos los elementos se han creado correctamente, y tendremos que pasar del estado BOOT al RUNNING
                        if ((myElements.isEmpty()) && (replicasID.size() == Integer.parseInt(redundancy) - 1)) {
                            moreMsg = false;
                            // Pasar a estado running
                            String query = "set " + myAgent.getLocalName() + " state=" + getArgumentOfAgent(agent, "firstState");
                            try {
                                ACLMessage reply = sendCommand(myAgent, query, conversationId);
                                System.out.println(reply.getContent());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (!parentAgentID.equals("sa"))
                                sendElementCreatedMessage(myAgent, parentAgentID, seType, false);

                            System.out.println("\tEl agente " + myAgent.getLocalName() + " ha finalizado su estado BOOT y pasar� al estado RUNNING");
                            agent.initTransition = ControlBehaviour.RUNNING;
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

        return replicasID;
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

    /**
     * Metodo para conseguir todos mis elementos (p.e. si es un MPlan todos los orders y batch asociados a ese MPlan)
     * @param agent
     * @param seID
     * @param conversationId
     * @return
     */
    public List<String> getAllElements(Agent agent, String seID, String conversationId){

        this.myAgent = agent;
        List<String> items = new ArrayList<>();
        // myElementType --> Tipo del hijo (p.e. En el caso de MPlan seria order)

        // seID --> ID del Agent
        String parentQuery = "get " + seID + " attrib=parent";
        ACLMessage reply = null;
        String seCategory = null;
        Stack<String> elementsToAnalyze = new Stack<>();
        elementsToAnalyze.push(seID);

        // Consigo la categoria del agente
        try {
            reply = sendCommand(myAgent, "get " + myAgent.getLocalName() + " attrib=category", conversationId);
            if (reply != null)
                seCategory = reply.getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }

        while(!elementsToAnalyze.isEmpty()) {
            try {
                // Consigo el padre del agente
                String element = elementsToAnalyze.pop();
                //reply = sendCommand(myAgent, parentQuery, conversationId);
                String query = "get " + element + " attrib=category";
                reply = sendCommand(myAgent, query, conversationId);
                String category2 = reply.getContent();
                if (category2.equals(seCategory)) {
                    reply = sendCommand(myAgent, "get " + element + " attrib=parent", conversationId);
                    // ID del elemento con el cual el agente est� relacionado (p.e: mplanagent1 --> mplan1)
                    if (reply != null)   // Si no existe el id en el registro devuelve error
                        element = reply.getContent();   // Sobreescribo el elemento ya que el agente no tiene los hijos, los tiene el elemento con el que esta asociado
                }

                query = "get * parent=" + element; // Busco todos sus hijos
                reply = sendCommand(myAgent, query, conversationId);

                String allElements = null;
                if (!reply.getContent().equals("")) {
                    allElements = reply.getContent();
                    String[] aux = allElements.split(",");
                    for (String elem : aux) {
                        query = "get " + elem + " attrib=category";
                        reply = sendCommand(myAgent, query, conversationId);
                        if (reply != null) {
                            String category = reply.getContent();
                            if ((!category.equals(seCategory)) && (!category.contains("Agent"))) {  // Filtro y me quedo con los que no sean del mismo tipo que el agente, asi filtro los agentes en tracking
                                items.add(elem);
                                elementsToAnalyze.add(elem);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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

        System.out.println("\tEl agente " + myAgent.getLocalName() + " ha finalizado su estado BOOT y pasar� al estado TRACKING");

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

    public HashMap<String,String> getMyElementsClasses(Agent agent, List<String> allElements) {

        this.myAgent = agent;
        HashMap<String,String> result = new HashMap<>();
        String category = null;
        String categoryClass = null;

        for(String elem: allElements) {
            try {
                ACLMessage reply = sendCommand(myAgent, "get " + elem + " attrib=category", "");
                if (reply != null)
                    category = reply.getContent();
                categoryClass = String.valueOf(category.charAt(0)).toUpperCase() + category.substring(1) + "Agent";
                if (result.get(category) == null)
                    result.put(category, "es.ehu.domain.manufacturing.agents." + categoryClass);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return result;
    }

    public List<String> getELementsToCreate(Agent agent, List<String> allElements, String creationCategory) {

        this.myAgent = agent;
        List<String> result = new ArrayList<>();
        String category = null;

        for (String elem: allElements) {
            try {
                ACLMessage reply = sendCommand(myAgent, "get " + elem + " attrib=category", "");
                if (reply != null)
                    category = reply.getContent();
                if (category.equals(creationCategory))
                    result.add(elem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

}
