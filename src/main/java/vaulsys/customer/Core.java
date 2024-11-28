package vaulsys.customer;

import java.util.ArrayList;
import java.util.List;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class Core implements IEnum {

	private static final int FANAP_CORE_VALUE = 0;
	private static final int Negin_CORE_VALUE = 1;
	private static final int Saderat_CORE_VALUE = 2;

	public static final Core FANAP_CORE = new Core(FANAP_CORE_VALUE);
	public static final Core NEGIN_CORE = new Core(Negin_CORE_VALUE);
	public static final Core Saderat_CORE = new Core(Saderat_CORE_VALUE);

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

	public Core() {
		super();
	}

	public Core(int type) {
		super();
		this.type = type;
	}

	public static List<Core> getAll() {
		List<Core> result = new ArrayList<Core>();
		result.add(FANAP_CORE);
		result.add(NEGIN_CORE);
		result.add(Saderat_CORE);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Core that = (Core) o;

		if (type != that.type) return false;

		return true;
	}

	public int hashCode() {
		return type;
	}

	@Override
	protected Object clone() {
		return new Core(this.type);
	}

	@Override
	public String toString() {
		switch (type) {
			case FANAP_CORE_VALUE:
				return "فناپ";
			case Negin_CORE_VALUE:
				return "نگين";
			case Saderat_CORE_VALUE:
				return "صادرات";
			default:
				return "";
		}
	}

	public Core copy() {
		return (Core) clone();
	}
}
