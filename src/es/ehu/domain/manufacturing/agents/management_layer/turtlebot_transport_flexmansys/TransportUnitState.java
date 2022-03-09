package es.ehu.domain.manufacturing.agents.management_layer.turtlebot_transport_flexmansys;

public interface TransportUnitState extends org.ros.internal.message.Message {
  static final String _TYPE = "turtlebot_transport_flexmansys/TransportUnitState";
  static final String _DEFINITION = "KobukiGeneral kobuki_general\nKobukiObstacle kobuki_obstacle\nKobukiPosition kobuki_position\nKOdroidDate odroid_date\n\n";

  KobukiGeneral getKobukiGeneral();
  void setKobukiGeneral(KobukiGeneral value);

  KobukiObstacle getKobukiObstacle();
  void setKobukiObstacle(KobukiObstacle value);

  KobukiPosition getKobukiPosition();
  void setKobukiPosition(KobukiPosition value);

  OdroidDate getOdroidDate();
  void setOdroidDate(OdroidDate value);

}
