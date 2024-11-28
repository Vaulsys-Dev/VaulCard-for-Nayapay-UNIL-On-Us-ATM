package vaulsys.cms.base;

import vaulsys.persistence.BaseEntity;

import javax.persistence.*;

/**
 * Created by HP on 4/27/2017.
 */

// Author: Asim Shahzad, Date: 20th Dec 2017, Desc: Added this class for enabling the PAN range for card number generation
@Entity
@Table(name = "CMS_PRODUCT_PANRANGE")
@PrimaryKeyJoinColumn(name = "ID")
public class CMSPANRange extends BaseEntity<Long> {
    @Id
    @Column(name="ID")
    private Long id;

    @Column(name="PRODUCT_ID")
    private String productID;

    @Column(name="LOW_RANGE")
    private String lowRange;

    @Column(name="HIGH_RANGE")
    private String highRange;

    @Column(name="STATUS")
    private boolean status;

    @Column(name="LAST_VALUE")
    private String lastValue;

    public CMSPANRange()
    {}

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        id = id;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getLowRange() {
        return lowRange;
    }

    public void setLowRange(String lowRange) {
        this.lowRange = lowRange;
    }

    public String getHighRange() {
        return highRange;
    }

    public void setHighRange(String highRange) {
        this.highRange = highRange;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getLastValue() {
        return lastValue;
    }

    public void setLastValue(String lastValue) {
        this.lastValue = lastValue;
    }
}
