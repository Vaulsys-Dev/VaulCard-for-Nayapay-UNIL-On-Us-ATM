package vaulsys.cms.base;

import vaulsys.protocols.PaymentSchemes.EMV.BERTLV;
import vaulsys.protocols.PaymentSchemes.EMV.EMVTags;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.Util;
import vaulsys.webservice.walletcardmgmtwebservice.entity.WalletCMSWsEntity;
import org.apache.log4j.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by HP on 5/3/2017.
 */
@Entity
@Table(name="cms_track2format")
public class CMSTrack2Format {

    private static Logger logger = Logger.getLogger(CMSTrack2Format.class);

    @Id
    @Column(name="track_id")
    private String trackId;

    @Column(name="start_sentinal")
    private String startSentinal;

    @Column(name="format_code")
    private String formatCode;

    @Column(name="pan_req")
    private String panReq;

    @Column(name="imd_req")
    private String imdReq;

    @Column(name="branch_code_req")
    private String branchCodeReq;

    @Column(name="account_no_req")
    private String accountNoReq;

    @Column(name="card_no_req")
    private String cardNoReq;

    @Column(name="country_code_req")
    private String countryCodeReq;

    @Column(name="country_code")
    private String countryCode;

    @Column(name="service_code_req")
    private String serviceCodeReq;

    @Column(name="service_code")
    private String serviceCode;

    @Column(name="card_name_req")
    private String cardNameReq;

    @Column(name="expiry_date_req")
    private String expiryDateReq;

    @Column(name="end_sentinal")
    private String endSentinal;

    @Column(name="pvv_req")
    private Boolean pvvReq;

    @Column(name="pvv")
    private String pvv;

    @Column(name="cvv_req")
    private Boolean cvvReq;

    @Column(name="cvv")
    private String cvv;

    @Column(name="cvv1_req")
    private Boolean cvv1Req;

    @Column(name="cvv1")
    private String cvv1;

    @Column(name="field_name")
    private String fieldName;

    @Column(name="field_description")
    private String fieldDescription;

    @Column(name="disc_data")
    private String discretionaryData;

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getStartSentinal() {
        return startSentinal;
    }

    public void setStartSentinal(String startSentinal) {
        this.startSentinal = startSentinal;
    }

    public String getFormatCode() {
        return formatCode;
    }

    public void setFormatCode(String formatCode) {
        this.formatCode = formatCode;
    }

    public String getPanReq() {
        return panReq;
    }

    public void setPanReq(String panReq) {
        this.panReq = panReq;
    }

    public String getImdReq() {
        return imdReq;
    }

    public void setImdReq(String imdReq) {
        this.imdReq = imdReq;
    }

    public String getBranchCodeReq() {
        return branchCodeReq;
    }

    public void setBranchCodeReq(String branchCodeReq) {
        this.branchCodeReq = branchCodeReq;
    }

    public String getAccountNoReq() {
        return accountNoReq;
    }

    public void setAccountNoReq(String accountNoReq) {
        this.accountNoReq = accountNoReq;
    }

    public String getCardNoReq() {
        return cardNoReq;
    }

    public void setCardNoReq(String cardNoReq) {
        this.cardNoReq = cardNoReq;
    }

    public String getCountryCodeReq() {
        return countryCodeReq;
    }

