package es.ehu.domain.manufacturing.agents.cognitive.ros.jade;

/**
 * Defines social message define to communicate the different layer
 * in the JADE-ROS integration.
 * 
 * @author Brais Fortes (@fortes23) - Euskal Herriko Unibersitatea
 */
public class RosNodeMsg {
	private String conversationID;
	private String ontology;
	private String[] content;

	// Constructor
	public RosNodeMsg(String cnvID, String ont, String... content) {
		this.conversationID = cnvID;
		this.ontology = ont;
		this.content = content;
	}

	// Getter methods
	public String getConversationID() {
		return this.conversationID;
	}

	public String getOntology() {
		return this.ontology;
	}

	public String[] getContent() {
		return this.content;
	}

	// Setter methods
	public void setConversationID(String cnvID) {
		this.conversationID = cnvID;
	}

	public void setOntology(String ont) {
		this.ontology = ont;
	}

	public void setContent(String... content) {
		this.content = content;
	}
}