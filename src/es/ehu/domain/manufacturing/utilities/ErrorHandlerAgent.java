package es.ehu.domain.manufacturing.utilities;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ErrorHandlerAgent extends Agent{
    private Agent myAgent;

    static final Logger LOGGER = LogManager.getLogger(ErrorHandlerAgent.class.getName());
    public ErrorHandlerAgent() {
    }

    protected void setup() {
        LOGGER.entry();
        LOGGER.info("New error handler agent started");

        addBehaviour(new ErrorHandlerAgent.EHAgent() );
        LOGGER.exit();
    }

    public class EHAgent extends CyclicBehaviour {
        @Override
        public void action() {

        }
    }

    public ACLMessage sendCommand(Agent agent, String cmd, String conversationId) throws Exception {

//        this.myAgent = agent;

            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();

            sd.setType("sa");
            dfd.addServices(sd);
            String mwm;

            while (true) {
                DFAgentDescription[] result = DFService.search(agent, dfd);

                if ((result != null) && (result.length > 0)) {
                    dfd = result[0];
                    mwm = dfd.getName().getLocalName();
                    break;
                }
                LOGGER.info(".");
                Thread.sleep(100);

            } //end while (true)

            LOGGER.entry(mwm, cmd);
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID(mwm, AID.ISLOCALNAME));
            msg.setConversationId(conversationId);
            msg.setOntology("control");
            msg.setContent(cmd);
            msg.setReplyWith(cmd);
            send(msg);
            ACLMessage reply = agent.blockingReceive(
                    MessageTemplate.and(
                            MessageTemplate.MatchInReplyTo(msg.getReplyWith()),
                            MessageTemplate.MatchPerformative(ACLMessage.INFORM))
                    , 2000);

            return LOGGER.exit(reply);
        }

        public int SearchAgent (String name,Agent agent){  //busqueda de ams por nombre de agente
            int found=0;
            AMSAgentDescription[] agents = null;

            try {
                SearchConstraints c = new SearchConstraints();
                c.setMaxResults ( new Long(-1) );
                agents = AMSService.search(agent, new AMSAgentDescription (), c );
            }
            catch (Exception e) {
                System.out.println(e);
            }
            for (int i=0; i<agents.length;i++){
                AID agentID = agents[i].getName();
                String agent_to_check=agentID.getLocalName();
//            System.out.println(agent_to_check);
                if(agent_to_check.contains(name)){
                    found++;
                }
            }
            return found;
        }

        public boolean PingAgent (String name,Agent agent){  //checkea el estado de los agentes de aplicación, recurso y gateway
//        this.myAgent = agent;
            MessageTemplate pingtemplate=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchOntology("ping"));
            boolean state;
            int n=SearchAgent(name,agent); //primero se observa cuantos agentes ve el ams con en nombre proporcionado
            if(n>0){
//            AID Agent_to_ping_ID=new AID(name,false);
//            ACLMessage ping=new ACLMessage(ACLMessage.REQUEST);
//            ping.setOntology("ping");
//            ping.addReceiver(Agent_to_ping_ID);
//            ping.setContent("");
//            myAgent.send(ping);
                sendACL(16,name,"ping","",agent);
                ACLMessage echo=agent.blockingReceive(pingtemplate,500);
                if(echo!=null) {
                    LOGGER.info(name+" answered on time.");
                    state=true;
                }else{
                    LOGGER.error(name+" did not answer on time. Confirming failure.");
                    state=false;
                }
            }else{
                state=false;
            }
            return state;
        }
        public void sendACL(int performative,String receiver,String ontology,String content,Agent agent){ //Funcion estándar de envío de mensajes
//        this.myAgent = agent;
            AID receiverAID=new AID(receiver,false); //pasamos la máquina a estado idle
            ACLMessage msg=new ACLMessage(performative);
            msg.addReceiver(receiverAID);
            msg.setOntology(ontology);
            msg.setContent(content);
            send(msg);
        }



    }


