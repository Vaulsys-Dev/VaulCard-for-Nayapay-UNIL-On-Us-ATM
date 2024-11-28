package vaulsys.clearing.base;

import vaulsys.customer.Core;
import vaulsys.persistence.IEntity;
import vaulsys.util.Util;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "settlement_report")
public class SettlementReport implements IEntity<Long> {

	@Id
//	@GeneratedValue(generator = "switch-gen")
    @GeneratedValue(generator="settlereport-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "settlereport-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "settlereport_seq")
    				})
	private Long id;

	@Embedded
	@AttributeOverrides( { @AttributeOverride(name = "type", column = @Column(name = "core")) })
	private Core core;

	@Lob
	private String report;

	@Column(name = "doc_num")
	private String documentNumber;

	@Column(name = "ref_doc_num")
	private String referenceDocumentNumber;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "stl_state")
	@ForeignKey(name = "stlreport_stlstate_fk")
	private SettlementState settlementState;

	public SettlementReport() {
		super();
	}

	public SettlementReport(Core core, String report, String referenceDocumentNumber, SettlementState settlementState) {
		super();
		this.core = core;
		this.report = report;
		this.settlementState = settlementState;
		this.referenceDocumentNumber = referenceDocumentNumber;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Core getCore() {
		return core;
	}

	public void setCore(Core core) {
		this.core = core;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

	@Override
	public String toString() {
		if (getCore() != null || Util.hasText(getReport()))
			return getCore() + ": " + getReport();

		return super.toString();
	}

	public SettlementState getSettlementState() {
		return settlementState;
	}

	public void setSettlementState(SettlementState settlementState) {
		this.settlementState = settlementState;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((core == null) ? 0 : core.hashCode());
		result = prime * result + ((report == null) ? 0 : report.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SettlementReport other = (SettlementReport) obj;
		if (core == null) {
			if (other.core != null)
				return false;
		} else if (!core.equals(other.core))
			return false;
		if (report == null) {
			if (other.report != null)
				return false;
		} else if (!report.equalsIgnoreCase(other.report))
			return false;
		return true;
	}

	public String getReferenceDocumentNumber() {
		return referenceDocumentNumber;
	}

	public void setReferenceDocumentNumber(String referenceDocumentNumber) {
		this.referenceDocumentNumber = referenceDocumentNumber;
	}
}
