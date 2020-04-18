package es.ehu.domain.manufacturing.utilities;

import es.ehu.platform.utilities.XMLReader;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ManResourceLauncher {
    public static void main(String[] args) throws Exception {
        String uri="classes/resources/ResInstances/RA1.xml";
        XMLReader fileReader = new XMLReader();
        ArrayList<ArrayList<ArrayList<String>>> xmlelements = fileReader.readFile(uri);
        System.out.println();

        //Variable initialization
        Integer size = xmlelements.get(0).get(2).size();
        String[] command = new String[size];
        //String parentId = "system";
        String seId = "";
        String className = "es.ehu.domain.manufacturing.agents.ManResourceAgent";
        String nickname = "aux-%random%";

        for (int j = 0; j < xmlelements.get(0).get(2).size(); j++){
            command[j] = xmlelements.get(0).get(2).get(j) + "=" + xmlelements.get(0).get(3).get(j);
        }

        //Create a new container
        // get a JADE runtime
        jade.core.Runtime rt = jade.core.Runtime.instance();
        // create a default profile
        Profile p = new ProfileImpl();
        // create the Main-container
        ContainerController cc = rt.createAgentContainer(p);
        AgentController ac = cc.createNewAgent(nickname, className, command);
        ac.start();
    }
}
