package es.ehu;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Scanner;


/*
cd C:\Users\bcsgaguu\workspace\sede.ws\JadeMW\JadeMiddleware
java -cp bin;lib\jade.jar jade.Boot -container cc:es.ehu.Registerer
*/

public class Registerer extends Agent {

  
  private static final long serialVersionUID = 1L;
  
  static final Logger LOGGER = LogManager.getLogger(Registerer.class.getName()) ;
  private String mwm = null;
  
  public static void main(String... args){
    
  }
  
  
  
  protected void setup() {
    LOGGER.entry();
    LOGGER.warn("warning output sample");
    try {
      Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
         LOGGER.debug("A�adida tarea de apagado...");
       } catch (Throwable t) {
         LOGGER.debug(" *** Error: No se ha podido a�adir tarea de apagado");
    }    
    addBehaviour(new mmcBehaviour(this));
    LOGGER.exit();
  }
  
  class mmcBehaviour extends SimpleBehaviour
    {   
    private static final long serialVersionUID = 6711046229173067015L;
    
        public mmcBehaviour(Agent a) {
            super(a);
        }
        
        public void action() {
          
          LOGGER.entry();
          try {

          DFAgentDescription dfd = new DFAgentDescription();
          ServiceDescription sd = new ServiceDescription();
        
          sd.setType("sa");
          dfd.addServices(sd);
            
          while (true) {
                DFAgentDescription[] result = DFService.search(myAgent,dfd);
                
                if ((result != null) && (result.length > 0)) {
                  dfd = result[0]; 
                  mwm = dfd.getName().getLocalName();
                  break;
                }
                System.out.print(".");
                Thread.sleep(100);
              
            } //end while (true)
          
          //String cmd = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?> <!--This is a comment--> <root> <item myattr=\"attrvalue\">text</item> </root>";
          //String cmd = "loadnum";//
          String cmd = "";
          //String cmd = "help";
          String help = "Local commands:\n"
              + "loadnum > loads number application\n"
              + "loadhc > loads eHealth application\n"
              + "loadplc > loads PLC application\n"
              + "help > middleware commands summary\n"
              + "exit";
          LOGGER.info(help);
          
          while (!cmd.equals("exit")) {
            

            if (cmd.startsWith("load")) loadPredefined(cmd);
            //loadPredefined("loadnum");
            //loadPredefined("loadeh");

            Scanner in = new Scanner(System.in);
                System.out.print("cmd: ");
                cmd = in.nextLine();
                cmd = cmd.trim();
                //vaciar cola de mensajes
                
                ACLMessage flush = receive();
                while (flush!=null) {                    
                  System.out.println(flush.getInReplyTo()+" : "+ flush.getContent());
                  flush = receive();
                } 
                
                
                if (cmd.length()>0) {
                String [] cmds = cmd.split(";");
                //if (cmd.length()>0)
                for (int i=0; i<cmds.length;i++){
                  ACLMessage reply = sendCommand(mwm, cmds[i]);
                  if (reply!=null) {
                    System.out.print(reply.getInReplyTo()+": "+reply.getContent());
                    if (cmds.length>1) System.out.print(" < "+cmds[i]);
                    System.out.println();
                  }                  
                }
                }
          }
        System.exit(0); //agur
        
          } catch (Exception e) {e.printStackTrace();}
        LOGGER.exit();
        }

