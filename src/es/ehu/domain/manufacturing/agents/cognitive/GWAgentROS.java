package es.ehu.domain.manufacturing.agents.cognitive;

import es.ehu.domain.manufacturing.utilities.StructMessage;
import jade.core.AID;
import jade.wrapper.gateway.GatewayAgent;
import org.apache.commons.collections4.queue.CircularFifoQueue;

public class GWAgentROS extends GatewayAgent {

    public String msgRecv;
    public AID TransportAgentName;
    public static final int bufferSize = 6;
    CircularFifoQueue msgInFIFO = new CircularFifoQueue(bufferSize);

    protected void processCommand(java.lang.Object command) { //The method is called each time a request to process a command is received from the JSP Gateway. receive strmessage
        System.out.println("-->Gateway processes execute");
        if (!(command instanceof StructMessage)) {
            System.out.println("---Error, unexpected type");
            releaseCommand(command);
        }
        StructMessage msgStruct = (StructMessage) command;
        String action = msgStruct.readAction();
        if (action.equals("receive")) {     // JadeGateway.execute command was called for new message reading (Agent -> PLC)
            System.out.println("---GW, recv function");
            msgRecv = (String) msgInFIFO.poll();    //reads the oldest message from FIFO
            if (msgRecv != null) {
                System.out.println("---GW, new message to read");
                ((StructMessage) command).setMessage(msgRecv);  //message is saved in StructMessage data structure, then ExternalJADEgw class will read it from there
                ((StructMessage) command).setNewData(true);
            } else {
                ((StructMessage) command).setNewData(false);
                System.out.println("---GW, message queue is empty");
            }
        } else if (action.equals("send")) {


        } else if (action.equals("init")) {

        }
    }
}














