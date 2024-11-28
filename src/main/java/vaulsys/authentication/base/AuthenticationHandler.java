package vaulsys.authentication.base;

import vaulsys.authentication.exception.AuthenticationException;
import vaulsys.authentication.exception.DisableFinancialEntityException;
import vaulsys.authentication.exception.DisableTerminalException;
import vaulsys.authentication.exception.IncorrectWorkingDay;
import vaulsys.authentication.exception.InvalidTerminalOrMerchantException;
import vaulsys.authentication.exception.MacFailException;
import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.base.ClearingDate;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.entity.impl.Branch;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.entity.impl.Institution;
import vaulsys.entity.impl.Shop;
import vaulsys.message.Message;
import vaulsys.network.NetworkManager;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.endpoint.EndPointType;
import vaulsys.network.exception.NetworkException;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.base.ProtocolSecurityFunctions;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.atm.ATMState;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.PINPADTerminal;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

public class AuthenticationHandler extends BaseHandler {
	private static Logger logger = Logger.getLogger(AuthenticationHandler.class);
	
	public static final AuthenticationHandler Instance = new AuthenticationHandler();
	
	private AuthenticationHandler(){}
	
	@Override
	public void execute(ProcessContext processContext) throws Exception {
		logger.info("In Authentication Handler"); //Raza TEMP
		Message incomingMessage = processContext.getInputMessage();
		Ifx ifx = incomingMessage.getIfx();

		try {
			Channel channel = incomingMessage.getChannel();
			Terminal endPointTerminal = incomingMessage.getEndPointTerminal();
			
			if(endPointTerminal == null){
				logger.warn("No endpoint terminal on incomingMessage["+ incomingMessage.getId()+"]");
				throw new InvalidTerminalOrMerchantException("No endpoint terminal on incomingMessage["+ incomingMessage.getId()+"]");
			}

			try {
				verifyMac(endPointTerminal, channel.getProtocol().getSecurityFunctions(), endPointTerminal.getOwnOrParentSecurityProfileId(),
						endPointTerminal.getKeySet(), ifx.getMsgAuthCode(), incomingMessage.getBinaryData(), channel.getMacEnable());
			} catch (Exception e) {
				//TODO: Temporary just for Shetab BUG on reversal Response
				if(channel.getInstitutionId()!= null
					&& channel.getInstitutionId().equals("9000") && ISOFinalMessageType.isReversalOrRepeatRsMessage(ifx.getIfxType()))
					logger.error("Shetab Mac fail on reversalRs message, ignoring....");
				else
					throw new MacFailException("Failed: Mac verification failed.", e);
			}

			if (!EndPointType.isSwitchTerminal(channel.getEndPointType())) {
				authenticateTerminalMessage(endPointTerminal, ifx);
			} else {


				// If switch terminal_in(like Shetab_acqr ) is disable:
				if (!incomingMessage.getEndPointTerminal().isOwnOrParentEnabled()) {
					logger.warn("Failed:Terminal or TerminalGroup is disabled. (terminal.code= "+ incomingMessage.getEndPointTerminal().getCode() + ")");
					throw new DisableTerminalException("Failed:Terminal or TerminalGroup is disabled. (terminal.code= "
							+ incomingMessage.getEndPointTerminal().getCode() + ")");
				}


				if (EndPointType.EPAY_SWITCH_TERMINAL.equals(channel.getEndPointType())) {
					authenticateEpayInstitutionMessage(incomingMessage, ifx, channel);
				} else
					authenticateInstitutionMessage(incomingMessage, ifx, channel, ((SwitchTerminal)endPointTerminal).getOwner().getBin());
					logger.info("Institution Msg Authenticated..!");
			}
			//logger.info("Here 1"); //Raza TEMP
			if (ISOFinalMessageType.isResponseMessage(ifx.getIfxType())
					&& ISOResponseCodes.isSuccess(ifx.getRsCode())) {
				//logger.info("Here 2"); //Raza TEMP
				Transaction firstTransaction = ifx.getTransaction().getFirstTransaction();
				Channel firstTrxChannel = firstTransaction.getInputMessage().getChannel();
				
				EndPointType originalEndPoint = (firstTrxChannel!= null) ? firstTrxChannel.getEndPointType(): null;

				//TODO check this condition!! is it valid yet?
				if (originalEndPoint != null && !EndPointType.SWITCH_TERMINAL.equals(originalEndPoint)){

//				if (originalEndPoint != null && !EndPointType.SWITCH_TERMINAL.equals(originalEndPoint) && !EndPointType.DEPENDENT_SWITCH_TERMINAL.equals(originalEndPoint)){
					if (ifx.getOrgIdNum() != null && !ifx.getOrgIdNum().isEmpty())
						authenticateTerminalOwner(endPointTerminal, ifx.getOrgIdNum());
					else if (firstTransaction != null)
//						authenticateTerminalOwner(endPointTerminal, firstTransaction.getOutputMessage().getIfx().getOrgIdNum());
						authenticateTerminalOwner(endPointTerminal, firstTransaction.getOutgoingIfx().getOrgIdNum());
				}
			}
			//logger.info("Here 3"); //Raza TEMP
        } catch (Exception e) {
        	if(	e instanceof MacFailException ||
        		e instanceof InvalidTerminalOrMerchantException ||
        		e instanceof DisableFinancialEntityException ||
        		e instanceof DisableTerminalException){
        		logger.error("Authentication: "+e.getClass().getSimpleName()+": "+ e.getMessage(), e);
        	}else{
        		logger.error("Authentication: "+e.getClass().getSimpleName()+": "+ e.getMessage(), e);
        	}

			e.printStackTrace();
			processContext.getTransaction().getInputMessage().setNeedToBeInstantlyReversed(false);
			throw e;
		}
		//logger.info("Here 4"); //Raza TEMP
	}
	
