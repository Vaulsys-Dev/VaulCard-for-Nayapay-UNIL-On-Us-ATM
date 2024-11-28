package vaulsys.cms.base;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;
import vaulsys.persistence.IEntity;

import javax.persistence.*;

/**
 * Created by Asim Shahzad on 13/01/2021.
 * Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 1)
 */

@Entity
@Table(name="CMS_CARD_CONTROL_CONFIG")
public class CMSCardControlConfig implements IEntity<Long> {
    @Id
    @GeneratedValue(generator="CMS_CARD_CTRL_CONFIG_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "CMS_CARD_CTRL_CONFIG_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "CMS_CARD_CTRL_CONFIG_ID_SEQ")
            })
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CMS_CARD", insertable = true, updatable = true)
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="CMS_CARD_CONTROL_FK1")
    private CMSCard card;

    @Column(name="IS_CHIP_PIN_ENABLED")
    private Boolean isChipPinEnabled;

    @Column(name="IS_MAG_STRIPE_ENABLED")
    private Boolean isMagStripeEnabled;

    @Column(name="IS_CASH_WITHDRAWAL_ENABLED")
    private Boolean isCashWithdrawalEnabled;

    @Column(name="IS_NFC_ENABLED")
    private Boolean isNFCEnabled;

    @Column(name="IS_ONLINE_ENABLED")
    private Boolean isOnlineEnabled;

    @Column(name="IS_INT_TXNS_ENABLED")
    private Boolean isInternationalTxnsEnabled;

    public CMSCardControlConfig()
    {}

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public CMSCard getCard() {
        return card;
    }

    public void setCard(CMSCard card) {
        this.card = card;
    }

    public Boolean getChipPinEnabled() {
        return isChipPinEnabled;
    }

    public void setChipPinEnabled(Boolean chipPinEnabled) {
        isChipPinEnabled = chipPinEnabled;
    }

    public Boolean getMagStripeEnabled() {
        return isMagStripeEnabled;
    }

    public void setMagStripeEnabled(Boolean magStripeEnabled) {
        isMagStripeEnabled = magStripeEnabled;
    }

    public Boolean getCashWithdrawalEnabled() {
        return isCashWithdrawalEnabled;
    }

    public void setCashWithdrawalEnabled(Boolean cashWithdrawalEnabled) {
        isCashWithdrawalEnabled = cashWithdrawalEnabled;
    }

    public Boolean getNFCEnabled() {
        return isNFCEnabled;
    }

    public void setNFCEnabled(Boolean NFCEnabled) {
        isNFCEnabled = NFCEnabled;
    }

    public Boolean getOnlineEnabled() {
        return isOnlineEnabled;
    }

    public void setOnlineEnabled(Boolean onlineEnabled) {
        isOnlineEnabled = onlineEnabled;
    }

    public Boolean getInternationalTxnsEnabled() {
        return isInternationalTxnsEnabled;
    }

    public void setInternationalTxnsEnabled(Boolean internationalTxnsEnabled) {
        isInternationalTxnsEnabled = internationalTxnsEnabled;
    }
}
