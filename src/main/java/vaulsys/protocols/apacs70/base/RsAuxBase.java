package vaulsys.protocols.apacs70.base;

import static vaulsys.protocols.apacs70.base.ApacsConstants.GS;
import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.Util;

import java.io.IOException;

import org.apache.log4j.Logger;

public abstract class RsAuxBase {
	protected static final Logger logger = Logger.getLogger(RsAuxBase.class);

	private String subType;
	public  String issuerCode;
	
	public RsAuxBase(String subType) {
		this.subType = subType;
	}

	public void fromIfx(Ifx ifx) {
		issuerCode = Apacs70Utils.issuerCode(ifx);
	}

	public void pack(ApacsByteArrayWriter out) throws IOException{
		out.write("Z6", 2);
		out.write(subType, 2);
		out.write(GS);
		if (Util.hasText(issuerCode))
			out.writePadded(issuerCode, 3, false);
	}
	
	public static RsAuxBase createAux(Ifx ifx) {
		IfxType type = ifx.getIfxType();
		RsAuxBase rsAux = null;
		if(IfxType.PURCHASE_CHARGE_RS.equals(type))
			rsAux = new RsAuxEVoucher();

		else if(IfxType.BILL_PMT_RS.equals(type))
			rsAux = new RsAuxBillPayment();
		
		else if(IfxType.SADERAT_AUTHORIZATION_BILL_PMT_RS.equals(type))
			rsAux = new RsAuxBillPayment();
		
		else if(IfxType.SADERAT_BILL_PMT_RS.equals(type))
			rsAux = new RsAuxBillPayment();

		else if(IfxType.LOG_ON_RS.equals(type))
			rsAux = new RsAuxLogon();

		else if(IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(type) || IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS.equals(type))
			rsAux = new RsAuxTransferFundAuth();

		else if(IfxType.CHANGE_PIN_BLOCK_RS.equals(type) && TrnType.CHANGEINTERNETPINBLOCK.equals(ifx.getTrnType()))
			rsAux = new RsAuxPinChange();

		else if(IfxType.ONLINE_BILLPAYMENT_TRACKING.equals(type) || IfxType.PREPARE_ONLINE_BILLPAYMENT.equals(type))
			rsAux = new RsAuxRefPayment();
		
		else if(IfxType.THIRD_PARTY_PURCHASE_RS.equals(type))
			rsAux = new RsAuxThirdPartyPurchase();
		
		else if(IfxType.BANK_STATEMENT_RS.equals(type))
			rsAux = new RsAuxCardStatement();
		
		//ghasedak
		else if (IfxType.GHASEDAK_RS.equals(type))
			rsAux = new RsAuxGhasedak();
		
		else
			rsAux = new RsAuxSaleBalanceRefundResetTransfer();

		return rsAux;
	}

	protected abstract void auxString(StringBuilder builder);

	@Override
	public final String toString() {
		StringBuilder builder = new StringBuilder();
		if(issuerCode != null)
			builder.append("\nIssuer Code: ").append(issuerCode);
		auxString(builder);
		return builder.toString();
	}
}
