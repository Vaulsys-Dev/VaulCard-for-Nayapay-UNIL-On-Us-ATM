package vaulsys.clearing.reconcile;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.ClearingIfxReconcilementData;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.pos87.encoding.HasinFarsiConvertor;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.ClearingInfo;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.SourceDestination;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.ConfigUtil;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

public class POSReconcilement extends AbstractReconcilement {

	public static final POSReconcilement Instance = new POSReconcilement();
	private POSReconcilement(){
		setResponseDataProcessor(POSResponseDataProcessor.Instance);
	}
    private static final String posReconcileSchema = ConfigUtil.getProperty(ConfigUtil.GLOBAL_PATH_SCHEMA_POSRECONCILE);
    static Logger logger = Logger.getLogger(POSReconcilement.class);
    static File schemaFile = new File(posReconcileSchema); 

    public ProtocolMessage buildRequest(Terminal terminal) throws Exception {
    	logger.error("UnsupportedOperationException");
        throw new UnsupportedOperationException();
    }

    @Override
    public ProtocolMessage buildResponse(ProtocolMessage message, Ifx ifx ,Terminal terminal, ProcessContext processContext) throws Exception {
        ISOMsg incomingMsg = (ISOMsg) message;
        ISOMsg outgoingMsg = null;
        Long toBeSettledAmount = 0L;
        POSTerminal posTerminal = (POSTerminal) terminal;
        
        try {
            try {
            	//FIXME:
            	//TODO: Just for Pep Backward Compatibility
            	if(ifx.getBankId().equals(639347L)){
	                List<ClearingIfxReconcilementData> reconcileData = parsePOSAdditionalData(posTerminal, incomingMsg, ifx);
	                setNecessaryFlagsOnTransactions(posTerminal, incomingMsg, ifx, reconcileData);
            	}
            } catch (Exception e) {
                logger.error("Error in parsing POS additional data and setting flags." + e/*, e*/);
            }

            char resetResponse = '0';

            try {
            	resetResponse = verifyPasswordResetCode(posTerminal, incomingMsg, ifx);
            } catch (Exception e) {
                logger.error("Can't retrieve reset code/account for field 43."/*, e*/+ e);
                ifx.setStatusDesc("Can't retrieve reset code/account for field 43. ("+e.getClass().getSimpleName()+": "+ e.getMessage()+")");
                ifx.setSeverity(Severity.WARN);
            }
            
            String accountNumber = "";
            try {
            	HasinFarsiConvertor convertor = new HasinFarsiConvertor();
            	String accountNumber2 = " ";
//            	String accountNumber2 = posTerminal.getOwner().getOwnOrParentAccount().getAccountNumber();
				accountNumber = new String(convertor.encode(new StringBuffer(accountNumber2).reverse().toString()));
            } catch (Exception e) {
            	logger.error("Can't retrieve account number for field 43."+ e);
            	ifx.setStatusDesc("Can't retrieve account number for field 43. ("+e.getClass().getSimpleName()+": "+ e.getMessage()+")");
            	ifx.setSeverity(Severity.WARN);
            }
            
            try {
            	retrieveApplicationVersion(posTerminal, incomingMsg, ifx);
            } catch (Exception e) {
            	logger.error("Application version of terminal "+ posTerminal.getCode() +" cannot be retrieved!");
            	ifx.setStatusDesc("Application version of terminal "+ posTerminal.getCode() +" cannot be retrieved! "+ e.getClass().getSimpleName()+": "+e.getMessage());
            	ifx.setSeverity(Severity.WARN);
            }
            
            if (TerminalService.hasRequestBasedClearingProfile(posTerminal)) {

            	toBeSettledAmount = 0L;
            	
                /******** Request Based Settlement *********/
                try {
                	TerminalService.createRequestBasedSettlementThread(terminal);
                } catch(Exception e) {
                	logger.error("Error in creating request based settlement of POS ...", e);
                }
                /*****************/	

            } else {
            	try {
            		toBeSettledAmount = TerminalService.getSumOfUnsettledFlags(posTerminal);
            	} catch (Exception e) {
            		logger.error("Can't getSumOfUnsettledFlags"+ e /*, e*/);
            	}
            }
            
            
            String toBeSettledAmountString = 
            	StringFormat.formatNew(12, StringFormat.JUST_RIGHT, toBeSettledAmount.toString(), '0');
            
    		MonthDayDate settleDt = MonthDayDate.now();
    		Map<Integer, String> processedData = getResponseDataProcessor().process(incomingMsg, posTerminal, settleDt);

    		outgoingMsg = generateISOResponseMsg(incomingMsg, toBeSettledAmountString, processedData, resetResponse, accountNumber);

        } catch (Exception e) {
        	logger.error("Error in POS buildResponse"+ e);
        }
        return outgoingMsg;
    }

