package es.ehu.domain.manufacturing.agents.management_layer.fms_msgs;

public interface KobukiGeneral extends org.ros.internal.message.Message {

  static final String _TYPE = "turtlebot_transport_flexmansys/KobukiGeneral";
  static final String _DEFINITION = "String transport_unit_name\nString transport_unit_state\nFloat battery\n\n";

  String getTransportUnitName();
  void setTransportUnitName(String value);
  String getTransportUnitState();
  void setTransportUnitState(String value);
  Float getBattery();
  void setBattery(std_msgs.Float32 value);
}


