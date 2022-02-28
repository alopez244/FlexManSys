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

                while(!cmd.equals("exit")){
                    Scanner in = new Scanner(System.in);
                    System.out.print("Enter a valid node (number): ");
                    cmd = in.nextLine();
                    System.out.println();
                    String pn="pnodeagent"+cmd;
                    int found =SearchAgent(pn, myAgent);
                    if(found==1){
                        sendACL(ACLMessage.REQUEST,pn,"node_kill","",myAgent);
                        System.out.println("Node "+pn+" killed");
                    }else {
                        System.out.println(pn+" is not a valid node to kill");
                    }
                }
            }
        });
    }
}

