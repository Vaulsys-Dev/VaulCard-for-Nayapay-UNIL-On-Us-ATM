package vaulsys.clearing.base;

import vaulsys.persistence.IEntity;

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
@Table(name = "settlement_data_report")
public class SettlementDataReport implements IEntity<Long> {
	@Id
//	@GeneratedValue(generator = "switch-gen")
    @GeneratedValue(generator="settledatareport-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "settledatareport-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "settledatareport_seq")
    				})
	private Long id;

    @Lob
    private byte[] report;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stl_data")
	@ForeignKey(name = "stldatareport_stldata_fk")
	private SettlementData settlementData;

	@Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "type"))
    })
    private SettlementDataReportType type;

	public SettlementDataReport() {
	}

	public SettlementDataReport(SettlementData settlementData, byte[] report, SettlementDataReportType type) {
		this.report = report;
		this.settlementData = settlementData;
		this.type = type;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public byte[] getReport() {
		return report;
	}

	public void setReport(byte[] report) {
		this.report = report;
	}

	public SettlementData getSettlementData() {
		return settlementData;
	}

	public void setSettlementData(SettlementData settlementData) {
		this.settlementData = settlementData;
	}

	public SettlementDataReportType getType() {
		return type;
	}

	public void setType(SettlementDataReportType type) {
		this.type = type;
	}

    

}
