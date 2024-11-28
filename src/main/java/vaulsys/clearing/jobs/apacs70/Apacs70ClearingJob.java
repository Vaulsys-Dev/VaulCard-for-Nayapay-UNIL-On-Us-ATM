package vaulsys.clearing.jobs.apacs70;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.jobs.AbstractClearingJob;
import vaulsys.clearing.reconcile.ICutover;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.network.channel.endpoint.EndPointType;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.apacs70.base.Apacs70Utils;
import vaulsys.protocols.apacs70.base.RqBaseMsg;
import vaulsys.protocols.apacs70.base.RsReconMsg;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.ProtocolToXmlUtils;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class Apacs70ClearingJob extends AbstractClearingJob {
	private static Logger logger = Logger.getLogger(Apacs70ClearingJob.class);

	public static final Apacs70ClearingJob Instance = new Apacs70ClearingJob();

	private Apacs70ClearingJob(){}

	@Override
	public void execute(Message incomingMessage, Transaction refTransaction, ProcessContext processContext) throws Exception {
		refTransaction.setDebugTag("POSReconcilement");
		RqBaseMsg rqMsg = (RqBaseMsg) incomingMessage.getProtocolMessage();

		EndPointType endPointType = EndPointType.POS_TERMINAL/*incomingMessage.getChannel().getEndPointType()*/;

		Terminal terminal = TerminalService.findTerminal(endPointType.getClassType(), rqMsg.terminalIdentity);
		Ifx inIfx = new Ifx();
		rqMsg.toIfx(inIfx);
        if(terminal == null) {
        	inIfx.setSeverity(Severity.ERROR);
        	inIfx.setStatusDesc("Invalid POS id: " + rqMsg.terminalIdentity);
        	logger.error("Apacs Reconcilation Message: Invalid POS id: " + rqMsg.terminalIdentity);
        }
        else {
    		incomingMessage.setNeedToBeSent(false);
    		inIfx.setTerminalType(EndPointType.getTerminalType(endPointType));
		inIfx.setOrigDt(DateTime.now());
		inIfx.setEndPointTerminal(terminal);
    		Apacs70Utils.checkValidityOfLastTransactionStatus(terminal, inIfx);
        }
		inIfx.setReceivedDt(incomingMessage.getStartDateTime());
		incomingMessage.setIfx(inIfx);
		incomingMessage.setEndPointTerminal(terminal);

		GeneralDao.Instance.saveOrUpdate(incomingMessage.getIfx());
		GeneralDao.Instance.saveOrUpdate(incomingMessage);
		GeneralDao.Instance.saveOrUpdate(incomingMessage.getMsgXml());

		Ifx outIfx = createOutgoingIfx(inIfx, terminal);
		RsReconMsg rsMsg = createOutgoingMsg(outIfx, terminal);

		Message outMessage = new Message(MessageType.OUTGOING);
        outMessage.setTransaction(refTransaction);
        InputChannel inputChannel = (InputChannel) incomingMessage.getChannel();
        outMessage.setChannel(inputChannel.getOriginatorChannel());
        outMessage.setProtocolMessage(rsMsg);
        outMessage.setEndPointTerminal(terminal);        
        ProtocolToXmlUtils.setXMLdata(outMessage);
        outMessage.setIfx(outIfx);
		outMessage.setRequest(false);
		outMessage.setNeedResponse(false);
		outMessage.setNeedToBeInstantlyReversed(false);
		outMessage.setNeedToBeSent(true);
		GeneralDao.Instance.saveOrUpdate(outMessage.getIfx());
		GeneralDao.Instance.saveOrUpdate(outMessage);
		GeneralDao.Instance.saveOrUpdate(outMessage.getMsgXml());
		refTransaction.addOutputMessage(outMessage);
	}

	private RsReconMsg createOutgoingMsg(Ifx outIfx, Terminal terminal) throws Exception {
		RsReconMsg rsMsg = new RsReconMsg();
		rsMsg.fromIfx(outIfx);
		rsMsg.merchantAccountNumber = " ";
		//rsMsg.merchantAccountNumber = terminal.getOwner().getOwnOrParentAccount().getAccountNumber();
		if(terminal != null) {
			
			if (TerminalService.hasRequestBasedClearingProfile(terminal)) {
				rsMsg.merchantBalance = 0L;
				
				/******** Request Based Settlement *********/
				try {
					TerminalService.createRequestBasedSettlementThread(terminal);
				} catch(Exception e) {
					logger.error("Error in creating request based settlement of POS ...", e);
				}
				/*****************/
				
			} else {
				rsMsg.merchantBalance = TerminalService.getSumOfUnsettledFlags(terminal);
				
			}
		}

		return rsMsg;
	}

	private Ifx createOutgoingIfx(Ifx inIfx, Terminal terminal) {
		Ifx ifx = inIfx.clone();
		ifx.setIfxDirection(IfxDirection.OUTGOING);
        ifx.setIfxType(IfxType.ACQUIRER_REC_RS);
        if(terminal != null)
        	ifx.setRsCode(ISOResponseCodes.APPROVED);
        else
        	ifx.setRsCode(ISOResponseCodes.WALLET_IN_PROVISIONAL_STATE);
		return ifx;
	}
	
	@Override
	protected ICutover getCutover() {
		throw new RuntimeException("Not Implemented");
	}
}
