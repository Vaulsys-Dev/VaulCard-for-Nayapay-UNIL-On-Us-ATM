package vaulsys.lottery.impl;

import vaulsys.lottery.Lottery;
import vaulsys.lottery.LotteryAssignmentPolicy;
import vaulsys.lottery.LotteryService;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.SwitchApplication;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

@Entity
@DiscriminatorValue(value = "general_lottery_policy")
public class GeneralLotteryAssignmentPolicy extends LotteryAssignmentPolicy/*<EmptyLotteryPolicyData>*/{

	@Transient
	private transient Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public Lottery getLottery(Ifx ifx) throws Exception {
		Lottery lottery = null;
		
		try {
			List<Long> credit = calculate(ifx);
			if (credit == null || credit.size() == 0) {
				logger.info("No Credit Assigned to ifx: " + ifx.getId());
				return null;
			}
			lottery = LotteryService.getLottery(ifx, credit, this);
			logger.info("Credit " + (lottery != null ? lottery.getCredit() : "null") + " Assigned to ifx: " + ifx.getId());
		} catch (Exception e) {
			logger.info("No Lottery Assigned to ifx: " + ifx.getId());
			logger.info(e);
			return null;
		}
		return lottery;
	}

	@Override
	public Lottery update(Ifx ifx) throws Exception {
		return null;
	}
}
