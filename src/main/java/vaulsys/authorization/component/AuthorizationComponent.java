package vaulsys.authorization.component;

import vaulsys.authorization.exception.*;
import vaulsys.authorization.exception.card.CardAuthorizerException;
import vaulsys.authorization.policy.Policy;
import vaulsys.authorization.policy.PolicyComparator;
import vaulsys.billpayment.exception.DuplicateBillPaymentMessageException;
import vaulsys.billpayment.exception.NotValidBillPaymentMessageException;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.eft.base.ifxTypeProcessor.IfxTypeProcessMap;
import vaulsys.eft.base.ifxTypeProcessor.MessageProcessor;
import vaulsys.eft.base.terminalTypeProcessor.TerminalTypeProcessMap;
import vaulsys.eft.base.terminalTypeProcessor.TerminalTypeProcessor;
import vaulsys.entity.Contract;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.entity.impl.Institution;
import vaulsys.entity.impl.Merchant;
import vaulsys.entity.impl.Shop;
import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.endpoint.EndPointType;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.apacs70.base.RqBaseMsg;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

import java.util.*;

public class AuthorizationComponent {
	private static Logger logger = Logger.getLogger(AuthorizationComponent.class);
	
	private AuthorizationComponent() {}

	public static void authorize(ProcessContext processContext) throws AuthorizationException, Exception {
		Message incomingMessage = processContext.getInputMessage();
		Ifx ifx = incomingMessage.getIfx();
		IfxType ifxType = ifx.getIfxType();

		try {
			Channel channel = incomingMessage.getChannel();
			Terminal endPointTerminal = incomingMessage.getEndPointTerminal();
			
			/*if (ShetabFinalMessageType.isRequestMessage(ifxType) ||
					ShetabFinalMessageType.isPrepareMessage(ifxType) ||
					ShetabFinalMessageType.isPrepareReversalMessage(ifxType)) {
				ifx.setOriginatorTerminal(endPointTerminal);
			}*/
			
			
			if (!EndPointType.isSwitchTerminal(channel.getEndPointType())) {
				authorizeTerminalMessage(incomingMessage, endPointTerminal, ifx);
			} else {
				if (EndPointType.EPAY_SWITCH_TERMINAL.equals(channel.getEndPointType())) {
					authorizeEpayInstitutionMessage(incomingMessage, ifx, channel);
				} else {
					//m.rehman: if switch is pos controller, need to verify terminal policies
					if (channel.getIsPosSwitch()) {
						logger.info("Terminal ID [" + ifx.getTerminalId() + "]"); //Raza TEMP
						endPointTerminal = TerminalService.findTerminal(POSTerminal.class,
								Long.parseLong(ifx.getTerminalId()));
						if (endPointTerminal == null)
							throw new Exception("POS Terminal not found.");
						else
							authorizeTerminalMessage(incomingMessage, endPointTerminal, ifx);
					}
					authorizeInstitutionMessage(incomingMessage, ifx, channel);
				}
			}
			
			/***********************/
//			Institution myInstitution = GlobalContext.getInstance().getMyInstitution();
			Institution myInstitution = ProcessContext.get().getMyInstitution();
			Long myBin = myInstitution.getBin();
			String acqCode = ifx.getBankId();

			if (acqCode != null && acqCode.equals(myBin) && FinancialEntityRole.MY_SELF.equals(myInstitution.getRole())) {
				if (!TerminalService.isOriginatorSwitchTerminal(incomingMessage)) {
			
					TerminalTypeProcessor terminalTypeProcessor = TerminalTypeProcessMap.getAuthorizationProcessor(ifx, incomingMessage.getEndPointTerminal().getTerminalType());
					terminalTypeProcessor.messageValidation(ifx, incomingMessage.getId());
					
					MessageProcessor processor = IfxTypeProcessMap.getAuthorizationProcessor(ifx, channel);
					processor.messageValidation(ifx, incomingMessage);
				}
			}
			/***********************/
			if (ISOFinalMessageType.isResponseMessage(ifxType)){
				Channel firstTrxChannel = ifx.getTransaction().getFirstTransaction().getInputMessage().getChannel();
				if (ifxType.equals(IfxType.TRANSFER_TO_ACCOUNT_RS)) {
					if (ifx.getTransaction().getReferenceTransaction() != null &&
							ifx.getTransaction().getReferenceTransaction().getInputMessage() != null &&
							ifx.getTransaction().getReferenceTransaction().getInputMessage().getChannel() != null)
					firstTrxChannel = ifx.getTransaction().getReferenceTransaction().getInputMessage().getChannel();
				}
				//-----------------------------Moosavi: Task 50617 : Add New Policy for max card amount for Currency ATM-----------------------------
				else if(ifxType.equals(IfxType.BILL_PMT_REV_REPEAT_RS) &&
						ifx.getTransaction().getReferenceTransaction().getIncomingIfx().getIfxType().equals(IfxType.WITHDRAWAL_CUR_RQ)){
					if (ifx.getTransaction().getReferenceTransaction() != null &&
							ifx.getTransaction().getReferenceTransaction().getInputMessage() != null &&
							ifx.getTransaction().getReferenceTransaction().getInputMessage().getChannel() != null)
					firstTrxChannel = ifx.getTransaction().getReferenceTransaction().getInputMessage().getChannel();
				}
				//-----------------------------------------------------------------------------------------------------------------------------------
				EndPointType originalEndPoint = (firstTrxChannel!= null) ? firstTrxChannel.getEndPointType(): null;

				//m.rehman: adding check for Pos controller switch
				if ((originalEndPoint != null && !EndPointType.SWITCH_TERMINAL.equals(originalEndPoint))
						|| (firstTrxChannel != null && firstTrxChannel.getIsPosSwitch())) {
					if(ifx.getOriginatorTerminal() != null){
						processContext.setOriginatorTerminal(ifx.getOriginatorTerminal());
					}
					updateTerminalMessage(processContext, ifx, ifx.getTransaction());
				}
			}

//			processContext.getTransaction().setAuthorized(true);
		} catch (Exception e) {
        	if( 	e instanceof DuplicateBillPaymentMessageException || 
        			e instanceof FITControlNotAllowedException ||
        			e instanceof NotPaperReceiptException ||
        			e instanceof NotRoundAmountException ||
        			e instanceof PanPrefixServiceNotAllowedException ||
        			e instanceof MandatoryFieldException || 
        			e instanceof NotValidBillPaymentMessageException ||
        			e instanceof TransactionAmountNotAcceptableException ||
        			e instanceof NotSubsidiaryAccountException ||
        			e instanceof CardAuthorizerException ||
        			e instanceof TransactionAmountNotAcceptableException ||
        			e instanceof ServiceTypeNotAllowedException ||
        			e instanceof DailyAmountExceededException
        			){
        		//Just for exceptions that are not so important....
        		logger.warn(e.getClass().getSimpleName()+": "+ e.getMessage(), e);
        	}else{
        		logger.error(e.getClass().getSimpleName()+": "+ e.getMessage(), e);
        	}
			throw e;
		}
	}

