package es.ehu.domain.manufacturing.agents.cognitive;

import jade.core.Agent;

public class instanciaGW {

    private static Boolean working =false;
    public static void main(String[] args){

        //Opcion 1.

        ROSJADEgw gw = new ROSJADEgw();
        try {
            Thread.sleep(3);
        } catch (InterruptedException ie)
        {
            System.out.println("Scanning...");
        }

        ROSJADEgw.init();

        try {
            Thread.sleep(10);
        } catch (InterruptedException ie)
        {
            System.out.println("Scanning...");
        }

        System.out.println("///////////////////////////////////////////////////////////////////");
       //while(working!=true){

           String recMS=ROSJADEgw.recv();

           if(recMS!=null){
                Ros_Jade_Msg nuevoMsg = new Ros_Jade_Msg("1","data",recMS);

                gw.enviarMSG(nuevoMsg);
                //working=true;

           }else{
               //System.out.println("No se ha recibido mensaje");
           }


       // }



        //System.out.println("Comienza arranque del Nodo Dummy(Kobuki)");
        //Arrancar nodo dummy
        // Ros_Jade_Dummy dummy=new Ros_Jade_Dummy();

        //Opcion 2


        //ROSJADEgw gw =new ROSJADEgw();



    }
}
