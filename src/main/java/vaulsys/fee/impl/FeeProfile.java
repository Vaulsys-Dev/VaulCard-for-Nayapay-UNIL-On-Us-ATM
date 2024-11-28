package vaulsys.fee.impl;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.user.User;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fee_prof")
public class FeeProfile implements IEntity<Long> {
	@Id
	@GeneratedValue(generator = "switch-gen")
	private Long id;
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "owner")
//    @JoinTable(name = "fee_profile_transaction_fees",
//    		joinColumns = {@JoinColumn(name = "fee_prof")}, 
//    		inverseJoinColumns = {@JoinColumn(name = "trx_fee")}
//    )
	@ForeignKey(name = "feeprof_basefee_fk")
	List<BaseFee> baseFees;

//    @OneToMany(fetch = FetchType.LAZY, mappedBy="super.owner")
//    @JoinTable(name = "fee_profile_eventbased_fees",
//    		joinColumns = {@JoinColumn(name = "fee_prof")}, 
//    		inverseJoinColumns = {@JoinColumn(name = "event_fee")}
//    )
//    @ForeignKey(name = "feeprof_event_feeprof_fk", inverseName = "feeprof_event_fee_fk")
	//    List<EventBasedFee> eventBasedFees;
	@Column(unique = true)
	String name;
	String description;
	boolean enabled = true;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_user")
	@ForeignKey(name = "feeprof_user_fk")
	protected User creatorUser;
	@AttributeOverrides({
	@AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
			})
	protected DateTime createdDateTime;

	public FeeProfile(String name) {
		this.baseFees = new ArrayList<BaseFee>();
//        this.eventBasedFees = new ArrayList<EventBasedFee>();
		this.name = name;
		description = "";
		this.enabled = true;
	}

	public FeeProfile() {
		this.baseFees = new ArrayList<BaseFee>();
//        this.eventBasedFees = new ArrayList<EventBasedFee>();
		this.enabled = true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

//    public List<TransactionFee> getTransactionFees() {
//        return transactionFees;
//    }
//
//    public void addTransactionFee(TransactionFee transactionFee) {
//        if (transactionFees == null)
//            transactionFees = new ArrayList<TransactionFee>();
//        transactionFee.setOwner(this);
//        transactionFees.add(transactionFee);
//    }
//
//    public TransactionFee getTransactionFee(int index) {
//        return transactionFees.get(index);
//    }
//
//    public List<EventBasedFee> getEventBasedFees() {
//        return eventBasedFees;
//    }
//
//    public boolean addEventBasedFee(EventBasedFee eventBasedFee) {
//        return eventBasedFees.add(eventBasedFee);
//    }
//
//    public EventBasedFee getEventBasedFee(int index) {
//        return eventBasedFees.get(index);
//    }

	//

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return name;
	}

	public List<BaseFee> getBaseFees() {
		return baseFees;
	}

	public void addBaseFee(BaseFee baseFee) {
		if (baseFees == null)
			baseFees = new ArrayList<BaseFee>();
		baseFee.setOwner(this);
		baseFees.add(baseFee);
	}

	public BaseFee getBaseFee(int index) {
		return baseFees.get(index);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		//result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof FeeProfile)) return false;

		FeeProfile that = (FeeProfile) o;

//		if (enabled != that.enabled) return false;
//		if (createdDateTime != null ? !createdDateTime.equals(that.createdDateTime) : that.createdDateTime != null)
//			return false;
//		if (creatorUser != null ? !creatorUser.equals(that.creatorUser) : that.creatorUser != null) return false;
//		if (description != null ? !description.equals(that.description) : that.description != null) return false;
		if (id != null ? !id.equals(that.id) : that.id != null) return false;
//		if (name != null ? !name.equals(that.name) : that.name != null) return false;

		return true;
	}
}
