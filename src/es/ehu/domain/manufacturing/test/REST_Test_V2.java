package es.ehu.domain.manufacturing.test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import es.ehu.domain.manufacturing.utilities.StructMessage;
import jade.core.Profile;
import jade.util.leap.Properties;
import jade.wrapper.ControllerException;
import jade.wrapper.gateway.JadeGateway;
import org.ros.address.InetAddressFactory;

import java.util.Scanner;

public class REST_Test_V2 {

    private boolean workingFlag;
    private  String msg;

    public REST_Test_V2() {

        //Primero, las presentaciones (portada para el usuario)
        System.out.println("This is a Java Class acting as a gateway between ACL (FlexManSys Agents) and HTTP (IPB Demonstrator).\n");

        //Primero habr� que inicializar el gatewayAgent
        try {
            this.jadeInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Despu�s habr� que declarar el while para mantener el hilo en marcha
        while (true){

            //Dentro del while, se comprobar� constantemente si se han recibido mensajes en el gatewayAgent
            try {
               msg = jadeReceive();
            } catch (ControllerException | InterruptedException e) {
                e.printStackTrace();
            }

            if (msg != null) { //Si se ha recibido un mensaje, se procesa el servicio

                try {
                    requestService_HTTP(msg);
                } catch (UnirestException e) {
                    e.printStackTrace();
                }

            }
        }

        //Desde el while habr� que gestionar la recepci�n y env�o entre el gatewayAgent y el asset.
    }

    private void jadeInit() throws Exception {

        //Se definen las propiedades que caracterizan al contenedor del GatewayAgent: IP, puerto y nombre del contenedor
        Properties pp = new Properties();
        String host = InetAddressFactory.newNonLoopback().getHostName();
        String port = "1099";
        pp.setProperty(Profile.MAIN_HOST, host);
        pp.setProperty(Profile.MAIN_PORT, port);
        pp.setProperty(Profile.LOCAL_PORT, port);
        String containerName = "GatewayContTest1"; //TODO: Cambiar el nombre del contenedor para que dependa de a qu� M�quina se conecta
        pp.setProperty(Profile.CONTAINER_NAME, containerName);

        //Se inicializa el GatewayAgent
        JadeGateway.init("es.ehu.domain.manufacturing.agents.cognitive.GWAgentHTTP", pp);

        //Se ejecuta el comando init para garantizar el arranque del GatewayAgent
        StructMessage strMessage = new StructMessage();
        strMessage.setAction("init");
        JadeGateway.execute(strMessage);
    }

    private String jadeReceive() throws ControllerException, InterruptedException {

        //Primero se comprueba si el asset est� ocupado
        if (!workingFlag){

            //Si est� libre, se invoca la acci�n receive del GatewayAgent para recibir posibles mensajes
            StructMessage strMessage = new StructMessage();
            strMessage.setAction("receive");
            JadeGateway.execute(strMessage);

            //Se comprueba si hay un nuevo mensaje
            String msgFromGW;
            if (strMessage.readNewData()) { //En caso afirmativo, se lee el mensaje y se pone el workingFlag a true
                msgFromGW = strMessage.readMessage();
                workingFlag=true;

            } else { //En caso contrario, se devuelve null
                msgFromGW = null;
            }

            return msgFromGW;

        } else { //Si el asset est� ocupado, no se pueden leer nuevos mensajes, se devuelve null

            return null;
        }
    }

    private void requestService_HTTP (String cmd) throws UnirestException {
        switch (cmd) {
            case "POST_Reset":

                //Recibo la respuesta al post
                HttpResponse<JsonNode> post_Reset = Unirest.post("http://127.0.0.1:1880/Reset/ManufacturingStation").asJson();

                if (post_Reset.getStatus() == 200) {
                    System.out.println("The response received is:\n");
                    System.out.println(post_Reset.getBody().toString());
                }

                break;
            case "GET_PA":

                //Recibo la respuesta al get
                HttpResponse<JsonNode> get_PA = Unirest.get("http://127.0.0.1:1880/State/ManufacturingStation/PA").asJson();

                //Solo me quedo con el contenido si la comunicaci�n ha sido corecta
                if (get_PA.getStatus() == 200) {
                    System.out.println("The response received is:\n");
                    System.out.println(get_PA.getBody().toString());
                }
                break;
            case "POST_PA": {

                //Solicito al usuario del programa el valor de la referencia del producto
                String type;
                Scanner in_2 = new Scanner(System.in);
                System.out.print("Please, introduce the Ref_Subproduct_Type: ");
                type = in_2.nextLine();
                System.out.println();

                //Construyo el cuerpo del mensaje a enviar
                String body = "{\n  \"Ref_Subproduct_Type\": \"" + type + "\"\n}\n";

                //Recibo la respuesta al post
                HttpResponse<JsonNode> post_PA = Unirest.post("http://127.0.0.1:1880/Request/ManufacturingStation/PA")
                        .header("Content-type", "application/json")
                        .body(body)
                        .asJson();

                //Solo me quedo con el contenido si la comunicaci�n ha sido corecta
                if (post_PA.getStatus() == 200) {
                    System.out.println("The response received is:\n");
                    System.out.println(post_PA.getBody().toString());
                }
                break;
            }
            case "GET_PB":

                //Recibo la respuesta al get
                HttpResponse<JsonNode> get_PB = Unirest.get("http://127.0.0.1:1880/State/ManufacturingStation/PB").asJson();

                //Solo me quedo con el contenido si la comunicaci�n ha sido corecta
                if (get_PB.getStatus() == 200) {
                    System.out.println("The response received is:\n");
                    System.out.println(get_PB.getBody().toString());
                }
                break;
            case "POST_PB": {

                //Solicito al usuario del programa el valor de la referencia del producto
                String type;
                Scanner in_2 = new Scanner(System.in);
                System.out.print("Please, introduce the Ref_Subproduct_Type: ");
                type = in_2.nextLine();
                System.out.println();

                //Construyo el cuerpo del mensaje a enviar
                String body = "{\n  \"Ref_Subproduct_Type\": \"" + type + "\"\n}\n";

                //Recibo la respuesta al post
                HttpResponse<JsonNode> post_PB = Unirest.post("http://127.0.0.1:1880/Request/ManufacturingStation/PB")
                        .header("Content-type", "application/json")
                        .body(body)
                        .asJson();

                //Solo me quedo con el contenido si la comunicaci�n ha sido corecta
                if (post_PB.getStatus() == 200) {
                    System.out.println("The response received is:\n");
                    System.out.println(post_PB.getBody().toString());
                }
                break;
            }
            case "GET_IA":

                //Recibo la respuesta al get
                HttpResponse<JsonNode> get_IA = Unirest.get("http://127.0.0.1:1880/State/ManufacturingStation/IA").asJson();

                //Solo me quedo con el contenido si la comunicaci�n ha sido corecta
                if (get_IA.getStatus() == 200) {
                    System.out.println("The response received is:\n");
                    System.out.println(get_IA.getBody().toString());
                }
                break;
            case "POST_IA": {

                //Solicito al usuario del programa el valor de la referencia del producto
                String type;
                Scanner in_2 = new Scanner(System.in);
                System.out.print("Please, introduce the Ref_Subproduct_Type: ");
                type = in_2.nextLine();
                System.out.println();

                //Construyo el cuerpo del mensaje a enviar
                String body = "{\n  \"Ref_Subproduct_Type\": \"" + type + "\"\n}\n";

                //Recibo la respuesta al post
                HttpResponse<JsonNode> post_IA = Unirest.post("http://127.0.0.1:1880/Request/ManufacturingStation/IA")
                        .header("Content-type", "application/json")
                        .body(body)
                        .asJson();

                //Solo me quedo con el contenido si la comunicaci�n ha sido corecta
                if (post_IA.getStatus() == 200) {
                    System.out.println("The response received is:\n");
                    System.out.println(post_IA.getBody().toString());
                }
                break;
            }
            case "GET_IB":

                //Recibo la respuesta al get
                HttpResponse<JsonNode> get_IB = Unirest.get("http://127.0.0.1:1880/State/ManufacturingStation/IB").asJson();

                //Solo me quedo con el contenido si la comunicaci�n ha sido corecta
                if (get_IB.getStatus() == 200) {
                    System.out.println("The response received is:\n");
                    System.out.println(get_IB.getBody().toString());
                }
                break;
            case "POST_IB": {

                //Solicito al usuario del programa el valor de la referencia del producto
                String type;
                Scanner in_2 = new Scanner(System.in);
                System.out.print("Please, introduce the Ref_Subproduct_Type: ");
                type = in_2.nextLine();
                System.out.println();

                //Construyo el cuerpo del mensaje a enviar
                String body = "{\n  \"Ref_Subproduct_Type\": \"" + type + "\"\n}\n";

                //Recibo la respuesta al post
                HttpResponse<JsonNode> post_IB = Unirest.post("http://127.0.0.1:1880/Request/ManufacturingStation/IB")
                        .header("Content-type", "application/json")
                        .body(body)
                        .asJson();

                //Solo me quedo con el contenido si la comunicaci�n ha sido corecta
                if (post_IB.getStatus() == 200) {
                    System.out.println("The response received is:\n");
                    System.out.println(post_IB.getBody().toString());
                }
                break;
            }
            case "GET_Robot":

                //Recibo la respuesta al get
                HttpResponse<JsonNode> get_Robot = Unirest.get("http://127.0.0.1:1880/State/TrasportRobot").asJson();

                //Solo me quedo con el contenido si la comunicaci�n ha sido corecta
                if (get_Robot.getStatus() == 200) {
                    System.out.println("The response received is:\n");
                    System.out.println(get_Robot.getBody().toString());
                }
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
                String first;
                Scanner in_2 = new Scanner(System.in);
                System.out.print("Please, introduce the first position: ");
                first = in_2.nextLine();
                System.out.println();

                String last;
                Scanner in_3 = new Scanner(System.in);
                System.out.print("Please, introduce the final position: ");
                last = in_3.nextLine();
                System.out.println();

                //Construyo el cuerpo del mensaje a enviar
                String body = "{\n  \"Initial\": \"" + first + "\",\n  \"Final\": \"" + last + "\"\n}\n";

                //Recibo la respuesta al post
                HttpResponse<JsonNode> post_Robot = Unirest.post("http://127.0.0.1:1880/Request/TrasportRobot")
                        .header("Content-type", "application/json")
                        .body(body)
                        .asJson();

                //Solo me quedo con el contenido si la comunicaci�n ha sido corecta
                if (post_Robot.getStatus() == 200) {
                    System.out.println("The response received is:\n");
                    System.out.println(post_Robot.getBody().toString());
                }
                break;
            }
            case "exit":
                System.out.println("Goodbye.");
                break;
            default:
                System.out.println("The service requested is incorrect.");
                break;
        }
    }

    public static void main(String[] args) {
        REST_Test_V2 GW_ACL_HTTP= new REST_Test_V2();
    }
}
