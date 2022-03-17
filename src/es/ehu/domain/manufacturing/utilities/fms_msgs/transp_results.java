package es.ehu.domain.manufacturing.utilities.fms_msgs;

public interface transp_results extends org.ros.internal.message.Message {
  static final String _TYPE = "fms_msgs/transp_results";
  static final String _DEFINITION = "Integer battery\nString currentPos\nFloat initialTimeStamp\nFloat finalTimeStamp\n\n";
  Integer getBattery();
  void setBattery(std_msgs.Int8 value);
  String getCurrentPos();
  void setCurrentPos(String value);
  Float getInitialTimeStamp();
  void setInitialTimeStamp(std_msgs.Float32 value);
  Float getFinalTimeStamp();
  void setFinalTimeStamp(std_msgs.Float32 value);
}
