package vaulsys.entity.impl;

import vaulsys.calendar.DayDate;
import vaulsys.entity.Contract;
import vaulsys.entity.MerchantCategory;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "fine_shop_ver")
public class ShopVersion extends FinancialEntityVersion {
	@ManyToOne
	@JoinColumn(name = "parent")
	@ForeignKey(name = "shop_vers_parent_fk")
	private Shop parent;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category")
	@ForeignKey(name = "shop_vers_category_fk")
	private MerchantCategory category;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "visitor")
	@ForeignKey(name = "shop_vers_visitor_fk")
	protected Visitor visitor;
	
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "type", column = @Column(name = "vis_type"))})
	private VisitorType visitorType;

	private String economicCode;
	
	private String agentCode;
	
	private Long nationalNumber;
	
	@AttributeOverride(name = "date", column=@Column(name="lease_dt"))
	private DayDate leaseDate; 

	
	@Embedded
	@AttributeOverrides( {
			@AttributeOverride(name = "startDate.date", column = @Column(name = "contract_start_date")),
			@AttributeOverride(name = "endDate.date", column = @Column(name = "contract_end_date")),
			@AttributeOverride(name = "contractNumber", column = @Column(name = "contract_num"))})
	protected Contract contract;

	public void setVisitor(Visitor visitor) {
		this.visitor = visitor;
	}

	public Visitor getVisitor() {
		return visitor;
	}
	
	public VisitorType getVisitorType() {
		return visitorType;
	}

	public void setVisitorType(VisitorType visitorType) {
		this.visitorType = visitorType;
	}

	public ShopVersion() {
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

	public Shop getParent() {
		return parent;
	}

	public void setParent(Shop parent) {
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
	
	public DayDate getLeaseDate() {
		return leaseDate;
	}

	public void setLeaseDate(DayDate leaseDate) {
		this.leaseDate = leaseDate;
	}

	public Contract getContract() {
		return contract;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}

	public String getAgentCode() {
		return agentCode;
	}

	public void setAgentCode(String agentCode) {
		this.agentCode = agentCode;
	}
}
