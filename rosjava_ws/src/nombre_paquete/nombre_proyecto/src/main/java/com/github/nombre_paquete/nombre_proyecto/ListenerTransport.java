package com.github.rosjava.nombre_paquete.nombre_proyecto;

// ******************* General *************************

import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

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
 *  Este codigo escucha de un topico con datos no oficiales y se guarda la
 *  todos los campos del topico escuchado en variables locales de la clase
 *  ListenerBorradorBorrador
 * */

public class ListenerTransport extends AbstractNodeMain {

  @Override

  public GraphName getDefaultNodeName() {

    return GraphName.of("listener_borrador");

  }

  @Override

  public void onStart(ConnectedNode connectedNode) {

    final Log log = connectedNode.getLog();

    Subscriber<turtlebot_transport_flexmansys.TransportUnitState> subscriber = connectedNode.newSubscriber(
            "/flexmansys/state/leonardo", turtlebot_transport_flexmansys.TransportUnitState._TYPE);

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
}
