package vaulsys.webservices.ghasedak;

import vaulsys.persistence.IEnum;

public class GhasedakUnitType implements IEnum, Cloneable{
	private static final String USD_VALUE = "USD"; 
	private static final String IRR_VALUE = "IRR";
	
	public static final GhasedakUnitType USD = new GhasedakUnitType(USD_VALUE) ;
	public static final GhasedakUnitType IRR = new GhasedakUnitType(IRR_VALUE);
	
	
	
	private String type;
	 
	 public GhasedakUnitType() {
	 }

	 public GhasedakUnitType(String type) {
		super();
		this.type = type;
	 }
	 
	 @Override
	 public boolean equals(Object obj) {
	 	if (this == obj)
	 		return true;
	 	if (obj == null || !(obj instanceof GhasedakUnitType))
	 		return false;
	 	GhasedakUnitType that = (GhasedakUnitType) obj;
	 	return type == that.type;
	 }
	 
//	 @Override
//	 public int hashCode() {
//	 	return type;
//	 }
	 
	 @Override
	 public String toString() {
	    	return type;
	 }
}
