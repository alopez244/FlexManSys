package nombre_paquete;

public interface TransportPrivateState extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "nombre_paquete/TransportPrivateState";
  static final java.lang.String _DEFINITION = "string transport_state\nbool transport_docked\n";
  java.lang.String getTransportState();
  void setTransportState(java.lang.String value);
  boolean getTransportDocked();
  void setTransportDocked(boolean value);
}
