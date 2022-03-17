package es.ehu.domain.manufacturing.agents.managementLayer;

import es.ehu.domain.manufacturing.utilities.StructMessage;
import jade.core.Profile;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import jade.wrapper.ControllerException;
import jade.wrapper.gateway.JadeGateway;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ACL_ODK_Gateway {

    public static void agentInit(String machineID) throws UnknownHostException {

        //A continuación, se definen el resto de parámetros que van a hacer falta para crear el gatewayAgent
        String localHostName = InetAddress.getLocalHost().getHostName();
        InetAddress[] addressses = InetAddress.getAllByName(localHostName);
        String host = "10.253.59.133";             //host of Alejandro PC at IPB
        String port = "1099";
        String containerName = "GatewayCont"+machineID;

        //Se declara un bucle para iterar sobre todas las IPs que se han obtenido en el array addresses[]
        for (InetAddress addresss : addressses) {
            if (addresss instanceof Inet4Address) {

                String[] localHost = String.valueOf(addresss).split("/");

                //Se definen las propiedades que caracterizan al contenedor del GatewayAgent:  puerto y nombre del contenedor
                Properties pp = new Properties();
                pp.setProperty(Profile.LOCAL_HOST, localHost[localHost.length - 1]); //Dirección IP del gatewayAgent
                pp.setProperty(Profile.MAIN_HOST, localHost[localHost.length - 1]); //Dirección IP de la plataforma de agentes (JADE)
                pp.setProperty(Profile.MAIN_PORT, port); //Puerto de acceso del gatewayAgent
                pp.setProperty(Profile.LOCAL_PORT, port); //Puerto de acceso de la plataforma de agentes (JADE)
                pp.setProperty(Profile.CONTAINER_NAME, containerName); //Nombre del contenedor

                //Se inicializa el GatewayAgent de la clase correspondiente
                JadeGateway.init("es.ehu.domain.manufacturing.agents.managementLayer.GWAgentODK", pp);

                //Se ejecuta el comando init para garantizar el arranque del GatewayAgent
                StructMessage strMessage = new StructMessage();
                strMessage.setAction("init");
                try {
                    JadeGateway.execute(strMessage);
                    break;
                } catch (ControllerException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static String recv() throws ControllerException, InterruptedException {

        //Se invoca la acción receive del GatewayAgent para recibir posibles mensajes
        StructMessage strMessage = new StructMessage();
        strMessage.setAction("receive");
        JadeGateway.execute(strMessage);

        //Se comprueba si hay un nuevo mensaje
        String msgFromGW;
        if (strMessage.readMessage() != null) { //En caso afirmativo, se lee el mensaje
            msgFromGW =strMessage.readMessage();
        }else{ //En caso contrario, se devuelve ""
            msgFromGW ="";
        }

        return msgFromGW;
    }


    public static void send(String msgOut) throws ControllerException, InterruptedException {

        //Se declara la estructura que se le va a pasar al GatewayAgent
        StructMessage strMessage = new StructMessage();

        //Se definen la acción (enviar) y el contenido (response)
        strMessage.setAction("send");
        strMessage.setMessage(msgOut);

        //Por último, se envía el mensaje
        JadeGateway.execute(strMessage);
    }

    public static boolean askstate() throws ControllerException, InterruptedException {

        //Se ejecuta el comando init para garantizar el arranque del GatewayAgent
        StructMessage strMessage = new StructMessage();
        strMessage.setAction("ask_state");
        JadeGateway.execute(strMessage);

        //Se comprueba si hay un nuevo mensaje
        if (strMessage.readMessage() != null) { //En caso afirmativo, se devuelve TRUE
            return true;
        }else{ //En caso contrario, se devuelve FALSE
            return false;
        }
    }

    public static void rcvstate(String state) throws ControllerException, InterruptedException { //recibe estado del PLC

        //Se declara la estructura que se le va a pasar al GatewayAgent
        StructMessage strMessage = new StructMessage();

        //Se definen la acción (enviar)y el contenido (response)
        strMessage.setAction("rcv_state");
        strMessage.setMessage(state);

        //Por último, se envía el mensaje
        JadeGateway.execute(strMessage);
    }
}
