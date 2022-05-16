package my_package;

public interface Age extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "my_package/Age";
  static final java.lang.String _DEFINITION = "float32 years\nfloat32 months\nfloat32 days\n";
  float getYears();
  void setYears(float value);
  float getMonths();
  void setMonths(float value);
  float getDays();
  void setDays(float value);
}
