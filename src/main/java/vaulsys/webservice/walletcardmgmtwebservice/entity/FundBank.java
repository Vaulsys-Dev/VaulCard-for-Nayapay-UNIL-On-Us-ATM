package vaulsys.webservice.walletcardmgmtwebservice.entity;

import vaulsys.persistence.IEntity;

import javax.persistence.*;

/**
 * Created by HP on 3/13/2019.
 */

@Entity
@Table(name = "fund_bank")
public class FundBank implements IEntity<Long> {
    @Id
    @GeneratedValue(generator="FUND_BANK_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "FUND_BANK_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "FUND_BANK_ID_SEQ")
            })
    private Long id;

    @Column(name = "BANK")
    private String bankName;

    @Column(name = "SETTLEMENT_TYPE")
    private String settlementType;

    @Column(name = "SETTLEMENT_PERIOD")
    private String settlementPeriod;

    @Column(name = "BANK_CODE")
    private String bankCode;

    @Column(name = "BANK_ACRONYM")
    private String bankAcro;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(String settlementType) {
        this.settlementType = settlementType;
    }

    public String getSettlementPeriod() {
        return settlementPeriod;
    }

    public void setSettlementPeriod(String settlementPeriod) {
        this.settlementPeriod = settlementPeriod;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankAcro() {
        return bankAcro;
    }

    public void setBankAcro(String bankAcro) {
        this.bankAcro = bankAcro;
    }

    public String toString() {
        return bankName;
    }
}
