package vaulsys.protocols.apacs70.base;

import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

public class RqAuxAuthAndFundTransfer extends RqAuxBase {
	public String destinationCardNumber;
	
	@Override
	public void unpack(ApacsByteArrayReader in) {
		destinationCardNumber = in.getStringMaxToSep("destinationCardNumber", 20, ApacsConstants.GS);

		super.unpack(in);
	}

	@Override
	public void toIfx(Ifx ifx) {
		super.toIfx(ifx);
		IfxType type = ifx.getIfxType();

		if (IfxType.TRANSFER_CHECK_ACCOUNT_RQ.equals(type) || IfxType.TRANSFER_RQ.equals(type)) {
			if (Util.hasText(destinationCardNumber) && Util.isValidAppPan(destinationCardNumber)) {
				try {
					if (IfxType.TRANSFER_CHECK_ACCOUNT_RQ.equals(type)) {
						ifx.setAppPAN(destinationCardNumber);
						ifx.setDestBankId(destinationCardNumber.substring(0, 6));
						if (ifx.getSecondAppPan() != null)
							ifx.setRecvBankId(ifx.getSecondAppPan().substring(0, 6));
					} else if (IfxType.TRANSFER_RQ.equals(type)) {
						ifx.setSecondAppPan(destinationCardNumber);
						ifx.setRecvBankId(destinationCardNumber.substring(0, 6));
					}
				} catch (Exception e) {
					logger.warn("Setting destination card number info: ", e);
					ifx.setStatusDesc("Invalid destination card number: " + destinationCardNumber);
					ifx.setSeverity(Severity.WARN);
				}
			} else {
				ifx.setStatusDesc("Invalid destination card number: " + destinationCardNumber);
				ifx.setSeverity(Severity.ERROR);
			}
		} else 		if (IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ.equals(type) || IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(type)) {
			if (Util.hasText(destinationCardNumber) && (Util.isValidAppPan(destinationCardNumber)|| destinationCardNumber.matches("\\d+.\\d+.\\d+.\\d+")) ) {
				String myBin = ""+ProcessContext.get().getMyInstitution().getBin();
				try {
					if (IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ.equals(type)) {
						ifx.setAppPAN(destinationCardNumber);
						ifx.setDestBankId(/*Long.parseLong(destinationCardNumber.substring(0, 6))*/myBin);
						if (ifx.getSecondAppPan() != null)
							ifx.setRecvBankId(ifx.getSecondAppPan().substring(0, 6));
					} else if (IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(type)) {
						ifx.setSecondAppPan(destinationCardNumber);
						ifx.setRecvBankId(/*Long.parseLong(destinationCardNumber.substring(0, 6))*/myBin);
					}
				} catch (Exception e) {
					logger.warn("Setting destination card number info: ", e);
					ifx.setStatusDesc("Invalid destination card number: " + destinationCardNumber);
					ifx.setSeverity(Severity.WARN);
				}
			} else {
				ifx.setStatusDesc("Invalid destination card number: " + destinationCardNumber);
				ifx.setSeverity(Severity.ERROR);
			}
		}
	}

	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\nDestination Card: ").append(destinationCardNumber);
	}
}
