package vaulsys.util;

import com.fanap.cms.exception.BusinessException;
import com.ghasemkiani.util.icu.PersianCalendar;
import com.ghasemkiani.util.icu.PersianDateFormat;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.clearing.cyclecriteria.CycleCriteria;
import vaulsys.clearing.cyclecriteria.CycleCriteriaConsts;
import vaulsys.clearing.cyclecriteria.CycleType;
import vaulsys.cms.base.CMSCardSequenceTracker;
import vaulsys.contact.Contact;
import vaulsys.message.SwitchRestarter;
import vaulsys.util.coreLoadBalancer.CoreLoadBalancer;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.GlobalContext;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

	transient static Logger logger = Logger.getLogger(Util.class);
	
    public static String byteToHex(byte[] str) {
        return new String(Hex.encode(str)).toUpperCase();
    }

    public static String trimLeftZeros(String s) {
        if(!hasText(s)) return "";
        try{
        return new Long(Long.parseLong(s.trim())).toString();
        }catch (Exception e){}
        return ""; //Raza Adding from TPSP start
    }
    
    public static String trimLeftZerosDouble(String s) {
        if(!hasText(s)) return "";
        
        try{
        	return new Double(Double.parseDouble(s.trim())).toString();
        }catch (Exception e){} //Raza Adding from TPSP end
        return "";
    }

    public static boolean[] byteToBits(byte b) {
        boolean[] bits = new boolean[8];
        for (int i = 0; i < bits.length; i++) {
            bits[i] = ((b & (1 << (7 - i))) != 0);
        }
        return bits;
    }

    public static byte bitsToByte(boolean[] bits) {
        return bitsToByte(bits, 0);
    }

    public static byte bitsToByte(boolean[] bits, int offset) {
        int value = 0;
        for (int i = 0; i < 8; i++) {
            if (bits[i] == true) {
                value = value | (1 << i);
            }
        }
        return (byte) value;
    }

    public static boolean[] bytesToBits(byte[] b) {
        boolean[] bits = new boolean[8 * b.length + 1];
        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < 8; j++) {
                bits[i * 8 + j + 1] = ((b[i] & (1 << (7 - j))) != 0);
            }
        }
        return bits;
    }
    
    public static boolean isAccount(String st){
    	int count=0;
    	if(st != null && Util.hasText(st)){
	    	int  len = st.length();
	    	for(int i=0 ; i<len && Util.hasText(st); i++){
	    		if(st.contains(".")){
	    			count++;
	    			st = st.substring(st.indexOf(".")+1);
	    			if(st.startsWith("."))
	    				return false;
	    		}
	    	}
    	}
    	if(count == 3 )
    		return true;
    	return false;
    }
    public static String getCoreUrl() throws BusinessException { //Raza Adding from TPSP
        // return CoreLoadBalancer.getCoreURL();
 /*
     	if (indexCoreUrl > coreUrl.size())
     		indexCoreUrl = 1L;
     	return coreUrl.get(indexCoreUrl++);
 */
         String coreUrl = CoreLoadBalancer.getCoreURL();
         logger.debug("Core Url is: " + coreUrl);
         if(!Util.hasText(coreUrl)) {
         	logger.error("Could not access to any core server");
         	PersianDateFormat dateFormatPers = new PersianDateFormat("yyyy/MM/dd HH:mm:ss");
         	String smsText = "Switch: All Core is down!" + "\r\n" + "Time: " + dateFormatPers.format(DateTime.now().toDate());
         	
         	for (Contact coreDownCheckingSMSMobileNo : GlobalContext.getInstance().getAllCoreDownContact()) {
               	if (coreDownCheckingSMSMobileNo != null && coreDownCheckingSMSMobileNo.getPhoneNumber() != null && (coreDownCheckingSMSMobileNo.getPhoneNumber().toString()).length()> 0) {
               		SwitchRestarter.sendSMS(""+coreDownCheckingSMSMobileNo.getPhoneNumber(), smsText);
               	}
              }
         	
         	throw new BusinessException();
         } else {
         	return coreUrl;
         	}
         
     }
    /**
     * Converts a binary representation of a Bitmap field into a Java BitSet
     *
     * @param b                    -
     *                             binary representation
     * @param offset               -
     *                             staring offset
     * @param bitZeroMeansExtended -
     *                             true for ISO-8583
     * @return java BitSet object
     */
    public static BitSet byte2BitSet(byte[] b, int offset, boolean bitZeroMeansExtended) {
        int len = bitZeroMeansExtended ? ((b[offset] & 0x80) == 0x80 ? 128 : 64) : 64;
        BitSet bmap = new BitSet(len);
        for (int i = 0; i < len; i++)
            if (((b[offset + (i >> 3)]) & (0x80 >> (i % 8))) > 0)
                bmap.set(i + 1);
        return bmap;
    }

    /**
     * Converts a binary representation of a Bitmap field into a Java BitSet
     *
     * @param b       -
     *                binary representation
     * @param offset  -
     *                staring offset
     * @param maxBits -
     *                max number of bits (supports 64,128 or 192)
     * @return java BitSet object
     */
    public static BitSet byte2BitSet(byte[] b, int offset, int maxBits) {
        int len = maxBits > 64 ? ((b[offset] & 0x80) == 0x80 ? 128 : 64) : 64;

        if (maxBits > 128 && b.length > offset + 8 && (b[offset + 8] & 0x80) == 0x80) {
            len = 192;
        }
        BitSet bmap = new BitSet(len);
        for (int i = 0; i < len; i++)
            if (((b[offset + (i >> 3)]) & (0x80 >> (i % 8))) > 0)
                bmap.set(i + 1);
        return bmap;
    }

    /**
     * Converts a binary representation of a Bitmap field into a Java BitSet
     *
     * @param bmap      -
     *                  BitSet
     * @param b         -
     *                  hex representation
     * @param bitOffset -
     *                  (i.e. 0 for primary bitmap, 64 for secondary)
     * @return java BitSet object
     */
    public static BitSet byte2BitSet(BitSet bmap, byte[] b, int bitOffset) {
        int len = b.length << 3;
        for (int i = 0; i < len; i++)
            if (((b[i >> 3]) & (0x80 >> (i % 8))) > 0)
                bmap.set(bitOffset + i + 1);
        return bmap;
    }

    public static String[] bitmapDecoder(String strBitmap) {

        boolean[] bitmaps = bytesToBits(Hex.decode(strBitmap));
        ArrayList<String> strArray = new ArrayList<String>();
        for (int i = 1; i < bitmaps.length; i++) {
            if (bitmaps[i] == true) {
                if (i <= 64) {
                    strArray.add("p" + i);
                } else
                    strArray.add("s" + i);
            }
        }
        String[] strOut = new String[strArray.size()];
        for (int i = 0; i < strArray.size(); i++)
            strOut[i] = (String) strArray.get(i);
        return strOut;
    }

    public static int countLine(String fileName) {
        int nmbLine = 0;

        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader reader = new BufferedReader(fileReader);

            while (reader.readLine() != null) {
                nmbLine++;
            }
        } catch (FileNotFoundException e) {
        	logger.error(e.getClass().getSimpleName()+": "+ e.getMessage(), e);
//            e.printStackTrace();
        } catch (IOException e) {
        	logger.error(e.getClass().getSimpleName()+": "+ e.getMessage(), e);
//            e.printStackTrace();
        }

        return nmbLine;
    }

    public static String generateATMTrnSeqCntr(String lastSeq) {
        if (lastSeq == null)
            return "0001";
//        StringFormat format = new StringFormat(4, StringFormat.JUST_RIGHT);
        int lastSeqNo = Integer.parseInt(lastSeq);
        if (lastSeqNo >= 9999)
            return "0001";
        return StringFormat.formatNew(4, StringFormat.JUST_RIGHT, "" + (lastSeqNo + 1), '0');
    }

    public static String generateTrnSeqCntr(int length) {
    	String seq = "0";
        while (seq.startsWith("0")) {
        	seq = new Long(UUID.randomUUID().getLeastSignificantBits()).toString().substring(1, length+1);
		}
		return seq;
    }

    // sample (1.2345, "0.00") = 1.23
    // sample (1.2365, "0.00") = 1.24
    // its's default Rounding mode is RoundingMode.Half_up
    public static String valueWithPrecision(double d, RoundingMode roundingMode, String pattern) {
        int indexOfPoint = pattern.indexOf(".");
        int scale = (indexOfPoint == -1) ? 0 : pattern.length() - indexOfPoint - 1;
        NumberFormat numberFormat = new DecimalFormat(pattern);
        BigDecimal number = new BigDecimal(d);
        number = number.setScale(scale, roundingMode);
        return numberFormat.format(number.doubleValue());
    }

    public static String valueWithPrecision(double d, String pattern) {
        return valueWithPrecision(d, RoundingMode.HALF_UP, pattern);
    }

    public static boolean isFirstDateSmaller(Date d1, Date d2) {
        if (d1 == null)
            return false;

        return d1.compareTo(d2) < 0;
    }

    public static String convertDate(Date date) {
        if (date == null)
            return "null";
        long time = date.getTime();
        time -= time % 1000;
        return time + "";
    }
    
    public static boolean isValidCurrency(String currencyCode){
    	try {
			if (Currency.getInstance(currencyCode) != null)
				return true;
			return false;
		} catch (Exception e) {
			return false; 
		} 
    }

    public static Long longValueOf(String s ){
    	if (s==null || s.trim().isEmpty())
    		return null;
    	return Long.valueOf(s.trim());
    }
    
    public static Integer integerValueOf(String s ){
    	if (s==null || s.trim().isEmpty())
    		return null;
    	return Integer.valueOf(s.trim());
    }
    
    public static boolean isValidInteger(String s ){
    	if (s==null || s.trim().isEmpty())
    		return true;
        try{
    	    Integer.valueOf(s.trim());
        }catch (Exception e){
            return false;
        }
        return true;
    }


    public static int intValueOf(String s){
        Integer ingVal = integerValueOf(s);
        if(ingVal == null ) return 0;
        return ingVal.intValue();
    }

    public static String generateCronExpression(CycleCriteria cycleCriteria, Date fromDate) {
        PersianCalendar pc; 
        if (fromDate == null)
            pc = new PersianCalendar(0,0,0,0,0,0);
        else
            pc = new PersianCalendar(fromDate);
        StringBuilder sb = new StringBuilder();
		switch (cycleCriteria.getCycleType().getType()) {
            case CycleType.PER_MINUTE_VALUE:
                return sb.append(CycleCriteriaConsts.zero).append(CycleCriteriaConsts.space).append(
                        CycleCriteriaConsts.star).append(CycleCriteriaConsts.slash).append(
                        cycleCriteria.getCycleCount()).append(CycleCriteriaConsts.space).append(
                        CycleCriteriaConsts.star).append(CycleCriteriaConsts.space).append(CycleCriteriaConsts.star)
                        .append(CycleCriteriaConsts.space).append(CycleCriteriaConsts.star).append(
                        CycleCriteriaConsts.space).append(CycleCriteriaConsts.qSign).toString();
            case CycleType.PER_HOUR_VALUE:
                return sb.append(CycleCriteriaConsts.zero).append(CycleCriteriaConsts.space).append(
                        pc.get(PersianCalendar.MINUTE)).append(CycleCriteriaConsts.space).append(CycleCriteriaConsts.star)
                        .append(CycleCriteriaConsts.slash).append(cycleCriteria.getCycleCount())
                        .append(CycleCriteriaConsts.space).append(CycleCriteriaConsts.star).append(
                        CycleCriteriaConsts.space).append(CycleCriteriaConsts.star).append(
                        CycleCriteriaConsts.space).append(CycleCriteriaConsts.qSign).toString();
            case CycleType.PER_DAY_VALUE:
                return sb.append(CycleCriteriaConsts.zero).append(CycleCriteriaConsts.space).append(
                        pc.get(PersianCalendar.MINUTE)).append(CycleCriteriaConsts.space).append(
                        pc.get(PersianCalendar.HOUR_OF_DAY)).append(CycleCriteriaConsts.space).append(
                        CycleCriteriaConsts.star).append(CycleCriteriaConsts.slash).append(
                        cycleCriteria.getCycleCount()).append(CycleCriteriaConsts.space).append(
                        CycleCriteriaConsts.star).append(CycleCriteriaConsts.space).append(CycleCriteriaConsts.qSign)
                        .toString();
            case CycleType.PER_MONTH_VALUE:
                return sb.append(CycleCriteriaConsts.zero).append(CycleCriteriaConsts.space).append(
                        pc.get(PersianCalendar.MINUTE)).append(CycleCriteriaConsts.space).append(
                        pc.get(PersianCalendar.HOUR_OF_DAY)).append(CycleCriteriaConsts.space).append(
                        pc.get(PersianCalendar.DAY_OF_MONTH)).append(CycleCriteriaConsts.space).append(
                        CycleCriteriaConsts.star).append(CycleCriteriaConsts.slash).append(
                        cycleCriteria.getCycleCount()).append(CycleCriteriaConsts.space).append(
                        CycleCriteriaConsts.qSign).toString();
            case CycleType.PER_WEEK_VALUE:
                return sb.append(CycleCriteriaConsts.zero).append(CycleCriteriaConsts.space).append(
                        pc.get(PersianCalendar.MINUTE)).append(CycleCriteriaConsts.space).append(
                        pc.get(PersianCalendar.HOUR_OF_DAY)).append(CycleCriteriaConsts.space).append(
                        CycleCriteriaConsts.star).append(CycleCriteriaConsts.space).append(CycleCriteriaConsts.star)
                        .append(CycleCriteriaConsts.space).append(pc.get(PersianCalendar.DAY_OF_WEEK)).append(
                        CycleCriteriaConsts.slash).append(cycleCriteria.getCycleCount())
                        .toString();
            case CycleType.PER_YEAR_VALUE:
                return sb.append(CycleCriteriaConsts.zero).append(CycleCriteriaConsts.space).append(
                        pc.get(PersianCalendar.MINUTE)).append(CycleCriteriaConsts.space).append(
                        pc.get(PersianCalendar.HOUR_OF_DAY)).append(CycleCriteriaConsts.space).append(
                        pc.get(PersianCalendar.DAY_OF_MONTH)).append(CycleCriteriaConsts.space).append(
                        pc.get(PersianCalendar.MONTH)).append(CycleCriteriaConsts.space).append(CycleCriteriaConsts.qSign)
                        .append(CycleCriteriaConsts.space).append(CycleCriteriaConsts.star).append(
                        CycleCriteriaConsts.slash).append(cycleCriteria.getCycleCount())
                        .toString();
        }

        return "";
    }

    public static boolean isInCurrentCycle(CycleType cycleType, DateTime currentDate, Date newDate) {
        PersianCalendar currentPC = new PersianCalendar();
        PersianCalendar newPC = new PersianCalendar();
        currentPC.setTime(currentDate.toDate());
        newPC.setTime(newDate);

        switch (cycleType.getType()) {
            case CycleType.PER_MINUTE_VALUE:
                return newPC.get(PersianCalendar.YEAR) == currentPC.get(PersianCalendar.YEAR)
                        && newPC.get(PersianCalendar.MONTH) == currentPC.get(PersianCalendar.MONTH)
                        && newPC.get(PersianCalendar.DAY_OF_MONTH) == currentPC.get(PersianCalendar.DAY_OF_MONTH)
                        && newPC.get(PersianCalendar.HOUR) == currentPC.get(PersianCalendar.HOUR)
                        && newPC.get(PersianCalendar.MINUTE) == currentPC.get(PersianCalendar.MINUTE);
            case CycleType.PER_HOUR_VALUE:
                return newPC.get(PersianCalendar.YEAR) == currentPC.get(PersianCalendar.YEAR)
                        && newPC.get(PersianCalendar.MONTH) == currentPC.get(PersianCalendar.MONTH)
                        && newPC.get(PersianCalendar.DAY_OF_MONTH) == currentPC.get(PersianCalendar.DAY_OF_MONTH)
                        && newPC.get(PersianCalendar.HOUR) == currentPC.get(PersianCalendar.HOUR);
            case CycleType.PER_DAY_VALUE:
                return newPC.get(PersianCalendar.YEAR) == currentPC.get(PersianCalendar.YEAR)
                        && newPC.get(PersianCalendar.MONTH) == currentPC.get(PersianCalendar.MONTH)
                        && newPC.get(PersianCalendar.DAY_OF_MONTH) == currentPC.get(PersianCalendar.DAY_OF_MONTH);
            case CycleType.PER_WEEK_VALUE:
                return newPC.get(PersianCalendar.YEAR) == currentPC.get(PersianCalendar.YEAR)
                        && newPC.get(PersianCalendar.MONTH) == currentPC.get(PersianCalendar.MONTH)
                        && newPC.get(PersianCalendar.WEEK_OF_MONTH) == currentPC.get(PersianCalendar.WEEK_OF_MONTH);
            case CycleType.PER_MONTH_VALUE:
                return newPC.get(PersianCalendar.YEAR) == currentPC.get(PersianCalendar.YEAR)
                        && newPC.get(PersianCalendar.MONTH) == currentPC.get(PersianCalendar.MONTH);
            case CycleType.PER_YEAR_VALUE:
                return newPC.get(PersianCalendar.YEAR) == currentPC.get(PersianCalendar.YEAR);
        }

        return true;
    }
    
    public static boolean hasText(String text) {
    	return (text != null && !"".equals(text) && text.length() > 0);
    }
    
    public static boolean isValidAppPan(String appPan) {
//    	if (!hasText(appPan))
//    		return true;
    		if (appPan.matches("\\d+")) {
    			if (appPan.length() == 16 || appPan.length() == 19)
    				return true;
    		}
    	return false;
    }
    
    public static String ansiFormat(String name) {
		if (name == null)
			return "";
    	name = name.replaceAll("ی", "ي");
 		name = name.replaceAll("ء", "ئ");
 		name = name.replaceAll("ک", "ك");
 		return name;
     }

	// bizhani: please don't delete this function, it may be used in groovy scripts 
	public static Integer date(String str){
		String[] parts = str.split("/");
		DayDate pers = new DayDate(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
		DayDate greg = new DayDate(vaulsys.calendar.PersianCalendar.getGregorianDate(pers));
		return greg.getDate();
	}
	
	public static String getMainClassName() {
		StackTraceElement[] elems = new Exception().getStackTrace(); 
		return elems[elems.length - 1].getClassName();
	}

    public static List<List<String>> readCSVFile(String filePath) throws Exception {
        logger.debug("START readCSVFile : " + filePath);
        try {
            List<List<String>> result = new ArrayList<List<String>>();
            FileReader fileReader = new FileReader(filePath);
            BufferedReader reader = new BufferedReader(fileReader);
            Pattern pat = Pattern.compile(",(\".*?\")");
            String line;
            List<String> record;
            final String comma = ",";
            final String comma2 = "&comm";
            final String dblqt = "\"";
            final String uStr = "\\u0000";
            final String emptyStr = "";

            while ((line = reader.readLine()) != null && !line.trim().equals(emptyStr)) {
                record = new Vector<String>();
                Matcher matcher = pat.matcher(line);
                StringBuffer sb = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(sb, comma + matcher.group(1).replaceAll(comma, comma2));
                }
                matcher.appendTail(sb);
                String[] fields = sb.toString().split(comma);
                for (String field : fields) {
                    field = (field != null ? field.replaceAll(comma2, comma) : emptyStr).replace(dblqt, emptyStr).trim();
                    if (field.equals(emptyStr)) {
                        field = null;
                    }
                    field = (field != null ? field.replaceAll(uStr, emptyStr) : emptyStr).replace(dblqt, emptyStr).trim();
                    record.add(field);
                }

                result.add(record);
            }
            reader.close();
            fileReader.close();

            logger.debug("END readCSVFile");
            return result;
        } catch (Exception e) {
            logger.error(e, e);
            throw e;
        }
    }
    public static boolean isNumeric(String str){
    	String pattern = "[0-9]+";
    	return str.matches(pattern);
    }
    public static boolean hasValidNumber(String text) { //Raza Adding from TPSP
        try {
            return (text != null && !"".equals(text) && text.length() > 0) && Long.parseLong(text) > 0L;
        }catch (Exception e){}
        return false;
    }
    public static List<String> splitCommaSeparatedItems(String commaSeparatedRecipients) {
        return Arrays.asList(commaSeparatedRecipients.split("\\s*,\\s*"));
    }
    public static String quote(String s){
        return new StringBuilder()
                .append("'")
                .append(s)
                .append("'").toString();
    }
    public static String nowDateTimeString(){
        return new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    // Asim Shahzad, Date : 30th July 2021, Tracking ID : VC-NAP-202107121

    public static int[] convertListToArray(List<CMSCardSequenceTracker> listCardSeq) {
        int[] arr = new int[listCardSeq.size()];

        for(int i=0;i<listCardSeq.size();i++){
            arr[i] = Integer.valueOf(listCardSeq.get(i).getSequenceValue());
        }

        return arr;
    }

    public static Boolean binarySearch(int arr[], int elementToSearch) {

        int firstIndex = 0;
        int lastIndex = arr.length - 1;

        // termination condition (element isn't present)
        while(firstIndex <= lastIndex) {
            int middleIndex = (firstIndex + lastIndex) / 2;
            // if the middle element is our goal element, return its index
            if (arr[middleIndex] == elementToSearch) {
                return true;
            }

            // if the middle element is smaller
            // point our index to the middle+1, taking the first half out of consideration
            else if (arr[middleIndex] < elementToSearch)
                firstIndex = middleIndex + 1;

                // if the middle element is bigger
                // point our index to the middle-1, taking the second half out of consideration
            else if (arr[middleIndex] > elementToSearch)
                lastIndex = middleIndex - 1;

        }
        return false;
    }

    // ====================================================================
}
