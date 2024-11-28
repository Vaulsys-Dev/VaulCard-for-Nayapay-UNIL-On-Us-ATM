package vaulsys.protocols.apacs70.base;

import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.ifx.imp.Ifx;

import org.apache.log4j.Logger;

public abstract class RqAuxBase {
	protected static final Logger logger = Logger.getLogger(RqAuxBase.class);

	public static final String RECORD_TYPE = "Z6";
	
	public Integer lastSuccessfulSequenceNumber;
	public String posSerialNum;

	public void unpack(ApacsByteArrayReader in) {
		lastSuccessfulSequenceNumber = in.getIntegerFixedToSep("lastSuccessfulSequenceNumber", 4, ApacsConstants.GS);
		posSerialNum = in.getStringMax("posSerialNum", 20);
	}
	
	public void toIfx(Ifx ifx) {
		ifx.setLast_TrnSeqCntr(lastSuccessfulSequenceNumber.toString());
		ifx.setSerialno(posSerialNum);
	}
	
	public static RqAuxBase createAux(int type) {
		RqAuxBase aux = null;
		switch(type) {
			case 1: // E-Voucher
				aux = new RqAuxEVoucher();
				break;
			
			case 2: // Sale, Balance Enquiry, Logon
				aux = new RqAuxSaleBalanceLogon();
				break;

			case 3: // Bill Payment
				aux = new RqAuxBillPayment();
				break;

			case 4: // Refund
				aux = new RqAuxRefund();
				break;

			case 5: // Reset Password
				aux = new RqAuxResetPassword();
				break;

			case 6:
				aux = new RqAuxRefPayment();
				break;
				
			case 10: //ghasedak
				aux = new RqAuxGhasedak();
				break;
				
			case 20: // Pin Change
				aux = new RqAuxPinChange();
				break;

			case 21: // Auth & Fund Transfer
				aux = new RqAuxAuthAndFundTransfer();
				break;

			//case 49: // TMS
				//aux = new RqAuxVersions();
				//break;

//			case 50: // Mizan
//				aux = new RqAuxMizan();
//				break;
				
			case 7: //ThirdParty Purchase
				aux = new RqAuxThirdPartyPurchase();
				break;

			default:
				throw new IllegalArgumentException("Wrong subtype for auxiliary data: " + type);
		}
		return aux;
	}

	protected abstract void auxString(StringBuilder builder);

	@Override
	public final String toString() {
		StringBuilder builder = new StringBuilder();
		auxString(builder);
		if(lastSuccessfulSequenceNumber != null)
			builder.append("\nLast Successful Sequence Number: ").append(lastSuccessfulSequenceNumber);
		if(posSerialNum != null)
			builder.append("\nPOS Serial no: ").append(posSerialNum);
		return builder.toString();
	}
}
