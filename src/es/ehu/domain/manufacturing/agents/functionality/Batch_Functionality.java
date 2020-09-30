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
import java.util.ArrayList;

public class Batch_Functionality implements BasicFunctionality {

    private static final long serialVersionUID = 1L;
    private Agent myAgent;

    private String parentAgentID;
    private ArrayList<ArrayList<ArrayList<String>>> productInfo;

    @Override
    public Void init(MWAgent myAgent) {

        this.myAgent = myAgent;


        // Envio un mensaje a mi parent diciendole que me he creado correctamente
        parentAgentID = getParentAgentID(myAgent.getLocalName());
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(parentAgentID, AID.ISLOCALNAME));
        msg.setContent("Batch created successfully");
        myAgent.send(msg);


        // Conseguir la referencia del producto
        String productID = getProductID(myAgent.getLocalName());
        System.out.println("La referencia de producto del batch del agente " + myAgent.getLocalName() + " es: " + productID);

        // Conseguimos toda la informacion del producto utilizando su ID
        productInfo = getProductInfo(productID);
        System.out.println("ID del producto asociado al agente " + myAgent.getLocalName() + ": " + productInfo.get(0).get(3).get(2) + " - " + productID);

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
}
