package es.ehu.domain.manufacturing.agents.functionality;

import com.google.gson.Gson;
import es.ehu.domain.manufacturing.agents.TransportAgent;
import es.ehu.domain.manufacturing.utilities.StructTranspRequest;
import es.ehu.domain.manufacturing.utilities.StructTranspResults;
import es.ehu.domain.manufacturing.utilities.StructTranspState;
import es.ehu.domain.manufacturing.utilities.StructTransportUnitState;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.behaviour.NegotiatingBehaviour;
import es.ehu.platform.template.interfaces.AssetManagementFunctionality;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.NegFunctionality;
import es.ehu.platform.utilities.Cmd;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Objects;


public class Transport_Functionality extends DomRes_Functionality implements BasicFunctionality, NegFunctionality, AssetManagementFunctionality {

    static final Logger LOGGER = LogManager.getLogger(Transport_Functionality.class.getName());


    /* DECLARACI�N DE VARIABLES */

    /* Agente que utiliza esta funcionalidad (se recibe como par�metro en diferentes m�todos) */
    private TransportAgent myAgent;

    /* Id del agente en el sistema*/
    private  String seId;

    /* Nombre de la clase (para poder crear el agente transporte definitivo) */
    private String className;

    /* Flag que se activa cuando el transporte esta trabajando */
    private Boolean workInProgress = false;

    /* Nombre del gatewayAgent con el que interact�a el agente */
    private AID gatewayAgentID =null;

    /* Identificador de la conversaci�n iniciada por el agente transporte */
    private int conversationId = 0;

    /* Flag que permite conocer si el transporte ha vuelto de Operative a Active */
    private boolean TransportOperative = false;


    /* OPERACIONES DE INICIALIZACI�N Y PUESTA EN MARCHA */

    @Override
    public Void init(MWAgent mwAgent) {

        /* Se hace el cambio de tipo */
        this.myAgent = (TransportAgent) mwAgent;
        LOGGER.entry();

        /* Se guarda el nombre del gatewayAgent con el que interact�a el agente */
        /* myAgent.resourceName recoge el primer argumento de entrada en la ejecucion, por ejemplo: */
        /* ControlGatewayContT_01 si T_01 es el argumento*/
        mwAgent.gatewayAgentName = "ControlGatewayCont"+myAgent.resourceName;
        gatewayAgentID = new AID(myAgent.gatewayAgentName, false);

        /* Se leen los argumentos con los que se ha llamado al agente y se comprueba si tiene un id */
        String [] args = (String[]) myAgent.getArguments();

        for (int i=0; i<args.length; i++){
            /* En caso afirmativo, es el agente transporte definitivo y no hay que hacer nada m�s */
            if (args[i].toLowerCase().startsWith("id=")) return null;
        }

        /* En caso negativo, se trata del agente auxiliar y hay que realizar m�s acciones */
        /* En primer lugar, hay que comprobar la conectividad con el asset en dos pasos: */
        /* Paso 1: contacto con el gatewayAgent y espero respuesta (si no hay, no se puede continuar con el registro) */
        //sendACLMessage(ACLMessage.REQUEST,gatewayAgentID,"ping",myAgent.getLocalName()+"_"+ conversationId++,"",myAgent);
        //sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "ping","0",null,myAgent);
        //ACLMessage answer_gw = myAgent.blockingReceive(MessageTemplate.MatchOntology("ping"), 1000);

        //if(answer_gw==null){
        //    System.out.println("GW is not online. Start GW and repeat.");
        //    System.exit(0);
        //}

        /* Paso 2: contacto con el asset a trav�s del gatewayAgent y espero respuesta (si no hay, no se puede conitnuar con el registro) */
        //sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "check_asset",myAgent.getLocalName()+"_"+ conversationId++,"ask_state",myAgent);
        //ACLMessage answer = myAgent.blockingReceive(MessageTemplate.MatchOntology("asset_state"), 300);

        // Esperamos cuatro segundos a recibir mensajes por parte del ACLGWAgentROS para verificar que esta vivo
        ACLMessage answer = myAgent.blockingReceive(MessageTemplate.MatchOntology("asset_state"), 4000);
        sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "asset_checked","0",null,myAgent);

