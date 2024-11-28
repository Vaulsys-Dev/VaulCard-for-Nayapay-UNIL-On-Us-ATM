package vaulsys.fee;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.FeeType;
import vaulsys.fee.base.FeeInfo;
import vaulsys.fee.impl.BaseFee;
import vaulsys.fee.impl.Fee;
import vaulsys.fee.impl.FeeProfile;
import vaulsys.message.Message;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.transaction.Transaction;
import vaulsys.util.NotUsed;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeeService {
	@NotUsed
	public FeeProfile findFeeProfile(String name) {
		String query = "from FeeProfile f where f.name = :name";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("name", name);
		return (FeeProfile) GeneralDao.Instance.findObject(query, param);
	}

	public static void computeFees(FeeProfile feeProfile, ClearingProfile clearingProfile, Ifx ifx) {
		List<FeeInfo> feeInfos = calculateFees(feeProfile, ifx);

		if (feeInfos == null || feeInfos.size() <= 0) {
			return;
		}

		for (FeeInfo feeInfo : feeInfos) {
			Fee fee = new Fee();
			fee.setTransaction(ifx.getTransaction());
			fee.setFeeProfile(feeProfile);
			fee.setClearingProfile(clearingProfile);
			fee.setAmount(feeInfo.getAmount());
			fee.setInsertionTime(DateTime.now());

			fee.setEntityToBeDebited(feeInfo.getEntityToBeDebited());
			fee.setEntityToBeCredited(feeInfo.getEntityToBeCredited());

			fee.setFeeItem(feeInfo.getFeeItem());
			GeneralDao.Instance.saveOrUpdate(fee);
		}
	}

	public static void computeFees(FeeProfile feeProfile, ClearingProfile clearingProfile, Message message) {
		Ifx ifx = message.getIfx();
		List<FeeInfo> feeInfos = calculateFees(feeProfile, ifx);

		if (feeInfos == null || feeInfos.size() <= 0) {
			return;
		}

		for (FeeInfo feeInfo : feeInfos) {
			Fee fee = new Fee();
			fee.setTransaction(message.getTransaction());
			fee.setFeeProfile(feeProfile);
			fee.setClearingProfile(clearingProfile);
			fee.setAmount(feeInfo.getAmount());
			fee.setInsertionTime(DateTime.now());

			fee.setEntityToBeDebited(feeInfo.getEntityToBeDebited());
			fee.setEntityToBeCredited(feeInfo.getEntityToBeCredited());

			fee.setFeeItem(feeInfo.getFeeItem());
			GeneralDao.Instance.saveOrUpdate(fee);
		}
	}

	public static List getFees(Transaction transaction, ClearingProfile clearingProfile, FeeType feeType) {
		String entityToBeDebitedOrCredited = (feeType == FeeType.CREDIT ? "d.entityToBeCredited" : "d.entityToBeDebited");
		String query = "select " + entityToBeDebitedOrCredited
				+ ".id ,sum(d.amount) from " + Fee.class.getName() + " d "
				+ " where d.transaction.id = :transaction "
				+ " and d.clearingProfile = :clearingProfile " + " group by "
				+ entityToBeDebitedOrCredited + ".id";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("transaction", transaction.getId());
		params.put("clearingProfile", clearingProfile);
		return GeneralDao.Instance.find(query, params);
	}
	//Mirkamali(Task179): Currency ATM
	public static List<FeeInfo> calculateFees(FeeProfile feeProfile, Ifx ifx) {
		if (feeProfile != null) {
			return calculateTransactionFee(feeProfile, ifx);
		}
		return new ArrayList<FeeInfo>(0);
	}

	private static List<FeeInfo> calculateTransactionFee(FeeProfile feeProfile, Ifx ifx) {
		if (!feeProfile.isEnabled())
			return null;
		List<FeeInfo> result = new ArrayList<FeeInfo>();
		List<BaseFee> transactionFees = getTransactionFee(feeProfile.getId());
		for (BaseFee transactionFee : transactionFees) {
			List<FeeInfo> fees = transactionFee.calculate(ifx);
			if (fees != null) {
				result.addAll(fees);
			}
		}
		return result;
	}

	public static List<BaseFee> getTransactionFee(Long feeProfileId) {
		// String query =
		// "from TransactionFee i where i.owner.id = :feeProfileId ";
		// Map<String, Object> params = new HashMap<String, Object>();
		// params.put("feeProfileId", feeProfileId);
		// return generalDao.find(query, params, false);
//		return GlobalContext.getInstance().getTransactionFees(feeProfileId);
		return ProcessContext.get().getTransactionFees(feeProfileId);
	}
	
	//Mirkamali(Task179): Currency ATM
		public static void updateFee(Transaction transaction, Transaction refTransaction) {
			String query = "from Fee where transaction = :transaction";
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("transaction", refTransaction);
			List<Fee> fees = GeneralDao.Instance.find(query, param);
			if(fees != null && fees.size() > 0) {
				for(Fee fee : fees) {
					fee.setTransaction(transaction);
					GeneralDao.Instance.saveOrUpdate(fee);
				}
			}
		}
}
