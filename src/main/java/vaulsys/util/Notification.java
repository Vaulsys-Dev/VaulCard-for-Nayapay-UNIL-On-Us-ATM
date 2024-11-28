package vaulsys.util;

import java.util.ArrayList;
import java.util.List;

public class Notification {

	private List<String> errors;
	public Notification() {
		this.errors = new ArrayList<String>();
	}
	public void addError(String errorMessage) {
		this.errors.add(errorMessage);
	}
	public boolean hasError() {
		return ! errors.isEmpty();
	}
	public String getErrorMessages() {
		String listString = "";
		for (String s : errors)
		{
		    listString += s + "\n";
		}
		return listString;
	}
}
