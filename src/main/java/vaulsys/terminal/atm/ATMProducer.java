package vaulsys.terminal.atm;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class ATMProducer implements IEnum {

    private static final Integer WINCORE_VALUE = 1;
    private static final Integer GRG_VALUE = 2;
    private static final Integer HYOSUNG_VALUE = 3;
    private static final Integer HATEF_VALUE = 4;
    private static final Integer NCR_VALUE = 5;
    private static final Integer EASTCOM_VALUE = 6;


    public static final ATMProducer WINCORE = new ATMProducer(WINCORE_VALUE);
    public static final ATMProducer GRG = new ATMProducer(GRG_VALUE);
    public static final ATMProducer HYOSUNG = new ATMProducer(HYOSUNG_VALUE);
    public static final ATMProducer HATEF = new ATMProducer(HATEF_VALUE);
    public static final ATMProducer NCR = new ATMProducer(NCR_VALUE);
    public static final ATMProducer EASTCOM = new ATMProducer(EASTCOM_VALUE);

    private Integer type;

    public ATMProducer() {
    }

    public ATMProducer(Integer type) {
        this.type = type;
    }

    @Override
    public String toString() {
        if (type == 1)
            return "WINCORE";

        if (type == 2)
            return "GRG";

        if (type == 3)
            return "HYOSUNG";

        if (type == 4)
            return "HATEF";

        if (type == 5)
            return "NCR";

    	if (type == 6)
    		return "EASTCOM";

    	return "UNKNOWN";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ATMProducer)) return false;

        ATMProducer that = (ATMProducer) o;

        return type.equals(that.type);
    }

    public int hashCode() {
        return type;
    }
}
