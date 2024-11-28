package vaulsys.entity.impl;

import vaulsys.persistence.IEntity;

import javax.persistence.*;

@Entity
@Table(name = "contact_entity")
public class Contact implements IEntity<Long>, Cloneable{

	
	@Id
//  @GeneratedValue(generator="switch-gen")
  @GeneratedValue(generator="contact-task-seq-gen")
  @org.hibernate.annotations.GenericGenerator(name = "contact-task-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
  		parameters = {
  			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
  			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
  			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "contact-task-seq")
  				})
  Long id;
	
	
	
	
	
	@Column(name="contact_name")
	 private String contactName;
	
	
	@Column(name="phone_number")
	 private String phoneNumber;
	
	
	@Column(name="enabled")
	 private Boolean enabled;
	
	
	@Override
	public Long getId() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
		// TODO Auto-generated method stub
		
	}
	
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	
	public String getContactName() {
		return contactName;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
	public Boolean getEnabled() {
		return enabled;
	}
	
	
}
