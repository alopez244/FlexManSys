package es.ehu.platform;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.ThreadedBehaviourFactory.ThreadedBehaviourWrapper;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.xml.sax.SAXException;

import es.ehu.ThreadedCommandProcessor;



/* 
 cd C:\Users\bcsgaguu\workspace\sede.ws\JadeMW\JadeMiddleware
 java -cp bin;lib\jade.jar jade.Boot -container mwm:es.ehu.MiddlewareManager 
 */

// Taskkill /F /IM java.exe

public class AvailabilityManager extends Agent {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  static final Logger LOGGER = LogManager.getLogger(AvailabilityManager.class.getName()) ;

  final public ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
  protected DataStore ds = null;

  final protected ConcurrentHashMap<String, SimpleBehaviour> behaviours = new ConcurrentHashMap<String, SimpleBehaviour>();
  final protected ConcurrentHashMap<String, String> threadLog = new ConcurrentHashMap<String, String>();

  public ConcurrentHashMap<String, Object> objects = new ConcurrentHashMap<String, Object>();
  public ConcurrentHashMap<String, Object> executionStates = new ConcurrentHashMap<String, Object>();
  public ConcurrentHashMap<String, Hashtable<String, String>> elements = new ConcurrentHashMap<String, Hashtable<String, String>>();
  public ConcurrentHashMap<String, Integer> count = new ConcurrentHashMap<String, Integer>();
  //final public ConcurrentHashMap<String, ArrayList<String>> cmpMap = new ConcurrentHashMap<String, ArrayList<String>>();
  private int cmdId = 1000;
  public static long startTime = System.currentTimeMillis();

  private String mwm;
  
  protected void setup() {
    LOGGER.entry();

    String key = "avb_"+System.currentTimeMillis();
    behaviours.put(key, new TheadedAvailabilityManagerBehaviour(this));
    this.addBehaviour(tbf.wrap(behaviours.get(key)));

    LOGGER.debug("this.addBehaviour(tbf.wrap(behaviours.get("+key+")));");
    LOGGER.exit();
  }

  private class TheadedAvailabilityManagerBehaviour extends SimpleBehaviour {

    private static final long serialVersionUID = 7023269912150712219L;
    private MessageTemplate template = null;
    private AvailabilityManager myAgent= null;


    public TheadedAvailabilityManagerBehaviour(Agent a) {
      super(a);
      this.myAgent=(AvailabilityManager) a;
    }

    public void onStart(){
      LOGGER.entry();

      myAgent.ds = this.getDataStore();
      ((AvailabilityManager)myAgent).registerAgent("avbm"); //registrar mwm en el df
      
      //filtro de recepción de mesnajes (state) || (control&REQUEST+CONTROL)
      template = MessageTemplate.or(MessageTemplate.MatchOntology("state"),
                                    MessageTemplate.and(MessageTemplate.MatchOntology("control"), 
                                                        MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), 
                                                                           MessageTemplate.MatchPerformative(ACLMessage.INFORM)))); 
      
