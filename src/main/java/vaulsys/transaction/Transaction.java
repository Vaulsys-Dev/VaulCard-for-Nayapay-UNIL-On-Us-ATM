package vaulsys.transaction;

import vaulsys.calendar.DateTime;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.persistence.GeneralDao;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.TerminalClearingMode;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.impl.ManuallyProcessdTransaction;
import vaulsys.wfe.GlobalContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "trx_transaxion")
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class Transaction implements IEntity<Long> {
	private transient Logger logger = Logger.getLogger(Transaction.class);
	
    @Id
    @GeneratedValue(generator="trx-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "trx-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "trx_seq")
				})
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ref_trx", nullable = true)
    @ForeignKey(name="trx_reftrx_fk")
    private Transaction referenceTransaction;
    
    @Column(name="ref_trx", insertable = false, updatable = false)
    private Long referenceId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="first_trx", nullable = true)
    @ForeignKey(name="trx_firsttrx_fk")
    private Transaction firstTransaction;

    @Column(name = "first_trx", insertable = false, updatable = false)
    private Long firstId;

    @Transient
    private transient LifeCycle lifeCycle;
    
    @Column(name="lifecycle", nullable = true)
    private Long lifeCycleId;
    
    @Column(name = "debug_tag")
    private String debugTag;

