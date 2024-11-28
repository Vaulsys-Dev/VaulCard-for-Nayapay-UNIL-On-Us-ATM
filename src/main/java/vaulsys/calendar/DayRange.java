package vaulsys.calendar;

import javax.persistence.*;
import java.io.Serializable;

@Embeddable
public class DayRange implements Serializable  {

    @Embedded
    private DayDate startDate;
    @Embedded
    private DayDate endDate;

    public DayRange() {
        startDate = DayDate.MIN_DAY_DATE;
        endDate = DayDate.MAX_DAY_DATE;
    }

    public DayRange(DayDate startDate, DayDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean isEmpty() {
        return startDate.after(endDate);
    }

    public boolean includes(DayDate dayDate) {
        return dayDate.afterEquals(startDate) && dayDate.beforeEquals(endDate);
    }
    
    public DayDate getStartDate() {
        return startDate;
    }

    public void setStartDate(DayDate startDate) {
        this.startDate = startDate;
    }

    public DayDate getEndDate() {
        return endDate;
    }

    public void setEndDate(DayDate endDate) {
        this.endDate = endDate;
    }
}
