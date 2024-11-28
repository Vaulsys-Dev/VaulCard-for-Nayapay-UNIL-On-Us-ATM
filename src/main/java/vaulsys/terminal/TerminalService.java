package vaulsys.terminal;

import vaulsys.authorization.data.PolicyData;
import vaulsys.authorization.data.TerminalData;
import vaulsys.authorization.data.TerminalPolicyData;
import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.authorization.policy.Bank;
import vaulsys.authorization.policy.CycleConstraintTransactionPolicy;
import vaulsys.authorization.policy.Policy;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.SynchronizationService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementRecord;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.settlement.OnlinePerTransactionSettlementServiceImpl;
import vaulsys.clearing.settlement.OnlineSettlementService;
import vaulsys.clearing.settlement.RequestBasedSettlementServiceImpl;
import vaulsys.clearing.settlement.RequestBasedSettlementThread;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.Institution;
import vaulsys.lottery.consts.LotteryState;
import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.endpoint.EndPointType;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.EPAYTerminal;
import vaulsys.terminal.impl.KIOSKCardPresentTerminal;
import vaulsys.terminal.impl.PINPADTerminal;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.POSConfiguration;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.terminal.impl.TerminalGroup;
import vaulsys.terminal.impl.ThirdPartyVirtualTerminal;
import vaulsys.transaction.AccountingState;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.SettledState;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.transaction.TransactionType;
import vaulsys.util.ConfigUtil;
import vaulsys.util.NotUsed;
import vaulsys.util.Util;
import vaulsys.util.constants.ASCIIConstants;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.exception.LockAcquisitionException;

public class TerminalService {
	private static final Logger logger = Logger.getLogger(TerminalService.class);

	public static <T extends Terminal> T findTerminal(Class<T> clazz, Long code) {
//		if(clazz.equals(SwitchTerminal.class)){
//			return (T) GlobalContext.getInstance().getSwitchTerminal(code);
//		}
		if(clazz.equals(SwitchTerminal.class)){
			return (T) ProcessContext.get().getSwitchTerminal(code);
		}
		
		String queryString = "from " + clazz.getName() 
			+ " t left join fetch t.keySet"
			+" where t.code = :code";
		//left join fetch t.keySet left join fetch t.keySet.keysByType
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("code", code);
		return (T) GeneralDao.Instance.findObject(queryString, params);
	}
	
	public static TerminalGroup findTerminalGroup(String name) {
		String queryString = "from " + TerminalGroup.class.getName() 
		+" t where t.name = :name";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", name);
		return (TerminalGroup) GeneralDao.Instance.findObject(queryString, params);
	}
	
	public static TerminalGroup findTerminalGroup(Long id) {
		String queryString = "from " + TerminalGroup.class.getName() 
		+" t where t.id = :id";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", id);
		return (TerminalGroup) GeneralDao.Instance.findObject(queryString, params);
	}

	public static <T extends Terminal> List<T> findAllTerminals(Class<T> clazz, ClearingProfile clearingProfile) { 
		return findAllTerminals(null, clazz, clearingProfile);
	}
	
	public static <T extends Terminal> List<T> findAllTerminals(Class<T> clazz, List<Long> terminalCodes, ClearingProfile clearingProfile) { 
		return findAllTerminals(null, terminalCodes, clazz, clearingProfile);
	}
	
	public static <T extends Terminal> List<T> findAllTerminals(List<Terminal> terminals, Class<T> clazz, ClearingProfile clearingProfile) { 
		Map<String, Object> params = new HashMap<String, Object>();
		String queryString = "select t from " + clazz.getName() + " t ";
		
		if (clearingProfile != null) {
			queryString +=
				" left outer join t.sharedFeature sh " +
				" where " +
				" ((t.clearingProfile is null and sh.clearingProfile = :clearingProfile) " +
				" or " +
				" t.clearingProfile = :clearingProfile) " +
				" ";
			
			params.put("clearingProfile", clearingProfile);
		}
		
		if (terminals != null) {
			queryString += " and t in (:termList) ";
			params.put("termList", terminals);
		}
		
		return GeneralDao.Instance.find(queryString, params);
	}
	
	
	public static <T extends Terminal> List<T> findAllTerminals(List<Terminal> terminals, List<Long> terminalCodes, Class<T> clazz, ClearingProfile clearingProfile) { 
		
		Map<String, Object> params = new HashMap<String, Object>();
		String queryString = "select t from " + clazz.getName() + " t ";
		
		if (clearingProfile != null) {
			queryString +=
				" left outer join t.sharedFeature sh " +
				" where " +
				" ((t.clearingProfile is null and sh.clearingProfile = :clearingProfile) " +
				" or " +
				" t.clearingProfile = :clearingProfile) " +
				" ";
			
			params.put("clearingProfile", clearingProfile);
		}
		
		if (terminals != null && !terminals.isEmpty()) {
			queryString += " and t in (:termList) ";
			params.put("termList", terminals);
		}
		
		if (terminalCodes!=null && !terminalCodes.isEmpty()){
			queryString +=" and t.code in (:termCodes) ";
			params.put("termCodes", terminalCodes);
		}
		
		return GeneralDao.Instance.find(queryString, params);
	}
	
//	@Override
//	public <T extends Terminal> List<T> findAllTerminalsWithTrxUntilTime(Class<T> clazz, ClearingProfile clearingProfile, DateTime untilTime) {
//		return findAllTerminalsWithTrxUntilTime(null, clazz, clearingProfile, untilTime);
//	}
	
