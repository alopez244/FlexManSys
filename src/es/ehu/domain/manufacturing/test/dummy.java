package es.ehu.domain.manufacturing.test;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class dummy extends Agent{
    protected Agent myAgent=this;
    MessageTemplate template = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchOntology("pong")),MessageTemplate.MatchPerformative(7));
    public void setup(){

//        ACLMessage id=blockingReceive();
//        System.out.println(id.getContent());
        AID receiverAID=new AID("dummy2",false);
        ACLMessage id = null;
        while(true){

            ACLMessage reply=new ACLMessage(16);
            reply.setContent("ping");
            reply.setOntology("ping");


                reply.addReceiver(receiverAID);

            send(reply);
            id=blockingReceive(template);
//            System.out.println(id.getContent());

//            ACLMessage msg=blockingReceive();
//            if(msg!=null) {
                System.out.println(id.getContent());
//            }else{
//                System.out.println("not recieved");
//            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


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
