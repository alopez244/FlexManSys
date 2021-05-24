package es.ehu.domain.manufacturing.agents.cognitive;

import es.ehu.domain.manufacturing.utilities.StructMessage;
import jade.core.Profile;
import jade.util.leap.Properties;
import jade.wrapper.gateway.JadeGateway;

public class ROSJADEgw  {

    public static void Init(String transportID){


        //unir ROSJADEgw con GWAgentRos
        String host = "192.168.2.17"; ///prueba
        String port = "1099";//prueba
        Properties pp = new Properties();
        pp.setProperty(Profile.MAIN_HOST, host);
//
        pp.setProperty(Profile.MAIN_PORT, port);
        pp.setProperty(Profile.LOCAL_PORT, port);

        String containerName = "GatewayContenedor" + transportID;   // se define el nombre del contenedor donde se inicializara el agente
        pp.setProperty(Profile.CONTAINER_NAME, containerName);
        JadeGateway.init("es.ehu.domain.manufacturing.agents.cognitive.GWAgentROS",pp);

        StructMessage strMessage = new StructMessage();
        strMessage.setAction("init");
        try {
            JadeGateway.execute(strMessage);    // calls processCommand method of Gateway Agent
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("<-Java Agent Init");
    }

    //Function for reading the data received in ACL messages
    public static String recv() {
        String recvMs;
        StructMessage strMessage = new StructMessage();
        strMessage.setAction("receive");
        try {
            JadeGateway.execute(strMessage); //llamar a GWAgentRos process command
        } catch(Exception e) {
            System.out.println(e);
        }
        if(strMessage.readNewData()){
            recvMs=strMessage.readMessage();
            System.out.println("--Received: " + recvMs);
        }else{
            System.out.println("--No answer");
            recvMs="";
        }
        System.out.println("<-Java recv");
        return recvMs;
    }

    public static void send(String msgOut) {  //Sends the data String that has been given

        StructMessage strMessage = new StructMessage();
        strMessage.setAction("send");
        strMessage.setMessage(msgOut);

        //Dependiendo del tipo de mensaje recivido cambiar el performative
       /* if(msgOut.contains("Received")){    // Depending of the message type (confirmation or data exchanging) the performative will be different
            strMessage.setPerformative(7);  // Performative = INFORM
        } else {
            strMessage.setPerformative(16); // Performative = REQUEST
        }
        */

        System.out.println("--Sended message: " + strMessage.readMessage());
        try {
            JadeGateway.execute(strMessage);    // Llamar a GWAgentROS para que envie el mensaje finalmente.
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("<-Java Send");
    }







}
