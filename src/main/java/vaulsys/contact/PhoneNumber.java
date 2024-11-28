package vaulsys.contact;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class PhoneNumber implements Serializable {

	private String areaCode;
	private String number;

	public PhoneNumber() {
	}

	public PhoneNumber(String areaCode, String number) {
		this.areaCode = areaCode;
		this.number = number;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	@Override
	public String toString() {
		return (areaCode != null ? areaCode+"-" : "") + number;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PhoneNumber that = (PhoneNumber) o;

		if (areaCode != null ? !areaCode.equals(that.areaCode) : that.areaCode != null) return false;
		if (number != null ? !number.equals(that.number) : that.number != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = (areaCode != null ? areaCode.hashCode() : 0);
		result = 31 * result + (number != null ? number.hashCode() : 0);
		return result;
	}
}
