package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.utilities.XMLReader;
import jade.Boot;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

public class DomApp_Functionality extends Dom_Functionality{

    /**
     * Class for DomainApplication_Functionality
     * Methods to Mplan, Order, Batch_Functionality
     */

    static final Logger LOGGER = LogManager.getLogger(DomApp_Functionality.class.getName());

    private Agent myAgent;

    //////////////////////////
    //  BOOT STATE METHODS
    //////////////////////////

    public Object[] processACLMessages(MWAgent agent, String seType, List<String> myElements, String conversationId, String redundancy, String parentAgentID) {

        this.myAgent = agent;
        ArrayList<String> replicasID = new ArrayList<>();
        String myParentID = null;
//        ArrayList<AID> senderAgentsID = new ArrayList<>();
        ArrayList<String> senderAgentsID = new ArrayList<>();
        try {
            ACLMessage reply = sendCommand(myAgent, "get " + myAgent.getLocalName() + " attrib=parent", conversationId);
            if (reply != null)
                myParentID = reply.getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }

        while ((!myElements.isEmpty()) || (replicasID.size() != Integer.parseInt(redundancy) - 1)) {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                // TODO COMPROBAR TAMBIEN LOS TRACKING si esta bien programado (sin probar)
                if ((msg.getPerformative() == ACLMessage.INFORM)) {
                    String senderParentID = null;
                    try {
                        ACLMessage reply = sendCommand(myAgent, "get " + msg.getSender().getLocalName() + " attrib=parent", conversationId);
                        if (reply != null)
                            senderParentID = reply.getContent();
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
                        senderAgentsID.add(msg.getSender().getLocalName()); //.getName().split("@")[0])
                    }
                }
            }
        }

        // Si la lista esta vacia, todos los elementos se han creado correctamente, y tendremos que pasar del estado BOOT al RUNNING
        // Pasar a estado running
        if (!parentAgentID.equals("sa"))
            sendElementCreatedMessage(myAgent, parentAgentID, seType, false);