        public void loadPredefined(String cmd) {
          
          if (cmd.equals("loadnum")) { // n componentes
                                            
            
            /** SPUTTTTTTNIK ***/

            
            //Adaptability: sobre las 5 raspberries lanzar evento de reconfiguraci�n que lanza aplicaci�n con 2, 4, 8, 16, 32 componentes. Medir tiempo hasta arranque de la aplicaci�n. 

            
            String systID = sendCommand(mwm,"reg system ID=system101 name=Sistema desc=General system definition qos=resourceOptimized").getContent();
            String scenID = sendCommand(mwm,"reg scenary name=Escenario parent="+systID+" desc=Escenario principal").getContent();
            
            
            for (int j=1; j<=1;j++) {
              
              int componentes = (int)Math.pow(2, j);
              //Aplicaci�n
              sendCommand(mwm,"reg application ID=applic"+(100+componentes)+" name=Application"+(100+componentes)+" parent="+scenID+" desc=Aplicaci�n de ejemplo 1 activation=periodic period=1000 deadline=1000");
              //Generador
              sendCommand(mwm,"reg component ID=compon"+j+""+(100+1)+" targetComponentIDs=compon"+j+"102 name=Generador parent=applic"+(100+componentes)+" desc=Generador pares activation=periodic period=2000 nodeRestriction=node101 negotiationCriteria=max freeMem redundancy=0 isFirst=true");
              sendCommand(mwm,"reg cmpImplementation name=Generador parent=compon"+j+""+(100+1)+" desc=Generador class=es.ehu.numeros.Generador platform=java7_32 version=1.0");
              //n procesadores
              for (int i=2;i<=componentes; i++) { 
                sendCommand(mwm,"reg component ID=compon"+j+""+(100+i)+" sourceComponentIDs=compon"+j+""+(99+i)+" targetComponentIDs=compon"+j+""+(101+i)+" name=Procesador1 parent=applic"+(100+componentes)+" desc=Sumador activation=sporadic nodeRestriction=node102 negotiationCriteria=max freeMem redundancy=1");
                sendCommand(mwm,"reg cmpImplementation name=Procesador parent=compon"+j+""+(100+i)+" desc=Procesador class=es.ehu.numeros.Procesador platform=java7_32 version=1.0");
              }
              //consumidor
              sendCommand(mwm,"reg component ID=compon"+j+""+(100+componentes+1)+" sourceComponentIDs=compon"+j+""+(100+componentes)+" name=Consumidor parent=applic"+(100+componentes)+" desc=Monitorizador activation=sporadic nodeRestriction=node103 negotiationCriteria=max freeMem redundancy=0 isLast=true");
              sendCommand(mwm,"reg cmpImplementation name=Consumidor parent=compon"+j+""+(100+componentes+1)+" desc=Consumidor class=es.ehu.numeros.Consumidor platform=java7_32 version=1.0");
            } // end for componentes
            
            

            String event1ID = sendCommand(mwm,"reg event name=Evento1 parent="+scenID+" desc=Evento de scenario1").getContent();
            String action1ID = sendCommand(mwm,"reg action name=Accion1 parent="+event1ID+" desc=Arrancar aplicaci�n action=start compon101").getContent();
            String action2ID = sendCommand(mwm,"reg action name=Accion2 parent="+event1ID+" desc=Parar aplicaci�n action=start compon102 order=2").getContent();
            String even1Scn1Act1ID = sendCommand(mwm,"reg action name=Accion3 parent="+event1ID+" desc=Para aplicaci�n action=start compon103 order=3").getContent();
            String evenPrpgID = sendCommand(mwm,"reg event name=EventoPropagado parent=systemNursingHome desc=Incendio").getContent();
            String eventSPID = sendCommand(mwm,"reg event ID=eventSetGenPeriod name=Evento1 parent="+scenID+" desc=Evento de scenario1").getContent();
            String actionSPID = sendCommand(mwm,"reg action name=actSetEvent parent=eventSetGenPeriod desc=Cambia temporizaci�n action=\"localcmd (getins compon101) cmd=set period 1000\"").getContent();

          } else if (cmd.equals("loadhc")) {


            /****************** Health Care ************************/


            sendCommand(mwm,"reg system ID=systemNursingHome name=NursingHome desc=NursingHome qos=resourceOptimized");
            sendCommand(mwm,"reg event ID=eventPropagatorNH name=eventPropagatorNH parent=systemNursingHome desc=Evento de sistema");
            sendCommand(mwm,"reg action name=StopapplicPRC parent=eventPropagatorNH desc=lanza evento paciente 1 action=start eventStopapplicPRC"); 
            sendCommand(mwm,"reg action name=StartapplicBPM parent=eventPropagatorNH desc=lanza evento paciente 2 action=start eventStartApplicBPM");
            sendCommand(mwm,"reg action name=StartapplicSputnik parent=eventPropagatorNH desc=lanza sputnik action=start eventStartSputnik");


            sendCommand(mwm,"reg scenary ID=scenaryPatient1 name=NursingHome parent=systemNursingHome desc=Escenario principal");
            sendCommand(mwm,"reg event ID=eventStopapplicPRC name=relaxed parent=scenaryPatient1 desc=Evento de scenario1");

            sendCommand(mwm,"reg action ID=actionStopRPC name=StopapplicPRC parent=eventStopapplicPRC desc=destruye la aplicaci�n PRC action=\"localcmd applicPatientRelaxedChecking cmd=setstate stop\"");
            //          sendCommand(mwm,"reg event ID=eventRelaxed name=relaxed parent=scenaryPatient1 desc=Evento de scenario1");
            //          sendCommand(mwm,"reg action ID=actionSetPeriod name=setPeriod parent=eventRelaxed desc=cambia el periodo de lectura action=\"localcmd componHRAcquisition cmd=set period 3000\"");

            sendCommand(mwm,"reg application ID=applicPatientRelaxedChecking name=Patient Relaxed Checking parent=scenaryPatient1 activation=periodic period=216000000");
            sendCommand(mwm,"reg component ID=componHRAcquisition name=HR_Acquisition parent=applicPatientRelaxedChecking desc=HR_Acquisition activation=periodic period=10000 CPUreq=low StorageReq=low NetworkReg=low node=node102");
            sendCommand(mwm,"reg cmpImplementation name=HR_Acquisition parent=componHRAcquisition desc=HR_Acquisition class=es.ehu.eHealthSensors.generatedAgents.scnNursingHome.appPatientRelaxedChecking.compHR_Acquisition.HR_Acquisition platform=java7_32 version=1.0");
            sendCommand(mwm,"reg component ID=componHRStorage name=HR_Storage parent=applicPatientRelaxedChecking desc=HR_Storage CPUreq=low StorageReq=low NetworkReg=low node=node101");
            sendCommand(mwm,"reg cmpImplementation name=HR_Storage parent=componHRStorage desc=HR_Storage class=es.ehu.eHealthSensors.generatedAgents.scnNursingHome.appPatientRelaxedChecking.compHR_Storage.HR_Storage platform=java7_32 version=1.0");
            sendCommand(mwm,"reg component ID=componHRCheckInterval name=HR_CheckInterval parent=applicPatientRelaxedChecking desc=HR_CheckInterval CPUreq=low StorageReq=low NetworkReg=low node=node103");
            sendCommand(mwm,"reg cmpImplementation name=HR_CheckInterval parent=componHRCheckInterval desc=HR_CheckInterval class=es.ehu.eHealthSensors.generatedAgents.scnNursingHome.appPatientRelaxedChecking.compHR_CheckInterval.HR_CheckInterval platform=java7_32 version=1.0");
            sendCommand(mwm,"reg component ID=componHRCheckRelaxed name=HR_CheckRelaxed parent=applicPatientRelaxedChecking desc=HR_CheckRelaxed CPUreq=low StorageReq=low NetworkReg=low node=node104,node106 redundancy=1");
            sendCommand(mwm,"reg cmpImplementation name=HR_CheckRelaxed parent=componHRCheckRelaxed desc=HR_CheckRelaxed class=es.ehu.eHealthSensors.generatedAgents.scnNursingHome.appPatientRelaxedChecking.compHR_CheckRelaxed.HR_CheckRelaxed platform=java7_32 version=1.0");

            //Evento lanzado por el componente HR_CheckRelaxed.
            //Tipo de acci�n asociada al evento = ModifyQoS
            //Se cambia el periodo de la aplicaci�n, que en realidad es el periodo del componente HR_Acquisition que es el �nico peri�dico.

            sendCommand(mwm,"reg scenary ID=scenaryPatient2 name=NursingHomePatien2 parent=systemNursingHome desc=Escenario paciente 2");

            sendCommand(mwm,"reg event ID=eventStartApplicBPM name=relaxed parent=scenaryPatient2 desc=Evento de scenario2");
            sendCommand(mwm,"reg action ID=actionStartBPM name=StartApplicBPM parent=eventStartApplicBPM desc=inicia la aplicaci�n BPM action=\"start applicPatientBPM\"");

            sendCommand(mwm,"reg application ID=applicPatientBPM name=Patient 2 check blood pressure parent=scenaryPatient2 activation=periodic period=216000000");

            sendCommand(mwm,"reg component ID=componBPAcquisition name=BP_Acquisition parent=applicPatientBPM desc=BP_Acquisition activation=periodic period=3000 CPUreq=low StorageReq=low NetworkReg=low node=node102");
            sendCommand(mwm,"reg cmpImplementation name=BP_Acquisition parent=componBPAcquisition desc=BP_Acquisition " +
                "class=es.ehu.eHealthSensors.generatedAgents.scnNursingHome.appBloodPressureMonitoring.compBP_Acquisition.BP_Acquisition platform=java7_32 version=1.0");

            sendCommand(mwm,"reg component ID=componBPStorage name=BP_Storage parent=applicPatientBPM desc=BP_Storage CPUreq=low StorageReq=low NetworkReg=low node=node101");
            sendCommand(mwm,"reg cmpImplementation name=BP_Storage parent=componBPStorage desc=BP_Storage " +
                "class=es.ehu.eHealthSensors.generatedAgents.scnNursingHome.appBloodPressureMonitoring.compBP_Storage.BP_Storage platform=java7_32 version=1.0");


            String scenID = sendCommand(mwm,"reg scenary ID=scenarySputnik name=sputnik parent=systemNursingHome desc=sputnik").getContent();

            String app1ID = sendCommand(mwm,"reg application name=sputnik1 parent="+scenID+" desc=Aplicaci�n de ejemplo 1 activation=periodic period=1000").getContent();

            sendCommand(mwm,"reg event ID=eventStartSputnik name=StartSputnik parent=scenarySputnik desc=lanzar sputnik");
            sendCommand(mwm,"reg action ID=actionStartSputnik name=StartApplicsputnik parent=eventStartSputnik desc=inicia sputnik action=\"start "+app1ID+"\"");

            String com1ID = sendCommand(mwm,"reg component name=sensorNeutrinos parent="+app1ID+" desc=sensorNeutrinos activation=periodic period=2000 CPUreq=low StorageReq=low NetworkReg=low node=node104 redundancy=0").getContent();
            String cmp1implID = sendCommand(mwm,"reg cmpImplementation name=SensorNeutrinos parent="+com1ID+" desc=SensorNeutrinos class=es.ehu.numeros.Generador platform=java7_32 version=1.0").getContent();

            String com2ID = sendCommand(mwm,"reg component name=Procesador parent="+app1ID+" desc=Procesador activation=sporadic CPUreq=low StorageReq=low NetworkReg=low node=node102,node106 defaultNode=node102,node106 redundancy=1 negotiation=max freeMem").getContent();
            String cmp2implID = sendCommand(mwm,"reg cmpImplementation name=Procesador parent="+com2ID+" desc=Procesador class=es.ehu.numeros.Procesador platform=java7_32 version=1.0").getContent();

            String com3ID = sendCommand(mwm,"reg component name=Consumidor parent="+app1ID+" desc=Monitorizador activation=sporadic CPUreq=low StorageReq=low NetworkReg=low node=node103,node108 redundancy=0").getContent();
            String cmp3implID = sendCommand(mwm,"reg cmpImplementation name=Consumidor parent="+com3ID+" desc=Consumidor class=es.ehu.numeros.Consumidor platform=java7_32 version=1.0").getContent();
            //            cmd = "reg event ID=eventPropagated name=relaxed parent=systemNursingHome desc=Evento de scenario1";
            //            cmd = "reg action ID=actionxx name=setPeriod parent=eventPropagated desc=cambia el periodo de lectura action=start eventxx";
            //            cmd = "reg action ID=actionxx name=setPeriod parent=eventPropagated desc=cambia el periodo de lectura action=start eventxx";
            //            cmd = "reg action ID=actionxx name=setPeriod parent=eventPropagated desc=cambia el periodo de lectura action=start eventxx";



            //        cmd = "reg cmpImplementation name=Procesador parent="+com2ID+" desc=Procesador"
            //            + " class=es.ehu.numeros.Procesador platform=java7_32 version=1.0";
            //        String cmp2implID = sendCommand(mwm, cmd).getContent();
            //        LOGGER.info(cmd+" : "+cmp2implID);
            //        
            //        cmd = "reg cmpImplementation name=Consumidor parent="+com3ID+" desc=Consumidor"
            //            + " class=es.ehu.numeros.Consumidor platform=java7_32 version=1.0";
            //        String cmp3implID = sendCommand(mwm, cmd).getContent();
            //        LOGGER.info(cmd+" : "+cmp3implID);
            //        
            //        cmd = "reg event name=Evento1 parent="+scenID+" desc=Evento de scenario1";
            //        String event1ID = sendCommand(mwm, cmd).getContent();
            //        LOGGER.info(cmd+" : "+event1ID);
            //        
            //        cmd = "reg action name=Accion1 parent="+event1ID+" desc=Arrancar aplicaci�n "
            //            + "action=start " + com1ID ;
            //        String action1ID = sendCommand(mwm, cmd).getContent();
            //        LOGGER.info(cmd+" : "+action1ID);
            //        
            //        cmd = "reg action name=Accion2 parent="+event1ID+" desc=Parar aplicaci�n "
            //            + "action=start " + com2ID + " order=2";
            //        String action2ID = sendCommand(mwm, cmd).getContent();
            //        LOGGER.info(cmd+" : "+action2ID);
            //        
            //        cmd = "reg action name=Accion3 parent="+event1ID+" desc=Para aplicaci�n "
            //            + "action=start " + com3ID + " order=3";
            //        String even1Scn1Act1ID = sendCommand(mwm, cmd).getContent();
            //        LOGGER.info(cmd+" : "+even1Scn1Act1ID);
            //        
            //        
            //        cmd = "reg event name=EventoPropagado parent="+systID+" desc=Incendio";
            //        String evenPrpgID = sendCommand(mwm, cmd).getContent();
            //        LOGGER.info(cmd+" : "+evenPrpgID);



          } else if (cmd.equals("loadplc")) {

            /****************** PLC ************************/

            String systID = sendCommand(mwm, "reg system name=Sistema desc=PLC Function qos=resourceOptimized ID=systemPLCSup").getContent();
            String scenID = sendCommand(mwm, "reg scenary name=Escenario parent="+systID+" desc=Escenario principal").getContent();

            String app1ID = sendCommand(mwm, "reg application name=Application1 parent="+scenID+" desc=Aplicacion PLC activation=periodic").getContent();

            String com1ID = sendCommand(mwm, "reg component name=Estacio1 parent="+app1ID+" desc=Estacion1 activation=periodic period=10 CPUreq=low StorageReq=low NetworkReg=low nodeRestriction=node121,node122,node123 defaultNode=node121 systemLoad=5 negotiation=min systemLoad").getContent();
            sendCommand(mwm, "reg cmpImplementation name=Estacio1 parent="+com1ID+" desc=Estacio1 class=es.ehu.PLC.agent.Estacion1 platform=java7_32 version=1.0").getContent();
          
            String com2ID = sendCommand(mwm, "reg component name=Estacio2 parent="+app1ID+" desc=Estacion2 activation=periodic period=10 CPUreq=low StorageReq=low NetworkReg=low nodeRestriction=node122 systemLoad=7 negotiation=min systemLoad").getContent();
            sendCommand(mwm, "reg cmpImplementation name=Estacio2 parent="+com2ID+" desc=Estacio2 class=es.ehu.PLC.agent.Estacion2 platform=java7_32 version=1.0").getContent();

            String com3ID = sendCommand(mwm, "reg component name=Estacio3 parent="+app1ID+" desc=Estacion3 activation=periodic period=10 CPUreq=low StorageReq=low NetworkReg=low nodeRestriction=node121,node122,node123 defaultNode=node122 systemLoad=7 negotiation=min systemLoad").getContent();
            sendCommand(mwm, "reg cmpImplementation name=Estacio3 parent="+com3ID+" desc=Estacio3 class=es.ehu.PLC.agent.Estacion3 platform=java7_32 version=1.0").getContent();

          } else if (cmd.equals("loadeh")) {
            /**
             * Registro eHealth
             */

            cmd = "reg system name=Sistema desc=General system definition qos=resourceOptimized";
            String systID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd+" : "+systID);

            cmd = "reg scenary name=Escenario parent="+systID+" desc=Escenario principal";
            String scenID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd+" : "+scenID);


