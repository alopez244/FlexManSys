package es.ehu.domain.manufacturing.template.interfaces;

import jade.lang.acl.ACLMessage;
import java.io.Serializable;

public interface AssetManagement extends Serializable {

    public void rcvDataFromDevice(ACLMessage msg);

    public void sendDataToDevice();

}
