package vaulsys.calendar;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
public class DateRange implements Serializable, Cloneable {

    @Embedded
    private DateTime startTime;
    
    @Embedded
    private DateTime endTime;

    private static final DateRange UNKNOWN = new DateRange(DateTime.UNKNOWN, DateTime.UNKNOWN);
    private static final DateRange INFINIT = new DateRange(DateTime.MIN_DATE_TIME, DateTime.MAX_DATE_TIME);


    public static DateRange unknown() {
        return UNKNOWN.clone();
    }

    public static DateRange infinit() {
        return INFINIT.clone();
    }

    public DateRange() {
    }

    public DateRange(DateTime startTime, DateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public DateRange clone() {
        return new DateRange(startTime, endTime);
    }

    public boolean isEmpty() {
        return startTime.after(endTime);
    }

    public boolean includes(DateTime dateTime) {
        return dateTime.afterEquals(startTime) && dateTime.beforeEquals(endTime);
    }

    public boolean includes(DateRange range) {
        return this.includes(range.startTime) && this.includes(range.endTime);
    }

    public boolean overlaps(DateRange range) {
        return range.includes(startTime) || range.includes(endTime) || this.includes(range);
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }
}
