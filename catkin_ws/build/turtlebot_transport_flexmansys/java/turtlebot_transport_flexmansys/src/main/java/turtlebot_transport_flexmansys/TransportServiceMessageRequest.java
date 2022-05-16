package turtlebot_transport_flexmansys;

public interface TransportServiceMessageRequest extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "turtlebot_transport_flexmansys/TransportServiceMessageRequest";
  static final java.lang.String _DEFINITION = "string coordinate # Coordenada la cual retiene la peticion\n";
  java.lang.String getCoordinate();
  void setCoordinate(java.lang.String value);
}
