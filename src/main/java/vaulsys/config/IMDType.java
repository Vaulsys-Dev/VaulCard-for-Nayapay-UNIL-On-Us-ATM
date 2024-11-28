package vaulsys.config;

import vaulsys.persistence.IEnum;
import javax.persistence.Embeddable;

/**
 * Created by Asim Shahzad on 4/19/2016.
 */

@Embeddable
public class IMDType implements IEnum {

    private static final int LOCAL_VALUE = 1;
    private static final int FOREIGN_VALUE = 2;
    private static final int WALLET_VALUE = 3;

    public static final IMDType Local = new IMDType(LOCAL_VALUE);
    public static final IMDType Foreign = new IMDType(FOREIGN_VALUE);
    public static final IMDType Wallet = new IMDType(WALLET_VALUE);

    private int IMD_TYPE;

    public IMDType() {
    }

    public IMDType(int IMDType) {
        this.IMD_TYPE = IMDType;
    }

    public int getType() {
        return IMD_TYPE;
    }

    public void setType(int Type)
    {
        this.IMD_TYPE = Type;
    }

    public void setType(Integer Type)
    {
        this.IMD_TYPE = Type;
    }

    public int hashCode() {
        return IMD_TYPE;
    }

    public boolean equals(Object obj) {
        if(obj != null && obj instanceof IMDType) {
            IMDType that = (IMDType)obj;
            return this.IMD_TYPE == that.IMD_TYPE;
        } else {
            return false;
        }
    }

    public String toString() {
        switch(IMD_TYPE) {
            case LOCAL_VALUE:
                return "Local";
            case FOREIGN_VALUE:
                return "Foreign";
            default:
                return "";
        }
    }
}
