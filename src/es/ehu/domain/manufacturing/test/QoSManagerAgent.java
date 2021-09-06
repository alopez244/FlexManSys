package es.ehu.domain.manufacturing.test;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.StringACLCodec;
import jade.lang.acl.MessageTemplate;
import java.io.StringReader;
import java.util.ArrayList;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.AMSService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class QoSManagerAgent extends Agent {
    private Agent myAgent=this;
    private int agent_found_qty=0;
    private int gateway_found_qty=0;
    private ArrayList<String> ActualBatch = new ArrayList<String>();
    private ArrayList<ArrayList<String>> ErrorList=new ArrayList<ArrayList<String>>();
    private int i=0;
    private ArrayList<ArrayList<String>> allDelays = new ArrayList<ArrayList<String>>();
    private MessageTemplate delaytemplate;
    private int j=0;
    private int l=0;
    private ArrayList<ArrayList<String>> batch_and_machine = new ArrayList<ArrayList<String>>();
    private ArrayList<String> delay_asking_queue=new ArrayList<String>();
    private ACLMessage machinenbr=null;
    static final Logger LOGGER = LogManager.getLogger(QoSManagerAgent.class.getName());

    protected void setup() {
        delaytemplate= MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchOntology("delay"));
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {

                ACLMessage msg = blockingReceive();

                if(msg!=null){

                    if(msg.getPerformative()==ACLMessage.FAILURE){ //Se recibe un posible error
                        if(msg.getOntology().equals("timeout")){ //Se recibe aviso de que ha habido un timeout

                            String[] parts=msg.getContent().split("/");
                            String timeout_batch_id=parts[0];
                            String timeout_item_id=parts[1];

                            for(int m=0; m<ErrorList.size();m++) { //Primero comprueba que el error no esté repetido
                                if (ErrorList.get(m).get(0).equals("timeout")) {
                                    if (ErrorList.get(m).get(1).equals(timeout_batch_id)) {
                                        if(ErrorList.get(m).get(2).equals(timeout_item_id)){
                                        System.out.println("Timeout repeated on same batch and item");
                                        }
                                    }
                                }
                            }

                            ErrorList.add(l,new ArrayList<>());  //Añade al listado de errores el timeout
                            ErrorList.get(l).add(0,"timeout");

                            System.out.println(timeout_batch_id +" batch has thrown a timeout on item "+timeout_item_id+" Checking failure...");
                            if(batch_and_machine.size()>0){ //buscamos el batch en el listado y conseguimos el ID del machine agent responsable
                                for(int k=0;k<batch_and_machine.size();k++){
                                    if(batch_and_machine.get(k).get(0).equals(timeout_batch_id)){

                                        String MA=batch_and_machine.get(k).get(1);
                                        agent_found_qty=SearchAgent(MA); //La funcion search agent devuelve la cantidad de coincidencias con el nombre que le pasemos
                                        ErrorList.get(l).add(1,timeout_batch_id);
                                        ErrorList.get(l).add(2,timeout_item_id);
                                        if(agent_found_qty>0){  //Si es mayor de 0, el agente maquina vive
                                            System.out.println("The machine agent "+MA+ " which was executing batch number "+timeout_batch_id+" is alive");
                                            ErrorList.get(l).add(3,MA+"->OK");

                                        }else{
                                            System.out.println("The machine agent "+MA+ " which was executing batch number "+timeout_batch_id+" is not alive"); //Si no hay coincidencias el agente máquina no esta activo
                                            ErrorList.get(l).add(3,MA+"->NO OK");
                                        }
                                        try {
                                            machinenbr=sendCommand(myAgent,"get "+MA+" attrib=id","name"); //consultamos la id de la estacion al SA para saber que gateway agent le corresponde. Si es 41 seria ControlGatewayCont4.

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        char[] ch= new char[machinenbr.getContent().length()];
                                        ch[0] = machinenbr.getContent().charAt(0);
//                                System.out.println(ch[0]);
                                        gateway_found_qty=SearchAgent("ControlGatewayCont"+ch[0]); //Buscamos a su vez que el agente gateway este vivo

                                        if(gateway_found_qty>0){  //Si es mayor de 0, el agente gateway vive
                                            System.out.println("Gateway agent of "+MA+" found alive: "+ "ControlGatewayCont"+ch[0]);
                                            ErrorList.get(l).add(4,"ControlGatewayCont"+ch[0]+"->OK");
                                        }else{
                                            System.out.println("No gateway agent found for "+MA+": ControlGatewayCont"+ch[0]);
                                            ErrorList.get(l).add(4,"ControlGatewayCont"+ch[0]+"->NO OK");
                                        }
                                        /**
                                         * Situaciones tras timeout:
                                         *
                                         * -> Si machine agent OK y control gateway OK, posible fallo fisico de máquina, atasco, etc. Añadimos tiempo al timeout¿?
                                         *
                                         * -> Si machine agent NO OK y control gateway OK, posible fallo en software de machine agent, la máquina seguirá trabajando y posiblemente complete el batch
                                         *    pero no tendremos trazabilidad
                                         *
                                         * -> Si machine agent OK y control gateway NO OK, posible fallo de red entre asset y machine agent, es posible que la máquina siga trabajando y complete el batch
                                         *    pero no tendremos trazabilidad. Es posible también que la máquina se haya caído.
                                         *
                                         * -> Si machine agent NO OK y control gateway NO OK, fallo de red o de software, o el timeout no tenia la info correcta.
                                         *
                                         * **/

                                    }
                                }
                            }else{
                                System.out.println("No data available to check failure");
                                ErrorList.get(l).add(1,timeout_batch_id);
                                ErrorList.get(l).add(2,"machine agent unknown");
                                ErrorList.get(l).add(3,"gateway agent unknown");
                            }
                            l++;
                        }
                    }
                    if(msg.getPerformative()==ACLMessage.INFORM) { //Se recibe algun tipo de info
                        if (msg.getOntology().equals("delay")) {

                            System.out.println(msg.getContent());
                            ActualBatch = getDelays(msg.getContent()); //Añade el batch especificado con su correspondiente delay a la lista de delays

                            for (int l = 0; l < delay_asking_queue.size(); l++) {
                                if (ActualBatch.get(0).equals(delay_asking_queue.get(l))) {
                                    ACLMessage msgtobatch = new ACLMessage(ACLMessage.INFORM);
                                    AID batchID = new AID(delay_asking_queue.get(l + 1), false);
                                    msgtobatch.addReceiver(batchID);
                                    msgtobatch.setContent(ActualBatch.get(1));
                                    msgtobatch.setOntology("askdelay");
                                    send(msgtobatch);
                                    delay_asking_queue.remove(l + 1);
                                    delay_asking_queue.remove(l);
                                    System.out.println("DELAY SENDED");

                                }
                            }
                            allDelays.add(i, ActualBatch);
                            i++;

                            String sender = msg.getSender().getLocalName();
                            ArrayList<String> temp = BatchAndMachines(msg.getContent(), sender); //Crea un listado de agentes maquina con los batch que tengan asignados
                            batch_and_machine.add(j, temp);
                            j++;
                        }
                    }
                    if(msg.getPerformative()==ACLMessage.REQUEST) { //Se recibe algun tipo de petición
                        if (msg.getOntology().equals("askdelay")) { //Si un timeout le consulta el delay

//                        System.out.println("DELAY ASKED. BEFORE OR AFTER HAVING THE DATA?"); //usar para debug

                            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                            String asking_batch = msg.getContent();
                            reply.setContent("0"); //si no encuentra ningun batch que coincida en la lista devuelve un 0
                            boolean flag = true;
                            if (allDelays.size() != 0) {
                                for (int k = 0; k < allDelays.size() || flag; k++) {

                                    if (allDelays.get(k).get(0).equals(asking_batch)) {
                                        reply.setContent(allDelays.get(k).get(1));
                                        flag = false;
                                    }
                                }

                            }
                            if (flag) {
                                System.out.println("Batch " + asking_batch + " has no delays registered yet. Adding to queue.");
                                delay_asking_queue.add(asking_batch);
                                delay_asking_queue.add(msg.getSender().getLocalName());

                            } else {
                                flag = true;
                                reply.addReceiver(msg.getSender());
                                reply.setOntology(msg.getOntology());
                                send(reply);
                                System.out.println("DELAY SENDED"); //usar para debug
                            }
                        }
                    }

                }

            }
        });
    }

    private ArrayList<String> getDelays(String data){

     ArrayList<String> batchdelay= new ArrayList<String>();//Creamos un Arraylist para los delay de cada batch.
     String[] parts = data.split("/");
     String part1 = parts[0]; // BatchID
     String part2 = parts[1]; // Delay in minutes
     batchdelay.add(part1);
     batchdelay.add(part2);

     return batchdelay;
    }

   private ArrayList<String> BatchAndMachines(String data, String sender){
    ArrayList<String> MachineAgentList= new ArrayList<String>(); //Creamos un Arraylist para los machine agent que ejecutan cada batch.
    String[] parts=data.split("/");
    String batch = parts[0]; // BatchID
    MachineAgentList.add(batch);
    MachineAgentList.add(sender);


    return MachineAgentList;
    }

    private int SearchAgent (String agent){
        int found=0;
        Agent myAgent=this;
        AMSAgentDescription [] agents = null;

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

private ACLMessage sendCommand(Agent agent, String cmd, String conversationId) throws Exception {

    this.myAgent = agent;

    DFAgentDescription dfd = new DFAgentDescription();
    ServiceDescription sd = new ServiceDescription();

    sd.setType("sa");
    dfd.addServices(sd);
    String mwm;

    while (true) {
        DFAgentDescription[] result = DFService.search(myAgent, dfd);

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
    myAgent.send(msg);
    ACLMessage reply = myAgent.blockingReceive(
            MessageTemplate.and(
                    MessageTemplate.MatchInReplyTo(msg.getReplyWith()),
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM))
            , 1000);

    return LOGGER.exit(reply);

}


}