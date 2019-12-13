package es.ehu.platform.utilities;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;


import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.ehu.XSDException;



public class MasReconAgent {
  
  
  Agent myAgent = null;
  /**
   * 
   */
  private static final long serialVersionUID = -6032892162787798648L;
  static final Logger LOGGER = LogManager.getLogger(MasReconAgent.class.getName()) ;

  private String mwm = null;
  
  public MasReconAgent(Agent myAgent){
    this.myAgent = myAgent;
  }
  
  
  
  public void searchMwm() throws FIPAException, InterruptedException{
    LOGGER.entry();
  //arranque, esperar a tmwm activo
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
          LOGGER.info(".");
          Thread.sleep(100);
        
      } //end while (true)
    LOGGER.exit();
  }
  
  public String register(String seType, String parentId,ConcurrentHashMap<String, String> attributes ){
    LOGGER.entry(seType, parentId, attributes);
    String command = "reg "+seType+" parent="+parentId;
    //if (parentId.equals("system")) parentId="registering";
    if (attributes!=null)
    for (Entry<String, String> entry : attributes.entrySet())
      command = command+" "+entry.getKey()+"="+entry.getValue();     
        
    return LOGGER.exit(sendCommand(mwm,command).getContent());
  }
  
  /**
   * Registers a new System Element.
   * 
   * @param seType system element type
   * @param parentId hierarchical position
   * @param attributes element attributes
   * @return system element id
   * @throws XSDException
   */
  public String seRegister(String seType, String parentId, ConcurrentHashMap<String, String> attributes, 
      ConcurrentHashMap<String,ConcurrentHashMap<String, String>> restrictionLists) throws Exception {
    LOGGER.entry(seType, parentId, attributes, restrictionLists);
    
//    String command = "reg "+seType+" parent="+parentId;
//    //if (parentId.equals("system")) parentId="registering";
//    if (attributes!=null)
//    for (Entry<String, String> entry : attributes.entrySet())
//      command = command+" "+entry.getKey()+"="+entry.getValue();
    
    //por aquí voy
    
    //compruebo restricciones
    //ConcurrentHashMap<String, ConcurrentHashMap<String, String>> restrictionLists
    
    System.out.println(sendCommand(mwm,"help").getContent());
    
    String restrictionMatch = null;
    if (restrictionLists!=null)
    for (Entry<String, ConcurrentHashMap<String, String>> restriction : restrictionLists.entrySet()){
      //restriction 1
      //String query = "get * category="+restriction.getKey();
      String query = "get * category=service";
      
      for (Entry<String, String> entry : restriction.getValue().entrySet()){
        //cada atributo
        //System.out.println("*********** "+entry.getKey()+"="+entry.getValue());
        query = query +" "+entry.getKey()+"="+entry.getValue();
      }
      query = "get (get ("+query+") attrib=parent) category="+restriction.getKey();
     // get (get (get * category=service serviceType=healthSensor) attrib=parent) category=procNode
          
      System.out.println("***************** Lanzo consulta de comprobación "+query);
      String validateRestriction = sendCommand(mwm,query).getContent();
//      if (validateRestriction.isEmpty()) {
//        LOGGER.info(query+">"+validateRestriction+": restricción incumplida");
//        throw new Exception();
//      }
      
    }

    //command = command+" "+entry.getKey()+"="+entry.getValue();
    
    //localizo tipo del padre    // TODO si el padre es "system" no comprobar
    // TODO si el padre está en systemmodel.xml:
      // ir a systemmodel.xsd y buscar <xs:extension base="tipo" y en sus hijos
      // getFixed (tipo, atributo) > buscar <xs:extension base="tipo" y en sus hijos devuelve el fixed del que tenga nombre atributo
      // 
      // si es extensible el padre traigo la estructura desde el hijo de system con los atributos, (resolver su ID **registering**), validar appvalidar.xsd
      // si valida > volver a montarlo en systemmodel
      
      
      
    String parentType = sendCommand (mwm, "get "+parentId+" attrib=category").getContent();

    if (parentType.equals("")) {     //no existe padre
      LOGGER.info("ERROR: parent id not found");
      throw new Exception();
    }
    LOGGER.info(parentId+" type="+parentType);
    
    //System.out.println("******* parent "+parentType);
    //throw new XSDException(noParent);
    
    //compruebo jerarquía // TODO si el padre es "system" comprobar que el se es raiz del appvalidation xsd -> dom 

    String validateHierarchy = sendCommand(mwm,"validate hierarchy "+seType+" "+parentType).getContent();
    if (!validateHierarchy.equals("valid")) {
      LOGGER.info(seType+">"+parentType+": jerarquía incorrecta");
      throw new Exception();
    }
    LOGGER.info(seType+">"+parentType+": jerarquía correcta");
    
    // registro elemento en xml elements
    
    String command = "reg "+seType+" seParent="+parentId+ " parent=concepts";
    //if (parentId.equals("system")) parentId="registering";
    if (attributes!=null)
    for (Entry<String, String> entry : attributes.entrySet())
      command = command+" "+entry.getKey()+"="+entry.getValue();     
    String ID = sendCommand(mwm,command).getContent();
    
     
    
   
    
    
    // TODO: por cada restrictionList una llamada al get y comprobar que existen en el SystemModel
    for (String keyi: restrictionLists.keySet()){
      System.out.println("*******************key="+keyi);
      String restrictionList = sendCommand(mwm,"reg restrictionList se="+keyi+" parent="+ID).getContent();
      
      
      for (String keyj: restrictionLists.get(keyi).keySet()){
        String restriction = sendCommand(mwm,"reg restriction attribName="+keyj+" attribValue="+restrictionLists.get(keyi).get(keyj)+" parent="+restrictionList).getContent();
        System.out.println("keyj="+keyj);
      }
    }
      
    //validar elemento contra esquema systemElements
    String validation =  sendCommand(mwm,"validate systemElement "+ID).getContent();
    LOGGER.info(validation);
        
     
    if (!validation.equals("valid")) { 
        sendCommand(mwm,"del "+ID).getContent();
        LOGGER.info("error xsd concepts");
        throw new Exception();
      //throw new XSDException(validation);
    }else 
      LOGGER.info("xsd concepts correcto");
    
    // mover a registering.xml
    if (parentId.equals("system")) 
      sendCommand(mwm,"set "+ID+" parent=registering").getContent();
    else 
      sendCommand(mwm,"set "+ID+" parent=(get "+ID+" attrib=seParent) seParent=").getContent();
    
    
    return ID;
    
  }
  
 public String iValidate(String se) throws Exception {
    
   
   
    //localizo tipo
    LOGGER.info("iValidate("+se+")");
    String seType = sendCommand (mwm, "get "+se+" attrib=category").getContent();

    //no existe
    if (seType.equals("")) {
      LOGGER.info("ERROR: id not found");
      return "";
    }
    LOGGER.info(seType+" type="+seType);
    
    //System.out.println("******* parent "+parentType);
    //throw new XSDException(noParent);
    
    //compruebo jerarquía
    String validateHierarchy = sendCommand(mwm,"validate appValidation "+se+" "+seType).getContent();
    if (!validateHierarchy.equals("valid")) {
      LOGGER.info(se+">"+seType+": xsd incorrecta");
      throw new Exception();
      
      // TODO: Borrar
    }
    LOGGER.info(validateHierarchy+">"+seType+": xsd correcta");
    
    //sendCommand(mwm,"set "+se+" parent=(get "+se+" attrib=seParent) seParent=").getContent();
    
    
        
     
//    if (!validation.equals("valid")) { 
//        sendCommand(mwm,"del "+ID).getContent();
//        LOGGER.info("error xsd concepts");
//        return "";
//      //throw new XSDException(validation);
//    }else 
//      LOGGER.info("xsd concepts correcto");
    
    
    // mover a registering
    sendCommand(mwm,"set "+se+" parent=(get "+se+" attrib=seParent) seParent=").getContent();
    
    
    return se;
    
  }
  
 
 public String iStart(String se) throws Exception {
   //VALIDACION 
   // TODO: comprobar si es de tipo "startable"
   //EJECUCIÓN
   return sendCommand(mwm,"start "+se).getContent();
   
   
   
 }
  public int deRegister(String id){
    //String result = 
    return 0; //0 correcto, 1 error
  }
  
 
  
  public String validate(String seId){
    return sendCommand(mwm,"validate "+seId).getContent();
  }
  
  /**
   * @param seId system element id
   * @return element xml structure
   */
  public String getSeInfo (String seId) {
    String xml = sendCommand(mwm,"listXml "+seId).getContent();
    return xml; //xml a partir de la estructura o vacío si no existe el seID
  }
  
  public String[] getAttribInfo(String attribName, ConcurrentHashMap filtro){
    return null; //devuelve lista de todos los valores de atributos de los elementos que cumplen el filtro
  }
  
  /**
   * @param seId element id
   * @param attributes element attributes
   * @return 
   */
  public String setAtribInf(String seId, ConcurrentHashMap<String, String> attributes){
       
    StringBuilder command = new StringBuilder("set "+seId);
    
    if (attributes!=null)
      attributes.entrySet().stream().forEach(entry -> command.append(" "+entry.getKey()+"="+entry.getValue()));
    
    return sendCommand(mwm,command.toString()).getContent();
  }
  
  public String start(String seId, ConcurrentHashMap<String, String> attributes){
    LOGGER.entry(seId, attributes);
    
    StringBuilder command = new StringBuilder("sestart "+seId);
    
    if (attributes!=null)
      attributes.entrySet().stream().forEach(entry -> command.append(" "+entry.getKey()+"="+entry.getValue()));
    
    return LOGGER.exit(sendCommand(mwm,command.toString()).getContent());
    
    
  }
  
  public int stop(String seId){
    return 0; //0 correcto, 1 error
  }
  
   public int pause(String seId){
    return 0; //0 correcto, 1 error
  }

  public int resume(String seId){
    return 0; //0 correcto, 1 error
  }
  
 

  public ACLMessage sendCommand(String cmd) {
  return sendCommand(mwm, cmd);
  }
    
  public ACLMessage sendCommand(String mwm, String cmd) {
      LOGGER.entry(mwm, cmd);
      if (mwm==null)
        try {
          searchMwm();
        } catch (Exception e) {e.printStackTrace();}
      
      ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
      msg.addReceiver(new AID(mwm, AID.ISLOCALNAME));
      msg.setOntology("control");
      msg.setContent(cmd);
      msg.setReplyWith(cmd);
      
      
      myAgent.send(msg);
      
      ACLMessage reply = myAgent.blockingReceive();
//          MessageTemplate.and(
//          MessageTemplate.MatchInReplyTo(msg.getReplyWith()),
//          MessageTemplate.MatchPerformative(ACLMessage.INFORM))
//          , 1000);
      
      LOGGER.info((cmd.startsWith("validate"))?"xsd: "+reply.getContent(): cmd+" > "+reply.getContent());
      
      return LOGGER.exit(reply);
  }
  
  
}
