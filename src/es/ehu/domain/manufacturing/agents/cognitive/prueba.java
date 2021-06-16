package es.ehu.domain.manufacturing.agents.cognitive;

import org.ros.internal.message.Message;
import org.ros.internal.message.RawMessage;
import std_msgs.String;

public class prueba implements String {
    java.lang.String _TYPE = "std_msgs/String";
    java.lang.String _DEFINITION = "string data\n";

    public java.lang.String getData(){
        return _DEFINITION;
    }

    public void setData(java.lang.String var1){
        _DEFINITION=var1;
    }

    @Override
    public RawMessage toRawMessage() {
        return null;
    }
}
