package com.github.rosjava.fms_transp.turtlebot2;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.gateway.GatewayAgent;

import com.github.rosjava.fms_transp.turtlebot2.StructCommand;
import com.github.rosjava.fms_transp.turtlebot2.StructTransportUnitState;
import com.github.rosjava.fms_transp.turtlebot2.StructTranspRequest;

import com.google.gson.Gson;


/* Este codigo tiene como funcion el ejecutar una clase que inicialice los parametros
   de comunicacion entre agentes necesarios. Es decir, establece las etiquetas o
   performativas, el ontology y el conversation ID.
 */

public class GWagentROS extends GatewayAgent {

    private String msg_content = null;
    StructTransportUnitState TUS_object = new StructTransportUnitState();

    public boolean TUS_object_flag = false;
    public boolean TransportAgent_Init = false;

    public String TransportGatewayContainer;
    public String TransportAgentAID;

    // TaskList de 30 posiciones
    public String[] TaskList = {"0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0"};
    public String TaskListSize;
    public int TaskListIndx;
    public boolean TaskListFlag = false;
    public boolean TaskReadyToSend = false;
    public boolean TransportOperative = false;

    public String CurrentTransportMachineState;

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
        System.out.println("***************************************************");

        // La accion init no devuelve ningun contenido

      } else if(action.equals("recv")) {

        if (msg_content != null) {

          // Rellenamos el contenido de command desde GWAgent ya que recv si que devuelve
          // contenido, mas concretamente, lo que haya leido GWagentROS del mensaje recibido
          // desde el TransportAgent

          //((StructCommand) command).setContent(msg_content); // Aqui se define el contenido que se va a enviar al transporte
          //msg_content = null;

           if (TaskReadyToSend == false){

               // Aqui se define el contenido que se va a enviar al transporte si el mensaje se recibe por ComandoCoordenada
               ((StructCommand) command).setContent(msg_content);
               msg_content = null;

           }

           else if (TaskReadyToSend == true){

               // Aqui se define el contenido que se va a enviar al transporte si el mensaje se recibe por PlanCoordenadas

               System.out.println(CurrentTransportMachineState);

               if (CurrentTransportMachineState.equals("ACTIVE") && TransportOperative == false){

                   // Solo le mandaremos un comando de lista de tareas si se encuentra en estado ACTIVO, de esta manera
                   // tambien se puede llevar un control a modo de "cuenta gotas" de los comandos que se le envian al
                   // transporte

                   ((StructCommand) command).setContent(TaskList[TaskListIndx]);
                   TransportOperative = true;

                   if (TaskListIndx >= 1) {
                       TaskListIndx = TaskListIndx - 1;
                   }
                   else if (TaskListIndx == 0){

                       // Ya se han enviado todas las tareas de la lista, desactivamos este modo
                       TaskReadyToSend = false;
                       msg_content = null;
                   }

               }

               else if (CurrentTransportMachineState.equals("OPERATIVE") && TransportOperative == true){

                   // Este estado nos permite darle tiempo al transporte para entrar en modo operativo, de modo
                   // que no se pierda ningun comando de la TaskList en el tiempo que tarda en realizar la transicion
                   // de ACTIVE a OPERATIVE. Es decir, un comprobador el cual no permite mandar una nueva tarea hasta
                   // que el transporte no haya pasado por OPERATIVE y haya vuelto al estado ACTIVE.

                   TransportOperative = false;

               }

               // METER FLANCO DE ESTADO OPERATIVO

           }

          // Esto seria para contestar al agente transporte e indicarle que todo correcto (BORRAR)

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

          CurrentTransportMachineState = command.getTransport_machine_state();

          if (TransportAgent_Init == false) {

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
              //ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
              AID TransportAgent = new AID(TransportAgentAID, false);
              //AID TransportAgent = new AID("TransportAgent1", false);
              msg.addReceiver(TransportAgent);
              msg.setOntology("assetdata");
              //msg.setOntology("negotiation");
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
      MessageTemplate matchOntology = MessageTemplate.or(MessageTemplate.MatchOntology("ComandoCoordenada"),MessageTemplate.MatchOntology("PlanCoordenadas"));
      MessageTemplate matchConversationID = MessageTemplate.MatchConversationId("1234");

      final MessageTemplate messageTemplate =  MessageTemplate.and(MessageTemplate.and(matchPerformative, matchOntology), matchConversationID);

      // Para obtener el nombre del contenedor JADE en el que se situa el GWAgent

        jade.core.Location location = this.here();
        TransportGatewayContainer = location.getName();

        switch (TransportGatewayContainer){

            case "GatewayContT_01":

                TransportAgentAID = "transport1";

                break;

            case "GatewayContT_02":

                TransportAgentAID = "transport2";

                break;

            case "GatewayContT_03":

                TransportAgentAID = "transport3";

                break;

            case "GatewayContT_04":

                TransportAgentAID = "transport4";

                break;

            default:

                System.out.println("************************************************************");
                System.out.println("Contenedor no reconocido, compruebe el argumento introducido a ACLGWAgentROS");
                System.out.println("************************************************************");

                break;

        }

        addBehaviour(new CyclicBehaviour() {

        public void action() {

          ACLMessage msg = receive(messageTemplate);

          if(msg != null) {

              String Ontology = msg.getOntology();

              System.out.println("Se ha recibido un mensaje desde el TransportAgent con Ontologia: " + Ontology);

            if(Ontology.equals("ComandoCoordenada")){

                // Hemos recibido unicamente una coordenada

                msg_content = msg.getContent();

                // Puede haberse recibido una unica coordenada puesto que pueden ser comandos de STOP o EMERGENCY
                // mientras el transporte opera, es por ello que es necesario no dejarle enviar mas tareas al transporte

                TaskReadyToSend = false;

            }

            else if(Ontology.equals("PlanCoordenadas")){

                // Hemos recibido una lista de coordenadas

                msg_content = msg.getContent();

                if (TaskListFlag == false){

                    // Si recibimos un mensaje con la ontologia PlanCoordenadas, tendra como primera posicion
                    // el numero de coordenadas que se van a recibir, es como un comando "START"

                    TaskListSize = msg_content;
                    TaskListIndx = Integer.valueOf(TaskListSize);

                    System.out.println(TaskListSize);

                }

                else if (!msg_content.equals("END") &&  TaskListFlag == true){

                    // Una vez recibidas el numero de coordenadas y que no se reciba un "END", los siguientes mensajes
                    // continenen las coordenadas que conforman la lista de tareas

                    TaskList[TaskListIndx] = msg_content;
                    TaskListIndx = TaskListIndx - 1;

                    if (TaskListIndx == 0){

                        // Si TasListIndx es cero, quiere decir que no vamos a recibir nuevas coordenadas, luego se
                        // pueden mandar al transporte las ya recibidas

                        TaskReadyToSend = true;
                        TaskListIndx = Integer.valueOf(TaskListSize);

                    }
                }

                else if (msg_content.equals("END") && TaskListFlag == true){

                    // Hemos recibido el final de la trama, no escuchamos mas mensajes hasta completar tareas

                    TaskListFlag = false;

                }

                if (!msg_content.equals("END") && TaskListFlag == false ) {

                    // Si hemos recibido ya la primera posicion del TaskList, es decir, el numero de tareas que vienen
                    // recogidas en allCoordinates, ponemos TaskListFlag a true para indicar que el resto de mensajes
                    // con la ontologia "PlanCoordenadas" seran las coordenadas que conforman el plan

                    TaskListFlag = true;
                }


                //La comunicacion con el TransportAgent mediante gson parece tener problemas de lectura por espacios
                //en blanco o caracteres que este paquete no puede leer, se propone leer las posiciones una a una
                //dado que no deja de ser una lista de coordenadas.

                //Gson gson_tareas = new Gson();
                //StructTranspRequest javaTranspRequest = gson_tareas.fromJson(msg_content, StructTranspRequest.class);
                //String [] ListaTareas = javaTranspRequest.getTask();
                //System.out.println("Se ha recibido la siguiente lista de tareas: " + ListaTareas);

            }

          } else {

            System.out.println("Bloqueando el GWagentROS hasta recibir otro mensaje de TransportAgent");
            block();

          }
        }

      });

    }
}

