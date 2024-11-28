package vaulsys.calendar;

public enum WeekDay {

    SATURDAY, SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, UNKNOWN;

    public static WeekDay get(int index) {
        for (WeekDay day : values()) {
            if (day.ordinal() == index)
                return day;
        }
        return UNKNOWN;
    }

}
