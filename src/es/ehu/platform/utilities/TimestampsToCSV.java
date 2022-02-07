package es.ehu.platform.utilities;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimestampsToCSV {
    private static TimestampsToCSV instance;
    public HashMap <String, HashMap<String,String>> times;
    List <String[]> allData;

    private TimestampsToCSV() {

        times = new HashMap<>();
    }

    public static TimestampsToCSV getInstance() {
        if (instance == null) {
            instance = new TimestampsToCSV();
        }
        return instance;
    }

    public void FillCSV () {

        for (Map.Entry<String, HashMap<String, String>> componente : times.entrySet()){
            System.out.println(componente.getKey());
            for (Map.Entry<String,String> tiempos : componente.getValue().entrySet() ) {
                System.out.println("  " + tiempos.getKey() + " "+tiempos.getValue());
            }
        }

//        //Pasar el HashMap times a una estructura List<String[]>
//        List <String[]> allData;



    }

    public void CreateComponent (String id){

        times.put(id,new HashMap<>());
    }

    public void AddData (String id, String category, String data){

        times.get(id).put(category,data);
    }

}
