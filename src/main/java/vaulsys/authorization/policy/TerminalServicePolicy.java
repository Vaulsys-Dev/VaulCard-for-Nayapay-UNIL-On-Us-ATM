package vaulsys.authorization.policy;

import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.authorization.exception.ServiceTypeNotAllowedException;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.impl.Terminal;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.ForeignKey;

@Entity
@DiscriminatorValue(value = "TerminalService")
public class TerminalServicePolicy extends Policy/*<EmptyPolicyData>*/
{
	@CollectionOfElements(fetch = FetchType.LAZY)
	@Enumerated(value = EnumType.STRING)
	@JoinTable(name = "auth_plc_trm__alw_typ", 
			joinColumns = {@JoinColumn(name = "term_serv")}
//	,
//			inverseJoinColumns = {@JoinColumn(name = "trntype")}
	)
	@ForeignKey(name = "termserv_plc_fk", inverseName = "termserv_trntype_fk")
	private List<TrnType> trmAlwdTypes;
	
	public TerminalServicePolicy(){
//    	setSynchronized(false);
	}

	public Policy clone()
	{
		TerminalServicePolicy policy = new TerminalServicePolicy();
//		policy.setGeneralDao(generalDao);
//		policy.setProfile(profile);
//		policy.setTerminalService(terminalService);
		List<TrnType> newTypes = new ArrayList<TrnType>(trmAlwdTypes.size());
		for (TrnType alowedType : trmAlwdTypes)
		{
			newTypes.add(alowedType);
		}
		policy.setAlowedTypes(newTypes);
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

	public List<TrnType> getAlowedTypes()
	{
		return trmAlwdTypes;
	}

	public void setAlowedTypes(List<TrnType> alowedTypes)
	{
		this.trmAlwdTypes = alowedTypes;
	}

	@Override
	protected void authorizeNormalCondition(Ifx ifx, Terminal terminal) throws AuthorizationException {
		if (!trmAlwdTypes.contains(ifx.getTrnType())) {
			throw new ServiceTypeNotAllowedException("Failed: Terminal not allowed on the requested service: "
					+ ifx.getTrnType().toString());
		}
		return;
	}
	
	@Override
	protected void authorizeNotCondition(Ifx ifx, Terminal terminal) throws AuthorizationException {
		if (trmAlwdTypes.contains(ifx.getTrnType())) {
			throw new ServiceTypeNotAllowedException("Failed: Terminal not allowed on the requested service: "
					+ ifx.getTrnType().toString());
		}
		return;
	}

	@Override
	public void update(Ifx ifx, Terminal terminal) {
	}

	@Override
	public boolean isSynchronized() {
		// TODO Auto-generated method stub
		return false;
	}
}
