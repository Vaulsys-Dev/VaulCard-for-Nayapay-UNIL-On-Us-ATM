package vaulsys.terminal;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.message.Message;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.enums.UserLanguage;
import vaulsys.protocols.ifx.imp.ATMSpecificData;
import vaulsys.protocols.ifx.imp.CardAccountInformation;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCFunctionCommandMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCNetworkToTerminalMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCOperationalMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCResponseMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCWriteCommandMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCWriteCommandOARMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.operational.NDCOperationalGoInServiceMessage;
import vaulsys.protocols.ndc.base.NetworkToTerminal.operational.NDCOperationalGoOutOfServiceMessage;
//import vaulsys.protocols.ndc.base.NetworkToTerminal.operational.NDCOperationalSendCashInSupplyCountersMessage;
import vaulsys.protocols.ndc.base.NetworkToTerminal.operational.NDCOperationalSendConfigIDMessage;
import vaulsys.protocols.ndc.base.NetworkToTerminal.operational.NDCOperationalSendConfigInfoMessage;
import vaulsys.protocols.ndc.base.NetworkToTerminal.operational.NDCOperationalSendSupplyCountersMessage;
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandConfigurationIDLoadMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandDateTimeLoad;
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandEnhancedParameterTableLoadMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandExtEncKeyChange;
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandFitTableLoadMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandScreenTableLoadMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandStateTableLoadMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusFitnessDataResponseMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedJournalPrinterStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedReceiptPrinterStatusMsg;
import vaulsys.protocols.ndc.base.config.ErrorSeverity;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCashHandler;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCJournalPrinter;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCReceiptPrinter;
import vaulsys.protocols.ndc.constants.ATMErrorCodes;
import vaulsys.protocols.ndc.constants.LastStatusIssued;
import vaulsys.protocols.ndc.constants.NDCActiveKeys;
import vaulsys.protocols.ndc.constants.NDCCardRetainFlagConstants;
import vaulsys.protocols.ndc.constants.NDCConstants;
import vaulsys.protocols.ndc.constants.NDCExtEncryptionKeyChangeType;
import vaulsys.protocols.ndc.constants.NDCMessageClassNetworkToTerminal;
import vaulsys.protocols.ndc.constants.NDCOperationalTypes;
import vaulsys.protocols.ndc.constants.NDCTerminalCommandModifier;
import vaulsys.protocols.ndc.constants.NDCTerminalCommandModifierConfigurationInfo;
import vaulsys.protocols.ndc.constants.NDCPrinterFlag;
import vaulsys.protocols.ndc.encoding.NDCConvertor;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.securekey.SecureKey;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.terminal.atm.ATMConfiguration;
import vaulsys.terminal.atm.ATMDisplayFlag;
import vaulsys.terminal.atm.ATMLog;
import vaulsys.terminal.atm.ATMProducer;
import vaulsys.terminal.atm.ATMRequest;
import vaulsys.terminal.atm.ATMResponse;
import vaulsys.terminal.atm.ActionType;
import vaulsys.terminal.atm.FunctionCommandResponse;
import vaulsys.terminal.atm.NewCashDispenser;
import vaulsys.terminal.atm.OARResponse;
import vaulsys.terminal.atm.Receipt;
import vaulsys.terminal.atm.ResponseScreen;
import vaulsys.terminal.atm.RsCodeResponses;
import vaulsys.terminal.atm.action.consumer.ConsumerStartingState;
import vaulsys.terminal.atm.constants.CustomizationDataLength;
import vaulsys.terminal.atm.customizationdata.ATMCustomizationData;
import vaulsys.terminal.atm.customizationdata.EnhancedParameterData;
import vaulsys.terminal.atm.customizationdata.FITData;
import vaulsys.terminal.atm.customizationdata.ScreenData;
import vaulsys.terminal.atm.customizationdata.StateData;
import vaulsys.terminal.atm.customizationdata.TimerData;
import vaulsys.terminal.atm.device.Cassette;
import vaulsys.terminal.atm.device.CassetteA;
import vaulsys.terminal.atm.device.CassetteB;
import vaulsys.terminal.atm.device.CassetteC;
import vaulsys.terminal.atm.device.CassetteD;
import vaulsys.terminal.atm.device.JournalPrinter;
import vaulsys.terminal.atm.device.ReceiptPrinter;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.ClearingInfo;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.MyInteger;
import vaulsys.util.NotUsed;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.util.constants.ASCIIConstants;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

public class ATMTerminalService {
	private static final Logger logger = Logger.getLogger(ATMTerminalService.class);

	public static NDCNetworkToTerminalMsg generateGoInServiceMessage(Long luno) {
		NDCOperationalMsg ndcmsg = new NDCOperationalGoInServiceMessage();
		ndcmsg.messageType = NDCMessageClassNetworkToTerminal.TERMINAL_COMMAND;
		ndcmsg.logicalUnitNumber = luno;
		ndcmsg.messageSequenceNumber = "001";
		ndcmsg.MAC = "01020304";
		// ndcmsg.responseFlag = '0';
		return ndcmsg;
	}

	public static NDCNetworkToTerminalMsg generateGoOutOfServiceMessage(Long luno) {
		NDCOperationalMsg ndcmsg = new NDCOperationalGoOutOfServiceMessage();
		ndcmsg.messageType = NDCMessageClassNetworkToTerminal.TERMINAL_COMMAND;
		ndcmsg.logicalUnitNumber = luno;
		ndcmsg.messageSequenceNumber = "001";
		ndcmsg.MAC = "01020304";
		return ndcmsg;
	}
	
	public static NDCNetworkToTerminalMsg generateSendConfigIDMessage(Long luno) {
		NDCOperationalMsg ndcmsg = new NDCOperationalSendConfigIDMessage();
		ndcmsg.messageType = NDCMessageClassNetworkToTerminal.TERMINAL_COMMAND;
		ndcmsg.logicalUnitNumber = luno;
		ndcmsg.messageSequenceNumber = "001";
		ndcmsg.MAC = "01020304";
		return ndcmsg;
	}
	
	public static NDCNetworkToTerminalMsg generateSendConfigInfoMessage(Long luno, NDCTerminalCommandModifier commandModifier) {
		NDCOperationalMsg ndcmsg = new NDCOperationalSendConfigInfoMessage();
		ndcmsg.messageType = NDCMessageClassNetworkToTerminal.TERMINAL_COMMAND;
		ndcmsg.commandCode = NDCOperationalTypes.SEND_CONFIG_INFO;
		
		if (commandModifier == null)
			ndcmsg.commandModifier = NDCTerminalCommandModifierConfigurationInfo.SEND_ENHANCED_CONFIGURATION_DATA;
			
		ndcmsg.commandModifier = commandModifier;
		
		ndcmsg.logicalUnitNumber = luno;
		ndcmsg.messageSequenceNumber = "001";
		ndcmsg.MAC = "01020304";
		return ndcmsg;
//		return new NDCOperationalSendConfigInfoMessage(luno);
	}
	
	public static NDCNetworkToTerminalMsg generateSupplyCountersMessage(Long luno) {
		NDCOperationalMsg ndcmsg = new NDCOperationalSendSupplyCountersMessage(luno);
		// NDCOperationalMsg ndcmsg = new NDCOperationalGoInServiceMessage();
		ndcmsg.messageType = NDCMessageClassNetworkToTerminal.TERMINAL_COMMAND;
		ndcmsg.logicalUnitNumber = luno;
		ndcmsg.messageSequenceNumber = "001";
		ndcmsg.MAC = "01020304";
		// ndcmsg.responseFlag = '0';
		return ndcmsg;
	}
	
	//public static NDCNetworkToTerminalMsg generateCashInSupplyCountersMessage(Long luno){
		
	//	NDCOperationalMsg ndcmsg = new NDCOperationalSendCashInSupplyCountersMessage(luno);
	//	ndcmsg.messageType = NDCMessageClassNetworkToTerminal.TERMINAL_COMMAND;
	//	ndcmsg.logicalUnitNumber = luno;
	//	ndcmsg.messageSequenceNumber = "001";
	//	ndcmsg.MAC = "01020304";
	//	ndcmsg.commandModifier = NDCTerminalCommandModifierConfigurationInfo.SEND_CASHIN_SUPPY_COUNTERS;
	//	
	//	return ndcmsg;
//	}

	public static NDCNetworkToTerminalMsg generateEnhancedParameterTableLoadMessage(Long luno) {
		NDCWriteCommandEnhancedParameterTableLoadMsg ndcmsg = new NDCWriteCommandEnhancedParameterTableLoadMsg();
		ndcmsg.logicalUnitNumber = luno;
		ndcmsg.messageSequenceNumber = "001";
		ndcmsg.MAC = "01020304";
		// ndcmsg.responseFlag = '0';
		return ndcmsg;
	}
	
	public static NDCNetworkToTerminalMsg generateDateTimeLoadMessage(Long luno) {
		DateTime now = DateTime.now();
		DayDate dd = now.getDayDate();
		DayTime dt = now.getDayTime();
		String data = String.format("%02d%02d%02d%02d%02d", 
				dd.getYear()%100, dd.getMonth(), dd.getDay(), dt.getHour(), dt.getMinute());
		return new NDCWriteCommandDateTimeLoad("001", data.getBytes(), luno);
	}
	
