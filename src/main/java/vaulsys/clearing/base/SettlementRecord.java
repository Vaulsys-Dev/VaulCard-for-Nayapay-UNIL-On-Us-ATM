package vaulsys.clearing.base;

import vaulsys.clearing.ClearingService;
import vaulsys.clearing.consts.SettlementDataCriteria;
import vaulsys.clearing.consts.SettlementDataType;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.Transaction;
import vaulsys.wfe.ProcessContext;

import javax.persistence.*;

import org.apache.log4j.Logger;
import org.hibernate.annotations.ForeignKey;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "settlement_record")
public class SettlementRecord implements IEntity<Long> {

    @Id
    @Column(name = "trx")
    private Long transactionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trx", insertable = false, updatable = false)
    @ForeignKey(name="stlrecord_trx_fk")
    private Transaction transaction;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ifx_id", insertable = false, updatable = false)
    @ForeignKey(name="stlrecord_ifx_fk")
    private Ifx ifx;


    @Column(name = "ifx_id")
    private Long ifxId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal")
    @ForeignKey(name="stlrecord_term_fk")
    private Terminal terminal;

    @Column(name = "terminal", insertable = false, updatable = false)
    private Long terminalId;

    @Column(name = "received_dt")
    private Long receivedDt;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "type"))
    })
    private SettlementRecordType settlementRecordType;

    @ManyToOne
    @JoinColumn(name = "clr_prof")
    @ForeignKey(name = "stlrecord_clrprof_fk")
    private ClearingProfile clearingProfile;

    public SettlementRecord() {
    }

    public static SettlementRecord getInstance(Transaction transaction, Ifx ifx, ClearingProfile clearingProfile, Terminal terminal, /*ClearingProfile clearingProfile, */Long receivedDt) {
        List<SettlementDataType> types = ClearingService.getSettlementDataTypes(clearingProfile);

        boolean terminalType = false;
        boolean trnType = false;
        boolean ifxType = false;

        boolean hasBeenSetTerminalType = false;
        boolean hasBeenSetTrnType = false;
        boolean hasBeenSetIfxType = false;

        for (SettlementDataType type : types) {
            SettlementDataCriteria criteria = ClearingService.getSettlementDataCriteria(clearingProfile, type);
            if (criteria == null || criteria.getCriteriaDatas() == null || criteria.getCriteriaDatas().isEmpty()) {
                return null;
            }
            Map<Class, List<Object>> criteriaNameValues = ClearingService.getSeperateCriteriaByName(criteria.getCriteriaDatas());
            Set<Class> keySet = criteriaNameValues.keySet();

            for (Class criteriaName : keySet) {

                List<Object> criteriaValues = criteriaNameValues.get(criteriaName);
                for (Object criteriaValue : criteriaValues) {
                    if (TerminalType.class.equals(criteriaName) && criteriaValue != null) {
                        if(terminalType) continue;
                        hasBeenSetTerminalType = true;
                        if(ifx.getNetworkTrnInfo().getTerminalType().getCode() == Integer.parseInt(criteriaValue.toString()))
                            terminalType = true;
                    }

                    if ( TrnType.class.equals(criteriaName) && criteriaValue != null) {
                        if(trnType) continue;
                        hasBeenSetTrnType = true;
                        if(ifx.getTrnType().getType() == Integer.parseInt(criteriaValue.toString()))
                            trnType = true;
                    }

                    if ( IfxType.class.equals(criteriaName) && criteriaValue != null) {
                        if(ifxType) continue;
                        hasBeenSetIfxType = true;
                        if(ifx.getIfxType().getType() == Integer.parseInt(criteriaValue.toString()))
                            ifxType = true;
                    }
                }
            }
        }
        if((hasBeenSetTerminalType && !terminalType) || ( hasBeenSetTrnType && !trnType) || ( hasBeenSetIfxType && !ifxType))
            return new SettlementRecord(transaction, ifx.getId(), terminal, receivedDt, SettlementRecordType.ONLYFORFORM1);
        return new SettlementRecord(transaction, ifx.getId(), terminal, receivedDt, SettlementRecordType.SETTLEMENTRECORD);

    }

    public SettlementRecord(Transaction transaction, Long ifxId, /*IfxType ifxType,*/ Terminal terminal, /*ClearingProfile clearingProfile, */Long receivedDt, SettlementRecordType settlementRecordType) {
        setTransaction(transaction);
        this.ifxId = ifxId;
//		this.ifxType = ifxType;
        setTerminal(terminal);
//		this.clearingProfile = clearingProfile;
        this.receivedDt = receivedDt;
        this.settlementRecordType = settlementRecordType;
    }


    public Ifx getIfx() {
        return ifx;
    }

    public void setIfx(Ifx ifx) {
        this.ifx = ifx;
    }

    public Long getId() {
        return getTransactionId();
    }

    public void setId(Long id) {
        setTransactionId(id);
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(Long terminalId) {
        this.terminalId = terminalId;
    }

    public Long getReceivedDt() {
        return receivedDt;
    }

    public void setReceivedDt(Long receivedDt) {
        this.receivedDt = receivedDt;
    }

    public ClearingProfile getClearingProfile() {
        return clearingProfile;
    }

    public void setClearingProfile(ClearingProfile clearingProfile) {
        this.clearingProfile = clearingProfile;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
        if (terminal != null) {
            setClearingProfile(ProcessContext.get().getClearingProfile(terminal.getOwnOrParentClearingProfileId()));
        }

    }

    public Long getIfxId() {
        return ifxId;
    }

    public void setIfxId(Long ifxId) {
        this.ifxId = ifxId;
    }

//	public IfxType getIfxType() {
//		return ifxType;
//	}
//
//	public void setIfxType(IfxType ifxType) {
//		this.ifxType = ifxType;
//	}

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
        if (transaction != null)
            this.transactionId = transaction.getId();
    }

    public SettlementRecordType getSettlementRecordType() {
        return settlementRecordType;
    }

    public void setSettlementRecordType(SettlementRecordType settlementRecordType) {
        this.settlementRecordType = settlementRecordType;
    }
}