        String query = "set " + myAgent.getLocalName() + " state=" + getArgumentOfAgent(agent, "firstState");
        try {
            ACLMessage reply = sendCommand(myAgent, query, conversationId);
            System.out.println(reply.getContent());

            // Ponemos el atributo execution_phase a started
            sendCommand(myAgent, "set " + myParentID + " execution_phase=started", conversationId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Como ya se ha creado todo el plan, avisará a las maquinas
        // Enviar a todas las maquinas que participan en el plan
        // Le va a pedir a todos sus batches que le den sus maquinas para tener todas y mandarles el mensaje???

        System.out.println("\tEl agente " + myAgent.getLocalName() + " ha finalizado su estado BOOT y pasará al estado RUNNING");
        agent.initTransition = ControlBehaviour.RUNNING;

        Object[] result = new Object[2];
        result[0] = replicasID;
        result[1] = senderAgentsID;
        return result;
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
            if (reply != null) {
                if(!reply.getContent().equals("")){ //si una replica en tracking es creada a posteriori de la ejecucion del plan no es necesario avisar a la replica en running
                    runningAgentID = reply.getContent();
                    sendElementCreatedMessage(myAgent, runningAgentID, seType, true);
                }
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

        System.out.println("\tEl agente " + myAgent.getLocalName() + " ha finalizado su estado BOOT y pasará al estado TRACKING");

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
     *
     * @param agent
     * @param seID
     * @param conversationId
     * @return
     */
    public List<String> getAllElements(Agent agent, String seID, String conversationId) {

        this.myAgent = agent;
        List<String> items = new ArrayList<>();

        // seID --> ID del Agent
        ACLMessage reply = null;
        Stack<String> elementsToAnalyze = new Stack<>();

        try {
            reply = sendCommand(myAgent, "get " + seID + " attrib=parent", conversationId);
            // ID del elemento con el cual el agente está relacionado (p.e: mplanagent1 --> mplan1)
            if (reply != null)   // Si no existe el id en el registro devuelve error
                elementsToAnalyze.push(reply.getContent()); // El primer elemento a analizar sera el padre del agente
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (!elementsToAnalyze.isEmpty()) {
            try {
                // Consigo el padre del agente
                String element = elementsToAnalyze.pop();

                String query = "get * parent=" + element; // Busco todos sus hijos
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
                            if (!category.contains("Agent")) {  // Filtro y me quedo con los que no sean del mismo tipo que el agente, asi filtro los agentes en tracking
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
        for (String elementID : allElementsID) {
            // Creamos los agentes para cada elemento
            try {

//                    reply = sendCommand(myAgent, "get (get * parent=(get * parent=" + elementID + " category=restrictionList)) attrib=attribValue", conversationId);
//                    String refServID = null;
//                    if (reply != null)
//                        refServID = reply.getContent();
//                    reply = sendCommand(myAgent, "get * category=pNodeAgent" + ((refServID.length() > 0) ? " refServID=" + refServID : ""), conversationId);
//
//                    if (reply != null) {
//                        targets = reply.getContent();
//                    }
//                if(myAgent.getLocalName().contains("mplan")){
//                    AID sa=new AID("sa",AID.ISLOCALNAME);
//                    sendACLMessage(7,sa,"rdy",conversationId,"ready",myAgent); //avisa al SA de que el agente esta a la espera del trigger de la negociación
//                }else if(myAgent.getLocalName().contains("order")){
//                    ACLMessage running_replica= sendCommand(myAgent, "get * state=bootToRunning category=mPlanAgent", conversationId);
//                    AID RR=new AID(running_replica.getContent(),AID.ISLOCALNAME);
//                    sendACLMessage(7,RR,"rdy",conversationId,"ready",myAgent); //avisa al SA de que el agente esta a la espera del trigger de la negociación
//                }
//                myAgent.blockingReceive(MessageTemplate.MatchOntology("trigger_negotiation"));

                    reply = sendCommand(myAgent, "get " + elementID + " attrib=category", conversationId);
                    String seCategory = null;
                    if (reply != null)
                        seCategory = reply.getContent();
                    String seClass = attribs.get("seClass");
                ACLMessage All_process_nodes = sendCommand(myAgent, "get * category=pNodeAgent", "GetAllNodes");
                String targets=All_process_nodes.getContent();
                for (int i = 0; i < Integer.parseInt(redundancy); i++) {
                // Orden de negociacion a todos los nodos
                //***********for antiguo

//                    =getTargets(elementID);
                    conversationId = myAgent.getLocalName() + "_" + chatID++;

                    //negotiate(myAgent, targets, "max mem", "start", elementID + "," + seCategory + "," + seClass + "," + ((i == 0) ? "running" : "tracking")+","+redundancy+","+myAgent.getLocalName(), conversationId);
                    String negotiationQuery = "localneg " + targets + " criterion=max mem action=start externaldata=" + elementID + "," + seCategory + "," + seClass + "," + ((i == 0) ? "running" : "tracking") + "," + redundancy + "," + myAgent.getLocalName();
                    sendCommand(myAgent, negotiationQuery, conversationId);
//                    myAgent.blockingReceive(MessageTemplate.MatchOntology("Negotiation_winner"));
                }

//                if(myAgent.getLocalName().contains("mplan")){
//                    ACLMessage running_replica=sendCommand(myAgent,"get * state=bootToRunning category=orderAgent" ,conversationId);
//                    if(running_replica!=null){
//                        AID ReplicaRunning=new AID(running_replica.getContent(), AID.ISLOCALNAME);
//                        myAgent.blockingReceive(MessageTemplate.MatchOntology("rdy"));
//                        sendACLMessage(7,ReplicaRunning,"trigger_negotiation",conversationId,"",myAgent);
//                    }else{
//                        LOGGER.error("Son agent did not receive negotiation trigger");
//                    }
//                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return chatID;
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
        return String.valueOf(seCategory.charAt(0)).toUpperCase() + seCategory.substring(1).replace("Agent", "");
    }

    public List seStart(Agent agent, String seID, Hashtable<String, String> attribs, String conversationId, ArrayList<String> creationCategories, int chatID, String redundancy) {

        this.myAgent = agent;
        List result = new ArrayList();
        ArrayList<String> elementsToCreate = new ArrayList<>();

        for (String creationCategory : creationCategories) {
            // Conseguimos los elementos de la categoria y su clase
            List elementsInfo = getElementsToCreate(myAgent, seID, creationCategory, conversationId);
            elementsToCreate.addAll((List<String>) elementsInfo.get(0));
            attribs.put("seClass", (String) elementsInfo.get(1));

            // Creamos los elementos
            chatID = createAllElementsAgents(myAgent, (List<String>) elementsInfo.get(0), attribs, conversationId, redundancy, chatID);
        }

        result.add(elementsToCreate);
        result.add(chatID);
        return result;
    }

    public List getElementsToCreate(Agent agent, String seID, String creationCategory, String conversationId) {

        this.myAgent = agent;
        List result = new ArrayList();
        List<String> elementsList = new ArrayList<>();
        String category = null;
        String categoryClass = null;

        List<String> allElements = getAllElements(myAgent, seID, conversationId);

        for (String elem : allElements) {//
            try {
                ACLMessage reply = sendCommand(myAgent, "get " + elem + " attrib=category", conversationId);
                if (reply != null)
                    category = reply.getContent();
                if (category.equals(creationCategory)) {
                    elementsList.add(elem);
                    categoryClass = String.valueOf(category.charAt(0)).toUpperCase() + category.substring(1) + "Agent";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        result.add(elementsList);
        result.add("es.ehu.domain.manufacturing.agents." + categoryClass);

        try {
            String command = "sestart " + myAgent.getLocalName();
            //for (String elem : (List<String>) result.get(0))
            for (int i = 0; i < elementsList.size(); i++)
                command = command + " element" + i + "=" + elementsList.get(i);

            ACLMessage reply = sendCommand(myAgent, command, conversationId);
            if (reply.getContent().equals("OK"))
                return result;
            else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //////////////////////////
    //  RUNNING STATE METHODS
    //////////////////////////


    //////////////////////////
    //  MULTI STATE METHODS
    //////////////////////////

    public String negotiate(Agent agent, String targets, String negotiationCriteria, String action, String externalData, String conversationId) {

        //Request de nueva negociación
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);

        for (String target : targets.split(","))
            msg.addReceiver(new AID(target, AID.ISLOCALNAME));
        msg.setConversationId(conversationId);
        msg.setOntology(es.ehu.platform.utilities.MasReconOntologies.ONT_NEGOTIATE);

        msg.setContent("negotiate " + targets + " criterion=" + negotiationCriteria + " action=" + action + " externaldata=" + externalData);
        agent.send(msg);

        return "Negotiation message sent";
    }

    //Metodo para añadir un nuevo nivel al registro de fabricacion de las piezas y ponerlo en primer lugar
    //Se utiliza para poder adecuar la estructura de datos antes de llamar a la funcion que crea el XML
    public ArrayList<ArrayList<ArrayList<ArrayList<String>>>> addNewLevel(ArrayList<ArrayList<ArrayList<ArrayList<String>>>> traceability, ArrayList<ArrayList<ArrayList<ArrayList<String>>>> deserializedMessage, Boolean addNewSpace) {

        ArrayList<ArrayList<ArrayList<String>>> newLevel = new ArrayList<>();
        int size = deserializedMessage.size();
        if (addNewSpace) { //solo se añadira el espacio para la nueva informacion si se activa el flag, si no unicamente se incrementaran los niveles
            //se añade el espacio del nuevo nivel para despues ser rellenado
            newLevel.add(new ArrayList<>());
            newLevel.get(0).add(0, new ArrayList<>());
            newLevel.get(0).add(1, new ArrayList<>());
            newLevel.get(0).add(2, new ArrayList<>());
            newLevel.get(0).add(3, new ArrayList<>());
            newLevel.get(0).get(1).add("1");
            traceability.add(newLevel);
        }
        //busca todos los datos que rerpresentan el nivel en el xml y los incrementa
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < deserializedMessage.get(i).size(); j++) {
                int index = Integer.parseInt(deserializedMessage.get(i).get(j).get(1).get(0));//convierte el valor a entero para poder ser incrementado
                index++;
                deserializedMessage.get(i).get(j).get(1).set(0, String.valueOf(index));//añade el valor modificado donde le corresponde
            }
            traceability.add(deserializedMessage.get(i));
        }

        return traceability;
    }
    //Metedo que deserializa los mensajes que se envian desde el BatchAgent al OrderAgent y desde el OrderAgent al MPlanAgent
    public ArrayList<ArrayList<ArrayList<ArrayList<String>>>> deserializeMsg(String msgContent) { //metodo que deserializa el mensage recibido desde el batch agent

        ArrayList<ArrayList<ArrayList<ArrayList<String>>>> traceability = new ArrayList<>();

        String letter = "", data = "";
        int index1 = 0, index2 = 0, index3 = 0, counter = 1;
        Boolean controlFlag = false;

        for (int i = 0; i < msgContent.length(); i++) {
            letter = Character.toString(msgContent.charAt(i));
            if (letter.equals("[")) {
                switch (counter) {
                    case 1:
                        traceability.add(new ArrayList<>());
                        counter++;
                        break;
                    case 2:
                        traceability.get(index1).add(new ArrayList<>());
                        counter++;
                        break;
                    case 3:
                        traceability.get(index1).get(index2).add(new ArrayList<>());
                        controlFlag = true;
                        break;
                }
            } else if (letter.equals("]")) {
                switch (counter) {
                    case 2:
                        counter--;
                        index2 = 0;
                        index1++;
                        break;
                    case 3:
                        if (controlFlag) {
                            traceability.get(index1).get(index2).get(index3).add(data);
                            data = ""; // una vez añadido el dato, se vacia la variable
                            index3++;
                            controlFlag = false;
                        } else {
                            counter--;
                            index3 = 0;
                            index2++;
                        }
                        break;
                }
            } else if (letter.equals(",")) {
                if (data.equals("")) {
                    i++;
                } else {
                    traceability.get(index1).get(index2).get(index3).add(data);
                    data = "";
                    i++;
                }
            } else {
                data = data.concat(letter);
            }
        }
        return traceability;
    }

    protected Date getactualtime(){
        String actualTime;
        int ano, mes, dia, hora, minutos, segundos;
        Calendar calendario = Calendar.getInstance();
        ano = calendario.get(Calendar.YEAR);
        mes = calendario.get(Calendar.MONTH) + 1;
        dia = calendario.get(Calendar.DAY_OF_MONTH);
        hora = calendario.get(Calendar.HOUR_OF_DAY);
        minutos = calendario.get(Calendar.MINUTE);
        segundos = calendario.get(Calendar.SECOND);
        actualTime = ano + "-" + mes + "-" + dia + "T" + hora + ":" + minutos + ":" + segundos;
        Date actualdate = null;
        try {
            actualdate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(actualTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return actualdate;
    }
    protected LocalDateTime convertToLocalDateTimeViaSqlTimestamp(Date dateToConvert) {
        return new java.sql.Timestamp(
                dateToConvert.getTime()).toLocalDateTime();
    }
    protected Date convertToDateViaSqlTimestamp(LocalDateTime dateToConvert) {
        return java.sql.Timestamp.valueOf(dateToConvert);
    }

    protected void KillReplicas(ArrayList<String> replicas){
        for(int i=0; i<replicas.size();i++){
            AID AgentID = new AID(replicas.get(i), false);
            sendACLMessage(16, AgentID, "control", "Shutdown", "setstate stop", myAgent);
            int  found =SearchAgent(replicas.get(i));
            while(found!=0){  //hay que esperar a que los tracking desaparezcan antes de desregistrar el agente running
                found =SearchAgent(replicas.get(i));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getTargets(String refServID) {
        String targets = "";
        ACLMessage All_process_nodes = null;
        try {
            All_process_nodes = sendCommand(myAgent, "get * category=pNodeAgent", "GetAllNodes");
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<String> ParticipatingPnodes = new ArrayList<>();
        if (All_process_nodes != null) {
            String[] ListOfAllPnodes = new String[1];
            if (All_process_nodes.getContent().contains(",")) {
                ListOfAllPnodes = All_process_nodes.getContent().split(",");
            } else {
                ListOfAllPnodes[0] = All_process_nodes.getContent();
            }
            try{

            for (int i = 0; i < ListOfAllPnodes.length; i++) {
                ACLMessage UsedPNodes = sendCommand(myAgent, "get " + ListOfAllPnodes[i] + " attrib=refServID", "CheckIfValidNode"); //todas los pnode.
                if (!UsedPNodes.getContent().contains(refServID)) {
                    ParticipatingPnodes.add(ListOfAllPnodes[i]);
                }
            }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(ParticipatingPnodes.size()==0){
                LOGGER.warn("There is no node available to host a replica. Some nodes may host multiple replicas.");
                return All_process_nodes.getContent(); //si no hay nodos disponibles que permita que se repita alguna replica en algun nodo
            }
            for (int i = 0; i < ParticipatingPnodes.size(); i++) {
                if (i == 0) {
                    targets = ParticipatingPnodes.get(i);
                } else {
                    targets = targets + "," + ParticipatingPnodes.get(i);
                }
            }
        }

            return targets;
    }

    private int SearchAgent (String agent){
        int found=0;
        AMSAgentDescription[] agents = null;

        try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults ( new Long(-1) );
            agents = AMSService.search(myAgent, new AMSAgentDescription (), c );
        }
        catch (Exception e) {
            System.out.println(e);
        }
        for (int i=0; i<agents.length;i++){
            AID agentID = agents[i].getName();
            String agent_to_check=agentID.getLocalName();
//            System.out.println(agent_to_check);
            if(agent_to_check.contains(agent)){
                found++;
            }
        }
        return found;
    }
    public void unregister_from_node(){ //desregistra del nodo el parent del agente

        try {
            ACLMessage parent= sendCommand(myAgent, "get "+myAgent.getLocalName()+" attrib=parent", myAgent.getLocalName()+"_Parent");
            ACLMessage myAgent_node=sendCommand(myAgent, "get "+myAgent.getLocalName()+" attrib=node", myAgent.getLocalName()+"_PNodeNumber");
            String hosting_node="pnodeagent"+myAgent_node.getContent();
            ACLMessage hosted_elements=sendCommand(myAgent, "get "+hosting_node+" attrib=refServID", myAgent.getLocalName()+"_HENode");
            String[] HE=new String[1];
            if(hosted_elements.getContent().contains(",")){
                HE=hosted_elements.getContent().split(",");
            }else{
                HE[0]=hosted_elements.getContent();
            }
            String new_HE="";
            ArrayList<String> updated_hosted_elements=new ArrayList<String>();
            for(int i=0;i<HE.length;i++){
                updated_hosted_elements.add(HE[i]);
            }
            for(int i=0;i< updated_hosted_elements.size();i++){
                if(updated_hosted_elements.get(i).contains(parent.getContent())){
                    updated_hosted_elements.remove(i);
                }else{
                    if(i==0){
                        new_HE=updated_hosted_elements.get(i);
                    }else{
                        new_HE=new_HE+","+updated_hosted_elements.get(i);
                    }
                }
            }
            sendCommand(myAgent, "set "+hosting_node+" refServID="+new_HE, myAgent.getLocalName()+"_EraseHostedElements");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
