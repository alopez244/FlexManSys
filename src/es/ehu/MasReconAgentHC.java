package es.ehu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.xml.sax.SAXException;

import es.ehu.platform.utilities.MasReconAgent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;



public class MasReconAgentHC extends Agent{

  
  private static final long serialVersionUID = 1L;
  
  static final Logger LOGGER = LogManager.getLogger(Registerer.class.getName()) ;
  
  
   
  protected void setup() {
    LOGGER.entry();
    LOGGER.warn("warning output sample");
    try {
      Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
         LOGGER.debug("Añadida tarea de apagado...");
       } catch (Throwable t) {
         LOGGER.debug(" *** Error: No se ha podido aï¿½adir tarea de apagado");
    }    
    addBehaviour(new Mra(this));
    LOGGER.exit();
  }
  
  class Mra extends SimpleBehaviour 
    {   
    
    private MasReconAgent mra = new MasReconAgent(myAgent);
    private static final long serialVersionUID = 6711046229173067015L;
    
    
        public Mra(Agent a) { 
            super(a);
        }
        
        public void action() {
          LOGGER.entry();
          
          try {
            
            LOGGER.info("start.");
            mra.searchMwm();
            
            ConcurrentHashMap<String, String> atributes = new ConcurrentHashMap<String, String>();
            //ConcurrentHashMap<String, String> restrictions = new ConcurrentHashMap<String, String>();
            
            
            
            atributes.put("name", "ManufacturingPlan");
            atributes.put("category", "aaaan");
            String aps1 = mra.seRegister ("applicationSet", "system", atributes, null);
            
            atributes.clear();
            atributes.put("name", "ManufacturingPlan");
            atributes.put("category", "aaaan");
            String ap1 = mra.seRegister ("application", aps1 , atributes, null);
            
            
            
//            atributes.put("name", "ManufacturingPlan");
//            String appID = mra.seRegister ("application", snrID, atributes, restrictions);
            
              
//            atributes.clear();
//            mra.setAtribInf(snrID, atributes);
//            
//            atributes.clear();
//            atributes.put("name", "Aplicación de ejemplo");
//            String appID = mra.seRegister ("application", snrID, atributes, restrictions);
//
//            
//              atributes.clear();
//              atributes.put("targetComponentIDs", "compon102");
//              atributes.put("name", "Generador");
//              atributes.put("activation", "periodic");
//              atributes.put("period", "1000");
//              atributes.put("nodeRestriction", "node101");
//              atributes.put("negotiationCriteria", "max freeMem");
//              atributes.put("isFirst", "true");
//              atributes.put("redundancy", "0");
//              String cmp101 = mra.seRegister ("component", appID, atributes, restrictions);

//            atributes.clear();
//            atributes.put("name", "Generador");
//            atributes.put("class", "es.ehu.numeros.Generador");
//            atributes.put("platform", "java7_32");
//            mra.seRegister ("cmpImplementation", cmp101, atributes, restrictions);

//            atributes.clear();
//            atributes.put("sourceComponentIDs", "compon101");
//            atributes.put("targetComponentIDs", "compon103");
//            atributes.put("name", "Procesador");
//            atributes.put("nodeRestriction", "node102,node104");
//            atributes.put("negotiationCriteria", "max freeMem");
//            atributes.put("redundancy", "2");
//            String cmp102 = mra.seRegister ("component", appID, atributes, restrictions);
//
//            atributes.clear();
//            atributes.put("name", "Procesador");
//            atributes.put("class", "es.ehu.numeros.Procesador");
//            atributes.put("platform", "java7_32");
//            mra.seRegister ("cmpImplementation", cmp102, atributes, restrictions);
//              
//            atributes.clear();
//            atributes.put("sourceComponentIDs", "compon102");
//            atributes.put("name", "Consumidor");
//            atributes.put("nodeRestriction", "node103");
//            String cmp103 = mra.seRegister ("component", appID, atributes, restrictions);
//              
//            atributes.clear();
//            atributes.put("name", "Consumidor");
//            atributes.put("class", "es.ehu.numeros.Consumidor");
//            atributes.put("platform", "java7_32");
//            mra.seRegister ("cmpImplementation", cmp103, atributes, restrictions);

            //mra.start("applic101", null);
              
            
            LOGGER.info("finish.");
            
            while(true)
              Thread.sleep(1000);
            } catch (Exception e) {e.printStackTrace();}
          
            LOGGER.exit();
        }
        
        
        
        /**
         * Registers a new System Element
         * 
         * @param seType: system element type
         * @param parentId: hierarchical position
         * @param parentId: hierarchical position
         * @return element id
         */
        
        
        
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
 
  private String readFile(String file) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader (file));
    String         line = null;
    StringBuilder  stringBuilder = new StringBuilder();
    String         ls = System.getProperty("line.separator");

    try {
        while((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        return stringBuilder.toString();
    } finally {
        reader.close();
    }
}
  
  public String validate (String xsd, String file) {
  File schemaFile = new File("c:/temp/"+xsd);//Concepts.xsd"); // etc.
  Source xmlFile = new StreamSource(new File("c:/temp/"+file));///Concept.xml"));
  SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
  try {
    Schema schema = schemaFactory.newSchema(schemaFile);
    Validator validator = schema.newValidator();
    validator.validate(xmlFile);
    System.out.println(xmlFile.getSystemId() + " is valid");
  } catch (SAXException e) {
    System.out.println(xmlFile.getSystemId() + " is NOT valid reason:" + e);
    return e.toString();
  } catch (IOException e) {
    e.printStackTrace();
  }
  return "";
  }
  

}
