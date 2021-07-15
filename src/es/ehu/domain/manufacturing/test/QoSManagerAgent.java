package es.ehu.domain.manufacturing.test;

import es.ehu.domain.manufacturing.utilities.Planner;
import es.ehu.platform.utilities.XMLReader;
import es.ehu.domain.manufacturing.utilities.StructMessage;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import jade.lang.acl.MessageTemplate;
import jade.wrapper.gateway.JadeGateway;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import es.ehu.domain.manufacturing.test.timeout;

public class QoSManagerAgent extends Agent {
    private String mensaje;
    String AgentToPing;
    protected void setup() {

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                System.out.println("Sendind ping...");
                AgentToPing="ControlGatewayCont1";
                ACLMessage ping = new ACLMessage(ACLMessage.REQUEST);
                ping.setPerformative(ACLMessage.REQUEST);
                AID AgentToPingID = new AID(AgentToPing, false);
                ping.addReceiver(AgentToPingID);
                ping.setContent("¿Alive? :(");
                ping.setOntology("ping");
                myAgent.send(ping);

                System.out.println("Searching for errors...");
                System.out.println("Waiting for pong...");
                ACLMessage msg = blockingReceive();
                mensaje = msg.getContent();

                System.out.println(mensaje);
                while(true){}
                /*
                AgentToPing=msg.getOntology();
                ACLMessage ping = new ACLMessage(ACLMessage.INFORM);
                AID AgentToPingID = new AID(AgentToPing, false);
                ping.addReceiver(AgentToPingID);
                ping.setContent("¿Alive? :(");
                myAgent.send(ping);
*/
            }
        });
    }
}