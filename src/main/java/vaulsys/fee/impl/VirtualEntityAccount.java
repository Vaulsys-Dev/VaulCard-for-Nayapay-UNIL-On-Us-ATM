package vaulsys.fee.impl;

import vaulsys.customer.Account;
import vaulsys.customer.AccountOwnerType;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.entity.impl.Shop;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.Util;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "Virtual")
public class VirtualEntityAccount extends AbstractEntityAccount {

    @Embedded
    @AttributeOverride(name = "type", column = @Column(name = "owner_type"))
    private AccountOwnerType ownerType;

    public VirtualEntityAccount() {
    }

    public VirtualEntityAccount(AccountOwnerType ownerType) {
        this.ownerType = ownerType;
    }

    public Account getAccount(final Ifx ifx) {
        FinancialEntity entity = getFinancialEntity(ifx);
        if (entity != null)
            return entity.getOwnOrParentAccount();
        return null;
    }

    public FinancialEntity getFinancialEntity(Ifx ifx) {
        if (ownerType.equals(AccountOwnerType.ACQUIRER)) {
            return FinancialEntityService.getInstitutionByBIN(Util.longValueOf(ifx.getBankId()));
        } else if (ownerType.equals(AccountOwnerType.ISSUER)) {
            return FinancialEntityService.getInstitutionByBIN(Util.longValueOf(ifx.getDestBankId()));
        } else if (ownerType.equals(AccountOwnerType.MERCHANT)) {
        	Shop shop = FinancialEntityService.findEntity(Shop.class, ifx.getOrgIdNum());
            return shop.getOwner();
        } else if (ownerType.equals(AccountOwnerType.SHOP)) {
            return FinancialEntityService.findEntity(Shop.class, ifx.getOrgIdNum());
        } else if (ownerType.equals(AccountOwnerType.ORGANIZATION)) {
        	return ifx.getThirdParty(ifx.getIfxType()); 
//            return SwitchApplication.get().getOrganizationService().findOrganizationByCompanyCode(companyCode, BillPaymentUtil.extractBillOrgType(ifx.getBillID()));
//            Integer companyCode = BillPaymentUtil.extractCompanyCode(ifx.getBillID()); 
//            return SwitchApplication.get().getOrganizationService().findOrganizationByCompanyCode(companyCode, BillPaymentUtil.extractBillOrgType(ifx.getBillID()));
        }
        return null;
    }

    public AccountOwnerType getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(AccountOwnerType ownerType) {
        this.ownerType = ownerType;
    }

	@Override
	public String toString() {
		return ownerType!=null ? ownerType.toString():"";
	}
}
