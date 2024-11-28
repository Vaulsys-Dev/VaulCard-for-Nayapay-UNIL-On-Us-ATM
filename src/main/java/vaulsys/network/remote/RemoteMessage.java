package vaulsys.network.remote;

import java.io.Serializable;

public class RemoteMessage implements Serializable {
	private static final long serialVersionUID = 7955883807536208177L;
	
	private MessageType type;
	private Serializable requestObject;
	private Serializable responseObject;
	private String responseMessage;
	private String requesterUsername;
	
	public MessageType getType() {
		return type;
	}
	public void setType(MessageType type) {
		this.type = type;
	}

	public Serializable getRequestObject() {
		return requestObject;
	}
	public void setRequestObject(Serializable requestObject) {
		this.requestObject = requestObject;
	}
	
	public Serializable getResponseObject() {
		return responseObject;
	}
	public void setResponseObject(Serializable responseObject) {
		this.responseObject = responseObject;
	}
	
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	
	public String getRequesterUsername() {
		return requesterUsername;
	}
	public void setRequesterUsername(String requesterUsername) {
		this.requesterUsername = requesterUsername;
	}
	
	@Override
	public String toString(){
		return String.format("%s(%s): Rq[%s], Rs[%s]-[%s]", type, requesterUsername==null ? "-":requesterUsername, 
				requestObject==null ? "-":requestObject, responseObject==null ? "-":responseObject, 
				responseMessage==null ? "-":responseMessage);
	}
}
