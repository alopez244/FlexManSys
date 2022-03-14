package com.github.rosjava.fms_transp.turtlebot2;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.gateway.GatewayAgent;

import com.github.rosjava.fms_transp.turtlebot2.StructCommand;
import com.github.rosjava.fms_transp.turtlebot2.StructTransportUnitState;

import com.google.gson.Gson;


/* Este codigo tiene como funcion el ejecutar una clase que inicialice los parametros
   de comunicacion entre agentes necesarios. Es decir, establece las etiquetas o
   performativas, el ontology y el conversation ID.
 */

public class GWagentROS extends GatewayAgent {

    private String msg_content = null;
    StructTransportUnitState TUS_object = new StructTransportUnitState();
    boolean TUS_object_flag = false;
    public boolean TransportAgent_Init = false;

    @Override
    protected void processCommand(java.lang.Object _command) {

      // Creamos un objeto command con estructura StructCommand, que recoge dos variables
      // La accion a realizar (init o recv) y el contenido de la accion, el cual ha de ser
      // devuelto por esta clase a la clase que la instancia, es decir, GWagentROS debe de
      // devolverle el contenido de command a GWAgent

      StructCommand command = (StructCommand) _command;

      // Obtenemos la accion instanciada desde la clase que instancia a GWagentROS, es decir,
      // leemos si se ha introducido un init o recv
      String action = command.getAction();

      if(action.equals("init")) {

        // Mostramos por pantalla que el agente GW ha sido inicializado

        System.out.println("***************************************************");
        System.out.println("Se ha inicializado el ACLGWAgentROS");

        // La accion init no devuelve ningun contenido

      } else if(action.equals("recv")) {

        if (msg_content != null) {

          // Rellenamos el contenido de command desde GWAgent ya que recv si que devuelve
          // contenido, mas concretamente, lo que haya leido GWagentROS del mensaje recibido
          // desde el TransportAgent

          ((StructCommand) command).setContent(msg_content);
          msg_content = null;

          ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
          //"TransportAgent" Nombre que se le da al inicializar el agente en la plataforma JADE
          AID TransportAgent = new AID("TransportAgent", false);
          msg.addReceiver(TransportAgent);
          msg.setOntology("ComandoCoordenada");
          msg.setConversationId("1234");
          msg.setContent("CoordenadaRecibida");
          send(msg);

        }

      } else if(action.equals("send")) {

          // Mediante la accion "send", se indica al nodo ROS GWAgent, que a traves de su
          // apendice JADE GWagentROS, envie datos sobre el estado del transporte correspondiente.

          /*
          ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
          //"TransportAgent" Nombre que se le da al inicializar el agente en la plataforma JADE
          AID TransportAgent = new AID("TransportAgent", false);
          msg.addReceiver(TransportAgent);
          msg.setOntology("asset_state");
          msg.setConversationId("1234"); */

          if ( TransportAgent_Init == false) {

              ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
              //"TransportAgent" Nombre que se le da al inicializar el agente en la plataforma JADE
              AID TransportAgent = new AID("auxma-local", false);
              msg.addReceiver(TransportAgent);
              msg.setOntology("asset_state");
              msg.setConversationId("0");

              StructTransportUnitState TransportState = command.getTransport_state();

              // Convertimos el objeto TUS_object, de tipo StructTransportUnitState, e instanciado
              // a TransportState a un formato de tipo JSON. Lo haremos mediante la ayuda de la
              // liberia GSON.

              Gson gson = new Gson();
              msg.setContent(gson.toJson(TransportState));

              send(msg);

              ACLMessage answer = blockingReceive(MessageTemplate.MatchOntology("asset_checked"), 1000);

              if (answer != null) {
                  TransportAgent_Init = true;
              }

          }

          else if (TransportAgent_Init == true) {

              ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
              //"TransportAgent" Nombre que se le da al inicializar el agente en la plataforma JADE
              AID TransportAgent = new AID("transport1", false);
              //AID TransportAgent = new AID("TransportAgent1", false);
              msg.addReceiver(TransportAgent);
              msg.setOntology("current_asset_state");
              msg.setConversationId("1234");

              StructTransportUnitState TransportState = command.getTransport_state();

              // Convertimos el objeto TUS_object, de tipo StructTransportUnitState, e instanciado
              // a TransportState a un formato de tipo JSON. Lo haremos mediante la ayuda de la
              // liberia GSON.

              Gson gson = new Gson();
              msg.setContent(gson.toJson(TransportState));

              send(msg);
          }

      }

      releaseCommand(command);

    }

    @Override
    public void setup() {
      super.setup();

      // Estos parametros de comunicacion ACL, deben de coincidir con los que envie el TransportAgent.java

      MessageTemplate matchPerformative = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),MessageTemplate.MatchPerformative(ACLMessage.INFORM));
      MessageTemplate matchOntology = MessageTemplate.MatchOntology("ComandoCoordenada");
      MessageTemplate matchConversationID = MessageTemplate.MatchConversationId("1234");

      final MessageTemplate messageTemplate =  MessageTemplate.and(MessageTemplate.and(matchPerformative, matchOntology), matchConversationID);
      /*
      // Parametros de comunicacion ACL para el primer contacto con el Agent Transporte
      MessageTemplate matchPerformative_Transp = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
      MessageTemplate matchOntology_Transp = MessageTemplate.MatchOntology("check_asset");
      MessageTemplate matchConversationID_Transp = MessageTemplate.MatchConversationId("0");

      final MessageTemplate messageTemplate_TransportAgent_Setup =  MessageTemplate.and(MessageTemplate.and(matchPerformative_Transp, matchOntology_Transp), matchConversationID_Transp);
      */
        addBehaviour(new CyclicBehaviour() {

        public void action() {

          ACLMessage msg = receive(messageTemplate);

          if(msg != null) {

            System.out.println("Se ha recibido un mensaje desde el TransportAgent");
            msg_content = msg.getContent();

          } else {

            System.out.println("Bloqueando el GWagentROS hasta recibir otro mensaje de TransportAgent");
            block();

          }
        }

      });
        /*

        addBehaviour(new CyclicBehaviour() {

            public void action() {

                ACLMessage msg_Transp = receive(messageTemplate_TransportAgent_Setup);

                if(msg_Transp != null) {

                    System.out.println("Se ha realizado contacto con el agente Transporte");

                }

            }

        }); */


    }
}

