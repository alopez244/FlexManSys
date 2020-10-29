package es.ehu.domain.manufacturing.utilities;

import es.ehu.platform.utilities.XMLReader;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Planner extends Agent {

    private static final long serialVersionUID = 1L;
    static final Logger LOGGER = LogManager.getLogger(Planner.class.getName()) ;

    private int chatID = 0;

    protected void setup() {
        LOGGER.entry();
        LOGGER.warn("warning output sample");
        try {
            Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
            LOGGER.debug("A�adida tarea de apagado...");
        } catch (Throwable t) {
            LOGGER.debug(" *** Error: No se ha podido a�adir tarea de apagado");
        }
        addBehaviour(new PlannerInterface(this));
        LOGGER.exit();
    }

    class PlannerInterface extends SimpleBehaviour {

        private static final long serialVersionUID = 6711046229173067015L;

        public PlannerInterface(Agent a) {
            super(a);
        }

        public void action() {

            LOGGER.entry();
            try {

                LOGGER.info("start.");

                //User interface. The operator will use it for the register of mannufacturing applications.
                String cmd = "";
                String api = "Planner Agent local commands:\n"
                        + "register > Register a Manufacturing Plan of your desire\n"
                        + "help > SystemModelAgent commands summary\n"
                        + "exit > Shut down Planner Agent\n\n";
                System.out.print(api);
                LOGGER.info(api);

                //Decision loop related to the user interface.
                //This loop will remain active until the operator chooses the command "exit".
                while (!cmd.equals("exit")) {

                    //A new command is required to the operator.
                    Scanner in = new Scanner(System.in);
                    System.out.print("cmd: ");
                    cmd = in.nextLine();
                    System.out.println();

                    //The Planner Agent message queue is flushed away.
                    ACLMessage flush = receive();
                    while (flush!=null) {
                        System.out.println(flush.getInReplyTo()+" : "+ flush.getContent());
                        flush = receive();
                    }

                    // Create conversationId
                    String conversationId = myAgent.getLocalName() + "_" + chatID++;

                    //The field cmd is checked.
                    //If it is not empty, it is spplited with respect to the character ; as separation.
                    //Each new String is stored in a position of the array "cmds".
                    if (cmd.length()>0) {
                        String [] cmds = cmd.split(";");

                        //An ACL message is sent per each String in "cmds".
                        for (int i=0; i<cmds.length;i++){
                            cmds[i] = cmds[i].trim();
                            if (cmds[i].equals("register")) {
                                registerPredefined(cmds[i]);
                                if (cmds.length>1) System.out.print(" < "+cmds[i]);
                                System.out.print("\n\n");
                            }
                            else if (cmds[i].equals("exit")) {
                                cmd = "exit";
                                break;
                            }
                            else {
                                ACLMessage reply = sendCommand(cmds[i], conversationId);
                                if (reply!=null) {
                                    if (cmds.length>1) System.out.print(" < "+cmds[i]);
                                    System.out.print("\n\n");
                                }
                            }
                        }
                    }
                }

                System.exit(0);

            } catch (Exception e) {
                e.printStackTrace();
            }

            while(true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }

        public void registerPredefined(String cmd){

            //Definition of the HashMaps
            ConcurrentHashMap<String, String> attributes = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, String> restrictionList = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, ConcurrentHashMap<String, String>> restrictionLists = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();
            ConcurrentHashMap<String, String> serviceList = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, ConcurrentHashMap<String, String>> serviceLists = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();
            ConcurrentHashMap<String, String> agentAttributes = new ConcurrentHashMap<String, String>();

            agentAttributes.put("seClass", "es.ehu.domain.manufacturing.agents.MPlanAgent");

            // TODO Para pruebas con replicas
            Scanner in = new Scanner(System.in);
            System.out.println("Please, introduce the number of replicas you want to register.");
            System.out.print("Replicas: ");
            String redundancy = in.nextLine();
            try {
                Integer.parseInt(redundancy);
            } catch (NumberFormatException e) {
                LOGGER.error("ERROR GETTING REDUNDANCY NUMBER IN PLANNER");
                System.out.println("\nSorry, but you have not introduce a number, repeat the action please.\n");
                return;
            }
            if(!redundancy.equals(""))
                agentAttributes.put("redundancy", redundancy);

            String appPath="classes/resources/AppInstances/";
            String file="";
            System.out.println("\nPlease, introduce the name of the XML File you want to register.");
            System.out.print("File: ");
            file = in.nextLine();
            System.out.println();
            String uri=appPath+file;
            XMLReader fileReader = new XMLReader();
            ArrayList<ArrayList<ArrayList<String>>> xmlelements = null;
            try {
                xmlelements = fileReader.readFile(uri);
            } catch (Exception e) {
                LOGGER.error("ERROR GETTING FILE IN PLANNER");
                System.out.println("\nSorry, but you have not introduce the right file name, repeat the action please.\n");
                return;
            }

            //Variable initialization at their first levels
            ArrayList<String> parentIdList = new ArrayList<>();
            parentIdList.add(0,"system");
            String parentId = "";
            String seId = "";

            String conversationId = myAgent.getLocalName() + "_" + chatID++;

            //For structure is used to register all the elements
            for (int i = 0; i < xmlelements.size(); i++) {

                //First the attributes are collected
                attributes.clear();
                for (int j = 0; j < xmlelements.get(i).get(2).size(); j++) {
                    attributes.put(xmlelements.get(i).get(2).get(j), xmlelements.get(i).get(3).get(j));
                }

                //The parent Id is always the last element Id of the upper level
                parentId = parentIdList.get(Integer.parseInt(xmlelements.get(i).get(1).get(0)) - 1);

                // TODO BORRAR --> prueba seRegister systemModelAgent
                String commandReg = "seregister parentId="+parentId+ " parent=concepts seType=" + xmlelements.get(i).get(0).get(0);
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    commandReg = commandReg+" "+entry.getKey()+"="+entry.getValue();
                }

                restrictionList.put("refServID", "id56");
                restrictionList.put("aaa", "bbb");
                restrictionLists.put("procNode", restrictionList);

                restrictionLists.clear();

                if (!restrictionLists.isEmpty()) {
                    commandReg = commandReg + " & " + restrictionLists.keys().nextElement();

                    for (Map.Entry<String, ConcurrentHashMap<String, String>> restriction : restrictionLists.entrySet()) {
                        for (Map.Entry<String, String> entry : restriction.getValue().entrySet()) {

                            //Aqu� se obtienen las restricciones asociadas a ese tipo de recurso
                            commandReg = commandReg + " " + entry.getKey() + "=" + entry.getValue();
                        }
                    }
                }

                try {
                    sendCommand(commandReg, conversationId);
                } catch (FIPAException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                // TODO acaba el borrar

                //Now the register is performed and the element name is obtained
                try {
                    seId = seRegister(xmlelements.get(i).get(0).get(0), parentId, attributes, restrictionLists, conversationId);
                } catch (Exception e) {
                    LOGGER.error("ERROR IN seRegister METHOD OF PLANNER");
                    e.printStackTrace();
                }

                //Finally, the new seId is added to the parent Id list
                parentIdList.add(Integer.parseInt(xmlelements.get(i).get(1).get(0)), seId);
            }

            //After the register, the element to be validated and started will be the second on the list (the level 1 element)
            String app = parentIdList.get(1);

            //Validation
            try {
                iValidate(app, conversationId);
            } catch (Exception e) {
                LOGGER.error("ERROR IN iValidate METHOD OF PLANNER");
                e.printStackTrace();
            }

            //Start
            try {
                start(app, agentAttributes, conversationId);
            } catch (Exception e) {
                LOGGER.error("ERROR IN start METHOD OF PLANNER: Sending command to systemModelAgent");
                e.printStackTrace();
            }
        }

        private boolean finished = false;

        public boolean done() {
            LOGGER.entry();
            return LOGGER.exit(finished);
        }

    } //End of behaviour

    class ShutdownThread extends Thread {
        private Agent myAgent = null;

        public ShutdownThread(Agent myAgent) {
            super();
            this.myAgent = myAgent;
        }

        public void run() {
            LOGGER.entry();
            try {
                DFService.deregister(myAgent);
                myAgent.doDelete();
            } catch (Exception e) {}
            LOGGER.exit();
        }
    }

    // METHODS OF MasReconAgent
    public String seRegister(String seType, String parentId, ConcurrentHashMap<String, String> attributes, ConcurrentHashMap<String,ConcurrentHashMap<String, String>> restrictionLists, String conversationId) throws Exception {
        LOGGER.entry(seType, parentId, attributes, restrictionLists, conversationId);

        //compruebo restricciones

        String restrictionMatch = null;
        if (restrictionLists!=null) {
            for (Map.Entry<String, ConcurrentHashMap<String, String>> restriction : restrictionLists.entrySet()) {

                //Aqu� se obtiene el tipo de recurso del que se quiere comprobar las restricciones
                //String query = "get * category="+restriction.getKey();
                String query = "get * category=service";

                for (Map.Entry<String, String> entry : restriction.getValue().entrySet()) {

                    //Aqu� se obtienen las restricciones asociadas a ese tipo de recurso
                    query = query + " " + entry.getKey() + "=" + entry.getValue();
                }

                query = "get (get ("+query+") attrib=parent) category=" + restriction.getKey();

                System.out.println("***************** Lanzo consulta de comprobaci�n " + query);
                String validateRestriction = sendCommand(query, conversationId).getContent();
                if (validateRestriction.isEmpty()) {
                    LOGGER.info(query+">"+validateRestriction+": restricci�n incumplida");
                    throw new Exception();
                }
            }
        }

        //localizo tipo del padre    // TODO si el padre es "system" no comprobar
        // TODO si el padre est� en systemmodel.xml:
        // ir a systemmodel.xsd y buscar <xs:extension base="tipo" y en sus hijos
        // getFixed (tipo, atributo) > buscar <xs:extension base="tipo" y en sus hijos devuelve el fixed del que tenga nombre atributo

        // si es extensible el padre traigo la estructura desde el hijo de system con los atributos, (resolver su ID **registering**), validar appvalidar.xsd
        // si valida > volver a montarlo en systemmodel

        String parentType = sendCommand ("get "+parentId+" attrib=category", conversationId).getContent();
        if (parentType.equals("")) {
            LOGGER.info("ERROR: parent id not found"); //no existe padre
            throw new Exception();
        }
        LOGGER.info(parentId+" type="+parentType);

        //compruebo jerarqu�a // TODO si el padre es "system" comprobar que el se es raiz del appvalidation xsd -> dom

        String validateHierarchy = sendCommand("validate hierarchy "+seType+" "+parentType, conversationId).getContent();
        if (!validateHierarchy.equals("valid")) {
            LOGGER.info(seType+">"+parentType+": jerarqu�a incorrecta");
            throw new Exception();
        }
        LOGGER.info(seType+">"+parentType+": jerarqu�a correcta");

        // registro elemento en xml elements

        String command = "reg "+seType+" seParent="+parentId+ " parent=concepts";
        if (attributes!=null) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                command = command+" "+entry.getKey()+"="+entry.getValue();
            }
        }
        String ID = sendCommand(command, conversationId).getContent();

        // TODO: por cada restrictionList una llamada al get y comprobar que existen en el SystemModel
        for (String keyi: restrictionLists.keySet()){
            System.out.println("*******************key="+keyi);
            String restrictionList = sendCommand("reg restrictionList se="+keyi+" parent="+ID, conversationId).getContent();

            for (String keyj: restrictionLists.get(keyi).keySet()){
                String restriction = sendCommand("reg restriction attribName="+keyj+" attribValue="+restrictionLists.get(keyi).get(keyj)+" parent="+restrictionList, conversationId).getContent();
                System.out.println("keyj="+keyj);
            }
        }

        //validar elemento contra esquema systemElements

        String validation =  sendCommand("validate systemElement "+ID, conversationId).getContent();
        LOGGER.info(validation);

        if (!validation.equals("valid")) {
            sendCommand("del "+ID, conversationId).getContent();
            LOGGER.info("error xsd concepts");
            throw new Exception();
            //throw new XSDException(validation);

        } else LOGGER.info("xsd concepts correcto");

        // mover a registering.xml

        if (parentId.equals("system")) sendCommand("set " + ID + " parent=registering", conversationId).getContent();
        else sendCommand("set " + ID + " parent=(get " + ID + " attrib=seParent) seParent=", conversationId).getContent();

        return ID;
    }

    public String iValidate(String se, String conversationId) throws Exception {

        //localizo tipo

        LOGGER.info("iValidate("+se+")");
        String seType = sendCommand ("get "+se+" attrib=category", conversationId).getContent();

        //no existe

        if (seType.equals("")) {
            LOGGER.info("ERROR: id not found");
            return "";
        }
        LOGGER.info(seType+" type="+seType);

        //compruebo jerarqu�a
        String validateHierarchy = sendCommand("validate appValidation "+se+" "+seType, conversationId).getContent();
        if (!validateHierarchy.equals("valid")) {
            LOGGER.info(se+">"+seType+": xsd incorrecta");
            throw new Exception();

            // TODO: Borrar
        }
        LOGGER.info(validateHierarchy+">"+seType+": xsd correcta");

        // mover a registering

        sendCommand("set "+se+" parent=(get "+se+" attrib=seParent) seParent=", conversationId).getContent();

        return se;
    }

    public String start(String seId, ConcurrentHashMap<String, String> attributes, String conversationId) throws InterruptedException, FIPAException {
        LOGGER.entry(seId, attributes);

        StringBuilder command = new StringBuilder("sestart "+seId);

        if (attributes!=null)
            attributes.entrySet().stream().forEach(entry -> command.append(" " + entry.getKey() + "=" + entry.getValue()));

        return LOGGER.exit(sendCommand(command.toString(), conversationId).getContent());
    }

    public ACLMessage sendCommand(String cmd, String conversationId) throws FIPAException, InterruptedException {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType("sa");
        dfd.addServices(sd);
        String mwm;

        while (true) {
            DFAgentDescription[] result = DFService.search(this,dfd);

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

        this.send(msg);

        ACLMessage reply = this.blockingReceive();

        LOGGER.info((cmd.startsWith("validate"))?"xsd: "+reply.getContent(): cmd+" > "+reply.getContent());

        return LOGGER.exit(reply);
    }

}
