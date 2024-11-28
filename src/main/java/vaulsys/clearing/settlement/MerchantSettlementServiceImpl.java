package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.EPAYTerminal;
import vaulsys.terminal.impl.KIOSKCardPresentTerminal;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class MerchantSettlementServiceImpl extends SettlementService {
	private static final Logger logger = Logger.getLogger(MerchantSettlementServiceImpl.class);
	
	private MerchantSettlementServiceImpl(){}
	
	public static final MerchantSettlementServiceImpl Instance = new MerchantSettlementServiceImpl();

	@Override
	public List<Terminal> findAllTerminals(List<Terminal> terminals, List<Long> termCodes, ClearingProfile clearingProfile) {
		if (terminals == null)
			terminals = new ArrayList<Terminal>();
		List<POSTerminal> posTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(terminals, termCodes, POSTerminal.class, clearingProfile);
		List<EPAYTerminal> epayTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(terminals, termCodes, EPAYTerminal.class, clearingProfile);
		List<KIOSKCardPresentTerminal> kioskTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(terminals, termCodes, KIOSKCardPresentTerminal.class, clearingProfile);
		if (posTerminals != null && posTerminals.size() > 0)
			terminals.addAll(posTerminals);
		if (epayTerminals != null && epayTerminals.size() > 0)
			terminals.addAll(epayTerminals);
		if (kioskTerminals != null && kioskTerminals.size() > 0)
			terminals.addAll(kioskTerminals);

		
		return terminals;
	}
	
	@Override
	List<String> findDesiredTerminalCodes(DateTime accountUntilTime, Boolean justToday, ClearingProfile clearingProfile) {
		List<String> terminals = new ArrayList<String>();
		
		Integer guaranteePeriod = 0;
		if(justToday){
			guaranteePeriod = clearingProfile.getAccountingGuaranteeMinute();
		}else{
			guaranteePeriod = clearingProfile.getSettleGuaranteeDay();			
		}
		
		List<String> posTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(POSTerminal.class, accountUntilTime, justToday, guaranteePeriod);
		List<String> kioskTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(KIOSKCardPresentTerminal.class, accountUntilTime, justToday, guaranteePeriod);
//		List<String> epayTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(EPAYTerminal.class, accountUntilTime, justToday);

		if (posTerminals != null && posTerminals.size() > 0)
			terminals.addAll(posTerminals);
		if (kioskTerminals != null && kioskTerminals.size() > 0)
			terminals.addAll(kioskTerminals);
//		if (epayTerminals != null && epayTerminals.size() > 0)
//			terminals.addAll(epayTerminals);
		return terminals;
	}

	@Override
	public List<Terminal> findAllTerminals(List<Terminal> terminals, ClearingProfile clearingProfile) {
		if (terminals == null)
			terminals = new ArrayList<Terminal>();
		List<POSTerminal> posTerminals = TerminalService.findAllTerminals(POSTerminal.class, clearingProfile);
		List<EPAYTerminal> epayTerminals = TerminalService.findAllTerminals(EPAYTerminal.class, clearingProfile);
		List<KIOSKCardPresentTerminal> kioskTerminals = TerminalService.findAllTerminals(KIOSKCardPresentTerminal.class, clearingProfile);
		if (posTerminals != null && posTerminals.size() > 0)
			terminals.addAll(posTerminals);
		if (epayTerminals != null && epayTerminals.size() > 0)
			terminals.addAll(epayTerminals);
		if (kioskTerminals != null && kioskTerminals.size() > 0)
			terminals.addAll(kioskTerminals);

		return terminals;
		
	}
	
	@Override
	public void generateDesiredSettlementReports(ClearingProfile clearingProfile, /*List<Terminal> terminals, */DateTime settleDate) throws Exception {
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean isDesiredOwnerForPreprocessing(FinancialEntity entity) {
		return (FinancialEntityRole.SHOP.equals(entity.getRole()) ||
				FinancialEntityRole.MERCHANT.equals(entity.getRole()));
	}
	
	@Override
	public List<String> getSrcDest() {
		List<String> result = new ArrayList<String>();
		result.add("source");
		return result;
	}
	
	@Override
	public String getSettlementTypeDesc() {
		return "پذيرندگان";
	}
}
