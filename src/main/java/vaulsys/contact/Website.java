package vaulsys.contact;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class Website implements Serializable {

	private String websiteAddress;
	private String email;

	public Website() {
	}

	public String getWebsiteAddress() {
		return websiteAddress;
	}

	public void setWebsiteAddress(String websiteAddress) {
		this.websiteAddress = websiteAddress;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Website website = (Website) o;

		if (email != null ? !email.equals(website.email) : website.email != null) return false;
		if (websiteAddress != null ? !websiteAddress.equals(website.websiteAddress) : website.websiteAddress != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = (websiteAddress != null ? websiteAddress.hashCode() : 0);
		result = 31 * result + (email != null ? email.hashCode() : 0);
		return result;
	}
}