      //aMsg.setOntology("state");
      LOGGER.debug("template=" + template);
      LOGGER.warn("AVBM Started!");
      LOGGER.exit();
    }

    public void action() {
      LOGGER.entry();
      
      /** recore comportamientos y dataStores 
      for (String key : behaviours.keySet()) {
        LOGGER.trace(key+".executionState="+behaviours.get(key).getExecutionState());       
        if (behaviours.get(key).getExecutionState().equals("READY")) {
          LOGGER.trace(key + "restart()");        
          for (Iterator iter=behaviours.get(key).getDataStore().keySet().iterator();iter.hasNext();) {            
            ACLMessage reply = (ACLMessage)behaviours.get(key).getDataStore().get(iter.next());               
            if (reply.getPerformative()==ACLMessage.FAILURE)
              LOGGER.info("FAILURE "+reply.getContent());
          } // for iterator
          myAgent.removeBehaviour(behaviours.get(key)); 
        } // if behaviour is READY
      } // for behaviours
       */
      ACLMessage msg = receive (template);

      if (msg != null) {
//        if (msg.getOntology().equals("state")) { //ontologia de estado, recibo un estado de un componente
//          
//          
//          String compon = (elements.containsKey(msg.getSender().getLocalName()))? getCmp(msg.getSender().getLocalName()) : "";          
//          //LOGGER.debug(msg.getSender().getLocalName()+" ("+compon+"): update execState");
//          
//          try{
//            executionStates.put(compon, msg.getContentObject());
//          } catch (Exception e) {e.printStackTrace();}
//          
          
        } else { // ontología de control
        if (msg.getConversationId()!=null) { //si hay id de conversación despertaremos la tarea que interviene en ella
          LOGGER.trace("***************** Conversation message *******************");
          if (behaviours.containsKey(msg.getConversationId())){ //si hay una coportamiento con ese id
            LOGGER.trace("Localizado comportamiento");
            myAgent.ds.put(msg.getConversationId(), msg); // guardamos el dato en el ds común
            LOGGER.trace("behaviours.get("+msg.getConversationId()+").resume()");
            myAgent.tbf.getThread(behaviours.get(msg.getConversationId())).resume();
          } // end existe la tarea
        } // end llega conversationId

        LOGGER.trace("***************** New message *******************");
        LOGGER.trace("received message from "+msg.getSender().getLocalName());
        LOGGER.trace("msg.getContent()="+msg.getContent());

        String key = String.valueOf(cmdId++);     
        ds.put(key, msg);
        behaviours.put(key, new ThreadedCommandProcessor(key, myAgent));
        myAgent.addBehaviour(tbf.wrap(behaviours.get(key)));

      } // end ontología de control 
    
    
        block();
      }
    
      @Override
      public boolean done() {
        // TODO Auto-generated method stub
        return false;
      }
      

    } // end MWMBehaviour

    private boolean finished = false;

    public boolean done() {
      LOGGER.entry();
      return LOGGER.exit(finished);
    }
   // ----------- End myBehaviour

  public String processCmd(String cmd, String conversationId) {
    LOGGER.entry(cmd, conversationId);
    if (conversationId==null) conversationId=String.valueOf(cmdId++);
//    if (!cmd.startsWith("getins "))
//      LOGGER.info(cmd + "("+conversationId+")");
    StringBuilder result = new StringBuilder();
    
    
    // soporte de subcomandos como:
    // localcmd (getins compon102 state=tracking) cmd=setstate paused
    // pasa a paused todas las instancias en tracking del componente compon102
    
    while (cmd.length()>0 && cmd.charAt(0)=='\"' && cmd.charAt(cmd.length()-1)=='\"') cmd=cmd.substring(1, cmd.length()-1);
        
    boolean dentroDeComilla = false;
    char[] cCmd = cmd.toCharArray();
    for (int i=0; i<cCmd.length; i++) { 
      if (cCmd[i]=='\"') dentroDeComilla = !dentroDeComilla;
      if (dentroDeComilla) {
        if (cCmd[i]=='(') cCmd[i]='{';
        else if (cCmd[i]==')') cCmd[i]='}';
        else if (cCmd[i]=='=') cCmd[i]='#';
      }
    } //end forCCmd;
    cmd = new String(cCmd);
    
    if (dentroDeComilla) result.append("No closed \"");
      
    while (cmd.contains("(")) {
      String subCmd = cmd.substring(cmd.lastIndexOf('(', cmd.indexOf(')'))+1, cmd.indexOf(')'));
      String subCmdResult = processCmd(subCmd, conversationId);
      cmd=cmd.replace('('+subCmd+')', (subCmdResult.isEmpty()?"null":subCmdResult));     
    } 
    
    if (!cmd.startsWith("getins "))
      LOGGER.info(cmd + "("+conversationId+")");
    

    while (cmd.contains("  ")) cmd=cmd.replace("  ", " ");
    while (cmd.contains(" =")) cmd=cmd.replace(" =", "=");
    
    String[] cmds = cmd.split(" ");
    Hashtable<String, String> attribs = processAttribs(cmds);

    try {
      
      
      
      
      if (cmds[0].equals("report")) result.append(report(cmd));
      
//      else if (cmds[0].equals("negotiate")) result.append(negotiateRecovery(cmds[1], conversationId));
//      else if (cmds[0].equals("localcmd")) result.append(localCmd(cmds[1], attribs, conversationId));
//      else if (cmds[0].equals("localneg")) result.append(negotiate(cmds[1], attribs.get("negotiationCriteria"), attribs.get("action"), conversationId));
//      else if (cmds[0].equals("setlocal")) { attribs.put("cmd", "set"); result.append(localCmd(cmds[1], attribs, conversationId)); }
//      else if (cmds[0].equals("getlocal")) { attribs.put("cmd", "get"); result.append(localCmd(cmds[1], attribs, conversationId)); }
 
      else result.append("cmd not found:" + cmds[0]);
       
        
      /**
       * ereg event type=system
       * reg event type=scenario 
       * reg action parent=even001 order=1 event orden
       * reg event
       * reg acciones event orden
       *       anterior terminado, si anterior no cumple bloqueo
       *       kill - get
       *       start - get
       *       set - get
       *       
       *       
       *       
       */            

    } catch (Exception e) {
      LOGGER.error("ERROR:"+e.getLocalizedMessage());
      e.printStackTrace();
    }

    
    
    if (conversationId==null)
    threadLog.put(String.valueOf(cmdId++), ((result.length()>20)?result.substring(0,20)+"...":result) + " < "  
        + ((cmd.length()<=60)?cmd:cmd.substring(0,60)+"...")
        +" ("+(System.currentTimeMillis()-startTime)+"ms)");
    
    return result.toString();
    
  }

  

  public String[] splitCmds (final String content){
    LOGGER.entry(content);
    String response=content.trim();
    
    boolean dentroDeComilla = false;
    char[] cCmd = content.toCharArray();
    for (int i=0; i<cCmd.length; i++) { 
      if (cCmd[i]=='\"') dentroDeComilla = !dentroDeComilla;
      if (dentroDeComilla) {
        if (cCmd[i]=='(') cCmd[i]='{';
        else if (cCmd[i]==')') cCmd[i]='}';
        else if (cCmd[i]=='=') cCmd[i]='#';
      }
    } //end forCCmd;
    String cmd = cCmd.toString();
      
    
    //String[] cmds = cmd.replaceAll("  ", " ").replace(" =", "=").replace("= ", "=").split(" ");
    
    return LOGGER.exit(cmd.replaceAll("  ", " ").replace(" =", "=").replace("= ", "=").split(" "));
    //return LOGGER.exit(response.split(" "));
  } // end process

  public Hashtable<String, String> processAttribs(String... cmdLine){
    LOGGER.entry((Object[])cmdLine);
    
    if (cmdLine.length < 3) return null; //no hay atributos

    Hashtable<String, String> attribs = new Hashtable<String, String>();
    String attrib = "attrib";

    for (int i = 2; i < cmdLine.length; i++) {
      if (cmdLine[i].contains("=")) { // encuentro otro atributo
        String[] attribDef = cmdLine[i].split("=");
        attrib = attribDef[0];
        attribs.put(attrib, attribDef[1]);
      } else
        attribs.put(attrib, attribs.get(attrib)+" "+cmdLine[i]);
     String attribValue = attribs.get(attrib);
     while (attribValue.contains("{")) attribValue = attribValue.replace("{", "(");
     while (attribValue.contains("}")) attribValue = attribValue.replace("}", ")");
     while (attribValue.contains("#")) attribValue = attribValue.replace("#", "=");
     attribs.put(attrib, attribValue); 
     
    }
    return LOGGER.exit(attribs);
  }

  private String getChildren(String parent, String prefijo) {
    LOGGER.entry(parent, prefijo);
    StringBuffer response = new StringBuffer();
    for (String key : elements.keySet())
      for (String key2 : elements.get(key).keySet())
        if (key2.equals("parent") && elements.get(key).get(key2).equals(parent)) {
          response.append(prefijo + list(key)).append("\n");
          response.append(getChildren(key, prefijo + "\t"));
        } else
          if (key2.equals("node") && elements.get(key).get(key2).equals(parent)) {
            response.append(prefijo + list(key)).append("\n");
            response.append(getChildren(key, prefijo + "\t"));
          }
    return LOGGER.exit(response.toString());
  }

  