	private void verifyMac(Terminal terminal, ProtocolSecurityFunctions securityFunctions, Long securityProfileId,
			Set<SecureKey> keySet, String mac, byte[] binaryData, boolean enable) throws AuthenticationException {
		try {
			if (!enable)
				return;

			if (mac == null) {
				logger.error("Failed: Mac Verification failed! (mac = null)");
				throw new MacFailException("Failed:Mac verification failed.(mac = null)");
			}

			securityFunctions.verifyMac(terminal, securityProfileId, keySet, mac, binaryData, enable);

		} catch (AuthenticationException e) {
			logger.warn(e.getClass().getSimpleName()+": "+ e.getMessage(), e);
			throw e;
		}
	}

	private void authenticateInstitutionMessage(Message incomingMessage, Ifx ifx, Channel channel, Long bin) throws AuthenticationException {
//		Institution inst = FinancialEntityService.findEntity(Institution.class, channel.getInstitution());
//		SwitchTerminal terminal = (SwitchTerminal) incomingMessage.getEndPointTerminal();
		SwitchTerminal terminal = ProcessContext.get().getSwitchTerminal(incomingMessage.getEndPointTerminalId());
		
		if( terminal == null || !bin.toString().equals(channel.getInstitutionId()) ){
			logger.error("Invalid terminal ("+terminal+") or institution "+channel.getInstitutionId());
			throw new InvalidTerminalOrMerchantException("Invalid terminal ("+terminal+") or institution "+channel.getInstitutionId());
		}

		Institution inst = terminal.getOwner();
		logger.info("Try to authorize institution[ "+ inst.getCode()+"]");

		ClearingDate lastWorkingDay = null;
		try {
			lastWorkingDay = inst.getCurrentWorkingDay();
		} catch (Exception e) {
//			Terminal terminal = incomingMessage.getEndPointTerminal();
			logger.error("Couldn't find workingDay for terminal( " + terminal.getClass().getSimpleName() + ": "
					+ terminal.getCode() + ") " + e.getClass().getSimpleName()+": "+e.getMessage());
		}
		
		MonthDayDate settleDt = null;
//		DayDate settleDt = null;

//		Long myBIN = GlobalContext.getInstance().getMyInstitution().getBin();
		Long myBIN = ProcessContext.get().getMyInstitution().getBin();
		Boolean isCheckWorkingDay = true;
		// we are issuer bank but not acquire bank
		isCheckWorkingDay = myBIN.equals(ifx.getBankId()) || !myBIN.equals(ifx.getDestBankId());
		
		if (myBIN.equals(ifx.getBankId()))
			settleDt = ifx.getPostedDt();
		else
			settleDt = ifx.getSettleDt();
		
		if (isCheckWorkingDay && lastWorkingDay != null && settleDt != null && settleDt.getDate() != null 
			&& ISOFinalMessageType.isRequestMessage(ifx.getIfxType())
			&& !lastWorkingDay.getDate().equals(settleDt)) {
			logger.error("Actual Working Day: " + lastWorkingDay.getDate()+ " Wrong institution working day:" + settleDt);
			throw new IncorrectWorkingDay("Actual Working Day: " + lastWorkingDay.getDate()+ " Wrong institution working day:" + settleDt);
		}
	}

