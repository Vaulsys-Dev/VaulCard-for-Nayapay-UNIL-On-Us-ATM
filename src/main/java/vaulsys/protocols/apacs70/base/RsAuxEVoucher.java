package vaulsys.protocols.apacs70.base;

import static vaulsys.protocols.apacs70.base.ApacsConstants.*;
import vaulsys.mtn.MTNChargeService;
import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.security.component.SecurityComponent;
import vaulsys.util.encoders.Hex;

import java.io.IOException;

import org.apache.log4j.Logger;

public class RsAuxEVoucher extends RsAuxBase {
	private static Logger logger = Logger.getLogger(RsAuxEVoucher.class);
	
	public String voucherSerialNumber;
	public String voucherPassword;
	public String voucherData1;
	
	public RsAuxEVoucher() {
		super("50");
	}

	@Override
	public void fromIfx(Ifx ifx) {
		super.fromIfx(ifx);

		if (ifx.getChargeData() != null && ISOResponseCodes.isSuccess(ifx.getRsCode())) {
			voucherSerialNumber = ifx.getChargeData().getCharge().getCardSerialNo().toString();
			if (ifx.getThirdPartyCode() == 9935) {
				voucherSerialNumber = "IR" + voucherSerialNumber;
				voucherData1 = MTNChargeService.getRealChargeCredit(
						ifx.getChargeData().getCharge().getCredit(),
						ifx.getChargeData().getCharge().getEntity().getCode()).toString();
			}
			try {
				byte[] actualPIN = SecurityComponent.rsaDecrypt(Hex.decode(ifx.getChargeData().getCharge().getCardPIN()));
				voucherPassword = new String(actualPIN);
			} catch (Exception e) {
				logger.error("Decrypting PIN: ", e);
			}
		}
	}

	@Override
	public void pack(ApacsByteArrayWriter out) throws IOException{
		super.pack(out);

		out.write(GS);
		out.write(voucherSerialNumber, 32);
		out.write(GS);
		out.write(voucherPassword, 32);
		out.write(GS);
		out.write(voucherData1, 32); // E-Voucher Data 1
		out.write(GS);
		// E-Voucher Data 2
		out.write(GS);
		// E-Voucher Data 3
		out.write(GS);
		// E-Voucher Data 4
		out.write(GS);
		// E-Voucher Data 5
	}

	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\nVoucher Serial Number: ").append(voucherSerialNumber);
		if(voucherData1 != null)
			builder.append("\nVoucher Data1: ").append(voucherData1);
	}
}

