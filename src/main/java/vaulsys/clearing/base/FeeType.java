package vaulsys.clearing.base;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class FeeType implements IEnum {

    private static final int DEBIT_FEE_VALUE = 1001;
    private static final int CREDIT_FEE_VALUE = 1002;

    public static final FeeType DEBIT = new FeeType(DEBIT_FEE_VALUE);
    public static final FeeType CREDIT = new FeeType(CREDIT_FEE_VALUE);

    private int type;
    
    public int getType() {
		return type;
	}
    
    public FeeType() {
    }

    public FeeType(int type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + type;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FeeType other = (FeeType) obj;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		switch (type) {
		case CREDIT_FEE_VALUE:
			return "شارژ";
		case DEBIT_FEE_VALUE:
			return "کارمزد";
		default:
			return "";
	}
	}
}
