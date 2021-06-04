package es.ehu.domain.manufacturing.agents;

import es.ehu.domain.manufacturing.agents.functionality.Batch_Functionality;
import es.ehu.domain.manufacturing.agents.functionality.Transport_Functionality;
import es.ehu.domain.manufacturing.template.DomResAgentTemplate;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.template.ResourceAgentTemplate;
import es.ehu.platform.utilities.XMLReader;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Stack;


public class TransportAgent extends DomResAgentTemplate  {

    static final Logger LOGGER = LogManager.getLogger(ResourceAgentTemplate.class.getName());
    private static final long serialVersionUID = -214426101233212079L;


    /* position x axis */
    public float xPos;

    /*position y axis */
    public float yPos;

    /*battery perc */
    public float battery;

    /* HashMap to locate machines position, punto de carga, almacen de material, entrada de material KUKA, salida material KUKA.*/
    public HashMap<String, String> keyLocalization = new HashMap<>();

    /* Stack to know works transport has to do*/
    public Stack<String> pilaTareas = new Stack<String>();

    @Override
    protected MessageTemplate variableInitialization(Object[] arguments, Behaviour behaviour) {

        LOGGER.entry(arguments);
        System.out.println("es.ehu.platform.template.ApplicationAgentTemplate.variableInitialization()");


        /* DEPENDE DE COMO SE PASAN LOS ARGUMENTS

        if ((arguments != null) && (arguments.length>=4)){//       introduce resourceName,resourceModel, xAxis,yAxis and battery.

            this.resourceName=arguments[0].toString();
            this.xPos=(Float) arguments[1];
            this.yPos = (Float) arguments[2] ;
            this.battery = (Float) arguments[3];

            XMLReader fileReader = new XMLReader();
            try {
                this.resourceModel = fileReader.readFile(arguments[4].toString());
            } catch (Exception e) {
                LOGGER.info("Parse can not generate documents");
                this.initTransition = ControlBehaviour.STOP;
            }

        } else {
            LOGGER.info("There are not sufficient arguments to start");
            this.initTransition = ControlBehaviour.STOP;
        }
        */

        this.resourceName = arguments[0].toString();
        functionalityInstance = new Transport_Functionality();
        return null;  // return LOGGER.exit(null); //


    }
    protected void takeDown() {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}


