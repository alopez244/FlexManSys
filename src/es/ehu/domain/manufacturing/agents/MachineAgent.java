package es.ehu.domain.manufacturing.agents;

import es.ehu.domain.manufacturing.agents.functionality.Machine_Functionality;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.template.ResourceAgentTemplate;
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

import static es.ehu.platform.utilities.MasReconOntologies.ONT_NEGOTIATE;
import static es.ehu.domain.manufacturing.utilities.FmsNegotiation.ONT_DEBUG;

public class MachineAgent extends ResourceAgentTemplate {

    private static final long serialVersionUID = -3672658381864883026L;

    static final Logger LOGGER = LogManager.getLogger(ResourceAgentTemplate.class.getName());

    /** String representing the services machine agent offers. */
    public String machineServices;

    /** Machine Plan . */
    public Document machinePlan;

    @Override
    protected MessageTemplate variableInitialization(Object[] arguments, Behaviour behaviour) {
        LOGGER.entry(arguments);

        MessageTemplate informNeg = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_NEGOTIATE),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        MessageTemplate debugSim = MessageTemplate.and(MessageTemplate.MatchOntology(ONT_DEBUG),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));

        //Declaration of the variable used to communicate the agent and the machine (at the moment, Siemens PLC)


        if ((arguments != null) && (arguments.length >= 3)) {
            this.resourceName = arguments[0].toString();
            machineServices = arguments[1].toString();
            File xmlFile = new File(arguments[2].toString());
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = null;
            try {
                dBuilder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                LOGGER.info("Document can not be generated");
                this.initTransition = ControlBehaviour.STOP;
            }
            try {
                this.resourceModel = dBuilder.parse(xmlFile);
            } catch (Exception e) {
                LOGGER.info("Parse can not generate documents");
                this.initTransition = ControlBehaviour.STOP;
            }

            File xmlFile2 = new File(arguments[3].toString());

            try {
                dBuilder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                LOGGER.info("Document can not be generated");
                this.initTransition = ControlBehaviour.STOP;
            }
            try {
                machinePlan = dBuilder.parse(xmlFile2);
            } catch (Exception e) {
                LOGGER.info("Parse can not generate documents");
                this.initTransition = ControlBehaviour.STOP;
            }

        } else {
            LOGGER.info("There are not sufficient arguments to start");
            this.initTransition = ControlBehaviour.STOP;
        }

        functionalityInstance = new Machine_Functionality(this);
        return LOGGER.exit(MessageTemplate.or(informNeg, debugSim));
    }
}
