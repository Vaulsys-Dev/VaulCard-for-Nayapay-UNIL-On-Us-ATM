package vaulsys.clearing;

import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.consts.CriteriaData;
import vaulsys.clearing.consts.SettlementDataCriteria;
import vaulsys.clearing.consts.SettlementDataType;
import vaulsys.clearing.settlement.*;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.AccountingState;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.SettledState;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.NotUsed;
import vaulsys.wfe.ProcessContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClearingService {
	@NotUsed
	//only in other mains
	public static ClearingProfile findClearingProfile(String name) {
		String query = "from " + ClearingProfile.class.getName() + " f where f.name = :name";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("name", name);
		return (ClearingProfile) GeneralDao.Instance.findObject(query, param);
	}

	public static ClearingProfile findClearingProfile(Serializable clearingProfileID) {
		String query = "from " + ClearingProfile.class.getName() + " f where f.id = :id";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("id", clearingProfileID);
		return (ClearingProfile) GeneralDao.Instance.findObject(query, param);
	}
	
	public static ClearingProfile findClearingProfile(Class settlementClass) {
		String query = "from " + ClearingProfile.class.getName() + " f where f.settlementClass = :settlementClass";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("settlementClass", settlementClass);
		return (ClearingProfile) GeneralDao.Instance.findObject(query, param);
	}

	@NotUsed
	public List<ClearingProfile> findAllClearingProfile() {
		String query = "from " + ClearingProfile.class.getName();
		return GeneralDao.Instance.find(query);
	}

	public static List<SettlementDataType> getSettlementDataTypes(ClearingProfile clearingProfile) {
		List<SettlementDataType> result = new ArrayList<SettlementDataType>();
		Set<SettlementDataCriteria> criterias = clearingProfile.getCriterias();
		if (criterias != null) {
			for (SettlementDataCriteria criteria : criterias) {
				SettlementDataType type = criteria.getType();
				if (!result.contains(type))
					result.add(type);
			}
		}
		return result;
	}

	public static SettlementDataCriteria getSettlementDataCriteria(ClearingProfile clearingProfile, SettlementDataType type) {
		Set<SettlementDataCriteria> criterias = clearingProfile.getCriterias();
		if (criterias != null && type != null) {
			for (SettlementDataCriteria criteria : criterias) {
				if (criteria.getType().equals(type))
					return criteria;
			}
		}
		return null;
	}

	public static List<CriteriaData> getSettlementDataCriteria(ClearingProfile clearingProfile, SettlementDataType type, Class criteriaName) {
		List<CriteriaData> list = new ArrayList<CriteriaData>();
		Set<SettlementDataCriteria> criterias = clearingProfile.getCriterias();
		if (criterias != null && type != null) {
			for (SettlementDataCriteria criteria : criterias) {
				if (criteria.getType().equals(type)) {
					for (CriteriaData criteriaData : criteria.getCriteriaDatas())
						if (criteriaData.getCriteriaName().equals(criteriaName))
							list.add(criteriaData);
				}
			}
		}

		return list;
	}

	public static String addCriteriaQuery(Map<String, Object> parameters,
			Terminal terminal, Class criteriaName, List<Object> criteriaValues) {
		String query = "";
		String srcDest = TransactionService.findSrcDest(terminal);
		if (ClearingState.class.equals(criteriaName)) {
			if (!TerminalType.THIRDPARTY.equals(terminal.getTerminalType())) {
				query += " and t."
						+ srcDest
						+ "ClearingInfo.clearingState.state in (:toBeClrState) ";
				parameters.put("toBeClrState", criteriaValues);
			} else {
				query += " and t.sourceClearingInfo.clearingState.state in (:toBeClrState) ";
				parameters.put("toBeClrState", criteriaValues);
			}
		}

		if (TerminalType.class.equals(criteriaName)) {
			query += " and i.networkTrnInfo.TerminalType.code in (:termTypes) ";
			parameters.put("termTypes", criteriaValues);
		}

		if (TrnType.class.equals(criteriaName)) {
			query += " and i.trnType.type in (:toBeClrTrx) ";
			parameters.put("toBeClrTrx", criteriaValues);
		}

		if (IfxType.class.equals(criteriaName)) {
			query += " and i.ifxType.type in (:toBeClrIfxType) ";
			parameters.put("toBeClrIfxType", criteriaValues);
		}

		if (AccountingState.class.equals(criteriaName)) {
			query += " and t." + srcDest
					+ "SettleInfo.accountingState = :accState";
			parameters.put("accState", criteriaValues);
		}

		return query;
	}

	public static SettlementDataType isNeedToSettleTransaction(ClearingProfile clearingProfile, Terminal terminal, Transaction transaction) {
		if (terminal.getTerminalType().equals(TerminalType.POS) &&
				ISOFinalMessageType.isPurchaseMessage(transaction.getOutgoingIfx().getIfxType()) &&
				transaction.getSourceClearingInfo().getClearingState().equals(ClearingState.CLEARED)
				)
			
			return SettlementDataType.MAIN;
		
		return null;
//		List<SettlementDataType> types = ClearingService.getSettlementDataTypes(clearingProfile);
//		
//		for (SettlementDataType type: types) {
//			SettlementDataCriteria criteria = ClearingService.getSettlementDataCriteria(clearingProfile, type);
//			
//			if (criteria==null || criteria.getCriteriaDatas()== null || criteria.getCriteriaDatas().isEmpty()){
////				logger.info("There is no criteria data for settlementDataType: "+ clearingProfile.getId()+"."+ type);
//				continue;
//			}
//
//			Map<Class, List<Object>> criteriaNameValues = ClearingService.getSeperateCriteriaByName(criteria.getCriteriaDatas());
//			Set<Class> keySet = criteriaNameValues.keySet();
//			
//			
//			
//			String query = "";
//			String srcDest = TransactionService.findSrcDest(terminal);
//			
//			for (Class criteriaName: keySet) {
//				if (ClearingState.class.equals(criteriaName)) {
//					for (Object obj: criteriaNameValues.get(criteriaName)) {
//						if (!TerminalType.THIRDPARTY.equals(terminal.getTerminalType())) {
//							
//							if (srcDest.equals("source")) {
//								if (transaction.getSourceClearingInfo().getClearingState().equals(obj)) {
//									break;
//								}
//							} else if (srcDest.equals("destination")) {
//								if (transaction.getDestinationClearingInfo().getClearingState().equals(obj)) {
//									break;
//								}
//							}
//							
////							query += " and t."
////									+ srcDest
////									+ "ClearingInfo.clearingState.state in (:toBeClrState) ";
////							parameters.put("toBeClrState", criteriaValues);
//							
//						} else {
//							if (transaction.getSourceClearingInfo().getClearingState().equals(obj)) {
//								break;
//							}
//
////							query += " and t.sourceClearingInfo.clearingState.state in (:toBeClrState) ";
////							parameters.put("toBeClrState", criteriaValues);
//						}
//					}
//				}
//	
//				if (TerminalType.class.equals(criteriaName)) {
//					query += " and i.networkTrnInfo.TerminalType.code in (:termTypes) ";
//					parameters.put("termTypes", criteriaValues);
//				}
//	
//				if (TrnType.class.equals(criteriaName)) {
//					query += " and i.trnType.type in (:toBeClrTrx) ";
//					parameters.put("toBeClrTrx", criteriaValues);
//				}
//	
//				if (IfxType.class.equals(criteriaName)) {
//					query += " and i.ifxType.type in (:toBeClrIfxType) ";
//					parameters.put("toBeClrIfxType", criteriaValues);
//				}
//	
//				if (AccountingState.class.equals(criteriaName)) {
//					query += " and t." + srcDest
//							+ "SettleInfo.accountingState = :accState";
//					parameters.put("accState", criteriaValues);
//				}
//			}
//
//		}
	}
	
	public static Map<Class, List<Object>> getSeperateCriteriaByName(Set<CriteriaData> criterias) {
		Map<Class, List<Object>> result = new HashMap<Class, List<Object>>();
		for (CriteriaData criteria : criterias) {
			List<Object> values = result.get(criteria.getCriteriaName());
			if (values == null)
				values = new ArrayList<Object>();
			values.add(criteria.getCriteriaValue());
			result.put(criteria.getCriteriaName(), values);
		}
		return result;
	}

	public static String getDocDesc(SettlementData settlementData) {
		if (settlementData == null || settlementData.getClearingProfile() == null)
			return "";
		SettlementDataCriteria dataCriteria = getSettlementDataCriteria(
				settlementData.getClearingProfile(), settlementData.getType());
		if (dataCriteria == null)
			return "";
		return dataCriteria.getDocDesc();
	}

	public static SettlementService getSettlementService(ClearingProfile clearingProfile) {
		Class settlementClass = clearingProfile.getSettlementClass();
		if (MerchantSettlementServiceImpl.class.equals(settlementClass))
			return MerchantSettlementServiceImpl.Instance;
		if (BillPaymentSettlementServiceImpl.class.equals(settlementClass))
			return BillPaymentSettlementServiceImpl.Instance;
		if (MCIBillPaymentSettlementServiceImpl.class.equals(settlementClass))
			return MCIBillPaymentSettlementServiceImpl.Instance;
		if (ChargeSettlementServiceImpl.class.equals(settlementClass))
			return ChargeSettlementServiceImpl.Instance;
		if (InstitutionSettlementServiceImpl.class.equals(settlementClass))
			return InstitutionSettlementServiceImpl.Instance;
		if (PEPSettlementServiceImpl.class.equals(settlementClass))
			return PEPSettlementServiceImpl.Instance;
		if (ATMSettlementServiceImpl.class.equals(settlementClass))
			return ATMSettlementServiceImpl.Instance;
		if (ATMDailySettlementServiceImpl.class.equals(settlementClass))
			return ATMDailySettlementServiceImpl.Instance;
		if (ATMDailyRecordSettlementServiceImpl.class.equals(settlementClass))
			return ATMDailyRecordSettlementServiceImpl.Instance;
		if (OnlineSettlementService.class.equals(settlementClass))
			return OnlineSettlementService.Instance;
		if (RequestBasedSettlementServiceImpl.class.equals(settlementClass))
			return RequestBasedSettlementServiceImpl.Instance;
		if (SaderatSettlementServiceImpl.class.equals(settlementClass))
			return SaderatSettlementServiceImpl.Instance;
		if (PerTransactionSettlementServiceImpl.class.equals(settlementClass))
			return PerTransactionSettlementServiceImpl.Instance;
		if (PerTransactionOnlineBillSettlementServiceImpl.class.equals(settlementClass))
			return PerTransactionOnlineBillSettlementServiceImpl.Instance;
		if (OnlinePerTransactionSettlementServiceImpl.class.equals(settlementClass))
			return OnlinePerTransactionSettlementServiceImpl.Instance;
		if (ThirdPartySettlementServiceImpl.class.equals(settlementClass))
			return ThirdPartySettlementServiceImpl.Instance;
		if (PerTransactionBillPaymentSettlementServiceImpl.class.equals(settlementClass))
			return PerTransactionBillPaymentSettlementServiceImpl.Instance;
		if (PerTransactionThirdPartySettlementServiceImpl.class.equals(settlementClass))
			return PerTransactionThirdPartySettlementServiceImpl.Instance;
		if(SeveralPerDaySettlementServiceImpl.class.equals(settlementClass))
			return SeveralPerDaySettlementServiceImpl.Instance;
		if(SeveralPerDayPerTrxSettlementServiceImpl.class.equals(settlementClass))
			return SeveralPerDayPerTrxSettlementServiceImpl.Instance;
        if(PerTransactionKioskBillPaymentSettlementServiceImpl.class.equals(settlementClass))
            return PerTransactionKioskBillPaymentSettlementServiceImpl.Instance;
        if(ATMCurrencySettlementServiceImpl.class.equals(settlementClass))	//Mirkamali(Task179):> Currency ATM
        	return ATMCurrencySettlementServiceImpl.Instance;

		return null;
	}
	
	public static SettledState getSettledStateAfterAccounting(ClearingProfile clearingProfile, Boolean settleTime){
		Class settlementClass = clearingProfile.getSettlementClass();
		if (OnlineSettlementService.class.equals(settlementClass) ||
				OnlinePerTransactionSettlementServiceImpl.class.equals(settlementClass))
			return SettledState.SENT_FOR_SETTLEMENT;
		
		if (RequestBasedSettlementServiceImpl.class.equals(settlementClass) && Boolean.TRUE.equals(settleTime))
			return SettledState.SENT_FOR_SETTLEMENT;
		
		return null;
	}

	@NotUsed
	public Boolean needOnlineSettlement(Terminal terminal, Ifx ifx) {
		ClearingProfile clearingProfile = ProcessContext.get().getClearingProfile(terminal.getOwnOrParentClearingProfileId());
		if (clearingProfile.getSettlementClass().equals(OnlineSettlementService.class)) {
			List<SettlementDataType> types = getSettlementDataTypes(clearingProfile);
			for (SettlementDataType type : types) {
				List<CriteriaData> criteria = ClearingService.getSettlementDataCriteria(clearingProfile, type, TrnType.class);

				for (CriteriaData c : criteria) {
					if (c.getCriteriaValue() == ifx.getTrnType().getType())
						return true;
				}
			}
		}
		return false;
	}
}
