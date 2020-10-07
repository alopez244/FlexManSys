package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.utilities.XMLReader;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.*;

public class Batch_Functionality implements BasicFunctionality {

    private static final long serialVersionUID = 1L;
    private Agent myAgent;

    private String parentAgentID;
    private ArrayList<ArrayList<ArrayList<String>>> productInfo;
    private HashMap<String, String> machinesForOperations = new HashMap<>();
    private int chatID = 0; // Numero incremental para crear conversationID

    @Override
    public Void init(MWAgent myAgent) {

        this.myAgent = myAgent;


        // Envio un mensaje a mi parent diciendole que me he creado correctamente
        parentAgentID = getParentAgentID(myAgent.getLocalName());
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(parentAgentID, AID.ISLOCALNAME));
        msg.setContent("Batch created successfully");
        myAgent.send(msg);


        // sendPlan method of interface ITraceability
        sendPlan(myAgent);

        // Cambiar el estado del batch de BOOT a RUNNING
        String query = "set " + myAgent.getLocalName() + " state=running";
        try {
            ACLMessage reply = sendCommand(query);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Object execute(Object[] input) {
        return null;
    }

    public ACLMessage sendCommand(String cmd) throws Exception {

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

    //====================================================================
    //ITRACEABILITY INTERFACE
    //====================================================================

    private void sendPlan(MWAgent myAgent) {

        // Conseguir la referencia del producto
        String productID = getProductID(myAgent.getLocalName());
        System.out.println("La referencia de producto del batch del agente " + myAgent.getLocalName() + " es: " + productID);

        // Conseguimos toda la informacion del producto utilizando su ID
        productInfo = getProductInfo(productID);
        System.out.println("ID del producto asociado al agente " + myAgent.getLocalName() + ": " + productInfo.get(0).get(3).get(2) + " - " + productID);

        // Teniendo toda la informacion del producto vamos a conseguir las maquinas que vayan a realizar todas las operaciones
        machinesForOperations = getMachines(myAgent.getLocalName(), productInfo);

        // Conseguir la cantidad de productos
        String numOfItems = getNumOfItems(myAgent.getLocalName());

        // Por cada operacion vamos a negociar con todos sus maquinas para asignar a la mejor
        for (Map.Entry<String, String> entry : machinesForOperations.entrySet()) {
            String operationID = entry.getKey();
            String machinesID = entry.getValue();
            System.out.println(operationID + " - " + machinesID);

            String conversationId = myAgent.getLocalName() + "_" + chatID++;

            negotiate(machinesID, "lead time", "execute", myAgent.getLocalName() + "," + numOfItems + "," + operationID, conversationId);

        }



    }

    private void readPlan() {
        // TODO
    }

    //====================================================================

    private String getParentAgentID(String seID) {
        String parentAgID = null;
        String parentQuery = "get " + seID + " attrib=parent";
        ACLMessage reply = null;

        try {
            reply = sendCommand(parentQuery);
            // ID del batch con el cual el agente está relacionado
            String batchID;
            if (reply == null)  // Si no existe el id en el registro devuelve error
                return "-1";
            else
                batchID = reply.getContent();

            reply = sendCommand("get " + batchID + " attrib=parent");
            if (reply != null) {  // ID del plan
                String orderID = reply.getContent();     // Con el ID del order conseguir su agente
                reply = sendCommand("get * parent=" + orderID + " category=orderAgent");
                if (reply != null) {
                    parentAgID = reply.getContent();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parentAgID;
    }

    private String getProductID(String seID) {
        String productID = null;
        String query = "get " + seID + " attrib=parent";
        ACLMessage reply = null;

        try {
            reply = sendCommand(query);
            // ID del batch con el cual el agente está relacionado
            // Ya que es este objeto el que tiene la referencia del producto
            String batchID = null;
            if (reply != null) {
                batchID = reply.getContent();
                query = "get " + batchID + " attrib=refProductID";
                reply = sendCommand(query);
                if (reply != null)
                    productID = reply.getContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productID;
    }

    private String getNumOfItems(String seID) {
        String numOfItems = null;
        String query = "get " + seID + " attrib=parent";
        ACLMessage reply = null;

        try {
            reply = sendCommand(query);
            // ID del batch con el cual el agente está relacionado
            // Ya que es este objeto el que tiene la cantidad de productos
            String batchID = null;
            if (reply != null) {
                batchID = reply.getContent();
                query = "get " + batchID + " attrib=numberOfItems";
                reply = sendCommand(query);
                if (reply != null)
                    numOfItems = reply.getContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numOfItems;
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

    private HashMap<String, String> getMachines(String localName, ArrayList<ArrayList<ArrayList<String>>> productInfo) {

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
                    reply = sendCommand(getAllMachines);
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
                                reply = sendCommand(simpleQuery);
                            else
                                reply = sendCommand(complexQuery);
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

    private String negotiate(String targets, String negotiationCriteria, String action, String externalData, String conversationId) {

        //Request de nueva negociación
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);

        for (String target: targets.split(","))
            msg.addReceiver(new AID(target, AID.ISLOCALNAME));
        msg.setConversationId(conversationId);
        msg.setOntology(es.ehu.platform.utilities.MasReconOntologies.ONT_NEGOTIATE );

        msg.setContent("negotiate " +targets+ " criterion=" +negotiationCriteria+ " action=" +action+ " externaldata=" +externalData);
        myAgent.send(msg);

        return "Negotiation message sent";
    }

}
