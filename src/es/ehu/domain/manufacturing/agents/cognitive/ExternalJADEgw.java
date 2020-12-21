package es.ehu.domain.manufacturing.agents.cognitive;

import jade.core.Profile;
import jade.util.leap.Properties;
import jade.wrapper.gateway.JadeGateway;


import java.io.*;
import java.util.HashMap;
import es.ehu.domain.manufacturing.utilities.StructMessage;
import com.google.gson.Gson;

public class ExternalJADEgw {
    private static int exID=0;
    //Main --> Usado solo como ejemplo para probar el funcionamiento sin llegar a implementar el ODK
    public static void main(String[] args) {
        String msg;
        System.out.println("-->Main Init");
        agentInit();

        while(true) {
            msg=ejemploStringDatos();   //Genera un String de datos, para ser enviado al Agente como ejemplo
            send(msg);                  //Se envia el mensaje a trav�s del gatewayAgent
            System.out.println("*** Enviado: " + msg);
            try {
                Thread.sleep(500); // para dar tiempo a que los mensajes de log se impriman despu�s del log del arranque del contenedor
            } catch(Exception e) {
                System.out.println(e);
            }
            msg=ejemploStringRecivido();    //Genera un String simulando una confirmacion de recepcion
            send(msg);                      //Se envia la confirmacion de recepcion al agente
            System.out.println("*** Enviado: " + msg);
            try {
                Thread.sleep(500); // para dar tiempo a que los mensajes de log se impriman despu�s del log del arranque del contenedor
            } catch(Exception e) {
                System.out.println(e);
            }
            String msgRecv=recv();          //Se ejecuta la func. recv, con lo que se tiene un String con el mensaje recibido
            System.out.println("*** Recibido: " + msgRecv);
            try {
                Thread.sleep(500); // para dar tiempo a que los mensajes de log se impriman despu�s del log del arranque del contenedor
            } catch(Exception e) {
                System.out.println(e);
            }
            System.out.println();   //Espacio en blanco
        }
    }

    //Gateway Agent Initialization. Necessary before executing the functions of sending and receiving messages
    public static void agentInit(){
        //redirectOutput();   //Util cuando no se dispone de terminal para mostrar las trazas. Trazas -> archivo txt
        System.out.println("->Java Agent Init");
        String host = "127.0.0.1";              //IP del local host (este equipo)
        String port = "1099";                   //Puerto en el que se esta ejecutando el gestor de agentes
        Properties pp = new Properties();
        pp.setProperty(Profile.MAIN_HOST, host);
        pp.setProperty(Profile.MAIN_PORT, port);
        pp.setProperty(Profile.CONTAINER_NAME, "GatewayCont");      //-->Nombre ControlGatewayCont
        JadeGateway.init("es.ehu.domain.manufacturing.agents.cognitive.GWAgent", pp);            //Inicializa el agente gateway
        System.out.println("<-Java Agent Init");
    }

    //Funcion para el env�o de mensajes ACL recibiendo un String que se a�ade en el mensaje.
    public static void send(String msgOut) {  //Env�a el String de datos que se le ha dado
        System.out.println("->Java Send");
        StructMessage strMessage = new StructMessage();
        strMessage.setAction("enviar");
        strMessage.setMessage(msgOut); //Problema traido desde ODK, primeros 2 caracteres son tama�o y longitud del array
        System.out.println("--Se envia: " + strMessage.readMessage());
        try {
            JadeGateway.execute(strMessage);
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("<-Java Send");
    }

    //Funcion para la lectura de los datos recibidos en mensajes ACL
    public static String recv() {       //Copia el String del mensaje recibido
        String recvMsg;
        System.out.println("->Java recv");
        StructMessage strMensaje = new StructMessage();
        strMensaje.setAction("recibir");
        try {
            JadeGateway.execute(strMensaje);
        } catch(Exception e) {
            System.out.println(e);
        }
        if(strMensaje.readNewData()==true){
            recvMsg=strMensaje.readMessage();
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
        File directoryLogs = new File("D:\\Documentos personales\\Adrian\\Master\\TFM\\Pruebas\\agenteGateway\\logs");
        directoryLogs.mkdirs();
        try {
            // Create a log file
            File fileLog = new File(directoryLogs, "log-ExternalJADEgw_v2.txt");
            fileLog.createNewFile();
            // Create a stream to to the log file
            FileOutputStream f = new FileOutputStream(fileLog);
            System.setOut(new PrintStream(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Genera un String para enviar como ejemplo
    public static String ejemploStringDatos(){
        exID++;
        HashMap map = new HashMap();
        map.put("Flag_Item_Completed", true);
        map.put("Batch_Reference",exID);
        return new Gson().toJson(map);
    }

    //Genera un String para simular una confirmacion de recepcion
    public static String ejemploStringRecivido(){
        HashMap map = new HashMap();
        map.put("Recibido", true);
        return new Gson().toJson(map);
    }
}
