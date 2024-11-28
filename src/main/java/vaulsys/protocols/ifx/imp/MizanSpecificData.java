package vaulsys.protocols.ifx.imp;

import vaulsys.persistence.IEntity;

import javax.persistence.*;

@Entity
@Table(name = "ifx_mizan_specific")
public class MizanSpecificData implements IEntity<Long>, Cloneable {
	@Id
	@GeneratedValue(generator = "mizanspecificdata-seq-gen")
	@org.hibernate.annotations.GenericGenerator(name = "mizanspecificdata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
					@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "mizanspecificdata_seq")
			})
	private Long id;

	@Column(name = "items_cnt")
	private Integer itemsCount;

	@Column(name = "request_cnt")
	private Integer requestCount;

	@Column(length = 1000)
	private String items;

	@Column(name = "mizan_prcnt")
	private Integer mizanPercent;

	@Column(name = "bank_prcnt")
	private Integer bankPercent;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getItemsCount() {
		return itemsCount;
	}

	public void setItemsCount(Integer itemsCount) {
		this.itemsCount = itemsCount;
	}

	public Integer getRequestCount() {
		return requestCount;
	}

	public void setRequestCount(Integer requestCount) {
		this.requestCount = requestCount;
	}

	public String getItems() {
		return items;
	}

	public void setItems(String items) {
		this.items = items;
	}

	public Integer getMizanPercent() {
		return mizanPercent;
	}

	public void setMizanPercent(Integer mizanPercent) {
		this.mizanPercent = mizanPercent;
	}

	public Integer getBankPercent() {
		return bankPercent;
	}

	public void setBankPercent(Integer bankPercent) {
		this.bankPercent = bankPercent;
	}
}
