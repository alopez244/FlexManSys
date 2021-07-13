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


public class QoSManagerAgent extends Agent {
    private String mensaje;

    protected void setup() {

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                System.out.println("Searching for errors...");
                ACLMessage msg = blockingReceive();
                mensaje = msg.getContent();
                //System.out.println("test2");
                System.out.println(mensaje);
            }
        });
    }
}