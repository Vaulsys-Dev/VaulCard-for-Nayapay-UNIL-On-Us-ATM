package vaulsys.cms.base;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * Created by HP on 5/8/2017.
 */
@Entity
@Table(name="cms_productdetail")
public class CMSProductDetail {

    @Id
    @Column(name="productdetail_id")
    private String productDetailId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="trackformat_id")
    private CMSTrack2Format trackFormatId;

    @Column(name="cardmachine_id")
    private String cardMachineId;

    //m.rehman: 06-08-2020, Euronet Integration, PAN generation according to pan format defined for scheme
    //@Column(name="panformat_id")
    //private String panFormatId;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="panformat_id")
    private CMSPANFormat panFormatId;

    @Column(name="printformat_id")
    private String printFormatId;

    @Column(name="embossingformat_id")
    private String embossingFormatId;

    @Column(name="track1_req")
    private String track1Required;

    @Column(name="track2_req")
    private String track2Required;

    @Column(name="track3_req")
    private String track3Required;

    @Column(name="recardable")
    private String reCardable;

    @Column(name="product_life")
    private String productLife;

    @Column(name="cvvrequired")
    private String cvvRequired;

    @Column(name="cvv2required")
    private String cvv2Required;

    @Column(name="isdefault_adc")
    private String isDefaultADC;

    @Column(name="validyears_renewal")
    private String validYearsRenewal;

    @Column(name="regenerate_pan")
    private String regeneratePAN;

    @Column(name="deactivate_on_renew")
    private String deactivateOnRenew;

    @Column(name="max_supplementary_allowed")
    private String maxSupplementaryAllowed;

    @Column(name="maxcardpercustomer")
    private String maxCardPerCustomer;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PANRANGE_ID", insertable = true, updatable = true)
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="CMS_PRODUCTDETAIL_CMS_PRO_FK1")
    private CMSPANRange panRange;

    @Column(name = "QR_ACCOUNT_TYPE")
    private String qrAccountType;

    @Column(name = "SERVICE_CODE")
    private String serviceCode;

    @Column(name = "PREP_PROD_AMOUNT")
    private String prepProdAmount;

    //@OneToOne(fetch = FetchType.LAZY)
    //@PrimaryKeyJoinColumn
    //@Column(name = "product_id")
    //private CMSProduct productId;

    @Column(name = "RENEWAL_DAYS")
    private String renewalDays;

    public String getProductDetailId() {
        return productDetailId;
    }

    public void setProductDetailId(String productDetailId) {
        this.productDetailId = productDetailId;
    }

    public CMSTrack2Format getTrackFormatId() {
        return trackFormatId;
    }

    public void setTrackFormatId(CMSTrack2Format trackFormatId) {
        this.trackFormatId = trackFormatId;
    }

    public String getCardMachineId() {
        return cardMachineId;
    }

    public void setCardMachineId(String cardMachineId) {
        this.cardMachineId = cardMachineId;
    }

	//m.rehman: Euronet Integration
    public CMSPANFormat getPanFormatId() {
        return panFormatId;
    }

    public void setPanFormatId(CMSPANFormat panFormatId) {
        this.panFormatId = panFormatId;
    }

    public String getPrintFormatId() {
        return printFormatId;
    }

    public void setPrintFormatId(String printFormatId) {
        this.printFormatId = printFormatId;
    }

    public String getEmbossingFormatId() {
        return embossingFormatId;
    }

    public void setEmbossingFormatId(String embossingFormatId) {
        this.embossingFormatId = embossingFormatId;
    }

    public String getTrack1Required() {
        return track1Required;
    }

    public void setTrack1Required(String track1Required) {
        this.track1Required = track1Required;
    }

    public String getTrack2Required() {
        return track2Required;
    }

    public void setTrack2Required(String track2Required) {
        this.track2Required = track2Required;
    }

    public String getTrack3Required() {
        return track3Required;
    }

    public void setTrack3Required(String track3Required) {
        this.track3Required = track3Required;
    }

    public String getReCardable() {
        return reCardable;
    }

    public void setReCardable(String reCardable) {
        this.reCardable = reCardable;
    }

    public String getProductLife() {
        return productLife;
    }

    public void setProductLife(String productLife) {
        this.productLife = productLife;
    }

    public String getCvvRequired() {
        return cvvRequired;
    }

    public void setCvvRequired(String cvvRequired) {
        this.cvvRequired = cvvRequired;
    }

    public String getCvv2Required() {
        return cvv2Required;
    }

    public void setCvv2Required(String cvv2Required) {
        this.cvv2Required = cvv2Required;
    }

    public String getIsDefaultADC() {
        return isDefaultADC;
    }

    public void setIsDefaultADC(String isDefaultADC) {
        this.isDefaultADC = isDefaultADC;
    }

    public String getValidYearsRenewal() {
        return validYearsRenewal;
    }

    public void setValidYearsRenewal(String validYearsRenewal) {
        this.validYearsRenewal = validYearsRenewal;
    }

    public String getRegeneratePAN() {
        return regeneratePAN;
    }

    public void setRegeneratePAN(String regeneratePAN) {
        this.regeneratePAN = regeneratePAN;
    }

    public String getDeactivateOnRenew() {
        return deactivateOnRenew;
    }

    public void setDeactivateOnRenew(String deactivateOnRenew) {
        this.deactivateOnRenew = deactivateOnRenew;
    }

    public String getMaxSupplementaryAllowed() {
        return maxSupplementaryAllowed;
    }

    public void setMaxSupplementaryAllowed(String maxSupplementaryAllowed) {
        this.maxSupplementaryAllowed = maxSupplementaryAllowed;
    }

    public String getMaxCardPerCustomer() {
        return maxCardPerCustomer;
    }

    public void setMaxCardPerCustomer(String maxCardPerCustomer) {
        this.maxCardPerCustomer = maxCardPerCustomer;
    }

    public CMSPANRange getPanRange() {
        return panRange;
    }

    public void setPanRange(CMSPANRange panRange) {
        this.panRange = panRange;
    }

    public String getQrAccountType() {
        return qrAccountType;
    }

    public void setQrAccountType(String qrAccountType) {
        this.qrAccountType = qrAccountType;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getPrepProdAmount() {
        return prepProdAmount;
    }

    public void setPrepProdAmount(String prepProdAmount) {
        this.prepProdAmount = prepProdAmount;
    }

    public String getRenewalDays() {
        return renewalDays;
    }

    public void setRenewalDays(String renewalDays) {
        this.renewalDays = renewalDays;
    }

    /*public CMSProduct getProductId() {
        return productId;
    }

    public void setProductId(CMSProduct productId) {
        this.productId = productId;
    }*/
}
