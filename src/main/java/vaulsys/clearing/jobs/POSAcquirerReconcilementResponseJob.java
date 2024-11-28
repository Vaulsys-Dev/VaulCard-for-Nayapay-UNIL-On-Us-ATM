package vaulsys.clearing.jobs;

import vaulsys.authentication.exception.InvalidTerminalOrMerchantException;
import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.calendar.DateTime;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.reconcile.POSReconcilement;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.entity.impl.Shop;
import vaulsys.message.Message;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.exception.exception.CantAddNecessaryDataToIfxException;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.terminal.TerminalClearingMode;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

public class POSAcquirerReconcilementResponseJob extends AbstractISOClearingJob {

	public static final POSAcquirerReconcilementResponseJob Instance = new POSAcquirerReconcilementResponseJob();
	private POSAcquirerReconcilementResponseJob(){
		setReconcilement(POSReconcilement.Instance);
	}
	
    public void execute(Message incomingMessage, Transaction refTransaction, ProcessContext processContext) throws Exception {
    	Message outMessage = null;
    	try {
			refTransaction.setDebugTag("POSReconcilement");
//			refTransaction.setAuthorized(true);
			ISOMsg isoMsg = (ISOMsg) incomingMessage.getProtocolMessage();
			Long termianlCode = Util.longValueOf(isoMsg.getString(41));
			POSTerminal terminal = TerminalService.findTerminal(POSTerminal.class, termianlCode);
			
			incomingMessage.setIfx(createIncommingIfx(incomingMessage, terminal));
			incomingMessage.setEndPointTerminal(terminal);
			
			if (terminal == null || terminal.getCode() == null){
//				logger.error("Terminal["+ termianlCode+"] = null!");
				throw new InvalidTerminalOrMerchantException("Terminal["+ termianlCode+"] = null!");
			}
			
			if (Severity.ERROR.equals(incomingMessage.getIfx().getSeverity())){
//				logger.error("CantAddNecessaryDataToIfxException: "+ incomingMessage.getIfx().getStatusDesc());
				throw new CantAddNecessaryDataToIfxException(incomingMessage.getIfx().getStatusDesc());
			}
			
			GeneralDao.Instance.saveOrUpdate(incomingMessage.getIfx());
			GeneralDao.Instance.saveOrUpdate(incomingMessage);
			GeneralDao.Instance.saveOrUpdate(incomingMessage.getMsgXml());
			ISOMsg outIsoMsg1 = (ISOMsg) getReconcilement().buildResponse(isoMsg, incomingMessage.getIfx(), terminal,
					processContext);
			outIsoMsg1.unset(66);
			outIsoMsg1.unset(128);
			outIsoMsg1.set(64, "0102030405060708");
			outMessage = createOutputMessage(outIsoMsg1, incomingMessage, refTransaction, terminal);
			outMessage.setIfx(createOutgoingIfx(incomingMessage));
			outMessage.setRequest(false);
			outMessage.setNeedResponse(false);
			outMessage.setNeedToBeInstantlyReversed(false);
			outMessage.setNeedToBeSent(true);
			GeneralDao.Instance.saveOrUpdate(outMessage.getIfx());
			GeneralDao.Instance.saveOrUpdate(outMessage);
			GeneralDao.Instance.saveOrUpdate(outMessage.getMsgXml());
			refTransaction.addOutputMessage(outMessage);
		} catch (Exception e) {
			if (incomingMessage.getIfx() != null){
				GeneralDao.Instance.saveOrUpdate(incomingMessage.getIfx());
				GeneralDao.Instance.saveOrUpdate(incomingMessage);
				GeneralDao.Instance.saveOrUpdate(incomingMessage.getMsgXml());
			}
			
			if (outMessage!= null && outMessage.getIfx()!= null){
				GeneralDao.Instance.saveOrUpdate(outMessage.getIfx());
				GeneralDao.Instance.saveOrUpdate(outMessage);
				GeneralDao.Instance.saveOrUpdate(outMessage.getMsgXml());
			}
			logger.error(e.getClass().getSimpleName()+": "+ e.getMessage());
			throw e; 
		}
    }

//    public IReconcilement getReconcilement() {
//        return (IReconcilement) SwitchApplication.get().getBean("posReconcilement");
//    }
    
    
    protected Ifx createIncommingIfx(Message message, POSTerminal terminal) throws Exception {
        ISOMsg protocolMessage = (ISOMsg) message.getProtocolMessage();
        Ifx ifx = new Ifx();
        
        ifx.setIfxType(IfxType.ACQUIRER_REC_RQ);

        ifx.setIfxDirection(IfxDirection.INCOMING);
        ifx.setReceivedDt(message.getStartDateTime());
        ifx.setSrc_TrnSeqCntr (Util.trimLeftZeros(protocolMessage.getString(11)));
        ifx.setMy_TrnSeqCntr (ifx.getSrc_TrnSeqCntr());
        ifx.setNetworkRefId(ifx.getSrc_TrnSeqCntr());

        String localTime = protocolMessage.getString(12).trim();
        String localDate = protocolMessage.getString(13).trim();
//        MyDateFormat dateFormatMMDDhhmmss = new MyDateFormat("MMddHHmmss");
        
        try {
			ifx.setOrigDt(new DateTime( MyDateFormatNew.parse("MMddHHmmss", localDate + localTime)));
		} catch (Exception e) {
			logger.error("Error: cannot set OrigDt"+ e); 
		}
		
//        ifx.setBankId( Util.longValueOf(protocolMessage.getString(32)));
//        ifx.setBankId (GlobalContext.getInstance().getMyInstitution().getBin());
		ifx.setBankId(ProcessContext.get().getMyInstitution().getBin().toString());
        ifx.setTerminalId(Util.trimLeftZeros(protocolMessage.getString(41)));
        ifx.setOrgIdNum(Util.trimLeftZeros(protocolMessage.getString(42)));
        ifx.setTerminalType(TerminalType.POS);
        
    	if(terminal == null){
			logger.error("Terminal is null, POSReconcilemtnMessage is not authorized. (trx: "+ message.getTransaction().getId()+") ");
			ifx.setSeverity(Severity.ERROR);
			ifx.setStatusDesc("Invalid terminal code is recieved...");
			return ifx;
    	}    		

        try {
			FinancialEntity entity = getMatchingEntity(terminal, ifx.getOrgIdNum());
			if (entity != null) {
				ifx.setName(entity.getName());
//				ifx.setCountry((entity.getSafeCountry()== null)? null:entity.getSafeCountry());
				ifx.setCountryCode((entity.getSafeCountryCode()== null)? null:entity.getSafeCountryCode());

//				ifx.setCity((entity.getSafeCity()== null)?null: entity.getSafeCity());
				ifx.setCityCode((entity.getSafeCityCode()== null)?null: entity.getSafeCityCode());
				
//				ifx.setStateProv((entity.getSafeState()== null)? null:entity.getSafeState());
				ifx.setStateCode((entity.getSafeStateCode()== null)? null:entity.getSafeStateCode());
				
//				ifx.setAddress(entity.getSafeAddress());

				if (FinancialEntityRole.SHOP.equals(entity.getRole())) {
//					if (((Shop) entity).getOwnOrParentCategory() != null)
//						ifx.setOrgIdType(((Shop) entity).getOwnOrParentCategory().getCode());
					if (((Shop) entity).getOwnOrParentCategoryId() != null)
						ifx.setOrgIdType(((Shop) entity).getOwnOrParentCategoryId());
				}
			}
		} catch (Exception e) {
			logger.error("POSReconcilemtnMessage is not authorized. (trx: "+ message.getTransaction().getId()+") " + e);
			ifx.setSeverity(Severity.ERROR);
			ifx.setStatusDesc( "Error setting Shop/Merchant information"+this.getClass().getSimpleName() + ": " + e.getMessage());
		}
		
		return ifx;
    }

