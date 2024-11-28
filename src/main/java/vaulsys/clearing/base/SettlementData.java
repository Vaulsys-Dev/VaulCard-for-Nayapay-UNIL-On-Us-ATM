package vaulsys.clearing.base;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.consts.SettlementDataType;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.persistence.GeneralDao;
import vaulsys.persistence.IEntity;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.SettlementInfo;
import vaulsys.transaction.Transaction;
import vaulsys.util.ConfigUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "settlement_data")
public class SettlementData implements IEntity<Long> {

	@Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="settledata-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "settledata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "settledata_seq")
    				})
    private Long id;

	@Embedded
	@AttributeOverride(name = "type", column = @Column(name = "type"))
	SettlementDataType type;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal")
	@ForeignKey(name="stldata_term_fk")
    private Terminal terminal;

    @Column(name = "terminal", insertable = false, updatable = false)
    private Long terminalId;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "fin_entity")
	@ForeignKey(name="stldata_finentity_fk")
    private FinancialEntity financialEntity;
    
    @Column(name = "fin_entity", insertable = false, updatable = false)
    private Long financialEntityId;

    private int numTransaction;

//    @Embedded
//    @AttributeOverrides({
//    @AttributeOverride(name = "dayDate.date", column = @Column(name = "settlement_date")),
//    @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "settlement_time"))
//            })
//    private DateTime settlementTime;
    
    @Column(name = "settlement_dt")
	private Long settlementTimeLong;

    private long totalAmount;

    private long totalFee;

    private long totalSettlementAmount;
    
    @Column(name = "doc_num")
    private String documentNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stl_state")
	@ForeignKey(name="stldata_stlstate_fk")
    private SettlementState settlementState;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "settlementData")
    private Set<SettlementInfo> settlementInfos;
    
    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Transaction> transactions; 
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clr_prof")
    @ForeignKey(name="stldata_clrprof_fk")
    private ClearingProfile clearingProfile;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stl_report")
	@ForeignKey(name="stldata_stlreport_fk")
	private SettlementReport settlementReport;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "settlementData")
    private List<SettlementDataReport> settlementDataReport;

//    @Lob
//    @Column(name="third_report")
//    private String oldReport;
    
    public SettlementData() {
    }
