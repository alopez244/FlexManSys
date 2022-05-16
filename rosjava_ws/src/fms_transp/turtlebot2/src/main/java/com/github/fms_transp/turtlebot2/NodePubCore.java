package com.github.rosjava.fms_transp.turtlebot2;

import org.ros.RosCore;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.apache.commons.logging.Log;


public class NodePubCore extends AbstractNodeMain {

  public NodePubCore() {
    RosCore rosCore = RosCore.newPublic(11311);
    rosCore.start();
    try {
      rosCore.awaitStart();
    } catch (InterruptedException e) {
      e.printStackTrace();
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

      try {
        Thread.sleep(3000);
      } catch(Exception e) {
        ;
      }
    }
  }
}