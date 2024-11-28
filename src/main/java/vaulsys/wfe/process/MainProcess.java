package vaulsys.wfe.process;

import vaulsys.authentication.base.AuthenticationHandler;
import vaulsys.authentication.exception.DisableFinancialEntityException;
import vaulsys.authentication.exception.IncorrectWorkingDay;
import vaulsys.authentication.exception.InvalidTerminalOrMerchantException;
import vaulsys.authentication.exception.MacFailException;
import vaulsys.authorization.component.AuthorizationHandler;
import vaulsys.authorization.exception.DailyAmountExceededException;
import vaulsys.authorization.exception.FITControlNotAllowedException;
import vaulsys.authorization.exception.MandatoryFieldException;
import vaulsys.authorization.exception.NotPaperReceiptException;
import vaulsys.authorization.exception.NotRoundAmountException;
import vaulsys.authorization.exception.NotSubsidiaryAccountException;
import vaulsys.authorization.exception.PanPrefixServiceNotAllowedException;
import vaulsys.authorization.exception.ServiceTypeNotAllowedException;
import vaulsys.authorization.exception.SufficientAmountException;
import vaulsys.authorization.exception.TransactionAmountNotAcceptableException;
import vaulsys.authorization.exception.card.CardAuthorizerException;
import vaulsys.authorization.component.ChannelTxnAuthorizationHandler;
import vaulsys.base.components.MessageTypeFlowDirection;
import vaulsys.billpayment.exception.DuplicateBillPaymentMessageException;
import vaulsys.billpayment.exception.NotValidBillPaymentMessageException;
import vaulsys.clearing.components.ClearingHandler;
import vaulsys.cms.components.CMSDBOperations;
import vaulsys.cms.components.CardAuthorizationHandler;
import vaulsys.config.IMDType;
import vaulsys.eft.base.MessageProcessHandler;
import vaulsys.eft.exception.PinBlockException;
import vaulsys.message.Message;
import vaulsys.message.MessageManager;
import vaulsys.message.MessageType;
import vaulsys.message.components.MessageBinderHandler;
import vaulsys.message.exception.MessageBindingException;
import vaulsys.mtn.exception.NoChargeAvailableException;
import vaulsys.netmgmt.component.NetworkManagementHandler;
import vaulsys.network.NetworkManager;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.CommunicationMethod;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.NetworkUi.MessageObject;
import vaulsys.protocols.PaymentSchemes.NetworkUi.NetworkUiMessageProcessorHandler;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.exception.exception.CantAddNecessaryDataToIfxException;
import vaulsys.protocols.exception.exception.NotMappedProtocolToIfxException;
import vaulsys.protocols.exception.exception.ReferenceTransactionNotFoundException;
import vaulsys.protocols.handlers.BinaryToProtocolHandler;
import vaulsys.protocols.handlers.ExceptionBinaryHandler;
import vaulsys.protocols.handlers.IfxToProtocolHandler;
import vaulsys.protocols.handlers.ProtocolToBinaryHandler;
import vaulsys.protocols.handlers.ProtocolToIfxHandler;
import vaulsys.protocols.ui.UiMessageProcessorHandler;
import vaulsys.routing.components.RoutingHandler;
import vaulsys.routing.exception.ScheduleMessageFlowBreakDown;
import vaulsys.scheduler.base.AddRepeatReversalTriggerHandler;
import vaulsys.scheduler.base.RemoveRepeatReversalTriggerHandler;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionType;
import vaulsys.util.Util;
import vaulsys.wallet.components.WalletAuthorizationHandler;
import vaulsys.wallet.components.WalletDBOperations;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;
import vaulsys.wfe.base.FlowDispatcher;
import vaulsys.util.encoders.Hex;
import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

public class MainProcess implements Runnable {
	private static final Logger logger = Logger.getLogger(MainProcess.class);

	public long time;
	public Long trxId;
	public String trxType;
	//public boolean isWebService;

	private long id;
	private ProcessContext processContext = null;

