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

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;
import jade.wrapper.gateway.JadeGateway;

/*
 Este codigo arranca un publicista ROS y un JadeGateway, cada uno por su cuenta.
 */

public class NodePubRunMainJadeGW_v1 extends AbstractNodeMain {

  public NodePubRunMainJadeGW_v1() {
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
    URI masterURI = new URI(masterURI_str);

    NodeConfiguration nodeConfiguration = NodeConfiguration.newPrivate();
    nodeConfiguration.setMasterUri(masterURI);
    nodeConfiguration.setNodeName("NodePub");

    NodeMain nodeMain = (NodeMain) this;
    NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
    nodeMainExecutor.execute(nodeMain, nodeConfiguration);
  }

  private void jadeInit() throws Exception {
    JadeGateway.execute(new CyclicBehaviour() {
      public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        AID jadeAgent = new AID("agente", false);
        msg.addReceiver(jadeAgent);
        msg.setConversationId("1234");
        msg.setContent("bla-bla-bla");
        myAgent.send(msg);

	wait3s();
      }
    });
  }

  private void wait3s() {
    try {
      Thread.sleep(3000);
    } catch(Exception e) {
      ;
    }
  }

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("NodePub");
  }

  @Override
  public void onStart(final ConnectedNode connectedNode) {
    Publisher<std_msgs.String> publisher = connectedNode.newPublisher("coordinate", std_msgs.String._TYPE);
    final Log log = connectedNode.getLog();

    Integer data = 0;
    while(true) {
      std_msgs.String destination = publisher.newMessage();
      destination.setData(data.toString());
      publisher.publish(destination);
      log.info("NodePub publishing in topic /coordinate: " + destination.getData());
      data++;

      wait3s();
    }
  }

  public static void main(String[] args) {
    NodePubRunMainJadeGW_v1 nodePubObj = new NodePubRunMainJadeGW_v1();
  }

}

