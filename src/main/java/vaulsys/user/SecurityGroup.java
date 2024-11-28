package vaulsys.user;

import vaulsys.persistence.IEnum;

public class SecurityGroup implements IEnum {

	public static final SecurityGroup GROUPONE = new SecurityGroup((byte) 1);
	public static final SecurityGroup GROUPTWO = new SecurityGroup((byte) 2);
	public static final SecurityGroup GROUPTHREE = new SecurityGroup((byte) 3);
	public static final SecurityGroup GROUPFOUR = new SecurityGroup((byte) 4);
	public static final SecurityGroup GROUPFIVE = new SecurityGroup((byte) 5);
	public static final SecurityGroup GROUPSIX = new SecurityGroup((byte) 6);
	public static final SecurityGroup GROUPSEVEN = new SecurityGroup((byte) 7);
	public static final SecurityGroup GROUPEIGHT = new SecurityGroup((byte) 8);
	public static final SecurityGroup GROUPNINE = new SecurityGroup((byte) 9);
	public static final SecurityGroup GROUPTEN = new SecurityGroup((byte) 10);
    public static final SecurityGroup GROUPELEVEN = new SecurityGroup((byte) 11);

	private byte group;

	public SecurityGroup() {
	}

	public SecurityGroup(byte group) {
		this.group = group;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SecurityGroup))
			return false;
		SecurityGroup other = (SecurityGroup) obj;
		if (group != other.group)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return group;
	}

	@Override
	public String toString() {
		switch (group) {
		case 1:
			return "GROUPONE";
		case 2:
			return "GROUPTWO";
		case 3:
			return "GROUPTHREE";
		case 4:
			return "GROUPFOUR";
		case 5:
			return "GROUPFIVE";
		case 6:
			return "GROUPSIX";
		case 7:
			return "GROUPSEVEN";
		case 8:
			return "GROUPEIGHT";
		case 9:
			return "GROUPNINE";
		case 10:
			return "GROUPTEN";
        case 11:
			return "GROUPELEVEN";
		default:
			return "-";
		}
	}
}
