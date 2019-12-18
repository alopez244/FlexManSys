package es.ehu.domain.eHealth.message;

import java.io.Serializable;
import java.util.Date;

public class MsgHR_CheckInterval2HR_CheckRelaxed implements Serializable {
	private static final long serialVersionUID = 1L;
	private int param_1;
	private Date param_2;
	private int param_3;
		
	public MsgHR_CheckInterval2HR_CheckRelaxed (int param_1, Date param_2, int param_3) {
		this.param_1 = param_1;
		this.param_2 = param_2;
		this.param_3 = param_3;			
	}
	
	public int getparam_1() {
		return this.param_1;
	}
	public Date getparam_2() {
		return this.param_2;
	}
	public int getparam_3() {
		return this.param_3;
	}
}