	public static NDCNetworkToTerminalMsg generateExtEncKeyChngMsg_newMasterByCurMaster(Long luno) {
		ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, luno);
		SecureKey tmk = null;
		for(SecureKey key: atm.getKeySet()){
			if(KeyType.TYPE_TMK.equals(key.getKeyType())){
				tmk = key;
				break;
			}
		}
		try {
//			SMAdapter adapter = SecurityComponent.getDefaultSecurityModule();
			SecureDESKey newTmk = SecurityComponent.generateKey(SMAdapter.LENGTH_DES, KeyType.TYPE_TMK);
			newTmk.setTerminal(atm);
			GeneralDao.Instance.saveOrUpdate(newTmk);

			byte[] encNewTak = SecurityComponent.exportKey(newTmk, (SecureDESKey) tmk);
			String newTakKey = new String(Hex.encode(encNewTak));
			StringBuilder builder = new StringBuilder();
			for(int i=0; i<newTakKey.length()-1; i+=2) {
				String dg = newTakKey.substring(i, i+2);
				builder.append(String.format("%03d", Integer.parseInt(dg,16)));
			}
			GeneralDao.Instance.delete(tmk);
			return new NDCWriteCommandExtEncKeyChange(NDCExtEncryptionKeyChangeType.NEW_MASTER_BY_CUR_MASTER_1, builder.toString(), luno, "001");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static NDCNetworkToTerminalMsg generateExtEncKeyChngMsg_PINByMaster(Long luno) {
		ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, luno);
		SecureKey tmk = null;
		for(SecureKey key: atm.getKeySet()){
			if(KeyType.TYPE_TMK.equals(key.getKeyType()))
				tmk = key;
			else if(KeyType.TYPE_TPK.equals(key.getKeyType()))
				GeneralDao.Instance.delete(key);
		}
		try {
//			SMAdapter adapter = SecurityComponent.getDefaultSecurityModule();
			SecureDESKey newTpk = SecurityComponent.generateKey(SMAdapter.LENGTH_DES, KeyType.TYPE_TPK);
			newTpk.setTerminal(atm);
			GeneralDao.Instance.saveOrUpdate(newTpk);

			byte[] encNewTak = SecurityComponent.exportKey(newTpk, (SecureDESKey) tmk);
			String newTakKey = new String(Hex.encode(encNewTak));
			StringBuilder builder = new StringBuilder();
			for(int i=0; i<newTakKey.length()-1; i+=2) {
				String dg = newTakKey.substring(i, i+2);
				builder.append(String.format("%03d", Integer.parseInt(dg,16)));
			}
			return new NDCWriteCommandExtEncKeyChange(NDCExtEncryptionKeyChangeType.NEW_COMMUN_BY_CUR_MASTER_2, builder.toString(), luno, "001");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static NDCNetworkToTerminalMsg generateExtEncKeyChngMsg_MACByMaster(Long luno) {
		ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, luno);
		SecureKey tak = null;
		for(SecureKey key: atm.getKeySet()){
			if(KeyType.TYPE_TMK.equals(key.getKeyType()))
				tak = key;
			else if(KeyType.TYPE_TAK.equals(key.getKeyType()))
				GeneralDao.Instance.delete(key);
		}
		try {
//			SMAdapter adapter = SecurityComponent.getDefaultSecurityModule();
			SecureDESKey newTak = SecurityComponent.generateKey(SMAdapter.LENGTH_DES, KeyType.TYPE_TAK);
			newTak.setTerminal(atm);
			GeneralDao.Instance.saveOrUpdate(newTak);

			byte[] encNewTak = SecurityComponent.exportKey(newTak, (SecureDESKey) tak);
			String newTakKey = new String(Hex.encode(encNewTak));
			StringBuilder builder = new StringBuilder();
			for(int i=0; i<newTakKey.length()-1; i+=2) {
				String dg = newTakKey.substring(i, i+2);
				builder.append(String.format("%03d", Integer.parseInt(dg,16)));
			}
			return new NDCWriteCommandExtEncKeyChange(NDCExtEncryptionKeyChangeType.NEW_MAC_BY_CUR_MASTER_5, builder.toString(), luno, "001");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static NDCNetworkToTerminalMsg generateExtEncKeyChngMsg_localPINAsPIN(Long luno) {
		ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, luno);
		for(SecureKey key: atm.getKeySet()) {
			if(KeyType.TYPE_TPK.equals(key.getKeyType())) {
				GeneralDao.Instance.delete(key);
				break;
			}
		}
		try {
//			SMAdapter adapter = SecurityComponent.getDefaultSecurityModule();
			String pinKey = GlobalContext.getInstance().getATMKey();
			if (pinKey != null && !pinKey.isEmpty()) {
				Key pkey = new SecretKeySpec(Hex.decode(pinKey), "DES");
				SecureDESKey keyPin = SecurityComponent.importKey(SMAdapter.LENGTH_DES, KeyType.TYPE_TPK, pkey.getEncoded(), null, false);
				keyPin.setTerminal(atm);
				GeneralDao.Instance.saveOrUpdate(keyPin);
			}
			return new NDCWriteCommandExtEncKeyChange(NDCExtEncryptionKeyChangeType.LOCAL_COMMUN_AS_CUR_COMMUN_4, luno, "001");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static NDCNetworkToTerminalMsg generateExtEncKeyChngMsg_localPINAsMAC(Long luno) {
		ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, luno);
		for(SecureKey key: atm.getKeySet()) {
			if(KeyType.TYPE_TAK.equals(key.getKeyType())) {
				GeneralDao.Instance.delete(key);
				break;
			}
		}
		try {
//			SMAdapter adapter = SecurityComponent.getDefaultSecurityModule();
			String macKey = GlobalContext.getInstance().getATMKey();
			if (macKey != null && !macKey.isEmpty()) {
				Key pkey = new SecretKeySpec(Hex.decode(macKey), "DES");
				SecureDESKey keyPin = SecurityComponent.importKey(SMAdapter.LENGTH_DES, KeyType.TYPE_TAK, pkey.getEncoded(), null, false);
				keyPin.setTerminal(atm);
				GeneralDao.Instance.saveOrUpdate(keyPin);
			}
			return new NDCWriteCommandExtEncKeyChange(NDCExtEncryptionKeyChangeType.LOCAL_COMMUN_AS_CUR_MAC_7, luno, "001");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static NDCNetworkToTerminalMsg generateExtEncKeyChngMsg_acquireATMPublicKey(Long luno) {
		try {
			return new NDCWriteCommandExtEncKeyChange(NDCExtEncryptionKeyChangeType.SEND_EPP_PUBLIC_KEY_G, luno, "001");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static NDCNetworkToTerminalMsg generateExtEncKeyChngMsg_acquireAllKVV(Long luno) {
		try {
			return new NDCWriteCommandExtEncKeyChange(NDCExtEncryptionKeyChangeType.SEND_ALL_KVV_H, luno, "001");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static ATMRequest findATMRequest(ATMTerminal atm, String opkey) {
//		ATMConfiguration configuration = atm.getOwnOrParentConfiguration();
		ATMConfiguration configuration = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		Serializable configId = configuration.getId();
		
		String query = "from ATMRequest as rq " +
				" where rq.opkey = :opkey " + 
				" and rq.configuration.id = :configId";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("opkey", opkey);
		params.put("configId", configId);
		return (ATMRequest) GeneralDao.Instance.findObject(query, params);
	}

	public static boolean isNeedToSendConfigData(ATMTerminal atm) {
		int maxCustomData = getMaxCustomizationDataConfigId(atm);
		return maxCustomData > atm.getConfigId();	
	}
	
	public static Integer getMaxCustomizationDataConfigId(ATMTerminal atm){
		List<Class> classList = new ArrayList<Class>();
		classList.add(TimerData.class);
		classList.add(EnhancedParameterData.class);
		classList.add(ScreenData.class);
		classList.add(StateData.class);
		classList.add(FITData.class);
		
		int maxCustomData = 0;
		for (Class clazz: classList) {
			Integer maxCustomizationDataConfigId = getMaxCustomizationDataConfigId(atm, clazz);
			if(maxCustomizationDataConfigId != null)
				maxCustomData = Math.max(maxCustomData, maxCustomizationDataConfigId);
		}
		return maxCustomData;
	}
	
	private static Integer getMaxCustomizationDataConfigId(ATMTerminal atm, Class clazz){
//		ATMConfiguration configuration = atm.getOwnOrParentConfiguration();
		ATMConfiguration configuration = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		
		String query = "select max(configid) from " + clazz.getName() + " as d " +
				" where d.atmConfiguration = :config "; 

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("config", configuration);
		return (Integer) GeneralDao.Instance.findObject(query, params);
	}
	
	public static <T extends ATMCustomizationData> List<T> getCustomizationDataAfter(ATMTerminal atm, Class<T> clazz, Integer configid){
//		ATMConfiguration configuration = atm.getOwnOrParentConfiguration();
		ATMConfiguration configuration = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		
		String query = "from " + clazz.getName() + " as d " +
				" where d.atmConfiguration = :config ";
		if(configid != null)
			query += " and d.configid > :configid ";
//		else {
			query += " and d.configid in (select max(d2.configid) from " + clazz.getName() + " d2 where d2.number = d.number and d2.atmConfiguration = :config)";
//		}
		query += " order by d.number "; 

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("config", configuration);
		if(configid != null)
			params.put("configid", configid);
		return GeneralDao.Instance.find(query, params);
	}

	public static void updateTerminalStatus(Ifx ifx, ATMTerminal atm) {
		if (!Util.hasText(ifx.getCurrentDispense()) ||
			ISOFinalMessageType.isReversalMessage(ifx.getIfxType())){
			return;
		}

		int[] notes = NDCParserUtils.parseDispensedNote(ifx.getCurrentDispense(), 2);
		CassetteA casseteA = atm.getDevice(CassetteA.class);
		casseteA.decreseNotes(notes[0]);
		CassetteB casseteB = atm.getDevice(CassetteB.class);
		casseteB.decreseNotes(notes[1]);
		CassetteC casseteC = atm.getDevice(CassetteC.class);
		casseteC.decreseNotes(notes[2]);
		CassetteD casseteD = atm.getDevice(CassetteD.class);
		casseteD.decreseNotes(notes[3]);
		GeneralDao.Instance.saveOrUpdate(atm);
	}

	public static void prepareProcess(ATMTerminal atm) {
			atm.setLastSentFitIndex(Integer.MAX_VALUE);
			atm.setLastSentStateIndex(Integer.MAX_VALUE);
			atm.setLastSentScreenIndex(Integer.MAX_VALUE);
			GeneralDao.Instance.saveOrUpdate(atm);
	}

	public static void prepareProcessForSentConfig(ATMTerminal atm) {
		atm.setLastSentFitIndex(0);
		atm.setLastSentStateIndex(0);
		atm.setLastSentScreenIndex(0);
//		atm.setLastSentParamIndex(0);
		GeneralDao.Instance.saveOrUpdate(atm);
	}

	
	public static void updateTerminalStatus(ATMTerminal atm, NDCCashHandler cashHandler) {
		ErrorSeverity[] errorSeverity = cashHandler.errorSeverity;
		DateTime now = DateTime.now();
		
		CassetteA casseteA = atm.getDevice(CassetteA.class);
		casseteA.decreseNotes(cashHandler.notesDispensed[0]);
		
		CassetteB casseteB = atm.getDevice(CassetteB.class);
		casseteB.decreseNotes(cashHandler.notesDispensed[1]);

		CassetteC casseteC = atm.getDevice(CassetteC.class);
		casseteC.decreseNotes(cashHandler.notesDispensed[2]);
		
		CassetteD casseteD = atm.getDevice(CassetteD.class);
		casseteD.decreseNotes(cashHandler.notesDispensed[3]);

		int[] currentDispenseNotes = NDCParserUtils.parseDispensedNote(atm.getLastTransaction().getOutgoingIfx().getCurrentDispense(), 2);

		/******* NCR ha faghad dar morede casette haei ke goftim dispense kon ezhare nazar mikonad va baghie ra no error midaha!!! *******/
		if (currentDispenseNotes != null && currentDispenseNotes.length >= 4 && ATMProducer.NCR.equals(atm.getProducer())) 
		{
	    	if (currentDispenseNotes[0] > 0 ) //Ager >0 means az in cassete pardakht karde
	    	{
				casseteA.setErrorSeverity(errorSeverity[1]);
				casseteA.setTotalErrorSeverity(errorSeverity[1]);
				casseteA.setErrorSeverityDate(now);
	    	}

	    	if (currentDispenseNotes[1] > 0 ) 
	    	{
				casseteB.setErrorSeverity(errorSeverity[2]);
				casseteB.setTotalErrorSeverity(errorSeverity[2]);
				casseteB.setErrorSeverityDate(now);
	    	}
	    	if (currentDispenseNotes[2] > 0 ) 
	    	{   	
				casseteC.setErrorSeverity(errorSeverity[3]);
				casseteC.setTotalErrorSeverity(errorSeverity[3]);
				casseteC.setErrorSeverityDate(now);
	    	}
	    	if (currentDispenseNotes[3] > 0 ) 
	    	{
				casseteD.setErrorSeverity(errorSeverity[4]);
				casseteD.setTotalErrorSeverity(errorSeverity[4]);
				casseteD.setErrorSeverityDate(now);
	    	}			
		}
		else 
		{
			casseteA.setErrorSeverity(errorSeverity[1]);
			casseteA.setTotalErrorSeverity(errorSeverity[1]);
			casseteA.setErrorSeverityDate(now);
			
			casseteB.setErrorSeverity(errorSeverity[2]);
			casseteB.setTotalErrorSeverity(errorSeverity[2]);
			casseteB.setErrorSeverityDate(now);

			casseteC.setErrorSeverity(errorSeverity[3]);
			casseteC.setTotalErrorSeverity(errorSeverity[3]);
			casseteC.setErrorSeverityDate(now);

			casseteD.setErrorSeverity(errorSeverity[4]);
			casseteD.setTotalErrorSeverity(errorSeverity[4]);
			casseteD.setErrorSeverityDate(now);
		}
		
		/*if(ErrorSeverity.FATAL.equals(errorSeverity[0])) {
			casseteA.setTotalErrorSeverity(ErrorSeverity.FATAL);
			casseteB.setTotalErrorSeverity(ErrorSeverity.FATAL);
			casseteC.setTotalErrorSeverity(ErrorSeverity.FATAL);
			casseteD.setTotalErrorSeverity(ErrorSeverity.FATAL);
        }*/
		
		if(errorSeverity[0] != null && !ErrorSeverity.UNKNOWN.equals(errorSeverity[0])) {
			casseteA.setTotalErrorSeverity(errorSeverity[0]);
			casseteB.setTotalErrorSeverity(errorSeverity[0]);
			casseteC.setTotalErrorSeverity(errorSeverity[0]);
			casseteD.setTotalErrorSeverity(errorSeverity[0]);
        }
		
		GeneralDao.Instance.saveOrUpdate(casseteA);
		GeneralDao.Instance.saveOrUpdate(casseteB);
		GeneralDao.Instance.saveOrUpdate(casseteC);
		GeneralDao.Instance.saveOrUpdate(casseteD);
		GeneralDao.Instance.saveOrUpdate(atm);
		
		ATMLog log = new ATMLog(atm.getCode(), cashHandler.transactionStatus.toString(), ActionType.CASH_HANDLER);
		GeneralDao.Instance.saveOrUpdate(log);
		
		log = new ATMLog(atm.getCode(), "CASSETTE_A:" + cashHandler.cassetteSuppliesStatus[0].toString(), ActionType.SUPPLY_STATUS);
		GeneralDao.Instance.saveOrUpdate(log);
		
		log = new ATMLog(atm.getCode(), "CASSETTE_B:" + cashHandler.cassetteSuppliesStatus[1].toString(), ActionType.SUPPLY_STATUS);
		GeneralDao.Instance.saveOrUpdate(log);
		
		log = new ATMLog(atm.getCode(), "CASSETTE_C:" + cashHandler.cassetteSuppliesStatus[2].toString(), ActionType.SUPPLY_STATUS);
		GeneralDao.Instance.saveOrUpdate(log);
		
		log = new ATMLog(atm.getCode(), "CASSETTE_D:" + cashHandler.cassetteSuppliesStatus[3].toString(), ActionType.SUPPLY_STATUS);
		GeneralDao.Instance.saveOrUpdate(log);
		
		log = new ATMLog(atm.getCode(), "REJECT_BIN:" + cashHandler.rejectBinSuppliesStatus.toString(), ActionType.SUPPLY_STATUS);
		GeneralDao.Instance.saveOrUpdate(log);
	}
	
	public static void updateReceiptPrinter(ATMTerminal atm, Message inputMessage) {
    	NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
        NDCUnsolicitedReceiptPrinterStatusMsg msg = (NDCUnsolicitedReceiptPrinterStatusMsg) ndcMsg;
        NDCReceiptPrinter printerInfo = msg.statusInformation;
        ReceiptPrinter printer = atm.getDevice(ReceiptPrinter.class);
//        DeviceStatus prevStatus = printer.getStatus();
        
        printer.setErrorSeverity(printerInfo.errorSeverity);
        printer.setStatus(printerInfo.deviceStatus);
        printer.setPaperStatus(printerInfo.paperStatus);
        printer.setRibbonStatus(printerInfo.ribbonStatus);
        printer.setPrintheadStatus(printerInfo.printheadStatus);
        printer.setKnifeStatus(printerInfo.knifeStatus);
        
        GeneralDao.Instance.saveOrUpdate(printer);
        GeneralDao.Instance.saveOrUpdate(atm);

//        if(prevStatus != null){
//	        ATMLog log = new ATMLog(atm.getCode(), "RECEIPT_PRINTER:" + ReceiptPrinterStatus.getByCode(prevStatus.getStatus()).toString(), "LAST_STATE", ActionType.DEVICE_UPDATE);
//			GeneralDao.Instance.saveOrUpdate(log);
//			
//			log = new ATMLog(atm.getCode(), "RECEIPT_PRINTER:" + printer.getStatus().toString(), "NEXT_STATE", ActionType.DEVICE_UPDATE);
//			GeneralDao.Instance.saveOrUpdate(log);
//        }
	}
	
public static void updateFitnessData(ATMTerminal atm, Message inputMessage) {
		
		DateTime now = DateTime.now();
		
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
		NDCSolicitedStatusFitnessDataResponseMsg msg = (NDCSolicitedStatusFitnessDataResponseMsg) ndcMsg;

		NDCReceiptPrinter receiptPrinterInfo = msg.receiptPrinter;
		ReceiptPrinter receiptPrinterDevice = atm.getDevice(ReceiptPrinter.class);
		receiptPrinterDevice.setErrorSeverity(receiptPrinterInfo.errorSeverity);
		
		NDCJournalPrinter journalPrinterInfo = msg.journalPrinter;
		JournalPrinter journalPrinterDevice = atm.getDevice(JournalPrinter.class);
		journalPrinterDevice.setErrorSeverity(journalPrinterInfo.errorSeverity);
		
		NDCCashHandler cashHandlerInfo = msg.cashHandler;
		CassetteA cassetteADecvice = atm.getDevice(CassetteA.class);
		cassetteADecvice.setErrorSeverity(cashHandlerInfo.errorSeverity[1]);
		CassetteB cassetteBDecvice = atm.getDevice(CassetteB.class);
		cassetteBDecvice.setErrorSeverity(cashHandlerInfo.errorSeverity[2]);
		CassetteC cassetteCDecvice = atm.getDevice(CassetteC.class);
		cassetteCDecvice.setErrorSeverity(cashHandlerInfo.errorSeverity[3]);
		CassetteD cassetteDDecvice = atm.getDevice(CassetteD.class);
		cassetteDDecvice.setErrorSeverity(cashHandlerInfo.errorSeverity[4]);
		
		cassetteADecvice.setErrorSeverityDate(now);
		cassetteBDecvice.setErrorSeverityDate(now);
		cassetteCDecvice.setErrorSeverityDate(now);
		cassetteDDecvice.setErrorSeverityDate(now);
		
		ErrorSeverity errorSeverity0 = cashHandlerInfo.errorSeverity[0];
		if(errorSeverity0 != null && !ErrorSeverity.UNKNOWN.equals(errorSeverity0)) {
			cassetteADecvice.setTotalErrorSeverity(errorSeverity0);
			cassetteBDecvice.setTotalErrorSeverity(errorSeverity0);
			cassetteCDecvice.setTotalErrorSeverity(errorSeverity0);
			cassetteDDecvice.setTotalErrorSeverity(errorSeverity0);
        }
		
		GeneralDao.Instance.saveOrUpdate(receiptPrinterDevice);
		GeneralDao.Instance.saveOrUpdate(journalPrinterDevice);
		GeneralDao.Instance.saveOrUpdate(cassetteADecvice);
		GeneralDao.Instance.saveOrUpdate(cassetteBDecvice);
		GeneralDao.Instance.saveOrUpdate(cassetteCDecvice);
		GeneralDao.Instance.saveOrUpdate(cassetteDDecvice);
		GeneralDao.Instance.saveOrUpdate(atm);
		
//		if(prevStatus != null){
//			ATMLog log = new ATMLog(atm.getCode(), "RECEIPT_PRINTER:" + ReceiptPrinterStatus.getByCode(prevStatus.getStatus()).toString(), "LAST_STATE", ActionType.DEVICE_UPDATE);
//			GeneralDao.Instance.saveOrUpdate(log);
//			
//			log = new ATMLog(atm.getCode(), "RECEIPT_PRINTER:" + printer.getStatus().toString(), "NEXT_STATE", ActionType.DEVICE_UPDATE);
//			GeneralDao.Instance.saveOrUpdate(log);
//		}
	}
	
	public static void updateJournalPrinter(ATMTerminal atm, Message inputMessage) {
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
        NDCUnsolicitedJournalPrinterStatusMsg msg = (NDCUnsolicitedJournalPrinterStatusMsg) ndcMsg;
        NDCJournalPrinter printerInfo = msg.statusInformation;
        JournalPrinter printer = atm.getDevice(JournalPrinter.class);
//        DeviceStatus prevStatus = printer.getStatus();
        
        printer.setErrorSeverity(printerInfo.errorSeverity);
        printer.setStatus(printerInfo.deviceStatus);
        printer.setPaperStatus(printerInfo.paperStatus);
        printer.setRibbonStatus(printerInfo.ribbonStatus);
        printer.setPrintheadStatus(printerInfo.printheadStatus);
        printer.setKnifeStatus(printerInfo.knifeStatus);
        printer.setKnifeStatus(printerInfo.knifeStatus);

        GeneralDao.Instance.saveOrUpdate(printer);
		GeneralDao.Instance.saveOrUpdate(atm);
		
//		if(prevStatus != null){
//			ATMLog log = new ATMLog(atm.getCode(), "JOURNAL_PRINTER:" + JournalPrinterStatus.getByCode(prevStatus.getStatus()).toString(), "LAST_STATE", ActionType.DEVICE_UPDATE);
//			GeneralDao.Instance.saveOrUpdate(log);
//			
//			log = new ATMLog(atm.getCode(), "JOURNAL_PRINTER:" + printer.getStatus().toString(), "NEXT_STATE", ActionType.DEVICE_UPDATE);
//			GeneralDao.Instance.saveOrUpdate(log);
//		}
	}
	

	/*public DeviceStatus getDeviceStatus(ErrorSeverity type) {
		if (type.equals(ErrorSeverity.FATAL) ||
				type.equals(ErrorSeverity.ROUTINE_ERROR))
			return DeviceStatus.FATAL;
		
		if (type.equals(ErrorSeverity.WARNING))
			return DeviceStatus.WARNING;
		
		if (type.equals(ErrorSeverity.NO_ERROR))
			return DeviceStatus.NORMAL;
		
		return DeviceStatus.UNKOWN;
	}*/
	
	/*public NDCPrinterStatus getPrinterStatus(ErrorSeverity type) {
		if (type.equals(ErrorSeverity.FATAL) ||
				type.equals(ErrorSeverity.ROUTINE_ERROR))
			return NDCPrinterStatus.PRINT_OPERATION_NOT_SUCCESS_COMPLETED;
		
		if (type.equals(ErrorSeverity.WARNING))
			return NDCPrinterStatus.MEDIA_LOW;
		
		if (type.equals(ErrorSeverity.NO_ERROR))
			return NDCPrinterStatus.SUCCESSFUL_PRINT;
		
		return NDCPrinterStatus.UNKNOWN;
	}*/
	
	public static Long timeVariantToNetworkRefId(String timeVariant) {
		return Long.parseLong(timeVariant, 16);
	}
	
	public static String networkRefIdToTimeVariant(String networkRefId) {
//		StringFormat format = new StringFormat(8, StringFormat.JUST_RIGHT);
		String strNetRefId = Long.toHexString(Long.parseLong(networkRefId)).toUpperCase();
		return StringFormat.formatNew(8, StringFormat.JUST_RIGHT, strNetRefId, '0');
	}
	
	public static ClearingState checkValidityOfLastTransactionStatus(Terminal terminal, Ifx inIfx, Ifx myLastIfxOut) {
		ClearingState flag = ClearingState.CLEARED;
		
		if (inIfx.getLastTrxStatusIssue() != null 
				&& !LastStatusIssued.UNKNOWN.equals(inIfx.getLastTrxStatusIssue())
				&& !LastStatusIssued.GOOD_TERMINATION_SENT.equals(inIfx.getLastTrxStatusIssue())) {

			logger.info("Though myLastSeqCntr is equall to terminalLastSeqCntr, its LastTrxStatus is not GOOD_TERMINATION!");
			flag = ClearingState.SUSPECTED_DISAGREEMENT;

		} else if (myLastIfxOut.getTransaction().getSourceClearingInfo() != null) {
			if (ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED.equals(myLastIfxOut.getTransaction().getSourceClearingInfo().getClearingState())) {
				logger.info("OHOH last clearingState is NOT_NOTE_SUCCESSFULLY_DISPENSED but we want to clear it!");
				flag = ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED;
			}
		}
		
		if (myLastIfxOut.getTotalStep() != null && 
				myLastIfxOut.getTotalStep().equals(new Integer(1)) && 
				myLastIfxOut.getTransaction().getSourceClearingInfo() != null) {
			
			int[] atmLastNotes = NDCParserUtils.parseDispensedNote(inIfx.getLastTrxNotesDispensed(), 5);
			
			boolean diff = false;
			
			Integer note1 = myLastIfxOut.getDesiredDispenseCaset1();
			Integer note2 = myLastIfxOut.getDesiredDispenseCaset2();
			Integer note3 = myLastIfxOut.getDesiredDispenseCaset3();
			Integer note4 = myLastIfxOut.getDesiredDispenseCaset4();
			
			String outNotes = note1.toString() + note2.toString() + note3.toString() + note4.toString();
			
			if (atmLastNotes != null/* && atmLastNotes != null && atmLastNotes != null && atmLastNotes != null && atmLastNotes != null*/) {
				int[] ourNotes = new int[] { note1, note2, note3, note4 };
				for (int i = 0; i < 4; i++) {
					if (atmLastNotes[i] != ourNotes[i]) {
						diff = true;
						break;
					}
				}
			}
			
			if (diff) {
				logger.error("OHOH NOTE DIFFERENCE: we want put " + flag.getName() + " to last_trx (ifx: " + myLastIfxOut.getId() + ") " +
						"but myLastNoteDispensed: " + outNotes + " different to atm said: " + inIfx.getLastTrxNotesDispensed());
				
				flag = ClearingState.SUSPECTED_DISAGREEMENT; 
			}
		}
		
		return flag;
	}
	
	@NotUsed
	public static void checkLastTransactionStatus(Long logicalUnitNumber, String lastTrxSeqCounter, char lastStatusIssue,
			String lastTrxNotesDispensed) {
		ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, logicalUnitNumber);
		boolean diffrent = false;
		Transaction lastTransactionAtm = atm.getLastTransaction();
		if (lastTransactionAtm == null || lastTransactionAtm.getInputMessage() == null
				|| lastTransactionAtm.getIncomingIfx()/*getInputMessage().getIfx()*/ == null || lastTransactionAtm.getSequenceCounter() == null)
			return;
		if (lastTransactionAtm.getInputMessage().isScheduleMessage())
			return;
		if (Integer.parseInt(lastTrxSeqCounter) != Integer.parseInt(lastTransactionAtm.getSequenceCounter())) {
			diffrent = true;
		} else {
			Transaction trx = lastTransactionAtm.getFirstTransaction() == null ? lastTransactionAtm : 
				lastTransactionAtm.getFirstTransaction();
			int[] notes = NDCParserUtils.parseDispensedNote(lastTrxNotesDispensed, 5);
			// String noteDispense =
			// trx.getInputMessage().getIfx().getNoteDispense();
			Ifx ifx = trx.getIncomingIfx()/*getInputMessage().getIfx()*/;

			Integer note1 = ifx.getDesiredDispenseCaset1();
			Integer note2 = ifx.getDesiredDispenseCaset2();
			Integer note3 = ifx.getDesiredDispenseCaset3();
			Integer note4 = ifx.getDesiredDispenseCaset4();
			if (notes != null && note1 != null && note2 != null && note3 != null && note4 != null) {
				// int[] ourNotes =
				// NDCParserUtils.parseDispensedNote(noteDispense, 2);
				int[] ourNotes = new int[] { note1, note2, note3, note4 };
				for (int i = 0; i < 4; i++) {
					if (notes[i] != ourNotes[i]) {
						diffrent = true;
						break;
					}
				}
			} else {
				String noteStr = "";

				if (notes != null)
					for (int i = 0; i < notes.length; i++)
						noteStr += notes[i];
				diffrent = !(
						(notes == null || Util.longValueOf(noteStr) == 0) && 
						(
								(note1 == null && note2 == null && note3 == null && note4 == null) || 
								(note1.equals(0) && note2.equals(0) && note3.equals(0) && note4.equals(0))));
			}
		}
		DateTime currentTime = DateTime.now();
		ClearingInfo lastSourceClearingInfo = lastTransactionAtm.getSourceClearingInfo();

		if (lastSourceClearingInfo != null) {
			if ((diffrent || lastStatusIssue == '2') /*&& !lastTransactionAtm.getOutputMessage().getNeedToBeInstantlyReversed()*/) {
				lastSourceClearingInfo.setClearingState(ClearingState.DISAGREEMENT);
				lastSourceClearingInfo.setClearingDate(currentTime);
				GeneralDao.Instance.saveOrUpdate(lastSourceClearingInfo);
				// transactionService.copyFlagsToFirstTransaction(lastTransactionAtm);
			} else {
				lastSourceClearingInfo.setClearingState(ClearingState.CLEARED);
				lastSourceClearingInfo.setClearingDate(currentTime);
				GeneralDao.Instance.saveOrUpdate(lastSourceClearingInfo);
				// transactionService.copyFlagsToFirstTransaction(lastTransactionAtm);
			}
		}
	}

	public static int[] dynamicDispenseNotes(ATMTerminal atm, long amount)
	{
		try
		{
			NewCashDispenser dc = NewCashDispenser.getInstance();

			List<Cassette> cassettes = new ArrayList<Cassette>(4);
			int[] result = new int[] { 0, 0, 0, 0 };

			cassettes.add(atm.getDevice(CassetteA.class));
			cassettes.add(atm.getDevice(CassetteB.class));
			cassettes.add(atm.getDevice(CassetteC.class));
			cassettes.add(atm.getDevice(CassetteD.class));
			boolean possible = dc.isPossible(amount, result, cassettes);
			
			long amt = 0;
			for(int i=0; i<result.length; i++)
				amt += result[i] * cassettes.get(i).getDenomination();
			
			if(possible && (amt != amount || result[0] < 0  || result[1] < 0 || result[2] < 0 || result[3] <0)){
				String str = "";
				for(int i=0; i<result.length; i++)
					str += "["+i+"]: "+result[i];
				
				logger.error("OHOH: our cash dispensing algorithm is very bad: "+amount + " but returned: "+amt+" with: "+str);
				return null;
			}
			
			if(possible && amt == amount)
				return result;
			
			if(!possible)
				return null;
			
			return null;
		} catch (Exception e)
		{
			return null;
		}
	}

	public static NDCResponseMsg fromIfx(Ifx ifx, ATMResponse atmRs, ATMTerminal atm, EncodingConvertor convertor) throws Exception {
//		if (ifx.getId() != null)
//			atm.setLastTransaction(ifx.getTransaction());
		if (atmRs instanceof OARResponse) {
//			GeneralDao.Instance.saveOrUpdate(atm);
			return getResponseMessage(ifx, (OARResponse) atmRs, (NDCConvertor) convertor);
		}
		if (atmRs instanceof FunctionCommandResponse) {
			atm.setCurrentAbstractStateClass(ConsumerStartingState.Instance);
			GeneralDao.Instance.saveOrUpdate(atm);
//			atmRs = GeneralDao.Instance.getObject(ATMResponse.class, atmRs.getId());
			return getResponseMessage(ifx, (FunctionCommandResponse) atmRs, atm, (NDCConvertor) convertor);
		}
		return null;
	}

	public static NDCResponseMsg getResponseMessage(Ifx ifxOut, FunctionCommandResponse atmRs, ATMTerminal atm, NDCConvertor convertor) throws Exception
	{
		FunctionCommandResponse atmResponse = atmRs;
		NDCFunctionCommandMsg functionCommandMsg = new NDCFunctionCommandMsg();
		functionCommandMsg.printerData = new ArrayList<byte[]>();
		
		boolean needAccountAmount = false;
		String allCassettesAsByte = "";
		if (atmResponse.getDispense() == null)
			needAccountAmount = true;
		else {
			allCassettesAsByte = atmResponse.getDispense().getAllCassettesAsByte();
			if (allCassettesAsByte == null)
				needAccountAmount = true;
		}
		
		int totalNotes = 0;
//		Integer maxDespensableNotes = atm.getOwnOrParentConfiguration().getMaxDespensingNotes();
		Integer maxDespensableNotes = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId()).getMaxDespensingNotes();
		int[] dispense = null;
		//Mirkamali(Task179)
		if ((ifxOut.getTrnType().equals(TrnType.WITHDRAWAL) || ifxOut.getTrnType().equals(TrnType.WITHDRAWAL_CUR))  && needAccountAmount && ifxOut.getAuth_Amt() != 0L) {

			if (ifxOut.getAtmSpecificData() == null || !Util.hasText(ifxOut.getAtmSpecificData().getCurrentDispense())) {
				//Mirkamali(Task179)
				if(ifxOut.getTrnType().equals(TrnType.WITHDRAWAL_CUR)) {
					dispense = dynamicDispenseNotes(atm, ifxOut.getReal_Amt().longValue());
				} else
					dispense = dynamicDispenseNotes(atm, ifxOut.getAuth_Amt().longValue());
				if(dispense == null){
					functionCommandMsg.nextStateID = atmResponse.getNextState();
					functionCommandMsg.screenNumber = atmResponse.getScreen(ifxOut.getUserLanguage()).getScreenno();
				
					//TODO: double check this line, change from getByteOfReciept to ndcconvertor!
					functionCommandMsg.screenDisplayUpdateData = convertor.convert(null, atmResponse.getScreen(ifxOut.getUserLanguage()).getScreenData(), ifxOut);
					functionCommandMsg.cardReturnRetainFlag = NDCCardRetainFlagConstants.getByBoolean(atmResponse.getCradRetain());

					for (Receipt r : atmResponse.getReceipt(ifxOut.getUserLanguage())) { // SwitchApplication.get().getAtmTerminalService().getReceipt(atmResponse.getId())){
		            	if(atm.getProducer() != null && atm.getProducer().equals(ATMProducer.NCR) && NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY.equals(r.getPrinterFlag())){
		            		ByteArrayOutputStream out = new ByteArrayOutputStream();
		            		out.write("2".getBytes());
		            		out.write(ASCIIConstants.ESC);
		            		out.write(0x25);
		            		out.write("000".getBytes());

		            		out.write(convertor.convert(r.getPrinterFlag(), r.getReplacedText()/*r.getText()*/, ifxOut));
		            		functionCommandMsg.printerData.add(out.toByteArray());            		
		            	}else{            		
						    functionCommandMsg.printerData.add(convertor.convert(r.getPrinterFlag(), r.getReplacedText(), ifxOut));
	//					    functionCommandMsg.printerData.add(ProcessContext.get().getATMConfiguration(atm.getConfigurationId()).getReceiptConvertor().getBytes());
		            	}
					}
		
					functionCommandMsg.functionIdentifier = atmResponse.getFunctionCommand();
					functionCommandMsg.logicalUnitNumber = Long.valueOf(ifxOut.getTerminalId());

//					StringFormat format = new StringFormat(4, StringFormat.JUST_RIGHT);
					functionCommandMsg.messageSequenceNumber = networkRefIdToTimeVariant(ifxOut.getNetworkRefId()); // ifxOut.getSrc_TrnSeqCntr();
		        	functionCommandMsg.transactionSerialNumber = StringFormat.formatNew(4, StringFormat.JUST_RIGHT, ifxOut.getSrc_TrnSeqCntr(), '0');
   		    		functionCommandMsg.messageCoordinationNumber = ifxOut.getCoordinationNumber();

			    	String authCode = ifxOut.getMsgAuthCode();
	        	    functionCommandMsg.MAC = (authCode != null && !authCode.equals("")) ? authCode.substring(0, 8) : "01020304";
			    	return functionCommandMsg;
				}
				
				ifxOut.setDesiredDispenseCaset1(dispense[0]);
				ifxOut.setDesiredDispenseCaset2(dispense[1]);
				ifxOut.setDesiredDispenseCaset3(dispense[2]);
				ifxOut.setDesiredDispenseCaset4(dispense[3]);

				ifxOut.setActualDispenseCaset1(0);
				ifxOut.setActualDispenseCaset2(0);
				ifxOut.setActualDispenseCaset3(0);
				ifxOut.setActualDispenseCaset4(0);

				totalNotes = dispense[0] + dispense[1] + dispense[2] + dispense[3];
				ifxOut.setCurrentStep(0);

				ifxOut.setTotalStep((int) Math.ceil((double) totalNotes / maxDespensableNotes));
			} else {
				dispense = new int[4];
				dispense[0] = ifxOut.getDesiredDispenseCaset1();
				dispense[1] = ifxOut.getDesiredDispenseCaset2();
				dispense[2] = ifxOut.getDesiredDispenseCaset3();
				dispense[3] = ifxOut.getDesiredDispenseCaset4();
			}

			if (ifxOut.getCurrentStep() > 0) {
				Integer cassette1 = Integer.parseInt(ifxOut.getCurrentDispense().substring(0, 2));
				Integer cassette2 = Integer.parseInt(ifxOut.getCurrentDispense().substring(2, 4));
				Integer cassette3 = Integer.parseInt(ifxOut.getCurrentDispense().substring(4, 6));
				Integer cassette4 = Integer.parseInt(ifxOut.getCurrentDispense().substring(6, 8));

				ifxOut.setActualDispenseCaset1(ifxOut.getActualDispenseCaset1() + cassette1);
				ifxOut.setActualDispenseCaset2(ifxOut.getActualDispenseCaset2() + cassette2);
				ifxOut.setActualDispenseCaset3(ifxOut.getActualDispenseCaset3() + cassette3);
				ifxOut.setActualDispenseCaset4(ifxOut.getActualDispenseCaset4() + cassette4);
				
				int actualDispenseAmt = ifxOut.getActualDispenseCaset1()* atm.getDevice(CassetteA.class).getDenomination();
				actualDispenseAmt += ifxOut.getActualDispenseCaset2()* atm.getDevice(CassetteB.class).getDenomination();
				actualDispenseAmt += ifxOut.getActualDispenseCaset3()* atm.getDevice(CassetteC.class).getDenomination();
				actualDispenseAmt += ifxOut.getActualDispenseCaset4()* atm.getDevice(CassetteD.class).getDenomination();
				ifxOut.setActualDispenseAmt(new Long(actualDispenseAmt));
			}

			ifxOut.setCurrentStep(ifxOut.getCurrentStep() + 1);

			String currentNoteDispense = allCassettesAsByte;

			if (!ISOResponseCodes.APPROVED.equals(ifxOut.getRsCode())) {
				if (Util.hasText(currentNoteDispense))
					functionCommandMsg.numberOfBillsToDispense = currentNoteDispense;
			} else if (!Util.hasText(currentNoteDispense)) {
				currentNoteDispense = getNotesInOneStep(dispense, maxDespensableNotes,
						ifxOut.getActualDispenseCaset1(), ifxOut.getActualDispenseCaset2(), 
						ifxOut.getActualDispenseCaset3(), ifxOut.getActualDispenseCaset4());
			}
			
			functionCommandMsg.numberOfBillsToDispense = currentNoteDispense;
			ifxOut.setCurrentDispense(currentNoteDispense);
			int[] currentDispensedNote = NDCParserUtils.parseDispensedNote(currentNoteDispense, 2);
			if (currentDispensedNote.length == 4) {
				ifxOut.setCurrentDispenseCaset1(currentDispensedNote[0]);
				ifxOut.setCurrentDispenseCaset2(currentDispensedNote[1]);
				ifxOut.setCurrentDispenseCaset3(currentDispensedNote[2]);
				ifxOut.setCurrentDispenseCaset4(currentDispensedNote[3]);
				int currentDispenseAmt = ifxOut.getCurrentDispenseCaset1()* atm.getDevice(CassetteA.class).getDenomination();
				currentDispenseAmt += ifxOut.getCurrentDispenseCaset2()* atm.getDevice(CassetteB.class).getDenomination();
				currentDispenseAmt += ifxOut.getCurrentDispenseCaset3()* atm.getDevice(CassetteC.class).getDenomination();
				currentDispenseAmt += ifxOut.getCurrentDispenseCaset4()* atm.getDevice(CassetteD.class).getDenomination();
				ifxOut.setCurrentDispenseAmt(new Long(currentDispenseAmt));
			}
			
			if ( ((IfxType.WITHDRAWAL_RS.equals(ifxOut.getIfxType()) || IfxType.WITHDRAWAL_CUR_RS.equals(ifxOut.getIfxType())) 
					&& ISOResponseCodes.APPROVED.equals(ifxOut.getRsCode())
					&& !ifxOut.getCurrentStep().equals(ifxOut.getTotalStep())) || 
				 (IfxType.PARTIAL_DISPENSE_RS.equals(ifxOut.getIfxType()) 
						 && ifxOut.getCurrentStep().equals(ifxOut.getTotalStep()))) {
				String opkey = /*ifxOut.getOpkey()*/ifxOut.getProperOpkey();
//				ATMRequest atmRequest = findATMRequest(atm, opkey);
				ATMRequest atmRequest = ProcessContext.get().getATMRequest(atm.getOwnOrParentConfigurationId(), opkey);
				atmResponse = atmRequest.getAtmResponse(ATMErrorCodes.ATM_SUCCESS_PARTIAL_DISPENSE);
			}
			
			if (!ISOFinalMessageType.isReversalMessage(ifxOut.getIfxType())) {
				Transaction refTrx = ifxOut.getTransaction().getReferenceTransaction();
				refTrx = (refTrx == null ||
							(refTrx.getOutputMessage()!= null && !ISOFinalMessageType.isWithdrawalOrPartialMessage(refTrx.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getIfxType()))
						  )? ifxOut.getTransaction().getFirstTransaction() : refTrx;
				updateRealAmount(atm, ifxOut, refTrx);
			}
		}
		
		functionCommandMsg.nextStateID = atmResponse.getNextState();
		functionCommandMsg.screenNumber = atmResponse.getScreen(ifxOut.getUserLanguage()).getScreenno();
				
		//TODO: double check this line, change from getByteOfReciept to ndcconvertor!
		functionCommandMsg.screenDisplayUpdateData = convertor.convert(null, atmResponse.getScreen(ifxOut.getUserLanguage())./*getScreenData()*/getReplacedScreenData(), ifxOut);
		functionCommandMsg.cardReturnRetainFlag = NDCCardRetainFlagConstants.getByBoolean(atmResponse.getCradRetain());

		for (Receipt r : atmResponse.getReceipt(ifxOut.getUserLanguage())) { // SwitchApplication.get().getAtmTerminalService().getReceipt(atmResponse.getId())){
        	if(atm.getProducer() != null && atm.getProducer().equals(ATMProducer.NCR) && NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY.equals(r.getPrinterFlag())){
        		ByteArrayOutputStream out = new ByteArrayOutputStream();
        		
        		out.write("2".getBytes());
        		
//        		out.write(ASCIIConstants.ESC);
//        		out.write(0x3A);
//        		out.write("000".getBytes());
//        		
//        		out.write(ASCIIConstants.ESC);
//        		out.write(0x26);
//        		out.write("FANAPFA.FNT".getBytes());
//        		out.write(ASCIIConstants.ESC);
//        		out.write(0x5c);
//

        		out.write(ASCIIConstants.ESC);
        		out.write(0x5b);
        		out.write("001".getBytes()); //Line Spacing: 7.5
        		out.write(0x72);

        		out.write(ASCIIConstants.ESC);
        		out.write(0x5b);
        		out.write("049".getBytes()); //Form Length: 40 (=> 40)
        		out.write(0x73);
        		
        		out.write(ASCIIConstants.ESC);
        		out.write(0x5b);
        		out.write("020".getBytes()); //Top Margin: 20
        		out.write(0x75);
        		
        		out.write(ASCIIConstants.ESC);
        		out.write(0x25);
        		out.write("000".getBytes());

        		//convert method has been changed
        		out.write(convertor.convert(r.getPrinterFlag(), r.getReplacedText()/*r.getText()*/, ifxOut));
//        		out.write(convertor.convert(r.getPrinterFlag(), "[GR simpleCharTable()]", ifxOut));
        		
//        		out.write(0x92);
//        		out.write(0x20);
//        		out.write(0x93);
//        		out.write(0x20);
//        		out.write(0x94);
//        		out.write(0x20);
//        		out.write(0x95);
//        		out.write(0x20);
//        		out.write(ASCIIConstants.FF);
        		functionCommandMsg.printerData.add(out.toByteArray());            		
        	}else{            		
        		functionCommandMsg.printerData.add(convertor.convert(r.getPrinterFlag(), r.getReplacedText(), ifxOut));
        	}

//			functionCommandMsg.printerData.add(GlobalContext.getInstance().getReceipt(r).getBytes());
//			functionCommandMsg.printerData.add(convertor.convert(r.getPrinterFlag(),GlobalContext.getInstance().getReceipt(r) , ifxOut));
		}
		
		functionCommandMsg.functionIdentifier = atmResponse.getFunctionCommand();
   		//if(functionCommandMsg.functionIdentifier.equals(NDCFunctionIdentifierConstants.CASHIN)){
   			//functionCommandMsg.bufferData=atmResponse.getBuf_Id();   			
   	//	}
		functionCommandMsg.logicalUnitNumber = Long.valueOf(ifxOut.getTerminalId());

//		StringFormat format = new StringFormat(4, StringFormat.JUST_RIGHT);
		functionCommandMsg.messageSequenceNumber = networkRefIdToTimeVariant(ifxOut.getNetworkRefId()); 
		functionCommandMsg.transactionSerialNumber = StringFormat.formatNew(4, StringFormat.JUST_RIGHT, ifxOut.getSrc_TrnSeqCntr(), '0');
		functionCommandMsg.messageCoordinationNumber = ifxOut.getCoordinationNumber();
		String authCode = ifxOut.getMsgAuthCode();
		functionCommandMsg.MAC = (authCode != null && !authCode.equals("")) ? authCode.substring(0, 8) : "01020304";
		return functionCommandMsg;
	}

	private static void updateRealAmount(ATMTerminal atm, Ifx ifxOut,Transaction refTransaction){
		Ifx refIfxRq = refTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/;
		Ifx refIfxRs;
		Transaction withdrawalRespTrx = TransactionService.findWithdrawalResponse(refTransaction);
		
		if(withdrawalRespTrx == null || withdrawalRespTrx.getInputMessage() == null || withdrawalRespTrx.getIncomingIfx()/*getInputMessage().getIfx()*/ ==null){
			return;
		}
		
		refIfxRs = /*TransactionService.findWithdrawalResponse(refTransaction)*/withdrawalRespTrx.getIncomingIfx()/*getInputMessage().getIfx()*/; 
		ATMSpecificData  atmSpecificData = ifxOut.getAtmSpecificData();
		
		CassetteA cassetteA = atm.getDevice(CassetteA.class);
		CassetteB cassetteB = atm.getDevice(CassetteB.class);
		CassetteC cassetteC = atm.getDevice(CassetteC.class);
		CassetteD cassetteD = atm.getDevice(CassetteD.class);
		Integer denominationA = (cassetteA.getDenomination()==null)?0:cassetteA.getDenomination();
		Integer denominationB = (cassetteB.getDenomination()==null)?0:cassetteB.getDenomination();
		Integer denominationC = (cassetteC.getDenomination()==null)?0:cassetteC.getDenomination();
		Integer denominationD = (cassetteD.getDenomination()==null)?0:cassetteD.getDenomination();
		
		if(atmSpecificData == null || atmSpecificData.getCurrentDispense() == null || atmSpecificData.getCurrentDispense().equals("")){
			return;
		}
		
		Integer cassette1 = Integer.parseInt(atmSpecificData.getCurrentDispense().substring(0, 2)) + atmSpecificData.getActualDispenseCaset1();
		Integer cassette2 = Integer.parseInt(atmSpecificData.getCurrentDispense().substring(2, 4))+ atmSpecificData.getActualDispenseCaset2();
		Integer cassette3 = Integer.parseInt(atmSpecificData.getCurrentDispense().substring(4, 6))+ atmSpecificData.getActualDispenseCaset3();
		Integer cassette4 = Integer.parseInt(atmSpecificData.getCurrentDispense().substring(6, 8))+ atmSpecificData.getActualDispenseCaset4();
		
		Long real_Amt = new Long(cassette1*denominationA + cassette2* denominationB + cassette3* denominationC + cassette4* denominationD);
		refIfxRq.setReal_Amt(real_Amt);
		refIfxRs.setReal_Amt(real_Amt);
		ifxOut.setReal_Amt(real_Amt);
		GeneralDao.Instance.saveOrUpdate(refIfxRq);
		GeneralDao.Instance.saveOrUpdate(refIfxRs);
		GeneralDao.Instance.saveOrUpdate(ifxOut);
	}
	
	public static String getNotesInOneStep(int[] allCassettes, int NoteInOneStep, Integer cas1, Integer cas2, Integer cas3 , Integer cas4) {
//		StringFormat format = new StringFormat(2, StringFormat.JUST_RIGHT);
		String result = "";
		cas1 = allCassettes[0] - cas1;
		cas2 = allCassettes[1] - cas2;
		cas3 = allCassettes[2] - cas3;
		cas4 = allCassettes[3] - cas4;

		if (cas1 < NoteInOneStep){
			result = StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "" + cas1, '0');
		} else {
			result = StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "" + NoteInOneStep, '0');
			result += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "0", '0');
			result += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "0", '0');
			result += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "0", '0');
			return result;
		}
		
		if (cas1 + cas2 < NoteInOneStep){
			result += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "" + cas2, '0');
		} else {
			result += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "" + (cas2 - (cas1 + cas2 - NoteInOneStep)), '0');
			result += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "0", '0');
			result += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "0", '0');
			return result;
		}
		
		if (cas1 + cas2 + cas3 < NoteInOneStep){
			result += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "" + cas3, '0');
		} else {
			result += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "" + (cas3 - (cas1 + cas2 + cas3 - NoteInOneStep)), '0');
			result += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "0", '0');
			return result;
		}
		
		if (cas1 + cas2 + cas3 + cas4 < NoteInOneStep){
			result += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "" + cas4, '0');
		} else {
			result += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "" + (cas4 - (cas1 + cas2 + cas3 + cas4 - NoteInOneStep)), '0');
			return result;
		}
		
		return result;
	}

	public static NDCResponseMsg getResponseMessage(Ifx ifx, OARResponse atmRs, NDCConvertor convertor) {
		// generate OARCommand!
		NDCWriteCommandOARMsg oarMsg = new NDCWriteCommandOARMsg();
		oarMsg.logicalUnitNumber = Long.valueOf(ifx.getTerminalId());
		oarMsg.messageSequenceNumber = ifx.getNetworkRefId();
		oarMsg.displayFlag = atmRs.getDisplayFlag() != null ? atmRs.getDisplayFlag().getValue() : ATMDisplayFlag.DISPLAY.getValue();

		oarMsg.activeKeys = new NDCActiveKeys(atmRs.isAllNumericKeys(), false, false, false, false, true/*atmRs.isCancelKey()*/, 
				false, false, false, false);
//		oarMsg.activeKeys = new NDCActiveKeys(atmRs.isAllNumericKeys(), atmRs.isOpKeyA(), atmRs.isOpKeyB(), atmRs
//				.isOpKeyC(), atmRs.isOpKeyD(), atmRs.isCancelKey(), atmRs.isOpKeyF(), atmRs.isOpKeyG(), atmRs
//				.isOpKeyH(), atmRs.isOpKeyI());

		oarMsg.screenTimer = atmRs.getScreenTimer();
		try {
			String screenData = "";
			
			if (ISOFinalMessageType.isGetAccountMessage(ifx.getIfxType())) {
				List<CardAccountInformation> data = ifx.getCardAccountInformation();
				ResponseScreen responseScreen = atmRs.getScreen(ifx.getUserLanguage());
				screenData = (responseScreen!=null)? responseScreen.getReplacedScreenData()/*getScreenData()*/: "";
				//TASK Task141 : NCR
				if(ifx.getUserLanguage().equals(UserLanguage.FARSI_LANG)){
					
					String[][] position = {
							{"A", "F4"},
							{"B", "I4"},
							{"C", "L4"},
							{"D", "O4"}, 
							{"F", "OA"},
							{"G", "LA"},
							{"H", "IA"},
							{"I", "FA"},
							};
					ATMTerminal atm = null;
					if (ifx.getEndPointTerminal() instanceof ATMTerminal) {
						atm = (ATMTerminal) ifx.getEndPointTerminal();
					}
					if (atm != null && atm.getProducer() != null && atm.getProducer().equals(ATMProducer.NCR)) {
						position = new String[][]{
								{"A", "FL"},
								{"B", "IL"},
								{"C", "LL"},
								{"D", "OL"}, 
								{"F", "OA"},
								{"G", "LA"},
								{"H", "IA"},
								{"I", "FA"},
								};						
					}
					
					//orig before Task141 : ncr
//					String[][] position = {
//							{"A", "F4"},
//							{"B", "I4"},
//							{"C", "L4"},
//							{"D", "O4"}, 
//							{"F", "OA"},
//							{"G", "LA"},
//							{"H", "IA"},
//							{"I", "FA"},
//							};
					
					
					for (int i = 0; i < 7 && i < data.size(); i++) {
						CardAccountInformation item = data.get(i);
						String accountNumber = item.getAccountNumber();
						String index = position[i][0];
						item.setIndex(index);
						GeneralDao.Instance.saveOrUpdate(item);
//						screenData += "[SI]" + position[i][1] + "[GR accountFormat('" + accountNumber + "')]";
						screenData += "[SI]" + position[i][1] + "[GR convertor.accountFormat(text_dir,'" + accountNumber + "')]";
						
	//					screenData += "[SI]" + position[i][1] + "[GR reverse(c2F('" + accountNumber + "'))]"; 
						
						if ("A".equals(index))
							oarMsg.activeKeys.opKeyA = '1';
							
						if ("B".equals(index))
							oarMsg.activeKeys.opKeyB = '1';
						
						if ("C".equals(index))
							oarMsg.activeKeys.opKeyC = '1';
						
						if ("D".equals(index))
							oarMsg.activeKeys.opKeyD = '1';
						
						if ("F".equals(index))
							oarMsg.activeKeys.opKeyF = '1';
						
						if ("G".equals(index))
							oarMsg.activeKeys.opKeyG = '1';
						
						if ("H".equals(index))
							oarMsg.activeKeys.opKeyH = '1';
						
						if ("I".equals(index))
							oarMsg.activeKeys.opKeyI = '1';
						
					}
	//				oarMsg.screenData = screenData;
					oarMsg.screenData = convertor.convert(null, screenData, ifx);
				}else if(ifx.getUserLanguage().equals(UserLanguage.ENGLISH_LANG)){  //TASK Task141 : NCR
					String[][] position = {
							{"A", "F4"},
							{"B", "I4"},
							{"C", "L4"},
							{"D", "O4"}, 
							{"F", "OA"},
							{"G", "LA"},
							{"H", "IA"},
							{"I", "FA"},
							};
					ATMTerminal atm = null;
					if (ifx.getEndPointTerminal() instanceof ATMTerminal) {
						atm = (ATMTerminal) ifx.getEndPointTerminal();
					}
					if (atm != null && atm.getProducer() != null && atm.getProducer().equals(ATMProducer.NCR)) {
						position = new String[][]{
								{"A", "FL"},
								{"B", "IL"},
								{"C", "LL"},
								{"D", "OL"}, 
								{"F", "OA"},
								{"G", "LA"},
								{"H", "IA"},
								{"I", "FA"},
								};						
					}
					
					//orig before Task141 : ncr
//					String[][] position = {
//							{"A", "F4"},//ina bayad tagheer kone ke jaie dorost beiofte shomare hesab va inke yedoor farda reset ko atm ro age ramz jaie dorost nabud
//							{"B", "I4"},
//							{"C", "L4"},
//							{"D", "O4"}, 
//							{"F", "OA"},
//							{"G", "LA"},
//							{"H", "IA"},
//							{"I", "FA"},
//							};

					
					for (int i = 0; i < 7 && i < data.size(); i++) {
						
						CardAccountInformation item = data.get(i);
						String accountNumber = item.getAccountNumber();
						String index = position[7-i][0];//me
						item.setIndex(index);
						GeneralDao.Instance.saveOrUpdate(item);
//						screenData += "[SI]" + position[7-i][1] + "[GR accountFormat('" + accountNumber + "')]"; 
						screenData += "[SI]" + position[7-i][1] + "[GR convertor.accountFormat(text_dir,'" + accountNumber + "')]";
	//					screenData += "[SI]" + position[i][1] + "[GR reverse(c2F('" + accountNumber + "'))]"; 
						
						if ("A".equals(index))
							oarMsg.activeKeys.opKeyA = '1';
							
						if ("B".equals(index))
							oarMsg.activeKeys.opKeyB = '1';
						
						if ("C".equals(index))
							oarMsg.activeKeys.opKeyC = '1';
						
						if ("D".equals(index))
							oarMsg.activeKeys.opKeyD = '1';
						
						if ("F".equals(index))
							oarMsg.activeKeys.opKeyF = '1';
						
						if ("G".equals(index))
							oarMsg.activeKeys.opKeyG = '1';
						
						if ("H".equals(index))
							oarMsg.activeKeys.opKeyH = '1';
						
						if ("I".equals(index))
							oarMsg.activeKeys.opKeyI = '1';
						
					}
	//				oarMsg.screenData = screenData;
					oarMsg.screenData = convertor.convert(null, screenData, ifx);
				}
			
			} else {
//				buildScreenData(atmRs.getScreenData(), ifx);
//				oarMsg.screenData = NDCParserUtils.getByteOfReciept(null, screenData, ifx).toString();
			}
			
			
		} catch (IOException e) {
			logger.error(e.getClass().getSimpleName()+": "+ e.getMessage(), e);
		}


		String authCode = ifx.getMsgAuthCode();
		oarMsg.MAC = (authCode != null && !authCode.equals("")) ? authCode.substring(0, 8) : null;

		return oarMsg;
	}

	public static ATMResponse findResponse(ATMRequest atmRequest, int rsCode) {
		ATMResponse atmResponse = atmRequest.getAtmResponse(rsCode);
		if (atmResponse == null) {
			ATMConfiguration configuration = GeneralDao.Instance.getObject(ATMConfiguration.class, atmRequest.getConfiguration().getId());
			atmResponse = configuration.getResponse(atmRequest.getFit(), rsCode);
		}
		return atmResponse;
	}

	public static NDCWriteCommandMsg generateParameterAndTimerTableLoadMessage(NDCMsg msg, List<EnhancedParameterData> params, List<TimerData> timers) {
		NDCWriteCommandEnhancedParameterTableLoadMsg ndcMsg;
		try {
			ndcMsg = new NDCWriteCommandEnhancedParameterTableLoadMsg();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int index = 0;
			for (index = 0; index < params.size()/*ConfigDataLength.MAX_PARAM_IN_LENGTH && index < length*/; index++) {
				ATMCustomizationData f = params.get(index);
				out.write(f.getValueForDownload());
			}
			
			out.write(ASCIIConstants.FS);
				
			for (int i = 0; i < timers.size(); i++, index++) {
				ATMCustomizationData f = timers.get(i);
				out.write(f.getValueForDownload());
			}
				
			ndcMsg.allConfigData = out.toByteArray();
			ndcMsg.writeIdentifier = NDCConstants.WRITE_IDENTIFIER;

//			ndcMsg.logicalUnitNumber = msg.getLogicalUnitNumber();
			ndcMsg.logicalUnitNumber = /*242500L;*/ msg.getLogicalUnitNumber();
			ndcMsg.messageSequenceNumber = "111";
//			ndcMsg.MAC = "01020304";

			return ndcMsg;
		} catch (IOException e) {
			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
		}
		return null;
	}
	
	@NotUsed
	public static NDCWriteCommandMsg generateParameterTableLoadMessage(NDCMsg msg, List<EnhancedParameterData> params, int offset, int length) {
		NDCWriteCommandEnhancedParameterTableLoadMsg ndcMsg;
		try {
			ndcMsg = new NDCWriteCommandEnhancedParameterTableLoadMsg();
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			for (int index = 0; index < CustomizationDataLength.MAX_PARAM_IN_LENGTH && index < length; index++) {
				ATMCustomizationData f = params.get(index + offset);
				out.write(f.getValueForDownload());
//				out.write(ASCIIConstants.FS);
			}
			
			ndcMsg.allConfigData = out.toByteArray();
			ndcMsg.writeIdentifier = NDCConstants.WRITE_IDENTIFIER;
			
//			ndcMsg.logicalUnitNumber = 0L;//msg.getLogicalUnitNumber();
			ndcMsg.logicalUnitNumber = msg.getLogicalUnitNumber();
			ndcMsg.messageSequenceNumber = "111";
//			ndcMsg.MAC = "01020304";
			
			return ndcMsg;
		} catch (IOException e) {
			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
		}
		return null;
	}
	
	@NotUsed
	public static NDCWriteCommandMsg generateTimerTableLoadMessage(NDCMsg msg, List<TimerData> params, int offset, int length) {
		NDCWriteCommandEnhancedParameterTableLoadMsg ndcMsg;
		try {
			ndcMsg = new NDCWriteCommandEnhancedParameterTableLoadMsg();
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			for (int index = 0; index < CustomizationDataLength.MAX_TIMER_IN_LENGTH && index < length; index++) {
				ATMCustomizationData f = params.get(index + offset);
				out.write(f.getValueForDownload());
//				out.write(ASCIIConstants.FS);
			}
			
			ndcMsg.allConfigData = out.toByteArray();
			ndcMsg.writeIdentifier = NDCConstants.WRITE_IDENTIFIER;
			
//			ndcMsg.logicalUnitNumber = 0L;//msg.getLogicalUnitNumber();
			ndcMsg.logicalUnitNumber = msg.getLogicalUnitNumber();
			ndcMsg.messageSequenceNumber = "111";
//			ndcMsg.MAC = "01020304";
			
			return ndcMsg;
		} catch (IOException e) {
			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
		}
		return null;
	}
	
	public static NDCWriteCommandMsg generateFITTableLoadMessage(NDCMsg msg, List<FITData> fits, int offset, int length) {
		NDCWriteCommandFitTableLoadMsg ndcMsg;
		try {
			ndcMsg = new NDCWriteCommandFitTableLoadMsg();
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			for (int index = 0; index < CustomizationDataLength.MAX_FITS_IN_MSG && index < length; index++) {
				ATMCustomizationData f = fits.get(index + offset);
				out.write(f.getValueForDownload());
				out.write(ASCIIConstants.FS);
			}

			byte[] bOut=out.toByteArray();
			byte[] bAllConfigData = new byte[bOut.length-1];
			
			System.arraycopy(bOut, 0, bAllConfigData, 0, bAllConfigData.length);
			ndcMsg.allConfigData = bAllConfigData;
			ndcMsg.writeIdentifier = NDCConstants.WRITE_IDENTIFIER;
			
			ndcMsg.logicalUnitNumber = msg.getLogicalUnitNumber();
			ndcMsg.messageSequenceNumber = "111";
			ndcMsg.MAC = "01020304";
			
			return ndcMsg;
		} catch (IOException e) {
			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
		}
		return null;
	}

	public static NDCWriteCommandMsg generateStateTableLoadMessage(Long logicalUnitNumber, List<StateData> states, int offset, int length) {
		NDCWriteCommandStateTableLoadMsg ndcMsg;
		try {
			ndcMsg = new NDCWriteCommandStateTableLoadMsg();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			for (int index = 0; index < CustomizationDataLength.MAX_STATES_IN_MSG && index < length; index++) {
				ATMCustomizationData f = states.get(index + offset);
				out.write(f.getValueForDownload());
				out.write(ASCIIConstants.FS);
			}

			byte[] bOut=out.toByteArray();
			byte[] bAllConfigData = new byte[bOut.length-1];
			
			System.arraycopy(bOut, 0, bAllConfigData, 0, bAllConfigData.length);
			ndcMsg.allConfigData = bAllConfigData;			
//			ndcMsg.allConfigData = out.toByteArray();
			ndcMsg.writeIdentifier = NDCConstants.WRITE_IDENTIFIER;

			ndcMsg.logicalUnitNumber = logicalUnitNumber;
			ndcMsg.messageSequenceNumber = "111";
			ndcMsg.MAC = "01020304";

			return ndcMsg;
		} catch (IOException e) {
			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
		}
		return null;
	}

	public static NDCWriteCommandMsg generateScreenTableLoadMessage(Long logicalUnitNumber, List<ScreenData> screens, int offset, MyInteger length) {
		NDCWriteCommandScreenTableLoadMsg ndcMsg;
		try {
			ndcMsg = new NDCWriteCommandScreenTableLoadMsg();

			int curLen = 0;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			for (int index = 0; index < screens.size() - offset && curLen < CustomizationDataLength.MAX_SCREEN_MSG_LENGTH; index++) {
				ATMCustomizationData f = screens.get(index + offset);
				byte[] b = f.getValueForDownload();
				// Hex.decode(f.getValueForDownload());
				out.write(b);
				out.write(ASCIIConstants.FS);
				curLen += (b.length + 1);
				length.value++;
			}
			ndcMsg.allConfigData = out.toByteArray();
			ndcMsg.writeIdentifier = NDCConstants.WRITE_IDENTIFIER;

			ndcMsg.logicalUnitNumber = logicalUnitNumber;
			ndcMsg.messageSequenceNumber = "111";
			ndcMsg.MAC = "01020304";

			return ndcMsg;
		} catch (IOException e) {
			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
		}
		return null;
	}

	public static NDCWriteCommandMsg generateConfigIdLoadMessage(NDCMsg msg, Integer configId) {
		NDCWriteCommandConfigurationIDLoadMsg ndcMsg;
		ndcMsg = new NDCWriteCommandConfigurationIDLoadMsg();

//		StringFormat format = new StringFormat(4, StringFormat.JUST_RIGHT);
		String configIdStr = StringFormat.formatNew(4, StringFormat.JUST_RIGHT, configId.toString(), '0');
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try
		{
			out.write(configIdStr.getBytes());
//			out.write(ASCIIConstants.FS);

			ndcMsg.allConfigData = out.toByteArray();
			ndcMsg.writeIdentifier = NDCConstants.WRITE_IDENTIFIER;

//			ndcMsg.logicalUnitNumber = 0L;//msg.getLogicalUnitNumber();
			ndcMsg.logicalUnitNumber = msg.getLogicalUnitNumber();
			ndcMsg.messageSequenceNumber = "111";
			ndcMsg.MAC = "01020304";
		} catch (IOException e)
		{
			logger.error(e.getClass().getSimpleName()+": "+ e.getMessage(), e);
//			e.printStackTrace();
		}

		return ndcMsg;
	}

