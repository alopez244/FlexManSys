package es.ehu.domain.manufacturing.agents.managementLayer;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import es.ehu.domain.manufacturing.utilities.StructMessage;
import jade.core.Profile;
import jade.util.leap.Properties;
import jade.wrapper.ControllerException;
import jade.wrapper.gateway.JadeGateway;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class ACL_HTTP_Gateway {

    private String request;
    private String response;
    private String assetName;
    private String host;
    private HashMap<String,Object> cmdHashMap;

    public ACL_HTTP_Gateway(String[] args) {

        //Antes de empezar, las presentaciones (portada para el usuario)
        System.out.println("This is a Java Class acting as a gateway between ACL (FlexManSys Agents) and HTTP (IPB Demonstrator).\n");

        //Primero habrá que inicializar el gatewayAgent
        try {
            this.jadeInit(args);
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

    private void jadeInit(String[] args) throws Exception {

        //Primero, se leen los argumentos recibidos en la invocación de la clase
        assetName = args[0];
        host = args[1];

        //A continuación, se definen el resto de parámetros que van a hacer falta para crear el gatewayAgent
        String localHostName = InetAddress.getLocalHost().getHostName();
        InetAddress[] addressses = InetAddress.getAllByName(localHostName);
        String port = "1099";
        String containerName = "GatewayCont"+assetName;

        //Se declara un bucle para iterar sobre todas las IPs que se han obtenido en el array addresses[]
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

        //Antes de devolver los resultados, vamos a completar la estructura con la información que le falta
        //Solo se van a hacer cambios si se han recibido los resultados de una operación POST (devuelve Time_Stamps)
        if(response.contains("Initial_Time_Stamp")){

            //En primer lugar, eliminamos el último caracter del string original
            String responseTrim=response.substring(0, response.length() - 1);
//            System.out.println(responseTrim);

            //El segundo paso será modificar los campos que ya están en el string para añadirles la cabecera
            String responseUpdate=responseTrim.replace("Initial_Time_Stamp","Data_Initial_Time_Stamp")
                    .replace("Final_Time_Stamp","Data_Final_Time_Stamp")
                    .replace("Item_Number","Id_Item_Number");

            //Si la operación la hace el robot, no nos devuelve item number, hay que añadirlo
            if (assetName.contains("Robot")){
                responseUpdate=responseUpdate+",\"Id_Item_number\":1";
            }

            //A continuación, se coge la información que queremos recuperar del mensaje recibido
            responseUpdate=responseUpdate+",\"Control_Flag_Item_Completed\":True,\"Control_Flag_Service_Completed\":True,"+
                    "Id_Machine_Reference:"+cmdHashMap.get("Id_Machine_Reference")+",Id_Order_Reference:"
                    +cmdHashMap.get("Id_Order_Reference")+",Id_Batch_Reference:"+cmdHashMap.get("Id_Batch_Reference")
                    +",Id_Ref_Subproduct_Type:"+cmdHashMap.get("Id_Ref_Subproduct_Type")+",Id_Ref_Service_Type:"
                    +cmdHashMap.get("Operation_Ref_Service_Type")+",Data_Service_Time_Stamp:1234567898765"+"}";

            System.out.println(responseUpdate);

            //Por último, se actualiza la variable response
            response=responseUpdate;
        }

        //Se declara la estructura que se le va a pasar al GatewayAgent
        StructMessage strMessage = new StructMessage();

        //Se definen la acción (enviar) y el contenido (response)
        strMessage.setAction("send");
        strMessage.setMessage(response);

        //Por último, se envía el mensaje
        JadeGateway.execute(strMessage);
    }

    private String requestService_HTTP (String cmd) throws UnirestException {

        //Primero inicializo las variables que voy a necesitar
        String result = null;
        String service;
        HashMap<String,String> body = new HashMap<>();

        //A continuación, se comprueba si el mensaje es de chequeo
        if (cmd.equals("ask_state")){
            service="GET_";
        } else {

            //Si no es un mensaje de chequeo, transformo el mensaje recibido de vuelta en un HashMap
            cmdHashMap = new Gson().fromJson(cmd, HashMap.class);

            //Se eliminan los decimales de los valores numéricos
            for(Map.Entry<String,Object> item : cmdHashMap.entrySet()){
                if (item.getValue() instanceof Double){
                    cmdHashMap.put(item.getKey(), String.valueOf(Math.round((Double) item.getValue())));
                }
            }

            //Obtengo el tipo de servicio y compruebo qué tipo de petición es (0=GET,1=POST)
            service = cmdHashMap.get("Operation_Ref_Service_Type").toString();
            if (service.equals("0")){
                service="GET_";
            } else if (service.equals("1")){
                service="POST_";
            }
        }

        service=service+assetName;

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
                if (get_PA.getStatus() == 200){
                    //result = get_PA.getBody().toString();
                    result = "Not working"; //Prueba, si hago un GET, devuelvo un Not working
                }
                break;
            case "POST_PA":

//                //Obtengo el nombre y el valor de los parámetros
//                body.put("Ref_Subproduct_Type", String.valueOf(cmdHashMap.get("Id_Ref_Subproduct_Type")));
//                String bodyJson = new Gson().toJson(body);

                //Obtengo el nombre y el valor de los parámetros (versión test)
                body.put("Ref_Subproduct_Type", String.valueOf(1));
                String bodyJson = new Gson().toJson(body);

                //Recibo la respuesta al post
                HttpResponse<JsonNode> post_PA = Unirest.post("http://127.0.0.1:1880/Request/ManufacturingStation/PA")
                        .header("Content-type", "application/json")
                        .body(bodyJson)
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

//                //Obtengo el nombre y el valor de los parámetros
//                body.put("Ref_Subproduct_Type", String.valueOf(cmdHashMap.get("Id_Ref_Subproduct_Type")));
//                bodyJson = new Gson().toJson(body);

                //Obtengo el nombre y el valor de los parámetros (versión test)
                body.put("Ref_Subproduct_Type", String.valueOf(1));
                bodyJson = new Gson().toJson(body);

                //Recibo la respuesta al post
                HttpResponse<JsonNode> post_PB = Unirest.post("http://127.0.0.1:1880/Request/ManufacturingStation/PB")
                        .header("Content-type", "application/json")
                        .body(bodyJson)
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

//                //Obtengo el nombre y el valor de los parámetros
//                body.put("Ref_Subproduct_Type", String.valueOf(cmdHashMap.get("Id_Ref_Subproduct_Type")));
//                bodyJson = new Gson().toJson(body);

                //Obtengo el nombre y el valor de los parámetros (versión test)
                body.put("Ref_Subproduct_Type", String.valueOf(1));
                bodyJson = new Gson().toJson(body);

                //Recibo la respuesta al post
                HttpResponse<JsonNode> post_IA = Unirest.post("http://127.0.0.1:1880/Request/ManufacturingStation/IA")
                        .header("Content-type", "application/json")
                        .body(bodyJson)
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

//                //Obtengo el nombre y el valor de los parámetros
//                body.put("Ref_Subproduct_Type", String.valueOf(cmdHashMap.get("Id_Ref_Subproduct_Type")));
//                bodyJson = new Gson().toJson(body);

                //Obtengo el nombre y el valor de los parámetros (versión test)
                body.put("Ref_Subproduct_Type", String.valueOf(1));
                bodyJson = new Gson().toJson(body);

                //Recibo la respuesta al post
                HttpResponse<JsonNode> post_IB = Unirest.post("http://127.0.0.1:1880/Request/ManufacturingStation/IB")
                        .header("Content-type", "application/json")
                        .body(bodyJson)
                        .asJson();

                //Solo me quedo con el contenido si la comunicación ha sido corecta
                if (post_IB.getStatus() == 200) result = post_IB.getBody().toString();
                break;
            case "GET_Robot":

                //Recibo la respuesta al get
                HttpResponse<JsonNode> get_Robot = Unirest.get("http://127.0.0.1:1880/State/TrasportRobot").asJson();

                //Solo me quedo con el contenido si la comunicación ha sido corecta
                if (get_Robot.getStatus() == 200){
                    //result = get_Robot.getBody().toString();
                    result = "Not working"; //Prueba, si hago un GET, devuelvo un Not working
                }
                break;
            case "POST_Robot": {

                //Obtengo el nombre y el valor de los parámetros
                String robotBody = String.valueOf(cmdHashMap.get("Operation_Parameters"));

                //Recibo la respuesta al post
                HttpResponse<JsonNode> post_Robot = Unirest.post("http://127.0.0.1:1880/Request/TrasportRobot")
                        .header("Content-type", "application/json")
                        .body(robotBody)
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

        ACL_HTTP_Gateway GW_ACL_HTTP= new ACL_HTTP_Gateway(args);
    }
}
