package vaulsys.lottery;

import vaulsys.lottery.consts.LotteryState;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.transaction.Transaction;
import vaulsys.util.NotUsed;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

public class LotteryService {
	private static final Logger logger = Logger.getLogger(LotteryService.class);

	@NotUsed
	synchronized public Lottery getLottery(Ifx ifx, Long credit) {
		
		String query = "from " + Lottery.class.getName() + " l where " +
						" l.credit = :credit " +
						" and l.state = :state ";
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		params.put("credit", credit);
		params.put("state", LotteryState.NOT_ASSIGNED);
		
		logger.debug("before get lottery...");
		Lottery lottery = (Lottery) GeneralDao.Instance.findObject(query, params);
		logger.debug("after get lottery...");
		
		return lottery;
	}
	
	synchronized public static Lottery getLottery(Ifx ifx, List<Long> creditList, LotteryAssignmentPolicy policy) {

		String query = "from " + Lottery.class.getName() +
			" l where " +
			" l.credit in (:credit) " + 
			" and l.state = :state " +
			" and l.assignmentPolicy = :policy";

		Map<String, Object> params = new HashMap<String, Object>();

		params.put("credit", creditList);
		params.put("state", LotteryState.NOT_ASSIGNED);
		params.put("policy", policy);

		logger.debug("before get lottery...");
		List<Lottery> lotteryList = GeneralDao.Instance.find(query, params);
		Lottery lottery = null;
		if (lotteryList != null && lotteryList.size() > 0) {
			Random randomGenerator = new Random();
			int random = randomGenerator.nextInt(lotteryList.size());
			lottery = lotteryList.get(random);
			// Lottery lottery = (Lottery) generalDao.findObject(query, params,
			// false);
			logger.debug("after get lottery...");
		}
		return lottery;
	}

	public static void unlockLottery(Ifx ifx, Transaction transaction){
//		if(ifx != null)
//			logger.debug("Unlock: " + ifx.getId() + " ," + ifx.getIfxType());
		
		if (ifx != null 
//				&& ShetabFinalMessageType.isCellChargeAndReversalMessage(ifx.getIfxType())
				&& ifx.getLottery() != null 
				&& transaction.getLifeCycle() != null
				&& ifx.getLottery().getLifeCycle() != null
				&& transaction.getLifeCycle().equals(ifx.getLottery().getLifeCycle())
				&& !ifx.getLottery().getState().equals(LotteryState.NOT_ASSIGNED)
				) {
			
			Lottery lottery = ifx.getLottery();
			GeneralDao.Instance.lockReadAndWrite(lottery);
			
			ifx.setLotteryStatePrv(lottery.getState());

			lottery.setState(LotteryState.NOT_ASSIGNED);
			ifx.setLotteryStateNxt(lottery.getState());
			
			logger.debug("Lottery with cardAppPan unlocked:" + lottery.getSerial());
			GeneralDao.Instance.saveOrUpdate(lottery);
			
		}
	}
}
