package es.ehu.domain.manufacturing.agents.managementLayer;

import es.ehu.domain.manufacturing.utilities.StructCommandMsg;
import es.ehu.domain.manufacturing.utilities.StructTranspState;
import es.ehu.domain.manufacturing.utilities.StructTranspResults;
import es.ehu.domain.manufacturing.utilities.StructTranspRequest;
import es.ehu.domain.manufacturing.utilities.fms_msgs.*;
import jade.core.Profile;
import jade.util.leap.Properties;
import jade.wrapper.gateway.JadeGateway;
import org.apache.commons.logging.Log;
import org.ros.address.InetAddressFactory;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.*;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.net.URI;


public class ACL_ROS_Gateway extends AbstractNodeMain {

  public ACL_ROS_Gateway() {
    try {
      this.jadeInit();
      this.rosInit();
    } catch(Exception e) {
      System.out.println(e.getMessage());
    }
  }

  private void rosInit() throws Exception {
    String host = InetAddressFactory.newNonLoopback().getHostName();
    String port = "11311";
    String masterURI_str = "http://" + host + ":" + port;
    URI masterURI = new URI(masterURI_str);

    NodeConfiguration nodeConfiguration = NodeConfiguration.newPrivate();
    nodeConfiguration.setMasterUri(masterURI);
    nodeConfiguration.setNodeName("NodePubMsg");

    NodeMain nodeMain = (NodeMain) this;
    NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
    nodeMainExecutor.execute(nodeMain, nodeConfiguration);
  }

  private void jadeInit() throws Exception {
    Properties pp = new Properties();

    String host = InetAddressFactory.newNonLoopback().getHostName();
    String port = "1099";
    pp.setProperty(Profile.MAIN_HOST, host);
    pp.setProperty(Profile.MAIN_PORT, port);
    pp.setProperty(Profile.LOCAL_PORT, port);

    String containerName = "GatewayContMsg1"; //TODO: Cambiar el nombre del contenedor para que dependa de a qué Kobuki se conecta
    pp.setProperty(Profile.CONTAINER_NAME, containerName);

    JadeGateway.init("es.ehu.domain.manufacturing.agents.managementLayer.GWAgentROS", pp);

    StructCommandMsg command = new StructCommandMsg();
    command.setAction("init");
    JadeGateway.execute(command);
  }

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("NodePubMsg");
  }

  @Override
  public void onStart(final ConnectedNode connectedNode) {

    //TODO:
    // Generar nuevas estructuras de mensajes ROS para adaptarse a las estructuras que se proponen (transp_request, transp_state y transp_results)
    // Estos son un tranpantojo
    Publisher<transp_request> publisher = connectedNode.newPublisher("coordinateMsg", transp_request._TYPE);

    //TODO: Añadir suscriptores con tipos de mensaje adaptados a las estructuras que se proponen

    Subscriber<transp_state> subscriber1 = connectedNode.newSubscriber("stateMsg", transp_state._TYPE);
    Subscriber<transp_results> subscriber2 = connectedNode.newSubscriber("resultsMsg", transp_results._TYPE);

    subscriber1.addMessageListener(new MessageListener<transp_state>() {
      @Override
      public void onNewMessage(transp_state stateMsg) {

        Integer battery = stateMsg.getBattery();
        String currentPos = stateMsg.getCurrentPos();
        Boolean assetLiveness = stateMsg.getAssetLiveness();

        StructTranspState javaTranspState = new StructTranspState();
        javaTranspState.setBattery(battery);
        javaTranspState.setCurrentPos(currentPos);
        javaTranspState.setAssetLiveness(assetLiveness);

        StructCommandMsg command = new StructCommandMsg();
        command.setAction("sendState");
        ((StructCommandMsg) command).setContent(javaTranspState);

        try {
          JadeGateway.execute(command);
        } catch(Exception e) {
          System.out.println(e.getMessage());
        }
      }
    });

    subscriber2.addMessageListener(new MessageListener<transp_results>() {
      @Override
      public void onNewMessage(transp_results resultsMsg) {

        Integer battery = resultsMsg.getBattery();
        String currentPos = resultsMsg.getCurrentPos();
        Float initialTimeStamp = resultsMsg.getInitialTimeStamp();
        Float finalTimeStamp = resultsMsg.getFinalTimeStamp();

        StructTranspResults javaTranspResults = new StructTranspResults();
        javaTranspResults.setBattery(battery);
        javaTranspResults.setCurrentPos(currentPos);
        javaTranspResults.setInitial_timeStamp(initialTimeStamp);
        javaTranspResults.setFinal_timeStamp(finalTimeStamp);

        StructCommandMsg command = new StructCommandMsg();
        command.setAction("sendResults");
        ((StructCommandMsg) command).setContent(javaTranspResults);

        try {
          JadeGateway.execute(command);
        } catch(Exception e) {
          System.out.println(e.getMessage());
        }
      }
    });

    final Log log = connectedNode.getLog();

    while(true) {
      StructCommandMsg command = new StructCommandMsg();
      command.setAction("recv");

      try {
        JadeGateway.execute(command);
      } catch(Exception e) {
        System.out.println(e.getMessage());
      }
      StructTranspRequest javaTranspRequest = (StructTranspRequest) command.getContent();

      if(javaTranspRequest != null) {

        String[] task = javaTranspRequest.getTask();

        //TODO: modificar la estructura del publicista (tendrá que ser transp_request)

        transp_request rosTranspState = publisher.newMessage();
        rosTranspState.setTask(task);

        publisher.publish(rosTranspState);
        log.info("NodePub publishing in topic /coordinateMsg");
      } else {
        log.info("NodePub has no message to publish");
      }
    }
  }

  public static void main(String[] args) {
    ACL_ROS_Gateway nodePubObj = new ACL_ROS_Gateway();
  }

}

