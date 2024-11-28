package vaulsys.clearing.jobs;

import vaulsys.calendar.DateTime;
import vaulsys.entity.impl.Institution;
import vaulsys.message.Message;
import vaulsys.message.exception.OriginalMessageNotFound;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.terminal.TerminalClearingMode;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionStatus;
import vaulsys.wfe.ProcessContext;

import java.util.HashMap;
import java.util.Map;

public class BindISOReconcilementRequest extends AbstractISOClearingJob {

	public static final BindISOReconcilementRequest Instance = new BindISOReconcilementRequest();
	private BindISOReconcilementRequest(){}
	
	
    @Override
    public void execute(Message incomingMessage, Transaction refTransaction, ProcessContext processContext)
            throws Exception {
    	ISOMsg protocolMessage = (ISOMsg) incomingMessage.getProtocolMessage();

        Terminal endPointTerminal = TerminalService.findEndpointTerminalForMessageWithoutIFX(incomingMessage, null);

        Institution institution = 
//        	ProcessContext.get().getInstitution(endPointTerminal.getOwnerId()) ;
        	(Institution) endPointTerminal.getOwner();
        
        //KEENU Batch start
        String terminalid = protocolMessage.getString(41);
        String merchantid = protocolMessage.getString(42);

        logger.info("Batch for Terminal [" + terminalid + "] Merchant [" + merchantid + "]");

//        String queryString = "select i.transaction from Ifx as i where "
//                + " i.nettrninfo.terminalid = :termid "
//                + " and i.nettrninfo.OrgIdNumendPointTerminal = :merchid "
//                + " and i.receivedDtLong >= :workingDate "
//                + " order by i.receivedDtLong desc ";
//
//        Map<String, Object> params = new HashMap<String, Object>();
//        params.put("termid", terminalid);
//        params.put("merchid", merchantid);
//		params.put("DONE", TransactionStatus.DONE);
        //params.put("workingDate", recievedWorkingDate.getDateTimeLong());

        //KEENU Batch end
        DateTime recievedWorkingDate = institution.getCurrentWorkingDay().getRecievedDate();
	recievedWorkingDate = DateTime.toDateTime(recievedWorkingDate.getTime() -  DateTime.ONE_MINUTE_MILLIS);

        String queryString = "select i.transaction from Ifx as i where "
				+ " i.ifxType = :ifxType "
				+ " and i.endPointTerminal = :endPointTerminal " 
//				+ " and i.transaction.status = :DONE "
				+ " and i.receivedDtLong >= :workingDate "
				+ " order by i.receivedDtLong desc ";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ifxType", IfxType.CUTOVER_RQ);
		params.put("endPointTerminal", endPointTerminal);
//		params.put("DONE", TransactionStatus.DONE);
		params.put("workingDate", recievedWorkingDate.getDateTimeLong());

        
        Transaction refTrnx = (Transaction) GeneralDao.Instance.findUniqueObject(queryString, params);
        if (refTrnx != null) {
            logger.info("refrence transaction of received "+protocolMessage.getMTI()+" is transaction with id:	" + refTrnx.getId());
            refTransaction.setReferenceTransaction(refTrnx);
            refTransaction.setFirstTransaction(refTrnx);
//            getSchedulerService().removeRepeatOrReversalJobInfo(refTrnx.getId());
        } else {
        	logger.error("Refrence Transaction Message with MTI: " + protocolMessage.getMTI());
            throw new OriginalMessageNotFound("Refrence Transaction Message with MTI: " + protocolMessage.getMTI(), false);
        }
    }

	@Override
	protected TerminalClearingMode getClearingMode() {
		return null;
	}
}
