package vaulsys.customer;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;
import java.lang.reflect.Field;

@Embeddable
public class AccountOwnerType implements IEnum {

    private static final byte DEFAULT_VALUE = 1;
    private static final byte MERCHANT_VALUE = 2;
    private static final byte ISSUER_VALUE = 3;
    private static final byte ACQUIRER_VALUE = 4;
    private static final byte CARD_OWNER_VALUE = 5;
    private static final byte CUSTOMER_VALUE = 6;
    private static final byte SHOP_VALUE = 7;
    private static final byte ORGANIZATION_VALUE = 8;

    public static final AccountOwnerType DEFAULT = new AccountOwnerType(DEFAULT_VALUE);
    public static final AccountOwnerType MERCHANT = new AccountOwnerType(MERCHANT_VALUE);
    public static final AccountOwnerType ISSUER = new AccountOwnerType(ISSUER_VALUE);
    public static final AccountOwnerType ACQUIRER = new AccountOwnerType(ACQUIRER_VALUE);
    public static final AccountOwnerType CARD_OWNER = new AccountOwnerType(CARD_OWNER_VALUE);
    public static final AccountOwnerType CUSTOMER = new AccountOwnerType(CUSTOMER_VALUE);
    public static final AccountOwnerType SHOP = new AccountOwnerType(SHOP_VALUE);
    public static final AccountOwnerType ORGANIZATION = new AccountOwnerType(ORGANIZATION_VALUE);

    private byte type;

    public AccountOwnerType() {
    }

    public AccountOwnerType(byte type) {
        this.type = type;
    }

    public static AccountOwnerType getByName(String s) {
        if (s.equals("MERCHANT"))
            return AccountOwnerType.MERCHANT;

        if (s.equals("ISSUER"))
            return AccountOwnerType.ISSUER;

        if (s.equals("ACQUIRER"))
            return AccountOwnerType.ACQUIRER;

        if (s.equals("CARD_OWNER"))
            return AccountOwnerType.CARD_OWNER;

        if (s.equals("CUSTOMER"))
            return AccountOwnerType.CUSTOMER;

        if (s.equals("SHOP"))
        	return AccountOwnerType.SHOP;

        if (s.equals("ORGANIZATION"))
        	return AccountOwnerType.ORGANIZATION;

        return AccountOwnerType.DEFAULT;

    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountOwnerType type1 = (AccountOwnerType) o;

        if (type != type1.type) return false;

        return true;
    }

    public int hashCode() {
        return (int) type;
    }

	public String toString(){
		for (Field field : this.getClass().getFields()) {
			try {
				if(this.equals(field.get(null)))
					return field.getName();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return "";
	}
}
