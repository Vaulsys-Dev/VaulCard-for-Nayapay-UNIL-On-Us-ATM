package vaulsys.eft.base.terminalTypeProcessor;

import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.network.channel.endpoint.EndPointType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class TerminalTypeProcessMap {

	private static Logger logger = Logger.getLogger(TerminalTypeProcessMap.class);

	public static TerminalTypeProcessor getAuthorizationProcessor(Ifx ifx, TerminalType endPointTerminalType) {

		if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())
			&& TerminalType.ATM.equals(ifx.getTerminalType())
			&& TerminalType.ATM.equals(endPointTerminalType)) {
			return ATMProcessor.Instance;

		} else if (ISOFinalMessageType.isResponseMessage(ifx.getIfxType())
				&& TerminalType.ATM.equals(ifx.getTerminalType())) {
			return ATMProcessor.Instance;

		} else if (TerminalType.ATM.equals(ifx.getTerminalType())) {
			return ATMProcessor.Instance;

		} else if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())
				&& TerminalType.INTERNET.equals(ifx.getTerminalType())) {
			return EpayProcessor.Instance;
			
		} else if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())
					&& TerminalType.POS.equals(ifx.getTerminalType())
					&& TerminalType.POS.equals(endPointTerminalType)) {
				return POSProcessor.Instance;
				
		}  else if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())
				&& TerminalType.KIOSK_CARD_PRESENT.equals(ifx.getTerminalType())
				&& TerminalType.KIOSK_CARD_PRESENT.equals(endPointTerminalType)) {
			return KioskCardPresentProcessor.Instance;
	}


		return GeneralTerminalTypeProcessor.Instance;
	}

	public static TerminalTypeProcessor getMessageBinderProcessor(Ifx ifx) {
		if (ISOFinalMessageType.isResponseMessage(ifx.getIfxType()))
			return GeneralTerminalTypeProcessor.Instance;

//		if(GlobalContext.getInstance().getMyInstitution().getBin().equals(ifx.getBankId()) &&
		if(ProcessContext.get().getMyInstitution().getBin().equals(ifx.getBankId()) &&
//				FinancialEntityRole.MY_SELF.equals(GlobalContext.getInstance().getMyInstitution().getRole())) {
				FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole())) {
			
			if(ifx.getEndPointTerminal() != null && !TerminalType.UNKNOWN.equals(ifx.getEndPointTerminal().getTerminalType())){
				GlobalContext.getInstance().addTerminalId_Type(ifx.getTerminalId(), ifx.getEndPointTerminal().getTerminalType());				
			}
			
			if(	ISOFinalMessageType.isRequestMessage(ifx.getIfxType()) ||
				ISOFinalMessageType.isPrepareMessage(ifx.getIfxType()) ||
				ISOFinalMessageType.isPrepareReversalMessage(ifx.getIfxType())){
				
				if (TerminalType.ATM.equals(ifx.getTerminalType()) && 
						TerminalType.ATM.equals(ifx.getEndPointTerminal().getTerminalType()))
					return ATMProcessor.Instance;
				
				if (TerminalType.POS.equals(ifx.getTerminalType()) && 
						TerminalType.POS.equals(ifx.getEndPointTerminal().getTerminalType()))
					return POSProcessor.Instance;
				
				if (TerminalType.KIOSK_CARD_PRESENT.equals(ifx.getTerminalType()) && 
						TerminalType.KIOSK_CARD_PRESENT.equals(ifx.getEndPointTerminal().getTerminalType()))
					return KioskCardPresentProcessor.Instance;
				
				if (TerminalType.PINPAD.equals(ifx.getTerminalType()))
					return PINPADProcessor.Instance;
			}
			
			/*** in opkey undefined exception, must be clear last transaction ***/
			else if (ifx.getIfxType() == null) {
				if (TerminalType.ATM.equals(ifx.getTerminalType()) && TerminalType.ATM.equals(ifx.getEndPointTerminal().getTerminalType()))
					return ATMProcessor.Instance;
			}
		}

		return GeneralTerminalTypeProcessor.Instance;
	}

}
