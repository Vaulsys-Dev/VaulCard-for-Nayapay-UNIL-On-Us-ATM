package vaulsys.billpayment.consts;

import java.util.ArrayList;
import java.util.List;

import vaulsys.persistence.IEnum;
import vaulsys.thirdparty.consts.ThirdPartyType;

import javax.persistence.Embeddable;

@Embeddable
public class OrganizationType implements IEnum {
	private static final String UNKNOWN_VALUE = "UNK";
	private static final String WATER_VALUE = "WA";
	private static final String ELECTRONIC_VALUE = "EL";
	private static final String GAZ_VALUE = "GA";
	private static final String TEL_VALUE = "TC";
	private static final String MOBILE_VALUE = "MC";
	private static final String MANAGE_NET_VALUE = "MN";
	private static final String MANAGE_NET7_VALUE = "MN";
	private static final String UNDEFINED_VALUE = "UD";
	private static final String MTN_IRANCELL_VALUE = "MTNIRANCELL";
	private static final String FREEZONE_VALUE = "FZ";
	private static final String THIRD_PARTY_PURCHASE_VALUE = "TP";
	private static final String TAX_GOVERMENT_VALUE = "TG";
	
	public static final OrganizationType UNKNOWN = new OrganizationType(UNKNOWN_VALUE);
	public static final OrganizationType WATER = new OrganizationType(WATER_VALUE);
	public static final OrganizationType ELECTRONIC = new OrganizationType(ELECTRONIC_VALUE);
	public static final OrganizationType GAZ = new OrganizationType(GAZ_VALUE);
	public static final OrganizationType TEL = new OrganizationType(TEL_VALUE);
	public static final OrganizationType MOBILE = new OrganizationType(MOBILE_VALUE);
	public static final OrganizationType MANAGE_NET = new OrganizationType(MANAGE_NET_VALUE);
	public static final OrganizationType MANAGE_NET7 = new OrganizationType(MANAGE_NET7_VALUE);
	public static final OrganizationType UNDEFINED = new OrganizationType(UNDEFINED_VALUE);
	public static final OrganizationType MTNIRANCELL = new OrganizationType(MTN_IRANCELL_VALUE);
	public static final OrganizationType FREEZONE = new OrganizationType(FREEZONE_VALUE);
	public static final OrganizationType TAX_GOVERMENT = new OrganizationType(TAX_GOVERMENT_VALUE);
	
	public static final OrganizationType THIRDPARTYPURCHASE = new OrganizationType(THIRD_PARTY_PURCHASE_VALUE);
	public static final String MTNPARSNIKATEL = null;
	public static final String THIRDPARTY = null;
	
	private String type;

	public OrganizationType() {
	}

	public OrganizationType(String type) {
		this.type = type;
	}

	public static OrganizationType getByCode(Integer code) {
		if (code.equals(1))
			return WATER;
		if (code.equals(2))
			return ELECTRONIC;
		if (code.equals(3))
			return GAZ;
		if (code.equals(4))
			return TEL;
		if (code.equals(5))
			return MOBILE;
		if (code.equals(6))
			return MANAGE_NET;
		if (code.equals(7))
			return MANAGE_NET7;
		if (code.equals(0))
			return UNDEFINED;
		if (code.equals(8))
			return TAX_GOVERMENT;
		
		if (code.equals(9))
			return FREEZONE;
		
		
		if (code.equals(99))
			return MTNIRANCELL;
		
		if(code.equals(97))
			return THIRDPARTYPURCHASE;

		return UNKNOWN;
	}

	public static byte getCode(OrganizationType orgType) {

		if (orgType.equals(UNDEFINED))
			return 0;
		
		if (orgType.equals(WATER))
			return 1;

		if (orgType.equals(ELECTRONIC))
			return 2;

		if (orgType.equals(GAZ))
			return 3;

		if (orgType.equals(TEL))
			return 4;

		if (orgType.equals(MOBILE))
			return 5;

		if (orgType.equals(MANAGE_NET))
			return 6;

		if (orgType.equals(MANAGE_NET7))
			return 7;

		if (orgType.equals(TAX_GOVERMENT))
			return 8;
		
		if (orgType.equals(FREEZONE))
			return 9;
		
		
		if (orgType.equals(MTNIRANCELL))
			return 99;
		
		if (orgType.equals(THIRDPARTYPURCHASE))
			return 97;

		return -1;
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
		OrganizationType that = (OrganizationType) o;
		return this.type.equals(that.type);
	}

	public static OrganizationType convert(String type) {
		return new OrganizationType(type);
	}

