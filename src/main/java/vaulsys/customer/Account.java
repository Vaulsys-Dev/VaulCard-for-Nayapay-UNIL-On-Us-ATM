package vaulsys.customer;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class Account implements Serializable {

	@Column(name = "account_num")
	String accountNumber;

	@Column(name = "card_num")
	String cardNumber;

	@Column(name = "account_holder")
	String accountHolderName;

	@Embedded
	@AttributeOverrides({
	@AttributeOverride(name = "type", column = @Column(name = "type"))
			})
	AccountType type;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_currency")
	Currency currency;
	
	@Column(name = "account_currency", insertable = false, updatable = false)
	Integer currencyId;
	
	public Integer getCurrencyId() {
		return currencyId;
	}

	@Embedded
	@AttributeOverrides({
	@AttributeOverride(name = "type", column = @Column(name = "core"))
			})
	Core core;

	public Account() {
	}

	public Account(String ownerName, String accountId, Currency currency) {
		this(ownerName, accountId, currency, null);
	}

	public Account(String ownerName, String accountId, Currency currency, Core core) {
		this.accountHolderName = ownerName;
		this.accountNumber = accountId;
		this.currency = currency;
		this.core = core;
	}
	
	public Account(String ownerName, String accountId, Currency currency, Core core, AccountType type) {
		this.accountHolderName = ownerName;
		this.accountNumber = accountId;
		this.currency = currency;
		this.core = core;
		this.type = type;
	}

	public Account(String accountNumber, String cardNumber, String accountHolderName, AccountType type,
			Currency currency, Core core) {
		this.accountNumber = accountNumber;
		this.cardNumber = cardNumber;
		this.accountHolderName = accountHolderName;
		this.type = type;
		this.currency = currency;
		this.core = core;
	}

	public Account getAccount(String accountId) {
		Account account = null;
		if (this.accountNumber.endsWith(accountId)) {
			account = new Account(this.accountHolderName, this.accountNumber, this.currency, Core.NEGIN_CORE);
		}
		return account;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getAccountHolderName() {
		return accountHolderName;
	}

	public void setAccountHolderName(String accountHolderName) {
		this.accountHolderName = accountHolderName;
	}

	@Override
	public String toString() {
		return accountNumber != null ? accountNumber.toString():"";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountHolderName == null) ? 0 : accountHolderName.hashCode());
		result = prime * result + ((accountNumber == null) ? 0 : accountNumber.hashCode());
		result = prime * result + ((cardNumber == null) ? 0 : cardNumber.hashCode());
		result = prime * result + ((currencyId == null) ? 0 : currencyId.hashCode());
		result = prime * result + ((core == null) ? 0 : core.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	public Core getCore() {
		return core;
	}

	public void setCore(Core core) {
		this.core = core;
	}

	public AccountType getType() {
		return type;
	}

	public void setType(AccountType type) {
		this.type = type;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Account))
			return false;
		Account other = (Account) obj;
		if (accountHolderName == null) {
			if (other.accountHolderName != null)
				return false;
		} else if (!accountHolderName.equals(other.accountHolderName))
			return false;
		if (accountNumber == null) {
			if (other.accountNumber != null)
				return false;
		} else if (!accountNumber.equals(other.accountNumber))
			return false;
		if (cardNumber == null) {
			if (other.cardNumber != null)
				return false;
		} else if (!cardNumber.equals(other.cardNumber))
			return false;
		if (core == null) {
			if (other.core != null)
				return false;
		} else if (!core.equals(other.core))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	public boolean partailEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Account))
			return false;
		Account other = (Account) obj;
		if (accountHolderName == null) {
			if (other.accountHolderName != null)
				return false;
		} else if (!accountHolderName.equals(other.accountHolderName))
			return false;
		if (accountNumber == null) {
			if (other.accountNumber != null)
				return false;
		} else if (!accountNumber.equals(other.accountNumber))
			return false;
		if (core == null) {
			if (other.core != null)
				return false;
		} else if (!core.equals(other.core))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
