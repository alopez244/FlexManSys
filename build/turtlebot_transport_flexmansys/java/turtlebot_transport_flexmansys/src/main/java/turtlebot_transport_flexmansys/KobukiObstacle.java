package turtlebot_transport_flexmansys;

public interface KobukiObstacle extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "turtlebot_transport_flexmansys/KobukiObstacle";
  static final java.lang.String _DEFINITION = "bool detected_obstacle_bumper\nbool detected_obstacle_camera";
  boolean getDetectedObstacleBumper();
  void setDetectedObstacleBumper(boolean value);
  boolean getDetectedObstacleCamera();
  void setDetectedObstacleCamera(boolean value);
}
