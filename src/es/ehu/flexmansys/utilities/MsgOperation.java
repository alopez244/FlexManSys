package es.ehu.flexmansys.utilities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Defines operation messaage content format. It is used to exchange content in
 * an ACL message between resources and batch agent.
 * 
 * @author Brais Fortes (@fortes23) - Euskal Herriko Unibersitatea
 * @author Mikel López (@lopeziglesiasmikel) - Euskal Herriko Unibersitatea
 */
public class MsgOperation implements Serializable {

	private static final long serialVersionUID = -1894516651508079719L;

	/** Possible resource types. */
	public static final int MACHINE = 0, TRANSPORT = 1;

	/** Identifier of the resource type. */
	private int ResourceType;

	/** Identifier of the operation progress.Start or finish. */
	private boolean Start;

	/** Identifier of the operation that is being performed. */
	private String OperationID;

	/** Identifier of the task that is being performed. */
	private String taskID;

	/** Identifier of the operation estimated time. */
	private long EstimatedTime;

	/** Identifier of the pallet position. */
	private Position position;

	// Contructor
	public MsgOperation(int ResourceType, boolean Start, String OperationID, String taskID) {
		this.ResourceType = ResourceType;
		this.Start = Start;
		this.OperationID = OperationID;
		this.EstimatedTime = -1;
		this.position = null;
		this.taskID = taskID;
	}

	// Contructor Machine Transport Start
	public MsgOperation(int ResourceType, boolean Start, String OperationID, String taskID, long EstimatedTime) {
		this(ResourceType, Start, OperationID, taskID);
		this.EstimatedTime = EstimatedTime;
	}

	// Contructor Machine Transport Finish
	public MsgOperation(int ResourceType, boolean Start, String OperationID, String taskID, Position position) {
		this(ResourceType, Start, OperationID, taskID);
		this.position = position;
	}

	// Getter methods
	public int getResourceType() {
		return this.ResourceType;
	}

	public boolean getStart() {
		return this.Start;
	}

	public String getOperationID() {
		return this.OperationID;
	}

	public String getTaskID() {
		return this.taskID;
	}

	public long getEstimatedTime() {
		return this.EstimatedTime;
	}

	public Position getPosition() {
		return this.position;
	}

	@Override
	public boolean equals(Object o) {

		if (o == this) {
			return true;
		}
		if (!(o instanceof MsgOperation)) {
			return false;
		}
		MsgOperation c = (MsgOperation) o;
		return ((ResourceType == c.getResourceType()) && (Boolean.compare(Start, c.getStart()) == 0)
				&& (OperationID.equals(c.getOperationID())) && (taskID.equals(c.getTaskID()))
				&& (Long.compare(EstimatedTime, c.getEstimatedTime()) == 0) && (position.equals(c.getPosition())));
	}

	@Override
	public int hashCode() {
		return Objects.hash(ResourceType, Start, OperationID, taskID, EstimatedTime, position);
	}
}