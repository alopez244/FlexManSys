package es.ehu.flexmansys.utilities;

import java.io.Serializable;
import java.util.Objects;

import jade.core.AID;

/**
 * Defines timeout class.
 * 
 * @author Brais Fortes (@fortes23) - Euskal Herriko Unibersitatea
 * @author Mikel López (@lopeziglesiasmikel) - Euskal Herriko Unibersitatea
 */
public class Timeout implements Serializable {

	private static final long serialVersionUID = -6293097527445002047L;

	/** resource AID. */
	private AID resourceAID;

	/** timeout value. */
	private long timeoutValue;

	public Timeout(AID resource, long timeout) {
		this.resourceAID = resource;
		this.timeoutValue = timeout;
	}

	// Getter methods
	public AID getResourceAID() {
		return this.resourceAID;
	}

	public long getTimeout() {
		return this.timeoutValue;
	}

	// Setter methods
	public void setTimeout(long in) {
		this.timeoutValue = in;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Timeout)) {
			return false;
		}
		Timeout c = (Timeout) o;
		return ((Long.compare(timeoutValue, c.getTimeout()) == 0) && (Objects.equals(resourceAID, c.getResourceAID())));
	}

	@Override
	public int hashCode() {
		return Objects.hash(timeoutValue, resourceAID);
	}

}