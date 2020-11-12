package es.ehu.platform.template.interfaces;

import java.io.Serializable;
import java.util.Hashtable;

public interface IExecManagement extends Serializable {

    public String appStart(String seID, Hashtable<String, String> attribs, String conversationId);

    public String appStop(String... seID); // Sin implementar en SystemModelAgent
                                            // Las variables hay que definirlas

}
