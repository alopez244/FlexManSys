package es.ehu.domain.manufacturing.test;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;

public class QoSManagerAgent extends Agent {
    private String mensaje;
    private ArrayList<String> ActualBatch = new ArrayList<String>();
    private int i=0;
    private ArrayList<ArrayList<String>> allDelays = new ArrayList<ArrayList<String>>();
    private MessageTemplate delaytemplate;


    protected void setup() {
        delaytemplate= MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchOntology("delay"));

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {

                ACLMessage msg = blockingReceive();

                if(msg!=null){
                    if(msg.getOntology().equals("delay")){

                        System.out.println(msg.getContent());
                        ActualBatch= organizeBatch(msg.getContent()); //Añade el batch especificado con su correspondiente delay a la lista de delays
                        allDelays.add(i,ActualBatch);
                        i++;
                    }
                    else if(msg.getOntology().equals("askdelay")){ //Si le consultan el delay

                        System.out.println("DELAY ASKED. BEFORE OR AFTER HAVING THE DATA?"); //usar para debug

                        ACLMessage reply=new ACLMessage(ACLMessage.INFORM);
                        String asking_batch=msg.getContent();
                        reply.setContent("0"); //si no encuentra singun batch que coincida en la lista devuelve un 0
                        boolean flag=true;
                        for(int i=0;i<allDelays.size()||flag==true;i++){

                                if (allDelays.get(i).get(0).equals(asking_batch)) {
                                    reply.setContent(allDelays.get(i).get(1));
                                    flag = false;
                                }
                        }
                        flag=true;
                        reply.addReceiver(msg.getSender());
                        reply.setOntology(msg.getOntology());
                        send(reply);
                        System.out.println("DELAY SENDED"); //usar para debug
                    }

                }


            }
        });
    }

    private ArrayList<String> organizeBatch(String data){
     ArrayList<String> batchdelay= new ArrayList<String>();
     String[] parts = data.split("/");
     String part1 = parts[0]; // BatchID
     String part2 = parts[1]; // Delay in minutes
     batchdelay.add(part1);
     batchdelay.add(part2);
     return batchdelay;
    }
}