package com.github.rosjava.nombre_paquete.nombre_proyecto;

// ******************* General *************************

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;
import org.apache.commons.logging.Log;

// ******************* Messages *************************

// Libreria de creacion y casteo de tipos datos no oficiales
import org.ros.message.MessageFactory;

import custom_messages.Prueba;
import custom_messages.KobukiGeneral;
import custom_messages.KobukiObstacle;
import custom_messages.KobukiPosition;
import custom_messages.TimeDate;
import custom_messages.TransportUnitState;
import custom_messages.TransportPrivateState;

/*
*  Este codigo publica en los distintos campos de un mensaje customizado,
*  en este caso, publica en los campos de custom_message.TransportUnitState
* */


public class TalkerBorradorBorrador extends AbstractNodeMain {

  @Override

  public GraphName getDefaultNodeName() {

    return GraphName.of("TalkerBorrador");

  }

  @Override

  public void onStart(final ConnectedNode connectedNode) {

    final Publisher<custom_messages.TransportUnitState> publisher =
        connectedNode.newPublisher("/borrador/prueba", custom_messages.TransportUnitState._TYPE);
    // This CancellableLoop will be canceled automatically when the node shuts
    // down.

    final Log log = connectedNode.getLog(); // Para poder presentar informacion en pantalla

    connectedNode.executeCancellableLoop(new CancellableLoop() {
      private int sequenceNumber;
      private int numero;

      @Override
      protected void setup() {
        sequenceNumber = 0;
        numero = 1;

      }

      @Override
      protected void loop() throws InterruptedException {

        // Esta es la parte del publiciste

        // Instanciamos a la variable local "data" el tipo de mensaje que queremos leer,
        // en este caso custom_messages.Prueba

        /*
        MessageFactory topicMessageFactory = connectedNode.getTopicMessageFactory();
        custom_messages.Prueba data = topicMessageFactory.newFromType(custom_messages.Prueba._TYPE);
        data.setNumeroPrueba(3); //Variables con _ se tienen que declarar en el set junto y en mayusculas
        publisher.publish(data);
        sequenceNumber++;
        Thread.sleep(1000);
        */

        MessageFactory topicMessageFactory = connectedNode.getTopicMessageFactory();
        custom_messages.TransportUnitState data = topicMessageFactory.newFromType(custom_messages.TransportUnitState._TYPE);

        // Campos de KobukiGeneral
        data.getKobukiGeneral().setTransportUnitName("leonardo");
        data.getKobukiGeneral().setTransportUnitState("idle");
        data.getKobukiGeneral().setBattery(14);

        // Campos de KobukiObstacle
        data.getKobukiObstacle().setDetectedObstacleBumper(true);
        data.getKobukiObstacle().setDetectedObstacleCamera(true);

        // Campos de KobukiPosition
        data.getKobukiPosition().setTransportInDock(true);
        data.getKobukiPosition().setRecoveryPoint("D1");
        data.getKobukiPosition().setOdomX(3);
        data.getKobukiPosition().setOdomY(3);
        data.getKobukiPosition().setRotation(300);

        // Campos de OdroidDate
        data.getOdroidDate().setYear(2022);
        data.getOdroidDate().setMonth(1);
        data.getOdroidDate().setDay(25);
        data.getOdroidDate().setHour(11);
        data.getOdroidDate().setMinute(30);
        data.getOdroidDate().setSeconds(50);

        publisher.publish(data);
        sequenceNumber++;

        Thread.sleep(2000);


      }
    });

  }

}