	private void retrieveApplicationVersion(Terminal terminal, ISOMsg incomingMsg, Ifx ifx) throws Exception {
		String f_48 = new String((byte[])incomingMsg.getValue(48));
		
		ifx.setLast_TrnSeqCntr((Integer.parseInt(f_48.substring(15, 15 + 6))) + "");
		String applicationVersion = f_48.substring(15 + 6 + 1, 15 + 6 + 1 + Integer.parseInt(f_48.substring(15 + 6, 15 + 6 + 1)));
		ifx.setApplicationVersion(applicationVersion);
		
		TransactionService.checkValidityOfLastTransactionStatus(terminal, ifx);
		
		if (applicationVersion == null || applicationVersion.length() <= 0) {
			logger.error("Invalid Appliaction Version of Terminal["+ +terminal.getCode()+"] :" + applicationVersion);
		    throw new Exception("Invalid Appliaction Version of Terminal["+ +terminal.getCode()+"] :" + applicationVersion);
		} 
		
		((POSTerminal) terminal).setApplicationVersion(applicationVersion);
	}

	private ISOMsg generateISOResponseMsg(ISOMsg incomingMsg, String toBeSettledAmountString, Map<Integer, String> processedData, 
			char resetResponse, String accountNumber) throws NumberFormatException, ISOException, IOException {
		ISOMsg outgoingMsg = new ISOMsg();
		Integer mti = Integer.parseInt(incomingMsg.getMTI());

		String responseMTI = "0"
		        + (mti.equals(ISOMessageTypes.ACQUIRER_RECON_REQUEST_87) ? ISOMessageTypes.ACQUIRER_RECON_RESPONSE_87
		        : ISOMessageTypes.ACQUIRER_RECON_ADVICE_RESPONSE_87);
		outgoingMsg.setMTI(responseMTI);

		outgoingMsg.set(11, ISOUtil.zeroUnPad(incomingMsg.getString(11)));
		outgoingMsg.set(12, incomingMsg.getString(12));
		outgoingMsg.set(13, incomingMsg.getString(13));
		outgoingMsg.set(25, incomingMsg.getString(25));
		outgoingMsg.set(32, incomingMsg.getString(32));
		outgoingMsg.set(39, ISOResponseCodes.APPROVED/* parsePosAdditionalData(incomingMsg) */);
		outgoingMsg.set(41, ISOUtil.zeroUnPad(incomingMsg.getString(41)));
		outgoingMsg.set(42, ISOUtil.zeroUnPad(incomingMsg.getString(42)));
		outgoingMsg.set(43, incomingMsg.getString(43));

		String currentTime = MyDateFormatNew.format("yy", Calendar.getInstance().getTime());

		outgoingMsg.unset(38);
		
		ByteArrayOutputStream field48 = new ByteArrayOutputStream();
		field48.write(currentTime.getBytes());
		field48.write(toBeSettledAmountString.getBytes());
		field48.write(resetResponse);
        field48.write(accountNumber.getBytes());
		outgoingMsg.set(48, field48.toByteArray());
		
		outgoingMsg.set(66, processedData.get(66));
		outgoingMsg.set(128, "0000000000000000");
		
		return outgoingMsg;
	}

	private char verifyPasswordResetCode(POSTerminal terminal, ISOMsg incomingMsg, Ifx ifx) {
		char resetResponse = '0';
//      String accountCode = "0000-0000-0000";
		String incomingCode = incomingMsg.getString(38);
		if (incomingCode == null || incomingCode.length() != 6) {
			ifx.setResetingPassword(incomingCode);
		    incomingCode = "";
		    logger.error("Invalid reset code string.");
		} else {
		    incomingCode = incomingCode.substring(2);
		}
		ifx.setResetingPassword(incomingCode);

		String ourResetCode = (terminal).getResetCode();
		// accountCode = "";
		if (incomingCode.equals(ourResetCode)) {
			resetResponse = '1';
			(terminal).setResetCode(null);
			GeneralDao.Instance.saveOrUpdate(terminal);
		}
		return resetResponse;
	}
	
