package es.ehu.platform;

/**
 * Agente base el cual contiene las funciones necesarias para interactuar con el middleware
 */

import com.google.gson.Gson;
import es.ehu.domain.manufacturing.utilities.StructBatchAgentState;
import es.ehu.domain.manufacturing.utilities.StructMplanAgentState;
import es.ehu.domain.manufacturing.utilities.StructOrderAgentState;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;



public class MWAgent extends Agent {
    private static final long serialVersionUID = 8505462503088901786L;

    static final Logger LOGGER = LogManager.getLogger(MWAgent.class.getName()) ;

    /**
     *  Instancia en running del componente (actualizarla cuando al MWM llega un setState)
     */
    //TODO refresh local cache
    public static ConcurrentHashMap<String, String> runningMwm = new ConcurrentHashMap<String, String>();

    /**
     *  Instancias en tracking del componente (arraylist para actualiza el estado, actualizarla cuando al MWM llega un setState)
     */
    //TODO refresh local cache

    public BasicFunctionality functionalityInstance;
    public String[] targetComponentIDs, sourceComponentIDs;
    public int period = -1;
    public String ActualState=null;
    public ArrayList<String> replicas=new ArrayList<String>();
    public boolean ExecTimeStamped=false;
    public String cmpID = null;
    public boolean antiloopflag=false;
    public String state ="";    //para uso en autoidle
    public boolean change_state=false; //para cambiar el estado de una m�quina a si misma
    public CircularFifoQueue msgFIFO = new CircularFifoQueue(5);    //variable de almacenamiento de ACL
    public int initTransition;
    public String conversationId;
    public int TMSTMP_cnt=0;
    public Object initialExecutionState = null;
    public ArrayList<Object[]> expected_msgs= new ArrayList<Object[]>();
    private int convIDCounter=1;
    public HashMap<String,ArrayList<ACLMessage>> msg_buffer=new HashMap<String,ArrayList<ACLMessage>>(); //listado de mensajes guardados para reenviar cuando el receptor este disponible
    public String gatewayAgentName; // Guarda el nombre del agente pasarela
    // Par�metros de configuraci�n
    public boolean mwmStoresExecutionState = true;


    /**
     * Primera transici�n a realizar
     */

    public MWAgent() {
    }

    protected void setup() {
    }

    protected void afterMove() {
        try { resolveMWM(); } catch (Exception e) {e.printStackTrace();}
        String container="";
        try {  container=this.getContainerController().getContainerName(); } catch (Exception e) {e.printStackTrace();}
        String cmd = "set" + " " + this.getLocalName()+" node="+container;
        String response = sendCommand(cmd).getContent();
        this.getContainerController().getName();
        LOGGER.debug(cmd + " > " + response);
        LOGGER.exit();
    }

    protected void takeDown() {
        LOGGER.entry();
        try { resolveMWM(); } catch (Exception e) {e.printStackTrace();}

        String cmd = "del" + " "+this.getLocalName();
        String response = sendCommand(cmd).getContent();
        this.getContainerController().getName();
        LOGGER.debug(cmd + " > " + response);
        LOGGER.exit();
    }

    /**
     * Esperar a que todos los componetes que le siguen esten registrados en el MM
     *
     * @param componentIDs
     *            : array con nombre de componentes
     * @param type
     *            : tipo de componentes "receivers" o "sources"
     * @param state
     *            : estado en el que deben estar los componentes
     * @param myAgent
     * @throws Exception
     */
    private void waitForComponents(String[] componentIDs, String type, String state, Agent myAgent) throws Exception  {
        LOGGER.entry(componentIDs, type, state, myAgent);

        try { resolveMWM(); } catch (Exception e) {e.printStackTrace();}

        // espero hasta que en MM est�n dados de alta
        for (String componentID : componentIDs) {
            // targetComponentIDs
            LOGGER.debug("Esperando a " + componentID);

            String result = getInstances(componentID, state);

            while (result.equals("")) {
                result = getInstances(componentID, state);
                Thread.sleep(100);
            }

        } // end for targetIDs
        LOGGER.exit();
    }

