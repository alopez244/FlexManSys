package nombre_paquete;

public interface TransportUnitState extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "nombre_paquete/TransportUnitState";
  static final java.lang.String _DEFINITION = "KobukiGeneral kobuki_general\nKobukiObstacle kobuki_obstacle\nKobukiPosition kobuki_position\nTimeDate odroid_date\n\n";
  nombre_paquete.KobukiGeneral getKobukiGeneral();
  void setKobukiGeneral(nombre_paquete.KobukiGeneral value);
  nombre_paquete.KobukiObstacle getKobukiObstacle();
  void setKobukiObstacle(nombre_paquete.KobukiObstacle value);
  nombre_paquete.KobukiPosition getKobukiPosition();
  void setKobukiPosition(nombre_paquete.KobukiPosition value);
  nombre_paquete.TimeDate getOdroidDate();
  void setOdroidDate(nombre_paquete.TimeDate value);
}