	private static void authorizeEpayInstitutionMessage(Message incomingMessage, Ifx ifx, Channel channel) throws Exception {
		Terminal terminal = TerminalService.getMatchingTerminal(ifx);
		authorizeTerminalMessage(incomingMessage, terminal, ifx);
		authorizeInstitutionMessage(incomingMessage, ifx, channel);
//		authorizeTransactionMessage(incomingMessage, ifx, channel);
	}


	private static void updateTerminalMessage(ProcessContext processContext, Ifx ifx, Transaction transaction) throws AuthorizationException, Exception {
		Terminal terminal = null;
		Transaction firstTransaction = transaction.getFirstTransaction();

		//m.rehman: if switch is pos controller, need to find terminal by terminal id
		Channel firstTrxChannel = firstTransaction.getInputMessage().getChannel();
		if (firstTrxChannel != null && firstTrxChannel.getIsPosSwitch()) {
			terminal = TerminalService.findTerminal(POSTerminal.class,
					Long.parseLong(ifx.getTerminalId()));
		} else if(ifx.getOriginatorTerminal() == null){
			if (ifx.getTerminalId() != null && !ifx.getTerminalId().isEmpty())
				terminal = TerminalService.getMatchingTerminal(ifx);
			else if (firstTransaction != null) {
//				terminal = TerminalService.getMatchingTerminal(firstTransaction.getOutputMessage().getIfx());
				terminal = TerminalService.getMatchingTerminal(firstTransaction.getOutgoingIfx());
			}
			processContext.setOriginatorTerminal(terminal);
		}else{
			terminal = ifx.getOriginatorTerminal();
		}
		
		if (ISOResponseCodes.isSuccess(ifx.getRsCode())) {
			logger.debug("Try to update authorization policy terminal[ "+ terminal.getId()+"]");
			
			FinancialEntity entity = terminal.getOwner();
			
//			if (ifx.getOrgIdNum() != null && !ifx.getOrgIdNum().isEmpty())
//				entity = getMatchingEntity(terminal, ifx.getOrgIdNum());
//			else if (firstTransaction != null)
//				entity = getMatchingEntity(terminal, firstTransaction.getOutputMessage().getIfx().getOrgIdNum());

			Set<Policy> policies = gatherPolicies(terminal, entity);
			List<Policy> synchPolicies = new ArrayList<Policy>();

			checkMerchantContract(entity, ifx);
			try {
				for (Policy policy : policies) {
					if(policy.isSynchronized()){
						synchPolicies.add(policy);
					}
				}
				if(synchPolicies.size() > 0){
					Collections.sort(synchPolicies, new PolicyComparator());
					for (Policy policy : synchPolicies) {
//						policy = (Policy) GeneralDao.Instance.synchObject(policy);
						policy.update(firstTransaction.getOutgoingIfx(), terminal);					
					}				
				}
			} catch (Exception e) {
				logger.warn("Can't run update authorization policy:" + e.getClass().getSimpleName()+"- "+e.getMessage());
				throw e;
			}
		}
	}

