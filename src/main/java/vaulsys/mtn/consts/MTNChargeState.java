package vaulsys.mtn.consts;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class MTNChargeState implements IEnum, Comparable {

	private static final Integer NOT_ASSIGNED_VALUE = 0;
    private static final Integer ASSIGNED_VALUE = 1;
    private static final Integer IN_ASSIGNED_VALUE = 2;
    private static final Integer LOCKED_VALUE = 3;
    private static final Integer CACHED_VALUE = 4;
    private static final Integer SOLD_BEFORE_VALUE = 5;
    private static final Integer SETTLED_SUSPICIOUS_VALUE = 6;
    private static final Integer UN_SETTLED_SUSPICIOUS_VALUE = 7;

    private static final Integer DUMMY_50_VALUE = 50;
    private static final Integer DUMMY_51_VALUE = 51;
    private static final Integer DUMMY_52_VALUE = 52;
    private static final Integer DUMMY_53_VALUE = 53;
    private static final Integer DUMMY_54_VALUE = 54;
    private static final Integer DUMMY_55_VALUE = 55;
    private static final Integer DUMMY_56_VALUE = 56;
    private static final Integer DUMMY_57_VALUE = 57;
    private static final Integer DUMMY_58_VALUE = 58;
    private static final Integer DUMMY_59_VALUE = 59;
    private static final Integer DUMMY_60_VALUE = 60;

    
    public static final MTNChargeState NOT_ASSIGNED = new MTNChargeState(NOT_ASSIGNED_VALUE); 
    public static final MTNChargeState ASSIGNED = new MTNChargeState(ASSIGNED_VALUE); 
    public static final MTNChargeState IN_ASSIGNED = new MTNChargeState(IN_ASSIGNED_VALUE); 
    public static final MTNChargeState LOCKED = new MTNChargeState(LOCKED_VALUE); 
    public static final MTNChargeState CACHED = new MTNChargeState(CACHED_VALUE); 
    public static final MTNChargeState SOLD_BEFORE = new MTNChargeState(SOLD_BEFORE_VALUE);
    public static final MTNChargeState SETTLED_SUSPICIOUS = new MTNChargeState(SETTLED_SUSPICIOUS_VALUE);
    public static final MTNChargeState UN_SETTLED_SUSPICIOUS = new MTNChargeState(UN_SETTLED_SUSPICIOUS_VALUE);

    public static final MTNChargeState DUMMY_50 = new MTNChargeState(DUMMY_50_VALUE);
    public static final MTNChargeState DUMMY_51 = new MTNChargeState(DUMMY_51_VALUE);
    public static final MTNChargeState DUMMY_52 = new MTNChargeState(DUMMY_52_VALUE);
    public static final MTNChargeState DUMMY_53 = new MTNChargeState(DUMMY_53_VALUE);
    public static final MTNChargeState DUMMY_54 = new MTNChargeState(DUMMY_54_VALUE);
    public static final MTNChargeState DUMMY_55 = new MTNChargeState(DUMMY_55_VALUE);
    public static final MTNChargeState DUMMY_56 = new MTNChargeState(DUMMY_56_VALUE);
    public static final MTNChargeState DUMMY_57 = new MTNChargeState(DUMMY_57_VALUE);
    public static final MTNChargeState DUMMY_58 = new MTNChargeState(DUMMY_58_VALUE);
    public static final MTNChargeState DUMMY_59 = new MTNChargeState(DUMMY_59_VALUE);
    public static final MTNChargeState DUMMY_60 = new MTNChargeState(DUMMY_60_VALUE);

    
    private Integer type;

	public MTNChargeState() {
    }
    
    public MTNChargeState(Integer type) {
        this.type = type;
    }
    

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
    	if (this == o) return true;
		if (o == null && type == null) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MTNChargeState that = (MTNChargeState) o;
		if (type == null && that.type != null) return false;
        return type.equals(that.type);
    }
    
    
    @Override
    public String toString() {
    	if (type.equals(NOT_ASSIGNED_VALUE))
    		return "آزاد";
    	if (type.equals(ASSIGNED_VALUE))
    		return "فروخته شده";
    	if (type.equals(IN_ASSIGNED_VALUE))
    		return "در حال تخصیص";
    	if (type.equals(LOCKED_VALUE))
    		return "بلوک";
    	if (type.equals(CACHED_VALUE))
    		return "در اختیار سوئیچ";
    	if (type.equals(SOLD_BEFORE_VALUE))
    		return "مشکوک";
    	if (type.equals(SETTLED_SUSPICIOUS_VALUE))
    		return "مشکوک تسویه شده";
    	if (type.equals(UN_SETTLED_SUSPICIOUS_VALUE))
    		return "مشکوک تسویه نشده";
        if (type.equals(DUMMY_50_VALUE))
            return "نامشخص - 50";
        if (type.equals(DUMMY_51_VALUE))
            return "نامشخص - 51";
        if (type.equals(DUMMY_52_VALUE))
            return "نامشخص - 52";
        if (type.equals(DUMMY_53_VALUE))
            return "نامشخص - 53";
        if (type.equals(DUMMY_54_VALUE))
            return "نامشخص - 54";
        if (type.equals(DUMMY_55_VALUE))
            return "نامشخص - 55";
        if (type.equals(DUMMY_56_VALUE))
            return "نامشخص - 56";
        if (type.equals(DUMMY_57_VALUE))
            return "نامشخص - 57";
        if (type.equals(DUMMY_58_VALUE))
            return "نامشخص - 58";
        if (type.equals(DUMMY_59_VALUE))
            return "نامشخص - 59";
        if (type.equals(DUMMY_60_VALUE))
            return "نامشخص - 60";
    	return "ناشناخته";
    }

	@Override
	public int hashCode() {
		return type;
	}

    public int compareTo(Object o) {
        if (this == o) return 0;
        if (o == null && type == null) return 0;
        if (o == null || getClass() != o.getClass()) return -1;
        MTNChargeState that = (MTNChargeState) o;
        if (type == null && that.type != null)
            return -1;
        else if (type != null && that.type == null)
            return 1;
        else
            return type.compareTo(that.type);
    }
}