//	public ArrayList<Receipt> getReceipt(Serializable functionCommandID) {
//		String query = "select f.receipt from " + FunctionCommandResponse.class.getName() + " as f where f.id = :functionCommandID";
//		Map<String, Object> parameters = new HashMap<String, Object>(1);
//		parameters.put("functionCommandID", functionCommandID);
//		return (ArrayList<Receipt>) GeneralDao.Instance.find(query, parameters, false);
//	}
	
	@NotUsed
	private static String buildScreenData(String screenData, Ifx ifx) {
		String result = "";
		if (ISOFinalMessageType.isGetAccountMessage(ifx.getIfxType())) {
			String allAccounts = "";// ifx.getAllAccounts();
			result += "086[ESC]P2086[ESC]\\" 
				+ "[ESC](7[ESC][OC]80;m" ;
			for (int i = 0; i < allAccounts.length();) {
				int len = Integer.parseInt(allAccounts.substring(i, i+2));
				String account = allAccounts.substring(i+2, i+2+len);
				result += "[SI]" + "DG" + account;
				i = i + 2 + len;
			}
		}
		return result;
	}
	
	public static ATMResponse getResponse(ATMConfiguration configuration, Integer fit, Integer rsCode) {
		if(configuration.getFitResponses() == null)
			return null;
		
		RsCodeResponses rsCodeResponses = configuration.getFitResponses().get(fit);
		if(rsCodeResponses == null || rsCodeResponses.getRsCodeResponses() == null)
			return null;
		
		return rsCodeResponses.getRsCodeResponses().get(rsCode);
	}
	
