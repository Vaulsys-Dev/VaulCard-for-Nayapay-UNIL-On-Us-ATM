package vaulsys.calendar;



import java.util.Calendar;
import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

@Embeddable
@org.hibernate.annotations.Entity(mutable = false)
public class YearMonthDate extends DayDate {


    public static final YearMonthDate UNKNOWN = new YearMonthDate(0, 0, 0);
    public static final YearMonthDate MIN_DAY_DATE = new YearMonthDate(0, 0, 0);
    public static final YearMonthDate MAX_DAY_DATE = new YearMonthDate(9999, 12, 30);

    public YearMonthDate(Date date) {
        if (date == null) {
            return;
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        setYear(calendar.get(Calendar.YEAR));
        setMonth(calendar.get(Calendar.MONTH) + 1);
        setDay(calendar.get(Calendar.DAY_OF_MONTH));
    }

  

    public YearMonthDate() {
    }

    public YearMonthDate(int year, int month, int day) {
        super(year, month, 1);
    }
    
    public boolean equals(Date date) {
        YearMonthDate other = new YearMonthDate(date);
        return this.equals(other);
    }


    @Override
    public String toString() {
        int year = getYear()%100;
		return ((year+"").length()<2 ? "0"+ year : year)+""+ getMonth();
    }

    @Transient
    public boolean isValid() {
        return getYear()>1300 && getMonth() > 0 && getMonth() < 13 ;
    }


    @Override
    public YearMonthDate clone() {
        return new YearMonthDate(getYear(), getMonth(), getDay());
    }


    public static YearMonthDate getUnknownInstance() {
        return (YearMonthDate) YearMonthDate.UNKNOWN.clone();
    }

}