        if(answer!=null){

            // Deserializamos los datos contenidos en answer, que corresponden a los datos
            // del objeto TransportState desde el GWagentROS, que a su vez corresponden a TUS_object
            // de la clase ACLGWAgentROS
            Gson gson = new Gson();

            //StructTranspState javaTranspState = gson.fromJson(answer.getContent(), StructTranspState.class);

            StructTransportUnitState javaTranspState = gson.fromJson(answer.getContent(), StructTransportUnitState.class);

            myAgent.ActualState = javaTranspState.getTransport_unit_state();

            if(!Objects.equals(myAgent.ActualState, "NONE") && !Objects.equals(myAgent.ActualState, "ERROR") && !Objects.equals(myAgent.ActualState, "STOP")){
                System.out.println("The asset is ready to work: " +myAgent.ActualState);
                myAgent.battery = javaTranspState.getBattery();
                myAgent.currentPos_X = javaTranspState.getOdom_x();
                myAgent.currentPos_Y = javaTranspState.getOdom_y();
                sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "asset_checked","0",null,myAgent);

            } else {
                /* Si se recibe respuesta pero no es la adecuada, significa que el asset no est� listo y no se puede continuar con el registro */
                /* Para que la respuesta sea adecuada, el transporte debe de estar disponible para operar, es decir, que su estado debe de ser*/
                /* diferente a NONE, STOP y ERROR */
                System.out.println("The asset is not ready to work. Trasnport not ready.");
                System.exit(0);
            }
        }else{
            /* Si no se recibe respuesta, significa que el asset no est� listo y no se puede continuar con el registro */
            System.out.println("The asset is not ready to work. No response from transport");
            System.exit(0);
        }

        sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "asset_checked","0",null,myAgent);

        /* Si la comunicaci�n con el asset es correcta, se procede a registrar el agente transporte en el SystemModelAgent */
        String attribs = " battery="+ myAgent.battery + " currentPos_X="+myAgent.currentPos_X + " currentPos_Y="+myAgent.currentPos_Y;
        String cmd = "reg transport parent=system"+attribs;

        ACLMessage reply = null;

        try {
            reply = myAgent.sendCommand(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }

        seId = reply.getContent();

        sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "asset_checked","0",null,myAgent);

        try {
            /* Una vez registrado el agente transporte, es creado por el agente auxiliar, pas�ndole como argumento su id */
            className = myAgent.getClass().getName();
            String [] args2 = {"ID="+seId, "description=description" };
            args = ArrayUtils.addAll(args,args2);
            ((AgentController)myAgent.getContainerController().createNewAgent(seId,className, args)).start();
            Thread.sleep(1000);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "asset_checked","0",null,myAgent);

        return null;

    }


    /* OPERACIONES DE ACTUALIZACI�N DE LA LISTA DE TAREAS */

    @Override
    public Object execute(Object[] input) {

        System.out.println("**********************************");
        System.out.println("JEJEJEJ");

        if (input[0] != null) {
            ACLMessage msg = (ACLMessage) input[0];
            Acknowledge(msg,myAgent);

            /* Se guardan en un array de String todas las posibles operaciones recibidas */
            String[] allOperations = msg.getContent().split("&");

            /* Se recorre el array para ir grabando las operaciones una a una*/
            for (String singleOperation : allOperations) {

                /* Se declara un arraylist para guardar la informaci�n sobre cada operaci�n recibida */
                ArrayList<ArrayList<String>> operationInfo = new ArrayList<>();

                /* Se crean y se rellenan dos arraylist de string para la cabecera del listado de operaciones */
                ArrayList<String> elementName = new ArrayList<>();
                ArrayList<String> hierarchyLevel = new ArrayList<>();
                elementName.add("operation");
                hierarchyLevel.add("2");

                /* Se declaran dos arraylist de string en los que se guardas el nombre y el valor de los atributos */
                ArrayList<String> attNames = new ArrayList<>();
                ArrayList<String> attValues = new ArrayList<>();

                /* Se guardan en un array de String todos los atributos de la operaci�n */
                String [] allAttributes = singleOperation.split(" ");

                /* Se recorre el array para ir grabando los atributos uno a uno */
                for (String singleAttribute : allAttributes){

                    /* Se separa el nombre del atributo de los valores */
                    String [] attInfo = singleAttribute.split("=");
                    attNames.add(attInfo[0]);
                    attValues.add(attInfo[1]);

                }

                /* Por �ltimo, se rellena el arraylist operationInfo y se graba la operaci�n en el plan del transporte */
                operationInfo.add(0, elementName);
                operationInfo.add(1, hierarchyLevel);
                operationInfo.add(2, attNames);
                operationInfo.add(3, attValues);
                myAgent.transportPlan.add(operationInfo);

            }

            return LOGGER.exit("done");

        } else {
            return LOGGER.exit(null);
        }
    }



    /* OPERACIONES DE NEGOCIADO DE ASIGNACI�N DE TAREAS */

    @Override
    public long calculateNegotiationValue(String negAction, String negCriterion, Object... negExternalData) {

        //TODO implementar el m�todo. Para ello hay que:
        //Leer la pila de tareas (obtener todas las coordenadas)
        //Estimar un valor de negociaci�n en base a estas (tiempo o distancia, preferiblemente tiempo)
        //Devolver el valor obtenido

        System.out.println("**********************************");
        System.out.println(negAction);

        /* Se van a recibir dos campos en el objeto negExternalData:
        *  En el primer campo, se recibir� la operaci�n (u operaciones) que tendr� que a�adir a su pila el transporte ganador
        *  En el segundo campo, se recibir� el nombre del agente m�quina que solicita la operaci�n */
        String positionsArray = (String) negExternalData[0];
        String machineAgentName = (String) negExternalData[1]; /* Creo que este par�metro no deber�a de hacer falta para este m�todo, pero por si acaso */

        /* If redundante solo para confirmar que la petici�n se ajusta a un criterio que tenga sentido para el agente
        *  Este if cobrar� m�s sentido si en el futuro se consideran varios criterios posibles a elecci�n del solicitante */
        //TODO: cambiar el criterio "position" por "battery" (luego ya lo cambio yo en el resto de sitios que haga falta)
        if (negCriterion.equals("position")){

        }

        return 0; /* Este 0 solo lo he puesto para que no de problemas al compilar, luego habr� que sustituirlo por lo que corresponda */
    }

    @Override
    public int checkNegotiation(String conversationId, String sAction, double negReceivedValue, long negScalarValue, boolean tieBreak, boolean checkReplies, boolean isPartialWinner, Object... negExternalData) {

        LOGGER.entry(conversationId, sAction, negReceivedValue, negScalarValue);

        /* Se van a recibir dos campos en el objeto negExternalData:
         *  En el primer campo, se recibir� la operaci�n (u operaciones) que tendr� que a�adir a su pila el transporte ganador
         *  En el segundo campo, se recibir� el nombre del agente m�quina que solicita la operaci�n */
        String positionsArray = (String) negExternalData[0];
        String machineAgentName = (String) negExternalData[1];

        /* Se comparan el valor propio y el valor recibido */
        if (negReceivedValue<negScalarValue) return NegotiatingBehaviour.NEG_LOST; /* pierde la negociaci�n */
        if ((negReceivedValue==negScalarValue) && !tieBreak ) return NegotiatingBehaviour.NEG_LOST; /* empata la negociaci�n pero no es quien fija el desempate */

        LOGGER.info("negotiation(id:"+conversationId+") partial winner "+myAgent.getLocalName()+"(value:"+negScalarValue+")");
        if (!checkReplies) return NegotiatingBehaviour.NEG_PARTIAL_WON; /* es el ganador parcial, pero faltan negociaciones por finalizar */


        if (!isPartialWinner) return NegotiatingBehaviour.NEG_LOST; /* Para ser el ganadores verdadero un agente tendr� que ser ganador parcial en cada momento */

        /* El agente es el ganador final (ha ganado todas las comparaciones) */
        LOGGER.info("ejecutar "+sAction);
        Cmd action = new Cmd(sAction);

        if (action.cmd.equals("supplyConsumables")) {
            LOGGER.info("id=" + action.who);

            //TODO implementar las acciones que tiene que hacer el agente ganador. Para ello hay que:
            //Encapsular la operaci�n (u operaciones) recibida en un mensaje ACL e invocar el m�todo Execute
            //Una vez a�adida la operaci�n al plan, enviar un mensaje al agente que solicit� el servicio (machineAgentName)
            //Este mensaje deber� informar de qu� transporte ha ganado la negociaci�n y el momento de entrega estimado (solo en caso de que las negociaciones se hagan en base a tiempos)
        }

        return NegotiatingBehaviour.NEG_WON;

    }


    /* OPERACIONES DE INTERACCI�N CON EL ASSET */

    @Override
    public void sendDataToDevice() {

        /* En primer lugar se comprueba si el transporte est� realizando alguna operaci�n (workInProgress=TRUE) */
        if (!workInProgress){

            // Si entramos aqui, el Transporte se encuentra libre y sin un plan asignado

            /* Si el transporte est� libre, se comprueba si hay tareas en el plan
            * La cabecera del modelo ocupa una posici�n, por lo que para que haya tareas, el tama�o del modelo tiene que ser de 2 o m�s */
            if (myAgent.transportPlan.size() >= 2) {

                /* Se leen las posiciones de la primera operaci�n del plan (el tercer elemento del plan) */
                /* Recordatorio: del segundo elemento del modelo (primera operaci�n), se obtiene el cuarto elemento (valores de sus atributos)...
                y de se lee la primera posici�n que tiene (un string con todas las coordenadas) */
                /* Las coordenadas est�n unidas por comas, y se separan para obtener el array de String que queremos enviar */
                String [] allPositions = myAgent.transportPlan.get(1).get(3).get(0).split(",");

                /* El siguiente paso es transformar cada posici�n en una coordenada que el asset pueda identificar
                *  Primero, se crea un array en el que se van a guardar las coordenadas, de la misma longitud que el array de posiciones */
                String [] allCoordinates = new String[allPositions.length];

                /* Despu�s, se recorre el array de posiciones y se van guardando las coordenadas correspondientes en el array de coordenadas */
                for (int i = 0; i < allPositions.length; i++) {

                    /* Para cada posici�n tengo que encontrar la coordenada correspondiente
                    *  Para ello, recorro el submodelo de posiciones clave */
                    for (int j = 0; j<myAgent.keyPosition.get(0).get(2).size(); j++){

                        /* Compruebo si la posici�n i del array allPositions coincide con la posici�n j de keyPosition */
                        if (myAgent.keyPosition.get(0).get(2).get(j).equals(allPositions[i])){

                            /* Si coinciden, obtengo la coordenada */
                            allCoordinates[i]=myAgent.keyPosition.get(0).get(3).get(j);
                        }
                    }
                }

                /* Declaro la estructura en la que voy a formatear el mensaje */
                StructTranspRequest javaTranspRequest = new StructTranspRequest();
                javaTranspRequest.setTask(allCoordinates);

                /* Ahora se formatea la estructura a json*/
                // EL tamanio de la estructura de Json parece ser demasiado grande, hay problemas de lectura en el
                // GatewayAgent, se propone pasar las posiciones del string allCoordinates de una a una
                //Gson gson_request = new Gson();
                //gson_request.toJson(javaTranspRequest);

                /* Despu�s, se prepara el mensaje y se env�a al gatewayAgent */
                //sendACLMessage(ACLMessage.REQUEST,gatewayAgentID,"data",myAgent.getLocalName()+"_"+ conversationId++, String.valueOf(gson),myAgent);

                /* Se define el numero de coordenadas del TransportPlan correspondiente a pasar al transporte */

                // Se envia previamente la cantidad de tareas que se van a recibir
                sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "PlanCoordenadas", "1234", String.valueOf(allCoordinates.length), myAgent);

                for (int i = 0; i < allCoordinates.length; i++) {

                    sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "PlanCoordenadas", "1234", allCoordinates[i], myAgent);

                }

                /* Indicamos al transporte que el numero de coordenadas */

                sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "PlanCoordenadas", "1234", "END", myAgent);

                /* Por �ltimo, pongo el workInProgress a true*/
                workInProgress=true;

            } else {

                /* Si el transporte est� libre y adem�s no tiene tareas asignadas, debe volver a la estaci�n de carga */
                //Si no tiene ninguna tarea mas asignada, el transporte debe de volver a su estacion de carga

                sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "ComandoCoordenada", "1234", "DOCK", myAgent);
                System.out.println(myAgent.transport_unit_name + " Transport going to docking station");
            }
        }else{

            /* Dejar para debug, luego comentar o quitar*/
            System.out.println(myAgent.transport_unit_name + " Transport has a plan");
        }
    }

    @Override
    public void rcvDataFromDevice(ACLMessage msg) {

        /* Se recibe el mensaje del gatewayAgent y se filtra por estado del transporte*/

        Gson gson = new Gson();
        StructTransportUnitState javaTranspState = gson.fromJson(msg.getContent(), StructTransportUnitState.class);

        myAgent.ActualState = javaTranspState.getTransport_unit_state();
        myAgent.transport_unit_name = javaTranspState.getTransport_unit_name();

        if (!Objects.equals(myAgent.ActualState, "ACTIVE")){

            /* El transporte o bien se encuentra en un estado que no puede operar o bien se encuentra operando */
            /* Enviamos al SMA la informacion que proviene desde el transporte */

            /* Antes de considerar al transporte como operable, verificamos que ha llevado a cabo su etapa de
            /* calibracion. En caso de no estar calibrado, se le envia el comando de calibracion */

            if (Objects.equals(myAgent.ActualState, "IDLE") || (Objects.equals(myAgent.ActualState, "LOCALIZATION"))){

                if (Objects.equals(myAgent.ActualState, "IDLE")) {

                    sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "ComandoCoordenada", "1234", "X", myAgent);
                    myAgent.ActualState = javaTranspState.getTransport_unit_state();
                    System.out.println(myAgent.transport_unit_name + " Transport needs calibration");

                }

                else if (Objects.equals(myAgent.ActualState, "LOCALIZATION")){

                    myAgent.ActualState = javaTranspState.getTransport_unit_state();
                    System.out.println(myAgent.transport_unit_name + " Transport is calibrating");

                }

            }

            if (Objects.equals(myAgent.ActualState, "RECOVERY")){

                // A futuro faltaria notificar que el transporte ha encontrado un obstaculo en el camino al SMA
                // para que un operario proceda a retirar el obstaculo

                myAgent.ActualState = javaTranspState.getTransport_unit_state();
                myAgent.cameraObstacle = javaTranspState.getDetected_obstacle_camera();
                myAgent.bumperObstacle = javaTranspState.getDetected_obstacle_bumper();

                if (TransportOperative) {

                    /* EL transporte ha entrado a RECOVERY desde OPERATIVE */

                    System.out.println(myAgent.transport_unit_name + " Transport needs assistance, obstacle detected during operation");

                    if (!myAgent.cameraObstacle && !myAgent.bumperObstacle){

                        /* Si ya no se detectan obstaculos segun la camara y el parachoques, la persona operaria ha retirado el obstaculo, via libre*/

                        sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "ComandoCoordenada", "1234", "FREEWAY", myAgent);

                    }

                }

                else {

                    /* EL transporte ha entrado a RECOVERY desde LOCALIZATION */

                    System.out.println(myAgent.transport_unit_name + " Transport needs assistance, obstacle detected during localization");

                    if (!myAgent.cameraObstacle && !myAgent.bumperObstacle){

                        /* Si ya no se detectan obstaculos segun la camara y el parachoques, la persona operaria ha retirado el obstaculo, via libre*/

                        sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "ComandoCoordenada", "1234", "FREEWAY", myAgent);

                    }

                }

            }

            System.out.println(myAgent.transport_unit_name + " Transport isn't active");

            myAgent.battery = javaTranspState.getBattery();
            myAgent.currentPos_X = javaTranspState.getOdom_x();
            myAgent.currentPos_Y = javaTranspState.getOdom_y();

            // Se actualiza el valor de estos par�metros en el SystemModelAgent
            String cmd = "set "+seId+" battery="+ myAgent.battery;
            ACLMessage reply = null;
            try {
                reply = myAgent.sendCommand(cmd);
            } catch (Exception e) {
                e.printStackTrace();
            }

            cmd = "set "+seId+" currentPos_X="+ myAgent.currentPos_X;
            try {
                reply = myAgent.sendCommand(cmd);
            } catch (Exception e) {
                e.printStackTrace();
            }

            cmd = "set "+seId+" currentPos_Y="+ myAgent.currentPos_Y;
            try {
                reply = myAgent.sendCommand(cmd);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (myAgent.ActualState.equals("OPERATIVE")){

                // El transporte esta en operacion, este flag permitira saber cuando el transporte ha vuelto a estado
                // ACTIVE habiendo estado en operacion anteriormente

                if (!TransportOperative){

                    // Cuando entre por primera vez a Operative, el flag TransportOperative sera false, por lo
                    // que se aprovecha a recoger el momento exacto en el que empieza a llevar a cabo la tarea
                    // que le ha hecho entrar al transporte al estado de Operative.

                    myAgent.hour = javaTranspState.getHour();
                    myAgent.minute = javaTranspState.getMinute();
                    myAgent.seconds = javaTranspState.getSeconds();

                    myAgent.initialTimeStamp = myAgent.hour + ":" + myAgent.minute + ":" + myAgent.seconds + " h";

                }

                TransportOperative = true;
                System.out.println(myAgent.transport_unit_name + " Transport started a new task at " + myAgent.initialTimeStamp);

            }

        } else if (myAgent.ActualState.equals("ACTIVE")){

            // El transporte o bien acaba de terminar una operacion o bien esta listo para operar y recibir tareas
            System.out.println(myAgent.transport_unit_name + " Transport is active");

            // Se actualiza el valor de estos par�metros en el SystemModelAgent
            String cmd = "set "+seId+" battery="+ myAgent.battery;
            ACLMessage reply = null;
            try {
                reply = myAgent.sendCommand(cmd);
            } catch (Exception e) {
                e.printStackTrace();
            }

            cmd = "set "+seId+" currentPos_X="+ myAgent.currentPos_X;
            try {
                reply = myAgent.sendCommand(cmd);
            } catch (Exception e) {
                e.printStackTrace();
            }

            cmd = "set "+seId+" currentPos_Y="+ myAgent.currentPos_Y;
            try {
                reply = myAgent.sendCommand(cmd);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (TransportOperative){

                // El transporte ha efectuado su tarea correctamente, ha vuelto al estado ACTIVE, esto indica que
                // el transporte esta preparado para recibir nuevas tareas.

                System.out.println(myAgent.transport_unit_name + " Transport ended his task");

                AID serviceRequester = new AID(myAgent.transportPlan.get(1).get(3).get(1), false);
                AID serviceReceiver = new AID(myAgent.transportPlan.get(1).get(3).get(2), false);

                myAgent.hour = javaTranspState.getHour();
                myAgent.minute = javaTranspState.getMinute();
                myAgent.seconds = javaTranspState.getSeconds();

                myAgent.finalTimeStamp = myAgent.hour + ":" + myAgent.minute + ":" + myAgent.seconds + " h";
                System.out.println(myAgent.transport_unit_name + " Transport ended his task at " + myAgent.finalTimeStamp);

                // Se env�a un mensaje al solicitante del servicio
                sendACLMessage(ACLMessage.INFORM,serviceRequester,"data",myAgent.getLocalName()+"_"+ conversationId++,"initialTimeStamp="+ myAgent.initialTimeStamp +" finalTimeStamp="+ myAgent.finalTimeStamp,myAgent);

                // Se comprueba si el solicitante y el receptor del servicio son distintos agentes, para enviar un segundo mensaje si es necesario
                if (serviceReceiver != serviceRequester){
                    sendACLMessage(ACLMessage.INFORM,serviceReceiver,"data",myAgent.getLocalName()+"_"+ conversationId++,"initialTimeStamp="+ myAgent.initialTimeStamp +" finalTimeStamp="+myAgent.finalTimeStamp,myAgent);
                }

                // Se contin�a eliminando esta tarea del plan (la primera tarea de la lista ocupa la segunda posici�n del submodelo)
                myAgent.transportPlan.remove(1);

                // Por �ltimo, se resetea el flag para indicar que el transaporte ha quedado libre
                workInProgress = false;
                TransportOperative = false;

            }

        }

    }


    /* OPERACIONES DE FINALIZACI�N DEL AGENTE TRANSPORTE*/

    @Override
    public Void terminate(MWAgent myAgent) {return null;}
}


