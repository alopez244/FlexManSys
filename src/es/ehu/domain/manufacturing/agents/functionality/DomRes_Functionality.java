package es.ehu.domain.manufacturing.agents.functionality;

import java.util.ArrayList;
import java.util.HashMap;

public class DomRes_Functionality extends Dom_Functionality{

//    public HashMap createOperationHashMap(ArrayList<ArrayList<ArrayList<String>>> machinePlan) {
//
//        /* Hay una declaraci�n e inicializaci�n de variables */
//        HashMap PLCmsgOut = new HashMap();
//        Integer NumOfItems = 1; //Al menos va a haber un item
//
//        /* Queremos guardar la referencia de la m�quina (el segundo elemento del plan) */
//        PLCmsgOut.put("Id_Machine_Reference", Integer.parseInt(machinePlan.get(1).get(3).get(0)));
//
//        /* Queremos guardar los valores de algunos atributos de la tercera posici�n (primera operaci�n) */
//        PLCmsgOut.put("Control_Flag_New_Service", true);
//        PLCmsgOut.put("Id_Batch_Reference", Integer.parseInt(machinePlan.get(2).get(3).get(3)));
//        PLCmsgOut.put("Id_Order_Reference", Integer.parseInt(machinePlan.get(2).get(3).get(5))); //antes get 6 tras cambio en xml -> 5
//        PLCmsgOut.put("Id_Ref_Subproduct_Type", Integer.parseInt(machinePlan.get(2).get(3).get(6))); //antes get 7 tras cambio en xml -> 6
//        PLCmsgOut.put("Operation_Ref_Service_Type", Integer.parseInt(machinePlan.get(2).get(3).get(0)));
//        PLCmsgOut.put("Operation_No_of_Items", NumOfItems);
//
//        /* S� que tengo al menos un item, pero no s� si tengo m�s. Lo compruebo */
//        if (machinePlan.size()>=4) {
//
//            /* Ahora recorro el resto del plan para ir incrementando el n�mero de items que pertenecen al batch */
//            for (int i = 3; i< machinePlan.size(); i++){
//
//                /* Si el batch_Id coincide, hay que incrementar el n�mero de items */
//                if (machinePlan.get(i).get(3).get(3).equals(PLCmsgOut.get("Id_Batch_Reference").toString())){
//                    NumOfItems++;
//                    PLCmsgOut.put("Operation_No_of_Items", NumOfItems);
//
//                } else { //Si el batch_id no coincide, se sale del bucle
//                    break;
//                }
//            }
//        }
//        return PLCmsgOut;
//    }
}