//TODO: settlementTimeLong bayad ezafe beshe be inam.
    public SettlementData (FinancialEntity entity, Terminal terminal, ClearingProfile clearingProfile, SettlementDataType type, DateTime settlementTime) {
    	this.financialEntity = entity;
    	this.terminal = terminal;
    	this.clearingProfile = clearingProfile;
    	this.type = type;
//    	this.settlementTime = settlementTime;
    	this.settlementTimeLong = settlementTime.getDateTimeLong();
    }
    
	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }
    
    public Long getTerminalId() {
		return terminalId;
	}

	public int getNumTransaction() {
        return numTransaction;
    }

    public void setNumTransaction(int numTransaction) {
        this.numTransaction = numTransaction;
    }

    public DateTime getSettlementTime() {
    	Long dayLong = settlementTimeLong / 1000000L;
    	DayDate day = new DayDate();
    	day.setDate(dayLong.intValue());
    	
    	Long timeLong = settlementTimeLong % 1000000L;
    	DayTime time = new DayTime();
    	time.setDayTime(timeLong.intValue());
    	
    	DateTime dateTime = new DateTime(day, time);
    	return dateTime;
    	
//        return settlementTime;
    }

    public void setSettlementTime(DateTime settlementTime) {
        this.settlementTimeLong = settlementTime.getDateTimeLong();
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(long totalFee) {
        this.totalFee = totalFee;
    }

    public long getTotalSettlementAmount() {
        return totalSettlementAmount;
    }

    public void setTotalSettlementAmount(long totalSettlementAmount) {
        this.totalSettlementAmount = totalSettlementAmount;
    }

	public FinancialEntity getFinancialEntity() {
		return financialEntity;
	}

	public void setFinancialEntity(FinancialEntity financialEntity) {
		this.financialEntity = financialEntity;
	}

	public Long getFinancialEntityId() {
		return financialEntityId;
	}

	public SettlementState getSettlementState() {
		return settlementState;
	}

	public void setSettlementState(SettlementState settlementState) {
		this.settlementState = settlementState;
	}
	
	public Set<Transaction> getTransactions() {
		return transactions;
	}

	public void addTransaction(Transaction transaction) {
		if (transactions == null)
			transactions = new HashSet<Transaction>();
//		transaction./*getSourceSettleInfo().*/setSettlementData(this);
		transactions.add(transaction);
	}
	
	public void addTransactionNew(Transaction transaction) {
		//		if (transactions == null)
//			transactions = new HashSet<Transaction>();
//		transactions.add+(transaction);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("settleData", this.id);
		params.put("trx", transaction.getId());
		GeneralDao.Instance.executeSqlUpdate("insert into "+ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA)+".settlement_data_trx_transaxion (SETTLEMENT_DATA_ID, TRANSACTIONS_ID) " +
				" values(:settleData, :trx)", params);
		
//				" values(" +this.id+", "+transaction.getId()+")");
	}

	public void removeTransaction(Transaction transaction) {
		if (transactions == null)
			return;
		transactions.remove(transaction);
	}
	
	public void removeTransactions(List<Transaction> transactions) {
		if (this.transactions == null || transactions == null)
			return;
		this.transactions.removeAll(transactions);
	}
	
	public Set<SettlementInfo> getSettlementInfos() {
		return settlementInfos;
	}
	
	public void addSettlementInfo(SettlementInfo settlementInfo) {
		if (settlementInfos == null)
			settlementInfos = new HashSet<SettlementInfo>();
		settlementInfo.setSettlementData(this);
		settlementInfos.add(settlementInfo);
	}

	public void addSettlementInfoNew(SettlementInfo settlementInfo) {
		settlementInfo.setSettlementData(this);
	}
	

	public void removeSettlementInfo(SettlementInfo settlementInfo) {
		if (settlementInfos == null)
			return;
		settlementInfo.setSettlementData(null);
		settlementInfos.remove(settlementInfo);
	}

	public byte[] getReport() {
		if(settlementDataReport != null){
			for(SettlementDataReport report:settlementDataReport){
				if(SettlementDataReportType.MAIN_REPORT.equals(report.getType()))
					return report.getReport();
			}
		}
			
		return null;
	}

	public SettlementDataReport addReport(byte[] report) {
		if(this.settlementDataReport == null){
			this.settlementDataReport = new ArrayList<SettlementDataReport>();
		}
		SettlementDataReport sdr = new SettlementDataReport(this, report, SettlementDataReportType.MAIN_REPORT);
		this.settlementDataReport.add(sdr);
		return sdr;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public ClearingProfile getClearingProfile() {
		return clearingProfile;
	}

	public void setClearingProfile(ClearingProfile clearingProfile) {
		this.clearingProfile = clearingProfile;
	}

	public byte[] getThirdPartyReport() {
		if(settlementDataReport != null){
			for(SettlementDataReport report:settlementDataReport){
				if(SettlementDataReportType.THIRDPARTY_REPORT.equals(report.getType()))
					return report.getReport();
			}
		}
			
		return null;
	}

	public SettlementDataReport addThirdPartyReport(byte[] thirdPartyReport) {
		if(this.settlementDataReport == null){
			this.settlementDataReport = new ArrayList<SettlementDataReport>();
		}
		SettlementDataReport sdr = new SettlementDataReport(this, thirdPartyReport, SettlementDataReportType.THIRDPARTY_REPORT);
		this.settlementDataReport.add(sdr);
		return sdr;
	}

	public SettlementDataType getType() {
		return type;
	}

	public void setType(SettlementDataType type) {
		this.type = type;
	}
	
	@Override
	public String toString(){
		return id != null ? id.toString() : "";
	}

	public SettlementReport getSettlementReport() {
		return settlementReport;
	}

	public void setSettlementReport(SettlementReport settlementReport) {
		this.settlementReport = settlementReport;
	}

	public List<SettlementDataReport> getSettlementDataReport() {
		return settlementDataReport;
	}

	public void setSettlementDataReport(List<SettlementDataReport> settlementDataReport) {
		this.settlementDataReport = settlementDataReport;
	}
	
	public Long getSettlementTimeLong() {
		return settlementTimeLong;
	}

	public void setSettlementTimeLong(Long settlementTimeLong) {
		this.settlementTimeLong = settlementTimeLong;
	}

//	public String getOldReport() {
//		return oldReport;
//	}
//
//	public void setOldReport(String oldReport) {
//		this.oldReport = oldReport;
//	}
	
}
