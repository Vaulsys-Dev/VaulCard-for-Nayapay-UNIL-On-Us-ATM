package vaulsys.protocols.handlers;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.calendar.DateTime;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.protocols.base.ProtocolFunctions;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;

public class IfxToProtocolHandler extends BaseHandler {
    private static Logger logger = Logger.getLogger(IfxToProtocolHandler.class);

    public static final IfxToProtocolHandler Instance = new IfxToProtocolHandler();

    private IfxToProtocolHandler(){
    }

    @Override
    public void execute(ProcessContext processContext) throws Exception {
        try {
        	Message outgoingMessage = processContext.getOutputMessage();

//            putFlagOnTransaction(outgoingMessage);
        	TransactionService.putFlagOnTransaction(processContext.getTransaction());

            try {
                if (outgoingMessage != null) {
                    Channel channel = outgoingMessage.getChannel();

                    ProtocolFunctions mapper = channel.getProtocol().getMapper();
//                    logger.debug("producing protocol message ...");
//                    ((BaseComponent) mapper).setProcessContext(processContext);

                     Ifx ifx = outgoingMessage.getIfx();
                    
                    //Mirkamali(Task166): Adapt with shetab's V7
                    TransactionService.matchTrnSeqCntr(ifx);
                    
                    /*************** Use accInfo that save in a map in transfer_from_rs trx(KAMELIA) ***************/
                    if(ifx.getIfxType().equals(IfxType.TRANSFER_RS)) {
                    	String listOfAcctInTransFrom = GlobalContext.getInstance().getAccInfoForTransfer(ifx.getTransaction().getLifeCycleId());
                    	if(Util.hasText(listOfAcctInTransFrom)){
                    		StringTokenizer tokenizer = new StringTokenizer(listOfAcctInTransFrom, "|");
                        	tokenizer.nextToken(); //dateTime
                        	
                        	// set acctBalAvailable
                        	if(tokenizer.hasMoreTokens())
                        		ifx.setTransientAcctBalAvailableType(new AccType(Integer.valueOf(tokenizer.nextToken().trim())));
                        	if(tokenizer.hasMoreTokens())
                        		ifx.setTransientAcctBalAvailableBalType(new BalType(Integer.valueOf(tokenizer.nextToken().trim())));
                        	if(tokenizer.hasMoreTokens())
                        		ifx.setTransientAcctBalAvailableAmt(tokenizer.nextToken().trim());
                        	if(tokenizer.hasMoreTokens())
                        		ifx.setTransientAcctBalAvailableCurCode(tokenizer.nextToken().trim());
                        	
                        	//set AccBalLedger
                        	if(tokenizer.hasMoreTokens())
                        		ifx.setTransientAcctBalLedgerType(new AccType(Integer.valueOf(tokenizer.nextToken().trim())));
                        	if(tokenizer.hasMoreTokens())
                        		ifx.setTransientAcctBalLedgerBalType(new BalType(Integer.valueOf(tokenizer.nextToken().trim())));
                        	if(tokenizer.hasMoreTokens())
                        		ifx.setTransientAcctBalLedgerAmt(tokenizer.nextToken().trim());
                        	if(tokenizer.hasMoreTokens())
                        		ifx.setTransientAcctBalLedgerCurCode(tokenizer.nextToken().trim());
                        	
                        	GlobalContext.getInstance().removeAcctInfoForTransfer(ifx.getTransaction().getLifeCycleId());
                    	}
                    }
					mapper.addOutgoingNecessaryData(ifx, outgoingMessage.getTransaction());
//                    EncodingConvertor convertor = GlobalContext.getInstance().getConvertor(channel.getEncodingConvertor());
                    EncodingConvertor convertor = ProcessContext.get().getConvertor(channel.getEncodingConverter());
                    outgoingMessage.setProtocolMessage(mapper.fromIfx(ifx, convertor));
                    
//                    ifx.setFwdToBankId(channel.getInstitution());
                    
                    /*** 1390/07/25  Performance ***/
                    if(ISOFinalMessageType.isRequestMessage(ifx.getIfxType()) &&
                    		!ISOFinalMessageType.isReversalMessage(ifx.getIfxType()) &&
                    		ProcessContext.get().getMyInstitution().getBin().equals(ifx.getBankId()) &&
                    		!ProcessContext.get().getMyInstitution().getBin().equals(ifx.getDestBankId()) &&
                    		ifx.getTerminalType() != null && !TerminalType.isPhisycalDeviceTerminal(ifx.getTerminalType()) &&
        					FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole())) {
                    	
                    	String bindingParam = TransactionService.getBindingString(ifx);
                    	
						GlobalContext.getInstance().addBindTransaction(bindingParam, processContext.getTransaction().getId());
                    }
                    
                    /*** 1390/08/18  security fields ***/
                    if(ISOFinalMessageType.isRequestMessage(ifx.getIfxType()) &&
//                    		!ShetabFinalMessageType.isReversalMessage(ifx.getIfxType()) &&
//                    		ProcessContext.get().getMyInstitution().getBin().equals(ifx.getBankId()) &&
//                    		!ProcessContext.get().getMyInstitution().getBin().equals(ifx.getDestBankId()) &&
//                    		ifx.getTerminalType() != null && !TerminalType.isPhisycalDeviceTerminal(ifx.getTerminalType()) &&
                    		FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole())) {
                    	
                    	String securityParam = TransactionService.getSecurityString(ifx.getEMVRqData());
                    	
                    	GlobalContext.getInstance().addSecurityData(ifx.getTransaction().getLifeCycleId(), securityParam);
                    }
                    
                    /*************** Add acctInfo to a map in a transfer_from_rs trx(KAMELIA) ***************/
                    Message incomingMessage = processContext.getInputMessage();
                    Ifx incomingIfx = incomingMessage.getIfx();
    				if( incomingIfx != null
    					&& incomingIfx.getIfxType().equals(IfxType.TRANSFER_FROM_ACCOUNT_RS)
    					&& processContext.getMyInstitution().getBin().equals(ifx.getBankId()) ) {
    					
    					String listOfAcct = DateTime.now().toString()+ "|";
    					//mande hesab
    					AcctBal acctBalAvailable = incomingIfx/*.getSafeTransientAcctBalAvailable().getEmvrsdata()*/.getTransientAcctBalAvailable();
    					//ghabele bardasht
    					AcctBal acctBalLedger = incomingIfx/*.getSafeTransientAcctBalAvailable().getEmvrsdata()*/.getSafeTransientAcctBalLedger();
    					
    					if(acctBalAvailable != null){
    						if(acctBalAvailable.getAcctType() != null) {
        						listOfAcct += acctBalAvailable.getAcctType().toString();
        						listOfAcct += "|";
        					}

        					listOfAcct += BalType.AVAIL;
//        					listOfAcct += acctBalAvailable.getBalType();
        					listOfAcct += "|";
        					
        					if(Util.hasText(acctBalAvailable.getAmt())) {
        						listOfAcct += acctBalAvailable.getAmt().trim();
        						listOfAcct += "|";
        					}

        					if (Util.hasText(acctBalAvailable.getCurCode()))
        						listOfAcct += acctBalAvailable.getCurCode().trim();
        					else
        						listOfAcct += ProcessContext.get().getRialCurrency().getCode().toString();
        					listOfAcct += "|";
    					}

    					if(acctBalLedger != null){
    						if(acctBalLedger.getAcctType() != null) {
        						listOfAcct += acctBalLedger.getAcctType().toString();
        						listOfAcct += "|";
        					}
        					
        					listOfAcct += BalType.LEDGER;
//        					listOfAcct += acctBalLedger.getBalType();
        					listOfAcct += "|";
        					
        					if (Util.hasText(acctBalLedger.getAmt())) {
        						listOfAcct += acctBalLedger.getAmt().trim();
        						listOfAcct += "|";
        					}	
        					else
        						if(acctBalAvailable != null){
        							if (Util.hasText(acctBalAvailable.getAmt())) {
            							listOfAcct += acctBalAvailable.getAmt().trim();
            							listOfAcct += "|";
            						}
        						}
        						
        							
        					if (Util.hasText(acctBalLedger.getCurCode()))
        						listOfAcct += acctBalLedger.getCurCode().trim();
        					else
        						listOfAcct += ProcessContext.get().getRialCurrency().getCode().toString();
        					listOfAcct += "|";
    					}
    					
    					
    					GlobalContext.getInstance().addAcctInfoForTransfer(incomingMessage.getTransaction().getLifeCycleId(), listOfAcct);
    				}
                	/*******************************************************************************************************/
                    
                } else {
                    // throw new NotApplicableTypeMessageException();
                }
            } catch (Exception ex) {
                throw ex;
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
