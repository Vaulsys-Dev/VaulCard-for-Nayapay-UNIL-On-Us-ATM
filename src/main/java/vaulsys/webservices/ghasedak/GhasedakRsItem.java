package vaulsys.webservices.ghasedak;

import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.imp.GhasedakData;
import vaulsys.webservices.ghasedak.GhasedakItemType;
import vaulsys.webservices.ghasedak.GhasedakUnitType;

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
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "ifx_ghasedak_response_items")
public class GhasedakRsItem  implements IEntity<Long> {
	
	@Id
	   @GeneratedValue(generator="ghasedakrsitem-seq-gen")
	   @org.hibernate.annotations.GenericGenerator(name = "ghasedakrsitem-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
	   		parameters = {
	   			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
	   			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
	   			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "ghasedakrsitem_seq")
	   				})
		private Long id;
	
	
	@Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "itemType", column = @Column(name = "item_type"))
    })
	private GhasedakItemType itemType;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ghasedak_data")
	@ForeignKey(name = "ghasedak_data_fk")
	private GhasedakData ghasedakData;
	
	@Column(name = "amount")
	private Long amount;
	
	@Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "currency"))
    })
	private GhasedakUnitType currencyCode;
	
	@Column(name = "credit_date")
	private String creditDate;
	
	@Column(name = "credit_time")
	private String creditTime;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public GhasedakData getGhasedakData() {
		return ghasedakData;
	}

	public void setGhasedakData(GhasedakData ghasedakData) {
		this.ghasedakData = ghasedakData;
	}

	public GhasedakItemType getItemType() {
		return itemType;
	}

	public void setItemType(GhasedakItemType itemType) {
		this.itemType = itemType;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	public GhasedakUnitType getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(GhasedakUnitType currencyCode) {
		this.currencyCode = currencyCode;
	}
	public String getCreditDate() {
		return creditDate;
	}

	public void setCreditDate(String creditDate) {
		this.creditDate = creditDate;
	}

	public String getCreditTime() {
		return creditTime;
	}

	public void setCreditTime(String creditTime) {
		this.creditTime = creditTime;
	}
}
