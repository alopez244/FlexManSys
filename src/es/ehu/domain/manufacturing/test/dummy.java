package es.ehu.domain.manufacturing.test;
import es.ehu.domain.manufacturing.utilities.DiagnosisAndDecision;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.sql.SQLOutput;
import java.util.ArrayList;

public class dummy extends Agent{
    protected Agent myAgent=this;
    MessageTemplate template = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchOntology("pong")),MessageTemplate.MatchPerformative(7));
    public void setup(){
        addBehaviour(new dummy.do_heavy_stuff() );

    }

    class do_heavy_stuff extends OneShotBehaviour {
        ArrayList<String> results=new ArrayList<String>();
       int max=3000000;
       double res=0;
       double num=523456.54544;
       double den=254315.14544;
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
        }
    }

    private void sendACL(int performative,String receiver,String ontology,String content){ //Funcion estándar de envío de mensajes
        AID receiverAID=new AID(receiver,false); //pasamos la máquina a estado idle
        ACLMessage msg=new ACLMessage(performative);
        msg.addReceiver(receiverAID);
        msg.setOntology(ontology);
        msg.setContent(content);
        send(msg);
    }
    private int SearchAgent (String agent){
        int found=0;

        AMSAgentDescription[] agents = null;

        try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults ( new Long(-1) );
            agents = AMSService.search(myAgent, new AMSAgentDescription (), c );
        }
        catch (Exception e) {
            System.out.println(e);
        }
        for (int i=0; i<agents.length;i++){
            AID agentID = agents[i].getName();
            String agent_to_check=agentID.getLocalName();
//            System.out.println(agent_to_check);
            if(agent_to_check.contains(agent)){
                found++;
            }
        }
        return found;
    }
}
