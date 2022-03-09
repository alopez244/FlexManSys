package es.ehu.domain.manufacturing.agents.management_layer.turtlebot_transport_flexmansys;

public interface KobukiObstacle extends org.ros.internal.message.Message {

  static final String _TYPE = "turtlebot_transport_flexmansys/KobukiObstacle";
  static final String _DEFINITION = "Boolean detected_obstacle_bumper\nBoolean detected_obstacle_camera\n\n";

  Boolean getDetectedObstacleBumper();
  void setDetectedObstacleBumper(std_msgs.Bool value);
  Boolean getDetectedObstacleCamera();
  void setDetectedObstacleCamera(std_msgs.Bool value);

}


