package servicio_cuadrado;

public interface MyCustomServiceMessageRequest extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "servicio_cuadrado/MyCustomServiceMessageRequest";
  static final java.lang.String _DEFINITION = "float64 radius # Distancia de cada lado del cuadrado\nint32 repetitions # Repeticiones de cada una de las vueltas\n";
  double getRadius();
  void setRadius(double value);
  int getRepetitions();
  void setRepetitions(int value);
}
