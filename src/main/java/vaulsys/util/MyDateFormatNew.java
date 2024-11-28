package vaulsys.util;

import vaulsys.calendar.DayDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MyDateFormatNew {
	private static final ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>();
	private static final ThreadLocal<String> datePattern = new ThreadLocal<String>();
	
	public static void remove(){
		dateFormat.remove();
		datePattern.remove();
	}
	
    public static final String formatNew(String pattern, Date date) {
        if (date == null)
            return "";
        
        SimpleDateFormat simpleDateFormat = dateFormat.get();
        if(simpleDateFormat == null){
        	simpleDateFormat = new SimpleDateFormat(pattern);
        	dateFormat.set(simpleDateFormat);
        	datePattern.set(pattern);
        }else{
        	if(!datePattern.get().equals(pattern)) {
        		simpleDateFormat.applyPattern(pattern);
        		datePattern.set(pattern);
        	}
        }
        	
        return simpleDateFormat.format(date);
    }

	public static final String format(String pattern, Date date) {
        if (date == null)
            return "";
        
        SimpleDateFormat simpleDateFormat = dateFormat.get();
        if(simpleDateFormat == null){
        	simpleDateFormat = new SimpleDateFormat(pattern);
        	dateFormat.set(simpleDateFormat);
        }else{
        	simpleDateFormat.applyPattern(pattern);
        }
        	
        return simpleDateFormat.format(date);
    }

    public static final String format(String pattern, DayDate date) {
        if (date == null)
            return "";

        SimpleDateFormat simpleDateFormat = dateFormat.get();
        if(simpleDateFormat == null){
        	simpleDateFormat = new SimpleDateFormat(pattern);
        	dateFormat.set(simpleDateFormat);
        }else{
        	simpleDateFormat.applyPattern(pattern);
        }
        
        return simpleDateFormat.format(date.toDate());
    }

    public static final Date parseNew(String pattern, String source) throws ParseException{	
		if (source != null && source != "") {
	        SimpleDateFormat simpleDateFormat = dateFormat.get();
	        if(simpleDateFormat == null){
	        	simpleDateFormat = new SimpleDateFormat(pattern);
	        	dateFormat.set(simpleDateFormat);
	        }else{
	        	if(!datePattern.get().equals(pattern)) {
	        		simpleDateFormat.applyPattern(pattern);
	        		datePattern.set(pattern);
	        	}
	        }

			
			Date dt = simpleDateFormat.parse(source);
//			Calendar c = new java.util.GregorianCalendar();
//			c.setTime(dt);
//			if (c.get(Calendar.YEAR) == 1970)
//				c.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
			return dt;
		}
		return null; 
	}

    public static final Date parse(String pattern, String source) throws ParseException{	
		if (source != null && source != "") {
	        SimpleDateFormat simpleDateFormat = dateFormat.get();

	        if(!pattern.startsWith("yy")){
	        	pattern = "yyyy" + pattern;
	        	source = Calendar.getInstance().get(Calendar.YEAR) + source;
	        }
	        

	        if(simpleDateFormat == null){
	        	simpleDateFormat = new SimpleDateFormat(pattern);
	        	dateFormat.set(simpleDateFormat);
	        }else{
	        	simpleDateFormat.applyPattern(pattern);
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

    
    public static final boolean checkSimpleValidity(String pattern, String source){
		int indexOfyyyy = pattern.indexOf("yyyy");
    		int indexOfyy = -1;
		if(indexOfyyyy < 0 )
		 	indexOfyy = pattern.indexOf("yy");
		int indexOfMM = pattern.indexOf("MM");
		int indexOfdd = pattern.indexOf("dd");
		int indexOfHH = pattern.indexOf("HH");
		int indexOfhh = pattern.indexOf("hh");
		int indexOfmm = pattern.indexOf("mm");
		int indexOfss = pattern.indexOf("ss");
		
		boolean valid = false; 
		
		try{
		 	 if (indexOfyyyy != -1)
				valid = checkYearValidity(source.substring(indexOfyyyy, indexOfyyyy+4));
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
		}catch(Exception e){
			return false;
		}
		
		return valid;
	}

	private static boolean checkSecondValidity(String s) throws ParseException {
		int second = Integer.parseInt(s); 
		
		if (second< 0 || second >60)
			throw new ParseException("Invalid Second: "+ second, -1 );
		return true;
	}

	private static boolean checkMinuteValidity(String m) throws ParseException{
		 int minute = Integer.parseInt(m); 
		
		if (minute< 0 || minute > 60)
			throw new ParseException("Invalid Minute: "+ minute, -1); 
		
		return true;
	}

	private static boolean checkHourValidity(String h) throws ParseException{
		int hour = Integer.parseInt(h);

		if (hour < 0 || hour > 24)
			throw new ParseException("Invalid Hour: "+ hour, -1 );

		return true;
	}

	private static boolean checkDayValidity(String d) throws ParseException {
		int day = Integer.parseInt(d);

		if (day < 1 || day > 31)
			throw new ParseException("Invalid Day: "+ day, -1 );
		
		return true;
	}

	private static boolean checkMonthValidity(String m) throws ParseException{
		int month = Integer.parseInt(m);

		if (month < 1 || month > 12)
			throw new ParseException("Invalid Month: "+ month, -1 );

		return true;
	}

	private static boolean checkYearValidity(String substring) {
		return true;
	}

}
