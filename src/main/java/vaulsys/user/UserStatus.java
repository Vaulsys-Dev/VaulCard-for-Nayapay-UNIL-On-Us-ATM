package vaulsys.user;

import javax.persistence.Embeddable;

import vaulsys.persistence.IEnum;

@Embeddable
public class UserStatus implements IEnum {
	public static final UserStatus ENABLED = new UserStatus((byte) 1);
	public static final UserStatus DISABLED = new UserStatus((byte) 2);
	public static final UserStatus SECURITY_BLOCKED = new UserStatus((byte) 3);
	public static final UserStatus ADMIN_BLOCKED = new UserStatus((byte) 4);
	
	private byte status;
	
	public UserStatus() {
		super();
	}

	public UserStatus(byte state) {
		this.status = state;
	}

	public byte getStatus() {
		return status;
	}
	
	public void setStatus(byte status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		return status;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof UserStatus))
			return false;
		UserStatus other = (UserStatus) obj;
		if (status != other.status)
			return false;
		return true;
	}
}
