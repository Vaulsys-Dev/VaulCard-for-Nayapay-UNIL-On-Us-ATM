package vaulsys.protocols.ifx.imp;

import vaulsys.persistence.IEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ifx_topup_data")
public class TopupData implements IEntity<Long>, Cloneable {

	@Id
	@GeneratedValue(generator = "topupdata-seq-gen")
	@org.hibernate.annotations.GenericGenerator(name = "topupdata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "topupdata_seq") })
	Long id;
	
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "company")
//    @ForeignKey(name="topup_organization_fk")
//	private Organization topupCompany;
	
//	@Column(name="company", insertable = false, updatable = false)
	@Column(name="company")
	private Long topupCompanyCode;

	@Column(name="phone_no")
	private Long cellPhoneNumber;
	
	private Long serialNo;
	
	@Column(length=3)
	private String rsCode;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCellPhoneNumber() {
		return cellPhoneNumber;
	}

	public void setCellPhoneNumber(Long cellPhoneNumber) {
		this.cellPhoneNumber = cellPhoneNumber;
	}

	public Long getSerialNo() {
		return serialNo;
	}
	
	public void setSerialNo(Long serialNo) {
		this.serialNo = serialNo;
	}

	public Long getTopupCompanyCode() {
		return topupCompanyCode;
	}

	public void setTopupCompanyCode(Long topupCompanyCode) {
		this.topupCompanyCode = topupCompanyCode;
	}

	public void copyFields(TopupData source) {
		if(topupCompanyCode == null)
			topupCompanyCode = source.getTopupCompanyCode();
		
		if(cellPhoneNumber == null)
			cellPhoneNumber = source.getCellPhoneNumber();
		
		if(serialNo == null)
			serialNo = source.getSerialNo();
	}

	public TopupData copy() {
		return (TopupData) clone();
	}

	@Override
	protected Object clone() {
		TopupData obj = new TopupData();
		obj.setCellPhoneNumber(this.cellPhoneNumber);
		obj.setSerialNo(this.serialNo);
		obj.setTopupCompanyCode(this.topupCompanyCode);
		return obj;
	}

	public String getRsCode() {
		return rsCode;
	}

	public void setRsCode(String rsCode) {
		this.rsCode = rsCode;
	}

	
//	public Organization getTopupCompany() {
//		return topupCompany;
//	}
//
//	public void setTopupCompany(Organization topupCompany) {
//		this.topupCompany = topupCompany;
//	}
}
