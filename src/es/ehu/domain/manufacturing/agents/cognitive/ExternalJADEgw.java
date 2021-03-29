package es.ehu.domain.manufacturing.agents.cognitive;

import com.google.gson.Gson;
import es.ehu.domain.manufacturing.utilities.StructMessage;
import jade.core.Profile;
import jade.util.leap.Properties;
import jade.wrapper.gateway.JadeGateway;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class ExternalJADEgw {

    public static void agentInit(String machineID){
        //redirectOutput();
        System.out.println("->Java Agent Init");
//        String host = "127.0.0.1";              //Local host IP)
        String host = "192.168.2.17";              // host of Alejandro PC
        String localHost = "192.168.2.3";              //Local host of PLC
        String port = "1099";                   //Port on which the agent manager is running

        Properties pp = new Properties();
        pp.setProperty(Profile.MAIN_HOST, host);
        pp.setProperty(Profile.LOCAL_HOST, localHost);
        pp.setProperty(Profile.MAIN_PORT, port);
        pp.setProperty(Profile.LOCAL_PORT, port);

        String containerName = "GatewayCont" + machineID;   // se define el nombre del contenedor donde se inicializara el agente
        pp.setProperty(Profile.CONTAINER_NAME, containerName);      //-->Name ControlGatewayContX
        JadeGateway.init("es.ehu.domain.manufacturing.agents.cognitive.GWAgent",pp);    //Gateway Agent Initialization, must define package directory
        StructMessage strMessage = new StructMessage();
        strMessage.setAction("init");
        try {
            JadeGateway.execute(strMessage);    // calls processCommand method of Gateway Agent
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("<-Java Agent Init");
    }

    //Function to send ACL messages by receiving a String that is added in the message.
    public static void send(String msgOut) {  //Sends the data String that has been given
        System.out.println("->Java Send");
        String host = "192.168.2.17";              // host of Alejandro PC
        String localHost = "192.168.2.3";              //Local host of PLC
        String port = "1099";                   //Port on which the agent manager is running

        Properties pp = new Properties();
        pp.setProperty(Profile.MAIN_HOST, host);
        pp.setProperty(Profile.LOCAL_HOST, localHost);
        pp.setProperty(Profile.MAIN_PORT, port);
        pp.setProperty(Profile.LOCAL_PORT, port);

        String containerName = "GatewayCont1";   // se define el nombre del contenedor donde se inicializara el agente
        pp.setProperty(Profile.CONTAINER_NAME, containerName);      //-->Name ControlGatewayContX
        JadeGateway.init("es.ehu.domain.manufacturing.agents.cognitive.GWAgent",pp);    //Gateway Agent Initialization, must define package directory
        StructMessage strMessage = new StructMessage();
        strMessage.setAction("send");
        strMessage.setMessage(msgOut);
        //Test

        if(msgOut.contains("Received")){    // Depending of the message type (confirmation or data exchanging) the performative will be different
            strMessage.setPerformative(7);  // Performative = INFORM
        } else {
            strMessage.setPerformative(16); // Performative = REQUEST
        }
        System.out.println("--Sended message: " + strMessage.readMessage());
        try {
            JadeGateway.execute(strMessage);    // calls processCommand method of Gateway Agent
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("<-Java Send");
    }

    //Function for reading the data received in ACL messages
    public static String recv() {
//        String recvMsg = "{\"Operation_No_of_Items\":3,\"Id_Batch_Reference\":111,\"Id_Machine_Reference\":11,\"Operation_Ref_Service_Type\":4,\"Control_Flag_New_Service\":true,\"Id_Order_Reference\":11,\"Id_Ref_Subproduct_Type\":1}";
        String recvMsg;
        StructMessage strMessage = new StructMessage();
        strMessage.setAction("receive");
        System.out.println("Calling Execute");
        try {
            JadeGateway.execute(strMessage);
        } catch(Exception e) {
            System.out.println(e);
        }
        if(strMessage.readNewData()){
            recvMsg=strMessage.readMessage();
            System.out.println("--Received: " + recvMsg);
        }else{
            System.out.println("--No answer");
            recvMsg="";
        }
        System.out.println("<-Java recv");
        return recvMsg;
    }

    //Modifica la direccon de Sistem.out, teniendo las trazas en un fichero en lugar de por terminal.
    public static void redirectOutput(){
        // Create a log directory
        File directoryLogs = new File("C:\\Users\\Operator\\Documents");
        directoryLogs.mkdirs();
        try {
            // Create a log file
            File fileLog = new File(directoryLogs, "debugFile.txt");
            fileLog.createNewFile();
            // Create a stream to to the log file
            FileOutputStream f = new FileOutputStream(fileLog);
            System.setOut(new PrintStream(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
