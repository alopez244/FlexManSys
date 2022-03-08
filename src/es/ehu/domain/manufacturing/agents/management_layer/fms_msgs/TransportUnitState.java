package es.ehu.domain.manufacturing.agents.management_layer.fms_msgs;

import es.ehu.domain.manufacturing.agents.management_layer.fms_msgs.KobukiGeneral;
import es.ehu.domain.manufacturing.agents.management_layer.fms_msgs.KobukiObstacle;
import es.ehu.domain.manufacturing.agents.management_layer.fms_msgs.KobukiPosition;
import es.ehu.domain.manufacturing.agents.management_layer.fms_msgs.OdroidDate;

public interface TransportUnitState extends org.ros.internal.message.Message {
  static final String _TYPE = "turtlebot_transport_flexmansys/TransportUnitState";
  static final String _DEFINITION = "KobukiGeneral kobuki_general\nKobukiObstacle kobuki_obstacle\nBoolean assetLiveness\n\n";

  KobukiGeneral getKobukiGeneral();
  void setKobukiGeneral(KobukiGeneral value);
  KobukiObstacle getKobukiObstacle();
  void setKobukiObstacle(KobukiObstacle value);
  Boolean getAssetLiveness();
  void setAssetLiveness(std_msgs.Bool value);
}
