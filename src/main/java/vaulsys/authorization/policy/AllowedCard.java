package vaulsys.authorization.policy;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.user.User;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "auth_plc_alwd_crd")
public class AllowedCard implements IEntity<Integer>, Cloneable {

	@Id
	@GeneratedValue(generator = "switch-gen")
	private Integer id;

	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bank")
	@ForeignKey(name="auth_plc_alwd_crd_bnk_fk")
	private Bank bank;
	
	private Long cardGroupId;
	
	private Long minCardNo;
	
	private Long maxCardNo;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_user")
	@ForeignKey(name = "disprof_user_fk")
	protected User creatorUser;
	
	@AttributeOverrides({
		@AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
		@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time")) 
	})
	protected DateTime createdDateTime;
	
	public AllowedCard() {
	}
	
	public AllowedCard(Bank bank, Long cardGroupId, Long min, Long max) {
		this.bank = bank;
		this.cardGroupId = cardGroupId;
		this.minCardNo = min;
		this.maxCardNo = max;
	}
	
	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getMinCardNo() {
		return minCardNo;
	}

	public void setMinCardNo(Long minCardNo) {
		this.minCardNo = minCardNo;
	}

	public Long getMaxCardNo() {
		return maxCardNo;
	}

	public void setMaxCardNo(Long maxCardNo) {
		this.maxCardNo = maxCardNo;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		AllowedCard range = new AllowedCard(bank, cardGroupId, minCardNo,maxCardNo);
		return range;
	}

	public Long getCardGroupId() {
		return cardGroupId;
	}

	public void setCardGroupId(Long cardGroupId) {
		this.cardGroupId = cardGroupId;
	}

	public Bank getBank() {
		return bank;
	}

	public void setBank(Bank bank) {
		this.bank = bank;
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
	public String toString() {
		return name != null ? name : "id=" + id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AllowedCard))
			return false;
		AllowedCard other = (AllowedCard) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
