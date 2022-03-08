package es.ehu.domain.manufacturing.test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

import java.util.Scanner;

public class REST_Test {
    public static void main(String[] args) throws UnirestException {

        // Esto es una primera prueba
//        try {
//            HttpResponse<JsonNode> jsonResponse = Unirest.post("").header("","").asJson();
//        } catch (UnirestException e) {
//            e.printStackTrace();
//        }
//        HttpRequest request = Unirest.get("");

        // Ahora voy a hacer una prueba con alguno de los GET y con el POST del reset.

        //Portada para el usuario
        System.out.println("This is a test method to check connectivity with the demonstrator at IPB.\n");
        String cmd = "";

        String api = "IPB demonstrator available services:\n"
                + " POST_Reset > Resets the state of the demonstrator before executing any other service.\n"
                + " GET_PA > Informs whether the PA is executing an operation or not.\n"
                + " POST_PA > Requests the PA to execute a punching operation.\n"
                + " GET_PB > Informs whether the PB is executing an operation or not.\n"
                + " POST_PB > Requests the PB to execute a punching operation.\n"
                + " GET_IA > Informs whether the IA is executing an operation or not.\n"
                + " POST_IA > Requests the IA to execute an indexing operation.\n"
                + " GET_IB > Informs whether the IB is executing an operation or not.\n"
                + " POST_PB > Requests the PB to execute an indexing operation.\n"
                + " GET_Robot > Informs whether the robot is executing an operation or not.\n"
                + " POST_Robot > Requests the robot to execute a manipulation operation.\n"
                + " exit > Shut down Planner Agent\n\n";
        System.out.print(api);

        //Entro en el loop
        while (!cmd.equals("exit")){
            Scanner in = new Scanner(System.in);
            System.out.print("Please, introduce the name of the service you want to invoke, or the word exit to stop the test: ");
            cmd = in.nextLine();
            System.out.println();

            //Evalúo el comando introducido por el usuario y ejecuto el método que corresponda en cada caso
            if (cmd.length()>0) {
                if (cmd.equals("POST_Reset")) {
                    HttpResponse<String> post_Reset_Response = Unirest.get("http://127.0.0.1:1880/Reset/ManufacturingStation").asString();

                } else if (cmd.equals("GET_PA")){

                    //Esto funciona, no tocar
                    HttpRequest get_PA = Unirest.get("http://127.0.0.1:1880/State/ManufacturingStation/PA");

                    //Siguiente paso, conseguir la respuesta
                    HttpResponse<JsonNode> get_PA_Response = get_PA.asJson();

                    //Solo me quedo con el contenido si la comunicación ha sido corecta
//                    if(get_PA.getStatus() == 200){
                        System.out.println("The response received is:\n");
                        System.out.println(get_PA.getBody().toString());
//                    }

                } else if (cmd.equals("POST_PA")){

                } else if (cmd.equals("GET_PB")){
                    HttpRequest get_PB = Unirest.get("http://127.0.0.1:1880/State/ManufacturingStation/PB");
                } else if (cmd.equals("POST_PB")){

                } else if (cmd.equals("GET_IA")){
                    HttpRequest get_IA = Unirest.get("http://127.0.0.1:1880/State/ManufacturingStation/IA");
                } else if (cmd.equals("POST_IA")){

                } else if (cmd.equals("GET_IB")){
                    HttpRequest get_IB = Unirest.get("http://127.0.0.1:1880/State/ManufacturingStation/IB");
                } else if (cmd.equals("POST_IB")){

                } else if (cmd.equals("GET_Robot")){
                    HttpRequest get_Robot = Unirest.get("http://127.0.0.1:1880/State/TransportRobot");
                } else if (cmd.equals("POST_Robot")){

                }else {
                    System.out.println("The service requested is incorrect.");
                }
            }
        }
    }
}
