package es.ehu.domain.manufacturing.agents.cognitive;

import es.ehu.domain.manufacturing.utilities.StructMessage;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.behaviours.Behaviour;
import jade.util.leap.Properties;
import jade.wrapper.gateway.JadeGateway;
import org.ros.RosCore;
import org.ros.message.MessageListener;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.*;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import social_msgs.social;
import std_msgs.String;


import java.util.Arrays;

public class ROSJADEgw extends AbstractNodeMain {
    public static boolean workingFlag =false;
    //Nodo Rosjava del agente, interfaz entre agentes y ROS.
    // Gateway between non-JADE and a JADE agent system.
    //Crear comportamientos adecuados que ejecuten los comandos que debe emitir al sistema JADE y pasarlos como parametro en execute()
    private Agent myAgent;

    public java.lang.String message;

    private ConnectedNode connectedNode;
    private boolean connected;

    /** Subscriber in the {@code TOPIC2/<agent_name>} topic */
    private Subscriber<std_msgs.String> suscriptor;
    public Boolean sendMsgFlag=false;
    //public std_msgs.String message;

    /** Subscriber in the {@code TOPIC3/<agent_name>} topic */
    private Subscriber<std_msgs.String> suscriptor2;
    /** Behaviour to wake up the agent if there are new events */
    private Behaviour controlledBehaviour;

    /** Publisher in the {@code TOPIC1} topic */
    private Publisher<std_msgs.String> publicista;


