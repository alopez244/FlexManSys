package turtlebot_transport_flexmansys;

public interface TransportUnitState extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "turtlebot_transport_flexmansys/TransportUnitState";
  static final java.lang.String _DEFINITION = "KobukiGeneral kobuki_general\nKobukiObstacle kobuki_obstacle\nKobukiPosition kobuki_position\nTimeDate odroid_date\n\n";
  turtlebot_transport_flexmansys.KobukiGeneral getKobukiGeneral();
  void setKobukiGeneral(turtlebot_transport_flexmansys.KobukiGeneral value);
  turtlebot_transport_flexmansys.KobukiObstacle getKobukiObstacle();
  void setKobukiObstacle(turtlebot_transport_flexmansys.KobukiObstacle value);
  turtlebot_transport_flexmansys.KobukiPosition getKobukiPosition();
  void setKobukiPosition(turtlebot_transport_flexmansys.KobukiPosition value);
  turtlebot_transport_flexmansys.TimeDate getOdroidDate();
  void setOdroidDate(turtlebot_transport_flexmansys.TimeDate value);
}
