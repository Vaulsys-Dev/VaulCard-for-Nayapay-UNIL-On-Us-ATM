package vaulsys.protocols.ifx.imp;

import java.io.Serializable;


import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.webservice.mcivirtualvosoli.common.MCIVosoliState;
//import vaulsys.webservices.mcivirtualvosoli.common.MCIVosoliState;

import javax.persistence.*;

//@Entity
//@Table(name = "ifx_bill_payment_data")
@Embeddable
public class BillPaymentData implements Serializable, Cloneable {
//    @Id
//    @GeneratedValue(generator="switch-gen")
//	private Long id;

    private String billID;

    private String billPaymentID;

    private Integer billCompanyCode;

    private String billUnParsedData;
    
   //majid_prg 20150520 naja vosoli
    // state for general porpose vosoli state declaration
    private Integer vosoliState;

    public int getVosoliState() {
		return vosoliState;
	}
    public void setVosoliState(Integer vosoliState) {
		this.vosoliState = vosoliState;
	}

    @Embedded
    @AttributeOverrides( { @AttributeOverride(name = "type", column = @Column(name = "bill_org_type")) })
    private OrganizationType billOrgType = OrganizationType.UNKNOWN;

    @Transient
    private Byte bill_org_typeId;

    @Embedded
	@AttributeOverrides({@AttributeOverride(name = "state", column = @Column(name = "mcivosoli_state"))})
	private MCIVosoliState mciVosoliState /*= MCIVosoliState.NOT_SEND*/;

	@Column(name = "mcivosoli_desc")
	private String mciVosoliDesc;

	public MCIVosoliState getMciVosoliState() {
		return mciVosoliState;
	}

	public void setMciVosoliState(MCIVosoliState mciVosoliState) {
		this.mciVosoliState = mciVosoliState;
	}

	public String getMciVosoliDesc() {
		return mciVosoliDesc;
	}

	public void setMciVosoliDesc(String mciVosoliDesc) {
		this.mciVosoliDesc = mciVosoliDesc;
	}

    public BillPaymentData() {
    }

    public BillPaymentData(String billID, String billPaymentID, Integer billCompanyCode, OrganizationType billOrgType) {
        this.billID = billID;
        this.billPaymentID = billPaymentID;
        this.billCompanyCode = billCompanyCode;
        this.billOrgType = billOrgType;
        this.bill_org_typeId = new Byte(OrganizationType.getCode(billOrgType));
    }

    public BillPaymentData(String billID, String paymentID, Integer billCompanyCode) {
        super();
        this.billID = billID;
        this.billPaymentID = paymentID;
        this.billCompanyCode = billCompanyCode;
    }

    public BillPaymentData(String billID, String paymentID) {
        this.billID = billID;
        this.billPaymentID = paymentID;
    }


    public String getBillID() {
        return billID;
    }

    public void setBillID(String billID) {
        this.billID = billID;
    }

    public String getBillPaymentID() {
        return billPaymentID;
    }

    public void setBillPaymentID(String billPaymentID) {
        this.billPaymentID = billPaymentID;
    }

    public OrganizationType getBillOrgType() {
        return billOrgType;
    }

    public void setBillOrgType(OrganizationType billOrgType) {
        this.billOrgType = billOrgType;
        this.bill_org_typeId = new Byte(OrganizationType.getCode(billOrgType));
    }

    public Integer getBillCompanyCode() {
        return billCompanyCode;
    }

    public void setBillCompanyCode(Integer billCompanyCode) {
        this.billCompanyCode = billCompanyCode;
    }

    @Override
    protected Object clone() {
        BillPaymentData obj = new BillPaymentData();
        obj.setBillOrgType(billOrgType);
        obj.setBillID(billID);
        obj.setBillPaymentID(billPaymentID);
        obj.setBillCompanyCode(billCompanyCode);
        obj.setBillUnParsedData(billUnParsedData);
		obj.setMciVosoliState(mciVosoliState);
		obj.setMciVosoliDesc(mciVosoliDesc);
        return obj;
    }


    public BillPaymentData copy() {
        return (BillPaymentData) clone();
    }

    public void copyFields(BillPaymentData source) {
        if (billCompanyCode == null)
            billCompanyCode = source.getBillCompanyCode();

        if (billID == null || "".equals(billID))
            billID = source.getBillID();

        if (billPaymentID == null || "".equals(billPaymentID))
            billPaymentID = source.getBillPaymentID();

        if (billOrgType == null || OrganizationType.UNKNOWN.equals(billOrgType)){
            billOrgType = source.getBillOrgType();
            bill_org_typeId = new Byte(OrganizationType.getCode(billOrgType));
        }

        if (billUnParsedData == null || "".equals(billUnParsedData))
            billUnParsedData = source.getBillUnParsedData();

		if(mciVosoliState == null || "".equals(mciVosoliState))
			mciVosoliState = source.getMciVosoliState();

		if(mciVosoliDesc == null || "".equals(mciVosoliDesc))
			mciVosoliDesc = source.getMciVosoliDesc();
    }

    public String getBillUnParsedData() {
        return billUnParsedData;
    }

    public void setBillUnParsedData(String unParsedData) {
        this.billUnParsedData = unParsedData;
    }

}
