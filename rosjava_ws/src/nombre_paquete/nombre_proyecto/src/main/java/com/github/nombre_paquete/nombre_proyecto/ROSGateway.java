package com.github.rosjava.nombre_paquete.nombre_proyecto;

// Publisher
import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

// Subscriber
import org.ros.node.topic.Subscriber;
import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;

// Messages

//import nombre_paquete.Prueba;


// Creamos una clase llamada ROSGateway que se instancia de AbstractNodeMain

public class ROSGateway extends AbstractNodeMain {

  @Override
  public GraphName getDefaultNodeName() {

    // Definimos el nombre del nodo en la red ROS
    return GraphName.of("ROSGateway");

  }

  @Override

  public void onStart(final ConnectedNode connectedNode) {

    // Publicamos al nodo /flexmansys/coordenada/leonardo un mensaje del tipo String

    final Publisher<std_msgs.String> publisher =
        connectedNode.newPublisher("/flexmansys/coordenada/leonardo", std_msgs.String._TYPE);

    // Nos suscribiremos al nodo /flexmansys/prueba

    final Log log = connectedNode.getLog(); // Para poder presentar informacion en pantalla

    Subscriber<std_msgs.String> subscriber =
        connectedNode.newSubscriber("/flexmansys/prueba", std_msgs.String._TYPE);

    // La funcion executeCancellableLoop nos permite ejecutar un loop que sea cancelable,
    // es mas, cuando se detenga la ejecucion del nodo ROSGateway que hemos inicializado,
    // la ejecucion del loop se detendra automaticamente

    connectedNode.executeCancellableLoop(new CancellableLoop() {
      private int sequenceNumber;
      private String Coordenada;

      @Override
      protected void setup() {

         // Variables de inicializacion, esta funcion se ejecuta una unica vez nada mas arrancar
         // la clase ROSGateway

        sequenceNumber = 0;

        // Iniciamos la coordenada a NONE hasta que no recibamos desde el agente correspondiente
        // una coordenada en especifico a mandar a la unidad de transporte

        Coordenada = "NONE";

      }

      @Override
      protected void loop() throws InterruptedException {

        // Publicamos al nodo de /flexmansys/coordenada/leonardo

        std_msgs.String str = publisher.newMessage();
        //str.setData("Hello world! " + sequenceNumber);
        str.setData(Coordenada);
        publisher.publish(str);
        sequenceNumber++;
        Thread.sleep(1000);

        // Hay que declarar la suscripcion cada vez que queremos leer el topico

        Subscriber<std_msgs.String> subscriber =
            connectedNode.newSubscriber("/flexmansys/prueba", std_msgs.String._TYPE);

        subscriber.addMessageListener(new MessageListener<std_msgs.String>() {

         @Override
         public void onNewMessage(std_msgs.String message) {
            log.info("He escuchado: \"" + message.getData() + "\"");

            // Simulando que hemos recibido un mensaje ACL, sobreescribimos la coordenada que publicamos
            // al topico /flexmansys/coordenada/leonardo

            Coordenada = message.getData();

         }
        });

      }

    });

  }

}