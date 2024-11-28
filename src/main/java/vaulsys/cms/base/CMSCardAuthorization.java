package vaulsys.cms.base;

import vaulsys.persistence.IEntity;

import javax.persistence.*;

/**
 * Created by HP on 4/21/2017.
 */
@Entity
@Table(name="cms_cardauth")
public class CMSCardAuthorization implements IEntity<Integer> {
    @Id
    @GeneratedValue(generator="CMS_CARD_AUTH_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "CMS_CARD_AUTH_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "CMS_CARD_AUTH_ID_SEQ")
            })
    Integer id;

    @Column(name="encrypted_pin")
    String encryptedPin;

    @Column(name="remaining_retries")
    String remainingRetries;

    @Column(name="status")
    String status;

    @Column(name="reason_code")
    String reasonCode;

//    @Column(
//            name="auth_id",
//            nullable=false,
//            unique=true,
//            insertable = false,
//            updatable = false,
//            columnDefinition = "BIGINT DEFAULT nextval('CMS_RELAUTH_ID_SEQ')"
//    )
//    @Generated(GenerationTime.INSERT)
//    Long authId;

    @Column(name="maximum_retries")
    String maximumRetries;

    public CMSCardAuthorization()
    {}

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getEncryptedPin() {
        return encryptedPin;
    }

    public void setEncryptedPin(String encryptedPin) {
        this.encryptedPin = encryptedPin;
    }

    public String getRemainingRetries() {
        return remainingRetries;
    }

    public void setRemainingRetries(String remainingRetries) {
        this.remainingRetries = remainingRetries;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

//    public Long getAuthId() {
//        return authId;
//    }
//
//    public void setAuthId(Long authId) {
//        this.authId = authId;
//    }

    public String getMaximumRetries() {
        return maximumRetries;
    }

    public void setMaximumRetries(String maximumRetries) {
        this.maximumRetries = maximumRetries;
    }
}
