package vaulsys.terminal.impl;

import vaulsys.persistence.IEnum;

public class UpdateStatus implements IEnum {
	public static final UpdateStatus NEED_UPDATE = new UpdateStatus((byte)1);
	public static final UpdateStatus UPDATED = new UpdateStatus((byte)2);
	public static final UpdateStatus NO_UPDATE = new UpdateStatus((byte)3);

	private byte status;

	public UpdateStatus() {
	}

	public UpdateStatus(byte status) {
		this.status = status;
	}

	public byte getStatus() {
		return status;
	}

	@Override
	public int hashCode() {
		return status;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || !(obj instanceof UpdateStatus))
			return false;
		UpdateStatus other = (UpdateStatus) obj;
		if (status != other.status)
			return false;
		return true;
	}

	@Override
	public String toString() {
		switch (status) {
			case 1:
				return "NEED_UPDATE";
			case 2:
				return "UPDATED";
			case 3:
				return "NO_UPDATE";
		}
		return "?";
	}
}
