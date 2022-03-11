package es.ehu.domain.manufacturing.agents.functionality;

import com.google.gson.Gson;
import es.ehu.domain.manufacturing.agents.TransportAgent;
import es.ehu.domain.manufacturing.utilities.StructTranspRequest;
import es.ehu.domain.manufacturing.utilities.StructTranspResults;
import es.ehu.domain.manufacturing.utilities.StructTranspState;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.NegotiatingBehaviour;
import es.ehu.domain.manufacturing.template.interfaces.AssetManagement;
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

    /* Nombre del gatewayAgent con el que interactúa el agente */
    private AID gatewayAgentID =null;

    /* Identificador de la conversación iniciada por el agente transporte */
    private int conversationId = 0;


    /* OPERACIONES DE INICIALIZACIÓN Y PUESTA EN MARCHA */

    @Override
    public Void init(MWAgent mwAgent) {

        /* Se hace el cambio de tipo */
        this.myAgent = (TransportAgent) mwAgent;
        LOGGER.entry();

        /* Se guarda el nombre del gatewayAgent con el que interactúa el agente */
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
        sendACLMessage(ACLMessage.REQUEST,gatewayAgentID,"ping",myAgent.getLocalName()+"_"+ conversationId++,"",myAgent);
        ACLMessage answer_gw = myAgent.blockingReceive(MessageTemplate.MatchOntology("ping"), 300);
        if(answer_gw==null){
            System.out.println("GW is not online. Start GW and repeat.");
            System.exit(0);
        }

        /* Paso 2: contacto con el asset a través del gatewayAgent y espero respuesta (si no hay, no se puede conitnuar con el registro) */
        sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "check_asset",myAgent.getLocalName()+"_"+ conversationId++,"ask_state",myAgent);
        ACLMessage answer = myAgent.blockingReceive(MessageTemplate.MatchOntology("asset_state"), 300);
        if(answer!=null){

            Gson gson = new Gson();
            StructTranspState javaTranspState = gson.fromJson(answer.getContent(), StructTranspState.class);

            if(javaTranspState.getassetLiveness()){
                System.out.println("The asset is ready to work.");
                myAgent.battery = javaTranspState.getBattery();
                myAgent.currentPos = javaTranspState.getCurrentPos();
            } else {
                /* Si se recibe respuesta pero no es la adecuada, significa que el asset no está listo y no se puede continuar con el registro */
                System.out.println("The asset is not ready to work. 11");
                System.exit(0);
            }
        }else{
            /* Si no se recibe respuesta, significa que el asset no está listo y no se puede continuar con el registro */
            System.out.println("The asset is not ready to work. 22");
            System.exit(0);
        }

        /* Si la comunicación con el asset es correcta, se procede a registrar el agente transporte en el SystemModelAgent */
        String attribs = " battery="+ myAgent.battery + " currentPos="+myAgent.currentPos;
        String cmd = "reg transport parent=system"+attribs;

        ACLMessage reply = null;

        try {
            reply = myAgent.sendCommand(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }

        seId = reply.getContent();

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

        return null;

    }


    /* OPERACIONES DE ACTUALIZACIÓN DE LA LISTA DE TAREAS */

    @Override
    public Object execute(Object[] input) {

        if (input[0] != null) {
            ACLMessage msg = (ACLMessage) input[0];
            myAgent.Acknowledge(msg,myAgent);

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

                /* Se declaran dos arraylist de string en los que se guarda el nombre y el valor de los atributos */
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

                }

                /* Por último, se rellena el arraylist operationInfo y se graba la operación en el plan del transporte */
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


    /* OPERACIONES DE NEGOCIADO DE ASIGNACIÓN DE TAREAS */

    @Override
    public long calculateNegotiationValue(String negAction, String negCriterion, Object... negExternalData) {

        //TODO implementar el método. Para ello hay que:
        //Leer la pila de tareas (obtener todas las coordenadas)
        //Estimar un valor de negociación en base a estas (tiempo o distancia, preferiblemente tiempo)
        //Devolver el valor obtenido

        /* Se van a recibir dos campos en el objeto negExternalData:
        *  En el primer campo, se recibirá la operación (u operaciones) que tendrá que añadir a su pila el transporte ganador
        *  En el segundo campo, se recibirá el nombre del agente máquina que solicita la operación */
        String positionsArray = (String) negExternalData[0];
        String machineAgentName = (String) negExternalData[1]; /* Creo que este parámetro no debería de hacer falta para este método, pero por si acaso */

        /* If redundante solo para confirmar que la petición se ajusta a un criterio que tenga sentido para el agente
        *  Este if cobrará más sentido si en el futuro se consideran varios criterios posibles a elección del solicitante */
        //TODO: cambiar el criterio "position" por "battery" (luego ya lo cambio yo en el resto de sitios que haga falta)
        if (negCriterion.equals("position")){

        }

        return 0; /* Este 0 solo lo he puesto para que no de problemas al compilar, luego habrá que sustituirlo por lo que corresponda */
    }

    @Override
    public int checkNegotiation(String conversationId, String sAction, double negReceivedValue, long negScalarValue, boolean tieBreak, boolean checkReplies, boolean isPartialWinner, Object... negExternalData) {

        LOGGER.entry(conversationId, sAction, negReceivedValue, negScalarValue);

        /* Se van a recibir dos campos en el objeto negExternalData:
         *  En el primer campo, se recibirá la operación (u operaciones) que tendrá que añadir a su pila el transporte ganador
         *  En el segundo campo, se recibirá el nombre del agente máquina que solicita la operación */
        String positionsArray = (String) negExternalData[0];
        String machineAgentName = (String) negExternalData[1];

        /* Se comparan el valor propio y el valor recibido */
        if (negReceivedValue<negScalarValue) return NegotiatingBehaviour.NEG_LOST; /* pierde la negociación */
        if ((negReceivedValue==negScalarValue) && !tieBreak ) return NegotiatingBehaviour.NEG_LOST; /* empata la negociación pero no es quien fija el desempate */

        LOGGER.info("negotiation(id:"+conversationId+") partial winner "+myAgent.getLocalName()+"(value:"+negScalarValue+")");
        if (!checkReplies) return NegotiatingBehaviour.NEG_PARTIAL_WON; /* es el ganador parcial, pero faltan negociaciones por finalizar */


        if (!isPartialWinner) return NegotiatingBehaviour.NEG_LOST; /* Para ser el ganadores verdadero un agente tendrá que ser ganador parcial en cada momento */

        /* El agente es el ganador final (ha ganado todas las comparaciones) */
        LOGGER.info("ejecutar "+sAction);
        Cmd action = new Cmd(sAction);

        if (action.cmd.equals("supplyConsumables")) {
            LOGGER.info("id=" + action.who);

            //TODO implementar las acciones que tiene que hacer el agente ganador. Para ello hay que:
            //Encapsular la operación (u operaciones) recibida en un mensaje ACL e invocar el método Execute
            //Una vez añadida la operación al plan, enviar un mensaje al agente que solicitó el servicio (machineAgentName)
            //Este mensaje deberá informar de qué transporte ha ganado la negociación y el momento de entrega estimado (solo en caso de que las negociaciones se hagan en base a tiempos)
        }

        return NegotiatingBehaviour.NEG_WON;

    }


    /* OPERACIONES DE INTERACCIÓN CON EL ASSET */

    @Override
    public void sendDataToDevice() {

        /* En primer lugar se comprueba si el transporte está realizando alguna operación (workInProgress=TRUE) */
        if (workInProgress !=true){

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

                /* Ahora se formatea la estructura a json*/
                Gson gson = new Gson();
                gson.toJson(javaTranspRequest);

                /* Después, se prepara el mensaje y se envía al gatewayAgent */
                sendACLMessage(ACLMessage.REQUEST,gatewayAgentID,"data",myAgent.getLocalName()+"_"+ conversationId++, String.valueOf(gson),myAgent);

                /* Por último, pongo el workInProgress a true*/
                workInProgress=true;

            } else { /* Si el transporte está libre y además no tiene tareas asignadas, debe volver a la estación de carga */

                //TODO: Enviar un mensaje al transporte para hacer que vuelva al puesto de carga (hablar en la reunión)

            }
        }else{
            System.out.println("The asset is busy"); /* Dejar para debug, luego comentar o quitar*/
        }
    }

    @Override
    public void rcvDataFromDevice(ACLMessage msg) {

        /* Se recibe el mensaje del gatewayAgent y se filtra por performativa*/
        if (msg.getPerformative() == ACLMessage.CONFIRM){

            /* Si se trata de un mensaje de confirmación, se tiene que procesar con la estructura "StructTranspState" */
            Gson gson = new Gson();
            StructTranspState javaTranspState = gson.fromJson(msg.getContent(), StructTranspState.class);

            myAgent.battery = javaTranspState.getBattery();
            myAgent.currentPos = javaTranspState.getCurrentPos();

            /* Se actualiza el valor de estos parámetros en el SystemModelAgent */
            String cmd = "set "+seId+" battery="+ myAgent.battery;
            ACLMessage reply = null;
            try {
                reply = myAgent.sendCommand(cmd);
            } catch (Exception e) {
                e.printStackTrace();
            }

            cmd = "set "+seId+" currentPos="+ myAgent.currentPos;
            try {
                reply = myAgent.sendCommand(cmd);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (msg.getPerformative() == ACLMessage.INFORM){

            /* Si se trata de un mensaje con información, se tiene que procesar con la estructura "StructTranspResults" */
            Gson gson = new Gson();
            StructTranspResults javaTranspResults = gson.fromJson(msg.getContent(), StructTranspResults.class);

            /* En la primera parte se hace lo mismo*/
            //TODO: sacar la parte de código común a los dos casos a un método auxiliar
            myAgent.battery = javaTranspResults.getBattery();
            myAgent.currentPos = javaTranspResults.getCurrentPos();
            float initialTimeStamp = javaTranspResults.getInitial_timeStamp();
            float finalTimeStamp = javaTranspResults.getFinal_timeStamp();

            /* Se actualiza el valor de estos parámetros en el SystemModelAgent */
            String cmd = "set "+seId+" battery="+ myAgent.battery;
            ACLMessage reply = null;
            try {
                reply = myAgent.sendCommand(cmd);
            } catch (Exception e) {
                e.printStackTrace();
            }

            cmd = "set "+seId+" currentPos="+ myAgent.currentPos;
            try {
                reply = myAgent.sendCommand(cmd);
            } catch (Exception e) {
                e.printStackTrace();
            }

            /* Después, se informa a la máquina que solicitó el servicio de que este ha sido completado */
            //TODO: enviar un mensaje de confirmación de servicio realizado al machine agent que corresponda (hablar en la reunión)

            /* Primero se obtienen los nombres del agente que solicitó el servicio y del agente que recibe el servicio */
            AID serviceRequester = new AID(myAgent.transportPlan.get(1).get(3).get(1), false);
            AID serviceReceiver = new AID(myAgent.transportPlan.get(1).get(3).get(2), false);

            /* Se envía un mensaje al solicitante del servicio */
            sendACLMessage(ACLMessage.INFORM,serviceRequester,"data",myAgent.getLocalName()+"_"+ conversationId++,"initialTimeStamp="+ initialTimeStamp +" finalTimeStamp="+finalTimeStamp,myAgent);

            /* Se comprueba si el solicitante y el receptor del servicio son distintos agentes, para enviar un segundo mensaje si es necesario */
            if (serviceReceiver != serviceRequester){
                sendACLMessage(ACLMessage.INFORM,serviceReceiver,"data",myAgent.getLocalName()+"_"+ conversationId++,"initialTimeStamp="+ initialTimeStamp +" finalTimeStamp="+finalTimeStamp,myAgent);
            }

            /* Se continúa eliminando esta tarea del plan (la primera tarea de la lista ocupa la segunda posición del submodelo) */
            myAgent.transportPlan.remove(1);

            /* Por último, se resetea el flag para indicar que el transaporte ha quedado libre*/
            workInProgress=false;
        }
    }


    /* OPERACIONES DE FINALIZACIÓN DEL AGENTE TRANSPORTE*/

    @Override
    public Void terminate(MWAgent myAgent) {return null;}
}

