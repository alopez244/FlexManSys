package com.github.nombre_paquete.nombre_proyecto; 

import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher; //Esta es para publicar mensajes, importante
import org.ros.RosCore; // Esta libreria es para lo referente al maestro
import org.ros.node.*;

// Las siguientes librerias son para traer las funcionalidades propias de Agentes
// De esta forma, podremos comunicarnos con otros agentes
import jade.core.Agent;

// Nombre que toma el nodo en la red ROS
// El nombre que toma también sirve como clase, luego otro nodo podría usarlo como funcion
public class movini extends AbstractNodeMain {

  @Override
  public GraphName getDefaultNodeName() {	
    return GraphName.of("movini");
  }

//Dentro de la funcion OnStart se incluye el codigo a ejecutar durante la ejecucion
  @Override
  public void onStart(final ConnectedNode connectedNode) {

  /*  final Log log = connectedNode.getLog();//Registra el nodo
    //Crea una instancia de un publicista con el comando de debajo
    //Publica dentro del topico /move, mensajes del tipo String
    Publisher<std_msgs.String> publisher =
	connectedNode.newPublisher("move", std_msgs.String._TYPE);
    while (true) {
      //Creacion de un nuevo mensaje denominado "trajectory"
      std_msgs.String trajectory =  publisher.newMessage();
      trajectory.setData("Muevete brazo");
      publisher.publish(trajectory);
      log.info("Lo enviado es: \n" + trajectory.getData() + "\n");
    }*/

  }

  private Agent miAgente;
  public movini(Agent a, int n) {

  this.miAgente = a;
  //this.numero = Integer.toString(n);
  //Se define un roscore y el puerto al que se va a conectar el maestro  				
  RosCore rosCore = RosCore.newPublic(11311);
  //Se inicializa el maestro de ROS
  rosCore.start();
  try {
    //Se espera a que se inicialice el maestro de 
    rosCore.awaitStart();
  } catch (InterruptedException e) {
    e.printStackTrace();
  }


   //Se empieza configurando el nodo a ejecutar, en este caso, se crea una nueva 
   //configuracion denominada "nodeConfiguration" para guardar las variables de
   //configuracion.
   NodeConfiguration nodeConfiguration = NodeConfiguration.newPrivate();
   //Se obtiene la URI del maestro ROS
   nodeConfiguration.setMasterUri(rosCore.getUri());
   //Se establece el nombre del nodo a ejecutar
   nodeConfiguration.setNodeName(miAgente.getLocalName());
   //Establece en que nodo debe de ejecutarse la configuracion, en este caso NodeMain
   NodeMain nodeMain = (NodeMain) this;
   //Finalmente, se establece una variable en la cual se va a ejecutar el nodo
   //un nuevo tipo de variable denominada "nodeMain, que se ejecuta bajo la configuracion
   //nodeConfiguration
   NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
   nodeMainExecutor.execute(nodeMain, nodeConfiguration);


  }



}
