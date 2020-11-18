package es.ehu.domain.manufacturing.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MPlanInterpreter {

    public static void getManEntities (ArrayList<ArrayList<ArrayList<String>>> roughPlan) {//Aquí le pasaremos también el agente para que pueda usar el método sendCommand?
        //Recibimos el plan de fabricación
        //Nos quedamos solo con los elementos masterRecipe
        ArrayList<ArrayList<ArrayList<String>>> masterRecipes = new ArrayList<ArrayList<ArrayList<String>>>();
        int size = masterRecipes.size();
        for (int i = 0; i < roughPlan.size(); i++) {
            if (roughPlan.get(i).get(0).get(0).equals("masterRecipe")) {
                masterRecipes.add(size, roughPlan.get(i));
            }
        }

        // Conseguir las maquinas del plan de fabricacion para saber si estan disponibles en el sistema
        ArrayList<String> allMachines = new ArrayList<>();
        for (int i = 0; i < roughPlan.size(); i++) {
            if (roughPlan.get(i).get(0).get(0).contains("operation")) {
                for (int j=0; j < roughPlan.get(i).get(2).size(); j++) {
                    if(roughPlan.get(i).get(2).get(j).equals("actualMachineId")) {  // Este es el id que debe tener la maquina?
                        if (!allMachines.contains(roughPlan.get(i).get(3).get(j)))
                            allMachines.add(roughPlan.get(i).get(3).get(j));
                        // Aqui se podria evitar el crear una lista y enviarle un mensaje al SystemModel agent pidiendole la maquina
                        // Como pedirselo? Con que id? machine1 != M_01
                        // Si lo tiene perfecto, si no error
                        // En el planner --> ya que es un agente
                        // sendCommand("get " + roughPlan.get(i).get(3).get(j), conversationId);
                    }
                }
            }
        }

        //Ahora componenmos el plan de fabricación con su jerarquía a partir de la secuencia de Master Recipes.
        ArrayList<ArrayList<ArrayList<String>>> structuredPlan = new ArrayList<ArrayList<ArrayList<String>>>();
        ArrayList<String> orderList = new ArrayList<String>();
        ArrayList<String> batchList = new ArrayList<String>();
        String thisOrder = "";
        String thisBatch = "";
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
        structuredPlan.get(entities).get(3).add("MP1"); //Este nombre podría salir del nombre del fichero XML
        entities=entities+1;//Después de guardar un elemento, sumo 1 al contador

        //Ahora completamos la jerarquía
        for (int j = 0; j < masterRecipes.size(); j++){
            //Compruebo si la receta tiene order asociado en sus atributos
            if(masterRecipes.get(j).get(2).contains("orderName")){
                //Estoy en order, nivel 2
                hl=2; //Este valor luego no irá hard coded, sino que se extraerá de un modelo

                //Obtengo la posición en la que está el orderName, y obtengo su valor
                index=masterRecipes.get(j).get(2).indexOf("orderName");
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
                    structuredPlan.get(entities).get(2).add("orderName");
                    structuredPlan.get(entities).get(3).add(thisOrder); //El orderName lo tengo buscado de antes
                    entities=entities+1;//Después de guardar un elemento, sumo 1 al contador
                    orderList.add(thisOrder);//Añadimos el order al orderList
                } else {//Si ya lo contiene, no hago nada (en nuestro caso)
                }
            }

            //Compruebo si la receta tiene batch asociado
            if (masterRecipes.get(j).get(2).contains("batchName")) {
                //Estoy en batch, nivel 3
                hl=3; //Este valor luego no irá hard coded, sino que se extraerá de un modelo

                //Obtengo la posición en la que está el orderName, y obtengo su valor
                index=masterRecipes.get(j).get(2).indexOf("batchName");
                thisBatch=masterRecipes.get(j).get(3).get(index);

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
                    structuredPlan.get(entities).get(2).add("batchName");
                    structuredPlan.get(entities).get(2).add("numberofItems");
                    structuredPlan.get(entities).get(2).add("prodId");
                    structuredPlan.get(entities).get(3).add(thisBatch); //El batchName lo tengo buscado de antes
                    structuredPlan.get(entities).get(3).add(String.valueOf(1)); //Inicializo el número de items a 1
                    //el refProdName no se en qué posición está, lo busco
                    index=masterRecipes.get(j).get(2).indexOf("prodId");
                    structuredPlan.get(entities).get(3).add(masterRecipes.get(j).get(3).get(index));
                    entities=entities+1;//Después de guardar un elemento, sumo 1 al contador
                    batchList.add(thisBatch);//Añadimos el order al orderList
                } else {//Si lo contiene, tengo que actualizar el número de items //AQUÍ LO HE DEJADO
                    numberofItems= Integer.parseInt(structuredPlan.get(entities-1).get(3).get(1));
                    structuredPlan.get(entities-1).get(3).set(1,String.valueOf(numberofItems+1));
                }
            }
        }

//        //Ahora determinamos las entidades a las que está asociada cada receta (cada instancia de producto)
//        Integer numberofItems =0;
//        String thisBatch = "";
//
//        HashMap<String,HashMap<String,String>> batchList = new HashMap<String,HashMap<String,String>>();
//
//        String thisOrder = "";
//        HashMap<String,ArrayList<String>> orderList = new HashMap<>();
//
//        for (int j = 0; j < masterRecipes.size(); j++){
//            //Busco el batch asociado
//            for (int k = 0; k < masterRecipes.get(j).get(2).size();k++) {
//                //Primero compruebo si la receta tiene batch asociado
//                if (masterRecipes.get(j).get(2).get(k).contains("batch")) {
//                    thisBatch = masterRecipes.get(j).get(3).get(k);
//                    //Compruebo si ya había otros elementos asociados a este batch
//                    if (!batchList.containsKey(thisBatch)){ //Si no lo contiene, lo añado a la lista
//                        batchList.put(thisBatch,new HashMap<String, String>());
//                        batchList.get(thisBatch).put("numberofItems", String.valueOf(1));
//                        batchList.get(thisBatch).put("refProdName", masterRecipes.get(j).get(3).get(5));
//                    } else { //Si lo contiene, sumo 1 al valor de la variable "numberofItems"
//                        numberofItems= Integer.parseInt(batchList.get(thisBatch).get("numberofItems"));
//                        batchList.get(thisBatch).replace("numberofItems",String.valueOf(numberofItems+1));
//                    }
//                } else if (masterRecipes.get(j).get(2).get(k).contains("order")) {
//                    thisOrder = masterRecipes.get(j).get(3).get(k);
//                    //Compruebo si ya había otros elementos asociados a este batch
//                    if (!orderList.containsKey(thisOrder)) { //Si no lo contiene, lo añado a la lista
//                        orderList.put(thisOrder, new ArrayList<>());
//                        orderList.get(thisOrder).add(masterRecipes.get(j).get(3).get(0));
//                    } else if (!orderList.get(thisOrder).contains(masterRecipes.get(j).get(3).get(0)))
//                        orderList.get(thisOrder).add(masterRecipes.get(j).get(3).get(0));
//                }
//            }
//        }
//        int stop = 2;

        System.out.println("--- BATCHES ---");
        Iterator it = batchList.listIterator();
        while (it.hasNext())
            System.out.println(it.next());

        System.out.println("--- ORDERS ---");
        it = orderList.listIterator();
        while (it.hasNext())
            System.out.println(it.next());
    }
}
