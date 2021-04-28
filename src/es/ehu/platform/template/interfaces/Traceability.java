package es.ehu.platform.template.interfaces;

import jade.lang.acl.ACLMessage;
import java.io.Serializable;

public interface Traceability extends Serializable {

    public void recvBatchInfo(ACLMessage msg);

}
