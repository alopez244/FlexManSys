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

import custom_messages.Prueba;
import custom_messages.KobukiGeneral;
import custom_messages.KobukiObstacle;
import custom_messages.KobukiPosition;
import custom_messages.TimeDate;
import custom_messages.TransportUnitState;
import custom_messages.TransportPrivateState;

/*
 *  Este codigo escucha de un topico con datos no oficiales y se guarda la
 *  variable escuchada, en este caso de custom_messages/Prueba
 * */

public class ListenerBorrador extends AbstractNodeMain {

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("listener_borrador");
  }

  @Override
  public void onStart(ConnectedNode connectedNode) {
    final Log log = connectedNode.getLog();
    Subscriber<custom_messages.Prueba> subscriber = connectedNode.newSubscriber("/borrador/prueba", custom_messages.Prueba._TYPE);
    subscriber.addMessageListener(new MessageListener<custom_messages.Prueba>() {

      @Override
      public void onNewMessage(custom_messages.Prueba message) {
        log.info("He escuchado: \"" + message.getNumeroPrueba() + "\"");
      }
    });
  }
}
