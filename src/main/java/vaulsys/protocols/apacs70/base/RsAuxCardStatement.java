package vaulsys.protocols.apacs70.base;

import static vaulsys.protocols.apacs70.base.ApacsConstants.GS;
import vaulsys.mtn.MTNChargeService;
import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.ifx.imp.BankStatementData;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.security.component.SecurityComponent;
import vaulsys.util.StringFormat;
import vaulsys.util.encoders.Hex;

import java.io.IOException;
import java.util.List;

//TASK Task008 : Add Bank Statement feature to Apacs70
public class RsAuxCardStatement extends RsAuxBase{
	public List<BankStatementData> bankStatementData;
	public String accountNumber;
	
	public RsAuxCardStatement() {
		super("72");
	}

	@Override
	public void fromIfx(Ifx ifx) {
		super.fromIfx(ifx);

		accountNumber = ifx.getSubsidiaryAccFrom();
		if (ifx.getBankStatementData() != null && ISOResponseCodes.isSuccess(ifx.getRsCode())) {
			bankStatementData = ifx.getBankStatementData();
		}		
	}

	@Override
	public void pack(ApacsByteArrayWriter out) throws IOException{
		super.pack(out); 
		out.write(GS);
		out.write(accountNumber,20);
		out.write(GS);	
		if (bankStatementData != null && bankStatementData.size() > 0)
		{
			out.writePadded(bankStatementData.size(), 2, false);			
			for(BankStatementData data : bankStatementData)
			{
				out.write(GS);		
				String strDate = data.getTrxDt().getDayDate().toString();
				out.write((strDate.substring(2, 4).getBytes()),2);
				out.write((strDate.substring(5, 7).getBytes()),2);
				out.write((strDate.substring(8, 10).getBytes()),2);
				String strTime = data.getTrxDt().getDayTime().toString();
				out.write((strTime.substring(0, 2).getBytes()),2);
				out.write((strTime.substring(3, 5).getBytes()),2);
				out.write((strTime.substring(6, 8).getBytes()),2);
				//Amount
				out.write(GS);
				String amount = String.valueOf(Math.abs(data.getAmount())); 
				out.write(amount.getBytes(),amount.length());
				//Type
				out.write(GS);
				out.write(data.getTrnType().equalsIgnoreCase("D")? (byte)'-' : (byte)'+');
				//Balance
				out.write(GS);
				String balance = String.valueOf(Math.abs(data.getBalance())); 
				out.write(balance.getBytes(),balance.length());		
			}
		}
		else
		{
			out.writePadded(0L, 2, false);		
			out.write(GS);
		}
	}
	
	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\nAccount Number : ").append(accountNumber);
		if(bankStatementData != null)
			builder.append("\nStatement count : ").append(bankStatementData.size());
	}
	
}
