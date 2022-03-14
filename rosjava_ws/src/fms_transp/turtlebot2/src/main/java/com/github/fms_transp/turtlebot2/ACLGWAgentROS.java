package com.github.rosjava.fms_transp.turtlebot2;

// ******************* GENERAL *************************

import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;
import org.ros.node.topic.Publisher;
import org.ros.concurrent.CancellableLoop;
import org.ros.address.InetAddressFactory;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.*;

// ******************* MESSAGES *************************

// Libreria de creacion y casteo de tipos datos no oficiales
import org.ros.message.MessageFactory;

// Mensaje customizado para la comunicacion agentes <-> transportes
import turtlebot_transport_flexmansys.Prueba;
import turtlebot_transport_flexmansys.KobukiGeneral;
import turtlebot_transport_flexmansys.KobukiObstacle;
import turtlebot_transport_flexmansys.KobukiPosition;
import turtlebot_transport_flexmansys.TimeDate;
import turtlebot_transport_flexmansys.TransportUnitState;
import turtlebot_transport_flexmansys.TransportPrivateState;

// Libreria para emplear JSON

import com.google.gson.Gson;

// ****************** JADE *******************************

import java.net.URI;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;
import jade.wrapper.gateway.JadeGateway;
import jade.util.leap.Properties;
import jade.core.Profile;
import org.ros.address.InetAddressFactory;

import com.github.rosjava.fms_transp.turtlebot2.StructCommand;
import com.github.rosjava.fms_transp.turtlebot2.StructTransportUnitState;
import com.github.rosjava.fms_transp.turtlebot2.GWagentROS;

/*
 *  OLD: GWAgent
 *  NEW: ACLGWagentROS
 * */


/*
 *  La funcion de este codigo es la de ejecutar un nodo ROS denominado GWAgent.
 *  Su funcion, es la de ejecutar un nodo ROS que instancie una clase tipo "Agente",
 *  o "GatewayAgent" a traves de la clase GWagentROS, donde se inicializaran todos los
 *  parametros necesarios para la comunicacion con el agente de transporte. Por otro lado,
 *  su funcion es la de escuchar lo publicado en el topico correspondiente del estado de las
 *  unidades de transporte: /flexmansys/state/leonardo. El mensaje escuchado en dicho topico,
 *  se convierte a formato ACL para mandarlo de vuelta al mismo agente de transporte desde el
 *  recibimos las coordenadas.
 * */

public class ACLGWAgentROS extends AbstractNodeMain {

  public ACLGWAgentROS() {

    try {
      //Inicializamos el apendice de agente de este nodo ROS instanciando la clase GWagentROS
      this.jadeInit();

      //Al ejecutar el roscore antes que el GWAgent, no es necesario un rosinit, solo si no se hace
      //un rosrun previo. Si se llama desde java en vez de rosrun, es necesario el ros.Init()
      this.rosInit();

    }
    catch(Exception e) {
      System.out.println(e.getMessage());
    }
  }

  private void jadeInit() throws Exception {

    // Obtenemos el host donde operan los agentes
    String host = InetAddressFactory.newNonLoopback().getHostName();
    // Puerto por defecto de comunicacion de los agentes, no tocar
    String port = "1099";

    // Definimos las propiedades de comunicacion entre los agentes
    Properties pp = new Properties();
    pp.setProperty(Profile.MAIN_HOST, host);
    //pp.setProperty(Profile.MAIN_HOST, "10.109.11.45");

    System.out.println("*********************");
    System.out.println(host);
    System.out.println("*********************");

    pp.setProperty(Profile.MAIN_PORT, port);
    pp.setProperty(Profile.LOCAL_PORT, port);

    // Creamos un contenedor, el cual por defecto en JADE anade "Control" al inicio
    // Lo modificamos para T_01
    java.lang.String containerName = "GatewayContT_01";
    pp.setProperty(Profile.CONTAINER_NAME, containerName);

    // Aqui es donde se instancia a la clase GWagentROS
    JadeGateway.init("com.github.rosjava.fms_transp.turtlebot2.GWagentROS", pp);

    // Le definimos a traves del comando de tipo StructCommand a la clase GWagentROS que debe
    // de realizar la accion "init", es decir, inicializarse. A su vez, si entramos dentro de la
    // clase, se vera que simplemente se especifica por pantalla que el agente Gateway se ha
    // inicializado.

    StructCommand command = new StructCommand();
    command.setAction("init");
    JadeGateway.execute(command);

  }

