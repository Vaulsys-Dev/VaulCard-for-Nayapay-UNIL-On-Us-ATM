package vaulsys.wallet.base;

import vaulsys.cms.base.CMSProduct;
import vaulsys.persistence.IEntity;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.List;

/**
 * Created by HP on 4/21/2017.
 */
@Entity
@Table(name="cms_card")
public class WalletCard implements IEntity<Long> {
    @Id
    private Long id;

    @Column(name="card_number")
    private String cardNumber;

    @Column(name="card_name")
    private String cardName;

    @Column(name="import_date")
    private String importDate;

    @Column(name="create_date")
    private String createDate;

    @Column(name="activation_date")
    private String activationDate;

    @Column(name="expiry_date")
    private String expiryDate;

    @Column(name="delivery_date")
    private String deliveryDate;

    @Column(name="cardnumber_expiry_relation")
    private String cardNumberExpiryRelation;

    @Column(name="is_supplementary")
    private boolean isSupplementary;

    @Column(name="card_status")
    private String cardStatus;

    @Column(name="branch")
    String branch;
    //Branch branch; //Raza commenting not using Branch Object

    @Column(name="primary_card_number")
    private String primaryCardNumber;

    @Column(name="last_card_number")
    private String lastCardNumber;

    @Column(name="CUSTOMER_ID")
    private String customerID;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID", insertable = true, updatable = true)
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="CMS_CARD_CMS_PRODUCT_FK1")
    private CMSProduct product;

    @Transient
    private List<CMSProduct> list_CustProducts;

    @Transient
    private List<WalletAccount> list_CustAccounts;

    public WalletCard()
    {}

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getImportDate() {
        return importDate;
    }

    public void setImportDate(String importDate) {
        this.importDate = importDate;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(String activationDate) {
        this.activationDate = activationDate;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getCardNumberExpiryRelation() {
        return cardNumberExpiryRelation;
    }

    public void setCardNumberExpiryRelation(String cardNumberExpiryRelation) {
        this.cardNumberExpiryRelation = cardNumberExpiryRelation;
    }

    public boolean getSupplementary() {
        return isSupplementary;
    }

    public void setSupplementary(boolean supplementary) {
        isSupplementary = supplementary;
    }

    public String getCardStatus() {
        return cardStatus;
    }

    public void setCardStatus(String cardStatus) {
        this.cardStatus = cardStatus;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getPrimaryCardNumber() {
        return primaryCardNumber;
    }

    public void setPrimaryCardNumber(String primaryCardNumber) {
        this.primaryCardNumber = primaryCardNumber;
    }

    public String getLastCardNumber() {
        return lastCardNumber;
    }

    public void setLastCardNumber(String lastCardNumber) {
        this.lastCardNumber = lastCardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public CMSProduct getProduct() {
        return product;
    }

    public void setProduct(CMSProduct product) {
        this.product = product;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public List<WalletAccount> getList_CustAccounts() {
        return list_CustAccounts;
    }

    public void setList_CustAccounts(List<WalletAccount> list_CustAccounts) {
        this.list_CustAccounts = list_CustAccounts;
    }

    public List<CMSProduct> getList_CustProducts() {
        return list_CustProducts;
    }

    public void setList_CustProducts(List<CMSProduct> list_CustProducts) {
        this.list_CustProducts = list_CustProducts;
    }
}
