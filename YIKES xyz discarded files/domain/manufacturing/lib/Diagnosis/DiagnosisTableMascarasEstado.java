package es.ehu.domain.manufacturing.lib.Diagnosis;

/**
 * Clase la definicion del contenido de las tablas de diagnostico
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/

public class DiagnosisTableMascarasEstado {
	private byte _reconAction = 0;
	private String _id = "";
	private byte[] _value;
	private byte[] _mask;
	private byte[] _stateAND;
	private byte[] _stateOR;

	public DiagnosisTableMascarasEstado(byte reconAction, String id, byte[] mask,
			byte[] value, byte[] stateAND, byte[] stateOR) {
		super();
		this._reconAction = reconAction;
		this._id = id;
		this._value = value;
		this._mask = mask;
		this._stateAND = stateAND;
		this._stateOR = stateOR;
	}

	public byte get_reconAction() {
		return _reconAction;
	}

	public String get_id() {
		return _id;
	}

	public byte[] get_value() {
		return _value;
	}

	public byte[] get_mask() {
		return _mask;
	}

	public byte[] get_stateAND() {
		return _stateAND;
	}
	
	public byte[] get_stateOR() {
		return _stateOR;
	}

}
