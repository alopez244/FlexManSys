package es.ehu.domain.manufacturing.agents.cognitive;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import es.ehu.domain.manufacturing.utilities.StructMessage;
import jade.core.Profile;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import jade.wrapper.ControllerException;
import jade.wrapper.gateway.JadeGateway;
import org.ros.address.InetAddressFactory;
import java.util.HashMap;

public class ACL_HTTP_Gateway {

    private String request;
    private String response;

    public ACL_HTTP_Gateway(String assetName) {

        //Primero, las presentaciones (portada para el usuario)
        System.out.println("This is a Java Class acting as a gateway between ACL (FlexManSys Agents) and HTTP (IPB Demonstrator).\n");

        //Primero habrá que inicializar el gatewayAgent
        try {
            this.jadeInit(assetName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Después habrá que declarar el while para mantener el hilo en marcha
        while (true){

            //Dentro del while, se comprobará constantemente si se han recibido mensajes en el gatewayAgent
            try {
               request = jadeReceive();
            } catch (ControllerException | InterruptedException e) {
                e.printStackTrace();
            }

            if (request != null) { //Si se ha recibido un mensaje, se procesa el servicio

                try {
                    response = requestService_HTTP(request);
                } catch (UnirestException e) {
                    e.printStackTrace();
                }

                if (response != null) {
                    try {
                        jadeSend(response);
                    } catch (ControllerException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    private void jadeInit(String assetName) throws Exception {

        //Se definen las propiedades que caracterizan al contenedor del GatewayAgent: IP, puerto y nombre del contenedor
        Properties pp = new Properties();
        String host = InetAddressFactory.newNonLoopback().getHostName();
        String port = "1099";
        pp.setProperty(Profile.MAIN_HOST, host);
        pp.setProperty(Profile.MAIN_PORT, port);
        pp.setProperty(Profile.LOCAL_PORT, port);
        String containerName = "GatewayCont"+assetName;
        pp.setProperty(Profile.CONTAINER_NAME, containerName);

        //Se inicializa el GatewayAgent
        JadeGateway.init("es.ehu.domain.manufacturing.agents.cognitive.GWAgentHTTP", pp);

        //Se ejecuta el comando init para garantizar el arranque del GatewayAgent
        StructMessage strMessage = new StructMessage();
        strMessage.setAction("init");
        JadeGateway.execute(strMessage);
    }

    private String jadeReceive() throws ControllerException, InterruptedException {

            //Se invoca la acción receive del GatewayAgent para recibir posibles mensajes
            StructMessage strMessage = new StructMessage();
            strMessage.setAction("receive");
            JadeGateway.execute(strMessage);

            //Se comprueba si hay un nuevo mensaje
            String msgFromGW;
            if (strMessage.readMessage() != null) { //En caso afirmativo, se lee el mensaje
                msgFromGW = strMessage.readMessage();

            } else { //En caso contrario, se devuelve null
                msgFromGW = null;
            }

            return msgFromGW;
    }

    private void jadeSend(String response) throws ControllerException, InterruptedException {

        //Se declara la estructura que se le va a pasar al GatewayAgent
        StructMessage strMessage = new StructMessage();

        //Se definen la acción (enviar), el contenido (response), y la performativa (inform)
        strMessage.setAction("send");
        strMessage.setMessage(response);
        strMessage.setPerformative(ACLMessage.INFORM);

        //Por último, se envía el mensaje
        JadeGateway.execute(strMessage);
    }

    private String requestService_HTTP (String cmd) throws UnirestException {

        //Primero inicializo las variables que voy a necesitar
        String result = null;
        String service;
        String body;
        HashMap cmdHashMap;

        //Después, transformo el mensaje recibido de vuelta en un HashMap
        cmdHashMap = new Gson().fromJson(cmd, HashMap.class);

        //Obtengo el tipo de servicio
        service = cmdHashMap.get("Service").toString();

        //Obtengo el nombre y valor de los parámetros
         body = (String) cmdHashMap.get("Parameters");

        //Después ejecuto el switch
        switch (service) {
            case "POST_Reset":

                //Recibo la respuesta
                HttpResponse<JsonNode> post_Reset = Unirest.post("http://127.0.0.1:1880/Reset/ManufacturingStation").asJson();

                //Solo me quedo con el contenido si la comunicación ha sido correcta
                if (post_Reset.getStatus() == 200) result = post_Reset.getBody().toString();
                break;
            case "GET_PA":

                //Recibo la respuesta al get
                HttpResponse<JsonNode> get_PA = Unirest.get("http://127.0.0.1:1880/State/ManufacturingStation/PA").asJson();

                //Solo me quedo con el contenido si la comunicación ha sido corecta
                if (get_PA.getStatus() == 200) result = get_PA.getBody().toString();
                break;
            case "POST_PA":

                //Recibo la respuesta al post
                HttpResponse<JsonNode> post_PA = Unirest.post("http://127.0.0.1:1880/Request/ManufacturingStation/PA")
                        .header("Content-type", "application/json")
                        .body(body)
                        .asJson();

                //Solo me quedo con el contenido si la comunicación ha sido corecta
                if (post_PA.getStatus() == 200) result = post_PA.getBody().toString();
                break;
            case "GET_PB":

                //Recibo la respuesta al get
                HttpResponse<JsonNode> get_PB = Unirest.get("http://127.0.0.1:1880/State/ManufacturingStation/PB").asJson();

                //Solo me quedo con el contenido si la comunicación ha sido corecta
                if (get_PB.getStatus() == 200) result = get_PB.getBody().toString();
                break;
            case "POST_PB":

                //Recibo la respuesta al post
                HttpResponse<JsonNode> post_PB = Unirest.post("http://127.0.0.1:1880/Request/ManufacturingStation/PB")
                        .header("Content-type", "application/json")
                        .body(body)
                        .asJson();

                //Solo me quedo con el contenido si la comunicación ha sido corecta
                if (post_PB.getStatus() == 200) result = post_PB.getBody().toString();
                break;
            case "GET_IA":

                //Recibo la respuesta al get
                HttpResponse<JsonNode> get_IA = Unirest.get("http://127.0.0.1:1880/State/ManufacturingStation/IA").asJson();

                //Solo me quedo con el contenido si la comunicación ha sido corecta
                if (get_IA.getStatus() == 200) result = get_IA.getBody().toString();
                break;
            case "POST_IA":

                //Recibo la respuesta al post
                HttpResponse<JsonNode> post_IA = Unirest.post("http://127.0.0.1:1880/Request/ManufacturingStation/IA")
                        .header("Content-type", "application/json")
                        .body(body)
                        .asJson();

                //Solo me quedo con el contenido si la comunicación ha sido corecta
                if (post_IA.getStatus() == 200) result = post_IA.getBody().toString();
                break;
            case "GET_IB":

                //Recibo la respuesta al get
                HttpResponse<JsonNode> get_IB = Unirest.get("http://127.0.0.1:1880/State/ManufacturingStation/IB").asJson();

                //Solo me quedo con el contenido si la comunicación ha sido corecta
                if (get_IB.getStatus() == 200) result = get_IB.getBody().toString();
                break;
            case "POST_IB":

                //Recibo la respuesta al post
                HttpResponse<JsonNode> post_IB = Unirest.post("http://127.0.0.1:1880/Request/ManufacturingStation/IB")
                        .header("Content-type", "application/json")
                        .body(body)
                        .asJson();

                //Solo me quedo con el contenido si la comunicación ha sido corecta
                if (post_IB.getStatus() == 200) result = post_IB.getBody().toString();
                break;
            case "GET_Robot":

                //Recibo la respuesta al get
                HttpResponse<JsonNode> get_Robot = Unirest.get("http://127.0.0.1:1880/State/TrasportRobot").asJson();

                //Solo me quedo con el contenido si la comunicación ha sido corecta
                if (get_Robot.getStatus() == 200) result = get_Robot.getBody().toString();
                break;
            case "POST_Robot": {

                //Recibo la respuesta al post
                HttpResponse<JsonNode> post_Robot = Unirest.post("http://127.0.0.1:1880/Request/TrasportRobot")
                        .header("Content-type", "application/json")
                        .body(body)
                        .asJson();

                //Solo me quedo con el contenido si la comunicación ha sido corecta
                if (post_Robot.getStatus() == 200) result = post_Robot.getBody().toString();
                break;
            }
            default:
                System.out.println("The service requested is incorrect.");
        }

        //Por último, devuelvo el resultado
        return result;
    }

    public static void main(String[] args) {
        String assetName = args[0];
        ACL_HTTP_Gateway GW_ACL_HTTP= new ACL_HTTP_Gateway(assetName);
    }
}
