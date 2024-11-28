package vaulsys.calendar;

import java.util.Calendar;
import java.util.Date;

import com.ghasemkiani.util.DateFields;
import com.ghasemkiani.util.SimplePersianCalendar;

public class PersianCalendar extends SimplePersianCalendar {
	/**
	 * Constant for Farvardin, the 1st month of the Persian year.
	 */
	public static final int FARVARDIN = 1;
	/**
	 * Constant for Ordibehesht, the 2nd month of the Persian year.
	 */
	public static final int ORDIBEHESHT = 2;
	/**
	 * Constant for Khordad, the 3rd month of the Persian year.
	 */
	public static final int KHORDAD = 3;
	/**
	 * Constant for Tir, the 4th month of the Persian year.
	 */
	public static final int TIR = 4;
	/**
	 * Constant for Mordad, the 5th month of the Persian year.
	 */
	public static final int MORDAD = 5;
	/**
	 * Constant for Shahrivar, the 6th month of the Persian year.
	 */
	public static final int SHAHRIVAR = 6;
	/**
	 * Constant for Mehr, the 7th month of the Persian year.
	 */
	public static final int MEHR = 7;
	/**
	 * Constant for Aban, the 8th month of the Persian year.
	 */
	public static final int ABAN = 8;
	/**
	 * Constant for Azar, the 9th month of the Persian year.
	 */
	public static final int AZAR = 9;
	/**
	 * Constant for Dey, the 10th month of the Persian year.
	 */
	public static final int DEY = 10;
	/**
	 * Constant for Bahman, the 11th month of the Persian year.
	 */
	public static final int BAHMAN = 11;
	/**
	 * Constant for Esfand, the 12th month of the Persian year.
	 */
	public static final int ESFAND = 12;

	/**
	 * Sets the date of this calendar object to the specified
	 * Persian date (year, month, and day fields)
	 *
	 * @param year  the Persian year.
	 * @param month the Persian month (1-based).
	 * @param day   the Persian day of month.
	 * @since 1.0
	 */
	public void setDayDate(int year, int month, int day) {
		setDayDate(new DayDate(year, month, day));
	}

	/**
	 * Sets the date of this calendar object to the specified
	 * Persian date fields
	 *
	 * @param dateFields the Persian date fields.
	 * @since 1.0
	 */
	public void setDayDate(DayDate dateFields) {
		setDateFields(new DateFields(dateFields.getYear(), dateFields.getMonth()/* - 1*/, dateFields.getDay()));
	}

	/**
	 * Retrieves the date of this calendar object as the
	 * Persian date fields
	 *
	 * @return the date of this calendar as Persian date fields.
	 * @since 1.0
	 */
	public DayDate getDayDate() {
		DateFields dateFields = getDateFields();
		return new DayDate(dateFields.getYear(), dateFields.getMonth(), dateFields.getDay());
	}

	/**
	 * Persian month names.
	 */
	public static final String[] persianMonths =
			{
					"",
					"\u0641\u0631\u0648\u0631\u062f\u06cc\u0646",			 // Farvardin
					"\u0627\u0631\u062f\u06cc\u0628\u0647\u0634\u062a", // Ordibehesht
					"\u062e\u0631\u062f\u0627\u062f",						 // Khordad
					"\u062a\u06cc\u0631",									 // Tir
					"\u0645\u0631\u062f\u0627\u062f",						 // Mordad
					"\u0634\u0647\u0631\u06cc\u0648\u0631",				   // Shahrivar
					"\u0645\u0647\u0631",									 // Mehr
					"\u0622\u0628\u0627\u0646",							   // Aban
					"\u0622\u0630\u0631",									 // Azar
					"\u062f\u06cc",										   // Dey
					"\u0628\u0647\u0645\u0646",							   // Bahman
					"\u0627\u0633\u0641\u0646\u062f"						  // Esfand
			};

