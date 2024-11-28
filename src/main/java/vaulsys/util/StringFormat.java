package vaulsys.util;

public class StringFormat {
    /* Constant for left justification. */
    public static final int JUST_LEFT = 'l';

    /* Constant for centering. */
    public static final int JUST_CENTRE = 'c';

    /* Constant for centering, for those who spell "centre" the American way. */
    public static final int JUST_CENTER = JUST_CENTRE;

    /**
     * Constant for right-justified Strings.
     */
    public static final int JUST_RIGHT = 'r';

//    /**
//     * Current justification
//     */
//    private int just;
//
//    /**
//     * Current max length
//     */
//    private int maxChars;
//
//    private StringFormat(int maxCh, int justn) {
//        switch (justn) {
//            case JUST_LEFT:
//            case JUST_CENTRE:
//            case JUST_RIGHT:
//                this.just = justn;
//                break;
//            default:
//                throw new IllegalArgumentException("invalid justification arg.");
//        }
//        if (maxCh < 1) {
//            throw new IllegalArgumentException("maxChars must be positive.");
//        }
//        this.maxChars = maxCh;
//    }
//
//    public StringBuffer format(Object obj, StringBuffer where,
//                               FieldPosition ignore) {
//        return format(obj, where, ignore, ' ');
//    }
//
//    /**
//     * Format a String
//     */
//    public StringBuffer format(Object obj, StringBuffer where,
//                               FieldPosition ignore, char padChar) {
//
//        String s = (obj == null) ? "" : obj.toString();
//        String wanted = s.substring(0, Math.min(s.length(), maxChars));
//
//        // If no space left for justification, return maxChars' worth */
//        if (wanted.length() > maxChars) {
//            where.append(wanted);
//        }
//        // Else get the spaces in the right place.
//        else
//            switch (just) {
//                case JUST_RIGHT:
//                    pad(where, maxChars - wanted.length(), padChar);
//                    where.append(wanted);
//                    break;
//                case JUST_CENTRE:
//                    int startPos = where.length();
//                    pad(where, (maxChars - wanted.length()) / 2, padChar);
//                    where.append(wanted);
//                    pad(where, (maxChars - wanted.length()) / 2, padChar);
//                    // Adjust for "rounding error"
//                    pad(where, maxChars - (where.length() - startPos), padChar);
//                    break;
//                case JUST_LEFT:
//                    where.append(wanted);
//                    pad(where, maxChars - wanted.length(), padChar);
//                    break;
//            }
//        return where;
//    }
//
//    protected final void pad(StringBuffer to, int howMany, char c) {
//        for (int i = 0; i < howMany; i++)
//            to.append(c);
//    }
//
//    /**
//     * Convenience Routine
//     */
//    public String format(String s, char padChar) {
//        return format(s, new StringBuffer(), null, padChar).toString();
//    }
//
//    public String format(Double s, char padChar) {
//        return format(s.toString(), new StringBuffer(), null, padChar).toString();
//    }
//
//    public String format(Long s, char padChar) {
//        return format(s.toString(), new StringBuffer(), null, padChar).toString();
//    }
//
//    /**
//     * Convenience Routine
//     */
//    public String format(String s) {
//        return format(s, new StringBuffer(), null, ' ').toString();
//    }
//
//    /**
//     * ParseObject is required by Format interface, but not useful here.
//     */
//    public Object parseObject(String source, ParsePosition pos) {
//        return source;
//    }
//
    protected static final void pad(StringBuilder to, int howMany, char c) {
        for (int i = 0; i < howMany; i++)
            to.append(c);
    }

    public static String formatNew(final int maxChars, final int just, String str, char padChar) {
		// If no space left for justification, return maxChars' worth */
    	if(str == null){
    		str = "";
    	}
    		
    	if (str.length() >= maxChars) {
			str = str.substring(0, Math.min(str.length(), maxChars));
			return str;
		}
		// Else get the spaces in the right place.
		else{
	    	StringBuilder where = new StringBuilder();

			switch (just) {
			case JUST_RIGHT:
//				pad(where, maxChars - str.length(), padChar);
		        for (int i = 0; i < maxChars - str.length(); i++)
		        	where.append(padChar);
				where.append(str);
				break;
			case JUST_CENTRE:
				int startPos = where.length();
//				pad(where, (maxChars - str.length()) / 2, padChar);
		        for (int i = 0; i < (maxChars - str.length()) / 2; i++)
		        	where.append(padChar);
		        
				where.append(str);
//				pad(where, (maxChars - str.length()) / 2, padChar);
		        for (int i = 0; i < (maxChars - str.length()) / 2; i++)
		        	where.append(padChar);
				// Adjust for "rounding error"
//				pad(where, maxChars - (where.length() - startPos), padChar);
		        for (int i = 0; i < maxChars - (where.length() - startPos); i++)
		        	where.append(padChar);
				break;
			case JUST_LEFT:
				where.append(str);
//				pad(where, maxChars - str.length(), padChar);
		        for (int i = 0; i < maxChars - str.length(); i++)
		        	where.append(padChar);
				break;
			}
			return where.toString();
		}
	}

    public static String formatNew(final int maxChars, final int just, String str) {
    	return StringFormat.formatNew(maxChars, just, str, ' ');
    }
    
    public static String formatNew(final int maxChars, final int just, Object obj, char padChar) {
    	return StringFormat.formatNew(maxChars, just, (obj == null) ? "" : obj.toString(), padChar);
    }
}
