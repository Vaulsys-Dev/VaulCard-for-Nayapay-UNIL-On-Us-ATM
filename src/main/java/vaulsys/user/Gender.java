package vaulsys.user;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class Gender implements IEnum {

	public static final Gender FEMALE = new Gender((byte) 1);
	public static final Gender MALE = new Gender((byte) 2);

	private byte gender;

	public Gender() {
	}

	public Gender(byte state) {
		this.gender = state;
	}

	public byte getGender() {
		return gender;
	}

	@Override
	public int hashCode() {
		return gender;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Gender))
			return false;
		Gender other = (Gender) obj;
		if (gender != other.gender)
			return false;
		return true;
	}
}
