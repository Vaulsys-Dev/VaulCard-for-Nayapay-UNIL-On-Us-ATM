package vaulsys.clearing.jobs;

import vaulsys.message.Message;
import vaulsys.transaction.Transaction;
import vaulsys.wfe.ProcessContext;

public interface ClearingJob {

    void execute(Message incomingMessage, Transaction refTransaction, ProcessContext processContext) throws Exception;

    ClearingJob postJob() throws Exception;

    //TODO Noroozi: we can bind received response message to its corresponding request!
    ClearingJob preJob() throws Exception;
}
