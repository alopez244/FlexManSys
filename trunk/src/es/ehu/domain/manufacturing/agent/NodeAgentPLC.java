package es.ehu.domain.manufacturing.agent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import de.beckhoff.jni.Convert;
import es.ehu.NodeAgent_obsoleto;
import es.ehu.domain.manufacturing.lib.CallAdsFuncs;
import es.ehu.domain.manufacturing.lib.TypeVar;
import es.ehu.domain.manufacturing.lib.Variable;

public class NodeAgentPLC extends NodeAgent_obsoleto {
  static final Logger LOGGER = LogManager.getLogger(NodeAgent_obsoleto.class.getName());
  private static final long serialVersionUID = 1L;

  private int workloadPLC = 0;

  // int Eliminar=0;

  @Override
  protected void extraBehaviour(Agent a) {
    // Prepara el comportamiento de calcular la carga de trabajo
    Behaviour workloafCalculate = new WorkLoadCalculate(this, 10);
    addBehaviour(workloafCalculate);
  }

  @Override
  protected double systemLoad() {
    return (double) workloadPLC;
  }

  public int getWorkloadPLC() {
    return workloadPLC;
  }

  public void setWorkloadPLC(int WorkloadPLC) {
    this.workloadPLC = WorkloadPLC;
  }

  private class WorkLoadCalculate extends TickerBehaviour {
    private static final long serialVersionUID = 5827993056861243328L;

    private int[] iaWorkLoad; // = new int [10];
    private int iAVGWorkLoad;
    private int iTick;
    private boolean bStart;
    private NodeAgentPLC myAgent;

    private CallAdsFuncs callAdsFuncs = new CallAdsFuncs();
    private Variable workloadPercent = new Variable();
    private TypeVar sizeType = new TypeVar();

    /**
     * Calcula la carga de trabajo de un sistema
     * 
     * @param a
     *          :
     * @param period
     *          : (ms) intervalo de tiempo entre cada ejecución del sistema.
     */
    public WorkLoadCalculate(NodeAgentPLC a, long period) {
      super(a, period);
      myAgent = a;
    }

    @Override
    public void onStart() {
      super.onStart();
      iTick = 0;
      bStart = false;

      iaWorkLoad = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
      iAVGWorkLoad = -1;

      // Abrir el pueto de comunicacion con el PLC
      callAdsFuncs.openPort();

      // Rellenar variables que contiene la carga de trabajo
      workloadPercent.setName("MAIN.cpuCharge_percent");
      workloadPercent.setType("UDINT");
      try {
        workloadPercent.setHandler(callAdsFuncs.getHandleBySymbol(workloadPercent.getName()));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    @Override
    protected void onTick() {

      if (iTick == 9) {
        bStart = true;
        iTick = 0;
      } else {
        iTick++;
      }

      // Guardamos el valor de la carga de trabajo en el array
      try {
        iaWorkLoad[iTick] = Convert.ByteArrToInt(callAdsFuncs.readByHandle(
            workloadPercent.getHandler(), sizeType.get(workloadPercent.getType())));
      } catch (Exception e) {
        e.printStackTrace();
      }

      // System.out.println("Valor de tiempo de ciclo del NODO: " + iaWorkLoad[iTick] );
      if (bStart) {
        iAVGWorkLoad = 0;
        for (int i = 0; i < 9; i++) {
          iAVGWorkLoad = (iAVGWorkLoad + iaWorkLoad[i]);
        }
        myAgent.setWorkloadPLC(iAVGWorkLoad / 10);

        // //Calcular carga de trabajo contorlaores
        // Eliminar++;
        // if(Eliminar==100){
        // LOGGER.info("========= MEDIA Estaciones: "+iAVGWorkLoad / 10);
        // Eliminar=0;
        // }
      }
      // System.out.println("Valor medio del tiempo de ciclo del NODO: "+ iAVGWorkLoad/10);
    }

  }

}
