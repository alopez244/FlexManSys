package com.github.rosjava.nombre_paquete.nombre_proyecto;

// ******************* General *************************

import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;
import org.ros.node.topic.Publisher;
import org.ros.concurrent.CancellableLoop;

// ******************* Messages *************************

// Libreria de creacion y casteo de tipos datos no oficiales
import org.ros.message.MessageFactory;

import turtlebot_transport_flexmansys.Prueba;
import turtlebot_transport_flexmansys.KobukiGeneral;
import turtlebot_transport_flexmansys.KobukiObstacle;
import turtlebot_transport_flexmansys.KobukiPosition;
import turtlebot_transport_flexmansys.TimeDate;
import turtlebot_transport_flexmansys.TransportUnitState;
import turtlebot_transport_flexmansys.TransportPrivateState;

/*
 *  Este codigo es el borrador correspondiente del nodo ROS GWAgent
 *  que reside en el paquete fms_transp y que se emplea para la comunicacion
 *  entre agentes y entorno ROS
 * */

public class GWAgentBorrador extends AbstractNodeMain {

  @Override

  public GraphName getDefaultNodeName() {

    // Definimos el nombre del nodo GWAgent
    return GraphName.of("GWAgentBorrador");

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

        private int sequenceNumber;
        private String Coordenada;

        @Override
        protected void setup () {

          // Variables de inicializacion, esta funcion se ejecuta una unica vez nada mas arrancar
          // la clase ROSGateway

          sequenceNumber = 0;

          // Iniciamos la coordenada a NONE hasta que no recibamos desde el agente correspondiente
          // una coordenada en especifico a mandar a la unidad de transporte

          Coordenada = "NONE";

        }

        @Override
        protected void loop () throws InterruptedException {

          // Publicamos al nodo de /flexmansys/coordenada/leonardo

          std_msgs.String str = publisher.newMessage();
          str.setData(Coordenada);
          publisher.publish(str);
          sequenceNumber++;
          Thread.sleep(1000);

          // Indicamos lectura de topico

          subscriber.addMessageListener(new MessageListener<turtlebot_transport_flexmansys.TransportUnitState>() {

            @Override
            public void onNewMessage(turtlebot_transport_flexmansys.TransportUnitState TransportMessage) {

              // Entramos aqui cada vez que escucha un nuevo mensaje

              // Para escuchar todo el mensaje podemos poner unicamente TransportMessage, la variable que hemos
              // empleado para instanciar los campos recibidos cuando se lee el topico
              // log.info("He escuchado: \"" + message + "\"");

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


              log.info("*************** GW AGENT ***************" + "\n" +
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
            }
          });

        }

      });
    }

  }

