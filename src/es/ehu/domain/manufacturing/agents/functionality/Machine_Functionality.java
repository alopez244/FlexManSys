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

    /* DECLARACI�N DE VARIABLES */

    /* Agente que utiliza esta funcionalidad (se recibe como par�metro en diferentes m�todos) */
    private MachineAgent myAgent;

    /* Id del agente en el sistema*/
    private String seId;

    /* Nombre de la clase (para poder crear el agente m�quina definitivo) */
    private String className;

    /* Flag que se activa cuando el transporte esta trabajando */
    private Boolean workInProgress = false;

    /* Nombre del gatewayAgent con el que interact�a el agente */
    private AID gatewayAgentID =null;

    /* Identificador de la conversaci�n iniciada por el agente transporte */
    private Integer conversationId =0;

    /* Identificador del lote que se est� procesando */
    private String BatchID = "";

    /* N�mero de items que hay que fabricar en el servicio actual */
    private Integer NumOfItems = 0;

    /* Flag que indica si hay una petici�n de reponer materiales en marcha */
    private Boolean materialRequest = false;

    /* Estructura de datos que se env�a al asset */
    private HashMap msgToAsset = new HashMap();

    /* Estructura de datos que se recibe del asset */
    private HashMap msgFromAsset = new HashMap();

    /* Lista de mensajes ACL que se est�n guardando para enviar al batchAgent cuando corresponda */
    public ArrayList<ACLMessage> posponed_msgs_to_batch= new ArrayList<>();


    /* OPERACIONES DE INICIALIZACI�N Y PUESTA EN MARCHA */

    @Override
    public Void init(MWAgent mwAgent) {

        /* Se obtiene el nombre del m�todo (se utilizar� en el conversationId) */
        String methodName = new Machine_Functionality() {}.getClass().getEnclosingMethod().getName();

        /* Se hace el cambio de tipo */
        this.myAgent = (MachineAgent) mwAgent;
        LOGGER.entry();

        /* Se guarda el nombre del gatewayAgent con el que interact�a el agente */
        String machineName = myAgent.resourceName;
        Integer machineNumber = Integer.parseInt(machineName.split("_")[1]);
        myAgent.gatewayAgentName = "ControlGatewayCont" + machineNumber;
        gatewayAgentID = new AID(myAgent.gatewayAgentName, false);

        /* Se leen los argumentos con los que se ha llamado al agente y se comprueba si tiene un id */
        String [] args = (String[]) myAgent.getArguments();

        for (String arg : args) {
            if (arg.toLowerCase().startsWith("id=")) return null;
        }

		/* En caso negativo, se trata del agente auxiliar y hay que realizar m�s acciones */
		/* En primer lugar, hay que comprobar la conectividad con el asset en dos pasos: */
		/* Paso 1: contacto con el gatewayAgent y espero respuesta (si no hay, no se puede continuar con el registro) */
		sendACLMessage(ACLMessage.REQUEST,gatewayAgentID,"ping",
                myAgent.getLocalName()+"_"+methodName+"_"+conversationId++,"",myAgent);
		ACLMessage answer_gw = myAgent.blockingReceive(MessageTemplate.MatchOntology("ping"), 300);
		if(answer_gw==null){
			System.out.println("GW is not online. Start GW and repeat.");
			System.exit(0);
		}

		/* Paso 2: contacto con el asset a trav�s del gatewayAgent y espero respuesta (si no hay, no se puede continuar con el registro) */
		sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "check_asset",
                myAgent.getLocalName()+"_"+methodName+"_"+conversationId++,"ask_state",myAgent);
		ACLMessage answer = myAgent.blockingReceive(MessageTemplate.MatchOntology("asset_state"), 300);
		if(answer!=null){
			if(!answer.getContent().equals("Working")&&!answer.getContent().equals("Not working")){
				System.out.println("PLC is not prepared to work.");
				System.exit(0); //si el asset o el gwAgent no est�n disponibles no tiene sentido que iniciemos el agente
			}else{
				System.out.println("PLC is "+answer.getContent());
			}
		}else{
			System.out.println("PLC is not prepared to work.");
			System.exit(0); //si el asset o el gwAgent no est�n disponible no tiene sentido que iniciemos el agente m�quina
		}

        /* Si la comunicaci�n con el asset es correcta, se procede a registrar el agente transporte en el SystemModelAgent */
        /* Primero, se registra el listado de materiales disponibles en la estaci�n */
        int index = 0;
        for (int i = 0; i < myAgent.resourceModel.size() - 1; i++) {
            if (myAgent.resourceModel.get(i).get(0).get(0).equals("buffer")){
                myAgent.availableMaterial.add(new HashMap<>());   //Se a�aden niveles nuevos para poder ser rellenados con datos
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

        /* Tercero, se a�aden las operaciones del asset al string de atributos */
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

        /* Cuarto, se env�a el mensaje de registro */
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
            /* Una vez registrado el agente m�quina, es creado por el agente auxiliar, pas�ndole como argumento su id */
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


    /* OPERACIONES DE ACTUALIZACI�N DE LA LISTA DE TAREAS */

    @Override
    public Object execute(Object[] input) {

        if (input[0] != null) {
            ACLMessage msg = (ACLMessage) input[0];
            myAgent.Acknowledge(msg,myAgent);

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
                hierarchyLevel.add("3");

                /* Se declaran dos arraylist de string en los que se guarda el nombre y el valor de los atributos */
                ArrayList<String> attNames = new ArrayList<>();
                ArrayList<String> attValues = new ArrayList<>();

                /* Se guardan en un array de String todos los atributos de la operaci�n */
                String[] allAttributes = singleOperation.split(" ");

                /* Se recorre el array para ir grabando los atributos uno a uno */
                for (String singleAttribute : allAttributes) {
                    String attrName = singleAttribute.split("=")[0];
                    String attrValue = singleAttribute.split("=")[1];

                    /* Se separa el nombre del atributo de los valores */
                    attNames.add(attrName);
                    attValues.add(attrValue);
                }

                /* Por �ltimo, se rellena el arraylist operationInfo y se graba la operaci�n en el plan del transporte */
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


    /* OPERACIONES DE NEGOCIADO DE ASIGNACI�N DE TAREAS */

    @Override
    public long calculateNegotiationValue(String negAction, String negCriterion, Object... negExternalData) {

        //TODO implementar el m�todo. Para ello hay que:
        //Leer la pila de tareas (obtener todas las operaciones pendientes)
        //Estimar un valor de negociaci�n en base a estas (hora estimada de inicio de la tarea)
        //Devolver el valor obtenido

        // Se van a recibir varios campos en el objeto negExternalData
        //TODO definir qu� par�metros van a hacer falta para esta negociaci�n
        String seID = (String)negExternalData[0];
        String seNumOfItems = (String) negExternalData[1];
        int numItems = Integer.parseInt(seNumOfItems);
        String seOperationID = (String)negExternalData[2];

        /* If redundante solo para confirmar que la petici�n se ajusta a un criterio que tenga sentido para el agente
         *  Este if cobrar� m�s sentido si en el futuro se consideran varios criterios posibles a elecci�n del solicitante */
        if (negCriterion.equals("shortestTime")){

        }

        return 0; /* Este 0 solo lo he puesto para que no de problemas al compilar, luego habr� que sustituirlo por lo que corresponda */
    }

    @Override
    public int checkNegotiation(String conversationId, String sAction, double negReceivedValue, long negScalarValue, boolean tieBreak, boolean checkReplies, boolean isPartialWinner, Object... negExternalData) {

        LOGGER.entry(conversationId, sAction, negReceivedValue, negScalarValue);

        String seID = (String)negExternalData[0];
        String seNumOfItems = (String)negExternalData[1];
        String seOperationID = (String)negExternalData[2];

        /* Se comparan el valor propio y el valor recibido */
        if (negReceivedValue<negScalarValue) return NegotiatingBehaviour.NEG_LOST; /* pierde la negociaci�n */
        if ((negReceivedValue==negScalarValue) && !tieBreak ) return NegotiatingBehaviour.NEG_LOST; /* empata la negociaci�n pero no es quien fija el desempate */

        LOGGER.info("negotiation(id:"+conversationId+") partial winner "+myAgent.getLocalName()+"(value:"+negScalarValue+")");
        if (!checkReplies) return NegotiatingBehaviour.NEG_PARTIAL_WON; /* es el ganador parcial, pero faltan negociaciones por finalizar */


        if (!isPartialWinner) return NegotiatingBehaviour.NEG_LOST; /* Para ser el ganadores verdadero un agente tendr� que ser ganador parcial en cada momento */

        /* El agente es el ganador final (ha ganado todas las comparaciones) */
        LOGGER.info("ejecutar "+sAction);
        Cmd action = new Cmd(sAction);

        if (action.cmd.equals("execute")) {

            //TODO implementar las acciones que tiene que hacer el agente ganador. Para ello hay que:
            //Encapsular la operaci�n (u operaciones) recibida en un mensaje ACL e invocar el m�todo Execute
            //Una vez a�adida la operaci�n al plan, enviar un mensaje al agente que solicit� el servicio
            //Este mensaje deber� informar de qu� transporte ha ganado la negociaci�n y el momento de entrega estimado

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

        /* Se obtiene el nombre del m�todo (se utilizar� en el conversationId) */
        String methodName = new Machine_Functionality() {}.getClass().getEnclosingMethod().getName();

        /* Primero se comprueba si se han recibido actualizaciones de material */
        updateConsumableMaterials();

        if (!workInProgress){

            /* Si el asset est� libre, se comprueba si hay tareas en el plan
             * La cabecera del modelo ocupa dos posiciones, por lo que para que haya tareas, el tama�o del modelo tiene que ser de 3 o m�s */
            if (myAgent.machinePlan.size() >= 3) {

                /* Primero se prepara la estructura de datos que se va a enviar al gatewayAgent */
                msgToAsset = createOperationHashMap(myAgent.machinePlan);

                /* A continuaci�n, se identifican los materiales consumibles necesarios para realizar la operaci�n */
                String serviceType = String.valueOf(msgToAsset.get("Operation_Ref_Service_Type"));
                Object [] actionsConsumables = defineAction_And_ConsumableList(serviceType);
                ArrayList<String> consumableList = (ArrayList<String>) actionsConsumables[1];

                /* Unido a esto, se consulta el n�mero de items que hay que procesar en este servicio */
                NumOfItems = (Integer) msgToAsset.get("Operation_No_of_Items");

                /* Se comprueba que se disponga de material suficiente para poder fabricar el lote */
                for (int i = 0; i < myAgent.availableMaterial.size(); i++) {

                    /* Se comprueba si en el arraylist de materiales consumibles tenemos el consumible de esta posici�n del availableMaterial */
                    if (consumableList.contains(myAgent.availableMaterial.get(i).get("consumable_id"))) {

                        /* Se consulta si el n�mero actual de consumibles (current) es inferior al n�mero de items a fabricar */
                        /* Tambi�n se comprueba si no hay una petici�n de material en marcha */
                        if (Integer.parseInt(myAgent.availableMaterial.get(i).get("current")) < NumOfItems && !materialRequest) {

                            /* Si no hay consumibles suficientes ni hay una petici�n en curso, se solicitan */
                            requestConsumableMaterials(methodName);
                        }
                    }
                }

                /* Antes de enviar la operaci�n, se notifica el delay existente respecto a lo planificado */
                calculateDelay(methodName);

                /* Finalmente, se env�a la operaci�n al GatewayAgent (se guarda en el buz�n por si acaso) */
                String MessageContent = new Gson().toJson(msgToAsset);
                ACLMessage msg_to_gw=sendACLMessage(ACLMessage.REQUEST, gatewayAgentID, "data",
                        myAgent.getLocalName()+"_"+methodName+"_"+conversationId++, MessageContent, myAgent);
                myAgent.AddToExpectedMsgs(msg_to_gw);

                /* Por �ltimo, pongo el workInProgress a true*/
                workInProgress=true;
            }
        } else{
            /* System.out.println("The asset is busy"); /* Dejar para debug, luego comentar o quitar*/
        }
    }

    public void rcvDataFromDevice(ACLMessage results) {

        /* Se obtiene el nombre del m�todo (se utilizar� en el conversationId) */
        String methodName = new Machine_Functionality() {}.getClass().getEnclosingMethod().getName();

        myAgent.msgFIFO.add(results.getContent());

        /* Se transforma el mensaje recibido en un HashMap */
        msgFromAsset = new Gson().fromJson(results.getContent(), HashMap.class);

       /* Una vez, se tiene el mensaje, se comprueba qu� tipo de mensaje es */
        if(msgFromAsset.containsKey("Received")){

            /* Si es un mensaje de confirmaci�n se printea el resultado */
            if(msgFromAsset.get("Received").equals(true)){
                System.out.println("<--Asset reception confirmation");
            }else{
                System.out.println("<--Problem receiving the message"); //Aqu� se deber�a de informar de que el asset no est� disponible
            }
        }else if(msgFromAsset.containsKey("Control_Flag_Item_Completed")){

            /* Si se trata de un mensaje con contenido, se responde con un mensaje que confirme la recepci�n */
            HashMap confirmation = new HashMap();
            confirmation.put("Received", true);
            sendACLMessage(7, gatewayAgentID,"",
                    myAgent.getLocalName()+"_"+methodName+"_"+conversationId++, new Gson().toJson(confirmation), myAgent);

            /* Se comprueba si el mensaje contiene resultados evaluando el flag de item completado */
            if (msgFromAsset.get("Control_Flag_Item_Completed").equals(true)){

                /* TEMPORALMENTE HASTA QUE CAMBIE LAS ESTRUCTURAS DE DATOS, NECESITO UN BOOLEANO AQU� */
                boolean serviceCompleted = (boolean) msgFromAsset.get("Control_Flag_Service_Completed");

                /* Se eliminan los decimales de los valores num�ricos */
                removeDecimals();

                /* A continuaci�n, se identifican los materiales consumibles utilizados para realizar la operaci�n */
                String serviceType = String.valueOf(msgFromAsset.get("Id_Ref_Service_Type"));
                Object [] actionsConsumables = defineAction_And_ConsumableList(serviceType);
                ArrayList<String> actionList = (ArrayList<String>) actionsConsumables[0]; // Lista de acciones que componen el servicio actual
                ArrayList<String> consumableList = (ArrayList<String>) actionsConsumables[1]; // Lista de consumibles que se utilizan para el servicio actual

                /* Se comprueba que se disponga de material suficiente para poder fabricar el lote */
                for (int i = 0; i < myAgent.availableMaterial.size(); i++) {

                    /* Se comprueba si en el arraylist de materiales consumibles tenemos el consumible de esta posici�n del availableMaterial */
                    if (consumableList.contains(myAgent.availableMaterial.get(i).get("consumable_id"))) {

                        /* Se obtienen los consumibles actuales y se descuenta 1 (se acaba de consumir uno para hacer el servicio) */
                        int currentConsumables = Integer.parseInt(myAgent.availableMaterial.get(i).get("current"));
                        currentConsumables--; //una vez identificado el nombre del consumible deseado, se descuenta
                        myAgent.availableMaterial.get(i).put("current", Integer.toString(currentConsumables));

                        /* Se obtiene el valor de alerta */
                        int warningConsumable = Integer.parseInt(myAgent.availableMaterial.get(i).get("warning"));

                        /* Se comprueba si quedan menos consumibles que los recomendables */
                        if (currentConsumables <= warningConsumable && !materialRequest){

                            /* Si no hay consumibles suficientes ni hay una petici�n en curso, se solicita */
                            requestConsumableMaterials(methodName);
                        }
                    }
                }

                /* Se a�aden al mensaje las acciones asociadas al servicio realizado */
                msgFromAsset.put("Id_Action_Type", actionList);

                /* Aqu� se elimina de la estructura del mensaje recibida toda la informaci�n que no interesa al BatchAgent
                *  Tal vez este c�digo acabe dentro del recvBatchInfo, ya veremos */
                if (msgFromAsset.get("Control_Flag_Service_Completed").equals(false)) {
                    msgFromAsset.remove("Data_Service_Time_Stamp");    //remove unnecessary data from message
                }
                msgFromAsset.remove("Control_Flag_Service_Completed");    //remove unnecessary data from message
                msgFromAsset.remove("Control_Flag_Item_Completed");   //remove unnecessary data from message
                msgFromAsset.remove("Id_Ref_Service_Type");   // when all actions are identified, the Ref_Service_Type data is unnecessary


                /* Aqu� se pasa a String el contenido del mensaje que se le va a enviar al BatchAgent */
                String messageContent = new Gson().toJson(msgFromAsset);
                /* System.out.println(messageContent); */

                /* A continuaci�n, se env�an los resultados al BatchAgent */
                recvBatchInfo(messageContent,methodName);   // M�todo para enviar los resultados al BatchAgent correspondiente

                /* Se comprueba si el mensaje corresponde a la �ltima pieza del lote evaluando el flag de servicio completo */
                if (serviceCompleted) {

                    /* Si se ha terminado el servicio, se eliminan las operaciones del plan de m�quina */
                    BatchID = String.valueOf(msgFromAsset.get("Id_Batch_Reference"));

                    /* Apunto siempre a la posici�n 2 (primera operaci�n), y voy borrando hasta que no quedan items */
                    while (NumOfItems != 0){
                        if (myAgent.machinePlan.get(2).get(0).get(0).equals("operation")){
                            if (myAgent.machinePlan.get(2).get(3).get(3).equals(BatchID)){
                                myAgent.machinePlan.remove(2);
                                NumOfItems--;
                            }
                        }
                    }

                    /* Por �ltimo, se resetea el flag para indicar que el transaporte ha quedado libre*/
                    workInProgress=false;
                }
            }
        }
    }


    /* OPERACIONES DE FINALIZACI�N DEL AGENTE M�QUINA*/

    @Override
    public Void terminate(MWAgent myAgent) { return null;}


    /* M�TODOS AUXILIARES */

    private void calculateDelay(String methodName) {

        /* Buscamos el atributo plannedStartTime */
        for (int k = 0; k < myAgent.machinePlan.get(2).get(2).size(); k++) {
            if (myAgent.machinePlan.get(2).get(2).get(k).equals("plannedStartTime")) {
                String startTime = myAgent.machinePlan.get(2).get(3).get(k);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); //Se usa el formato XS:DateTime
                Date date2 = getactualtime();
                Date date1 = null;

                try {
                    date1 = formatter.parse(startTime); /* �Nos podr�amos evitar esto cambiando el formato de los modelos? */

                } catch (ParseException e) {
                    System.out.println("ERROR dando formato a una fecha");
                    e.printStackTrace();
                }
                while (date1.after(date2)) { //Se queda actualizando la fecha hasta que se alcance la fecha definida en el plan de fabricaci�n
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

        /* Env�o de mensajes que hayan quedado pendientes porque no hab�a un batchAgent en running disponible */
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
            }else{  //habia algun mensaje pendiente de env�ar a un receptor aun no definido
                System.out.println("Added message to buffer:\nContent: "+messageContent+"\nTo: "+batchName);
                ACLMessage msg_to_buffer=new ACLMessage(ACLMessage.INFORM);
                msg_to_buffer.setConversationId(myAgent.getLocalName()+"_"+methodName+"_"+conversationId++);
                msg_to_buffer.setContent(messageContent);
                msg_to_buffer.setOntology("data");
                posponed_msgs_to_batch.add(msg_to_buffer);
                myAgent.msg_buffer.put(batchName,posponed_msgs_to_batch);

                ACLMessage running_replica = sendCommand(myAgent, "get * parent=" + batchName +" state=running",
                        myAgent.getLocalName()+"_"+methodName+"_"+conversationId++);
                if(!running_replica.getContent().equals("")){ //nos aseguramos de que el receptor aun no exista. Si existe ya, vaciamos el caj�n con un automensaje.
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
        System.out.println(myAgent.availableMaterial);

        /* �Bloqueo aqu� el agente hasta recibir los consumibles o d�nde espero?
         *  De momento, se va a quedar donde est� (al principio del m�todo sendDataToDevice) */
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

            /* Una vez repuesto el material se resetea el flag */
            materialRequest = false;
        }
    }

    private void removeDecimals() {

        /* M�todo para quitarle los decimales a los datos num�ricos */
        // Se crea el array list con las keys que se necesitaran para eliminar el .0 de los datos que se pasen de a tipo string
        ArrayList<String> replace = new ArrayList<>(Arrays.asList("Id_Machine_Reference", "Id_Order_Reference", "Id_Batch_Reference", "Id_Ref_Subproduct_Type", "Id_Item_Number","Id_Ref_Service_Type"));

        for (String s : replace) {  //for loop to remove the .0 of the data that contains the keys defined in replace variable
            String newValue = String.valueOf(msgFromAsset.get(s));
            newValue = newValue.split("\\.")[0];
            msgFromAsset.put(s, newValue);
        }
    }

    public Object[] defineAction_And_ConsumableList(String serviceType) {

        ArrayList<String> actionList = new ArrayList<>();
        ArrayList<String> consumableList = new ArrayList<>();
        for (int j = 0; j < myAgent.resourceModel.size(); j++) {

            /* Se itera elemento a elemento hasta encontrar alg�n servicio (simple_operation) */
            if (myAgent.resourceModel.get(j).get(0).get(0).equals("simple_operation")) {

                /* Se comprueba si este servicio es del que se han recibido resultados evaluando la variable serviceType */
                if (myAgent.resourceModel.get(j).get(3).get(1).equals(serviceType)) {
                    for (int k = j + 1; k < myAgent.resourceModel.size(); k++)  {

                        /* Se buscan las acciones y se guardan en actionList
                         *  Tambi�n se guardan los id de los consumibles asociados a esa acci�n en consumableList */
                        if (myAgent.resourceModel.get(k).get(0).get(0).equals("action")){
                            actionList.add(myAgent.resourceModel.get(k).get(3).get(2));
                            consumableList.add(myAgent.resourceModel.get(k+1).get(3).get(1));
                        } else if (myAgent.resourceModel.get(k).get(0).get(0).equals("simple_operation")) {
                            /* Si se llega a la siguiente operaci�n se acaba el bucle */
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
}



