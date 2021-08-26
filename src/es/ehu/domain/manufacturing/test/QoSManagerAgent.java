package es.ehu.domain.manufacturing.test;

import es.ehu.domain.manufacturing.agents.functionality.Machine_Functionality;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.StringACLCodec;
import jade.lang.acl.MessageTemplate;
import java.io.StringReader;
import java.util.ArrayList;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.AMSService;


public class QoSManagerAgent extends Agent {
    private int agent_found_qty=0;
    private int gateway_found_qty=0;
    private String mensaje;
    private ArrayList<String> ActualBatch = new ArrayList<String>();
    private ArrayList<ArrayList<String>> ErrorList=new ArrayList<ArrayList<String>>();
    private int i=0;
    private ArrayList<ArrayList<String>> allDelays = new ArrayList<ArrayList<String>>();
    private MessageTemplate delaytemplate;
    private int j=0;
    private int k=0;
    private ArrayList<ArrayList<String>> batch_and_machine = new ArrayList<ArrayList<String>>();

    protected void setup() {
        delaytemplate= MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchOntology("delay"));
           Agent myAgent=this;
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {

                ACLMessage msg = blockingReceive();

                if(msg!=null){

                    if(msg.getOntology().equals("delay")){

                        System.out.println(msg.getContent());
                        ActualBatch= getDelays(msg.getContent()); //Añade el batch especificado con su correspondiente delay a la lista de delays
                        allDelays.add(i,ActualBatch);
                        i++;

                        String sender=msg.getSender().getLocalName();
                        ArrayList<String> temp = BatchAndMachines(msg.getContent(), sender); //Crea un listado de agentes maquina con los batch que tengan asignados
                        batch_and_machine.add(j,temp);
                        j++;
                    }
                    else if(msg.getOntology().equals("askdelay")){ //Si le consultan el delay

                        System.out.println("DELAY ASKED. BEFORE OR AFTER HAVING THE DATA?"); //usar para debug

                        ACLMessage reply=new ACLMessage(ACLMessage.INFORM);
                        String asking_batch=msg.getContent();
                        reply.setContent("0"); //si no encuentra singun batch que coincida en la lista devuelve un 0
                        boolean flag=true;
                        if(allDelays.size()!=0) {
                            for (int i = 0; i < allDelays.size() || flag == true; i++) {

                                if (allDelays.get(i).get(0).equals(asking_batch)) {
                                    reply.setContent(allDelays.get(i).get(1));
                                    flag = false;
                                }

                            }
                            if(flag){
                                System.out.println("Batch "+asking_batch+" has no delays registered");
                            }
                        }
                        flag=true;
                        reply.addReceiver(msg.getSender());
                        reply.setOntology(msg.getOntology());
                        send(reply);
                        System.out.println("DELAY SENDED"); //usar para debug
                    }
                    else if(msg.getOntology().equals("timeout")){ //Se recibe aviso de que ha habido un timeout

                        String timeout_batch_id=msg.getContent();
                        System.out.println(timeout_batch_id +" batch has thrown a timeout. Checking failure");
                        if(batch_and_machine.size()>0){ //buscamos el batch en el listado y conseguimos el ID del machine agent responsable
                         for(int k=0;k<batch_and_machine.size();k++){
                            if(batch_and_machine.get(k).get(0).equals(msg.getContent())){

                                String MA=batch_and_machine.get(k).get(1);
                                agent_found_qty=SearchAgent(MA); //La funcion search agent devuelve la cantidad de coincidencias con el nombre que le pasemos

                                if(agent_found_qty>0){  //Si es mayor de 0, el agente maquina vive
                                    System.out.println("The machine agent "+MA+ " which was executing batch number "+msg.getContent()+" is alive");

                                    String[] parts=MA.split("machine");
                                    String machinenumber = parts[1];
                                    gateway_found_qty=SearchAgent("ControlGatewayCont"+machinenumber); //Buscamos a su vez que el agente gateway este vivo
                                    if(gateway_found_qty>0){  //Si es mayor de 0, el agente gateway vive
                                        System.out.println("Gateway agent for "+MA+" found alive");
                                    }else{
                                       System.out.println("No gateway agent found for "+MA);
                                    }

                                }else{
                                    System.out.println("The machine agent "+MA+ " which was executing batch number "+msg.getContent()+" is not alive"); //Si no hay coincidencias el agente máquina no esta activo
                                }
                                System.out.println(" ");

                            }
                         }
                        }else{
                            System.out.println("No data available to check failure");
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


}