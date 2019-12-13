package es.ehu.platform.utilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import jade.core.AID;

/**
 * Defines negotiation message content format..
 * 
 * @author Brais Fortes (@fortes23) - Euskal Herriko Unibersitatea
 * @author Mikel López (@lopeziglesiasmikel) - Euskal Herriko Unibersitatea
 * @author Jon Martin (@jonmg) - Euskal Herriko Unibersitatea
 * 
 */

// targets, taskId, negAction and criterion
public class MsgNegotiation implements Serializable {

	private static final long serialVersionUID = -1266245015349901329L;

	/** Targets taking part in the negotiation. */
	private AID[] targets;

	/** Task identifier. */
	private String taskID;

	/** Action to perform if the negotiation is won. */
	private String negAction;

	/** Criteria for the calculation of the negotiation value. */
	private String criterion;

	/** Extra required data for the calculation of the negotiation value. */
	private Object[] externalData;

	
	public MsgNegotiation(Iterator<AID> iter, String taskID, String negAction, String criterion, Object... externalData) {
	  
	  List<AID> copy = new ArrayList<AID>();
	  while (iter.hasNext()) copy.add(iter.next());
	  
	  this.targets = new AID[copy.size()];
	  for (int i=0;i<this.targets.length; i++)
	    this.targets[i]=copy.get(i);
	     
	    
	  
	  //this.targets = (AID[]) copy.toArray();
	      //.toArray<AID>(this.targets);
	   
	   
	  
    //this.targets = targets;
    this.taskID = taskID;
    this.negAction = negAction;
    this.criterion = criterion;
    this.externalData = externalData;
  }
	
	
  
	// Contructor
	public MsgNegotiation(AID[] targets, String taskID, String negAction, String criterion, Object... externalData) {
		this.targets = targets;
		this.taskID = taskID;
		this.negAction = negAction;
		this.criterion = criterion;
		this.externalData = externalData;
	}

	// Getter methods
	public AID[] getTargets() {
		return this.targets;
	}

	public String getTaskID() {
		return this.taskID;
	}

	public String getNegAction() {
		return this.negAction;
	}

	public String getCriterion() {
		return this.criterion;
	}

	public Object[] getExternalData() {
		return this.externalData;
	}

	@Override
	public boolean equals(Object o) {

		if (o == this) {
			return true;
		}
		if (!(o instanceof MsgNegotiation)) {
			return false;
		}
		MsgNegotiation c = (MsgNegotiation) o;
		return (negAction.equals(c.getNegAction())) && criterion.equals(c.getCriterion())
				&& Objects.equals(externalData, c.getExternalData()) && Objects.equals(taskID, c.getTaskID())
				&& Objects.equals(targets, c.getTargets());
	}

	@Override
	public int hashCode() {
		return Objects.hash(targets, taskID, negAction, criterion, externalData);
	}
}