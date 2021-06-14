package es.ehu.domain.manufacturing.agents.cognitive;

import es.ehu.domain.manufacturing.behaviour.InformAgent;
import es.ehu.domain.manufacturing.utilities.StructMessage;
import jade.core.Profile;
import jade.core.behaviours.Behaviour;
import jade.util.leap.Properties;
import jade.wrapper.gateway.GatewayAgent;
import jade.wrapper.gateway.JadeGateway;
import org.ros.RosCore;
import org.ros.message.MessageListener;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.*;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import social_msgs.social;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ROSJADEgw extends AbstractNodeMain { //Nodo Rosjava del agente, interfaz entre agentes y ROS.
    // Gateway between non-JADE and a JADE agent system.
    //Crear comportamientos adecuados que ejecuten los comandos que debe emitir al sistema JADE y pasarlos como parametro en execute()
    private GatewayAgent myAgent;
    private  Boolean workingFlag = false; //Flag que se activa cuando el transporte esta trabajando.

    private ConnectedNode connectedNode;
    private boolean connected;

    /** Subscriber in the {@code TOPIC2/<agent_name>} topic */
    private Subscriber<social> suscriptor;

    /** Subscriber in the {@code TOPIC3/<agent_name>} topic */
    private Subscriber<social> suscriptor2;

    /** Publisher in the {@code TOPIC1} topic */
    private Publisher<social> publicista;

    public ROSJADEgw (GatewayAgent a){
        this.myAgent=a;
        //this.controlledBehaviour=null;
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
        //lamar al metodo init dentro de ROSJADEgw, para que se incie el GWAgentROS

       //pruebas de ejecucion
       // init();
    }
    @Override
    public GraphName getDefaultNodeName() {

        return GraphName.of(myAgent.getLocalName());
    }
    @Override
    public void onStart(final ConnectedNode connectedNode) {

        //System.out.println("running");
        this.connectedNode = connectedNode;
        connected = true;
        this.publicista = connectedNode.newPublisher("TOPICO1", social._TYPE);
        this.suscriptor = connectedNode.newSubscriber("TOPICO2", social._TYPE);
        this.suscriptor2 = connectedNode.newSubscriber("TOPICO3", social._TYPE);

        //esperar a que el suscriptor reciba un mensaje
        suscriptor.addMessageListener(new MessageListener<social>() {
            @Override
            public void onNewMessage(social msg) {
                send(msg.getContent().get(0));

            }
        });
        suscriptor2.addMessageListener(new MessageListener<social>() {
            @Override
            public void onNewMessage(social msg) {
                send(msg.getContent().get(0));
                //managereceivedmsg(msg);
            }
        });

    }

    public void enviarMSG (Ros_Jade_Msg data) {  //ACL --> ROS  ,  Array to ArrayList

        social msg = connectedNode.getTopicMessageFactory().newFromType(social._TYPE);
        msg.setOntology(data.getOntology());
        msg.setConversationID(data.getConversationID());
        msg.setSender(this.getDefaultNodeName().toString());
        msg.getHeader().setStamp(Time.fromMillis(System.currentTimeMillis()));

        msg.setContent(Arrays.asList(data.getContent()));
        publicista.publish(msg);
    }


/// de antes


    public static void init(){


        //unir ROSJADEgw con GWAgentRos
        System.out.println("En rosjadegw");
        //Unirlo al contenedor que asumimos que esta en localHost, port 1099
        String host = "192.168.187.131"; ///prueba
        String port = "1099";//prueba
        Properties pp = new Properties();
        pp.setProperty(Profile.MAIN_HOST, host);
//
        pp.setProperty(Profile.MAIN_PORT, port);
        pp.setProperty(Profile.LOCAL_PORT, port);

        String containerName = "GatewayCont1";   // se define el nombre del contenedor donde se inicializara el agente
        pp.setProperty(Profile.CONTAINER_NAME, containerName);
        JadeGateway.init("es.ehu.domain.manufacturing.agents.cognitive.GWAgentROS",pp);

        StructMessage strMessage = new StructMessage();
        strMessage.setAction("init");
        try {
            JadeGateway.execute(strMessage);    // calls processCommand method of Gateway Agent
        } catch(Exception e) {
            e.printStackTrace();

        }

        System.out.println("<-Java Agent Init");
    }

    //Function for reading the data received in ACL messages
    public static String recv() {  //Agent --> ROS
        String recvMs;
        StructMessage strMessage = new StructMessage();
        strMessage.setAction("receive");
        try {
            JadeGateway.execute(strMessage);
        } catch(Exception e) {
            System.out.println(e);
        }
        if(strMessage.readNewData()){
            recvMs=strMessage.readMessage();
            System.out.println("--Received: " + recvMs);

        }else{
            System.out.println("--No answer");
            recvMs="";
        }
        System.out.println("<-Java recv");
        return recvMs;
    }

    public static void send(String msgOut) {  //Sends the data String that has been given from kobuki ROS-->Agent

        //crea mensaje ACL que se lo envia al TransportAgent
        StructMessage strMessage = new StructMessage();
        strMessage.setAction("send");
        strMessage.setMessage(msgOut);
        strMessage.setPerformative(7); //INFORM

        //Dependiendo del tipo de mensaje recivido cambiar el performative
       /*
        if(msgOut.contains("Received")){    // Depending of the message type (confirmation or data exchanging) the performative will be different
            strMessage.setPerformative(7);  // Performative = INFORM
        } else {
            strMessage.setPerformative(16); // Performative = REQUEST
        }

        */

        System.out.println("--Sended message: " + strMessage.readMessage());
        try {
            JadeGateway.execute(strMessage);    // Llamar a GWAgentROS para que envie el mensaje finalmente.
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("<-Java Send");
    }


}


