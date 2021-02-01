package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.template.interfaces.AvailabilityFunctionality;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.utilities.XMLReader;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.*;

public class Batch_Functionality extends DomApp_Functionality implements BasicFunctionality, AvailabilityFunctionality {

    private static final long serialVersionUID = 1L;
    private Agent myAgent;

    private ArrayList<ArrayList<ArrayList<String>>> productInfo;
    private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> productsTraceability = new ArrayList<>();
    private HashMap<String, String> machinesForOperations = new HashMap<>();
    private Integer numOfItems;

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

        String conversationId = myAgent.getLocalName() + "_" + chatID++;

        firstState = getArgumentOfAgent(myAgent, "firstState");
        redundancy = getArgumentOfAgent(myAgent, "redundancy");
        parentAgentID = getArgumentOfAgent(myAgent, "parentAgent");
        mySeType = getMySeType(myAgent, conversationId);

        // Hay que leer el modelo de trazabilidad que estara en alguna carpeta
        // Tanto el running como los tracking tendran que leerlo
        // Cuando tenga la informacion la guardara en productsTraceability

        if(firstState.equals("running")) {

            // Cambiar a estado bootToRunning para que los tracking le puedan enviar mensajes
            String query = "set " + myAgent.getLocalName() + " state=bootToRunning";
            try {
                sendCommand(myAgent, query, conversationId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            myReplicasID = processACLMessages(myAgent, mySeType, new ArrayList<>(), conversationId, redundancy, parentAgentID);

            // TODO esta comentado ya que peta al estar en el init --> La ejecucion sigue adelante antes de recoger todos los mensajes y despues da problemas
            // sendPlan method of interface ITraceability
            createPlan(myAgent, conversationId);


        } else {
            // Si su estado es tracking
            trackingOnBoot(myAgent, mySeType, conversationId);

            myAgent.initTransition = ControlBehaviour.TRACKING;
        }


        return null;
    }

    @Override
    public Object execute(Object[] input) {
        System.out.println("El agente " + myAgent.getLocalName() + " esta en el metodo execute de su estado running");

        System.out.println("Ahora el agente " + myAgent.getLocalName() + " se va a quedar a la espera de la informacion de las operaciones");

        ACLMessage msg = myAgent.receive();
        if (msg != null) {
            if (msg.getPerformative() == ACLMessage.INFORM) {
                System.out.println("Mensaje con la informacion del PLC");
                System.out.println("Quien envia el mensaje: " + msg.getSender());
                System.out.println("Contenido: " + msg.getContent());
                System.out.println("ConversationId: " + msg.getConversationId());
            }
        }


        return null;
    }

    //====================================================================
    //ITRACEABILITY INTERFACE
    //====================================================================

    private void createPlan(MWAgent myAgent, String conversationId) {

        // Conseguir la referencia del producto
        String productID = getProductID(myAgent.getLocalName(), conversationId);
        System.out.println("La referencia de producto del batch del agente " + myAgent.getLocalName() + " es: " + productID);

        // Conseguimos toda la informacion del producto utilizando su ID
        productInfo = getProductInfo(productID);
        System.out.println("ID del producto asociado al agente " + myAgent.getLocalName() + ": " + productInfo.get(0).get(3).get(1) + " - " + productID);

        // Conseguir la cantidad de productos
        numOfItems = getNumOfItems(myAgent.getLocalName(), conversationId);

        // Ahora podremos proceder a conseguir la trazabilidad de los productos
        getProductsTraceability();

    }

    private void readPlan() {
        // TODO
    }

    //====================================================================




    private String getProductID(String seID, String conversationId) {
        String productID = null;
        String query = "get " + seID + " attrib=parent";
        ACLMessage reply = null;

        try {
            reply = sendCommand(myAgent, query, conversationId);
            // ID del batch con el cual el agente está relacionado
            // Ya que es este objeto el que tiene la referencia del producto
            String batchID = null;
            if (reply != null) {
                batchID = reply.getContent();
                query = "get " + batchID + " attrib=refProductID";
                reply = sendCommand(myAgent, query, conversationId);
                if (reply != null)
                    productID = reply.getContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productID;
    }

    private Integer getNumOfItems(String seID, String conversationId) {
        Integer numOfItems = null;
        String query = "get " + seID + " attrib=parent";
        ACLMessage reply = null;

        try {
            reply = sendCommand(myAgent, query, conversationId);
            // ID del batch con el cual el agente está relacionado
            // Ya que es este objeto el que tiene la cantidad de productos
            String batchID = null;
            if (reply != null) {
                batchID = reply.getContent();
                query = "get " + batchID + " attrib=numberOfItems";
                reply = sendCommand(myAgent, query, conversationId);
                if (reply != null)
                    numOfItems = reply.getContent().split(",").length;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numOfItems;
    }

    private HashMap<String,String> getNegotiationWinners() {

        HashMap<String,String> operationsWithMachines = new HashMap<>();

        while (!machinesForOperations.isEmpty()) {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                // TODO COMPROBAR TAMBIEN LOS TRACKING si esta bien programado (sin probar)
                if ((msg.getPerformative() == ACLMessage.INFORM) && (msg.getContent().contains("I am the winner"))) {

                    String operationID = msg.getContent().split(":")[1];
                    // Con la ID de la operacion lo borramos de la lista que teniamos, y añadimos a la nueva lista la maquina que se le ha asociado
                    machinesForOperations.remove(operationID);
                    operationsWithMachines.put(operationID, msg.getSender().getLocalName());

                }
            }
        }

        // Si se han borrado todas las operaciones es que ya tenemos todas las maquinas asociadas a alguna operacion
        System.out.println("Todas las operaciones tienen asociada una maquina");

        Iterator it =operationsWithMachines.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println("Operation " + pair.getKey() + " is for machine " + pair.getValue());
        }

        // Ahora podremos proceder a conseguir la trazabilidad de los productos
        getProductsTraceability();

        return operationsWithMachines;

    }

    private ArrayList<ArrayList<ArrayList<String>>> getProductInfo(String productID) {

        ArrayList<ArrayList<ArrayList<String>>> allProductInfo = null;

        String productsURL = "classes/resources/ProductInstances";
        //String path = getClass().getResource(productsURL).getPath();
        XMLReader fileReader = new XMLReader();
        ArrayList<ArrayList<ArrayList<String>>> xmlelements = null;
        File directory = new File(productsURL);

        if (directory.isDirectory()) {
            // Recorremos toda la carpeta
            for (File productXML: directory.listFiles()) {
                // Miramos solo los archivos tipo XML
                if (FilenameUtils.getExtension(productXML.getPath()).equals("xml")) {
                    xmlelements = fileReader.readFile(productXML.getPath());
                    String idValue = null;
                    // Buscamos el atributo id para conseguir su valor
                    for (int i = 0; i < xmlelements.get(0).get(2).size(); i++) {
                        if (xmlelements.get(0).get(2).get(i).equals("productType"))
                            idValue = xmlelements.get(0).get(3).get(i);
                    }
                    // Si el id coincide, es el producto que buscamos
                    if (idValue.equals(productID))
                        allProductInfo = xmlelements;
                }
            }
        }

        return allProductInfo;
    }

    private HashMap<String, String> getMachines(String localName, ArrayList<ArrayList<ArrayList<String>>> productInfo, String conversationId) {

        // Teniendo toda la informacion del producto vamos a conseguir las maquinas que vayan a realizar todas las operaciones
        // Por cada operacion que exista en el producto, vamos a buscar las maquinas que puedan realizar esa operacion
        // y guardaremos esa informacion en el hashmap --> <"S_01", "machine1, machine3">
        HashMap<String, String> machinesForOperations = new HashMap<>();

        String operationID = null;
        String operationType = null;
        for (int i=0; i < productInfo.size(); i++) {
            // Solo analizaremos cuando el atributo contenga la palabra operation
            if (productInfo.get(i).get(0).get(0).contains("_operation")) {
                operationType = productInfo.get(i).get(0).get(0);
                // Teniendo el tipo de operacion conseguiremos su valor
                for (int j=0; j < productInfo.get(i).get(2).size(); j++) {
                    if (productInfo.get(i).get(2).get(j).equals("id"))
                        operationID = productInfo.get(i).get(3).get(j);
                }
                System.out.println("\t\tOPERACION --> " + operationType + " y el ID:" + operationID);

                // Ahora, primero conseguiremos todas las maquinas (sus ID)
                String getAllMachines = "get * category=machine";
                ACLMessage reply = null;
                try {
                    reply = sendCommand(myAgent, getAllMachines, conversationId);
                    if (reply != null) {
                        String allMachines = reply.getContent();
                        System.out.println("ALL MACHINES: " + allMachines);
                        List<String> items = new ArrayList<>();
                        String[] aux = allMachines.split(",");
                        // Ahora por cada maquina miraremos si contiene nuestra operacion
                        for (String machineID:aux) {
                            String simpleQuery = "get " + machineID + " attrib=simpleOperations";
                            String complexQuery = "get " + machineID + " attrib=complexOperations";
                            // Dependiendo del tipo de operacion miraremos en un atributo o en otro
                            if (operationType.equals("simple_operation"))
                                reply = sendCommand(myAgent, simpleQuery, conversationId);
                            else
                                reply = sendCommand(myAgent, complexQuery, conversationId);
                            if (reply != null) {
                                System.out.println("All "+operationType+"s of " +machineID+ ": " +reply.getContent());
                                if (reply.getContent().contains(operationID)) {
                                    // Si todavia no se ha añadido ninguna maquina a esa operacion, la meteremos directamente
                                    if (machinesForOperations.get(operationID) == null)
                                        machinesForOperations.put(operationID, machineID);
                                    // En el caso de que tenga alguna maquina, sumaremos la nueva al final y la añadiremos
                                    else {
                                        String newMachine = machinesForOperations.get(operationID) + "," + machineID;
                                        machinesForOperations.put(operationID, newMachine);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        // Al final tendremos un HashMap con todas las operaciones, y las maquinas que las pueden realizar
        System.out.println("All operations and the machines of each operation of the batch " +myAgent.getLocalName() + " --> "+machinesForOperations);
        System.out.println();

        return machinesForOperations;

    }

    private void getProductsTraceability() {

        // Cogeremos como base la informacion del producto que ya hemos conseguido y añadiremos las nuevas variables
        ArrayList<ArrayList<ArrayList<String>>> aux = productInfo;
        for (int i=0; i < aux.size(); i++) {
            // Solo analizaremos cuando el atributo contenga la palabra operation
            if (aux.get(i).get(0).get(0).contains("_operation")) {
                aux.get(i).get(2).add("actualMachineId");
                aux.get(i).get(2).add("actualStationId");
                aux.get(i).get(2).add("startTime");
                aux.get(i).get(2).add("finishTime");

                aux.get(i).get(3).add("");
                aux.get(i).get(3).add("");
                aux.get(i).get(3).add("");
                aux.get(i).get(3).add("");
            }
        }

        // Ya que de momento no tenemos mas informacion, añadiremos todos los productos del lote a lista (de momento todos son iguales)
        for (int i = 0; i < numOfItems; i++) {
            productsTraceability.add(aux);
        }
        System.out.println("PRODUCT TRACEABILITY OF " + myAgent.getLocalName() + ":\n" + productsTraceability);
        System.out.println("\n");
    }

}
