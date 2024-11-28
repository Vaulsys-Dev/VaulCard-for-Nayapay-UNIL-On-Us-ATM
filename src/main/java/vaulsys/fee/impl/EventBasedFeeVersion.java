package vaulsys.fee.impl;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "EventBasedFee_ver")
public class EventBasedFeeVersion extends BaseFeeVersion{
//	@ManyToOne
//	@JoinColumn(name = "parent")
//	@ForeignKey(name = "baseFee_vers_parent_fk")
//	private EventBasedFee parent;
//
//	public EventBasedFee getParent() {
//		return parent;
//	}
//
//	public void setParent(EventBasedFee parent) {
//		this.parent = parent;
//	}

}
