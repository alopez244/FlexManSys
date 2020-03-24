package es.ehu.domain.manufacturing;

import es.ehu.platform.utilities.MasReconAgent;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

                //Se comprueba que el SystemModelAgent se encuentra activo y obtiene su LocalName.

                mra.searchMwm();

                //Interfaz de usuario. El operario la usará para registrar aplicaciones

                String cmd = "";
                String api = "Planner Agent local commands:\n"
                        + "register_MP1 > Loads Manufacturing Plan 1 application\n"
                        + "register_MP2 > Loads Manufacturing Plan 2 application\n"
                        + "register_MP3 > Loads Manufacturing Plan 3 application\n"
                        + "help > SystemModelAgent commands summary\n"
                        + "exit > Shut down Planner Agent\n";
                System.out.print(api);
                LOGGER.info(api);

                //Bucle de decisión asociado a la interfaz de usuario.
                //Este bucle estará en ejecución hasta que el operario utilice el comando exit.

                while (!cmd.equals("exit")) {

                    //Se solicita un comando al operario y se recoge el resultado

                    Scanner in = new Scanner(System.in);
                    System.out.print("\ncmd: ");
                    cmd = in.nextLine();

                    //Se vacia la cola de mensajes recibidos por el Planner Agent

                    ACLMessage flush = receive();
                    while (flush!=null) {
                        System.out.println(flush.getInReplyTo()+" : "+ flush.getContent());
                        flush = receive();
                    }

                    //Se comprueba si el campo cmd está vacío.
                    //En caso contrario, se divide su contenido con el separador ";".
                    //Cada nuevo String se guarda en el array "cmds".

                    if (cmd.length()>0) {
                        String [] cmds = cmd.split(";");

                        //Se envía un mensaje ACL por cada String del array "cmds".

                        for (int i=0; i<cmds.length;i++){
                            cmds[i] = cmds[i].trim();
                            if (cmds[i].startsWith("register")) registerPredefined(cmds[i]);
                            else if (cmds[i].equals("exit")) {
                                cmd = "exit";
                                break;
                            }
                            else {
                                ACLMessage reply = mra.sendCommand(cmds[i]);
                                if (reply!=null) {
                                    System.out.print(reply.getInReplyTo()+": "+reply.getContent());
                                    if (cmds.length>1) System.out.print(" < "+cmds[i]);
                                    System.out.println();
                                }
                            }
                        }
                    }
                }

                System.exit(0); //Agur

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

            //En este método se recogen las aplicaciones predefinidas
            //Primero se define el HashMap

            ConcurrentHashMap<String, String> attributes = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, String> restrictionList = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, ConcurrentHashMap<String, String>> restrictionLists = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();
            ConcurrentHashMap<String, String> serviceList = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, ConcurrentHashMap<String, String>> serviceLists = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();

            //Mediante un if-else se accede a cada una de las aplicaciones

            if (cmd.equals("register_MP1")) {

                // Registro del elemento MANUFACTURING PLAN

                attributes.clear();
                attributes.put("name","MP1");
                String mp1 = mra.seRegister("manufacturingPlan", "system", attributes, restrictionLists);

                // Registro del elemento ORDER

                attributes.clear();
                attributes.put("reference","O1_MP1");
                attributes.put("customer","Marga");
                String o1_1 = mra.seRegister("order", mp1, attributes, restrictionLists);

                // Registro del elemento BATCH

                attributes.clear();
                attributes.put("reference","B1_O1_MP1");
                attributes.put("numberOfItems", "6");
                attributes.put("refProductID", "P_01");
                String b1_1_1 = mra.seRegister("batch", o1_1, attributes, restrictionLists);

                //Validación de la aplicación MANUFACTURING PLAN

                mra.iValidate(mp1);

                this.finished=true;
                LOGGER.info("register process finished.");

            } else if (cmd.equals("register_MP2")) {

                // Registro del elemento MANUFACTURING PLAN

                attributes.clear();
                attributes.put("name","MP2");
                String mp2 = mra.seRegister("manufacturingPlan", "system", attributes, restrictionLists);

                // Registro del elemento ORDER

                attributes.clear();
                attributes.put("reference","O1_MP2");
                attributes.put("customer","Oskar");
                String o1_2 = mra.seRegister("order", mp2, attributes, restrictionLists);

                // Registro del elemento BATCH 1

                attributes.clear();
                attributes.put("reference","B1_O1_MP2");
                attributes.put("numberOfItems", "2");
                attributes.put("refProductID", "P_01");
                String b1_1_2 = mra.seRegister("batch", o1_2, attributes, restrictionLists);

                // Registro del elemento BATCH 2

                attributes.clear();
                attributes.put("reference","B2_O1_MP2");
                attributes.put("numberOfItems", "4");
                attributes.put("refProductID", "P_03");
                String b2_1_2 = mra.seRegister("batch", o1_2, attributes, restrictionLists);

                //Validación de la aplicación MANUFACTURING PLAN

                mra.iValidate(mp2);

                this.finished=true;
                LOGGER.info("register process finished.");

            } else if (cmd.equals("register_MP3")) {

                // Registro del elemento MANUFACTURING PLAN

                attributes.clear();
                attributes.put("name","MP3");
                String mp3 = mra.seRegister("manufacturingPlan", "system", attributes, restrictionLists);

                // Registro del elemento ORDER 1

                attributes.clear();
                attributes.put("reference","O1_MP3");
                attributes.put("customer","Ekatiz");
                String o1_3 = mra.seRegister("order", mp3, attributes, restrictionLists);

                // Registro del elemento BATCH 1 (ORDER 1)

                attributes.clear();
                attributes.put("reference","B1_O1_MP3");
                attributes.put("numberOfItems", "3");
                attributes.put("refProductID", "P_02");
                String b1_1_3 = mra.seRegister("batch", o1_3, attributes, restrictionLists);

                // Registro del elemento ORDER 2

                attributes.clear();
                attributes.put("reference","O2_MP3");
                attributes.put("customer","Jon");
                String o2_3 = mra.seRegister("order", mp3, attributes, restrictionLists);

                // Registro del elemento BATCH 1 (ORDER 2)

                attributes.clear();
                attributes.put("reference","B1_O2_MP3");
                attributes.put("numberOfItems", "3");
                attributes.put("refProductID", "P_02");
                String b1_2_3 = mra.seRegister("batch", o2_3, attributes, restrictionLists);

                //Validación de la aplicación MANUFACTURING PLAN

                mra.iValidate(mp3);

                this.finished=true;
                LOGGER.info("register process finished.");

            }

        }

        private boolean finished = false;

        public boolean done() {
            LOGGER.entry();
            return LOGGER.exit(finished);
        }

    } // Fin del comportamiento

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
