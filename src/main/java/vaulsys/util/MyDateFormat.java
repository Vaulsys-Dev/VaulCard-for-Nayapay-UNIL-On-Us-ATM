package vaulsys.util;

import vaulsys.calendar.DayDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MyDateFormat {
    private SimpleDateFormat simpleDateFormat;

    public MyDateFormat(String format) {
        super();
        simpleDateFormat = new SimpleDateFormat(format);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Calendar.getInstance().getTime());
        simpleDateFormat.setCalendar(calendar);
    }

    public final String format(Date date) {
        if (date == null)
            return "";
        return simpleDateFormat.format(date);
    }

    public final String format(DayDate date) {
        if (date == null)
            return "";
        return simpleDateFormat.format(date.toDate());
    }

    public Date parse(String source) throws ParseException {
        return parse(source, false); 
    }

    
    public Date parse(String source, boolean checkValidity) throws ParseException{	
		if (source != null && source != "") {
			if (checkValidity) {
				String pattern = simpleDateFormat.toPattern();
				try {
					checkSimpleValidity(source, pattern);
				} catch (ParseException e) {
					throw new ParseException("Invalid Date:" + source, -1);
				}
			}
			Date dt = simpleDateFormat.parse(source);
			Calendar c = new java.util.GregorianCalendar();
			c.setTime(dt);
			if (c.get(Calendar.YEAR) == 1970)
				c.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
			return c.getTime();
		}
		return null; 
	}

	private boolean checkSimpleValidity(String source, String pattern) throws ParseException {
		int indexOfyy = pattern.indexOf("yy");
		int indexOfMM = pattern.indexOf("MM");
		int indexOfdd = pattern.indexOf("dd");
		int indexOfHH = pattern.indexOf("HH");
		int indexOfhh = pattern.indexOf("hh");
		int indexOfmm = pattern.indexOf("mm");
		int indexOfss = pattern.indexOf("ss");
		
		boolean valid = false; 
		
		if (indexOfyy != -1 )
			valid = checkYearValidity(source.substring(indexOfyy, indexOfyy+2));
		if (indexOfMM != -1)
			valid = checkMonthValidity(source.substring(indexOfMM, indexOfMM+2));
		if (indexOfdd != -1)
			valid = checkDayValidity(source.substring(indexOfdd, indexOfdd+2));
		if (indexOfHH != -1)
			valid = checkHourValidity(source.substring(indexOfHH, indexOfHH+2));
		if (indexOfhh != -1)
			valid = checkHourValidity(source.substring(indexOfhh, indexOfhh+2));
		if (indexOfmm != -1)
			valid = checkMinuteValidity(source.substring(indexOfmm, indexOfmm+2));
		if (indexOfss != -1)
			valid = checkSecondValidity(source.substring(indexOfss, indexOfss+2));
		
		return valid;
	}

	private boolean checkSecondValidity(String s) throws ParseException {
		int second = Integer.parseInt(s); 
		
		if (second< 0 || second >60)
			throw new ParseException("Invalid Second: "+ second, -1 );
		return true;
	}

	private boolean checkMinuteValidity(String m) throws ParseException{
		 int minute = Integer.parseInt(m); 
		
		if (minute< 0 || minute > 60)
			throw new ParseException("Invalid Minute: "+ minute, -1); 
		
		return true;
	}

	private boolean checkHourValidity(String h) throws ParseException{
		int hour = Integer.parseInt(h);

		if (hour < 0 || hour > 24)
			throw new ParseException("Invalid Hour: "+ hour, -1 );

		return true;
	}

	private boolean checkDayValidity(String d) throws ParseException {
		int day = Integer.parseInt(d);

		if (day < 1 || day > 31)
			throw new ParseException("Invalid Day: "+ day, -1 );
		
		return true;
	}

	private boolean checkMonthValidity(String m) throws ParseException{
		int month = Integer.parseInt(m);

		if (month < 1 || month > 12)
			throw new ParseException("Invalid Month: "+ month, -1 );

		return true;
	}

	private boolean checkYearValidity(String substring) {
		return true;
	}
}
