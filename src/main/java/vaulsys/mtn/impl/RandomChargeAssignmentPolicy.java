package vaulsys.mtn.impl;

import vaulsys.entity.impl.Organization;
import vaulsys.mtn.ChargeAssignmentPolicy;
import vaulsys.mtn.MTNCharge;
import vaulsys.mtn.MTNChargeService;
import vaulsys.mtn.exception.CellChargePurchaseException;
import vaulsys.protocols.ifx.imp.Ifx;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
@Entity
@DiscriminatorValue(value = "random_policy")
public class RandomChargeAssignmentPolicy extends ChargeAssignmentPolicy<RandomChargePolicyData> {

	@Override
	public MTNCharge getCharge(Ifx ifx) throws CellChargePurchaseException {

		Random randomGenerator = new Random();
		int random = randomGenerator.nextInt(getPolicyData().getPortions());
		Map<Integer, Organization> portions = getPolicyData().getCompanyPortions();
		Organization comanyCode = null;
		
		Iterator<Integer> companyPortions = portions.keySet().iterator();
		
		while (companyPortions.hasNext() && comanyCode == null) {
			Integer code = companyPortions.next();
			if (code > random)
				comanyCode = portions.get(code);
		}

		
		
		MTNCharge charge = MTNChargeService.getCharge(ifx.getAuth_Amt(), comanyCode);

//		if (charge == null) {
//			charge = MTNChargeService.getCharge(ifx.getAuth_Amt());
//			
//			if (charge == null) {
////				if (MTNChargeService.isAllSold(ifx.getAuth_Amt()))
////					throw new AllChargeSoldException("All Charge with Credit " + ifx.getAuth_Amt() + " have been sold.");
//				throw new NoChargeAvailableException("No Available Charge with Credit: " + ifx.getAuth_Amt());
//			}
//		}
		
		return charge;
	}

	@Override
	public MTNCharge update(Ifx ifx) throws CellChargePurchaseException {
		return null;
	}

	
	public RandomChargePolicyData getPolicyData() {
		return (RandomChargePolicyData)super.getPolicyData();
	}
}
