package vaulsys.mtn.impl;

import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.Organization;
import vaulsys.mtn.ChargeAssignmentPolicy;
import vaulsys.mtn.MTNCharge;
import vaulsys.mtn.MTNChargeService;
import vaulsys.mtn.exception.CellChargePurchaseException;
import vaulsys.mtn.exception.NoChargeAvailableException;
import vaulsys.protocols.ifx.imp.Ifx;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.log4j.Logger;

@Entity
@DiscriminatorValue(value = "general_policy")
public class GeneralChargeAssignmentPolicy extends ChargeAssignmentPolicy<EmptyChargePolicyData> {
	private static final Logger logger = Logger.getLogger(GeneralChargeAssignmentPolicy.class);

	@Override
	public MTNCharge getCharge(Ifx ifx) throws CellChargePurchaseException {
		MTNCharge charge = null;
		Organization organization = null;
		
		if (ifx.getThirdPartyCode()!=null) {
			organization = FinancialEntityService.findEntity(Organization.class, ""+ifx.getThirdPartyCode());
			if (organization == null) {
				throw new NoChargeAvailableException("Invalid Company Code: " + ifx.getThirdPartyCode());
			}
		}else{
			logger.error("ifx.getThirdPartyCode()==null");
			throw new NoChargeAvailableException("ifx.getThirdPartyCode()==null");	
		}
		
		try {
			charge = MTNChargeService.getCharge(ifx.getAuth_Amt(), organization);
		} catch (Exception e) {
			throw new NoChargeAvailableException("No Available Charge with Credit: " + ifx.getAuth_Amt()+ " and companyCode "+ ifx.getThirdPartyCode());
		}
		
		if (charge == null) {
//			if (MTNChargeService.isAllSold(ifx.getAuth_Amt(), organization))
//				throw new AllChargeSoldException("All Charge with Credit " + ifx.getAuth_Amt() + "and companyCode "+ ifx.getThirdPartyCode()+" have been sold.");

			throw new NoChargeAvailableException("No Available Charge with Credit: " + ifx.getAuth_Amt()+ " and companyCode "+ ifx.getThirdPartyCode());
		}
		return charge;
	}
	
	@Override
	public MTNCharge update(Ifx ifx) throws CellChargePurchaseException {
		return null;
	}

}
