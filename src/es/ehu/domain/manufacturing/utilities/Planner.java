package es.ehu.domain.manufacturing.utilities;

import es.ehu.platform.utilities.MasReconAgent;
import es.ehu.platform.utilities.XMLReader;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Planner extends Agent {

    private static final long serialVersionUID = 1L;
    static final Logger LOGGER = LogManager.getLogger(Planner.class.getName()) ;

    protected void setup() {
        LOGGER.entry();
        LOGGER.warn("warning output sample");
        try {
            Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
            LOGGER.debug("Añadida tarea de apagado...");
        } catch (Throwable t) {
            LOGGER.debug(" *** Error: No se ha podido añadir tarea de apagado");
        }
        addBehaviour(new Mra(this));
        LOGGER.exit();
    }

    class Mra extends SimpleBehaviour {

        private MasReconAgent mra = new MasReconAgent(myAgent);
        private static final long serialVersionUID = 6711046229173067015L;

        public Mra(Agent a) {
            super(a);
        }

        public void action() {

            LOGGER.entry();
            try {

                LOGGER.info("start.");

                //This method checks the SystemModelAgent is active. If so, retrieves its LocalName.
                mra.searchMwm();

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
                                ACLMessage reply = mra.sendCommand(cmds[i]);
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

        public void registerPredefined(String cmd) throws Exception {

            //Definition of the HashMaps
            ConcurrentHashMap<String, String> attributes = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, String> restrictionList = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, ConcurrentHashMap<String, String>> restrictionLists = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();
            ConcurrentHashMap<String, String> serviceList = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, ConcurrentHashMap<String, String>> serviceLists = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();

            String appPath="classes/resources/AppInstances/";
            String file="";
            Scanner in = new Scanner(System.in);
            System.out.println("Please, introduce the name of the XML File you want to register.");
            System.out.print("File: ");
            file = in.nextLine();
            System.out.println();
            String uri=appPath+file;
            XMLReader fileReader = new XMLReader();
            ArrayList<ArrayList<ArrayList<String>>> xmlelements = fileReader.readFile(uri);

            //Variable initialization at their first levels
            ArrayList<String> parentIdList = new ArrayList<String>();
            parentIdList.add(0,"system");
            String parentId = "";
            String seId = "";

            //For structure is used to register all the elements
            for (int i = 0; i < xmlelements.size(); i++){

                //First the attributes are collected
                attributes.clear();
                for (int j = 0; j < xmlelements.get(i).get(2).size(); j++){
                    attributes.put(xmlelements.get(i).get(2).get(j),xmlelements.get(i).get(3).get(j));
                }

                //The parent Id is always the last element Id of the upper level
                parentId = parentIdList.get(Integer.parseInt(xmlelements.get(i).get(1).get(0))-1);

                //Now the register is performed and the element name is obtained
                seId = mra.seRegister(xmlelements.get(i).get(0).get(0),parentId,attributes,restrictionLists);

                //Finally, the new seId is added to the parent Id list
                parentIdList.add(Integer.parseInt(xmlelements.get(i).get(1).get(0)),seId);
            }

            //After the register, the element to be validated and started will be the second on the list (the level 1 element)
            String app = parentIdList.get(1);

            //Validation
            mra.iValidate(app);

            //Start
            mra.start(app,null);
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
}
