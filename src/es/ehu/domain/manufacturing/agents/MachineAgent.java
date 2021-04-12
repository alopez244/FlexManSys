package es.ehu.domain.manufacturing.agents;

import es.ehu.domain.manufacturing.agents.functionality.Machine_Functionality;
import es.ehu.domain.manufacturing.template.DomResAgentTemplate;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.template.ResourceAgentTemplate;
import es.ehu.platform.utilities.XMLReader;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static es.ehu.platform.utilities.MasReconOntologies.ONT_NEGOTIATE;
import static es.ehu.domain.manufacturing.utilities.FmsNegotiation.ONT_DEBUG;

public class MachineAgent extends DomResAgentTemplate {

    private static final long serialVersionUID = -3672658381864883026L;

    static final Logger LOGGER = LogManager.getLogger(ResourceAgentTemplate.class.getName());

    /** String representing the services machine agent offers. */
    public String machineServices;

    /** Machine Plan . */
    public ArrayList<ArrayList<ArrayList<String>>> machinePlan;

    /** Machine Consumables . */
    public static ArrayList<HashMap<String, String>> availableMaterial = new ArrayList<>();


    @Override
    protected MessageTemplate variableInitialization(Object[] arguments, Behaviour behaviour) {
        LOGGER.entry(arguments);

        MessageTemplate informNeg = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_NEGOTIATE),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        MessageTemplate debugSim = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DEBUG),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));

        if ((arguments != null) && (arguments.length >= 3)) {
            this.resourceName = arguments[0].toString();
            machineServices = arguments[1].toString();
            XMLReader fileReader = new XMLReader();

            try {
                this.resourceModel = fileReader.readFile(arguments[2].toString());
            } catch (Exception e) {
                LOGGER.info("Parse can not generate documents");
                this.initTransition = ControlBehaviour.STOP;
            }

            try {
                machinePlan = fileReader.readFile(arguments[3].toString());
            } catch (Exception e) {
                LOGGER.info("Parse can not generate documents");
                this.initTransition = ControlBehaviour.STOP;
            }

        } else {
            LOGGER.info("There are not sufficient arguments to start");
            this.initTransition = ControlBehaviour.STOP;
        }

        functionalityInstance = new Machine_Functionality();
        return LOGGER.exit(MessageTemplate.or(informNeg, debugSim));
    }
}
