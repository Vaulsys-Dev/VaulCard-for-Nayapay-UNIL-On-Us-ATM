package vaulsys.terminal.atm.action.consumer;

import vaulsys.eft.util.MsgProcessor;
import vaulsys.message.Message;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.ATMSpecificData;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedCashHandlerStatusMsg;
import vaulsys.protocols.ndc.base.config.TransactionStatusType;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCashHandler;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.Util;

public class ConsumerHoldingState extends ConsumerState {
	public static final ConsumerHoldingState Instance = new ConsumerHoldingState();

	private ConsumerHoldingState(){}
	
    @Override
    protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
    	NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
        if (ndcMsg instanceof NDCUnsolicitedCashHandlerStatusMsg) {
        	NDCUnsolicitedCashHandlerStatusMsg faultMsg = (NDCUnsolicitedCashHandlerStatusMsg) ndcMsg;
            NDCCashHandler cashHandler = faultMsg.statusInformation;
        	inputMessage.getIfx().setIfxType(IfxType.CASH_HANDLER);
        	inputMessage.getIfx().setTransactionStatus(cashHandler.transactionStatus);
        	
//        	if (TransactionStatusType.SOME_NOTES_RETRACTED.equals(cashHandler.transactionStatus)) {
 //       		return ConsumerReversalState.Instance;
        		
 //       	}
        	
            return ConsumerCashHandlerState.Instance;
        }

        return ConsumerEndState.Instance;
    }

    @Override
    protected Message process(Message inputMessage, ATMTerminal atm) {
    	NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
    	
    	//TODO AS soon as possible
    	//FIXME lastTransaction = atm.getLastRealTransaction() instead of getLastTransaction() 
		Transaction lastTransaction = atm.getLastTransaction();
		if (lastTransaction!= null) {
			Ifx ifx = lastTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/;
			ATMTerminalService.updateTerminalStatus(ifx, atm);
		}
		return null;
    }

	private boolean isFinishedTransaction(ATMSpecificData atmSpecificData) {
		if (atmSpecificData == null)
			return true;
		
		if (!Util.hasText(atmSpecificData.getCurrentDispense()))
			return true;
		
		Integer cassette1 = Integer.parseInt(atmSpecificData.getCurrentDispense().substring(0, 2));
		Integer cassette2 = Integer.parseInt(atmSpecificData.getCurrentDispense().substring(2, 4));
		Integer cassette3 = Integer.parseInt(atmSpecificData.getCurrentDispense().substring(4, 6));
		Integer cassette4 = Integer.parseInt(atmSpecificData.getCurrentDispense().substring(6, 8));
		
		if (atmSpecificData.getDesiredDispenseCaset1().equals(atmSpecificData.getActualDispenseCaset1()+ cassette1)
			&& atmSpecificData.getDesiredDispenseCaset2().equals(atmSpecificData.getActualDispenseCaset2()+ cassette2)
			&& atmSpecificData.getDesiredDispenseCaset3().equals(atmSpecificData.getActualDispenseCaset3()+ cassette3)
			&& atmSpecificData.getDesiredDispenseCaset4().equals(atmSpecificData.getActualDispenseCaset4()+ cassette4)
			)
			return true;
		
		return false;
	}

	@Override
	protected Ifx createOutgoingIfx(Message message, ATMTerminal atm) {
		Ifx ifx = null;
		try {
			ifx = MsgProcessor.processor(message.getIfx());
		} catch (CloneNotSupportedException e) {
			return ifx;
		}
		ifx.setSrc_TrnSeqCntr(Util.generateTrnSeqCntr(ifx.getSrc_TrnSeqCntr().length()));
		ifx.setMy_TrnSeqCntr(ifx.getSrc_TrnSeqCntr());
		
		ifx.setAtmSpecificData(message.getIfx().getAtmSpecificData().copy());
		
		return ifx;
	}
	
	@Override
	protected void setDebugTag(Transaction t) {
		Ifx ifx = t.getOutgoingIfx()/*getOutputMessage().getIfx()*/;
		t.setDebugTag(ifx.getIfxType()+"_"+ ifx.getCurrentStep());
	}
}
