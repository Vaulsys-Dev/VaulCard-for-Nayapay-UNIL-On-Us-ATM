package vaulsys.scheduler.job;

import vaulsys.message.Message;
import vaulsys.message.MessageManager;
import vaulsys.message.ScheduleMessage;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionType;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ConfirmationTransactionJobThread implements Runnable{
	Logger logger = Logger.getLogger(this.getClass());

	Transaction transaction;
	
	public ConfirmationTransactionJobThread(Transaction trx) {
		super();
		this.transaction = trx;
	}


	@Override
	public void run() {
		GeneralDao.Instance.beginTransaction();
		List<Message> msg = new ArrayList<Message>();
		ProcessContext.get().init();
		logger.info("Try to Send Message Number 220... " );
		try {
				ScheduleMessage scheduleMessage;
		        scheduleMessage = new ScheduleMessage(SchedulerConsts.CONFIRMATION_TRX_TYP, transaction.getFirstTransaction().getOutgoingIfx().getAuth_Amt());
//		        scheduleMessage.setEndPointTerminal(transaction.getInputMessage().getEndPointTerminal());
		        scheduleMessage.setResponseCode(ISOResponseCodes.APPROVED);
		        Transaction newTransaction = new Transaction(TransactionType.SELF_GENERATED);
		        newTransaction.setDebugTag("ConfTrx_"+ transaction.getId());
		        newTransaction.setInputMessage(scheduleMessage);
		        newTransaction.setFirstTransaction(newTransaction);

		        Ifx ifx = transaction.getFirstTransaction().getOutgoingIfx();
		        scheduleMessage.setIfx(ifx);
		        newTransaction.setLifeCycle(transaction.getLifeCycle());
		        GeneralDao.Instance.saveOrUpdate(newTransaction);
		        scheduleMessage.setTransaction(newTransaction);
		        GeneralDao.Instance.saveOrUpdate(ifx);
		        GeneralDao.Instance.saveOrUpdate(scheduleMessage);
		        GeneralDao.Instance.saveOrUpdate(scheduleMessage.getMsgXml());
		        msg.add(scheduleMessage);
			
			MessageManager.getInstance().putRequests(msg);
			
			
			GeneralDao.Instance.endTransaction();
		} catch (Exception e) {
			logger.error("Error in sending Message Number 220...", e);
		}	
	}

}
