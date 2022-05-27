package es.ehu.domain.manufacturing.utilities;

import es.ehu.platform.utilities.XMLReader;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Planner extends Agent {
    private MessageTemplate template;
    private String itemfinishtime=null;
    private static final long serialVersionUID = 1L;
    static final Logger LOGGER = LogManager.getLogger(Planner.class.getName()) ;
    private int chatID = 0;
    private ArrayList<String> item_ft_list=new ArrayList<String>();
    private ArrayList<String> batch_ft_list=new ArrayList<String>();
    public String control="automatic";
    private Agent myAgent=this;
    private MessageTemplate template1=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
            MessageTemplate.MatchOntology("man/auto"));
    private int TMSTMP_cnt=0;

    protected void setup() {
        LOGGER.entry();
        LOGGER.warn("warning output sample");
        try {
            Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
            LOGGER.debug("Añadida tarea de apagado...");
        } catch (Throwable t) {
            LOGGER.debug(" *** Error: No se ha podido añadir tarea de apagado");
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

                //User interface. The operator will use it for the register of manufacturing applications.
                String cmd = "";
                String api = "Planner Agent local commands:\n"
                        + "register > Register a Manufacturing Plan of your desire\n"
                        + "demo > Register several instances of a Manufacturing Plan of your desire for testing with large number of agents\n"
                        + "toggle > Toggles between automatic or manual error management\n"
                        + "errorlist > Lists registered errors\n"
                        + "ping > Checks if an agent is online\n"
                        + "checkstate > Checks asset state\n"
                        + "setstate > Makes an agent change its state\n"
                        + "relationship > Gives de relationship between batch agent and machine agent\n"
                        + "help > SystemModelAgent commands summary\n"
                        + "exit > Shut down Planner Agent\n\n";
                System.out.print(api);
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
                        for (int i=0; i<cmds.length;i++) {
                            cmds[i] = cmds[i].trim();
                            if (cmds[i].equals("register")) {
                                registerPredefined(cmds[i]);
                                if (cmds.length > 1) System.out.print(" < " + cmds[i]);
                                System.out.print("\n\n");
                            }else if (cmds[i].equals("demo")) {
                                demoPredefined(cmds[i]);
                                if (cmds.length > 1) System.out.print(" < " + cmds[i]);
                                System.out.print("\n\n");
                            }else if(cmds[i].equals("toggle")){
                                togglemode();  //cambia el modo en el D&D
                                System.out.println("Changed to "+control+" mode.");
                            }else if(cmds[i].equals("errorlist")){
                                PrintErrorList();  //obtiene los errores registrados por el QoS
                            }else if(cmds[i].equals("relationship")){
                                AskRelationship(); //Obtiene la relacion entre batch y maquina
                            }else if (cmds[i].equals("setstate")) {
                                if(control.equals("manual")){
                                    changestate();
                                }else{
                                    LOGGER.error("Invalid command on automatic mode.\n");
                                }
                            }else if(cmds[i].equals("ping")){
                                PingAgent();
                            }else if(cmds[i].equals("checkstate")) {
                                checkasset();
                            }else if (cmds[i].equals("exit")) {
                                cmd = "exit";
                                break;
                            }
                            else {
                                ACLMessage reply = sendCommand(cmds[i], conversationId);
                                if (reply!=null) {
                                    if (cmds.length>1) System.out.print(" < "+cmds[i]);
                                    System.out.print("\n");
                                }
                            }
                            System.out.print("\n\n");
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

        public void checkasset(){
            Scanner in = new Scanner(System.in);
            System.out.println("Define to which GW agents you want to ask their asset state: ");
            System.out.print("GW agents: ");
            String cmd = in.nextLine();
            String[] cmds = cmd.split(";");
            for(int i=0;i<cmds.length;i++){
                if (cmds[i].contains("ControlGatewayCont")) {
                    int found = SearchAgent(cmds[i]);
                    if (found != 1) {
                        LOGGER.error("Multiple or no agents found for provided name. Operation cancelled.");
                    } else {
                        LOGGER.info("Agent found by AMS.");
                        sendACL(ACLMessage.REQUEST, cmds[i], "check_asset", "How are you feeling PLC?");
                        ACLMessage state = blockingReceive(MessageTemplate.MatchOntology("asset_state"), 500);
                        if (state != null) {
                            LOGGER.info("Asset with GW "+cmds[i]+ " replied: ");
                            System.out.println(state.getContent());
                        } else {
                            LOGGER.error("No answer from asset");
                        }
                    }
                } else {
                    LOGGER.error("User entered a invalid agent. Repeat operation.\n");
                }
            }

        }
        public void changestate(){
            Scanner in = new Scanner(System.in);
            System.out.println("Define the agent to change his state: ");
            System.out.print("Agent: ");
            String cmd = in.nextLine();
            int found = SearchAgent(cmd);
            if (found != 1) {
                LOGGER.error("Multiple or no agents found for provided name "+cmd);
            }else{
                if(cmd.contains("machine")){
                    Scanner in2 = new Scanner(System.in);
                    System.out.println("Define the state (idle, running): ");
                    System.out.print("State: ");
                    String cmd2 = in2.nextLine();
                    if(cmd2.equals("idle")||cmd2.equals("running")){
                        sendACL(16, cmd, "control", "setstate "+cmd2);
                    }else{
                        LOGGER.error("User entered an invalid state");
                    }
                }else if(cmd.contains("batchagent")||cmd.contains("orderagent")||cmd.contains("mplanagent")){
                    Scanner in2 = new Scanner(System.in);
                    System.out.println("Define the state (tracking, running): ");
                    System.out.print("State: ");
                    String cmd2 = in2.nextLine();
                    if(cmd2.equals("tracking")||cmd2.equals("running")){
                        sendACL(16, cmd, "control", "setstate "+cmd2);
                    }else{
                        LOGGER.error("User entered an invalid state");
                    }
                }else{
                    LOGGER.error("User entered an invalid agent");
                }
            }
        }

        public void registerPredefined(String cmd){
            String conversationId = myAgent.getLocalName() + "_" + chatID++;
            //Definition of the HashMaps
            ConcurrentHashMap<String, String> attributes = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, String> restrictionList = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, ConcurrentHashMap<String, String>> restrictionLists = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();
            ConcurrentHashMap<String, String> serviceList = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, ConcurrentHashMap<String, String>> serviceLists = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();
            ConcurrentHashMap<String, String> agentAttributes = new ConcurrentHashMap<String, String>();
            String itemList = "";
            Integer index;

            agentAttributes.put("seClass", "es.ehu.domain.manufacturing.agents.MPlanAgent");
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
            try {
                String command="get * category=pNodeAgent";
                ACLMessage available_pnodes = sendCommand(command, conversationId);
                String[] pnodes=new String[1];
                if(available_pnodes!=null){
                    if(available_pnodes.getContent().contains(",")){
                        pnodes=available_pnodes.getContent().split(",");
                    }else{
                        pnodes[0]=available_pnodes.getContent();
                    }
                    if(pnodes.length<Integer.parseInt(redundancy)){
//                        Scanner in2 = new Scanner(System.in);
                        LOGGER.warn("The indicated redundancy is superior to node availability: "+redundancy+">"+pnodes.length+". Use maximum redundancy possible instead?");
                        System.out.print("Y/N: ");
                        String confirmation = in.nextLine();
                        if(confirmation.equals("Y")){
                            String new_redundancy= String.valueOf(pnodes.length);
                            redundancy= new_redundancy;
                        }else if(confirmation.equals("N")){
                            System.out.println("Repeat action please");
                            return;
                        }else{
                            System.out.println("Not valid command. Repeat action please");
                            return;
                        }
                    }
                }

            } catch (FIPAException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!redundancy.equals(""))
                agentAttributes.put("redundancy", redundancy);

            String appPath="classes/resources/AppInstances/";
            String file="";
            System.out.println("\nPlease, introduce the name of the XML File you want to register.");
            System.out.print("File: ");
            file = in.nextLine();
            System.out.println();
            sendACL(ACLMessage.INFORM,"D&D","plan_id",file); //se avisa al D&D de que plan queremos ejecutar
            String uri=appPath+file;
            XMLReader fileReader = new XMLReader();
            ArrayList<ArrayList<ArrayList<String>>> xmlelements = null;
            ArrayList<String> batchlist =new ArrayList<String>();
            ArrayList<String> orderlist =new ArrayList<String>();
            ArrayList<String> mplanlist =new ArrayList<String>();

            try {
                xmlelements = fileReader.readFile(uri);
            } catch (Exception e) {
                LOGGER.error("ERROR GETTING FILE IN PLANNER");
                System.out.println("\nSorry, but you have not introduce the right file name, repeat the action please.\n");
                return;
            }

            // We use MPlanInterpreter to obtain all information
            String planName = file.substring(0, file.length()-4); //saves the plan´s ID to be assigned later in the name attribute
            xmlelements = MPlanInterpreter.getManEntities(myAgent, xmlelements, planName);
            if(xmlelements!=null){
                //Variable initialization at their first levels
                ArrayList<String> parentIdList = new ArrayList<>();
                parentIdList.add(0,"system");
                String parentId = "";
                String seId = "";



                // TODO mas adelante mirar lo de las restricciones --> Si no hay que escribirlas en el SystemModelAgent buscar una solucion
                //restrictionList.put("HostedElements", "id55");
                //restrictionLists.put("pNodeAgent", restrictionList);

                //For structure is used to register all the elements
                for (int i = 0; i < xmlelements.size(); i++) {

                    //First the attributes are collected
                    attributes.clear();

                    String attrName = xmlelements.get(i).get(0).get(0);
                    if (attrName.equals("batch")||attrName.equals("order")||attrName.equals("mPlan")){
                        if(attrName.equals("mPlan")){
                            //attributes.put("reference",xmlelements.get(i).get(2).get(2));
                            attributes.put("name",xmlelements.get(i).get(3).get(0)); //buscamos y añadimos los atributos para el agente mplan
                            mplanlist.add(xmlelements.get(i).get(3).get(0));
                        }
                        if(attrName.equals("order")){ //buscamos y añadimos los atributos para el agente order
                            attributes.put("reference",xmlelements.get(i).get(3).get(0));
                            orderlist.add(xmlelements.get(i).get(3).get(0));

                        }
                        int l=0;
                        if(attrName.equals("batch")){ //buscamos y añadimos los atributos para el agente batch
                            batchlist.add(xmlelements.get(i).get(3).get(2));

                            for(int j=i;j<xmlelements.size();j++){
                                if(xmlelements.get(j).get(0).get(0).equals("PlannedItem")){
                                    index=xmlelements.get(j).get(2).indexOf("item_ID");
                                    itemList= itemList + xmlelements.get(j).get(3).get(index)+","; //concatenamos cada item ID separandolo con comas. Queda coma al final del último item pero no parece dar problemas al registrar.

                                    for(int n=j+1;n<xmlelements.size()&&!xmlelements.get(n).get(0).get(0).contains("PlannedItem");n++) {
                                        if(xmlelements.get(n).get(0).get(0).contains("Operation")) {
//                                            itemfinishtime = xmlelements.get(j).get(3).get(index) + "/" + xmlelements.get(n).get(3).get(2);
                                        }
                                    }

                                }attributes.put("numberOfItems",itemList);
                            }
                            attributes.put("reference",xmlelements.get(i).get(3).get(2));
                            attributes.put("refProductID",xmlelements.get(i).get(3).get(1));
                        }
                        l++;
                        //The parent Id is always the last element Id of the upper level
                        parentId = parentIdList.get(Integer.parseInt(xmlelements.get(i).get(1).get(0)) - 1);

                        String commandSeReg = "seregister seParent=" + parentId + " parent=concepts seType=" + xmlelements.get(i).get(0).get(0);
                        for (Map.Entry<String, String> entry : attributes.entrySet()) {
                            commandSeReg = commandSeReg + " " + entry.getKey() + "=" + entry.getValue();
                        }

                        if (!restrictionLists.isEmpty()) {
                            commandSeReg = commandSeReg + " & " + restrictionLists.keys().nextElement();
                            for (Map.Entry<String, ConcurrentHashMap<String, String>> restriction : restrictionLists.entrySet()) {
                                for (Map.Entry<String, String> entry : restriction.getValue().entrySet()) {
                                    //Aquí se obtienen las restricciones asociadas a ese tipo de recurso
                                    commandSeReg = commandSeReg + " " + entry.getKey() + "=" + entry.getValue();
                                }
                            }
                        }


                        try {
                            ACLMessage reply = sendCommand(commandSeReg, conversationId);
                            seId = reply.getContent();
                        } catch (FIPAException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //Finally, the new seId is added to the parent Id list
                        parentIdList.add(Integer.parseInt(xmlelements.get(i).get(1).get(0)), seId);
                    }
                }

                //After the register, the element to be validated and started will be the second on the list (the level 1 element)
                String app = parentIdList.get(1);

                String commandIValid = "ivalidate " + app;
                try {
                    sendCommand(commandIValid, conversationId);
                } catch (FIPAException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                //Start
                try {
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    start(app, agentAttributes, conversationId);

                    get_timestamp(parentIdList,timestamp);

                } catch (Exception e) {
                    LOGGER.error("ERROR IN start METHOD OF PLANNER: Sending command to systemModelAgent");
                    e.printStackTrace();
                }

                String BatchToFind;
                String OrderToFind;
                template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                        MessageTemplate.MatchOntology("Ftime_order_ask"));
                while(orderlist.size()>0){
                    ACLMessage order_asking=blockingReceive(template);
                    ACLMessage reply_to_order=new ACLMessage(ACLMessage.INFORM);
                    AID orderAID = order_asking.getSender();
                    OrderToFind=order_asking.getContent();
                    batch_ft_list=MPlanInterpreter.getBatchFT(myAgent, xmlelements, planName,OrderToFind); //devuelve ve el finish time de la ultima operación de cada batch
                    String each_batch_time=OrderToFind+"&";
                    for(int i=0;i<batch_ft_list.size();i++){
                        each_batch_time=each_batch_time+batch_ft_list.get(i);
                        if(i+1!=batch_ft_list.size()){
                            each_batch_time=each_batch_time+"_";
                        }
                    }
                    reply_to_order.setContent(each_batch_time);
                    reply_to_order.addReceiver(orderAID);
                    reply_to_order.setOntology("Ftime_order_ask");
                    send(reply_to_order);

                    for(int j=0;j<orderlist.size();j++){
                        if(orderlist.get(j).equals(OrderToFind)){
                            orderlist.remove(j);
                            j--;
                        }
                    }
                }

                template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                        MessageTemplate.MatchOntology("Ftime_batch_ask"));

                while(batchlist.size()>0){//Se queda a la espera de recibir las consultas de finish time de cada batch
                    ACLMessage batch_asking=blockingReceive(template);
                    ACLMessage reply_to_batch=new ACLMessage(ACLMessage.INFORM);
                    AID batchAID = batch_asking.getSender();
                    BatchToFind=batch_asking.getContent();
                    item_ft_list=MPlanInterpreter.getItemFT(myAgent, xmlelements, planName,BatchToFind); //devuelve el finish time de la última operacion de cada item
                    String each_operation_time=BatchToFind+"&";
                    for(int i=0;i<item_ft_list.size();i++){
                        each_operation_time=each_operation_time+item_ft_list.get(i);
                        if(i+1!=item_ft_list.size()){
                            each_operation_time=each_operation_time+"_";
                        }
                    }
                    reply_to_batch.setContent(each_operation_time);
                    reply_to_batch.addReceiver(batchAID);
                    reply_to_batch.setOntology("Ftime_batch_ask");
                    send(reply_to_batch);

                    for(int j=0;j<batchlist.size();j++){  //buscamos y eliminamos el batch del listado.
                        if(batchlist.get(j).equals(BatchToFind)){
                            batchlist.remove(j);
                            j--;
                        }
                    }
                }
                //The plan has been successfully read
                //Next step is to interpret the set of masterRecipes to compose the hierarchy of applications
            }else{
                LOGGER.error("ERROR. Could not create plan. One or more machines are not online.");
            }

//            batch_ft_list=MPlanInterpreter.getBatchFT(myAgent, xmlelements, planName,"11"); //para debug
        }

        public void demoPredefined(String cmd) {

            String conversationId = myAgent.getLocalName() + "_" + chatID++;
            //Definition of the HashMaps
            ConcurrentHashMap<String, String> attributes = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, String> restrictionList = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, ConcurrentHashMap<String, String>> restrictionLists = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();
            ConcurrentHashMap<String, String> serviceList = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, ConcurrentHashMap<String, String>> serviceLists = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();
            ConcurrentHashMap<String, String> agentAttributes = new ConcurrentHashMap<String, String>();
            agentAttributes.put("redundancy", "1");
            String itemList = "";
            Integer index;

            agentAttributes.put("seClass", "es.ehu.domain.manufacturing.agents.MPlanAgent");

            // TODO Para pruebas con replicas
            Scanner in = new Scanner(System.in);
            System.out.println("Please, introduce the number of applications you want to register (3 components per application).");
            System.out.print("Instances: ");
            String instances = in.nextLine();

            String appPath="classes/resources/AppInstances/";
            String file="";
            System.out.println("\nPlease, introduce the name of the XML File you want to register.");
            System.out.print("File: ");
            file = in.nextLine();
            System.out.println();
            String uri=appPath+file;
            XMLReader fileReader = new XMLReader();
            ArrayList<ArrayList<ArrayList<String>>> xmlelements = null;
            ArrayList<String> batchlist =new ArrayList<String>();
            ArrayList<String> orderlist =new ArrayList<String>();
            ArrayList<String> mplanlist =new ArrayList<String>();

            try {
                xmlelements = fileReader.readFile(uri);
            } catch (Exception e) {
                LOGGER.error("ERROR GETTING FILE IN PLANNER");
                System.out.println("\nSorry, but you have not introduce the right file name, repeat the action please.\n");
                return;
            }

            // We use MPlanInterpreter tu obtain all information
            String planName = file.substring(0, file.length()-4); //saves the plan´s ID to be assigned later in the name attribute
            xmlelements = MPlanInterpreter.getManEntities(myAgent, xmlelements, planName);

            //The plan has been successfully read

            for (int q=1; q<=Integer.parseInt(instances);q++) {

                //Variable initialization at their first levels
                ArrayList<String> parentIdList = new ArrayList<>();
                parentIdList.add(0,"system");
                String parentId = "";
                String seId = "";



                // TODO mas adelante mirar lo de las restricciones --> Si no hay que escribirlas en el SystemModelAgent buscar una solucion
                //restrictionList.put("HostedElements", "id55");
                //restrictionLists.put("pNodeAgent", restrictionList);

                //For structure is used to register all the elements
                for (int i = 0; i < xmlelements.size(); i++) {

                    //First the attributes are collected
                    attributes.clear();

                    String attrName = xmlelements.get(i).get(0).get(0);
                    if (attrName.equals("batch")||attrName.equals("order")||attrName.equals("mPlan")){
                        if(attrName.equals("mPlan")){
                            //attributes.put("reference",xmlelements.get(i).get(2).get(2));
                            attributes.put("name",xmlelements.get(i).get(3).get(0)); //buscamos y añadimos los atributos para el agente mplan
                            mplanlist.add(xmlelements.get(i).get(3).get(0));
                        }
                        if(attrName.equals("order")){ //buscamos y añadimos los atributos para el agente order
                            attributes.put("reference",xmlelements.get(i).get(3).get(0));
                            orderlist.add(xmlelements.get(i).get(3).get(0));

                        }
                        int l=0;
                        if(attrName.equals("batch")){ //buscamos y añadimos los atributos para el agente batch
                            batchlist.add(xmlelements.get(i).get(3).get(2));

                            for(int j=i;j<xmlelements.size();j++){
                                if(xmlelements.get(j).get(0).get(0).equals("PlannedItem")){
                                    index=xmlelements.get(j).get(2).indexOf("item_ID");
                                    itemList= itemList + xmlelements.get(j).get(3).get(index)+","; //concatenamos cada item ID separandolo con comas. Queda coma al final del último item pero no parece dar problemas al registrar.

                                    for(int n=j+1;n<xmlelements.size()&&!xmlelements.get(n).get(0).get(0).contains("PlannedItem");n++) {
                                        if(xmlelements.get(n).get(0).get(0).contains("Operation")) {
//                                            itemfinishtime = xmlelements.get(j).get(3).get(index) + "/" + xmlelements.get(n).get(3).get(2);
                                        }
                                    }

                                }attributes.put("numberOfItems",itemList);
                            }
                            attributes.put("reference",xmlelements.get(i).get(3).get(2));
                            attributes.put("refProductID",xmlelements.get(i).get(3).get(1));
                        }
                        l++;
                        //The parent Id is always the last element Id of the upper level
                        parentId = parentIdList.get(Integer.parseInt(xmlelements.get(i).get(1).get(0)) - 1);

                        String commandSeReg = "seregister seParent=" + parentId + " parent=concepts seType=" + xmlelements.get(i).get(0).get(0);
                        for (Map.Entry<String, String> entry : attributes.entrySet()) {
                            commandSeReg = commandSeReg + " " + entry.getKey() + "=" + entry.getValue();
                        }

                        if (!restrictionLists.isEmpty()) {
                            commandSeReg = commandSeReg + " & " + restrictionLists.keys().nextElement();
                            for (Map.Entry<String, ConcurrentHashMap<String, String>> restriction : restrictionLists.entrySet()) {
                                for (Map.Entry<String, String> entry : restriction.getValue().entrySet()) {
                                    //Aquí se obtienen las restricciones asociadas a ese tipo de recurso
                                    commandSeReg = commandSeReg + " " + entry.getKey() + "=" + entry.getValue();
                                }
                            }
                        }


                        try {
                            ACLMessage reply = sendCommand(commandSeReg, conversationId);
                            seId = reply.getContent();
                        } catch (FIPAException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //Finally, the new seId is added to the parent Id list
                        parentIdList.add(Integer.parseInt(xmlelements.get(i).get(1).get(0)), seId);
                    }
                }

                //After the register, the element to be validated and started will be the second on the list (the level 1 element)
                String app = parentIdList.get(1);

                String commandIValid = "ivalidate " + app;
                try {
                    sendCommand(commandIValid, conversationId);
                } catch (FIPAException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                //Start
                try {

                    start(app, agentAttributes, conversationId);
                } catch (Exception e) {
                    LOGGER.error("ERROR IN start METHOD OF PLANNER: Sending command to systemModelAgent");
                    e.printStackTrace();
                }

                template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                        MessageTemplate.MatchOntology("Ftime_batch_ask"));

                String BatchToFind;
                String OrderToFind;

                while(batchlist.size()>0){//Se queda a la espera de recibir las consultas de finish time de cada batch

                    ACLMessage batch_asking=blockingReceive(template);
                    ACLMessage reply_to_batch=new ACLMessage(ACLMessage.INFORM);
                    AID batchAID = batch_asking.getSender();
                    BatchToFind=batch_asking.getContent();
                    item_ft_list=MPlanInterpreter.getItemFT(myAgent, xmlelements, planName,BatchToFind); //devuelve el finish time de la última operacion de cada item
                    String each_operation_time=BatchToFind+"&";
                    for(int i=0;i<item_ft_list.size();i++){
                        each_operation_time=each_operation_time+item_ft_list.get(i);
                        if(i+1!=item_ft_list.size()){
                            each_operation_time=each_operation_time+"_";
                        }
                    }
                    reply_to_batch.setContent(each_operation_time);
                    reply_to_batch.addReceiver(batchAID);
                    reply_to_batch.setOntology("Ftime_batch_ask");
                    send(reply_to_batch);

                    for(int j=0;j<batchlist.size();j++){
                        if(batchlist.get(j).equals(BatchToFind)){
                            batchlist.remove(j);
                        }
                    }
                }
//            batch_ft_list=MPlanInterpreter.getBatchFT(myAgent, xmlelements, planName,"11"); //para debug
                template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                        MessageTemplate.MatchOntology("Ftime_order_ask"));

                while(orderlist.size()>0){
                    ACLMessage order_asking=blockingReceive(template);
                    ACLMessage reply_to_order=new ACLMessage(ACLMessage.INFORM);
                    AID orderAID = order_asking.getSender();
                    OrderToFind=order_asking.getContent();
                    batch_ft_list=MPlanInterpreter.getBatchFT(myAgent, xmlelements, planName,OrderToFind); //devuelve ve el finish time de la ultima operación de cada batch
                    String each_batch_time=OrderToFind+"&";
                    for(int i=0;i<batch_ft_list.size();i++){
                        each_batch_time=each_batch_time+batch_ft_list.get(i);
                        if(i+1!=batch_ft_list.size()){
                            each_batch_time=each_batch_time+"_";
                        }
                    }
                    reply_to_order.setContent(each_batch_time);
                    reply_to_order.addReceiver(orderAID);
                    reply_to_order.setOntology("Ftime_order_ask");
                    send(reply_to_order);

                    for(int j=0;j<orderlist.size();j++){
                        if(orderlist.get(j).equals(OrderToFind)){
                            orderlist.remove(j);
                        }
                    }
                }
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

    public String start(String seId, ConcurrentHashMap<String, String> attributes, String conversationId) throws InterruptedException, FIPAException {
        LOGGER.entry(seId, attributes);

        StringBuilder command = new StringBuilder("appstart "+seId);

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

        ACLMessage reply = this.blockingReceive(
                MessageTemplate.and(
                        MessageTemplate.MatchInReplyTo(msg.getReplyWith()),
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM))
                , 5000);
        if(!cmd.contains("get *")){
            LOGGER.info((cmd.startsWith("validate"))?"xsd: "+reply.getContent(): cmd+" > "+reply.getContent());
        }



        return LOGGER.exit(reply);
    }
    private void sendACL(int performative,String receiver,String ontology,String content){ //Funcion estándar de envío de mensajes
        AID receiverAID=new AID(receiver,false); //pasamos la máquina a estado idle
        ACLMessage msg=new ACLMessage(performative);
        msg.addReceiver(receiverAID);
        msg.setOntology(ontology);
        msg.setContent(content);
        send(msg);
    }

    private void PingAgent (){  //checkea el estado de los agentes de aplicación, recurso y gateway


        Scanner in = new Scanner(System.in);
        System.out.println("Define which agents you want to ping: ");
        System.out.print("Agents: ");
        String cmd = in.nextLine();
        String[] cmds = cmd.split(";");
        MessageTemplate pingtemplate=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("ping"));
        for(int i=0;i< cmds.length;i++){
            int found=SearchAgent(cmds[i]);
            if(found!=1){
                LOGGER.error("Multiple or no agents found for provided name "+cmds[i]);
            }else {
                if (cmds[i].contains("machine")||cmds[i].contains("batch")||cmds[i].contains("order")||cmds[i].contains("mplan")||cmds[i].contains("ControlGatewayCont")){
                    AID Agent_to_ping_ID = new AID(cmds[i], false);
                    ACLMessage ping = new ACLMessage(ACLMessage.REQUEST);
                    ping.setOntology("ping");
                    ping.addReceiver(Agent_to_ping_ID);
                    ping.setContent("");
                    send(ping);
                    ACLMessage echo = blockingReceive(pingtemplate, 500);
                    if (echo != null) {
                        LOGGER.info(cmds[i] + " answered on time.");
                    } else {
                        LOGGER.error(cmds[i] + " did not answer on time.");
                    }
                }else{
                    LOGGER.error(cmds[i] + " is an invalid agent to ping");
                }
            }
        }
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
        if(agents!=null) {
            for (int i = 0; i < agents.length; i++) {
                AID agentID = agents[i].getName();
                String agent_to_check = agentID.getLocalName();
//            System.out.println(agent_to_check);
                if (agent_to_check.contains(agent)) {
                    found++;
                }
            }
        }
        return found;
    }
    private void togglemode(){
        if(control.equals("automatic")){
            sendACL(ACLMessage.REQUEST,"D&D","man/auto","manual");
            if(blockingReceive(template1,250)!=null){
                control="manual";
            }else{
                LOGGER.error("D&D did not answer. Keeping last mode "+ control);
                control="automatic";
            }
        } else if(control.equals("manual")){
            sendACL(ACLMessage.REQUEST,"D&D","man/auto","automatic");
            if(blockingReceive(template1,250)!=null){
                control="automatic";
            }else{
                LOGGER.error("D&D did not answer. Keeping last mode "+ control);
                control="manual";
            }
        }
    }
    private void PrintErrorList(){
        MessageTemplate errtemplate=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("command"));
        sendACL(ACLMessage.REQUEST, "QoSManagerAgent", "command", "errorlist");
        ACLMessage reply = blockingReceive(errtemplate, 500);
        if(reply!=null) {
            if (!reply.getContent().equals("")) {
                String args[];
                String errors[] = reply.getContent().split("/err/");
                for (int j = 0; j < errors.length; j++) {
                    System.out.println("ERROR " + j);
                    args = errors[j].split("/inf/");
                    for (int k = 0; k < args.length; k++) {
                        System.out.print("    ");
                        System.out.println(args[k]);
                    }
                }
            } else {
                System.out.println("No errors");
            }
        }else{
            System.out.println("QoS Manager did not answer on time.");
        }
    }

    public void get_timestamp(ArrayList<String> parents,Timestamp timestamp){

        String id="";
        String category="";
        String appId="";

        for (int k=0;k<parents.size();k++) {

            if(!parents.get(k).equals("system")){
                id=parents.get(k);
                category = id.substring(0,id.length()-3);
                if(parents.get(k).contains("mplan")){
                    appId = "app"+id.substring(id.length()-3);

                    //Mando un mensaje para añadir el tiempo en el que se solicita la creación de la aplicación (solo se envía una vez)
//                    String contenido = appId+",planner"+",DeploymentRequestTime,"+String.valueOf(timestamp.getTime());
                    String contenido = "app, "+",DeploymentRequestTime,"+String.valueOf(timestamp.getTime());
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(new AID("ControlContainer-GWDataAcq", AID.ISLOCALNAME)); //Confirma el nombre del control container antes de nada
                    msg.setOntology("timestamp");
                    msg.setConversationId("");
                    msg.setContent(contenido);
//                    myAgent.send(msg);

                    String contenido1 = id+",planner"+",DeploymentRequestTime,"+String.valueOf(timestamp.getTime());
                    ACLMessage msg3 = new ACLMessage(ACLMessage.INFORM);
                    msg3.addReceiver(new AID("ControlContainer-GWDataAcq", AID.ISLOCALNAME));
                    msg3.setOntology("timestamp");
                    msg3.setConversationId("");
                    msg3.setContent(contenido1);
//                    myAgent.send(msg3);
                }
                //Por último, mando un otro mensaje para añadir el tiempo en el que se solicita la creación del componente

            }

        }
    }

    private void AskRelationship(){
        MessageTemplate reltemplate=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("askrelationship"));
        Scanner in2 = new Scanner(System.in);
        System.out.println("Define a batch or a machine agent: ");
        System.out.print("Agent: ");
        String cmd2 = in2.nextLine();
        if(cmd2.contains("machine")||cmd2.contains("batchagent")){
            int found = SearchAgent(cmd2);
            if (found != 1) {
                LOGGER.error("Multiple or no resource agents found for provided name "+cmd2);
            } else {
                sendACL(16, "QoSManagerAgent","askrelationship" , cmd2);
                ACLMessage reply = blockingReceive(reltemplate, 500);
                if(reply!=null) {
                    System.out.print(cmd2 +" is assigned to "+reply.getContent());
                }else{
                    System.out.println("QoS Manager did not answer on time.");
                }
            }
        }else{
            System.out.println("Error. Not a valid agent.");
        }

    }

}
