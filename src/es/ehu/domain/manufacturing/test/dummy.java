package es.ehu.domain.manufacturing.test;
import jade.core.Agent;

public class dummy extends Agent{
    public void setup(){
        Agent myAgent=this;
        System.out.println("i'm dummy");
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        doDelete();
    }

}
