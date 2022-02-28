package es.ehu.domain.manufacturing.utilities;

public class StructCommandMsg {

    private String action;
    private Object content;

    public StructCommandMsg() {}

    public void setAction (String _action) { this.action = _action; }
    public void setContent (Object _content) { this.content = _content; }

    public String getAction () { return this.action; }
    public Object getContent () { return this.content; }

}