    /**
     * Eliminar un agente del registro del Middleware Manager
     *
     * @param localName
     *            : nombre del agente
     * @throws Exception
     */
    public void deregisterAgent(String localName) throws Exception {
        LOGGER.entry(localName);

        String cmd = "del" + "  " + localName;
        String response = sendCommand(cmd).getContent();
        LOGGER.info(cmd + " > " + response);

        LOGGER.exit();

    }

    /**
     * Devuelve el componente a partir la instancia/implementaci�n.
     *
     * @param id id de la instancia/implementaci�n
     * @return
     * @throws FIPAException
     */
    public String getComponent(String id) {
        return (sendCommand("getcmp" + " " + id)).getContent();
    }

    /**
     * Devuelve las instacias de un componente en un estado.
     * @param cmpID id del componente del que requerimos las instancias
     * @param state puede ser running/tracking/...
     * @return
     * @throws FIPAException
     */
    public String getInstances(String cmpID, String state)  {
        String msg = "getins" + " " +cmpID+" state="+state;
        ACLMessage reply = sendCommand(msg);
        String response = (reply==null)?"":reply.getContent();
        LOGGER.info(msg+">"+response);
        return response;
    }



    /**
     * Enviar mensaje de control al Middleware Manager
     *
     * @param cmd
     *            : mensaje a enviar al MM
     * @return: respuseta del manager
     * @throws Exception
     */
    public ACLMessage sendCommand(String cmd)  {
        LOGGER.entry(cmd);
        System.out.println("cmd="+cmd);

        try { resolveMWM(); } catch (Exception e) {e.printStackTrace();}
        ACLMessage aMsg = new ACLMessage(ACLMessage.REQUEST);

        aMsg.setContent(cmd);
        aMsg.setOntology("control");
        aMsg.addReceiver(new AID("sa", AID.ISLOCALNAME));

        aMsg.setReplyWith(cmd+"_"+System.currentTimeMillis());
        // el conversationId lo utiliza el MWM para despertar la tarea que interveniene en esta conversaci�n (el id coincide con el id de la tarea)
        aMsg.setConversationId(conversationId);

        send(aMsg);

        MessageTemplate mt = MessageTemplate.MatchInReplyTo(aMsg.getReplyWith());

        ACLMessage reply = blockingReceive(mt, 1000);

        int i=0;
        while ((reply == null) && (i<1000)){

            try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
            reply = receive(mt);
//            LOGGER.debug("reply = receive(mt);");
            i++;

        }
        if (reply == null)
            LOGGER.warn("mwm tarda en responder");
        else
            LOGGER.info("mm("+aMsg.getContent()+ ") > " + reply.getContent());

        return LOGGER.exit(reply);
    }

    /**
     * Realiza los procesos necesarios para inicializar el middleware como
     * obtener las direciones reales de los componentes y registrar el agente en el MM
     *
     * @param targetComponentIDs
     *            : lista con los componentes a los cuales se les envia el
     *            mensaje
     * @param sourceComponentIDs
     *            : lista con los componentes de los cuales se reciben mensajes
     * @param myAgent
     * @throws Exception
     */

    public void MWInit(String[] targetComponentIDs, String[] sourceComponentIDs, Agent myAgent) throws Exception {
        LOGGER.entry(targetComponentIDs, sourceComponentIDs, myAgent);
        // esperar a que todos los targets est�n en el MM

        LOGGER.info("waitForComponents(receivers)");
        if (targetComponentIDs != null) waitForComponents(targetComponentIDs, "receivers", "running|paused", myAgent);

        // instancias. TODO: refrescar esto cuando hay cambios en el MM
        LOGGER.info("waitForComponents(sources)");
        if (sourceComponentIDs != null) waitForComponents(sourceComponentIDs, "sources", "boot|running|paused", myAgent); //TODO: pensar esta parte como encaja el estado

        LOGGER.exit();
    }



