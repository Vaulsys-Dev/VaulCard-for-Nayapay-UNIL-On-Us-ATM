package vaulsys.scheduler;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.SettlementReport;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;

@Entity
@DiscriminatorValue(value = "IssuingFCBDocument")
public class IssuingFCBDocumentJobInfo extends JobInfo {

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "report")
    @ForeignKey(name="issuingFCBDoc_report_fk")
    private SettlementReport report;
    

    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "action", column = @Column(name = "action"))
    })
    private IssuingDocumentAction action;

    public IssuingFCBDocumentJobInfo() {
    }

    public IssuingFCBDocumentJobInfo(DateTime fireTime, SettlementReport report, IssuingDocumentAction action) {
        super(fireTime);
        this.report = report;
        this.action = action;
    }

	public SettlementReport getReport() {
		return report;
	}

	public void setReport(SettlementReport report) {
		this.report = report;
	}

	public IssuingDocumentAction getAction() {
		return action;
	}

	public void setAction(IssuingDocumentAction action) {
		this.action = action;
	}
    
}
