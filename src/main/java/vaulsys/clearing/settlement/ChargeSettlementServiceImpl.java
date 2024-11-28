package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.consts.SettlementDataType;
import vaulsys.clearing.report.ReportGenerator;
import vaulsys.customer.Account;
import vaulsys.customer.AccountType;
import vaulsys.customer.Core;
import vaulsys.customer.CustomerService;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.entity.impl.Organization;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.terminal.impl.ThirdPartyVirtualTerminal;
import vaulsys.thirdparty.consts.ThirdPartyType;
import vaulsys.util.ConfigUtil;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class ChargeSettlementServiceImpl extends SettlementService {
	private static final Logger logger = Logger.getLogger(ChargeSettlementServiceImpl.class);
	
	private ChargeSettlementServiceImpl(){}
	
	public static final ChargeSettlementServiceImpl Instance = new ChargeSettlementServiceImpl();

	@Override
	public boolean isDesiredOwnerForPreprocessing(FinancialEntity entity) {
		return FinancialEntityRole.ORGANIZATION.equals(entity.getRole()) && 
			ThirdPartyType.CHARGE.equals(((Organization)entity).getType().findThirdpartyType());
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
//		Integer guaranteePeriod = 0;
//		if(justToday){
//			guaranteePeriod = clearingProfile.getAccountingGuaranteeMinute();
//		}else{
//			guaranteePeriod = clearingProfile.getSettleGuaranteeDay();			
//		}
//		List<Long> thirdPartyTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(ThirdPartyVirtualTerminal.class, accountUntilTime, justToday, guaranteePeriod);
//		if (thirdPartyTerminals != null && thirdPartyTerminals.size() > 0)
//			terminals.addAll(thirdPartyTerminals);
//		return terminals;
		List<Terminal> thirdPartyTerminals = findAllTerminals(null, clearingProfile);
		if (thirdPartyTerminals != null && thirdPartyTerminals.size() > 0){
			for(Terminal trdTerm : thirdPartyTerminals){
				terminals.add(trdTerm.getCode());
			}
		}
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
			
			ReportGenerator.generateThirdPartySettlementReport(clearingProfile, settleDate, getThirdPartyType(), ip, "charge");
		} catch (Exception e) {
			logger.error("Exception in Generating ThirdParty Settlement Report  " + e.getCause());
			logger.error("Exception in Generating ThirdParty Settlement Report  " + e);
		}
	}
	
	@Override
	public Account getAccount(SettlementData settlementData) {
		FinancialEntity entity = settlementData.getFinancialEntity();
		if (SettlementDataType.SECOND.equals(settlementData.getType()) && 
				FinancialEntityRole.ORGANIZATION.equals(entity.getRole()) &&
				entity.getCode().equals(9935L)) {
			if (ProcessContext.get().getMyInstitution().getBin().equals(502229L))
				return new Account("ايرانسل", "219,8100,21176,4", CustomerService.findCurrency(364), Core.FANAP_CORE, AccountType.DEPOSIT);
			else if (ProcessContext.get().getMyInstitution().getBin().equals(502908L))
				return new Account("ايرانسل", "313,311,670842,2", CustomerService.findCurrency(364), Core.FANAP_CORE, AccountType.DEPOSIT);
		}

		//TODO: Leila check it!
		return entity.getOwnOrParentAccount();
	}
	
	@Override
	public List<String> getSrcDest() {
		List<String> result = new ArrayList<String>();
		result.add("thirdParty");
		return result;
	}
	
	@Override
	public String getSettlementTypeDesc() {
		return "شارژ";
	}
	
//	@Override
//	String getSettlementStateDesc() {
//		return "پرداخت شارژ ";
//	}
//	
//	@Override
//	String getSettlementStateReportTitle() {
//		return "گزارش مبالغ شارژ ";
//	}
//	
//	@Override
//	String getFinalSettlementStateDocDesc() {
//		return "پرداخت شارژ ";
//	}
	
	ThirdPartyType getThirdPartyType() {
		return ThirdPartyType.CHARGE;
	}
}
