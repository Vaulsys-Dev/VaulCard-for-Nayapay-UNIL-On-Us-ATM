package vaulsys.protocols.ndc.base.config;

import vaulsys.persistence.IEnum;

public class StatusQualifierType implements IEnum {

	private static final String UNKNOWN_VALUE = null;
	
	public static final StatusQualifierType UNKNOWN = new StatusQualifierType(UNKNOWN_VALUE);
	
	private String type;
	
    public StatusQualifierType() {
    }

    public StatusQualifierType(String type) {
    	this.type= type ;
    }
    
//    public static StatusQualifierType getByType(String type){
//    	return UNKNOWN;
//    }
//    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatusQualifierType that = (StatusQualifierType) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

	public String getType() {
		return type;
	}

	
	public String getDescription(){
		return "UNKNOWN";
	}
	
	@Override
	public String toString() {
    	return getDescription()+"("+type+")";
	}
}
