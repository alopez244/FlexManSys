package com.github.rosjava.fms_transp.turtlebot2;

import java.net.URI;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.address.InetAddressFactory;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;
import org.apache.commons.logging.Log;

import jade.core.Profile;
import jade.util.leap.Properties;
import jade.wrapper.gateway.JadeGateway;

import com.github.rosjava.fms_transp.turtlebot2.StructCommand;


/*
 Clase que recibe mensajes ACL a través de GWagentROS y publica su contenido en ROS
 */

public class NodePubRunMainJadeGW_v2 extends AbstractNodeMain {

  public NodePubRunMainJadeGW_v2() {
    try {
      this.rosInit();
      this.jadeInit();
    } catch(Exception e) {
      System.out.println(e.getMessage());
    }
  }

  private void rosInit() throws Exception {
    String host = InetAddressFactory.newNonLoopback().getHostName();
    String port = "11311";
    String masterURI_str = "http://" + host + ":" + port;

    // Master host casa
    //String masterURI_str = "http://10.0.0.19:11311";

    URI masterURI = new URI(masterURI_str);

    NodeConfiguration nodeConfiguration = NodeConfiguration.newPrivate();
    nodeConfiguration.setMasterUri(masterURI);
    nodeConfiguration.setNodeName("NodePub");

    NodeMain nodeMain = (NodeMain) this;
    NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
    nodeMainExecutor.execute(nodeMain, nodeConfiguration);
  }

  private void jadeInit() throws Exception {
    String host = InetAddressFactory.newNonLoopback().getHostName();
    String port = "1099";

    Properties pp = new Properties();
    pp.setProperty(Profile.MAIN_HOST, host);
    pp.setProperty(Profile.MAIN_PORT, port);
    pp.setProperty(Profile.LOCAL_PORT, port);

    java.lang.String containerName = "GatewayCont1"; 
    pp.setProperty(Profile.CONTAINER_NAME, containerName);
    JadeGateway.init("com.github.rosjava.fms_transp.turtlebot2.GWagentROS", pp);

    StructCommand command = new StructCommand();
    command.setAction("init");
    JadeGateway.execute(command);
  }

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("NodePub");
  }

  @Override
  public void onStart(final ConnectedNode connectedNode) {
    Publisher<std_msgs.String> publisher = connectedNode.newPublisher("coordinate", std_msgs.String._TYPE);
    final Log log = connectedNode.getLog();

    while(true) {
      StructCommand command = new StructCommand();
      command.setAction("recv");
      
      try {
      	JadeGateway.execute(command);
      } catch(Exception e) {
        System.out.println(e.getMessage());
      }
      String msg_content = command.getContent();
      
      if(msg_content != null) {
        std_msgs.String destination = publisher.newMessage();
        destination.setData(msg_content);
        publisher.publish(destination);
        log.info("NodePub publishing in topic /coordinate: " + destination.getData());
      } 
    }
  }

  public static void main(String[] args) {
    NodePubRunMainJadeGW_v2 nodePubObj = new NodePubRunMainJadeGW_v2();
  }

}

