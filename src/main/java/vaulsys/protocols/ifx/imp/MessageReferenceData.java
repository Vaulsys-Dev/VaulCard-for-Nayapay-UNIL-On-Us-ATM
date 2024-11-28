package vaulsys.protocols.ifx.imp;


import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.transaction.Transaction;
import vaulsys.util.Util;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "ifx_message_reference_data")
public class MessageReferenceData implements IEntity<Long>, Cloneable {

    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="msgrefdata-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "msgrefdata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "msgrefdata_seq")
    				})
    private Long id;

    private String MessageType = "0200";

    private String TrnSeqCounter;
    
    private String networkTrnInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refSorush_trx", nullable = true, updatable = true)
    @ForeignKey(name="refSorush_trx_fk")
    private Transaction refSorushiTransaction;

    @Column(name = "refSorush_trx", insertable = false, updatable = false)
    private Long refSorushiTransactionId;

    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "dayDate.date", column = @Column(name = "orig_date")),
    	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "orig_time"))
    })
    private DateTime OrigDt;

    private String BankId;
    private String FwdBankId;
    
    private String AppPAN;
    
	private String TerminalId;
    
    public MessageReferenceData() {
		super();
	}

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	
	public String getBankId() {
		return this.BankId;
	}

	
	public String getFwdBankId() {
		return this.FwdBankId;
	}

	
	public String getMessageType() {
		return this.MessageType;
	}

	
	public DateTime getOrigDt() {
		return this.OrigDt;
	}


	
	public String getTrnSeqCounter() {
		return this.TrnSeqCounter;
	}

	
	public void setBankId(String bankId) {
		this.BankId = bankId;
	}

	
	public void setFwdBankId(String fwdBankId) {
		this.FwdBankId = fwdBankId;
	}

	
	public void setMessageType(String messageType) {
		this.MessageType = messageType;
	}

	
	public void setOrigDt(DateTime origDt) {
		this.OrigDt = origDt;
	}

	
	public void setTrnSeqCounter(String trnSeqCounter) {
		this.TrnSeqCounter = trnSeqCounter;
	}
	
	public Transaction getRefSorushiTransaction() {
		return refSorushiTransaction;
	}

	public void setRefSorushiTransaction(Transaction refSorushiTransaction) {
		this.refSorushiTransaction = refSorushiTransaction;
	}

    public Long getRefSorushiTransactionId() {
        return refSorushiTransactionId;
    }

    public void setRefSorushiTransactionId(Long refSorushiTransactionId) {
        this.refSorushiTransactionId = refSorushiTransactionId;
    }

    protected Object clone() {
		MessageReferenceData obj = new MessageReferenceData();
		obj.setBankId(BankId);
		obj.setFwdBankId(FwdBankId);
		obj.setMessageType(this.MessageType);
		obj.setOrigDt(OrigDt);
		obj.setTrnSeqCounter(TrnSeqCounter);
		obj.setNetworkTrnInfo(networkTrnInfo);
		obj.setTerminalId(TerminalId);
		obj.setAppPAN(AppPAN);
		return obj;
	}
	
	
	public MessageReferenceData copy() {
		return (MessageReferenceData) clone();
	}

	public void copyFields(MessageReferenceData source) {
		if (getBankId()== null && source.getBankId() != null)
			setBankId(source.getBankId());
		if (getFwdBankId()== null && source.getFwdBankId() != null)
			setFwdBankId(source.getFwdBankId());
		if (!Util.hasText(getMessageType()) && Util.hasText(source.getMessageType()))
			setMessageType(source.getMessageType());
		if (getOrigDt()== null && source.getOrigDt() != null)
			setOrigDt(source.getOrigDt());
		if (!Util.hasText(getTrnSeqCounter())&& Util.hasText(source.getTrnSeqCounter()))
			setTrnSeqCounter(source.getTrnSeqCounter());
		if (!Util.hasText(getNetworkTrnInfo())&& Util.hasText(source.getNetworkTrnInfo()))
			setNetworkTrnInfo(source.getNetworkTrnInfo());
		if (!Util.hasText(getTerminalId())&& Util.hasText(source.getTerminalId()))
			setTerminalId(source.getTerminalId());
		if (!Util.hasText(getAppPAN())&& Util.hasText(source.getAppPAN()))
			setAppPAN(source.getAppPAN());
	}

	public String getNetworkTrnInfo() {
		return networkTrnInfo;
	}

	public void setNetworkTrnInfo(String networkTrnInfo) {
		this.networkTrnInfo = networkTrnInfo;
	}

	public String getAppPAN() {
		return AppPAN;
	}

	public void setAppPAN(String appPAN) {
		AppPAN = appPAN;
	}

	public String getTerminalId() {
		return TerminalId;
	}

	public void setTerminalId(String terminalId) {
		TerminalId = terminalId;
	}
}
