package packet.mqtt;

public class StructMessage {

    private String action;
    private String messageJson;

    public StructMessage() {}

    public void setAction(String _action) {
        this.action = _action;
    }
    public void setMessage(String _message) {
        this.messageJson = _message;
    }

    public String getAction() {
        return this.action;
    }
    public String getMessage() {
        return this.messageJson;
    }
}