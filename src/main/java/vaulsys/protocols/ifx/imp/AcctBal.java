package vaulsys.protocols.ifx.imp;


import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.transaction.Transaction;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

@Entity
@Table(name = "ifx_Acct_Bal")
public class AcctBal implements IEntity<Long>, Cloneable {
   
    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="acctbal-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "acctbal-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "acctbal_seq")
    				})
    private Long id;
    
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "acctype"))
    })    
    private AccType AcctType = AccType.UNKNOWN;

    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "baltype"))
    })    
    private BalType BalType = vaulsys.protocols.ifx.enums.BalType.UNKNOWN;

    @Basic
    private String Amt;
    
    @Basic
    private String CurCode;

    @ManyToOne(fetch = FetchType.LAZY/*, cascade = CascadeType.ALL*/)
//    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL })
    @JoinColumn(name = "emvrsdata", nullable = true, updatable = true)
    @ForeignKey(name="acctbal_emvrsdata_fk")
//    @Index(name="idx_msg_trx")
    private EMVRsData emvrsdata;
    
    public void setAcctType(AccType acctType) {
        AcctType = acctType;
    }

    public void setBalType(BalType balType) {
        BalType = balType;
    }

    public void setAmt(String amt) {
        Amt = amt;
    }

    public void setCurCode(String curCode) {
        CurCode = curCode;
    }

	
	public AccType getAcctType() {
		return this.AcctType;
	}

	
	public String getAmt() {
		return this.Amt;
	}

	
	public BalType getBalType() {
		return this.BalType;
	}

	
	public String getCurCode() {
		return this.CurCode;
	}

	
	public Long getId() {
		return this.id;
	}

	
	public void setId(Long id) {
		this.id = id; 
	}

	
	protected Object clone() {
		AcctBal obj = new AcctBal();
		obj.setAcctType(getAcctType().copy());
		obj.setAmt(Amt);
		obj.setBalType(getBalType().copy());
		obj.setCurCode(CurCode);
		return obj;
	}
	
	
	public AcctBal copy() {
		return (AcctBal) clone();
	}

	public EMVRsData getEmvrsdata() {
		return emvrsdata;
	}

	public void setEmvrsdata(EMVRsData emvrsdata) {
		this.emvrsdata = emvrsdata;
	}
}
