package vaulsys.authorization.policy;

import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.authorization.exception.PanPrefixServiceNotAllowedException;
import vaulsys.authorization.exception.TransactionAmountNotAcceptableException;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.impl.Terminal;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

@Entity
@DiscriminatorValue(value = "PanPrefixTrx")
public class PanPrefixTransactionPolicy extends Policy/*<EmptyPolicyData>*/
{
	@Transient
	public transient static final Bank UNDEFINED_PREFIX = null;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "pan_plc")
    @ForeignKey(name = "pan_plc_fk")
    private List<AllowedTranaction> transactions;

	public PanPrefixTransactionPolicy(){
//    	setSynchronized(false);
	}

	public Policy clone()
	{
		PanPrefixTransactionPolicy policy = new PanPrefixTransactionPolicy();
//		policy.setGeneralDao(generalDao);
//		policy.setProfile(profile);
//		policy.setTerminalService(terminalService);
		List<AllowedTranaction> newTransactions = new ArrayList<AllowedTranaction>(transactions.size());
		for (AllowedTranaction alowedTrx : transactions)
		{
			newTransactions.add(alowedTrx.clone());
		}
		policy.setTransactions(newTransactions);
		return policy;
	}

//	public EmptyPolicyData getPolicyData()
//	{
//		if (policyData == null)
//		{
//			policyData = new EmptyPolicyData();
//			policyData.setPolicy(this);
//		}
//		return (EmptyPolicyData) policyData;
//	}

	@Override
	protected void authorizeNormalCondition(Ifx ifx, Terminal terminal) throws AuthorizationException
	{
		if( ISOFinalMessageType.isReversalMessage(ifx.getIfxType()))
			return;
		
		String appPan = ifx.getAppPAN();
		TrnType trnType = ifx.getTrnType();

		if(appPan == null || appPan.length() == 0){
			throw new PanPrefixServiceNotAllowedException("Failed: PAN is null....");			
		}
		
		for (AllowedTranaction alowedTrx : transactions){
			if  (ifx != null && TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT.equals(ifx.getTrnType()))
                return;
			if ( (alowedTrx.getBank()==null || appPan.startsWith(alowedTrx.getBank().getBin().toString()) )
					&&
				 (TrnType.UNKNOWN.equals(alowedTrx.getTrnType()) || trnType.equals(alowedTrx.getTrnType()))
				){
				Long transactionAmount = ifx.getAuth_Amt();
		        Long minAmount = alowedTrx.getMinAmount();
		        
		        if (transactionAmount == null)
		        	return;
				
		        if (minAmount != null)
		        	if (transactionAmount < minAmount) 
		        		throw new TransactionAmountNotAcceptableException("Failed: Only transactions with amounts greater than "
		                        + minAmount + " are accepted for "+appPan +", "+trnType+". " + transactionAmount + " requested.", ISOResponseCodes.RESTRICTED_MIN_WITHDRAWAL_AMOUNT);
		        
		        Long maxAmount = alowedTrx.getMaxAmount();
				if (maxAmount != null)
		        	if (transactionAmount > maxAmount) 
		        		throw new TransactionAmountNotAcceptableException("Failed: Only transactions with amounts less than "
		        				+ maxAmount + " are accepted for "+appPan +", "+trnType+". "+ transactionAmount + " requested.", ISOResponseCodes.TRANSACTION_REJECTED_SWITCH_TO_CONTACT_INTERFACE);
				
				return;
			}
		}
		
		throw new PanPrefixServiceNotAllowedException("Failed: Pan not allowed on the requested service: "
				+ ifx.getAppPAN() + ", " + ifx.getTrnType().toString());
	}

	@Override
	protected void authorizeNotCondition(Ifx ifx, Terminal terminal) throws AuthorizationException {
		if( ISOFinalMessageType.isReversalMessage(ifx.getIfxType()))
			return;
		
		String appPan = ifx.getAppPAN();
		TrnType trnType = ifx.getTrnType();
		
		if(appPan == null || appPan.length() == 0){
			throw new PanPrefixServiceNotAllowedException("Failed: PAN is null....");			
		}
		
		for (AllowedTranaction alowedTrx : transactions){
			if ( (TrnType.UNKNOWN.equals(alowedTrx.getTrnType()) || trnType.equals(alowedTrx.getTrnType()))
					&&
				 (alowedTrx.getBank()==null || appPan.startsWith(alowedTrx.getBank().getBin().toString()) )
				){
				
				throw new PanPrefixServiceNotAllowedException("Failed: Pan not allowed on the requested service: "
						+ ifx.getAppPAN() + ", " + ifx.getTrnType().toString());
			}
		}
	}
	
	@Override
	public void update(Ifx ifx, Terminal terminal) {
	}

	@Override
	public boolean isSynchronized() {
		// TODO Auto-generated method stub
		return false;
	}

	public List<AllowedTranaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<AllowedTranaction> transactions) {
		this.transactions = transactions;
	}
}
