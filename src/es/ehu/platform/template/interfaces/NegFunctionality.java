package es.ehu.platform.template.interfaces;

import java.io.Serializable;
import es.ehu.platform.MWAgent;
import jade.core.AID;

public interface NegFunctionality extends Serializable{

  //
	public long calculateNegotiationValue(String negAction, String negCriterion, Object... negExternalData);
	
	// negId - id de la negociaci�n
	// negReceivedValue - valor recibido para comparar
	// negScalarValue - valor propio de negociaci�n
	// winnerAction - acci�n a ejecutar
	// tieBreaker - criterio de desempate
	// checkReplies - true si han competido todos
	
	public int checkNegotiation(String negId, String winnerAction, double negReceivedValue, long negScalarValue, boolean tieBreaker, boolean checkReplies, Object... negExternalData);

}
