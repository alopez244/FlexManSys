package es.ehu.domain.manufacturing.lib;

/**
 * Clase con los datos de las variables que se necesitan para el estado
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/

public class Variable {
	private String name = "";
	private String type = "";
	private long handler;
	private byte[] value;
	
	public byte[] getValue() {
		return value;
	}
	public void setValue(byte[] value) {
		this.value = value;
	}
	public long getHandler() {
		return handler;
	}
	public void setHandler(long handler) {
		this.handler = handler;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
