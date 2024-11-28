package vaulsys.protocols.ui;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.calendar.DateTime;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.base.SettlementReport;
import vaulsys.clearing.base.SettlementState;
import vaulsys.clearing.report.ShetabDocumentService;
import vaulsys.clearing.settlement.ATMSettlementServiceImpl;
import vaulsys.clearing.settlement.SettlementService;
import vaulsys.cms.components.CMSStatusCodes;
import vaulsys.customer.Core;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.exception.SwitchSystemException;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.network.NetworkManager;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.base.ProtocolSecurityFunctions;
import vaulsys.protocols.exception.exception.NotProducedProtocolToBinaryException;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCNetworkToTerminalMsg;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.atm.ATMState;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.constants.CustomizationDataLength;
import vaulsys.terminal.atm.constants.NDCUtil;
import vaulsys.terminal.atm.customizationdata.ScreenData;
import vaulsys.terminal.atm.customizationdata.StateData;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.SettlementInfo;
import vaulsys.transaction.Transaction;
import vaulsys.util.MyInteger;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

public class UiMessageProcessorHandler extends BaseHandler {
	private static final Logger logger = Logger.getLogger(UiMessageProcessorHandler.class);
	private static final String ATM_CHANNEL = "channelNDCProcachIn";

	public static final UiMessageProcessorHandler Instance = new UiMessageProcessorHandler();
	
	private UiMessageProcessorHandler(){
	}

	@Override
	public void execute(ProcessContext processContext) throws Exception {
		Message incomingMessage = processContext.getInputMessage();
		Transaction transaction = incomingMessage.getTransaction();
		IfxType ifxType = incomingMessage.getIfx().getIfxType();

		Message outMessage = null;
		MessageObject messageObject = (MessageObject) incomingMessage.getProtocolMessage();

		if (IfxType.UI_ISSUE_SHETAB_DOCUMENT_RQ.equals(ifxType)) {
			outMessage = issueShetabDocument(transaction, messageObject, incomingMessage.getChannel());
		}
		else if(isATMManagement(ifxType)) {
//			Channel channel = GlobalContext.getInstance().getChannel(ATM_CHANNEL);
			Channel channel = ProcessContext.get().getChannel(ATM_CHANNEL);
			NetworkManager networkManager = NetworkManager.getInstance();
			List<Long> codes = (List<Long>) messageObject.getParameter("atms");
			for (Long code : codes) {
				ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, code);
				List<NDCNetworkToTerminalMsg> ndcMsgs = generateATMMsg(ifxType, atm, messageObject.getParameters());
				for (NDCNetworkToTerminalMsg ndcMsg : ndcMsgs) {
					Message outMsg = new Message(MessageType.OUTGOING);
					outMsg.setProtocolMessage(ndcMsg);
					outMsg.setTransaction(transaction);
					outMsg.setChannel(channel);
					outMsg.setEndPointTerminal(atm);
					outMsg.setRequest(true);
					outMsg.setNeedResponse(false);
					outMsg.setNeedToBeInstantlyReversed(false);
					outMsg.setNeedToBeSent(true); //FIXME: it should be false, must be correct
					outMsg.setStartDateTime(incomingMessage.getStartDateTime());
					outMsg.setXML(ndcMsg.toString());

					try {
						byte[] binary = channel.getProtocol().getMapper().toBinary(ndcMsg);
						outMsg.setBinaryData(binary);
						if (NDCUtil.isNeedSetMac(ndcMsg)){
							ProtocolSecurityFunctions securityFunctions = channel.getProtocol().getSecurityFunctions();
							securityFunctions.setMac(processContext, atm, atm.getOwnOrParentSecurityProfileId(), atm.getKeySet(), outMsg, channel.getMacEnable());
						}
						IoSession session = networkManager.getTerminalOpenConnection(atm.getIP());
						session.write(outMsg.getBinaryData());
					} catch (NotProducedProtocolToBinaryException e) {
						logger.error("Exception in ATM Message Management toBinary, ", e);
					}

					transaction.addOutputMessage(outMsg);
					Ifx outIfx = new Ifx();
					outIfx.setReceivedDt(incomingMessage.getStartDateTime());
					outIfx.setIfxDirection(IfxDirection.OUTGOING);
					outIfx.setIfxType(ifxType);
					outIfx.setTerminalId(atm.getCode().toString());
					outIfx.setTerminalType(TerminalType.ATM);
					outIfx.setRequest(true);
					outIfx.setTransaction(transaction);
					outIfx.setRsCode(messageObject.getResponseCode());
					outMsg.setIfx(outIfx);
					GeneralDao.Instance.saveOrUpdate(outMsg);
			        GeneralDao.Instance.saveOrUpdate(outMsg.getMsgXml());
					GeneralDao.Instance.saveOrUpdate(outIfx);
					
					Thread.sleep(1000);
				}
			}
		}
		else if (IfxType.UI_SETTLE_RQ.equals(ifxType)) {
			outMessage = settle(transaction, messageObject, incomingMessage.getChannel());
		}
		else if (IfxType.UI_ISSUE_CORE_DOCUMENT_RQ.equals(ifxType)) {
			outMessage = issueCoreDocument(transaction, messageObject, incomingMessage.getChannel());
		}
		else if(IfxType.SYSTEM_INITIALIZE_RQ.equals(ifxType)) //Raza INITIALIZE
		{
			outMessage = initialize(transaction, messageObject, incomingMessage.getChannel());
		}
		else {
			throw new SwitchSystemException("Not Supported IfxType: " + ifxType);
		}

