package com.github.rosjava.fms_transp.turtlebot2;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.apache.commons.logging.Log;


public class NodePub extends AbstractNodeMain {

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("NodePub");
  }

  @Override
  public void onStart(final ConnectedNode connectedNode) {
    Publisher<std_msgs.String> publisher = connectedNode.newPublisher("coordinate", std_msgs.String._TYPE);
    final Log log = connectedNode.getLog();

    Integer i = 0;
    while(true) {
      std_msgs.String destination = publisher.newMessage();
      destination.setData(i.toString());
      publisher.publish(destination);
      log.info("NodePub publishing in topic /coordinate: " + destination.getData());
      i++;

      try {
        Thread.sleep(3000);
      } catch(Exception e) {
        ;
      }
    }
  }

}
