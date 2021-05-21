package es.ehu.domain.manufacturing.agents.functionality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DomRes_Functionality extends Dom_Functionality{

    public HashMap createOperationHashmap(ArrayList<ArrayList<ArrayList<String>>> machinePlan, int index) {

        ArrayList<String> auxiliar = new ArrayList<>();
        List<String> itemNumbers = new ArrayList<String>(); //to track each of the items that are added to the operation
        Boolean ItemContFlag = true;
        Boolean newItem = false;
        Boolean breakFlag = false;
        HashMap PLCmsgOut = new HashMap();
        String BathcID = "";
        Integer NumOfItems = 0;

        for (int j = index; j < machinePlan.size(); j++) {  //Looks for the operation to be manufactured in the machine plan
            for (int k = 0; k < machinePlan.get(j).size(); k++) {
                auxiliar = machinePlan.get(j).get(k);
                if (auxiliar.get(0).equals("operation")) {
                    ArrayList<String> auxiliar2 = machinePlan.get(j).get(k + 3);

                    if (ItemContFlag == true) { //saves the information of the operation only when founds the first item, then just increments the item counter
                        BathcID = auxiliar2.get(4);  //saves the information of the operation in PLCmsgOut
                        PLCmsgOut.put("Control_Flag_New_Service", true);
                        PLCmsgOut.put("Id_Batch_Reference", Integer.parseInt(BathcID));
                        PLCmsgOut.put("Id_Order_Reference", Integer.parseInt(auxiliar2.get(6)));
                        PLCmsgOut.put("Id_Ref_Subproduct_Type", Integer.parseInt(auxiliar2.get(7)));
                        PLCmsgOut.put("Operation_Ref_Service_Type", Integer.parseInt(auxiliar2.get(0)));

                        ItemContFlag = false;
                    }
                    // Se comprueba que el id del item no este registrado. En este caso, se añade el id a la lista y se activa el flag newItem para que sea contado
                    if (!itemNumbers.contains(auxiliar2.get(5)) && auxiliar2.get(4).equals(BathcID)) {
                        itemNumbers.add(auxiliar2.get(5));
                        newItem = true;     //the item is counted
                    }
                    //Si newItem esta a true y el batch ID concuerda con el esperado se incrementa el contador de piezas
                    if (ItemContFlag == false && auxiliar2.get(4).equals(BathcID) && newItem == true) { //counts all the items with the same batch number
                        NumOfItems++;
                        newItem = false;
                    }
                    // Si se llega a una pieza que ya no pertene al lote que se esta contabilizando, se sale del bucle
                    if (!itemNumbers.contains(auxiliar2.get(5)) && !auxiliar2.get(4).equals(BathcID)) {
                        index = j; // Se guarda el indice para seguir contando desde ese punto en la siguiente llamada a la funcion
                        breakFlag = true;
                        break;
                    }
                }
            }
            if (breakFlag) {
                break;
            }
        }
        if (!breakFlag) {
            index = machinePlan.size();
        }
        PLCmsgOut.put("Operation_No_of_Items", NumOfItems);
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
