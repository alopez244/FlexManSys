package es.ehu.domain.manufacturing.agents.functionality;

import com.google.gson.Gson;
import es.ehu.domain.manufacturing.agents.MachineAgent;
import es.ehu.domain.manufacturing.agents.TransportAgent;
import es.ehu.platform.MWAgent;
import es.ehu.platform.behaviour.NegotiatingBehaviour;
import es.ehu.platform.template.interfaces.AssetManagement;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.NegFunctionality;
import es.ehu.platform.utilities.Cmd;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Transport_Functionality extends DomApp_Functionality implements BasicFunctionality, NegFunctionality, AssetManagement {



    private HashMap PLCmsgIn = new HashMap(); // Estructura de datos que se envia al PLC
    private HashMap PLCmsgOut = new HashMap(); // Estructura de datos que se recibe del PLC

    static final Logger LOGGER = LogManager.getLogger(Transport_Functionality.class.getName());

    /** Identifier of the agent. */
    private TransportAgent myAgent;

    /** Class name to switch on the agent */
    private String className;

    private MessageTemplate template;

    @Override
    public Void init(MWAgent mwAgent) {


        this.template = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        //If the previous condition is accomplished, the agent is registered
        this.myAgent = (TransportAgent) mwAgent;

        //First, the Machine Model is read

        String [] args = (String[]) myAgent.getArguments();

        for (int i=0; i<args.length; i++){
            if (args[i].toLowerCase().startsWith("id=")) return null;
        }

        //The TransportAgent is registered in the System Model

        String cmd = "reg transport parent=system";

        ACLMessage reply = null;
        try {
            reply = myAgent.sendCommand(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String seId = reply.getContent();

        //Finally, the TransportAgent is started.

        try {
            // Agent generation
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

    @Override
    public Object execute(Object[] input) {

        ACLMessage msg = (ACLMessage) input[0];
        String content = msg.getContent();
        AID sender = msg.getSender();

        String providedConsumables = processData(content);

        sendACLMessage(7, sender, "data", "ProvidedConsumables", providedConsumables, myAgent);

        return null;
    }

    @Override
    public Void terminate(MWAgent myAgent) {return null;}

    @Override  //cambiar//
    public long calculateNegotiationValue(String negAction, String negCriterion, Object... negExternalData) {


        String externalData = (String) negExternalData[0];
        String MachineName = (String) negExternalData[1];


        /*
        if (negCriterion.equals("position")) {
            Random rand = new Random();
            float xPos = (float) (rand.nextFloat() * (10.0 - 0.0) + 0.0);// posicion en el eje x
            float yPos = (float) (rand.nextFloat() * (10.0 - 0.0) + 0.0);//posicion en el eje y
            long distance = (long) (Math.hypot(xPos,yPos) * 1000.0); // distancia en mm en linea recta
            return distance;
        }else {
            return Runtime.getRuntime().freeMemory();
        }
        */
        return Runtime.getRuntime().freeMemory();
    }

    @Override  //cambiar//
    public int checkNegotiation(String conversationId, String sAction, double negReceivedValue, long negScalarValue, boolean tieBreak, boolean checkReplies, Object... negExternalData) {

        LOGGER.entry(conversationId, sAction, negReceivedValue, negScalarValue);

        String externalData = (String) negExternalData[0];
        String machineAgentName = (String) negExternalData[1];

        if (negReceivedValue<negScalarValue) return NegotiatingBehaviour.NEG_LOST; //pierde negociación
        if ((negReceivedValue==negScalarValue) && !tieBreak ) return NegotiatingBehaviour.NEG_LOST; //empata negocicación pero no es quien fija desempate

        LOGGER.info("es el ganador ("+negScalarValue+")");
        if (!checkReplies) return NegotiatingBehaviour.NEG_PARTIAL_WON; // es ganador parcial, faltan negociaciones por finalizar

        LOGGER.info("ejecutar "+sAction);

        Cmd action = new Cmd(sAction);

        if (action.cmd.equals("supplyConsumables")) {
            LOGGER.info("id=" + action.who);

            String providedConsumables = processData(externalData);
            AID machineID = new AID(machineAgentName, false);
            sendACLMessage(7, machineID, "data", "ProvidedConsumables", providedConsumables, myAgent);
        }

        return NegotiatingBehaviour.NEG_WON;

    }

    @Override
    public void sendDataToDevice() {

        //enviar msg a GatewayAgent
        //origen A5 coordenada B8, topic A5-> B8
        ACLMessage msg = myAgent.receive(template);
        if (msg != null) {

        }
    }

    @Override
    public void rcvDataFromDevice(ACLMessage msg) {

        //msg.getContent();
        //recibir msg de confirmacion, recibir nivel de bateria.
    }


    // Metodo temporal que deserializa la peticion recibida y lo vuelve a serializar cuando se simule que el transporte ha entregado los consumibles
    public String processData(String content) {
        ArrayList<ArrayList<String>> consumables = new ArrayList<>();
        consumables.add(new ArrayList<>()); consumables.add(new ArrayList<>());

        String [] neededMaterial = content.split(";");
        for (int i = 0; i < neededMaterial.length ; i++) {
            consumables.get(0).add(neededMaterial[i].split(":")[0]);
            consumables.get(1).add(neededMaterial[i].split(":")[1]);
        }

        //TODO Dar funcionalidad a los datos guardados en consumables (material que se ha pedido al transporte)

        //Peticion de entrada provisional para simular que el transporte entrega el material pedido
        System.out.println("Pulse intro cuando desee simular que el transporte ha entregado los nuevos consumibles");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String providedConsumables = "";
        for (int i = 0; i < consumables.get(0).size(); i++) {
            providedConsumables = providedConsumables.concat(consumables.get(0).get(i) + ":" + consumables.get(1).get(i) + ";");
        }

        return providedConsumables;
    }
}

