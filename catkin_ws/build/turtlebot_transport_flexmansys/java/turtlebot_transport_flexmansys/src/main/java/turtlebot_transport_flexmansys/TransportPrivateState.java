package turtlebot_transport_flexmansys;

public interface TransportPrivateState extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "turtlebot_transport_flexmansys/TransportPrivateState";
  static final java.lang.String _DEFINITION = "string transport_state\nbool transport_docked\n";
  java.lang.String getTransportState();
  void setTransportState(java.lang.String value);
  boolean getTransportDocked();
  void setTransportDocked(boolean value);
}
