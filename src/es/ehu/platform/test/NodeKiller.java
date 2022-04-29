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
                        Thread.sleep(20000); // por ajustar

                        sendACL(ACLMessage.REQUEST,"machine1","node_kill","",myAgent);

//                        reply = sendCommand(myAgent, "get * category=batch reference=" + batch_machine.getContent(), "");
//                        ACLMessage reply2 = sendCommand(myAgent, "get * category=batchAgent parent=" + reply.getContent() + " state=running", "");
//                        ACLMessage reply3 = sendCommand(myAgent, "get "+reply2.getContent()+ " attrib=node", "");
//                        String pn="pnodeagent"+reply3.getContent();
//                        System.out.println(pn);
//                        sendACL(ACLMessage.REQUEST,pn,"node_kill","",myAgent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    }
            }

            }
        });
    }
}

