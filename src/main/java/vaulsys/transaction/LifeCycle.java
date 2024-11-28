package vaulsys.transaction;

import vaulsys.persistence.IEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "trx_lifecycle")
public class LifeCycle implements IEntity<Long>{
    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="lifecycle-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "lifecycle-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "lifecycle_seq")
            })
    private Long id;

    private Boolean isComplete;
    private LifeCycleStatus isReturned;
    private LifeCycleStatus isReturnReversed;
    private LifeCycleStatus isFullyReveresed;
    private LifeCycleStatus isPartiallyReveresed;
    private LifeCycleStatus hasAuthorization;
    private LifeCycleStatus confirmationStatus;

    @Column(name="rev_rscode")
    private String reversalRsCode;

    @Column(name="sorush_lifecycle")
    private Long sorushLifeCycle;
    
	//TASK Task103 
    @Column(name = "reconcile_sup_doc")
    private String disagrementSupplementaryDocnum;

	//TASK Task103
	public String getDisagrementSupplementaryDocnum() {
		return disagrementSupplementaryDocnum;
	}

	//TASK Task103
	public void setDisagrementSupplementaryDocnum(
			String disagrementSupplementaryDocnum) {
		this.disagrementSupplementaryDocnum = disagrementSupplementaryDocnum;
	}    

    public String getReversalRsCode() {
        return reversalRsCode;
    }

    public void setReversalRsCode(String reversalRsCode) {
        this.reversalRsCode = reversalRsCode;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }


    public LifeCycle() {
        super();
        isReturned = LifeCycleStatus.NOTHING;
        isComplete = false;
        isReturnReversed = LifeCycleStatus.NOTHING;
        isFullyReveresed = LifeCycleStatus.NOTHING;
        isPartiallyReveresed = LifeCycleStatus.NOTHING;
        hasAuthorization = LifeCycleStatus.NOTHING;
        confirmationStatus = LifeCycleStatus.NOTHING;
    }

    public LifeCycleStatus getIsReturned() {
        return isReturned;
    }

    public void setIsReturned(LifeCycleStatus isReturned) {
        this.isReturned = isReturned;
    }

    public LifeCycleStatus getIsFullyReveresed() {
        return isFullyReveresed;
    }

    public void setIsFullyReveresed(LifeCycleStatus isFullyReveresed) {
        this.isFullyReveresed = isFullyReveresed;
    }

    public LifeCycleStatus getIsPartiallyReveresed() {
        return isPartiallyReveresed;
    }

    public void setIsPartiallyReveresed(LifeCycleStatus isPartiallyReveresed) {
        this.isPartiallyReveresed = isPartiallyReveresed;
    }

    public LifeCycleStatus getHasAuthorization() {
        return hasAuthorization;
    }

    public void setHasAuthorization(LifeCycleStatus hasAuthorization) {
        this.hasAuthorization = hasAuthorization;
    }

    public LifeCycleStatus getIsReturnReversed() {
        return isReturnReversed;
    }

    public void setIsReturnReversed(LifeCycleStatus isReturnReversed) {
        this.isReturnReversed = isReturnReversed;
    }

    public LifeCycleStatus getConfirmationStatus() {
        return confirmationStatus;
    }

    public void setConfirmationStatus(LifeCycleStatus confirmationStatus) {
        this.confirmationStatus = confirmationStatus;
    }


    public Long getSorushLifeCycle() {
        return sorushLifeCycle;
    }

    public void setSorushLifeCycle(Long sorushLifeCycle) {
        this.sorushLifeCycle = sorushLifeCycle;
    }

    public LifeCycle clone() {
        LifeCycle newLifeCycle = new LifeCycle();
        newLifeCycle.setHasAuthorization(getHasAuthorization());
        newLifeCycle.setIsFullyReveresed(getIsFullyReveresed());
        newLifeCycle.setIsPartiallyReveresed(getIsPartiallyReveresed());
        newLifeCycle.setIsReturned(getIsReturned());
        newLifeCycle.setIsReturnReversed(getIsReturnReversed());
        newLifeCycle.setIsComplete(getIsComplete());
        newLifeCycle.setConfirmationStatus(getConfirmationStatus());
		//TASK Task103
		newLifeCycle.setDisagrementSupplementaryDocnum(getDisagrementSupplementaryDocnum());
		
        newLifeCycle.id = null;
        return newLifeCycle;
    }

    public Boolean getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(Boolean isComplete) {
        this.isComplete = isComplete;
    }

    @Override
    public String toString() {
        return id!=null ? id.toString():"";
    }
}

