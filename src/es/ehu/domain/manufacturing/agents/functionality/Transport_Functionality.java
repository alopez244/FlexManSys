package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.domain.manufacturing.agents.MachineAgent;
import es.ehu.domain.manufacturing.agents.TransportAgent;
import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.AssetManagement;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.NegFunctionality;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import jade.util.Logger;
import jade.wrapper.AgentController;
import org.apache.commons.lang3.ArrayUtils;
import java.util.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Transport_Functionality extends DomApp_Functionality implements BasicFunctionality, NegFunctionality, AssetManagement {

    /** Identifier of the agent. */
    private TransportAgent myAgent;

    /** Class name to switch on the agent */
    private String className;

    @Override
    public Void init(MWAgent mwAgent) {

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

        //myAgent.initTransition = ControlBehaviour.RUNNING;

        return null;

    }

    @Override
    public Object execute(Object[] input) {

        ArrayList<ArrayList<String>> consumables = new ArrayList<>();
        consumables.add(new ArrayList<>()); consumables.add(new ArrayList<>());
        ACLMessage msg = (ACLMessage) input[0];
        String content = msg.getContent();
        AID sender = msg.getSender();
        String [] neededMaterial = content.split(";");
        for (int i = 0; i < neededMaterial.length ; i++) {
            consumables.get(0).add(neededMaterial[i].split(":")[0]);
            consumables.get(1).add(neededMaterial[i].split(":")[1]);
        }

        //TODO Dar funcionalidad a los datos guardados en consumables (material que se ha pedido al transporte)

        //Peticion de entrada provisional para simular que el transporte entrega el material pedido
        Scanner reader = new Scanner(System.in);
        int caracter;
        System.out.println("Introduzca cualquier numero cuando desee simular que el transporte ha entregado los nuevos consumibles");
        caracter = reader.nextInt();

        String providedConsumables = "";
        for (int i = 0; i < consumables.get(0).size(); i++) {
            providedConsumables = providedConsumables.concat(consumables.get(0).get(i) + ":" + consumables.get(1).get(i) + ";");
        }

        sendACLMessage(7, sender, "data", "ProvidedConsumables", providedConsumables, myAgent);

        return null;
    }

    @Override
    public Void terminate(MWAgent myAgent) {return null;}

    @Override
    public long calculateNegotiationValue(String negAction, String negCriterion, Object... negExternalData) {
        return 0;
    }

    @Override
    public int checkNegotiation(String negId, String winnerAction, double negReceivedValue, long negScalarValue, boolean tieBreaker, boolean checkReplies, Object... negExternalData) {
        return 0;
    }

    @Override
    public void rcvDataFromPLC(ACLMessage msg) {

    }

    @Override
    public void recvBatchInfo(ACLMessage msg) {

    }

    @Override
    public void sendDataToPLC() {

    }
}
