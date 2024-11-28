package vaulsys.cms.base;

import vaulsys.contact.City;
import vaulsys.contact.Country;
import vaulsys.entity.impl.Branch;
import vaulsys.persistence.BaseEntity;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Raza on 24-Apr-17.
 */
@Entity
@Table(name = "cms_customer")
@PrimaryKeyJoinColumn(name = "ID")
public class CMSCustomer extends BaseEntity<Long> { //Raza This Class should be ReadOnly, Not removing public Setter as this class is also used by UI

    @Id
    @GeneratedValue(generator="CMS_CUSTOMER_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "CMS_CUSTOMER_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "CMS_CUSTOMER_ID_SEQ")
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

    @Column(name = "COMPANY")
    private String company;

    @Column(name = "CUSTOMERTYPE")
    private String CustomerType;

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

    @Column(name = "CUSTOMER_PIC")
    private String customerpicture;

    @Column(name = "PLACEOFBIRTH")
    private String placeofbirth;

    @Column(name = "TELECOMSP")
    private String tsp;

    @Column(name = "CNICEXPIRY")
    private String cnicexpiry;
    //Raza adding for NayaPay end

    @OneToMany(

            fetch = FetchType.LAZY
    )
    @JoinTable(
            name = "CMS_CUSTPROD",
            joinColumns = {            @JoinColumn(
                    name = "CUSTOMER_ID"
            )},
            inverseJoinColumns = {            @JoinColumn(
                    name = "PRODUCT_ID"
            )}
    )
    @ForeignKey(
            name = "customer_products_fk"
    )
    private List<CMSProduct> list_CustProducts;

    @Column(name = "IS_BIO_VERIFIED")
    private String isBioVerified;

    // Asim Shahzad, Date : 27th Aug 2020, Call ID : VC-NAP-202008073/ VC-NAP-202009301
    @Column(name = "SEC_QUES_RETRIES")
    private String secretQuestionRetries;

    // Asim Shahzad, Date : 1st Nov 2021, Call ID : VC-NAP-202110282
    @Column(name = "SEC_QUES_1_RETRIES")
    private String secretQuestion1Retries;

    @Column(name = "SEC_QUES_2_RETRIES")
    private String secretQuestion2Retries;
    // ==============================================================

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

    public List<CMSProduct> getList_CustProducts() {
        return list_CustProducts;
    }

    public void setList_CustProducts(List<CMSProduct> list_CustProducts) {
        this.list_CustProducts = list_CustProducts;
    }

    public void addProductChannel(CMSProduct customerProducts) {
        if(this.list_CustProducts == null) {
            this.list_CustProducts = new ArrayList(1);
        }

        this.list_CustProducts.add(customerProducts);
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

    public String getTsp() {
        return tsp;
    }

    public void setTsp(String tsp) {
        this.tsp = tsp;
    }

    public String getIsBioVerified() {
        return isBioVerified;
    }

    public void setIsBioVerified(String isBioVerified) {
        this.isBioVerified = isBioVerified;
    }

    // Asim Shahzad, Date : 27th Aug 2020, Call ID : VC-NAP-202008073/ VC-NAP-202009301
    public String getSecretQuestionRetries() {
        return secretQuestionRetries;
    }

    public void setSecretQuestionRetries(String secretQuestionRetries) {
        this.secretQuestionRetries = secretQuestionRetries;
    }

    // Asim Shahzad, Date : 1st Nov 2021, Call ID : VC-NAP-202110282

    public String getSecretQuestion1Retries() {
        return secretQuestion1Retries;
    }

    public void setSecretQuestion1Retries(String secretQuestion1Retries) {
        this.secretQuestion1Retries = secretQuestion1Retries;
    }

    public String getSecretQuestion2Retries() {
        return secretQuestion2Retries;
    }

    public void setSecretQuestion2Retries(String secretQuestion2Retries) {
        this.secretQuestion2Retries = secretQuestion2Retries;
    }

    // ===============================================================
}