    /**
     * Metodo para enviar el mensaje a los distintos receptores.
     *
     * @param msg
     *            : mensaje a enviar
     * @param cmpIDs
     *            : lista de los componentes a los que se les envia el mensaje.
     */
    public String sendMessage(Serializable msg, String[] cmpIDs) {
        if (cmpIDs == null) {
            return LOGGER.exit("null cmpID");
        }
        LOGGER.entry(msg, cmpIDs);

        String response = "";

        //if (targets.length==0) return response;
        //try {

        ACLMessage aMsg = new ACLMessage(ACLMessage.INFORM);
        aMsg.setOntology("data");
        try { aMsg.setContentObject(msg); } catch (IOException e) { e.printStackTrace(); }
        aMsg.setReplyWith("data_"+System.currentTimeMillis());

        StringBuilder sTargets = new StringBuilder();
        boolean separador=false;

        for (String cmpID: cmpIDs) {
            //log.info("Sending data to "+cmpID);

            //String cmpins = runningInstance.get(cmpID);
            String cmpins = this.getInstances(cmpID, "running");

            LOGGER.debug("this.getInstances("+cmpID+", \"running\"))" + cmpins);
            if (separador) sTargets.append(",");
            else separador=true;

            sTargets.append(cmpID+"("+cmpins+")");
            if (cmpins.isEmpty()) cmpins="null";  // si no hay instancia env�o "null" > desencadena negociaci�n.

            aMsg.addReceiver(new AID (cmpins, AID.ISLOCALNAME));
        }

//			Properties prop = aMsg.getAllUserDefinedParameters();
//			prop.setProperty("SF_TIMEOUT", "0");
//			for (Object key: prop.keySet()) System.out.println(key+"="+prop.get(key));
//			aMsg.setAllUserDefinedParameters(prop);
//			System.out.println("*********");
        send(aMsg);
        //ACLMessage msg = blockingReceive(MessageTemplate.);
        LOGGER.info(cmpID+"("+getLocalName() + "):data("+
                ((msg.getClass()==null)?"null":msg.getClass().getSimpleName())+
                ") > "+sTargets );
        return LOGGER.exit(response);
    }

    public String sendStateToReplicas(String msg, final String sTargets){ //en uso
        LOGGER.entry(msg, sTargets);
//        String [] cmpinss = sTargets.split("/div1/");
        String [] cmpinss = sTargets.split(",");
        if (cmpinss == null) return null;
        if (msg==null) msg = "null";

        String response = "";
        try {

            ACLMessage aMsg = new ACLMessage(ACLMessage.INFORM);
            aMsg.setConversationId("state_refresh_"+String.valueOf(convIDCounter));
            aMsg.setOntology("state");
            aMsg.setContent(msg);
            for (String cmpins: cmpinss){
                aMsg.addReceiver(new AID(cmpins, AID.ISLOCALNAME));
            }
            send(aMsg);
            AddToExpectedMsgs(aMsg); //un mensaje esperado por cada replica
            convIDCounter++;
            LOGGER.debug("sendState().send("+sTargets+"):"+aMsg);
            LOGGER.info(cmpID+"("+getLocalName() + "):state("+ ((msg.getClass()==null)?"null":msg.getClass().getSimpleName())+ ") > "+cmpID+"("+sTargets+")" );

        } catch (Exception e) {
            e.printStackTrace();
        }
        return LOGGER.exit(response);
    }


    /**
     * Search trackings of a component and the MWM to send them the state.
     * If there are not any tracking, the state is only sent to the MWM.
     * @param msg State information
     */

