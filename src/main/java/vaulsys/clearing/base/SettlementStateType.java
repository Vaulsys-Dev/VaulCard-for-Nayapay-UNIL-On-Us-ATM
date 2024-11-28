package vaulsys.clearing.base;

import java.util.ArrayList;
import java.util.List;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class SettlementStateType implements IEnum {

	private static final int UNDEFINED_VALUE = -1;
//	private static final byte NOT_SETTLED_VALUE = 1;
	private static final int FILECREATED_VALUE = 0;
//  private static final byte SENT_FOR_SETTLEMENT_VALUE = 2;
	private static final int SETTLED_VALUE = 1;
//  private static final byte SETTLED_VALUE = 3;
	private static final int AUTOSETTLED_VALUE = 2;
//  private static final byte RETURNED_VALUE = 4;

	public static final SettlementStateType UNDEFINED = new SettlementStateType(UNDEFINED_VALUE);
//	public static final SettledState NOT_SETTLED = new SettledState(NOT_SETTLED_VALUE);
	public static final SettlementStateType FILECREATED = new SettlementStateType(FILECREATED_VALUE);
//	public static final SettledState SENT_FOR_SETTLEMENT = new SettledState(SENT_FOR_SETTLEMENT_VALUE);
	public static final SettlementStateType SETTLED = new SettlementStateType(SETTLED_VALUE);
//	public static final SettledState SETTLED = new SettledState(SETTLED_VALUE);
	public static final SettlementStateType AUTOSETTLED = new SettlementStateType(AUTOSETTLED_VALUE);
//	public static final SettledState RETURNED = new SettledState(RETURNED_VALUE);

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
	
	public SettlementStateType() {
		super();
	}
	
	public SettlementStateType(int type){
		super();
		this.type = type;
	}

	public static List<SettlementStateType> getAllStates() {
		List<SettlementStateType> result = new ArrayList<SettlementStateType>();
		result.add(FILECREATED);
		result.add(SETTLED);
		result.add(AUTOSETTLED);
		return result;
	}
	
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SettlementStateType that = (SettlementStateType) o;

        if (type != that.type) return false;

        return true;
    }

    public int hashCode() {
        return type;
    }

    @Override
	protected Object clone() {
		return new SettlementStateType(this.type); 
	}
	
	public SettlementStateType copy() {
		return (SettlementStateType) clone();
	}

}