	public static <T extends Terminal> List findAllTerminalsWithTrxUntilTime(Class<T> clazz, DateTime untilTime, Boolean justToday, Integer guaranteePeriod) {
		
		String selectTermClause = "";
		String stlFlgClause = "";
		String clrFlgClause = "";
		String ifxClause = "";
		
		if(	clazz.equals(POSTerminal.class) || 
				clazz.equals(EPAYTerminal.class) ||
				clazz.equals(KIOSKCardPresentTerminal.class)) { 
			selectTermClause = "SELECT DISTINCT i.endPointTerminalCode as termid ";
			stlFlgClause = " INNER JOIN t.sourceSettleInfo as ts ";
			clrFlgClause = " INNER JOIN t.sourceClearingInfo as tc ";

			ifxClause = " and i.ifxDirection = "+IfxDirection.OUTGOING.getType();
			ifxClause += " and i.request = false ";			
		} else if(clazz.equals(ThirdPartyVirtualTerminal.class)) {
			selectTermClause = "SELECT DISTINCT i.ThirdPartyTerminalCode as termid ";
			stlFlgClause = " INNER JOIN t.thirdPartySettleInfo as ts ";
			clrFlgClause = " INNER JOIN t.sourceClearingInfo as tc ";			
			
			ifxClause = " and i.ifxDirection = "+IfxDirection.OUTGOING.getType();
			ifxClause += " and i.request = false ";
		} else {
			return findAllTerminals(clazz, null);
		}
		
		String mainQuery = 
			selectTermClause +
			" from Ifx as i " +
			" inner join i.transaction as t " +
			stlFlgClause +
			clrFlgClause +
		  	" WHERE ";
		
		
		if (justToday) {
			if(guaranteePeriod != null && guaranteePeriod < 0){
				DateTime guaranteeTime = untilTime.clone();
				guaranteeTime.increase(guaranteePeriod);

				mainQuery += " i.receivedDtLong between "+guaranteeTime.getDateTimeLong()+" and "+untilTime.getDateTimeLong();
			}else{				
				DateTime today = untilTime.clone();
				today.setDayTime(new DayTime(0, 0, 0));

				mainQuery += " i.receivedDtLong between "+today.getDateTimeLong()+" and "+untilTime.getDateTimeLong();
			}
		} else {
			DateTime untilDate2 = new DateTime(untilTime.getDayDate().nextDay(guaranteePeriod), untilTime.getDayTime());

			mainQuery += " i.receivedDtLong between "+untilDate2.getDateTimeLong()+" and "+untilTime.getDateTimeLong();
		}
		mainQuery += " and i.dummycol in (0,1,2,3,4,5,6,7,8,9)";
		mainQuery += ifxClause;

		String query1 = mainQuery + " and ts.accountingState = "+AccountingState.COUNTED.getState()+
			" and tc.clearingState in ("+ClearingState.DISAGREEMENT.getState()+", "+ClearingState.DISPUTE.getState()+", "+
			ClearingState.SUSPECTED_DISAGREEMENT.getState()+", "+ClearingState.SUSPECTED_DISPUTE.getState()+") ";
		
		String query2 = mainQuery + " and ts.accountingState = "+AccountingState.NOT_COUNTED.getState()+
			" and tc.clearingState in ("+ClearingState.NOT_CLEARED.getState()+", "+ClearingState.CLEARED.getState()+") ";

		
		List<T> result = GeneralDao.Instance.find(query1);

		result.addAll(GeneralDao.Instance.find(query2));
		
		return result;
	}

    public static <T extends Terminal> List findAllTerminalsWithTrxUntilTimeBasedOnSettlementRecord(Class<T> clazz, DateTime untilTime, Boolean justToday, Integer guaranteePeriod, ClearingProfile clearingProfile) {
		Map<String, Object> params = new HashMap<String, Object>();

		if(!(clazz.equals(POSTerminal.class) || 
				clazz.equals(EPAYTerminal.class) ||
				clazz.equals(KIOSKCardPresentTerminal.class) ||
				clazz.equals(ATMTerminal.class)
				)) {
			return findAllTerminalsWithTrxUntilTime(clazz, untilTime, justToday, guaranteePeriod);
		}
	

        String mainQuery = "select distinct sr.terminalId from SettlementRecord sr where sr.clearingProfile = :clearingProfile and ";
        params.put("clearingProfile", clearingProfile);

		if (justToday) {
			if(guaranteePeriod != null && guaranteePeriod < 0){
				DateTime guaranteeTime = untilTime.clone();
				guaranteeTime.increase(guaranteePeriod);
//				mainQuery +=  " sr.receivedDt between :guaranteeTime and :untilTime ";
				mainQuery +=  " sr.receivedDt <= :untilTime ";

//				params.put("guaranteeTime", guaranteeTime.getDateTimeLong());
				params.put("untilTime", untilTime.getDateTimeLong());
			}else{
//				mainQuery += " sr.receivedDt between :untilDateLong2 and :untilDateLong ";
				mainQuery += " sr.receivedDt <= :untilDateLong ";
				DateTime newDate = new DateTime(untilTime.getDayDate(), new DayTime(0, 0, 0));
//				params.put("untilDateLong2", newDate.getDateTimeLong());
				params.put("untilDateLong", untilTime.getDateTimeLong());
			}
		} else {
//			mainQuery +=  " sr.receivedDt between :untilDateLong2 and :untilDateLong ";
			mainQuery +=  " sr.receivedDt <= :untilDateLong ";

			params.put("untilDateLong", untilTime.getDateTimeLong());
//			DateTime untilDate2 = new DateTime(untilTime.getDayDate().nextDay(guaranteePeriod), untilTime.getDayTime());
//			params.put("untilDateLong2", untilDate2.getDateTimeLong());
		}
		
		List<T> result = GeneralDao.Instance.find(mainQuery, params);
		
		return result;
	}

	
	private static <T extends Terminal> TerminalType getTerminalTypeByClass(Class<T> clazz) {
		if (clazz.equals(ATMTerminal.class))
			return TerminalType.ATM;
		
		if (clazz.equals(POSTerminal.class))
			return TerminalType.POS;
		
		if (clazz.equals(EPAYTerminal.class))
			return TerminalType.INTERNET;
		
		if (clazz.equals(ThirdPartyVirtualTerminal.class))
			return TerminalType.THIRDPARTY;
		
		if (clazz.equals(KIOSKCardPresentTerminal.class))
			return TerminalType.KIOSK_CARD_PRESENT;
		
		return TerminalType.UNKNOWN;
	}
	
	public static <T extends Terminal> List<T> findAllTerminalsWithTrxUntilTime(List<Terminal> terminals, List<Long> terminalCodes, Class<T> clazz, ClearingProfile clearingProfile) {
		if( !clazz.equals(ATMTerminal.class) && 
				!clazz.equals(POSTerminal.class) && 
				!clazz.equals(EPAYTerminal.class) &&
				!clazz.equals(KIOSKCardPresentTerminal.class) &&
				!clazz.equals(ThirdPartyVirtualTerminal.class)) { 
			//We cannot get info from ifx, so get terminal by simple query
			return findAllTerminals( clazz, terminalCodes, clearingProfile);
		}

		Map<String, Object> params = new HashMap<String, Object>();
		String queryString = "select distinct t from " + clazz.getName() + " t " +
				" left outer join t.sharedFeature sh  " +
				" where " +
				" ((t.clearingProfile is null and sh.clearingProfile = :clearingProfile) " +
				" or " +
				" t.clearingProfile = :clearingProfile) " ;
		
//		if (terminalCodes!= null && terminalCodes.size() >0){
			queryString +=" and t.code in (:termCodes) ";
			params.put("termCodes", terminalCodes);
//		}
		if (terminals != null && terminals.size() > 0) {
			queryString += " and t in (:termList) ";
			params.put("termList", terminals);
		}
		
		params.put("clearingProfile", clearingProfile);
		return GeneralDao.Instance.find(queryString, params);
	}
	
