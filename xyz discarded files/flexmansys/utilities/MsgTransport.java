package es.ehu.flexmansys.utilities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Defines information needed in transports negotiations.
 * 
 * @author Brais Fortes (@fortes23) - Euskal Herriko Unibersitatea
 * @author Mikel López (@lopeziglesiasmikel) - Euskal Herriko Unibersitatea
 */
public class MsgTransport implements Serializable {

	private static final long serialVersionUID = 9136753992718878097L;

	/** Identifier of the operation that machine is going to perform. */
	private String palletID;

	/** Identifier of the subproduct requested. */
	private Position initPosition;

	/** Identifier of the postion of the machine palletin. */
	private Position finalPosition;

	// Contructor
	public MsgTransport(String palletID, Position initPos, Position finalPos) {
		this.palletID = palletID;
		this.initPosition = initPos;
		this.finalPosition = finalPos;
	}

	// Getter methods
	public String getPalletID() {
		return this.palletID;
	}

	public Position getInitPos() {
		return this.initPosition;
	}

	public Position getFinalPos() {
		return this.finalPosition;
	}

	@Override
	public boolean equals(Object o) {

		if (o == this) {
			return true;
		}
		if (!(o instanceof MsgTransport)) {
			return false;
		}
		MsgTransport c = (MsgTransport) o;
		return (finalPosition.equals(c.getFinalPos()))
				&& (initPosition.equals(c.getInitPos()) && (palletID.equals(c.getPalletID())));
	}

	@Override
	public int hashCode() {
		return Objects.hash(palletID, initPosition, finalPosition);
	}

}