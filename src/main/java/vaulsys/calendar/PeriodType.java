package vaulsys.calendar;

import java.util.ArrayList;
import java.util.List;

public enum PeriodType {
    BEFORE, DURING, AFTER;

    public static List<PeriodType> periodTypes = new ArrayList<PeriodType>(3);

    static {
        periodTypes.add(PeriodType.BEFORE);
        periodTypes.add(PeriodType.DURING);
        periodTypes.add(PeriodType.AFTER);
    }
}