  private void rosInit() throws Exception {
    String host = InetAddressFactory.newNonLoopback().getHostName();
    String port = "11311";
    String masterURI_str = "http://" + host + ":" + port;
    URI masterURI = new URI(masterURI_str);

    NodeConfiguration nodeConfiguration = NodeConfiguration.newPrivate();
    nodeConfiguration.setMasterUri(masterURI);
    nodeConfiguration.setNodeName("GWAgent");

    NodeMain nodeMain = (NodeMain) this;
    NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
    nodeMainExecutor.execute(nodeMain, nodeConfiguration);
  }

  @Override
  public GraphName getDefaultNodeName() {

    // Definimos el nombre del nodo GWAgent
    return GraphName.of("GWAgent");

  }

  @Override
  public void onStart(ConnectedNode connectedNode) {

    // Para poder mostrar datos por pantalla

    final Log log = connectedNode.getLog();

    // Definimos el publicista en el nodo de coordenadas

    final Publisher<std_msgs.String> publisher =
            connectedNode.newPublisher("/flexmansys/coordenada/leonardo", std_msgs.String._TYPE);

    // Definimos la suscripcion al nodo de estado del transporte leonardo

    final Subscriber<turtlebot_transport_flexmansys.TransportUnitState> subscriber = connectedNode.newSubscriber(
            "/flexmansys/state/leonardo", turtlebot_transport_flexmansys.TransportUnitState._TYPE);

      connectedNode.executeCancellableLoop(new CancellableLoop() {

        private String Coordenada;
        StructTransportUnitState TUS_object = new StructTransportUnitState();
        boolean TUS_object_flag = false;

        private GWagentROS send_message = new GWagentROS();

        @Override
        protected void setup () {

          // Iniciamos la coordenada a NONE hasta que no recibamos desde el agente correspondiente
          // una coordenada en especifico a mandar a la unidad de transporte

          Coordenada = "NONE";

          // Creacion de objeto con una estructura que guarde los datos obtenidos del topico
          // /flexmansys/state/leonardo

        }

        @Override
        protected void loop () throws InterruptedException {

          // Definimos una variable local con la estructura StructCommand
          StructCommand command = new StructCommand();

          // Se define una accion "recv" para que la clase GWagentROS sepa que debe de verificar
          // si hay un mensaje en JADE que cumpla todos los requisitos (conversationID, ontology...)
          // y rellenar el contenido de command con el contenido del mensaje que reside en JADE

          command.setAction("recv");

          // Obligamos al nodo ROS, a traves de su instancia GWagentROS a ejecutar el comando
          // previamente definido, recv, o recibir mensaje ACL

          try {
            JadeGateway.execute(command);
          } catch(Exception e) {
            System.out.println(e.getMessage());
          }

          // Obtenemos el contenido de recv de vuelta y lo guardamos en el objeto msg_content

          String msg_content = command.getContent();

          if(msg_content != null) {

            // Publicamos en el topico /flexmansys/coordenada/leonardo la coordenada recibida
            // desde el TransportAgent

            std_msgs.String destination = publisher.newMessage();
            destination.setData(msg_content);
            publisher.publish(destination);

          }

          Thread.sleep(2000);

          // Indicamos lectura de topico /flexmansys/state/leonardo y guardamos sus campos en
          // variables locales de la clase GWAgent.java

          subscriber.addMessageListener(new MessageListener<turtlebot_transport_flexmansys.TransportUnitState>() {

            @Override
            public void onNewMessage(turtlebot_transport_flexmansys.TransportUnitState TransportMessage) {

              // Entramos aqui cada vez que escucha un nuevo mensaje

              // Para escuchar todo el mensaje podemos poner unicamente TransportMessage, la variable que hemos
              // empleado para instanciar los campos recibidos cuando se lee el topico

              // Campos de KobukiGeneral
              String transport_unit_name = TransportMessage.getKobukiGeneral().getTransportUnitName();
              String transport_unit_state = TransportMessage.getKobukiGeneral().getTransportUnitState();
              Float battery = TransportMessage.getKobukiGeneral().getBattery();

              // Campos de KobukiObstacle

              boolean detected_obstacle_bumper = TransportMessage.getKobukiObstacle().getDetectedObstacleBumper();
              boolean detected_obstacle_camera = TransportMessage.getKobukiObstacle().getDetectedObstacleCamera();

              // Campos de KobukiPosition

              boolean transport_in_dock = TransportMessage.getKobukiPosition().getTransportInDock();
              String recovery_point = TransportMessage.getKobukiPosition().getRecoveryPoint();
              Double odom_x = TransportMessage.getKobukiPosition().getOdomX();
              Double odom_y = TransportMessage.getKobukiPosition().getOdomY();
              Double rotation = TransportMessage.getKobukiPosition().getRotation();

              // Campos de OdroidDate

              int year = TransportMessage.getOdroidDate().getYear();
              int month = TransportMessage.getOdroidDate().getMonth();
              int day = TransportMessage.getOdroidDate().getDay();
              int hour = TransportMessage.getOdroidDate().getHour();
              int minute = TransportMessage.getOdroidDate().getMinute();
              int seconds = TransportMessage.getOdroidDate().getSeconds();

              // Mostramos por pantalla los datos leidos del topico

              log.info("\n" +
                      "*************** GW AGENT ***************" + "\n" +
                      "Nombre AGV: " + transport_unit_name + "\n" +
                      "Estado AGV: " + transport_unit_state + "\n" +
                      "Bateria AGV: " + Float.toString(battery) + "\n" +
                      "---------------------------------------------------" + "\n" +
                      "Obstaculo bumper: " + Boolean.toString(detected_obstacle_bumper) + "\n" +
                      "Obstaculo camera: " + Boolean.toString(detected_obstacle_camera) + "\n" +
                      "---------------------------------------------------" + "\n" +
                      "AGV en Dock: " + Boolean.toString(transport_in_dock) + "\n" +
                      "Recovery Point: " + recovery_point + "\n" +
                      "Odom X: " + Double.toString(odom_x) + "\n" +
                      "Odom Y: " + Double.toString(odom_y) + "\n" +
                      "Rotation: " + Double.toString(rotation) + "\n" +
                      "---------------------------------------------------" + "\n" +
                      "Year: " + Integer.toString(year) + "\n" +
                      "Month: " + Integer.toString(month) + "\n" +
                      "Day: " + Integer.toString(day) + "\n" +
                      "Hour: " + Integer.toString(hour) + "\n" +
                      "Minute: " + Integer.toString(minute) + "\n" +
                      "Seconds: " + Integer.toString(seconds) + "\n"

              );

              // Campos de KobukiGeneral
              TUS_object.setTransport_unit_name(transport_unit_name);
              TUS_object.setTransport_unit_state(transport_unit_state);
              TUS_object.setBattery(battery);

              // Campos de KobukiObstacle
              TUS_object.setDetected_obstacle_bumper(detected_obstacle_bumper);
              TUS_object.setDetected_obstacle_camera(detected_obstacle_camera);

              // Campos de KobukiPosition
              TUS_object.setTransport_in_dock(transport_in_dock);
              TUS_object.setRecovery_point(recovery_point);
              TUS_object.setOdom_x(odom_x);
              TUS_object.setOdom_y(odom_y);
              TUS_object.setRotation(rotation);

              // Campos de OdroidDate
              TUS_object.setYear(year);
              TUS_object.setMonth(month);
              TUS_object.setDay(day);
              TUS_object.setHour(hour);
              TUS_object.setMinute(minute);
              TUS_object.setSeconds(seconds);

              // Activamos el flag que indica que los campos del objeto TUS se han actualizado
              TUS_object_flag = true;

            }
          });

           if (TUS_object_flag == true) {

             // Cambiamos la actividad que quiere realizar el agente a "send"
             command.setAction("send");

             // Enviamos TUS_object en formato StructTransportUnitState al apendice GWagentROS
             // mediante la estructura  de datos que ambos comparten, StructCommand.
             // Posteriormente, GWagentROS empleara el campo Transport_state de esa estructura
             // para obtener convertir a formato JSON.  es decir, una especie de string que
             // cuenta con una estructura tipo diccionario estilo python.
             command.setTransport_state(TUS_object);

            try {
              JadeGateway.execute(command);
            } catch(Exception e) {
              System.out.println(e.getMessage());
            }

            // Ya se han enviado al apendice GWagentROS los campos leidos de TUS_object, por los
            // que desactivamos el flag que habilita el envio de TUS_object
            TUS_object_flag = false;

            // No es necesario cambiar la accion a "recv", puesto que al reanudar este loop ya
            // se indica al principio.

          }

        }
      });
    }

  public static void main(String[] args) {
    ACLGWAgentROS GWAgentObject = new ACLGWAgentROS();
  }

  }

