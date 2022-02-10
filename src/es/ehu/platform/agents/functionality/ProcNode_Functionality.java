package es.ehu.platform.agents.functionality;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;

import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.*;
import es.ehu.platform.utilities.Cmd;
import es.ehu.platform.behaviour.*;

import java.sql.Timestamp;

public class ProcNode_Functionality implements BasicFunctionality, NegFunctionality{

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Agent myAgent;
    private String ID, className;
    private String ListAttrib="";
    private boolean firstime=true;
    private int TMSTMP_cnt=0;

    @Override
    public Void init(MWAgent myAgent) {
        this.myAgent = myAgent;
        LOGGER.entry();

        String attribs = "";
        String [] args = (String[]) myAgent.getArguments();

        //First, the arguments of the auxiliar agent are read

        for (int i=0; i<args.length; i++){
            if (!args[i].toString().toLowerCase().startsWith("id=")) attribs += " "+args[i];
            if (args[i].toString().toLowerCase().startsWith("id=")) return null;
        }

        //if the agent has no ID, it means it is an auxiliary agent
        //Therefore, a new agent is registered in the System Model and later it is started
        LOGGER.info(myAgent.getLocalName()+": autoreg > ");
        if (myAgent==null) System.out.println("My agent is null");
        if (myAgent.functionalityInstance==null) System.out.println("functionalityInstance is null");

        //Secondly, the ProcessNodeAgent is registered in the System Model

        String cmd = "reg pNodeAgent parent=system"+attribs;

        ACLMessage reply = null;
        try {
            reply = myAgent.sendCommand(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ID = reply.getContent();

        LOGGER.info(myAgent.getLocalName()+" ("+cmd+")"+" > mwm < "+ID);

        //Finally, the ProcessNodeAgent is started

        try {
            // Agent generation
            className = myAgent.getClass().getName();
            ((AgentController)myAgent.getContainerController().createNewAgent(ID,className, new String[] { "ID="+ID, "description=description" })).start();

            Thread.sleep(1000);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        return null;
    }

    @Override
    public long calculateNegotiationValue(String negAction, String negCriterion, Object... negExternalData) {
        // approximation to the total amount of memory currently available for future allocated objects, measured in bytes
        if(ListAttrib.contains((String)negExternalData[0])){
            long used_node=0; //Si en la lista de atributos ya existe el parent se devuelve un valor no competente para perder automáticamente.
            return used_node;
        }else{
            return Runtime.getRuntime().freeMemory();
        }
    }

    @Override
    public int checkNegotiation(String conversationId, String sAction, double negReceivedValue, long negScalarValue,
                                boolean tieBreak, boolean checkReplies,  boolean isPartialWinner, Object... negExternalData) {

        LOGGER.entry(conversationId, sAction, negReceivedValue, negScalarValue);

        String seID = (String)negExternalData[0];
        String seType = (String)negExternalData[1];
        String seClass = (String)negExternalData[2];
        String parentAgentID = (String)negExternalData[3];
        String redundancy = (String)negExternalData[4];
        String seFirstTransition = (String)negExternalData[5];


        if (negReceivedValue>negScalarValue) return NegotiatingBehaviour.NEG_LOST; //pierde negociación
        if ((negReceivedValue==negScalarValue) && !tieBreak ) return NegotiatingBehaviour.NEG_LOST; //empata negocicación pero no es quien fija desempate
//        if ((negReceivedValue==negScalarValue) && !tieBreak ) return NegotiatingBehaviour.NEG_RETRY;

        LOGGER.info("negotiation(id:"+conversationId+") partial winner "+myAgent.getLocalName()+"(value:"+negScalarValue+")");
        if (!checkReplies) return NegotiatingBehaviour.NEG_PARTIAL_WON; // es ganador parcial, faltan negociaciones por finalizar

        // estamos en la ?ltima comparaci?n y adem?s la hemos ganado---
        // para ser los ganadores verdaderos tendremos que ser ganadores parciales en cada momento
        if (!isPartialWinner) return NegotiatingBehaviour.NEG_LOST;

        //TODO: si este agente ha ganado algo posterior al timestamp del cfp.
        // si es que no actualizar este tiempo y ok
        // si ha ganado hay que pedir que se repita la actual> return NegotiatingBehaviour.NEG_NULL

        LOGGER.info("ejecutar "+sAction);

        Cmd action = new Cmd(sAction);

        if (action.cmd.equals("start")) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());


            LOGGER.info("id="+action.who);
//            ACLMessage hosted_elements =null;
            try {
//                hosted_elements = sendCommand("get "+myAgent.getLocalName()+" attrib=HostedElements");
                    if(!ListAttrib.contains(seID)) { //Si la lista de atributos no tiene la ID del sistem element lo añade
                        ACLMessage Actual_Attrib= sendCommand("get " + myAgent.getLocalName() + " attrib=HostedElements");
                        ListAttrib=Actual_Attrib.getContent(); //primero actualizamos la variable (para ejecucion de planes de manera consecutiva sin reinicializar el sistema).

                        if (ListAttrib.equals("")) {
                            ListAttrib = seID; //si es el primero lo añade sin más
//                            firstime = false;
                        } else {
                            ListAttrib = ListAttrib + "," + seID; //si no es el primer lo concatena
                        }
                        LOGGER.debug("ACTUAL ATRIBUTES: " + ListAttrib);
                        sendCommand("set " + myAgent.getLocalName() + " HostedElements=" + ListAttrib); //peticion al SA de actualización
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            try{
                // Registro el agente id>appagn101. seTypeAgent ASA, APA
                String agnID = sendCommand("reg "+seType+"Agent parent="+seID).getContent();
                // Instancio nuevo agente
                get_timestamp(timestamp, seID,agnID,"Node");
                get_timestamp(timestamp, seID,agnID,"NegotiationTime");

                AgentController ac = ((AgentController) myAgent.getContainerController().createNewAgent(agnID, seClass, new Object[] { "firstState="+seFirstTransition , "redundancy="+redundancy , "parentAgent=" + parentAgentID,"recovery=false"}));
                ac.start();
                String parts[]=myAgent.getLocalName().split("pnodeagent");
                sendCommand("set "+agnID+" node="+parts[1]); //Añade el número de nodo en el que se va a encontrar el agente


            }catch (Exception e) {e.printStackTrace();}
        }else if(action.cmd.equals("recover_tracking")){
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());


            LOGGER.info("id="+action.who);
//            ACLMessage hosted_elements =null;
            try {
//                hosted_elements = sendCommand("get "+myAgent.getLocalName()+" attrib=HostedElements");
                if(!ListAttrib.contains(seID)) { //Si la lista de atributos no tiene la ID del sistem element lo añade
                    ACLMessage Actual_Attrib= sendCommand("get " + myAgent.getLocalName() + " attrib=HostedElements");
                    ListAttrib=Actual_Attrib.getContent(); //primero actualizamos la variable (para ejecucion de planes de manera consecutiva sin reinicializar el sistema).

                    if (ListAttrib.equals("")) {
                        ListAttrib = seID; //si es el primero lo añade sin más
//                            firstime = false;
                    } else {
                        ListAttrib = ListAttrib + "," + seID; //si no es el primer lo concatena
                    }
                    LOGGER.debug("ACTUAL ATRIBUTES: " + ListAttrib);
                    sendCommand("set " + myAgent.getLocalName() + " HostedElements=" + ListAttrib); //peticion al SA de actualización
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                // Registro el agente id>appagn101. seTypeAgent ASA, APA
                String agnID = sendCommand("reg " + seType + "Agent parent=" + seID).getContent();
                // Instancio nuevo agente
                get_timestamp(timestamp, seID, agnID, "Node");
                get_timestamp(timestamp, seID, agnID, "NegotiationTime");

                AgentController ac = ((AgentController) myAgent.getContainerController().createNewAgent(agnID, seClass, new Object[]{"firstState=" + seFirstTransition, "redundancy=" + redundancy, "parentAgent=" + parentAgentID,"recovery=true"}));
                ac.start();
                String parts[] = myAgent.getLocalName().split("pnodeagent");
                sendCommand("set " + agnID + " node=" + parts[1]); //Añade el número de nodo en el que se va a encontrar el agente
            } catch (Exception e) {
                e.printStackTrace();
            }
            }

        return NegotiatingBehaviour.NEG_WON;
    }

    @Override
    public Object execute(Object[] input) {
        return null;
    }

    @Override
    public Void terminate(MWAgent myAgent) { return null; }

    public ACLMessage sendCommand(String cmd) throws Exception {

        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType("sa");
        dfd.addServices(sd);
        String mwm;

        while (true) {
            DFAgentDescription[] result = DFService.search(myAgent,dfd);

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
        msg.setOntology("control");
        msg.setContent(cmd);
        msg.setReplyWith(cmd);
        myAgent.send(msg);
//        ACLMessage reply = myAgent.blockingReceive(
//                MessageTemplate.and(
//                        MessageTemplate.MatchInReplyTo(msg.getReplyWith()),
//                        MessageTemplate.MatchPerformative(ACLMessage.INFORM))
//                , 1000);
        ACLMessage reply = myAgent.blockingReceive(
                MessageTemplate.and(
                        MessageTemplate.MatchInReplyTo(msg.getReplyWith()),
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM))
                );

        return LOGGER.exit(reply);
    }
    public void sendACL(int performative,String receiver,String ontology,String content,String ConvID){ //Funcion estándar de envío de mensajes
        AID receiverAID=new AID(receiver,false);
        ACLMessage msg=new ACLMessage(performative);
        msg.addReceiver(receiverAID);
        msg.setOntology(ontology);
        msg.setContent(content);
        msg.setConversationId(ConvID);
        myAgent.send(msg);
    }
    public void get_timestamp(Timestamp timestamp, String seID,String agent,String type){


        String contenido =null;
        if(type.equals("Node")){
            contenido = seID +","+agent+","+type+","+myAgent.getLocalName();
        }else{
            contenido = seID +","+agent+","+type+","+String.valueOf(timestamp.getTime());
//            if(seID.contains("batch")){
//                String appId =  "app"+seID.substring(seID.length()-3);
                String contenido1 = "app,"+" ,NegotiationTime,"+String.valueOf(timestamp.getTime());
                ACLMessage msg3 = new ACLMessage(ACLMessage.INFORM);
                msg3.addReceiver(new AID("ControlContainer-GWDataAcq", AID.ISLOCALNAME));
                msg3.setOntology("timestamp");
                msg3.setContent(contenido1);
                myAgent.send(msg3);
//            }
        }

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("ControlContainer-GWDataAcq", AID.ISLOCALNAME));
        msg.setOntology("timestamp");
        msg.setConversationId(myAgent.getLocalName()+"_"+type+"_timestamp_"+TMSTMP_cnt++);
        msg.setContent(contenido);
        myAgent.send(msg);

    }
}
