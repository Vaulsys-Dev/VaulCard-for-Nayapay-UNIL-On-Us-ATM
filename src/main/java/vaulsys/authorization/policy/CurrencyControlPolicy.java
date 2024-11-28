package vaulsys.authorization.policy;

import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.authorization.exception.CurrencyControlNotAllowedException;
import vaulsys.customer.Currency;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.GlobalContext;

import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "CurrencyControl")
public class CurrencyControlPolicy extends Policy/*<EmptyPolicyData>*/
{
//	@ManyToOne(cascade = CascadeType.ALL)
//	private EmptyPolicyData emptyPolicyData;

	public CurrencyControlPolicy(){
//    	setSynchronized(false);
	}

	public Policy clone()
	{
		CurrencyControlPolicy policy = new CurrencyControlPolicy();
//		policy.setGeneralDao(generalDao);
//		policy.setProfile(profile);
//		policy.setTerminalService(terminalService);
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

//	public void setPolicyData(EmptyPolicyData policyData)
//	{
//		this.emptyPolicyData = policyData;
//	}

	@Override
	protected void authorizeNormalCondition(Ifx ifx, Terminal terminal) throws AuthorizationException
	{
		Map<Integer, Currency> allCurrency = GlobalContext.getInstance().getAllCurrencies();
		
		Integer currencyCode = ifx.getAuth_Currency();

		if(!allCurrency.containsKey(currencyCode))
			throw new CurrencyControlNotAllowedException("Failed: Currency has not allowed :"
					+ currencyCode);
	}

	@Override
	public void update(Ifx ifx, Terminal terminal) {
	}

	@Override
	public boolean isSynchronized() {
		return false;
	}

	@Override
	protected void authorizeNotCondition(Ifx ifx, Terminal terminal) throws AuthorizationException {
		// TODO Auto-generated method stub
		
	}
}
