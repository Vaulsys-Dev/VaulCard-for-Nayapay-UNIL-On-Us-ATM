package vaulsys.customer;

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

import org.hibernate.annotations.ForeignKey;

@Entity
public class Currency implements IEntity<Integer> {

	@Id
	Integer code;

	String name;
	String description;
	String pattern;
	// Added by : Asim Shahzad, Date : 24th Nov 2016, Desc : Merged from TPSP for VISA SMS
	@Column(name = "exchange_rate")
	Long exchangeRate;
	// End
	@Column(name = "is_base_currency")
	Boolean isBase;
	@Column(name = "decimal_position")
	Integer decimalPosition;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_user")
	@ForeignKey(name = "currency_user_fk")
	protected User creatorUser;

	@AttributeOverrides({
	@AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
			})
	protected DateTime createdDateTime;

	public Currency() {
		this.name = "";
		this.code = 0;
		this.description = "";
		this.pattern = "0";
		this.exchangeRate = 0L; // Added by : Asim Shahzad, Date : 24th Nov 2016, Desc : Merged from TPSP for VISA SMS
		this.isBase = Boolean.FALSE;
		this.decimalPosition = 0;
	}

	public Currency(Integer code) {
		this.name = Integer.toString(code);
		this.code = code;
		this.description = "";
		this.exchangeRate = 0L; // Added by : Asim Shahzad, Date : 24th Nov 2016, Desc : Merged from TPSP for VISA SMS
		this.isBase = Boolean.FALSE;
		this.decimalPosition = 0;
	}

	public Currency(String name, Integer code) {
		this.name = name;
		this.code = code;
		this.description = "";
		this.exchangeRate = 0L; // Added by : Asim Shahzad, Date : 24th Nov 2016, Desc : Merged from TPSP for VISA SMS
		this.isBase = Boolean.FALSE;
		this.decimalPosition = 0;
	}

	public Currency(String name, Integer code, String description, Long exchangeRate, Boolean isBase, Integer decimalPosition) {
		this.name = name;
		this.code = code;
		this.description = description;
		this.exchangeRate = exchangeRate;
		this.isBase = isBase;
		this.decimalPosition = 0;
	}


	public Currency(String name, Integer code, String description, String pattern, Long exchangeRate, Boolean isBase, Integer decimalPosition) {
		this.name = name;
		this.code = code;
		this.description = description;
		this.pattern = pattern;
		this.exchangeRate = exchangeRate; // Added by : Asim Shahzad, Date : 24th Nov 2016, Desc : Merged from TPSP for VISA SMS
		this.isBase = isBase;
		this.decimalPosition = 0;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@Override
	public String toString() {
		return name;
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
	public Integer getId() {
		return code;
	}

	@Override
	public void setId(Integer code) {
		this.code = code;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Currency currency = (Currency) o;
		if (code != null ? !code.equals(currency.code) : currency.code != null) return false;
		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = (code != null ? code.hashCode() : 0);
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (pattern != null ? pattern.hashCode() : 0);
		return result;
	}

	// Added by : Asim Shahzad, Date : 24th Nov 2016, Desc : Merged from TPSP for VISA SMS
	public Long getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(Long exchangeRate) {
		this.exchangeRate = exchangeRate;
	}
	// End
	public Boolean getIsBase() {
		return isBase;
	}

	public void setIsBase(Boolean isBase) {
		this.isBase = isBase;
	}

	public Integer getDecimalPosition() {
		return decimalPosition;
	}

	public void setDecimalPosition(Integer decimalPosition) {
		this.decimalPosition = decimalPosition;
	}
}
