package es.ehu.domain.manufacturing.test;

import com.google.gson.Gson;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashMap;
import java.util.Scanner;

public class IPB_Operator_Agent extends Agent {

    @Override
    protected void setup() {
        // When this method is called the agent has been already registered with the Agent Platform AMS and is able to send and receive messages.
        // However, the agent execution model is still sequential and no behaviour scheduling is active yet.
        // This method can be used for ordinary startup tasks such as DF registration, but is essential to add at least a Behaviour object to the agent.

        addBehaviour(new CyclicBehaviour() {

            String assetName;
            String service;
            String parameters;
            String msgContent;
            HashMap msgHashMap = new HashMap();
            HashMap paramHashMap = new HashMap();
            boolean finish = false;

            public void action() {

                //Introduzco el nombre del asset
                Scanner in = new Scanner(System.in);
                System.out.print("Please, introduce the name of the asset you want to request a service: ");
                assetName = in.nextLine();

                //Introduzco el nombre de un servicio
                System.out.print("Please, introduce the name of the service you want to invoke (GET/POST): ");
                service = in.nextLine()+"_"+assetName;
                System.out.println();
                msgHashMap.put("Service",service);

                //Introduzco los parámetros que pueda necesitar
                System.out.print("Do you want to include  parameters? (Y/N): ");
                boolean exit = in.nextLine().equalsIgnoreCase("N");
                System.out.println();

                while (!exit){

                    System.out.print("Please, introduce the name and value of the parameters you require for this service " +
                            "(for example, Ref_Subproduct_Type=1234): ");
                    parameters = in.nextLine();
                    String[] parametersArray = parameters.split("=");
                    paramHashMap.put(parametersArray[0],parametersArray[1]);

                    System.out.print("Do you want to include another parameter? (Y/N): ");
                    exit = in.nextLine().equalsIgnoreCase("N");
                    System.out.println();
                }

                //Preparo el contenido del mensaje
                parameters = new Gson().toJson(paramHashMap);
                msgHashMap.put("Parameters",parameters);
                msgContent = new Gson().toJson(msgHashMap);

                //Envío el mensaje al agente asociado a ese asset
                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                AID IPB_Agent = new AID(assetName, false);
                request.addReceiver(IPB_Agent);
                request.setOntology("data");
                request.setContent(msgContent);
                send(request);

                //Recibo la respuesta y la imprimo
                ACLMessage response = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                if (response != null){
                    System.out.println("Results from service requested to "+response.getSender().getLocalName()+": ");
                    System.out.println(response.getContent()+"\n");
                }
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
