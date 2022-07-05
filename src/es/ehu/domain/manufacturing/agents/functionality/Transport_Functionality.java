package es.ehu.domain.manufacturing.agents.functionality;

import com.google.gson.Gson;
import es.ehu.domain.manufacturing.agents.TransportAgent;
import es.ehu.domain.manufacturing.utilities.StructTranspRequest;
import es.ehu.domain.manufacturing.utilities.StructTransportUnitState;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.NegotiatingBehaviour;
import es.ehu.platform.template.interfaces.AssetManagement;
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


public class Transport_Functionality extends DomRes_Functionality implements BasicFunctionality, NegFunctionality, AssetManagement {

    static final Logger LOGGER = LogManager.getLogger(Transport_Functionality.class.getName());


    /* DECLARACIÓN DE VARIABLES */

    /* Agente que utiliza esta funcionalidad (se recibe como parámetro en diferentes métodos) */
    private TransportAgent myAgent;

    /* Id del agente en el sistema*/
    private  String seId;

    /* Nombre de la clase (para poder crear el agente transporte definitivo) */
    private String className;

    /* Flag que se activa cuando el transporte esta trabajando */
    private Boolean workInProgress = false;
    private Boolean transportActive = false;

    /* Ultima coordenada enviada al transporte */
    private int TasksNumber;
    private int LeftTaskCounter = 0;
    private int AccomplishedTaskCounter;


    /* Nombre del gatewayAgent con el que interactúa el agente */
    private AID gatewayAgentID =null;

    /* Identificador de la conversación iniciada por el agente transporte */
    private int conversationId = 0;

    /* Flag que permite conocer si el transporte ha vuelto de Operative a Active */
    private boolean TransportOperative = false;


    /* OPERACIONES DE INICIALIZACIÓN Y PUESTA EN MARCHA */

