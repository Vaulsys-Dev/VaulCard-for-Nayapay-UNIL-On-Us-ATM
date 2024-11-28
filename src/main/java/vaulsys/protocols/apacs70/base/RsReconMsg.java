package vaulsys.protocols.apacs70.base;

import static vaulsys.protocols.apacs70.base.ApacsConstants.FS;
import static vaulsys.protocols.apacs70.base.ApacsConstants.US;
import static vaulsys.protocols.apacs70.base.ApacsConstants.ESC;
import vaulsys.protocols.apacs70.ApacsByteArrayWriter;

import java.io.IOException;

public class RsReconMsg extends RsBaseMsg {
	public Integer totalValueDebits;
	public Integer totalQuantityDebits;
	public Integer totalValueCredits;
	public Integer totalQuantityCredits;
	public Long    merchantBalance;
	public String  merchantAccountNumber;
	
	@Override
	public void pack(ApacsByteArrayWriter out) throws IOException {
		super.pack(out);

		out.write(totalValueDebits, 11);
		out.write(FS);
		out.writePadded(totalQuantityDebits, 4, true);
		out.write(FS);
		out.write(totalValueCredits, 11);
		out.write(FS);
		out.writePadded(totalQuantityCredits, 4, true);
		out.write(FS);
		/* Message Part */
			//Card Acceptor Display Data
			out.write(US);
			out.writePadded(merchantBalance, 19, true);
			out.write(ESC);
			out.write(merchantAccountNumber, 29);
			out.write(US);
			//PINPAD Data
		out.write(FS);
		// Amount Session Number
		out.write(FS);
		out.write(MAC, 8);
	}
	
	@Override
	protected void msgString(StringBuilder builder) {
		super.msgString(builder);
		builder.append("\nMerchant Account Number: ").append(merchantAccountNumber);
		builder.append("\nMerchant Balance: ").append(merchantBalance);
	}
}
