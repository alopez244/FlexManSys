package servicio_cuadrado;

public interface MyCustomServiceMessageResponse extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "servicio_cuadrado/MyCustomServiceMessageResponse";
  static final java.lang.String _DEFINITION = "bool success # Se ha realizado el servicio o no";
  boolean getSuccess();
  void setSuccess(boolean value);
}
