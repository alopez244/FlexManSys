package es.ehu.domain.manufacturing.utilities;

import com.sun.org.apache.xerces.internal.xs.ItemPSVI;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

public class MPlanInterpreter {

    public static ArrayList<ArrayList<ArrayList<String>>> getManEntities (Agent myAgent, ArrayList<ArrayList<ArrayList<String>>> roughPlan, String planName) {//Aquí le pasaremos también el agente para que pueda usar el método sendCommand?

        //Recibimos el plan de fabricación
        //Nos quedamos solo con los elementos masterRecipe
        ArrayList<ArrayList<ArrayList<String>>> masterRecipes = new ArrayList<ArrayList<ArrayList<String>>>();
        int size = masterRecipes.size();
        for (int i = 0; i < roughPlan.size(); i++) {
            if (roughPlan.get(i).get(0).get(0).equals("PlannedItem")) {
                masterRecipes.add(size, roughPlan.get(i));
            }
        }

        // Conseguir las máquinas del plan de fabricación para saber si están disponibles en el sistema
        ArrayList<String> allMachines = new ArrayList<>();
        for (int i = 0; i < roughPlan.size(); i++) {
            if (roughPlan.get(i).get(0).get(0).contains("Operation")) {
                for (int j=0; j < roughPlan.get(i).get(2).size(); j++) {
                    if(roughPlan.get(i).get(2).get(j).equals("plannedStationId")) {  // De momento guardamos la id de la estacion
                        if (!allMachines.contains(roughPlan.get(i).get(3).get(j)))
                            allMachines.add(roughPlan.get(i).get(3).get(j));

                        // Como pedirselo? Con que id? machine1 != M_01
                        // Si lo tiene perfecto, si no error
                        // En el planner --> ya que es un agente
                        // sendCommand("get " + roughPlan.get(i).get(3).get(j), conversationId);
                    }
                }
            }
        }

        // Teniendo la lista de todas las maquinas del plan comprobaremos que estan disponibles
        boolean error = false;
        for (String machine: allMachines) {
            String getMachineQuery = "get * category=machine id=" + machine;
            try {
                ACLMessage reply = sendMessage(myAgent, getMachineQuery, "MPlanInterpreter"+ new Random().nextInt(100-1) + 1);
                if (reply != null) {
                    String content = reply.getContent();
                    if (content == null)    // De momento lo hacemos asi, mas adelante se puede comprobar si la id de la maquina del SMA concuerda con la del plan
                        error = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (error) {
            // Si alguna maquina no esta disponible no se puede registrar la maquina
            return null;    // De momento lo dejamos asi, pero habria que poner un mensaje de error o lanzar una excepcion
            //TODO antes del return, printear por pantalla que no se puede registrar el plan porque alguna máquina no está disponible
        }

        //TODO si las maquinas estan disponibles hay que enviarles las operaciones que van a hacer

        // Recorremos otra vez el roughPlan y a cada maquina le enviamos la informacion de sus operaciones
        // Vamos guardamos en un HashMap toda la informacion de todas las operaciones por cada maquina
        HashMap<String, String> machinesWithAllOpInfo = new HashMap<>();

        // Lista para saber que atributos buscar en cada operacion y en la masterRecipe
        ArrayList<String> attribsToFind = new ArrayList<>();

        attribsToFind.add("batch_ID");
        attribsToFind.add("id");
        attribsToFind.add("item_ID");
        attribsToFind.add("order_ID");
        attribsToFind.add("plannedFinishTime");
        attribsToFind.add("plannedStartTime");
        attribsToFind.add("productType");
        attribsToFind.add("type");

        String masterAttributes = "";
        String machineId = null;

        for (int i = 0; i < roughPlan.size(); i++) {
            if (roughPlan.get(i).get(0).get(0).equals("PlannedItem")) {
                masterAttributes = "";
                for (int m = 0; m < roughPlan.get(i).get(2).size(); m++) {
                    if (attribsToFind.contains(roughPlan.get(i).get(2).get(m)))
                        masterAttributes = masterAttributes + roughPlan.get(i).get(2).get(m) + "=" + roughPlan.get(i).get(3).get(m) + " ";
                }
            }
            else if (roughPlan.get(i).get(0).get(0).contains("Operation")) {

                // Get machine ID
                for (int z = 0; z < roughPlan.get(i).get(2).size(); z++) {
                    if (roughPlan.get(i).get(2).get(z).equals("plannedStationId")) {
                        ACLMessage reply = null;
                        try {
                            String getMachineQuery = "get * category=machine id=" + roughPlan.get(i).get(3).get(z);
                            reply = sendMessage(myAgent, getMachineQuery, "MPlanInterpreter"+ new Random().nextInt(100-1) + 1);
                            if (reply != null)
                                machineId = reply.getContent();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                // Añadimos las informacion que se le va a enviar a las maquinas
                for (int j = 0; j < roughPlan.get(i).get(2).size(); j++) {
                    if (attribsToFind.contains(roughPlan.get(i).get(2).get(j)))
                        if (machinesWithAllOpInfo.get(machineId) == null)
                            machinesWithAllOpInfo.put(machineId, roughPlan.get(i).get(2).get(j) + "=" + roughPlan.get(i).get(3).get(j) + " ");
                        else
                            machinesWithAllOpInfo.put(machineId, machinesWithAllOpInfo.get(machineId) + roughPlan.get(i).get(2).get(j) + "=" + roughPlan.get(i).get(3).get(j) + " ");
                }
                machinesWithAllOpInfo.put(machineId, machinesWithAllOpInfo.get(machineId) + masterAttributes + "&");

            }
        }

        Iterator itr = machinesWithAllOpInfo.entrySet().iterator();
        while (itr.hasNext()) {

            Map.Entry pair = (Map.Entry) itr.next();
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID((String) pair.getKey(), AID.ISLOCALNAME));
            msg.setOntology("data");
            msg.setContent((String) pair.getValue());
            myAgent.send(msg);
            ACLMessage ack= myAgent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),500);
            if(ack==null){
//                System.out.println("ERROR. "+msg.getAllReceiver()+" did not answer on time.");
                return null;
            }

        }
        //No es necesario con la estructura nueva de XML ***************************************************************


        //Ahora componenmos el plan de fabricación con su jerarquía a partir de la secuencia de Master Recipes.

        ArrayList<ArrayList<ArrayList<String>>> structuredPlan = new ArrayList<ArrayList<ArrayList<String>>>();
        ArrayList<String> orderList = new ArrayList<String>();
        ArrayList<String> batchList = new ArrayList<String>();
        ArrayList<String> ItemList = new ArrayList<String>();
        ArrayList<String> OPList = new ArrayList<String>();
        ArrayList<String> ActionList = new ArrayList<String>();
        String thisOrder = "";
        String thisBatch = "";
        String thisItem = "";
        String thisOp = "";
        Integer index;
        Integer hl = 1;
        Integer entities = structuredPlan.size();
        Integer numberofItems =0;

        //Primero, añadimos el elemento MPlan
        structuredPlan.add(entities,new ArrayList<ArrayList<String>>());
        structuredPlan.get(entities).add(0,new ArrayList<String>());
        structuredPlan.get(entities).add(1,new ArrayList<String>());
        structuredPlan.get(entities).add(2,new ArrayList<String>());
        structuredPlan.get(entities).add(3,new ArrayList<String>());
        structuredPlan.get(entities).get(0).add("mPlan");
        structuredPlan.get(entities).get(1).add(hl.toString());
        structuredPlan.get(entities).get(2).add("name");
        structuredPlan.get(entities).get(3).add(planName); //Este nombre podría salir del nombre del fichero XML
        entities=entities+1;//Después de guardar un elemento, sumo 1 al contador

        //Ahora completamos la jerarquía
        for (int j = 0; j < masterRecipes.size(); j++){
            //Compruebo si la receta tiene order asociado en sus atributos
            if(masterRecipes.get(j).get(2).contains("order_ID")){
                //Estoy en order, nivel 2
                hl=2; //Este valor luego no irá hard coded, sino que se extraerá de un modelo

                //Obtengo la posición en la que está el orderName, y obtengo su valor
                index=masterRecipes.get(j).get(2).indexOf("order_ID");
                thisOrder=masterRecipes.get(j).get(3).get(index);

                //Compruebo si es la primera receta asociada a este order
                if (!orderList.contains(thisOrder)) {//Si no lo contiene, lo guardo
                    structuredPlan.add(entities,new ArrayList<ArrayList<String>>());
                    structuredPlan.get(entities).add(0,new ArrayList<String>());
                    structuredPlan.get(entities).add(1,new ArrayList<String>());
                    structuredPlan.get(entities).add(2,new ArrayList<String>());
                    structuredPlan.get(entities).add(3,new ArrayList<String>());
                    //La información que está escrita en string directamente, posteriormente se leerá de un modelo
                    structuredPlan.get(entities).get(0).add("order");
                    structuredPlan.get(entities).get(1).add(hl.toString());
                    structuredPlan.get(entities).get(2).add("order_ID");
                    structuredPlan.get(entities).get(3).add(thisOrder); //El order_ID lo tengo buscado de antes
                    entities=entities+1;//Después de guardar un elemento, sumo 1 al contador
                    orderList.add(thisOrder);//Añadimos el order al orderList
                } else {//Si ya lo contiene, no hago nada (en nuestro caso)
                }
            }

            //Compruebo si la receta tiene batch asociado
            if (masterRecipes.get(j).get(2).contains("batch_ID")) {
                //Estoy en batch, nivel 3
                hl=3; //Este valor luego no irá hard coded, sino que se extraerá de un modelo

                //Obtengo la posición en la que está el orderName, y obtengo su valor
                index=masterRecipes.get(j).get(2).indexOf("batch_ID");
                thisBatch=masterRecipes.get(j).get(3).get(index);

                index=masterRecipes.get(j).get(2).indexOf("item_ID");
                thisItem = masterRecipes.get(j).get(3).get(index);



                //Compruebo si es la primera receta asociada a este batch
                if (!batchList.contains(thisBatch)) {//Si no lo contiene, lo guardo
                    structuredPlan.add(entities,new ArrayList<ArrayList<String>>());
                    structuredPlan.get(entities).add(0,new ArrayList<String>());
                    structuredPlan.get(entities).add(1,new ArrayList<String>());
                    structuredPlan.get(entities).add(2,new ArrayList<String>());
                    structuredPlan.get(entities).add(3,new ArrayList<String>());

                    //La información que está escrita en string directamente, posteriormente se leerá de un modelo
                    structuredPlan.get(entities).get(0).add("batch");
                    structuredPlan.get(entities).get(1).add(hl.toString());
                    structuredPlan.get(entities).get(2).add("batch_ID");
                    structuredPlan.get(entities).get(2).add("numberOfItems");
                    structuredPlan.get(entities).get(2).add("productType");

                    structuredPlan.get(entities).get(3).add(thisBatch); //El batch_ID lo tengo buscado de antes

                    // Añado a la lista de items ID
                    structuredPlan.get(entities).get(3).add(thisItem);
                    //structuredPlan.get(entities).get(3).add(String.valueOf(1)); //Inicializo el número de items a 1

                    //el productType no se en qué posición está, lo busco
                    index=masterRecipes.get(j).get(2).indexOf("productType");
                    structuredPlan.get(entities).get(3).add(masterRecipes.get(j).get(3).get(index));
                    entities=entities+1;//Después de guardar un elemento, sumo 1 al contador
                    batchList.add(thisBatch);//Añadimos el batch al batchList



                } else {//Si lo contiene, tengo que actualizar el número de items //AQUÍ LO HE DEJADO

                    structuredPlan.get(entities - 1).get(3).set(1, structuredPlan.get(entities-1).get(3).get(1) + "," + thisItem);
                    //String allItems = structuredPlan.get(entities-1).get(3).get(1);
                    //structuredPlan.get(entities-1).get(3).set(1,allItems.substring(0, allItems.length()-1));
                }




            }
        }

        System.out.println("--- BATCHES ---");
        Iterator it = batchList.listIterator();
        while (it.hasNext())
            System.out.println(it.next());

        System.out.println("--- ORDERS ---");
        it = orderList.listIterator();
        while (it.hasNext())
            System.out.println(it.next());

        return roughPlan;
    }

    public static ArrayList<String> getItemFT (Agent myAgent, ArrayList<ArrayList<ArrayList<String>>> roughPlan, String planName, String batch) {

        ArrayList<String>itemFT=new ArrayList<String>();
        for(int i=0;i<roughPlan.size();i++){
            if(roughPlan.get(i).get(0).get(0).equals("batch")){
                if(roughPlan.get(i).get(3).get(2).equals(batch)) {
                    for (int j = 1; j + i < roughPlan.size() && !roughPlan.get(j + i).get(0).get(0).equals("batch"); j++) {
                        if (roughPlan.get(j + i).get(0).get(0).contains("PlannedItem")) {
                            String ToAdd=roughPlan.get(j + i).get(3).get(1);
                            String FTItem="";
                            for (int k = 1; k + j + i < roughPlan.size() && !roughPlan.get(k + j + i).get(0).get(0).contains("PlannedItem"); k++) {
                                if (roughPlan.get(k + j + i).get(0).get(0).contains("Operation")) {
                                    FTItem=roughPlan.get(k + j + i).get(3).get(2);
                                }
                            }
//                            ToAdd=ToAdd+"/"+FTItem;
                            itemFT.add(ToAdd+"/"+FTItem);
                        }
                    }
                }
            }
        }
        return itemFT;
    }

    public static ArrayList<String> getBatchFT (Agent myAgent, ArrayList<ArrayList<ArrayList<String>>> roughPlan, String planName, String order) {

        ArrayList<String>batchFT=new ArrayList<String>();
        for(int i=0;i<roughPlan.size();i++){
            if(roughPlan.get(i).get(0).get(0).equals("order")){
                if(roughPlan.get(i).get(3).get(0).equals(order)) {
                    for(int j=1;j+i<roughPlan.size()&& !roughPlan.get(j+i).get(0).get(0).equals("order");j++) {
                        if(roughPlan.get(j+i).get(0).get(0).equals("batch")) {
                            String ToAdd = roughPlan.get(j + i).get(3).get(2);
                            String FTBatch = "";
                            for (int k = 1; k + j + i < roughPlan.size() && !roughPlan.get(k + j + i).get(0).get(0).equals("batch"); k++) {
                                if (roughPlan.get(k + j + i).get(0).get(0).contains("PlannedItem")) {

                                    for (int l = 1; l + k + j + i < roughPlan.size() && !roughPlan.get(l + k + j + i).get(0).get(0).contains("PlannedItem"); l++) {
                                        if (roughPlan.get(l + k + j + i).get(0).get(0).contains("Operation")) {
                                            FTBatch = roughPlan.get(l + k + j + i).get(3).get(2);
                                        }
                                    }
//                            ToAdd=ToAdd+"/"+FTItem;

                                }

                            }
                            batchFT.add(ToAdd + "/" + FTBatch);
                        }
                    }
                }
            }
        }
        return batchFT;
    }


    public static ACLMessage sendMessage(Agent myAgent, String cmd, String conversationId) throws Exception {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType("sa");
        dfd.addServices(sd);
        String mwm;

        while (true) {
            DFAgentDescription[] result = DFService.search(myAgent,dfd);

            if ((result != null) && (result.length > 0)) {
                dfd = result[0];
                mwm = dfd.getName().getLocalName();
                break;
            }

            Thread.sleep(100);

        } //end while (true)

        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID(mwm, AID.ISLOCALNAME));
        msg.setConversationId(conversationId);
        msg.setOntology("control");
        msg.setContent(cmd);
        msg.setReplyWith(cmd);

        myAgent.send(msg);
//Todo revisar este blocking receive
        /*
        ACLMessage reply = myAgent.blockingReceive(MessageTemplate.and(
                MessageTemplate.MatchInReplyTo(msg.getReplyWith()),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM))
                , 1000);
         */
        ACLMessage reply = myAgent.blockingReceive();
        return reply;
    }
}
