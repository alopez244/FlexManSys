package es.ehu.domain.eHealth.functionality;

import java.io.Serializable;
import java.util.Date;

public class Measure implements Serializable {		
	private static final long serialVersionUID = 1L;
	public int value;
	public Date timeStamp;
	
	public Measure (int value, Date timeStamp) {
		this.value = value;
		this.timeStamp = timeStamp;
	}
}
