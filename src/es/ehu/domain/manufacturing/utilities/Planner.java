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

            // We use MPlanInterpreter tu obtain all information
            xmlelements = MPlanInterpreter.getManEntities(myAgent, xmlelements);

            //The plan has been successfully read
            //Next step is to interpret the set of masterRecipes to compose the hierarchy of applications

            //Variable initialization at their first levels
            ArrayList<String> parentIdList = new ArrayList<>();
            parentIdList.add(0,"system");
            String parentId = "";
            String seId = "";

            String conversationId = myAgent.getLocalName() + "_" + chatID++;

            // TODO mas adelante mirar lo de las restricciones --> Si no hay que escribirlas en el SystemModelAgent buscar una solucion
            //restrictionList.put("refServID", "id55");
            //restrictionLists.put("pNodeAgent", restrictionList);

            //For structure is used to register all the elements
            for (int i = 0; i < xmlelements.size(); i++) {

                //First the attributes are collected
                attributes.clear();
                for (int j = 0; j < xmlelements.get(i).get(2).size(); j++) {
                    String attrName = xmlelements.get(i).get(2).get(j);
                    if ((attrName.equals("batchName")) || (attrName.equals("orderName")))
                        attributes.put("reference", xmlelements.get(i).get(3).get(j));
                    else if (attrName.equals("prodId"))
                        attributes.put("refProductID", xmlelements.get(i).get(3).get(j));
                    else
                        attributes.put(xmlelements.get(i).get(2).get(j), xmlelements.get(i).get(3).get(j));
                }

                //The parent Id is always the last element Id of the upper level
                parentId = parentIdList.get(Integer.parseInt(xmlelements.get(i).get(1).get(0)) - 1);

                String commandSeReg = "seregister seParent="+parentId+ " parent=concepts seType=" + xmlelements.get(i).get(0).get(0);
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    commandSeReg = commandSeReg+" "+entry.getKey()+"="+entry.getValue();
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

        ACLMessage reply = this.blockingReceive();

        LOGGER.info((cmd.startsWith("validate"))?"xsd: "+reply.getContent(): cmd+" > "+reply.getContent());

        return LOGGER.exit(reply);
    }

}