    public ROSJADEgw (   ){

        System.out.println("Comienza arranque de Nodo pasarela");
        //this.myAgent=a;
        this.controlledBehaviour=null;


        RosCore rosCore = RosCore.newPublic(11311);
        rosCore.start();
        try {
            rosCore.awaitStart();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        NodeMain nodeMain = (NodeMain) this;
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPrivate();
        //nodeConfiguration.setNodeName(myAgent.getLocalName());
        nodeConfiguration.setNodeName("ROSJADEgw");

        if (rosCore != null) {
            nodeConfiguration.setMasterUri(rosCore.getUri());
        }
        NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
        nodeMainExecutor.execute(nodeMain, nodeConfiguration);



        System.out.println("Nodo pasarela iniciado");
        //lamar al metodo init dentro de ROSJADEgw, para que se incie el GWAgentROS

        //pruebas de ejecucion

        //haciendo primero en main instanciar
        //init();
    }
    @Override
    public GraphName getDefaultNodeName() {

        //return GraphName.of(myAgent.getLocalName());
        return GraphName.of("ROSJADEgw");
    }
    @Override
    public void onStart(final ConnectedNode connectedNode) {

        //System.out.println("en onStart, preparando suscriptores y publicistas del ROSJADEgw");
        this.connectedNode = connectedNode;
        connected = true;
        this.publicista = connectedNode.newPublisher("TOPICO1", std_msgs.String._TYPE);
        this.suscriptor = connectedNode.newSubscriber("TOPICO2", std_msgs.String._TYPE);
        this.suscriptor2 = connectedNode.newSubscriber("TOPICO3", std_msgs.String._TYPE);
        //
        //esperar a que el suscriptor reciba un mensaje

        suscriptor.addMessageListener(new MessageListener<std_msgs.String>() {
            @Override
            public void onNewMessage(std_msgs.String a) {

                System.out.println("Mensaje 1 del Kobuki recibido correctamente :"+ a.getData());

                message = a.getData();
                sendMsgFlag=true;
                System.out.println("ojo");
                ROSJADEgw.send(message);


                /// O /
                //ROSJADEgw.send(msg.getContent().get(0));
                // send(msg.getContent().get(0));


            }
        });
        suscriptor2.addMessageListener(new MessageListener<std_msgs.String>() {
            @Override
            public void onNewMessage(std_msgs.String a) {
                //send(msg.getContent().get(0)); // mala pi
                System.out.println("Mensaje 2 del Kobuki recibido correctamente :" +a.getData());
                //managereceivedmsg(msg);
                message = a.getData();
                sendMsgFlag=true;
                workingFlag=false;
                ROSJADEgw.send(message);
            }
        });

    }
    public static Boolean getWorkingFlag(){
        return workingFlag;
    }
    public void setWorkingFlag(Boolean state){
        workingFlag=state;
    }

    public java.lang.String getMessage(){
        return this.message;
    }
    public Boolean getSendMsgFlag(){
        return this.sendMsgFlag;
    }
    public void setSendMsgFlag(Boolean state){
        this.sendMsgFlag=state;
    }

    public void enviarMSG (Ros_Jade_Msg data) {  //ACL --> ROS  ,  Array to ArrayList

        //System.out.println("Publicando mensaje del Agente Transporte en el primer topico");
        //std_msgs.String msg = connectedNode.getTopicMessageFactory().newFromType(std_msgs.String._TYPE);
        std_msgs.String msg= publicista.newMessage();

        //msg.setOntology(data.getOntology());
        //msg.setConversationID(data.getConversationID());
        //msg.setSender(this.getDefaultNodeName().toString());
        //msg.getHeader().setStamp(Time.fromMillis(System.currentTimeMillis()));

        //sg.setContent(Arrays.asList(data.getContent()));
        msg.setData(data.getContent()[0]);
        //msg.setData("Hola mundo");

        publicista.publish(msg);
        System.out.println("Mensaje publicado en topico1 "+ msg.getData());
    }


/// de antes


    public static void init(){


        //unir ROSJADEgw con GWAgentRos
        System.out.println("En ROSJADEgw INIT");
        //Unirlo al contenedor que asumimos que esta en localHost, port 1099
        java.lang.String host = "192.168.187.131"; ///
        java.lang.String port = "1099";//

        Properties pp = new Properties();
        pp.setProperty(Profile.MAIN_HOST, host);
        pp.setProperty(Profile.MAIN_PORT, port);
        pp.setProperty(Profile.LOCAL_PORT, port);

        java.lang.String containerName = "GatewayCont1";   // se define el nombre del contenedor donde se inicializara el agente
        pp.setProperty(Profile.CONTAINER_NAME, containerName);
        JadeGateway.init("es.ehu.domain.manufacturing.agents.cognitive.GWAgentROS",pp);
        StructMessage strMessage = new StructMessage();
        strMessage.setAction("init");
        try {
            JadeGateway.execute(strMessage);// calls processCommand method of Gateway Agent

        } catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("<-Java Agent Init");
    }

    //Function for reading the data received in ACL messages
    public static java.lang.String recv() {  //Agent --> ROS

        System.out.println("en recv");
        if (workingFlag!=true) {
//            workingFlag=true;
            java.lang.String host = "192.168.187.131"; ///
            java.lang.String port = "1099";//

            Properties pp = new Properties();
            pp.setProperty(Profile.MAIN_HOST, host);
            pp.setProperty(Profile.MAIN_PORT, port);
            pp.setProperty(Profile.LOCAL_PORT, port);

            java.lang.String containerName = "GatewayCont1";   // se define el nombre del contenedor donde se inicializara el agente
            pp.setProperty(Profile.CONTAINER_NAME, containerName);
            //JadeGateway.init("es.ehu.domain.manufacturing.agents.cognitive.GWAgentROS",pp);
            java.lang.String recvMs;
            StructMessage strMessage = new StructMessage();
            strMessage.setAction("receive");
            try {
                // System.out.println("recv jadeGateway.execute");
                JadeGateway.execute(strMessage);
                //System.out.println("fuera de recv jadeexecute");
            } catch (Exception e) {

                System.out.println("Error jadeGateway.execute : " + e);
            }
            try {
                Thread.sleep(3);
            } catch (InterruptedException ie)
            {
                System.out.println("Scanning...");
            }

            if (strMessage.readNewData()) {
                recvMs = strMessage.readMessage();
                workingFlag=true;
                //  System.out.println("--Received oooo: " + recvMs);

            } else {
                System.out.println("--No answer");
                recvMs = null;
            }
            System.out.println("<-Java recv");
            return recvMs;
        }else{
            //System.out.println("working");
            return "null";
        }

    }

    public static void send(java.lang.String msgOut) {  //Sends the data String that has been given from kobuki ROS-->Agent


        // System.out.println("En ROSJADEgw send");
        //Unirlo al contenedor que asumimos que esta en localHost, port 1099
        java.lang.String host = "192.168.187.131"; ///
        java.lang.String port = "1099";//

        Properties pp = new Properties();
        pp.setProperty(Profile.MAIN_HOST, host);
        pp.setProperty(Profile.MAIN_PORT, port);
        pp.setProperty(Profile.LOCAL_PORT, port);

        java.lang.String containerName = "GatewayCont1";   // se define el nombre del contenedor donde se inicializara el agente
        pp.setProperty(Profile.CONTAINER_NAME, containerName);

        //crea mensaje ACL que se lo envia al TransportAgent
        StructMessage strMessage = new StructMessage();
        strMessage.setAction("send");
        strMessage.setMessage(msgOut);
        // System.out.println("Mensaje tipo social(ROS) recibido" +msgOut);
        strMessage.setPerformative(7); //INFORM

        //Dependiendo del tipo de mensaje recivido cambiar el performative
       /*
        if(msgOut.contains("Received")){    // Depending of the message type (confirmation or data exchanging) the performative will be different
            strMessage.setPerformative(7);  // Performative = INFORM
        } else {
            strMessage.setPerformative(16); // Performative = REQUEST
        }
        */

        // System.out.println("--Sended message: " + strMessage.readMessage());
        try {
            JadeGateway.execute(strMessage);    // Llamar a GWAgentROS para que envie el mensaje finalmente.
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("<-Java Send");
    }


}