	private static List<ClearingIfxReconcilementData> parsePOSAdditionalData(POSTerminal terminal, ISOMsg isoMsg, Ifx ifx) throws Exception{
		List<ClearingIfxReconcilementData> list = new ArrayList<ClearingIfxReconcilementData>();
		StringTokenizer tokenizer;
		StringTokenizer timeTokenizer;
		StringTokenizer dateTokenizer;
		StringTokenizer lineTokenizer;
		lineTokenizer = new StringTokenizer(new String((byte[])isoMsg.getValue(48)), "!");
		
		while(lineTokenizer.hasMoreTokens()){
			String ans=lineTokenizer.nextToken().trim();
			tokenizer = new StringTokenizer(ans, "|");
			
			String row = (tokenizer.nextToken().trim());
			String trxtype = tokenizer.nextToken().trim();
			
			String dateStr = tokenizer.nextToken().trim();
			dateTokenizer = new StringTokenizer(dateStr, "/");
			int year = Integer.parseInt(dateTokenizer.nextToken().trim());
			int month = Integer.parseInt(dateTokenizer.nextToken().trim());
			int day = Integer.parseInt(dateTokenizer.nextToken().trim());		
			
			String timeStr = tokenizer.nextToken().trim();
			timeTokenizer = new StringTokenizer(timeStr, ":");
			int hour = Integer.parseInt(timeTokenizer.nextToken().trim());
			int minute = Integer.parseInt(timeTokenizer.nextToken().trim());
			int second = Integer.parseInt(timeTokenizer.nextToken().trim());
			
			String origDataStr = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
			
			String seqCtr = Util.trimLeftZeros(tokenizer.nextToken().trim());
			String pan = tokenizer.nextToken().trim();
			Long issuerId = Util.longValueOf(tokenizer.nextToken().trim());
			String terminalId = Util.trimLeftZeros(tokenizer.nextToken().trim());
			Long amount = Util.longValueOf(tokenizer.nextToken().trim());
			
			ClearingIfxReconcilementData reconcilementData = new ClearingIfxReconcilementData(ifx, trxtype, new DateTime(MyDateFormatNew.parse("yyyy-MM-dd HH:mm:ss", origDataStr)), seqCtr, pan, issuerId, terminalId, amount);
            list.add(reconcilementData);
			
	
		}
		return list;
	}
		
