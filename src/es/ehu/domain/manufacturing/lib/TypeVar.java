package es.ehu.domain.manufacturing.lib;

/**
 * Clase la gestionar la los tipos de variables que da el  PLC
 * @author Rafael Priego Rementeria - Universidad del Pais Vasco
 **/

import java.util.HashMap;
import java.util.Map;

import de.beckhoff.jni.Convert;

public class TypeVar
{
	// Para saber el numero de bytes que ocupa un tipo
	private final Map<String, Integer> sizeType = new HashMap<String, Integer>()
	{
		private static final long serialVersionUID = 7615348566331587000L;
		{
			put("BOOL", 1);
			put("BYTE", 1);
			put("WORD", 2);
			put("DWORD", 4);
			put("SINT", 1);
			put("INT", 2);
			put("DINT", 4);
			put("USINT", 1);
			put("UINT", 2);
			put("UDINT", 4);
			put("REAL", 4);
			put("LREAL", 8);
			put("TIME", 4);
			put("TOD", 4);
			put("DATE", 4);
			put("DT", 4);
		}
	};
	
	/**
	 * Obtener el numero de bytes que ocupa una variable del tipo especificado
	 * @param type: tipo de variable
	 * @return numero de bytes
	 */
	public int get(String type){
		if (type.contains("Array")){
			return 0;
		}else {
			return sizeType.get(type);
		}
	}
	
	/**
	 * Mete el valor que se le pasa a un array de bytes. El tama�o del array depende del tipo de variable.
	 * @param value: valor a comvertir
	 * @param type: tipo de la variable 
	 * @return
	 */
	public byte[] convetirArray(String value, String type)
	{
		byte[] salida;
		switch (type)
		{
			case "BOOL":
				salida = new byte[1];
				salida=Convert.ByteToByteArr(Byte.valueOf(value));
				break;
			case "BYTE":
				salida = new byte[1];
				salida=Convert.ByteToByteArr(Byte.valueOf(value));
				break;
			case "WORD":
				salida = new byte[2];
				salida=Convert.ShortToByteArr(Short.valueOf(value));
				break;
			case "DWORD":
				salida = new byte[4];
				salida=Convert.IntToByteArr(Integer.valueOf(value));
				break;
			case "SINT":
				salida = new byte[1];
				salida=Convert.ByteToByteArr(Byte.valueOf(value));
				break;
			case "INT":
				salida = new byte[2];
				salida=Convert.ShortToByteArr(Short.valueOf(value));
				break;
			case "DINT":
				salida = new byte[4];
				salida=Convert.IntToByteArr(Integer.valueOf(value));
				break;
			case "USINT":
				salida = new byte[1];
				salida=Convert.ByteToByteArr(Byte.valueOf(value));
				break;
			case "UINT":
				salida = new byte[2];
				salida=Convert.ShortToByteArr(Short.valueOf(value));
				break;
			case "UDINT":
				salida = new byte[4];
				salida=Convert.IntToByteArr(Integer.valueOf(value));
				break;
			case "REAL":
				salida = new byte[4];
				//TODO no se como hacerlo
				break;
			case "LREAL":
				salida = new byte[8];
				//TODO no se como hacerlo
				break;
			case "TIME":
				salida = new byte[4];
				salida=Convert.IntToByteArr(Integer.valueOf(value));
				break;
			case "TOD":
				salida = new byte[8];
				//TODO no se como hacerlo
				break;
			case "DATE":
				salida = new byte[4];
				salida=Convert.IntToByteArr(Integer.valueOf(value));
				break;
			case "DT":
				salida = new byte[4];
				salida=Convert.IntToByteArr(Integer.valueOf(value));
				break;
			default:
				salida = new byte[1];
				break;
		}
		return salida;
	}
}
