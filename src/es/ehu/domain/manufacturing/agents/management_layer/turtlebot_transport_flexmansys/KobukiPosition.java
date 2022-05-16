package es.ehu.domain.manufacturing.agents.management_layer.turtlebot_transport_flexmansys;

public interface KobukiPosition extends org.ros.internal.message.Message {

  static final String _TYPE = "turtlebot_transport_flexmansys/KobukiPosition";
  static final String _DEFINITION = "Boolean transport_in_dock\nString recovery_point\nDouble odom_x\nDouble odom_y\nDouble rotation\n\n";

  Boolean getTransportInDock();
  void setTransportInDock(Boolean value);

  String getRecoveryPoint();
  void setRecoveryPoint(String value);

  Double getOdom_x();
  void setOdom_x(std_msgs.Float64 value);

  Double getOdom_y();
  void setOdom_y(std_msgs.Float64 value);

  Double getRotation();
  void setRotation(std_msgs.Float64 value);


}


