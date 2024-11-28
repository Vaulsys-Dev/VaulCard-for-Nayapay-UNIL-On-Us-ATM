package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.authorization.exception.MandatoryFieldException;
import vaulsys.authorization.exception.NotRoundAmountException;
import vaulsys.authorization.exception.SufficientAmountException;
import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.fee.FeeService;
import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.atm.ATMRequest;
import vaulsys.terminal.atm.ATMResponse;
import vaulsys.terminal.atm.FunctionCommandResponse;
import vaulsys.terminal.atm.OARResponse;
import vaulsys.terminal.atm.device.CassetteA;
import vaulsys.terminal.atm.device.CassetteB;
import vaulsys.terminal.atm.device.CassetteC;
import vaulsys.terminal.atm.device.CassetteD;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.ConfigUtil;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class WithdrawalProcess extends MessageProcessor {

	private Logger logger = Logger.getLogger(this.getClass());
	
	
	public static final WithdrawalProcess Instance = new WithdrawalProcess();
	private WithdrawalProcess(){};
	
	
	@Override
	public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext) throws Exception {
		
		Message outgoingMessage = (GeneralMessageProcessor.Instance).createOutgoingMessage(transaction, incomingMessage, channel, processContext);
		
		//Mirkamali(Task179): Currency ATM
		Ifx incomingIfx = incomingMessage.getIfx();
			Ifx outgoingIfx = null;
			if (ISOFinalMessageType.isRequestMessage(incomingIfx.getIfxType())) {
				
				outgoingIfx = outgoingMessage.getIfx();
				outgoingIfx.setIfxType(IfxType.BILL_PMT_RQ);
				outgoingIfx.setTrnType(TrnType.BILLPAYMENT);
				outgoingIfx.setBillOrgType(OrganizationType.UNDEFINED);
				outgoingIfx.setBillID("0");
				outgoingIfx.setBillPaymentID("0");
				transaction.setOutgoingIfx(outgoingIfx);
				
			}else {
				outgoingIfx = outgoingMessage.getIfx();
				outgoingIfx.setIfxType(IfxType.WITHDRAWAL_CUR_RS);
				outgoingIfx.setTrnType(TrnType.WITHDRAWAL_CUR);
				transaction.setDebugTag(outgoingIfx.getIfxType().toString());
			}
		return outgoingMessage;
	}

	@Override
	public Message postProcess(Transaction transaction, Message incomingMessage, Message outgoingMessage, Channel channel)
			throws Exception {
		
        Ifx incomingIfx = incomingMessage.getIfx();
        Ifx outgoingIfx = outgoingMessage.getIfx();
        
        
		if(ISOFinalMessageType.isWithdrawalCurMessage(incomingIfx.getIfxType()) || ISOFinalMessageType.isWithdrawalCurMessage(outgoingIfx.getIfxType())) {
	    	  
        	if(incomingIfx.getTransaction().getReferenceTransaction() != null && incomingIfx.getTransaction().getReferenceTransaction().getIncomingIfx() != null) {
    		  
        		incomingIfx.setReal_Amt(incomingIfx.getTransaction().getReferenceTransaction().getIncomingIfx().getReal_Amt());
        		incomingIfx.setAuth_Amt(incomingIfx.getTransaction().getReferenceTransaction().getIncomingIfx().getAuth_Amt());
        		incomingIfx.setAuth_CurRate(incomingIfx.getTransaction().getReferenceTransaction().getIncomingIfx().getAuth_CurRate());
        		incomingIfx.setAuth_Currency(incomingIfx.getTransaction().getReferenceTransaction().getIncomingIfx().getAuth_Currency());
        		
        		outgoingIfx.setReal_Amt(incomingIfx.getTransaction().getReferenceTransaction().getIncomingIfx().getReal_Amt());
        		outgoingIfx.setAuth_Amt(incomingIfx.getTransaction().getReferenceTransaction().getIncomingIfx().getAuth_Amt());
        		outgoingIfx.setAuth_CurRate(incomingIfx.getTransaction().getReferenceTransaction().getIncomingIfx().getAuth_CurRate());
        		outgoingIfx.setAuth_Currency(incomingIfx.getTransaction().getReferenceTransaction().getIncomingIfx().getAuth_Currency());
        		
        		
        		if(ISOFinalMessageType.isWithdrawalCurMessage(outgoingIfx.getIfxType())){
        			incomingIfx.setTotalFeeAmt(incomingIfx.getTransaction().getReferenceTransaction().getIncomingIfx().getTotalFeeAmt());
        			outgoingIfx.setTotalFeeAmt(incomingIfx.getTransaction().getReferenceTransaction().getIncomingIfx().getTotalFeeAmt());
        			FeeService.updateFee(outgoingIfx.getTransaction(), outgoingIfx.getTransaction().getReferenceTransaction());
        		}
			} 
        }
		
		GeneralDao.Instance.saveOrUpdate(incomingIfx);
		GeneralDao.Instance.saveOrUpdate(outgoingIfx);
		
		return outgoingMessage;
	}

	@Override
	public void messageValidation(Ifx ifx, Message incomingMessage) throws Exception {
		ATMTerminal atm = (ATMTerminal) TerminalService.getMatchingTerminal(ifx);
		String opkey = ifx.getOpkey();
		if ("".equals(opkey))
			return;
		
		//Mirkamali(Task179): Currency ATM
		if(IfxType.PREPARE_WITHDRAWAL.equals(ifx.getIfxType()))
			return;

		if (ifx.getAuth_Amt() == null || ifx.getAuth_Amt() <= 0)
			throw new MandatoryFieldException("Failed: Bad Amount Withdrawal");
		
//		ATMRequest atmRequest = ATMTerminalService.findATMRequest(atm, opkey);
		ATMRequest atmRequest = ProcessContext.get().getATMRequest(atm.getOwnOrParentConfigurationId(), opkey);

		ATMResponse atmRs1 = ATMTerminalService.findResponse(atmRequest, Integer.parseInt(ISOResponseCodes.APPROVED));
		
//		if (!(atmRs1 instanceof FunctionCommandResponse)) 
//			return;

		boolean needToCountAmount = false;
		int notes1 = 0;
		int notes2 = 0;
		int notes3 = 0;
		int notes4 = 0;
		
		if (atmRs1 instanceof OARResponse) {
			needToCountAmount = true;
			
		} else if (atmRs1 instanceof FunctionCommandResponse) {
			FunctionCommandResponse atmRs = (FunctionCommandResponse) atmRs1;
			if (atmRs.getDispense() == null)
				needToCountAmount = true;
			else if (atmRs.getDispense().getCassette1() == null || atmRs.getDispense().getCassette2() == null
					|| atmRs.getDispense().getCassette3() == null || atmRs.getDispense().getCassette4() == null)
				needToCountAmount = true;
			
			if (!needToCountAmount) {
				notes1 = Integer.parseInt(atmRs.getDispense().getCassette1());
				notes2 = Integer.parseInt(atmRs.getDispense().getCassette2());
				notes3 = Integer.parseInt(atmRs.getDispense().getCassette3());
				notes4 = Integer.parseInt(atmRs.getDispense().getCassette4());
			}
		}

		if (needToCountAmount) {
			long amount;
			amount = ifx.getAuth_Amt();
			
			//Mirkamali(Task179): Currency ATM
			if(IfxType.WITHDRAWAL_CUR_RQ.equals(ifx.getIfxType()) || IfxType.WITHDRAWAL_CUR_RS.equals(ifx.getIfxType()))
				amount = ifx.getReal_Amt();


			//System.out.println("WithdrawalProcess:: atm [" + atm.getId() + "]"); //Raza TEMP
			//System.out.println("WithdrawalProcess:: amount [" + amount + "]"); //Raza TEMP

			int[] notes = ATMTerminalService.dynamicDispenseNotes(atm, amount);
			if (notes == null)
				throw new NotRoundAmountException();
			notes1 = notes[0];
			notes2 = notes[1];
			notes3 = notes[2];
			notes4 = notes[3];

			if (notes1 == 0 && notes2 == 0 && notes3 == 0 && notes4 == 0)
				throw new NotRoundAmountException();
		}
		
		if (ConfigUtil.getBoolean(ConfigUtil.PARTIAL_DISPENSE_SUPPORT)) {

			if (!ifx.getDestBankId().equals(ProcessContext.get().getMyInstitution().getBin()) &&
					!ProcessContext.get().isPeerInstitution(Util.longValueOf(ifx.getDestBankId()))){
				/*** this block for the banks that not supported partial reverse! ***/
				int totalNote = notes1 + notes2 + notes3 + notes4;
				if(totalNote > ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId()).getMaxDespensingNotes())
					throw new NotRoundAmountException();
			}
			
		} else {
			int totalNote = notes1 + notes2 + notes3 + notes4;
			if(totalNote > ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId()).getMaxDespensingNotes())
				throw new NotRoundAmountException();
		}

		CassetteA cassetteA = atm.getDevice(CassetteA.class);
		CassetteB cassetteB = atm.getDevice(CassetteB.class);
		CassetteC cassetteC = atm.getDevice(CassetteC.class);
		CassetteD cassetteD = atm.getDevice(CassetteD.class);
		
		if (notes1 > cassetteA.getNotes() && cassetteA.getNotes() >= 0 && notes1 != 0)
			throw new SufficientAmountException();
		if( notes2 > cassetteB.getNotes() && cassetteB.getNotes() >= 0 && notes2 != 0)
			throw new SufficientAmountException();
		if(notes3 > cassetteC.getNotes() && cassetteC.getNotes() >= 0 && notes3 != 0)
			throw new SufficientAmountException();
		if(notes4 > cassetteD.getNotes() && cassetteD.getNotes() >= 0 && notes4 != 0) {
			throw new SufficientAmountException();
		}
	}
	
}
