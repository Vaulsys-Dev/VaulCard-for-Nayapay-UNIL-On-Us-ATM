package vaulsys.customer;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;
import java.lang.reflect.Field;

@Embeddable
public class AccountType implements IEnum {

    private static final int DEFAULT_VALUE = 1;
    private static final int TOPIC_VALUE = 2;
    private static final int ACCOUNT_VALUE = 3;
    private static final int DEPOSIT_VALUE = 4;

    public static final AccountType DEFAULT = new AccountType(DEFAULT_VALUE);
    public static final AccountType TOPIC = new AccountType(TOPIC_VALUE);
    public static final AccountType ACCOUNT = new AccountType(ACCOUNT_VALUE);
    public static final AccountType DEPOSIT = new AccountType(DEPOSIT_VALUE);

    private int type;

    public AccountType() {
    }

    public AccountType(int type) {
        this.type = type;
    }

    public int getType() {
		return type;
	}
    
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountType type1 = (AccountType) o;

        if (type != type1.type) return false;

        return true;
    }

    public int hashCode() {
        return type;
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
