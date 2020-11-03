package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.template.interfaces.AvailabilityFunctionality;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.utilities.XMLReader;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
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
    private String numOfItems;

    private int chatID = 0; // Numero incremental para crear conversationID

    private boolean moreMsg = true;

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
            sendPlan(myAgent, conversationId);


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
        return null;
    }

    //====================================================================
    //ITRACEABILITY INTERFACE
    //====================================================================

    private void sendPlan(MWAgent myAgent, String conversationId) {

        // Conseguir la referencia del producto
        String productID = getProductID(myAgent.getLocalName(), conversationId);
        System.out.println("La referencia de producto del batch del agente " + myAgent.getLocalName() + " es: " + productID);

        // Conseguimos toda la informacion del producto utilizando su ID
        productInfo = getProductInfo(productID);
        System.out.println("ID del producto asociado al agente " + myAgent.getLocalName() + ": " + productInfo.get(0).get(3).get(2) + " - " + productID);

        // Teniendo toda la informacion del producto vamos a conseguir las maquinas que vayan a realizar todas las operaciones
        machinesForOperations = getMachines(myAgent.getLocalName(), productInfo, conversationId);

        // Conseguir la cantidad de productos
        numOfItems = getNumOfItems(myAgent.getLocalName(), conversationId);

        // Por cada operacion vamos a negociar con todos sus maquinas para asignar a la mejor
        for (Map.Entry<String, String> entry : machinesForOperations.entrySet()) {
            String operationID = entry.getKey();
            String machinesID = entry.getValue();
            System.out.println(operationID + " - " + machinesID);

            negotiate(myAgent, machinesID, "lead time", "execute", myAgent.getLocalName() + "," + numOfItems + "," + operationID, conversationId);

        }

        // Comprobamos que todas las operaciones se han asociado a una maquina, y conseguimos dicha maquina para cada operacion
        HashMap<String,String> operationsWithMachines = getNegotiationWinners();
        for (Map.Entry<String, String> entry : operationsWithMachines.entrySet()) {
            String operationID = entry.getKey();
            String machineID = entry.getValue();
            System.out.println(operationID + " - " + machineID);
        }

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

    private String getNumOfItems(String seID, String conversationId) {
        String numOfItems = null;
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
                    numOfItems = reply.getContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numOfItems;
    }

    private HashMap<String,String> getNegotiationWinners() {

        HashMap<String,String> operationsWithMachines = new HashMap<>();

        // Le añadimos un comportamiento para recibir los mensajes de las maquinas
        myAgent.addBehaviour(new SimpleBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if(msg != null) {
                    if ((msg.getPerformative() == 7) && (msg.getContent().contains("I am the winner"))) {
                        String operationID = msg.getContent().split(":")[1];
                        // Con la ID de la operacion lo borramos de la lista que teniamos, y añadimos a la nueva lista la maquina que se le ha asociado
                        machinesForOperations.remove(operationID);
                        operationsWithMachines.put(operationID, msg.getSender().getLocalName());
                        // Si se han borrado todas las operaciones es que ya tenemos todas las maquinas asociadas a alguna operacion
                        if (machinesForOperations.isEmpty()) {
                            System.out.println("Todas las operaciones tienen asociada una maquina");
                            moreMsg = false;
                            // Ahora podremos proceder a conseguir la trazabilidad de los productos
                            getProductsTraceability(operationsWithMachines);
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

        return operationsWithMachines;

    }

    private ArrayList<ArrayList<ArrayList<String>>> getProductInfo(String productID) {

        ArrayList<ArrayList<ArrayList<String>>> allProductInfo = null;

        String productsURL = "/resources/ProductInstances";
        String path = getClass().getResource(productsURL).getPath();
        XMLReader fileReader = new XMLReader();
        ArrayList<ArrayList<ArrayList<String>>> xmlelements = null;
        File directory = new File(path);

        if (directory.isDirectory()) {
            // Recorremos toda la carpeta
            for (File productXML: directory.listFiles()) {
                // Miramos solo los archivos tipo XML
                if (FilenameUtils.getExtension(productXML.getPath()).equals("xml")) {
                    xmlelements = fileReader.readFile(productXML.getPath());
                    String idValue = null;
                    // Buscamos el atributo id para conseguir su valor
                    for (int i = 0; i < xmlelements.get(0).get(2).size(); i++) {
                        if (xmlelements.get(0).get(2).get(i).equals("id"))
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

    private void getProductsTraceability(HashMap<String,String> operationsWithMachines) {

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
                for (int j=0; j < aux.get(i).get(2).size(); j++) {
                    if (aux.get(i).get(2).get(j).equals("id"))
                        aux.get(i).get(3).add(operationsWithMachines.get(aux.get(i).get(3).get(j)));
                }
                aux.get(i).get(3).add("");
                aux.get(i).get(3).add("");
            }
        }

        // Ya que de momento no tenemos mas informacion, añadiremos todos los productos del lote a lista (de momento todos son iguales)
        for (int i = 0; i < Integer.parseInt(numOfItems); i++) {
            productsTraceability.add(aux);
        }
        System.out.println("PRODUCT TRACEABILITY OF " + myAgent.getLocalName() + ":\n" + productsTraceability);

    }

}