	public static <T extends Terminal> List<T> findAllTerminalsWithTrxUntilTimeBasedOnSettlementRecord(List<Terminal> terminals, List<Long> terminalCodes, Class<T> clazz, ClearingProfile clearingProfile) {
		if( !clazz.equals(ATMTerminal.class) && 
				!clazz.equals(POSTerminal.class) && 
				!clazz.equals(EPAYTerminal.class) &&
				!clazz.equals(KIOSKCardPresentTerminal.class) &&
				!clazz.equals(ThirdPartyVirtualTerminal.class)) { 
			//We cannot get info from ifx, so get terminal by simple query
			return findAllTerminals( clazz, terminalCodes, clearingProfile);
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		String queryString = "select distinct sr.terminal from " + SettlementRecord.class.getName() + " sr " +
//		" left outer join t.sharedFeature sh  " +
		" where " +
		" sr.clearingProfile = :clearingProfile " +
		" and sr.terminalId in (:termCodes) ";

		params.put("termCodes", terminalCodes);
		params.put("clearingProfile", clearingProfile);
		
		if (terminals != null && terminals.size() > 0) {
			queryString += " and sr.terminal in (:termList) ";
			params.put("termList", terminals);
		}
		
		return GeneralDao.Instance.find(queryString, params);
	}

	@NotUsed
    public static List<TerminalGroup> findTerminalGroupHierarchy(Terminal terminal) {
        List<TerminalGroup> terminalGroups = new ArrayList<TerminalGroup>();
        TerminalGroup terminalGroup = terminal.getParentGroup();
        while (terminalGroup != null) {
//            terminalGroup = GeneralDao.Instance.reload(terminalGroup);
            terminalGroups.add(terminalGroup);
            terminalGroup = terminalGroup.getParentGroup();
        }
        return terminalGroups;
    }

    public static Terminal getMatchingTerminal(Ifx ifx) throws AuthorizationException {
		Long terminalCode = Long.valueOf(ifx.getTerminalId());
		Class<? extends Terminal> termType = Terminal.class;
		if(ifx.getTerminalType() != null){
			if(!TerminalType.INTERNET.equals(ifx.getTerminalType()))
				termType = ifx.getTerminalType().getClassType();
		}
		return findTerminal(termType, terminalCode);
	}
        
	public static Terminal findEndpointTerminal(Message message, Ifx ifx, EndPointType incomingEndPointType) {
		if (ISOFinalMessageType.isResponseMessage(/*message.getIfx()*/ifx.getIfxType())) {
			return findEndpointTerminalForResponse(message, ifx, incomingEndPointType);
		} else {
			return findEndpointTerminalForRequest(message, ifx, incomingEndPointType);
		}
	}

	private static Terminal findEndpointTerminalForRequest(Message message, Ifx ifx, EndPointType incomingEndPointType) {
		Terminal endPointTerminal = null;
		EndPointType endPointType = message.getChannel().getEndPointType();
		
		
		if (!EndPointType.isSwitchTerminal(incomingEndPointType)) {
			if (message.isIncomingMessage()) {
				Long terminalId = Long.valueOf(/*message.getIfx()*/ifx.getTerminalId());
				endPointTerminal = findTerminal(endPointType.getClassType(), terminalId);
			} else if (message.isOutgoingMessage()) {
				if (EndPointType.isSwitchTerminal(endPointType)) {
					String institutionCode = message.getChannel().getInstitutionId();
					Institution institution = FinancialEntityService.findEntity(Institution.class, institutionCode);
//					endPointTerminal = FinancialEntityService.getIssuerSwitchTerminal(institution);
					endPointTerminal = ProcessContext.get().getIssuerSwitchTerminal(institution);
				} else {
					endPointTerminal = message.getTransaction().getIncomingIfxOrMessageEndpoint();
					message.setNeedToBeInstantlyReversed(false);
				}
			}
		} else {
			if (EndPointType.isSwitchTerminal(endPointType)) {

				String institutionCode = message.getChannel().getInstitutionId();
				Institution institution = FinancialEntityService.findEntity(Institution.class, institutionCode);
				
//				if (GlobalContext.getInstance().getMyInstitution().getRole().equals(FinancialEntityRole.MY_SELF_INTERMEDIATE)){
				if (ProcessContext.get().getMyInstitution().getRole().equals(FinancialEntityRole.MY_SELF_INTERMEDIATE)){
					if (message.isIncomingMessage()) {
						if (institution.getCode().equals(/*message.getIfx()*/ifx.getDestBankId())) {
//							endPointTerminal = FinancialEntityService.getIssuerSwitchTerminal(institution);
							endPointTerminal = ProcessContext.get().getIssuerSwitchTerminal(institution);
						} else
//							endPointTerminal = FinancialEntityService.getAcquireSwitchTerminal(institution);
							endPointTerminal = ProcessContext.get().getAcquireSwitchTerminal(institution);
					}else {
						if (institution.getCode().equals(/*message.getIfx()*/ifx.getBankId())) {
//							endPointTerminal = FinancialEntityService.getAcquireSwitchTerminal(institution);
							endPointTerminal = ProcessContext.get().getAcquireSwitchTerminal(institution);
						} else
//							endPointTerminal = FinancialEntityService.getIssuerSwitchTerminal(institution);
							endPointTerminal = ProcessContext.get().getIssuerSwitchTerminal(institution);
					}
					
//				} else if (GlobalContext.getInstance().getMyInstitution().getCode().equals(/*message.getIfx()*/ifx.getDestBankId())) {
				} else if (ProcessContext.get().getMyInstitution().getCode().equals(/*message.getIfx()*/ifx.getDestBankId())) {
//					endPointTerminal = FinancialEntityService.getAcquireSwitchTerminal(institution);
					endPointTerminal = ProcessContext.get().getAcquireSwitchTerminal(institution);
					
				} else if (institution.getCode().equals(/*message.getIfx()*/ifx.getDestBankId())) {
//					endPointTerminal = FinancialEntityService.getIssuerSwitchTerminal(institution);
					endPointTerminal = ProcessContext.get().getIssuerSwitchTerminal(institution);
					
				} else if (institution.getCode().equals(institution.getBin())) {
//					endPointTerminal = FinancialEntityService.getIssuerSwitchTerminal(institution);
					endPointTerminal = ProcessContext.get().getIssuerSwitchTerminal(institution);
					
				} else {
//					endPointTerminal = FinancialEntityService.getAcquireSwitchTerminal(institution);
					endPointTerminal = ProcessContext.get().getAcquireSwitchTerminal(institution);
				}
			} else /*if (!message.getChannel().getEndPointType().equals(EndPointType.SWITCH_TERMINAL))*/ {
				Long terminalId = Long.valueOf(/*message.getIfx()*/ifx.getTerminalId());
				endPointTerminal = findTerminal(endPointType.getClassType(), terminalId);
			}
		}
		
		return endPointTerminal;
	}

	private static Terminal findEndpointTerminalForResponse(Message message, Ifx ifx, EndPointType incomingEndPointType) {
		Terminal endPointTerminal = null;
		Message refMessages = null;

		if (message.isIncomingMessage()) {

			if (!EndPointType.isSwitchTerminal(incomingEndPointType)) {
				Long terminalId = Long.valueOf(/*message.getIfx()*/ifx.getTerminalId());
				endPointTerminal = findTerminal(incomingEndPointType.getClassType(), terminalId);

			} else if (EndPointType.isSwitchTerminal(incomingEndPointType)) {

				String institutionCode = message.getChannel().getInstitutionId();
				Institution institution = FinancialEntityService.findEntity(Institution.class, institutionCode);

//				Institution myInstitution = GlobalContext.getInstance().getMyInstitution();
				Institution myInstitution = ProcessContext.get().getMyInstitution();
				if (myInstitution.getCode().equals(/*message.getIfx()*/ifx.getDestBankId())) {
					if (myInstitution.getCode().equals(/*message.getIfx()*/ifx.getBankId())
						&& !TerminalType.INTERNET.equals(/*message.getIfx()*/ifx.getTerminalType())) {
//						endPointTerminal = FinancialEntityService.getIssuerSwitchTerminal(myInstitution);
						endPointTerminal = ProcessContext.get().getIssuerSwitchTerminal(myInstitution);
					} else 
//						endPointTerminal = FinancialEntityService.getAcquireSwitchTerminal(institution);
						endPointTerminal = ProcessContext.get().getAcquireSwitchTerminal(institution);

				} else if (institution.getCode().equals(/*message.getIfx()*/ifx.getDestBankId())) {
//					endPointTerminal = FinancialEntityService.getIssuerSwitchTerminal(institution);
					endPointTerminal = ProcessContext.get().getIssuerSwitchTerminal(institution);
					
				} else if (institution.getCode().equals(institution.getBin())) {
//					endPointTerminal = FinancialEntityService.getIssuerSwitchTerminal(institution);
					endPointTerminal = ProcessContext.get().getIssuerSwitchTerminal(institution);
					
				} else {
//					endPointTerminal = FinancialEntityService.getAcquireSwitchTerminal(institution);
					endPointTerminal = ProcessContext.get().getAcquireSwitchTerminal(institution);
				}
			}
			// refMessages =
			// message.getTransaction().getFirstTransaction().getOutputMessage();

		} else if (message.isOutgoingMessage()) {
			refMessages = message.getTransaction().getFirstTransaction().getInputMessage();

//			Ifx ifx = message.getIfx();

			IfxType ifxType = ifx.getIfxType();
//			Long myBin = GlobalContext.getInstance().getMyInstitution().getBin();
			Long myBin = ProcessContext.get().getMyInstitution().getBin();
			
//			if (!FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(GlobalContext.getInstance().getMyInstitution().getRole())){
			if (!FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(ProcessContext.get().getMyInstitution().getRole())){
			if ((IfxType.TRANSFER_RS.equals(ifxType) ||
						IfxType.TRANSFER_REV_REPEAT_RS.equals(ifxType)||
						IfxType.TRANSFER_CARD_TO_ACCOUNT_RS.equals(ifxType)||
						IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RS.equals(ifxType)) 						
						&& (ifx.getDestBankId().equals(myBin) ^ ifx.getRecvBankId().equals(myBin))){
					refMessages = message.getTransaction().getReferenceTransaction().getInputMessage();
				}
			}
			endPointTerminal = refMessages.getEndPointTerminal();
		}
		
		return endPointTerminal; 
	}

	public static Terminal findEndpointTerminalForExceptionMessage(Message message) {
		Terminal terminal = null;
		EndPointType endPointType = message.getChannel().getEndPointType();
		
		if (EndPointType.isSwitchTerminal(endPointType)) {
			String institutionCode = message.getChannel().getInstitutionId();
			Institution institution = FinancialEntityService.findEntity(Institution.class, institutionCode);

//			if (GlobalContext.getInstance().getMyInstitution().getBin().equals(message.getIfx().getDestBankId())) {
			if (ProcessContext.get().getMyInstitution().getBin().equals(message.getIfx().getDestBankId())) {
//				terminal = FinancialEntityService.getAcquireSwitchTerminal(institution);
				terminal = ProcessContext.get().getAcquireSwitchTerminal(institution);
			} else if (institution.getCode().equals(message.getIfx().getDestBankId())) {
//				terminal = FinancialEntityService.getIssuerSwitchTerminal(institution);
				terminal = ProcessContext.get().getIssuerSwitchTerminal(institution);
			} else {
//				terminal = FinancialEntityService.getAcquireSwitchTerminal(institution);
				terminal = ProcessContext.get().getAcquireSwitchTerminal(institution);
			}
		} else {
			Long terminalCode = Long.valueOf(message.getIfx().getTerminalId());
			terminal = findTerminal(endPointType.getClassType(), terminalCode);
		}
		return terminal;
	}

	public static Terminal findEndpointTerminalForMessageWithoutIFX(Message message, Long endpointCode) {
		Terminal endPointTerminal = null;
		EndPointType endPointType = message.getChannel().getEndPointType();

		if (EndPointType.isSwitchTerminal(endPointType)) {
			Institution institution = FinancialEntityService.findEntity(Institution.class, message.getChannel().getInstitutionId());
//			endPointTerminal = FinancialEntityService.getAcquireSwitchTerminal(institution);
			//System.out.println("Institution ID [" + institution.getCode() + "]"); //Raza TEMP
			endPointTerminal = ProcessContext.get().getAcquireSwitchTerminal(institution);
		} else {
			if (endpointCode == null) {
				throw new UnsupportedOperationException("Invalid Originator");
			}
			Long terminalCode = endpointCode;
			endPointTerminal = findTerminal(endPointType.getClassType(), terminalCode);
		}
		return endPointTerminal;
	}
	
	@NotUsed
	//used only in other mains
	public static List<ClearingProfile> getGroupingClearingProfile(List<Terminal> terminals) {
		List<ClearingProfile> result = new ArrayList<ClearingProfile>();
		String query = "select " 
			+ "d.clearingProfile from " 
			+ Terminal.class.getName() + " d "
			+ " where "
			+ " d in (:termList) group by d.clearingProfile";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("termList", terminals);
		return GeneralDao.Instance.find(query, params);
	}
	
	public static void removeKeySet(Terminal terminal) {
		if(terminal == null)
			return;
		
		Set<SecureKey> keySet = terminal.getKeySet();
		if (keySet!= null) {
			for (SecureKey key : keySet) {
                if(terminal instanceof KIOSKCardPresentTerminal && "TMK".equals(key.getKeyType()))
                    continue;
				GeneralDao.Instance.delete(key);
			}
			terminal.removeKeySet();
		}
	}

	@NotUsed
	public List<TerminalGroup> getTerminalGroupHierarchy(Terminal terminal){
		TerminalGroup tg = terminal.getParentGroup();
		List<TerminalGroup> groups = new ArrayList<TerminalGroup>();
		if (tg == null)
			return groups;
		while(tg!= null){
			groups.add(tg);
			tg = tg.getParentGroup();
		}
		return groups;
	}
	
	public static Object[] getPolicyTerminalData(Terminal terminal, Policy policy){
		String queryString = "select d, tpd, p from TerminalData d,"
			+ " TerminalPolicyData tpd, " 
			+ policy.getClass().getName() + " p "
//			+ " CycleConstraintTransactionPolicy p"
			+ " where p.id = tpd.policy.id "
			+ " and tpd.termianlData[:terminal].id = d.id "
			+ " and p.id = :id ";
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("terminal", terminal);
		params.put("id", policy.getId());
		
		List<Object[]> list = GeneralDao.Instance.find(queryString, params, "d", LockMode.UPGRADE);
		
		return (list.size()>0)? list.get(0):null; 
//		return (list.size()>0)?(TerminalData) list.get(0):null; 
	}
	//--------------Moosavi: Task 50617 : Add New Policy for max card amount for Currency ATM------------------
	public static Object[] getPolicyCardData(String appPan, Policy policy){
		String queryString = "select d, cpd, p from CardData d,"
				+ " CardPolicyData cpd, " 
				+ policy.getClass().getName() + " p "
//				+ " CycleConstraintTransactionPolicy p"
				+ " where p.id = cpd.policy.id "
				+ " and cpd.cardData.id = d.id "
				+ " and p.id = :id "
				+ " and d.appPAN = :appPan ";
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("appPan", appPan);
			params.put("id", policy.getId());
			
			List<Object[]> list = GeneralDao.Instance.find(queryString, params, "d", LockMode.UPGRADE);
			
			return (list.size()>0)? list.get(0):null; 
//			return (list.size()>0)?(TerminalData) list.get(0):null; 
//			return (list.size()>0)?(TerminalData) list.get(0):null; 

	}
	//-------------------------------------------------------------------------------------------------------
	
	public static void setLastIncomingTransaction(Terminal endPointTerminal, Message incomingMessage, Ifx incomingIfx) {
		try {
			Transaction lastTransaction = incomingMessage.getTransaction();
			IfxType ifxType = /*incomingMessage.getIfx()*/incomingIfx.getIfxType();
		
			if (ISOFinalMessageType.isRequestMessage(ifxType)) {
				
				/**
				 * @author p.moosavi
				 * Check last incoming message
				 */

/*				Transaction lastIncomingtransaction=endPointTerminal.getLastIncomingTransaction();
				Ifx lastIncomingIfx=lastIncomingtransaction.getIncomingIfx();
				
				
				logger.error("POS: " + endPointTerminal.getCode() + ", incoming trnSeqCntr: " +  incomingIfx.getSrc_TrnSeqCntr() + ", last incoming trnSeqCntr: " + lastIncomingIfx.getSrc_TrnSeqCntr());
				if( incomingIfx.getSrc_TrnSeqCntr().compareTo(lastIncomingIfx.getSrc_TrnSeqCntr())<=0 )
				{
					throw new AuthorizationException("POS: " + endPointTerminal.getCode() + ", incoming trnSeqCntr: " +  incomingIfx.getSrc_TrnSeqCntr() + ", last Incoming trnSeqCntr: " + lastIncomingIfx.getSrc_TrnSeqCntr(), false);
				}
				else*/
				endPointTerminal.setLastIncomingTransaction(lastTransaction);
				
				if (lastTransaction.getId() != null)
					logger.info("set last incoming transaction of Terminal(" + endPointTerminal.getCode().toString() + "): "+ lastTransaction.getId());
				else 
					logger.info("set last incoming transaction of Terminal(" + endPointTerminal.getCode().toString() + "): NULL!");
			}
			
		} catch(Exception e) {
			logger.error(e.getClass().getSimpleName()+": "+ e.getMessage(), e);
		}
	}
	
	public static void setLastTransaction(Terminal endPointTerminal, Message outgoingMessage) {
		
		try {
			Transaction lastTransaction = outgoingMessage.getTransaction();
			IfxType ifxType = outgoingMessage.getIfx().getIfxType();
			if (TerminalType.ATM.equals(endPointTerminal.getTerminalType())) {
//				endPointTerminal = findTerminal(ATMTerminal.class, endPointTerminal.getCode());
				try {

					
					if (lastTransaction.getInputMessage() == null ||
							lastTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/ == null) {
						logger.info("last transaction is special, don't set last transaction..");
					} else
					if ((ISOFinalMessageType.isResponseMessage(ifxType)
							&& ISOFinalMessageType.isATMMessage(ifxType)
//							&& !ShetabFinalMessageType.isReversalRsMessage(ifxType)
							) 
						|| IfxType.CASH_HANDLER_RESPONSE.equals(ifxType)
						|| IfxType.PREPARE_BILL_PMT.equals(ifxType)
//						|| IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT.equals(ifxType)
						|| ISOFinalMessageType.isPrepareTranferCardToAccountMessage(ifxType)
						|| IfxType.PREPARE_THIRD_PARTY_PURCHASE.equals(ifxType)
						|| IfxType.PREPARE_WITHDRAWAL.equals(ifxType)) {	//Mirkamali(Task179): Currency ATM
						
						endPointTerminal.setLastTransaction(lastTransaction);
						
						if (lastTransaction.getId() != null)
							logger.info("set last transaction of ATM(" + endPointTerminal.getCode().toString() + "): "+ lastTransaction.getId());
						
						else 
							logger.info("set last transaction of ATM(" + endPointTerminal.getCode().toString() + "): NULL!");
					}
				} catch (Exception e) {
					logger.warn("No Valid Last Transcation for ATM: " + endPointTerminal.getCode());
				}
				
				((ATMTerminal)endPointTerminal).setLastRealTransaction(lastTransaction);
				if (lastTransaction.getId() != null)
					logger.info("set real last transaction of ATM(" + endPointTerminal.getCode().toString() + "): "+ lastTransaction.getId());
				
				else 
					logger.info("set real last transaction of ATM(" + endPointTerminal.getCode().toString() + "): NULL!");
				
			} 
			/*else if (IfxType.LOG_ON_RQ.equals(ifxType) || IfxType.LOG_ON_RS.equals(ifxType)) {
				logger.info("last transaction is LOG_ON, don't set last transaction");
				
			}*/
			else if (IfxType.ACQUIRER_REC_RS.equals(ifxType) || IfxType.ACQUIRER_REC_RQ.equals(ifxType)){
				logger.info("last transaction is ACQUIRER_REC, don't set last transaction");
			}
			else {
				endPointTerminal.setLastTransaction(lastTransaction);
				if (lastTransaction.getId() != null)
					logger.info("set last transaction of Terminal(" + endPointTerminal.getCode().toString() + "): "+ lastTransaction.getId());
				else 
					logger.info("set last transaction of Terminal(" + endPointTerminal.getCode().toString() + "): NULL!");
			}
		} catch (Exception e) {
			logger.error(e.getClass().getSimpleName()+": "+ e.getMessage(), e);
		}
	}
	
	public static byte[] generalInfotechField48Rs(Ifx ifx, EncodingConvertor convertor, Terminal t) {
		ByteArrayOutputStream finalBytes = new ByteArrayOutputStream();
		
		POSConfiguration conf = null;
		POSTerminal pos = null;
		if (t instanceof POSTerminal) {
			pos = (POSTerminal) t;
			conf = pos.getOwnOrParentConfiguration();
		}
		
		String posConfigStr = "";
		if(conf != null){
			posConfigStr = conf.getCardholderHeader() + " " + conf.getCardholderFooter() + " "  + 
					conf.getMerchantHeader() + " " + conf.getMerchantFooter();
		}
		
		posConfigStr = posConfigStr.toLowerCase();
		
		try {
//			if (ConfigUtil.getBoolean(ConfigUtil.INFOTECH_HAS_DAILY_MESSAGE)) {
			if(posConfigStr.contains("dailymsg") || (conf == null && ConfigUtil.getBoolean(ConfigUtil.INFOTECH_HAS_DAILY_MESSAGE))){
				String dailyMessage = t.getDailyMessage();
				if (Util.hasText(dailyMessage)) {
					finalBytes.write(convertor.encode(dailyMessage));
				} else {
					finalBytes.write(convertor.encode("روز خوشی داشته باشید"));
				}
			}
		finalBytes.write(ASCIIConstants.FS);
		
		
		/***** lottery for infotech pos(cardHolder's receipt) *****/
		if (ifx.getLotteryData() != null &&
				LotteryState.ASSIGNED.equals(ifx.getLotteryStateNxt())) {

			finalBytes.write(convertor.encode("شما برنده کارت با شماره مرجع"));
			finalBytes.write(convertor.encode(ifx.getLottery().getSerial().toString()));
			finalBytes.write(convertor.encode(" به مبلغ "));
			finalBytes.write(convertor.encode(ifx.getLottery().getCredit().toString()));
			finalBytes.write(convertor.encode(" ريال میباشيد"));
		}else if (ifx.getLotteryData() != null 
					&& LotteryState.NOT_ASSIGNED.equals(ifx.getLotteryStateNxt())
					&& LotteryState.ASSIGNED.equals(ifx.getLotteryStatePrv()) 
				&& ISOFinalMessageType.isReturnMessage(ifx.getIfxType())) {
			finalBytes.write(convertor.encode("دارنده کارت گرامی! به علت برگشت تراکنش کارت جایزه شما فاقد اعتبار است"));
		}
		/****** end lottery(cardHolder's receipt) ******/

		finalBytes.write(ASCIIConstants.FS);

		finalBytes.write(ASCIIConstants.FS);

		if (ifx.getDestBankId() != null) {
			Bank bank = ProcessContext.get().getBank(Util.integerValueOf(ifx.getDestBankId()));
			if(bank != null && Util.hasText(bank.getName()))
				finalBytes.write(convertor.encode(bank.getName()));
		}
		finalBytes.write(ASCIIConstants.FS);

		if (ConfigUtil.getBoolean(ConfigUtil.INFOTECH_HAS_CARD_PRODUCT)) {
			if(ifx.getAppPAN() != null && (ifx.getAppPAN().length() == 16 || ifx.getAppPAN().length() == 19)){
				int cardProductCode = Integer.parseInt(ifx.getAppPAN().substring(6,7));
				String product = GlobalContext.getInstance().getCardProductType(cardProductCode);
				if(Util.hasText(product))
					finalBytes.write(convertor.encode(product));
			}
		} else {
			finalBytes.write(convertor.encode(ConfigUtil.getProperty(ConfigUtil.INFOTECH_SPECIAL_CHAR)));
		}
		finalBytes.write(ASCIIConstants.FS);

//		if (ConfigUtil.getBoolean(ConfigUtil.INFOTECH_HAS_NAME)) {
		if(posConfigStr.contains("ifx.name") || (conf == null && ConfigUtil.getBoolean(ConfigUtil.INFOTECH_HAS_NAME))){
			finalBytes.write(convertor.encode(t.getOwner().getName()));
		} else {
			finalBytes.write(convertor.encode(ConfigUtil.getProperty(ConfigUtil.INFOTECH_SPECIAL_CHAR)));
		}
		finalBytes.write(ASCIIConstants.FS);

//		if (ConfigUtil.getBoolean(ConfigUtil.INFOTECH_HAS_ADDRESS)) {
		if(posConfigStr.contains("safeaddress") || (conf == null && ConfigUtil.getBoolean(ConfigUtil.INFOTECH_HAS_ADDRESS))){
			finalBytes.write(convertor.encode(t.getOwner().getSafeAddress()));
		} else {
			finalBytes.write(convertor.encode(ConfigUtil.getProperty(ConfigUtil.INFOTECH_SPECIAL_CHAR)));
		}
		finalBytes.write(ASCIIConstants.FS);

//		if (ConfigUtil.getBoolean(ConfigUtil.INFOTECH_HAS_PHONE_NUMBER)) {
		if(posConfigStr.contains("safephonenumber") || (conf == null && ConfigUtil.getBoolean(ConfigUtil.INFOTECH_HAS_PHONE_NUMBER))){
			String fullPhoneNum = t.getOwner().getSafeFullPhoneNumber();
			if (Util.hasText(fullPhoneNum))
				finalBytes.write(convertor.encode(fullPhoneNum));
		} else {
			finalBytes.write(convertor.encode(ConfigUtil.getProperty(ConfigUtil.INFOTECH_SPECIAL_CHAR)));
		}
		finalBytes.write(ASCIIConstants.FS);
		
//		if (ConfigUtil.getBoolean(ConfigUtil.INFOTECH_HAS_WEB_SITE_ADDRESS)) {
		if (posConfigStr.contains("safewebsiteaddress") || (conf == null && ConfigUtil.getBoolean(ConfigUtil.INFOTECH_HAS_WEB_SITE_ADDRESS))){
			String website = t.getOwner().getSafeWebsiteAddress();
			if (website != null && website.length()>0)
					finalBytes.write(website.getBytes());
		} else {
			finalBytes.write(convertor.encode(ConfigUtil.getProperty(ConfigUtil.INFOTECH_SPECIAL_CHAR)));
		}
		finalBytes.write(ASCIIConstants.FS);

		if (ConfigUtil.getBoolean(ConfigUtil.INFOTECH_HAS_FOOTER)) {
			finalBytes.write("www.bpi.ir".getBytes());
		} else {
			finalBytes.write(convertor.encode(ConfigUtil.getProperty(ConfigUtil.INFOTECH_SPECIAL_CHAR)));
		}
		finalBytes.write(ASCIIConstants.FS);
		
		if (pos != null) {
			if (Util.hasText(pos.getDescription())) {
				finalBytes.write(convertor.encode(pos.getDescription()));
			}
		}
		finalBytes.write(ASCIIConstants.FS);
		
		} catch (Exception e) {
			logger.error("Encoutering an Exception in filling field_48: "+ e.getClass().getSimpleName()+":"+ e.getMessage(), e);
			ifx.setSeverity(Severity.ERROR);
			ifx.setStatusDesc("Encoutering an Exception in filling field_48: "+ e.getClass().getSimpleName()+":"+ e.getMessage());
			return null;
		}
		return finalBytes.toByteArray();
	}
	
	
	public static boolean isOriginatorSwitchTerminal(Message message) {
		Channel channel = message.getChannel();
		Ifx ifx = message.getIfx();
		
		try {
			if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())) {
				if (TerminalType.SWITCH.equals(ifx.getEndPointTerminal().getTerminalType()) &&
						!EndPointType.EPAY_SWITCH_TERMINAL.equals(channel.getEndPointType()) &&
						!EndPointType.DEPENDENT_SWITCH_TERMINAL.equals(channel.getEndPointType()))
					return true;
				else 
					return false;
				
			} else if (ISOFinalMessageType.isResponseMessage(ifx.getIfxType())) {
				Transaction refTrx = message.getTransaction().getFirstTransaction();
				
				if (IfxType.TRANSFER_TO_ACCOUNT_RS.equals(ifx.getIfxType())) {
					refTrx = message.getTransaction().getReferenceTransaction();
				}
				
				if (TransactionType.SELF_GENERATED.equals(refTrx.getTransactionType())) {
					refTrx = refTrx.getReferenceTransaction();
					
					/*** online settlement ***/
					if (refTrx == null) 
						return true;
				}
				Message origInMessage = refTrx.getInputMessage();
				Channel origChannel = origInMessage.getChannel();
				Ifx origIfx = origInMessage.getIfx();
				
				/*** reverse online settlement ***/
				if (origIfx == null)
					return true;
				
				if (TerminalType.SWITCH.equals(origIfx.getEndPointTerminal().getTerminalType()) &&
						!EndPointType.EPAY_SWITCH_TERMINAL.equals(origChannel.getEndPointType()))
					return true;
				else 
					return false;
				
			} else {
				if (TerminalType.SWITCH.equals(ifx.getEndPointTerminal().getTerminalType()) &&
						!EndPointType.EPAY_SWITCH_TERMINAL.equals(channel.getEndPointType()))
					return true;
				else 
					return false;
			}
		} catch(Exception e) {
			logger.warn("exception in getting originator terminal, return switch terminal!");
			return true;
		}
	}
	
	public static boolean isNeedToSetSettleDate(Transaction transaction) {
		logger.debug("isNeedToSetSettleDate is starting...");
		
		Ifx outgoingIFX = transaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/;
		Channel inputChannel = null;
		Channel outputChannel = null;
		try {
			inputChannel = transaction.getInputMessage().getChannel();
			outputChannel = transaction.getOutputMessage().getChannel();
			
		} catch(Exception e) {
			logger.info("Exception in getting in/out channel");
			return true;
		}
		
		if (inputChannel == null || outputChannel == null)
			return true;
		
		if (ISOFinalMessageType.isRequestMessage(outgoingIFX.getIfxType())) {
			if (TerminalType.SWITCH.equals(transaction.getIncomingIfxOrMessageEndpoint())
					&& !inputChannel.getEndPointType().equals(EndPointType.EPAY_SWITCH_TERMINAL)) {

				if (inputChannel.getInstitutionId().equals(outgoingIFX.getBankId()) ||
						outputChannel.getInstitutionId().equals(outgoingIFX.getDestBankId())) {

					if ((inputChannel.getMasterDependant() && !outputChannel.getMasterDependant()) ||
							(!inputChannel.getMasterDependant() && outputChannel.getMasterDependant())) {
						logger.info("input msg of trx: " + transaction.getId() + " is RQ from SWITCH as MASTER_DEPENDANT: forward settleDate");
						return false;
						
					} else {
						logger.info("input msg of trx: " + transaction.getId() + " is RQ from SWITCH as !MASTER_DEPENDANT: set settleDate");
						return true;
					}
				} else {
					logger.warn("incoming message must be acquiere message!");
					return true;
				}
			} else {
				logger.info("input msg of trx: " + transaction.getId() + " is RQ from !SWITCH: set settleDate");
				return true;

			}
		} else {
			if (TerminalType.SWITCH.equals(transaction.getIncomingIfxOrMessageEndpoint().getTerminalType())
					&& !inputChannel.getEndPointType().equals(EndPointType.EPAY_SWITCH_TERMINAL)) {

				if (inputChannel.getInstitutionId().equals(outgoingIFX.getDestBankId()) ||
						outputChannel.getInstitutionId().equals(outgoingIFX.getBankId())) {
					
					if ((inputChannel.getMasterDependant() && !outputChannel.getMasterDependant()) ||
							(!inputChannel.getMasterDependant() && outputChannel.getMasterDependant())) {
						logger.info("input msg of trx: " + transaction.getId() + " is RS from SWITCH as MASTER_DEPENDANT: forward settleDate");
						return false;

					} else {
						logger.info("input msg of trx: " + transaction.getId() + " is RS from SWITCH as !MASTER_DEPENDANT: set settleDate");
						return true;
					}
				} else {
					logger.warn("incoming message must be issuer message!");
					return true;
				}
			} else {
				logger.info("input msg of trx: " + transaction.getId() + " is RS from !SWITCH: set settleDate");
				return true;

			}
		}
	}
	
	// TASK Task136 [26143] - Change Mojodi Pazirande query Pasargad
	public static long getSumOfUnsettledFlags(Terminal terminal) throws Exception {
        long sum = 0;
        
        if (TerminalType.POS.equals(terminal.getTerminalType())) {
            Class settlementClass = terminal.getOwnOrParentClearingProfile().getSettlementClass();
            if (OnlinePerTransactionSettlementServiceImpl.class.equals(settlementClass) ||
                            OnlineSettlementService.class.equals(settlementClass) ||
                            RequestBasedSettlementServiceImpl.class.equals(settlementClass)) {
                    return sum;
            }
		}
		
		List<SettledState> stlList = new ArrayList<SettledState>();
		stlList.add(SettledState.NOT_SETTLED);
		stlList.add(SettledState.SENT_FOR_SETTLEMENT);
		
		List<ClearingState> clrList = new ArrayList<ClearingState>();
		clrList.add(ClearingState.CLEARED);
		clrList.add(ClearingState.NOT_CLEARED);
		
		List<IfxType> ifxTypes = new ArrayList<IfxType>();
		ifxTypes.add(IfxType.PURCHASE_RS);
		ifxTypes.add(IfxType.RETURN_REV_REPEAT_RS);
		//ifxTypes.add(IfxType.RETURN_REV_RS);
		
		// Task136 [26143] - Change Mojodi Pazirande query Pasargad
		List<TrnType> trnTypes = new ArrayList<TrnType>();
		trnTypes.add(TrnType.PURCHASE);
		trnTypes.add(TrnType.RETURN);
		
		//ClearingProfile clrProf = ProcessContext.get().getClearingProfile(terminal.getOwnOrParentClearingProfileId());
		DateTime settlementTime = new DateTime(DayDate.now(), new DayTime(0, 0, 0));
		//DateTime settlementTime = new DateTime(DayDate.now().nextDay(clrProf.getSettleGuaranteeDay()), new DayTime(0, 0, 0));
		
		// Task136 [26143] - Change Mojodi Pazirande query Pasargad
		Long sumOfTrnxAmnt = TransactionService.findPosTransactions(terminal.getCode(), stlList, clrList, ifxTypes, trnTypes, settlementTime);
		
        if (sumOfTrnxAmnt!= null)
        	sum += sumOfTrnxAmnt;
        
        ifxTypes.clear();
        ifxTypes.add(IfxType.RETURN_RS);
        ifxTypes.add(IfxType.PURCHASE_REV_REPEAT_RS);
//        ifxTypes.add(IfxType.PURCHASE_REV_RS);
		// Task136 [26143] - Change Mojodi Pazirande query Pasargad
		trnTypes.clear();
		trnTypes.add(TrnType.RETURN);
		trnTypes.add(TrnType.PURCHASE);
        
        sumOfTrnxAmnt = 0L;
		// Task136 [26143] - Change Mojodi Pazirande query Pasargad
        sumOfTrnxAmnt = TransactionService.findPosTransactions(terminal.getCode(), stlList, clrList, ifxTypes, trnTypes, settlementTime);
        if (sumOfTrnxAmnt!= null)
        	sum -= sumOfTrnxAmnt;
        return sum;
    }

    public static void lockTerminal(String terminalCode, LockMode lockMode) {
        if (LockMode.UPGRADE.equals(lockMode))
            lockTerminalUpGradeMode(terminalCode);
        else {
            String queryString = "select code from "+ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA)+".term_terminal where code = :code ";

            if (LockMode.UPGRADE_NOWAIT.equals(lockMode)) {
                queryString += " for update nowait ";
            } else {
                queryString += " for update ";
            }

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("code", terminalCode);
            GeneralDao.Instance.executeSqlQuery(queryString, params);
        }
    }

    public static void lockTerminalUpGradeMode(String terminalCode) {
        int retryCnt = 0;
        while (true) {
            try {
                String queryString = "select code from "+ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA)+".term_terminal where code = :code for update nowait ";
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("code", terminalCode);
                GeneralDao.Instance.executeSqlQuery(queryString, params);
                break;
            } catch (HibernateException e) {
                //logger.error(e);
                if (e instanceof LockAcquisitionException && retryCnt < ConfigUtil.getInteger(ConfigUtil.DB_UPGRADE_LOCK_MAX_RETRY_COUNT).intValue()) {
                    try{
                        Thread.sleep(ConfigUtil.getInteger(ConfigUtil.DB_UPGRADE_LOCK_WAIT));
                    }catch(Exception ee){
                        throw e;
                    }
                    retryCnt++;
                    continue;
                } else {
                    throw e;
                }
            }
        }
    }

	public static void createRequestBasedSettlementThread(Terminal terminal) {
		logger.debug("start create RequestBased Settlement Thread...");
		GeneralDao.Instance.endTransaction();
		
		GeneralDao.Instance.beginTransaction();
		GeneralDao.Instance.refresh(terminal);
		
		RequestBasedSettlementThread thread = new RequestBasedSettlementThread((POSTerminal) terminal);
		Thread settlementThread = new Thread(thread);
		logger.debug("Thread: " + settlementThread.getName() + " is starting...");
		settlementThread.start();
	}
	
	public static boolean hasRequestBasedClearingProfile(Terminal terminal) {
		if (TerminalType.POS.equals(terminal.getTerminalType())) {
			if (terminal.getOwnOrParentClearingProfile().getSettlementClass().equals(RequestBasedSettlementServiceImpl.class)) {
//				if (terminal.getOwnOrParentClearingProfileId().equals(170606L)) {
				return true;
			} else 
				return false;
		} else 
			return false;
	}
	
	public static void synchObject(Terminal terminal, Boolean waitForSyncObject) {
		Class<? extends Terminal> classType = terminal.getTerminalType().getClassType();
		
		if (TerminalType.ATM.equals(terminal.getTerminalType())) {
			if (waitForSyncObject.equals(true)) {
				SynchronizationService.getSynchornizationObject((ATMTerminal) terminal, classType, LockMode.UPGRADE);
			} else {
				SynchronizationService.getSynchornizationObject((ATMTerminal) terminal, classType, LockMode.UPGRADE_NOWAIT);
			}
			
		} else if (TerminalType.POS.equals(terminal.getTerminalType())) {
			if (waitForSyncObject.equals(true)) {
				SynchronizationService.getSynchornizationObject((POSTerminal) terminal, classType, LockMode.UPGRADE);
			} else {
				SynchronizationService.getSynchornizationObject((POSTerminal) terminal, classType, LockMode.UPGRADE_NOWAIT);
			}
			
		} else if (TerminalType.PINPAD.equals(terminal.getTerminalType())) {
			if (waitForSyncObject.equals(true)) {
				SynchronizationService.getSynchornizationObject((PINPADTerminal) terminal, classType, LockMode.UPGRADE);
			} else {
				SynchronizationService.getSynchornizationObject((PINPADTerminal) terminal, classType, LockMode.UPGRADE_NOWAIT);
			}
			
		} else if (TerminalType.KIOSK_CARD_PRESENT.equals(terminal.getTerminalType())) {
			if (waitForSyncObject.equals(true)) {
				SynchronizationService.getSynchornizationObject((KIOSKCardPresentTerminal) terminal, classType, LockMode.UPGRADE);
			} else {
				SynchronizationService.getSynchornizationObject((KIOSKCardPresentTerminal) terminal, classType, LockMode.UPGRADE_NOWAIT);
			}
		}
		
		
		SynchronizationService.lock(terminal, classType);
	}
}	