	private static void authorizeTerminalMessage(Message inputMessage, Terminal terminal, Ifx ifx) throws AuthorizationException, Exception {	
		logger.debug("Try to authorize  terminal[ "+ terminal.getId()+"]");
				
		if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType()) ||
				ISOFinalMessageType.isPrepareMessage(ifx.getIfxType()) ||
				ISOFinalMessageType.isPrepareReversalMessage(ifx.getIfxType())) {
			FinancialEntity entity = terminal.getOwner();

			checkMerchantContract(entity, ifx);

			Set<Policy> policies = gatherPolicies(terminal, entity);
			List<Policy> synchPolicies = new ArrayList<Policy>();
			
			try {
				for (Policy policy : policies) {
					if(policy.isSynchronized()){
						synchPolicies.add(policy);
					}else{
						// If policy is not synchronized, authorize it in this step otherwise it will 
						// be authorized in the next step
						policy.authorize(ifx, terminal);
					}
				}
				if(synchPolicies.size() > 0){
					Collections.sort(synchPolicies, new PolicyComparator());
					for (Policy policy : synchPolicies) {
						//Authorize not-synchronized policies
						policy.authorize(ifx, terminal);
					}				
				}
			} catch (Exception e) {
	        	if (e instanceof FITControlNotAllowedException ||
	        		e instanceof CardAuthorizerException ||
	        		e instanceof TransactionAmountNotAcceptableException ||
	        		e instanceof PanPrefixServiceNotAllowedException){
					logger.warn("Can't run update authorization policy:" + e.getClass().getSimpleName()+"- "+e.getMessage());
	        	}else{
	        		logger.error("Can't run update authorization policy:" + e.getClass().getSimpleName()+"- "+e.getMessage());
	        	}
				throw e;
			}

			authorizeProtocolDependentMessage(inputMessage, terminal, ifx);
		}
	}
	
	private static void authorizeProtocolDependentMessage(Message inputMessage, Terminal terminal, Ifx ifx) throws Exception {
		if(inputMessage.getProtocolMessage() instanceof RqBaseMsg) {
			ifx.setUpdateReceiptRequired(true);

			//RqBaseMsg rqMsg = (RqBaseMsg) inputMessage.getProtocolMessage();
/*			POSTerminal pos = null;
			PINPADTerminal pinpad = null;
			if(terminal instanceof POSTerminal)
				pos = (POSTerminal) terminal;
			else
				pinpad = (PINPADTerminal) terminal;

//			POSConfiguration posCfg = pos != null ? pos.getConfiguration() : pinpad.getConfiguration();
			POSConfiguration posCfg = pos != null ? pos.getOwnOrParentConfiguration() : pinpad.getOwnOrParentConfiguration();

			POSSpecificFilesVersion psfv = pos != null ? pos.getFilesVersion() : pinpad.getFilesVersion();
			if(psfv == null) {
				logger.debug("Creating SpecificFilesVersion for POS/PinPad: " + terminal.getCode());
				psfv = new POSSpecificFilesVersion();
				if (pos != null)
					pos.setFilesVersion(psfv);
				else
					pinpad.setFilesVersion(psfv);
				GeneralDao.Instance.saveOrUpdate(terminal);
			}
			psfv.setCreatedDateTime(DateTime.now());

			// When a receipt parts of a POSConfiguration changed, the ReceiptVer of POSSpecificFilesVersion of 
			// all of its POS or PINPAD changed to ? to show update
			if(psfv != null && "?".equals(psfv.getReceiptVer())) {
				ifx.setUpdateReceiptRequired(true);
				psfv.setReceiptVer(posCfg != null ? posCfg.getReceiptVersion() : null);
				GeneralDao.Instance.saveOrUpdate(psfv);
			}*/
		}
	}

	private static void authorizeInstitutionMessage(Message incomingMessage, Ifx ifx, Channel channel)	throws AuthorizationException {
		Terminal t = incomingMessage.getEndPointTerminal();
		Institution inst = (Institution) t.getOwner();
		
		if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())){
			Set<Policy> policies = gatherPolicies(t, inst);
			List<Policy> synchPolicies = new ArrayList<Policy>();
			
			if(policies == null)
				return;
			
			try {
				for (Policy policy : policies) {
					if(policy.isSynchronized()){
						synchPolicies.add(policy);
					}else{
						// If policy is not synchronized, authorize it in this step otherwise it will 
						// be authorized in the next step
						policy.authorize(ifx, t);
					}
				}

				if(synchPolicies.size() > 1){
					Collections.sort(synchPolicies, new PolicyComparator());
					for (Policy policy : synchPolicies) {
						//Authorize not-synchronized policies
						policy = (Policy) GeneralDao.Instance.synchObject(policy);
						policy.authorize(ifx, t);
					}				
				}
			} catch (AuthorizationException e) {
	        	if(e instanceof FITControlNotAllowedException){
					logger.warn("Can't run update authorization policy:" + e.getClass().getSimpleName()+"- "+e.getMessage());
	        	}else{
	        		logger.error("Can't run update authorization policy:" + e.getClass().getSimpleName()+"- "+e.getMessage());
	        	}
				throw e;
			}
		}
	}

	
	private static void checkMerchantContract(FinancialEntity entity, Ifx ifx) throws AuthorizationException {
		if (entity != null && FinancialEntityRole.SHOP.equals(entity.getRole())) {
			Shop shop = (Shop) entity;
			try {
				//			
				if (shop.getOwnOrParentContract() != null) {
					Contract contract = shop.getOwnOrParentContract();
					if (contract.getStartDate().after(ifx.getReceivedDt().toDate()))
						throw new AuthorizationException("Failed:Before contract start day. (shop.code= "
								+ shop.getCode() + ")", true);
					if (contract.getEndDate().before(ifx.getReceivedDt().toDate()))
						throw new AuthorizationException("Failed:Contract Expired. (shop.code= " + shop.getCode() + ")", true);
					Merchant merchant = shop.getOwner();
					if (merchant != null && !contract.equals(merchant.getContract())) {
						contract = merchant.getContract();
						if (contract.getStartDate().after(ifx.getReceivedDt().toDate()))
							throw new AuthorizationException("Failed:Before contract start day. (merchant.code= "
									+ merchant.getCode() + ")", true);
						if (contract.getEndDate().before(ifx.getReceivedDt().toDate()))
							throw new AuthorizationException("Failed:Contract Expired. (merchant.code= "
									+ merchant.getCode() + ")", true);
					}
				} else {
					throw new AuthorizationException(
							"Failed:Corresponding merchant doesn't have a contract. (shop.code= " + shop.getCode()
									+ ")", true);
				}
			} catch (AuthorizationException e) {
				logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
				throw e;
			}
		}
	}

	private static Set<Policy> gatherPolicies(Terminal selectedTerminal, FinancialEntity entity) throws AuthorizationException {
		if (!entity.isOwnOrParentEnabled()) {
			logger.error("Failed:FinancialEntity or FinancialEntityGroup is disabled. (entity.code= "
					+ entity.getCode() + ")");
			throw new AuthorizationException("Failed:FinancialEntity or FinancialEntityGroup is disabled. (entity.code= "
					+ entity.getCode() + ")", true);
		}

		if (!selectedTerminal.isOwnOrParentEnabled()) {
			logger.error("Failed:Terminal or TerminalGroup is disabled. (terminal.code= "
					+ selectedTerminal.getCode() + ")");
			throw new AuthorizationException("Failed:Terminal or TerminalGroup is disabled. (terminal.code= "
					+ selectedTerminal.getCode() + ")", true);
		}

		if (entity != null && FinancialEntityRole.SHOP.equals(entity.getRole())) {
			Shop shop = (Shop) entity;
			if (shop.getOwner()!= null && !shop.getOwner().isOwnOrParentEnabled()){
				logger.error("Failed:FinancialEntity or FinancialEntityGroup is disabled. (entity.owner.code= "
						+ shop.getOwner().getCode() + ")");
				throw new AuthorizationException("Failed:FinancialEntity or FinancialEntityGroup is disabled. (entity.owner.code= "
						+ shop.getOwner().getCode() + ")");
			}
		}
		
		Set<Policy> policies = new HashSet<Policy>();
//		AuthorizationProfile terminalAuthorizationProfile = selectedTerminal.getOwnOrParentAuthorizationProfile();
//		AuthorizationProfile entityAuthorizationProfile = entity.getOwnOrParentAuthorizationProfile();
		
		Long terminalAuthProfId = selectedTerminal.getOwnOrParentAuthorizationProfileId();
		Long entityAuthProfId = entity.getOwnOrParentAuthorizationProfileId();

//		if (terminalAuthorizationProfile != null)
//			policies.addAll(terminalAuthorizationProfile.getPolicies());
//			policies.addAll(getAuthorizationService().getAuthorizationPolicies(terminalAuthorizationProfile));
//		if (entityAuthorizationProfile != null)
//			policies.addAll(entityAuthorizationProfile.getPolicies());
		if(terminalAuthProfId != null && ProcessContext.get().getAuthorizationProfile(terminalAuthProfId).isEnabled())
			policies.addAll(ProcessContext.get().getAllAuthorizationPolicies(terminalAuthProfId));
		
		if(entityAuthProfId != null && ProcessContext.get().getAuthorizationProfile(entityAuthProfId).isEnabled())
			policies.addAll(ProcessContext.get().getAllAuthorizationPolicies(entityAuthProfId));
		
//			policies.addAll(getAuthorizationService().getAuthorizationPolicies(entityAuthorizationProfile));
		return policies;	
	}
}
