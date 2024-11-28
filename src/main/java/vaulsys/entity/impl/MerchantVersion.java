package vaulsys.entity.impl;

import vaulsys.entity.Contract;
import vaulsys.entity.MerchantCategory;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "fine_merchant_ver")
public class MerchantVersion extends FinancialEntityVersion {

	@ManyToOne
	@JoinColumn(name = "parent")
	@ForeignKey(name = "merch_vers_parent_fk")
	private Merchant parent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category")
	@ForeignKey(name = "merch_vers_category_fk")
	private MerchantCategory category;

	private String economicCode;

	private Long nationalNumber;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "visitor")
	@ForeignKey(name = "merch_vers_visitor_fk")
	protected Visitor visitor;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "startDate.date", column = @Column(name = "contract_start_date")),
		@AttributeOverride(name = "endDate.date", column = @Column(name = "contract_end_date")),
		@AttributeOverride(name = "contractNumber", column = @Column(name = "contract_num"))
	})
	protected Contract contract;

	public MerchantVersion() {
	}

//    public IVersion clone() {
//        MerchantVersion version = new MerchantVersion();
//        version.validRange = validRange.clone();
//        version.feeProfile = feeProfile;
//        version.contract = contract;
//        version.contact = contact;
//        version.authorizationProfile = authorizationProfile;
//        version.visitor = visitor; 
//        return version;
//    }

	public Merchant getParent() {
		return parent;
	}

	public void setParent(Merchant parent) {
		this.parent = parent;
	}

	public MerchantCategory getCategory() {
		return category;
	}

	public void setCategory(MerchantCategory category) {
		this.category = category;
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

	public Contract getContract() {
		return contract;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}

	public void setVisitor(Visitor visitor) {
		this.visitor = visitor;
	}

	public Visitor getVisitor() {
		return visitor;
	}
}
