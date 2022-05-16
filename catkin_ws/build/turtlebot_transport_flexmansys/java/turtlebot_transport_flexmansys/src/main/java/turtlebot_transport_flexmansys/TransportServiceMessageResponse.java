package turtlebot_transport_flexmansys;

public interface TransportServiceMessageResponse extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "turtlebot_transport_flexmansys/TransportServiceMessageResponse";
  static final java.lang.String _DEFINITION = "bool success # Se ha realizado el servicio o no";
  boolean getSuccess();
  void setSuccess(boolean value);
}