private Document listDom(String _prm) throws Exception{
    
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.newDocument();
    
    for (String element: elements.keySet()) {
      LOGGER.info("recorriendo "+element);
      if (element.matches(_prm)) {
        LOGGER.info("*****"+elements.get(element).get("category"));

        // Genero elememto raiz del DOM
        Element rootElement = doc.createElement(elements.get(element).get("category"));
        doc.appendChild(rootElement);
        
        Attr attr = doc.createAttribute("ID");
        attr.setValue(element);
        rootElement.setAttributeNode(attr);
        
        //añado atributos al raiz
        forKeys: for (String key : elements.get(element).keySet()) {
          if (key.equals("category") || key.equals("parent") || key.equals("xsd")) continue forKeys;
          attr =  doc.createAttribute(key);
          attr.setValue(elements.get(element).get(key));
          rootElement.setAttributeNode(attr);
        }

        appendChildren(doc, rootElement, element);

      } 
    } // end for
    return doc;
  }

  private void appendChildren(Document doc, Element parent, String parentID) {
    LOGGER.entry(parent, parentID);

    //elements.keySet().stream().forEach(i->elements.get(i).keySet().stream());
    for (String key : elements.keySet())
      //elements.get(key).keySet().stream().filter(e -> e.equals("parent") && elements.get(key).get(e).equals(parentID))
      for (String key2 : elements.get(key).keySet())
        if (key2.equals("parent") && elements.get(key).get(key2).equals(parentID)) {
          
          Element hijo = doc.createElement(elements.get(key).get("category"));
          parent.appendChild(hijo);
          
          Attr attr = doc.createAttribute("ID");
          attr.setValue(key);
          hijo.setAttributeNode(attr);
          
          forKeys: for (String eachKey : elements.get(key).keySet()) {
            if (eachKey.equals("category") || eachKey.equals("parent") || eachKey.equals("xsd")) continue forKeys;
            attr =  doc.createAttribute(eachKey);
            attr.setValue(elements.get(key).get(eachKey));
            
            hijo.setAttributeNode(attr);
            
          }
    
          //llamada recursiva para generar todo el arbol
          appendChildren(doc, hijo, key);
        } 

    return ;//LOGGER.exit(response.toString());
  }
  
  private String validate(String... _prm) throws Exception {
    String output = "valid";
    Document document = listDom(_prm[0]);
    
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

 // load a WXS schema, represented by a Schema instance
       
    Source schemaFile = new StreamSource(new StringReader(URLDecoder.decode(elements.get(_prm[0]).get("xsd"), "UTF-8")));
        
    Schema schema = schemaFactory.newSchema(schemaFile);
    
 // create a Validator instance, which can be used to validate an instance document
    Validator validator = schema.newValidator();
    
 // validate the DOM tree
    try {
        validator.validate(new DOMSource(document));
    } catch (SAXException e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      output = sw.toString(); // stack trace as a string
      LOGGER.info(output);
        // instance document is invalid!
    }
    if (output.indexOf(';')>0) //hay error
    output = output.substring(output.indexOf(";")+2, output.indexOf('\n')); //muestro la descripción
    
    return output;
  }
  
 
  private String listXml(String... prm) throws Exception{ 
    LOGGER.entry(prm);
    String _prm = prm[0].replace("*", ".*");
    
    
    //Transformar dom a String
    StringWriter sw = new StringWriter();
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

    transformer.transform(new DOMSource(listDom(_prm)), new StreamResult(sw));
    return sw.toString();
    
    
  }
  
  private String list(String... prm) {
    LOGGER.entry(prm);
    String _prm = prm[0].replace("*", ".*");
    
        
    StringBuffer response = new StringBuffer("");

    if (_prm.startsWith("desi")) { // desing
      String comienzo = (prm.length>1 && !prm[1].isEmpty())? prm[1].replace("*", ".*"):"system.*";      
      for (String key : elements.keySet())
        if (key.matches(comienzo)) {
          response.append(list(key)).append("\n");
          response.append(getChildren(key, "\t"));
        }
    } else if (_prm.startsWith("infr")) { // infrastructure
      for (String key : elements.keySet())
        if (key.startsWith("syst")) {
          response.append(list(key)).append("\n");
          for (String nodeKey : elements.keySet())
            if (nodeKey.startsWith("node"))
              // el nodo pertenece al sistema que pintamos
              if (elements.get(nodeKey).get("parent").equals(key))
                response.append("\t").append(list(nodeKey)).append("\n");
        }
    } else if (_prm.startsWith("runt")) { // runtime
      response.append("not implemented"); // TODO: runtime list
    } else if (_prm.equals("objects")) {  //objects
      for (String key : objects.keySet())
        response.append(key).append(": ").append(objects.get(key).getClass()).append("\n");
      //    } else if (_prm.equals("cmpmap")){
      //      for (String cmp: cmpMap.keySet()){
      //        response.append(cmp).append(" >");
      //        for (String cmpIns: cmpMap.get(cmp))
      //          response.append(" ").append(cmpIns);
      //        response.append("\n");
      //      }

    } else if (_prm.equals("states")) {  //states
      for (String key : executionStates.keySet())
        response.append(key).append(": ").append(executionStates.get(key).getClass()).append("\n");
    }else if (_prm.startsWith("activethreads")) {
      for (ThreadedBehaviourWrapper tbw: tbf.getWrappers()) 
        try{
          response.append(((ThreadedCommandProcessor)tbw.getBehaviour()).key).append(" ").append(((ThreadedCommandProcessor)tbw.getBehaviour())
              .msg.getContent()).append(" ").append(" (cond:").append(((ThreadedCommandProcessor)tbw.getBehaviour()).condition).append(")\n");
        }catch (Exception e) {}
    } else if (_prm.startsWith("threads")) {
      for (int i=((threadLog.size()<500)?1000:threadLog.size()+500); i<threadLog.size()+1000;i++) {
        
        if (threadLog.containsKey(String.valueOf(i)) && !threadLog.get(String.valueOf(i)).contains("< getins") && !threadLog.get(String.valueOf(i)).contains("< list")) 
          response.append(String.valueOf(i)).append(": ").append(threadLog.get(String.valueOf(i))).append("\n");
        
      }
    } else
      for (String element: elements.keySet()) {
        if (element.matches(_prm)) {
          response.append("ID=" + element + " ");
          for (String key : elements.get(element).keySet()) {
            response.append(key).append("=").append(elements.get(element).get(key)).append(" ");
          }
          response.append("\n");
        } 
      } // end for


    if (response.length()>0)
      while (response.charAt(response.length()-1)=='\n') 
        response.deleteCharAt(response.length()-1);
    else response.append("not found!");

    return LOGGER.exit(response.toString());
  }

  private String negotiate (String prm, Hashtable<String, String> attribs, String conversationId){
    
    System.out.println("condition="+attribs.get("condition"));
    System.out.println("ids="+attribs.get("ids"));
    String[] lids = attribs.get("ids").split(",");
    String winner = null;
    
    long bestValue=(prm.equals("max")?Long.MIN_VALUE:Long.MAX_VALUE);
    
    int step=0;
    int repliesCnt=0;
    MessageTemplate mt = null;
//    System.out.println("empiezo");
    while (step<2)
    switch (step) {
          case 0:
                  // Send the cfp to all sellers
//              System.out.println("paso "+step);
                  ACLMessage cfp = new ACLMessage(ACLMessage.CFP); //Call For Proposals
                  for (String id: lids) cfp.addReceiver(new AID(id, AID.ISLOCALNAME));
                  cfp.setContent(attribs.get("condition"));
                  cfp.setOntology("control");
                  cfp.setConversationId(conversationId);
                  cfp.setReplyWith("cfp_"+System.currentTimeMillis()); // Unique value
                  this.send(cfp);
//                  System.out.println("enviado mensaje "+step);
                  // Prepare the template to get proposals
                  mt = MessageTemplate.MatchInReplyTo(cfp.getReplyWith());
                  step = 1;
                  break;
          case 1:
                  // Receive all proposals/refusals from seller agents
            
//              System.out.println("paso "+step);
                  ACLMessage reply = receive(mt);
                  // esperar a mensaje
                  if (reply != null) {
                  // Reply received
                      if (reply.getPerformative() == ACLMessage.PROPOSE) {
                        
                          // This is an offer
                          long value = Long.parseLong(reply.getContent());
                          System.out.println(reply.getSender().getLocalName()+" PROPOSE "+value);
                          value=(prm.equals("max")?value:-value);
                          if (winner == null || value > bestValue) {
                              // This is the best offer at present
                            bestValue = value;
                            winner = reply.getSender().getLocalName();
                          }
                      } else if (reply.getPerformative() == ACLMessage.FAILURE) {                         
                        String name=reply.getContent().substring(reply.getContent().indexOf(":name ", reply.getContent().indexOf("MTS-error"))+":name ".length());
                        name=name.substring(0, name.indexOf('@'));                        
                        System.out.println(name+" FAILURE");
                        //for (Iterator<AID> iter=reply.getAllIntendedReceiver(); iter.hasNext();) System.out.println(iter.next().getLocalName()+(iter.hasNext()?",":" FAILURE"));
                      }
                      
                      repliesCnt++;
//                      System.out.println("recibidos="+repliesCnt+" esperados="+lids.length);
                      if (repliesCnt >= lids.length)  // We received all replies
                          step = 2;                        
                  }
                  
//          case 2:
//                  // Send the purchase order to the seller that provided the best offer
//                  ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
//                  order.addReceiver(bestSeller);
//                  order.setContent(titolo.toLowerCase());
//                  order.setConversationId("book-trade");
//                  order.setReplyWith("order"+System.currentTimeMillis());
//                  myAgent.send(order);
//                  // Prepare the template to get the purchase order reply
//                  mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),MessageTemplate.MatchInReplyTo(order.getReplyWith()));
//                  step = 3;
//                  break;
//          case 3:
//                  // Receive the purchase order reply
//                  reply = myAgent.receive(mt);
//                  if (reply != null) {
//                      // Purchase order reply received
//                      if (reply.getPerformative() == ACLMessage.INFORM) {
//                          // Purchase successful. We can terminate
//                          System.out.println(titolo.toLowerCase()+" è stato venduto con successo da "+reply.getSender().getName());
//                          System.out.println("Prezzo = "+bestPrice+" €");
//                          myAgent.doDelete();
//                      }
//                      else {
//                          System.out.println("Tentativo fallito: il libro richiesto è già stato venduto.");
//                          myAgent.doDelete();
//                      }
//                      step = 4;
//                  }
//                  else
//                      block();
    
    } // end switch
    
//    System.out.println("winner="+winner);
    
    return winner;
  }

   
  
  
