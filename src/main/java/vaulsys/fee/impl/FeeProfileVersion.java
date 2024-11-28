package vaulsys.fee.impl;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;

import java.util.List;

import javax.persistence.*;

import vaulsys.user.User;
import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "fee_prof_ver")
public class FeeProfileVersion implements IEntity<Long> {
	
	@Id
	@GeneratedValue(generator = "switch-gen")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "parent")
	@ForeignKey(name = "feeProfile_vers_parent_fk")
	private FeeProfile parent;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "owner")
	@ForeignKey(name = "feeprof_basefee_vers_fk")
	List<BaseFee> baseFees;
	
    //@Column(unique = true)
	String name;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user")
    @ForeignKey(name = "fine_user_fk")
    protected User creatorUser;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
    })
    protected DateTime createdDateTime;

	boolean enabled = true;

	public FeeProfile getParent() {
		return parent;
	}

	public void setParent(FeeProfile parent) {
		this.parent = parent;
	}
	
	public List<BaseFee> getBaseFees() {
		return baseFees;
	}

	public BaseFee getBaseFee(int index) {
		return baseFees.get(index);
	}
//	public void addBaseFee(BaseFee baseFee) {
//		if (baseFees == null)
//			baseFees = new ArrayList<BaseFee>();
//		baseFee.setOwner(this);
//		baseFees.add(baseFee);
//	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
}