            cmd = "reg scenary name=eHealth parent=" + systID + " desc=eHealth scenario";
            scenID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd + " : " + scenID);

            cmd = "reg application name=Pulso parent=" + scenID + " desc=Aplicacion de pulso"
                + " activation=periodic period=1000 deadline=1000";
            String appID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd + " : " + appID);

            // HR_Acquisition
            cmd = "reg component name=HR_Acquisition parent=" + appID + " desc=HR_Acquisition"
                + " activation=periodic period=1500 redundancy=0 CPUreq=low StorageReq=low"
                + " NetworkReg=low nodeRestriction=node101";
            String comID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd + " : " + comID);

            cmd = "reg cmpImplementation name=HR_Acquisition parent=" + comID + " desc=HR_Acquisition"
                + " class=es.ehu.eHealth.HR_Acquisition platform=java7_32 version=1.0";
            String cmpimplID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd + " : " + cmpimplID);

            // HR_RepStorage
            cmd = "reg component name=HR_RepStorage parent=" + appID + " desc=HR_RepStorage"
                + " activation=sporadic redundancy=0 CPUreq=low StorageReq=low NetworkReg=low"
                + " nodeRestriction=node102";
            comID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd + " : " + comID);

            cmd = "reg cmpImplementation name=HR_RepStorage parent=" + comID + " desc=HR_RepStorage"
                + " class=es.ehu.eHealth.HR_RepStorage platform=java7_32 version=1.0";
            cmpimplID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd + " : " + cmpimplID);

            // HR_CheckInterval
            cmd = "reg component name=HR_CheckInterval parent=" + appID + " desc=HR_CheckInterval"
                + " activation=sporadic redundancy=0 CPUreq=low StorageReq=low NetworkReg=low"
                + " nodeRestriction=node102";
            comID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd + " : " + comID);

            cmd = "reg cmpImplementation name=HR_CheckInterval parent=" + comID + " desc=HR_RepStorage"
                + " class=es.ehu.eHealth.HR_CheckInterval platform=java7_32 version=1.0";
            cmpimplID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd + " : " + cmpimplID);

            // HR_CheckRelaxed
            cmd = "reg component name=HR_CheckRelaxed parent=" + appID + " desc=HR_CheckRelaxed"
                + " activation=sporadic redundancy=0 CPUreq=low StorageReq=low NetworkReg=low"
                + " nodeRestriction=node102";
            comID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd + " : " + comID);

            cmd = "reg cmpImplementation name=HR_CheckRelaxed parent=" + comID + " desc=HR_RepStorage"
                + " class=es.ehu.eHealth.HR_CheckRelaxed platform=java7_32 version=1.0";
            cmpimplID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd + " : " + cmpimplID);

            // HR_Unsuitable
            cmd = "reg component name=HR_Unsuitable parent=" + appID + " desc=HR_Unsuitable"
                + " activation=sporadic redundancy=0 CPUreq=low StorageReq=low NetworkReg=low"
                + " nodeRestriction=node102";
            comID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd + " : " + comID);

            cmd = "reg cmpImplementation name=HR_Unsuitable parent=" + comID + " desc=HR_RepStorage"
                + " class=es.ehu.eHealth.HR_Unsuitable platform=java7_32 version=1.0";
            cmpimplID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd + " : " + cmpimplID);

            // HR_NoRelaxedWarning
            cmd = "reg component name=HR_NoRelaxedWarning parent=" + appID + " desc=HR_NoRelaxedWarning"
                + " activation=sporadic redundancy=0 CPUreq=low StorageReq=low NetworkReg=low"
                + " nodeRestriction=node102";
            comID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd + " : " + comID);

            cmd = "reg cmpImplementation name=HR_NoRelaxedWarning parent=" + comID + " desc=HR_RepStorage"
                + " class=es.ehu.eHealth.HR_NoRelaxedWarning platform=java7_32 version=1.0";
            cmpimplID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd + " : " + cmpimplID);


            cmd="";
          } else if (cmd.equals("loadn2")) { //generado y consumidor
            cmd = "reg system name=Sistema desc=General system definition qos=resourceOptimized";
            String systID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd+" : "+systID);

            cmd = "reg scenary name=Escenario parent="+systID+" desc=Escenario principal";
            String scenID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd+" : "+scenID);

            cmd = "reg application name=Application1 parent="+scenID+" desc=Aplicaci�n de ejemplo 1"
                + " activation=periodic period=1000 deadline=1000";
            String app1ID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd+" : "+app1ID);

            cmd = "reg component name=Generador parent="+app1ID+" desc=Generador pares"
                + " activation=periodic period=2000 redundancy=0 CPUreq=low StorageReq=low"
                + " NetworkReg=low nodeRestriction=node101";
            String com1ID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd+" : "+com1ID);

            cmd = "reg cmpImplementation name=Generador parent="+com1ID+" desc=Generador"
                + " class=es.ehu.numeros.Generador platform=java7_32 version=1.0";
            String cmp1implID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd+" : "+cmp1implID);


            cmd = "reg component name=Consumidor parent="+app1ID+" desc=Monitorizador"
                + " activation=sporadic redundancy=0 CPUreq=low StorageReq=low NetworkReg=low"
                + " nodeRestriction=node103";
            String com3ID = sendCommand(mwm, cmd).getContent();

            cmd = "reg cmpImplementation name=Consumidor parent="+com3ID+" desc=Consumidor"
                + " class=es.ehu.numeros.Consumidor platform=java7_32 version=1.0";
            String cmp3implID = sendCommand(mwm, cmd).getContent();
            LOGGER.info(cmd+" : "+cmp3implID);


          }
        }
        
    public ACLMessage sendCommand(String mwm, String cmd) {
      LOGGER.entry(mwm, cmd);
      ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
      msg.addReceiver(new AID(mwm, AID.ISLOCALNAME));
      msg.setOntology("control");
      msg.setContent(cmd);
      msg.setReplyWith(cmd);
      send(msg);
      ACLMessage reply = blockingReceive(
          MessageTemplate.and(
          MessageTemplate.MatchInReplyTo(msg.getReplyWith()),
          MessageTemplate.MatchPerformative(ACLMessage.INFORM))
          , 1000);
      
      return LOGGER.exit(reply);
     }
        
        private boolean finished = false;
        
        public boolean done() {
            LOGGER.entry();
            return LOGGER.exit(finished);  
        }
        
    } // ----------- End myBehaviour
  
  class ShutdownThread extends Thread {
      private Agent myAgent = null;
      
      public ShutdownThread(Agent myAgent) {
        super();
        this.myAgent = myAgent;
      }
       
      public void run() {
        LOGGER.entry();        
        try { 
          DFService.deregister(myAgent);
          myAgent.doDelete();}
        catch (Exception e) {}
        LOGGER.exit();
      }
    }

}
