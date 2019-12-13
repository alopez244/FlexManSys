package es.ehu.flexmansys.utilities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Defines operation request message content format. It is used to exchange
 * content in an ACL message between machine and batch agent.
 * 
 * @author Brais Fortes (@fortes23) - Euskal Herriko Unibersitatea
 * @author Mikel López (@lopeziglesiasmikel) - Euskal Herriko Unibersitatea
 */
public class MsgReqOperation implements Serializable {

	private static final long serialVersionUID = 1090032672153652352L;

	/** Identifier of the operation that machine is going to perform. */
	private String operationID;

	/** Identifier of the subproduct requested. */
	private String subproductID;

	/** Identifier of the postion of the machine palletin. */
	private Position finalPosition;

	// Contructor
	public MsgReqOperation(String operationID, String subproductID, Position finalPosition) {
		this.operationID = operationID;
		this.subproductID = subproductID;
		this.finalPosition = finalPosition;
	}

	// Getter methods
	public String getOperationID() {
		return this.operationID;
	}

	public String getSubproductID() {
		return this.subproductID;
	}

	public Position getFinalPosition() {
		return this.finalPosition;
	}

	@Override
	public boolean equals(Object o) {

		if (o == this) {
			return true;
		}
		if (!(o instanceof MsgReqOperation)) {
			return false;
		}
		MsgReqOperation c = (MsgReqOperation) o;
		return (finalPosition.equals(c.getFinalPosition()))
				&& (subproductID.equals(c.getSubproductID()) && (operationID.equals(c.getOperationID())));
	}

	@Override
	public int hashCode() {
		return Objects.hash(finalPosition, subproductID, operationID);
	}

}