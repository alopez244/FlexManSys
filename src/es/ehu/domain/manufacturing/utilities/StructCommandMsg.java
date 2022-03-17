package es.ehu.domain.manufacturing.utilities;

/* Este codigo define una clase definida como StructCommand. Cuenta con dos campos,
   primero la "action", donde se define al GWagentROS el tipo de accion que debe realizar
   (si init, rcv...) y posteriormente el contenido, que son los datos asociados a la accion
   el el agente ha empleado.
 */

public class StructCommandMsg {

    private String action;
    private Object content;

    public StructCommandMsg() {}

    public void setAction (String _action) { this.action = _action; }
    public void setContent (Object _content) { this.content = _content; }


    public String getAction () { return this.action; }
    public Object getContent () { return this.content; }

}
