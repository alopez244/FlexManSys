package es.ehu.platform.template.interfaces;

import jade.lang.acl.ACLMessage;

import java.io.Serializable;

public interface DDInterface extends Serializable {

    public void recover_redundancy(ACLMessage msg);

    public void actions_after_not_found(ACLMessage msg);

    public void actions_after_msg_lost(ACLMessage msg);

    public void change_DD_state(ACLMessage msg);

    public void redistribute_machine_operations(ACLMessage msg);


}
