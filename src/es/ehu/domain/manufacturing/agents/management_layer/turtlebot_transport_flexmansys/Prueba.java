package es.ehu.domain.manufacturing.agents.management_layer.turtlebot_transport_flexmansys;

public interface Prueba extends org.ros.internal.message.Message {

  static final String _TYPE = "turtlebot_transport_flexmansys/Prueba";
  static final String _DEFINITION = "int32 numero_prueba\n\n";

  int getNumeroPrueba();
  void setNumeroPrueba(int numero_prueba);

}



