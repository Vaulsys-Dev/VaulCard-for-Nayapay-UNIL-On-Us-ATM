package vaulsys.clearing.consts;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class FinancialEntityRole implements IEnum {

	private static final byte UNKNOWN_VALUE = -1;
    private static final byte MASTER_VALUE = 1;
    private static final byte SLAVE_VALUE = 2;
    private static final byte MY_SELF_VALUE = 3;
    private static final byte MERCHANT_VALUE = 4;
    private static final byte BRANCH_VALUE = 5;
    private static final byte ORGANIZATION_VALUE = 6;
    private static final byte SHOP_VALUE = 7;
    private static final byte PEER_VALUE = 8;
    private static final byte STAKE_HOLDER_VALUE = 9;
    private static final byte MY_SELF_INTERMEDIATE_VALUE = 10;

    public static final FinancialEntityRole UNKNOWN = new FinancialEntityRole(UNKNOWN_VALUE);
    public static final FinancialEntityRole MASTER = new FinancialEntityRole(MASTER_VALUE);
    public static final FinancialEntityRole SLAVE = new FinancialEntityRole(SLAVE_VALUE);
    public static final FinancialEntityRole MY_SELF = new FinancialEntityRole(MY_SELF_VALUE);
    public static final FinancialEntityRole MERCHANT = new FinancialEntityRole(MERCHANT_VALUE);
    public static final FinancialEntityRole BRANCH = new FinancialEntityRole(BRANCH_VALUE);
    public static final FinancialEntityRole ORGANIZATION = new FinancialEntityRole(ORGANIZATION_VALUE);
    public static final FinancialEntityRole SHOP = new FinancialEntityRole(SHOP_VALUE);
    public static final FinancialEntityRole PEER = new FinancialEntityRole(PEER_VALUE);
    public static final FinancialEntityRole STAKE_HOLDER = new FinancialEntityRole(STAKE_HOLDER_VALUE);
    public static final FinancialEntityRole MY_SELF_INTERMEDIATE = new FinancialEntityRole(MY_SELF_INTERMEDIATE_VALUE);

    private byte type;

    public FinancialEntityRole() {
    }

    public FinancialEntityRole(byte type) {
        this.type = type;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FinancialEntityRole that = (FinancialEntityRole) o;

        if (type != that.type) return false;

        return true;
    }

    public int hashCode() {
        return (int) type;
    }

    public String getName() {
		switch (type) {
            case MERCHANT_VALUE:
            case SHOP_VALUE:
                return "پذيرنده";
            case ORGANIZATION_VALUE:
                return "سازمان";
            case BRANCH_VALUE:
                return "شعبه";
            case STAKE_HOLDER_VALUE:
                return "ذینفع";
            default:
                return "موسسه";
        }
	}
    
    @Override
    public String toString() {
    	switch (type) {
            case MASTER_VALUE:
                return "MASTER";
            case MY_SELF_VALUE:
                return "MY_SELF";
            case MY_SELF_INTERMEDIATE_VALUE:
                return "MY_SELF_INTERMEDIATE";
            case SLAVE_VALUE:
                return "SLAVE";
            case PEER_VALUE:
                return "PEER";
            case MERCHANT_VALUE:
                return "MERCHANT";
            case BRANCH_VALUE:
                return "BRANCH";
            case ORGANIZATION_VALUE:
                return "ORGANIZATION";
            case SHOP_VALUE:
                return "SHOP";
            case STAKE_HOLDER_VALUE:
                return "STACK_HOLDER";
            default:
                return super.toString();
        }
    }
    
}
