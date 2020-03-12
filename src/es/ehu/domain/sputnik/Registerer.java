package es.ehu.domain.sputnik;

import es.ehu.platform.utilities.MasReconAgent;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

public class Registerer extends Agent {
    private static final long serialVersionUID = 1L;

    static final Logger LOGGER = LogManager.getLogger(Registerer.class.getName()) ;

    protected void setup() {
        LOGGER.entry();
        LOGGER.warn("warning output sample");
        try {
            Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
            LOGGER.debug("Añadida tarea de apagado...");
        } catch (Throwable t) {
            LOGGER.debug(" *** Error: No se ha podido aï¿½adir tarea de apagado");
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

                mra.searchMwm();

                ConcurrentHashMap<String, String> attributes = new ConcurrentHashMap<String, String>();
                ConcurrentHashMap<String, String> restrictionList = new ConcurrentHashMap<String, String>();
                ConcurrentHashMap<String, ConcurrentHashMap<String, String>> restrictionLists = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();
                ConcurrentHashMap<String, String> serviceList = new ConcurrentHashMap<String, String>();

                ConcurrentHashMap<String, ConcurrentHashMap<String, String>> serviceLists = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();

                // ************* NODE
                attributes.clear();
                attributes.put("refServID", "id55");
                attributes.put("sensor", "Pulse-cardio");
                attributes.put("room", "P3B66");
                mra.register("service","system", attributes);

                attributes.clear();
                attributes.put("refServID", "id56");
                attributes.put("sensor", "Pulse-cardio");
                attributes.put("room", "P3B67");
                mra.register("service","system", attributes);

                attributes.clear();
                attributes.put("name", "Pepe");
                restrictionList.clear();
                restrictionLists.clear();
                String as1 = mra.seRegister ("applicationSet", "system", attributes, restrictionLists);

                //APPLICATION 1
                attributes.clear();
                attributes.put("name", "Sputnik App");
                attributes.put("deadline", "10");
                restrictionLists.clear();
                String a1 = mra.seRegister ("application", as1 , attributes, restrictionLists);
                LOGGER.debug("a1"+a1);
                // COMPONENT 1
                attributes.clear();
                attributes.put("name", "Comp_1");
                attributes.put("activation", "periodic");
                attributes.put("period", "3.5");
                // ************* COMPONENT - restricción 1
                restrictionList.clear();
                restrictionList.put("refServID", "id56");
                restrictionLists.clear();
                restrictionLists.put("procNode", restrictionList);
                String c1 = mra.seRegister ("component", a1 , attributes, restrictionLists);
                // IMPLEMENTATION 1
                attributes.clear();
                attributes.put("name", "Impl_1");
                attributes.put("class", "es.ehu.sputnik.Comp1Agent");
                attributes.put("maxMemory", "35");
                restrictionLists.clear();
                String i1 = mra.seRegister ("implementation", c1 , attributes, restrictionLists);
                // COMPONENT 2
                attributes.clear();
                attributes.put("name", "Comp_2");
                attributes.put("activation", "onDemand");
                restrictionLists.clear();
                String c2 = mra.seRegister ("component", a1 , attributes, restrictionLists);
                // IMPLEMENTATION 2
                attributes.clear();
                attributes.put("name", "Impl_2");
                attributes.put("class", "es.ehu.sputnik.Comp2Agent");
                attributes.put("maxMemory", "25");
                restrictionLists.clear();
                String i2 = mra.seRegister ("implementation", c2 , attributes, restrictionLists);
                // COMPONENT 3
                attributes.clear();
                attributes.put("name", "Comp_3");
                attributes.put("activation", "onDemand");
                restrictionLists.clear();
                String c3 = mra.seRegister ("component", a1 , attributes, restrictionLists);
                // IMPLEMENTATION 3
                attributes.clear();
                attributes.put("name", "Impl_3");
                attributes.put("class", "es.ehu.sputnik.Comp3Agent");
                attributes.put("maxMemory", "15");
                restrictionLists.clear();
                String i3 = mra.seRegister ("implementation", c3 , attributes, restrictionLists);

                //APPLICATION 2
                attributes.clear();
                attributes.put("name", "Sputnik App2");
                attributes.put("deadline", "10");
                restrictionLists.clear();
                String a2 = mra.seRegister ("application", as1 , attributes, restrictionLists);
                LOGGER.debug("a1"+a1);
                // COMPONENT 1
                attributes.clear();
                attributes.put("name", "Comp_1");
                attributes.put("activation", "periodic");
                attributes.put("period", "3.5");
                // ************* COMPONENT - restricción 1
                restrictionList.clear();
                restrictionList.put("refServID", "id56");
                restrictionLists.clear();
                restrictionLists.put("procNode", restrictionList);
                String c21 = mra.seRegister ("component", a2 , attributes, restrictionLists);
                // IMPLEMENTATION 1
                attributes.clear();
                attributes.put("name", "Impl_1");
                attributes.put("class", "es.ehu.sputnik.Comp1Agent");
                attributes.put("maxMemory", "35");
                restrictionLists.clear();
                String i21 = mra.seRegister ("implementation", c21 , attributes, restrictionLists);

                mra.iValidate(as1);

                mra.start(as1,null);

                this.finished=true;
                LOGGER.info("finish.");
                while(true) {Thread.sleep(1000);}

            } catch (Exception e) {e.printStackTrace();}

            while(true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        private boolean finished = false;

        public boolean done() {
            LOGGER.entry();
            return LOGGER.exit(finished);
        }

    } // ----------- End myBehaviour

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