//
  
//  private void resolveMWM () throws Excepti//on{
//    LOGGER.entry//()//;
//
//    DFAgentDescription dfd = new DFAgentDescription()//;  
//    ServiceDescription sd = new ServiceDescription//();
//    sd.setType("tmwm//");
//    dfd.addServices(s//d);
//       //   
//    while (true//) {
//      //System.out.print(".//");
//      DFAgentDescription[] result = DFService.search(myAgent,df//d);
//      if ((result != null) && (result.length > 0)//) {
//        dfd = result[0//]; 
//        mwm = dfd.getName().getLocalName//();
//        LOGGER.debug("mwm="+mw//m);
//        bre//ak;
//    //  }
//      System.out.print(".//");
//      Thread.sleep(10//0);
//     //   
//    } //end while (tr//ue)
//    LOGGER.exit//();
//  }
  
  /**
   * Sends a command to a target agent. If sync=true the methods waits for and returns the response.
   * @param cmd
   * @param target
   * @param conversationId
   * @return if sync returns ACLMessage, if asyn returns null
   * @throws FIPAException
   */

  protected String sendCommand(StringBuilder cmd, String target, String conversationId) throws FIPAException {
    LOGGER.entry(cmd, target, conversationId);
    if (!elements.containsKey(target)) return LOGGER.exit("element not found");

    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
    msg.setContent(cmd.toString());
    msg.setOntology("control");
    msg.addReceiver(new AID(target, AID.ISLOCALNAME));
    msg.setReplyWith(cmd.append("#").append(System.currentTimeMillis()).toString());
    msg.setConversationId(conversationId);

    send(msg);
    LOGGER.info(msg);

    if (conversationId!=null) return "threaded#"; //si no hay conversationId

    ACLMessage reply=blockingReceive(MessageTemplate.MatchInReplyTo(msg.getReplyWith()), 1000);

    return LOGGER.exit((reply!=null)? reply.getContent() : "");
  }

 
  

 

 

  public <T> String join(T[] array, String cement) {
    LOGGER.entry(array, cement);
    StringBuilder builder = new StringBuilder();

    if(array == null || array.length == 0) return LOGGER.exit(null);

    for (T t : array)  builder.append(t).append(cement);

    builder.delete(builder.length() - cement.length(), builder.length());

    return LOGGER.exit(builder.toString());
  }





  class ShutdownThread extends Thread {
    private Agent myAgent = null;

    public ShutdownThread(Agent a) {
      super(a);
      //this.myAgent = myAgent;
    }

    public void run() {
      LOGGER.info("Tarea de apagado");
      try {
        DFService.deregister(myAgent);
        myAgent.doDelete();
      } catch (Exception e) {
      }
      // LOGGER.info("Agente borrado");
    }
  }

  private void registerAgent(String localName) {
    LOGGER.entry(localName);
    DFAgentDescription dfd = new DFAgentDescription();
    ServiceDescription sd = new ServiceDescription();

    dfd = new DFAgentDescription();
    sd = new ServiceDescription();
    sd.setType(getLocalName());
    setRState("initialSate");
    // AID aid = getAID();

    sd.setName(getName());
    sd.setOwnership("Ownership");
    dfd.addServices(sd);
    dfd.setName(getAID());
    dfd.addOntologies("ontology");
    dfd.setState("initialSate");
    try {DFService.deregister(this);} catch (Exception e) {} //si está lo deregistro
    try {
      DFService.register(this, dfd);
    } catch (FIPAException e) {
      LOGGER.error(getLocalName() + " no registrado. Motivo: " + e.getMessage());
      doDelete();
    }
    // LOGGER.info(getLocalName()+
    // " registrado correctamente en el DF");
    LOGGER.exit();
  } // end registerAgent

 
  
  private //String report(String element, Hashtable<String, String> attribs, String conversationId)
           String report(String cmd){
    
    LOGGER.entry(cmd);
    //this.sendCommand(cmd, target, conversationId)
    return "done";
  }
  
  
 
  private String negotiate(String targets, String negotiationCriteria, String action, String conversationId){
    LOGGER.entry(targets, negotiationCriteria, action, conversationId);

    //Decirles a los nodos que negocien
    ACLMessage msg = new ACLMessage(ACLMessage.CFP);

    for (String target: targets.split(",")) 
      msg.addReceiver(new AID(target, AID.ISLOCALNAME));
    
    msg.setConversationId(conversationId);
    msg.setOntology("control");
    
    msg.setContent("negotiate "+targets+" negotiationCriteria="+negotiationCriteria+" action="+action);
    LOGGER.debug(msg);
    send(msg);
    
    return LOGGER.exit("threaded#localcmd .* setstate=running");
  }
  
  
  private String negotiateRecovery(String element, String conversationId){
    LOGGER.entry(element, conversationId);

    //Buscar las instancias en traking
    String response = processCmd("getins "+ element+" state=tracking|paused",null); //attrib=node
    String[] cmpIMPs = response.split(",");
    String scmpIMP= response;
    //Pasar instancias a pause
    for (String cmpIMP:cmpIMPs){
      response = processCmd("localcmd "+cmpIMP+" cmd=setstate paused",null);
    }

    //TODO No es necesario negociar si solo hay una instancia en traking

    //Obtener los nodos de la instancias
    String[] nodes = new String[cmpIMPs.length];
    nodes[0] = processCmd("get "+cmpIMPs[0]+" attrib=node",null);
    String snode=nodes[0];

    for (int i=1; i<nodes.length; i++){
      nodes[i] = processCmd("get "+cmpIMPs[i]+" attrib=node",null);
      snode=snode+","+nodes[i];
    }

    //TODO Obtener las condiciones de negociacion
    String condition = processCmd("get "+element+" attrib=negotiation",null); //max freeMem

    //Decirles a los nodos que negocien
    ACLMessage msg = new ACLMessage();

    for (String node: nodes) 
      msg.addReceiver(new AID(node, AID.ISLOCALNAME));
    
    msg.setConversationId(conversationId);
    msg.setOntology("control");
    msg.setContent("negotiate "+element+" nodes="+snode+" condition="+condition +" cmpIMPs="+scmpIMP);
    send(msg);
    return LOGGER.exit("threaded#localcmd .* setstate=running");

  }

  
  
    
  
}