	/**
	 * Persian week day names.
	 */
	public static final String[] persianWeekDays =
			{
					"\u0634\u0646\u0628\u0647",						 // shanbeh
					"\u06cc\u06a9\u200c\u0634\u0646\u0628\u0647",	   // yek-shanbeh
					"\u062f\u0648\u0634\u0646\u0628\u0647",			 // do-shanbeh
					"\u0633\u0647\u200c\u0634\u0646\u0628\u0647",	   // seh-shanbeh
					"\u0686\u0647\u0627\u0631\u0634\u0646\u0628\u0647", // chahar-shanbeh
					"\u067e\u0646\u062c\u200c\u0634\u0646\u0628\u0647", // panj-shanbeh
					"\u062c\u0645\u0639\u0647"						  // jom'eh
			};

	/**
	 * Gives the name of the specified Persian month.
	 *
	 * @param month the Persian month (zero-based).
	 * @return the name of the specified Persian month in Persian.
	 * @since 1.1
	 */
	public static String getPersianMonthName(int month) {
		return persianMonths[month];
	}

	/**
	 * Gives the Persian name of the specified day of week.
	 *
	 * @param weekDay the day of week (use symbolic constants in the <code>java.util.Calendar</code> class).
	 * @return the name of the specified day of week in Persian.
	 * @since 1.1
	 */
	public static String getPersianWeekDayName(int weekDay) {
		switch (weekDay) {
			case SATURDAY:
				return persianWeekDays[0];
			case SUNDAY:
				return persianWeekDays[1];
			case MONDAY:
				return persianWeekDays[2];
			case TUESDAY:
				return persianWeekDays[3];
			case WEDNESDAY:
				return persianWeekDays[4];
			case THURSDAY:
				return persianWeekDays[5];
			case FRIDAY:
				return persianWeekDays[6];
		}
		return "";
	}

	public static String getPersianWeekDayName(WeekDay weekDay) {
		switch (weekDay) {
			case SATURDAY:
				return persianWeekDays[0];
			case SUNDAY:
				return persianWeekDays[1];
			case MONDAY:
				return persianWeekDays[2];
			case TUESDAY:
				return persianWeekDays[3];
			case WEDNESDAY:
				return persianWeekDays[4];
			case THURSDAY:
				return persianWeekDays[5];
			case FRIDAY:
				return persianWeekDays[6];
		}
		return "";
	}

	/**
	 * Gives the Persian name of the current day of the week for this
	 * calendar's date.
	 *
	 * @return the name of the current day of week for this calendar's date in Persian.
	 */
	public static DayDate getPersianDayDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		SimplePersianCalendar simplePersianCalendar = new SimplePersianCalendar();
		simplePersianCalendar.set(Calendar.YEAR, cal.get(Calendar.YEAR));
		simplePersianCalendar.set(Calendar.MONTH, cal.get(Calendar.MONTH));
		simplePersianCalendar.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
		simplePersianCalendar.set(Calendar.HOUR_OF_DAY, 12);
		simplePersianCalendar.set(Calendar.MINUTE, 0);
		DateFields dateFields = simplePersianCalendar.getDateFields();
		return new DayDate(dateFields.getYear(), dateFields.getMonth() + 1, dateFields.getDay());
	}

	public static Date getGregorianDate(DayDate dayDate) {
		SimplePersianCalendar simplePersianCalendar = new SimplePersianCalendar();
		simplePersianCalendar.setDateFields(dayDate.getYear(), dayDate.getMonth() - 1, dayDate.getDay());
		return simplePersianCalendar.getTime();
	}

	public static DayDate today() {
		return getPersianDayDate(new Date());
	}

	public static DateTime toGregorian(DateTime persian) {
		DayDate dayDate = persian.getDayDate();
		Date gregorianDate = PersianCalendar.getGregorianDate(dayDate);
		return new DateTime(new DayDate(gregorianDate), persian.getDayTime());
	}
}
