package vaulsys.scheduler.base;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.message.Message;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.scheduler.SchedulerService;
import vaulsys.transaction.Transaction;
import vaulsys.util.SwitchRuntimeException;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

public class RemoveRepeatReversalTriggerHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(RemoveRepeatReversalTriggerHandler.class);

    public static final RemoveRepeatReversalTriggerHandler Instance = new RemoveRepeatReversalTriggerHandler();

    private RemoveRepeatReversalTriggerHandler(){
    }

    @Override
    public void execute(ProcessContext processContext) throws Exception {
        removeFromScheduler(processContext);
    }

    public void removeFromScheduler(ProcessContext processContext) throws SchedulerException {

        try {
            Message incomingMessage;
            Transaction transaction = processContext.getTransaction();
            if (transaction.getInputMessage().isIncomingMessage()){
            	if((transaction.getIncomingIfx().getIfxType().equals(IfxType.CUTOVER_RS) || transaction.getIncomingIfx().getIfxType().equals(IfxType.CUTOVER_REPEAT_RS))
            			&& !ISOResponseCodes.isSuccess(transaction.getIncomingIfx().getRsCode())){
            		logger.error("CutOff response is not successful,filed 39 is: " + transaction.getIncomingIfx().getRsCode() + " we should repeat it");
            		return;
            	}

                incomingMessage = transaction.getInputMessage();
                Transaction referenceTransaction = transaction.getReferenceTransaction();


				if (incomingMessage.getIfx() != null) {
                    Ifx incomigIfx = incomingMessage.getIfx();
                    if (ISOFinalMessageType.isReversalRsMessage(incomigIfx.getIfxType())
                    	//NOTE: should only remove our own reverses
                    	&& isSelfGeneratedReverse(incomigIfx)
                    	&& !ISOResponseCodes.shouldBeRepeated(incomigIfx.getRsCode())) {


                    	if (referenceTransaction == null)
                        	referenceTransaction = transaction.getFirstTransaction().getReferenceTransaction();
                    	
                    	if (referenceTransaction == null)
                    		referenceTransaction = transaction.getFirstTransaction();

						//m.rehman: we need to remove advices only in case of APPROVED Response Code, otherwise it will remain in db with response code
						if (incomigIfx.getRsCode().equals(ISOResponseCodes.APPROVED))
							SchedulerService.removeReversalJobInfo(referenceTransaction.getId());
						else
							SchedulerService.updateJobInfo(referenceTransaction.getId(), incomigIfx.getRsCode());
                        //SchedulerService.removeReversalJobInfo(referenceTransaction.getId());

                        logger.debug("try to remove unschedule reversal for reversal transactionId = " + referenceTransaction.getId() );

                    }
					//m.rehman: to handle advice messages
					else if ((ISOMessageTypes.isFinancialAdviceResponseMessage(incomigIfx.getMti())
							|| ISOMessageTypes.isLoroAdviceResponseMessage(incomigIfx.getMti()))
							&& isSelfGeneratedReverse(incomigIfx)) {

						if (referenceTransaction == null)
							referenceTransaction = transaction.getFirstTransaction().getReferenceTransaction();

						if (referenceTransaction == null)
							referenceTransaction = transaction.getFirstTransaction();

						if (incomigIfx.getRsCode().equals(ISOResponseCodes.APPROVED))
							SchedulerService.removeConfirmationJobInfo(referenceTransaction.getId());
						else
							SchedulerService.updateJobInfo(referenceTransaction.getId(), incomigIfx.getRsCode());

						logger.debug("try to remove/update unschedule advice for confirmation transactionId = " + referenceTransaction.getId() );

					} else if (!ISOFinalMessageType.isReversalRsMessage(incomigIfx.getIfxType())
                            && ISOFinalMessageType.isResponseMessage(incomigIfx.getIfxType())
                            /*&& hasReversTrigger(incomigIfx)
                              &&(ShetabErrorCodes.isSuccess(incomigIfx.RsCode)
                            || ShetabErrorCodes.cannotBeDone(incomigIfx.RsCode) )*/
    //                        !ShetabErrorCodes.shouldBeRepeated(incomigIfx.RsCode)
                            ) {

                    	
                    	/**** created: 2011/11/28 change in transfer reversal ****/
                    	if (ISOResponseCodes.isMessageDone(incomigIfx.getRsCode()) ||
							(!ISOResponseCodes.isMessageDone(incomigIfx.getRsCode()) &&
                    					!ISOFinalMessageType.isTransferToMessage(incomigIfx.getIfxType()) &&
                    					!ISOFinalMessageType.isTransferToAccountTransferToMessage(incomigIfx.getIfxType()))
			) {
                    		logger.debug("try to remove unschedule reversal for financial transactionId = " + transaction.getFirstTransaction().getId() );
            				SchedulerService.removeReversalJobInfo(transaction.getFirstTransaction().getId());
                    	} else {
                    		logger.debug("don't remove unschedule reversal for financial transactionId = " + transaction.getFirstTransaction().getId() );
                    	}
                    	
                    	/**** deleted: 2011/11/28 change in transfer reversal ****/
                    	/*if (ShetabFinalMessageType.isTransferToMessage(incomigIfx.getIfxType())) {
                    			if (ErrorCodes.shouldNotBeReversedForTransfer(incomigIfx.getRsCode())) {
                    				logger.debug("try to remove unschedule reversal for financial transactionId = " + transaction.getFirstTransaction().getId() );
                    				SchedulerService.removeReversalJobInfo(transaction.getFirstTransaction().getId());
                    			} else {
                    				logger.debug("don't remove unschedule reversal for financial transactionId = " + transaction.getFirstTransaction().getId() );
                    				
                    			}
                    		
                    	} else {
	                    	if (!ErrorCodes.shouldNotBeReversedForTransfer(incomigIfx.getRsCode())) {
		                    	logger.debug("try to remove unschedule reversal for financial transactionId = " + transaction.getFirstTransaction().getId() );
		                        SchedulerService.removeReversalJobInfo(transaction.getFirstTransaction().getId());
		                        
	                    	} else {
	                    		if (ProcessContext.get().getMyInstitution().getBin().equals(incomigIfx.getDestBankId()) && ErrorCodes.MESSAGE_FORMAT_ERROR.equals(incomigIfx.getRsCode())) {
	                    			logger.debug("we are issuer! try to remove unschedule reversal for financial transactionId = " + transaction.getFirstTransaction().getId() );
	                    			SchedulerService.removeReversalJobInfo(transaction.getFirstTransaction().getId());
	                    			
	                    		} else {
	                    			logger.debug("don't remove unschedule reversal for financial transactionId = " + transaction.getFirstTransaction().getId() );
	                    		}	                    		
	                    	}
                    	}*/
                    }
                } else {
                	//dige nabayad byayim injaa! chon hatta network message haa ham ifx darand!!
                	logger.warn("There must not be any message without IFX!!");
                    try {
                        ISOMsg isoMsg = (ISOMsg) incomingMessage.getProtocolMessage();
                        String strMTI;
                        strMTI = isoMsg.getMTI();
                        //int mti = Integer.parseInt(strMTI); //Raza commenting Int not using
                        // Ifx outgoingIfx = outgoingMsg.getIfx();
                        if (strMTI.equals(ISOMessageTypes.NETWORK_MANAGEMENT_RESPONSE_87)
                                || strMTI.equals(ISOMessageTypes.NETWORK_MANAGEMENT_RESPONSE_93)
                                || strMTI.equals(ISOMessageTypes.ACQUIRER_RECON_RESPONSE_87)
                                || strMTI.equals(ISOMessageTypes.ACQUIRER_RECON_RESPONSE_93)
                                || strMTI.equals(ISOMessageTypes.ISSUER_RECON_RESPONSE_87)
                                || strMTI.equals(ISOMessageTypes.ISSUER_RECON_RESPONSE_93)
                                || strMTI.equals(ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_RESPONSE_87)
                                || strMTI.equals(ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_RESPONSE_93)
                                || strMTI.equals(ISOMessageTypes.ACQUIRER_RECON_ADVICE_RESPONSE_87)
                                || strMTI.equals(ISOMessageTypes.ACQUIRER_RECON_ADVICE_RESPONSE_93)
                                || strMTI.equals(ISOMessageTypes.ISSUER_RECON_ADVICE_RESPONSE_87)
                                || strMTI.equals(ISOMessageTypes.ISSUER_RECON_ADVICE_RESPONSE_93)) {

                        	if (referenceTransaction == null) {
                        		if (transaction != null) {
                        			if (transaction.getFirstTransaction() != null) {
                        				referenceTransaction = transaction.getFirstTransaction();
	                        			SchedulerService.removeReversalJobInfo(referenceTransaction.getId());
	                        			logger.debug("try to remove unschedule repeat or reversal for transactionId = " + referenceTransaction.getId() );
                        			}
                        			if (transaction.getReferenceTransaction() != null) {
                        				referenceTransaction = transaction.getReferenceTransaction();
                        				SchedulerService.removeReversalJobInfo(referenceTransaction.getId());
                        				logger.debug("try to remove unschedule repeat or reversal for transactionId = " + referenceTransaction.getId() );
                        			}
                        			if (transaction.getFirstTransaction().getFirstTransaction() != null) {
                        				referenceTransaction = transaction.getFirstTransaction().getFirstTransaction();
                        				SchedulerService.removeReversalJobInfo(referenceTransaction.getId());
                        				logger.debug("try to remove unschedule repeat or reversal for transactionId = " + referenceTransaction.getId() );
                        			}
                        		}
                        		else 
                        			referenceTransaction = transaction;
                        	}
                            SchedulerService.removeReversalJobInfo(referenceTransaction.getId());
                            logger.debug("try to remove unschedule repeat or reversal for transactionId = " + referenceTransaction.getId() );
                        }
                    } catch (ISOException e) {
                        logger.error(e,e);
                    }

                }
            }
        } catch (NumberFormatException e) {
            logger.error(e,e);
            throw new SwitchRuntimeException(e);
        }
    }

    private boolean isSelfGeneratedReverse(Ifx incomigIfx) {
    	return true;
    	/*
    	 *  1- if we are aqcuier, reversal message is self-generated
    	 *  2- if we are not issuer and aqcuier (we're only forwarding switch), reversal message is self-generated,
    	 *  3- otherwise, we are only issuer so reversal message is not self-generated
    	*/

//		Long myBin = SwitchApplication.get().getFinancialEntityService().getMyInstitution().getBin();
//		if (myBin.equals(incomigIfx.getBankId()))
//			return true;
//		else if (!myBin.equals(incomigIfx.getDestBankId()))
//			return true;
//		return false;
	}

}
