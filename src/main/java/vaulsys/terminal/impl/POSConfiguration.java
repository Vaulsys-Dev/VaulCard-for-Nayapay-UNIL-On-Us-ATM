package vaulsys.terminal.impl;

import vaulsys.persistence.IEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "term_pos_config")
public class POSConfiguration implements IEntity<Long> {

	@Id
	private Long id;

	private String name;

	@Column(name = "rcpt_ver", length = 5)
	private String receiptVersion;

	private String merchantHeader;

	private String merchantFooter;

	private String cardholderHeader;

	private String cardholderFooter;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getReceiptVersion() {
		return receiptVersion;
	}
	public void setReceiptVersion(String receiptVersion) {
		this.receiptVersion = receiptVersion;
	}

	public String getMerchantHeader() {
		return merchantHeader;
	}
	public void setMerchantHeader(String merchantHeader) {
		this.merchantHeader = merchantHeader;
	}

	public String getMerchantFooter() {
		return merchantFooter;
	}
	public void setMerchantFooter(String merchantFooter) {
		this.merchantFooter = merchantFooter;
	}

	public String getCardholderHeader() {
		return cardholderHeader;
	}
	public void setCardholderHeader(String cardholderHeader) {
		this.cardholderHeader = cardholderHeader;
	}

	public String getCardholderFooter() {
		return cardholderFooter;
	}
	public void setCardholderFooter(String cardholderFooter) {
		this.cardholderFooter = cardholderFooter;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((merchantHeader == null) ? 0 : merchantHeader.hashCode());
		result = prime * result + ((merchantFooter == null) ? 0 : merchantFooter.hashCode());
		result = prime * result + ((cardholderHeader == null) ? 0 : cardholderHeader.hashCode());
		result = prime * result + ((cardholderFooter == null) ? 0 : cardholderFooter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof POSConfiguration))
			return false;
		POSConfiguration other = (POSConfiguration) obj;
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
	
	@Override
	public String toString() {
		return name == null ? "-" : name;
	}
}
