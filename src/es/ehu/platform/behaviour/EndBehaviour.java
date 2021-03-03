package es.ehu.platform.behaviour;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.platform.MWAgent;

import jade.core.behaviours.OneShotBehaviour;

public class EndBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = -2673578185687045396L;
    static final Logger LOGGER = LogManager.getLogger(EndBehaviour.class.getName()) ;

    MWAgent myAgent;

    public EndBehaviour(MWAgent a) {
        super(a);
        myAgent = a;
    }

    @Override
    public void action() {

        try {
            ((MWAgent)myAgent).deregisterAgent(myAgent.getLocalName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        myAgent.functionalityInstance.terminate(myAgent);
        ((MWAgent)myAgent).doDelete();

    } // end action

    public int onEnd() {
        return 1;
    }


}
