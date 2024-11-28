package vaulsys.clearing.settlement;

import vaulsys.config.ConfigurationManager;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;

public class CoreConfigDataManager {

	public static String CoreUrl = "CoreUrl";
	public static String CoreUserName = "CoreUserName";
	public static String CorePassword = "CorePassword";
	public static String CoreFakePass = "CoreFPass";
	public static String SwitchBranchId = "SwitchBranchId";
	
	public static String ShetabEODDocumentTitle = "ShetabEODDocumentTitle";
	public static String FanapEODDocumentTitle = "FanapEODDocumentTitle";
	public static String NeginEODDocumentTitle = "NeginEODDocumentTitle";
	public static String ShetabDebitDocument = "ShetabDebitDocument";
	public static String ShetabCreditDocument = "ShetabCreditDocument";
	public static String NeginDebitDocument = "NeginDebitDocument";
	public static String NeginCreditDocument = "NeginCreditDocument";
	public static String ToFanapDocumentTitle = "ToFanapDocumentTitle";
	public static String NeginTransactions = "NeginTransactions";
	public static String ShetabTransactions = "ShetabTransactions";
	public static String FanapMerchantDocumentTitle = "FanapMerchantDocumentTitle";
	public static String NeginMerchantDocumentTitle = "NeginMerchantDocumentTitle";
	public static String NeginMerchantDocumentItemTitle1 = "NeginMerchantDocumentItemTitle1";
	public static String NeginMerchantDocumentItemTitle2 = "NeginMerchantDocumentItemTitle2";
	public static String ShetabFeeReportEODDocumentTitle = "ShetabFeeReportEODDocumentTitle";
	public static String ShetabReportEODDocumentTitle = "ShetabReportEODDocumentTitle";
	
	public static String CBIAccount = "CBIAccount";
	public static String CBIPaidFeeAccount = "CBIPaidFeeAccount";
	public static String CBIReceivedFeeAccount = "CBIReceivedFeeAccount";
	public static String CBIDisagreementAccount = "CBIDisagreementAccount";
	
	public static String OnUsATMReturnedDocumentAccount = "OnUsATMReturnedDocumentAccount";
	
	public static String shetabExtraAccount = "shetabExtraAccount";
	
	public static String SwitchAccountForFanap = "SwitchAccountForFanap";
	public static String FanapTopicCodes = "FanapTopicCodes";
	public static String SwitchAccountForShetab = "SwitchAccountForShetab";
	public static String ShetabTopicCodes = "ShetabTopicCodes";
	public static String SwitchAccountForNegin = "SwitchAccountForNegin";
	public static String NeginTopicCodes = "NeginTopicCodes";
	public static String SwitchSpecialAccounts = "SwitchSpecialAccounts";
	public static String ShetabFeeTopicCodes = "ShetabFeeTopicCodes";
	public static String ShetabFeeMinAmount = "ShetabFeeMinAmount";
	
	public static String ShaparakSettlementMainTopic = "ShaparakSettlementMainTopic";
	public static String ShaparakSettlementTopicCodes = "ShaparakSettlementTopicCodes";
	

	private static Map<String, String> configData;

	static {
		loadConfiguration();
	}

	public static void loadConfiguration() {
		Configuration config = ConfigurationManager.getInstance().getConfiguration("core-config");
		String[] variables = config.getStringArray("field/@name");
		if (variables != null && variables.length > 0) {
			configData = new HashMap<String, String>();
			for (String var : variables) {
				configData.put(var, config.getString("field[@name='" + var + "']/value"));
			}
		}
	}

	public static String getValue(String variableName) {
		if (configData == null)
			return null;
		return configData.get(variableName);
	}
}
