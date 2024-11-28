package vaulsys.webservice.walletcardmgmtwebservice.model;

import javax.persistence.*;

/**
 * Created by HP on 29-Jan-18.
 */
@Entity
@Table(name = "nayapay_customer")
public class NayaPayCustomer {

    @Id
    @GeneratedValue(generator="NAYAPAY_CUSTOMER_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "NAYAPAY_CUSTOMER_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "NAYAPAY_CUSTOMER_ID_SEQ")
            })
    private Long id;

    @Column(name = "MOBILE_NUMBER")
    private String mobilenumber;

    @Column(name = "CNIC")
    private String cnic;

    @Column(name = "CNIC_PIC")
    private byte[] cnicpicture;

    @Column(name = "CUSTOMER_PIC")
    private byte[] customerpicture;

    @Column(name = "MOTHER_NAME")
    private String mothername;

    @Column(name = "DATEOFBIRTH")
    private String dataofbirth;

    @Column(name = "NAYAPAYID")
    private String nayapayid;

    public NayaPayCustomer()
    {
        this.mobilenumber = "";
        this.cnic = "";
        this.mothername = "";
        this.dataofbirth = "";
        this.nayapayid = "";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public byte[] getCnicpicture() {
        return cnicpicture;
    }

    public void setCnicpicture(byte[] cnicpicture) {
        this.cnicpicture = cnicpicture;
    }

    public byte[] getCustomerpicture() {
        return customerpicture;
    }

    public void setCustomerpicture(byte[] customerpicture) {
        this.customerpicture = customerpicture;
    }

    public String getMothername() {
        return mothername;
    }

    public void setMothername(String mothername) {
        this.mothername = mothername;
    }

    public String getDataofbirth() {
        return dataofbirth;
    }

    public void setDataofbirth(String dataofbirth) {
        this.dataofbirth = dataofbirth;
    }

    public String getNayapayid() {
        return nayapayid;
    }

    public void setNayapayid(String nayapayid) {
        this.nayapayid = nayapayid;
    }
}
