package es.ehu.domain.manufacturing.agents.management_layer.turtlebot_transport_flexmansys;

public interface OdroidDate extends org.ros.internal.message.Message {

  static final String _TYPE = "turtlebot_transport_flexmansys/OdroidDate";
  static final String _DEFINITION = "Int year\nInt month\nInt day\nInt hour\nInt minute\nInt seconds\n\n";

  int getYear();
  void setYear(int year);

  int getMonth();
  void setMonth(int month);

  int getDay();
  void setDay(int day);

  int getHour();
  void setHour(int hour);

  int getMinute();
  void setMinute(int minute);

  int getSeconds();
  void setSeconds(int seconds);





}


