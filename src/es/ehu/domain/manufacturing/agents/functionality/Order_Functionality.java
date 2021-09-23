package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.domain.manufacturing.test.QoSManagerAgent;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.template.interfaces.AvailabilityFunctionality;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.IExecManagement;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.commons.net.nntp.NewGroupsOrNewsQuery;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

public class Order_Functionality extends DomApp_Functionality implements BasicFunctionality, AvailabilityFunctionality {

    private static final long serialVersionUID = 1L;
    private Agent myAgent;

    private List<String> elementsToCreate = new ArrayList<>();
    private int chatID = 0; // Numero incremental para crear conversationID

    private String firstState;
    private String redundancy;
    private String parentAgentID, orderNumber;
    private ArrayList<String> myReplicasID = new ArrayList<>();
    private ArrayList<AID> sonAgentID = new ArrayList<>(); // lista con los nombres de los agentes de los que es padre
    private Integer batchIndex = 1;
    private Boolean newBatch = true, firstTime = true;
    private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> batchTraceability = new ArrayList<>();
    private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> deserializedMessage = new ArrayList<>(); // Mensaje recibido desde el batchAgent deserializado
    private String mySeType;
    private MessageTemplate template, template2,templateFT,template3,template4;
    private volatile String orderreference=null;
    private volatile String raw_ft=null;
    private volatile AID QoSID = new AID("QoSManagerAgent", false);
    private volatile ArrayList<String> online_batches=new ArrayList<String>();
    private volatile String batch_to_take_down="";
    private volatile Date new_expected_finish_time=null;
    private volatile boolean QoSresponse_flag=false;


    private ArrayList<ArrayList<String>> batch_last_items_ft=new ArrayList<ArrayList<String>>();


    class Ordertimeout extends Thread{

        private boolean exit_timeout=false,QoSreported=false;
        private Date expected_finish_time;
        private String batch;

        public Ordertimeout(Date expected_finish_time, String batch){
            this.expected_finish_time=expected_finish_time;
            this.batch=batch;
        }

        public void run() {

            while (!exit_timeout) {
                while (expected_finish_time.after(getactualtime()) && !exit_timeout) {

                    if (batch_to_take_down.equals(batch)) {
                        exit_timeout = true;
                    }
                    if (new_expected_finish_time != null) {
                        expected_finish_time = new_expected_finish_time;
                        System.out.println("Batch " + batch + " finish time updated to: " + expected_finish_time);
                        new_expected_finish_time = null;
                    }
                }
                if (!expected_finish_time.after(getactualtime()) && !exit_timeout&&!QoSreported) {
                    System.out.println("Order has thrown a timeout on batch " + batch + ". Checking with QoS.");
                    sendACLMessage(ACLMessage.FAILURE, QoSID, "timeout", "order_timeout", batch.substring(0, 2) + "/" + batch, myAgent); //avisa al QoS de fallo por timeout
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (!QoSresponse_flag) {
                        System.out.println("I'm probably isolated. Shutting down entire node");
                        System.exit(0);
                    } else {
                        QoSresponse_flag = false;
                    }
                    QoSreported=true;
                }
                if (batch_to_take_down.equals(batch)) {
                    exit_timeout = true;
                }
            }
            batch_to_take_down = "";
            System.out.println("Batch "+batch+" timeout finished.");
        }
    }


    @Override
    public Object getState() {
        return null;
    }

    @Override
    public void setState(Object state) {

    }

