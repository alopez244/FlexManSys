package es.ehu.domain.manufacturing.agents;

import es.ehu.domain.manufacturing.agents.functionality.Transport_Functionality;
import es.ehu.domain.manufacturing.template.DomResAgentTemplate;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.template.ResourceAgentTemplate;
import es.ehu.platform.utilities.XMLReader;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;


public class TransportAgent extends DomResAgentTemplate{

    static final Logger LOGGER = LogManager.getLogger(ResourceAgentTemplate.class.getName());
    private static final long serialVersionUID = -214426101233212079L;


    /* DECLARACION DE VARIABLES */

    /* Nombre del transporte */
    public String transport_unit_name;

    /* Posicion actual del transporte */
    public String currentPos;
    public String recovery_point;
    public Double currentPos_X;
    public Double currentPos_Y;
    public boolean transport_in_dock;

    /* Porcentaje de bateria */
    public Float battery;

    /* Flags de presencia de obstaculo */
    public boolean bumperObstacle;
    public boolean cameraObstacle;

    /* Variables de tiempo*/
    public String initialTimeStamp;
    public String finalTimeStamp;

    public int hour;
    public int minute;
    public int seconds;

    /* Listado de posiciones clave (punto de carga, almacen de material, entrada de material KUKA, salida material KUKA) */
    public ArrayList<ArrayList<ArrayList<String>>> keyPosition;

    /* Plan de transporte (listado de tareas a realizar, por ejemplo [A3,B6])   */
    public ArrayList<ArrayList<ArrayList<String>>> transportPlan;


    /* INICIALIZACION DE VARIABLES */

    @Override
    protected MessageTemplate variableInitialization(Object[] arguments, Behaviour behaviour) {

        LOGGER.entry(arguments);
        System.out.println("es.ehu.domain.manufacturing.template.DomResAgentTemplate.variableInitialization()");

        /* Se leen los argumentos con los que se ha llamado al agente (nombre de recurso, localizaciones clave y plan de transporte) */
        if ((arguments != null) && (arguments.length>=3)){
            this.resourceName=arguments[0].toString();
            XMLReader fileReader = new XMLReader();

            try {
                this.keyPosition = fileReader.readFile(arguments[1].toString());
            } catch (Exception e) {
                LOGGER.info("Parse can not generate documents");
                this.initTransition = ControlBehaviour.STOP;
            }

            try {
                this.transportPlan = fileReader.readFile(arguments[2].toString());
            } catch (Exception e) {
                LOGGER.info("Parse can not generate documents");
                this.initTransition = ControlBehaviour.STOP;
            }

        } else {
            LOGGER.info("There are not sufficient arguments to start");
            this.initTransition = ControlBehaviour.STOP;

        }

        /* Por ultimo, se especifica el tipo de funcionalidad (en este caso funcionalidad de transporte) */
        functionalityInstance = new Transport_Functionality();

        /*
        Object[] data = new Object[3];
        data[0]=this.resourceName;
        data[1]=this.keyPosition;
        data[2]=this.transportPlan;

        functionalityInstance.execute(data);*/

        //this.initTransition = ControlBehaviour.RUNNING;

        return null;

    }


    /* FINALIZACION AGENTE TRANSPORTE */

    protected void takeDown() {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


