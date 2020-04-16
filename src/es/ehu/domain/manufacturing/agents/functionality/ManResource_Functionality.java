package es.ehu.domain.manufacturing.agents.functionality;

import es.ehu.platform.MWAgent;
import es.ehu.platform.template.interfaces.BasicFunctionality;
import es.ehu.platform.template.interfaces.NegFunctionality;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class ManResource_Functionality implements BasicFunctionality, NegFunctionality {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Agent myAgent;

    @Override
    public String init(MWAgent myAgent) {
        this.myAgent = myAgent;
        LOGGER.entry();

        String attribs = "";
        String [] args = (String[]) myAgent.getArguments();

        for (int i=0; i<args.length; i++){
            if (!args[i].toString().toLowerCase().startsWith("id=")) attribs += " "+args[i];
            if (args[i].toString().toLowerCase().startsWith("id=")) return "";
        }

        String cmd = "reg manResource parent=system"+attribs;

        ACLMessage reply = null;
        try {
            reply = myAgent.sendCommand(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String respuesta = reply.getContent();

        LOGGER.info(myAgent.getLocalName()+" ("+cmd+")"+" > mwm < "+respuesta);
        return LOGGER.exit(respuesta);
    }

    @Override
    public Object execute(Object[] input) {
        return null;
    }

    @Override
    public long calculateNegotiationValue(String negCriterion, Object... negExternalData) {
        return 0;
    }

    @Override
    public int checkNegotiation(String negId, String winnerAction, double negReceivedValue, long negScalarValue, boolean tieBreaker, boolean checkReplies, Object... negExternalData) {
        return 0;
    }
}
