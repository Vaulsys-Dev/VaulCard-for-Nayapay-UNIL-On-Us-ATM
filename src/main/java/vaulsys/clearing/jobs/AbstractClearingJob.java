package vaulsys.clearing.jobs;

import vaulsys.clearing.reconcile.ICutover;
import vaulsys.clearing.reconcile.IReconcilement;
import vaulsys.message.Message;
import vaulsys.transaction.Transaction;
import vaulsys.util.SwitchContext;
import vaulsys.wfe.ProcessContext;

public abstract class AbstractClearingJob extends SwitchContext implements ClearingJob {

    protected IReconcilement reconcilement;
    protected ICutover cutover;
    protected ClearingJob preJob;
    protected ClearingJob postJob;


    public void setReconcilement(IReconcilement reconcilement) {
        this.reconcilement = reconcilement;
    }

    public void setCutover(ICutover cutover) {
        this.cutover = cutover;
    }

    public void setPostJob(ClearingJob postJob) {
        this.postJob = postJob;
    }

    public void setPreJob(ClearingJob preJob) {
        this.preJob = preJob;
    }

    public abstract void execute(Message incomingMessage,
                                 Transaction refTransaction, ProcessContext processContext) throws Exception;

    public ClearingJob postJob() throws Exception {
        return null;
    }

    @Override
    public ClearingJob preJob() throws Exception {
        return null;
    }

    protected abstract ICutover getCutover();

    protected  IReconcilement getReconcilement(){
    	return reconcilement;
    }

}
