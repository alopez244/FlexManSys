package es.ehu.domain.manufacturing.test;

import es.ehu.domain.manufacturing.utilities.StructMessage;
import jade.core.Profile;
import jade.util.leap.Properties;
import jade.wrapper.ControllerException;
import jade.wrapper.gateway.JadeGateway;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IP_Test {

    public static void main(String[] args) throws UnknownHostException {

        String hostName = InetAddress.getLocalHost().getHostName();
        InetAddress addrs[] = InetAddress.getAllByName(hostName);
        String assetName = args[0];
        String localHost = args[1];
        String port = "1099";

        for (int i=0;i< addrs.length;i++){
            if (addrs[i] instanceof Inet4Address){

                //Se definen las propiedades que caracterizan al contenedor del GatewayAgent: IP, puerto y nombre del contenedor
                Properties pp = new Properties();

                String IP[] = String.valueOf(addrs[i]).split("/");
                pp.setProperty(Profile.LOCAL_HOST, IP[IP.length-1]);
                pp.setProperty(Profile.MAIN_HOST,localHost);

                pp.setProperty(Profile.MAIN_PORT, port);
                pp.setProperty(Profile.LOCAL_PORT, port);

                String containerName = "GatewayCont"+assetName;
                pp.setProperty(Profile.CONTAINER_NAME, containerName);

                //Se inicializa el GatewayAgent
                JadeGateway.init("es.ehu.domain.manufacturing.agents.managementLayer.GWAgentHTTP", pp);

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
}
