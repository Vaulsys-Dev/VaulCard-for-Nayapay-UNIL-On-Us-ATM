package vaulsys.contact;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.user.User;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;


@Entity
@Table(name = "fine_contact_state")
public class State implements IEntity<Long> {


	@Id
//    @GeneratedValue(generator="switch-gen")
//    private Long id;
	Long code;

	String name;

	@Column(nullable = false)
	String abbreviation;

	@Column(length = 2)
	String comsCode;

	@ManyToOne(fetch = FetchType.EAGER) //Raza changing for NayaPay
	@JoinColumn(name = "country")
	@ForeignKey(name = "state_country_fk")
	Country country;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_user")
	@ForeignKey(name = "state_user_fk")
	protected User creatorUser;

	@AttributeOverrides({
	@AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
			})
	protected DateTime createdDateTime;

	public State() {
	}

	public State(String name) {
		this.name = name;
	}

	public State(String name, Long code) {
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getCode() {
		return code;
	}

	public void setCode(Long code) {
		this.code = code;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public Long getId() {
		return getCode();
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public void setId(Long id) {
		this.setCode(id);
	}

	@Override
	public String toString() {
		return String.format("%s - %s", this.name, this.code);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((abbreviation == null) ? 0 : abbreviation.hashCode());
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof State))
			return false;
		State that = (State) o;
		return code.equals(that.code) && name.equals(that.name) && abbreviation.equals(that.abbreviation);
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

	public String getComsCode() {
		return comsCode;
	}

	public void setComsCode(String comsCode) {
		this.comsCode = comsCode;
	}
}