    @Override
    public Void init(MWAgent mwAgent) {

        /* Se hace el cambio de tipo */
        this.myAgent = (TransportAgent) mwAgent;
        LOGGER.entry();

        /* Se guarda el nombre del gatewayAgent con el que interactúa el agente */
        /* myAgent.resourceName recoge el primer argumento de entrada en la ejecucion, por ejemplo: */
        /* ControlGatewayContT_01 si T_01 es el argumento*/
        mwAgent.gatewayAgentName = "ControlGatewayCont"+myAgent.resourceName;
        gatewayAgentID = new AID(myAgent.gatewayAgentName, false);

        /* Se leen los argumentos con los que se ha llamado al agente y se comprueba si tiene un id */
        String [] args = (String[]) myAgent.getArguments();

        for (int i=0; i<args.length; i++){
            /* En caso afirmativo, es el agente transporte definitivo y no hay que hacer nada más */
            if (args[i].toLowerCase().startsWith("id=")) return null;
        }

        /* En caso negativo, se trata del agente auxiliar y hay que realizar más acciones */
        /* En primer lugar, hay que comprobar la conectividad con el asset en dos pasos: */
        /* Paso 1: contacto con el gatewayAgent y espero respuesta (si no hay, no se puede continuar con el registro) */
        //sendACLMessage(ACLMessage.REQUEST,gatewayAgentID,"ping",myAgent.getLocalName()+"_"+ conversationId++,"",myAgent);
        //sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "ping","0",null,myAgent);
        //ACLMessage answer_gw = myAgent.blockingReceive(MessageTemplate.MatchOntology("ping"), 1000);

        //if(answer_gw==null){
        //    System.out.println("GW is not online. Start GW and repeat.");
        //    System.exit(0);
        //}

        /* Paso 2: contacto con el asset a través del gatewayAgent y espero respuesta (si no hay, no se puede conitnuar con el registro) */
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

            if(!Objects.equals(myAgent.ActualState, "Undefined") && !Objects.equals(myAgent.ActualState, "Error") && !Objects.equals(myAgent.ActualState, "Stop")){
                System.out.println("The asset is ready to work: " +myAgent.ActualState);
                myAgent.battery = javaTranspState.getBattery();
                myAgent.currentPos_X = javaTranspState.getOdom_x();
                myAgent.currentPos_Y = javaTranspState.getOdom_y();
                sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "asset_checked","0",null,myAgent);

            } else {
                /* Si se recibe respuesta pero no es la adecuada, significa que el asset no está listo y no se puede continuar con el registro */
                /* Para que la respuesta sea adecuada, el transporte debe de estar disponible para operar, es decir, que su estado debe de ser*/
                /* diferente a UNDEFINED, STOP y ERROR */
                System.out.println("The asset is not ready to work. Trasnport not ready.");
                System.exit(0);
            }
        }else{
            /* Si no se recibe respuesta, significa que el asset no está listo y no se puede continuar con el registro */
            System.out.println("The asset is not ready to work. No response from transport");
            System.exit(0);
        }

        sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "asset_checked","0",null,myAgent);

        /* Si la comunicación con el asset es correcta, se procede a registrar el agente transporte en el SystemModelAgent */
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
            /* Una vez registrado el agente transporte, es creado por el agente auxiliar, pasándole como argumento su id */
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


    /* OPERACIONES DE ACTUALIZACIÓN DE LA LISTA DE TAREAS */

    @Override
    public Object execute(Object[] input) {

        if (input[0] != null) {

            ACLMessage msg = (ACLMessage) input[0];
//            Acknowledge(msg,myAgent);

            /* Se guardan en un array de String todas las posibles operaciones recibidas */
            String[] allOperations = msg.getContent().split("&");

            /* Se recorre el array para ir grabando las operaciones una a una*/
            for (String singleOperation : allOperations) {

                /* Se declara un arraylist para guardar la información sobre cada operación recibida */
                ArrayList<ArrayList<String>> operationInfo = new ArrayList<>();

                /* Se crean y se rellenan dos arraylist de string para la cabecera del listado de operaciones */
                ArrayList<String> elementName = new ArrayList<>();
                ArrayList<String> hierarchyLevel = new ArrayList<>();
                elementName.add("operation");
                hierarchyLevel.add("2");

                /* Se declaran dos arraylist de string en los que se guardas el nombre y el valor de los atributos */
                ArrayList<String> attNames = new ArrayList<>();
                ArrayList<String> attValues = new ArrayList<>();

                /* Se guardan en un array de String todos los atributos de la operación */
                String [] allAttributes = singleOperation.split(" ");

                /* Se recorre el array para ir grabando los atributos uno a uno */
                for (String singleAttribute : allAttributes){

                    /* Se separa el nombre del atributo de los valores */
                    String [] attInfo = singleAttribute.split("=");
                    attNames.add(attInfo[0]);
                    attValues.add(attInfo[1]);
                    //attValues.add("transport1");

                }

                /* Por último, se rellena el arraylist operationInfo y se graba la operación en el plan del transporte */
                operationInfo.add(0, elementName);
                operationInfo.add(1, hierarchyLevel);
                operationInfo.add(2, attNames);
                operationInfo.add(3, attValues);
                myAgent.transportPlan.add(operationInfo);


                // Se actualiza el valor de las tareas introducidas al TransportPlan
                LeftTaskCounter = LeftTaskCounter + 1;

                /* El comando que se introduce al TransportPlan.xml incrusta una linea de codigo como la siguiente:
                 * <operation positions="dockingStation,kukaInput,kukaOutput,kukaInput" requester="transport1" receiver="transport1"> </operation>*/

            }

            return LOGGER.exit("done");

        } else {
            return LOGGER.exit(null);
        }
    }



    /* OPERACIONES DE NEGOCIADO DE ASIGNACIÓN DE TAREAS */

    @Override
    public long calculateNegotiationValue(String negAction, String negCriterion, Object... negExternalData) {

        long negValue = 0; // Valor de la negociacion, va desde 0 hasta 10, siendo este ultimo la maxima puntuacion
        double dnegValue = 0.0; // Valor auxiliar de la negociacion

        /* Se van a recibir dos campos en el objeto negExternalData:
        *  En el primer campo, se recibirá la operación (u operaciones) que tendrá que añadir a su pila el transporte ganador
        *  En el segundo campo, se recibirá el nombre del agente máquina que solicita la operación */
        String positionsArray = (String) negExternalData[0]; //Coordenada1;Coordenada2
        String machineAgentName = (String) negExternalData[1]; //requester,receiver

        /* Se verifica bajo que criterio se quiere negociar. Posteriormente, se le dara una puntuacion al agente
         * transporte dentro de la negociacion segun los datos que reciba desde el transporte que represente. */

        if (negCriterion.equals("position")){

            // Se calculara en funcion de la posicion actual del transporte

        }

        else if (negCriterion.equals("battery")){

            /* Se calculara en funcion de la bateria actual del transporte */

            /* La bateria de los transportes Turtlebot2 esta formada por celdas del tipo Lithium-ion, concretamente con
             * una configuracion de 4s1p. Esta configuracion de 4 celdas en serie y 1 en paralelo, le da una tension
             * nominal de 14.8 VDC con una capacidad de 2200 mAh; en torno a 2 o 3 horas de autonomia, en funcion del
             * estado de salud de las celdas de cada transporte y de las tareas asignadas. Es decir, cada celda cuenta
             * con 3.7 VDC de tension nominal y 2200 mAh. */

            /* La principal caracteristica de las celdas tipo Lithium-ion a la hora de calcular la cantidad de energia
             * que contienen, es decir, calcular el SOC (State of Charge), es la no linealidad de la curva de descarga
             * de estas celdas. Esta curva, cuenta con dos regiones exponenciales negativas con pendiente muy pronunciadas
             * y una region plana, donde la tension varia muy poco. Es esta region plana la que supone un problema
             * considerable, al significar que 14.8 VDC puede equivaler al 80 % de su nivel de energia o bien al
             * 20 % de su energia. */

            /* Al no contar con un modelo de la bateria de los transportes, y al ser necesario el contar con un estimador
             * del SOC para cada transporte segun la cantidad de energia que se va consumiendo en funcion de los perifericos
             * que tiene conectados, se va a implementar un modelo muy simple basado en el nivel de bateria que devuelve
             * el topico /flexmansys/state/TUN*/

            /* Se va a emplear la siguiente curva de descarga, basada en la curva de descarga nominal aportada por un
             * usuario en ROS.org para un Turtletbot2
             *
             *      [VDC] Bateria
             * 16.8   | X
             *        |
             *        |
             *        |
             *        |
             * 14.8   |    X
             *        |
             *        |
             * 13.6   |                                                                 X
             *        |
             *        |
             * 13.2   |                                                                        X
             *        |
             *        |________________________________________________________________________________ [%] Energia
             *        100  80                                                           20     0
             *
             * De este grafico se sacan 3 regiones, las cuales se van a linealizar por separado:
             *
             * Region Alta  [Highland] 100 % - 80 % => 16.8 VDC a 14.8 VDC => Energia = 10*Bateria - 68
             * Region Media [Midland]   80 % - 20 % => 14.8 VDC a 13.6 VDC => Energia = 50*Bateria - 660
             * Region Baja  [Lowland]   20 % -  0 % => 13.6 VDC a 13.2 VDC => Energia = 50*Bateria - 660
             *
             *
             * En funcion del nivel de bateria leido desde el transporte, se asginara una de las tres regiones. La
             * posicion que ocupe el nivel de bateria en el espectro de energia restante [%], sera la puntuacion
             * obtenida por el transporte x100.
             * */

            if (myAgent.battery >= 14.8){

                dnegValue = (10.0*myAgent.battery-68.0)/10.0;

            }

            else if (myAgent.battery < 14.8 && myAgent.battery >= 13.6){

                dnegValue = (50.0*myAgent.battery-660.0)/10.0;

            }

            else if (myAgent.battery < 13.6){

                dnegValue = (50.0*myAgent.battery-660.0)/10.0;

            }

            else {

                dnegValue = 0.0;
            }


        }

        /* El metodo checkNegotiation recibe el valor de CalculateNegotiation con un tipo Long, luego truncamos el valor
         * tipo double calculado de dnegValue, como valor de negociacion. Si el valor es X.5 o mas, trunca hacia arriba,
         * en caso de ser menos de X.5, trunca hacia abajo. */

        negValue = Math.round(dnegValue);

        return negValue;

    }

    @Override
    public int checkNegotiation(String conversationId, String sAction, double negReceivedValue, long negScalarValue, boolean tieBreak, boolean checkReplies, boolean isPartialWinner, Object... negExternalData) {

        LOGGER.entry(conversationId, sAction, negReceivedValue, negScalarValue);

        /* Se van a recibir dos campos en el objeto negExternalData:
         *  En el primer campo, se recibirá la operación (u operaciones) que tendrá que añadir a su pila el transporte ganador
         *  En el segundo campo, se recibirá el nombre del agente máquina que solicita la operación */

        String positionsArray = (String) negExternalData[0];


        // Se obtienen los AID de los agentes requester y receiver, siendo Requester el primero AID que se recibe
        // en el content del mensaje de externalData

        String RequesterAID = (String) negExternalData[1];
        String ReceiverAID = (String) negExternalData[2];

        /* Se comparan el valor propio y el valor recibido */

        /* pierde la negociación, la negociacion es a MAXIMOS */
        if (negReceivedValue>negScalarValue) return NegotiatingBehaviour.NEG_LOST;

        /* empata la negociación pero no es quien fija el desempate */
        if ((negReceivedValue==negScalarValue) && !tieBreak ) return NegotiatingBehaviour.NEG_LOST;

        LOGGER.info("negotiation(id:"+conversationId+") partial winner "+myAgent.getLocalName()+"(value:"+negScalarValue+")");

        /* es el ganador parcial, pero faltan negociaciones por finalizar */
        if (!checkReplies) return NegotiatingBehaviour.NEG_PARTIAL_WON;

        /* Para ser el ganadores verdadero un agente tendrá que ser ganador parcial en cada momento */
        if (!isPartialWinner) return NegotiatingBehaviour.NEG_LOST;

        /* El agente es el ganador final (ha ganado todas las comparaciones) */
        LOGGER.info("ejecutar "+sAction);
        Cmd action = new Cmd(sAction);

        if (action.cmd.equals("supplyConsumables")) {
            LOGGER.info("id=" + action.who);

            // Recogemos cada coordenada del plan de transporte en un string
            String[] allOperations = positionsArray.split(";");

            // Trasnformamos la separacion por ; a , para que sea entendible por el TransportPlan.xml correspondiente
            for (int i = 0; i < allOperations.length; i++) {

                if(i==0) {
                    positionsArray = allOperations[i] + ",";
                }

                else if (i>0 && i < allOperations.length - 1){
                    positionsArray = positionsArray + allOperations[i] + ",";
                }

                else if (i >= allOperations.length - 1){
                    positionsArray = positionsArray + allOperations[i];
                }

            }

            String TA_name = myAgent.getLocalName();

            /* Se construyen los argumentos que se introduciran en el TransportPlan.xml */
            positionsArray = "positions=" + positionsArray + " " + "requester=" + RequesterAID + " " + "receiver=" + ReceiverAID;

            // Se encapsulan las operaciones en un mensaje tipo ACL
            ACLMessage positionsCoordinates = new ACLMessage(ACLMessage.INFORM);
            positionsCoordinates.setContent(positionsArray);

            Object[] data = new Object[1];
            data[0]=positionsCoordinates;
            execute(data);

            /* Tras introducir el plan requerido en el TransportPlan.xml, se le indica al agente que solicito el
             * el transporte que ha ganado la negociacion. */

            // Mensaje que notifica quien es el ganador de la negociacion al agente requester del servicio de transporte

            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID(RequesterAID, AID.ISLOCALNAME));
            //msg.setContent("I am the winner of:" + seOperationID);
            msg.setContent("El ganador de la negociacion es: " + TA_name);
            msg.setConversationId(conversationId);
            myAgent.send(msg);

        }

        return NegotiatingBehaviour.NEG_WON;

    }

    /* OPERACIONES DE INTERACCIÓN CON EL ASSET */

    @Override
    public void sendDataToDevice() {

        /* En primer lugar se comprueba si el transporte está realizando alguna operación (workInProgress=TRUE) */
        if (!workInProgress){

            // Si entramos aqui, el Transporte se encuentra libre y sin un plan asignado

            /* Si el transporte está libre, se comprueba si hay tareas en el plan
            * La cabecera del modelo ocupa una posición, por lo que para que haya tareas, el tamaño del modelo tiene que ser de 2 o más */
            if (myAgent.transportPlan.size() >= 2) {

                /* Se leen las posiciones de la primera operación del plan (el tercer elemento del plan) */
                /* Recordatorio: del segundo elemento del modelo (primera operación), se obtiene el cuarto elemento (valores de sus atributos)...
                y de se lee la primera posición que tiene (un string con todas las coordenadas) */
                /* Las coordenadas están unidas por comas, y se separan para obtener el array de String que queremos enviar */
                String [] allPositions = myAgent.transportPlan.get(1).get(3).get(0).split(",");

                /* El siguiente paso es transformar cada posición en una coordenada que el asset pueda identificar
                *  Primero, se crea un array en el que se van a guardar las coordenadas, de la misma longitud que el array de posiciones */
                String [] allCoordinates = new String[allPositions.length];

                /* Después, se recorre el array de posiciones y se van guardando las coordenadas correspondientes en el array de coordenadas */
                for (int i = 0; i < allPositions.length; i++) {

                    /* Para cada posición tengo que encontrar la coordenada correspondiente
                    *  Para ello, recorro el submodelo de posiciones clave */
                    for (int j = 0; j<myAgent.keyPosition.get(0).get(2).size(); j++){

                        /* Compruebo si la posición i del array allPositions coincide con la posición j de keyPosition */
                        if (myAgent.keyPosition.get(0).get(2).get(j).equals(allPositions[i])){

                            /* Si coinciden, obtengo la coordenada */
                            allCoordinates[i]=myAgent.keyPosition.get(0).get(3).get(j);
                        }
                    }
                }

                /* Declaro la estructura en la que voy a formatear el mensaje */
                StructTranspRequest javaTranspRequest = new StructTranspRequest();
                javaTranspRequest.setTask(allCoordinates);

                /* Solo se enviara al transporte su lista tareas siempre y cuando se encuentre en estado ACTIVE */

                if (transportActive){

                    // Cuando se inicia una conversacion con el Gateway con la ontologia PlanCoordenadas, sabe que se trata de
                    // una conversacion tipo serie. Es decir, que habra un START, unos DATOS y un END.

                    // En este caso, el START indica la cantidad de coordenadas que tiene la lista de tareas, es decir,
                    // el numero de DATOS que va a recibir de la trama de mensaje.

                    // transportAgent ==>  START | DATA1 | DATA2 | DATA3 | DATA4 | END ==> GWagentROS
                    // GWagentROS ==> DATA1 => TransportUnit ...  GWagentROS ==> DATA2 => TransportUnit ...

                    sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "PlanCoordenadas", "1234", String.valueOf(allCoordinates.length), myAgent);

                    for (int i = 0; i < allCoordinates.length; i++) {

                        // Posteriormente, se envian todos los DATOS de la lista de tareas, es decir, todas las coordenadas.
                        sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "PlanCoordenadas", "1234", allCoordinates[i], myAgent);

                        if(i == allCoordinates.length-1){

                            TasksNumber = allCoordinates.length;

                        }

                    }

                    // Finalmente, se le hace saber al Gateway que no recibira ninguna coordenada mas con el contenido END.
                    sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "PlanCoordenadas", "1234", "END", myAgent);

                    /* Por último, pongo el workInProgress a true*/
                    workInProgress = true;
                }

            } else {

                /* Si el transporte está libre y además no tiene tareas asignadas, debe volver a la estación de carga */
                //Si no tiene ninguna tarea mas asignada, el transporte debe de volver a su estacion de carga

                //sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "ComandoCoordenada", "1234", "X", myAgent);

            }
        }else{

            // Este else sobra, comprobar

        }
    }

    @Override
    public void rcvDataFromDevice(ACLMessage msg) {

        /* Se recibe el mensaje del gatewayAgent y se filtra por estado del transporte*/

        Gson gson = new Gson();
        StructTransportUnitState javaTranspState = gson.fromJson(msg.getContent(), StructTransportUnitState.class);

        myAgent.ActualState = javaTranspState.getTransport_unit_state();
        myAgent.transport_unit_name = javaTranspState.getTransport_unit_name();
        myAgent.battery = javaTranspState.getBattery();
        myAgent.currentPos_X = javaTranspState.getOdom_x();
        myAgent.currentPos_Y = javaTranspState.getOdom_y();

        if (!Objects.equals(myAgent.ActualState, "Active")){

            transportActive = false;

            /* El transporte o bien se encuentra en un estado que no puede operar o bien se encuentra operando */
            /* Enviamos al SMA la informacion que proviene desde el transporte */

            /* Antes de considerar al transporte como operable, verificamos que ha llevado a cabo su etapa de
            /* calibracion. En caso de no estar calibrado, se le envia el comando de calibracion */

            if (Objects.equals(myAgent.ActualState, "Idle") || (Objects.equals(myAgent.ActualState, "Localization"))){

                if (Objects.equals(myAgent.ActualState, "Idle")) {

                    sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "ComandoCoordenada", "1234", "X", myAgent);
                    myAgent.ActualState = javaTranspState.getTransport_unit_state();
                    System.out.println(myAgent.transport_unit_name + " Transport needs calibration");
                    sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "ComandoCoordenada", "1234", "X", myAgent);
                    sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "ComandoCoordenada", "1234", "X", myAgent);

                }

                else if (Objects.equals(myAgent.ActualState, "Localization")){

                    myAgent.ActualState = javaTranspState.getTransport_unit_state();
                    System.out.println(myAgent.transport_unit_name + " Transport is calibrating");

                }

            }

            if (Objects.equals(myAgent.ActualState, "Recovery")){

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

            // Se actualiza el valor de estos parámetros en el SystemModelAgent
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

            if (myAgent.ActualState.equals("Operative")){

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

        } else if (myAgent.ActualState.equals("Active")){

            transportActive = true;

            // El transporte o bien acaba de terminar una operacion o bien esta listo para operar y recibir tareas
            System.out.println(myAgent.transport_unit_name + " Transport is active");

            // Se actualiza el valor de estos parámetros en el SystemModelAgent
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

                myAgent.hour = javaTranspState.getHour();
                myAgent.minute = javaTranspState.getMinute();
                myAgent.seconds = javaTranspState.getSeconds();

                myAgent.finalTimeStamp = myAgent.hour + ":" + myAgent.minute + ":" + myAgent.seconds + " h";
                System.out.println(myAgent.transport_unit_name + " Transport ended his task at " + myAgent.finalTimeStamp);

                /* Se envia un mensaje al solicitante del servicio indicando el estado de lo que ha solicitado, sin embargo,
                 * unicamente lo hara si existe una operacion recogida dentro del TransportPlan.xml*/

                if (LeftTaskCounter > 0) {
                    AID serviceRequester = new AID(myAgent.transportPlan.get(1).get(3).get(1), false);
                    AID serviceReceiver = new AID(myAgent.transportPlan.get(1).get(3).get(2), false);

                    // Se envía un mensaje al solicitante del servicio
                    sendACLMessage(ACLMessage.INFORM, serviceRequester, "data", myAgent.getLocalName() + "_" + conversationId++, "initialTimeStamp=" + myAgent.initialTimeStamp + " finalTimeStamp=" + myAgent.finalTimeStamp, myAgent);

                }

                AccomplishedTaskCounter = AccomplishedTaskCounter + 1;

                //if (transportDockingRequired) {
                System.out.println("Number of Accomplished Tasks: " + AccomplishedTaskCounter );

                if (TasksNumber == AccomplishedTaskCounter) {

                    // Esta seccion unicamente se ejecutara cuando reciba un final de servicio por parte del transporte
                    // en este caso cuando el numero de tareas ejecutadas coincida con el numero de tareas enviadas

                    AID serviceRequester = new AID(myAgent.transportPlan.get(1).get(3).get(1), false);
                    AID serviceReceiver = new AID(myAgent.transportPlan.get(1).get(3).get(2), false);

                    // Se comprueba si el solicitante y el receptor del servicio son distintos agentes, para enviar un segundo mensaje si es necesario
                    if (serviceReceiver != serviceRequester) {
                        // Mensaje que informa al agente receiver, es decir, el que recibe el servicio de transporte,
                        // que el servicio de transporte ha sido completado y finalizado correctamente.
                        // sendACLMessage(ACLMessage.INFORM, serviceReceiver, "data", myAgent.getLocalName() + "_" + conversationId++, "initialTimeStamp=" + myAgent.initialTimeStamp + " finalTimeStamp=" + myAgent.finalTimeStamp, myAgent);
                        sendACLMessage(ACLMessage.INFORM, serviceReceiver, "data", "ProvidedConsumables", "initialTimeStamp=" + myAgent.initialTimeStamp + " finalTimeStamp=" + myAgent.finalTimeStamp, myAgent);
                    }

                    // Se continúa eliminando esta tarea del plan (la primera tarea de la lista ocupa la segunda posición del submodelo)
                    myAgent.transportPlan.remove(1);

                    // Por último, se resetea el flag para indicar que el transaporte ha quedado libre
                    workInProgress = false;

                    // Se indica que un plan ha sido completado del TransportPlan.xml
                    LeftTaskCounter = LeftTaskCounter - 1;

                    // Se da paso a un nuevo plan reseteando el numero de tareas completadas
                    AccomplishedTaskCounter = 0;

                }

                TransportOperative = false;

            }

        }

    }

    /* OPERACIONES DE FINALIZACIÓN DEL AGENTE TRANSPORTE*/

    @Override
    public Void terminate(MWAgent myAgent) {return null;}
}
