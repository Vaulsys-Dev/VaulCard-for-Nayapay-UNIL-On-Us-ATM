package vaulsys.protocols.ifx.imp;


import vaulsys.contact.City;
import vaulsys.contact.Country;
import vaulsys.contact.State;
import vaulsys.util.Util;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

//@Entity
//@Table(name = "ifx_org_rec")
@Embeddable
public class OrgRec implements Serializable, Cloneable {

//    @Id
//    @GeneratedValue(generator="switch-gen")
//	private Long id;
//	
//    private String OrgIdNum;
    private Long OrgIdType;
    
	private String Name;
	
//	private String Country;
//	private String StateProv;
//	private String City;
	
//	@Transient
//	@ManyToOne
//	private transient City city;
	private Long cityCode;

//	@Transient
//	@ManyToOne
//	private transient State stateProv;
	private Long stateCode;
	
//	@Transient
//	@ManyToOne
//	private transient Country country;
	private Long countryCode;
	
//	private String Address;

//	@Override
//	public Long getId() {
//		return this.id;
//}
//
//	@Override
//	public void setId(Long id) {
//		this.id = id;
//	}
//	
	@Override
	protected Object clone() {
		OrgRec obj = new OrgRec();
//		obj.setOrgIdNum(this.OrgIdNum);
		obj.setOrgIdType(this.OrgIdType);
		obj.setName(Name);
//		obj.setCity(getCity());
		obj.setCityCode(cityCode);
//		obj.setCountry(getCountry());
		obj.setCountryCode(countryCode);
//		obj.setStateProv(getStateProv());
		obj.setStateCode(stateCode);
//		obj.setAddress(Address);
		return obj;
	}
	
	
	public OrgRec copy() {
		return (OrgRec) clone();
	}
	
    public void setOrgIdType(Long orgIdType) {
        OrgIdType = orgIdType;
    }
	
	public Long getOrgIdType() {
		return OrgIdType;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getName() {
		return Name;
	}
	
	public void copyFields(OrgRec source) {
		
//		if (source.getCity() != null)
//			setCity(source.getCity());
		
//		if (source.getCountry() != null)
//			setCountry(source.getCountry());
		
//		if (source.getStateProv() != null)
//			setStateProv(source.getStateProv());
		
		if (source.getCityCode() != null)
			setCityCode(source.getCityCode());
		
		if (source.getCountryCode() != null)
			setCountryCode(source.getCountryCode());
		
		if (source.getStateCode() != null)
			setStateCode(source.getStateCode());
		
		if (Util.hasText(source.getName()))
			setName(source.getName());
		
//		if (Util.hasText(source.getAddress()))
//			setAddress(source.getAddress());
		
//		if(source.getOrgIdNum() != null && !"".equals(source.getOrgIdNum()) && source.getOrgIdNum().length() > 0)
//		if (Util.hasText(source.getOrgIdNum()))
//		if (Util.hasText(source.getOrgIdNum()))
//			setOrgIdNum(source.getOrgIdNum());
		
		if (source.getOrgIdType() != null)
			setOrgIdType(source.getOrgIdType());
		
	}

//	public void setAddress(String address) {
//		Address = address;
//	}
//
//	public String getAddress() {
//		return Address;
//	}

//	public City getCity() {
//		return city;
//	}
//
//	public void setCity(City city) {
//		this.city = city;
//	}
//
	public Long getCityCode() {
		return cityCode;
	}

	public void setCityCode(Long cityCode) {
		this.cityCode = cityCode;
	}

//	public State getStateProv() {
//		return stateProv;
//	}
//
//	public void setStateProv(State stateProv) {
//		this.stateProv = stateProv;
//	}
//
	public Long getStateCode() {
		return stateCode;
	}

	public void setStateCode(Long stateCode) {
		this.stateCode = stateCode;
	}

//	public Country getCountry() {
//		return country;
//	}
//
//	public void setCountry(Country country) {
//		this.country = country;
//	}
//
	public Long getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(Long countryCode) {
		this.countryCode = countryCode;
	}

}
