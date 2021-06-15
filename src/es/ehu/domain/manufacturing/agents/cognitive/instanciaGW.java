package es.ehu.domain.manufacturing.agents.cognitive;

import es.ehu.domain.manufacturing.agents.TransportAgent;
import jade.core.Agent;

public class instanciaGW {

    public static void main(String[] args){

        //Opcion 1. Error al hacer RosCore.start

        //ROSJADEgw.init();


        //Opcion 2
       // TransportAgent ta=new TransportAgent();
        //ROSJADEgw gw = new ROSJADEgw(ta);


        //Ros_Jade rj=new Ros_Jade(ta);


        //Arrancar nodo dummy
        Ros_Jade_Dummy dummy=new Ros_Jade_Dummy();

    }
}
