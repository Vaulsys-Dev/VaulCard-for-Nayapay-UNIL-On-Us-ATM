package vaulsys.protocols.ifx.enums;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class UserLanguage implements IEnum {

	private static final int FARSI_VALUE= 0;
	private static final int ENGLISH_VALUE = 1;

	public static final UserLanguage FARSI_LANG = new UserLanguage(FARSI_VALUE);
	public static final UserLanguage ENGLISH_LANG = new UserLanguage(ENGLISH_VALUE);

	private int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
	
	public UserLanguage() {
		super();
	}
	
	public UserLanguage(int type){
		super();
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		UserLanguage that = (UserLanguage) obj;
		return this.type == that.type;
	}

	@Override
	public int hashCode() {
		return type;
	}
	
	@Override
	protected Object clone() {
		return new UserLanguage(this.type); 
	}
	
	public UserLanguage copy() {
		return (UserLanguage) clone();
	}

}
