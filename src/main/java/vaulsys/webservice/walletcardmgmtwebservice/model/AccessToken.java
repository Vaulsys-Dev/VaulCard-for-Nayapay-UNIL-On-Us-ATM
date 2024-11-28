package vaulsys.webservice.walletcardmgmtwebservice.model;

import vaulsys.cms.base.CMSCustomer;
import vaulsys.persistence.IEntity;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Raza on 27-Nov-18.
 */
@Entity
@Table(name = "ACCESSTOKEN")
public class AccessToken implements IEntity<Long>, Cloneable {

    @Id
    @GeneratedValue(generator="ACCESSTOKEN_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "ACCESSTOKEN_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "ACCESSTOKEN_SEQ")
            })
    private Long id;

    @Column(name = "TOKEN_ID")
    private String tokenId;

    @Column(name = "USER_ID")
    private String userid;

    @Column(name = "CREATE_DATE_TIME")
    private Long createDate;    //private Date createDate; Raza will update This

    @Column(name = "EXPIRE_DATETIME")
    private Long expireDate;   //private Date expireDate; Raza will update This

    @Column(name = "IS_EXPIRED")
    private Boolean isExpired;   //private Date expireDate; Raza will update This

    @Column(name = "MANUAL_EXP_DATETIME")
    private Long manualexpiredate;   //private Date expireDate; Raza will update This

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="ACCESSTOKEN_CUST_FK")
    private CMSCustomer customer;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public Long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Long createDate) {
        this.createDate = createDate;
    }

    public Long getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Long expireDate) {
        this.expireDate = expireDate;
    }

    public CMSCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(CMSCustomer customer) {
        this.customer = customer;
    }

    public Boolean getExpired() {
        return isExpired;
    }

    public void setExpired(Boolean expired) {
        isExpired = expired;
    }

    public Long getManualexpiredate() {
        return manualexpiredate;
    }

    public void setManualexpiredate(Long manualexpiredate) {
        this.manualexpiredate = manualexpiredate;
    }
}
