package es.ehu.domain.manufacturing.test;

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

import java.util.Scanner;

public class REST_Test_V2 {

    private String request;
    private String response;

    public REST_Test_V2() {

        //Primero, las presentaciones (portada para el usuario)
        System.out.println("This is a Java Class acting as a gateway between ACL (FlexManSys Agents) and HTTP (IPB Demonstrator).\n");

        //Primero habrá que inicializar el gatewayAgent
        try {
            this.jadeInit();
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

    private void jadeInit() throws Exception {

        //Se definen las propiedades que caracterizan al contenedor del GatewayAgent: IP, puerto y nombre del contenedor
        Properties pp = new Properties();
        String host = InetAddressFactory.newNonLoopback().getHostName();
        String port = "1099";
        pp.setProperty(Profile.MAIN_HOST, host);
        pp.setProperty(Profile.MAIN_PORT, port);
        pp.setProperty(Profile.LOCAL_PORT, port);
        String containerName = "GatewayContTest1"; //TODO: Cambiar el nombre del contenedor para que dependa de a qué Máquina se conecta
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
            if (strMessage.readNewData()) { //En caso afirmativo, se lee el mensaje
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
        String type;
        Scanner in_1 = new Scanner(System.in);
        Scanner in_2 = new Scanner(System.in);
        String body;
        String first;
        String last;

        //Después ejecuto el switch
        switch (cmd) {
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

                //Solicito al usuario del programa el valor de la referencia del producto
                System.out.print("Please, introduce the Ref_Subproduct_Type: ");
                type = in_1.nextLine();
                System.out.println();

                //Construyo el cuerpo del mensaje a enviar
                body = "{\n  \"Ref_Subproduct_Type\": \"" + type + "\"\n}\n";

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

                //Solicito al usuario del programa el valor de la referencia del producto
                System.out.print("Please, introduce the Ref_Subproduct_Type: ");
                type = in_1.nextLine();
                System.out.println();

                //Construyo el cuerpo del mensaje a enviar
                body = "{\n  \"Ref_Subproduct_Type\": \"" + type + "\"\n}\n";

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

                //Solicito al usuario del programa el valor de la referencia del producto
                System.out.print("Please, introduce the Ref_Subproduct_Type: ");
                type = in_1.nextLine();
                System.out.println();

                //Construyo el cuerpo del mensaje a enviar
                body = "{\n  \"Ref_Subproduct_Type\": \"" + type + "\"\n}\n";

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

                //Solicito al usuario del programa el valor de la referencia del producto
                System.out.print("Please, introduce the Ref_Subproduct_Type: ");
                type = in_1.nextLine();
                System.out.println();

                //Construyo el cuerpo del mensaje a enviar
                body = "{\n  \"Ref_Subproduct_Type\": \"" + type + "\"\n}\n";

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

                String positions = "POST_Robot service available positions:\n"
                        + " PA > In/Out position for the Punching Machine A.\n"
                        + " PB > In/Out position for the Punching Machine B.\n"
                        + " IA > In/Out position for the Indexing Machine A.\n"
                        + " IB > In/Out position for the Indexing Machine B.\n"
                        + " warehouse_1 > Position of the input warehouse on the left.\n"
                        + " warehouse_2 > Position of the input warehouse on the right.\n"
                        + " warehouse_3 > Position of the output warehouse on the left.\n"
                        + " warehouse_4 > Position of the output warehouse on the right.\n\n";
                System.out.print(positions);

                //Solicito al usuario del programa el valor de la referencia del producto
                System.out.print("Please, introduce the first position: ");
                first = in_1.nextLine();
                System.out.println();

                System.out.print("Please, introduce the final position: ");
                last = in_2.nextLine();
                System.out.println();

                //Construyo el cuerpo del mensaje a enviar
                body = "{\n  \"Initial\": \"" + first + "\",\n  \"Final\": \"" + last + "\"\n}\n";

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
        REST_Test_V2 GW_ACL_HTTP= new REST_Test_V2();
    }
}
