package es.ehu.domain.manufacturing.lib;

/**
 * Clase la gestion y el alamacenamiento del estado
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.beckhoff.jni.Convert;

public class StateHandler {
  static final Logger LOGGER = LogManager.getLogger(StateHandler.class.getName());
  
	private CallAdsFuncs callAdsFuncs = new CallAdsFuncs();
	// Varaibles del PLC
	private ArrayList<Variable> _estado = new ArrayList<Variable>();
	private Variable _executionFlag = new Variable();
	private Variable _stateArrayDatos = new Variable();
	// Variable para almacenar el tamaño del array estadoSend
	private int sizeEstadoSend = 0;
	// Para saber el numero de bytes que ocupa un tipo
	private TypeVar sizeType = new TypeVar();
	// Variale para guardar el etado
	private byte[] _state;

	/**
	 * Clase que permite terabajar con el estado
	 * 
	 * @param XMLInit
	 *            : Path ddel archivo XML que contien el estado
	 * @param executionFlagName
	 *            : Nombre de la variable de fLag para iniciar el programa
	 * @param stateArrayName
	 *            : Nombre del array donde esta el estado
	 * @throws Exception
	 */
	public StateHandler(String XMLInit, String executionFlagName,
			String stateArrayName) throws Exception {
		// Se inicializa las variables del estado
		initialize(XMLInit, executionFlagName, stateArrayName);

	}

	/**
	 * Obtener el estado en bytes guardado
	 * 
	 * @return un array de bytes con el estado
	 */
	public byte[] getState() {
		return _state;
	}

	/**
	 * Almacenar el estado
	 * 
	 * @param state
	 *            : el estado a guardar
	 */
	public void setState(byte[] state) {
		this._state = state;
	}

	/**
	 * Inicializar la estructura estado con el nombre , el tipo y le handler,
	 * rellena la variable que contiene el flag de ejecucion y nos da el
	 * numIOName que es una varible que dice la posiscion de la estructura
	 * estado desde donde empizan las varibles (separado variables de las I/O).
	 * 
	 * @throws Exception
	 */
	private void initialize(String XMLInit, String executionFlagName, String stateArrayName) throws Exception {
		/***********************************************************************
		 * Open communication por ADB
		 ***********************************************************************/
		callAdsFuncs.openPort();
		// Rellenar la estructura
		File fXmlFile = new File(XMLInit);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("variable");

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Variable aux = new Variable();
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				aux.setName(eElement.getAttribute("name"));
				aux.setType(eElement.getAttribute("type"));
				aux.setHandler(callAdsFuncs.getHandleBySymbol(aux.getName()));
				if (aux.getHandler() != 0) {
					_estado.add(aux);
					sizeEstadoSend = sizeEstadoSend
							+ sizeType.get(aux.getType());
				} else {
					System.exit(0);
				}
			}
		}

		// Rellenar variables que gestiona la ejecuccion del programa
		_executionFlag.setName(executionFlagName);
		_executionFlag.setType("BOOL");
		_executionFlag.setHandler(callAdsFuncs
				.getHandleBySymbol(executionFlagName));

		// Rellenar variables que gestiona la ejecuccion del programa
		_stateArrayDatos.setName(stateArrayName);
		_stateArrayDatos.setType("ARRAY");
		_stateArrayDatos.setHandler(callAdsFuncs
				.getHandleBySymbol(stateArrayName));

		System.out.println("Inicialization completed");
	}

	/**
	 * Rellena el array de datos (estadoSend) con los valores de las I/O y
	 * variables del programa.
	 * 
	 * @return Array de bytes con el estado
	 * @throws Exception
	 */
	public byte[] readState() throws Exception {
		// Leer el array de datos que contien el estado
		return callAdsFuncs.readByHandle(_stateArrayDatos.getHandler(),
				sizeEstadoSend);
	}

	/**
	 * Escribe los valores de las variables del programa.
	 * 
	 * @throws Exception
	 */
	private void writeState() throws Exception {
		// Escribir las variables
		for (int i = 0; i < _estado.size(); i++) {
			callAdsFuncs.writeByHandle(_estado.get(i).getHandler(), _estado
					.get(i).getValue());
		}
	}

	/**
	 * Empieza el programa de PLC.
	 * 
	 * @param recovery_mode
	 *            : modo de recuperacion
	 * @throws Exception
	 */
	public void StartPLCProgram() throws Exception {
		// Encender la ejecicion del programa
		byte[] data = new byte[sizeType.get(_executionFlag.getType())];
		data = Convert.ByteToByteArr((byte) 1); //Funciona con la variable de start
		callAdsFuncs.writeByHandle(_executionFlag.getHandler(), data);
	}

	/**
	 * Para el programa de PLC.
	 * 
	 * @throws Exception
	 */
	public void StopPLCProgram() throws Exception {
		// Encender la ejecucion del programa
		byte[] data = new byte[sizeType.get(_executionFlag.getType())];
		data = Convert.ByteToByteArr((byte) 0);//Funciona con la variable de start
		callAdsFuncs.writeByHandle(_executionFlag.getHandler(), data);
	}

	/**
	 * Rellena los valores de la estructura de estados con los datos del array
	 * de datos
	 * 
	 * @param estadoRecive
	 *            : array de bytes a deserializar
	 */
	private void deserialize(byte[] estadoRecive) {
		int pointerData = 0;
		
		for (int i = 0; i < _estado.size(); i++) {
			byte[] aux = new byte[sizeType.get(_estado.get(i).getType())];
			for (int j = 0; j < aux.length; j++) {
				aux[j] = estadoRecive[pointerData];
				pointerData++;
			}
			_estado.get(i).setValue(aux);
		}
		
//		for (int i = 0; i < _estado.size(); i++) {
//			byte[] aux = new byte[sizeType.get(_estado.get(i).getType())];
//			System.out.print(_estado.get(i).getName()+": ");
//			for (int j = 0; j < aux.length; j++) {
//				System.out.print(_estado.get(i).getValue()[j]);
//			}
//			System.out.println("");
//		}
	}

	/**
	 * Escribir el estado en la funcionalidad
	 * 
	 * @param state
	 *            : estado a recuperar
	 * @throws Exception
	 */
	public void recover(byte[] state) throws Exception {
		deserialize(state);
		writeState();
	}

	/**
	 * Lee el tiempo del ciclo del PLC
	 * 
	 * @return tiempo de ciclo
	 */
	public int cycletime() {
		// TODO en est momento se hace aleatorio pero hay que leerlo del PCL
		Random generator = new Random();
		return generator.nextInt();
	}

}
