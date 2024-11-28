package vaulsys.lottery.consts;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class LotteryState implements IEnum{

	private static final Integer NOT_ASSIGNED_VALUE = 0;
    private static final Integer ASSIGNED_VALUE = 1;
//    private static final Integer IN_ASSIGNED_VALUE = 2;
    private static final Integer LOCKED_VALUE = 3;
//    private static final Integer CACHED_VALUE = 4;
    
    
    public static final LotteryState NOT_ASSIGNED = new LotteryState(NOT_ASSIGNED_VALUE); 
    public static final LotteryState ASSIGNED = new LotteryState(ASSIGNED_VALUE); 
//    public static final LotteryState IN_ASSIGNED = new LotteryState(IN_ASSIGNED_VALUE); 
    public static final LotteryState LOCKED = new LotteryState(LOCKED_VALUE); 
//    public static final LotteryState CACHED = new LotteryState(CACHED_VALUE); 
    
    
    private Integer type;

    public LotteryState(){
    }
    
    public LotteryState(Integer type) {
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
        if (o== null && type== null) return true; 
        if (o == null || getClass() != o.getClass()) return false;
        LotteryState that = (LotteryState) o;
        if (type == null && that.type!= null) return false;
        return type.equals(that.type);
    }
    
    
    @Override
    public String toString() {
    	if (type.equals(NOT_ASSIGNED_VALUE))
    		return "آزاد";
    	if (type.equals(ASSIGNED_VALUE))
    		return "فروخته شده";
//    	if (type.equals(IN_ASSIGNED_VALUE))
//    		return "در حال تخصیص";
    	if (type.equals(LOCKED_VALUE))
    		return "بلوک";
//    	if (type.equals(CACHED_VALUE))
//    		return "در اختیار سوئیچ";
    	
    	return "ناشناخته";
    }

	@Override
	public int hashCode() {
		return type;
	}
}