/*

public void rcvDataFromDevice(ACLMessage msg) {

      if (msg.getPerformative() == ACLMessage.CONFIRM){

            // Si se trata de un mensaje de confirmaci�n, se tiene que procesar con la estructura "StructTranspState"
Gson gson = new Gson();
    StructTransportUnitState javaTranspState = gson.fromJson(msg.getContent(), StructTransportUnitState.class);

            myAgent.battery = javaTranspState.getBattery();
                    myAgent.currentPos_X = javaTranspState.getOdom_x();
                    myAgent.currentPos_Y = javaTranspState.getOdom_y();

                    // Se actualiza el valor de estos par�metros en el SystemModelAgent
                    String cmd = "set "+seId+" battery="+ myAgent.battery;
                    ACLMessage reply = null;
                    try {
                    reply = myAgent.sendCommand(cmd);
                    } catch (Exception e) {
                    e.printStackTrace();
                    }

                    cmd = "set "+seId+" currentPos_X="+ myAgent.currentPos_X;
                    try {
                    reply = myAgent.sendCommand(cmd);
                    } catch (Exception e) {
                    e.printStackTrace();
                    }

                    cmd = "set "+seId+" currentPos_Y="+ myAgent.currentPos_Y;
                    try {
                    reply = myAgent.sendCommand(cmd);
                    } catch (Exception e) {
                    e.printStackTrace();
                    }


                    } else if (msg.getPerformative() == ACLMessage.INFORM){

                    // Si se trata de un mensaje con informaci�n, se tiene que procesar con la estructura "StructTranspResults"
                    Gson gson = new Gson();
                    StructTransportUnitState javaTranspResults = gson.fromJson(msg.getContent(), StructTransportUnitState.class);


        // En la primera parte se hace lo mismo
        //TOD O: sacar la parte de c�digo com�n a los dos casos a un m�todo auxiliar

        myAgent.battery = javaTranspResults.getBattery();
        myAgent.currentPos_X = javaTranspResults.getOdom_x();
        myAgent.currentPos_Y = javaTranspResults.getOdom_y();
        float initialTimeStamp = javaTranspResults.getSeconds();
        float finalTimeStamp = javaTranspResults.getSeconds();

        // Se actualiza el valor de estos par�metros en el SystemModelAgent
        String cmd = "set "+seId+" battery="+ myAgent.battery;
        ACLMessage reply = null;
        try {
        reply = myAgent.sendCommand(cmd);
        } catch (Exception e) {
        e.printStackTrace();
        }

        cmd = "set "+seId+" currentPos_X="+ myAgent.currentPos_X;
        try {
        reply = myAgent.sendCommand(cmd);
        } catch (Exception e) {
        e.printStackTrace();
        }

        cmd = "set "+seId+" currentPos_Y="+ myAgent.currentPos_Y;
        try {
        reply = myAgent.sendCommand(cmd);
        } catch (Exception e) {
        e.printStackTrace();
        }

        // Despu�s, se informa a la m�quina que solicit� el servicio de que este ha sido completado
        //TOD O: enviar un mensaje de confirmaci�n de servicio realizado al machine agent que corresponda (hablar en la reuni�n)

           // myAgent.ActualState = javaTranspResults.getTransport_unit_state();

            while (myAgent.ActualState != "ACTIVE"){

            }

        // Primero se obtienen los nombres del agente que solicit� el servicio y del agente que recibe el servicio
        AID serviceRequester = new AID(myAgent.transportPlan.get(1).get(3).get(1), false);
        AID serviceReceiver = new AID(myAgent.transportPlan.get(1).get(3).get(2), false);

        // Se env�a un mensaje al solicitante del servicio
        sendACLMessage(ACLMessage.INFORM,serviceRequester,"data",myAgent.getLocalName()+"_"+ conversationId++,"initialTimeStamp="+ initialTimeStamp +" finalTimeStamp="+finalTimeStamp,myAgent);

        // Se comprueba si el solicitante y el receptor del servicio son distintos agentes, para enviar un segundo mensaje si es necesario
        if (serviceReceiver != serviceRequester){
        sendACLMessage(ACLMessage.INFORM,serviceReceiver,"data",myAgent.getLocalName()+"_"+ conversationId++,"initialTimeStamp="+ initialTimeStamp +" finalTimeStamp="+finalTimeStamp,myAgent);
        }

        // Se contin�a eliminando esta tarea del plan (la primera tarea de la lista ocupa la segunda posici�n del submodelo)
        myAgent.transportPlan.remove(1);

        // Por �ltimo, se resetea el flag para indicar que el transaporte ha quedado libre
        workInProgress=false;
        }
        }

 */

