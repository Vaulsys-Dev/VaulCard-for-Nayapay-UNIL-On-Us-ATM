package vaulsys.clearing.base;

import vaulsys.persistence.IEnum;
import javax.persistence.Embeddable;

@Embeddable
public class SettlementRecordType implements IEnum {

    private static final int UNDEFINED_VALUE = 0;
    private static final int SETTLEMENTRECORD_VALUE = 1;
    private static final int ONLYFORFORM1_VALUE = 2;
    private static final int THIRDPARTHY_VALUE = 3;

    public static final SettlementRecordType UNDEFINED = new SettlementRecordType(UNDEFINED_VALUE);
    public static final SettlementRecordType SETTLEMENTRECORD = new SettlementRecordType(SETTLEMENTRECORD_VALUE);
    public static final SettlementRecordType ONLYFORFORM1 = new SettlementRecordType(ONLYFORFORM1_VALUE);
    public static final SettlementRecordType THIRDPARTHY = new SettlementRecordType(THIRDPARTHY_VALUE);

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

    public SettlementRecordType() {
        super();
    }

    public SettlementRecordType(int type){
        super();
        this.type = type;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SettlementRecordType that = (SettlementRecordType) o;

        if (type != that.type) return false;

        return true;
    }

    public int hashCode() {
        return type;
    }

    @Override
    protected Object clone() {
        return new SettlementRecordType(this.type);
    }

    public SettlementRecordType copy() {
        return (SettlementRecordType) clone();
    }

}
