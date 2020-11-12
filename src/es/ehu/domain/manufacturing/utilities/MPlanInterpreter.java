package es.ehu.domain.manufacturing.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MPlanInterpreter {

    public static void getManEntities (ArrayList<ArrayList<ArrayList<String>>> roughPlan) {
        //Recibimos el plan de fabricación
        //Nos quedamos solo con los elementos masterRecipe
        ArrayList<ArrayList<ArrayList<String>>> masterRecipes = new ArrayList<ArrayList<ArrayList<String>>>();
        int size = masterRecipes.size();
        for (int i = 0; i < roughPlan.size(); i++) {
            if (roughPlan.get(i).get(0).get(0).equals("masterRecipe")) {
                masterRecipes.add(size, roughPlan.get(i));
            }
        }

        //Ahora determinamos las entidades a las que está asociada cada receta (cada instancia de producto)
        Integer numberofItems =0;
        String thisBatch = "";
        HashMap<String,HashMap<String,String>> batchList = new HashMap<String,HashMap<String,String>>();
        for (int j = 0; j < masterRecipes.size(); j++){
            //Busco el batch asociado
            for (int k = 0; k < masterRecipes.get(j).get(2).size();k++){
                //Primero compruebo si la receta tiene batch asociado
                if (masterRecipes.get(j).get(2).get(k).contains("batch")){
                    thisBatch = masterRecipes.get(j).get(3).get(k);
                    //Compruebo si ya había otros elementos asociados a este batch
                    if (!batchList.containsKey(thisBatch)){ //Si no lo contiene, lo añado a la lista
                        batchList.put(thisBatch,new HashMap<String, String>());
                        batchList.get(thisBatch).put("numberofItems", String.valueOf(1));
                        batchList.get(thisBatch).put("refProdName", masterRecipes.get(j).get(3).get(5));
                    } else { //Si lo contiene, sumo 1 al valor de la variable "numberofItems"
                        numberofItems= Integer.parseInt(batchList.get(thisBatch).get("numberofItems"));
                        batchList.get(thisBatch).replace("numberofItems",String.valueOf(numberofItems+1));
                    }
                }
            }
        }
        int stop = 2;

        Iterator it = batchList.entrySet().iterator();
        while (it.hasNext())
            System.out.println(it.next());
    }
}
