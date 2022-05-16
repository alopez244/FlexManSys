package turtlebot_transport_flexmansys;

public interface KobukiGeneral extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "turtlebot_transport_flexmansys/KobukiGeneral";
  static final java.lang.String _DEFINITION = "string transport_unit_name\nstring transport_unit_state\nfloat32 battery";
  java.lang.String getTransportUnitName();
  void setTransportUnitName(java.lang.String value);
  java.lang.String getTransportUnitState();
  void setTransportUnitState(java.lang.String value);
  float getBattery();
  void setBattery(float value);
}
