package vaulsys.clearing.jobs;

import vaulsys.entity.FinancialEntityService;
import vaulsys.message.Message;
import vaulsys.message.exception.OriginalMessageNotFound;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.terminal.TerminalClearingMode;
import vaulsys.transaction.Transaction;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BindISORecocilementResponse extends AbstractISOClearingJob {

	public static final BindISORecocilementResponse Instance = new BindISORecocilementResponse();
	public BindISORecocilementResponse(){}
	
    @Override
    public void execute(Message incomingMessage, Transaction refTransaction, ProcessContext processContext)
            throws Exception {
        ISOMsg protocolMessage = (ISOMsg) incomingMessage.getProtocolMessage();
        refTransaction.setDebugTag("ISOReconcilement_"+protocolMessage.getMTI());
//        refTransaction.setAuthorized(true);

        String trnSeqCntr = ISOUtil.zeroUnPad(protocolMessage.getString(11));
        Long bankId = Util.longValueOf( protocolMessage.getString(99));
        Long fwdBankId = FinancialEntityService.getInstitutionByCode(incomingMessage.getChannel().getInstitutionId()).getBin();

        String queryString = "select i.transaction from Ifx as i"
    		+ " where"
            + " i.networkTrnInfo.Src_TrnSeqCntr = :trnSeqCntr"
            + " and i.networkTrnInfo.BankId = :bankId"
            + " and i.networkTrnInfo.FwdBankId = :fwdbankId"
            + " and i.ifxDirection = :direction"
            + " and i.ifxType in (:ListIfxType)";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("fwdbankId", fwdBankId);
        params.put("trnSeqCntr", trnSeqCntr);
        params.put("bankId", bankId);
        params.put("direction", IfxDirection.OUTGOING);
        params.put("ListIfxType", new ArrayList<IfxType>(){{add(IfxType.CUTOVER_RQ);
															add(IfxType.CUTOVER_REPEAT_RQ);
															add(IfxType.CARD_ISSUER_REC_RQ);
															add(IfxType.CARD_ISSUER_REC_REPEAT_RQ);
															add(IfxType.ACQUIRER_REC_REPEAT_RQ);
															add(IfxType.ACQUIRER_REC_RQ);
															}});

        Transaction refTrnx = (Transaction) GeneralDao.Instance.findUniqueObject(queryString, params);
        if (refTrnx != null){
//            refTransaction.setReferenceTransaction(refTrnx);
            //TODO: change refTransaction
            refTransaction.setFirstTransaction(refTrnx);
            refTransaction.setReferenceTransaction(refTrnx.getReferenceTransaction());
        } else {
            logger.error("No transaction Found with (trnSeqCntr = " + trnSeqCntr + ", BankId = " + bankId + ", FwdBankId = " + fwdBankId + ")");
            throw new OriginalMessageNotFound("No Request Message Found For Response Message with MTI: " + protocolMessage.getMTI(), false);
        }
    }

	@Override
	protected TerminalClearingMode getClearingMode() {
		// TODO Auto-generated method stub
		return null;
	}
}
