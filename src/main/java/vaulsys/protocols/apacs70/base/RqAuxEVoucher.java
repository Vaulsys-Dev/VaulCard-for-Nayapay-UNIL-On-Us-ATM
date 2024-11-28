package vaulsys.protocols.apacs70.base;

import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.wfe.GlobalContext;

public class RqAuxEVoucher extends RqAuxBase {
	public Long voucherType;

	@Override
	public void unpack(ApacsByteArrayReader in) {
		voucherType = in.getLongFixedToSep("voucherType", 4, ApacsConstants.GS);

		super.unpack(in);
	}

	@Override
	public void toIfx(Ifx ifx) {
		super.toIfx(ifx);

		ifx.setThirdPartyCode(this.voucherType);

if (GlobalContext.getInstance().getMyInstitution().getBin().equals(502229L) 
				&& ifx.getThirdPartyCode().equals(9935L)) {
			ifx.setThirdPartyCode(9936L);
		}
	}

	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\nVoucher Type: ").append(voucherType);
	}
}
