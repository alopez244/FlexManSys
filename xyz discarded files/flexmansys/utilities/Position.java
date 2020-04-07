package es.ehu.flexmansys.utilities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Defines position class.
 * 
 * @author Brais Fortes (@fortes23) - Euskal Herriko Unibersitatea
 * @author Mikel López (@lopeziglesiasmikel) - Euskal Herriko Unibersitatea
 */
public class Position implements Serializable {

	private static final long serialVersionUID = 9221108474401012677L;

	/** x position of the location. */
	private float xPos;

	/** y position of the location. */
	private float yPos;

	public Position(float xPos, float yPos) {
		this.xPos = xPos;
		this.yPos = yPos;
	}

	// Getter methods
	public float getxPos() {
		return this.xPos;
	}

	public float getyPos() {
		return this.yPos;
	}

	// Setter methods
	public void setPos(float xPos, float yPos) {
		this.xPos = xPos;
		this.yPos = yPos;
	}

	public void setPos(Position pos) {
		this.xPos = pos.getxPos();
		this.yPos = pos.getyPos();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Position)) {
			return false;
		}
		Position c = (Position) o;
		return ((Float.compare(xPos, c.getxPos()) == 0) && (Float.compare(yPos, c.getyPos()) == 0));
	}

	@Override
	public int hashCode() {
		return Objects.hash(xPos, yPos);
	}
}
