package es.ehu.domain.manufacturing.agents.cognitive;

import es.ehu.domain.manufacturing.utilities.StructMessage;
import jade.wrapper.gateway.GatewayAgent;
import org.apache.commons.collections4.queue.CircularFifoQueue;

public class GWAgent extends GatewayAgent {

    public String msgRecv;
    public static final int bufferSize =6;
    CircularFifoQueue msgInFIFO = new CircularFifoQueue(bufferSize);


    protected void proccessCommand(java.lang.Object command) {
        System.out.println("-->Gateway processes execute");
        if(!(command instanceof StructMessage)){
            System.out.println("---Error, unexpected type");
            releaseCommand(command);
        }
        StructMessage msgStruct = (StructMessage) command;
        if(msgStruct.readAction()=="recieve") {
            System.out.println("---GW, recv function");
            msgRecv = (String) msgInFIFO.poll();
            if ( msgRecv != null) {
                System.out.println("---GW, new message to read");
                ((StructMessage) command).setMessage(msgRecv);
                ((StructMessage) command).setNewData(true);
            } else {
                ((StructMessage) command).setNewData(false);
                System.out.println("---GW, message queue is empty");
            }
        }else if(msgStruct.readAction()=="send") {
            System.out.println("---Gateway send command");
            //enviaAgenteACL(msgStruct.readMessage());
        }
        System.out.println("<--Gateway processes execute");
        releaseCommand(command);
    }
}