//	public RsCodeResponses getResponses(ATMConfiguration configuration, Integer fit, Integer rsCode) {
//		RsCodeResponses rsCodeResponses = configuration.getFitResponses().get(fit);
//		Set<RsCodeResponses> datas = rsCodeResponses.getRsCodeResponses().get(rsCode)
//		if (datas != null && rsCode != null) {
//			for (RsCodeResponses response : datas) {
//				if (response.getRsCode().equals(rsCode))
//					return response;
//			}
//		}
//		return null;
//	}
//	
//	public FitRsCodeResponses getRsCodeResponses(ATMConfiguration configuration, Integer fit) {
//		Map<FITType, FitRsCodeResponses> criterias = configuration.getCriterias();
//		if (criterias != null && fit != null) {
//			return criterias.get(fit);
////    		for (FitRsCodeResponses criteria: criterias) {
////    			if (criteria.getFit().equals(fit))
////    					return criteria;
////    		}
//    	}
//    	return null;
//	}

	public static Long getDispenseAmount(ATMTerminal atm, Ifx ifx) {
		Cassette cassetteA = atm.getDevice(CassetteA.class);
        Cassette cassetteB = atm.getDevice(CassetteB.class);
        Cassette cassetteC = atm.getDevice(CassetteC.class);
        Cassette cassetteD = atm.getDevice(CassetteD.class);
        Long amountDispensed = new Long(ifx.getActualDispenseCaset1() * cassetteA.getDenomination());        
        amountDispensed += ifx.getActualDispenseCaset2() * cassetteB.getDenomination();
        amountDispensed += ifx.getActualDispenseCaset3() * cassetteC.getDenomination();
        amountDispensed += ifx.getActualDispenseCaset4() * cassetteD.getDenomination();
		return amountDispensed;
	}
	
	public static void addDefaultKeySetForTerminal(ATMTerminal selectedTerminal) throws Exception {
//		SMAdapter adapter = SecurityComponent.getDefaultSecurityModule();
		
//		List<SecureDESKey> keysByType = new ArrayList<SecureDESKey>();
		String macKey = GlobalContext.getInstance().getATMKey();
		if (macKey != null && !macKey.isEmpty()) {
			Key mkey = new SecretKeySpec(Hex.decode(macKey), "DES");
			SecureDESKey keyMac = SecurityComponent.importKey(SMAdapter.LENGTH_DES, KeyType.TYPE_TAK, mkey.getEncoded(), null, false);
			selectedTerminal.addSecureKey(keyMac);
			GeneralDao.Instance.saveOrUpdate(keyMac);
		}

		String pinKey = GlobalContext.getInstance().getATMKey();
		if (pinKey != null && !pinKey.isEmpty()) {
			Key pkey = new SecretKeySpec(Hex.decode(pinKey), "DES");
			SecureDESKey keyPin = SecurityComponent.importKey(SMAdapter.LENGTH_DES, KeyType.TYPE_TPK, pkey.getEncoded(), null, false);
			selectedTerminal.addSecureKey(keyPin);
			GeneralDao.Instance.saveOrUpdate(keyPin);
		}

		String masterKey = GlobalContext.getInstance().getATMKey();
		if (masterKey != null && !masterKey.isEmpty()) {
			Key pkey = new SecretKeySpec(Hex.decode(masterKey), "DES");
			SecureDESKey keyMaster = SecurityComponent.importKey(SMAdapter.LENGTH_DES, KeyType.TYPE_TMK, pkey.getEncoded(), null, false);
			selectedTerminal.addSecureKey(keyMaster);
			GeneralDao.Instance.saveOrUpdate(keyMaster);
		}

		GeneralDao.Instance.saveOrUpdate(selectedTerminal);
	}
}
	
