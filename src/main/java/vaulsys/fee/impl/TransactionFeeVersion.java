package vaulsys.fee.impl;

import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;

@Entity
@DiscriminatorValue(value = "TransactionFee_ver")
public class TransactionFeeVersion extends BaseFeeVersion{

	@ManyToOne
	@JoinColumn(name = "parent")
	@ForeignKey(name = "baseFee_vers_parent_fk")
	private TransactionFee parent;

	public TransactionFee getParent() {
		return parent;
}

	public void setParent(TransactionFee parent) {
		this.parent = parent;
	}

}
