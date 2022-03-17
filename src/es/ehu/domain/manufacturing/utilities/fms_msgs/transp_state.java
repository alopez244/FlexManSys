package es.ehu.domain.manufacturing.utilities.fms_msgs;

public interface transp_state extends org.ros.internal.message.Message {
  static final String _TYPE = "fms_msgs/transp_state";
  static final String _DEFINITION = "Integer battery\nString currentPos\nBoolean assetLiveness\n\n";
  Integer getBattery();
  void setBattery(std_msgs.Int8 value);
  String getCurrentPos();
  void setCurrentPos(String value);
  Boolean getAssetLiveness();
  void setAssetLiveness(std_msgs.Bool value);
}
