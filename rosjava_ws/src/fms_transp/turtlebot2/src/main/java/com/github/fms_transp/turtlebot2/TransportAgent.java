package com.github.rosjava.fms_transp.turtlebot2;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.gateway.GatewayAgent;
import jade.lang.acl.MessageTemplate;

import com.github.rosjava.fms_transp.turtlebot2.StructCommand;
import com.github.rosjava.fms_transp.turtlebot2.StructTransportUnitState;

import com.google.gson.Gson;

/* Este codigo tiene como funcion arrancar un agente dummy que simula a un
   agente de transporte de la plataforma MAS. Su funcion, es la de enviar una
   "coordenada" al agente GWAgent.java Esta coordeanada, es un contador integer
   que a su vez va incrementandose en cada una de las iteraciones
 */

public class TransportAgent extends Agent {

    private String JSONmsg_content = null;


    @Override
    protected void setup() {

        MessageTemplate matchPerformative = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate matchOntology = MessageTemplate.MatchOntology("EstadoTransporte");
        MessageTemplate matchConversationID = MessageTemplate.MatchConversationId("1234");

        final MessageTemplate messageTemplate =  MessageTemplate.and(MessageTemplate.and(matchPerformative, matchOntology), matchConversationID);

        addBehaviour(new CyclicBehaviour() {

            Integer i = 0;
            Integer State_Counter = 0;

            public void action() {

                // Definimos los parametros y campos del mensaje ACL que se va a enviar a GWAgent

                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                AID GWagentROS = new AID("ControlGatewayCont1", false);
                msg.addReceiver(GWagentROS);
                msg.setOntology("ComandoCoordenada");
                msg.setConversationId("1234");
                msg.setContent(i.toString());
                send(msg);
		        i++;

                ACLMessage msg_content = receive(messageTemplate);

                if (msg_content != null){

                    // El TransportAgent unicamente entrara aqui si se cumplen todos los requisitos del
                    // template especificado dentro de la variable "messageTemplate". En resumen, si
                    // recibe un mensaje sobre el estado de un estado de transporte desde el GWAgent.
                    // Estos datos vendran en formato JSON, por lo que sera necesario deserializar
                    // los datos de formato JSON a su tipo numerico original: StructTransportUnitState.

                    JSONmsg_content = msg_content.getContent();

                    // Deserializamos los datos contenidos en JSONmsg_conten, que corresponden a los datos
                    // del objeto TransportState desde el GWagentROS, que a su vez corresponden a TUS_object
                    Gson gson = new Gson();
                    //System.out.println(gson.toJson(JSONmsg_content));
                    StructTransportUnitState javaTranspState = gson.fromJson(JSONmsg_content, StructTransportUnitState.class);

                    // Mostramos por pantalla los datos leidos sobre el estado de la unidad de transporte
                    String Nombre = javaTranspState.getTransport_unit_name();
                    String Estado = javaTranspState.getTransport_unit_state();
                    State_Counter++;

                    System.out.println("***************************************************");
                    System.out.println("He recibido el estado del transporte: " + Nombre);
                    System.out.println("El transporte se encuentra en el estado: " + Estado);
                    System.out.println("Numero de trama: " + Integer.toString(State_Counter));

                    JSONmsg_content = null;

                }

                try {

                    Thread.sleep(5000);

                } catch(Exception e) {

                    ;

                }
            }
        });

    }

    @Override
    protected void takeDown() {
        System.out.println("TransportAgent TakeDown");
    }
}

