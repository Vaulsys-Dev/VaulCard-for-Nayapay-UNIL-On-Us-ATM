package vaulsys.thirdparty.consts;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class ThirdPartyType implements IEnum{

    private static final String UNKNOWN_VALUE = "UNK";
    private static final String BILLPAYMENT_VALUE = "BILLPAYMENT";
    private static final String CHARGE_VALUE = "CHARGE";
    private static final String THIRDPARTYPURCHASE_VALUE = "THIRDPARTYPURCHASE";

    
    public static final ThirdPartyType UNKNOWN = new ThirdPartyType(UNKNOWN_VALUE);
    public static final ThirdPartyType BILLPAYMENT = new ThirdPartyType(BILLPAYMENT_VALUE);
    public static final ThirdPartyType CHARGE = new ThirdPartyType(CHARGE_VALUE);
    public static final ThirdPartyType THIRDPARTYPURCHASE = new ThirdPartyType(THIRDPARTYPURCHASE_VALUE);

    private String type;

    public ThirdPartyType() {
    }

    public ThirdPartyType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThirdPartyType that = (ThirdPartyType) o;
        return type.equals(that.type);
    }
    
    public static ThirdPartyType convert(String type) {
    	return new ThirdPartyType(type);
    }
    
    @Override
    public String toString() {
    	if (type.equals(BILLPAYMENT_VALUE))
    		return "پرداخت قبض";
    	if (type.equals(CHARGE_VALUE))
    		return "شارژ";
    	if (type.equals(THIRDPARTYPURCHASE_VALUE))
    		return "عنصر سوم";
    	return "ناشناخته";
    }

	@Override
	public int hashCode() {
		return type!=null ? type.hashCode():0;
	}
}