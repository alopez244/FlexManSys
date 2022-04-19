package es.ehu.platform.test;

import com.opencsv.CSVWriter;
import es.ehu.domain.manufacturing.utilities.StructMessage;
import jade.core.Profile;
import jade.util.leap.Properties;
import jade.wrapper.gateway.JadeGateway;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentToCSVgw {

    public static void main(String[] args) {

        agentInit();

        System.out.println("Pulse intro cuando desee ver en pantalla los resultados recopilados por el gwAgent y pasarlos a un fichero");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Object [] resultsArray=printData();
        storedata(resultsArray);

    }

    private static void storedata(Object[] resultsArray) {

        //Recibo un array con dos HashMap <String,HashMap<String,String>> que tengo que pasar a List<String[]>
        //Primero, obtengo los HashMaps del objeto
        HashMap<String, HashMap<String, HashMap<String, String>>> ParentResults = (HashMap<String, HashMap<String, HashMap<String, String>>>) resultsArray [0];

        HashMap<String, HashMap<String, HashMap<String, String>>> ParentResultsApp = (HashMap<String, HashMap<String, HashMap<String, String>>>) resultsArray [1];

        HashMap<String, HashMap<String, HashMap<String, String>>> ParentResultsErr = (HashMap<String, HashMap<String, HashMap<String, String>>>) resultsArray [2];

        HashMap<String, HashMap<String, HashMap<String, String>>> ParentResultsNeg = (HashMap<String, HashMap<String, HashMap<String, String>>>) resultsArray [3];


//        HashMap<String, HashMap<String, String>> testResultsApp = (HashMap<String, HashMap<String, String>>) resultsArray [1];

        //-----HASHMAP DE COMPONENTES-----//

        //Declaro la lista raw (tiempos sin restar)
        List<String[]> testResultsCSV_raw = new ArrayList<>();

        //Declaro la lista final (tiempos restados)
        List<String[]> testResultsCSV = new ArrayList<>();

        //A�ado la cabecera raw
        testResultsCSV_raw.add(new String[] {"Parent","Agent","Node","t0_DeploymentRequest","t1_SchedulingFinished","t2_ComponentBooting","t3_ComponentRunning","t4_ComponentFinish"});

        //A�ado la cabecera final
        testResultsCSV.add(new String[]{"Parent","Agent","Node","SchedulingTime (t1-t0)","BootTime (t2-t1)","ExecutionTime (t3-t2)","DeploymentTime (t3-t0)","LiveTime (t4-t3)"});

        //Itero todas las posiciones del HasMap de componentes
        String t0="", t1="", t2="", t3="",t4="",t5="",t6="", t7="", t8="", t9="";
        for(Map.Entry<String, HashMap<String, HashMap<String, String>>> parent : ParentResults.entrySet()){
            if(parent.getKey().contains("mplan")){
                t0=parent.getValue().get("planner").get("DeploymentRequestTime");
            }
        }

        for(Map.Entry<String, HashMap<String, HashMap<String, String>>> parent : ParentResults.entrySet()){

            String node =" ";
            String agentname = " ";

            for (Map.Entry<String, HashMap<String, String>> agent : parent.getValue().entrySet()){

                    System.out.println(agent.getKey());


                if(agent.getKey().contains("mplanagent")||agent.getKey().contains("orderagent")||agent.getKey().contains("batchagent")){

                    agentname=agent.getKey();
                    t1 = agent.getValue().get("NegotiationTime");  //justo antes de que se pida inicio en pnode
                    t2 = agent.getValue().get("CreationTime");
                    t3 = agent.getValue().get("ExecutionTime");
                    t4 = agent.getValue().get("FinishTime");
                    node = agent.getValue().get("Node");

                    String taux0=null;
                    //checkea si es un agente que es de recuperaci�n
                    for(Map.Entry<String, HashMap<String, HashMap<String, String>>> parentErr : ParentResultsErr.entrySet()) {
                        for (Map.Entry<String, HashMap<String, String>> agentErr : parentErr.getValue().entrySet()) {
                            if(agentErr.getKey().equals(agent.getKey())){
                                if(agentErr.getValue().get("RedundancyRecovery")!=null){ //en caso de ser un agente recuperado de reduncancia no tiene sentido usar t0
                                    for (Map.Entry<String, HashMap<String, String>> agentErr_t2 : parentErr.getValue().entrySet()) {
                                        if(agentErr_t2.getValue().get("DeadAgentConfirmation")!=null){
                                            taux0=agentErr_t2.getValue().get("DeadAgentConfirmation");
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if(!t0.equals("")){
                        String schedTime ="";
                        if(taux0==null){
                            schedTime = String.valueOf((Double.valueOf(t1)-Double.valueOf(t0))/1000);
                        }else{
                            schedTime = String.valueOf((Double.valueOf(t1)-Double.valueOf(taux0))/1000);
                        }
                        String bootTime = String.valueOf((Double.valueOf(t2)-Double.valueOf(t1))/1000);
                        String execTime = String.valueOf((Double.valueOf(t3)-Double.valueOf(t2))/1000);
                        String deploymentTime="";
                        if(taux0==null){
                            deploymentTime = String.valueOf((Double.valueOf(t3)-Double.valueOf(t0))/1000);
                        }else{
                            deploymentTime = String.valueOf((Double.valueOf(t3)-Double.valueOf(taux0))/1000);
                        }
                        String LiveTime="Lost";
                        if(t4!=null){
                            LiveTime = String.valueOf((Double.valueOf(t4)-Double.valueOf(t3))/1000);
                        }else{
                            t4="Lost";
                        }
                        String [] data_raw =null;
                        //Generamos el array en el que metemos todos los datos sin restas y lo a�adimos donde corresponde
                        if(taux0==null){
                            data_raw = new String[] {parent.getKey(), agentname,node, t0, t1, t2, t3, t4};
                        }else{
                            data_raw = new String[] {parent.getKey(), agentname,node, taux0, t1, t2, t3, t4};
                        }

                        testResultsCSV_raw.add(data_raw);

                        String[] data = new String[] {parent.getKey(), agentname,node,schedTime,bootTime,execTime,deploymentTime, LiveTime};
                        testResultsCSV.add(data);
                    }else{
                        System.out.println("Time intervals lost for "+agent.getKey());
                    }
                }

            }
            //Por �ltimo, se calculan las restas y se pasan a segundos

        }
        //Ruta al fichero raw (sin restas)
//        String componentsRawPath = "/home/" + System.getProperty("user.name") + "/shared/timestamps/components_raw.csv";

        String componentsRawPath = "C:/FlexManSys/timestamps/components_raw.csv";
        //Ruta al fichero final (con restas)
//        String componentsPath = "/home/" + System.getProperty("user.name") + "/shared/timestamps/components.csv";

        String componentsPath = "C:/FlexManSys/timestamps/components.csv";

        //Ahora se escribe el tiempo de despliegue para los tres componentes
        try {
            CSVWriter writecomponents_raw = new CSVWriter(new FileWriter(componentsRawPath));
            writecomponents_raw.writeAll(testResultsCSV_raw);
            writecomponents_raw.close();

            CSVWriter writecomponents = new CSVWriter(new FileWriter(componentsPath));
            writecomponents.writeAll(testResultsCSV);
            writecomponents.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //-----HASHMAP DE APLICACIONES-----//

        //Declaro la lista raw (tiempos sin restar)
        List<String[]> testResultsAppCSV_raw = new ArrayList<>();

        //Declaro la lista final (tiempos restados)
        List<String[]> testResultsAppCSV = new ArrayList<>();

        //A�ado la cabecera raw
        testResultsAppCSV_raw.add(new String[] {"Aplication","t0_DeploymentRequest","t1_SchedulingFinished","t2_ComponentBooting","t3_ComponentRunning","t4_ComponentFinish"});

        //A�ado la cabecera final
        testResultsAppCSV.add(new String[]{"Aplication","SchedulingTime (t1-t0)","BootTime (t2-t1)","ExecutionTime (t3-t2)","DeploymentTime (t3-t0)","ProcessTime (t4-t3)"});

        //Itero todas las posiciones del HasMap de componentes
        t0="";
        t1="";
        t2="";
        t3="";
        t4="";

//        for(Map.Entry<String, HashMap<String, HashMap<String, String>>> parentapp : ParentResultsApp.entrySet()){
//            if(parentapp.getKey().contains("mplan")){
//                t0=parentapp.getValue().get("planner").get("DeploymentRequestTime");
//            }
//        }
        for(Map.Entry<String, HashMap<String, HashMap<String, String>>> parentapp : ParentResultsApp.entrySet()) {
//            HashMap<String, HashMap<String, String>> AgentResultsApp = ParentResultsApp.get(aplicacion1);

            for (Map.Entry<String, HashMap<String, String>> agentapp : parentapp.getValue().entrySet()) {

                //Voy obteniendo la informaci�n que tengo que guardar en el array
                //Solo lo guardo si tengo todos los tiempos de la aplicaci�n

                //Primero, el nombre de la aplicacion
                System.out.println(agentapp.getKey());

                    t0 = agentapp.getValue().get("DeploymentRequestTime");
                    t1 = agentapp.getValue().get("NegotiationTime");  //justo antes de que se pida inicio en pnode
                    t2 = agentapp.getValue().get("CreationTime");
                    t3 = agentapp.getValue().get("ExecutionTime");
                    t4 = agentapp.getValue().get("FinishTime");

                    if (!t0.equals("")) {
                        String schedTime ="";
                        if(t1!=null&&t0!=null){
                            schedTime = String.valueOf((Double.valueOf(t1) - Double.valueOf(t0)) / 1000);
                        }
                        String bootTime ="";
                        if(t2!=null&&t1!=null){
                            bootTime = String.valueOf((Double.valueOf(t2) - Double.valueOf(t1)) / 1000);
                        }
                        String execTime ="";
                        if(t3!=null&&t2!=null){
                            execTime = String.valueOf((Double.valueOf(t3) - Double.valueOf(t2)) / 1000);
                        }
                        String deploymentTime ="";
                        if(t3!=null&&t0!=null){
                            deploymentTime = String.valueOf((Double.valueOf(t3) - Double.valueOf(t0)) / 1000);
                        }
                        String ProcessTime ="";
                        if(t3!=null&&t4!=null){
                            ProcessTime = String.valueOf((Double.valueOf(t4) - Double.valueOf(t3)) / 1000);
                        }

                        //Generamos el array en el que metemos todos los datos sin restas y lo a�adimos donde corresponde
                        String[] data_raw = new String[]{parentapp.getKey(),t0, t1, t2, t3, t4};
                        testResultsAppCSV_raw.add(data_raw);

                        //Generamos el aerray en el que metemos todos los datos con restas y lo a�adimos donde corresponde
                        String[] data = new String[]{parentapp.getKey(),schedTime, bootTime, execTime, deploymentTime, ProcessTime};
                        testResultsAppCSV.add(data);
                    } else {
                        System.out.println("Time intervals of app lost for " + agentapp.getKey());
                    }


//                if(agentapp.getKey().contains("mplanagent")||agentapp.getKey().contains("orderagent")||agentapp.getKey().contains("batchagent")){
//                    agentappname=agentapp.getKey();
//                    t2 = agentapp.getValue().get("CreationTime");
//                    t3 = agentapp.getValue().get("ExecutionTime");
//                }
//                if(agentapp.getKey().contains("pnodeagent")){
//                    t1 = agentapp.getValue().get("NegotiationTime");  //justo antes de que se pida inicio en pnode
//                }
//                if(agentapp.getKey().contains("planner")){
//                    t0 = agentapp.getValue().get("DeploymentRequestTime"); //appstart en planner
//                }
//            }
//            String[] data_raw = new String[]{parentapp.getKey(), agentappname, t0, t1, t2, t3};
//            testResultsAppCSV_raw.add(data_raw);
//
//            //Generamos el aerray en el que metemos todos los datos con restas y lo a�adimos donde corresponde
//            String[] data = new String[]{parentapp.getKey(), agentappname, schedTime, bootTime, execTime, deploymentTime};
//            testResultsAppCSV.add(data);
            }
        }

        //Ruta al fichero raw (sin restas)
//        String applicationsRawPath = "/home/" + System.getProperty("user.name") + "/shared/timestamps/applications_raw.csv";
//        String applicationsRawPath = "C:/Users/" + System.getProperty("user.name") + "/IdeaProjects/FlexManSys/timestamps/applications_raw.csv";
        String applicationsRawPath = "C:/FlexManSys/timestamps/applications_raw.csv";

        //Ruta al fichero final (con restas)
//        String applicationsPath = "/home/" + System.getProperty("user.name") + "/shared/timestamps/applications.csv";
//        String applicationsPath = "C:/Users/" + System.getProperty("user.name") + "/IdeaProjects/FlexManSys/timestamps/applications.csv";
        String applicationsPath = "C:/FlexManSys/timestamps/applications.csv";

        //Ahora se escribe el tiempo de despliegue para los tres componentes
        try {
            CSVWriter writeapplications_raw = new CSVWriter(new FileWriter(applicationsRawPath));
            writeapplications_raw.writeAll(testResultsAppCSV_raw);
            writeapplications_raw.close();

            CSVWriter writeapplications = new CSVWriter(new FileWriter(applicationsPath));
            writeapplications.writeAll(testResultsAppCSV);
            writeapplications.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        //-----HASHMAP DE ERRORES-----//

        //Declaro la lista raw (tiempos sin restar)
        List<String[]> testResultsErrCSV_raw = new ArrayList<>();

        //Declaro la lista final (tiempos restados)
        List<String[]> testResultsErrCSV = new ArrayList<>();

        //A�ado la cabecera raw
        testResultsErrCSV_raw.add(new String[] {"Parent","Agent","t0_AgentKilled","t1_Detection","t2_Confirmation","t3_SystemRecovery","t4_RedundancyRecovery"});

        //A�ado la cabecera final
        testResultsErrCSV.add(new String[]{"Parent","DetectionTime (t1-t0)","ConfirmationTime (t2-t1)","FuntionalityRecoveryTime (t3-t1)","RedundancyRecoveryTime (t4-t1)"});

        //Itero todas las posiciones del HasMap de componentes

        for(Map.Entry<String, HashMap<String, HashMap<String, String>>> parentErr : ParentResultsErr.entrySet()){
            t0="";
            t1="";
            t2="";
            t3="";
            t4="";


            String agentname = " ";

            String DetectionTime ="";
            String ConfirmationTime ="";
            String FuntionalityRecoveryTime ="";
            String SystemRecoveryTime="";

            for (Map.Entry<String, HashMap<String, String>> agentErr : parentErr.getValue().entrySet()){

                String ta0="";
                String ta1="";
                String ta2="";
                String ta3="";
                String ta4="";
                System.out.println(agentErr.getKey());
                if(agentErr.getKey().contains("mplanagent")||agentErr.getKey().contains("orderagent")||agentErr.getKey().contains("batchagent")){
                    agentname=agentErr.getKey();
                    if(agentErr.getValue().get("AgentKilled")!=null){
                        t0 = agentErr.getValue().get("AgentKilled");
                        ta0=t0;
                    }
                    if(agentErr.getValue().get("DeadAgentDetection")!=null){
                        t1 = agentErr.getValue().get("DeadAgentDetection");  //QoS recibe la denuncia
                        ta1=t1;
                    }
                    if(agentErr.getValue().get("DeadAgentConfirmation")!=null){
                        t2 = agentErr.getValue().get("DeadAgentConfirmation"); //D&D recibe confirmacion del QoS
                        ta2=t2;
                    }
                    if(agentErr.getValue().get("RunningAgentRecovery")!=null){
                        t3 = agentErr.getValue().get("RunningAgentRecovery");  //El sistema ya puede funcionar (si procede)
                        ta3=t3;
                    }
                    if(agentErr.getValue().get("RedundancyRecovery")!=null){
                        t4 = agentErr.getValue().get("RedundancyRecovery"); //El sistema se encuentra en el mismo estado que originalmente
                        ta4=t4;
                    }


                    String [] data_raw = new String[] {parentErr.getKey(), agentname, ta0, ta1, ta2, ta3, ta4}; //Los datos raw se escriben por agente
                    testResultsErrCSV_raw.add(data_raw);
                }
            }

            if(!t1.equals("")&&!t0.equals("")){
                DetectionTime = String.valueOf((Double.valueOf(t1)-Double.valueOf(t0))/1000);
            }

            if(!t2.equals("")&&!t1.equals("")){
                ConfirmationTime = String.valueOf((Double.valueOf(t2)-Double.valueOf(t1))/1000);
            }

            if(!t3.equals("")&&!t1.equals("")){
                FuntionalityRecoveryTime = String.valueOf((Double.valueOf(t3)-Double.valueOf(t1))/1000);
            }

            if(!t4.equals("")&&!t1.equals("")){
                SystemRecoveryTime = String.valueOf((Double.valueOf(t4)-Double.valueOf(t1))/1000);
            }


            //Generamos el array en el que metemos todos los datos sin restas y lo a�adimos donde corresponde


            //Generamos el aerray en el que metemos todos los datos con restas y lo a�adimos donde corresponde
            String[] data = new String[] {parentErr.getKey(), DetectionTime,ConfirmationTime,FuntionalityRecoveryTime,SystemRecoveryTime};
            testResultsErrCSV.add(data);

            //Por �ltimo, se calculan las restas y se pasan a segundos

        }
        //Ruta al fichero raw (sin restas)
        String errorsRawPath = "C:/FlexManSys/timestamps/errors_raw.csv";
        //Ruta al fichero final (con restas)
        String errorsPath = "C:/FlexManSys/timestamps/errors.csv";

        //Ahora se escribe el tiempo de despliegue para los tres componentes
        try {
            CSVWriter writeerrors_raw = new CSVWriter(new FileWriter(errorsRawPath));
            writeerrors_raw.writeAll(testResultsErrCSV_raw);
            writeerrors_raw.close();

            CSVWriter writeerrors = new CSVWriter(new FileWriter(errorsPath));
            writeerrors.writeAll(testResultsErrCSV);
            writeerrors.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        //-----HASHMAP DE TIEMPOS DE C�LCULO-----//

        //Declaro la lista raw (tiempos sin restar)
        List<String[]> testResultsNegCSV_raw = new ArrayList<>();

        //Declaro la lista final (tiempos restados)
        List<String[]> testResultsNegCSV = new ArrayList<>();

        //A�ado la cabecera raw
//        testResultsNegCSV_raw.add(new String[] {"Parent","Agent","t0_MemoryCalcStart","t1_MemoryCalcFinish","t2_CPUCalcStart","t3_CPUCalcFinish","t4_StartSendState","t5_GetStateDone","t6_MsgSentDone","t7_AcknowledgeGenerated"});
        testResultsNegCSV_raw.add(new String[] {"Number","Agent","t0_MachineStart","t1_GWAnswer","t2_MachineRunning"});

        //A�ado la cabecera final
//        testResultsNegCSV.add(new String[]{"Parent","Agent","MemoryCalcTime (t1-t0)","CPUCalcTime (t3-t2)","GetStateInterval (t5-t4)","MsgSendInterval (t6-t5)","AckGererationInterva (t7-t6)"});
        testResultsNegCSV.add(new String[]{"Number","Agent","PLCCheck (t1-t0)","TransitionToRunning (t2-t1)","Total (t2-t0)"});

        //Itero todas las posiciones del HasMap de componentes

        for(Map.Entry<String, HashMap<String, HashMap<String, String>>> parentNeg : ParentResultsNeg.entrySet()){
            String agentname = " ";
            for (Map.Entry<String, HashMap<String, String>> agentNeg : parentNeg.getValue().entrySet()){
                t0="";
                t1="";
                t2="";
                t3="";
                t4="";
                t5="";
                t6="";
                t7="";
                String PLCCheck="";
                String TransitionToRunning="";
                String Total="";

                String MemoryCalcTime ="";
                String CPUCalcTime ="";
                String GetStateInterval ="";
                String MsgSendInterval ="";
                String AckGererationInterval ="";
                System.out.println(agentNeg.getKey());

                    agentname=agentNeg.getKey();
//                    if(agentNeg.getValue().get("MemoryCalcStart")!=null){
//                        t0 = agentNeg.getValue().get("MemoryCalcStart");
//                    }
                    if(agentNeg.getValue().get("MachineStart")!=null){
                        t0 = agentNeg.getValue().get("MachineStart");
                    }
                    if(agentNeg.getValue().get("GWAnswer")!=null){
                        t1 = agentNeg.getValue().get("GWAnswer");  //QoS recibe la denuncia
                    }
                    if(agentNeg.getValue().get("MachineRunning")!=null){
                        t2 = agentNeg.getValue().get("MachineRunning"); //D&D recibe confirmacion del QoS
                    }
//                    if(agentNeg.getValue().get("CPUCalcFinish")!=null){
//                        t3 = agentNeg.getValue().get("CPUCalcFinish");  //El sistema ya puede funcionar (si procede)
//                    }
//                    if(agentNeg.getValue().get("StartSendState")!=null){
//                        t4 = agentNeg.getValue().get("StartSendState");  //El sistema ya puede funcionar (si procede)
//                    }
//                    if(agentNeg.getValue().get("GetStateDone")!=null){
//                        t5 = agentNeg.getValue().get("GetStateDone");  //El sistema ya puede funcionar (si procede)
//                    }
//                    if(agentNeg.getValue().get("MsgSentDone")!=null){
//                        t6 = agentNeg.getValue().get("MsgSentDone");  //El sistema ya puede funcionar (si procede)
//                    }
//                    if(agentNeg.getValue().get("AcknowledgeGenerated")!=null){
//                        t7 = agentNeg.getValue().get("AcknowledgeGenerated");  //El sistema ya puede funcionar (si procede)
//                    }

//                    String [] data_raw = new String[] {parentNeg.getKey(), agentname, t0, t1, t2, t3,t4,t5,t6,t7}; //Los datos raw se escriben por agente
//                    testResultsNegCSV_raw.add(data_raw);
                    String [] data_raw = new String[] {parentNeg.getKey(),agentname, t0, t1, t2}; //Los datos raw se escriben por agente
                    testResultsNegCSV_raw.add(data_raw);

//                    if(!t1.equals("")&&!t0.equals("")){
//                        MemoryCalcTime = String.valueOf((Double.valueOf(t1)-Double.valueOf(t0))/1000);
//                    }
                    if(!t1.equals("")&&!t0.equals("")){
                        PLCCheck = String.valueOf((Double.valueOf(t1)-Double.valueOf(t0))/1000);
                    }
                    if(!t2.equals("")&&!t1.equals("")){
                        TransitionToRunning = String.valueOf((Double.valueOf(t2)-Double.valueOf(t1))/1000);
                    }
                    if(!t2.equals("")&&!t0.equals("")){
                        Total = String.valueOf((Double.valueOf(t2)-Double.valueOf(t0))/1000);
                    }
//                    if(!t3.equals("")&&!t2.equals("")){
//                        CPUCalcTime = String.valueOf((Double.valueOf(t3)-Double.valueOf(t2))/1000);
//                    }
//                    if(!t5.equals("")&&!t4.equals("")){
//                        GetStateInterval = String.valueOf((Double.valueOf(t5)-Double.valueOf(t4))/1000);
//                    }
//                    if(!t6.equals("")&&!t5.equals("")){
//                        MsgSendInterval = String.valueOf((Double.valueOf(t6)-Double.valueOf(t5))/1000);
//                    }
//                    if(!t7.equals("")&&!t6.equals("")){
//                        AckGererationInterval  = String.valueOf((Double.valueOf(t7)-Double.valueOf(t6))/1000);
//                    }


//                    String[] data = new String[] {parentNeg.getKey(), agentname,MemoryCalcTime,CPUCalcTime,GetStateInterval, MsgSendInterval, AckGererationInterval};
//                    testResultsNegCSV.add(data);
                    String[] data = new String[] {parentNeg.getKey(),agentname,PLCCheck,TransitionToRunning, Total};
                    testResultsNegCSV.add(data);

//                }
            }
        }
        //Ruta al fichero raw (sin restas)
        String NegCalcRawPath = "C:/FlexManSys/timestamps/NegCalc_raw.csv";
        //Ruta al fichero final (con restas)
        String NegCalcPath = "C:/FlexManSys/timestamps/NegCalc.csv";

        //Ahora se escribe el tiempo de despliegue para los tres componentes
        try {
            CSVWriter writeNegCalc_raw = new CSVWriter(new FileWriter(NegCalcRawPath));
            writeNegCalc_raw.writeAll(testResultsNegCSV_raw);
            writeNegCalc_raw.close();

            CSVWriter writeNegCalc = new CSVWriter(new FileWriter(NegCalcPath));
            writeNegCalc.writeAll(testResultsNegCSV);
            writeNegCalc.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object[] printData (){

        HashMap<String, HashMap<String, HashMap<String, String>>> testResultsHM = new HashMap<>();
        HashMap<String, HashMap<String, HashMap<String, String>>> testResultsAppHM = new HashMap<>();
        HashMap<String, HashMap<String, HashMap<String, String>>> testResultsErrHM = new HashMap<>();
        HashMap<String, HashMap<String, HashMap<String, String>>> testResultsNegHM = new HashMap<>();
        Properties pp = new Properties();

//        pp.setProperty(Profile.MAIN_HOST, "192.168.249.1");
        pp.setProperty(Profile.MAIN_HOST, "192.168.1.100");

//        pp.setProperty(Profile.LOCAL_HOST, "192.168.249.1");
        pp.setProperty(Profile.LOCAL_HOST, "192.168.1.100");

        pp.setProperty(Profile.MAIN_PORT, "1099");
        pp.setProperty(Profile.LOCAL_PORT, "1099");

        String containerName = "Container-GWDataAcq";   // se define el nombre del contenedor donde se inicializara el agente
        pp.setProperty(Profile.CONTAINER_NAME, containerName);

        JadeGateway.init("es.ehu.platform.test.DataAcq_GWAgent",pp);    //Gateway Agent Initialization, must define package directory
        StructMessage strMessage = new StructMessage();
        strMessage.setAction("print");
        try {
            JadeGateway.execute(strMessage);    // calls processCommand method of Gateway Agent
        } catch(Exception e) {
            e.printStackTrace();
        }

        if (strMessage.readNewData()) {
            testResultsHM = strMessage.readTestResults();
            testResultsAppHM = strMessage.readTestResultsApp();
            testResultsErrHM = strMessage.readTestResultsErr();
            testResultsNegHM = strMessage.readTestResultsNeg();

        } else {
            System.out.println("--No answer");
            testResultsHM = null;
            testResultsAppHM = null;
            testResultsErrHM = null;
            testResultsNegHM = null;
        }

        Object[] result = new Object[4];
        result[0] = testResultsHM;
        result[1] = testResultsAppHM;
        result[2] = testResultsErrHM;
        result[3] = testResultsNegHM;
        return result;
    }

    public static void agentInit (){

        Properties pp = new Properties();
//        pp.setProperty(Profile.MAIN_HOST, "192.168.249.1");
        pp.setProperty(Profile.MAIN_HOST, "192.168.1.100");

//        pp.setProperty(Profile.LOCAL_HOST, "192.168.249.1");
        pp.setProperty(Profile.LOCAL_HOST, "192.168.1.100");

        pp.setProperty(Profile.MAIN_PORT, "1099");
        pp.setProperty(Profile.LOCAL_PORT, "1099");

        String containerName = "Container-GWDataAcq";   // se define el nombre del contenedor donde se inicializara el agente
        pp.setProperty(Profile.CONTAINER_NAME, containerName);

        JadeGateway.init("es.ehu.platform.test.DataAcq_GWAgent",pp);    //Gateway Agent Initialization, must define package directory
//        JadeGateway.init("es.ehu.domain.manufacturing.agents.cognitive.GWAgentROS",pp);

        StructMessage strMessage = new StructMessage();
        strMessage.setAction("init");
        try {
            JadeGateway.execute(strMessage);    // calls processCommand method of Gateway Agent
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
