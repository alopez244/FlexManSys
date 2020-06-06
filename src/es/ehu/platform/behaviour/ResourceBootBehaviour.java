package es.ehu.platform.behaviour;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import es.ehu.platform.MWAgent;
import static es.ehu.platform.utilities.MasReconOntologies.*;

/**
 * This behaviour receives messages from the templates used in the constructor
 * and execute the functionality to perform its activity.
 * <p>
 * There are two possible cases:
 * <ul>
 * <li>If the MWAgent has any {@code sourceComponentIDs}, then it will send to
 * the execute functionality the contentObject. Finally, the returned value is
 * sent as content to the {@code targetComponentsIDs}</li>
 * <li>If the MWAgent does not have any {@code sourceComponentIDs}, then it will
 * send to the execute functionality the ACLMessage. Finally, the returned value
 * (ACLMessage) is sent.</li>
 * </ul>
 * <p>
 * <b>NOTE:</b> The transition to another state is done using a message to a
 * {@code ControlBehaviour}
 *
 * @author Brais Fortes (@fortes23) - Euskal Herriko Unibersitatea
 * @author Mikel Lopez (@lopeziglesiasmikel) - Euskal Herriko Unibersitatea
 **/

public class ResourceBootBehaviour extends SimpleBehaviour {
    private static final long serialVersionUID = 3456578696375317772L;
    static final Logger LOGGER = LogManager.getLogger(ResourceBootBehaviour.class.getName());

    private MessageTemplate template;
    private MWAgent myAgent;
    private int PrevPeriod;
    private long NextActivation;

    private String mwm;

    private boolean exit;
    private String ID, newID;

    // Constructor. Create a default template for the entry messages
    public ResourceBootBehaviour(MWAgent a) {
        super(a);

        LOGGER.debug("*** Constructing RunningBehaviour ***");
        this.myAgent = a;
        this.template = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_RUN),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
    }

    public void onStart() {
        LOGGER.entry();
        this.PrevPeriod = myAgent.period;
        if (myAgent.period < 0) {
            this.NextActivation = -1;
        } else {
            this.NextActivation = myAgent.period + System.currentTimeMillis();
        }
        LOGGER.exit();
    }

    public void action() {
        LOGGER.entry();

        String[] args = (String[])myAgent.getArguments();
        for (int i=0; i<args.length; i++) {
            if (args[i].startsWith("ID=")) {
                ID=args[i].substring(3);
            }
        }

        //I must change the action method. This behaviour must be of use for all the different resources. This requires...
        //...to remove part of the code in this method to embed it in the ProcNode_Functionality.init method.
        newID = "";
        if (ID == null) {
            //if the agent has no ID, it means it is an auxiliary agent
            //Therefore, we invoke functioinalityInstance.init

            LOGGER.info(myAgent.getLocalName()+": autoreg > ");
            if (myAgent==null) System.out.println("My agent is null");
            if (myAgent.functionalityInstance==null) System.out.println("functionalityInstance is null");
            myAgent.functionalityInstance.init(myAgent);
            exit = true;

        } else exit = true;

//        newID = myAgent.functionalityInstance.init(myAgent);
//        if (ID == null) {
//            LOGGER.info(myAgent.getLocalName()+": autoreg > ");
//            if (myAgent==null) System.out.println("My agent is null");
//            if (myAgent.functionalityInstance==null) System.out.println("functionalityInstance is null");
//
//            try {
//                // Agent generation;
//                //TODO parametrizar la clase que se pasa al crear el agente
//                className = myAgent.getClass().getName();
//                ((AgentController)myAgent.getContainerController().createNewAgent(newID,className, new String[] { "ID="+newID, "description=description" })).start();
//
//                Thread.sleep(1000);
//                exit = true;
//            } catch (Exception e1) {
//                e1.printStackTrace();
//            }
//        } else exit = true;

        LOGGER.exit();
    }

    public int onEnd() {
        if (!newID.isEmpty()) return ControlBehaviour.STOP;
        else return myAgent.initTransition;
    }

    @Override
    public boolean done() {
        return exit;
    }
}