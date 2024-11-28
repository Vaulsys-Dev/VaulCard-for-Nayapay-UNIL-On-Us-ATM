package vaulsys.protocols.ifx.imp;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;


@Entity
@Table(name = "ifx_clr_rec_data")
public class ClearingIfxReconcilementData implements IEntity<Long>{

    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="clrifxrecondata-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "clrifxrecondata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "clrifxrecondata_seq")
    				})
    private Long id;
	
	@ManyToOne
	@JoinColumn(name="clr_ifx")
	@ForeignKey(name="clr_ifx_rec__clr_ifx_fk")
	private Ifx clearingIfx;
	
	
	private String trnType;
	
	@Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "dayDate.date", column = @Column(name = "orig_date")),
    	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "orig_time"))
    })
    private DateTime OrigDt;


	private String trnSeqCntr;
	
	private String appPan;
	
	private Long bankId;
	
	private String terminalId;
	
	private Long amount;
		
	
    public ClearingIfxReconcilementData() {
    }
    
	public ClearingIfxReconcilementData(Ifx ifx, String trnType, DateTime origDt, String trnSeqCntr,
			String appPan, Long bankId, String terminalId, Long amount) {
		super();
		this.clearingIfx = ifx;
		this.trnType = trnType;
		OrigDt = origDt;
		this.trnSeqCntr = trnSeqCntr;
		this.appPan = appPan;
		this.bankId = bankId;
		this.terminalId = terminalId;
		this.amount = amount;
	}
	
	@Override
	public Long getId() {
		return id;
	} 
	@Override
	public void setId(Long id) {
		this.id = id; 
	}
	public Ifx getClearingIfx() {
		return clearingIfx;
	}
	public void setClearingIfx(Ifx clearingIfx) {
		this.clearingIfx = clearingIfx;
	}
	public void setTrnType(String trnType) {
		this.trnType = trnType;
	}
	public String getTrnType() {
		return trnType;
	}
	public DateTime getOrigDt() {
		return OrigDt;
	}
	public void setOrigDt(DateTime origDt) {
		OrigDt = origDt;
	}
	public String getTrnSeqCntr() {
		return trnSeqCntr;
	}
	public void setTrnSeqCntr(String trnSeqCntr) {
		this.trnSeqCntr = trnSeqCntr;
	}
	public String getAppPan() {
		return appPan;
	}
	public void setAppPan(String appPan) {
		this.appPan = appPan;
	}
	public Long getBankId() {
		return bankId;
	}
	public void setBankId(Long bankId) {
		this.bankId = bankId;
	}
	public String getTerminalId() {
		return terminalId;
	}
	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}
	public Long getAmount() {
		return amount;
	}
	public void setAmount(Long amount) {
		this.amount = amount;
	}

}