	public ThirdPartyType findThirdpartyType() {
		if (OrganizationType.ELECTRONIC.type.equals(type) 
				|| OrganizationType.GAZ.type.equals(type) 
				|| OrganizationType.MANAGE_NET.type.equals(type)
				|| OrganizationType.MANAGE_NET7.type.equals(type)
				|| OrganizationType.MOBILE.type.equals(type)
				|| OrganizationType.TEL.type.equals(type) 
				|| OrganizationType.WATER.type.equals(type)
				|| OrganizationType.FREEZONE.type.equals(type)
				|| OrganizationType.TAX_GOVERMENT.type.equals(type)
				|| OrganizationType.UNDEFINED.type.equals(type)
				)
			
			return ThirdPartyType.BILLPAYMENT;

		if (OrganizationType.MTNIRANCELL.type.equals(type)) 
			return ThirdPartyType.CHARGE;
		
		if (OrganizationType.THIRDPARTYPURCHASE.type.equals(type)) 
			return ThirdPartyType.THIRDPARTYPURCHASE;
		

		return ThirdPartyType.UNKNOWN;
	}

	@Override
	public String toString() {
		if (type.equals(WATER_VALUE))
			return "آب";
		if (type.equals(ELECTRONIC_VALUE))
			return "برق";
		if (type.equals(GAZ_VALUE))
			return "گاز";
		if (type.equals(TEL_VALUE))
			return "تلفن ثابت";
		if (type.equals(MOBILE_VALUE))
			return "تلفن همراه";
		if (type.equals(MANAGE_NET_VALUE))
			return "شهرداری";
		if (type.equals(MANAGE_NET7_VALUE))
			return "شهرداری";
		if (type.equals(MTN_IRANCELL_VALUE))
			return "ایرانسل";
		//if (type.equals(MTN_PARSNIKATEL_VALUE))
		//	return "پارس نیکاتل";
        if (type.equals(UNDEFINED_VALUE))
            return "ساير";
		if (type.equals(FREEZONE_VALUE))
			return "سازمان منطقه آزاد کیش";
		if (type.equals(TAX_GOVERMENT_VALUE))
			return "امور مالیاتی کشور";
		if (type.equals(THIRD_PARTY_PURCHASE_VALUE) )
			return "عنصر سوم";
		return "ناشناخته";
	}

	public String toStringEnglish() {
		if (type.equals(WATER_VALUE))
			return "WATER";
		if (type.equals(ELECTRONIC_VALUE))
			return "ELECTRICITY";
		if (type.equals(GAZ_VALUE))
			return "GAZ";
		if (type.equals(TEL_VALUE))
			return "TELEPHONE";
		if (type.equals(MOBILE_VALUE))
			return "MOBILE";
		if (type.equals(MANAGE_NET_VALUE))
			return "CITY";
		if (type.equals(MANAGE_NET7_VALUE))
			return "CITY";
		if (type.equals(MTN_IRANCELL_VALUE))
			return "MTN IRANCELL";
		if (type.equals(FREEZONE_VALUE))
			return "FREEZONE";
		if (type.equals(TAX_GOVERMENT))
			return "TAX";
		if(type.equals(THIRD_PARTY_PURCHASE_VALUE))
			return "THIRDPARTY";
		if (type.equals(UNDEFINED_VALUE))
			return "UNDEFINED";
		return "UNKNOWN";
	}
	@Override
	public int hashCode() {
		return type.hashCode();
	}
	
	 public static List<OrganizationType> convertType(String[] list) {
	        List<OrganizationType> organizationTypes = new ArrayList<OrganizationType>();
	        for (String aList : list) {
	            if (getByCode(Integer.valueOf(aList)).equals(WATER))
	                organizationTypes.add(WATER);

	            if (getByCode(Integer.valueOf(aList)).equals(GAZ))
	                organizationTypes.add(GAZ);

	            if (getByCode(Integer.valueOf(aList)).equals(ELECTRONIC))
	                organizationTypes.add(ELECTRONIC);

	            if (getByCode(Integer.valueOf(aList)).equals(TEL))
	                organizationTypes.add(TEL);

	            if (getByCode(Integer.valueOf(aList)).equals(MOBILE))
	                organizationTypes.add(MOBILE);

	            if (getByCode(Integer.valueOf(aList)).equals(MANAGE_NET))
	                organizationTypes.add(MANAGE_NET);

	            if (getByCode(Integer.valueOf(aList)).equals(UNDEFINED))
	                organizationTypes.add(UNDEFINED);

	            if (getByCode(Integer.valueOf(aList)).equals(MTNIRANCELL))
	                organizationTypes.add(MTNIRANCELL);

	            if (getByCode(Integer.valueOf(aList)).equals(FREEZONE))
	                organizationTypes.add(FREEZONE);

	            /*if (getByCode(Integer.valueOf(aList)).equals(THIRDPARTY))
	                organizationTypes.add(THIRDPARTY);*/

	            if (getByCode(Integer.valueOf(aList)).equals(UNKNOWN))
	                organizationTypes.add(UNKNOWN);
	        }
	        return organizationTypes;
	    }
}
