package es.ehu.domain.manufacturing.agents.functionality;

import com.google.gson.Gson;
import es.ehu.domain.manufacturing.agents.MachineAgent;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.NegotiatingBehaviour;
import es.ehu.domain.manufacturing.template.interfaces.AssetManagement;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.NegFunctionality;
import es.ehu.platform.utilities.Cmd;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Machine_Functionality extends DomRes_Functionality implements BasicFunctionality, NegFunctionality, AssetManagement {

    private static final long serialVersionUID = -4307559193624552630L;
    static final Logger LOGGER = LogManager.getLogger(Machine_Functionality.class.getName());

    /* DECLARACIÓN DE VARIABLES */

    /* Agente que utiliza esta funcionalidad (se recibe como parámetro en diferentes métodos) */
    private MachineAgent myAgent;

    /* Id del agente en el sistema*/
    private String seId;

    /* Nombre de la clase (para poder crear el agente máquina definitivo) */
    private String className;

    /* Flag que se activa cuando el transporte esta trabajando */
    private Boolean workInProgress = false;

    /* Nombre del gatewayAgent con el que interactúa el agente */
    private AID gatewayAgentID =null;

    /* Identificador de la conversación iniciada por el agente transporte */
    private Integer conversationId =0;

    /* Identificador del lote que se está procesando */
    private String BatchID = "";

    /* Número de items que hay que fabricar en el servicio actual */
    private Integer NumOfItems = 0;

    /* Flag que indica si hay material suficiente para realizar el servicio que corresponde en cada momento */
    private Boolean materialAvailable = true;

    /* Flag que indica si hay una petición de reponer materiales en marcha */
    private Boolean materialRequest = false;

    /* Estructura de datos que se envía al asset */
    private HashMap msgToAsset = new HashMap();

    /* Estructura de datos que se recibe del asset */
    private HashMap<String,Object> msgFromAsset = new HashMap<>();

    /* Lista de mensajes ACL que se están guardando para enviar al batchAgent cuando corresponda */
    public ArrayList<ACLMessage> posponed_msgs_to_batch= new ArrayList<>();

    /* Contador para los conversationId de los timeStamp */
    private int TMSTMP_cnt=0;

    /* OPERACIONES DE INICIALIZACIÓN Y PUESTA EN MARCHA */

    @Override
    public Void init(MWAgent mwAgent) {

        /* Se obtiene el nombre del método (se utilizará en el conversationId) */
        String methodName = new Machine_Functionality() {}.getClass().getEnclosingMethod().getName();

        /* Se hace el cambio de tipo */
        this.myAgent = (MachineAgent) mwAgent;
        LOGGER.entry();

        /* Se guarda el nombre del gatewayAgent con el que interactúa el agente */
        String machineName = myAgent.resourceName;
        String machineNumber = machineName.split("_")[1];
        myAgent.gatewayAgentName = "ControlGatewayCont" + machineNumber;
        gatewayAgentID = new AID(myAgent.gatewayAgentName, false);

        /* Se leen los argumentos con los que se ha llamado al agente y se comprueba si tiene un id */
        String [] args = (String[]) myAgent.getArguments();

        for (String arg : args) {
            if (arg.toLowerCase().startsWith("id=")) return null;
        }

		/* En caso negativo, se trata del agente auxiliar y hay que realizar más acciones */
		/* En primer lugar, hay que comprobar la conectividad con el asset en dos pasos: */
//		/* Paso 1: contacto con el gatewayAgent y espero respuesta (si no hay, no se puede continuar con el registro) */
//		sendACLMessage(ACLMessage.REQUEST,gatewayAgentID,"ping",
//                myAgent.getLocalName()+"_"+methodName+"_"+conversationId++,"",myAgent);
//		ACLMessage answer_gw = myAgent.blockingReceive(MessageTemplate.MatchOntology("ping"), 300);
//		if(answer_gw==null){
//			System.out.println("GW is not online. Start GW and repeat.");
//			System.exit(0);
//		}
//
//		/* Paso 2: contacto con el asset a través del gatewayAgent y espero respuesta (si no hay, no se puede continuar con el registro) */
//		sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "check_asset",
//                myAgent.getLocalName()+"_"+methodName+"_"+conversationId++,"ask_state",myAgent);
//		ACLMessage answer = myAgent.blockingReceive(MessageTemplate.MatchOntology("asset_state"), 2500);
//		if(answer!=null){
//			if(!answer.getContent().equals("Working")&&!answer.getContent().equals("Not working")){
//				System.out.println("PLC is not prepared to work.");
//				System.exit(0); //si el asset o el gwAgent no están disponibles no tiene sentido que iniciemos el agente
//			}else{
//				System.out.println("PLC is "+answer.getContent());
//			}
//		}else{
//			System.out.println("PLC is not prepared to work.");
//			System.exit(0); //si el asset o el gwAgent no están disponible no tiene sentido que iniciemos el agente máquina
//		}

        /* Si la comunicación con el asset es correcta, se procede a registrar el agente transporte en el SystemModelAgent */
        /* Primero, se registra el listado de materiales disponibles en la estación */
        int index = 0;
        for (int i = 0; i < myAgent.resourceModel.size() - 1; i++) {
            if (myAgent.resourceModel.get(i).get(0).get(0).equals("buffer")){
                myAgent.availableMaterial.add(new HashMap<>());   //Se añaden niveles nuevos para poder ser rellenados con datos
                myAgent.availableMaterial.get(index).put("consumable_id", myAgent.resourceModel.get(i).get(3).get(0));   // Valor de consumable_id
                myAgent.availableMaterial.get(index).put("current", myAgent.resourceModel.get(i).get(3).get(1));   // Valor de piezas disponibles
                myAgent.availableMaterial.get(index).put("max", myAgent.resourceModel.get(i).get(3).get(4));   // Valor de capacidad maxima
                myAgent.availableMaterial.get(index).put("warning", myAgent.resourceModel.get(i).get(3).get(5));   // Valor de warning
                index++;
            }
        }

        /* Segundo, se leen los atributos necesarios del modelo */
        StringBuilder attribs = new StringBuilder();
        for (int j = 0; j < myAgent.resourceModel.get(0).get(2).size(); j++){
            attribs.append(" ").append(myAgent.resourceModel.get(0).get(2).get(j)).append("=").append(myAgent.resourceModel.get(0).get(3).get(j));
        }

        /* Tercero, se añaden las operaciones del asset al string de atributos */
        attribs.append(" simpleOperations=");
        for (int j = 0; j < myAgent.resourceModel.size(); j++){
            if (myAgent.resourceModel.get(j).get(0).get(0).startsWith("simple")){
                for (int k = 0; k < myAgent.resourceModel.get(j).get(2).size();k++){
                    if (myAgent.resourceModel.get(j).get(2).get(k).startsWith("id")) attribs.append(myAgent.resourceModel.get(j).get(3).get(k)).append(",");
                }
            }
        }
        attribs = new StringBuilder(attribs.substring(0, attribs.length() - 1));

        attribs.append(" complexOperations=");
        for (int j = 0; j < myAgent.resourceModel.size(); j++){
            if (myAgent.resourceModel.get(j).get(0).get(0).startsWith("complex")){
                for (int k = 0; k < myAgent.resourceModel.get(j).get(2).size();k++){
                    if (myAgent.resourceModel.get(j).get(2).get(k).startsWith("id")) attribs.append(myAgent.resourceModel.get(j).get(3).get(k)).append(",");
                }
            }
        }

        /* Cuarto, se envía el mensaje de registro */
        attribs = new StringBuilder(attribs.substring(0, attribs.length() - 1));
        String cmd = "reg machine parent=system"+attribs;

        ACLMessage reply = null;
        try {
            reply = myAgent.sendCommand(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Finalmente, se obtiene el Id que el SystemModelAgent ha asignado al nuevo agente */
        seId = reply.getContent();

        try {
            /* Una vez registrado el agente máquina, es creado por el agente auxiliar, pasándole como argumento su id */
            className = myAgent.getClass().getName();
            String [] args2 = {"ID="+seId, "description=description" };
            args = ArrayUtils.addAll(args,args2);
            myAgent.getContainerController().createNewAgent(seId,className, args).start();
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
            for (int i=allOperations.length;i>0;i--) {

                /* Se declara un arraylist para guardar la información sobre cada operación recibida */
                ArrayList<ArrayList<String>> operationInfo = new ArrayList<>();

                /* Se crean y se rellenan dos arraylist de string para la cabecera del listado de operaciones */
                ArrayList<String> elementName = new ArrayList<>();
                ArrayList<String> hierarchyLevel = new ArrayList<>();
                elementName.add("operation");
                hierarchyLevel.add("3");

                /* Se declaran dos arraylist de string en los que se guarda el nombre y el valor de los atributos */
                ArrayList<String> attNames = new ArrayList<>();
                ArrayList<String> attValues = new ArrayList<>();

                /* Se guardan en un array de String todos los atributos de la operación */
                String[] allAttributes = allOperations[i-1].split(" ");

                /* Se recorre el array para ir grabando los atributos uno a uno */
                for (String singleAttribute : allAttributes) {
                    String attrName = singleAttribute.split("=")[0];
                    String attrValue = singleAttribute.split("=")[1];

                    /* Se separa el nombre del atributo de los valores */
                    attNames.add(attrName);
                    attValues.add(attrValue);
                }

                /* Por último, se rellena el arraylist operationInfo y se graba la operación en el plan del transporte */
                operationInfo.add(0, elementName);
                operationInfo.add(1, hierarchyLevel);
                operationInfo.add(2, attNames);
                operationInfo.add(3, attValues);
                myAgent.machinePlan.add(operationInfo);
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
        //Leer la pila de tareas (obtener todas las operaciones pendientes)
        //Estimar un valor de negociación en base a estas (hora estimada de inicio de la tarea)
        //Devolver el valor obtenido

        // Se van a recibir varios campos en el objeto negExternalData
        //TODO definir qué parámetros van a hacer falta para esta negociación
        String seID = (String)negExternalData[0];
        String seNumOfItems = (String) negExternalData[1];
        int numItems = Integer.parseInt(seNumOfItems);
        String seOperationID = (String)negExternalData[2];

        /* If redundante solo para confirmar que la petición se ajusta a un criterio que tenga sentido para el agente
         *  Este if cobrará más sentido si en el futuro se consideran varios criterios posibles a elección del solicitante */
        if (negCriterion.equals("shortestTime")){

        }

        return 0; /* Este 0 solo lo he puesto para que no de problemas al compilar, luego habrá que sustituirlo por lo que corresponda */
    }

    @Override
    public int checkNegotiation(String conversationId, String sAction, double negReceivedValue, long negScalarValue, boolean tieBreak, boolean checkReplies, boolean isPartialWinner, Object... negExternalData) {

        LOGGER.entry(conversationId, sAction, negReceivedValue, negScalarValue);

        String seID = (String)negExternalData[0];
        String seNumOfItems = (String)negExternalData[1];
        String seOperationID = (String)negExternalData[2];

        /* Se comparan el valor propio y el valor recibido */
        if (negReceivedValue<negScalarValue) return NegotiatingBehaviour.NEG_LOST; /* pierde la negociación */
        if ((negReceivedValue==negScalarValue) && !tieBreak ) return NegotiatingBehaviour.NEG_LOST; /* empata la negociación pero no es quien fija el desempate */

        LOGGER.info("negotiation(id:"+conversationId+") partial winner "+myAgent.getLocalName()+"(value:"+negScalarValue+")");
        if (!checkReplies) return NegotiatingBehaviour.NEG_PARTIAL_WON; /* es el ganador parcial, pero faltan negociaciones por finalizar */


        if (!isPartialWinner) return NegotiatingBehaviour.NEG_LOST; /* Para ser el ganadores verdadero un agente tendrá que ser ganador parcial en cada momento */

        /* El agente es el ganador final (ha ganado todas las comparaciones) */
        LOGGER.info("ejecutar "+sAction);
        Cmd action = new Cmd(sAction);

        if (action.cmd.equals("execute")) {

            //TODO implementar las acciones que tiene que hacer el agente ganador. Para ello hay que:
            //Encapsular la operación (u operaciones) recibida en un mensaje ACL e invocar el método Execute
            //Una vez añadida la operación al plan, enviar un mensaje al agente que solicitó el servicio
            //Este mensaje deberá informar de qué transporte ha ganado la negociación y el momento de entrega estimado

            // Envio un mensaje al BatchAgent para avisarle de que soy el ganador para asociarme esa operacion
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID(seID, AID.ISLOCALNAME));
            msg.setContent("I am the winner of:" + seOperationID);
            msg.setConversationId(conversationId);
            myAgent.send(msg);

            System.out.println("\tI am the winner to get operation " +seOperationID+ " from batch " + seID + ". NumItems: " + seNumOfItems);

            Object[] data = new Object[2];
            data[0]=seOperationID;
            data[1]=seID;
            execute(data);

        }

        return NegotiatingBehaviour.NEG_WON;

    }

    public void sendDataToDevice() {

        /* Se obtiene el nombre del método (se utilizará en el conversationId) */
        String methodName = new Machine_Functionality() {}.getClass().getEnclosingMethod().getName();

        /* Primero se comprueba si se han recibido actualizaciones de material */
        updateConsumableMaterials();

        if (!workInProgress){

            /* Si el asset está libre, se comprueba si hay tareas en el plan
             * La cabecera del modelo ocupa dos posiciones, por lo que para que haya tareas, el tamaño del modelo tiene que ser de 3 o más
             * Además, se comprueba el flag de disponibilidad de material
             * Si está a false, implica que ya sabemos que no hay materiales suficientes como para completar el servicio actual */
            if (myAgent.machinePlan.size() >= 3 && materialAvailable) {

                /* Primero se prepara la estructura de datos que se va a enviar al gatewayAgent */
                createOperationHashMap();

                /* A continuación, se identifican los materiales consumibles necesarios para realizar la operación */
                String serviceType = String.valueOf(msgToAsset.get("Operation_Ref_Service_Type"));
                Object [] actionsConsumables = defineAction_And_ConsumableList(serviceType);
                ArrayList<String> consumableList = (ArrayList<String>) actionsConsumables[1];

                /* Unido a esto, se consulta el número de items que hay que procesar en este servicio */
                NumOfItems = (Integer) msgToAsset.get("Operation_No_of_Items");

                /* Se comprueba que se disponga de material suficiente para poder fabricar el lote */
                for (int i = 0; i < myAgent.availableMaterial.size(); i++) {

                    /* Se comprueba si en el arraylist de materiales consumibles tenemos el consumible de esta posición del availableMaterial */
                    if (consumableList.contains(myAgent.availableMaterial.get(i).get("consumable_id"))) {

                        /* Se consulta si el número actual de consumibles (current) es inferior al número de items a fabricar */
                        if (Integer.parseInt(myAgent.availableMaterial.get(i).get("current")) < NumOfItems) {

                            /* Se indica que no hay materiales suficientes para realizar este servicio */
                            materialAvailable = false;

                            /* Se comprueba si hay alguna petición de material en marcha */
                            /* Si no hay ninguna petición en curso, se solicita el material necesario */
                            if (!materialRequest) {
                                requestConsumableMaterials(methodName);
                                break;
                            }
                        }
                    }
                }

                if (materialAvailable){
                    /* Antes de enviar la operación, se notifica el delay existente respecto a lo planificado */
                    calculateDelay(methodName);

                    /* Finalmente, se envía la operación al GatewayAgent (se guarda en el buzón por si acaso) */
                    String MessageContent = new Gson().toJson(msgToAsset);
                    ACLMessage msg_to_gw=sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "data",
                            myAgent.getLocalName()+"_"+methodName+"_"+conversationId++, MessageContent, myAgent);
                    myAgent.AddToExpectedMsgs(msg_to_gw);

                    /* Por último, pongo el workInProgress a true*/
                    workInProgress=true;
                }
            }
        } else{
            /* System.out.println("The asset is busy"); /* Dejar para debug, luego comentar o quitar*/
        }
    }

    public void rcvDataFromDevice(ACLMessage results) {

        /* Se obtiene el nombre del método (se utilizará en el conversationId) */
        String methodName = new Machine_Functionality() {}.getClass().getEnclosingMethod().getName();

        myAgent.msgFIFO.add(results.getContent());

        /* Se transforma el mensaje recibido en un HashMap */
        msgFromAsset = new Gson().fromJson(results.getContent(), HashMap.class);

       /* Una vez, se tiene el mensaje, se comprueba qué tipo de mensaje es */
        if(msgFromAsset.containsKey("Received")){

            /* Si es un mensaje de confirmación se printea el resultado */
            if(msgFromAsset.get("Received").equals(true)){
                System.out.println("<--Asset reception confirmation");
            }else{
                System.out.println("<--Problem receiving the message"); //Aquí se debería de informar de que el asset no está disponible
            }
        }else if(msgFromAsset.containsKey("Control_Flag_Item_Completed")){

            /* Si se trata de un mensaje con contenido, se responde con un mensaje que confirme la recepción */
            HashMap confirmation = new HashMap();
            confirmation.put("Received", true);
            sendACLMessage(7, gatewayAgentID,"",
                    myAgent.getLocalName()+"_"+methodName+"_"+conversationId++, new Gson().toJson(confirmation), myAgent);

            /* Se comprueba si el mensaje contiene resultados evaluando el flag de item completado */
            if (msgFromAsset.get("Control_Flag_Item_Completed").equals(true)){

                /* TEMPORALMENTE HASTA QUE CAMBIE LAS ESTRUCTURAS DE DATOS, NECESITO UN BOOLEANO AQUÍ */
                boolean serviceCompleted = (boolean) msgFromAsset.get("Control_Flag_Service_Completed");

                /* Se eliminan los decimales de los valores numéricos */
                for(Map.Entry<String,Object> item : msgFromAsset.entrySet()){
                    if (item.getValue() instanceof Double){
                        msgFromAsset.put(item.getKey(), String.valueOf(Math.round((Double) item.getValue())));
                    }
                }

                /* A continuación, se identifican los materiales consumibles utilizados para realizar la operación */
                String serviceType = String.valueOf(msgFromAsset.get("Id_Ref_Service_Type"));
                Object [] actionsConsumables = defineAction_And_ConsumableList(serviceType);
                ArrayList<String> actionList = (ArrayList<String>) actionsConsumables[0]; // Lista de acciones que componen el servicio actual
                ArrayList<String> consumableList = (ArrayList<String>) actionsConsumables[1]; // Lista de consumibles que se utilizan para el servicio actual

                /* Se comprueba que se disponga de material suficiente para poder fabricar el lote */
                for (int i = 0; i < myAgent.availableMaterial.size(); i++) {

                    /* Se comprueba si en el arraylist de materiales consumibles tenemos el consumible de esta posición del availableMaterial */
                    if (consumableList.contains(myAgent.availableMaterial.get(i).get("consumable_id"))) {

                        /* Se obtienen los consumibles actuales y se descuenta 1 (se acaba de consumir uno para hacer el servicio) */
                        int currentConsumables = Integer.parseInt(myAgent.availableMaterial.get(i).get("current"));
                        currentConsumables--; //una vez identificado el nombre del consumible deseado, se descuenta
                        myAgent.availableMaterial.get(i).put("current", Integer.toString(currentConsumables));

                        /* Se obtiene el valor de alerta */
                        int warningConsumable = Integer.parseInt(myAgent.availableMaterial.get(i).get("warning"));

                        /* Se comprueba si quedan menos consumibles que los recomendables */
                        if (currentConsumables <= warningConsumable){

                            /* Se comprueba si hay alguna petición de material en marcha */
                            /* Si no hay ninguna petición en curso, se solicita el material necesario */
                            if (!materialRequest) {
                                requestConsumableMaterials(methodName);
                                break;
                            }
                        }
                    }
                }

                /* Se añaden al mensaje las acciones asociadas al servicio realizado */
                msgFromAsset.put("Id_Action_Type", actionList);

                /* Aquí se elimina de la estructura del mensaje recibida toda la información que no interesa al BatchAgent
                *  Tal vez este código acabe dentro del recvBatchInfo, ya veremos */
                if (msgFromAsset.get("Control_Flag_Service_Completed").equals(false)) {
                    msgFromAsset.remove("Data_Service_Time_Stamp");    //remove unnecessary data from message
                }
                msgFromAsset.remove("Control_Flag_Service_Completed");    //remove unnecessary data from message
                msgFromAsset.remove("Control_Flag_Item_Completed");   //remove unnecessary data from message
                msgFromAsset.remove("Id_Ref_Service_Type");   // when all actions are identified, the Ref_Service_Type data is unnecessary


                /* Aquí se pasa a String el contenido del mensaje que se le va a enviar al BatchAgent */
                String messageContent = new Gson().toJson(msgFromAsset);
                /* System.out.println(messageContent); */

                /* A continuación, se envían los resultados al BatchAgent */
                recvBatchInfo(messageContent,methodName);   // Método para enviar los resultados al BatchAgent correspondiente

                /* Se comprueba si el mensaje corresponde a la última pieza del lote evaluando el flag de servicio completo */
                if (serviceCompleted) {

                    /* Si se ha terminado el servicio, se eliminan las operaciones del plan de máquina */
                    BatchID = String.valueOf(msgFromAsset.get("Id_Batch_Reference"));

                    /* Apunto siempre a la posición 2 (primera operación), y voy borrando hasta que no quedan items */
                    while (NumOfItems != 0){
                        if (myAgent.machinePlan.get(2).get(0).get(0).equals("operation")){
                            if (myAgent.machinePlan.get(2).get(3).get(3).equals(BatchID)){
                                myAgent.machinePlan.remove(2);
                                NumOfItems--;
                            }
                        }
                    }

                    /* Se envían los timeStamps al DataAcq_GWAgent */
                    get_timestamp(String.valueOf(msgFromAsset.get("Data_Initial_Time_Stamp")),String.valueOf(msgFromAsset.get("Id_Batch_Reference")),myAgent.getLocalName(),"Initial_Time_Stamp");
                    get_timestamp(String.valueOf(msgFromAsset.get("Data_Final_Time_Stamp")),String.valueOf(msgFromAsset.get("Id_Batch_Reference")),myAgent.getLocalName(),"Final_Time_Stamp");

                    /* Por último, se resetea el flag para indicar que el transaporte ha quedado libre*/
                    workInProgress=false;
                }
            }
        }
    }


    /* OPERACIONES DE FINALIZACIÓN DEL AGENTE MÁQUINA*/

    @Override
    public Void terminate(MWAgent myAgent) { return null;}


    /* MÉTODOS AUXILIARES */

    private void calculateDelay(String methodName) {

        /* Buscamos el atributo plannedStartTime */
        for (int k = 0; k < myAgent.machinePlan.get(2).get(2).size(); k++) {
            if (myAgent.machinePlan.get(2).get(2).get(k).equals("plannedStartTime")) {
                String startTime = myAgent.machinePlan.get(2).get(3).get(k);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); //Se usa el formato XS:DateTime
                Date date2 = getactualtime();
                Date date1 = null;

                try {
                    date1 = formatter.parse(startTime); /* ¿Nos podríamos evitar esto cambiando el formato de los modelos? */

                } catch (ParseException e) {
                    System.out.println("ERROR dando formato a una fecha");
                    e.printStackTrace();
                }
                while (date1.after(date2)) { //Se queda actualizando la fecha hasta que se alcance la fecha definida en el plan de fabricación
                    date2 = getactualtime();
                }
                long diferencia = ((date2.getTime() - date1.getTime())); //calculamos el retraso en iniciar en milisegundos
                AID QoSID = new AID("QoSManagerAgent", false);
                String content = Integer.toString((Integer) msgToAsset.get("Id_Batch_Reference"));
                String delay = String.valueOf(diferencia);
                content = content + "/" + delay;
                sendACLMessage(ACLMessage.INFORM, QoSID, "delay",
                        myAgent.getLocalName()+"_"+methodName+"_"+conversationId++, content, myAgent);
            }
        }
    }

    public void recvBatchInfo(String messageContent, String methodName) {

        ACLMessage reply;
        String  batchName = "";// Nombre del batch agent al que se le enviara el mensaje
        try {
            BatchID = String.valueOf(msgFromAsset.get("Id_Batch_Reference"));   //gets the batch reference from the received message
            reply = sendCommand(myAgent, "get * reference=" + BatchID,
                    myAgent.getLocalName()+"_"+methodName+"_"+conversationId++);
            //returns the id of the element that matches with the reference of the required batch
            if (reply != null) {   // If the id does not exist, it returns error
                myAgent.msgFIFO.add(reply.getContent());
                batchName = reply.getContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Envío de mensajes que hayan quedado pendientes porque no había un batchAgent en running disponible */
        try {
            posponed_msgs_to_batch= myAgent.msg_buffer.get(batchName);
            if(posponed_msgs_to_batch==null){ //si no se encuentra el parent en el listado de mensajes postpuestos entonces el receptor ha confirmado la recepcion de todos los mensajes hasta ahora. Todoo OK.
                ACLMessage running_replica = sendCommand(myAgent, "get * parent=" + batchName +" state=running",
                        myAgent.getLocalName()+"_"+methodName+"_"+conversationId++);
                if (running_replica != null) {
                    String batchAgentName = running_replica.getContent();
                    AID batchAgentID = new AID(batchAgentName, false);
                    if(!running_replica.getContent().equals("")){   //encontrada replica en running para este batch
                        ACLMessage msg_to_batchagent=sendACLMessage(ACLMessage.INFORM, batchAgentID, "data",
                                myAgent.getLocalName()+"_"+methodName+"_"+conversationId++, messageContent, myAgent);
                        myAgent.AddToExpectedMsgs(msg_to_batchagent);

                    }else{    //No encontrada replica en running para este batch. Puede que otro agente lo haya denunciado previamente o que el batch aun no se haya iniciado
                        posponed_msgs_to_batch = new ArrayList<>();
                        ACLMessage msg_to_buffer=new ACLMessage(ACLMessage.INFORM);
                        msg_to_buffer.setConversationId(myAgent.getLocalName()+"_"+methodName+"_"+conversationId++);
                        msg_to_buffer.setContent(messageContent);
                        msg_to_buffer.setOntology("data");
                        posponed_msgs_to_batch.add(msg_to_buffer);
                        myAgent.msg_buffer.put(batchName,posponed_msgs_to_batch); //guardamos el mensaje hasta que el D&D me informe de que ya tenemos disponible otro receptor
                    }
                }
            }else{  //habia algun mensaje pendiente de envíar a un receptor aun no definido
                System.out.println("Added message to buffer:\nContent: "+messageContent+"\nTo: "+batchName);
                ACLMessage msg_to_buffer=new ACLMessage(ACLMessage.INFORM);
                msg_to_buffer.setConversationId(myAgent.getLocalName()+"_"+methodName+"_"+conversationId++);
                msg_to_buffer.setContent(messageContent);
                msg_to_buffer.setOntology("data");
                posponed_msgs_to_batch.add(msg_to_buffer);
                myAgent.msg_buffer.put(batchName,posponed_msgs_to_batch);

                ACLMessage running_replica = sendCommand(myAgent, "get * parent=" + batchName +" state=running",
                        myAgent.getLocalName()+"_"+methodName+"_"+conversationId++);
                if(!running_replica.getContent().equals("")){ //nos aseguramos de que el receptor aun no exista. Si existe ya, vaciamos el cajón con un automensaje.
                    sendACLMessage(7, myAgent.getAID(), "release_buffer",myAgent.getLocalName()+"_"+ conversationId++,running_replica.getContent(),myAgent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestConsumableMaterials(String methodName) {

        String neededMaterial = "";
        Integer neededConsumable;

        /* Se calcula el material necesario para llenar cada uno de los buffer de piezas al maximo */
        for (int j = 0; j < myAgent.availableMaterial.size(); j++){
            int currentConsumables = Integer.parseInt(myAgent.availableMaterial.get(j).get("current"));
            neededConsumable = Integer.parseInt(myAgent.availableMaterial.get(j).get("max")) - currentConsumables;
            neededMaterial = neededMaterial.concat(myAgent.availableMaterial.get(j).get("consumable_id") + ":" + neededConsumable + ";");
        }

        //Peticion negociacion entre los agentes transporte disponibles
        try {
            String targets = "";
            ACLMessage reply2 = sendCommand(myAgent, "get * category=transport",
                    myAgent.getLocalName()+"_"+methodName+"_"+conversationId++);
            if (reply2 != null) {   // If the id does not exist, it returns error
                targets = reply2.getContent();
            }
            String negotiationQuery = "localneg " + targets + " criterion=position action=" +
                    "supplyConsumables externaldata=" + neededMaterial + "," + myAgent.getLocalName();
            ACLMessage result = sendCommand(myAgent, negotiationQuery,
                    myAgent.getLocalName()+"_"+methodName+"_"+conversationId++);

        } catch (Exception e) {
            e.printStackTrace();
        }

        materialRequest = true;
    }

    private void updateConsumableMaterials (){

        /* Se reciben los mensajes ACL que corresponden al material que se ha repuesto (se recibe al terminar el servicio) */
        /* Se actualizan los contadores de consumibles */
        MessageTemplate template = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology("data")),MessageTemplate.MatchConversationId("ProvidedConsumables"));

        ACLMessage msg = myAgent.receive(template);
        if (msg != null) {
            myAgent.msgFIFO.add(msg.getContent());
            ArrayList<ArrayList<String>> newConsumables = new ArrayList<>();
            newConsumables.add(new ArrayList<>()); newConsumables.add(new ArrayList<>());
            String content = msg.getContent();
            String [] contentSplited = content.split(";");
            for (String s : contentSplited) {

                /* Se deserializa el mensaje y se guardan los datos en un arraylist */
                newConsumables.get(0).add(s.split(":")[0]);
                newConsumables.get(1).add(s.split(":")[1]);
            }

            /* Bucle para sumar los nuevos consumibles en el contador de material */
            for (int i = 0; i < newConsumables.get(0).size(); i++){
                for (int j = 0; j < myAgent.availableMaterial.size(); j++){
                    if (newConsumables.get(0).get(i).equals(myAgent.availableMaterial.get(j).get("consumable_id"))) {
                        Integer currentConsumable = Integer.parseInt(myAgent.availableMaterial.get(j).get("current"));
                        Integer addedconsumable = Integer.parseInt(newConsumables.get(1).get(i));
                        myAgent.availableMaterial.get(j).put("current", Integer.toString(currentConsumable + addedconsumable));
                    }
                }
            }

            /* Una vez repuesto el material se resetea el flag de petición de materiales */
            materialRequest = false;

            /* También se resetea el flag que indica que tenemos materiales */
            materialAvailable = true;
        }
    }

    private void createOperationHashMap() {

        /* Se declara una variable para indicar el número de items a realizar en el servicio (mínimo 1) */
        Integer NumOfItems = 1;

        /* Queremos guardar la referencia de la máquina (el segundo elemento del plan) */
        msgToAsset.put("Id_Machine_Reference", Integer.parseInt(myAgent.machinePlan.get(1).get(3).get(0)));

        /* Queremos guardar los valores de algunos atributos de la tercera posición (primera operación) */
        msgToAsset.put("Control_Flag_New_Service", true);
        msgToAsset.put("Id_Batch_Reference", Integer.parseInt(myAgent.machinePlan.get(2).get(3).get(3)));
        msgToAsset.put("Id_Order_Reference", Integer.parseInt(myAgent.machinePlan.get(2).get(3).get(5))); //antes get 6 tras cambio en xml -> 5
        msgToAsset.put("Id_Ref_Subproduct_Type", Integer.parseInt(myAgent.machinePlan.get(2).get(3).get(6))); //antes get 7 tras cambio en xml -> 6
        msgToAsset.put("Operation_Ref_Service_Type", Integer.parseInt(myAgent.machinePlan.get(2).get(3).get(0)));
        msgToAsset.put("Operation_No_of_Items", NumOfItems);

        /* Compruebo si tengo parámetros (de momento asumo que son los mismos parámetros para todos los items porque son el mismo servicio)
         * Eso implica que tiene que haber 8 atributos (los 7 fijos más uno adicional que son los parámetros) */
        if (myAgent.machinePlan.get(2).get(3).size()>=8){

            /* Voy a darle el formato de hashMap a los parámetros (se puede discutir si esto tiene que ir en otro sitio) */
            String parameters = myAgent.machinePlan.get(2).get(3).get(7);
            String [] parametersArray = parameters.split(",");
            HashMap<String,String> parametersHashMap = new HashMap<>();
            for (int i=0; i<parametersArray.length; i++){
                String [] parameterSplitted = parametersArray[i].split(":");
                parametersHashMap.put(parameterSplitted[0],parameterSplitted[1]);
            }

            /* Una vez que se ha rellenado el hashMap, se añade al mensaje */
            msgToAsset.put("Operation_Parameters",new Gson().toJson(parametersHashMap));
        }

        /* Sé que tengo al menos un item, pero no sé si tengo más. Lo compruebo */
        if (myAgent.machinePlan.size()>=4) {

            /* Ahora recorro el resto del plan para ir incrementando el número de items que pertenecen al batch */
            for (int i = 3; i< myAgent.machinePlan.size(); i++){

                /* Si el batch_Id coincide, hay que incrementar el número de items */
                if (myAgent.machinePlan.get(i).get(3).get(3).equals(msgToAsset.get("Id_Batch_Reference").toString())){
                    NumOfItems++;
                    msgToAsset.put("Operation_No_of_Items", NumOfItems);

                } else { //Si el batch_id no coincide, se sale del bucle
                    break;
                }
            }
        }
    }

    private Object[] defineAction_And_ConsumableList(String serviceType) {

        ArrayList<String> actionList = new ArrayList<>();
        ArrayList<String> consumableList = new ArrayList<>();
        for (int j = 0; j < myAgent.resourceModel.size(); j++) {

            /* Se itera elemento a elemento hasta encontrar algún servicio (simple_operation) */
            if (myAgent.resourceModel.get(j).get(0).get(0).equals("simple_operation")) {

                /* Se comprueba si este servicio es del que se han recibido resultados evaluando la variable serviceType */
                if (myAgent.resourceModel.get(j).get(3).get(1).equals(serviceType)) {
                    for (int k = j + 1; k < myAgent.resourceModel.size(); k++)  {

                        /* Se buscan las acciones y se guardan en actionList */
                        if (myAgent.resourceModel.get(k).get(0).get(0).equals("action")){
                            actionList.add(myAgent.resourceModel.get(k).get(3).get(2));

                            /* También se guardan los id de los consumibles asociados a esa acción en consumableList */
                            /* Esto solo se puede hacer si la acción no es el último elemento del modelo... */
                            /* Y en caso de que el elemento siguiente sea un consumible (no tiene por qué serlo) */
                            if (k < (myAgent.resourceModel.size()-1)) {
                                if (myAgent.resourceModel.get(k+1).get(0).get(0).equals("consumable")){
                                    consumableList.add(myAgent.resourceModel.get(k+1).get(3).get(1));
                                }
                            }
                        } else if (myAgent.resourceModel.get(k).get(0).get(0).equals("simple_operation")) {
                            /* Si se llega a la siguiente operación se acaba el bucle */
                            break;
                        }
                    }
                }
            }
        }
        Object [] result = new Object[2];
        result [0] = actionList;
        result [1] = consumableList;

        return result;
    }

    protected Date getactualtime(){
        String actualTime;
        int ano, mes, dia, hora, minutos, segundos;
        Calendar calendario = Calendar.getInstance();
        ano = calendario.get(Calendar.YEAR);
        mes = calendario.get(Calendar.MONTH) + 1;
        dia = calendario.get(Calendar.DAY_OF_MONTH);
        hora = calendario.get(Calendar.HOUR_OF_DAY);
        minutos = calendario.get(Calendar.MINUTE);
        segundos = calendario.get(Calendar.SECOND);
        actualTime = ano + "-" + mes + "-" + dia + "T" + hora + ":" + minutos + ":" + segundos;
        Date actualdate = null;
        try {
            actualdate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(actualTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return actualdate;
    }

    public void get_timestamp(String timestamp, String batchNumber, String machine, String type) {

        String batchId = "";
        ACLMessage batchIdmsg = null;

        String machineName = "";
        ACLMessage machineNamemsg = null;

        /* Primero, conseguimos la referencia del batch a partir del batchNumber*/
        try {
            machineNamemsg = sendCommand(myAgent, "get "+machine+" attrib=description",myAgent.getLocalName());
            batchIdmsg = sendCommand(myAgent, "get * reference=" + batchNumber, myAgent.getLocalName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (machineNamemsg != null){
            machineName = machineNamemsg.getContent();
        }

        if (batchIdmsg != null) {
            batchId = batchIdmsg.getContent();
        }



        /* Después, construimos y enviamos el mensaje */
        String contenido = machineName + "," + batchId + "," + type + "," + timestamp;
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("ControlContainer-GWDataAcq", AID.ISLOCALNAME));
        msg.setOntology("timestamp_machine");
        msg.setConversationId(myAgent.getLocalName() + "_" + type + "_" + TMSTMP_cnt++);
        msg.setContent(contenido);
        myAgent.send(msg);

    }

}



