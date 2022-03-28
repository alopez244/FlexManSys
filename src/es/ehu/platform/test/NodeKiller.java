package es.ehu.platform.test;

import es.ehu.domain.manufacturing.utilities.ErrorHandlerAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Scanner;

public class NodeKiller extends ErrorHandlerAgent {
    protected void setup(){
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                String cmd = "";
            while(true){
                ACLMessage batch_machine=blockingReceive();
                System.out.println(batch_machine.getOntology());
                if(batch_machine.getOntology().equals("machine1")){ //elimina el batch en running asignado a la mquina que primero arrancados

                    ACLMessage reply = null;
                    try {
                        Thread.sleep(20000);
                        reply = sendCommand(myAgent, "get * category=batch reference=" + batch_machine.getContent(), "");
                        ACLMessage reply2 = sendCommand(myAgent, "get * category=batchAgent parent=" + reply.getContent() + " state=running", "");
                        ACLMessage reply3 = sendCommand(myAgent, "get "+reply2.getContent()+ " attrib=node", "");
                        String pn="pnodeagent"+reply3.getContent();
                        System.out.println(pn);
                        sendACL(ACLMessage.REQUEST,pn,"node_kill","",myAgent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    }
            }


//                while(!cmd.equals("exit")){
//                    Scanner in = new Scanner(System.in);
//                    System.out.print("Enter a valid node (number): ");
//                    cmd = in.nextLine();
//                    System.out.println();
//                    String pn="pnodeagent"+cmd;
//                    int found =SearchAgent(pn, myAgent);
//                    if(found==1){
//                        sendACL(ACLMessage.REQUEST,pn,"node_kill","",myAgent);
//                        System.out.println("Node "+pn+" killed");
//                    }else {
//                        System.out.println(pn+" is not a valid node to kill");
//                    }
//                }
            }
        });
    }
}

