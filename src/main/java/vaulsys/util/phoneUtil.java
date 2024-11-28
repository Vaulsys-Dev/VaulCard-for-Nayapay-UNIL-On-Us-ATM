package vaulsys.util;

public class phoneUtil {
	private static String samplePhoneNumber = "9141234567";
	private static String samplePhoneNumber0 = "09141234567";
	private static String samplePhoneNumber98 = "989141234567";
	private static final int TEN = samplePhoneNumber.length();
	private static final int ELEVEN = samplePhoneNumber0.length();
	private static final int TWELVE = samplePhoneNumber98.length();
	
	public static boolean isValidMobilePhoneNumber(String mobilePhoneNumber) {
		mobilePhoneNumber = removeFirstPlus(mobilePhoneNumber);
		if(! generalCheck(mobilePhoneNumber)){
			return false;
		}
		if(mobilePhoneNumber.length() == TEN && !mobilePhoneNumber.startsWith("9"))
			return false;
		if(mobilePhoneNumber.length() == ELEVEN && !mobilePhoneNumber.startsWith("09"))
			return false;
		if(mobilePhoneNumber.length() == TWELVE && !mobilePhoneNumber.startsWith("989"))
			return false;
		return true;
	}
	public static boolean isValidMCIMobilePhoneNumber(String mobilePhoneNumber){
		mobilePhoneNumber = removeFirstPlus(mobilePhoneNumber);
		if(! generalCheck(mobilePhoneNumber)){
			return false;
		}
		if(mobilePhoneNumber.length() == TEN && !mobilePhoneNumber.startsWith("91"))
			return false;
		if(mobilePhoneNumber.length() == ELEVEN && !mobilePhoneNumber.startsWith("091"))
			return false;
		if(mobilePhoneNumber.length() == TWELVE && !mobilePhoneNumber.startsWith("9891"))
			return false;
		return true;
	}
	private static boolean generalCheck(String mobilePhoneNumber){
		mobilePhoneNumber = removeFirstPlus(mobilePhoneNumber);
		if(! Util.isNumeric(mobilePhoneNumber))
			return false;
		if(mobilePhoneNumber.length()<TEN || mobilePhoneNumber.length()>TWELVE)
			return false;
		return true;
	}
	private static String removeFirstPlus(String mobilePhoneNumber){
		if(mobilePhoneNumber.startsWith("+"))
			return mobilePhoneNumber.substring(1);
		return mobilePhoneNumber;
	}
}
