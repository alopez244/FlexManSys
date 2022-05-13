package es.ehu;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
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
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import es.ehu.platform.template.interfaces.IExecManagement;

public class SystemModelAgent extends Agent implements IExecManagement {


    /* DECLARACIÓN DE VARIABLES */

    /* Variable que nos permite ver los logs en consola */
    static final Logger LOGGER = LogManager.getLogger(SystemModelAgent.class.getName()) ;

    /* ThreadedBehaviourFactory. Variable que permite ejecutar cada comportamiento asociado en un thread independiente */
    final public ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();

    /* HashMap con todos los comportamientos que se están ejecutando en el objeto tbf (ver variable anterior) */
    final protected ConcurrentHashMap<String, SimpleBehaviour> behaviours = new ConcurrentHashMap<>();

    /* HashMap que asocia cada mensaje recibido con la respuesta proporcionada y el tiempo de respuesta */
    final protected ConcurrentHashMap<String, String> threadLog = new ConcurrentHashMap<>();

    /* HashMap de elementos registrados en el sistema */
    public ConcurrentHashMap<String, Hashtable<String, String>> elements = new ConcurrentHashMap<>();

    /* HashMap utilizado para asignar IDs incrementales a cada elemento registrado en el SystemModelAgent */
    public ConcurrentHashMap<String, Integer> count = new ConcurrentHashMap<>();

    /* Contador incremental para generar conversationIds */
    private int cmdId = 1000;

    /* Instante de tiempo en el que se instancia el SystemModelAgent */
    public static long startTime = System.currentTimeMillis();


    /* SETUP Y COMPORTAMIENTO DEL SYSTEMMODELAGENT */

    protected void setup() {

        /* Se genera una clave en función del instante actual */
        /* Despues, se crea una nueva instancia del SMABehaviour y se guarda en el HashMap behaviours */
        String key = "tmwmb_"+System.currentTimeMillis();
        behaviours.put(key, new SMABehaviour(this));

        /* A continuación, este comportamiento se añade al objeto tbf para que se ejecute en un thread independiente */
        this.addBehaviour(tbf.wrap(behaviours.get(key)));

        /* Por último, se invoca el método initialize a través del método processCmd */
        this.processCmd("initialize","");

        LOGGER.debug("setup completado");
    }

    private class SMABehaviour extends SimpleBehaviour {

        /* Objeto que del tipo SystemModelAgent (el agente que va a utilizar este comportamiento) */
        private SystemModelAgent myAgent;

