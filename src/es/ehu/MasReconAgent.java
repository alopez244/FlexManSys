package es.ehu;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



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
    return;
  }
  
  public String resRegister(String seType, String parentId, ConcurrentHashMap<String, String> attribList ){
    
    String command = "reg "+seType+" parent="+parentId;
    //if (parentId.equals("system")) parentId="registering";
    if (attribList!=null)
    for (Entry<String, String> entry : attribList.entrySet())
      command = command+" "+entry.getKey()+"="+entry.getValue();     
        
    return sendCommand(mwm,command).getContent();
    
    
  }
  
  /**
   * Registers a new System Element.
   * 
   * @param seType system element type
   * @param parentId hierarchical position
   * @param attribList element attributes
   * @return system element id
   * @throws XSDException
   */
  public String appRegister(String seType, String parentId, ConcurrentHashMap<String, String> attribList, 
      ConcurrentHashMap<String,ConcurrentHashMap<String, String>> reqServs) throws Exception {
    
    

    LOGGER.info("seRegister("+seType+","+parentId+",attribute,restrictions)");
    System.out.println("seRegister("+seType+","+parentId+",attribute,restrictions)");
    
//    String command = "reg "+seType+" parent="+parentId;
//    //if (parentId.equals("system")) parentId="registering";
//    if (attributes!=null)
//    for (Entry<String, String> entry : attributes.entrySet())
//      command = command+" "+entry.getKey()+"="+entry.getValue();
    
    
    //por aquí voy
    
    //compruebo restricciones
    //ConcurrentHashMap<String, ConcurrentHashMap<String, String>> restrictionLists
    
    if (reqServs!=null)
    for (Entry<String, ConcurrentHashMap<String, String>> restriction : reqServs.entrySet()){
      //restriction 1
      String query = "get * category="+restriction.getKey();
      
      //System.out.println("*********** "+restriction.getKey());
      for (Entry<String, String> entry : restriction.getValue().entrySet()){
        //cada atributo
        //System.out.println("*********** "+entry.getKey()+"="+entry.getValue());
        query = query +" "+entry.getKey()+"="+entry.getValue();
      }
      System.out.println("***************** Lanzo consulta de comprobación - "+query);
      String validateRestriction = sendCommand(mwm,query).getContent();
      if (validateRestriction.isEmpty()) {
        LOGGER.info(query+">"+validateRestriction+": restricción incumplida");
        throw new Exception();
      }
      
    }
      System.out.println();
      //command = command+" "+entry.getKey()+"="+entry.getValue();
    
    //localizo tipo del padre    
    String parentType = sendCommand (mwm, "get "+parentId+" attrib=category").getContent();

    if (parentType.equals("")) {     //no existe padre
      LOGGER.info("ERROR: parent id not found");
      throw new Exception();
    }
    LOGGER.info(parentId+" type="+parentType);
    
    //System.out.println("******* parent "+parentType);
    //throw new XSDException(noParent);
    
    //compruebo jerarquía
    String validateHierarchy = sendCommand(mwm,"validate hierarchy "+seType+" "+parentType).getContent();
    if (!validateHierarchy.equals("valid")) {
      LOGGER.info(seType+">"+parentType+": jerarquía incorrecta");
      throw new Exception();
    }
    LOGGER.info(seType+">"+parentType+": jerarquía correcta");
    
    // registro elemento en rama "concepts"
    
    String command = "reg "+seType+" seParent="+parentId+ " parent=concepts";
    //if (parentId.equals("system")) parentId="registering";
    if (attribList!=null)
    for (Entry<String, String> entry : attribList.entrySet())
      command = command+" "+entry.getKey()+"="+entry.getValue();     
    String ID = sendCommand(mwm,command).getContent();
    
     
    
    String validation =  sendCommand(mwm,"validate systemElement "+ID).getContent();
    LOGGER.info(validation);
        
     
    if (!validation.equals("valid")) { 
        sendCommand(mwm,"del "+ID).getContent();
        LOGGER.info("error xsd concepts");
        throw new Exception();
      //throw new XSDException(validation);
    }else 
      LOGGER.info("xsd concepts correcto");
    
    
    // TODO: por cada restrictionList una llamada al get y comprobar que existen en el SystemModel
    for (String keyi: reqServs.keySet()){
      System.out.println("*******************key="+keyi);
      String restrictionList = sendCommand(mwm,"reg restrictionList se="+keyi+" parent="+ID).getContent();
      
      
      for (String keyj: reqServs.get(keyi).keySet()){
        String restriction = sendCommand(mwm,"reg restriction attribName="+keyj+" attribValue="+reqServs.get(keyi).get(keyj)+" parent="+restrictionList).getContent();
        System.out.println("keyj="+keyj);
      }
    }
      
    
    
    // mover a registering
    if (parentId.equals("system")) 
      sendCommand(mwm,"set "+ID+" parent=registering").getContent();
    else 
      sendCommand(mwm,"set "+ID+" parent=(get "+ID+" attrib=seParent) seParent=").getContent();
    
    
    return ID;
    
  }
  
 public String appValidate(String setId) throws Exception {
    
    //localizo tipo del padre
    LOGGER.info("iValidate("+setId+")");
    String parentType = sendCommand (mwm, "get "+setId+" attrib=category").getContent();

    //no existe
    if (parentType.equals("")) {
      LOGGER.info("ERROR: id not found");
      return "";
    }
    LOGGER.info(parentType+" type="+parentType);
    
    //System.out.println("******* parent "+parentType);
    //throw new XSDException(noParent);
    
    //compruebo jerarquía
    String validateHierarchy = sendCommand(mwm,"validate appValidation "+setId+" "+parentType).getContent();
    if (!validateHierarchy.equals("valid")) {
      LOGGER.info(setId+">"+parentType+": xsd incorrecta");
      throw new Exception();
      
      // TODO: Borrar
    }
    LOGGER.info(validateHierarchy+">"+parentType+": xsd correcta");
    
    //sendCommand(mwm,"set "+se+" parent=(get "+se+" attrib=seParent) seParent=").getContent();
    
    
        
     
//    if (!validation.equals("valid")) { 
//        sendCommand(mwm,"del "+ID).getContent();
//        LOGGER.info("error xsd concepts");
//        return "";
//      //throw new XSDException(validation);
//    }else 
//      LOGGER.info("xsd concepts correcto");
    
    
    // mover a registering
    sendCommand(mwm,"set "+setId+" parent=(get "+setId+" attrib=seParent) seParent=").getContent();
    
    
    return setId;
    
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
  
  public String[] getAttrib(String seId, ConcurrentHashMap attribName){
    return null; //devuelve lista de todos los valores de atributos de los elementos que cumplen el filtro
  }
  
  public String[] getSytemElement(String seType, ConcurrentHashMap <String, String> filtro){
    return null; //devuelve lista de todos los elementos que cumplen el filtro
  }
  
  /**
   * @param seId element id
   * @param attribList element attributes
   * @return 
   */
  public String setAtrib(String seId, ConcurrentHashMap<String, String> attribList){
       
    StringBuilder command = new StringBuilder("set "+seId);
    
    if (attribList!=null)
      attribList.entrySet().stream().forEach(entry -> command.append(" "+entry.getKey()+"="+entry.getValue()));
    
    return sendCommand(mwm,command.toString()).getContent();
  }
  
  public String seStart(String seId, ConcurrentHashMap<String, String> attribList){
    
    StringBuilder command = new StringBuilder("start "+seId);
    
    if (attribList!=null)
      attribList.entrySet().stream().forEach(entry -> command.append(" "+entry.getKey()+"="+entry.getValue()));
    
    return sendCommand(mwm,command.toString()).getContent();
    
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
    System.out.println("mwm="+mwm);
    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
    msg.addReceiver(new AID(mwm, AID.ISLOCALNAME));
    msg.setOntology("control");
    msg.setContent(cmd);
    msg.setReplyWith(cmd);
    myAgent.send(msg);
    ACLMessage reply = myAgent.blockingReceive(
        MessageTemplate.and(
        MessageTemplate.MatchInReplyTo(msg.getReplyWith()),
        MessageTemplate.MatchPerformative(ACLMessage.INFORM))
        , 1000);
    
    return LOGGER.exit(reply);
   }  
  
}
