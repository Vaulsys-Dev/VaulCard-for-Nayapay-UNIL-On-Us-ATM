package vaulsys.cms.base;

import vaulsys.persistence.BaseEntity;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by HP on 4/27/2017.
 */
@Entity
@Table(name = "CMS_PRODUCTCHANNELS")
@PrimaryKeyJoinColumn(name = "ID")
public class CMSProductChannels extends BaseEntity<Long> {
    @Id
    @GeneratedValue(generator="PRODUCT_CHANNEL_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "PRODUCT_CHANNEL_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "PRODUCT_CHANNEL_SEQ")
            })
    @Column(name = "ID")
    private Long id;

    @Column(name = "PRODUCT_ID")
    private String productID;

    @Column(name = "CHANNEL_ID")
    private String channelID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTIONCODES")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="ifx_emvrqdata_fk")
    private CMSTransactionCodes transactioncode;

    public CMSProductChannels()
    {}

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long s) {
        id = s;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(String channelID) {
        this.channelID = channelID;
    }

    public CMSTransactionCodes getTransactioncode() {
        return transactioncode;
    }

    public void setTransactioncode(CMSTransactionCodes transactioncode) {
        this.transactioncode = transactioncode;
    }
}

