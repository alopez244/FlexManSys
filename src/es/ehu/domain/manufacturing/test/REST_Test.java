package es.ehu.domain.manufacturing.test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

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

                    //Recibo la respuesta al post
                    HttpResponse<JsonNode> post_Reset = Unirest.post("http://127.0.0.1:1880/Reset/ManufacturingStation").asJson();

                    if(post_Reset.getStatus() == 200){
                        System.out.println("The response received is:\n");
                        System.out.println(post_Reset.getBody().toString());
                    }

                } else if (cmd.equals("GET_PA")){

                    //Recibo la respuesta al get
                    HttpResponse<JsonNode> get_PA = Unirest.get("http://127.0.0.1:1880/State/ManufacturingStation/PA").asJson();

                    //Solo me quedo con el contenido si la comunicación ha sido corecta
                    if(get_PA.getStatus() == 200){
                        System.out.println("The response received is:\n");
                        System.out.println(get_PA.getBody().toString());
                    }
                } else if (cmd.equals("POST_PA")){

                    //Solicito al usuario del programa el valor de la referencia del producto
                    String type;
                    Scanner in_2 = new Scanner(System.in);
                    System.out.print("Please, introduce the Ref_Subproduct_Type: ");
                    type = in_2.nextLine();
                    System.out.println();

                    //Construyo el cuerpo del mensaje a enviar
                    String body = "{\n  \"Ref_Subproduct_Type\": \""+type+"\"\n}\n";

                    //Recibo la respuesta al post
                    HttpResponse<JsonNode> post_PA = Unirest.post("http://127.0.0.1:1880/Request/ManufacturingStation/PA")
                            .header("Content-type","application/json")
                            .body(body)
                            .asJson();

                    //Solo me quedo con el contenido si la comunicación ha sido corecta
                    if(post_PA.getStatus() == 200){
                        System.out.println("The response received is:\n");
                        System.out.println(post_PA.getBody().toString());
                    }
                } else if (cmd.equals("GET_PB")){

                    //Recibo la respuesta al get
                    HttpResponse<JsonNode> get_PB = Unirest.get("http://127.0.0.1:1880/State/ManufacturingStation/PB").asJson();

                    //Solo me quedo con el contenido si la comunicación ha sido corecta
                    if(get_PB.getStatus() == 200){
                        System.out.println("The response received is:\n");
                        System.out.println(get_PB.getBody().toString());
                    }
                } else if (cmd.equals("POST_PB")){

                    //Solicito al usuario del programa el valor de la referencia del producto
                    String type;
                    Scanner in_2 = new Scanner(System.in);
                    System.out.print("Please, introduce the Ref_Subproduct_Type: ");
                    type = in_2.nextLine();
                    System.out.println();

                    //Construyo el cuerpo del mensaje a enviar
                    String body = "{\n  \"Ref_Subproduct_Type\": \""+type+"\"\n}\n";

                    //Recibo la respuesta al post
                    HttpResponse<JsonNode> post_PB = Unirest.post("http://127.0.0.1:1880/Request/ManufacturingStation/PB")
                            .header("Content-type","application/json")
                            .body(body)
                            .asJson();

                    //Solo me quedo con el contenido si la comunicación ha sido corecta
                    if(post_PB.getStatus() == 200){
                        System.out.println("The response received is:\n");
                        System.out.println(post_PB.getBody().toString());
                    }
                } else if (cmd.equals("GET_IA")){

                    //Recibo la respuesta al get
                    HttpResponse<JsonNode> get_IA = Unirest.get("http://127.0.0.1:1880/State/ManufacturingStation/IA").asJson();

                    //Solo me quedo con el contenido si la comunicación ha sido corecta
                    if(get_IA.getStatus() == 200){
                        System.out.println("The response received is:\n");
                        System.out.println(get_IA.getBody().toString());
                    }
                } else if (cmd.equals("POST_IA")){

                    //Solicito al usuario del programa el valor de la referencia del producto
                    String type;
                    Scanner in_2 = new Scanner(System.in);
                    System.out.print("Please, introduce the Ref_Subproduct_Type: ");
                    type = in_2.nextLine();
                    System.out.println();

                    //Construyo el cuerpo del mensaje a enviar
                    String body = "{\n  \"Ref_Subproduct_Type\": \""+type+"\"\n}\n";

                    //Recibo la respuesta al post
                    HttpResponse<JsonNode> post_IA = Unirest.post("http://127.0.0.1:1880/Request/ManufacturingStation/IA")
                            .header("Content-type","application/json")
                            .body(body)
                            .asJson();

                    //Solo me quedo con el contenido si la comunicación ha sido corecta
                    if(post_IA.getStatus() == 200){
                        System.out.println("The response received is:\n");
                        System.out.println(post_IA.getBody().toString());
                    }
                } else if (cmd.equals("GET_IB")){

                    //Recibo la respuesta al get
                    HttpResponse<JsonNode> get_IB = Unirest.get("http://127.0.0.1:1880/State/ManufacturingStation/IB").asJson();

                    //Solo me quedo con el contenido si la comunicación ha sido corecta
                    if(get_IB.getStatus() == 200){
                        System.out.println("The response received is:\n");
                        System.out.println(get_IB.getBody().toString());
                    }
                } else if (cmd.equals("POST_IB")){

                    //Solicito al usuario del programa el valor de la referencia del producto
                    String type;
                    Scanner in_2 = new Scanner(System.in);
                    System.out.print("Please, introduce the Ref_Subproduct_Type: ");
                    type = in_2.nextLine();
                    System.out.println();

                    //Construyo el cuerpo del mensaje a enviar
                    String body = "{\n  \"Ref_Subproduct_Type\": \""+type+"\"\n}\n";

                    //Recibo la respuesta al post
                    HttpResponse<JsonNode> post_IB = Unirest.post("http://127.0.0.1:1880/Request/ManufacturingStation/IB")
                            .header("Content-type","application/json")
                            .body(body)
                            .asJson();

                    //Solo me quedo con el contenido si la comunicación ha sido corecta
                    if(post_IB.getStatus() == 200){
                        System.out.println("The response received is:\n");
                        System.out.println(post_IB.getBody().toString());
                    }
                } else if (cmd.equals("GET_Robot")){

                    //Recibo la respuesta al get
                    HttpResponse<JsonNode> get_Robot = Unirest.get("http://127.0.0.1:1880/State/TransportRobot").asJson();

                    //Solo me quedo con el contenido si la comunicación ha sido corecta
                    if(get_Robot.getStatus() == 200){
                        System.out.println("The response received is:\n");
                        System.out.println(get_Robot.getBody().toString());
                    }
                } else if (cmd.equals("POST_Robot")){

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
                    String body = "{\n  \"Initial\": \""+first+"\",\n  \"Final\": \""+last+"\"\n}\n";

                    //Recibo la respuesta al post
                    HttpResponse<JsonNode> post_Robot = Unirest.post("http://127.0.0.1:1880/Request/TransportRobot")
                            .header("Content-type","application/json")
                            .body(body)
                            .asJson();

                    //Solo me quedo con el contenido si la comunicación ha sido corecta
                    if(post_Robot.getStatus() == 200){
                        System.out.println("The response received is:\n");
                        System.out.println(post_Robot.getBody().toString());
                    }
                }else {
                    System.out.println("The service requested is incorrect.");
                }
            }
        }
    }
}