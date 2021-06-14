package es.ehu.domain.manufacturing.agents.cognitive;
import es.ehu.domain.manufacturing.agents.TransportAgent;
import es.ehu.domain.manufacturing.behaviour.InformAgent;
import es.ehu.domain.manufacturing.behaviour.SendTaskBehaviour;
import jade.wrapper.gateway.GatewayAgent;
import org.ros.RosCore;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.*;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import org.ros.message.Time;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import social_msgs.social;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.TimeUnit;

public class Ros_Jade_Dummy extends AbstractNodeMain{ // SIMULADOR KOBUKI
    /** JADE Agent represented in the ROS platform */
    private Agent myAgent;
    /** Behaviour to wake up the agent if there are new events */
    private Behaviour controlledBehaviour;


    private Publisher<social> publicista2;
    /** Publisher in the {@code TOPIC4} topic */
    private Publisher<social> publicista3;
    /** Subscriber in the {@code TOPIC1/<agent_name>} topic */
    private Subscriber<social> suscriptor;


    private ConnectedNode connectedNode;
    private boolean connected;



    public Ros_Jade_Dummy(Agent a){
        this.myAgent=a;
        //this.controlledBehaviour=null; quitar para prueba Iñi
        this.controlledBehaviour=null;
        RosCore rosCore = null;
        try {
            rosCore.newPublic(11311);
            rosCore.start();
            rosCore.awaitStart(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            rosCore = null;
        }
        NodeMain nodeMain = (NodeMain) this;
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPrivate();
        nodeConfiguration.setNodeName(myAgent.getLocalName());
        if (rosCore != null) {
            nodeConfiguration.setMasterUri(rosCore.getUri());
        }
        NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
        nodeMainExecutor.execute(nodeMain, nodeConfiguration);
    }
  /*  public  Ros_Jade_Dummy(Agent a, Behaviour b){
        this(a);
        this.controlledBehaviour = b;
    }*/
    @Override
    public GraphName getDefaultNodeName() {

        return GraphName.of(myAgent.getLocalName());
    }

    /**
     * Initialization of the ROS variables themselves.
     *
     * @param connectedNode Node defined in the ROS graph and connected to MASTER.
     */
    @Override
    public void onStart(final ConnectedNode connectedNode) {


        System.out.println("Kobuki simulation running");

        this.connectedNode = connectedNode;
        connected = true;

        this.suscriptor = connectedNode.newSubscriber("TOPICO1", social._TYPE);
        this.publicista2 = connectedNode.newPublisher("TOPICO2", social._TYPE);
        this.publicista3 = connectedNode.newPublisher("TOPICO3", social._TYPE);

        //esperar a que el suscriptor reciba un mensaje
        suscriptor.addMessageListener(new MessageListener<social>() {
            @Override
            public void onNewMessage(social msg) {

                managereceivedmsg(msg);
            }
        });

    }


    private void managereceivedmsg(social msg) {
        //leer topico, recibir posicion de entrada y salida leyendo del topico
        Ros_Jade_Msg aux=new Ros_Jade_Msg(msg.getConversationID(),msg.getOntology(),msg.getContent().toArray(new String[0]));
        //List msg_recv= msg.getContent();
        System.out.println("Imprimir contenido recibido en el Kobuki "+ Arrays.toString(aux.getContent()));
        //....

        //Responder publicando en otro topico mensaje de confirmacion y nivel de bateria(azar).

        social msg2 =connectedNode.getTopicMessageFactory().newFromType(social._TYPE);
        msg2.setOntology("data");
        ArrayList<String> strings = new ArrayList<String>();  //ROS msg ArrayList<String>
        strings.add("received");
        strings.add("true");
        strings.add("availability");
        strings.add("true");
        strings.add("battery");
        strings.add("50");
        msg2.setContent(strings);
        msg2.setConversationID(msg.getConversationID());
        msg2.setSender(this.getDefaultNodeName().toString());
        msg2.getHeader().setStamp(Time.fromMillis(System.currentTimeMillis()));
        this.publicista2.publish(msg2);
        //esperar unos segundos mediante delay(simular desplazamiento)

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Enviar segundo mensaje de confirmacion de que tarea completa y nivel bateria
        social msg3=connectedNode.getTopicMessageFactory().newFromType(social._TYPE);
        msg3.setOntology("data");
        ArrayList<String> strings2 = new ArrayList<String>(); //ROS msg ArrayList<String>
        strings.add("received");
        strings.add("true");
        strings.add("availability");
        strings.add("true");
        strings.add("battery");
        strings.add("45");
        msg3.setContent(strings2);
        msg3.setConversationID("2");
        msg3.setSender(this.getDefaultNodeName().toString());
        msg3.getHeader().setStamp(Time.fromMillis(System.currentTimeMillis()));
        this.publicista3.publish(msg3);

    }

}


















