package es.ehu.domain.manufacturing.lib;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Es el interface que permite al agente interactuar con el PLC.
 * 	- Lectura y escritura del estado
 * 	- Diagnostico del estado
 * 	- Lectura de la carga de trabajo 
 * 
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco V0.0
 * @author Tomás Vergel Corada - EHU
 **/

import es.ehu.domain.manufacturing.lib.Diagnosis.DiagnosisMascarasEstado;

public class FunctionalityPLC implements Serializable {
  private static final long serialVersionUID = -5860503544920968020L;
  static final Logger LOGGER = LogManager.getLogger(FunctionalityPLC.class.getName());

  // Clase de comunicaciones con el Beckhoff
  private StateHandler stateHan;
  // Clase para el diagnostico
  private DiagnosisMascarasEstado diagnosis;
  // ID del Componente Mecatronico basico
  public String MC_ID;
  // Identificador del controlador en el que se encuentra el Job
  public String controllerID;

  public FunctionalityPLC() {

  }

  public void initialization(String XMLState, String codeExecutionFlag, String stateArray,
      String XMLDiagnosis) {
    try {
      // Inicializar las comunicaciones con el PLC
      stateHan = new StateHandler(XMLState, codeExecutionFlag, stateArray);
      // Inicializacion del diagnostico
      diagnosis = new DiagnosisMascarasEstado(XMLDiagnosis);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public byte[] readState() throws Exception {
    return stateHan.readState();
  }

  /**
   * Calculate the cycle time
   * 
   * @return the value of the cycle time.
   */
  public int cycleTime() {
    return stateHan.cycletime();
  }

  /**
   * Save the state of the functionality.
   * 
   * @param state
   */
  public void setState(Object state) {
    stateHan.setState((byte[]) state);
  }

  /**
   * Get the state that is stored
   * 
   * @return
   */
  public byte[] getState() {
    return stateHan.getState();
  }

  /**
   * Realiza el dianostico del estado y la recuperacion el el PLC.
   * 
   * @throws Exception
   */
  public void diagnosisRecover() throws Exception {
    LOGGER.entry();

    LOGGER.debug("Empezando diagnostico");
    DiagnosisMascarasEstado.reply rec = diagnosis.checkDiagnosis(stateHan.getState());
    LOGGER.debug("dignostico terminado: " + rec.get_id());

    // for (int i=0; i<stateHan.getState().length;i++){
    // System.out.println(stateHan.getState()[i] + " - "+rec.get_state()[i]);
    // }

    // Recuperacion
    LOGGER.debug("Empezando recuperaccion");
    stateHan.recover(rec.get_state());
    LOGGER.debug("recuperacion ternimada");

    LOGGER.exit();
  }

  /**
   * Realiza el diagnostico del estado
   * @param state
   * @return el id de las acion a realizar: en el caso de ser 0 es diectamente recuperabla, mienteras otro numero significa que no lo es
   * @throws Exception
   */
  public String diagnosis(byte[] state) throws Exception {
    LOGGER.entry(state);

    LOGGER.debug("Empezando diagnostico");
    DiagnosisMascarasEstado.reply rec = diagnosis.checkDiagnosis(state);
    LOGGER.debug("dignostico terminado: " + rec.get_id());

    return LOGGER.exit(rec.get_id());
  }

  /**
   * Start the execution of the MC code
   * 
   * @throws Exception
   */
  public void startMCcode() throws Exception {
    stateHan.StartPLCProgram();
  }

  /**
   * Stop the execution of the MC code
   * 
   * @throws Exception
   */
  public void stopMCcode() throws Exception {
    stateHan.StopPLCProgram();
  }
}
