package vaulsys.clearing.cyclecriteria;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class CycleType implements IEnum {
    
    public static final int PER_MINUTE_VALUE = 1;
    public static final int PER_HOUR_VALUE = 2;
    public static final int PER_DAY_VALUE = 3;
    public static final int PER_WEEK_VALUE = 4;
    public static final int PER_MONTH_VALUE = 5;
    public static final int PER_YEAR_VALUE = 6;

    public static final CycleType PER_MINUTE = new CycleType(PER_MINUTE_VALUE);
    public static final CycleType PER_HOUR = new CycleType(PER_HOUR_VALUE);
    public static final CycleType PER_DAY = new CycleType(PER_DAY_VALUE);
    public static final CycleType PER_WEEK = new CycleType(PER_WEEK_VALUE);
    public static final CycleType PER_MONTH = new CycleType(PER_MONTH_VALUE);
    public static final CycleType PER_YEAR = new CycleType(PER_YEAR_VALUE);

    private Integer type;


    public CycleType() {
    }

    CycleType(int value) {
        type = value;
    }

    public Integer getType() {
        return type;
    }

    public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof CycleType)) return false;
		CycleType that = (CycleType) o;
		if (type == null && that.type != null) return false;
		return type.equals(that.type);
	}

	public int hashCode() {
		return type;
	}
	
	
	public String getFarsiName(String code) {
		type = Integer.valueOf(code);
		switch(type){
		case PER_MINUTE_VALUE:
			return "دقیقه ای";
		case PER_HOUR_VALUE:
			return "ساعتی";
		case PER_DAY_VALUE:
			return "روزانه";
		case PER_WEEK_VALUE:
			return "هفته ای";
		case PER_MONTH_VALUE:
			return "ماهانه";
		case PER_YEAR_VALUE:
			return "سالانه";
		default:
			return "ناشناخته";
		}
	}
	
	public String getEnglishName(String code) {
		type = Integer.valueOf(code);
		switch(type){
		case PER_MINUTE_VALUE:
			return "MINUTELY";
		case PER_HOUR_VALUE:
			return "HOURLY";
		case PER_DAY_VALUE:
			return "DAILY";
		case PER_WEEK_VALUE:
			return "WEEKLY";
		case PER_MONTH_VALUE:
			return "MONTHLY";
		case PER_YEAR_VALUE:
			return "YEARLY";
		default:
			return "UNKNOWN";
		}
	}
}
