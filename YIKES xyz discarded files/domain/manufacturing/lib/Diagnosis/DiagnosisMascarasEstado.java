package es.ehu.domain.manufacturing.lib.Diagnosis;

import java.io.File;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Clase con los funcionaes para realizar el diagnostico
 * 
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/
public class DiagnosisMascarasEstado {
  static final Logger LOGGER = LogManager.getLogger(DiagnosisMascarasEstado.class.getName());
	private DiagnosisTableMascarasEstado[] diagnosisTables;

	public DiagnosisMascarasEstado(String pathXML) {
		loadDiagnosisTable(pathXML);

		// LOGGER.info("Init");
		// loadDiagnosisTable("X:/DCMP_PLC_Sup/DiagnosisEstacion1.xml");
		// loadDiagnosisTable("C:/DCMP_MW/examples/DCMP_PLC_Sup/DiagnosisEstacion1.xml");

	}

	private void loadDiagnosisTable(String XMLFile) {
	  LOGGER.entry(XMLFile);
		// Rellenar la estructura
		try {
			// Cargar la tabla contenida en el XML
			File fXmlFile = new File(XMLFile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			Element digNode = (Element) doc.getElementsByTagName("Diagnosis").item(0);
			// TODO Element digNode = (Element) doc.getElementsByTagName("diagnosis").item(0);
			// Obtener el nombre
			NodeList nList = digNode.getElementsByTagName("ExPoint");
			//TODO NodeList nList = digNode.getElementsByTagName("criticalInterval");
			diagnosisTables = new DiagnosisTableMascarasEstado[nList.getLength()];
			// Rellenar los sitintos nodos
			for (int i = 0; i < nList.getLength(); i++) {
				Element elRec = (Element) nList.item(i);
				// Accion de reconfiguracion
				byte reconfAction = Byte.valueOf(elRec
						.getAttribute("reconfAction"));
				// ID del punto de ejecucion
				String modeId = elRec.getAttribute("id");
				// Rellenar los array de estado, mascara y valor
				Element elState = (Element) elRec.getElementsByTagName("DiagInf").item(0);
				//TODO Element elState = (Element) elRec.getElementsByTagName("eqMask").item(0);
				// Inicializar los arrays
				int sizeState = Integer.valueOf(elState.getAttribute("size"));
				byte[] mascara = new byte[sizeState];
				byte[] valores = new byte[sizeState];
				byte[] estadoAND = new byte[sizeState];
				byte[] estadoOR = new byte[sizeState];
				// Rellenar los arrays
				NodeList nVariable = elState.getElementsByTagName("varDiag");
				for (int temp = 0; temp < nVariable.getLength(); temp++) {
					Node nNode = nVariable.item(temp);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						int posicion = Integer.valueOf(eElement
								.getAttribute("position"));

						int aux = Integer.valueOf(eElement.getAttribute("mask"));
						byte mask = Byte.valueOf((byte) aux);
						aux = Integer.valueOf(eElement.getAttribute("value"));
						byte valor = Byte.valueOf((byte) aux);
						aux = Integer.valueOf(eElement.getAttribute("stateAND"));
						byte stateAND = Byte.valueOf((byte) aux);
						aux = Integer.valueOf(eElement.getAttribute("stateOR"));
						byte stateOR = Byte.valueOf((byte) aux);

						mascara[posicion] = mask;
						valores[posicion] = valor;
						estadoAND[posicion] = stateAND;
						estadoOR[posicion] = stateOR;
					}
				}
				DiagnosisTableMascarasEstado aux = new DiagnosisTableMascarasEstado(reconfAction, modeId,
						mascara, valores, estadoAND, estadoOR);
				diagnosisTables[i] = aux;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.exit();
	}
	
	/**
	 * Determines the mode which corresponds with the given state.
	 * 
	 * @param state
	 *            : the current state of the component
	 * @return only the state and id in the reply class
	 * @throws Exception
	 */
	public reply checkDiagnosis(byte[] state) throws Exception {
	  LOGGER.entry(state);
		// Comprobar si el estado concuerda con alguna de los estado del
		// diagnostico
		for (int i = 0; i < diagnosisTables.length; i++) {
			
      // LOGGER.debug(" ======= tamaño tabla: "+diagnosisTables.length);
      // LOGGER.debug(" ======= num tabla: "+i);
      // LOGGER.debug(" ======= tamano estado: "+ state.length);
      // LOGGER.debug(" ======= Tamaño de las mascara: "+diagnosisTables[i].get_mask().length);
		  
		  // Comprobar si el estado tiene el tamaño correcto
			if (diagnosisTables[i].get_mask().length == state.length) {
				// Aplicar la mascara AND de diagnostico
				byte[] aux1 = MaskAND(diagnosisTables[i].get_mask(), state);
				// Comprobar si son iguales
				if (Arrays.equals(aux1, diagnosisTables[i].get_value())) {
					// Generar el estado mediante Mascaras				
					
					byte[] estadoAux1 = MaskAND(diagnosisTables[i].get_stateAND(), state);
					byte[]estadoAux = MaskOR(diagnosisTables[i].get_stateOR(), estadoAux1);
					//for(int j=0; j<estadoAux.length;j++){
					// LOGGER.info("entarda: "+state[j] +" - AND  "+diagnosisTables[i].get_stateAND()[j]+ " - res: "+estadoAux1[j]+" -OR: "+diagnosisTables[i].get_stateOR()[j]+" res:"+estadoAux[j]);
					//}
					return LOGGER.exit(new reply(diagnosisTables[i].get_id(), estadoAux, diagnosisTables[i].get_reconAction()));
				}
			} else {
				throw new Exception("The size of the state is incorrect");
			}
		}
		return LOGGER.exit(new reply("0", state, (byte)0));
	}

	/**
	 * Hacer una mascara AND entre array
	 * 
	 * @param mascara
	 * @param dato
	 * @return
	 */
	private byte[] MaskAND(byte[] mascara, byte[] dato) {
	  LOGGER.entry(mascara, dato);
		byte[] exit = new byte[mascara.length];
		for (int i = 0; i < exit.length; i++) {
			if (mascara[i] == (byte) 0) {
				exit[i] = 0;
			} else {
				exit[i] = (byte) (dato[i] & mascara[i]);
			}

		}
		return LOGGER.exit(exit);
	}

	/**
	 * Hacer una mascara OR entre array
	 * 
	 * @param mascara
	 * @param dato
	 * @return
	 */
	private byte[] MaskOR(byte[] mascara, byte[] dato) {
	  LOGGER.entry(mascara, dato);
		byte[] exit = new byte[mascara.length];
		for (int i = 0; i < exit.length; i++) {
			exit[i] = (byte) (dato[i] | mascara[i]);
		}
		return LOGGER.exit(exit);
	}

	public static void main(String[] args) {
		new DiagnosisMascarasEstado(
				"bin/XML/DiagnosisEstacion1MascaraEstado.xml");
		// "D:/Documents and Settings/GCIS/Escritorio/pruebaDiagnostico.xml");

		LOGGER.info("fin");

	}

	/**
	 * Clase que contiene la respuesta del diagnostico del estado
	 * 
	 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
	 *
	 */
	public class reply {
		private String _id = "";
		private byte[] _state;
		private byte _action;

		public reply(String id, byte[] state, byte action) {
			this._id = id;
			this._state = state;
			this._action = action;
		}

		public String get_id() {
			return _id;
		}

		public byte[] get_state() {
			return _state;
		}
		
		public byte  get_action() {
      return _action;
    }
	}
}
