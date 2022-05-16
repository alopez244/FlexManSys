package nombre_paquete;

public interface KobukiObstacle extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "nombre_paquete/KobukiObstacle";
  static final java.lang.String _DEFINITION = "bool detected_obstacle_bumper\nbool detected_obstacle_camera";
  boolean getDetectedObstacleBumper();
  void setDetectedObstacleBumper(boolean value);
  boolean getDetectedObstacleCamera();
  void setDetectedObstacleCamera(boolean value);
}
