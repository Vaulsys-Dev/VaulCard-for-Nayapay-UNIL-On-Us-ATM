package vaulsys.clearing.jobs.infotech;

import vaulsys.authentication.exception.InvalidTerminalOrMerchantException;
import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.jobs.AbstractISOClearingJob;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.entity.impl.Shop;
import vaulsys.message.Message;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.exception.exception.CantAddNecessaryDataToIfxException;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOBinaryField;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.PaymentSchemes.base.ISOTransactionCodes;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.terminal.TerminalClearingMode;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.MyInteger;
import vaulsys.util.Util;
import vaulsys.util.constants.ASCIIConstants;
import vaulsys.wfe.ProcessContext;

import java.io.ByteArrayOutputStream;

import org.apache.log4j.Logger;

public class InfotechClearingJob extends AbstractISOClearingJob {
	private static Logger logger = Logger.getLogger(InfotechClearingJob.class);
	
	public static final InfotechClearingJob Instance = new InfotechClearingJob();
	
	private InfotechClearingJob(){}
	
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
				logger.error("Terminal["+ termianlCode+"] = null!");
				throw new InvalidTerminalOrMerchantException("Terminal["+ termianlCode+"] = null!");
			}
			
			if (Severity.ERROR.equals(incomingMessage.getIfx().getSeverity())){
				logger.error("CantAddNecessaryDataToIfxException: "+ incomingMessage.getIfx().getStatusDesc());
				throw new CantAddNecessaryDataToIfxException(incomingMessage.getIfx().getStatusDesc());
			}
			
			GeneralDao.Instance.saveOrUpdate(incomingMessage.getIfx());
			GeneralDao.Instance.saveOrUpdate(incomingMessage);
			GeneralDao.Instance.saveOrUpdate(incomingMessage.getMsgXml());
			
			try {
				TransactionService.checkValidityOfLastTransactionStatus(terminal, incomingMessage.getIfx());
			} catch (Exception e) {
				logger.error("Error in putting desired flag on last transaction, received message is CLEARING", e);
			}
			
			ISOMsg outIsoMsg1 = (ISOMsg) buildResponse(isoMsg, incomingMessage, terminal, processContext);
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
    
    
    public ProtocolMessage buildResponse(ProtocolMessage message, Message msg ,Terminal terminal, ProcessContext processContext) throws Exception {
        ISOMsg incomingMsg = (ISOMsg) message;
        ISOMsg outgoingMsg = new ISOMsg();
        Ifx ifx = msg.getIfx();
        POSTerminal posTerminal = (POSTerminal) terminal;
        
        
        Integer mti = Integer.parseInt(incomingMsg.getMTI());

		String responseMTI = "0" + ISOMessageTypes.ACQUIRER_RECON_RESPONSE_87;
		outgoingMsg.setMTI(responseMTI);

//		MyDateFormat dateFormatYYYYMMDDhhmmss = new MyDateFormat("yyyyMMddHHmmss");
		outgoingMsg.set(3, incomingMsg.getString(3));
		outgoingMsg.set(7, MyDateFormatNew.format("yyyyMMddHHmmss", DateTime.now().toDate()));
		outgoingMsg.set(11, ISOUtil.zeroUnPad(incomingMsg.getString(11)));
		outgoingMsg.set(12, incomingMsg.getString(12));
		outgoingMsg.set(13, incomingMsg.getString(13));
		outgoingMsg.set(32, incomingMsg.getString(32));
		outgoingMsg.set(39, ISOResponseCodes.APPROVED);
		outgoingMsg.set(41, ISOUtil.zeroUnPad(incomingMsg.getString(41)));
		outgoingMsg.set(42, ISOUtil.zeroUnPad(incomingMsg.getString(42)));
		
//		EncodingConvertor convertor = GlobalContext.getInstance().getConvertor(msg.getChannel().getEncodingConvertor());
		EncodingConvertor convertor = ProcessContext.get().getConvertor(msg.getChannel().getEncodingConverter());
		byte[] field48Rs = TerminalService.generalInfotechField48Rs(ifx,convertor, posTerminal);
		ByteArrayOutputStream field48 = new ByteArrayOutputStream();
		field48.write(field48Rs);

		if (IfxType.MERCHANT_BALANCE_RQ.equals(ifx.getIfxType())) {
			
			Long toBeSettledAmount = 0L;
			
			
			if (TerminalService.hasRequestBasedClearingProfile(posTerminal)) {
				toBeSettledAmount = 0L;
				
				/******** Request Based Settlement *********/
	            
	            DateTime now = DateTime.now();
				if (now.after(new DateTime(now.getDayDate(), new DayTime(0, 25)))
						&& now.before(new DateTime(now.getDayDate(), new DayTime(0, 40)))) {
					logger.debug("infotech send automatically merchant_balance, ignoring....");
					
				} else {
					try {
						TerminalService.createRequestBasedSettlementThread(posTerminal);
					} catch (Exception e) {
						logger.error("Error in creating request based settlement of POS ...", e);
					}
				}
	            /*****************/
				
			} else {
				toBeSettledAmount = TerminalService.getSumOfUnsettledFlags(posTerminal);
				try {
				} catch (Exception e) {
					logger.error("Can't getSumOfUnsettledFlags"+ e /*, e*/);
				}
			}
            
    		

            
            field48.write(convertor.encode(toBeSettledAmount.toString()));
            field48.write(ASCIIConstants.FS);
			
//            byte[] accountNumber = null;
//            try {
//            	accountNumber = convertor.encode(posTerminal.getOwner().getOwnOrParentAccount().getAccountNumber());
//            } catch (Exception e) {
//            	logger.error("Can't retrieve account number for field 48."+ e);
//            	ifx.setStatusDesc("Can't retrieve account number for field 48. ("+e.getClass().getSimpleName()+": "+ e.getMessage()+")");
//            	ifx.setSeverity(Severity.WARN);
//            }
            
            field48.write(" ".getBytes());
//            field48.write(posTerminal.getOwner().getOwnOrParentAccount().getAccountNumber().getBytes());
            field48.write(ASCIIConstants.FS);

		}
		
		outgoingMsg.set(new ISOBinaryField(48, field48.toByteArray()));
		outgoingMsg.set(64, "0000000000000000");
        
        return outgoingMsg;
    }

    protected Ifx createIncommingIfx(Message message, POSTerminal terminal) throws Exception {
        ISOMsg protocolMessage = (ISOMsg) message.getProtocolMessage();
        Ifx ifx = new Ifx();

        Integer emvTrnType = Integer.parseInt(protocolMessage.getString(3).substring(0, 2).trim());
        if (emvTrnType.equals(ISOTransactionCodes.MERCHANT_BALANCE)) {
        	ifx.setIfxType(IfxType.MERCHANT_BALANCE_RQ);
        	
        } else if (emvTrnType.equals(ISOTransactionCodes.BATCH_UPLOAD)) {
        	ifx.setIfxType(IfxType.BATCH_UPLOAD_RQ);
        }

        ifx.setIfxDirection(IfxDirection.INCOMING);
        ifx.setReceivedDt(message.getStartDateTime());
        ifx.setSrc_TrnSeqCntr (Util.trimLeftZeros(protocolMessage.getString(11)));
        ifx.setMy_TrnSeqCntr (ifx.getSrc_TrnSeqCntr());
        ifx.setNetworkRefId(ifx.getSrc_TrnSeqCntr());

        String localTime = protocolMessage.getString(12).trim();
        String localDate = protocolMessage.getString(13).trim();
//        MyDateFormat dateFormatYYYYMMDDhhmmss = new MyDateFormat("yyyyMMddHHmmss");
        
        try {
			ifx.setOrigDt(new DateTime( MyDateFormatNew.parse("yyyyMMddHHmmss", localDate + localTime)));
		} catch (Exception e) {
			logger.error("Error: cannot set OrigDt"+ e); 
		}
		
        ifx.setBankId(protocolMessage.getString(32));
        ifx.setTerminalId(Util.trimLeftZeros(protocolMessage.getString(41)));
        ifx.setOrgIdNum(Util.trimLeftZeros(protocolMessage.getString(42)));
        ifx.setTerminalType(TerminalType.POS);

        byte[] field48 = null;
        if (protocolMessage.hasField(48)) {
			field48 = (byte[]) protocolMessage.getValue(48);
		}
        
        /***P48 INGENICO POS format: 
         * 6 byte last sequence counter,
         * application version,
         * ***/ 
        
        MyInteger offset = new MyInteger(0);
        ifx.setLast_TrnSeqCntr(NDCParserUtils.readUntilFS(field48, offset));
        NDCParserUtils.readFS(field48, offset);
        
        ifx.setApplicationVersion(NDCParserUtils.readUntilFS(field48, offset));
        NDCParserUtils.readFS(field48, offset);
        
        ifx.setSerialno(protocolMessage.getString(53));
        ifx.setMsgAuthCode(protocolMessage.getString(64));
        
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
        
        if (message.getIfx().getIfxType().equals(IfxType.MERCHANT_BALANCE_RQ))
        	ifx.setIfxType(IfxType.MERCHANT_BALANCE_RS);
        else if (message.getIfx().getIfxType().equals(IfxType.BATCH_UPLOAD_RQ))
        	ifx.setIfxType(IfxType.BATCH_UPLOAD_RS);

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
