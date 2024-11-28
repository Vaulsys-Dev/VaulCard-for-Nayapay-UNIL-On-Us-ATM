package vaulsys.migration;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class CardMigrationTransferStatusType implements IEnum{
	
	private static final int UNDEFINED_VALUE = -1;
	private static final int TRANSFER_VALUE = 1;
	private static final int NOT_TRANSFER_VALUE = 2;
	private static final int IN_TRANSFER_VALUE = 3;
	
	public static final CardMigrationTransferStatusType UNDEFINED  =new CardMigrationTransferStatusType(UNDEFINED_VALUE);
	public static final CardMigrationTransferStatusType TRANSFER = new CardMigrationTransferStatusType(TRANSFER_VALUE);
	public static final CardMigrationTransferStatusType NOT_TRANSFER = new CardMigrationTransferStatusType(NOT_TRANSFER_VALUE);
	public static final CardMigrationTransferStatusType IN_TRANSFER = new CardMigrationTransferStatusType(IN_TRANSFER_VALUE);
	
	private int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public CardMigrationTransferStatusType(int type) {
		super();
		this.type = type;
	}

	public CardMigrationTransferStatusType() {
		super();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		CardMigrationTransferStatusType that = (CardMigrationTransferStatusType) obj;
		return type == that.type;
	}

	@Override
	public int hashCode() {
		return type;
	}

}
