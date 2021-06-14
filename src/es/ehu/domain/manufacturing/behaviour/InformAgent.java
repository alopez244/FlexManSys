package es.ehu.domain.manufacturing.behaviour;

import es.ehu.domain.manufacturing.agents.cognitive.Ros_Jade_Msg;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.gateway.GatewayAgent;

public class InformAgent  extends SimpleBehaviour {

    // TODO ESTE COMPORTAMIENTO NO SE UTILIZA PARA NADA. SE PUEDE ELIMINAR.

    boolean done=false;
    Ros_Jade_Msg msg=null;

    public InformAgent(GatewayAgent agent, Ros_Jade_Msg msg){
        super(agent);
        this.msg=msg;
    }
    public void action(){
        ACLMessage inf= new ACLMessage(ACLMessage.INFORM);
        inf.setOntology(msg.getOntology());
        inf.setConversationId("1");
        inf.setContent("Prueba content");
        inf.addReceiver(new AID("TransportAgent",AID.ISLOCALNAME));
        myAgent.send(inf);
        this.done=true;
    }
    @Override
    public boolean done() {
        return false;
    }
}