    protected Ifx createOutgoingIfx(Message message) throws Exception {
    	Ifx ifx = null;
    	try {
        ifx = MsgProcessor.processor(message.getIfx());
        
        ifx.setIfxType(IfxType.ACQUIRER_REC_RS);

        ifx.setRsCode(ISOResponseCodes.APPROVED);
    	} catch (Exception e) {
    		logger.error("Error: creating Ifx for outgoing posReconcilemt rq.(Trx: "+ message.getTransaction().getId()+")" + e); 
    	}
        return ifx;
    }

	@Override
	protected TerminalClearingMode getClearingMode() {
		return TerminalClearingMode.TERMINAL;
	}
	
	private FinancialEntity getMatchingEntity(Terminal terminal, String entityCode) throws AuthorizationException {
		FinancialEntity owner = terminal.getOwner();
		try {
			if (owner == null)
				throw new AuthorizationException("Failed:FinancialEntity not available for the terminal "
						+ terminal.getCode());
			if (!owner.isOwnOrParentEnabled())
				throw new AuthorizationException("Failed:FinancialEntity " + owner.getCode() + " is disabled.");
			if (!owner.getCode().toString().equals(entityCode)) {
				throw new AuthorizationException("Failed:Owner of terminal " + terminal.getCode()
						+ " is different form merchant " + entityCode);
			}
		} catch (AuthorizationException e) {
			logger.error(e.getClass().getSimpleName()+": "+ e.getMessage());
			throw e;
		}
		return owner;
	}

	
}
