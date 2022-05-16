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


public class NodePubRunMain extends AbstractNodeMain {

  public NodePubRunMain() {
    try {
      this.rosInit();
    } catch(Exception e) {
      System.out.println(e.getMessage());
    }
    //this.jadeInit();
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

      try {
        Thread.sleep(3000);
      } catch(Exception e) {
        ;
      }
    }
  }

  public static void main(String[] args) {
    NodePubRunMain nodePubObj = new NodePubRunMain();
  }

}

