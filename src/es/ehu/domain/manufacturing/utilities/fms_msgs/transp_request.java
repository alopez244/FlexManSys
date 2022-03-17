package es.ehu.domain.manufacturing.utilities.fms_msgs;

public interface transp_request extends org.ros.internal.message.Message {
  static final String _TYPE = "fms_msgs/transp_request";
  static final String _DEFINITION = "Task string[]";
  String[] getTask();
  void setTask (String[] value);
}
