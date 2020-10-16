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

public class Order_Functionality extends DomApp_Functionality implements BasicFunctionality, IExecManagement {

    private static final long serialVersionUID = 1L;
    private Agent myAgent;

    private String parentAgentID;
    private List<String> myBatches;
    private int chatID = 0; // Numero incremental para crear conversationID

    private String firstState;
    private String redundancy;
    private ArrayList<String> myReplicasID = new ArrayList<>();

    @Override
    public Void init(MWAgent myAgent) {

        this.myAgent = myAgent;

        // Crear un nuevo conversationID
        String conversationId = myAgent.getLocalName() + "_" + chatID++;

        String[] firstArgument = myAgent.getArguments()[0].toString().split("=");
        if (firstArgument[0].equals("firstState"))
            firstState = firstArgument[1];

        String[] secondArgument = myAgent.getArguments()[1].toString().split("=");
        if (secondArgument[0].equals("redundancy"))
            redundancy = secondArgument[1];

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

            String status = seStart(myAgent.getLocalName(), attributes, conversationId);
            if (status == null)
                System.out.println("BatchAgents created");
            else if (status == "-1")
                System.out.println("ERROR creating BatchAgent -> No existe el ID del plan");
            else if (status == "-4")
                System.out.println("ERROR creating BatchAgent -> No targets");

            // TODO primero comprobara que todas las replicas (tracking) se han creado correctamente, y despues comprobara los batches
            // Es decir, antes de avisar a su padre que esta creado, comprueba las replicas y despues los batches

            // Le añadimos un comportamiento para que consiga todos los mensajes que le van a enviar los batch cuando se arranquen correctamente
            myAgent.addBehaviour(new SimpleBehaviour() {
                @Override
                public void action() {
                    boolean moreMsg = true;
                    ACLMessage msg = myAgent.receive();
                    if (msg != null) {
                        if ((msg.getPerformative() == 7)) {
                            if (msg.getContent().equals("Batch created successfully")) {
                                System.out.println("\tYa se ha creado el agente " + msg.getSender().getLocalName() + " - hay que borrarlo de la lista --> " + myBatches);
                                // Primero vamos a conseguir el ID del order (ya que el mensaje nos lo envia su agente)
                                String senderOrderID = null;
                                try {
                                    ACLMessage reply = sendCommand(myAgent, "get " + msg.getSender().getLocalName() + " attrib=parent", conversationId);
                                    if (reply != null)
                                        senderOrderID = reply.getContent();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                // Si el batch es uno de los hijos (que solo los hijos nos enviaran los mensaje, pero por se acaso), lo borramos de la lista
                                if (myBatches.contains(senderOrderID))
                                    myBatches.remove(senderOrderID);
                            } else if (msg.getContent().equals("Order replica created successfully")) {
                                System.out.println("\tYa se ha creado la replica " + msg.getSender().getLocalName());
                                myReplicasID.add(msg.getSender().getLocalName());
                            }
                            // Si la lista esta vacia, todos los orders se han creado correctamente, y miraremos si se han creado todos las replicas
                            // Si se cumple t0do, tendremos que pasar del estado BOOT al RUNNING
                            if ((myBatches.isEmpty()) && (myReplicasID.size() == Integer.parseInt(redundancy) - 1)) {
                                moreMsg = false;

                                // SystemModelAgent linea 1422
                                String query = "set " + myAgent.getLocalName() + " state=running";
                                try {
                                    ACLMessage reply = sendCommand(myAgent, query, conversationId);
                                    System.out.println("Dame el estado cambiado: " + reply.getContent());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                // Pasar a estado running
                                System.out.println("\tEl agente " + myAgent.getLocalName() + " ha finalizado su estado BOOT y pasará al estado RUNNING");

                                // Envio un mensaje a mi parent diciendole que me he creado correctamente
                                parentAgentID = getRunningParentAgentID(myAgent, conversationId);
                                ACLMessage msgOrder = new ACLMessage(ACLMessage.INFORM);
                                msgOrder.addReceiver(new AID(parentAgentID, AID.ISLOCALNAME));
                                msgOrder.setContent("Order created successfully");
                                myAgent.send(msgOrder);

                                //LOGGER.exit();
                                // TODO Mirar a ver como se puede hacer el cambio de estado (si lo hace la maquina de estados o hay que hacerlo desde aqui)
                                // ControlBehaviour --> cambio de estado


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

        } else {
            // Si su estado es tracking

            String runningAgentID = null;
            try {
                String parentID = null;
                ACLMessage reply = sendCommand(myAgent, "get " + myAgent.getLocalName() + " attrib=parent", conversationId);
                if (reply != null)
                    parentID = reply.getContent();
                reply = sendCommand(myAgent, "get * parent=" + parentID + " category=orderAgent state=bootToRunning", conversationId);
                if (reply !=null) {
                    runningAgentID = reply.getContent();

                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(new AID(runningAgentID, AID.ISLOCALNAME));
                    msg.setContent("Order replica created successfully");
                    myAgent.send(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Una vez mande el mensaje, registra que su estado es el tracking
            try {
                sendCommand(myAgent, "set " + myAgent.getLocalName() + " state=" + firstState, conversationId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public String seStart(String seID, Hashtable<String, String> attribs, String conversationId) {

        // seID --> ID del mPlanAgent
        String parentQuery = "get " + seID + " attrib=parent";
        ACLMessage reply = null;

        try {
            reply = sendCommand(myAgent, parentQuery, conversationId);
            // ID del order con el cual el agente está relacionado
            String orderID = null;
            if (reply == null)  // Si no existe el id en el registro devuelve error
                return "-1";
            else
                orderID = reply.getContent();

            String query = "get batch* parent="+ orderID;  // Busco todos los batch de los que el order es parent (no se buscan todos los elementos porque pueden existir otros orderAgent en tracking)
            reply = sendCommand(myAgent, query, conversationId);
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
                reply = sendCommand(myAgent, "get (get * parent=(get * parent=" + batchID + " category=restrictionList)) attrib=attribValue", conversationId);
                String refServID = null;
                if (reply != null)
                    refServID = reply.getContent();

                reply = sendCommand(myAgent, "get * category=pNodeAgent" + ((refServID.length()>0)?" refServID=" + refServID:""), conversationId);
                String targets = null;
                if (reply != null) {
                    targets = reply.getContent();
                    if (targets.length()<=0)
                        return "-4";
                }


                reply = sendCommand(myAgent, "get " + batchID + " attrib=category", conversationId);
                String seCategory = null;
                if (reply != null)
                    seCategory = reply.getContent();
                String seClass = attribs.get("seClass");

                // Orden de negociacion a todos los nodos
                for (int i=0; i<Integer.parseInt(redundancy); i++) {

                    conversationId = myAgent.getLocalName() + "_" + chatID++;

                    System.out.println("\tCONVERSATIONID for batch " + batchID + " and order " + myAgent.getLocalName() + ": " + conversationId);
                    //reply = sendCommand("localneg " + targets + " action=start " + batchID + " criterion=max mem externaldata=" + batchID + "," + seCategory + "," + seClass + "," + ((i==0)?"running":"tracking"));
                    negotiate(myAgent, targets, "max mem", "start", batchID + "," + seCategory + "," + seClass + "," + ((i == 0) ? "running" : "tracking")+","+redundancy, conversationId);
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


    private String getParentAgentID(String seID, String conversationId) {
        String parentAgID = null;
        String parentQuery = "get " + seID + " attrib=parent";
        ACLMessage reply = null;

        try {
            reply = sendCommand(myAgent, parentQuery, conversationId);
            // ID del order con el cual el agente está relacionado
            String orderID;
            if (reply == null)  // Si no existe el id en el registro devuelve error
                return "-1";
            else
                orderID = reply.getContent();

            reply = sendCommand(myAgent, "get " + orderID + " attrib=parent", conversationId);
            if (reply != null) {  // ID del plan
                String planID = reply.getContent();     // Con el ID del plan conseguir su agente
                reply = sendCommand(myAgent, "get * parent=" + planID + " category=mPlanAgent", conversationId);
                if (reply != null) {
                    parentAgID = reply.getContent();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parentAgID;
    }
}