//    Boolean authorized = false;

    @Embedded
    @AttributeOverride(name = "type", column = @Column(name = "trx_type"))
    private TransactionType transactionType;

    @Embedded
    @AttributeOverrides({
    @AttributeOverride(name = "dayDate.date", column = @Column(name = "begin_date")),
    @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "begin_time"))
            })
    private DateTime beginDateTime;

    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY)
    private Set<Message> messages;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "src_clr_flg", nullable = true, updatable = true)
    @ForeignKey(name="trx_src_clr_fk")
    private ClearingInfo sourceClearingInfo; 
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "src_stl_flg", nullable = true, updatable = true)
    @ForeignKey(name="trx_src_stl_fk")
    private SettlementInfo sourceSettleInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dst_clr_flg", nullable = true, updatable = true)
    @ForeignKey(name="trx_dst_clr_fk")
    private ClearingInfo destinationClearingInfo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dst_stl_flg", nullable = true, updatable = true)
    @ForeignKey(name="trx_dst_stl_fk")
    private SettlementInfo destinationSettleInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trd_stl_flg", nullable = true, updatable = true)
    @ForeignKey(name="trx_trd_stl_fk")
    private SettlementInfo thirdPartySettleInfo;
    
	@OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY)
    private List<ManuallyProcessdTransaction> manuallyProcessdTransactions;

	
	@Transient
	private transient Ifx outgoingIfx;
	
	@Transient
	private transient Ifx incomingIfx;

	public Transaction() {
    }

    public Transaction(TransactionType type) {
        super();
        this.beginDateTime = DateTime.now();
        this.transactionType = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DateTime getBeginDateTime() {
        return beginDateTime;
    }

    public void setBeginDateTime(DateTime beginDateTime) {
        this.beginDateTime = beginDateTime;
    }

    public Transaction getReferenceTransaction() {
        return referenceTransaction;
    }
    
    public Long getReferenceId(){
    	return referenceId;
    }

    public Transaction getOriginatorTransaction() {
        Transaction refTrx = firstTransaction;

        if (firstTransaction == null || this == firstTransaction)
            return this;

        //TODO: this block is build unlimited loop!!
//        while (refTrx != null && refTrx.firstTransaction != null) {
//            refTrx = refTrx.firstTransaction;
//            System.err.println("refTrx");
//        }

        return refTrx;
    }

    public void setReferenceTransaction(Transaction referenceTransaction) {
        this.referenceTransaction = referenceTransaction;
        if (referenceTransaction != null) {
        	setLifeCycle(referenceTransaction.getLifeCycle());
        	
        	if (incomingIfx != null &&
        			ISOFinalMessageType.isResponseMessage(incomingIfx.getIfxType()) &&
	        		!ISOFinalMessageType.isGetAccountMessage(incomingIfx.getIfxType()) &&
	        		ISOResponseCodes.shouldBeRemovedFromSecurityMap(incomingIfx.getRsCode())) {
	        	GlobalContext.getInstance().removeSecurityData(incomingIfx.getTransaction().getLifeCycleId());
	        }
        }
    }

    @Transient
    public Message getInputMessage() {
    	if(this.messages == null)
    		return null;
    	
        for (Message msg : messages) {
        	if(msg.isIncomingMessage() || msg.isScheduleMessage()){
        		return msg;
        	}
        }
        return null;
    }
    
    public Ifx getIncomingIfx(){
    	if(incomingIfx != null)
    		return incomingIfx;
    	
//    	if(this.messages == null)
//    		return null;
    	
    	logger.debug("find incoming ifx of trx: "+this.id);
    	String query = "from Ifx i where i.transaction = :trx and i.ifxDirection in (:dir)";
    	List<IfxDirection> direction =  new ArrayList<IfxDirection>();
    	direction.add(IfxDirection.INCOMING);
    	
    	Map<String, Object> params = new HashMap<String, Object>();
    	params.put("trx", this);
    	params.put("dir", direction);
    	incomingIfx = (Ifx)GeneralDao.Instance.findUniqueObject(query, params);
    	
    	if(incomingIfx == null){
    		logger.debug("Coud not find incoming ifx try old method");
    		Message inputMessage = this.getInputMessage();
    		if(inputMessage != null){
        		incomingIfx = inputMessage.getIfx();
    		}else{
    			logger.fatal("inputMessage is null, after trying old method...");
    			return null;
    		}
    	}
    	 return incomingIfx;    	
    }
    
	public Terminal getIncomingIfxOrMessageEndpoint(){
		if(getIncomingIfx() != null)
			return getIncomingIfx().getEndPointTerminal();
		return getInputMessage().getEndPointTerminal();		
	}
	
	public Terminal getOutgoingIfxOrMessageEndpoint(){
		if(getOutgoingIfx() != null)
			return getOutgoingIfx().getEndPointTerminal();
		return getOutputMessage().getEndPointTerminal();
	}

	public Terminal getOutgoingIfxOrMessageEndpoint2(){
		logger.debug("getOutgoingIfxOrMessageEndpoint2");
		if(getOutgoingIfx2() != null){
			logger.debug("getOutgoingIfx2() != null");			
			return getOutgoingIfx2().getEndPointTerminal();
		}
		logger.debug("before getOutputMessage2");		
		return getOutputMessage2().getEndPointTerminal();
	}
    
    public void setInputMessage(Message message) {
    	if(this.messages ==null)
    		this.messages=new HashSet<Message>();
        
        this.messages.add(message);
    }

    @Transient
    public Message getOutputMessage() {
    	if(this.messages == null)
    		return null;
    	
        for (Message msg : this.messages) {
        	if(msg.isOutgoingMessage()){
                return msg;
            }
        }

        return null;
    }

    @Transient
    public Message getOutputMessage2() {
    	logger.debug("getOutputMessage2");
    	if(this.messages == null)
    		return null;
    	
        for (Message msg : this.messages) {
        	if(msg.isOutgoingMessage()){
                return msg;
            }
        }

        return null;
    }

   
    public Ifx getOutgoingIfx() {
    	if (outgoingIfx != null)
    		return outgoingIfx;
  	   	
    	String query = "from Ifx i where i.transaction = :trx and i.ifxDirection in (:dir)";
    	List<IfxDirection> direction=new ArrayList<IfxDirection>();
    	direction.add(IfxDirection.OUTGOING);
    	direction.add(IfxDirection.SELF_GENERATED);
    	
    	Map<String, Object> params = new HashMap<String, Object>();
        params.put("trx", this);
        params.put("dir", direction);
        outgoingIfx = (Ifx) GeneralDao.Instance.findUniqueObject(query, params);

        if(outgoingIfx == null){
        	logger.debug("Could not find outgoing ifx try old method");
        	Message outputMessage = this.getOutputMessage();
        	if(outputMessage != null){
        		outgoingIfx = outputMessage.getIfx();
        	}else{
        		logger.fatal("outputMessage is null, after trying old method...");
        		return null;
        	}
        }
		
        return outgoingIfx;
   }

    public Ifx getOutgoingIfx2() {
    	if (outgoingIfx != null){
    		logger.debug("outgoingIfx != null");
    		return outgoingIfx;
    	}
    	   	
    	String query = "from Ifx i where i.transaction = :trx and i.ifxDirection in (:dir)";
    	List<IfxDirection> direction=new ArrayList<IfxDirection>();
    	direction.add(IfxDirection.OUTGOING);
    	direction.add(IfxDirection.SELF_GENERATED);
    	
    	Map<String, Object> params = new HashMap<String, Object>();
        params.put("trx", this);
        params.put("dir", direction);
        outgoingIfx = (Ifx) GeneralDao.Instance.findUniqueObject(query, params);
		logger.debug("after query findUniqueObject");

        if(outgoingIfx == null){
        	logger.debug("Could not find outgoing ifx try old method");
        	Message outputMessage = this.getOutputMessage();
        	if(outputMessage != null){
        		outgoingIfx = outputMessage.getIfx();
        	}else{
        		logger.fatal("outputMessage is null, after trying old method...");
        		return null;
        	}
        }
		
		logger.debug("end getOutgoingIfx2");
        return outgoingIfx;
   }

    public void removeAllOutputMessages() {
    	if(this.messages == null)
    		return;
    	
        Iterator<Message> it = messages.iterator();

        while (it.hasNext()) {
            Message msg = it.next();
            if (MessageType.OUTGOING.equals(msg.getType())){
            	msg.setNeedToBeSent(false);
//            	msg.setNeedResponse(false);
//            	msg.setNeedToBeInstantlyReversed(false);
                it.remove();
            }
        }
    }

    @Transient
    public void addOutputMessage(Message message) {
    	if(this.messages == null)
    		this.messages = new HashSet<Message>();
        this.messages.add(message);
    }

    public Set<Message> getMessages() {
        return messages;
    }

    public void setMessages(Set<Message> messages) {
        this.messages = messages;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getSequenceCounter() {
        return getIncomingIfx().getSrc_TrnSeqCntr();
    }

    public ClearingInfo getSourceClearingInfo() {
        return sourceClearingInfo;
    }

    public void setSourceClearingInfo(ClearingInfo sourceClearingInfo) {
        this.sourceClearingInfo = sourceClearingInfo;
    }

    public ClearingInfo getDestinationClearingInfo() {
        return destinationClearingInfo;
    }

    public void setDestinationClearingInfo(ClearingInfo destinationClearingInfo) {
        this.destinationClearingInfo = destinationClearingInfo;
    }

    public ClearingInfo findRelatedClearingInfo(TerminalClearingMode clearingMode) {
        switch (clearingMode) {
            case ACQUIER:
            case ISSUER:
            	return getDestinationClearingInfo();
            case TERMINAL:
                return getSourceClearingInfo();
        }
        return null;
    }

    public void setRelatedClearingInfo(TerminalClearingMode clearingMode, ClearingInfo clearingInfo) {
        switch (clearingMode) {
            case ACQUIER:
            case ISSUER:
            	setDestinationClearingInfo(clearingInfo);
            	break;
            case TERMINAL:
                setSourceClearingInfo(clearingInfo);
                break;
        }
    }

	public LifeCycle getLifeCycle() {
		if (lifeCycle != null)
			return lifeCycle;
		
		if (lifeCycleId == null)
			return null;
		
		String query = "from LifeCycle c where c.id = :lifeCycleId ";
    	
    	Map<String, Object> params = new HashMap<String, Object>();
    	params.put("lifeCycleId", lifeCycleId);
    	lifeCycle = (LifeCycle)GeneralDao.Instance.findUniqueObject(query, params);
    	
    	return lifeCycle;
	}
	
	public LifeCycle getAndLockLifeCycle(LockMode lockMode) {
		if (lifeCycle != null) {
			if (LockMode.UPGRADE.equals(GeneralDao.Instance.getCurrentLockMode(lifeCycle)) ||
					LockMode.WRITE.equals(GeneralDao.Instance.getCurrentLockMode(lifeCycle))) {
				logger.info("lifeCycle is locked before...");
				return lifeCycle;
			}
		}

		long currentTimeMillis = System.currentTimeMillis();
		logger.debug("lifeCycle is locking...");
		if (lifeCycle == null) {
			lifeCycle = GeneralDao.Instance.load(LifeCycle.class, lifeCycleId, lockMode);
			
		} else {
			lifeCycle = GeneralDao.Instance.load(LifeCycle.class, lifeCycleId, lockMode);
			GeneralDao.Instance.refresh(lifeCycle);
			
		}
		
		logger.debug("LifeCycle[" + lifeCycleId + ") has beeb locked and it's reloaded!, " + (System.currentTimeMillis()-currentTimeMillis));
		return lifeCycle;
	}

	public Long getLifeCycleId() {
    	return lifeCycleId;
    }
    
    private void setLifeCycleId(Long lifeCycleId) {
    	this.lifeCycleId = lifeCycleId;
    }
    
    public void setLifeCycle(LifeCycle lifeCycle) {
    	this.lifeCycle = lifeCycle;
    	
    	if (lifeCycle != null)
    		setLifeCycleId(lifeCycle.getId());
    	else 
    		setLifeCycleId(null);
    }
    
	public Transaction getFirstTransaction() {
		return firstTransaction;
	}

	public void setFirstTransaction(Transaction firstTransaction) {
		this.firstTransaction = firstTransaction;
		if (firstTransaction != null) {
        	setLifeCycle(firstTransaction.getLifeCycle());
        	
        	if (incomingIfx != null &&
        			ISOFinalMessageType.isResponseMessage(incomingIfx.getIfxType()) &&
	        		!ISOFinalMessageType.isGetAccountMessage(incomingIfx.getIfxType()) &&
	        		ISOResponseCodes.shouldBeRemovedFromSecurityMap(incomingIfx.getRsCode())) {
	        	GlobalContext.getInstance().removeSecurityData(incomingIfx.getTransaction().getLifeCycleId());
	        }
		if (incomingIfx != null &&
        			ISOFinalMessageType.isGetAccountMessage(incomingIfx.getIfxType()) &&
	        		ISOResponseCodes.HOST_LINK_DOWN.equals(incomingIfx.getRsCode())) {
	        	GlobalContext.getInstance().removeSecurityData(incomingIfx.getTransaction().getLifeCycleId());
	        } 

		}
	}

	public SettlementInfo getSourceSettleInfo() {
		return sourceSettleInfo;
	}

	public void setSourceSettleInfo(SettlementInfo sourceSettleInfo) {
		this.sourceSettleInfo = sourceSettleInfo;
	}

	public void setDebugTag(String debugTag) {
		this.debugTag = debugTag;
	}

	public String getDebugTag() {
		return debugTag;
	}

	public Transaction getOriginatorTransactionForTransfer() {
		Transaction t = this;
		
		while (t.getReferenceTransaction()!= null && !t.getReferenceTransaction().getDebugTag().equals(IfxType.TRANSFER_CHECK_ACCOUNT_RS.toString())) {
			t = t.getReferenceTransaction();
		}
		if (t.getReferenceTransaction()!= null && t.getReferenceTransaction().getDebugTag().equals(IfxType.TRANSFER_CHECK_ACCOUNT_RS.toString()))
//			t = t.getReferenceTransaction().getFirstTransaction();
			return t;
		return null;
	}

	public List<ManuallyProcessdTransaction> getManuallyProcessdTransactions() {
		return manuallyProcessdTransactions;
	}

	@Override
	public String toString() {
		return id!=null ? id.toString():"";
	}

	public SettlementInfo getDestinationSettleInfo() {
		return destinationSettleInfo;
	}

	public void setDestinationSettleInfo(SettlementInfo destinationSettleInfo) {
		this.destinationSettleInfo = destinationSettleInfo;
	}

	public SettlementInfo getThirdPartySettleInfo() {
		return thirdPartySettleInfo;
	}

	public void setThirdPartySettleInfo(SettlementInfo thirdPartySettleInfo) {
		this.thirdPartySettleInfo = thirdPartySettleInfo;
	}

	public Long getFirstId() {
		return firstId;
	}

	public void setOutgoingIfx(Ifx outgoingIfx2) {
		if(outgoingIfx == null){
			outgoingIfx = outgoingIfx2;
		}
	}

	public void setIncomingIfx(Ifx incomingIfx2) {
		if(incomingIfx == null){
			incomingIfx = incomingIfx2;
		}
	}
}
