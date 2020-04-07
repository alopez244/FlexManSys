package es.ehu;

import es.ehu.platform.behaviour.ControlBehaviour;
import es.ehu.platform.utilities.MWMCommand;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.core.behaviours.ThreadedBehaviourFactory.ThreadedBehaviourWrapper;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SystemModelAgent extends Agent {

    private static final long serialVersionUID = 1L;
    static final Logger LOGGER = LogManager.getLogger(SystemModelAgent.class.getName()) ;

    final public ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
    protected DataStore ds = null;

    final protected ConcurrentHashMap<String, SimpleBehaviour> behaviours = new ConcurrentHashMap<String, SimpleBehaviour>();
    final protected ConcurrentHashMap<String, String> threadLog = new ConcurrentHashMap<String, String>();

    public ConcurrentHashMap<String, Object> objects = new ConcurrentHashMap<String, Object>();
    public ConcurrentHashMap<String, Object> executionStates = new ConcurrentHashMap<String, Object>();
    public ConcurrentHashMap<String, Hashtable<String, String>> elements = new ConcurrentHashMap<String, Hashtable<String, String>>();
    public ConcurrentHashMap<String, Integer> count = new ConcurrentHashMap<String, Integer>();

    private int cmdId = 1000;
    public static long startTime = System.currentTimeMillis();

    protected void setup() {
        LOGGER.entry();

        String key = "tmwmb_"+System.currentTimeMillis();
        behaviours.put(key, new TheadedMiddlewareManagerBehaviour(this));

        this.addBehaviour(tbf.wrap(behaviours.get(key)));

        this.processCmd("initialize","");
        LOGGER.debug("this.addBehaviour(tbf.wrap(behaviours.get("+key+")));");
        LOGGER.exit();
    }

    private class TheadedMiddlewareManagerBehaviour extends SimpleBehaviour {

        private static final long serialVersionUID = 7023269912150712219L;
        private MessageTemplate template = null;
        private SystemModelAgent myAgent = null;

        public TheadedMiddlewareManagerBehaviour(Agent a) {
            super(a);
            this.myAgent = (SystemModelAgent) a;
        }

        public void onStart() {
            LOGGER.entry();

            myAgent.ds = this.getDataStore();
            ((SystemModelAgent) myAgent).registerAgent("sa"); //registrar mwm en el df

            //filtro de recepción de mesnajes (state) || (control&REQUEST+CONTROL)
            template = MessageTemplate.or(MessageTemplate.MatchOntology("state"),
                    MessageTemplate.and(MessageTemplate.MatchOntology("control"),
                            MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                                    MessageTemplate.MatchPerformative(ACLMessage.INFORM))));

            LOGGER.debug("template=" + template);
            LOGGER.warn("TMWM Started!");
            LOGGER.exit();
        }

        public void action() {
            LOGGER.entry();

            ACLMessage msg = receive(template);

            if (msg != null) {
                if (msg.getOntology().equals("state")) { //ontologia de estado, recibo un estado de un componente

                    String compon = (elements.containsKey(msg.getSender().getLocalName())) ? getCmp(msg.getSender().getLocalName()) : "";
                    //LOGGER.debug(msg.getSender().getLocalName()+" ("+compon+"): update execState");

                    try {
                        executionStates.put(compon, msg.getContentObject());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else { // ontología de control
                      if (msg.getConversationId() != null) { //si hay id de conversación despertaremos la tarea que interviene en ella
                          LOGGER.trace("***************** Conversation message *******************");
                          if (behaviours.containsKey(msg.getConversationId())) { //si hay una coportamiento con ese id
                              LOGGER.trace("Localizado comportamiento");
                              myAgent.ds.put(msg.getConversationId(), msg); // guardamos el dato en el ds común
                              LOGGER.trace("behaviours.get(" + msg.getConversationId() + ").resume()");
                              myAgent.tbf.getThread(behaviours.get(msg.getConversationId())).resume();
                          } // end existe la tarea
                      } // end llega conversationId

                      LOGGER.trace("***************** New message *******************");
                      LOGGER.trace("received message from " + msg.getSender().getLocalName());
                      LOGGER.trace("msg.getContent()=" + msg.getContent());

                      String key = String.valueOf(cmdId++);
                      ds.put(key, msg);
                      behaviours.put(key, new ThreadedCommandProcessor(key, myAgent));
                      myAgent.addBehaviour(tbf.wrap(behaviours.get(key)));

                } // end ontología de control
            } else {
                block();
            }
            LOGGER.exit();

        } // end MWMBehaviour

        private boolean finished = false;

        public boolean done() {
            LOGGER.entry();
            return LOGGER.exit(finished);
        }
    } // ----------- End myBehaviour

    public String processCmd(String cmd, String conversationId) {
        LOGGER.entry(cmd, conversationId);
        LOGGER.info ("cmd_"+conversationId+" \""+cmd+"\"..." );
        if (conversationId==null) conversationId=String.valueOf(cmdId++);
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

        while (cmd.contains("  ")) cmd=cmd.replace("  ", " ");
        while (cmd.contains(" =")) cmd=cmd.replace(" =", "=");

        String[] cmds = cmd.split(" ");
        Hashtable<String, String> attribs = processAttribs(cmds);

        try {
            if (cmds[0].equals("reg")) result.append(reg(cmds[1], attribs));

            else if (cmds[0].equals("del")) result.append(del(cmds[1]));
            else if (cmds[0].equals("list")) result.append(list((cmds.length>1)?cmds[1]:"design", (cmds.length>2?cmds[2]:"")));
            else if (cmds[0].equals("listXml")) result.append(listXml((cmds.length>1)?cmds[1]:"design", (cmds.length>2?cmds[2]:"")));
            else if (cmds[0].equals("validate")) result.append(validate((cmds.length>1)?cmds[1]:"design", (cmds.length>2?cmds[2]:""), (cmds.length>3?cmds[3]:"")));

            else if (cmds[0].equals("set")) result.append(set(cmds[1], attribs, conversationId));
            else if (cmds[0].equals("get")) result.append(get(cmds[1], attribs, conversationId));
            else if (cmds[0].equals("ext")) result.append(ext(cmds[1], attribs, conversationId)); //TODO Rafael

            else if (cmds[0].equals("localcmd")) result.append(localCmd(cmds[1], attribs, conversationId));
            else if (cmds[0].equals("localneg")) result.append(negotiate(cmds[1], attribs.get("criterion"), attribs.get("action"),attribs.get("externaldata"), conversationId));
                //else if (cmds[0].equals("localneg")) result.append(negotiate(cmds[1], attribs, conversationId));

            else if (cmds[0].equals("sestart")) result.append(seStart(cmds[1], attribs, conversationId)); // threaded#condition

            else if (cmds[0].equals("start")) result.append(start(cmds[1], attribs, conversationId)); // threaded#condition
            else if (cmds[0].equals("stop")) result.append(processCmd("localcmd " + cmds[1] + " cmd=setstate stop", conversationId));
            else if (cmds[0].equals("pause")) result.append(processCmd("localcmd " + cmds[1] + " cmd=setstate paused", conversationId));
            else if (cmds[0].equals("resume")) result.append(processCmd("localcmd " + cmds[1] + " cmd=setstate running", conversationId));
            else if (cmds[0].equals("track")) result.append(processCmd("localcmd " + cmds[1] + " cmd=setstate tracking", conversationId));
            else if (cmds[0].equals("move")) result.append(processCmd("localcmd " + cmds[1] + " cmd=move "+cmds[2], conversationId));

            else if (cmds[0].equals("report")) result.append(report(cmds[1], attribs, conversationId));
            else if (cmds[0].equals("negotiate")) result.append(negotiateRecovery(cmds[1], conversationId));

            else if (cmds[0].equals("setlocal")) { attribs.put("cmd", "set"); result.append(localCmd(cmds[1], attribs, conversationId)); }
            else if (cmds[0].equals("getlocal")) { attribs.put("cmd", "get"); result.append(localCmd(cmds[1], attribs, conversationId)); }
            else if (cmds[0].equals("getins")) result.append(getIns(cmds[1], attribs));
            else if (cmds[0].equals("getcmp")) result.append(getCmp(cmds[1]));
            else if (cmds[0].equals("save")) result.append(save(cmds[1]) + " saved");
            else if (cmds[0].equals("load")) result.append(load(cmds[1]) + " loaded");
            else if (cmds[0].equals("resetTimer")) {this.startTime=System.currentTimeMillis();  result.append("done"); }
            else if (cmds[0].equals("initialize")) result.append(initialize(cmds[0]));

            else if (cmds[0].equals("help")) result.append(help(cmds));
            else result.append("cmd not found:" + cmds[0]);

        } catch (Exception e) {
            LOGGER.error("ERROR:"+e.getLocalizedMessage());
            e.printStackTrace();
        }

        if (conversationId==null) {
            threadLog.put(String.valueOf(cmdId++), ((result.length()>20)?result.substring(0,20)+"...":result) + " < "
                    + ((cmd.length()<=60)?cmd:cmd.substring(0,60)+"...")
                    +" ("+(System.currentTimeMillis()-startTime)+"ms)");
        }

        LOGGER.info ("cmd_"+conversationId+" \""+cmd+"\" > "+result);

        return result.toString();

    }

    private String help (String [] cmds){

        //Detailed help menu of the different commands

        // LOCALCMD
        if ((cmds.length>1) && (cmds[1].equals("localcmd"))) {
            return "localcmd command:\n"
                    + "executes a command in a remote agent:\n"
                    + "\n"
                    + "examples:\n"
                    + "localcmd cmpins101 cmd=move node103    // moves cmpins101 to node103\n"
                    + "localcmd cmpins102 cmd=setstate paused // starts cmpins102 agent transaction to paused state\n"
                    + "localcmd node102 cmd=get freeMem       // gets free memory on node102";
        }

        // GET
        if ((cmds.length>1) && (cmds[1].equals("get"))) {
            return "get command:\n"
                    + "searches elements in database for a parameter:\n"
                    + "\n"
                    + "examples:\n"
                    + "get cmpins* node=node102               // gets component instances running on node102\n"
                    + "get action102 attrib=order             // get the order parameter of element action102";
        }

        // SET
        if ((cmds.length>1) && (cmds[1].equals("set"))) {
            return "set command:\n"
                    + "sets a parameter value for the selected element:\n"
                    + "\n"
                    + "examples:\n"
                    + "set action102 order=5                  // sets value 5 for order parameter in element action102\n"
                    + "\n"
                    + "NOTE that dinamyc parameteres changes only affect the database: state, node,...\n"
                    + "this values must be changed with localcmd: setstate, move,...";
        }

        // GETINS
        if ((cmds.length>1) && (cmds[1].equals("getins"))) {
            return "getins (get instances) command:\n"
                    + "gets the instances for a implementation/component/application/node that meets a filtre:\n"
                    + "\n"
                    + "examples:\n"
                    + "getins applic101 state=running              // returns component instances of applic101 which state is running\n"
                    + "getins compon102 state=tracking|running     // return tracking or running instances of component compon102\n"
                    + "getins compon102 state=tracking attrib=node // return tracking instances of component compon102";
        }

        //General help regarding the commands of the SystemModelAgent

        return "SystemModelAgent Commands general informacion:\n"
                + "\n"
                + "reg element attriblist                 // registers element\n"
                + "del element                            // removes element, allows wildcards\n"
                + "list element                           // shows element attibutes, allows wildcards\n"
                + "list design                            // design view\n"
                + "list infrastructure                    // infrastructure view\n"
                + "\n"
                + "set element attribname=value           // adds/modifies attribute value\n"
                + "get element attrib=attribname          // shows defined attrib value\n"
                + "ext element attrib=attribname          // extend the value of an attribute\n" //TODO Rafael
                + "\n"
                + "getins component state=statevalue      // returns instances of component in the defined state"
                + "getcmp element                        // return the component of the instance/implementation or application"
                + "gac applicId                           // returns application component ids"
                + "\n"
                + "setlocal cmpInsID attribname=value     // modifies local attribute in component instance\n"
                + "getlocal cmpInsID attrib=attribname    // get local attribute in component instance\n"
                + "start element                          // starts a component/application/event/action\n"
                + "kill element                           // stops a component/application/node\n"
                +  "\n"
                + "save filename                          // saves application in file\n"
                + "load filename                          // loads applictaion from file\n"
                + "restart                                // restarts mwm\n"
                + "\n"
                + "insert help + command name for detailed information (example: help localcmd)";
    }

    private String initialize(String arg) {

        Properties prop = new Properties();

        String url = "/resources/sa.properties";

        LOGGER.debug("Properties: "+ getClass().getResource(url).getPath());

        InputStream in = getClass().getResourceAsStream(url);
        try {
            prop.load(in);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        String systemElements = prop2String((String)prop.get("systemElements"));
        LOGGER.info("systemElements:"+systemElements );

        //normaliza xmlsn
        systemElements = systemElements.replace(systemElements.substring(systemElements.indexOf("<xs:schema "), systemElements.indexOf("\">")+2),
                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">");

        String appValidation = prop2String((String)prop.get("appValidation"));
        System.out.println("** buscar:"+appValidation.substring(appValidation.indexOf("<xs:schema "), appValidation.indexOf("\">")+2)+".");
        appValidation = appValidation.replace(appValidation.substring(appValidation.indexOf("<xs:schema "), appValidation.indexOf("\">")+2),
                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">");

        System.out.println("appValidation:"+appValidation );

        try {
            this.processCmd("reg system ID=system name=Sistema xsd="+URLEncoder.encode(prop2String((String)prop.get("systemModel")), "UTF-8"), "" );
            //+ "reg="+URLEncoder.encode(registerXsd, "UTF-8") );
            //+ "sta="+URLEncoder.encode(startableXsd, "UTF-8"));

            System.out.println("************************appvalidation**:"+prop2String((String)prop.get("appValidation")));

            this.processCmd("reg registering ID=registering name=registering xsd="+URLEncoder.encode(appValidation, "UTF-8"), "");
            this.processCmd("reg concepts ID=concepts name=concepts xsd="+URLEncoder.encode(systemElements, "UTF-8"), "");
        } catch (Exception e) {e.printStackTrace();}

        return "done";
    }

    private String prop2String(String file){
        String output = "";
        try {
            output = IOUtils.toString(this.getClass().getResourceAsStream("/resources/"+file), "UTF-8");
        } catch (IOException e1) {e1.printStackTrace();}
        return output.substring(output.indexOf("<"));
    }

    private String save(String prm) {
        String conf = "conf101";
        if (prm != null)
            conf = prm;
        saveObject(new ConcurrentHashMap[] { count, objects, elements }, conf + ".dat");
        return conf;
    }

    private String load(String prm) {
        String conf = "conf101";
        if (prm != null) conf = prm;
        ConcurrentHashMap[] ht = (ConcurrentHashMap[]) loadObject(conf + ".dat");
        count = ht[0];
        objects = ht[1];
        elements = ht[2];
        return conf;
    }

    public Hashtable<String, String> processAttribs(String... cmdLine){
        LOGGER.entry((Object[])cmdLine);

        if (cmdLine.length < 3) return null; //no hay atributos

        Hashtable<String, String> attribs = new Hashtable<String, String>();
        String attrib = "attrib";

        for (int i = 2; i < cmdLine.length; i++) {
            if (cmdLine[i].contains("=")) { // encuentro otro atributo
                String[] attribDef = cmdLine[i].split("=");
                attrib = attribDef[0];

                attribs.put(attrib, (attribDef.length>1)?attribDef[1]:""); // puede estar vacío
            } else attribs.put(attrib, attribs.get(attrib) + " " + cmdLine[i]);
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
        for (String key : elements.keySet()) {
            for (String key2 : elements.get(key).keySet()) {
                if (key2.equals("parent") && elements.get(key).get(key2).equals(parent)) {
                    response.append(prefijo + list(key)).append("\n");
                    response.append(getChildren(key, prefijo + "\t"));
                } else if (key2.equals("node") && elements.get(key).get(key2).equals(parent)) {
                    response.append(prefijo + list(key)).append("\n");
                    response.append(getChildren(key, prefijo + "\t"));
                }
            }
        }
        return LOGGER.exit(response.toString());
    }

    private Document listDom (String _prm ) throws Exception {
        return listDom (_prm, true);
    }

    private Document listDom(String _prm, boolean mostrarID) throws Exception{

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        for (String element: elements.keySet()) {
            LOGGER.debug("recorriendo "+element);
            if (element.matches(_prm)) {
                LOGGER.debug("*****"+elements.get(element).get("category"));

                // Genero elememto raiz del DOM
                Element rootElement = doc.createElement(elements.get(element).get("category"));
                doc.appendChild(rootElement);

                Attr attr = null;

                if (mostrarID) {
                    attr = doc.createAttribute("ID");
                    attr.setValue(element);
                    rootElement.setAttributeNode(attr);
                }

                //añado atributos al raiz
                forKeys: for (String key : elements.get(element).keySet()) {
                    if (key.equals("category") || key.equals("parent") || key.equals("xsd")|| key.equals("seParent")) continue forKeys;
                    attr =  doc.createAttribute(key);
                    attr.setValue(elements.get(element).get(key));
                    rootElement.setAttributeNode(attr);
                }

                appendChildren(doc, rootElement, element, mostrarID);

            }
        } // end for
        return doc;
    }

    private void appendChildren(Document doc, Element parent, String parentID, boolean mostrarID) {
        LOGGER.entry(parent, parentID);

        for (String key : elements.keySet()) {
            for (String key2 : elements.get(key).keySet()) {
                if (key2.equals("parent") && elements.get(key).get(key2).equals(parentID)) {

                    Element hijo = doc.createElement(elements.get(key).get("category"));
                    parent.appendChild(hijo);

                    boolean ocutarIDHastaCambiarXSD = elements.get(key).get("category").startsWith("restriction");

                    //TODO Aintzane: Ampliar ID a restriction en AppValidation

                    if (mostrarID && !ocutarIDHastaCambiarXSD)  {
                        Attr attr = doc.createAttribute("ID");
                        attr.setValue(key);
                        hijo.setAttributeNode(attr);
                    }

                    forKeys: for (String eachKey : elements.get(key).keySet()) {
                        if (eachKey.equals("category") || eachKey.equals("parent") || eachKey.equals("xsd")) continue forKeys;
                        Attr attr =  doc.createAttribute(eachKey);
                        attr.setValue(elements.get(key).get(eachKey));
                        hijo.setAttributeNode(attr);

                    }

                    //llamada recursiva para generar todo el arbol
                    appendChildren(doc, hijo, key, mostrarID);
                }
            }
        }

        return ;//LOGGER.exit(response.toString());
    }

    public void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }

    private String validate(String... _prm) throws Exception {

        String output = "valid";

        if ((_prm[0].equals("systemElement")) || (_prm[0].equals("appValidation"))){ //comprueba tconcepts.xsd

            boolean appValidation = _prm[0].equals("appValidation");

            Document document = listDom(_prm[1], appValidation); //segundo parámetro indica al DOM si arrastrar los IDs

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            Source schemaFile = new StreamSource(new StringReader(URLDecoder.decode(elements.get(appValidation?"registering":"concepts").get("xsd"), "UTF-8")));
            Schema schema = schemaFactory.newSchema(schemaFile);

            // create a Validator instance, which can be used to validate an instance document
            Validator validator = schema.newValidator();
            // validate the DOM tree
            try {
                validator.validate(new DOMSource(document));
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(new PrintWriter(sw));
                output = sw.toString(); // stack trace as a string
                //LOGGER.info(output);
            }

            // hay error, muestro la descripción
            if (output.indexOf(';')>0) output = output.substring(output.indexOf(";") + 2, output.indexOf('\n'));
            LOGGER.info(output);

        } else if (_prm[0].equals("hierarchy")) { //comprueba jerarquía
            String padre = _prm[2];
            String hijo = _prm[1];

            LOGGER.debug("comprobar jerarquía hijo="+hijo+" padre="+padre);

            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(URLDecoder.decode(elements.get("system").get("xsd"), "UTF-8")));
            //LOGGER.debug("carga "+_prm[0]+".xsd");
            //LOGGER.debug(URLDecoder.decode(elements.get(_prm[0]).get("xsd"), "UTF-8"));
            Document doc = db.parse(is);

            Vector<String> v = null;
            Vector<String> vt = null;
            NodeList nl = doc.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) nl.item(i);
                    v=nodeListToVector(el, new Vector<String>(), "name");
                    vt=nodeListToVector(el, new Vector<String>(), "type");
                    break;
                }
            }

            //Algoritmo ELI

            // 1. se buscaría el xs:element que tenga como atributo name y/o atributo ref el valor deseado. IR A PASO 2
            LOGGER.info("1. se buscaría el xs:element que tenga como atributo name y/o atributo ref el valor deseado. IR A PASO 2");

            boolean encontrado=false;
            int posicion = 0;
            for (int i=0; i<v.size(); i++) {
                if (v.get(i).equals("xs:element="+hijo)) {
                    encontrado = true;
                    posicion=i;
                    break;
                }
            }
            if (encontrado) { // IR A PASO 2
                LOGGER.info("encontrado pos="+posicion);
                // 2. de la lista que salga del paso 1, nos quedamos únicamente con el caso en el que su padre sea  xs:sequence. IR A PASO 3
                LOGGER.info("2. de la lista que salga del paso 1, nos quedamos únicamente con el caso en el que su padre sea  xs:sequence. IR A PASO 3");

                if (v.get(posicion-1).equals("xs:sequence=")){ //el anterior es sequence, IR A PASO 3
                    LOGGER.info("el anterior es xs:sequence");
                    LOGGER.info("3. En tal caso, se busca el predecesor más cercano que sea xs:element.");
                    encontrado=false;
                    for (int i=posicion-2; i>=0; i--) {
                        if (v.get(i).startsWith("xs:element")) {
                            encontrado=true;
                            LOGGER.info("encontrado. El padre ("+padre+") debería ser "+v.get(i).substring(v.get(i).indexOf("=")+1)+".");
                            if (padre.equals(v.get(i).substring(v.get(i).indexOf("=")+1))) return "valid";
                            break;
                        }
                    }

                    if (!encontrado) {
                        LOGGER.info("no encontrado ningún xs:element. Se buscará el xs:complexType predecesor más cercano y quedarnos con el valor de su atributo name.  IR A PASO 4");
                        String complexTypeName="";
                        for (int i=posicion-2; i>=0; i--) {
                            if (v.get(i).startsWith("xs:complexType")) {
                                encontrado=true;
                                complexTypeName=v.get(i).substring(v.get(i).indexOf("=")+1);
                                LOGGER.info("complexType encontrado "+complexTypeName+".");
                                break;
                            }
                        }

                        if (!encontrado) LOGGER.info("complexType (" + complexTypeName + ") NO encontrado ¿? .");

                        LOGGER.info("Buscar en el XML schema un xs:element que tenga como atributo type el valor que hemos guardado en el PASO 3 ("+complexTypeName+")");
                        encontrado=false;

                        for (int i=0; i<vt.size(); i++) {
                            if (vt.get(i).equals("xs:element="+complexTypeName)) {
                                encontrado=true;
                                LOGGER.info("padre ("+padre+") debería ser "+v.get(i).substring(v.get(i).indexOf("=")+1)+".");
                                if (padre.equals(v.get(i).substring(v.get(i).indexOf("=")+1))) return "valid";
                                break;
                            }
                        }
                        if (!encontrado) LOGGER.info("no encontrado");
                        return "ERROR de jerarquía";

                    }

                }

            } else { //no lo he encontrado
                LOGGER.info("no encontrado");
                return "ERROR de jerarquía";
            }

        }

        return output;
    }

    private Vector<String> nodeListToVector(Element element, Vector<String> v, String attrib) {
        v.add(element.getNodeName() + "=" +element.getAttribute(attrib));
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) nodeListToVector((Element) nl.item(i), v, attrib);
        }
        return v;
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
            for (String key : elements.keySet()) {
                if (key.matches(comienzo)) {
                    response.append(list(key)).append("\n");
                    response.append(getChildren(key, "\t"));
                }
            }
        } else if (_prm.startsWith("infr")) { // infrastructure
            for (String key : elements.keySet()) {
                if (key.startsWith("syst")) {
                    response.append(list(key)).append("\n");
                    for (String nodeKey : elements.keySet()) {
                        if (nodeKey.startsWith("node")) { // el nodo pertenece al sistema que pintamos
                            if (elements.get(nodeKey).get("parent").equals(key)) {
                                response.append("\t").append(list(nodeKey)).append("\n");
                            }
                        }
                    }
                }
            }
        } else if (_prm.startsWith("runt")) { // runtime
            response.append("not implemented"); // TODO: runtime list
        } else if (_prm.equals("objects")) {  //objects
            for (String key : objects.keySet()) {
                response.append(key).append(": ").append(objects.get(key).getClass()).append("\n");
            }
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
                    elements: for (String key : elements.get(element).keySet()) {
                        if (key.equals("seParent")) continue elements;
                        if (key.equals("xsd")) { response.append("xsd=file "); continue elements;}
                        response.append(key).append("=").append(elements.get(element).get(key)).append(" ");
                    }
                    response.append("\n");
                }
            } // end for

        if (response.length()>0) {
            while (response.charAt(response.length()-1)=='\n') response.deleteCharAt(response.length() - 1);
        } else response.append("not found!");

        return LOGGER.exit(response.toString());
    }

    private String negotiate (String prm, Hashtable<String, String> attribs, String conversationId){

        LOGGER.info("condition="+attribs.get("condition"));
        LOGGER.info("ids="+attribs.get("ids"));
        String[] lids = attribs.get("ids").split(",");
        String winner = null;

        long bestValue=(prm.equals("max")?Long.MIN_VALUE:Long.MAX_VALUE);

        int step=0;
        int repliesCnt=0;
        MessageTemplate mt = null;
        while (step<2) {
            switch (step) {
                case 0:
                    // Send the cfp to all sellers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP); //Call For Proposals
                    for (String id: lids) cfp.addReceiver(new AID(id, AID.ISLOCALNAME));
                    cfp.setContent(attribs.get("condition"));
                    cfp.setOntology("control");
                    cfp.setConversationId(conversationId);
                    cfp.setReplyWith("cfp_"+System.currentTimeMillis()); // Unique value
                    this.send(cfp);
                    // Prepare the template to get proposals
                    mt = MessageTemplate.MatchInReplyTo(cfp.getReplyWith());
                    step = 1;
                    break;
                case 1:
                    // Receive all proposals/refusals from seller agents
                    ACLMessage reply = receive(mt);
                    // esperar a mensaje
                    if (reply != null) {
                        // Reply received
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // This is an offer
                            long value = Long.parseLong(reply.getContent());
                            LOGGER.debug(reply.getSender().getLocalName()+" PROPOSE "+value);
                            value=(prm.equals("max")?value:-value);
                            if (winner == null || value > bestValue) {
                                // This is the best offer at present
                                bestValue = value;
                                winner = reply.getSender().getLocalName();
                            }
                        } else if (reply.getPerformative() == ACLMessage.FAILURE) {
                            String name=reply.getContent().substring(reply.getContent().indexOf(":name ", reply.getContent().indexOf("MTS-error"))+":name ".length());
                            name=name.substring(0, name.indexOf('@'));
                            LOGGER.debug(name+" FAILURE");

                        }

                        repliesCnt++;
                        if (repliesCnt >= lids.length)  // We received all replies
                            step = 2;
                    }
            } // end switch
        }

        return winner;
    }

    public String localCmd(String id, Hashtable<String, String> attribs) throws Exception {
        LOGGER.entry(id);

        if (id.endsWith("*")) {
            StringBuffer result = new StringBuffer();
            boolean necesitaSeparador = false;
            id=id.substring(0,id.indexOf("*"));
            for (String element: elements.keySet()) {
                if (element.startsWith(id)) {
                    localCmd(element, attribs);
                    if (necesitaSeparador) result.append(',');
                    else necesitaSeparador=true;
                    result.append(element);
                }
            }
            return LOGGER.exit(result.append(" killed").toString());
        } else if (id.startsWith("applic")) return LOGGER.exit(appLocalCmd(id, attribs));
        else if (id.startsWith("compon")) return LOGGER.exit(cmpLocalCmd(id, attribs));
        else if (id.startsWith("cmpins")) return LOGGER.exit(cmpInsLocalCmd(id, attribs));
        return LOGGER.exit(id + " not found");
    }

    public String appLocalCmd(String appID, Hashtable<String, String> attribs) throws Exception {
        LOGGER.entry(appID);
        if (!elements.containsKey(appID)) return LOGGER.exit(appID + "not found");

        // inicia todos los componentes que forman parte de la aplicación
        StringBuilder result = new StringBuilder();
        String[] appComponents = getCmp(appID).split(",");

        for (String cmpID : appComponents) result.append(cmpLocalCmd(cmpID, attribs)).append(",");

        result.deleteCharAt(result.length()-1);
        return LOGGER.exit(result.toString().substring(0, result.length()));
    }

    private String cmpLocalCmd (String cmpID, Hashtable<String, String> attribs) throws Exception {
        LOGGER.entry(cmpID);
        if (!elements.containsKey(cmpID)) return LOGGER.exit(cmpID + "not found");

        String sTarget=getIns(cmpID, null);
        String[] cmpinss = sTarget.split(",");
        sTarget=cmpID+"("+sTarget+")";

        String key = "killComponent_"+System.currentTimeMillis();
        behaviours.put(key, new MWMCommand(this, attribs, cmpinss));

        addBehaviour(tbf.wrap(behaviours.get(key)));

        return LOGGER.exit("sent");
    }

    private String cmpInsLocalCmd (String cmpins, Hashtable<String, String> attribs) throws Exception {
        LOGGER.entry();
        if (!elements.containsKey(cmpins)) return LOGGER.exit(cmpins + "not found");

        String key = "killInstance_"+System.currentTimeMillis();
        behaviours.put(key, new MWMCommand(this, attribs, cmpins));

        addBehaviour(tbf.wrap(behaviours.get(key)));

        return LOGGER.exit("sent");
    }

    public String seStart(String seID, Hashtable<String, String> attribs, String conversationId ) {
        LOGGER.entry(seID, attribs, conversationId);

        if (seID.indexOf(",")>0) {
            String[] tokens = seID.split(",");
            for (int j=0; j<tokens.length; j++) seStart(tokens[j], attribs, conversationId);
            return "";
        }

        String redundancy = "1";
        if ((attribs!=null) && (attribs.containsKey("redundancy")))
            redundancy = attribs.get("redundancy");

        // si no existe el id en el registro devuelve error (irá al launcher)
        if (!elements.containsKey(seID)) return "-1";

        //Calcular quiénes negocian:
        // leer del registro para este "seID" la lista entera de procNodes cada uno con sus refServId. - leo del registro los serviceid que requiere el seID
        // buscar procnodes que tengan estas refServId > lista de los procNodes negociadores
        // si no hay negociarres return "-4"; //TODO
        String refServID=processCmd("get (get * parent=(get * parent="+seID+" category=restrictionList)) attrib=attribValue", conversationId);
        //si hay restricción la añado al filtro, en caso contrario solo busco procNode
        String targets = processCmd("get * category=procNode"+((refServID.length()>0)?" refServID="+refServID:""), conversationId);
        if (targets.length()<=0) return "-4";

        String seCategory = processCmd("get "+seID+" attrib=category", conversationId);
        String seClass = "es.ehu.domain.manufacturing.agents.MPlanAgent";//processCmd("get "+seID+" attrib=class", conversationId);

        //mando negociar a todos
        for (int i=0; i<Integer.parseInt(redundancy); i++) {

            String neg = processCmd("localneg "+targets+" action=start "+seID+" criterion=max mem externaldata="+seID+","+seCategory+","+seClass+","+((i==0)?"running":"tracking"), conversationId);

            LOGGER.exit();
        }

        return "";

    }

    public String start(final String element, final Hashtable<String, String> attribs, final String conversationId) {
        LOGGER.entry(element, conversationId);

        if (element.startsWith("avail")) return startAvailabilityManager();

        //if (element.equals("cmpins")) return LOGGER.exit(startNewInstance(element, attribs, conversationId));
        if (!elements.containsKey(element) && !element.equals("cmpins")) {

            StringBuilder result = new StringBuilder();
            for (String candidato: elements.keySet())
                if (candidato.matches(element.replace("*", ".*")))
                    result.append(candidato).append(" ").append(start(candidato, attribs, conversationId));
            return LOGGER.exit((result.length()>0)? result.toString() : element + " not found" );

        }

        if (element.startsWith("cmpins")) return LOGGER.exit("threaded#"+startInstance(element, attribs, conversationId));

        return LOGGER.exit(element + " no runnable element");
    }

    public String startInstance(String cmpins, final Hashtable<String, String> attribs, final String conversationId) {
        LOGGER.entry(cmpins, attribs, conversationId);

        if (objects.containsKey(cmpins)) return LOGGER.exit(cmpins + " already instantiated");
        if (cmpins.equals("cmpins")) {
            final String cmpimp = processCmd("get cmpimp* parent="+attribs.get("compon"), conversationId).split(",")[0];
            cmpins = reg("cmpins", new Hashtable<String, String>() {{put("parent", cmpimp);}});
        }

        String compon = getCmp(cmpins);
        String cmpimp = elements.get(cmpins).get("parent");

        StringBuilder nodes = new StringBuilder();
        nodes.append((attribs!=null && attribs.containsKey("node")) ? attribs.get("node")+"," : "")
                .append((elements.get(compon).containsKey("defaultNode")) ? elements.get(compon).get("defaultNode")+"," : "") // si node por defecto en la definición
                .append((elements.get(compon).containsKey("node")) ? elements.get(compon).get("node")+"," : "")
                .append(get("node*", null,conversationId)); // cualquier node

        LOGGER.debug("nodes="+nodes);
        String node= "";
        forTestNode: for (String testNode : nodes.toString().split(",")) {
            String containsCompon = processCmd("getins "+compon+" node="+testNode, conversationId);
            if ((containsCompon.isEmpty() || containsCompon.equals("null")) && elements.containsKey(testNode)) {
                node = testNode;
                break forTestNode;
            }
        }

        LOGGER.debug("node = " + node);
        String defaultNode =  (attribs!=null && attribs.containsKey("defaultNode")) ? attribs.get("defaultNode"): // si node forzado
                (elements.get(compon).containsKey("defaultNode")) ? elements.get(compon).get("defaultNode"): ""; // si no en modelo
        LOGGER.debug("defaultNode = " + defaultNode);

        String negotiation = (attribs!=null && attribs.containsKey("negotiation")) ? attribs.get("negotiation"): // si negotiation forzado
                (elements.get(compon).containsKey("negotiation")) ? elements.get(compon).get("negotiation") : "";
        LOGGER.debug("negotiation = " + negotiation);

        String sInitState = (attribs!=null && attribs.containsKey("initState")) ? attribs.get("initState"): // si initState forzado
                (processCmd("getins "+compon+" state=running|boot", conversationId).isEmpty()) ? "running" : "tracking"; //si no hay instancias en running

        Integer initialFSMState = (sInitState.equals("running"))? ControlBehaviour.RUNNING:
                (sInitState.equals("tracking"))? ControlBehaviour.TRACKING:
                        ControlBehaviour.RUNNING;

        Integer period = (attribs!=null && attribs.containsKey("period")) ? Integer.parseInt(attribs.get("period")) : // si period forzado, si no lo hereda
                (elements.get(compon).containsKey("period"))? Integer.parseInt(elements.get(compon).get("period")) :
                        -1;

        Object executionState = (executionStates.containsKey(compon))?executionStates.get(compon):null;

        LOGGER.debug("Info de entrada cmpins: "+cmpins+" nodo: "+attribs.get("node")+" estado inicial: "+attribs.get("initState"));
        LOGGER.debug("Info de enviada agente cmpins: "+cmpins+" nodo: "+node+" estado inicial: "+initialFSMState);

        AgentController ac = null;
        try {

            if (elements.get(compon).containsKey("sourceComponentIDs") || elements.get(compon).containsKey("targetComponentIDs")) {
                LOGGER.info("*********** arranca con source y target");
                ac = ((AgentController) getContainerController().createNewAgent(cmpins, elements.get(cmpimp).get("class"),
                        new Object[] { getCmp(cmpins), node, initialFSMState, period, executionState, conversationId,
                                (elements.get(compon).containsKey("sourceComponentIDs")? new String[]{elements.get(compon).get("sourceComponentIDs")}: new String[]{}),
                                (elements.get(compon).containsKey("targetComponentIDs")? new String[]{elements.get(compon).get("targetComponentIDs")}: new String[]{})
                        }));

                LOGGER.debug("getContainerController().createNewAgent("+cmpins+", "+elements.get(cmpimp).get("class")+
                        ", new Object[] { "+getCmp(cmpins)+", "+node+", "+initialFSMState+", "+period+", "+
                        executionState+", "+conversationId + ", "+elements.get(compon).get("sourceComponentIDs")+
                        ", "+elements.get(compon).get("targetComponentIDs")+"})");

            } else {
                LOGGER.info("*********** arranca SIN source y target");
                ac = ((AgentController) getContainerController().createNewAgent(cmpins, elements.get(cmpimp).get("class"),
                        new Object[] { getCmp(cmpins), node, initialFSMState, period, executionState, conversationId }));
                LOGGER.debug("getContainerController().createNewAgent("+cmpins+", "+elements.get(cmpimp).get("class")+
                        ", new Object[] { "+getCmp(cmpins)+", "+node+", "+initialFSMState+", "+period+", "+
                        executionState+", "+conversationId +"})");
            }

            ac.start();
        } catch (StaleProxyException e) {
            LOGGER.warn(e.getLocalizedMessage());
            e.printStackTrace();
        }
        objects.put(cmpins, ac);

        return LOGGER.exit(cmpins + " started");

    }

    public String startAvailabilityManager(//String appID, String conversationId) throws Exception
    ) {
        LOGGER.entry();

        StringBuilder response = new StringBuilder();

        Hashtable<String, String> param = new Hashtable<String, String>();
        String avaManId = reg("avaMan", param);

        AgentController ac = null;

        try {
            ac = ((AgentController) getContainerController().createNewAgent(avaManId, "es.ehu.AvailabilityManager", new String[] { "sysID=sys001" }));
            ac.start();
        } catch (Exception e) {e.printStackTrace();}

        this.objects.put(avaManId, ac);
        response.append("started ").append(avaManId);

        return LOGGER.exit(response.toString());

    }

    public String startApplicationManager(String appID, String conversationId) throws Exception {
        LOGGER.entry(appID);

        StringBuilder response = new StringBuilder();
        if (!elements.containsKey(appID)) LOGGER.exit(response.append(appID).append(" not found"));

        Hashtable<String, String> param = new Hashtable<String, String>();
        param.put("parent", appID);
        String appManId = reg("appMan", param);
        AgentController ac = ((AgentController) getContainerController().createNewAgent(appManId, "es.ehu.ApplicationManager", new String[] { "appID="+appID }));
        ac.start();
        this.objects.put(appManId, ac);
        response.append("started ").append(appManId);

        return LOGGER.exit(response.toString());

    }

    public String startEventManager(String eventID, String conversationId) throws Exception {
        LOGGER.entry(eventID, conversationId);
        if (!elements.containsKey(eventID)) LOGGER.exit(eventID + " not found");

        StringBuilder response = new StringBuilder();

        Hashtable<String, String> param = new Hashtable<String, String>();
        param.put("parent", eventID);
        String eventManId = reg("eventManager", param);
        AgentController ac = ((AgentController) getContainerController().createNewAgent(eventID, "es.ehu.EventManager", new String[] { "eventID="+eventID }));
        ac.start();
        this.objects.put(eventManId, ac);
        response.append("started ").append(eventManId);

        return LOGGER.exit(response.toString());

    }

    private String set(String prm, Hashtable<String, String> attribs, String conversationId) {
        LOGGER.entry(prm, attribs);

        if (!elements.containsKey(prm)) return LOGGER.exit("element not found");

        for (String attrib : attribs.keySet()) {

            // si una instancia de un componente pasa a running, la que esté en running pasa a failure -- TODO esto habría que comprobarlo
            if (prm.startsWith("cmpins") && attrib.equals("state") && attribs.get(attrib).equals("running")) {
                //String cmpid = processCmd("getcmp "+prm, conversationId);

                if (processCmd("get (getcmp "+prm+") attrib=isFirst", conversationId).equals("true")) { // es primero
                    processCmd("set (get (getcmp "+prm+") attrib=parent) state=running", conversationId); //aplicación pasa a running
                }

                String runningInstance = processCmd("getins (getcmp "+prm+") state=running", conversationId);
                if (runningInstance.length()>0) processCmd("set "+runningInstance+" state=failure", conversationId);

            }

            //actualizo el actual
            //si el atributo va en blanco y existe lo borro

            if ((attribs.get(attrib).isEmpty()) && (elements.get(prm).containsKey(attrib)))  elements.get(prm).remove(attrib);
            else elements.get(prm).put(attrib, attribs.get(attrib));

            // TODO: refrescar cache local de los componentes asociados al que cambia de estado
            //        if ( attrib.equals("state")) { // Una instancia de componente cambia de estado

        }

        return LOGGER.exit("done");
    }

    private String localCmd(String target, Hashtable<String, String> attribs, String conversationId) throws Exception {
        LOGGER.entry(target, attribs, conversationId);

        if (target.contains(",")) {
            StringBuilder result = new StringBuilder();
            for (String targ: target.split(","))
                result.append(localCmd(targ, attribs, conversationId)).append(",");
            return LOGGER.exit(result.toString());
        }

        if (!elements.containsKey(target)) return LOGGER.exit("element not found");

        if (target.startsWith("cmpins")) { //es un ComponentInstance
            StringBuilder cmd = new StringBuilder();
            for (String key: new String[] {"cmd", "command", "prm", "value"}) {
                if (attribs.containsKey(key)) {
                    cmd.append(attribs.get(key)).append(" ");
                }
            }
            if (cmd.length()>0) cmd.deleteCharAt(cmd.length()-1);
            return LOGGER.exit(sendCommand(cmd, target, conversationId));

        } else if (target.startsWith("compon") || target.startsWith("applic")|| target.startsWith("node") ) { //es un Component
            StringBuilder result = new StringBuilder();
            for (String cmpins: getIns(target, null).split(","))
                result.append(localCmd(cmpins, attribs, conversationId)).append(",");
            return LOGGER.exit(result.toString());
        }

        return LOGGER.exit("no commandable element");
    }

    public String get(String prms, Hashtable<String, String> filters, String conversationId) {
        LOGGER.entry(prms, filters);

        StringBuffer result = new StringBuffer();
        boolean necesitaSeparador=false;

        if (prms.contains("*")) {
            prms=prms.replace("*", ".*");
            for (String element: elements.keySet()) {
                if (element.matches(prms)) {
                    String param = get(element, filters, conversationId);
                    if (!necesitaSeparador) necesitaSeparador=true;
                    else if (!param.isEmpty() && result.length()>0) result.append(",");
                    result.append(param);
                }
            }
        } else
            forPrm: for (String prm: prms.split(",")) {
                // el elemento existe y contiene el atributo que se pide o estamos pidiendo el ID

                if (!elements.containsKey(prm)) continue forPrm; // si no existe el parámetro

                if (filters!=null && filters.size()>0) {
                    if (filters.containsKey("attrib") && !elements.get(prm).containsKey(filters.get("attrib"))) continue forPrm; //si pedimos un attributo específico y el elemento NO lo contiene
                    forFilter: for (String filter : filters.keySet()) {
                        if (filter.equals("attrib")) continue forFilter;//si es attrib nos lo saltamos el atributo
                        if (!elements.get(prm).containsKey(filter)) continue forPrm; //si el candidato no contiene el parámetro del filtro descartamos el candidato
                        if (!elements.get(prm).get(filter).matches(filters.get(filter))) continue forPrm; // si el valor del filtro no coincide descartamos el candidato
                    } // end forKey
                }

                if (!necesitaSeparador) necesitaSeparador=true; else result.append(",");
                result.append((filters==null || !filters.containsKey("attrib"))? prm : elements.get(prm).get(filters.get("attrib")));

            }

        return LOGGER.exit(result.toString());
    }

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

    /**
     * Esta función devuelve el componente o componentes asociados a una instancia/implementación o aplicación
     * @param id id del elemento a procesar: puede ser aplicación, implementación o instancia
     * @return
     */

    public String getCmp (final String id) {
        LOGGER.entry(id);
        if (!elements.containsKey(id)) return LOGGER.exit(id+" not found");

        if (id.startsWith("cmpins") && elements.get(id).containsKey("parent")) return getCmp(elements.get(id).get("parent"));
        if (id.startsWith("cmpimp") && elements.get(id).containsKey("parent")) return getCmp(elements.get(id).get("parent"));
        if (id.startsWith("compon")) return LOGGER.exit(id);
        if (id.startsWith("applic")) {
            StringBuilder result = new StringBuilder();
            for (String element: elements.keySet()) {
                if (elements.get(element).containsKey("parent") && elements.get(element).get("parent").equals(id))
                    result.append(getCmp(element)).append(",");
            }

            return LOGGER.exit((result.length()>0) ? result.deleteCharAt(result.length()-1).toString() : result.toString()); //si termina en "," se la quito
        }
        return LOGGER.exit("not found");
    }

    private String getIns (final String id, final Hashtable<String, String> attribs) {
        LOGGER.entry(id, attribs);
        StringBuilder result = new StringBuilder();
        if (id.startsWith("cmpins")) { //es una instancia compruebo que cumpla filtro
            if (attribs!=null) forAttrib: for (String attrib: attribs.keySet()) {
                if (attrib.equals("attrib") || attrib.equals("limit") || attrib.equals("ID!")) continue forAttrib; // atributo especial para cuando requiramos como salida un atributo como el nodo, no se trata como filtro
                if (!elements.get(id).containsKey(attrib)) return null; // el elemento no contiene el filtro
                if (!elements.get(id).get(attrib).matches(attribs.get(attrib))) return null; // los filtros no coinciden
            } // end forAttrib;

            if (attribs!=null && attribs.containsKey("ID!")) {  //si estamos pidiendo en lugar de la id de la instancia un atributo (por ejemplo el nodo)
                if (id.equals(attribs.containsKey("ID"))) return null;
            }
            if (attribs!=null && attribs.containsKey("attrib")) {  //si estamos pidiendo en lugar de la id de la instancia un atributo (por ejemplo el nodo)
                if (!elements.get(id).containsKey(attribs.get("attrib"))) return null; // el elemento no contine el atributo que buscamos
                result.append(elements.get(id).get(attribs.get("attrib")));
            } else result.append(id);

            if (attribs!=null && attribs.containsKey("limit")) {// si hay límite lo descontamos
                if (Integer.parseInt(attribs.get("limit"))<=0) return null;
                attribs.put("limit", String.valueOf(Integer.parseInt(attribs.get("limit"))-1));
            }

            return result.toString();
        } else if (id.startsWith("cmpimp") || id.startsWith("node") || id.startsWith("compon") || id.startsWith("applic")) {
            String relationTag =  id.startsWith("node")? "node" : "parent"; //si es un node se indica la ubicación con "node" en caso contrario con "parent"
            String child = (id.startsWith("applic"))? "compon" : (id.startsWith("compon"))? "cmpimp" : "cmpins";
            forElement: for (String element: elements.keySet()) {
                if (!elements.get(element).containsKey(relationTag)) continue forElement; //el cmpins no contiene parent
                if (!elements.get(element).get(relationTag).equals(id)) continue forElement; // el parent del cmpisn no es el cmpimp
                if (!element.startsWith(child)) continue forElement; // el cmpins no empieza pon cmpins
                String ins = getIns(element, attribs);
                if (ins!=null) result.append(ins).append(",");
            } // end forElement

            return LOGGER.exit((result.length()>0) ? result.deleteCharAt(result.length()-1).toString() : ""); //si termina en "," se la quito
        }

        return LOGGER.exit("type not found");
    }

    private String reg(final String prm, Hashtable<String, String> attribs) {
        LOGGER.entry(prm, attribs);
        String type = prm;
        String id = "";
        if (!attribs.containsKey("ID")) {
            // si no llega un id lo genero

            id = (prm.length() > 6)? prm.substring(0, 6): prm;
            id = id.toLowerCase();
            if (!count.containsKey(id)) count.put(id, 101);
            else count.put(id, (count.get(id)) + 1);
            id = id + count.get(id);
        } else { // si llega lo guardo
            id = attribs.get("ID");
            attribs.remove("ID");
        }
        attribs.put("category", type);
        elements.put(id, attribs);

        //TODO Rafael: cuando es un registro de nodo re laszar los componentes asociados a ese nodo de las aplicaciones activas
        //TODO convertirlo en un thread
        //if(prm.equals("node")) recoverNode(id,"prueba TODO");

        return LOGGER.exit(id);
    }

    private String del(String prm) {
        LOGGER.entry(prm);
        String response = "not found";
        if (prm.equals("*")) {
            response = elements.size() + " deleted";
            elements.clear();

        } else if (elements.get(prm) != null) {
            elements.remove(prm);

            response = "done";
        }
        return LOGGER.exit(response);
    }

    public <T> String join(T[] array, String cement) {
        LOGGER.entry(array, cement);
        StringBuilder builder = new StringBuilder();

        if(array == null || array.length == 0) return LOGGER.exit(null);

        for (T t : array)  builder.append(t).append(cement);

        builder.delete(builder.length() - cement.length(), builder.length());

        return LOGGER.exit(builder.toString());
    }

    public void saveObject(Serializable object, String file) {
        LOGGER.entry(object, file);
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(object);
            out.flush();
            out.close();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.exit();
    }

    public Object loadObject(String file) {
        LOGGER.entry(file);
        Object result = null;
        try {
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            result = in.readObject();
            in.close();
            fileIn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return LOGGER.exit(result);
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

        sd.setName(getName());
        sd.setOwnership("Ownership");
        dfd.addServices(sd);
        dfd.setName(getAID());
        dfd.addOntologies("ontology");
        dfd.setState("initialSate");
        try {
            DFService.deregister(this);
        } catch (Exception e) {} //si está lo deregistro
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            LOGGER.error(getLocalName() + " no registrado. Motivo: " + e.getMessage());
            doDelete();
        }
        LOGGER.exit();
    } // end registerAgent

    /**
     * Proceso de negociacion entre nodos para determinar quien recupera la ejecucion del componente
     *
     * @param element: nombre del componente
     * @param conversationId: Id de conversación
     * @return
     */

    private String report(String element, Hashtable<String, String> attribs, String conversationId){
        LOGGER.entry(element, conversationId);

        if (!elements.containsKey(element)) LOGGER.exit(element + " not found");

        if (attribs.containsKey("action") && attribs.containsKey("winner")) { //acción resultado de una negociación
            String action = attribs.get("action").replaceAll("%winner%", attribs.get("winner"));
            LOGGER.debug("action="+action);
            this.processCmd(action, conversationId);

        } else if (attribs.containsKey("type")) { //reporte de tipo de error en un elemento

            //si el type es "notFound" recibo la instancia que ha fallado
            LOGGER.debug("************** recovering="+attribs.get("type"));
            String reported = elements.get(element).put("recovering", attribs.get("type"));

            if (reported==null) { //si no se ha reportado el problema previamente

                LOGGER.debug("************** recovering=miro si llega cmpins");
                if (attribs.containsKey("cmpins")) { //llega la instancia que ha fallado

                    String cmpins = attribs.get("cmpins");
                    LOGGER.debug("************** cmpins="+cmpins);

                    //si la instancia era de tracking la marco en Failure
                    if (elements.containsKey(cmpins) && elements.get(cmpins).containsKey("state") && elements.get(cmpins).get("state").equals("tracking")) {
                        if (elements.get(element).containsKey("recovering")) elements.get(element).remove("recovering");
                        LOGGER.debug("************** recovering="+"set "+cmpins+ " state=failure");
                        processCmd("set "+cmpins+ " state=failure", conversationId);
                        return "done";
                    } //end marcar en failure

                }
                String trackers =  processCmd("getins "+element+" state=tracking attrib=node", conversationId); //grupo que negociación
                if (!trackers.isEmpty() & trackers.contains(",")) { //hay trackers y más de uno> negociación
                    String condition = processCmd("get "+element+" attrib=negotiation", conversationId);
                    if (condition.isEmpty()) condition = "max freeMem";
                    processCmd("localcmd "+trackers+" cmd=setstate paused", conversationId);
                    negotiate(trackers, condition, "", "",element);
                } else if (!trackers.isEmpty() & !trackers.contains(",")) { //hay trackers y solo uno
                    processCmd("localcmd "+trackers+" cmd=setstate running", conversationId);
                    if (elements.get(element).containsKey("recovering")) elements.get(element).remove("recovering");
                } else if (trackers.isEmpty()){ //no hay trackers
                    LOGGER.debug("**************** No hay reservas");
                    processCmd("start "+element, conversationId);
                }
            }
        } else if (attribs.containsKey("winner")) { //reporte de resultado de negociación
            String winnerNode = attribs.get("winner");
            LOGGER.debug("*****************************");

            String winnerCmpIns = processCmd("getins "+element+" node="+winnerNode+" state=paused limit=1", conversationId).split(",")[0];
            LOGGER.debug("getins "+element+" node="+winnerNode+" state=paused limit=1");

            String trackingIns = processCmd("getins "+element+" state=paused", conversationId);
            LOGGER.debug("getins "+element+" state=paused");
            trackingIns = trackingIns.replace(winnerCmpIns+",","").replace(","+winnerCmpIns,"").replace(winnerCmpIns,"");

            processCmd("localcmd "+trackingIns+" cmd=setstate tracking", conversationId);
            processCmd("localcmd "+winnerCmpIns+" cmd=setstate running", conversationId);
            LOGGER.debug("localcmd "+trackingIns+" cmd=setstate tracking");
            LOGGER.debug("localcmd "+winnerCmpIns+" cmd=setstate running");
            if (elements.get(element).containsKey("recovering")) elements.get(element).remove("recovering");

        }

        return "done";
    }

    private String negotiate(String targets, String negotiationCriteria, String action, String externalData, String conversationId){
        LOGGER.entry(targets, negotiationCriteria, action, conversationId);

        //Request de nueva negociación
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);

        for (String target: targets.split(",")) msg.addReceiver(new AID(target, AID.ISLOCALNAME));

        msg.setConversationId(conversationId);
        msg.setOntology(es.ehu.platform.utilities.MasReconOntologies.ONT_NEGOTIATE );
        System.out.println("****************");

        msg.setContent("negotiate "+targets+" criterion="+negotiationCriteria+" action="+action+" externaldata="+externalData);
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
        for (String cmpIMP:cmpIMPs) response = processCmd("localcmd " + cmpIMP + " cmd=setstate paused", null);

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

        for (String node: nodes) msg.addReceiver(new AID(node, AID.ISLOCALNAME));

        msg.setConversationId(conversationId);
        msg.setOntology("control");
        msg.setContent("negotiate "+element+" nodes="+snode+" condition="+condition +" cmpIMPs="+scmpIMP);
        send(msg);
        return LOGGER.exit("threaded#localcmd .* setstate=running");

    }

    /**
     * Relanzar los componetes de una aplicaicon activa en el nodo que se ha recuperado
     * @author Rafael Priego
     *
     * @param id id del nodo que se ha recuperado
     * @param conversationId Id de conversación
     * @return el mensaje de parada
     */

    private String recoverNode(final String id, String conversationId){
        LOGGER.entry(id, conversationId);
        //TODO Rafael
        //Buscar las aplicaciones activas relacionadas al nodo
        //obtener el sistema relacionado al nodo
        String system =processCmd("get "+id+" attrib=parent",conversationId);
        //Mirar las aplicaciones del sistema
        String applics = getApp(system,"active");
        LOGGER.info("Aplicaciones en el scenario: "+applics);
        if(!applics.isEmpty() && !applics.contains("not found")){
            String[] applicSplit = applics.split(",");
            for(String applic:applicSplit){
                //obtner los componetes de aplicacion asociados a este nodo
                String compons = getCmp(applic);
                LOGGER.info("Componetes de la aplicacion: "+compons);
                String[] componsSplit = compons.split(",");
                for(String compon:componsSplit){
                    //mirar que puedan ser lanzados en este nodo (que contengan el nombre de nodo o sea igual a null que signifaca que se puele lanzar aqui)
                    //TODO esto hay qe extenderlo para limitar el numero de replicas
                    String nodos=processCmd("get "+compon+" attrib=nodeRestriction",conversationId);
                    LOGGER.info("Nodos del componente "+compon+": "+nodos);
                    if(nodos.contains(id) || nodos.equals(null)){
                        //Lanzar la instancia en el nodo en traking

                        // Buscar implementación TODO: por el momento la única, luego deberá estar restringida por los nodos disponibles
                        final String cmpimp = processCmd("get cmpimp* parent="+compon, conversationId).split(",")[0];
                        LOGGER.debug("cmpimp = " + cmpimp);
                        //Obtener el periodo
                        final String period =processCmd("get "+compon+" attrib=period",conversationId);

                        //registro la instancia
                        String cmpins = reg("cmpins", new Hashtable<String, String>() {
                            private static final long serialVersionUID = -4771195899355554947L;
                            {put("parent", cmpimp);}});
                        //TODO Rafael: que no utilice una nuevo ID cuando se reinicia. No se porque no me deja utilizar el antiguo anunque lo borre

                        //Eliminar la instancia ya existente y usar su id para ccrear una nueva
                        final String cmpinsAntiguo=processCmd("getins "+compon+" node="+id, conversationId);
                        elements.remove(cmpinsAntiguo);

                        // iniciar instancia si no hay ninguna en running se activa en running y sino se activa en tracking
                        if(processCmd("getins "+compon+" state=running", conversationId).isEmpty()){
                            //Empezar en running
                            start(cmpins, new Hashtable<String, String>() {
                                private static final long serialVersionUID = -3483494276084879693L;
                                {put("node", id);put("initState", "running");put("period", period);}}, conversationId);
                        }else{
                            //empezar en traking
                            start(cmpins, new Hashtable<String, String>() {
                                private static final long serialVersionUID = -3483494276084879693L;
                                {put("node", id);put("initState", "tracking");put("period", period);}}, conversationId);
                        }
                    }
                }
            }
        }
        //TODO quitar de aqui
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
        loadBalancing(id,conversationId);

        return LOGGER.exit("set.*state=tracking");
    }

    /**
     * Esta función devuelve las aplicaciones asociados a un sistema o escenario, que se encuentre en el estado espesificado
     * @author Rafael Priego
     * @param id id del elemento a procesar: puede ser el system o scenar
     * @param state el estado que debe estar la aplicacion qeu se esta buscando: puede ser activa, en el caso de estar vacio "" se retornan todas
     * @return
     */

    public String getApp (final String id, final String state) {
        LOGGER.entry(id);
        if (!elements.containsKey(id)) return LOGGER.exit(id+" not found");
        if (id.startsWith("system")){
            //Obtener las aplicaciones de los scenariso del sistema
            StringBuilder result = new StringBuilder();
            for (String element: elements.keySet()){
                if (element.startsWith("scenar") && elements.get(element).containsKey("parent") && elements.get(element).get("parent").equals(id) ){
                    result.append(getApp(element,state)).append(",");
                }
            }
            return LOGGER.exit((result.length()>0) ? result.deleteCharAt(result.length()-1).toString() : result.toString()); //si termina en "," se la quito
        }else if (id.startsWith("scenar")){
            StringBuilder result = new StringBuilder();
            for (String element: elements.keySet()){
                if(state.isEmpty()){
                    if (elements.get(element).containsKey("parent") && elements.get(element).get("parent").equals(id)){
                        result.append(element).append(",");
                    }
                } else{
                    if (elements.get(element).containsKey("parent") && elements.get(element).get("parent").equals(id) && state.equals(elements.get(element).get("state"))){
                        result.append(element).append(",");
                    }
                }
            }
            return LOGGER.exit((result.length()>0) ? result.deleteCharAt(result.length()-1).toString() : result.toString()); //si termina en "," se la quito
        }
        return LOGGER.exit("not found");
    }

    /**
     * Extender la inforamcion del un atributo
     * @author Rafael Priego
     * @param prms ids del elemento a procesar: puede ser uno o varios
     * @param attribs informacion y atributos a extender
     * @return
     */

    private String ext(String prms, Hashtable<String, String> attribs, String conversationId) {
        LOGGER.entry(prms, attribs);
        for(String prm: prms.split(",")){
            if (!elements.containsKey(prm)) return LOGGER.exit("element not found");

            for(String attrib : attribs.keySet()) {
                //obtener la inforamacion antigua del atributo
                String datosAntiguos=elements.get(prm).get(attrib);
                //Extender la inforamcion y guardarla
                elements.get(prm).put(attrib,datosAntiguos+","+attribs.get(attrib));
            }
        }

        return LOGGER.exit("done");
    }

    /**
     * Balancear la carga de las aplicacionciones activas al introducir un sistema.
     *
     * @author Rafael Priego
     * @param id id de elemanto desde el cual se obtiene la inforamciona a balancear:
     *  - ID node: balancear la carga de las aplicaciones activas de su sitema
     *  - ID system: balancear la carga de sus aplicaciones activas
     *  - ID scenar: balancear la carga de sus aplicaciones activas
     *  - ID applic: balancear la carga de sus componentes
     * @param conversationId Id de conversación
     * @return
     */

    private String loadBalancing(final String id, String conversationId){
        LOGGER.entry(id, conversationId);
        String result="";
        if (id.startsWith("node")) {
            // obtener el sistema relacionado al nodo
            String system = processCmd("get " + id + " attrib=parent", conversationId);
            // llamar de nuevo a la funcion con el nombre del sistema
            result=loadBalancing(system, conversationId);
        } else if (id.startsWith("system") || id.startsWith("scenar")) {
            // Mirar las aplicaciones activas
            String applics = getApp(id, "active");
            // llamar de nuevos a la funcion con el ide de la aplicaciones
            if (!applics.isEmpty() && !applics.contains("not found")){
                result=loadBalancing(applics, conversationId);
            }
        } else if (id.startsWith("applic")) {
            /*Preparar los arrays a utilizar
             *  -nodos involucrados ---> Componentes asocados - Array strings
             *                      |--> Carga de trabajo asignadoa - int
             *
             *  componets ---> Nodos para cada compoentes (nodeRestriction) -String[]
             *            |--> Carga de trabajo - int
             *            |--> Nodo en el cual se esta ejecutando - String
             *            |--> Instancia en running - String
             */
            ConcurrentHashMap<String, Hashtable<String, Object>> distribucioinNodos = new ConcurrentHashMap<String, Hashtable<String, Object>>();
            ConcurrentHashMap<String, Hashtable<String, Object>> cmpInfo = new ConcurrentHashMap<String, Hashtable<String, Object>>();

            if (!id.isEmpty() && !id.contains("not found")){
                //Obtener todos los componentes de las aplicaiones a balancerar
                String[] applics = id.split(",");
                for (String applic : applics) {
                    LOGGER.info("Aplicaion: "+applic);
                    //Obtener los componentes
                    String compons = getCmp(applic);
                    LOGGER.info("Componetes de la aplicacion: "+compons);
                    String[] componsSplit = compons.split(",");
                    for(String compon:componsSplit){
                        Hashtable<String, Object> auxCmpInfo = new Hashtable<String, Object>();
                        // Obtener la carga de trabajo
                        String sysLoad = processCmd("get "+compon+" attrib=systemLoad",conversationId);
                        auxCmpInfo.put("sysLoad",Integer.valueOf(sysLoad));
                        //Buscar las instancias en running
                        String runningIns=processCmd("getins "+ compon+" state=running",conversationId);
                        auxCmpInfo.put("runningIns",runningIns);
                        //  Obtener el nodo activo
                        String activeNode = processCmd("get "+runningIns+" attrib=node",conversationId);
                        auxCmpInfo.put("activeNode",activeNode);
                        // Obtener los nodos asociados
                        String[] nodes = processCmd("get "+compon+" attrib=nodeRestriction",conversationId).split(",");
                        LOGGER.info("Active node: "+ activeNode+" === system load: "+ sysLoad);
                        auxCmpInfo.put("nodes", nodes);
                        //Alamacenarlo en el hashmap
                        cmpInfo.put(compon, auxCmpInfo);
                        //Rellenar los nodos involucrados
                        for (String nodo: nodes){
                            //Checar si ya existe en el nodo
                            if(!distribucioinNodos.contains(nodo)){
                                distribucioinNodos.put(nodo, new Hashtable<String, Object>() {
                                    private static final long serialVersionUID = -3483494276084879693L;
                                    {put("componentes", new ArrayList<String>());put("sysLoad", 0);}});
                            }
                        }
                    }
                }
                //Ordenar los componentes de aplicacion desde en funcion de su carga de trabajo y el numero de nodos en los que se pueden colocar
                String[] ordenCmp= new String[cmpInfo.size()];
                for(String comp: cmpInfo.keySet()){
                    int valrSL=(int)cmpInfo.get(comp).get("sysLoad");
                    //Determinar en que posicion deve de ir
                    int pointer=0;
                    while (true){
                        if(ordenCmp[pointer]==null){ //Si se ha llegado al final de array
                            break;
                        }else{
                            if(valrSL>(int)cmpInfo.get(ordenCmp[pointer]).get("sysLoad")){ //si la carga es mayor que la del componente en esta posiscion del array
                                break;
                            }else if(valrSL==(int)cmpInfo.get(ordenCmp[pointer]).get("sysLoad")&&
                                    !((String[])cmpInfo.get(comp).get("nodes"))[0].equals("") &&
                                    ((String[])cmpInfo.get(comp).get("nodes")).length < ((String[])cmpInfo.get(ordenCmp[pointer]).get("nodes")).length ){
                                break;
                            }else{
                                pointer++;
                            }
                        }
                    }
                    //deplazar el array
                    for(int i=ordenCmp.length-1; i>pointer;i--){
                        ordenCmp[i]=ordenCmp[i-1];
                    }
                    //meterlo en su posicion
                    ordenCmp[pointer]=comp;
                }
                //Hacer el balace de la carga de trabajo
                for(String comp: ordenCmp){
                    String[] listaNodo = (String[])cmpInfo.get(comp).get("nodes");
                    String activeNode= (String)cmpInfo.get(comp).get("activeNode");
                    int sysLoad = (int)cmpInfo.get(comp).get("sysLoad");

                    //Condiciones
                    //Lo tengo y no tengo nada asignada
                    if(((ArrayList<String>)distribucioinNodos.get(activeNode).get("componentes")).size()==0){
                        ((ArrayList<String>)distribucioinNodos.get(activeNode).get("componentes")).add(comp);
                        distribucioinNodos.get(activeNode).put("sysLoad",((int)distribucioinNodos.get(activeNode).get("sysLoad"))+sysLoad);
                    }else{
                        /*
                         * menor carga de trabajo --->no lo tengo y no lo tengo asignado
                         *                        |-->tengo asignado y menor carga
                         */
                        String node = listaNodo[0];
                        int nodeSL= ((int)distribucioinNodos.get(node).get("sysLoad"));
                        for(int i=1; i< listaNodo.length;i++){
                            int compararSL = ((int)distribucioinNodos.get(listaNodo[i]).get("sysLoad"));
                            if (nodeSL==compararSL && listaNodo[i].equals(activeNode)){ //Si tiene la misma carga de trabajo y es mi nodo activo me quedo con el
                                node=listaNodo[i];
                                nodeSL=compararSL;
                            }else if(compararSL<nodeSL){
                                node=listaNodo[i];
                                nodeSL=compararSL;
                            }
                        }
                        ((ArrayList<String>)distribucioinNodos.get(node).get("componentes")).add(comp);
                        distribucioinNodos.get(node).put("sysLoad",((int)distribucioinNodos.get(node).get("sysLoad"))+sysLoad);
                    }
                }
                // Para ver el resultado
                for(String node: distribucioinNodos.keySet()){
                    String componentes="";
                    for(int i=0; i<((ArrayList<String>)distribucioinNodos.get(node).get("componentes")).size(); i++ ){
                        componentes = componentes + "," + ((ArrayList<String>)distribucioinNodos.get(node).get("componentes")).get(i);
                    }
                    LOGGER.info("Nodos: "+node+" Componentes: "+ componentes+" carga de trabajo: "+
                            String.valueOf(((int)distribucioinNodos.get(node).get("sysLoad"))));
                }

                /** Empzar la recuperacion */
                // Obtener las intancias a ganadora
                for (String node : distribucioinNodos.keySet()) {
                    ArrayList<String> compons = ((ArrayList<String>) distribucioinNodos.get(node).get("componentes"));
                    for (int i = 0; i < compons.size(); i++) {
                        String winner = processCmd("getins " + compons.get(i) + " node=" + node, conversationId).split(",")[0];
                        // pasar todas las instancas en traking a paused
                        processCmd("localcmd (getins " + compons.get(i) + " state=tracking) cmd=setstate paused", conversationId);
                        // envair la inforamcion del ganador a running
                        ACLMessage aMsg = new ACLMessage(ACLMessage.INFORM);
                        aMsg.setContent(winner);
                        aMsg.setOntology("move");
                        String runIns = (String) cmpInfo.get(compons.get(i)).get("runningIns");
                        aMsg.addReceiver(new AID(runIns, AID.ISLOCALNAME));
                        aMsg.setConversationId(conversationId);
                        send(aMsg);
                        // cambiar el running en moving (negotiation es la condicion de pasada) para que el cambio se aga cuando se esta en un estado directamente recuperable
                        processCmd("localcmd " + runIns + " cmd=setstate negotiating", conversationId);
                        LOGGER.info("== La instacia " + winner + " es la ganadora del compon: " + compons.get(i) + " nodo: " + node);
                    }
                }
            }else{
                result="not found"; //salir con error
            }
        } else {
            result="not found";
        }
        return LOGGER.exit(result);
    }
}



