package es.ehu.domain.manufacturing.test;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;


import java.util.ArrayList;

 public class dummy extends Agent{
     private boolean endFlag=false;
    protected Agent myAgent=this;


    public void setup(){
        addBehaviour(new do_heavy_stuff() );
        System.out.println("Dummy started");
    }

    public class do_heavy_stuff extends SimpleBehaviour {
        ArrayList<String> results=new ArrayList<String>();
       int max=3000000;
       double res=0;
       double num=523456.54544;
       double den=254315.14544;

       @Override
       public void onStart(){
            System.out.println("Inicia agente (solo una ejecución)***********************************");
       }
       @Override
       public int onEnd(){
            System.out.println("Muere agente***********************************");
           System.exit(0);
           return 0;
       }
       @Override
       public boolean done(){
            System.out.println("Finaliza***********************************");
           return endFlag;
       }

        @Override
        public void action() {
            int iteration = 0;

            System.out.println("Start: "+ System.currentTimeMillis());
            while(iteration<max){
                res=num/den;
                results.add(String.valueOf(res));
                iteration++;
            }
            System.out.println("Finish: "+ System.currentTimeMillis());
            endFlag=true;
        }
    }
}
