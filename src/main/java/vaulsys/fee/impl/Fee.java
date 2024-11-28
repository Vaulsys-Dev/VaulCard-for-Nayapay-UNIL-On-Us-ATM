package vaulsys.fee.impl;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.persistence.IEntity;
import vaulsys.transaction.Transaction;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "fee")
public class Fee implements IEntity<Long> {

    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="fee-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "fee-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "fee_seq")
    				})
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trx")
	@ForeignKey(name="fee_trx_fk")
    private Transaction transaction;

    private long amount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "dayDate.date", column = @Column(name = "insertaion_date")),
        @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "insertaion_time"))
            })
    private DateTime insertionTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_prof")
    @ForeignKey(name="fee_feeprof_fk")
    private FeeProfile feeProfile;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clr_prof")
    @ForeignKey(name="fee_clrprof_fk")
    private ClearingProfile clearingProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fine_credited")
    @ForeignKey(name="fee_fine_credited_fk")
    private FinancialEntity entityToBeCredited;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fine_debited")
    @ForeignKey(name="fee_fine_debited_fk")
    private FinancialEntity entityToBeDebited;

    @ManyToOne(targetEntity = FeeItem.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "feeitem")
    @ForeignKey(name="fee_feeitem_fk")
    private FeeItem feeItem;


    public Fee() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public DateTime getInsertionTime() {
        return insertionTime;
    }

    public void setInsertionTime(DateTime insertionTime) {
        this.insertionTime = insertionTime;
    }

    public FeeProfile getFeeProfile() {
        return feeProfile;
    }

    public void setFeeProfile(FeeProfile feeProfile) {
        this.feeProfile = feeProfile;
    }

    public FinancialEntity getEntityToBeCredited() {
        return entityToBeCredited;
    }

    public void setEntityToBeCredited(FinancialEntity entityToBeCredited) {
        this.entityToBeCredited = entityToBeCredited;
    }

    public FinancialEntity getEntityToBeDebited() {
        return entityToBeDebited;
    }

    public void setEntityToBeDebited(FinancialEntity entityToBeDebited) {
        this.entityToBeDebited = entityToBeDebited;
    }

    public FeeItem getFeeItem() {
        return feeItem;
    }

    public void setFeeItem(FeeItem feeItem) {
        this.feeItem = feeItem;
    }

	public ClearingProfile getClearingProfile() {
		return clearingProfile;
	}

	public void setClearingProfile(ClearingProfile clearingProfile) {
		this.clearingProfile = clearingProfile;
	}
}
