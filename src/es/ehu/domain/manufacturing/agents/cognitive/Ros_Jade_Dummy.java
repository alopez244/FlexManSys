package es.ehu.domain.manufacturing.agents.cognitive;
import org.ros.RosCore;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.*;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import org.ros.message.Time;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import java.util.concurrent.TimeUnit;
public class Ros_Jade_Dummy extends AbstractNodeMain{
    /** JADE Agent represented in the ROS platform */
    private Agent myAgent;
    /** Behaviour to wake up the agent if there are new events */
    private Behaviour controlledBehaviour;
    public Ros_Jade_Dummy(Agent a){
        this.myAgent=a;
        this.controlledBehaviour=null;
        RosCore rosCore = null;
        try {
            rosCore.newPublic(11311);
            rosCore.start();
            rosCore.awaitStart(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            rosCore = null;
        }
        NodeMain nodeMain = (NodeMain) this;
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPrivate();
        nodeConfiguration.setNodeName(myAgent.getLocalName());
        if (rosCore != null) {
            nodeConfiguration.setMasterUri(rosCore.getUri());
        }
        NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
        nodeMainExecutor.execute(nodeMain, nodeConfiguration);
    }
    public  Ros_Jade_Dummy(Agent a, Behaviour b){
        this(a);
        this.controlledBehaviour = b;
    }
    public GraphName getDefaultNodeName() {
        return GraphName.of(myAgent.getLocalName());
    }
    public void onStart(final ConnectedNode connectedNode) {
    }
}