    public void sendStateToTracking(String msg, String category){ //en uso
        LOGGER.entry(msg);
//        this.get_timestamp(this,"StartSendState");
        Gson gson = new Gson();
        ArrayList<String>replicas=null;

        if(category.equals("batch")){
            StructBatchAgentState state = gson.fromJson(msg, StructBatchAgentState.class);
            replicas=state.getreplicas();
        }else if(category.equals("order")){
            StructOrderAgentState state = gson.fromJson(msg, StructOrderAgentState.class);
            replicas=state.getreplicas();
        }else{
            StructMplanAgentState state = gson.fromJson(msg, StructMplanAgentState.class);
            replicas=state.getreplicas();
        }

        String sTracking = "";
        for(int i=0; i<replicas.size();i++){
            if(i==0){
                sTracking=replicas.get(i);
            }else{
                sTracking=sTracking+ ","+ replicas.get(i);
            }
        }

//        this.get_timestamp(this,"GetStateDone");
//        String parts1[] = msg.split("/div0/");

//        String sTracking = parts1[5]; //las replicas siempre van codificadas en la posicion 5 del estado
        LOGGER.info("Tracking: " + sTracking);

        if(sTracking!= null&&(!sTracking.equals(""))){
            if (sTracking.length() > 0) {
                LOGGER.info("Refresh state to tracking instances:"+ sTracking);
                this.sendStateToReplicas(msg, sTracking);
            }
        }else {
            LOGGER.info("No tracking instances:");
        }

        LOGGER.exit();
    }

    public void get_timestamp(Agent a,String type){

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String myParentID=null;
        try {

        if(type.equals("AgentKilled")){

            String[] number_of_node=a.getLocalName().split("pnodeagent");
            ACLMessage HE= sendCommand("get * node="+number_of_node[1]);
            String[] agents_killed=new String[1];
            if(HE.getContent().contains(",")){
                agents_killed=HE.getContent().split(",");
            }else{
                agents_killed[0]=HE.getContent();
            }


            for(int i=0;i< agents_killed.length;i++){

                ACLMessage parent = sendCommand("get " + agents_killed[i] + " attrib=parent");
                if(parent!=null){
                    String contenido = parent.getContent()+","+agents_killed[i] +","+type+","+String.valueOf(timestamp.getTime());
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(new AID("ControlContainer-GWDataAcq", AID.ISLOCALNAME));
                    msg.setOntology("timestamp_err");
                    msg.setConversationId(a.getLocalName()+"_"+type+"_timestamp_"+TMSTMP_cnt++);
                    msg.setContent(contenido);
                    a.send(msg);
                }
            }

        }else if(type.equals("RedundancyRecovery")||type.equals("StartSendState")||type.equals("GetStateDone")||type.equals("MsgSentDone")||type.equals("AcknowledgeGenerated")||type.equals("FinishSendState")) {
            ACLMessage parent = sendCommand("get " + a.getLocalName() + " attrib=parent");
            if(parent!=null){
                String contenido = parent.getContent()+","+a.getLocalName() +","+type+","+String.valueOf(timestamp.getTime());
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID("ControlContainer-GWDataAcq", AID.ISLOCALNAME));
                if(type.equals("RedundancyRecovery")){
                    msg.setOntology("timestamp_err");
                }else{
                    msg.setOntology("timestamp_neg");
                }

                msg.setConversationId(a.getLocalName()+"_"+type+"_timestamp_"+TMSTMP_cnt++);
                msg.setContent(contenido);
                a.send(msg);
            }

        }else if(type.equals("MachineStart")||type.equals("GWAnswer")||type.equals("MachineRunning")){
            String number="1";
            String contenido = number+","+a.getLocalName() +","+type+","+String.valueOf(timestamp.getTime());
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("ControlContainer-GWDataAcq", AID.ISLOCALNAME));
            msg.setOntology("timestamp_neg");
            msg.setConversationId(a.getLocalName()+"_"+type+"_timestamp_"+TMSTMP_cnt++);
            msg.setContent(contenido);
            a.send(msg);
        }else{
            ACLMessage reply = sendCommand("get " + a.getLocalName() + " attrib=parent");
            if (reply != null){
                myParentID = reply.getContent();
            }
            //        String contenido = myParentID+",CreationTime,"+String.valueOf(timestamp.getTime());
            String contenido = myParentID+","+a.getLocalName() +","+type+","+String.valueOf(timestamp.getTime());
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("ControlContainer-GWDataAcq", AID.ISLOCALNAME));
            msg.setOntology("timestamp");
            msg.setConversationId(a.getLocalName()+"_"+type+"_timestamp_"+TMSTMP_cnt++);
            msg.setContent(contenido);
            a.send(msg);


