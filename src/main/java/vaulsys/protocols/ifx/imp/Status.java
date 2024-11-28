package vaulsys.protocols.ifx.imp;


import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.Severity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

//@Embeddable
@Entity
@Table(name="ifx_status")
public class Status implements IEntity<Long>, Cloneable {

	@Id
    @GeneratedValue(generator="ifx-status-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "ifx-status-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "ifx_status_seq")
    				})
	Long id;
	
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "ifx")
	private Ifx ifx;
	

	//	 @Embedded
//	@AttributeOverrides( { @AttributeOverride(name = "type", column = @Column(name = "status_code")) })
//    private StatusCode StatusCode;
//
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "status_severity"))
    })
    private Severity Severity = vaulsys.protocols.ifx.enums.Severity.INFO;

    @Column(length=2000, name = "status_desc")
    private String StatusDesc;

	public Status() {
	}
	
	public Status(Ifx ifx) {
		setIfx(ifx);
	}

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

//    public void setStatusCode(StatusCode statusCode) {
//        this.StatusCode = statusCode;
//    }
//
    public void setSeverity(Severity severity) {
        this.Severity = severity;
    }

    public void setStatusDesc(String statusDesc) {
    	if (statusDesc.length()>1999)
    		statusDesc = statusDesc.substring(0, 1999);
        this.StatusDesc = statusDesc;
    }

	
	public Severity getSeverity() {
		return this.Severity;
	}

	
//	public StatusCode getStatusCode() {
//		return this.StatusCode;
//	}
	
	public String getStatusDesc() {
		return this.StatusDesc;
	}

	
	protected Object clone() {
		Status obj = new Status();
		obj.setSeverity(getSeverity().copy());
//		obj.setStatusCode(StatusCode);
		obj.setStatusDesc(StatusDesc);
		return obj;
	}
	
	
	public Status copy() {
		return (Status) clone();
	}

	public Ifx getIfx() {
		return ifx;
	}

	public void setIfx(Ifx ifx) {
		this.ifx = ifx;
	}
}
