//import jade.core.Profile;
//import jade.util.leap.Properties;
//import jade.wrapper.gateway.JadeGateway;
//
//import java.util.HashMap;
//import structs.Estructura;
//
//
//public class ExternalJADEgw_v2 {
//
//    public static void main(String[] args) {
//        System.out.println("Main");
//        while(true) {
//            send();
//            try {
//                Thread.sleep(500); // para dar tiempo a que los mensajes de log se impriman después del log del arranque del contenedor
//            } catch(Exception e) {
//                ;
//            }
//            recv();
//            try {
//                Thread.sleep(3000); // para dar tiempo a que los mensajes de log se impriman después del log del arranque del contenedor
//            } catch(Exception e) {
//                ;
//            }
//            System.out.println();
//        }
//    }
//
//    public static void send() {
//        String host = "192.168.5.200";
//        String port = "1099";
//        Properties pp = new Properties();
//        pp.setProperty(Profile.MAIN_HOST, host);
//        pp.setProperty(Profile.MAIN_PORT, port);
//        JadeGateway.init("GWagente", pp);
//
//        HashMap map = new HashMap();
//        map.put("num1", Double.toString(Math.random()));
//        map.put("num2", Double.toString(Math.random()));
//
//        try {
//            JadeGateway.execute(map);
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//
//        System.out.print("SEND#");
//        System.out.print(map.get("num1"));
//        System.out.print("#");
//        System.out.print(map.get("num2"));
//        System.out.println("#");
//    }
//
//    public static void recv() {
//        String host = "192.168.5.200";
//        String port = "1099";
//        Properties pp = new Properties();
//        pp.setProperty(Profile.MAIN_HOST, host);
//        pp.setProperty(Profile.MAIN_PORT, port);
//        JadeGateway.init("GWagente", pp);
//
//        Estructura estruc = new Estructura();
//
//        try {
//            JadeGateway.execute(estruc);
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//
//        System.out.print("RECV#");
//        System.out.print(estruc.getNum1());
//        System.out.print("#");
//        System.out.print(estruc.getNum2());
//        System.out.println("#");
//    }
//
//}
