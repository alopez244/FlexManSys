package es.ehu.platform.test;

import es.ehu.domain.manufacturing.utilities.StructMessage;
import es.ehu.domain.manufacturing.utilities.StructMessageTest;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.gateway.GatewayAgent;

import java.util.HashMap;
import java.util.Map;

public class DataAcq_GWAgent extends GatewayAgent {

    public HashMap<String,HashMap<String,HashMap<String,String>>> times = new HashMap<>();
    public HashMap<String,HashMap<String,HashMap<String,String>>> apptimes = new HashMap<>();
    public HashMap<String,HashMap<String,HashMap<String,String>>> errtimes= new HashMap<>();
    public HashMap<String,HashMap<String,HashMap<String,String>>> negtimes= new HashMap<>();

    protected void processCommand(java.lang.Object command) { //The method is called each time a request to process a command is received from the JSP Gateway. receive strmessage


        //ROSJADEgw gw =new ROSJADEgw(this);
        System.out.println("-->Gateway processes execute");
        if (!(command instanceof StructMessage)) {
            System.out.println("---Error, unexpected type");
            releaseCommand(command);
        }
        StructMessageTest msgStruct = (StructMessageTest) command;
        String action = msgStruct.readAction();
        if (action.equals("print")) {     // JadeGateway.execute command was called for new message reading (Agent -> PLC)
            System.out.println("---GW, print function");
            for (Map.Entry<String,HashMap<String,HashMap<String,String>>> parent : times.entrySet()){
                System.out.println(parent.getKey());
                for (Map.Entry<String, HashMap<String, String>> agent : parent.getValue().entrySet()){ //lanza nullpointer
                    System.out.println(agent.getKey());
                    for (Map.Entry<String,String> tiempos : agent.getValue().entrySet() ) {
                        System.out.println("  " + tiempos.getKey() + " "+tiempos.getValue());
                    }
                }
            }

            ((StructMessageTest) command).setTestResults(times);  //message is saved in StructMessage data structure, then ExternalJADEgw class will read it from there
            ((StructMessageTest) command).setTestResultsApp(apptimes);  //message is saved in StructMessage data structure, then ExternalJADEgw class will read it from there
            ((StructMessageTest) command).setTestResultsErr(errtimes);
            ((StructMessageTest) command).setTestResultsNeg(negtimes);
            ((StructMessageTest) command).setNewData(true);

        } else if (action.equals("init")) {
            System.out.println("---GW, init function");
            System.out.println("---Hello, I am a Gateway Agent");
        }else{ //Check if any msg is received
            System.out.println("---Gateway error function");
        }

        System.out.println("<--Gateway processes execute");
        releaseCommand(command);
    }


    protected void setup(){ //agent already registered and is able to send and receive messages. Necessary to add behaviour in order to to anything.

        System.out.println("En GWAgentRos");
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("timestamp"));
        MessageTemplate template_err = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("timestamp_err"));

        MessageTemplate template_neg = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("timestamp_neg"));

        // MENSAJE DESDE TRANSPORT AGENT

        addBehaviour(new CyclicBehaviour() { //keep executing constantly

            public void action() {

                //System.out.println("Entering CyclicBehaviour");
                ACLMessage msg = receive(template); //recivir mensaje desde Transport Agent
                ACLMessage msg_err=receive(template_err);
                ACLMessage msg_neg=receive(template_neg);
                if (msg != null) {
                    System.out.println("GWagent, message received");

                    //Si hay contenido en el mensaje lo separo en campos
                    String data = msg.getContent();
                    String[] dataArray = data.split(",");

                    //Compruebo si el mensaje es para el HashMap de aplicaciones o para el de componentes
                    if (dataArray[0].contains("app")){

                        //Guardo la información en el HashMap de aplicaciones
                        //Se comprueba si ya hay en el HashMap información sobre esta aplicación
//                        if (!apptimes.containsKey(dataArray[0])){
//
//                            //Si el HashMap no tiene ninguna clave con el nombre del componente, es el primer mensaje que recibo sobre él.
//                            //Por tanto, primero creo la clave, y luego añado los datos
//                            apptimes.put(dataArray[0],new HashMap<>());
//                            apptimes.get(dataArray[0]).put(dataArray[1],dataArray[2]);
//                        } else {
//
//                            //Si ya tengo información sobre este componente, no tengo que crear una nueva entrada en el HashMap, solo añadir información.
//                            apptimes.get(dataArray[0]).put(dataArray[1],dataArray[2]);
//                        }

                        apptimes=time_contructor(dataArray, apptimes);
                    } else {

                        //Guardo la información en el HashMap de componentes
                        //Se comprueba si ya hay en el HashMap información sobre este componente
                        times=time_contructor(dataArray, times);
                    }
                } else if(msg_err!=null) {
                    String data = msg_err.getContent();
                    String[] dataArray = data.split(",");
                   errtimes =time_contructor(dataArray, errtimes);

                }else if(msg_neg!=null) {
                    String data = msg_neg.getContent();
                    String[] dataArray = data.split(",");
                    negtimes =time_contructor(dataArray, negtimes);

                }else{
                    //System.out.println("Block the agent");
                    block();
                }
            }
        });
        super.setup();
    }

    private HashMap<String, HashMap<String, HashMap<String, String>>> time_contructor(String[] dataArray, HashMap<String, HashMap<String, HashMap<String, String>>> t) {
        if (!t.containsKey(dataArray[0])){
            //Si el HashMap no tiene ninguna clave con el nombre del componente, es el primer mensaje que recibo sobre él.
            //Por tanto, primero creo la clave, y luego añado los datos
            t.put(dataArray[0],new HashMap<>());
            t.get(dataArray[0]).put(dataArray[1],new HashMap<>()); //si no tenia parent no va a tener agent
            //                            times.get(dataArray[0]).put(dataArray[1],dataArray[2]);
        } else {
            if (!t.get(dataArray[0]).containsKey(dataArray[1])){ //puede tener el parent pero no el agent
                t.get(dataArray[0]).put(dataArray[1],new HashMap<>());
            }
        }
        t.get(dataArray[0]).get(dataArray[1]).put(dataArray[2],dataArray[3]);
        return t;
    }
}
