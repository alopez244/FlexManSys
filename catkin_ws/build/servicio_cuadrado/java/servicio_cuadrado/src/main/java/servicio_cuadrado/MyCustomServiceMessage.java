package servicio_cuadrado;

public interface MyCustomServiceMessage extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "servicio_cuadrado/MyCustomServiceMessage";
  static final java.lang.String _DEFINITION = "float64 radius # Distancia de cada lado del cuadrado\nint32 repetitions # Repeticiones de cada una de las vueltas\n---\nbool success # Se ha realizado el servicio o no\n";
}
