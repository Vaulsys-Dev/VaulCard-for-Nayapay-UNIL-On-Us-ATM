package vaulsys.webservice.walletcardmgmtwebservice.entity;


import vaulsys.persistence.IEntity;
import org.apache.log4j.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TRANSACTIONCODES")
public class SwitchTransactionCodes implements IEntity<Long>, Cloneable {


    private static final Logger logger = Logger.getLogger(SwitchTransactionCodes.class);

    @Id
    private Long id;

    @Column(name = "TXN_CODE")
    private String txncode;

    @Column(name = "TXN_DESC")
    private String txndesc;

    @Column(name = "IS_FINANCIAL")
    private Boolean isfinancial;

    @Column(name = "IS_ENABLED")
    private Boolean isenabled;

    @Column(name = "SERVICE_NAME")
    private String servicename;

    //m.rehman: 09-09-2021 - VP-NAP-202109091 - Non financial Transactions on VaulGuard
    @Column(name = "IS_BYPASS")
    private Boolean isBypass;
    ////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getTxncode() {
        return txncode;
    }

    public void setTxncode(String txncode) {
        this.txncode = txncode;
    }

    public String getTxndesc() {
        return txndesc;
    }

    public void setTxndesc(String txndesc) {
        this.txndesc = txndesc;
    }

    public Boolean getIsfinancial() {
        return isfinancial;
    }

    public void setIsfinancial(Boolean isfinancial) {
        this.isfinancial = isfinancial;
    }

    public Boolean getIsenabled() {
        return isenabled;
    }

    public void setIsenabled(Boolean isenabled) {
        this.isenabled = isenabled;
    }

    public String getServicename() {
        return servicename;
    }

    public void setServicename(String servicename) {
        this.servicename = servicename;
    }

    //m.rehman: 09-09-2021 - VP-NAP-202109091 - Non financial Transactions on VaulGuard
    public Boolean getIsBypass() {
        return isBypass;
    }

    public void setIsBypass(Boolean isBypass) {
        this.isBypass = isBypass;
    }
    /////////////////////////////////////////////////////////////////////////////////
}
