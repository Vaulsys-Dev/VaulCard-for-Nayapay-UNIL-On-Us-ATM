package vaulsys.job;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class SwitchJobGroup implements IEnum {

    private static final String GENERAL_VALUE = "GeneralGroup";
//    private static final String HALF_DAY_SETTLEMENT_VALUE = "HalfDaySettlementGroup";
//    private static final String SETTLEMENT_VALUE = "SettlementGroup";
    private static final String CYCLESETTLEMENT_VALUE = "CycleSettlementGroup";
    private static final String ONLINESETTLEMENT_VALUE = "OnlineSettlementGroup";
    private static final String CYCLEACCOUNT_VALUE = "CycleAccountGroup";
    private static final String EOD_VALUE = "EODGroup";
    private static final String REPEAT_VALUE = "RepeatGroup";
    private static final String REVERSAL_VALUE = "ReversalGroup";
    private static final String ISSUINGDOCUMNET_VALUE = "IssuingDocumnetGroup";
    private static final String MCI_TOPUP_VALUE = "MCITopup";
    //m.rehman: for reporting
    private static final String REPORT_VALUE = "ReportGroup";

    public static final SwitchJobGroup GENERAL = new SwitchJobGroup(GENERAL_VALUE);
//    public static final SwitchJobGroup HALF_DAY_SETTLEMENT = new SwitchJobGroup(HALF_DAY_SETTLEMENT_VALUE);
//    public static final SwitchJobGroup SETTLEMENT = new SwitchJobGroup(SETTLEMENT_VALUE);
    public static final SwitchJobGroup CYCLESETTLEMENT = new SwitchJobGroup(CYCLESETTLEMENT_VALUE);
    public static final SwitchJobGroup ONLINESETTLEMENT = new SwitchJobGroup(ONLINESETTLEMENT_VALUE);
    public static final SwitchJobGroup CYCLEACCOUNT = new SwitchJobGroup(CYCLEACCOUNT_VALUE);
    public static final SwitchJobGroup EOD = new SwitchJobGroup(EOD_VALUE);
    public static final SwitchJobGroup REPEAT = new SwitchJobGroup(REPEAT_VALUE);
    public static final SwitchJobGroup REVERSAL = new SwitchJobGroup(REVERSAL_VALUE);
    public static final SwitchJobGroup ISSUINGDOCUMNET = new SwitchJobGroup(ISSUINGDOCUMNET_VALUE);
    public static final SwitchJobGroup MCI_TOPUP = new SwitchJobGroup(MCI_TOPUP_VALUE);
    //m.rehman: for reporting
    public static final SwitchJobGroup REPORT = new SwitchJobGroup(REPORT_VALUE);

    private String group;

    public SwitchJobGroup() {
    }

    public SwitchJobGroup(String group) {
        this.group = group;
    }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SwitchJobGroup other = (SwitchJobGroup) obj;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		return true;
	}

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		return result;
	}

    @Override
    public String toString(){
    	return this.group;
    }
}
