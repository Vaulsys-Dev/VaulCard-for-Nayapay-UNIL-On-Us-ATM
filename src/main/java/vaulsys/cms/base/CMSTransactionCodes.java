package vaulsys.cms.base;

import vaulsys.persistence.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Raza on 11-Sep-18.
 */
@Entity
@Table(name = "transactioncodes")
public class CMSTransactionCodes extends BaseEntity<Long> {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "TXN_CODE")
    private String trancode;

    @Column(name = "TXN_NAME")
    private String txnname;

    @Column(name = "TXN_DESC")
    private String txndesc;

    @Column(name = "IS_FINANCIAL")
    private Boolean isFinancial;

    @Column(name = "IS_ENABLED")
    private Boolean isEnabled;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getTrancode() {
        return trancode;
    }

    public void setTrancode(String trancode) {
        this.trancode = trancode;
    }

    public String getTxnname() {
        return txnname;
    }

    public void setTxnname(String txnname) {
        this.txnname = txnname;
    }

    public String getTxndesc() {
        return txndesc;
    }

    public void setTxndesc(String txndesc) {
        this.txndesc = txndesc;
    }

    public Boolean getIsFinancial() {
        return isFinancial;
    }

    public void setIsFinancial(Boolean isFinancial) {
        this.isFinancial = isFinancial;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
