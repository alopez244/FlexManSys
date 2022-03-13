package es.ehu.domain.manufacturing.agents.functionality;

import java.util.ArrayList;
import java.util.HashMap;

public class DomRes_Functionality extends Dom_Functionality{

    public HashMap createOperationHashMap(ArrayList<ArrayList<ArrayList<String>>> machinePlan, int index) {

        /* Hay una declaración e inicialización de variables */
        HashMap PLCmsgOut = new HashMap();
        Integer NumOfItems = 1; //Al menos va a haber un item

        /* Queremos guardar la referencia de la máquina (el segundo elemento del plan) */
        PLCmsgOut.put("Id_Machine_Reference", Integer.parseInt(machinePlan.get(1).get(3).get(0)));

        /* Queremos guardar los valores de algunos atributos de la tercera posición (primera operación) */
        PLCmsgOut.put("Control_Flag_New_Service", true);
        PLCmsgOut.put("Id_Batch_Reference", Integer.parseInt(machinePlan.get(2).get(3).get(3)));
        PLCmsgOut.put("Id_Order_Reference", Integer.parseInt(machinePlan.get(2).get(3).get(5))); //antes get 6 tras cambio en xml -> 5
        PLCmsgOut.put("Id_Ref_Subproduct_Type", Integer.parseInt(machinePlan.get(2).get(3).get(6))); //antes get 7 tras cambio en xml -> 6
        PLCmsgOut.put("Operation_Ref_Service_Type", Integer.parseInt(machinePlan.get(2).get(3).get(0)));
        PLCmsgOut.put("Operation_No_of_Items", NumOfItems);

        /* Sé que tengo al menos un item, pero no sé si tengo más. Lo compruebo */
        if (machinePlan.size()>=4) {

            /* Ahora recorro el resto del plan para ir incrementando el número de items que pertenecen al batch */
            for (int i = 3; i< machinePlan.size(); i++){

                /* Si el batch_Id coincide, hay que incrementar el número de items */
                if (machinePlan.get(i).get(3).get(3).equals(PLCmsgOut.get("Id_Batch_Reference").toString())){
                    NumOfItems++;
                    PLCmsgOut.put("Operation_No_of_Items", NumOfItems);
                    index = i+1; //el índice apunta a la siguiente posición

                } else { //Si el batch_id no coincide, solo hay que indicar que esta es la siguiente posición a mirar
                    index = i;
                }
            }
        }
        PLCmsgOut.put("Index", index);
        return PLCmsgOut;
    }

    public ArrayList<String> defineConsumableList(String serviceType, ArrayList<ArrayList<ArrayList<String>>> resourceModel) {
        ArrayList<String> consumableList = new ArrayList<String>();
        for (int l = 0; l < resourceModel.size(); l++) {
            if (resourceModel.get(l).get(0).get(0).equals("simple_operation")) {
                if (resourceModel.get(l).get(3).get(1).equals(serviceType)) {
                    for (int m = l + 1; m < resourceModel.size(); m++)  {
                        if (resourceModel.get(m).get(0).get(0).equals("consumable")){
                            consumableList.add(resourceModel.get(m).get(3).get(1)); //The used consumable is saved
                        } else if (resourceModel.get(m).get(0).get(0).equals("simple_operation")) {
                            break;
                        }
                    }
                }
            }
        }
        return consumableList;
    }

}
