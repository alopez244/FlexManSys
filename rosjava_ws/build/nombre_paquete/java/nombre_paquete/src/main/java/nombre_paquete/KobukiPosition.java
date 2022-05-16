package nombre_paquete;

public interface KobukiPosition extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "nombre_paquete/KobukiPosition";
  static final java.lang.String _DEFINITION = "bool transport_in_dock\nstring recovery_point\nfloat64 odom_x\nfloat64 odom_y\nfloat64 rotation";
  boolean getTransportInDock();
  void setTransportInDock(boolean value);
  java.lang.String getRecoveryPoint();
  void setRecoveryPoint(java.lang.String value);
  double getOdomX();
  void setOdomX(double value);
  double getOdomY();
  void setOdomY(double value);
  double getRotation();
  void setRotation(double value);
}
