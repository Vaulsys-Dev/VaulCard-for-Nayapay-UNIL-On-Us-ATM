package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.report.ReportGenerator;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.entity.impl.Organization;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.terminal.impl.ThirdPartyVirtualTerminal;
import vaulsys.thirdparty.consts.ThirdPartyType;
import vaulsys.util.ConfigUtil;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class BillPaymentSettlementServiceImpl extends SettlementService {
	private static final Logger logger = Logger.getLogger(BillPaymentSettlementServiceImpl.class);
	
	protected BillPaymentSettlementServiceImpl(){}
	
	public static final BillPaymentSettlementServiceImpl Instance = new BillPaymentSettlementServiceImpl();

	@Override
	public boolean isDesiredOwnerForPreprocessing(FinancialEntity entity) {
		return FinancialEntityRole.ORGANIZATION.equals(entity.getRole()) && 
			ThirdPartyType.BILLPAYMENT.equals(((Organization)entity).getType().findThirdpartyType());
	}
	
	@Override
	public List<Terminal> findAllTerminals(List<Terminal> terminals, List<Long> termCodes, ClearingProfile clearingProfile) {
		if (terminals == null)
			terminals = new ArrayList<Terminal>();
		List<ThirdPartyVirtualTerminal> thirdPartyTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(terminals, termCodes, ThirdPartyVirtualTerminal.class, clearingProfile);
		if (thirdPartyTerminals != null && thirdPartyTerminals.size() > 0)
			terminals.addAll(thirdPartyTerminals);
		return terminals;
	}
	
	@Override
	List<Long> findDesiredTerminalCodes(DateTime accountUntilTime, Boolean justToday, ClearingProfile clearingProfile) {
		List<Long> terminals = new ArrayList<Long>();
		Integer guaranteePeriod = 0;
		if(justToday){
			guaranteePeriod = clearingProfile.getAccountingGuaranteeMinute();
		}else{
			guaranteePeriod = clearingProfile.getSettleGuaranteeDay();			
		}
		List<Long> thirdPartyTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(ThirdPartyVirtualTerminal.class, accountUntilTime, justToday, guaranteePeriod);
		if (thirdPartyTerminals != null && thirdPartyTerminals.size() > 0)
			terminals.addAll(thirdPartyTerminals);
		return terminals;
	}
	
	@Override
	public List<Terminal> findAllTerminals(List<Terminal> terminals, ClearingProfile clearingProfile) {
		if (terminals == null)
			terminals = new ArrayList<Terminal>();
		List<ThirdPartyVirtualTerminal> thirdPartyTerminals = TerminalService.findAllTerminals(ThirdPartyVirtualTerminal.class, clearingProfile);
		if (thirdPartyTerminals != null && thirdPartyTerminals.size() > 0)
			terminals.addAll(thirdPartyTerminals);
		return terminals;
	}
	
	@Override
	public void generateDesiredSettlementReports(ClearingProfile clearingProfile, /*List<Terminal> terminals, */DateTime settleDate) throws Exception {
		logger.info("Generating ThirdParty Settlement Report...");
		try { 
			String ip = null;
			
			try {
				ip = ConfigUtil.getProperty(ConfigUtil.SMB_IP);
			} catch (Exception e) {
				logger.error("Exception in getting ip property  " + e.getCause());
			}
			
			ReportGenerator.generateThirdPartySettlementReport(clearingProfile, settleDate, getThirdPartyType(), ip, "billpayment");
		} catch (Exception e) {
			logger.error("Exception in Generating ThirdParty Settlement Report  " + e.getCause());
			logger.error("Exception in Generating ThirdParty Settlement Report  " + e);
		}
	}
	
	@Override
	public List<String> getSrcDest() {
		List<String> result = new ArrayList<String>();
		result.add("thirdParty");
		return result;
	}
	
	@Override
	public String getSettlementTypeDesc() {
		return "قبوض";
	}
	
	ThirdPartyType getThirdPartyType() {
		return ThirdPartyType.BILLPAYMENT;
	}
}
