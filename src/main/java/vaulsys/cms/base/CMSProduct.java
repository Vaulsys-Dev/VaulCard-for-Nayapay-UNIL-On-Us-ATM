package vaulsys.cms.base;

import javax.persistence.*;
import java.util.List;

/**
 * Created by HP on 4/21/2017.
 */
@Entity
@Table(name="cms_product")
public class CMSProduct {

    @Id
    private Long id;

    @Column(name="product_id")
    private String productId;

    @Column(name="product_name")
    private String productName;

    @Column(name="product_decs")
    private String productDescription;

    @Column(name="product_type")
    private String productType;

    @Column(name="status")
    private String status;

    @Column(name="is_supplementry")
    private String isSupplementry;

    @Column(name="bin")
    private String bin;

    @Column(name="product_nature")
    private String productNature;

    @Column(name="parent_product_id")
    private String parentProductId;

    @Column(name="productchannels_key") //Raza updating for NayaPay
    private String productChannelsKey;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="productdetail_id")
    private CMSProductDetail productDetail;

    private String isdefault; //Raza adding for NayaPay Fraud

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "cmsProductId")
    private List<CMSProductKeys> productKeys;

    //m.rehman: for account balance limit
    @Column(name = "MAX_BALANCE_LIMIT")
    private String maxBalanceLimit;

    @Column(name = "RETRIES_COUNT")
    private Integer retriesCount;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIsSupplementry() {
        return isSupplementry;
    }

    public void setIsSupplementry(String isSupplementry) {
        this.isSupplementry = isSupplementry;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getProductNature() {
        return productNature;
    }

    public void setProductNature(String productNature) {
        this.productNature = productNature;
    }

    public String getParentProductId() {
        return parentProductId;
    }

    public void setParentProductId(String parentProductId) {
        this.parentProductId = parentProductId;
    }

    public String getProductChannelsKey() {
        return productChannelsKey;
    }

    public void setProductChannelsKey(String productChannelsKey) {
        this.productChannelsKey = productChannelsKey;
    }

    public CMSProductDetail getProductDetail() {
        return productDetail;
    }

    public void setProductDetail(CMSProductDetail productDetailId) {
        this.productDetail = productDetailId;
    }

    public List<CMSProductKeys> getProductKeys() {
        return productKeys;
    }

    public void setProductKeys(List<CMSProductKeys> productKeys) {
        this.productKeys = productKeys;
    }

    public String isdefault() {
        return isdefault;
    }

    public void setIsdefault(String isdefault) {
        this.isdefault = isdefault;
    }

    public String getMaxBalanceLimit() {
        return maxBalanceLimit;
    }

    public void setMaxBalanceLimit(String maxBalanceLimit) {
        this.maxBalanceLimit = maxBalanceLimit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRetriesCount() {
        return retriesCount;
    }

    public void setRetriesCount(Integer retriesCount) {
        this.retriesCount = retriesCount;
    }

    /*public String getProductDetailId() {
        return productDetailId;
    }

    public void setProductDetailId(String productDetailId) {
        this.productDetailId = productDetailId;
    }*/
}
