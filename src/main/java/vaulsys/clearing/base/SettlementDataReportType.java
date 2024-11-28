package vaulsys.clearing.base;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class SettlementDataReportType implements IEnum {
	private static final int UNDEFINED_VALUE = -1;
	private static final int MAIN_REPORT_VALUE = 0;
	private static final int THIRDPARTY_REPORT_VALUE = 1;

	public static final SettlementDataReportType UNDEFINED = new SettlementDataReportType(UNDEFINED_VALUE);
	public static final SettlementDataReportType MAIN_REPORT = new SettlementDataReportType(MAIN_REPORT_VALUE);
	public static final SettlementDataReportType THIRDPARTY_REPORT = new SettlementDataReportType(
			THIRDPARTY_REPORT_VALUE);

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

	public SettlementDataReportType() {
		super();
	}

	public SettlementDataReportType(int type) {
		super();
		this.type = type;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		SettlementDataReportType that = (SettlementDataReportType) o;

		if (type != that.type)
			return false;

		return true;
	}

	public int hashCode() {
		return type;
	}

	@Override
	protected Object clone() {
		return new SettlementDataReportType(this.type);
	}

	public SettlementDataReportType copy() {
		return (SettlementDataReportType) clone();
	}

}
