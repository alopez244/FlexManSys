package packet.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import jade.core.Profile;
import jade.util.leap.Properties;
import jade.wrapper.ControllerException;
import jade.wrapper.gateway.JadeGateway;

import java.net.Inet4Address;
import java.net.InetAddress;


public class ACL_MQTT_Gateway implements MqttCallback {
    private String assetName;
    private String host;

    private MqttClient mqttClient;

    // Calidad del mensaje
    private int qos = 0;

    //Tópicos
    private String agent2PLC;
    private String PLC2agent;
    private String MQTTmessagePayload = null;

    // Metodo constructor de la clase - metodo especial donde pueden inicializar cosas
    // Mismo nombre que la clase
    // args -> parametros desde línea de comandos
    public ACL_MQTT_Gateway(String[] args)  {
        try {
            this.jadeInit(args);
            this.mqttInit();
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void mqttInit(){
        // declaración de datos
        String broker = "tcp://192.168.2.151:1883";
        String clientId = this.assetName;
        MemoryPersistence persistence = new MemoryPersistence();

        this.agent2PLC = this.assetName + "/agent2PLC";
        this.PLC2agent = this.assetName + "/PLC2agent";

        try {
            // creamos un cliente MQTT
            mqttClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            // para indicar dónde se encuentran
            // los métodos para la gestión de eventos MQTT:
            // messageArrived(), deliveryComplete() y connectionLost()
            mqttClient.setCallback(this);

            // Realizar la conexión con el broker
            System.out.println("Connecting to broker: " + broker);
            mqttClient.connect(connOpts);
            System.out.println("Connected");

            // hacemos la subscripción
            mqttClient.subscribe(PLC2agent, qos);
            System.out.println("Subscribed to " + PLC2agent);

        } catch(MqttException e) {
            System.out.println(e.getMessage());;
        }
    }

    // Los tres metodos estos  estan definidos pero no estan rellenos. Estan
    // definidas las cabeceras pero no el interior.
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        this.MQTTmessagePayload = new String(message.getPayload());

        System.out.println("\nReceived a message!" +
                           "\n\tTopic:   " + topic +
                           "\n\tMessage: " + this.MQTTmessagePayload);
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("\nMessage published!");
    }

    public void connectionLost(Throwable cause) {
        System.out.println("\nConnection to broker lost!");
    }

    // en jadeInit arrancamos el agente pasarela, para ello hay que configurar todos los parametros
    private void jadeInit(String[] args) throws Exception {
        // Primero, se leen los argumentos recibidos en la invocación de la clase
        assetName = args[0];
        host = args[1];

        // A continuación, se definen el resto de parámetros que van a hacer falta para crear el gatewayAgent
        String localHostName = InetAddress.getLocalHost().getHostName();
        InetAddress[] addressses = InetAddress.getAllByName(localHostName);
        String port = "1099";
        String containerName = "GatewayCont" + assetName; // Nombre de contenedor. Fijamos este nombre para que se fije el nombre del agente.

        // Se declara un bucle para iterar sobre todas las IPs que se han obtenido en el array addresses[]
        for (InetAddress addresss : addressses) {
            if (addresss instanceof Inet4Address) {

                String[] localHost = String.valueOf(addresss).split("/");

                //Se definen las propiedades que caracterizan al contenedor del GatewayAgent:  puerto y nombre del contenedor
                Properties pp = new Properties();
                pp.setProperty(Profile.LOCAL_HOST, localHost[localHost.length - 1]); //Dirección IP del gatewayAgent
                pp.setProperty(Profile.MAIN_HOST, host); //Dirección IP de la plataforma de agentes (JADE)
                pp.setProperty(Profile.MAIN_PORT, port); //Puerto de acceso del gatewayAgent
                pp.setProperty(Profile.LOCAL_PORT, port); //Puerto de acceso de la plataforma de agentes (JADE)
                pp.setProperty(Profile.CONTAINER_NAME, containerName); //Nombre del contenedor

                //Se inicializa el GatewayAgent de la clase correspondiente
                JadeGateway.init("packet.mqtt.GWagentMQTT", pp);

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

    private void jadeReceive() throws ControllerException, InterruptedException, MqttException {

        // Obtengo el contenido el mensaje ACL
        StructMessage strMessage = new StructMessage();
        strMessage.setAction("recv");
        JadeGateway.execute(strMessage);

        if (strMessage.getMessage() != null) {
            String ACLmessagePayload = strMessage.getMessage();
            System.out.println("Publishing message...");
            MqttMessage message = new MqttMessage(ACLmessagePayload.getBytes());
            message.setQos(qos);
            this.mqttClient.publish(agent2PLC, message);
        }
    }

    private void jadeSend(String MQTTmessagePayload) throws ControllerException, InterruptedException {
         StructMessage strMessage = new StructMessage();
        strMessage.setAction("send");
        strMessage.setMessage(MQTTmessagePayload);

        JadeGateway.execute(strMessage);
    }

    public static void main(String[] args) {
        // Crear la instancia de una clase
        ACL_MQTT_Gateway gw = new ACL_MQTT_Gateway(args);

        try {
            while(true) {
                gw.jadeReceive();
                if (gw.MQTTmessagePayload != null) {
                    gw.jadeSend(gw.MQTTmessagePayload);
                    gw.MQTTmessagePayload = null;
                }
            }
        } catch(ControllerException | InterruptedException | MqttException e) {
            e.printStackTrace();
        }
    }
}