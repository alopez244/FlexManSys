package es.ehu.domain.manufacturing.utilities;

/* Este codigo define una clase definida como StructCommand. Cuenta con dos campos,
   primero la "action", donde se define al GWagentROS el tipo de accion que debe realizar
   (si init, rcv...) y posteriormente el contenido, que son los datos asociados a la accion
   el el agente ha empleado.
 */

import es.ehu.domain.manufacturing.utilities.StructTransportUnitState;

public class StructCommandMsg {

    private String action;
    private Object content;
    private StructTransportUnitState transport_state;

    public StructCommandMsg() {}

    public void setAction (String _action) { this.action = _action; }
    public void setContent (Object _content) { this.content = _content; }
    public void setTransport_state (StructTransportUnitState _transport_state) { this.transport_state = _transport_state; }


    public String getAction () { return this.action; }
    public Object getContent () { return this.content; }
    public StructTransportUnitState getTransport_state () { return this.transport_state; }

}
