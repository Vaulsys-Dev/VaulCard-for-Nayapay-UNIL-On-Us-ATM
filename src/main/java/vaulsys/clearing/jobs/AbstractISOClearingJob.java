package vaulsys.clearing.jobs;

import vaulsys.clearing.consts.ClearingMode;
import vaulsys.clearing.reconcile.ICutover;
import vaulsys.clearing.reconcile.IReconcilement;
import vaulsys.clearing.reconcile.ISOCutover;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.Institution;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.terminal.TerminalClearingMode;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.ProtocolToXmlUtils;
import vaulsys.wfe.ProcessContext;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

public abstract class AbstractISOClearingJob extends AbstractClearingJob {

    transient Logger logger = Logger.getLogger(this.getClass());

    public ICutover getCutover() {
        return ISOCutover.Instance;
    }

    public IReconcilement getReconcilement(){
        return this.reconcilement;
    }

    protected Message createOutputMessage(ISOMsg isoMsg, Message incomingMessage, Transaction refTransaction, Terminal endPointTerminal) {
    	Message outgoingMessage = new Message(MessageType.OUTGOING);
//    	outgoingMessage.setType(MessageType.OUTGOING);
        outgoingMessage.setTransaction(/*incomingMessage.getTransaction()*/ refTransaction);
        InputChannel inputChannel = (InputChannel) incomingMessage.getChannel();
        outgoingMessage.setChannel(inputChannel.getOriginatorChannel());
        outgoingMessage.setProtocolMessage(isoMsg);
        outgoingMessage.setEndPointTerminal(endPointTerminal);
        
        ProtocolToXmlUtils.setXMLdata(outgoingMessage);
        GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
        return outgoingMessage;
    }

    protected void generateReportFile(Integer responseCode,
                                      List<Transaction> checkedTrxList, Institution institution,
                                      Date stlDate, TerminalClearingMode mode) {

        try {
            if (responseCode.equals(1)) {
                //todo: (mojtaba) is this right?
                ClearingMode mode1 = null;
                if (mode.equals(TerminalClearingMode.ACQUIER))
                    mode1 = ClearingMode.Acquier;
                else if (mode.equals(TerminalClearingMode.ISSUER))
                    mode1 = ClearingMode.Issuer;
                
                //TODO generate institution reports in correctly place
//                getReportGenerator().generateReportsForInstitution(institution.getCode(), getFinancialEntityService().getIssuerSwitchTerminal(institution),
//                        mode1, checkedTrxList, stlDate, true, true, true, true);
            }

        } catch (Exception e) {
        	logger.error("Encounter with an Exception ("+e.getClass().getSimpleName()+": "+ e.getMessage()+")", e);
//            e.printStackTrace();
        }
    }
    
    
    protected Terminal findAppropriateTerminal(Institution institution) {
        switch (getClearingMode()) {
            case ACQUIER:
            	return ProcessContext.get().getIssuerSwitchTerminal(institution);
//                return FinancialEntityService.getIssuerSwitchTerminal(institution);
            case ISSUER:
            	return ProcessContext.get().getAcquireSwitchTerminal(institution);
//                return FinancialEntityService.getAcquireSwitchTerminal(institution);
            case TERMINAL:
                break;
        }
        return null;
    }
    
    protected abstract TerminalClearingMode getClearingMode();
}
