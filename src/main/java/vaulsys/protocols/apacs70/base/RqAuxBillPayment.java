package vaulsys.protocols.apacs70.base;

import org.apache.log4j.Logger;

import vaulsys.billpayment.BillPaymentUtil;
import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.Util;

public class RqAuxBillPayment extends RqAuxBase {
	private static final Logger logger = Logger.getLogger(RqAuxBillPayment.class);
	
	public String billId;
	public String billPaymentId;

	@Override
	public void unpack(ApacsByteArrayReader in) {
		billId = in.getStringMaxToSep("billId", 14, ApacsConstants.GS);
		billPaymentId = in.getStringMaxToSep("billPaymentId", 14, ApacsConstants.GS);

		super.unpack(in);
	}

	@Override
	public void toIfx(Ifx ifx) {
		super.toIfx(ifx);

		if (Util.hasText(billId)) {
			String billIdLong = "";
			try {
				billIdLong = String.valueOf(Long.parseLong(this.billId));
			} catch(Exception e) {
				logger.warn("bad BillID!");
			}
			ifx.setBillID(billIdLong);
        	ifx.setBillCompanyCode(BillPaymentUtil.extractCompanyCode(billIdLong));
        	ifx.setThirdPartyTerminalId(BillPaymentUtil.getThirdPartyTerminalId(billIdLong));
			ifx.setBillOrgType(BillPaymentUtil.extractBillOrgType(billIdLong));
		}
		
		if (Util.hasText(billPaymentId)) {
		String payIdLong = "";
		try {
			payIdLong = String.valueOf(Long.parseLong(this.billPaymentId));
		} catch(Exception e) {
			logger.warn("bad PaymentID!");
		}
		ifx.setBillPaymentID(payIdLong);
		}
	}
	
	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\nBill Id: ").append(billId);
		builder.append("\nBill Payment Id: ").append(billPaymentId);
	}
}
