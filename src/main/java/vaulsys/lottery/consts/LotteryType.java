package vaulsys.lottery.consts;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class LotteryType implements IEnum{

	private static final Integer UNDEFINED_VALUE = 0;
    private static final Integer GIFT_CARD_VALUE = 1;
    private static final Integer CHARGE_VALUE = 2;
    
    
    public static final LotteryType UNDEFINED = new LotteryType(UNDEFINED_VALUE); 
    public static final LotteryType GIFT_CARD = new LotteryType(GIFT_CARD_VALUE); 
    public static final LotteryType CHARGE = new LotteryType(CHARGE_VALUE); 
    
    private Integer type;

    public LotteryType(){
    }
    
    public LotteryType(Integer type) {
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
        LotteryType that = (LotteryType) o;
        if (type == null && that.type!= null) return false;
        return type.equals(that.type);
    }
    
    
    @Override
    public String toString() {
    	if (type.equals(GIFT_CARD_VALUE))
    		return "کارت هديه";
    	if (type.equals(CHARGE_VALUE))
    		return "شارژ واريزی";
    	
    	return "ناشناخته";
    }

	@Override
	public int hashCode() {
		return type;
	}
}
