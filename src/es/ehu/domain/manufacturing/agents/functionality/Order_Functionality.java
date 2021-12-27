package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.template.interfaces.AvailabilityFunctionality;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

public class Order_Functionality extends DomApp_Functionality implements BasicFunctionality, AvailabilityFunctionality {

    private static final long serialVersionUID = 1L;
    private MWAgent myAgent;

    private List<String> elementsToCreate = new ArrayList<>();
    private int chatID = 0; // Numero incremental para crear conversationID
    private ACLMessage orderName=new ACLMessage();
    private String firstState;
    private String redundancy;
    private String parentAgentID, orderNumber;
//    private ArrayList<String> myReplicasID = new ArrayList<>();
//    private ArrayList<AID> sonAgentID = new ArrayList<>(); // lista con los nombres de los agentes de los que es padre
    private ArrayList<String> sonAgentID = new ArrayList<>();
    private Integer batchIndex = 1;
    private Boolean newBatch = true, firstTime = true;
    private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> batchTraceability = new ArrayList<>();
    private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> deserializedMessage = new ArrayList<>(); // Mensaje recibido desde el batchAgent deserializado
    private String mySeType;
    private MessageTemplate template, template2,templateFT,template3,template4,template5, template6,template7;
    private volatile String orderreference=null;
    private volatile String raw_ft=null;
    private volatile AID QoSID = new AID("QoSManagerAgent", false);
    private volatile ArrayList<String> online_batches=new ArrayList<String>();
    private volatile String batch_to_take_down="";
    private volatile Date new_expected_finish_time=null;
    private volatile boolean QoSresponse_flag=false;
    private volatile String batch_to_update=null;
    private MessageTemplate echotemplate=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchOntology("Acknowledge"));
    private MessageTemplate QoStemplate=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchOntology("acl_error"));
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
            System.out.println(batch+" batch expected finish time: "+expected_finish_time);
            while (!exit_timeout) {
                while (expected_finish_time.after(getactualtime()) && !exit_timeout) {

                    if (batch_to_take_down.equals(batch)) {
                        exit_timeout = true;
                    }
                    if (new_expected_finish_time != null&&batch_to_update!=null) { //comprueba que se actualice
                        if(batch_to_update.equals(batch)) {
                            expected_finish_time = new_expected_finish_time;
                            System.out.println("BATCH FINISH-TIME UPDATED");
                            System.out.println(batch+" batch expected finish time: "+expected_finish_time);
                            //System.out.println("Batch " + batch + " finish time updated to: " + expected_finish_time);
                            new_expected_finish_time = null;
                            batch_to_update=null;
                        }
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

                    if (!QoSresponse_flag) { //En caso de no recibir respuesta del QoS a tiempo el order agent se detecta asi mismo aislado
                        System.out.println("I'm probably isolated. Shutting down entire node");
                        System.exit(0);  //mata a su nodo
                    } else {
                        QoSresponse_flag = false;
                    }
                    QoSreported=true;
                }
                if (batch_to_take_down.equals(batch)) { //cuando la variable volatil batch_to_take_down coincide con el batch del timeout el timeout finaliza porque el batch ha terminado
                    exit_timeout = true;
                    batch_to_take_down=""; //resetea la variable para el siguiente batch (evita bugs)
                }

            }
            batch_to_take_down = "";
            System.out.println("Batch "+batch+" timeout finished.");
        }
    }


    @Override
    public void setState(String state) {
        ArrayList<ArrayList<ArrayList<ArrayList<String>>>> Traceability=new ArrayList<>();
        ArrayList<String> remaining=new ArrayList<String>();
        ArrayList<ArrayList<String>> FT=new ArrayList<>();
        ArrayList<String> replicas=new ArrayList<String>();
        String parts1[] =state.split("/div0/"); //el divisor 0 divide los argumentos y el resto se usan para los arraylist
        String productTraceabilityConc = parts1[0]; //trazabilidad concatenada
        String remainingConc = null;
        if (parts1[1] != null&&parts1[1] != "") {
            remainingConc = parts1[1]; //solo si quedan acciones/SonAgentIDs
        }
        String firstimeString = parts1[2]; //primera vez

        String FinishTimesConc=parts1[3]; //finish times concatenados (cada agente de aplicación lleva un formato)
        parentAgentID=parts1[4]; 					//parent
        String replicasConc=parts1[5];		//replicas del agente

        String parts2[] = productTraceabilityConc.split("/div1/"); //construye la trazabilidad
        for (int i = 0; i < parts2.length; i++) {
            Traceability.add(i, new ArrayList<ArrayList<ArrayList<String>>>());
            String parts3[] = parts2[i].split("/div2/");
            for (int j = 0; j < parts3.length; j++) {
                Traceability.get(i).add(j, new ArrayList<ArrayList<String>>());
                String parts4[] = parts3[j].split("/div3/");
                for (int k = 0; k < parts4.length; k++) {
                    Traceability.get(i).get(j).add(k, new ArrayList<String>());
                    String parts5[] = parts4[k].split("/div4/");
                    for (int l = 0; l < parts5.length; l++) {
                        Traceability.get(i).get(j).get(k).add(parts5[l]);
                    }
                }
            }
        }
        batchTraceability=Traceability;
        if (remainingConc != null) {    //construye los sonagentID o actionlist
            String parts6[] = remainingConc.split("/div1/");
            for (int i = 0; i < parts6.length; i++) {
                remaining.add(parts6[i]);
            }
        }
        sonAgentID=remaining;
        firstTime = Boolean.parseBoolean(firstimeString);
        String parts7[]=FinishTimesConc.split("/div1/");
        for(int i=0;i<parts7.length;i++) {
            FT.add(i, new ArrayList<String>());
            String parts8[] = parts7[i].split("/div2/");
            for (int j = 0; j < parts8.length; j++) {
                FT.get(i).add(parts8[j]);
            }
        }
        batch_last_items_ft=FT;
        String parts9[]=replicasConc.split("/div1/");
        for(int k=0;k<parts9.length;k++){
            if(!parts9[k].equals(myAgent.getLocalName())){
                replicas.add(parts9[k]);
            }
        }
        myAgent.replicas=replicas;

    }
    @Override
    public String getState(){
        myAgent.antiloopflag=true;
        String state="";

        for(int i=0;i<batchTraceability.size();i++){
            if(i!=0){
                state=state+"/div1/";
            }
            for(int j=0;j<batchTraceability.get(i).size();j++){
                if(j!=0){
                    state=state+"/div2/";
                }
                for(int k=0;k<batchTraceability.get(i).get(j).size();k++){
                    if(k!=0){
                        state=state+"/div3/";
                    }
                    for(int l=0;l<batchTraceability.get(i).get(j).get(k).size();l++){
                        if(l!=0){
                            state=state+"/div4/";
                        }
                        state=state+batchTraceability.get(i).get(j).get(k).get(l);
                    }
                }
            }
        }
        state=state+"/div0/";
        for(int i=0;i<sonAgentID.size();i++){
            if(i!=0){
                state=state+"/div1/";
            }
            state=state+sonAgentID.get(i);
        }
        state=state+"/div0/"+String.valueOf(firstTime)+"/div0/";

        for(int i=0;i<batch_last_items_ft.size();i++){ //concatena los FT de los item
            if(i!=0){
                state=state+"/div1/";
            }
            for(int j=0;j<batch_last_items_ft.get(i).size();j++){
                if(j==0){
                    state=state+batch_last_items_ft.get(i).get(j);
                }else{
                    state=state+"/div2/"+batch_last_items_ft.get(i).get(j);
                }
            }
        }
        state=state+"/div0/"+parentAgentID+"/div0/";

        try {   //realiza la consulta al sa para tener la lista de replicas actualizada.

            String[] replicas=new String[1];
            if(redundancy.equals("1")){
                replicas[0] =" ";
            }else{
                ACLMessage parent = sendCommand(myAgent, "get " + myAgent.getLocalName() + " attrib=parent", "GetOrderParent");
                ACLMessage replicasACL = sendCommand(myAgent, "get * state=tracking parent=" + parent.getContent(), "GetOrderUpdatedReplicas");
                if(replicasACL.getContent().contains(",")){
                    replicas= replicasACL.getContent().split(",");
                }else{
                    replicas[0] = replicasACL.getContent();
                }
            }

            for(int i=0; i<replicas.length;i++){
                if(i==0){
                    state=state+replicas[i];
                }else{
                    state=state+"/div1/"+replicas[i];
                }
            }
        }catch  (Exception e) {
            e.printStackTrace();
        }
        myAgent.antiloopflag=false;
        return state;
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
        this.template5=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("delay"));
        this.template6=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("delete_replica"));
        this.template7=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("restore_replica"));
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
            sonAgentID = (ArrayList<String>) result[1];

            myAgent.replicas = (ArrayList<String>) result[0];

            templateFT=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchOntology("Ftime_order_ask"));
            try {
                orderName = sendCommand(myAgent, "get " + myAgent.getLocalName() + " attrib=parent", "name"); //consigue el nombre del order
                ACLMessage reference = sendCommand(myAgent, "get " + orderName.getContent() + " attrib=reference", "Reference"); //consigue la referencia del order
                AID plannerID = new AID("planner", false);
                orderreference=reference.getContent();
                sendACLMessage(16, plannerID,"Ftime_order_ask", "finnish_time", reference.getContent(), myAgent ); //pide el finish time de cada item al planner
                ACLMessage finishtime= myAgent.blockingReceive(templateFT); //recibe los finish times concatenados
                myAgent.msgFIFO.add((String) finishtime.getContent());
                System.out.println(finishtime.getContent());
                raw_ft=finishtime.getContent();
                batch_last_items_ft=batch_finish_times(raw_ft);

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
            myAgent.msgFIFO.add((String) msg.getContent());
            sendACLMessage(7, msg.getSender(), "Acknowledge", msg.getConversationId(),"Received",myAgent);


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
                String batchNumber =deserializedMessage.get(0).get(0).get(3).get(0);
                while(!batch_to_take_down.equals("")){} //espera al reseteo de la variable para escribir el batch (evita bugs)
                batch_to_take_down=batchNumber;
                batchTraceability = addNewLevel(batchTraceability, deserializedMessage,false);
            }
            newBatch = false; // hasta que el batch añadido no se complete, cada vez que se reciba un mensaje el dato se sobrescribira

        }
        ACLMessage msg2 = myAgent.receive(template2);
        // Recepcion de mensajes para eliminar de la lista de agentes hijo los agentes batch que ya han enviado toda la informacion
        if (msg2 != null) {
            myAgent.msgFIFO.add((String) msg2.getContent());
            AID sender = msg2.getSender();
            if (msg2.getContent().equals("Batch completed")){
                String msgSender = msg2.getOntology();
                for (int i = 0; i < sonAgentID.size(); i++) {
                    if (sonAgentID.get(i).equals(msgSender)){
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
                Object[] ExpMsg=AddToExpectedMsgs(parentAgentID,"OrderInfo",msgToMPLan);
                myAgent.expected_msgs.add(ExpMsg);
//                ACLMessage ack= myAgent.blockingReceive(echotemplate,250);
//
//                if(ack==null){
//                    String informQoS = "7" + "/div/" + "Information"+ "/div/" +"OrderInfo"+ "/div/" +parentAgentID+ "/div/" +msgToMPLan;
//                    sendACLMessage(ACLMessage.FAILURE, QoSID, "acl_error", "msgtoparent", informQoS, myAgent);
//                    ACLMessage QoSR = myAgent.blockingReceive(QoStemplate,2000);
//                    if(QoSR==null){
//                        System.out.println("I'm isolated. Shutting down entire node.");
//                        System.exit(0);
//                    }
//                    boolean f=false;
//                    for(int i=0;i<myAgent.ReportedAgents.size();i++){
//                        myAgent.ReportedAgents.get(i).equals(parentAgentID);
//                        f=true;
//                    }
//                    if(!f){
//                        myAgent.ReportedAgents.add(parentAgentID);//si no se ha denunciado el agente añadirlo a la lista
//                    }
//                }

                if (sonAgentID.size() == 0) { // todos los batch agent de los que es padre ya le han enviado la informacion
                    sendACLMessage(7, myAgent.getAID(), "Information", "Shutdown", "Shutdown", myAgent); // autoenvio de mensaje para asegurar que el agente de desregistre y se apague
                    return true;
                }
                batchIndex = batchTraceability.size() - 1;
                newBatch = true;
            }

        }
        ACLMessage msg3=myAgent.receive(template3);
        if(msg3!=null){            //confirmación de timeout
            myAgent.msgFIFO.add((String) msg3.getContent());
            QoSresponse_flag=true;
            batch_to_take_down=msg3.getContent();
        }
        ACLMessage msg4=myAgent.receive(template4);
        if(msg4!=null){ //Actualiza el finish time del batch recibido (por petición de reset del QoS)
            myAgent.msgFIFO.add((String) msg4.getContent());
            String[] parts=msg4.getContent().split("/");
            String timeout_batch_id=parts[0];
            String s_difference=parts[1];
            String ft_of_batch="";
            for(int m=0;m<batch_last_items_ft.size();m++){
                if(batch_last_items_ft.get(m).get(0).equals(timeout_batch_id)){
                    ft_of_batch= batch_last_items_ft.get(m).get(2);
                }
            }
            if(ft_of_batch!="") {
                long difference = Long.parseLong(s_difference);

                try {
                    Date tdate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(ft_of_batch);
                    batch_to_update=timeout_batch_id;
                    new_expected_finish_time= convertToDateViaSqlTimestamp(convertToLocalDateTimeViaSqlTimestamp(tdate).plusSeconds(difference/1000));

                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }else{
                System.out.println("Not my batch, something went wrong");
            }
        }
        ACLMessage msg6=myAgent.receive(template6);
        if(msg6!=null){
            boolean f=false;
            for(int i=0;i<myAgent.IgnoredReplicas.size();i++){ //se elimina de la lista de ignorados en caso de que esté
                if(myAgent.IgnoredReplicas.get(i).equals(msg.getContent())){
                    myAgent.IgnoredReplicas.remove(i);
                    f=true;
                }
            }
            if(f==false){ //en caso de no encontrarlo en la lista de ignorados, es posile que aun se encuentre en la lista de replicas normal
                for(int i=0;i<myAgent.replicas.size();i++){
                    if(myAgent.replicas.get(i).equals(msg.getContent())){
                        myAgent.replicas.remove(i);
                    }
                }
            }
        }
        ACLMessage msg7=myAgent.receive(template7);
        if(msg7!=null){
            for(int i=0;i<myAgent.IgnoredReplicas.size();i++) { //se elimina de la lista de ignorados en caso de que esté
                if (myAgent.IgnoredReplicas.get(i).equals(msg.getContent())) {
                    myAgent.replicas.add(myAgent.IgnoredReplicas.get(i)); //se vuelve a añadir a la lista de replicas
                    for(int j=0; j<myAgent.ReportedAgents.size();j++){
                        if(myAgent.ReportedAgents.get(j).equals(myAgent.IgnoredReplicas.get(i))){
                            myAgent.ReportedAgents.remove(j); // se elimina tambien de la lista de agentes reportados
                        }
                    }
                    myAgent.IgnoredReplicas.remove(i);
                }
            }
        }
        ACLMessage msg5=myAgent.receive(template5);
        if(msg5!=null){                                 //Genera el timeout al recibir el delay de cada batch
            myAgent.msgFIFO.add((String) msg5.getContent());
            String rawdelay=msg5.getContent();
            String[] parts=rawdelay.split("/");
            String batchref=parts[0];
            String delay=parts[1];
            Date expected_FT=null;
            String batchFT=null;
            int temp=0;
            for(int p=0;p<batch_last_items_ft.size();p++) {
                if(batch_last_items_ft.get(p).get(0).equals(batchref)){
                    batchFT=batch_last_items_ft.get(p).get(1);
                    temp=p;
                }
            }
            if(batchFT!=null) {
                try {
                    expected_FT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(batchFT);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long startime = getactualtime().getTime() - (Long.parseLong(delay));
                Date d = new Date(startime); //para debug
                LocalDateTime new_expected_FT = convertToLocalDateTimeViaSqlTimestamp(getactualtime());
                new_expected_FT = new_expected_FT.plusSeconds(((expected_FT.getTime() - startime) / 1000));
                expected_FT = convertToDateViaSqlTimestamp(new_expected_FT);
//                System.out.println(batchref + " batch expected finish time: " + expected_FT);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                String FT_string = sdf.format(expected_FT);
                batch_last_items_ft.get(temp).add(FT_string);

                Ordertimeout t = new Ordertimeout(expected_FT, batchref);
                t.start();
            }else{
                System.out.println("**ERROR**. No timeout generated for batch "+ batchref);
            }

        }

        return false;
    }

    @Override
    public Void terminate(MWAgent myAgent) {
        this.myAgent = myAgent;
        String parentName = "";
        unregister_from_node();
        if(myAgent.ActualState=="running"){ //para filtrar las replicas ejecutando terminate
            try {
                ACLMessage reply = sendCommand(myAgent, "get * reference=" + orderNumber, "parentAgentID");

                if (reply != null) {   // Si no existe el id en el registro devuelve error
                    parentName = reply.getContent(); //gets the name of the agent´s parent
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                AID Agent = new AID(parentAgentID, false);
                KillReplicas(myAgent.replicas);
                sendACLMessage(7, Agent, myAgent.getLocalName(), "Shutdown", "Order completed", myAgent); // Informa al Mplan Agent que ya ha finalizado su tarea
                myAgent.deregisterAgent(parentName);

            } catch (Exception e) {
                e.printStackTrace();
            }
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
}
