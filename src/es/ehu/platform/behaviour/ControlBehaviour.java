/**
 * Comportamiento que recive las ordenes del supervisor del alto nivel
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/
package es.ehu.platform.behaviour;

import static es.ehu.platform.utilities.MWMCommands.CMD_GETCOMPONENTS;
import static es.ehu.platform.utilities.MWMCommands.CMD_REPORT;
import static es.ehu.platform.utilities.MWMCommands.CMD_SET;
import static es.ehu.platform.utilities.MasReconOntologies.ONT_CONTROL;
import es.ehu.domain.manufacturing.agents.functionality.Machine_Functionality;
import jade.core.AID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.platform.MWAgent;

import jade.core.ContainerID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.FSMBehaviour;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class ControlBehaviour extends SimpleBehaviour {

    private static final long serialVersionUID = -4041584544631810983L;
    static final Logger LOGGER = LogManager.getLogger(ControlBehaviour.class.getName()) ;
    private static final String CMD_INCORRECTSTATE = "incorrect state";
    public static final String CMD_SETSTATE = "setstate";
    public static final int STOP=0, RUNNING=1, TRACKING=2,  WAITINGFORDECISION=3, RECOVERING=4, NEGOTIATING=5, IDLE=6;
    public static final String ST_STOP = "stop";
    public static final String ST_RUNNING = "running";
    public static final String ST_IDLE= "idle"; //idle behaviour
    public static final String ST_TRACKING = "tracking";
    public static final String ST_WAITINGFORDECISION = "waitingfordecision";
    public static final String ST_RECOVERING = "recovering";
    public static final String ST_NEGOTIATING = "negotiating";
    private MWAgent myAgent;
    private FSMBehaviour fsm;
    protected MessageTemplate template;
    int exitValue = 0;
    boolean exit = false;
    private AID QoSID = new AID("QoSManagerAgent", false);

    public ControlBehaviour(MWAgent a) {
        super(a);
        LOGGER.entry(a);
        LOGGER.trace("***************** inicio ControlBehaviour");
        myAgent = a;
        template =  MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.FAILURE),
                MessageTemplate.and(
                        MessageTemplate.MatchOntology(ONT_CONTROL),
                        MessageTemplate.MatchPerformative(ACLMessage.REQUEST))

        );

        LOGGER.exit();
    }

    public ControlBehaviour(MWAgent a, Behaviour fsmBeh) {
        super(a);
        LOGGER.entry(a);
        LOGGER.trace("***************** init ControlBehaviour");
        myAgent = a;
        fsm = (FSMBehaviour)fsmBeh;
        template =  MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.FAILURE),
                MessageTemplate.and(
                        MessageTemplate.MatchOntology(ONT_CONTROL),
                        MessageTemplate.MatchPerformative(ACLMessage.REQUEST))
        );

        LOGGER.exit();
    }

    @Override
    public void action()  {
        LOGGER.entry();
        this.exitValue=0;
        this.exit=false;

        LOGGER.debug(myAgent.cmpID+"("+myAgent.getLocalName()+"): SupervisorControl.action()");
        ACLMessage msg = myAgent.receive(template);

        if (msg != null|| myAgent.change_state) {

            if(msg!=null){
            if (msg.getContent() == null) {
                LOGGER.info("message content null!");
                return;
            }
            if (msg.getPerformative() == ACLMessage.FAILURE) { // if FAILURE

                LOGGER.info("****************ACLMessage.FAILURE (control):" + msg.getContent());
                String name = msg.getContent().substring(msg.getContent().indexOf(":name ", msg.getContent().indexOf("MTS-error")) + ":name ".length());
                name = name.substring(0, name.indexOf('@'));
                LOGGER.info("msg.getPerformative()==ACLMessage.FAILURE (sender=" + name + ")");


                    ACLMessage report= new ACLMessage(ACLMessage.FAILURE);
                    report.setOntology("ctrlbhv_failure");
                    report.setContent(name);
                    report.addReceiver(QoSID);
                    myAgent.send(report);


                try {
                    LOGGER.info(myAgent.sendCommand(CMD_REPORT + " (" + CMD_GETCOMPONENTS + " " + name + ") type=notFound cmpins=" + name));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                LOGGER.info("ACLMessage.REQUEST (control):" + msg.getContent());
                String result = "";
                LOGGER.info("* Received control msg:" + msg.getContent());
                LOGGER.info(myAgent.cmpID + "(" + myAgent.getLocalName() + ")" + " < control:" + msg.getContent() + " < " + msg.getSender().getLocalName());
                String[] cmd = msg.getContent().split(" ");
                if (cmd.length <= 1) {
                    return;
                }
                boolean sendReply = false;

                if (cmd[0].equals("set")) {
                    sendReply = true;

                    if (cmd[1].equals("period")) {
                        LOGGER.debug("myAgent.period = " + Integer.parseInt(cmd[2]));
                        myAgent.period = Integer.parseInt(cmd[2]);
                        myAgent.sendCommand(CMD_SET + " " + myAgent.cmpID + " period=" + cmd[2]);

                        result = "done";
                    } else if (cmd[1].equals("mwmStoresExecutionState")) {
                        myAgent.mwmStoresExecutionState = Boolean.parseBoolean(cmd[2]);
                    }
                } else if (cmd[0].equals(CMD_SETSTATE)) {

                    sendReply = true;
                    LOGGER.info("Set State ---------------");
                    if (cmd[1].equals(ST_RUNNING)) {
                        exitValue = RUNNING;
                    } else if (cmd[1].equals(ST_TRACKING)) {
                        exitValue = TRACKING;
                    } else if (cmd[1].equals(ST_STOP)) {
                        exitValue = STOP;
                    } else if (cmd[1].equals(ST_RECOVERING)) {
                        exitValue = RECOVERING;
                    } else if (cmd[1].equals(ST_NEGOTIATING)) {
                        exitValue = NEGOTIATING;
                    } else if (cmd[1].equals(ST_IDLE)) {
                        exitValue = IDLE;
                    } else {
                        result = CMD_INCORRECTSTATE;
                    }

                    if (!result.equals(CMD_INCORRECTSTATE)) {
                        exit = true;
                        result = "done";
                    }
                } else if (cmd[0].equals("move")) {
                    LOGGER.debug("doMove(new ContainerID(" + cmd[1] + ", null));"); //TODO comprobar que el nodo args[1] existe
                    myAgent.doMove(new ContainerID(cmd[1], null));
                }

                if (sendReply) { //devuelvo respuesta
                    ACLMessage aReply = msg.createReply();
                    aReply.setOntology(msg.getOntology());
                    aReply.setContent(result.trim());
                    aReply.setPerformative(ACLMessage.INFORM);
                    LOGGER.info("controlBehaviour().send(" + aReply.getContent() + ")");
                    myAgent.send(aReply);
                }
            }
        }else if(myAgent.getLocalName().contains("machine")&&  myAgent.change_state){ //autoidle del machine agent, para cuando se queda aislado

                String result = "";
                LOGGER.info("Set State ---------------");
                switch( myAgent.state){
                    case "idle":  exitValue = IDLE;
                        exit = true;
                    break;
                    case "running":  exitValue = RUNNING; //no usado por ahora
                        exit = true;
                    break;
                    default:  LOGGER.error("Asked a not valid state");
                    break;
                }
                myAgent.change_state=false;
            }
        } else {
            LOGGER.trace("ControlBehaviour.beh.block()");
            block();
        }

        LOGGER.exit();
    }



    @Override
    public boolean done() {
        return exit;
    }

    public int onEnd() {
        return exitValue;
    }

}