package vaulsys.modernpayment.onlinebillpayment;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.transaction.Transaction;
import vaulsys.util.NotUsed;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class OnlineBillPaymentService {
	private static final Logger logger = Logger.getLogger(OnlineBillPaymentService.class);
	

	@NotUsed
	synchronized public OnlineBillPayment getOnlineBillPayment(Ifx ifx, Long refnum) {
		
		String query = "from " + OnlineBillPayment.class.getName() + " l where " +
						" l.refNum = :refnum " +
						" and l.nextPaymentStatus = :status ";
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		params.put("refnum", refnum);
		params.put("status", OnlineBillPaymentStatus.NOT_PAID);
		
		logger.debug("before get lottery...");
		OnlineBillPayment onbill = (OnlineBillPayment) GeneralDao.Instance.findObject(query, params);
		logger.debug("after get lottery...");
		
		return onbill;
	}
	
//	synchronized public static OnlineBillPayment getOnlineBillPayment(Ifx ifx, List<Long> creditList, LotteryAssignmentPolicy policy) {
//
//		String query = "from " + Lottery.class.getName() +
//			" l where " +
//			" l.credit in (:credit) " + 
//			" and l.state = :state " +
//			" and l.assignmentPolicy = :policy";
//
//		Map<String, Object> params = new HashMap<String, Object>();
//
//		params.put("credit", creditList);
//		params.put("state", LotteryState.NOT_ASSIGNED);
//		params.put("policy", policy);
//
//		logger.debug("before get lottery...");
//		List<Lottery> lotteryList = GeneralDao.Instance.find(query, params);
//		Lottery lottery = null;
//		if (lotteryList != null && lotteryList.size() > 0) {
//			Random randomGenerator = new Random();
//			int random = randomGenerator.nextInt(lotteryList.size());
//			lottery = lotteryList.get(random);
//			// Lottery lottery = (Lottery) generalDao.findObject(query, params,
//			// false);
//			logger.debug("after get lottery...");
//		}
//		return lottery;
//	}

	public static void unlockOnlineBillPayment(Ifx ifx, Transaction transaction){
//		if(ifx != null)
//			logger.debug("Unlock: " + ifx.getId() + " ," + ifx.getIfxType());
		
		if (ifx != null 
				&& ifx.getOnlineBillPaymentData() != null 
				&& ifx.getOnlineBillPaymentData().getOnlineBillPayment() != null
				&& transaction.getLifeCycle() != null
				&& ifx.getOnlineBillPaymentData().getOnlineBillPayment().getLifeCycle() != null
				&& transaction.getLifeCycle().equals(ifx.getOnlineBillPaymentData().getOnlineBillPayment().getLifeCycle())
				&& !ifx.getOnlineBillPaymentData().getNextPaymentStatus().equals(OnlineBillPaymentStatus.NOT_PAID)
				) {
//			logger.debug("Unlock: " + ifx.getId() + " ," + ifx.getIfxType());			
			
//			Lottery lottery = ifx.getLottery();
			OnlineBillPayment onbill = ifx.getOnlineBillPaymentData().getOnlineBillPayment();
			logger.debug("try to lock onlineBillPayment with this refrence number: "+onbill.getRefNum());
			GeneralDao.Instance.lockReadAndWrite(onbill);
			logger.debug("onlineBillPayment with refrence number "+onbill.getRefNum()+"has been locked");
			
//			ifx.setLotteryStatePrv(lottery.getState());
			//TODO: in check beshe!
			ifx.getOnlineBillPaymentData().setPreviousPaymentStatus(onbill.getPaymentStatus());
//			ifx.getOnlineBillPaymentData().getOnlineBillPayment().setPaymentStatus(onbill.getPaymentStatus());

//			lottery.setState(LotteryState.NOT_ASSIGNED);
//			ifx.setLotteryStateNxt(lottery.getState());
			onbill.setPaymentStatus(OnlineBillPaymentStatus.NOT_PAID);
			ifx.getOnlineBillPaymentData().setNextPaymentStatus(OnlineBillPaymentStatus.NOT_PAID);
			
			onbill.setChangePaymentStatusTime(DateTime.now().getDateTimeLong());
//			ifx.getOnliBillPaymentData().getOnlineBillPayment().setNextPaymentStatus(nextPaymentStatus)
			
			logger.debug("onlineBillPayment with cardAppPan unlocked:" + onbill.getRefNum() /*lottery.getSerial()*/);
			GeneralDao.Instance.saveOrUpdate(onbill);
			
		}
	}

}
