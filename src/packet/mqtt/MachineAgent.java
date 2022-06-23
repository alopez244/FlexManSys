package packet.mqtt;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

import com.google.gson.Gson;
import jade.lang.acl.MessageTemplate;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Scanner;

// Creacion de un Agente -> Creas una clase en JACA que extienda a la clase Agente
public class MachineAgent extends Agent {

    @Override
    // Inicialización - configuración de lo que tiene que hacer el agente cuando salga del SETUP
    protected void setup() {
        // When this method is called the agent has been already registered with the Agent Platform AMS and is able to send and receive messages.
        // However, the agent execution model is still sequential and no behaviour scheduling is active yet.
        // This method can be used for ordinary startup tasks such as DF registration, but is essential to add at least a Behaviour object to the agent.

        // Recogida de datos por teclado
        // Datos de identificación
        Scanner entradaTeclado = new Scanner(System.in);
        System.out.println("Control structure: ");
        System.out.print("\tInsert machine reference: ");
        int machineReference = entradaTeclado.nextInt();
        System.out.print("\tInsert order reference: ");
        int orderReference = entradaTeclado.nextInt();
        System.out.print("\tInsert batch reference: ");
        int batchReference = entradaTeclado.nextInt();
        System.out.print("\tInsert subproduct type reference: ");
        int subProdReference = entradaTeclado.nextInt();

        // Datos de operacion
        System.out.print("\tInsert service type: ");
        int ServType = entradaTeclado.nextInt();
        System.out.print("\tInsert number of items: ");
        int NoItem = entradaTeclado.nextInt();

        System.out.println("\nYour choice:  ");
        System.out.println("\tReference: " + machineReference);
        System.out.println("\tOrder reference: " + orderReference);
        System.out.println("\tBatch reference: " + batchReference);
        System.out.println("\tSubproduct type reference: " + subProdReference);
        System.out.println("\n\tService type: " + ServType);
        System.out.println("\tNumber of items: " + NoItem);
        System.out.println("\nStart of the process...");


        // Formacion del HashMap
        HashMap<String, Boolean> mapControl = new HashMap<String, Boolean>(); //Creating HashMap
        mapControl.put("Flag_Service_Completed", false);
        mapControl.put("Flag_New_Service", true);  //Put elements in Map
        mapControl.put("Flag_Item_Completed", false);

        HashMap<String, Integer> mapId = new HashMap<String, Integer>(); //Creating HashMap
        mapId.put("Ref_Subproduct_Type", subProdReference);
        mapId.put("Batch_Reference", batchReference);
        mapId.put("Machine_Reference", machineReference);  //Put elements in Map
        mapId.put("Order_Reference", orderReference);

        HashMap<String, Integer> mapOperation = new HashMap<String, Integer>(); //Creating HashMap
        mapOperation.put("No_of_Items", NoItem);
        mapOperation.put("Ref_Service_Type", ServType);  //Put elements in Map


        HashMap<String, HashMap> mapAgent2PLC = new HashMap<String, HashMap>();
        mapAgent2PLC.put("Control", mapControl);
        mapAgent2PLC.put("Id", mapId);
        mapAgent2PLC.put("Operation", mapOperation);


        addBehaviour(new SimpleBehaviour() {

            public void onStart() {

                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                AID GWagentMQTT = new AID("ControlGatewayContUR3e", false);
                msg.addReceiver(GWagentMQTT);
                msg.setOntology("data");
                msg.setConversationId("1234");

                // Envio de la estructura de datos
                Gson gson = new Gson();
                msg.setContent(gson.toJson(mapAgent2PLC));
                send(msg);

            }

            public void action() {

                MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = receive(messageTemplate);
                if (msg != null) {
                    System.out.println("--- GWagentMQTT has received a message from MachineAgent");
                    System.out.println(msg.getContent());
                } else {
                    System.out.println("--- No messages from GWagentMQTT. The MachineAgent is blocking.");
                    block();
                }

            }

            @Override
            public boolean done() {
                return false;
            }

        });

    }

    @Override
    protected void takeDown() {
        // When this method is called the agent has not deregistered itself with the Agent Platform AMS and is still able to exchange messages with other agents.
        // However, no behaviour scheduling is active anymore and the Agent Platform Life Cycle state is already set to deleted.
        // This method can be used for ordinary cleanup tasks such as DF deregistration, but explicit removal of all agent behaviours is not needed.
        System.out.println("##### takeDown() #####");
    }
}
