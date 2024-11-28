package vaulsys.authorization.policy;

import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.authorization.exception.FITControlNotAllowedException;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.terminal.impl.Terminal;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.ForeignKey;

@Entity
@DiscriminatorValue(value = "FITControl")
public class FITControlPolicy extends Policy {
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "auth_plc_fit_ctrl_bnk",
			joinColumns = { @JoinColumn(name = "plc_fit_ctrl") },
			inverseJoinColumns = { @JoinColumn(name = "bnk") })
	@ForeignKey(name = "plc_fitctrl_plc_fk", inverseName = "plc_fitctrl_bnk_fk")
	private List<Bank> banks;

	public FITControlPolicy() {
	}

	public Policy clone() {
		FITControlPolicy policy = new FITControlPolicy();
		policy.setBanks(banks);
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
		String bankId = ifx.getBankId();
		String destBankId = ifx.getDestBankId();
		String recvBankId = ifx.getRecvBankId();

		boolean isAllowedPan = false;
		boolean isAllowedBankID = false;
		boolean isAllowedDestBankID = false;
		boolean isAllowedReceivedBankID = false;

		for (Bank bank : getBanks()) {
			Integer bin = bank.getBin();
			if (appPan == null || appPan.startsWith(bin.toString())
					|| ifx.getTrnType().equals(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT)
						|| ifx.getTrnType().equals(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT)) {
				isAllowedPan = true;
			}
			if (bankId == null || bankId.equals(bin.toString())) {
				isAllowedBankID = true;
			}
			if (destBankId == null
					|| destBankId.equals(bin.toString())) {
				isAllowedDestBankID = true;
			}
			if (recvBankId == null
					|| recvBankId.equals(bin.toString())) {
				isAllowedReceivedBankID = true;
			}

			if (isAllowedPan && isAllowedBankID && isAllowedDestBankID
					&& isAllowedReceivedBankID) {
				return;
			}
		}
		if (!isAllowedPan)
			throw new FITControlNotAllowedException(
					"Failed: Pan has not allowed FIT :" + appPan,
					ISOResponseCodes.ACCOUNT_LOCKED);
		if (!isAllowedBankID)
			throw new FITControlNotAllowedException(
					"Failed: BankID has not allowed FIT :" + bankId,
					ISOResponseCodes.WALLET_IN_PROVISIONAL_STATE);
		if (!isAllowedDestBankID)
			throw new FITControlNotAllowedException(
					"Failed: DestinationBankID has not allowed FIT :"
							+ destBankId, ISOResponseCodes.ACCOUNT_LOCKED);
		if (!isAllowedReceivedBankID)
			throw new FITControlNotAllowedException(
					"Failed: ReceivedBankID has not allowed FIT :" + recvBankId,
					ISOResponseCodes.ACCOUNT_LOCKED);
	}

	@Override
	protected void authorizeNotCondition(Ifx ifx, Terminal terminal) throws AuthorizationException {
		String appPan = ifx.getAppPAN();
		String bankId = ifx.getBankId();
		String destBankId = ifx.getDestBankId();
		String recvBankId = ifx.getRecvBankId();

		boolean isAllowedPan = true;
		boolean isAllowedBankID = true;
		boolean isAllowedDestBankID = true;
		boolean isAllowedReceivedBankID = true;

		int index = 0;
		while (isAllowedPan && isAllowedBankID && isAllowedDestBankID
				&& isAllowedReceivedBankID && index < getBanks().size()) {
			Integer bin = getBanks().get(index++).getBin();
			
			if (!ifx.getTrnType().equals(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT)
								&& !ifx.getTrnType().equals(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT)) {
				if (appPan != null && appPan.startsWith(bin.toString())) {
					isAllowedPan = false;
				}
			}
			if (bankId != null && bankId.equals(bin.toString())) {
				isAllowedBankID = false;
			}
			if (destBankId != null
					&& destBankId.equals(bin.toString())) {
				isAllowedDestBankID = false;
			}
			if (recvBankId != null
					&& recvBankId.equals(bin.toString())) {
				isAllowedReceivedBankID = false;
			}

			if (isAllowedPan && isAllowedBankID && isAllowedDestBankID
					&& isAllowedReceivedBankID) {
				return;
			}
		}
		if (!isAllowedPan)
			throw new FITControlNotAllowedException(
					"Failed: Pan has not allowed FIT :" + appPan,
					ISOResponseCodes.ACCOUNT_LOCKED);
		if (!isAllowedBankID)
			throw new FITControlNotAllowedException(
					"Failed: BankID has not allowed FIT :" + bankId,
					ISOResponseCodes.WALLET_IN_PROVISIONAL_STATE);
		if (!isAllowedDestBankID)
			throw new FITControlNotAllowedException(
					"Failed: DestinationBankID has not allowed FIT :"
							+ destBankId, ISOResponseCodes.ACCOUNT_LOCKED);
		if (!isAllowedReceivedBankID)
			throw new FITControlNotAllowedException(
					"Failed: ReceivedBankID has not allowed FIT :" + recvBankId,
					ISOResponseCodes.ACCOUNT_LOCKED);
	}

	@Override
	public void update(Ifx ifx, Terminal terminal) {
	}

	@Override
	public boolean isSynchronized() {
		return false;
	}

	public List<Bank> getBanks() {
		return banks;
	}

	public void setBanks(List<Bank> banks) {
		this.banks = banks;
	}
}
