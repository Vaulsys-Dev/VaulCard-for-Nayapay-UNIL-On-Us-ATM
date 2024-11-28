package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.Terminal;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class ATMCurrencySettlementServiceImpl extends SettlementService {
	
	private static final Logger logger = Logger.getLogger(ATMCurrencySettlementServiceImpl.class);
	
	private ATMCurrencySettlementServiceImpl(){}
	
	public static final ATMCurrencySettlementServiceImpl Instance = new ATMCurrencySettlementServiceImpl();
	
	@Override
	public List<String> getSrcDest() {
		List<String> result = new ArrayList<String>();
		result.add("branch");
		return result;
	}

	@Override
	public void generateDesiredSettlementReports(ClearingProfile clearingProfile, DateTime settleDate) throws Exception {
		logger.info("No report in necessary for Currency ATM");
	}

	@Override
	public String getSettlementTypeDesc() {
		return "برداشت ارزی";
	}

	@Override
	boolean isDesiredOwnerForPreprocessing(FinancialEntity entity) {
		return FinancialEntityRole.BRANCH.equals(entity.getRole());
	}

	@Override
	List<Terminal> findAllTerminals(List<Terminal> terminals, ClearingProfile clearingProfile) {
		if (terminals == null)
			terminals = new ArrayList<Terminal>();
		List<ATMTerminal> atmTerminals = TerminalService.findAllTerminals(ATMTerminal.class, clearingProfile);
		if (atmTerminals != null && atmTerminals.size() > 0)
			terminals.addAll(atmTerminals);
		return terminals;
	}

	@Override
	List<Terminal> findAllTerminals(List<Terminal> terminals, List<Long> termCodes, ClearingProfile clearingProfile) {
		if (terminals == null)
			terminals = new ArrayList<Terminal>();
		List<ATMTerminal> atmTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(terminals, termCodes, ATMTerminal.class, clearingProfile);
		if (atmTerminals != null && atmTerminals.size() > 0)
			terminals.addAll(atmTerminals);
		return terminals;
	}

	@Override
	List<Long> findDesiredTerminalCodes(DateTime accountUntilTime, Boolean justToday, ClearingProfile clearingProfile) {
		List<Long> terminals = new ArrayList<Long>();
		List<Terminal> atmTerminals = findAllTerminals(null, clearingProfile);
		if (atmTerminals != null && atmTerminals.size() > 0){
			for(Terminal trdTerm : atmTerminals){
				terminals.add(trdTerm.getCode());
			}
		}
		return terminals;
	}

}
