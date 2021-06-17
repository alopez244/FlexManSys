package es.ehu.domain.manufacturing.agents.cognitive;

import jade.core.Agent;
import social_msgs.social;

public class instanciaGW {

    private static Boolean working =false;
    public static void main(String[] args){

        //Opcion 1.

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ie)
        {
            System.out.println("Scanning...");
        }
        ROSJADEgw gw = new ROSJADEgw();
        try {
            Thread.sleep(3);
        } catch (InterruptedException ie)
        {
            System.out.println("Scanning...");
        }

        ROSJADEgw.init();

        try {
            Thread.sleep(20);
        } catch (InterruptedException ie)
        {
            System.out.println("Scanning...");
        }

        System.out.println("///////////////////////////////////////////////////////////////////");

        while (true) {

            java.lang.String recMS = ROSJADEgw.recv();

            if (recMS != null) {
                Ros_Jade_Msg nuevoMsg = new Ros_Jade_Msg("1", "data", recMS);

                gw.enviarMSG(nuevoMsg);
                gw.setWorkingFlag(false);


        /*
                //working=true;
               while (ROSJADEgw.getWorkingFlag()==true) {
                   if (gw.getSendMsgFlag() == true) {
                       java.lang.String msg = gw.getMessage();
                       if(msg!=null){
                           gw.setSendMsgFlag(false);
                           ROSJADEgw.send(msg);
                           System.out.println("AAAAAAAAAAAAAAAAAAA");
                       }
                   }
               }
         */

/*
               while(ROSJADEgw.getWorkingFlag()==true){
                   if (gw.getSendMsgFlag() == true) {
                       social msg = gw.getMessage();
                       if (msg != null) {
                           ROSJADEgw.send(msg.getContent().get(0));
                           gw.setSendMsgFlag(false);
                           //cont++;
                       }
                   }
                }
 */


            } else {
//                System.out.println("No se ha recibido mensaje");
            }
        }
    }
}