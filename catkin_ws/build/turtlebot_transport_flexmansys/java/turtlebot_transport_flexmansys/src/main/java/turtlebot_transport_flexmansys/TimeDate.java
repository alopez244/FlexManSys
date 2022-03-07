package turtlebot_transport_flexmansys;

public interface TimeDate extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "turtlebot_transport_flexmansys/TimeDate";
  static final java.lang.String _DEFINITION = "int32 year\nint32 month\nint32 day\nint32 hour\nint32 minute\nint32 seconds\n\n";
  int getYear();
  void setYear(int value);
  int getMonth();
  void setMonth(int value);
  int getDay();
  void setDay(int value);
  int getHour();
  void setHour(int value);
  int getMinute();
  void setMinute(int value);
  int getSeconds();
  void setSeconds(int value);
}
