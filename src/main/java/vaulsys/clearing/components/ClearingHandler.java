package vaulsys.clearing.components;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.clearing.base.ClearingAction;
import vaulsys.clearing.jobs.ClearingJob;
import vaulsys.message.Message;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.apacs70.base.RqBaseMsg;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.exception.exception.CantAddNecessaryDataToIfxException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.Transaction;
import vaulsys.util.ProtocolToXmlUtils;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class ClearingHandler extends BaseHandler {
	private static final Logger logger = Logger.getLogger(ClearingHandler.class);

	public static final ClearingHandler Instance = new ClearingHandler();

	private ClearingHandler(){
	}

    public void execute(ProcessContext processContext) throws Exception {
        try {
            Message inputMessage = processContext.getTransaction().getInputMessage();
            ProtocolMessage protocolMessage = inputMessage.getProtocolMessage();

            int mti = 0;
            String strMTI = "";
            if(protocolMessage instanceof RqBaseMsg) {
            	// no need to set mti, as the only ClearingAction is ACQUIRER_RECONCILEMNET_RESPONSE
            }
            else if(protocolMessage instanceof ISOMsg) {
                //TODO Noroozi: these two lines are ISO-based! we need to get "message type" from protocolMessage
                ISOMsg isoMsg = (ISOMsg) inputMessage.getProtocolMessage();
                strMTI = isoMsg.getMTI();
                mti = Integer.parseInt(strMTI);
            }
            else
            	throw new RuntimeException("Protocol not supported: " + protocolMessage.getClass().getSimpleName());

            ProtocolToXmlUtils.setXMLdata(inputMessage);
            GeneralDao.Instance.saveOrUpdate(inputMessage.getMsgXml());

            ClearingAction action = inputMessage.getChannel().getClearingMapper().findAction(mti);
            ClearingJob job = inputMessage.getChannel().getClearingActionJobs().findClearingJob(action);
            Transaction transaction = inputMessage.getTransaction();
            transaction.setFirstTransaction(transaction);

            if (job.preJob() != null)
            	job.preJob().execute(inputMessage, transaction, processContext);

            job.execute(inputMessage, transaction, processContext);

            if (job.postJob() != null) {
                job.postJob().execute(inputMessage, transaction, processContext);
            }
//                Message outputMessage = transaction.getOutputMessage();
//                Set<Message> messages = transaction.getMessages();
//                messages.remove(inputMessage);
//                messages.remove(outputMessage);
//
//                for (Message pm: messages)
////            		processContext.addPendingRequests(pm.getId());
//                	if (pm.isOutgoingMessage())
//                		processContext.addPendingResponses(pm);
//
//                transaction.removeAllOutputMessages();
//                transaction.setInputMessage(inputMessage);
//                transaction.addOutputMessage(outputMessage);
//            }

            if (transaction.getLifeCycle() == null){
            	LifeCycle lifeCycle = new LifeCycle();
//            	if (transaction.getOutputMessage().getIfx()!= null && ShetabFinalMessageType.isResponseMessage(transaction.getOutputMessage().getIfx().getIfxType()))
            	if (transaction.getOutgoingIfx()!= null && ISOFinalMessageType.isResponseMessage(transaction.getOutgoingIfx().getIfxType()))
            			lifeCycle.setIsComplete(true);

            	GeneralDao.Instance.saveOrUpdate(lifeCycle);
            	transaction.setLifeCycle(lifeCycle);
            	GeneralDao.Instance.saveOrUpdate(transaction);
            }

            if (/*action.equals(ClearingAction.ACQUIRER_FINALIZE_RECONCILEMNET)
                    || */action.equals(ClearingAction.ISSUER_FINALIZE_RECONCILEMNET)){
            	if (!transaction.getInputMessage().getChannel().getMasterDependant())
            		leaveToEndState(processContext);
            }

        } catch (Exception e) {
    		if(e instanceof CantAddNecessaryDataToIfxException) {
				logger.warn(e.getClass().getSimpleName() + ":" + e.getMessage(), e);
    		}else{
    			logger.error("rethrowing exception " + e, e);
    		}
            throw e;
        }
    }
}
