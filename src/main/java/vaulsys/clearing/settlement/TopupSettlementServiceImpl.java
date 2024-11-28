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
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class TopupSettlementServiceImpl  extends SettlementService{
	private static final Logger logger = Logger.getLogger(TopupSettlementServiceImpl.class);
	
	private TopupSettlementServiceImpl(){
	}
	
	public static final TopupSettlementServiceImpl Instance = new TopupSettlementServiceImpl();
	
	@Override
	public List<String> getSrcDest() {
		List<String> result = new ArrayList<String>();
		result.add("thirdParty");
		return result;
	}

	@Override
	public void generateDesiredSettlementReports(ClearingProfile clearingProfile, DateTime settleDate) throws Exception {
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
	public String getSettlementTypeDesc() {
		// TODO Auto-generated method stub
		return "شارژ تاپ آپ";
	}

	@Override
	boolean isDesiredOwnerForPreprocessing(FinancialEntity entity) {
		return FinancialEntityRole.ORGANIZATION.equals(entity.getRole()) && 
				ThirdPartyType.CHARGE.equals(((Organization)entity).getType().findThirdpartyType());
	}

	@Override
	List<Terminal> findAllTerminals(List<Terminal> terminals, ClearingProfile clearingProfile) {
		if (terminals == null)
			terminals = new ArrayList<Terminal>();
		List<ThirdPartyVirtualTerminal> thirdPartyTerminals = TerminalService.findAllTerminals(ThirdPartyVirtualTerminal.class, clearingProfile);
		if (thirdPartyTerminals != null && thirdPartyTerminals.size() > 0)
			terminals.addAll(thirdPartyTerminals);
		return terminals;
	}

	@Override
	List<Terminal> findAllTerminals(List<Terminal> terminals, List<Long> termCodes, ClearingProfile clearingProfile) {
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
		List<Terminal> thirdPartyTerminals = findAllTerminals(null, clearingProfile);
		if (thirdPartyTerminals != null && thirdPartyTerminals.size() > 0){
			for(Terminal trdTerm : thirdPartyTerminals){
				terminals.add(trdTerm.getCode());
			}
		}
		return terminals;
	}
	
	ThirdPartyType getThirdPartyType() {
		return ThirdPartyType.CHARGE;
	}
}
