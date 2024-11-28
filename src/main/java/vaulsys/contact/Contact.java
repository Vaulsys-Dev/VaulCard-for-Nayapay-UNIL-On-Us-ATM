package vaulsys.contact;

import java.io.Serializable;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.JoinColumn;

@Embeddable
public class Contact implements Serializable{

	private String name;
   
	@Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "address", column = @Column(name = "address")),
        @AttributeOverride(name = "postalCode", column = @Column(name = "postal_code"))
    })
    @AssociationOverrides({
        @AssociationOverride(name = "country", joinColumns={@JoinColumn(name = "country")}),
        @AssociationOverride(name = "state", joinColumns={@JoinColumn(name = "state")}),
        @AssociationOverride(name = "city", joinColumns={@JoinColumn(name = "city")})    	
    })
    private Address address;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "areaCode", column = @Column(name = "phone_area_code")),
        @AttributeOverride(name = "number", column = @Column(name = "phone_number"))
            })
    private PhoneNumber phoneNumber;
    
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "areaCode", column = @Column(name = "mobile_area_code")),
    	@AttributeOverride(name = "number", column = @Column(name = "mobile_number"))
    })
    private PhoneNumber mobileNumber;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "websiteAddress", column = @Column(name = "website")),
        @AttributeOverride(name = "email", column = @Column(name = "email"))
            })
    private Website website;

    public Contact() {
    }
    
    public Contact(String name, Address address, PhoneNumber phoneNumber, PhoneNumber mobileNumber, Website website) {
		this.name = name;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.mobileNumber = mobileNumber;
		this.website = website;
	}



    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

	public PhoneNumber getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(PhoneNumber mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((mobileNumber == null) ? 0 : mobileNumber.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
		result = prime * result + ((website == null) ? 0 : website.hashCode());
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
		Contact other = (Contact) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (mobileNumber == null) {
			if (other.mobileNumber != null)
				return false;
		} else if (!mobileNumber.equals(other.mobileNumber))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (phoneNumber == null) {
			if (other.phoneNumber != null)
				return false;
		} else if (!phoneNumber.equals(other.phoneNumber))
			return false;
		if (website == null) {
			if (other.website != null)
				return false;
		} else if (!website.equals(other.website))
			return false;
		return true;
	}

}
