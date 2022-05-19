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
                ACLMessage agent_to_kill=blockingReceive();
                System.out.println(agent_to_kill.getContent());
                if(agent_to_kill.getOntology().equals("killpls")){

                    ACLMessage reply = null;
                    try {
                        Thread.sleep(28000);

                        sendACL(ACLMessage.REQUEST,agent_to_kill.getContent(),"kill","",myAgent);

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

