package com.github.rosjava.fms_transp.turtlebot2;

/* Este codigo define una clase definida como StructCommand. Cuenta con dos campos,
   primero la "action", donde se define al GWagentROS el tipo de accion que debe realizar
   (si init, rcv...) y posteriormente el contenido, que son los datos asociados a la accion
   el el agente ha empleado.
 */

import com.github.rosjava.fms_transp.turtlebot2.StructTransportUnitState;

public class StructCommand {

    private String action;
    private String content;
    private StructTransportUnitState transport_state;
    private java.lang.Object content_state;

    public StructCommand() {}

    public void setAction (String _action) { this.action = _action; }
    public void setContent (String _content) { this.content = _content; }
    public void setTransport_state (StructTransportUnitState _transport_state) { this.transport_state = _transport_state; }
    public void setContent_state (java.lang.Object _content_state) { this.content_state = _content_state; }

    public String getAction () { return this.action; }
    public String getContent () { return this.content; }
    public StructTransportUnitState getTransport_state () { return this.transport_state; }
    public java.lang.Object getContent_state () { return this.content_state; }

}
