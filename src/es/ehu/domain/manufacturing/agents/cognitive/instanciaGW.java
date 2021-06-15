package es.ehu.domain.manufacturing.agents.cognitive;

public class instanciaGW {


    public static void main(String[] args){

        //Opcion 1. Error al hacer RosCore.start



        ROSJADEgw.init();


        //Opcion 2
       // TransportAgent ta=new TransportAgent();
       // ROSJADEgw gw = new ROSJADEgw();


        System.out.println("Comienza arranque del Nodo Dummy(Kobuki)");
        //Arrancar nodo dummy
        //Ros_Jade_Dummy dummy=new Ros_Jade_Dummy();



    }
}
