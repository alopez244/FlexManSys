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

public class GWagentROSState extends GatewayAgent {

    private String msg_content = null;
    StructTransportUnitState TUS_object = new StructTransportUnitState();
    boolean TUS_object_flag = false;

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

        System.out.println(" GWAgent inicializado");

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

          System.out.println("HOLA CON SEND");

          //if (msg_content != null) {

              // Rellenamos el contenido de command desde GWAgent ya que recv si que devuelve
              // contenido, mas concretamente, lo que haya leido GWagentROS del mensaje recibido
              // desde el TransportAgent

              //((StructCommand) command).setContent(msg_content);

              ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
              //"TransportAgent" Nombre que se le da al inicializar el agente en la plataforma JADE
              AID TransportAgent = new AID("TransportAgent", false);
              msg.addReceiver(TransportAgent);
              msg.setOntology("EstadoTransporte");
              msg.setConversationId("1234");
              msg.setContent(msg_content);
              send(msg);

              System.out.println("CONTENIDO GWagentROS: ");
              System.out.println(msg_content);

              msg_content = null;

          //}
      }

      releaseCommand(command);

    }

    public void send_message(StructTransportUnitState TUS_object_ROS) {

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        AID TransportAgent = new AID("TransportAgent", false);
        msg.addReceiver(TransportAgent);
        msg.setOntology("EstadoTransporte");
        msg.setConversationId("1234");

        // Convertimos el contenido del mensaje al agente Transporte de tipo
        // StructTransportUnitState a TUS_Object

        Gson gson = new Gson();
        msg.setContent(gson.toJson(TUS_object_ROS));
        System.out.println("************************");
        System.out.println(gson.toJson(TUS_object));
        System.out.println("************************");

        send(msg);

        System.out.println("HE ESCRITO CON GWagentROS = " + TUS_object.getTransport_unit_name());

    }

    public void set_message(StructTransportUnitState TUS_object_ROS) {

        TUS_object = TUS_object_ROS;
        TUS_object_flag = true;

    }

    @Override
    public void setup() {
      super.setup();

      // Estos parametros de comunicacion ACL, deben de coincidir con los que envie el TransportAgent.java

      MessageTemplate matchPerformative = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),MessageTemplate.MatchPerformative(ACLMessage.INFORM));
      MessageTemplate matchOntology = MessageTemplate.MatchOntology("ComandoCoordenada");
      MessageTemplate matchConversationID = MessageTemplate.MatchConversationId("1234");

      final MessageTemplate messageTemplate =  MessageTemplate.and(MessageTemplate.and(matchPerformative, matchOntology), matchConversationID);

      addBehaviour(new CyclicBehaviour() {

        public void action() {

          ACLMessage msg = receive(messageTemplate);

          if(msg != null) {

            System.out.println("Se ha recibido un mensaje desde el TransportAgent");
            msg_content = msg.getContent();

          } else {

              //if (TUS_object_flag == true){
              //    send_message(TUS_object);
              //    TUS_object_flag = false;
              //}

            System.out.println("Bloqueando el GWagentROS hasta recibir otro mensaje de TransportAgent");
            block();

          }
        }

      });

    }
}