		LifeCycle lifeCycle = new LifeCycle();
		lifeCycle.setIsComplete(true);
		GeneralDao.Instance.saveOrUpdate(lifeCycle);
		transaction.setLifeCycle(lifeCycle);
		
		if (outMessage != null) {
			transaction.addOutputMessage(outMessage);
			Ifx outIfx = MsgProcessor.processor(incomingMessage.getIfx());
			outIfx.setIfxType(IfxType.getResponseIfxType(ifxType));
			outMessage.setIfx(outIfx);
			GeneralDao.Instance.saveOrUpdate(outIfx);
			GeneralDao.Instance.saveOrUpdate(outMessage);
	        GeneralDao.Instance.saveOrUpdate(outMessage.getMsgXml());
		}
	}
	
	private boolean isATMManagement(IfxType type) {
		return IfxType.ATM_GO_OUT_OF_SERVICE.equals(type) ||
		       IfxType.ATM_GO_IN_SERVICE.equals(type) ||
		       IfxType.ATM_DATE_TIME_LOAD.equals(type) ||
		       IfxType.MASTER_KEY_CHANGE_RQ.equals(type) ||
		       IfxType.MAC_KEY_CHANGE_RQ.equals(type) ||
		       IfxType.PIN_KEY_CHANGE_RQ.equals(type) ||
		       IfxType.ATM_STATE_TABLE_LOAD.equals(type) ||
		       IfxType.ATM_SCREEN_TABLE_LOAD.equals(type) ||
		       IfxType.ATM_GET_ALL_KVV.equals(type);
	}

	/* The list return is because of multi-messages send commands like 
	 * StateTabelLoad
	 */
	private List<NDCNetworkToTerminalMsg> generateATMMsg(IfxType type, ATMTerminal atm, Map<String, Serializable> params) {
		if(IfxType.ATM_GO_OUT_OF_SERVICE.equals(type)){
	        atm.setATMState(ATMState.OUT_OF_SERVICE);
	        atm.setCurrentAbstractStateClass((AbstractState) null);
	        GeneralDao.Instance.saveOrUpdate(atm);
			return Arrays.asList(ATMTerminalService.generateGoOutOfServiceMessage(atm.getCode()));
		}
		else if(IfxType.ATM_GO_IN_SERVICE.equals(type)){
	        atm.setATMState(ATMState.IN_SERIVCE);
	        atm.setCurrentAbstractStateClass((AbstractState) null);
	        GeneralDao.Instance.saveOrUpdate(atm);
			return Arrays.asList(ATMTerminalService.generateGoInServiceMessage(atm.getCode()));
		}
		else if(IfxType.ATM_DATE_TIME_LOAD.equals(type)) {
			return Arrays.asList(ATMTerminalService.generateDateTimeLoadMessage(atm.getCode()));
		}
		else if(IfxType.MASTER_KEY_CHANGE_RQ.equals(type)){
			return Arrays.asList(ATMTerminalService.generateExtEncKeyChngMsg_newMasterByCurMaster(atm.getCode()));
		}
		else if(IfxType.MAC_KEY_CHANGE_RQ.equals(type)){
			return Arrays.asList(ATMTerminalService.generateExtEncKeyChngMsg_MACByMaster(atm.getCode()));
		}
		else if(IfxType.PIN_KEY_CHANGE_RQ.equals(type)){
			return Arrays.asList(ATMTerminalService.generateExtEncKeyChngMsg_PINByMaster(atm.getCode()));
		}
		else if(IfxType.ATM_STATE_TABLE_LOAD.equals(type)) {
			int lastIndex = 0;
			if(params.containsKey("last_index"))
				lastIndex = (Integer) params.get("last_index");
			Integer configId = null;
			if(params.containsKey("config_id"))
				configId = (Integer) params.get("config_id");
			List<StateData> states = ATMTerminalService.getCustomizationDataAfter(atm, StateData.class,	configId);
			List<NDCNetworkToTerminalMsg> sendingNDCMsgs = new ArrayList<NDCNetworkToTerminalMsg>();
			while (lastIndex < states.size()) {
				int length = Math.min(CustomizationDataLength.MAX_STATES_IN_MSG, states.size()-lastIndex);
				NDCNetworkToTerminalMsg sendStateMessage = ATMTerminalService.
					generateStateTableLoadMessage(atm.getCode(), states, lastIndex, length);
				if (sendStateMessage != null) {
					lastIndex += length;
					atm.setLastSentStateIndex(lastIndex);
					sendingNDCMsgs.add(sendStateMessage);
				}
				else
					break;
			}
			if(lastIndex==states.size())
				atm.setLastSentStateIndex(0);
			GeneralDao.Instance.saveOrUpdate(atm);
			logger.debug("UI Message Handler -> ATM_STATE_TABLE_LOAD -> No of msgs: " + sendingNDCMsgs.size());
			return sendingNDCMsgs;
		}
		else if(IfxType.ATM_SCREEN_TABLE_LOAD.equals(type)) {
			int lastIndex = 0;
			if(params.containsKey("last_index"))
				lastIndex = (Integer) params.get("last_index");
			Integer configId = null;
			if(params.containsKey("config_id"))
				configId = (Integer) params.get("config_id");
			List<ScreenData> screens = ATMTerminalService.getCustomizationDataAfter(atm, ScreenData.class, configId);
			List<NDCNetworkToTerminalMsg> sendingNDCMsgs = new ArrayList<NDCNetworkToTerminalMsg>();
			while (lastIndex < screens.size()) {
				MyInteger length = new MyInteger(0);
				NDCNetworkToTerminalMsg sendStateMessage = ATMTerminalService.
					generateScreenTableLoadMessage(atm.getCode(), screens, lastIndex, length);
				if (sendStateMessage != null) {
					lastIndex += length.value;
					atm.setLastSentStateIndex(lastIndex);
					sendingNDCMsgs.add(sendStateMessage);
				}
				else
					break;
			}
			if(lastIndex==screens.size())
				atm.setLastSentScreenIndex(0);
			GeneralDao.Instance.saveOrUpdate(atm);
			logger.debug("UI Message Handler -> ATM_SCREEN_TABLE_LOAD -> No of msgs: " + sendingNDCMsgs.size());
			return sendingNDCMsgs;
		}
		else if(IfxType.ATM_GET_ALL_KVV.equals(type)) {
			return Arrays.asList(ATMTerminalService.generateExtEncKeyChngMsg_acquireAllKVV(atm.getCode()));
		}

		throw new RuntimeException("IfxType not implemented!");
	}

	private Message issueCoreDocument(Transaction transaction, MessageObject messageObject, Channel channel) {

		String result = "";
		String responseCode = ISOResponseCodes.APPROVED;

		List<Long> ids = (List<Long>) messageObject.getParameter("settlementStates");
		List<SettlementState> settlementStates = new ArrayList<SettlementState>();
		String query = String.format("from %s s where s.id in (:list)", SettlementState.class.getName());
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("list", ids);
		settlementStates = GeneralDao.Instance.find(query, params);

		/*
		 * List<SettlementState> settlementStates = new
		 * ArrayList<SettlementState>(); SettlementState settlementState =
		 * getGeneralDao().load(SettlementState.class, 27300104L);
		 * settlementStates.add(settlementState);
		 */

		try {
			if (settlementStates != null && !settlementStates.isEmpty()) {
				Map<ClearingProfile, List<SettlementState>> clrProfileMap = new HashMap<ClearingProfile, List<SettlementState>>();
				List<SettlementReport> reports = new ArrayList<SettlementReport>();
				for (SettlementState state : settlementStates) {
					List<SettlementReport> settlementReports = AccountingService.findSettlementReport(state, Core.FANAP_CORE);
					if (settlementReports != null && !settlementReports.isEmpty()) {
						reports.addAll(settlementReports);
					} else {
						ClearingProfile clearingProfile = state.getClearingProfile();
						List<SettlementState> states = clrProfileMap.get(clearingProfile);
						if (states == null) {
							states = new ArrayList<SettlementState>();
							clrProfileMap.put(clearingProfile, states);
						}
						states.add(state);
					}
				}

				for (ClearingProfile clr : clrProfileMap.keySet()) {
					SettlementService settlementService = ClearingService.getSettlementService(clr);
					try {
						settlementService.generateDocumentSettlementState(clrProfileMap.get(clr));
					} catch (Exception e) {
					}
				}

				for (SettlementReport report : reports) {
					String documentNumber = AccountingService.issueFCBDocument(report, false);
					if (documentNumber != null) {
						report.setDocumentNumber(documentNumber);
						GeneralDao.Instance.saveOrUpdate(report);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Encounter with an exception in UI_IssueCore_RQ (" + e + ": " + e.getMessage());
			responseCode = ISOResponseCodes.INVALID_CARD_STATUS;
			result += e.getClass().getSimpleName() + ": " + e.getMessage();
		}

		MessageObject object = new MessageObject();
		object.setIfxType(IfxType.UI_ISSUE_CORE_DOCUMENT_RS);
		object.setParameters(new HashMap<String, Serializable>());
		object.getParameters().put("result", result);
		object.setResponseCode(responseCode);
		Message outMessage = new Message(MessageType.OUTGOING);
		outMessage.setProtocolMessage(object);
		outMessage.setTransaction(transaction);
		outMessage.setRequest(false);
		outMessage.setNeedResponse(false);
		outMessage.setNeedToBeInstantlyReversed(false);
		outMessage.setNeedToBeSent(true);
		outMessage.setChannel(channel);

		return outMessage;
	}

	private Message issueShetabDocument(Transaction transaction, MessageObject messageObject, Channel channel) {
		byte[] bytes = (byte[]) messageObject.getParameters().get("body");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		InputStreamReader streamReader = new InputStreamReader(inputStream);
		BufferedReader brShetabReport = new BufferedReader(streamReader);
		String result = "";
		String responseCode = "00";
		try {
			result = ShetabDocumentService.issueShetabDocument(brShetabReport);
		} catch (Exception e) {
			logger.error("Encounter with exception in issuing Shetab documents. (" + e.getClass().getSimpleName()
					+ ": " + e.getMessage() + ")");
			responseCode = ISOResponseCodes.INVALID_CARD_STATUS;
			result = e.getClass().getSimpleName() + ": " + e.getMessage();
		}
		MessageObject object = new MessageObject();
		object.setParameters(new HashMap<String, Serializable>());
		object.getParameters().put("result", result);
		object.setResponseCode(responseCode);
		Message outMessage = new Message(MessageType.OUTGOING);
		outMessage.setProtocolMessage(object);
		outMessage.setTransaction(transaction);
		outMessage.setRequest(false);
		outMessage.setNeedResponse(false);
		outMessage.setNeedToBeInstantlyReversed(false);
		outMessage.setNeedToBeSent(true);
		outMessage.setChannel(channel);

		return outMessage;
	}

	private Message settle(Transaction transaction, MessageObject messageObject, Channel channel) {
		List<Long> terminalCodes = (List<Long>) messageObject.getParameter("terminalCodes");

		String result = "";
		String responseCode = ISOResponseCodes.APPROVED;
		if (terminalCodes != null && !terminalCodes.isEmpty()) {
			Map<ClearingProfile, List<Terminal>> clrProfileMap = new HashMap<ClearingProfile, List<Terminal>>();
			DateTime now = (DateTime) messageObject.getParameters().get("settlementTime");
			if (now == null)
				now = DateTime.now();

			// List<Terminal> terminals = new ArrayList<Terminal>();
			for (Long code : terminalCodes) {
				Terminal terminal = TerminalService.findTerminal(Terminal.class, code);
				if (terminal == null)
					result += "Invalid Terminal Code: " + code + "\n";
				// terminals.add(terminal);
				ClearingProfile clearingProfile = ProcessContext.get().getClearingProfile(terminal.getOwnOrParentClearingProfileId());
				List<Terminal> terminals = clrProfileMap.get(clearingProfile);
				if (terminals == null) {
					terminals = new ArrayList<Terminal>();
					clrProfileMap.put(clearingProfile, terminals);
				}
				terminals.add(terminal);
			}

			try {
				for (ClearingProfile clearingProfile : clrProfileMap.keySet()) {

					/*
					 * CycleSettlementJob settlementJob =
					 * getSchedulerService().getCycleSettlementJob
					 * (clearingProfile); if (settlementJob == null) result +=
					 * "There is no settlementJob for this clearingProfile " +
					 * clearingProfile.getName() + "-" +
					 * clearingProfile.getId();
					 */

					SettlementService settlementService = ClearingService.getSettlementService(clearingProfile);
					if (settlementService == null)
						result += "There is no settlementService for this clearingProfile " + clearingProfile.getName()
								+ "-" + clearingProfile.getId();

					logger.debug("Trying to Settle terminals with clearingProfile " + clearingProfile.getName() + "-"
							+ clearingProfile.getId());

					settlementService.settle(clrProfileMap.get(clearingProfile), clearingProfile, clearingProfile
							.getSettleUntilTime(now), true, false, true, false);
					// settlementJob.getSettlementService().account(clrProfileMap.get(clearingProfile),
					// clearingProfile,
					// clearingProfile.getSettleUntilTime(now),clearingProfile.getSettleUntilTime(now),
					// true);
					logger.debug("terminals with clearingProfile " + clearingProfile.getName() + "-"
							+ clearingProfile.getId() + " is settled");
				}
			} catch (Exception e) {
				logger.error("Encounter with an exception in UI_Settlement_RQ (" + e + ": " + e.getMessage());
				responseCode = ISOResponseCodes.INVALID_CARD_STATUS;
				result += e.getClass().getSimpleName() + ": " + e.getMessage();
			}
		}

		MessageObject object = new MessageObject();
		object.setIfxType(IfxType.UI_SETTLE_RS);
		object.setParameters(new HashMap<String, Serializable>());
		object.getParameters().put("result", result);
		object.setResponseCode(responseCode);
		Message outMessage = new Message(MessageType.OUTGOING);
		outMessage.setProtocolMessage(object);
		outMessage.setTransaction(transaction);
		outMessage.setRequest(false);
		outMessage.setNeedResponse(false);
		outMessage.setNeedToBeInstantlyReversed(false);
		outMessage.setNeedToBeSent(true);
		outMessage.setChannel(channel);

		return outMessage;
	}

	public Message returnTransactions(Transaction transaction, MessageObject messageObject, Channel channel){
		List<Long> setlInfoCodes = (List<Long>) messageObject.getParameter("settlementInfo_codes");

		String result = "";
		String responseCode = ISOResponseCodes.APPROVED;
		if (setlInfoCodes != null && !setlInfoCodes.isEmpty()) {
			Map<ClearingProfile, List<SettlementInfo>> stlInfoMap = new HashMap<ClearingProfile, List<SettlementInfo>>();
			// List<Terminal> terminals = new ArrayList<Terminal>();
			for (Long code : setlInfoCodes) {
				SettlementInfo settlementInfo = null;
				try {
					settlementInfo = GeneralDao.Instance.load(SettlementInfo.class, code);
				} catch (Exception e) {
				}
				if (settlementInfo == null){
					result += "Invalid SettlementInfo Code: " + code + "\n";
					continue;
				}

				SettlementData settlementData = settlementInfo.getSettlementData();
				if (settlementData == null){
					result += "SettlementInfo " + code + ": has no settlementData\n";
					continue;
				}

				ClearingProfile clearingProfile = ProcessContext.get().getClearingProfile(settlementData.getTerminal().getOwnOrParentClearingProfileId());
				List<SettlementInfo> stlInfoList = stlInfoMap.get(clearingProfile);
				if (stlInfoList == null) {
					stlInfoList = new ArrayList<SettlementInfo>();
					stlInfoMap.put(clearingProfile, stlInfoList);
				}
				stlInfoList.add(settlementInfo);
			}

			try {
				for (ClearingProfile clearingProfile : stlInfoMap.keySet()) {
					if (!ATMSettlementServiceImpl.class.equals(clearingProfile.getSettlementClass()))
						continue;

					ATMSettlementServiceImpl settlementService = (ATMSettlementServiceImpl) ClearingService.getSettlementService(clearingProfile);

					if (settlementService == null)
						result += "There is no settlementService for this clearingProfile " + clearingProfile.getName()
								+ "-" + clearingProfile.getId();

					logger.debug("Trying to Settle terminals with clearingProfile " + clearingProfile.getName() + "-"
							+ clearingProfile.getId());

					//settlementService.generateATMReturnedReport(stlInfoMap.get(clearingProfile));

					/*settlementService.settle(stlInfoMap.get(clearingProfile), clearingProfile, clearingProfile
							.getSettleUntilTime(now), true);*/


					// settlementJob.getSettlementService().account(clrProfileMap.get(clearingProfile),
					// clearingProfile,
					// clearingProfile.getSettleUntilTime(now),clearingProfile.getSettleUntilTime(now),
					// true);
					logger.debug("terminals with clearingProfile " + clearingProfile.getName() + "-"
							+ clearingProfile.getId() + " is settled");
				}
			} catch (Exception e) {
				logger.error("Encounter with an exception in UI_Settlement_RQ (" + e + ": " + e.getMessage());
				responseCode = ISOResponseCodes.INVALID_CARD_STATUS;
				result += e.getClass().getSimpleName() + ": " + e.getMessage();
			}
		}

		MessageObject object = new MessageObject();
		object.setIfxType(IfxType.UI_SETTLE_RS);
		object.setParameters(new HashMap<String, Serializable>());
		object.getParameters().put("result", result);
		object.setResponseCode(responseCode);
		Message outMessage = new Message(MessageType.OUTGOING);
		outMessage.setProtocolMessage(object);
		outMessage.setTransaction(transaction);
		outMessage.setRequest(false);
		outMessage.setNeedResponse(false);
		outMessage.setNeedToBeInstantlyReversed(false);
		outMessage.setNeedToBeSent(true);
		outMessage.setChannel(channel);

		return outMessage;
	}

	//Raza INITIALIZE start
	private Message initialize(Transaction transaction, MessageObject messageObject, Channel channel) {
		String result = "PROCESSED";
		String responseCode = ISOResponseCodes.APPROVED;

			try {
				//INITIALIZE SWITCH HERE...
				GlobalContext.getInstance().setAllChannels();
				logger.debug("Puting all ClearingProfile in GlobalContext");
				GlobalContext.getInstance().setAllClearingProfile();
				logger.debug("Puting all LotteryAssignmentPolicy in GlobalContext");
				GlobalContext.getInstance().setAllLotteryAssignmentPolicy();

				logger.debug("Puting all convertors in GlobalContext");
				GlobalContext.getInstance().setAllConvertors();

				logger.debug("Puting all clearing stuff in GlobalContext");
				GlobalContext.getInstance().setAllClearingStuff();

				//logger.debug("Puting routing tables in GlobalContext"); //Raza Commenting now working on Routing from DB
				//setAllRoutingTables(); //Raza Commenting now working on Routing from DB

				logger.debug("Puting currencies in GlobalContext");
				GlobalContext.getInstance().setAllCurrencies();

				logger.debug("Puting all SecurityFunctions in GlobalContext");
				GlobalContext.getInstance().setAllSecurityFunctions();

				logger.debug("Puting all FeeProfiles in GlobalContext");
				GlobalContext.getInstance().setAllFeeProfiles();

				logger.debug("Puting all DiscountProfiles in GlobalContext");
				GlobalContext.getInstance().setAllDiscountProfiles();

				logger.debug("Puting all AuthorizationProfiles in GlobalContext");
				GlobalContext.getInstance().setAllAuthorizationProfiles();

				logger.debug("Puting all institutions in GlobalContext");
				GlobalContext.getInstance().setAllInstitutions();

				logger.debug("Puting all banks in GlobalContext");
				GlobalContext.getInstance().setAllBanks();

				logger.debug("Puting ATM Key in GlobalContext");
				GlobalContext.getInstance().setATMKey();

				logger.debug("Puting ATM Configurations in GlobalContext");
				GlobalContext.getInstance().setAllATMConfigurations();

				logger.debug("Putting Message Routing in GlobalContext");
				GlobalContext.getInstance().setAllMessageRouting();

				logger.debug("Putting HSM Channels in GlobalContext");
				GlobalContext.getInstance().setAllHSMChannels();

				logger.debug("Putting Card & Account Status Codes");
				CMSStatusCodes.LoadCodes(); //Raza adding for Card,Account&Customer Status

				try {
					logger.debug("Puting all Cell Phone Charge Specification in GlobalContext");
					GlobalContext.getInstance().setAllCellPhoneChargeSpecification();
					logger.debug("Puting charge policy in GlobalContext");
					GlobalContext.getInstance().setGeneralChargePolicy();
				} catch (Exception e) {
					logger.warn("MTN Charge couldn't loaded in GlobalContext");
				}

//				logger.debug("Puting switch user in GlobalContext");
//				GlobalContext.getInstance().setSwitchUser();

				logger.debug("Loading protocol configs");
				GlobalContext.getInstance().setAllProtocolConfig();

//				if (Util.getMainClassName().equals("vaulsys.application.VaulsysWCMS")) {
//					logger.debug("Starting release memory");
//					ReleaseMemoryThread rmThread = new ReleaseMemoryThread();
//					Thread thread = new Thread(rmThread);
//					logger.debug("Thread: " + thread.getName() + " is starting...");
//					thread.start();
//				}

				//m.rehman: set all loro entries
				logger.debug("Loading Loro entries");
				GlobalContext.getInstance().setAllLoroEntries();

				//m.rehman: loading cms products/product detail/track 2 format/product keys
				logger.debug("Loading Product entries");
				GlobalContext.getInstance().setAllCMSProducts();



			} catch (Exception e) {
				logger.error("Encounter with an exception in SYSTEM_INITIALIZE_RQ (" + e + ": " + e.getMessage());
				responseCode = ISOResponseCodes.INVALID_CARD_STATUS;
				result += e.getClass().getSimpleName() + ": " + e.getMessage();
			}


		MessageObject object = new MessageObject();
		object.setIfxType(IfxType.SYSTEM_INITIALIZE_RS);
		object.setParameters(new HashMap<String, Serializable>());
		object.getParameters().put("result", result);
		object.setResponseCode(responseCode);
		Message outMessage = new Message(MessageType.OUTGOING);
		outMessage.setProtocolMessage(object);
		outMessage.setTransaction(transaction);
		outMessage.setRequest(false);
		outMessage.setNeedResponse(false);
		outMessage.setNeedToBeInstantlyReversed(false);
		outMessage.setNeedToBeSent(true);
		outMessage.setChannel(channel);

		return outMessage;
	}
	//Raza INITIALIZE end
}
