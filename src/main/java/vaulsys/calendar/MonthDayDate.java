package vaulsys.calendar;


import java.util.Calendar;
import java.util.Date;

import javax.persistence.Transient;


//@Embeddable
//@org.hibernate.annotations.Entity(mutable = false)
public class MonthDayDate extends DayDate {


    public static final MonthDayDate UNKNOWN = new MonthDayDate(0, 0, 0);
    public static final MonthDayDate MIN_DAY_DATE = new MonthDayDate(0, 0, 0);
    public static final MonthDayDate MAX_DAY_DATE = new MonthDayDate(9999, 12, 30);

    public MonthDayDate(Date date) {
        if (date == null) {
            return;
        }
        
        Calendar calendar = Calendar.getInstance();
        //        int year = calendar.get(Calendar.YEAR);
        int year = changingYear(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) + 1);
        calendar.setTime(date);
        setYear(year);
        setMonth(calendar.get(Calendar.MONTH) + 1);
        setDay(calendar.get(Calendar.DAY_OF_MONTH));
    }


    public MonthDayDate() {
    }

    public MonthDayDate(int year, int month, int day) {
       //        super(Calendar.getInstance().get(Calendar.YEAR), month, day);
    	super(changingYear(/*Calendar.getInstance().get(Calendar.YEAR)*/year,month), month, day);
    }
    
    public MonthDayDate(MonthDayDate date){
	//    	setYear(Calendar.getInstance().get(Calendar.YEAR));
    	setYear(changingYear(Calendar.getInstance().get(Calendar.YEAR), date.getMonth()));
    	setMonth(date.getMonth());
    	setDay(date.getDay());
    }
    
    public MonthDayDate(DayDate date){
	//    	setYear(Calendar.getInstance().get(Calendar.YEAR));
    	setYear(changingYear(Calendar.getInstance().get(Calendar.YEAR), date.getMonth()));
    	setMonth(date.getMonth());
    	setDay(date.getDay());
    }
    
   private static int changingYear(int year, int month){
    	Calendar calendar = Calendar.getInstance();
    	calendar.get(calendar.DATE);
    	if(calendar.get(Calendar.MONTH) == 11 && month == 1 )
    		return year +1;
    	else if (calendar.get(Calendar.MONTH) == 0 && month==12)
    		return year -1;
    	return /*calendar.get(calendar.YEAR)*/ year;
    }	
    public boolean equals(MonthDayDate other) {
    	if (this == other)
			return true;
    	if (other == null || !(other instanceof MonthDayDate))
			return false;
    	return this.getMonth() == other.getMonth() && this.getDay() == other.getDay();
    }
    
    public boolean equals(Date date) {
        MonthDayDate other = new MonthDayDate(date);
        return this.equals(other);
    }


    @Override
    public String toString() {
        return  (getMonth()<10? "0"+getMonth(): getMonth() )+ "" + (getDay()<10? "0"+ getDay(): getDay());
    }

    @Transient
    public boolean isValid() {
        return getMonth() > 0 && getMonth() < 13 && getDay() > 0 && getDay() < 32;
    }


    @Override
    public MonthDayDate clone() {
        return new MonthDayDate(getYear(), getMonth(), getDay());
    }

    public static MonthDayDate getUnknownInstance() {
        return (MonthDayDate) MonthDayDate.UNKNOWN.clone();
    }
    
    public static MonthDayDate now() {
        return new MonthDayDate(new Date());
    }
    
    @Override
    public MonthDayDate nextDay() {
    	return nextDay(1);
    }
    
    @Override
    public MonthDayDate nextDay(int days) {
    	 Calendar calendar = Calendar.getInstance();
         calendar.set(Calendar.YEAR, getYear());
         calendar.set(Calendar.MONTH, getMonth() - 1);
         calendar.set(Calendar.DAY_OF_MONTH, getDay());
         calendar.set(Calendar.HOUR, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.MILLISECOND, 0);

         calendar.add(Calendar.DAY_OF_MONTH, days);
         return new MonthDayDate(calendar.getTime());
    }
}


