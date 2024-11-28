package vaulsys.entity.impl;

import vaulsys.calendar.DateTime;
import vaulsys.contact.Contact;
import vaulsys.entity.Contract;
import vaulsys.persistence.IEntity;
import vaulsys.user.User;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "fine_merchant_visitor")
public class Visitor implements IEntity<Long> {

    @Id
	@GeneratedValue(generator = "visitor-seq-gen")
	@SequenceGenerator(name = "visitor-seq-gen", allocationSize = 1, sequenceName = "visitor_code_seq", initialValue=1000)
    protected Long id;

	@Embedded
	@AttributeOverrides( {
			@AttributeOverride(name = "startDate.date", column = @Column(name = "contract_start_date")),
			@AttributeOverride(name = "endDate.date", column = @Column(name = "contract_end_date")),
			@AttributeOverride(name = "contractNumber", column = @Column(name = "contract_num"))})
	private Contract contract;

	private String name;
	
	
	@Embedded
	private Contact contact;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_user")
	@ForeignKey(name = "vist_user_fk")
	protected User creatorUser;

    @AttributeOverrides({
        @AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
        @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
            })
	protected DateTime createdDateTime;
    
    public boolean enabled = true;
	


	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}

	public Contract getContract() {
		return contract;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public Contact getContact() {
		return contact;
	}

	public User getCreatorUser() {
		return creatorUser;
	}

	public void setCreatorUser(User creatorUser) {
		this.creatorUser = creatorUser;
	}

	public DateTime getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(DateTime createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public boolean isEnabled() {
        return enabled ;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Visitor))
			return false;
		Visitor other = (Visitor) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