    @Override
    public Void init(MWAgent myAgent) {

        this.template = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("Information")),MessageTemplate.MatchConversationId("ItemsInfo"));
        this.template2 = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId("Shutdown"));
        this.template3 = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("timeout_confirmed"));
        this.template4 = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("update_timeout"));
        this.myAgent = myAgent;

        // Crear un nuevo conversationID
        String conversationId = myAgent.getLocalName() + "_" + chatID++;

        firstState = getArgumentOfAgent(myAgent, "firstState");
        redundancy = getArgumentOfAgent(myAgent, "redundancy");
        parentAgentID = getArgumentOfAgent(myAgent, "parentAgent");
        mySeType = getMySeType(myAgent, conversationId);

        if (firstState.equals("running")) {

            // Cambiar a estado bootToRunning para que los tracking le puedan enviar mensajes
            String query = "set " + myAgent.getLocalName() + " state=bootToRunning";
            try {
                sendCommand(myAgent, query, conversationId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Hashtable<String, String> attributes = new Hashtable<String, String>();
            attributes.put("seClass", "es.ehu.domain.manufacturing.agents.BatchAgent");

            seStart(myAgent.getLocalName(), attributes, conversationId);

            // TODO primero comprobara que todas las replicas (tracking) se han creado correctamente, y despues comprobara los batches
            // Es decir, antes de avisar a su padre que esta creado, comprueba las replicas y despues los batches
            // Le añadimos un comportamiento para que consiga todos los mensajes que le van a enviar los batch cuando se arranquen correctamente

            Object[] result = processACLMessages(myAgent, mySeType, elementsToCreate, conversationId, redundancy, parentAgentID);
            sonAgentID = (ArrayList<AID>) result[1];
            myReplicasID = (ArrayList<String>) result[0];

            templateFT=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchOntology("Ftime_order_ask"));
            try {
                ACLMessage orderName = sendCommand(myAgent, "get " + myAgent.getLocalName() + " attrib=parent", "name"); //consigue el nombre del order
                ACLMessage reference = sendCommand(myAgent, "get " + orderName.getContent() + " attrib=reference", "Reference"); //consigue la referencia del order
                AID plannerID = new AID("planner", false);
                orderreference=reference.getContent();
                sendACLMessage(16, plannerID,"Ftime_order_ask", "finnish_time", reference.getContent(), myAgent ); //pide el finish time de cada item al planner
                ACLMessage finishtime= myAgent.blockingReceive(templateFT); //recibe los finish times concatenados
                System.out.println(finishtime.getContent());
                raw_ft=finishtime.getContent();
                batch_last_items_ft=batch_finish_times(raw_ft);
                batch_last_items_ft=add_delays(batch_last_items_ft); //se añade delay
                for(int k=0;k<batch_last_items_ft.size();k++){

                    System.out.println(myAgent.getLocalName()+ " is starting a timeout thread for batch "+batch_last_items_ft.get(k).get(0));
                    Date expected_FT=null;
                    try {
                        expected_FT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(batch_last_items_ft.get(k).get(1));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long startime = getactualtime().getTime() - (Long.parseLong(batch_last_items_ft.get(k).get(2)));
                    LocalDateTime new_expected_finish_time = convertToLocalDateTimeViaSqlTimestamp(getactualtime());
                    new_expected_finish_time = new_expected_finish_time.plusSeconds(((expected_FT.getTime() - startime) / 1000));
                    expected_FT = convertToDateViaSqlTimestamp(new_expected_finish_time);

                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    String strDate = dateFormat.format( expected_FT);

                    batch_last_items_ft.get(k).add(strDate); //expected finish date con delay

                    System.out.println(batch_last_items_ft.get(k).get(0)+" batch expected finish time: "+expected_FT);
                    Ordertimeout t=new Ordertimeout(expected_FT,batch_last_items_ft.get(k).get(0));
                    t.start();
                }

            }catch(Exception e) {
                System.out.println(myAgent.getLocalName()+ " ERROR. Something happened at some point during initialization");
                e.printStackTrace();
            }

        } else {
            // Si su estado es tracking
            trackingOnBoot(myAgent, mySeType, conversationId);

            myAgent.initTransition = ControlBehaviour.TRACKING;
        }

        return null;
    }

    // TODO quitamos el atributo attribs y lo creamos dentro del metodo seStart ???
    public String seStart(String seID, Hashtable<String, String> attribs, String conversationId) {

        ArrayList<String> creationCategories = new ArrayList<>();
        creationCategories.add("batch");  // Aqui decidiremos que tipos de elementos queremos crear --> Order, Batch, las dos...

        List result = seStart(myAgent, seID, attribs, conversationId, creationCategories, chatID, redundancy);
        elementsToCreate = (List<String>) result.get(0);
        chatID = (int) result.get(1);

        return null;
    }

    public String seStop(String... seID) {
        return null;
    }

    @Override
    public Object execute(Object[] input) {

        System.out.println("El agente " + myAgent.getLocalName() + " esta en el metodo execute de su estado running");

        ACLMessage msg = myAgent.receive(template);
        if (msg != null) {
            if (firstTime) {
                deserializedMessage = deserializeMsg(msg.getContent());
                batchTraceability = addNewLevel(batchTraceability, deserializedMessage, true); //añade el espacio para la informacion de la orden en primera posicion, sumando un nivel mas a los datos anteriores
                batchTraceability.get(0).get(0).get(0).add("OrderLevel"); // en ese espacio creado, se añade la informacion del order
                batchTraceability.get(0).get(0).get(2).add("orderReference");
                String batchNumber = batchTraceability.get(1).get(0).get(3).get(0);
                while(!batch_to_take_down.equals("")){}
                batch_to_take_down=batchNumber;
                orderNumber = batchNumber.substring(0,2);
                batchTraceability.get(0).get(0).get(3).add(orderNumber);
                firstTime = false;
            } else {
                if (newBatch == false) {
                    for (int i = batchTraceability.size() - 1; i >= batchIndex; i--) {
                        batchTraceability.remove(i); //se elimina el ultimo batch añadido para poder sobreescribirlo
                    }
                }
                deserializedMessage = deserializeMsg(msg.getContent());
                batchTraceability = addNewLevel(batchTraceability, deserializedMessage,false);
            }
            newBatch = false; // hasta que el batch añadido no se complete, cada vez que se reciba un mensaje el dato se sobrescribira
        }
        ACLMessage msg2 = myAgent.receive(template2);
        // Recepcion de mensajes para eliminar de la lista de agentes hijo los agentes batch que ya han enviado toda la informacion
        if (msg2 != null) {
            AID sender = msg2.getSender();
            if (msg2.getContent().equals("Batch completed")){
                String msgSender = msg2.getOntology();
                for (int i = 0; i < sonAgentID.size(); i++) {
                    if (sonAgentID.get(i).getName().split("@")[0].equals(msgSender)) {
                        sonAgentID.remove(i);
                    }
                }
                String aux = "";
                String msgToMPLan = "";
                for (ArrayList<ArrayList<ArrayList<String>>>a : batchTraceability) { //serializacion de los datos a enviar
                    aux = a.toString();
                    msgToMPLan = msgToMPLan.concat(aux);
                }
                AID Agent = new AID(parentAgentID, false);
                sendACLMessage(7, Agent, "Information", "OrderInfo", msgToMPLan, myAgent);

                if (sonAgentID.size() == 0) { // todos los batch agent de los que es padre ya le han enviado la informacion
                    sendACLMessage(7, myAgent.getAID(), "Information", "Shutdown", "Shutdown", myAgent); // autoenvio de mensaje para asegurar que el agente de desregistre y se apague
                    return true;
                }
                batchIndex = batchTraceability.size() - 1;
                newBatch = true;
            }
        }
        ACLMessage msg3=myAgent.receive(template3);
        if(msg3!=null){
            QoSresponse_flag=true;
            batch_to_take_down=msg3.getContent();
        }
        ACLMessage msg4=myAgent.receive(template4);
        if(msg4!=null){
            String[] parts=msg4.getContent().split("/");
            String timeout_batch_id=parts[0];
            String s_difference=parts[1];
            String ft_of_batch="";
            for(int m=0;m<batch_last_items_ft.size();m++){
                if(batch_last_items_ft.get(m).get(0).equals(timeout_batch_id)){
                    ft_of_batch= batch_last_items_ft.get(m).get(3);
                }
            }
            if(ft_of_batch!="") {
                long difference = Long.parseLong(s_difference);

                try {
                    Date tdate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(ft_of_batch);
                    new_expected_finish_time= convertToDateViaSqlTimestamp(convertToLocalDateTimeViaSqlTimestamp(tdate).plusSeconds(difference/1000));

                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }else{
                System.out.println("Not my batch, something went wrong");
            }
        }
        return false;
    }

    @Override
    public Void terminate(MWAgent myAgent) {
        this.myAgent = myAgent;
        String parentName = "";

        try {
            ACLMessage reply = sendCommand(myAgent, "get * reference=" + orderNumber, "parentAgentID");
            //returns the names of all the agents that are sons
            if (reply != null)   // Si no existe el id en el registro devuelve error
                parentName = reply.getContent(); //gets the name of the agent´s parent
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            AID Agent = new AID(parentAgentID, false);
            sendACLMessage(7, Agent, myAgent.getLocalName(), "Shutdown", "Order completed", myAgent); // Informa al Mplan Agent que ya ha finalizado su tarea
            myAgent.deregisterAgent(parentName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    protected ArrayList<ArrayList<String>> batch_finish_times(String rawdata){

        ArrayList<ArrayList<String>> batchFT= new ArrayList<ArrayList<String>>();
        String[] parts1=rawdata.split("&");
        rawdata=parts1[1];
        ArrayList<String> data= new ArrayList<String>(Arrays.asList(rawdata.split("_")));
        for(int i=0;i<data.size();i++){
            String temp=data.get(i);
            String[] parts=temp.split("/");
            batchFT.add(i,new ArrayList<String>());
            batchFT.get(i).add(parts[0]);
            batchFT.get(i).add(parts[1]);
        }

        return batchFT;
    }

    protected ArrayList<ArrayList<String>> add_delays(ArrayList<ArrayList<String>> allbatches){
        ArrayList<ArrayList<String>> batches_with_delays=allbatches;
        MessageTemplate template3=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("delay"));
        int j=0;
        while (allbatches.size() > j) {
        ACLMessage msg=myAgent.blockingReceive(template3);
        String rawdelay=msg.getContent();
        String[] parts=rawdelay.split("/");
        String batch=parts[0];
        String delay=parts[1];
        for(int i=0;i<batches_with_delays.size();i++){
            if(batches_with_delays.get(i).get(0).equals(batch)){
                batches_with_delays.get(i).add(delay);
                j++;
            }
        }
        }
        return batches_with_delays;
    }


    protected Date getactualtime(){
        String actualTime;
        int ano, mes, dia, hora, minutos, segundos;
        Calendar calendario = Calendar.getInstance();
        ano = calendario.get(Calendar.YEAR);
        mes = calendario.get(Calendar.MONTH) + 1;
        dia = calendario.get(Calendar.DAY_OF_MONTH);
        hora = calendario.get(Calendar.HOUR_OF_DAY);
        minutos = calendario.get(Calendar.MINUTE);
        segundos = calendario.get(Calendar.SECOND);
        actualTime = ano + "-" + mes + "-" + dia + "T" + hora + ":" + minutos + ":" + segundos;
        Date actualdate = null;
        try {
            actualdate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(actualTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return actualdate;
    }

    protected LocalDateTime convertToLocalDateTimeViaSqlTimestamp(Date dateToConvert) {
        return new java.sql.Timestamp(
                dateToConvert.getTime()).toLocalDateTime();
    }
    protected Date convertToDateViaSqlTimestamp(LocalDateTime dateToConvert) {
        return java.sql.Timestamp.valueOf(dateToConvert);
    }




}
