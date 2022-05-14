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

            /* Se registra el SystemModelAgent en el Directory Facilitator de JADE */
            myAgent.registerSMA();
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
                case "initialize":
                    result.append(initialize());
                    break;
                case "help":
                    result.append(help(cmds));
                    break;
                case "reg":
                    result.append(reg(cmds[1], attribs));
                    break;
                case "del":
                    result.append(del(cmds[1]));
                    break;


                case "validate":
                    result.append(validate((cmds.length > 1) ? cmds[1] : "", (cmds.length > 2 ? cmds[2] : ""), (cmds.length > 3 ? cmds[3] : "")));
                    break;

                /* Los métodos que vaya revisando irán pasando arriba */

                case "list":
                    result.append(list((cmds.length > 1) ? cmds[1] : "design", (cmds.length > 2 ? cmds[2] : "")));
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
        return attribs;
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
        return restrictions;
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
     * @return Mensaje de confirmación para indicar que la inicialización ha concluido
     */
    private String initialize() {

        /* Se declara el objeto prop con el que se va a trabajar en el método */
        Properties prop = new Properties();

        /* A continuación, se declara el path relativo hasta el fichero de propiedades */
        String url = "/resources/sa.properties";
        LOGGER.debug("Properties: "+ getClass().getResource(url).getPath());

        /* Después, se obtiene el path completo a partir del path relativo */
        InputStream in = getClass().getResourceAsStream(url);

        /* Se carga el contenido del fichero de propiedades en la variable prop */
        try {
            prop.load(in);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        /* Se obtiene el contenido del esquema appConcepts (AppConcepts.xsd) */
        String appConcepts = prop2String((String)prop.get("appConcepts"));

        /* A continuación, se normaliza el namespace del fichero XML (xmlsn) */
        /* Para ello, se sustituye el contenido de la línea que empieza por xs: schema */
        appConcepts = appConcepts.replace(appConcepts.substring(appConcepts.indexOf("<xs:schema "), appConcepts.indexOf("\">")+2),
                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">");

        /* Se repite la misma operación con el esquema appHierarchy (AppHierarchy.xsd) */
        String appHierarchy = prop2String((String)prop.get("appHierarchy"));
        appHierarchy = appHierarchy.replace(appHierarchy.substring(appHierarchy.indexOf("<xs:schema "), appHierarchy.indexOf("\">")+2),
                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">");

        /* Se repite de nuevo la misma operación con el esquema systemModel (FLRXMANSYSystemModel.xsd) */
        String systemModel = prop2String((String)prop.get("systemModel"));
        systemModel = systemModel.replace(systemModel.substring(systemModel.indexOf("<xs:schema "), systemModel.indexOf("\">")+2),
                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">");

        try {

            /* Por último, se registran los tres elementos (system, hierarchy y concepts) */
            this.processCmd("reg system ID=system name=Sistema xsd="+URLEncoder.encode(systemModel, "UTF-8"), "");
            this.processCmd("reg hierarchy ID=hierarchy name=hierarchy xsd="+URLEncoder.encode(appHierarchy, "UTF-8"), "");
            this.processCmd("reg concepts ID=concepts name=concepts xsd="+URLEncoder.encode(appConcepts, "UTF-8"), "");
        } catch (Exception e) {e.printStackTrace();}

        return "done";
    }

    /**
     * Método auxiliar utilizado para leer el fichero de propiedades (sa.properties)
     * Devuelve un String con el contenido del fichero XSD asociado a un concepto del fichero de propiedades
     * (e.g., appConcepts=AppConcepts.xsd)
     * @param file Nombre del fichero XSD a buscar
     * @return String con el contenido del fichero XSD
     */
    private String prop2String(String file){

        String output = "";
        try {

            /* Se genera el path completo a partir de la variable file y se lee su contenido */
            output = IOUtils.toString(this.getClass().getResourceAsStream("/resources/"+file), "UTF-8");
        } catch (IOException e1) {e1.printStackTrace();}

        /* Por último, se elimina cualquier espacio o caracter de cualquier tipo que esté antes del primer símbolo < */
        return output.substring(output.indexOf("<"));
    }

    /**
     * Este método registra al SystemModelAgent en el servicio de páginas amarillas de JADE (JADE DF)
     */
    private void registerSMA() {

        /* Se declaran las variables que se van a utilizar en el método */
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        /* Se asigna el nombre con el que hemos creado el agent como tipo del objeto ServiceDescription sd */
        /* Para evitar problemas, es necesario crear el SystemModelAgent con el nombre "sa" */
        sd.setType(getLocalName());
        sd.setName(getName());

        /* Se añaden el servicio y el nombre del agente al objeto DFAgentDescription dfd */
        dfd.addServices(sd);
        dfd.setName(getAID());

        /* Se registra el agente en el JADE DF (si ya estaba registrado, se borra lo que había y se registra de nuevo) */
        try {
            DFService.deregister(this);
        } catch (Exception e) {}
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            LOGGER.error(getLocalName() + " no registrado. Motivo: " + e.getMessage());
            doDelete();
        }
    }


    /* IREGISTER INTERFACE */

    /**
     * Este método registra un elemento de la categoría solicitada en el HashMap elements
     * Si entre los atributos facilitados se incluye el atributo ID, se utilizará este ID para el registro
     * Si no tiene un ID previamente asignado, se le genera uno automáticament en función de su categoría
     * @param prm String que indica la categoría del nuevo elemento a registrar
     * @param attribs HashTable con los atributos asociados al nuevo elemento a registrar
     * @return ID con el que se ha registrado al nuevo elemento en el modelo del sistema
     */
    private String reg(final String prm, Hashtable<String, String> attribs) {

        /* Se inicializan las variables que se van a utilizar en este método*/
        String id;

        /* Se comprueba si el elemento a registrar ya tiene un ID preasignado (se revisan sus atributos) */
        if (!attribs.containsKey("ID")) {

            /* Si no tiene ID preasignado, se crea uno nuevo en dos pasos */
            /* Primero, se cogen los 10 primeros caracteres de la categoría del del elemento a registrar (prm) */
            id = (prm.length() > 10)? prm.substring(0, 10): prm;
            id = id.toLowerCase();

            /* Segundo, se asigna un número */
            if (!count.containsKey(id)) {

                /* Si es el primer elemento de esa categoría, se le asigna un 1 y se registra en el HashMap count */
                count.put(id, 1);
            } else {

                /* De ahí en adelante, el número a asignar se incrementa de uno en uno  */
                count.put(id, (count.get(id)) + 1);
            }
            id = id + count.get(id);

        } else {

            /* Si ya tiene un ID preasignado, se utiliza y después es borrado del HashTable attribs */
            id = attribs.get("ID");
            attribs.remove("ID");
        }

        /* Por último, se añade la categoría al HashTable de attribs y el nuevo elemento en el HashMap elements */
        attribs.put("category", prm);
        elements.put(id, attribs);
        return id;
    }

    /**
     * Método que permite eliminar uno o todos los elementos del HashMap elements
     * @param prm Nombre del elemento a eliminar (*) si se queren borrar todos los elementos del HashMap
     * @return Mensaje de confirmación
     */
    private String del(String prm) {

        /* Variable con la que se va a devolver el resultado */
        String response = "not found";

        /* Se evalúa la expresión recibida en el parámetro prm */
        if (prm.equals("*")) {

            /* Si se recibe el caracter "*", se eliminan todos los elementos del HashMap */
            response = elements.size() + " deleted";
            elements.clear();
        } else if (elements.get(prm) != null) {

            /* Si se recibe el nombre de un elemento que esté en la lista, es eliminado */
            elements.remove(prm);
            response = "done";
        }

        return response;
    }


    /* IVALIDATE INTERFACE */

    /**
     * Método de alto nivel para realizar la validación de la jerarquía de una aplicación
     * @param se Elemento padre de la aplicación (en nuestro caso, un MPlan)
     * @param conversationId Identificador de la conversación a través de la que se ha solicitado la validación
     * @return Mensaje de confirmación de la validación (o de fallo en caso de que sea incorrecta)
     * @throws Exception Se eleva excepción si la validación resulta incorrecta
     */
    private String iValidate(String se, String conversationId) throws Exception {

        /* En primer lugar, se comprueba la jerarquía de la aplicación asociada al parámetro se */
        /* Para ello, se invoca el método validate, pasando como parámetros appHierarchy y se */
        /* Se printea un mensaje diferente por consola dependiendo del resultado de la validación */
        String validateHierarchy = validate("appHierarchy", se);
        if (!validateHierarchy.equals("valid")) {
            LOGGER.info(se+" Application: jerarquía incorrecta");
            throw new Exception();
        } else {
            LOGGER.info(se+" Application: jerarquía correcta");
        }

        /* Después, se invoca el método get para consultar el seParent del elemento asociado al parámetro se (debería ser system) */
        Hashtable<String, String> aux = new Hashtable<>();
        aux.put("attrib","seParent");
        String parentID = get(se, aux, conversationId);

        /* Por último, se invoca el método set para asignar el parentID al atributo parent, y eliminar el atributo seParent */
        aux.clear();
        aux.put("parent",parentID);
        aux.put("seParent","");
        set(se, aux, conversationId);
        return se;
    }

    /**
     * Este método comprueba la conformidad de la aplicación que se va a registrar
     * Para ello, se apoya en los ficheros XSD refereidos en el fichero de propiedades (sa.properties).
     * @param _prm String o array de String con la información necesaria para la validación
     * @return Mensaje de confirmación, que indica si la aplicación es válidad o hay un error de jerarquía
     * @throws Exception Error de lectura del documento generado
     */
    private String validate(String... _prm) throws Exception {

        /* Se inicializan las variables que se van a utilizar en este método*/
        String output = "valid";

        /* Se el primer campo del array para determinar qué tipo de validación hay que hacer */
        if ((_prm[0].equals("appConcept")) || (_prm[0].equals("appHierarchy"))){

            /* Si se ha entrado en el if con la instrucción appHierarchy, se pone un booleano a true */
            boolean appHierarchy = _prm[0].equals("appHierarchy");

            /* Se genera un objeto de tipo Document en función del valor de _prm[1] */
            /* Este Document constará una secuencia formada por el elemento asociado a _prm[1] y todos su hijos */
            /* El segundo parámetro (appHierarchy) indica si los elementos tienen que tener el ID entre sus atributos */
            Document document = listDocument(_prm[1], appHierarchy);

            /* Se declaran variables para poder crear un objeto de tipo Schema */
            /* Este Schema va a utilizarse para validar el objeto Document que se acaba de generar */
            /* El fichero a partir del cual crear el Schema dependerá de qué se quiere validar (hierarchy o concepts) */
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source schemaFile = new StreamSource(new StringReader(URLDecoder.decode(elements.get(appHierarchy ?"hierarchy":"concepts").get("xsd"), "UTF-8")));
            Schema schema = schemaFactory.newSchema(schemaFile);

            /* A continuación se crea una instancia del tipo de objeto Validator */
            /* Se va a utilizar Schema recién creado para validar el Document */
            Validator validator = schema.newValidator();
            try {
                validator.validate(new DOMSource(document));
            } catch (Exception e) {

                /* En caso de error en la validación, se guarda en la variable output para printearlo y devolverlo */
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                output = sw.toString();
            }

            /* Si ha habido un error se muestra por consola */
            if (output.indexOf(';')>0) output = output.substring(output.indexOf(";") + 2, output.indexOf('\n'));
            LOGGER.info(output);

        } else if (_prm[0].equals("hierarchy")) {

            /* Si se ha entrado en este else, se comprueba la relación jerárquica padre-hijo entre dos elementos */
            String father = _prm[2];
            String son = _prm[1];
            LOGGER.debug("comprobar jerarquía hijo="+ son +" padre="+ father);

            /* En este caso, se va a generar un objeto Document en el que se va a meter el SystemModel */
            /* Así, se crea el Document parseando el contenido del SystemModel (atributo xsd del elemento system) */
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(URLDecoder.decode(elements.get("system").get("xsd"), "UTF-8")));
            Document doc = docBuilder.parse(is);

            /* El siguiente paso consiste en obtener dos vectores que contengan los nombres y tipos de cada nodo, respectivamente */
            Vector<String> vNames = null;
            Vector<String> vTypes = null;

            /* Para ello se obtiene el listado de nodos y se itera */
            NodeList nl = doc.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {

                    /* Si el nodo es de tipo elemento, se obtienen su nombre y su tipo */
                    Element el = (Element) nl.item(i);
                    vNames =nodeListToVector(el, new Vector<>(), "name");
                    vTypes =nodeListToVector(el, new Vector<>(), "type");
                    break;
                }
            }

            /* A continuación, se desarrolla el algoritmo ideado por Eli para hacer la comprobación de la jerarquía */
            /* 1. Se busca el xs:element que tenga como atributo name o ref el valor deseado (hijo) */
            boolean encontrado=false;
            int posicion = 0;
            for (int i = 0; i< vNames.size(); i++) {
                if (vNames.get(i).equals("xs:element="+ son)) {

                    /* Si se encuentra el elemento buscado, se pone encontrado a true y se guarda la posición del vector */
                    encontrado = true;
                    posicion=i;
                    break;
                }
            }

            if (encontrado) {

                /* Se ha encontrado el elemento buscado: IR A PASO 2 */
                /* 2. Se comprueba que el padre del elemento que buscamos sea  xs:sequence */
                if (vNames.get(posicion-1).equals("xs:sequence=")){

                    /* El anterior es sequence: IR A PASO 3 */
                    /* 3. Se busca el predecesor más cercano que sea xs:complexType y se obtiene su atributo name */
                    encontrado=false;
                    String complexTypeName="";
                    for (int i=posicion-2; i>=0; i--) {
                        if (vNames.get(i).startsWith("xs:complexType")) {

                            /* Si se encuentra el xs:complexType, se pone encontrado a true y se guarda el nombre del xs:complexType */
                            encontrado=true;
                            complexTypeName= vNames.get(i).substring(vNames.get(i).indexOf("=")+1);
                            break;
                        }
                    }

                    if (encontrado) {

                        /* Se ha encontrado un complexType: IR A PASO 4 */
                        /* 4. Se busca un xs:element que tenga como atributo type el valor guardado en complexTypeName */
                        encontrado=false;
                        for (int i = 0; i < vTypes.size(); i++) {
                            if (vTypes.get(i).equals("xs:element=" + complexTypeName)) {

                                /* Si se encuentra el elemento buscado, se comprueba si el nombre coincide con el del parámetro padre */
                                encontrado=true;
                                if (father.equals(vNames.get(i).substring(vNames.get(i).indexOf("=") + 1))) {
                                    return "valid";
                                } else {
                                    return "ERROR de jerarquía";
                                }
                            }
                        }

                        if (!encontrado){

                            /* Si no se encuentra ningún elemento cuyo type coincida con el valor de complexTypeName, se devuelve error */
                            return "ERROR de jerarquía";
                        }
                    } else {

                        /* Si se termina de recorrer el Vector y no se ha encontrado ningún xs:complexType, el proceso ha fallado */
                        return "ERROR de jerarquía";
                    }
                } else {

                    /* Si el elemento anterior no es xs:sequence, se devuelve un mensaje de error */
                    return "ERROR de jerarquía";
                }
            } else {

                /* Si no se encuentra el elemento buscado, se devuelve un mensaje de error */
                return "ERROR de jerarquía";
            }
        }

        /* Si se ha entrado en el primer if (si la validación era de tipo appConcept o appHierarchy) se devuelve output */
        return output;
    }

    /**
     * Método para crear un objeto de tipo Document
     * Este Document tendrá al elemento correspondiente al parámetro prm como elemento raiz, además de todos sus hijos
     * @param prm String que contiene el nombre del elemento raíz del documento
     * @param mostrarID Booleano para determinar si hay que añadir el ID como atributo en los elementos del objeto Document
     * @return Objeto de tipo Document que contiene al elemento padre (elemento inquirido) y todos sus hijos
     * @throws Exception Se lanza una excepción si falla la creación del DOcumentBuilder
     */
    private Document listDocument(String prm, boolean mostrarID) throws Exception{

        /* Se declaran las variables que se van a utilizar en el método */
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Attr attr;

        /* Se recorre el HashMap de elementos uno a uno */
        for (String elementID : elements.keySet()) {

            /* Se busca el elemento que se llame igual que el valor del parámetro prm */
            if (elementID.matches(prm)) {

                /* Si lo encontramos, se crea el objeto de tipo Element rootElement a partir del atributo category */
                Element rootElement = doc.createElement(elements.get(elementID).get("category"));

                /* Después, se añade este elemento al documento doc creado al principio del método */
                doc.appendChild(rootElement);

                if (mostrarID) {

                    /* Si se cumple esta condición, se crea un atributo ID y se asigna al rootElement */
                    /* El valor del atributo es el nombre (key) del elemento que se está analizando */
                    attr = doc.createAttribute("ID");
                    attr.setValue(elementID);
                    rootElement.setAttributeNode(attr);
                }

                /* Se comprueban todos los atributos del elemento analizado y se añaden aquellos que nos interesan */
                /* En este caso category, parent, xsd y seParent */
                forKeys: for (String key : elements.get(elementID).keySet()) {
                    if (key.equals("category") || key.equals("parent") || key.equals("xsd")|| key.equals("seParent")) continue forKeys;
                    attr =  doc.createAttribute(key);
                    attr.setValue(elements.get(elementID).get(key));
                    rootElement.setAttributeNode(attr);
                }

                /* Se llama al método appendChildren para que añada la información de los elementos hijos al doc */
                appendChildren(doc, rootElement, elementID, mostrarID);
            }
        }
        return doc;
    }

    /**
     * Método recursivo para añadir a un objeto de tipo Document los elementos hijos del elemento raíz (parámetro parent)
     * @param doc Documento que ya contiene, como mínimo, elemento raíz (tiene más elementos si la llamada es recursiva)
     * @param parent Elemento para el que hay que buscar y añadir el elemento hijo (en caso de que tenga hijos)
     * @param parentID ID del elemento padre, utilizado para confirmar si un elemento es hijo suyo
     * @param mostrarID Booleano para determinar si hay que añadir el ID como atributo en los elementos del objeto Document
     */
    private void appendChildren(Document doc, Element parent, String parentID, boolean mostrarID) {

        /* Se declaran las variables que se van a utilizar en el método */
        Attr attr;

        /* Se recorre el HashMap de elementos uno a uno */
        for (String childID : elements.keySet()) {

            /* Se recorren todos los atributos de cada elemento uno a uno */
            for (String attrib : elements.get(childID).keySet()) {

                /* Se comprueba si el atributo parent de este elemento se corresponde con la variable parentID */
                if (attrib.equals("parent") && elements.get(childID).get(attrib).equals(parentID)) {

                    /* En caso afirmativo, se crea el elemento childElement y se añade como hijo del elemento parent */
                    Element childElement = doc.createElement(elements.get(childID).get("category"));
                    parent.appendChild(childElement);

                    if (mostrarID)  {

                        /* Si se cumple esta condición, se crea un atributo ID y se asigna al rootElement */
                        /* El valor del atributo es el nombre (key) del elemento que se está analizando */
                        attr = doc.createAttribute("ID");
                        attr.setValue(childID);
                        childElement.setAttributeNode(attr);
                    }

                    /* Se comprueban todos los atributos del elemento analizado y se añaden aquellos que nos interesan */
                    /* En este caso category, parent y xsd */
                    forKeys: for (String eachKey : elements.get(childID).keySet()) {
                        if (eachKey.equals("category") || eachKey.equals("parent") || eachKey.equals("xsd")) continue forKeys;
                        attr =  doc.createAttribute(eachKey);
                        attr.setValue(elements.get(childID).get(eachKey));
                        childElement.setAttributeNode(attr);

                    }

                    /* Por último se llama de forma recursiva al método para añadir todos los hijos (y completar el doc) */
                    appendChildren(doc, childElement, childID, mostrarID);
                }
            }
        }
    }

    /**
     * Método para recorrer todos los nodos de tipo element de un objeto Document
     * El objetivo es generar un Vector con el nombre del element y el el valor de un atributo
     * El parámetro attrib determina de qué atributo del element hay que consultar el valor
     * @param element Elemento a añadir al Vector v
     * @param v Vector en el que hay que añadir el elemento recibido en el parámetro element
     * @param attrib Tipo de atributo del que hay que consultar el valor
     * @return Vector actualizado con todos los elementos del objeto Document incluidos
     */
    private Vector<String> nodeListToVector(Element element, Vector<String> v, String attrib) {

        /* Se añade al Vector v el nombre del nodo junto con el valor del atributo que se quiere guardar (name o type) */
        v.add(element.getNodeName() + "=" +element.getAttribute(attrib));

        /* A continuación, se saca el listado de nodos hijos de element y se itera */
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {

            /* Si alguno de los nodos hijos de element es a su vez de tipo element, se llama recursivamente al método */
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                nodeListToVector((Element) nl.item(i), v, attrib);
            }
        }
        return v;
    }

    /* ISYSTEMINFO INTERFACE */

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

        // si es extensible el padre traigo la estructura desde el hijo de system con los atributos, (resolver su ID **hi**), validar appvalidar.xsd
        // si valida > volver a montarlo en systemmodel

        auxAttribs.clear();
        auxAttribs.put("attrib", "category");
        String parentType = get(parentId, auxAttribs, conversationId);
        if (parentType.equals("")) {
            LOGGER.info("ERROR: parent id not found"); //no existe padre
            throw new Exception();
        }
        LOGGER.info(parentId+" type="+parentType);

        //compruebo jerarquía // TODO si el padre es "system" comprobar que el se es raiz del appHierarchy xsd -> dom

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
        String ID = reg(command.split(" ")[1], processAttribs(2, command.split(" ")));

        // TODO: por cada restrictionList una llamada al get y comprobar que existen en el SystemModel
        for (String keyi: restrictionLists.keySet()){
            System.out.println("*******************key="+keyi);
            String query = "reg restrictionList se="+keyi+" parent="+ID;
            String restrictionList = reg(query.split(" ")[1], processAttribs(2, query.split(" ")));

            for (String keyj: restrictionLists.get(keyi).keySet()){
                query = "reg restriction attribName="+keyj+" attribValue="+restrictionLists.get(keyi).get(keyj)+" parent="+restrictionList;
                String restriction = reg(query.split(" ")[1], processAttribs(2, query.split(" ")));
                System.out.println("keyj="+keyj);
            }
        }

        //validar elemento contra esquema appConcepts

        String validation =  validate("appConcept", ID);
        LOGGER.info(validation);

        if (!validation.equals("valid")) {
            del(ID);
            LOGGER.info("error xsd concepts");
            throw new Exception();
            //throw new XSDException(validation);

        } else LOGGER.info("xsd concepts correcto");

        // mover a validation.xml


        if (parentId.equals("system")) {
            command = "set " + ID + " parent=validation";
            set(command.split(" ")[1], processAttribs(2, command.split(" ")), conversationId);
        } else {
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



