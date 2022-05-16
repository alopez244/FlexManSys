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
 *  Este codigo publica en un topico con un mensaje customizado, en este caso,
 *  en un topico cuyo tipo es de custom_messages/Prueba
 * */


public class TalkerBorrador extends AbstractNodeMain {

  @Override

  public GraphName getDefaultNodeName() {

    return GraphName.of("TalkerBorrador");

  }

  @Override

  public void onStart(final ConnectedNode connectedNode) {

    final Publisher<custom_messages.Prueba> publisher =
        connectedNode.newPublisher("/borrador/prueba", custom_messages.Prueba._TYPE);
    // This CancellableLoop will be canceled automatically when the node shuts
    // down.
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

        MessageFactory topicMessageFactory = connectedNode.getTopicMessageFactory();
        custom_messages.Prueba data = topicMessageFactory.newFromType(custom_messages.Prueba._TYPE);
        data.setNumeroPrueba(3); //Variables con _ se tienen que declarar en el set junto y en mayusculas
        publisher.publish(data);
        sequenceNumber++;
        Thread.sleep(1000);

      }
    });

  }

}


