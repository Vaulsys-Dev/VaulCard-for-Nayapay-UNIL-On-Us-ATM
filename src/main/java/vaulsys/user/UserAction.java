package vaulsys.user;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class UserAction implements IEnum {
	public static final UserAction LOGIN = new UserAction((byte) 1);
	public static final UserAction LOGOUT = new UserAction((byte) 2);
	public static final UserAction VIEW_PAGE = new UserAction((byte) 3);
	public static final UserAction VIEW = new UserAction((byte) 4);
	public static final UserAction SEARCH = new UserAction((byte) 5);
	public static final UserAction EDIT = new UserAction((byte) 6);
	public static final UserAction UPDATE = new UserAction((byte) 7);
	public static final UserAction ADD = new UserAction((byte) 8);
	public static final UserAction CREATE = new UserAction((byte) 9);
	public static final UserAction DELETE = new UserAction((byte) 10);
	public static final UserAction CHG_PASS = new UserAction((byte) 11);

	private byte action;

	public UserAction() {
	}

	public UserAction(byte action) {
		this.action = action;
	}

	public byte getAction() {
		return action;
	}

	@Override
	public int hashCode() {
		return action;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof UserAction))
			return false;
		UserAction other = (UserAction) obj;
		if (action != other.action)
			return false;
		return true;
	}

	@Override
	public String toString() {
		switch (action) {
			case 1:
				return "LOGIN";
			case 2:
				return "LOGOUT";
			case 3:
				return "VIEW_PAGE";
			case 4:
				return "VIEW";
			case 5:
				return "SEARCH";
			case 6:
				return "EDIT";
			case 7:
				return "UPDATE";
			case 8:
				return "ADD";
			case 9:
				return "CREATE";
			case 10:
				return "DELETE";
			default:
				return "-";
		}
	}
}