        /* Template para filtrar los mensajes recibidos por el SMABehaviour */
        private MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchOntology("control"),
                        MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                                MessageTemplate.MatchPerformative(ACLMessage.INFORM)));

        /* Constructor */
        public SMABehaviour(Agent a) {
            super(a);
            this.myAgent = (SystemModelAgent) a;
        }

        /* Código ejecutado únicamente en la primera iteración de la ejecución del comportamiento */
        public void onStart() {

            /* Se registra el SystemModelAgent en el Directory Facilitator de JADE con el nombre "sa" */
            myAgent.registerAgent("sa");
            LOGGER.warn("Se ha inicializado el SMABehaviour");
        }

        /* Código ejecutado en cada iteración de la ejecución del comportamiento */
        public void action() {

            /* Se filtran los mensajes recibidos utilizando el template */
            ACLMessage msg = receive(template);

            if (msg != null) {

                /* Si hay un mensaje que cumple con el template, se procesará con un nuevo comportamiento */
                /* Primero se genera una clave en función de si el mensaje recibido tiene ConversationId */
               String key;
               if (msg.getConversationId() == null) {
                   key = "";
               } else {
                   key = msg.getConversationId();
               }

               /* Se instancia un nuevo comportamiento ThreadedCommandProcessor y se guarda en el HashMap behaviours */
               behaviours.put(key, new ThreadedCommandProcessor(key, myAgent,msg));

                /* Por último, este comportamiento se añade al objeto tbf para que se ejecute en un thread independiente */
                myAgent.addBehaviour(tbf.wrap(behaviours.get(key)));


            } else {

                /* Si no hay ningún mensaje que pase el filtro, se bloquea el comportamiento */
                /* El bloqueo dura hasta que se recibe un nuevo mensaje */
                block();
            }
        }

        public boolean done() {

            /* Como este método siempre devuelve false, el comportamiento no termina nunca (comportamiento cíclico) */
            return false;
        }
    }


    /* PROCESAMIENTO DE MENSAJES Y AYUDA DE USUARIO */

    /**
     * Procesa los mensajes recibidos por el SystemModelAgent
     * Invoca uno o varios métodos dependiendo del contenido del mensaje y devuelve la respuesta
     * @param cmd Mensaje a procesar
     * @param conversationId Identificador de la conversación
     * @return Respuesta al mensaje recibido
     */
    public String processCmd(String cmd, String conversationId) {

        LOGGER.info ("cmd_"+conversationId+" \""+cmd+"\"..." );

        /* Respuesta al mensaje recibido */
        StringBuilder result = new StringBuilder();

        /* Si el mensaje tiene comillas al inicio y al final, se eliminan */
        while (cmd.length()>0 && cmd.charAt(0)=='\"' && cmd.charAt(cmd.length()-1)=='\"') cmd=cmd.substring(1, cmd.length()-1);

        /* A continuación, se subsanan posibles errores de formato (doble espacio "  " o espacio antes de igual " =") */
        while (cmd.contains("  ")) cmd=cmd.replace("  ", " ");
        while (cmd.contains(" =")) cmd=cmd.replace(" =", "=");

        /* Se crea el array String cmds separando el mensaje recibido por espacios " " */
        /* La primera posición del array hace referencia al comando a ejecutar */
        /* La segunda posición del array hace referencia al elemento sobre el que se realiza la acción */
        String[] cmds = cmd.split(" ");

        /* Si el array tiene más de dos posiciones, el resto son atributos */
        Hashtable<String, String> attribs = processAttribs(2, cmds);

        try {

            /* Se evalúa el primer campo del array cmds para determinar qué método hay que invocar */
            switch (cmds[0]) {


                /* Los métodos que vaya revisando irán pasando arriba */
                case "initialize":
                    result.append(initialize());
                    break;
                case "reg":
                    result.append(reg(cmds[1], attribs));
                    break;
                case "del":
                    result.append(del(cmds[1]));
                    break;
                case "list":
                    result.append(list((cmds.length > 1) ? cmds[1] : "design", (cmds.length > 2 ? cmds[2] : "")));
                    break;
                case "validate":
                    result.append(validate((cmds.length > 1) ? cmds[1] : "design", (cmds.length > 2 ? cmds[2] : ""), (cmds.length > 3 ? cmds[3] : "")));
                    break;
                case "set":
                    result.append(set(cmds[1], attribs, conversationId));
                    break;
                case "get":
                    result.append(get(cmds[1], attribs, conversationId));
                    break;
                case "localneg":
                    result.append(negotiate(cmds[1], attribs.get("criterion"), attribs.get("action"), attribs.get("externaldata")));
                    break;
                case "appstart":
                    result.append(appStart(cmds[1], attribs, conversationId));
                    break;
                case "sestart":
                    result.append(seStart(cmds[1], attribs, conversationId));
                    break;
                case "seregister":
                    result.append(seRegister(cmd, conversationId));
                    break;
                case "ivalidate":
                    result.append(iValidate(cmds[1], conversationId));
                    break;
                case "getcmp":
                    result.append(getCmp(cmds[1]));
                    break;
                case "help":
                    result.append(help(cmds));
                    break;
                default:
                    result.append("cmd not found:").append(cmds[0]);
                    break;
            }
        } catch (Exception e) {
            LOGGER.error("ERROR:"+e.getLocalizedMessage());
            e.printStackTrace();
        }

        /* Para terminar, se muestra por consola el mensaje original con la respuesta recibida y se devuelve la respuesta */
        LOGGER.info ("cmd_"+conversationId+" \""+cmd+"\" > "+result);
        return result.toString();
    }

    /**
     * Pasa los atributos recibidos a un HashMap
     * @param firstAttribPos Indica la posición del arry cmds en el que está el primer atributo
     * @param cmdLine Array del que se van a obtener los atributos
     * @return HashTable con los atributos
     */
    public Hashtable<String, String> processAttribs(int firstAttribPos, String... cmdLine){

        /* Primero se declara el HashTable que se va a devolver */
        Hashtable<String, String> attribs = new Hashtable<>();

        /* Los dos primeros elementos del array nunca son atributos (acción y receptor de la acción) */
        /* Por tanto, si la longitud del array es menor de 3, no hay atributos */
        if (cmdLine.length < 3) return null;

        /* Se recorre el array a partir de la posición indicada en firstAttribPos */
        for (int i = firstAttribPos; i < cmdLine.length; i++) {

            /* Los atributos se componen de nombre y valor, separados por un signo = */
            if (cmdLine[i].contains("=")) {

                /* Se separan nombre y valor */
                String[] attribDef = cmdLine[i].split("=");

                /* Se guardan nombre y valor en el HashMap (si no hay valor se escribe "") */
                attribs.put(attribDef[0], (attribDef.length>1)?attribDef[1]:"");
            }
        }
        return LOGGER.exit(attribs);
    }

    /**
     * Pasa las restricciones recibidas a un HashMap
     * @param cmdLine Array del que se van a obtener los atributos
     * @return HashMap con los atributos
     */
    public ConcurrentHashMap<String, String> processRestrictions(String... cmdLine){

        /* Primero se declara el HashMap que se va a devolver */
        ConcurrentHashMap<String, String> restrictions = new ConcurrentHashMap<>();

        /* Se recorre el array */
        for (int i = 1; i < cmdLine.length; i++) {

            /* Los atributos se componen de nombre y valor, separados por un signo = */
            if (cmdLine[i].contains("=")) {

                /* Se separan nombre y valor */
                String[] attribDef = cmdLine[i].split("=");

                /* Se guardan nombre y valor en el HashMap (si no hay valor se escribe "") */
                restrictions.put(attribDef[0], (attribDef.length>1)?attribDef[1]:"");
            }
        }
        return LOGGER.exit(restrictions);
    }

    /**
     * Este método muestra una descripción general de los comandos disponibles en el SystemModelAgent
     * Además, se le puede solicitar información detallada sobre cómo utilizar algunos de esos comandos
     * @param cmds Array con la información relativa a la ayuda solicitada
     * @return Respuesta a la consulta realizada por el usuario
     */
    private String help (String [] cmds){

        /* Si se solicita, se ofrece ayuda detallada de algunos comandos */

        /* GET */
        if ((cmds.length>1) && (cmds[1].equals("get"))) {
            return "get command:\n"
                    + "searches elements in database for a parameter:\n"
                    + "\n"
                    + "examples:\n"
                    + "get cmpins* node=node102               // gets component instances running on node102\n"
                    + "get action102 attrib=order             // get the order parameter of element action102";
        }

        /* SET */
        if ((cmds.length>1) && (cmds[1].equals("set"))) {
            return "set command:\n"
                    + "sets a parameter value for the selected element:\n"
                    + "\n"
                    + "examples:\n"
                    + "set action102 order=5                  // sets value 5 for order parameter in element action102\n";
        }

        /* Se muestran de forma resumida los comandos disponibles del SystemModelAgent */
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
                + "\n"
                + "getcmp element                         // return the component of the instance/implementation or application"
                + "\n"
                + "sestart element                        // starts a component/application/event/action\n"
                +  "\n"
                + "insert help + command name for detailed information (example: help get)";
    }


    /* PUESTA EN MARCHA DEL SYSTEMMODELAGENT */

    /**
     * Este método apunta a un fichero de propiedades (sa.properties)
     * Este fichero indica contra qué ficheros XSD hay que hacer la validación del sistema y de las aplicaciones
     * Además, registra el elemento "system" en el modelo del sistema
     * @return
     */
    private String initialize() {

        Properties prop = new Properties();

        String url = "/resources/sa.properties";

        LOGGER.debug("Properties: "+ getClass().getResource(url).getPath());

        InputStream in = getClass().getResourceAsStream(url);
        try {
            prop.load(in);
        } catch (IOException e1) {
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

    /**
     * Auxiliar method used to read the properties file.
     * It returns a string with the content of the XML schema the input tag is pointing to.
     * (i.e. systemElements=AppConcepts.xsd)
     * @param file
     * @return
     */
    private String prop2String(String file){
        String output = "";
        try {
            output = IOUtils.toString(this.getClass().getResourceAsStream("/resources/"+file), "UTF-8");
        } catch (IOException e1) {e1.printStackTrace();}
        return output.substring(output.indexOf("<"));
    }

    /**
     * This method registers the SystemModelAgent in the JADE DF
     * @param localName
     */
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
    }





    //====================================================================
    //IREGISTER INTERFACE
    //====================================================================

    /**
     * This method check if the item to be registered has an ID. If not, it generates an ID and returns it.
     * The element gets registered in the system model ("elements" Hashtable)
     * @param prm
     * @param attribs
     * @return
     */
    private String reg(final String prm, Hashtable<String, String> attribs) {
        LOGGER.entry(prm, attribs);
        String type = prm;
        String id = "";
        if (!attribs.containsKey("ID")) { // si no llega un id lo genero
            id = (prm.length() > 10)? prm.substring(0, 10): prm;
            id = id.toLowerCase();
            if (!count.containsKey(id)) count.put(id, 1);
            else count.put(id, (count.get(id)) + 1);
            id = id + count.get(id);
        } else { // si llega lo guardo
            id = attribs.get("ID");
            attribs.remove("ID");
        }
        attribs.put("category", type);
        elements.put(id, attribs);
        return LOGGER.exit(id);
    }

    /**
     * Allows removal of all elements of the system model or just a single element.
     * @param prm
     * @return
     */
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

    /**
     * Check the compliance of the application to be registered.
     * For that purpose, it uses the XML schemas pointed in the properties file (sa.properties).
     * @param _prm
     * @return
     * @throws Exception
     */
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

    private String iValidate(String se, String conversationId) throws Exception {

        //localizo tipo

        LOGGER.info("iValidate("+se+")");
        //String seType = sendCommand ("get "+se+" attrib=category", conversationId).getContent();
        Hashtable<String, String> aux = new Hashtable<>();
        aux.put("attrib", "category");
        String seType = get(se, aux, conversationId);

        //no existe

        if (seType.equals("")) {
            LOGGER.info("ERROR: id not found");
            return "";
        }
        LOGGER.info(seType+" type="+seType);

        //compruebo jerarquía
        //String validateHierarchy = sendCommand("validate appValidation "+se+" "+seType, conversationId).getContent();
        String validateHierarchy = validate("appValidation", se, seType);
        if (!validateHierarchy.equals("valid")) {
            LOGGER.info(se+">"+seType+": xsd incorrecta");
            throw new Exception();

            // TODO: Borrar
        }
        LOGGER.info(validateHierarchy+">"+seType+": xsd correcta");

        // mover a registering

        aux.clear();
        aux.put("attrib","seParent");
        String parentID = get(se, aux, conversationId);
        //sendCommand("set "+se+" parent=(get "+se+" attrib=seParent) seParent=", conversationId).getContent();
        String command = "set " + se + " parent="+parentID+" seParent=";
        set(command.split(" ")[1], processAttribs(2, command.split(" ")), conversationId);

        return se;
    }



    //====================================================================
    //ISYSTEMINFO INTERFACE IMPLEMENTATION
    //====================================================================

    /**
     * Searches elements in database for a parameter.
     * @param prms
     * @param filters
     * @param conversationId
     * @return
     */
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

    private Vector<String> nodeListToVector(Element element, Vector<String> v, String attrib) {
        v.add(element.getNodeName() + "=" +element.getAttribute(attrib));
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) nodeListToVector((Element) nl.item(i), v, attrib);
        }
        return v;
    }



    private String list(String... prm) {
        LOGGER.entry(prm);
        String _prm = prm[0].replace("*", ".*");

        StringBuffer response = new StringBuffer("");

        if (_prm.startsWith("desi")) { // desing
            String comienzo = (prm.length>1 && !prm[1].isEmpty())? prm[1].replace("*", ".*"):"system.*";
            for (String key : elements.keySet()) {
                if (key.matches(comienzo)) {
                    response.append("\n\n").append(list(key)).append("\n");
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




    //====================================================================
    //IEXECMANAGEMENT INTERFACE IMPLEMENTATION
    //====================================================================

    @Override
    public String appStart(String seID, Hashtable<String, String> attribs, String conversationId ) {
        LOGGER.entry(seID, attribs, conversationId);

        if (seID.indexOf(",")>0) {
            String[] tokens = seID.split(",");
            for (int j=0; j<tokens.length; j++) appStart(tokens[j], attribs, conversationId);
            return "";
        }

        String redundancy = "1";
        if ((attribs!=null) && (attribs.containsKey("redundancy")))
            redundancy = attribs.get("redundancy");

        // si no existe el id en el registro devuelve error (irá al launcher)
        if (!elements.containsKey(seID)) return "-1";

        // Si no me han pasado conversationId es que yo empiezo la conversacion, por lo que tengo que crearlo
        if (conversationId==null) conversationId=String.valueOf(cmdId++);

        //Calcular quiénes negocian:
        // leer del registro para este "seID" la lista entera de procNodes cada uno con sus HostedElements. - leo del registro los serviceid que requiere el seID
        // buscar procnodes que tengan estas HostedElements > lista de los procNodes negociadores
        // si no hay negociarres return "-4"; //TODO
//        String targets ="";

        String HostedElements=processCmd("get (get * parent=(get * parent="+seID+" category=restrictionList)) attrib=attribValue", conversationId);
        //si hay restricción la añado al filtro, en caso contrario solo busco procNode
        String Alltargets = processCmd("get * category=pNodeAgent"+((HostedElements.length()>0)?" HostedElements="+HostedElements:""), conversationId);

        if (Alltargets.length()<=0) return "-4";

        String seCategory = processCmd("get "+seID+" attrib=category", conversationId);
        String seClass = "";
        if (attribs.containsKey("seClass")){
            //Si se ha pasado clase, arranco el agente con esa clase
            seClass = attribs.get("seClass");
        } else {
            //Si no se ha pasado clase, la clase por defecto será MPlanAgent
            seClass = "es.ehu.domain.manufacturing.agents.MPlanAgent";
        }

        // Atributo execution_phase a starting, para que se sepa que se esta creando
        String command = "set " + seID + " execution_phase=starting";
        set(command.split(" ")[1], processAttribs(2, command.split(" ")), conversationId);

//        if (targets.length()<=0) return "-4";
        String UpdatedTargets=Alltargets;

        //mando negociar a todos
        conversationId = getLocalName() + "_" + cmdId++;
        processCmd("localneg "+UpdatedTargets+" action=start criterion=max mem externaldata="+seID+","+seCategory+","+seClass+","+getLocalName()+","+redundancy, conversationId);
        LOGGER.exit();


        return "";
    }

    @Override
    public String appStop(String... seID) {
        return null;
    }

    public String seStart(String seID, Hashtable<String, String> attribs, String conversationId) {

        Set<String> keys = attribs.keySet();
        for(String key: keys) {
            String element = attribs.get(key);
            String execution_phase = elements.get(element).get("execution_phase");
            if (execution_phase == null) {
                elements.get(element).put("execution_phase", "starting");
            } else
                return "ERROR";
        }

        return "OK";
    }

    public String seRegister(String cmd, String conversationId) throws Exception {

        // Collect all the data from cmd
        Hashtable<String, String> attribs = null;
        Hashtable<String, String> auxAttribs = new Hashtable<>();
        ConcurrentHashMap<String, ConcurrentHashMap<String, String>> restrictionLists = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();
        if (cmd.contains("&")) {
            String[] aux = cmd.split(" & ");
            attribs = processAttribs(1, aux[0].split(" "));
            restrictionLists = new ConcurrentHashMap<>();
            String restrictionValue = aux[1].split(" ")[0];
            ConcurrentHashMap<String, String> restrictList = processRestrictions(aux[1].split(" "));
            restrictionLists.put(restrictionValue, restrictList);
        } else
            attribs = processAttribs(1, cmd.split(" "));

        String seType = attribs.get("seType");
        attribs.remove("seType");   // No tiene que ir en los atributos --> Al validar da fallo sino
        String parentId = attribs.get("seParent");

        LOGGER.entry("seType", parentId, attribs, restrictionLists, conversationId);

        String name = attribs.get("name");
        System.out.println("Attribs--> " + attribs);

        //compruebo restricciones
        String restrictionMatch = null;
        if (restrictionLists!=null) {
            for (Map.Entry<String, ConcurrentHashMap<String, String>> restriction : restrictionLists.entrySet()) {
                //Aquí se obtiene el tipo de recurso del que se quiere comprobar las restricciones
                String query = "get * category="+restriction.getKey();
                //String query = "get * category=service";

                for (Map.Entry<String, String> entry : restriction.getValue().entrySet()) {
                    //Aquí se obtienen las restricciones asociadas a ese tipo de recurso
                    query = query + " " + entry.getKey() + "=" + entry.getValue();
                }

                // TODO mirarlo porque igual falla --> un get dentro de otro get (mejor hacerlos separados)
                //query = "get (get ("+query+") attrib=parent) category=" + restriction.getKey();

                System.out.println("***************** Lanzo consulta de comprobación " + query);

                String[] querys = query.split(" ");
                auxAttribs.clear();
                auxAttribs = processAttribs(2, querys);
                String validateRestriction = get(querys[1], auxAttribs, conversationId);
                auxAttribs.clear();
                //String validateRestriction = sendCommand(new StringBuilder(query), "sa", conversationId);
                //String validateRestriction = get(query.split(" ")[1], processAttribs(2, query.split(" ")), conversationId);
                if (validateRestriction.isEmpty()) {
                    LOGGER.info(query+">"+validateRestriction+": restricción incumplida");
                    throw new Exception();
                }
            }
        }

        //localizo tipo del padre    // TODO si el padre es "system" no comprobar
        // TODO si el padre está en systemmodel.xml:
        // ir a systemmodel.xsd y buscar <xs:extension base="tipo" y en sus hijos
        // getFixed (tipo, atributo) > buscar <xs:extension base="tipo" y en sus hijos devuelve el fixed del que tenga nombre atributo

        // si es extensible el padre traigo la estructura desde el hijo de system con los atributos, (resolver su ID **registering**), validar appvalidar.xsd
        // si valida > volver a montarlo en systemmodel

        //String parentType = sendCommand ("get "+parentId+" attrib=category", conversationId).getContent();
        auxAttribs.clear();
        auxAttribs.put("attrib", "category");
        String parentType = get(parentId, auxAttribs, conversationId);
        if (parentType.equals("")) {
            LOGGER.info("ERROR: parent id not found"); //no existe padre
            throw new Exception();
        }
        LOGGER.info(parentId+" type="+parentType);

        //compruebo jerarquía // TODO si el padre es "system" comprobar que el se es raiz del appvalidation xsd -> dom

        //String validateHierarchy = sendCommand("validate hierarchy "+seType+" "+parentType, conversationId).getContent();
        String validateHierarchy = validate("hierarchy", seType, parentType);
        if (!validateHierarchy.equals("valid")) {
            LOGGER.info(seType+">"+parentType+": jerarquía incorrecta");
            throw new Exception();
        }
        LOGGER.info(seType+">"+parentType+": jerarquía correcta");

        // registro elemento en xml elements
        String command = "reg "+seType+" seParent="+parentId+ " parent=concepts";
        if (attribs!=null) {
            for (Map.Entry<String, String> entry : attribs.entrySet()) {
                command = command+" "+entry.getKey()+"="+entry.getValue();
            }
        }
        //String ID = sendCommand(command, conversationId).getContent();
        String ID = reg(command.split(" ")[1], processAttribs(2, command.split(" ")));

        // TODO: por cada restrictionList una llamada al get y comprobar que existen en el SystemModel
        for (String keyi: restrictionLists.keySet()){
            System.out.println("*******************key="+keyi);
            //String restrictionList = sendCommand("reg restrictionList se="+keyi+" parent="+ID, conversationId).getContent();
            String query = "reg restrictionList se="+keyi+" parent="+ID;
            String restrictionList = reg(query.split(" ")[1], processAttribs(2, query.split(" ")));

            for (String keyj: restrictionLists.get(keyi).keySet()){
                query = "reg restriction attribName="+keyj+" attribValue="+restrictionLists.get(keyi).get(keyj)+" parent="+restrictionList;
                //String restriction = sendCommand("reg restriction attribName="+keyj+" attribValue="+restrictionLists.get(keyi).get(keyj)+" parent="+restrictionList, conversationId).getContent();
                String restriction = reg(query.split(" ")[1], processAttribs(2, query.split(" ")));
                System.out.println("keyj="+keyj);
            }
        }

        //validar elemento contra esquema systemElements

        //String validation =  sendCommand("validate systemElement "+ID, conversationId).getContent();
        String validation =  validate("systemElement", ID);
        LOGGER.info(validation);

        if (!validation.equals("valid")) {
            //sendCommand("del "+ID, conversationId).getContent();
            del(ID);
            LOGGER.info("error xsd concepts");
            throw new Exception();
            //throw new XSDException(validation);

        } else LOGGER.info("xsd concepts correcto");

        // mover a registering.xml


        if (parentId.equals("system")) { //sendCommand("set " + ID + " parent=registering", conversationId).getContent();
            command = "set " + ID + " parent=registering";
            set(command.split(" ")[1], processAttribs(2, command.split(" ")), conversationId);
        } else { //sendCommand("set " + ID + " parent=(get " + ID + " attrib=seParent) seParent=", conversationId).getContent();
            auxAttribs.clear();
            auxAttribs.put("attrib", "seParent");
            String parentID = get(ID, auxAttribs, conversationId);
            //command = "set " + ID + " parent=(get " + ID + " attrib=seParent) seParent=";
            command = "set " + ID + " parent="+parentID+" seParent=";
            set(command.split(" ")[1], processAttribs(2, command.split(" ")), conversationId);
        }
        return ID;
    }







    //====================================================================
    //GENERAL PURPOSE METHODS
    //====================================================================

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

    private String negotiate(String targets, String negotiationCriteria, String action, String externalData){
        LOGGER.entry(targets, negotiationCriteria, action);
        String conversationId=null;

        //Primero se comprueba la acción a realizar como resultado de la negociación (la acción de arranque tiene requerimientos específicos).
        if (action.equals("start")) {


            //Separamos el external data para obtener la redundancia
            String externalDataArray[]=externalData.split(",");
            Integer redundancy = Integer.parseInt(externalDataArray[4]);
            String state="";

            //Con este valor, se puede iterar
            for (int i=0; i<redundancy; i++) {

                //Se comprueba si en la información externa se incluye el estado al que debe transicionar el agente tras el arranque.
                //Si no, se asignará el estado running o tracking según corresponda
                if (externalDataArray.length<6){
                    state=","+((i==0)?"running":"tracking");
                }

                //Request de nueva negociación
                ACLMessage msg = new ACLMessage(ACLMessage.CFP);

                for (String target: targets.split(",")) msg.addReceiver(new AID(target, AID.ISLOCALNAME));

                conversationId=String.valueOf(cmdId++);

                msg.setConversationId(conversationId);
                msg.setOntology("negotiation" );
                System.out.println("****************");

                msg.setContent("negotiate "+targets+" criterion="+negotiationCriteria+" action="+action+" externaldata="+externalData+state);
                LOGGER.debug(msg);
                send(msg);
            }

        } else {

            //Request de nueva negociación
            ACLMessage msg = new ACLMessage(ACLMessage.CFP);

            for (String target: targets.split(",")) msg.addReceiver(new AID(target, AID.ISLOCALNAME));

            // Si no me han pasado negotiationId, el sa es el que inicia la conversacion
            conversationId=String.valueOf(cmdId++);

            msg.setConversationId(conversationId);
            msg.setOntology("negotiation");
            System.out.println("****************");

            msg.setContent("negotiate "+targets+" criterion="+negotiationCriteria+" action="+action+" externaldata="+externalData);
            LOGGER.debug(msg);
            send(msg);
        }



        return LOGGER.exit("state=running");
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

}



