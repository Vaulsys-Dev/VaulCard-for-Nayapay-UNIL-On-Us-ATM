package vaulsys.authorization.policy;

import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.authorization.exception.PanPrefixServiceNotAllowedException;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
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
@DiscriminatorValue(value = "TrxTypePanPrefix")
public class TrxTypePanPrefixPolicy extends Policy/*<EmptyPolicyData>*/{
	@Transient
	public transient static final String OTHERS_PREFIX = "OTHERS";

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "trx_pan_plc")
    @ForeignKey(name = "trx_pan_plc_fk")
    private List<AllowedTranactionTypeBanks> alowedList;

	public TrxTypePanPrefixPolicy(){
	}

	public Policy clone() {
		TrxTypePanPrefixPolicy policy = new TrxTypePanPrefixPolicy();
		// policy.setGeneralDao(generalDao);
		// policy.addProfile(profile);
		// policy.setTerminalService(terminalService);
		List<AllowedTranactionTypeBanks> newTypes = new ArrayList<AllowedTranactionTypeBanks>(alowedList.size());
		for (AllowedTranactionTypeBanks alowedType : alowedList) {
			newTypes.add(alowedType.clone());
		}
		policy.setAlowedList(newTypes);
		return policy;
	}

//	public EmptyPolicyData getPolicyData() {
//		if (policyData == null) {
//			policyData = new EmptyPolicyData();
//			policyData.setPolicy(this);
//		}
//		return (EmptyPolicyData) policyData;
//	}
	
	@Override
	protected void authorizeNormalCondition(Ifx ifx, Terminal terminal) throws AuthorizationException {
		String appPan = ifx.getAppPAN();
		TrnType trxType = ifx.getTrnType();

		if(appPan == null || appPan.length() == 0){
			throw new PanPrefixServiceNotAllowedException("Failed: PAN is null....");			
		}

		for (AllowedTranactionTypeBanks alowedType : alowedList) {
			if (alowedType.getType().equals(trxType)) {
				boolean contains = false;
				List<Bank> banks = alowedType.getBanks();
				int index =0;
				while (!contains && banks.size() > index) {
					if (banks.get(index++).getBin().equals(Integer.parseInt(appPan.substring(0, 6))))
						contains = true;
				}
				if (!contains)
					throw new PanPrefixServiceNotAllowedException("Failed: Pan not allowed on the requested service: "
							+ ifx.getAppPAN() + ", " + trxType.toString());
			}
		}
	}

	@Override
	public void update(Ifx ifx, Terminal terminal) {
	}

	@Override
	public boolean isSynchronized() {
		return false;
	}

	public List<AllowedTranactionTypeBanks> getAlowedList() {
		return alowedList;
	}

	public void setAlowedList(List<AllowedTranactionTypeBanks> alowedList) {
		this.alowedList = alowedList;
	}

	@Override
	protected void authorizeNotCondition(Ifx ifx, Terminal terminal) throws AuthorizationException {
		// TODO Auto-generated method stub
		
	}

	// public boolean equals(Object obj) {
	// if (this == obj) {
	// return true;
	// }
	// if (obj == null || getClass() != obj.getClass()) {
	// return false;
	// }
	// PanPrefixServicePolicy policy = (PanPrefixServicePolicy) obj;
	//
	// if (alowedTypes == null && policy.alowedTypes != null
	// || alowedTypes != null && policy.alowedTypes == null
	// || alowedTypes.size() != policy.alowedTypes.size())
	// return false;
	//
	// for (AllowedTranactionType alowedType : alowedTypes) {
	// if (!policy.alowedTypes.contains(alowedType))
	// return false;
	// }
	// return true;
	// }
	//
	// public int hashCode() {
	// return alowedTypes != null ? alowedTypes.size() : 0;
	// }

}