	private void authenticateEpayInstitutionMessage(Message incomingMessage, Ifx ifx, Channel channel) throws Exception {
		Terminal terminal = TerminalService.getMatchingTerminal(ifx);
		authenticateTerminalMessage(terminal, ifx);
//		authenticateInstitutionMessage(incomingMessage, ifx, channel, ((SwitchTerminal)incomingMessage.getEndPointTerminal()).getOwner().getCode());
		authenticateInstitutionMessage(incomingMessage, ifx, channel, ProcessContext.get().getSwitchTerminal(incomingMessage.getEndPointTerminalId()).getOwnerId());
	}

	private void authenticateTerminalMessage(Terminal terminal, Ifx ifx) throws Exception {

		logger.debug("Try to authenticate terminal[ "+ terminal.getId()+"]");
		Terminal endPointTerminal = ifx.getEndPointTerminal();
		ATMTerminal atm = null;
		
		
		if (TerminalType.ATM.equals(endPointTerminal.getTerminalType()))
			atm = (ATMTerminal) endPointTerminal;
		else{
			atm = TerminalService.findTerminal(ATMTerminal.class, Long.valueOf(ifx.getTerminalId()));
			ifx.setOriginatorTerminal(atm);
		}
		
//		if (atm == null)
//			return;
		if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType()) ||
				ISOFinalMessageType.isPrepareMessage(ifx.getIfxType()) ||
				ISOFinalMessageType.isPrepareReversalMessage(ifx.getIfxType())) {
			if(terminal instanceof PINPADTerminal) {
				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("code", ifx.getOrgIdNum());
				Branch b = (Branch) GeneralDao.Instance.findObject("from Branch b where b.coreBranchCode=:code", params);
				if(b != null)
					ifx.setOrgIdNum(b.getCode().toString());
			}else if(terminal instanceof ATMTerminal){
				if (atm.getId().equals(endPointTerminal.getId())) {
					IoSession session = NetworkManager.getInstance().getResponseOnSameSocketConnectionById(ifx.getTransaction().getInputMessage().getId());
					//TASK Task108 - (3080) Bug Resalat
					String remoteAddress;
					if (session.getRemoteAddress() != null)
						remoteAddress= session.getRemoteAddress().toString();
					else {
						throw new NetworkException("Network problem - cannot get remoteAddress");
					}
					String ip = "";
					if (Util.hasText(remoteAddress)) {
						ip = remoteAddress.substring(1, remoteAddress.indexOf(":"));
					}
					
					if (Util.hasText(ip)) {
						String atmIP = atm.getIP();
						if (!Util.hasText(atmIP)){
							atm.setIP(ip);
							GeneralDao.Instance.saveOrUpdate(atm);
						} else {
							if(!ip.trim().equals(atmIP.trim())) {
								throw new AuthorizationException("last atm IP: " + atmIP + ", incomming IP: " + ip);
							}
						}
					}
					
					if (atm.getKeySet() == null || atm.getKeySet().isEmpty())
						ATMTerminalService.addDefaultKeySetForTerminal(atm);
					
					if(!ATMState.IN_SERIVCE.equals(atm.getState())){
						atm.setATMState(ATMState.IN_SERIVCE);
						GeneralDao.Instance.saveOrUpdate(atm);
					}
				}
			}
			FinancialEntity entity = authenticateTerminalOwner(terminal, ifx.getOrgIdNum());
			if (entity != null) {
				try {
					ifx.setName(entity.getName());
					
//					ifx.setCountry((entity.getSafeCountry() == null) ? null : entity.getSafeCountry());
					ifx.setCountryCode((entity.getSafeCountryCode() == null) ? null : entity.getSafeCountryCode());

//					ifx.setCity((entity.getSafeCity() == null) ? null : entity.getSafeCity());
					ifx.setCityCode((entity.getSafeCityCode() == null) ? null : entity.getSafeCityCode());

//					ifx.setStateProv((entity.getSafeState() == null) ? null : entity.getSafeState());
					ifx.setStateCode((entity.getSafeStateCode() == null) ? null : entity.getSafeStateCode());

//					ifx.setAddress(entity.getSafeAddress());
				} catch (Exception e) {
					logger.error("Encounter with an exception in setting merchant info.("
							+ e.getClass().getSimpleName() + ": " + e.getMessage() + ")", e);
					throw e;
				}

				if (FinancialEntityRole.SHOP.equals(entity.getRole())) {
//					if (((Shop) entity).getOwnOrParentCategory() != null){
					if (((Shop) entity).getOwnOrParentCategoryId() != null){
//						ifx.setOrgIdType(((Shop) entity).getOwnOrParentCategory().getCode());
						ifx.setOrgIdType(((Shop) entity).getOwnOrParentCategoryId());
					}
				}

				if (FinancialEntityRole.BRANCH.equals(entity.getRole())) {
					ifx.setName(entity.getName() + " - " + ((Branch) entity).getCoreBranchCode());
				}
				
				if (!terminal.isOwnOrParentEnabled()) {
					logger.warn("Failed:Terminal or TerminalGroup is disabled. (terminal.code= "+ terminal.getCode() + ")");
//					throw new AuthenticationException("Failed:Terminal or TerminalGroup is disabled. (terminal.code= "
//					+ terminal.getCode() + ")", true);
					throw new DisableTerminalException("Failed:Terminal or TerminalGroup is disabled. (terminal.code= "
							+ terminal.getCode() + ")");
				}
//				getGeneralDao().saveOrUpdate(ifx);
			}
		}
	}
	
	private FinancialEntity authenticateTerminalOwner(Terminal terminal, String entityCode) throws AuthenticationException {
		FinancialEntity owner = terminal.getOwner();
		if (owner == null){
			logger.warn("Failed: FinancialEntity not available for the terminal "
					+ terminal.getCode());
			throw new InvalidTerminalOrMerchantException("Failed: FinancialEntity not available for the terminal "
					+ terminal.getCode());
		}

		if (!owner.isOwnOrParentEnabled()){
			logger.warn("Failed:FinancialEntity or FinancialEntityGroup is disabled. (entity.code= " + owner.getCode() + ")");
//			throw new AuthenticationException("Failed:FinancialEntity or FinancialEntityGroup is disabled. (entity.code= "
//					+ owner.getCode() + ")", true);
			throw new DisableFinancialEntityException("Failed:FinancialEntity or FinancialEntityGroup is disabled. (entity.code= "
					+ owner.getCode() + ")");
		}

		if (!owner.getCode().toString().equals(entityCode)) {
			logger.warn("Failed: Owner of terminal " + terminal.getCode()
					+ " is different form merchant " + entityCode);
			throw new InvalidTerminalOrMerchantException("Failed: Owner of terminal " + terminal.getCode()
					+ " is different form merchant " + entityCode);
		}

		return owner;
	}

}