            String contenido1 = "app, "+","+type+","+String.valueOf(timestamp.getTime()); //pisa el ultimo dato hasta terminar de generar todos los agentes
            ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
            msg2.addReceiver(new AID("ControlContainer-GWDataAcq", AID.ISLOCALNAME));
            msg2.setOntology("timestamp");
            msg2.setConversationId(conversationId);
            msg2.setContent(contenido1);
            a.send(msg2);

        }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void AddToExpectedMsgs(ACLMessage msg){ //como se trata de un env�o multiple en este caso hay que a�adir un expected mesage para cada receptor

        jade.util.leap.Iterator itor = msg.getAllReceiver();

        while (itor.hasNext()) {
            ACLMessage confirmation=new ACLMessage(msg.getPerformative());
            Object[] ExpMsg=new Object[2];
            confirmation.setOntology(msg.getOntology());
            confirmation.setConversationId(msg.getConversationId());
            confirmation.setContent(msg.getContent());
            Date date = new Date();
            long instant = date.getTime();
            instant=instant+1000; //a�ade una espera de 1.5 seg (orig) / 3 seg(pruebas Raspberrys) / 1 seg (pruebas cluster PCs)
            ExpMsg[1]=instant;
            AID receiver=(AID) itor.next();
            confirmation.addReceiver(receiver);
            LOGGER.debug("Added expected mesage from: "+receiver.getLocalName()+" with ID conv: "+String.valueOf(convIDCounter));
            ExpMsg[0]=confirmation; //codificamos en el object el mensaje de confirmacion para un unico receptor
            expected_msgs.add(ExpMsg);
        }
    }
    public void Acknowledge(ACLMessage msg, Agent agent){
        ACLMessage confirmation=new ACLMessage(ACLMessage.CONFIRM);
        confirmation.setContent(msg.getContent());
        confirmation.setOntology(msg.getOntology());
        confirmation.setConversationId(msg.getConversationId());
        confirmation.addReceiver(msg.getSender());
        agent.send(confirmation);
//        sendACLMessage(ACLMessage.CONFIRM,msg.getSender(),msg.getOntology(),msg.getConversationId(),msg.getContent(),agent);
    }


    public void setState(String cmpIns, String state) throws Exception {
        LOGGER.entry(cmpIns, state);

        try { resolveMWM(); } catch (Exception e) {e.printStackTrace();}
        String cmd = "set" + " " + cmpIns + " state=" + state;
        String response = sendCommand(cmd).getContent();
        LOGGER.info(response +" < " + cmd);

        LOGGER.exit();
    }



    /**
     *Obtener la direccion del Middleware Manager
     *
     * @throws Exception
     */
    public void resolveMWM() throws FIPAException {
        LOGGER.entry();

        if (runningMwm.containsKey("tmwm"))	    return;

        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("sa");
        dfd.addServices(sd);

        while (true) {
            //System.out.print(".");
            DFAgentDescription[] result = DFService.search(this,dfd);
            if ((result != null) && (result.length > 0)) {

                dfd = result[0];
                runningMwm.put("tmwm", dfd.getName().getLocalName());
                System.out.println("sa > "+dfd.getName().getLocalName());
                break;
            }
            System.out.print(".");
            try { Thread.sleep(100); } catch (InterruptedException e) {}

        } //end while (true)

        LOGGER.exit();
    } // end resolveMM



}
