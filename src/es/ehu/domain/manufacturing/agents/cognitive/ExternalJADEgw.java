package es.ehu.domain.manufacturing.agents.cognitive;

import com.google.gson.Gson;
import es.ehu.domain.manufacturing.utilities.StructMessage;
import jade.core.Profile;
import jade.util.leap.Properties;
import jade.wrapper.gateway.JadeGateway;

import java.util.HashMap;
import java.util.Scanner;

public class ExternalJADEgw {


    public static void main(String[] args) {
        String msgRecv;
        Scanner in = new Scanner(System.in);
        String action;
        String batchID = "";
        HashMap msg;
        agentInit();

        while(true){
            System.out.println("Introduzca accion: ");
            action = in.nextLine();

            if (action.equals("recibir")){
                msgRecv = recv();
            } else if(action.equals("confirmar")){
                msg = messageReceived();
                send(msg);
            } else if(action.equals("servicioOK")){
                msg = ServiceCompleted(batchID);
                send(msg);
            }
        }
    }

    public static void agentInit(){
        System.out.println("->Java Agent Init");
        String host = "127.0.0.1";              //Local host IP)
        String port = "1099";                   //Port on which the agent manager is running
        Properties pp = new Properties();
        pp.setProperty(Profile.MAIN_HOST, host);
        pp.setProperty(Profile.MAIN_PORT, port);
        pp.setProperty(Profile.CONTAINER_NAME, "GatewayCont");      //-->Name ControlGatewayCont
        JadeGateway.init("es.ehu.domain.manufacturing.agents.cognitive.GWAgent",pp);            //Gateway Agent Initialization
        System.out.println("<-Java Agent Init");
    }

    //Function to send ACL messages by receiving a String that is added in the message.
    public static void send(HashMap msgOut) {  //Sends the data String that has been given
        System.out.println("->Java Send");
        StructMessage strMessage = new StructMessage();
        strMessage.setAction("send");
        strMessage.setMessage(new Gson().toJson(msgOut));
        if(msgOut.containsKey("Received")){
            strMessage.setPerformative(7);
        } else {
            strMessage.setPerformative(16);
        }
        System.out.println("--Sended message: " + strMessage.readMessage());
        try {
            JadeGateway.execute(strMessage);
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("<-Java Send");
    }

    //Function for reading the data received in ACL messages
    public static String recv() {
        String recvMsg;
        System.out.println("->Java recv");
        StructMessage strMessage = new StructMessage();
        strMessage.setAction("receive");
        try {
            JadeGateway.execute(strMessage);
        } catch(Exception e) {
            System.out.println(e);
        }
        if(strMessage.readNewData()==true){
            recvMsg=strMessage.readMessage();
            System.out.println("--Received: " + recvMsg);
        }else{
            System.out.println("--No answer");
            recvMsg="";
        }
        System.out.println("<-Java recv");
        return recvMsg;
    }

    public static HashMap messageReceived(){
        HashMap map = new HashMap();
        map.put("Received", true);
        return map;
    }

    public static HashMap ServiceCompleted(String batchID){
        HashMap map = new HashMap();
        map.put("Flag_Service_Completed", true);
        map.put("Batch_Reference", "B1_O1");
        return map;
    }

}
