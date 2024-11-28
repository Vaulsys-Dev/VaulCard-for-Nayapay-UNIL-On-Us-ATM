package vaulsys.protocols.ndc.constants;

import javax.persistence.Embeddable;

import vaulsys.persistence.IEnum;

//TASK Task019 : Receipt Option
//AldComment Task019 : Add in 92.05.02

@Embeddable
public class ReceiptOptionType implements IEnum{
    private static final int UNKNOWN_VALUE = -1;
    private static final int WITH_RECEIPT_VALUE = 1;
    private static final int WITHOUT_RECEIPT_VALUE = 2;
    
    public static final ReceiptOptionType UNKNOWN = new ReceiptOptionType(UNKNOWN_VALUE);
    public static final ReceiptOptionType WITH_RECEIPT = new ReceiptOptionType(WITH_RECEIPT_VALUE);
    public static final ReceiptOptionType WITHOUT_RECEIPT = new ReceiptOptionType(WITHOUT_RECEIPT_VALUE);
    
    private int type;
    
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setType(Integer type) {
		this.type = type;
	}    

    public ReceiptOptionType() {
    }

    public ReceiptOptionType(int type) {
        this.type = type;
    }   
    
    public String getName() {
    	if (type == WITH_RECEIPT_VALUE)
    		return "چاپ رسید";
    	if (type == WITHOUT_RECEIPT_VALUE)
    		return "عدم چاپ رسید";
    	
    	return "UNKNOWN";
    }
    
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReceiptOptionType that = (ReceiptOptionType) o;

        if (type != that.type) return false;

        return true;
    }


	public int hashCode() {
        return (int) type;
    }
	
	public int getState() {
		return type;
	}
	
	public static ReceiptOptionType getByCode(int value){
		if (value == WITH_RECEIPT_VALUE)
    		return WITH_RECEIPT;
    	if (value == WITHOUT_RECEIPT_VALUE)
    		return WITHOUT_RECEIPT;

    	
    	return UNKNOWN;
	}
	
	@Override
	public String toString() {
		return type+"";
	}    
}
