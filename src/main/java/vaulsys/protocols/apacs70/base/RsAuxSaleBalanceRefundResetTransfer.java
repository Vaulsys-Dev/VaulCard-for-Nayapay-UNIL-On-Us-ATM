package vaulsys.protocols.apacs70.base;

import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.ifx.imp.Ifx;

import java.io.IOException;

public class RsAuxSaleBalanceRefundResetTransfer extends RsAuxBase {
	public RsAuxSaleBalanceRefundResetTransfer() {
		super("51");
	}

	@Override
	public void fromIfx(Ifx ifx) {
		super.fromIfx(ifx);
	}

	@Override
	public void pack(ApacsByteArrayWriter out) throws IOException {
		super.pack(out);
	}

	@Override
	protected void auxString(StringBuilder builder) {
	}
}
