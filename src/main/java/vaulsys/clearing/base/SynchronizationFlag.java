package vaulsys.clearing.base;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class SynchronizationFlag implements IEnum {

	final private static int UNDEFINED_VALUE = -1;
	final private static int Free_VALUE = 0;
	final private static int LOCK_VALUE = 1;

	final public static SynchronizationFlag UNDEFINED = new SynchronizationFlag(UNDEFINED_VALUE);
	final public static SynchronizationFlag Free = new SynchronizationFlag(Free_VALUE);
	final public static SynchronizationFlag LOCK = new SynchronizationFlag(LOCK_VALUE);

	private int value;

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public SynchronizationFlag() {
		super();
	}

	public SynchronizationFlag(int value) {
		super();
		this.value = value;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		SynchronizationFlag that = (SynchronizationFlag) o;

		if (value != that.value)
			return false;

		return true;
	}

	public int hashCode() {
		return value;
	}
}
