package vaulsys.contact;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Embeddable
public class Address implements Serializable {

	@ManyToOne(fetch = FetchType.LAZY)
	private City city;

	@Column(name = "city", insertable = false, updatable = false)
	private Long cityId;

	@ManyToOne(fetch = FetchType.LAZY)
	private State state;

	@Column(name = "state", insertable = false, updatable = false)
	private Long stateId;

	@ManyToOne(fetch = FetchType.LAZY)
	private Country country;

	@Column(name = "country", insertable = false, updatable = false)
	private Long countryId;

	private String postalCode;
	
	private String address;

	public Address() {
	}

	public Address(City city, State state, Country country, String postalCode, String address) {
		this.city = city;
		this.state = state;
		this.country = country;
		this.postalCode = postalCode;
		this.address = address;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public Long getCityId() {
		return cityId;
	}

	public Long getStateId() {
		return stateId;
	}

	public Long getCountryId() {
		return countryId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (country != null)
			builder.append(country.getName());
		if (state != null)
			builder.append("-").append(state.getName());
		if (city != null)
			builder.append("-").append(city.getName());
		builder.append("-").append(address).append("-").append(postalCode);
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((postalCode == null) ? 0 : postalCode.hashCode());
		result = prime * result + ((cityId == null) ? 0 : cityId.hashCode());
		result = prime * result	+ ((countryId == null) ? 0 : countryId.hashCode());
		result = prime * result + ((stateId == null) ? 0 : stateId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Address))
			return false;
		Address other = (Address) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (postalCode == null) {
			if (other.postalCode != null)
				return false;
		} else if (!postalCode.equals(other.postalCode))
			return false;
		if (cityId == null) {
			if (other.cityId != null)
				return false;
		} else if (!cityId.equals(other.cityId))
			return false;
		if (countryId == null) {
			if (other.countryId != null)
				return false;
		} else if (!countryId.equals(other.countryId))
			return false;
		if (stateId == null) {
			if (other.stateId != null)
				return false;
		} else if (!stateId.equals(other.stateId))
			return false;
		return true;
	}
}
