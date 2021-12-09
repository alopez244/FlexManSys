package es.ehu.platform.template.interfaces;

import java.io.Serializable;
import es.ehu.platform.MWAgent;
import jade.core.AID;

public interface NegFunctionality extends Serializable{

	public long calculateNegotiationValue(String negAction, String negCriterion, Object... negExternalData);

	// negId - id de la negociación
	// negReceivedValue - valor recibido para comparar
	// negScalarValue - valor propio de negociación
	// winnerAction - acción a ejecutar
	// tieBreaker - criterio de desempate
	// checkReplies - true si han competido todos

	public int checkNegotiation(String conversationId, String sAction, double negReceivedValue, long negScalarValue, boolean tieBreak, boolean checkReplies, boolean isPartialWinner, Object... negExternalData);

}
