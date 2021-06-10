package es.ehu.domain.manufacturing.behaviour;

import es.ehu.domain.manufacturing.agents.cognitive.Ros_Jade_Msg;
import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.AssetManagement;
import es.ehu.platform.template.interfaces.NegFunctionality;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SendTaskBehaviour extends SimpleBehaviour {

    // TODO ESTE COMPORTAMIENTO NO SE UTILIZA PARA NADA. SE PUEDE ELIMINAR.

    static final Logger LOGGER = LogManager.getLogger(SendTaskBehaviour.class.getName());

    private MessageTemplate template;
    private MWAgent myAgent;
    private AssetManagement aAssetManagement;
    public Ros_Jade_Msg msg;
    private boolean done= false;

    public SendTaskBehaviour(MWAgent a/*, Ros_Jade_Msg m*/) {
        super(a);
        LOGGER.debug("*** Constructing RunningBehaviour ***");
        this.myAgent = a;
       // this.msg=m;
        this.aAssetManagement = (AssetManagement) a.functionalityInstance;

    }

    @Override
    public void action() {
        LOGGER.entry();

            block();

        LOGGER.exit();
        /*
        ACLMessage inf = new ACLMessage(ACLMessage.INFORM);
        inf.setOntology(msg.getOntology());
        inf.setConversationId("1 prueba");
        inf.setContent("Mensaje confirmacion");
        inf.addReceiver(new AID("TransportAgent",AID.ISLOCALNAME));
        myAgent.send(inf);
        this.done=true;

         */
    }

    @Override
    public boolean done() {
        return false;
    }
}
