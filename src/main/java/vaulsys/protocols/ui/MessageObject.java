package vaulsys.protocols.ui;

import vaulsys.calendar.DateTime;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.enums.IfxType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class  MessageObject implements ProtocolMessage {
	private static final long serialVersionUID = -4850681616717268812L;

	private IfxType ifxType;
	
	private String username;
	
	private HashMap<String, Serializable> parameters;

	private String responseCode;

	private DateTime startDateTime;
	
	public MessageObject() {
	}
	
	public MessageObject(IfxType ifxType, HashMap<String, Serializable> parameters) {
		this.ifxType = ifxType;
		this.parameters = parameters;
	}
	
	public IfxType getIfxType() {
		return ifxType;
	}
	public void setIfxType(IfxType ifxType) {
		this.ifxType = ifxType;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	public Object getParameter(String parameterName) {
		return parameters.get(parameterName);
	}

	public Map<String, Serializable> getParameters() {
		return parameters;
	}
	public void setParameters(HashMap<String, Serializable> parameters) {
		this.parameters = parameters;
	}

	@Override
	public Boolean isRequest() throws Exception {
		return responseCode==null;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public DateTime getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(DateTime startDateTime) {
		this.startDateTime = startDateTime;
	}

	@Override
	public String toString() {
		String result = "ifxType: "+ ifxType+"\r\n";
		if (parameters != null){
			result += "Parameters:";
			for (String key : parameters.keySet())
				result += "\r\n\t"+ key +": "+ parameters.get(key);
		}else 
			result += "Parameteres: NULL";
			
		result += "\r\n Response code: "+ responseCode;
		return result;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		MessageObject object = new MessageObject();
		object.setIfxType(getIfxType());
		if (parameters != null){
			object.setParameters(new HashMap<String, Serializable>());
			for (String key : parameters.keySet())
				object.getParameters().put(key, parameters.get(key));
		}
		object.setResponseCode(responseCode);
		return object;
	}
}
