package vaulsys.calendar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@Embeddable
@org.hibernate.annotations.Entity(mutable = false)
@MappedSuperclass
public class DayDate implements Serializable, Comparable, Cloneable {
	private static final long serialVersionUID = 1L;
	public static final DayDate UNKNOWN = new DayDate(0, 0, 0);
	public static final DayDate MIN_DAY_DATE = new DayDate(0, 0, 0);
	public static final DayDate MAX_DAY_DATE = new DayDate(9999, 12, 30);
	private Integer date;

	public DayDate(Date date) {
		if (date == null) {
			return;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		setYear(calendar.get(Calendar.YEAR));
		setMonth(calendar.get(Calendar.MONTH) + 1);
		setDay(calendar.get(Calendar.DAY_OF_MONTH));
	}

	public Integer getDate() {
		return date;
	}

	public void setDate(Integer date) {
		this.date = date;
	}

	@Transient
	public int getYear() {
		return date == null ? 0 : date / 10000;
	}

	public void setYear(int year) {
		setDate(year, getMonth(), getDay());
	}

	@Transient
	public int getDay() {
		return date == null ? 0 : date % 100;
	}

	public void setDay(int day) {
		setDate(getYear(), getMonth(), day);
	}

	@Transient
	public int getMonth() {
		return date == null ? 0 : ((date % 10000) / 100);
	}

	public void setMonth(int month) {
		setDate(getYear(), month, getDay());
	}

	public DayDate() {
	}

	public DayDate(int year, int month, int day) {
		setDate(year, month, day);
	}

	private void setDate(int year, int month, int day) {
//		Calendar c = Calendar.getInstance();
//    	c.set(Calendar.YEAR, year);
//    	c.set(Calendar.MONTH, month);
//    	c.set(Calendar.DAY_OF_MONTH, day);
//		date = c.get(Calendar.YEAR) * 10000 + c.get(Calendar.MONTH) * 100 + c.get(Calendar.DAY_OF_MONTH);
		date = year * 10000 + month * 100 + day;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || !(o instanceof DayDate))
			return false;

		DayDate dayDate = (DayDate) o;

		if (date != null ? !date.equals(dayDate.date) : dayDate.date != null)
			return false;
		return true;
	}

	public boolean equals(Date date) {
		DayDate other = new DayDate(date);
		return this.equals(other);
	}

	@Override
	public int hashCode() {
		return (date != null ? date.hashCode() : 0);
	}

	@Override
	public int compareTo(Object o) {
		if (o == null)
			return 1;
		DayDate dayDate = (DayDate) o;
		Integer date = getDate() == null ? 0 : getDate();
		Integer otherDate = dayDate.getDate() == null ? 0 : dayDate.getDate();
		return date - otherDate;
	}

	public boolean after(DayDate other) {
		return date - other.getDate() > 0;
	}

	public boolean afterEquals(DayDate other) {
		return date - other.getDate() >= 0;
	}

	public boolean before(DayDate other) {
		return date - other.getDate() < 0;
	}

	public boolean beforeEquals(DayDate other) {
		return date - other.getDate() <= 0;
	}

	public boolean after(Date other) {
		return date - new DayDate(other).getDate() > 0;
	}

	public boolean before(Date other) {
		return date - new DayDate(other).getDate() < 0;
	}

	@Override
	public String toString() {
		int month = getMonth();
		int day = getDay();
		String monthStr = month+"";
		String dayStr = day+"";
		if (month < 10)
			monthStr = "0" + month;
		
		if (day < 10)
			dayStr = "0" + day;
		
		return getYear() + "/" + monthStr + "/" + dayStr;
	}

	public DayDate nextDay() {
		return nextDay(1);
	}

	public DayDate previousDay() {
		return nextDay(-1);
	}

	public DayDate nextWeekDay() {
		return nextDay(7);
	}

	public DayDate previousWeekDay() {
		return nextDay(-7);
	}

