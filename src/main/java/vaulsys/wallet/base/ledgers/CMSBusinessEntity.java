package vaulsys.wallet.base.ledgers;

import vaulsys.persistence.BaseEntity;
import vaulsys.protocols.ifx.enums.AccType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Raza on 24-Apr-17.
 */
@Entity
@Table(name = "CMS_BUSINESS_ENTITY")
@PrimaryKeyJoinColumn(name = "ID")
public class CMSBusinessEntity extends BaseEntity<Long> { //Raza This Class should be ReadOnly, Not removing public Setter as this class is also used by UI

    @Id
    @GeneratedValue(generator="CMS_BUSINESS_ENTITY_ID_SEQ-gen")
    @GenericGenerator(name = "CMS_BUSINESS_ENTITY_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "optimizer", value = "pooled"),
                    @Parameter(name = "increment_size", value = "1"),
                    @Parameter(name = "sequence_name", value = "CMS_BUSINESS_ENTITY_ID_SEQ")
            })
    private Long id;

    @Column(name = "CUSTOMER_ID")
    private String CustomerId;

    @Column(name = "STATUS")
    private String Status;

    @Column(name = "FIRSTNAME")
    private String firstname;

    @Column(name = "MIDNAME")
    private String midname;

    @Column(name = "LASTNAME")
    private String lastname;

    @Column(name = "CNIC")
    private String cnic;

    @Column(name = "IDENTIFICATIONTYPE")
    private String identificationtype;

    @Column(name = "MOBILENUMBER")
    private String MobileNumber;

    @Column(name = "HOMENUMBER")
    private String HomeNumber;

    @Column(name = "OFFFICENUMBER")
    private String OfficeNumber;

    @Column(name = "FATHERNAME")
    private String FatherName;

    @Column(name = "MOTHERNAME")
    private String MotherName;

    @Column(name = "EMAILID")
    private String EmailId;

    @Column(name = "ALTERNATEEMAILID")
    private String alternateEmailId;

    @Column(name = "COMPANY")
    private String company;

    @Column(name = "CUSTOMERTYPE")
    private String CustomerType;

    @Column(name = "KYC_STATUS")
    private String KycStatus;

    @Column(name = "CITY")
    private String City;

    @Column(name = "COUNTRY")
    private String Country;

    @Column(name = "HOMEADDRESS")
    private String HomeAddress;

    @Column(name = "OFFICEADDRESS")
    private String OfficeAddress;

    @Column(name = "OCCUPATION")
    private String Occupation;

    @Column(name = "ACTIVATIONDATE")
    private Date ActivationDate;

    @Column(name = "LASTUPDATEDATE")
    private Date LastUpdateDate;

    @Column(name = "CREATEDATE")
    private Date CreateDate;

    @Column(name = "BRANCHCODE")
    private String BranchCode;

    @Column(name = "DATEOFBIRTH")
    private Date DateofBirth;

    @Column(name = "PROVINCE")
    private String Province;

    @Column(name = "GENDER")
    private String Gender;

    //Raza adding for NayaPay start
    @Column(name = "CNICPICTUREFRONT")
    private String cnicpictureFront;

    @Column(name = "CNICPICTUREBACK")
    private String cnicpictureBack;

    @Column(name = "CUSTOMERPICTURE")
    private String customerpicture;

    @Column(name = "PLACEOFBIRTH")
    private String placeofbirth;

    @Column(name = "CNICEXPIRY")
    private String cnicexpiry;
    //Raza adding for NayaPay end

    @Column(name = "USER_ID")
    private String userId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="customer")
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL})
    private List<CMSBusinessWallet> accountList;

    public String getCustomerId() {
        return CustomerId;
    }

    public void setCustomerId(String customerId) {
        CustomerId = customerId;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getMidname() {
        return midname;
    }

    public void setMidname(String midname) {
        this.midname = midname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public String getMobileNumber() {
        return MobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        MobileNumber = mobileNumber;
    }

    public String getHomeNumber() {
        return HomeNumber;
    }

    public void setHomeNumber(String homeNumber) {
        HomeNumber = homeNumber;
    }

    public String getOfficeNumber() {
        return OfficeNumber;
    }

    public void setOfficeNumber(String officeNumber) {
        OfficeNumber = officeNumber;
    }

    public String getFatherName() {
        return FatherName;
    }

    public void setFatherName(String fatherName) {
        FatherName = fatherName;
    }

    public String getMotherName() {
        return MotherName;
    }

    public void setMotherName(String motherName) {
        MotherName = motherName;
    }

    public String getEmailId() {
        return EmailId;
    }

    public void setEmailId(String emailId) {
        EmailId = emailId;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCustomerType() {
        return CustomerType;
    }

    public void setCustomerType(String customerType) {
        CustomerType = customerType;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public String getHomeAddress() {
        return HomeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        HomeAddress = homeAddress;
    }

    public String getOfficeAddress() {
        return OfficeAddress;
    }

    public void setOfficeAddress(String officeAddress) {
        OfficeAddress = officeAddress;
    }

    public String getOccupation() {
        return Occupation;
    }

    public void setOccupation(String occupation) {
        Occupation = occupation;
    }

    public Date getActivationDate() {
        return ActivationDate;
    }

    public void setActivationDate(Date activationDate) {
        ActivationDate = activationDate;
    }

    public Date getLastUpdateDate() {
        return LastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        LastUpdateDate = lastUpdateDate;
    }

    public Date getCreateDate() {
        return CreateDate;
    }

    public void setCreateDate(Date createDate) {
        CreateDate = createDate;
    }

    public String getBranchCode() {
        return BranchCode;
    }

    public void setBranchCode(String branchCode) {
        BranchCode = branchCode;
    }

    public Date getDateofBirth() {
        return DateofBirth;
    }

    public void setDateofBirth(Date dateofBirth) {
        DateofBirth = dateofBirth;
    }

    public String getProvince() {
        return Province;
    }

    public void setProvince(String province) {
        Province = province;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getCnicpictureFront() {
        return cnicpictureFront;
    }

    public void setCnicpictureFront(String cnicpicture) {
        this.cnicpictureFront = cnicpicture;
    }

    public String getCustomerpicture() {
        return customerpicture;
    }

    public void setCustomerpicture(String customerpicture) {
        this.customerpicture = customerpicture;
    }

    public String getCnicpictureBack() {
        return cnicpictureBack;
    }

    public void setCnicpictureBack(String cnicpictureBack) {
        this.cnicpictureBack = cnicpictureBack;
    }

    public String getCnicexpiry() {
        return cnicexpiry;
    }

    public void setCnicexpiry(String cnicexpiry) {
        this.cnicexpiry = cnicexpiry;
    }

    public String getPlaceofbirth() {
        return placeofbirth;
    }

    public void setPlaceofbirth(String placeofbirth) {
        this.placeofbirth = placeofbirth;
    }

    public String getKycStatus() {
        return KycStatus;
    }

    public void setKycStatus(String kycStatus) {
        KycStatus = kycStatus;
    }

    public String getAlternateEmailId() {
        return alternateEmailId;
    }

    public void setAlternateEmailId(String alternateEmailId) {
        this.alternateEmailId = alternateEmailId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CMSBusinessWallet> getAccountList() {
        return accountList;
    }

    public void setAccountList(List<CMSBusinessWallet> accountList) {
        this.accountList = accountList;
    }

    public String getIdentificationtype() {
        return identificationtype;
    }

    public void setIdentificationtype(String identificationtype) {
        this.identificationtype = identificationtype;
    }

    //TODO: Raza remove below CATEGORIES
    public CMSBusinessWallet getCDFWallet()
    {
        if(this.getAccountList() != null && this.getAccountList().size() > 0)
        {
            for(CMSBusinessWallet acct : this.getAccountList())
            {
                    if (acct.getCurrency().equals("976") && (acct.getCategory().equals(AccType.CAT_BILLER_WALLET.StringValue()) || acct.getCategory().equals(AccType.CAT_REVENUE_WLLT.StringValue()) || acct.getCategory().equals(AccType.CAT_SALESTAX_WLLT.StringValue()))) {
                        return acct;
                    }
            }
        }
        return null;
    }

    public CMSBusinessWallet getUSDWallet()
    {
        if(this.getAccountList() != null && this.getAccountList().size() > 0)
        {
            for(CMSBusinessWallet acct : this.getAccountList())
            {
                if(acct.getCurrency().equals("840") && (acct.getCategory().equals(AccType.CAT_BILLER_WALLET.StringValue()) || acct.getCategory().equals(AccType.CAT_REVENUE_WLLT.StringValue()) || acct.getCategory().equals(AccType.CAT_SALESTAX_WLLT.StringValue())))
                {
                    return acct;
                }
            }
        }
        return null;
    }


}
