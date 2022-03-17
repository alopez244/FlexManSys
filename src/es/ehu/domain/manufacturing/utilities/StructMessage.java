package es.ehu.domain.manufacturing.utilities;

public class StructMessage {

    private String messageGson;
    private String action;


    public StructMessage() {
    }


    public void setAction(String _action) {
        this.action = _action;
    }

    public void setMessage(String _message) {
        this.messageGson = _message;
    }


    public String readAction() {
        return this.action;
    }

    public String readMessage() {
        return this.messageGson;
    }
}