	public DayDate nextDay(int days) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, getYear());
		calendar.set(Calendar.MONTH, getMonth() - 1);
		calendar.set(Calendar.DAY_OF_MONTH, getDay());
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		calendar.add(Calendar.DAY_OF_MONTH, days);
		return new DayDate(calendar.getTime());
	}

	/**
	 * return WeekDay of this
	 */
//	public WeekDay weekDay() {
//		PersianCalendar persianCalendar = new PersianCalendar();
//		persianCalendar.setDayDate(this);
//		persianCalendar.get(PersianCalendar.DAY_OF_WEEK);
//		return WeekDay.get(persianCalendar.get(PersianCalendar.DAY_OF_WEEK) % 7);
//	}

//	public DayDate dayInThisWeek(WeekDay weekDay) {
//		return nextDay(weekDay.ordinal() - weekDay().ordinal());
//	}

	public DayDate firstDayOfMonth() {
		return new DayDate(getYear(), getMonth(), 1);
	}

	public DayDate lastDayOfMonth() {
		DayDate firstOfNextMonth;
		firstOfNextMonth = new DayDate(getYear() + getMonth() / 12, getMonth() % 12 + 1, 1);
		return firstOfNextMonth.nextDay(-1);
	}

//	public DayDate nextMonth() {
//		PersianCalendar persianCalendar = new PersianCalendar();
//		persianCalendar.setDayDate(this);
//		persianCalendar.add(Calendar.MONTH, 1);
//		return persianCalendar.getDayDate();
//	}
//
//	public DayDate previousMonth() {
//		PersianCalendar persianCalendar = new PersianCalendar();
//		persianCalendar.setDayDate(this);
//		persianCalendar.add(Calendar.MONTH, -1);
//		return persianCalendar.getDayDate();
//	}

	public List<DayDate> daysOfMonth() {
		DayDate firstDay = firstDayOfMonth();
		DayDate lastDay = lastDayOfMonth();
		List<DayDate> dates = new ArrayList<DayDate>();
		while (!firstDay.equals(lastDay)) {
			dates.add(firstDay);
			firstDay = firstDay.nextDay();
		}
		dates.add(lastDay);
		return dates;
	}

//	public List<List<DayDate>> weeksOfMonth() {
//		List<List<DayDate>> days = new ArrayList<List<DayDate>>();
//		DayDate firstDay = firstDayOfMonth().dayInThisWeek(WeekDay.SATURDAY);
//		DayDate lastDay = lastDayOfMonth().dayInThisWeek(WeekDay.FRIDAY);
//		while (firstDay.compareTo(lastDay) <= 0) {
//			List<DayDate> weekDays = new ArrayList<DayDate>();
//			while (true) {
//				weekDays.add(firstDay);
//				firstDay = firstDay.nextDay();
//				if (firstDay.weekDay().equals(WeekDay.SATURDAY))
//					break;
//			}
//			days.add(weekDays);
//		}
//		return days;
//	}

	@Transient
	public boolean isValid() {
		return getYear() > 1300 && getMonth() > 0 && getMonth() < 13 && getDay() > 0 && getDay() < 32;
	}

	@Override
	public DayDate clone() {
		return new DayDate(getYear(), getMonth(), getDay());
	}

	public static DayDate getUnknownInstance() {
		return UNKNOWN.clone();
	}

	public Date toDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, getYear());
		calendar.set(Calendar.MONTH, getMonth() - 1);
		calendar.set(Calendar.DAY_OF_MONTH, getDay());
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	public static DayDate now() {
		return new DayDate(new Date());
	}

	public static boolean isNullOrUnknown(DayDate dayDate) {
		return dayDate == null || dayDate.equals(UNKNOWN) || dayDate.equals(MAX_DAY_DATE);
	}

	public Long getDateTimeLong() {
		return date * 1000000L;
	}
}

