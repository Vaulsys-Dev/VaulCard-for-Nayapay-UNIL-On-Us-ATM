package vaulsys.entity.impl;

import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.entity.Contract;
import vaulsys.entity.MerchantCategory;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "fine_merchant")
@ForeignKey(name="merchant_fine_fk")
public class Merchant extends FinancialEntity{

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category")
	@ForeignKey(name = "merchant_category_fk")
	private MerchantCategory category;

	@Column(name = "category", insertable = false, updatable = false)
	private Long categoryId;

	public Long getCategoryId() {
		return categoryId;
	}

	private String economicCode;
	
	private Long nationalNumber;

	//m.rehman: payment method required in reporting
	@Column(name = "payment_method")
	private Integer paymentMethod;

	//m.rehman: merchant type required in reporting
	@Column(name = "merchant_type")
	private Integer merchantType;

	//m.rehman: ibft bank code required in reporting
	@Column(name = "bankid")
	private Integer ibftBankCode;

	//m.rehman: branch code required in reporting
	@Column(name = "branch_code")
	private Integer branchCode;

	//m.rehman: ibft branch code required in reporting
	@Column(name = "ibft_branch_code")
	private Integer ibftBranchCode;
	
	/******** Financial Entity Version Properties ********/
	/******** Start ********/
	@ManyToOne(fetch=FetchType.LAZY) 
	@JoinColumn(name = "visitor")
    @ForeignKey(name="merch_visitor_fk")
	protected Visitor visitor;
	
	@Column(name = "visitor", insertable = false, updatable = false)
	private Long visitorId;
	
	public Long getVisitorId() {
		return visitorId;
	}
	
	public void setVisitor(Visitor visitor) {
		this.visitor = visitor;
		if(visitor!=null)
			visitorId = visitor.getId();
	}
	
	public Visitor getVisitor() {
		return visitor;
	}
	
	@Embedded
	@AttributeOverrides( {
			@AttributeOverride(name = "startDate.date", column = @Column(name = "contract_start_date")),
			@AttributeOverride(name = "endDate.date", column = @Column(name = "contract_end_date")),
			@AttributeOverride(name = "contractNumber", column = @Column(name = "contract_num"))})
	protected Contract contract;

	public Contract getContract() {
		return contract;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}
	
	/******** End ********/
	/******** Financial Entity Version Properties ********/

	public Merchant() {
	}

	public MerchantCategory getCategory() {
		return category;
	}

	public void setCategory(MerchantCategory category) {
		this.category = category;
		if(category!=null)
			categoryId = category.getId();
	}

	@Override
	public FinancialEntityRole getRole() {
		return FinancialEntityRole.MERCHANT;
	}

	public String getEconomicCode() {
		return economicCode;
	}

	public void setEconomicCode(String economicCode) {
		this.economicCode = economicCode;
	}

	public Long getNationalNumber() {
		return nationalNumber;
	}

	public void setNationalNumber(Long nationalNumber) {
		this.nationalNumber = nationalNumber;
	}

	public Integer getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(Integer paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public Integer getMerchantType() {
		return merchantType;
	}

	public void setMerchantType(Integer merchantType) {
		this.merchantType = merchantType;
	}

	public Integer getIbftBankCode() {
		return ibftBankCode;
	}

	public void setIbftBankCode(Integer ibftBankCode) {
		this.ibftBankCode = ibftBankCode;
	}

	public Integer getBranchCode() {
		return branchCode;
	}

	public void setBranchCode(Integer branchCode) {
		this.branchCode = branchCode;
	}

	public Integer getIbftBranchCode() {
		return ibftBranchCode;
	}

	public void setIbftBranchCode(Integer ibftBranchCode) {
		this.ibftBranchCode = ibftBranchCode;
	}

	@Override
	public String toString() {
		return String.format("%s - %s", name, code);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((categoryId == null) ? 0 : categoryId.hashCode());
		result = prime * result + ((contract == null) ? 0 : contract.hashCode());
		result = prime * result + ((economicCode == null) ? 0 : economicCode.hashCode());
		result = prime * result + ((nationalNumber == null) ? 0 : nationalNumber.hashCode());
		result = prime * result + ((visitorId == null) ? 0 : visitorId.hashCode());
		return result;
	}
}
