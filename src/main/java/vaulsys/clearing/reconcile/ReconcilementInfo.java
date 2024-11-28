package vaulsys.clearing.reconcile;

import vaulsys.calendar.DateTime;

public class ReconcilementInfo
{

	private DateTime dateTime;

	private int authorizationNumber =0 ;
	private int debitNumber =0 ;
	private int creditNumber =0 ;
	private int transferNumber= 0 ;
	private int ballInqNumber=0 ;

	private int authorizationReversalNumber=0 ;
	private int inquiryReversalNumber=0 ;
	private int debitReversalNumber=0 ;
	private int creditReversalNumber=0 ;
	private int transferReversalNumber=0 ;
	private int paymentReversalNumber=0 ;

	private int debitChargebackNumber=0 ;
	private int creditChargebackNumber=0 ;

	private long debitAmount =0;
	private long creditAmount=0;
	private long debitReversalAmount=0;
	private long creditReversalAmount=0;
	private long debitChargebackAmount=0;
	private long creditChargebackAmount=0;

	private long debitFee;
	private long creditFee;

	private int transactionCount;

	private DateTime minReceivedTime;
	private DateTime maxReceivedTime;

	public ReconcilementInfo(){
	}

	public boolean equals(ReconcilementInfo recInfo)
	{
		if (authorizationNumber == recInfo.authorizationNumber && debitNumber == recInfo.debitNumber
				&& creditNumber == recInfo.creditNumber && transferNumber == recInfo.transferNumber
				&& ballInqNumber == recInfo.ballInqNumber

				&& authorizationReversalNumber == recInfo.authorizationReversalNumber
				&& inquiryReversalNumber == recInfo.inquiryReversalNumber
				&& debitReversalNumber == recInfo.debitReversalNumber
				&& creditReversalNumber == recInfo.creditReversalNumber
				&& transferReversalNumber == recInfo.transferReversalNumber
				&& paymentReversalNumber == recInfo.paymentReversalNumber

				&& debitChargebackNumber == recInfo.debitChargebackNumber
				&& creditChargebackNumber == recInfo.creditChargebackNumber

				&& debitAmount == recInfo.debitAmount && creditAmount == recInfo.creditAmount
				&& debitReversalAmount == recInfo.debitReversalAmount
				&& creditReversalAmount == recInfo.creditReversalAmount
				&& debitChargebackAmount == recInfo.debitChargebackAmount
				&& creditChargebackAmount == recInfo.creditChargebackAmount

				&& transactionCount == recInfo.transactionCount

				&& debitFee == recInfo.debitFee && creditFee == recInfo.creditFee)
			return true;
		return false;
	}

	public void clean()
	{
		authorizationNumber = 0;
		debitNumber = 0;
		creditNumber = 0;
		transferNumber = 0;
		ballInqNumber = 0;

		authorizationReversalNumber = 0;
		inquiryReversalNumber = 0;
		debitReversalNumber = 0;
		creditReversalNumber = 0;
		transferReversalNumber = 0;
		paymentReversalNumber = 0;

		debitChargebackNumber = 0;
		creditChargebackNumber = 0;

		debitAmount = 0;
		creditAmount = 0;
		debitReversalAmount = 0;
		creditReversalAmount = 0;
		debitChargebackAmount = 0;
		creditChargebackAmount = 0;

		debitFee = 0;
		creditFee = 0;
		transactionCount = 0;
	}

	@Override
	public String toString()
	{
		String result = this.getClass().getName() + "- " + this.dateTime;
		if (creditNumber > 0)
			result += "\nCredit = #" + creditNumber + " $" + creditAmount + " $" + creditFee + "\n";
		if (creditReversalNumber > 0)
			result += "Reversal Credit = #" + creditReversalNumber + " $" + creditReversalAmount + "\n";
		if (debitNumber > 0)
			result += "Debit = #" + debitNumber + " $" + debitAmount + " $" + debitFee + "\n";
		if (debitReversalNumber > 0)
			result += "Reversal Debit = #" + debitReversalNumber + " $" + debitReversalAmount;

		result = (transferNumber > 0) ? result + "Transfer = " + transferNumber : result;
		result = (ballInqNumber > 0) ? result + " Ballence Inquery = " + ballInqNumber : result;
		result = (authorizationNumber > 0) ? result + " Authorization Reversal = " + authorizationReversalNumber
				: result;
		result = (inquiryReversalNumber > 0) ? result + " Inquiry Reversal = " + inquiryReversalNumber : result;
		result = (debitChargebackNumber > 0) ? result + " Debit charge back = " + debitChargebackNumber : result;
		result = (creditChargebackNumber > 0) ? result + " Credit charge back = " + creditChargebackNumber : result;

		return result;
	}

}
