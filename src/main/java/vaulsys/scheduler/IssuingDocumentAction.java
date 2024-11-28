package vaulsys.scheduler;

import vaulsys.persistence.IEnum;

public class IssuingDocumentAction implements IEnum {

	private static final int REISSUE_VALUE = 1;
	private static final int TIME_OUT_VALUE = 2;
	private static final int RETURN_VALUE = 3;

	public static final IssuingDocumentAction REISSUE = new IssuingDocumentAction(REISSUE_VALUE);
	public static final IssuingDocumentAction RETURN = new IssuingDocumentAction(RETURN_VALUE);
	public static final IssuingDocumentAction TIME_OUT = new IssuingDocumentAction(TIME_OUT_VALUE);

	int action;

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	IssuingDocumentAction() {
		super();
	}

	IssuingDocumentAction(int action) {
		this.action = action;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + action;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IssuingDocumentAction other = (IssuingDocumentAction) obj;
		if (action != other.action)
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (REISSUE.equals(this))
			return "REISSUE";
		else if (RETURN.equals(RETURN_VALUE))
			return "RETURN";
		else
			return this.action + "";
	}
}