	public MainProcess(long processId, Transaction trx, IoSession session, long time) {
		this.id = processId;
		this.processContext = new ProcessContext();
		this.processContext.setTransaction(trx);
		this.processContext.setSession(session);
		this.trxId = 0L;
		this.time = time;
	}

	public MainProcess(long processId, Transaction trx, IoSession session, long time, boolean iswebservice) { //Raza for NAYAPAY
		this.id = processId;
		this.processContext = new ProcessContext();
		this.processContext.setTransaction(trx);
		this.processContext.setSession(session);
		this.trxId = 0L;
		this.time = time;
		//this.isWebService = iswebservice;
	}

	//Raza NayaPay start
	public MainProcess(long processId, Transaction trx, long time) {
		this.id = processId;
		this.processContext = new ProcessContext();
		this.processContext.setTransaction(trx);
		this.trxId = 0L;
		this.time = time;
	}
	//Raza NayaPay end

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void run() {
		processContext.init();

		logger.debug("Try to get a connection from DB");
		GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
		
		Transaction transaction = processContext.getTransaction();
		Message receivedMessage = processContext.getInputMessage();

		logger.debug("BINARY:" + new String(Hex.encode(receivedMessage.getBinaryData())));
		try {
			GeneralDao.Instance.saveOrUpdate(transaction);
			GeneralDao.Instance.saveOrUpdate(receivedMessage);
			trxId = processContext.getTransaction().getId();

			networkLayerProcess();

			processTrasaction();

			trxType = processContext.getTransaction().getDebugTag();

			byte[] binaryData = null;
			
			
			if(transaction.getOutputMessage() != null )
				binaryData = transaction.getOutputMessage().getBinaryData();
			
			processContext.setOutputMessage(transaction.getOutputMessage());
			
			GeneralDao.Instance.endTransaction();
			logger.info("Main process commited...");
			if (processContext.getOutputMessage() != null || (processContext.getPendingResponses() != null && processContext.getPendingResponses().size() > 0)) {

				GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);

				if (processContext.getOutputMessage().getId() != null) {
					processContext.getOutputMessage().setBinaryData(binaryData);
					GeneralDao.Instance.refresh(processContext.getOutputMessage());
				}

				sendOutputMessages();

				GeneralDao.Instance.endTransaction();
				sendPendingRequests();
			} else {
				logger.warn("IMPORTANT: Flow generated no response");
			}

		} catch (Exception e) {
			
				GeneralDao.Instance.endTransaction();
				if(e instanceof ReferenceTransactionNotFoundException){
					logger.error(e.getMessage());
				}else{
					logger.error("Killer exception: " + e, e);
					logger.debug("The exception that killed the flow: ", e);
				}
		}
	}

	//Raza NayaPay start
	public void run(boolean isWebService) {
		processContext.init();

		logger.debug("Try to get a connection from DB");
		GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);

		Transaction transaction = processContext.getTransaction();
		Message receivedMessage = processContext.getInputMessage();

		logger.debug("BINARY:" + new String(Hex.encode(receivedMessage.getBinaryData())));
		try {
			GeneralDao.Instance.saveOrUpdate(transaction);
			GeneralDao.Instance.saveOrUpdate(receivedMessage);
			trxId = processContext.getTransaction().getId();

			networkLayerProcess();

			processTrasaction();

			trxType = processContext.getTransaction().getDebugTag();
			byte[] binaryData = null;

			if(transaction.getOutputMessage() != null )
				binaryData = transaction.getOutputMessage().getBinaryData();

			processContext.setOutputMessage(transaction.getOutputMessage());

			GeneralDao.Instance.endTransaction();
			logger.info("Main process commited...");
			if (processContext.getOutputMessage() != null || (processContext.getPendingResponses() != null && processContext.getPendingResponses().size() > 0)) {

				GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);

				if (processContext.getOutputMessage().getId() != null) {
					processContext.getOutputMessage().setBinaryData(binaryData);
					GeneralDao.Instance.refresh(processContext.getOutputMessage());
				}

				sendOutputMessages();

				GeneralDao.Instance.endTransaction();
				sendPendingRequests();
			} else {
				logger.warn("IMPORTANT: Flow generated no response");
			}

		} catch (Exception e) {

			GeneralDao.Instance.endTransaction();
			if(e instanceof ReferenceTransactionNotFoundException){
				logger.error(e.getMessage());
			}else{
				logger.error("Killer exception: " + e, e);
				logger.debug("The exception that killed the flow: ", e);
			}
		}
	}
	//Raza NayaPay end


	private void networkLayerProcess() {
		Message receivedMessage = processContext.getInputMessage();
		Channel channel = receivedMessage.getChannel();
		
		if (channel instanceof InputChannel) {
			InputChannel inputChannel = (InputChannel) channel;
			CommunicationMethod responseMethod = inputChannel.getCommunicationMethod();

			if (CommunicationMethod.SAME_SOCKET.equals(responseMethod)) {
				NetworkManager.getInstance().addResponseOnSameSocketConnection(receivedMessage.getId(), processContext.getSession());
			}
		}
	}

	private void sendPendingRequests() {
		Set<Message> pendingRequests = this.getPendingRequests();
		if (pendingRequests!= null && !pendingRequests.isEmpty()){
			int messagestobesendNo = (pendingRequests!= null)? pendingRequests.size(): 0;
		    logger.info("Put Pending Requests : "+ messagestobesendNo +" messages are about to be sent!");
			MessageManager.getInstance().putRequests(pendingRequests);
		}
	}

	private void sendOutputMessages() {
		Message response = null;
		Set<Message> pendingResponses = null;

		response = this.getOutputMessage();
		pendingResponses = this.getPendingResponses();

		if (response != null) {

			// int messagestobesendNo = 0;
			// messagestobesendNo = (response != null) ? messagestobesendNo + 1: messagestobesendNo;
			// logger.info("Put Response Messages: " + messagestobesendNo + " messages are about to be sent!");

			// response.setPendingRequests(pendingMessages);
			Collection<Message> pendingRq = MessageManager.getInstance().putResponse(response);
			if (pendingRq != null && !pendingRq.isEmpty())
				this.processContext.addPendingRequests(pendingRq);

		} else if (pendingResponses != null && pendingResponses.size() > 0) {
			for (Message res : pendingResponses)
				MessageManager.getInstance().putResponse(res);

		} else {
			logger.warn("IMPORTANT: Flow generated no response.");
		}
	}

	private void processTrasaction() throws Exception {
		try {
			BinaryToProtocolHandler.Instance.execute(processContext);

			// Dispatch Incomming Message
			Message message = processContext.getTransaction().getInputMessage();

			//m.rehman: checking wallet card imd
			Boolean isWalletIMDFlag = Boolean.FALSE;

			// Asim Shahzad, Date : 21st Sep 2020, Tracking ID : VP-NAP-202008252
//			if (message.getProtocolMessage() instanceof ISOMsg) {
//				String pan = ((ISOMsg) message.getProtocolMessage()).getString(2);
//				if (Util.hasText(pan)) {
//					IMDType type = CMSDBOperations.Instance.getIMDType(pan.substring(0, 11));
//					if (type != null && type.equals(IMDType.Wallet))
//						isWalletIMDFlag = Boolean.TRUE;
//				}
//			}
			// ==================================================================

			FlowDispatcher dispatcher = message.getChannel().getProtocol().getFlowDispatcher();

			if (dispatcher == null) {
				logger.error("Incomming Message not supported!!!, switch cannot dispatch message to an appropriate path");
				throw new Exception("Incomming Message not supported!!!, switch cannot dispatch message to an appropriate path");
			}

			String destination = dispatcher.dispatch(message);

			// Main part of decision
			//m.rehman: for wallet transactions
			//if (destination.equals(MessageTypeFlowDirection.Financial))
			if (destination.equals(MessageTypeFlowDirection.Financial) && isWalletIMDFlag.equals(Boolean.FALSE))
				processFinancialTransaction();
			else if (destination.equals(MessageTypeFlowDirection.Network))
				processNetworkManagementTransaction();
			else if (destination.equals(MessageTypeFlowDirection.Clearing))
				processClearingTransaction();
			else if (destination.equals(MessageTypeFlowDirection.GENERAL_UI))
				processGeneralUITransaction();
			else if (destination.equals(MessageTypeFlowDirection.NETWORK_UI))
				processUINetworkTransaction(); //Raza Adding from TPSP for Network Messages
			// m.rehman: for wallet transactions
			else if (destination.equals(MessageTypeFlowDirection.Wallet) || isWalletIMDFlag.equals(Boolean.TRUE))
				processWalletTransaction();
			//m.rehman: for batch transaction from NAC
			else if (destination.equals(MessageTypeFlowDirection.Batch))
				processBatchTransaction();
			else {
				logger.error("Incomming Message not supported!!!, switch cannot dispatch message to an appropriate path. destination is: " + destination);
				throw new Exception("Incomming Message not supported!!!, switch cannot dispatch message to an appropriate path. destination is: " + destination);
			}

			if (processContext.getTransaction().getLifeCycle() == null) {
				LifeCycle lifeCycle = new LifeCycle();
				GeneralDao.Instance.saveOrUpdate(lifeCycle);
				processContext.getTransaction().setLifeCycle(lifeCycle);
				GeneralDao.Instance.saveOrUpdate(processContext.getTransaction());
			}	
		} catch (Exception e) {
			try {
				if( e instanceof DuplicateBillPaymentMessageException || 
	        		e instanceof FITControlNotAllowedException ||
	        		e instanceof NotPaperReceiptException ||
	        		e instanceof NotRoundAmountException ||
	        		e instanceof PanPrefixServiceNotAllowedException ||
	        		e instanceof MandatoryFieldException || 
	        		e instanceof NotValidBillPaymentMessageException ||
	        		e instanceof InvalidTerminalOrMerchantException ||
	        		e instanceof NotSubsidiaryAccountException ||
	        		e instanceof TransactionAmountNotAcceptableException ||
	        		e instanceof MacFailException ||
	        		e instanceof IncorrectWorkingDay || 
	        		e instanceof NotMappedProtocolToIfxException ||
	        		e instanceof CardAuthorizerException ||
	        		e instanceof NoChargeAvailableException ||
	        		e instanceof ServiceTypeNotAllowedException ||
	        		e instanceof CantAddNecessaryDataToIfxException ||
	        		e instanceof PinBlockException ||
	        		e instanceof DisableFinancialEntityException ||
	        		e instanceof SufficientAmountException ||
	        		e instanceof DailyAmountExceededException) {
					logger.warn(e.getClass().getSimpleName() + ":" + e.getMessage());
				}else if( e instanceof MessageBindingException){
					logger.warn(e.getClass().getSimpleName() + ":" + e.getMessage());
				}else if(e instanceof ReferenceTransactionNotFoundException){
					
				} else{
					logger.error(e.getClass().getSimpleName() + ":" + e.getMessage(), e);
				}
				processContext.addException(e);
				logger.info("Exception class ["+e.getClass()+"]");
				System.out.println("Exception class ["+e.getClass()+"]");
				e.printStackTrace();
				ExceptionBinaryHandler.Instance.execute(processContext);
				
			} catch (Exception ex) {
				logger.error("ExceptionBinaryHandler has thrown an Exception: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
				processContext.removeAllOutputMessage();
				throw ex;
			}
		}
	}

	private void processGeneralUITransaction() throws Exception {
		ProtocolToIfxHandler.Instance.execute(processContext);
		UiMessageProcessorHandler.Instance.execute(processContext);
		ProtocolToBinaryHandler.Instance.execute(processContext);
	}

	private void processUINetworkTransaction() throws Exception { //Raza CHANNEL msg from UI & INITIALIZE

		//GetProtocol Message here...
		MessageObject messageObject = (MessageObject) processContext.getInputMessage().getProtocolMessage();
		String command = messageObject.getCommand();
		logger.info("received command [" + command + "]");
		if(command.toLowerCase().contains("init")) //Raza separating flow, as IFX not required for INIT message
		{
			NetworkUiMessageProcessorHandler.ProcessInitRequest(command);
			//GeneralDao.Instance.evict(processContext.getInputMessage().getTransaction());
			//GeneralDao.Instance.evict(processContext.getInputMessage());
			//Raza INITIALIZE
		}
		else
		{
			ProtocolToIfxHandler.Instance.execute(processContext);
			NetworkUiMessageProcessorHandler.Instance.execute(processContext);
			IfxToProtocolHandler.Instance.execute(processContext);
			ProtocolToBinaryHandler.Instance.execute(processContext);
		}
	}

	private void processFinancialTransaction() throws Exception {
		ProtocolToIfxHandler.Instance.execute(processContext);
		
		// Authentication
		//logger.info("Before Authentication Handler"); //Raza TEMP
		AuthenticationHandler.Instance.execute(processContext);
		//logger.info("After Authentication Handler"); //Raza TEMP
		MessageBinderHandler.Instance.execute(processContext);
		//logger.info("After MsgBinder"); //Raza TEMP
		// Remove Repeat Reversal Trigger
		RemoveRepeatReversalTriggerHandler.Instance.execute(processContext);
		//logger.info("After Remove Rever"); //Raza TEMP
		// Authorization
		AuthorizationHandler.Instance.execute(processContext);
		//logger.info("After Authoriza"); //Raza TEMP

		RoutingHandler.Instance.execute(processContext);
		//logger.info("After Routing"); //Raza TEMP
		//logger.info("After Routing"); //Raza TEMP

		//Raza Card Authorization Should be here.. after routing b/c of Dest Channel Keys
		if(ISOFinalMessageType.isRequestMessage(processContext.getInputMessage().getIfx().getIfxType()))
		{
			ChannelTxnAuthorizationHandler.Instance.execute(processContext); //Raza adding for TXN Permission
			CardAuthorizationHandler.Instance.execute(processContext);
		}
		else if(ISOMessageTypes.isResponseMessage(processContext.getInputMessage().getIfx().getMti()) &&
				processContext.getInputMessage().getIfx().getRsCode() != ISOResponseCodes.APPROVED &&
				ISOFinalMessageType.isFinancialMessage(processContext.getInputMessage().getIfx().getIfxType(),true))
		{
				CardAuthorizationHandler.Instance.ReverseCardLimit(processContext);
		}
		// Process Message
		try {
			MessageProcessHandler.Instance.execute(processContext);
		} catch (Exception e) {
			if (e instanceof ScheduleMessageFlowBreakDown)
				return;

			if (processContext.isNextStateToEnd())
				return;
//			logger.error(e.getMessage());
			throw e;
		}

		IfxToProtocolHandler.Instance.execute(processContext);

		ProtocolToBinaryHandler.Instance.execute(processContext);

		// Adding repeat reversal trigger
		AddRepeatReversalTriggerHandler.Instance.execute(processContext);
	}

	private void processNetworkManagementTransaction() throws Exception {
		try {
			NetworkManagementHandler.Instance.execute(processContext);
		} catch (Exception e) {
			return;
		}

		if (processContext.isNextStateToEnd())
			return;

		// Protocol to binary
		try {
			ProtocolToBinaryHandler.Instance.execute(processContext);
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}

		// Adding repeat reversal trigger
		AddRepeatReversalTriggerHandler.Instance.execute(processContext);
	}

	private void processClearingTransaction() throws Exception {
		// Clearing Handler
		ClearingHandler.Instance.execute(processContext);

		// Remove repeat reversal trigger
		RemoveRepeatReversalTriggerHandler.Instance.execute(processContext);

		if (processContext.isNextStateToEnd())
			return;

//		GeneralDao.Instance.refresh(processContext.getOutputMessage().getEndPointTerminal());
		// Protocol to binary
		try {
			ProtocolToBinaryHandler.Instance.execute(processContext);
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}

		// Adding repeat reversal trigger
		AddRepeatReversalTriggerHandler.Instance.execute(processContext);
	}

	//m.rehman: for wallet transaction flow
	private void processWalletTransaction() throws Exception {
		ProtocolToIfxHandler.Instance.execute(processContext);

		// Authentication
		AuthenticationHandler.Instance.execute(processContext);

		MessageBinderHandler.Instance.execute(processContext);

		// Remove Repeat Reversal Trigger
		RemoveRepeatReversalTriggerHandler.Instance.execute(processContext);

		// Authorization
		AuthorizationHandler.Instance.execute(processContext);

		//Raza Card Authorization Should be here.. after routing b/c of Dest Channel Keys
		if(ISOFinalMessageType.isRequestMessage(processContext.getInputMessage().getIfx().getIfxType()))
		{
			WalletAuthorizationHandler.Instance.execute(processContext);
		}
		else if(ISOMessageTypes.isResponseMessage(processContext.getInputMessage().getIfx().getMti()) &&
				processContext.getInputMessage().getIfx().getRsCode() != ISOResponseCodes.APPROVED &&
				ISOFinalMessageType.isFinancialMessage(processContext.getInputMessage().getIfx().getIfxType(),true))
		{
			WalletDBOperations.Instance.ReverseCardLimit(processContext);
		}
		// Process Message
		try {
			MessageProcessHandler.Instance.execute(processContext);
		} catch (Exception e) {
			if (e instanceof ScheduleMessageFlowBreakDown)
				return;

			if (processContext.isNextStateToEnd())
				return;
			throw e;
		}

		IfxToProtocolHandler.Instance.execute(processContext);

		ProtocolToBinaryHandler.Instance.execute(processContext);

		// Adding repeat reversal trigger
		AddRepeatReversalTriggerHandler.Instance.execute(processContext);
	}

	//m.rehman: for batch transactions from NAC
	private void processBatchTransaction() throws Exception {
		ProtocolToIfxHandler.Instance.execute(processContext);

		// Authentication
		//logger.info("Before Authentication Handler"); //Raza TEMP
		AuthenticationHandler.Instance.execute(processContext);
		//logger.info("After Authentication Handler"); //Raza TEMP
		MessageBinderHandler.Instance.execute(processContext);
		logger.info("After MsgBinder"); //Raza TEMP
		// Remove Repeat Reversal Trigger
		RemoveRepeatReversalTriggerHandler.Instance.execute(processContext);
		//logger.info("After Remove Rever"); //Raza TEMP
		// Authorization
		AuthorizationHandler.Instance.execute(processContext);
		logger.info("After Authoriza"); //Raza TEMP

		//RoutingHandler.Instance.execute(processContext);
		//logger.info("After Routing"); //Raza TEMP
		//Raza Card Authorization Should be here.. after routing b/c of Dest Channel Keys
		/*if(ISOFinalMessageType.isRequestMessage(processContext.getInputMessage().getIfx().getIfxType()))
		{
			CardAuthorizationHandler.Instance.execute(processContext);
		}
		else if(ISOMessageTypes.isResponseMessage(processContext.getInputMessage().getIfx().getMti()) &&
				processContext.getInputMessage().getIfx().getRsCode() != ISOResponseCodes.APPROVED &&
				ISOFinalMessageType.isFinancialMessage(processContext.getInputMessage().getIfx().getIfxType(),true))
		{
			CardAuthorizationHandler.Instance.ReverseCardLimit(processContext);
		}*/
		// Process Message
		try {
			MessageProcessHandler.Instance.execute(processContext);
		} catch (Exception e) {
			if (e instanceof ScheduleMessageFlowBreakDown)
				return;

			if (processContext.isNextStateToEnd())
				return;
//			logger.error(e.getMessage());
			throw e;
		}

		IfxToProtocolHandler.Instance.execute(processContext);

		ProtocolToBinaryHandler.Instance.execute(processContext);

		// Adding repeat reversal trigger
		AddRepeatReversalTriggerHandler.Instance.execute(processContext);
	}

	public Message getOutputMessage() {
		return processContext.getOutputMessage();
	}

	public Set<Message> getPendingRequests() {
		return processContext.getPendingRequests();
	}

	public Set<Message> getPendingResponses() {
		return processContext.getPendingResponses();
	}
}
