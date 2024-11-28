package vaulsys.clearing.reconcile.form1;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.transaction.ClearingState;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 * User: k.khodadi
 * Date: 6/29/14
 * Time: 12:07 PM
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "Data_form_One_Cache")
public class DataForm1 implements IEntity<Long>{

	
	
	
    @Id
    @GeneratedValue(generator = "fine-seq-gen2")
    @SequenceGenerator(name = "fine-seq-gen2", allocationSize = 1, sequenceName = "fine_code_seq")
    protected  Long id;

    @Column(name = "trxID")
    protected  Long trxID;

    @Column(name = "trx_status")
    protected Long trx_status;

    @Column(name = "auth_amt")
    protected Long auth_amt;

    @Column(name = "bankid")
    protected  Long bankid;

    @Column(name = "destbankid")
    protected  Long destbankid;

    @Column(name = "My_TrnSeqCntr")
    protected String My_TrnSeqCntr;

    @Column(name = "terminaltype_code")
    private TerminalType TerminalType;

    @Column(name = "AppPAN")
    private String AppPAN;

     @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "ifx_type"))
    })
    private IfxType ifxType;

    private Boolean request;


    @Column(name = "received_dt")
     private Long receivedDtLong;


    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "trntype"))
    })
    private TrnType trnType = TrnType.UNKNOWN; // Note e.g: Purchase; Cash Withdraw

    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "state", column = @Column(name = "clr_state"))})
    private ClearingState clearingState = ClearingState.NOT_CLEARED;

    @Column(nullable = true)
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "clr_date")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "clr_time"))})
    private DateTime clearingDate;

      @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "direction"))
    })
    private IfxDirection ifxDirection;


    @Override
    public Long getId() {
        return id;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setId(Long id) {
        //To change body of implemented methods use File | Settings | File Templates.
        this.id = id;
    }

    public String getAppPAN() {
        return AppPAN;
    }

    public void setAppPAN(String appPAN) {
        AppPAN = appPAN;
    }

    public Long getTrxID() {
        return trxID;
    }

    public void setTrxID(Long trxID) {
        this.trxID = trxID;
    }

    public Long getTrx_status() {
        return trx_status;
    }

    public void setTrx_status(Long trx_status) {
        this.trx_status = trx_status;
    }

    public Long getAuth_amt() {
        return auth_amt;
    }

    public void setAuth_amt(Long auth_amt) {
        this.auth_amt = auth_amt;
    }

    public Long getBankid() {
        return bankid;
    }

    public void setBankid(Long bankid) {
        this.bankid = bankid;
    }

    public Long getDestbankid() {
        return destbankid;
    }

    public void setDestbankid(Long destbankid) {
        this.destbankid = destbankid;
    }

    public String getMy_TrnSeqCntr() {
        return My_TrnSeqCntr;
    }

    public void setMy_TrnSeqCntr(String my_TrnSeqCntr) {
        My_TrnSeqCntr = my_TrnSeqCntr;
    }

    public TerminalType getTerminalType() {
        return TerminalType;
    }

    public void setTerminalType(TerminalType terminalType) {
        TerminalType = terminalType;
    }

    public IfxType getIfxType() {
        return ifxType;
    }

    public void setIfxType(IfxType ifxType) {
        this.ifxType = ifxType;
    }

    public Boolean getRequest() {
        return request;
    }

    public void setRequest(Boolean request) {
        this.request = request;
    }

    public Long getReceivedDtLong() {
        return receivedDtLong;
    }

    public void setReceivedDtLong(Long receivedDtLong) {
        this.receivedDtLong = receivedDtLong;
    }

    public TrnType getTrnType() {
        return trnType;
    }

    public void setTrnType(TrnType trnType) {
        this.trnType = trnType;
    }

    public ClearingState getClearingState() {
        return clearingState;
    }

    public void setClearingState(ClearingState clearingState) {
        this.clearingState = clearingState;
    }

    public DateTime getClearingDate() {
        return clearingDate;
    }

    public void setClearingDate(DateTime clearingDate) {
        this.clearingDate = clearingDate;
    }

    public IfxDirection getIfxDirection() {
        return ifxDirection;
    }

    public void setIfxDirection(IfxDirection ifxDirection) {
        this.ifxDirection = ifxDirection;
    }
}
