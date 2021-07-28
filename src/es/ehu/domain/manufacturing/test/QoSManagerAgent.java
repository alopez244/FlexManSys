package es.ehu.domain.manufacturing.test;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

public class QoSManagerAgent extends Agent {
    private String mensaje;

    protected void setup() {

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                System.out.println("test");
                while(true){}

            }
        });
    }
}