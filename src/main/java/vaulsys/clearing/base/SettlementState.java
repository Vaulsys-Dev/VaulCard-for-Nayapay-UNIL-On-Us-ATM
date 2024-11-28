package vaulsys.clearing.base;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.AccountingService;
import vaulsys.customer.Core;
import vaulsys.persistence.IEntity;
import vaulsys.user.User;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "settlement_state")
public class SettlementState implements IEntity<Long> {
	@Id
//	@GeneratedValue(generator = "switch-gen")
    @GeneratedValue(generator="settlestate-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "settlestate-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "settlestate_seq")
    				})
	private Long id;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "dayDate.date", column = @Column(name = "settlement_date")),
		@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "settlement_time"))
			})
	private DateTime settlementDate;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "dayDate.date", column = @Column(name = "file_date")),
		@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "file_time"))
			})
	private DateTime settlementFileCreationDate;

	@Embedded
	@AttributeOverrides( { @AttributeOverride(name = "type", column = @Column(name = "core")) })
	private Core core;
	 

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "clr_prof")
	@ForeignKey(name = "stlstate_clrprof_fk")
	private ClearingProfile clearingProfile;

	private String description;

	@Lob
	private String report;

	@Lob
	@Column(name = "stl_report")
	private String settlementReport;
	
	@OneToMany(mappedBy = "settlementState", fetch=FetchType.LAZY)
	@Cascade(value = {CascadeType.ALL})
	private List<SettlementReport> settlementReportList;
	
	@OneToMany(mappedBy = "settlementState", fetch=FetchType.LAZY)
	@OrderBy("id")
	private List<SettlementData> settlementDatas;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "settling_user")
	@ForeignKey(name = "settling_user_fk")
	private User settlingUser;
	
//	@Column(name = "doc_amount")
//	private Long documentAmount;

	@Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "state"))
    })
    private SettlementStateType state;
	
	public SettlementState() {
	}

	public SettlementState(ClearingProfile clearingProfile, Core core, String desc) {
		this.clearingProfile = clearingProfile;
		this.core = core;
		this.description = desc;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DateTime getSettlementDate() {
		return settlementDate;
	}

	public void setSettlementDate(DateTime settlementDate) {
		this.settlementDate = settlementDate;
	}

	public DateTime getSettlementFileCreationDate() {
		return settlementFileCreationDate;
	}

	public void setSettlementFileCreationDate(DateTime settlementFileCreationDate) {
		this.settlementFileCreationDate = settlementFileCreationDate;
	}

	public SettlementStateType getState() {
		return state;
	}

	public void setState(SettlementStateType state) {
		this.state = state;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public List<SettlementData> getSettlementDatas() {
		return settlementDatas;
	}

	public ClearingProfile getClearingProfile() {
		return clearingProfile;
	}

	public void setClearingProfile(ClearingProfile clearingProfile) {
		this.clearingProfile = clearingProfile;
	}

	public String getSettlementReport() {
		return settlementReport;
	}

	public void setSettlementReport(String settlementReport) {
		this.settlementReport = settlementReport;
	}

	public User getSettlingUser() {
		return settlingUser;
	}

	public void setSettlingUser(User settlingUser) {
		this.settlingUser = settlingUser;
	}

//	public Long getDocumentAmount() {
//		return documentAmount;
//	}
//
//	public void setDocumentAmount(Long documentAmount) {
//		this.documentAmount = documentAmount;
//	}

	public void addSettlementData(SettlementData settlementData) {
		if (settlementDatas == null)
			settlementDatas = new ArrayList<SettlementData>();
		settlementDatas.add(settlementData);
		settlementData.setSettlementState(this);
	}

	public void addAllSettlementData(List<SettlementData> settlementData) {
		if (settlementDatas == null)
			settlementDatas = new ArrayList<SettlementData>();
		settlementDatas.addAll(settlementData);
		AccountingService.updateSettlementData(settlementData, this);
	}

	public Core getCore() {
		return core;
	}

	public void setCore(Core core) {
		this.core = core;
	}
	
	public String toString(){
		return id != null ? id.toString() : "";
	}

	public List<SettlementReport> getSettlementReportList() {
		return settlementReportList;
	}

	public void addSettlementReport(Core core, String report, String refNum) {
		if (settlementReportList == null)
			settlementReportList = new ArrayList<SettlementReport>();
		addSettlementReport(new SettlementReport(core, report, refNum, this));
	}
	
	public void addSettlementReport(SettlementReport report) {
		boolean duplicate = false;
		if (settlementReportList == null) {
			settlementReportList = new ArrayList<SettlementReport>();
		}

		int index =0;
		while (!duplicate && index < settlementReportList.size()) {
			if (settlementReportList.get(index++).equals(report)) {
				duplicate = true;
				break;
			}
		}
		if (!duplicate) {
			settlementReportList.add(report);
			report.setSettlementState(this);
		}
	}
}
