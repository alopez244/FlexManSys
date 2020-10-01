package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.IExecManagement;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class Order_Functionality implements BasicFunctionality, IExecManagement {

    private static final long serialVersionUID = 1L;
    private Agent myAgent;

    private String parentAgentID;
    private List<String> myBatches;
    private int chatID = 0; // Numero incremental para crear conversationID

    @Override
    public Void init(MWAgent myAgent) {

        this.myAgent = myAgent;



        Hashtable<String, String> attributes = new Hashtable<String, String>();
        attributes.put("seClass", "es.ehu.domain.manufacturing.agents.BatchAgent");

        String status = seStart(myAgent.getLocalName(), attributes, null);
        if (status == null)
            System.out.println("BatchAgents created");
        else if (status == "-1")
            System.out.println("ERROR creating BatchAgent -> No existe el ID del plan");
        else if (status == "-4")
            System.out.println("ERROR creating BatchAgent -> No targets");

        // Le añadimos un comportamiento para que consiga todos los mensajes que le van a enviar los batch cuando se arranquen correctamente
        myAgent.addBehaviour(new SimpleBehaviour() {
            @Override
            public void action() {
                boolean moreMsg = true;
                ACLMessage msg = myAgent.receive();
                if(msg != null) {
                    if ((msg.getPerformative() == 7) && (msg.getContent().equals("Batch created successfully"))) {
                        System.out.println("\tYa se ha creado el agente " + msg.getSender().getLocalName() + " - hay que borrarlo de la lista --> " + myBatches);
                        // Primero vamos a conseguir el ID del order (ya que el mensaje nos lo envia su agente)
                        String senderOrderID = null;
                        try {
                            ACLMessage reply = sendCommand("get " + msg.getSender().getLocalName() + " attrib=parent");
                            if (reply != null)
                                senderOrderID = reply.getContent();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Si el batch es uno de los hijos (que solo los hijos nos enviaran los mensaje, pero por se acaso), lo borramos de la lista
                        if(myBatches.contains(senderOrderID))
                            myBatches.remove(senderOrderID);
                        // Si la lista esta vacia, todos los orders se han creado correctamente, y tendremos que pasar del estado BOOT al RUNNING
                        if (myBatches.isEmpty()) {
                            moreMsg = false;

                            // Envio un mensaje a mi parent diciendole que me he creado correctamente
                            parentAgentID = getParentAgentID(myAgent.getLocalName());
                            ACLMessage msgOrder = new ACLMessage(ACLMessage.INFORM);
                            msgOrder.addReceiver(new AID(parentAgentID, AID.ISLOCALNAME));
                            msgOrder.setContent("Order created successfully");
                            myAgent.send(msgOrder);

                            // Pasar a estado running
                            System.out.println("\tEl agente " + myAgent.getLocalName() + " ha finalizado su estado BOOT y pasará al estado RUNNING");

                            System.out.println("Estado del agente " + myAgent.getLocalName() + ": " + myAgent.getState());

                            // SystemModelAgent linea 1422
                            String query1 = "get " + myAgent.getLocalName() + " attrib=state";
                            String query = "set " + myAgent.getLocalName() + " state=running";
                            try {
                                ACLMessage reply = sendCommand(query1);
                                System.out.println("Dame el estado: " + reply.getContent());
                                reply = sendCommand(query);
                                System.out.println("Cambio el estado: " + reply.getContent());
                                reply = sendCommand(query1);
                                System.out.println("Dame el estado cambiado: " + reply.getContent());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //LOGGER.exit();
                            // TODO Mirar a ver como se puede hacer el cambio de estado (si lo hace la maquina de estados o hay que hacerlo desde aqui)
                            // ControlBehaviour --> cambio de estado

                            System.out.println("Estado del agente " + myAgent.getLocalName() + ": " + myAgent.getState());

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

        return null;
    }

    @Override
    public String seStart(String seID, Hashtable<String, String> attribs, String conversationId) {

        // seID --> ID del mPlanAgent
        String parentQuery = "get " + seID + " attrib=parent";
        ACLMessage reply = null;

        try {
            reply = sendCommand(parentQuery);
            // ID del order con el cual el agente está relacionado
            String orderID = null;
            if (reply == null)  // Si no existe el id en el registro devuelve error
                return "-1";
            else
                orderID = reply.getContent();

            String query = "get batch* parent="+ orderID;  // Busco todos los batch de los que el order es parent (no se buscan todos los elementos porque pueden existir otros orderAgent en tracking)
            reply = sendCommand(query);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String allBatches;
        if (reply == null)  // Si no existen orders en el registro devuelve error
            return "-1";
        else
            allBatches = reply.getContent();

        //List<String> items = Arrays.asList(allBatches.split("\\s*,\\s*"));

        List<String> items = new ArrayList<>();
        String[] aux = allBatches.split(",");
        for (String a:aux)
            items.add(a);

        this.myBatches = items;
        for (String batchID: items) {
            // Creamos los agentes para cada batch
            try {
                String redundancy = "1";
                if ((attribs!=null) && (attribs.containsKey("redundancy")))
                    redundancy = attribs.get("redundancy");

                reply = sendCommand("get (get * parent=(get * parent=" + batchID + " category=restrictionList)) attrib=attribValue");
                String refServID = null;
                if (reply != null)
                    refServID = reply.getContent();

                reply = sendCommand("get * category=pNodeAgent" + ((refServID.length()>0)?" refServID=" + refServID:""));
                String targets = null;
                if (reply != null) {
                    targets = reply.getContent();
                    if (targets.length()<=0)
                        return "-4";
                }


                reply = sendCommand("get " + batchID + " attrib=category");
                String seCategory = null;
                if (reply != null)
                    seCategory = reply.getContent();
                String seClass = attribs.get("seClass");

                // Orden de negociacion a todos los nodos
                for (int i=0; i<Integer.parseInt(redundancy); i++) {
                    // Crear un nuevo conversationID
                    conversationId = myAgent.getLocalName() + "_" + chatID++;
                    System.out.println("\tCONVERSATIONID for batch " + batchID + " and order " + myAgent.getLocalName() + ": " + conversationId);
                    //reply = sendCommand("localneg " + targets + " action=start " + batchID + " criterion=max mem externaldata=" + batchID + "," + seCategory + "," + seClass + "," + ((i==0)?"running":"tracking"));
                    negotiate(targets, "max mem", "start", batchID + "," + seCategory + "," + seClass + "," + ((i == 0) ? "running" : "tracking"), conversationId);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public String seStop(String... seID) {
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
            // ID del order con el cual el agente está relacionado
            String orderID;
            if (reply == null)  // Si no existe el id en el registro devuelve error
                return "-1";
            else
                orderID = reply.getContent();

            reply = sendCommand("get " + orderID + " attrib=parent");
            if (reply != null) {  // ID del plan
                String planID = reply.getContent();     // Con el ID del plan conseguir su agente
                reply = sendCommand("get * parent=" + planID + " category=mPlanAgent");
                if (reply != null) {
                    parentAgID = reply.getContent();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parentAgID;
    }

    // TODO Mirar para meter el metodo negotiate en una interfaz
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