    public void setCountryCodeReq(String countryCodeReq) {
        this.countryCodeReq = countryCodeReq;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getServiceCodeReq() {
        return serviceCodeReq;
    }

    public void setServiceCodeReq(String serviceCodeReq) {
        this.serviceCodeReq = serviceCodeReq;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getCardNameReq() {
        return cardNameReq;
    }

    public void setCardNameReq(String cardNameReq) {
        this.cardNameReq = cardNameReq;
    }

    public String getExpiryDateReq() {
        return expiryDateReq;
    }

    public void setExpiryDateReq(String expiryDateReq) {
        this.expiryDateReq = expiryDateReq;
    }

    public String getEndSentinal() {
        return endSentinal;
    }

    public void setEndSentinal(String endSentinal) {
        this.endSentinal = endSentinal;
    }

    public Boolean getPvvReq() {
        return pvvReq;
    }

    public void setPvvReq(Boolean pvvReq) {
        this.pvvReq = pvvReq;
    }

    public String getPvv() {
        return pvv;
    }

    public void setPvv(String pvv) {
        this.pvv = pvv;
    }

    public Boolean getCvvReq() {
        return cvvReq;
    }

    public void setCvvReq(Boolean cvvReq) {
        this.cvvReq = cvvReq;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public Boolean getCvv1Req() {
        return cvv1Req;
    }

    public void setCvv1Req(Boolean cvv1Req) {
        this.cvv1Req = cvv1Req;
    }

    public String getCvv1() {
        return cvv1;
    }

    public void setCvv1(String cvv1) {
        this.cvv1 = cvv1;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldDescription() {
        return fieldDescription;
    }

    public void setFieldDescription(String fieldDescription) {
        this.fieldDescription = fieldDescription;
    }

    public String getDiscretionaryData() {
        return discretionaryData;
    }

    public void setDiscretionaryData(String discretionaryData) {
        this.discretionaryData = discretionaryData;
    }

    public void getTrack2InfoFromCard(Ifx ifx) throws Exception {
        String serviceCode, cvv, track2Data;
        Integer index, cvvIndex;

        track2Data = ifx.getTrk2EquivData();

        if (!Util.hasText(track2Data)) {
            logger.error("Track 2 Data not available. Unable to proceed");
            throw new Exception("CVV check possible but Track 2 Data not present. Error!!!");
        }

        index = track2Data.indexOf("=");
        if (index <= 0)
            index = track2Data.indexOf("D");

        //service code => separator + expiry (4) + 3
        serviceCode = track2Data.substring(index + 5, index + 8);
        ifx.getSafeEMVRqData().getSafeCardAcctId().setServiceCode(serviceCode);
        index += 8;
        cvvIndex = getDiscretionaryData().indexOf("CVV");

        if (getPvvReq().equals(Boolean.TRUE)) {
            logger.info("PVV Found");
            //if pvv available, move 5 index ahead to find cvv
            index += 5;
        }

        if (getCvvReq()) {
            cvv = track2Data.substring(index + cvvIndex, index + 3);
            ifx.getSafeEMVRqData().getSafeCardAcctId().setCVV(cvv);

        } else {
            logger.error("CVV Required false on Product. Unable to proceed");
            throw new Exception("CVV check possible but CVV Required false on Product. Error!!!");
        }
    }

    public void getTrack2InfoFromCard(WalletCMSWsEntity wsmodel) throws Exception {
        String serviceCode, cvv, track2Data;
        Integer index, cvvIndex;

        track2Data = wsmodel.getTrack2Data();
        if (!Util.hasText(track2Data) && Util.hasText(wsmodel.getIcccarddata()))
            track2Data = BERTLV.findTLVTagValue(wsmodel.getIcccarddata(), EMVTags.TRK2_EQUIV_DATA);

        if (!Util.hasText(track2Data)) {
            logger.error("Track 2 Data not available. Unable to proceed");
            throw new Exception("CVV check possible but Track 2 Data not present. Error!!!");
        }

        index = track2Data.indexOf("=");
        if (index <= 0)
            index = track2Data.indexOf("D");

        //service code => separator + expiry (4) + 3
        serviceCode = track2Data.substring(index + 5, index + 8);
        wsmodel.setServicecode(serviceCode);
        index += 8;
        cvvIndex = getDiscretionaryData().indexOf("CVV");

        if (getPvvReq().equals(Boolean.TRUE)) {
            logger.info("PVV Found");
            //if pvv available, move 5 index ahead to find cvv
            index += 5;
        }

        if (getCvvReq()) {
            cvv = track2Data.substring(index + cvvIndex, index + 3);
            wsmodel.setCvv(cvv);

        } else {
            logger.error("CVV Required false on Product. Unable to proceed");
            throw new Exception("CVV check possible but CVV Required false on Product. Error!!!");
        }
    }
}
