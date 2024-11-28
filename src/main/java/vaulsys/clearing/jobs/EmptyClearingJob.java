package vaulsys.clearing.jobs;

import vaulsys.message.Message;
import vaulsys.transaction.Transaction;
import vaulsys.wfe.ProcessContext;

public class EmptyClearingJob implements ClearingJob {
	
	public static final EmptyClearingJob Instance = new EmptyClearingJob();
	private EmptyClearingJob(){}
	
    public void execute(Message incomingMessage, Transaction refTransaction, ProcessContext processContext) throws Exception {
        throw new UnsupportedOperationException();
    }

    public ClearingJob postJob() throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ClearingJob preJob() throws Exception {
        return null;
    }
}