	private String setNecessaryFlagsOnTransactions(POSTerminal terminal, ISOMsg isoMsg, Ifx ifx, List<ClearingIfxReconcilementData> recDataList) throws Exception {

		Transaction transaction = null;
		List<Message> altMsg = new ArrayList<Message>();

		Long merchantCode = Util.longValueOf((isoMsg.getString(42)));

		for (ClearingIfxReconcilementData reconcilementData : recDataList) {

			GeneralDao.Instance.saveOrUpdate(reconcilementData);

			String seqCtr = reconcilementData.getTrnSeqCntr();

			String origDtStr = reconcilementData.getOrigDt().getDayDate().getMonth() + "-"
					+ reconcilementData.getOrigDt().getDayDate().getDay() + " "
					+ reconcilementData.getOrigDt().getDayTime().getHour() + ":"
					+ reconcilementData.getOrigDt().getDayTime().getMinute() + ":"
					+ reconcilementData.getOrigDt().getDayTime().getSecond();

			DateTime origDt = new DateTime(MyDateFormatNew.parse("MM-dd HH:mm:ss", origDtStr));

			String terminalId = reconcilementData.getTerminalId();

			String pan = reconcilementData.getAppPan();

			Long amount = reconcilementData.getAmount();

			try {
				String query = "select m from Message m inner join m.ifx as i "
						+ " where i.networkTrnInfo.Src_TrnSeqCntr = :trnSeqCntr "
						+ " and i.networkTrnInfo.OrigDt = :origDt" 
						+ " and i.networkTrnInfo.TerminalId = :terminalId"
						+ " and i.request = false "
//						+ " and i.ifxType in " + IfxType.getRsOrdinalsCollectionString() 
						+ " and m.type = :outgoing";

				Map<String, Object> params = new HashMap<String, Object>();
				params.put("trnSeqCntr", seqCtr);
				params.put("origDt", origDt);
				params.put("terminalId", terminalId);
				params.put("outgoing", MessageType.OUTGOING);

				List<Message> outgoingMessages = GeneralDao.Instance.find(query, params);

				if (outgoingMessages == null || outgoingMessages.size() == 0) {
					logger.error("No Response was recieved for TrnSeqCntr " + seqCtr + " on terminal " + terminalId
							+ " at original Date" + origDtStr);
					continue;
				} else if (outgoingMessages.size() > 1) {
					logger.error("Duplicate message found for TrnSeqCntr " + seqCtr + " on terminal " + terminalId);
					continue;
				}

				Message outgoingMsg = outgoingMessages.get(0);
				transaction = outgoingMsg.getTransaction();
				Ifx outIfx = outgoingMsg.getIfx();

//				if (!(TransactionStatus.DONE.equals(transaction.getStatus()))) {
//					logger.error("Not Complete Response for TrnSeqCntr " + seqCtr + " on terminal " + terminalId
//							+ " at original Date" + origDtStr);
//					ifx.addClearingTransaction(transaction, transaction.getSourceClearingInfo(), null, SourceDestination.SOURCE);
//					continue;
//				}
				if (!outIfx.getOrgIdNum().equals(merchantCode.toString())) {
					logger.error("Not matched Merchant for TrnSeqCntr " + seqCtr + " on terminal " + terminalId
							+ " at original Date" + origDtStr + "( ifx.MerchantCode = " + outIfx.getOrgIdNum()
							+ " Recieved merchant Code=" + merchantCode);
					ifx.addClearingTransaction(transaction, transaction.getSourceClearingInfo(), null, SourceDestination.SOURCE);
					continue;
				}

				if (!outIfx.getAppPAN().substring(0, 16).equals(pan)) {
					logger.error("Not matched AppPAN for TrnSeqCntr " + seqCtr + " on terminal " + terminalId
							+ " at original Date" + origDtStr);
					ifx.addClearingTransaction(transaction, transaction.getSourceClearingInfo(), null, SourceDestination.SOURCE);
					continue;
				}

				if (!outIfx.getAuth_Amt().equals(amount)) {
					logger.warn("Not matched Amount for TrnSeqCntr " + seqCtr + " on terminal " + terminalId
							+ " at original Date" + origDtStr);
					// continue;
				}

				if (ISOFinalMessageType.isReversalMessage(outIfx.getIfxType())) {
//					revMsgs.add(outgoingMsg);
				} else {
					altMsg.add(outgoingMsg);
				}
			} catch (Exception e) {
				logger.error("Encounter with an Exception( "+ e.getClass().getSimpleName()+" :"+ e.getMessage()+")", e);
//				e.printStackTrace();
			}
		}
            
		List<Message> balanceMessages = TransactionService.getDesiredMessages(ClearingState.NOT_CLEARED, terminal, TrnType.BALANCEINQUIRY);
			
		setDesiredFlagOnSetOfMessages(ifx, balanceMessages, ClearingState.CLEARED);
//		setDesiredFlagOnSetOfMessages(ifx, revMsgs, ClearingState.IGNORED);
		setDesiredFlagOnSetOfMessages(ifx, altMsg, ClearingState.CLEARED);
		
		//It should be the last piece of work to be done! Don't move it!!!
		List<Message> notCleardTransactions = TransactionService.getDesiredMessages(ClearingState.NOT_CLEARED, terminal, null);
		setDesiredFlagOnSetOfMessages(ifx, notCleardTransactions, ClearingState.DISAGREEMENT);
		
        return ISOResponseCodes.APPROVED;
	}

	private void setDesiredFlagOnSetOfMessages(Ifx ifx, List<Message> messages, ClearingState newState) {
		List<Transaction> trxs;
		if (messages == null || messages.size() == 0)
			return;
		trxs = TransactionService.getTransactionsFromMessages(messages);
		for (Transaction t : trxs) {
		    ClearingInfo prev = new ClearingInfo(t.getSourceClearingInfo());
		    ClearingInfo clearingInfo = t.getSourceClearingInfo();
		    clearingInfo.setClearingState(newState);
		    clearingInfo.setClearingDate(DateTime.now());
		    
		    GeneralDao.Instance.saveOrUpdate(clearingInfo);

		    ifx.addClearingTransaction(t, prev, t.getSourceClearingInfo(), SourceDestination.SOURCE);
		}
	}
}
