package vaulsys.webservices.ghasedak;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import vaulsys.persistence.IEnum;

public class GhasedakItemType implements IEnum, Cloneable{
	private static Logger logger = Logger.getLogger(GhasedakItemType.class);
	
	private static final Integer GOLD_ALL_VALUE = 100;
	private static final Integer GOLD_GLOBAL_VALUE = 101;
	private static final Integer GOLD_IRG17_VALUE = 102;
	private static final Integer GOLD_IRG18_VALUE = 103;
	private static final Integer GOLD_IRG20_VALUE = 104;
	private static final Integer GOLD_IRG22_VALUE = 105;
	private static final Integer GOLD_IRG24_VALUE = 106;
	
	private static final Integer IRC_ALL_VALUE = 200;
	private static final Integer IRC_IRCOLD_VALUE = 201;
	private static final Integer IRC_IRCNEW_VALUE = 202;
	private static final Integer IRC_IRC2_VALUE = 203;
	private static final Integer IRC_IRC4_VALUE = 204;
	private static final Integer IRC_IRCGRAM_VALUE = 205;
	
	private static final Integer METAL_ALL_VALUE = 300;
	private static final Integer SILVER_VALUE = 301;
	private static final Integer PLATINUM_VALUE = 302;
	private static final Integer PALLADIUM_VALUE = 303;
	private static final Integer ALUMINUM_VALUE = 304;
	private static final Integer COPPER_VALUE = 305;
	private static final Integer NICKEL_VALUE = 306;
	private static final Integer LEAD_VALUE = 307;
	private static final Integer ZINC_VALUE = 308;
	private static final Integer OLI_VALUE = 309;
	
	private static final Integer IR_ALL_VALUE = 400;
	private static final Integer IR_IRUSD_VALUE = 401;
	private static final Integer IR_IREUR_VALUE = 402;
	private static final Integer IR_IRGBP_VALUE = 403;
	private static final Integer IR_IRAED_VALUE = 404;
	private static final Integer IR_IRTRY_VALUE = 405;
	private static final Integer IR_IRTHB_VALUE = 406;
	private static final Integer IR_IRINR_VALUE = 407;
	private static final Integer IR_IRMYR_VALUE = 408;
	private static final Integer IR_IRCNY_VALUE = 409;
	
	private static final Integer $_ALL_VALUE = 500;
	private static final Integer $_EUR_VALUE = 501;
	private static final Integer $_GBP_VALUE = 502;
	private static final Integer $_CAD_VALUE = 503;
	private static final Integer $_JPY_VALUE = 504;
	private static final Integer $_CHF_VALUE = 505;
	private static final Integer $_AUD_VALUE = 506;
	private static final Integer $_NZD_VALUE = 507;
	private static final Integer $_INR_VALUE = 508;
	private static final Integer $_SEK_VALUE = 509;
	private static final Integer $_CNY_VALUE = 510;
	private static final Integer $_MYR_VALUE = 511;
	private static final Integer $_TRY_VALUE = 512;
	private static final Integer $_HKD_VALUE = 513;
	private static final Integer $_AZN_VALUE = 514;
	private static final Integer $_THB_VALUE = 515;
	
	private Integer type;
	
	public GhasedakItemType(){
	}
	
	public GhasedakItemType(Integer type){
		super();
		this.type = type;
	}
	
	public Integer getType() {
		return type;
	}
	
	public static final GhasedakItemType GOLD_ALL = new GhasedakItemType(GOLD_ALL_VALUE);
	public static final GhasedakItemType GOLD_GLOBAL = new GhasedakItemType(GOLD_GLOBAL_VALUE);
	public static final GhasedakItemType GOLD_IRG17  = new GhasedakItemType(GOLD_IRG17_VALUE);
	public static final GhasedakItemType GOLD_IRG18  = new GhasedakItemType(GOLD_IRG18_VALUE);
	public static final GhasedakItemType GOLD_IRG20  = new GhasedakItemType(GOLD_IRG20_VALUE);
	public static final GhasedakItemType GOLD_IRG22  = new GhasedakItemType(GOLD_IRG22_VALUE);
	public static final GhasedakItemType GOLD_IRG24  = new GhasedakItemType(GOLD_IRG24_VALUE);
	
	public static final GhasedakItemType IRC_ALL = new GhasedakItemType(IRC_ALL_VALUE);
	public static final GhasedakItemType IRC_IRCOLD = new GhasedakItemType(IRC_IRCOLD_VALUE);
	public static final GhasedakItemType IRC_IRCNEW = new GhasedakItemType(IRC_IRCNEW_VALUE);
	public static final GhasedakItemType IRC_IRC2 = new GhasedakItemType(IRC_IRC2_VALUE);
	public static final GhasedakItemType IRC_IRC4 = new GhasedakItemType(IRC_IRC4_VALUE);
	public static final GhasedakItemType IRC_IRCGRAM = new GhasedakItemType(IRC_IRCGRAM_VALUE);
	
	public static final GhasedakItemType METAL_ALL = new GhasedakItemType(METAL_ALL_VALUE);
	public static final GhasedakItemType SILVER = new GhasedakItemType(SILVER_VALUE);
	public static final GhasedakItemType PLATINUM = new GhasedakItemType(PLATINUM_VALUE);
	public static final GhasedakItemType PALLADIUM = new GhasedakItemType(PALLADIUM_VALUE);
	public static final GhasedakItemType ALUMINUM = new GhasedakItemType(ALUMINUM_VALUE);
	public static final GhasedakItemType COPPER = new GhasedakItemType(COPPER_VALUE);
	public static final GhasedakItemType NICKEL = new GhasedakItemType(NICKEL_VALUE);
	public static final GhasedakItemType LEAD = new GhasedakItemType(LEAD_VALUE);
	public static final GhasedakItemType ZINC = new GhasedakItemType(ZINC_VALUE);
	public static final GhasedakItemType OLI = new GhasedakItemType(OLI_VALUE);
	
	public static final GhasedakItemType IR_ALL = new GhasedakItemType(IR_ALL_VALUE);
	public static final GhasedakItemType IR_IRUSD = new GhasedakItemType(IR_IRUSD_VALUE);
	public static final GhasedakItemType IR_IREUR = new GhasedakItemType(IR_IREUR_VALUE);
	public static final GhasedakItemType IR_IRGBP = new GhasedakItemType(IR_IRGBP_VALUE);
	public static final GhasedakItemType IR_IRAED = new GhasedakItemType(IR_IRAED_VALUE);
	public static final GhasedakItemType IR_IRTRY = new GhasedakItemType(IR_IRTRY_VALUE);
	public static final GhasedakItemType IR_IRTHB = new GhasedakItemType(IR_IRTHB_VALUE);
	public static final GhasedakItemType IR_IRINR = new GhasedakItemType(IR_IRINR_VALUE);
	public static final GhasedakItemType IR_IRMYR = new GhasedakItemType(IR_IRMYR_VALUE);
	public static final GhasedakItemType IR_IRCNY = new GhasedakItemType(IR_IRCNY_VALUE);
	
	public static final GhasedakItemType  $_ALL = new GhasedakItemType($_ALL_VALUE);
	public static final GhasedakItemType  $_EUR = new GhasedakItemType($_EUR_VALUE);
	public static final GhasedakItemType  $_GBP = new GhasedakItemType($_GBP_VALUE);
	public static final GhasedakItemType  $_CAD = new GhasedakItemType($_CAD_VALUE);
	public static final GhasedakItemType  $_JPY = new GhasedakItemType($_JPY_VALUE);
	public static final GhasedakItemType  $_CHF = new GhasedakItemType($_CHF_VALUE);
	public static final GhasedakItemType  $_AUD = new GhasedakItemType($_AUD_VALUE);
	public static final GhasedakItemType  $_NZD = new GhasedakItemType($_NZD_VALUE);
	public static final GhasedakItemType  $_INR = new GhasedakItemType($_INR_VALUE);
	public static final GhasedakItemType  $_SEK = new GhasedakItemType($_SEK_VALUE);
	public static final GhasedakItemType  $_CNY = new GhasedakItemType($_CNY_VALUE);
	public static final GhasedakItemType  $_MYR = new GhasedakItemType($_MYR_VALUE);
	public static final GhasedakItemType  $_TRY = new GhasedakItemType($_TRY_VALUE);
	public static final GhasedakItemType  $_HKD = new GhasedakItemType($_HKD_VALUE);
	public static final GhasedakItemType  $_AZN = new GhasedakItemType($_AZN_VALUE);
	public static final GhasedakItemType  $_THB = new GhasedakItemType($_THB_VALUE);
	
	
	public static final List<Long> GOLDS = new ArrayList<Long>();
	public static final List<Long> GULDENS = new ArrayList<Long>();
	public static final List<Long> METALS = new ArrayList<Long>();
	public static final List<Long> CUREXCHGS = new ArrayList<Long>();
	public static final List<Long> DOLLAREXCHGS = new ArrayList<Long>();
	
	static {
		Field[] list = GhasedakItemType.class.getFields();
		Method getType = null;
		try {
			getType = GhasedakItemType.class.getMethod("getType");
		} catch (SecurityException e2) {
			e2.printStackTrace();
		} catch (NoSuchMethodException e2) {
			e2.printStackTrace();
		}
		for (Field e : list) {
			String name = e.getName();
			if("GOLDS".equals(name) || "GULDENS".equals(name) || "METALS".equals(name) || "CUREXCHGS".equals(name) || "DOLLAREXCHGS".equals(name))
				continue;
			try{
				Integer value = (Integer) getType.invoke(e.get(null), (Object[])null);
				if(name.contains("ALL"))
					continue;
				if(name.contains("GOLD_"))
					GOLDS.add(Long.valueOf(value));
				else if(name.contains("IRC_"))
					GULDENS.add(Long.valueOf(value));
				else if(name.contains("IR_"))
					CUREXCHGS.add(Long.valueOf(value));
				else if(name.contains("$_"))
					DOLLAREXCHGS.add(Long.valueOf(value));
				else /*if(name.contains("METAL"))*/
					METALS.add(Long.valueOf(value));
			}catch(Exception ex){
				Exception e1 = new Exception("error in getting value of "+ name, ex);
				logger.error(e1);
//				e1.printStackTrace();
			}
		}
	}
	
	
	@Override
	 public boolean equals(Object obj) {
	 	if (this == obj)
	 		return true;
	 	if (obj == null || !(obj instanceof GhasedakItemType))
	 		return false;
	 	GhasedakItemType that = (GhasedakItemType) obj;
	 	return type.equals(that.type);
	 }
	 
	 @Override
	 public int hashCode() {
	 	return type;
	 }
	 
	 @Override
	 public String toString() {
		 String name = "";
		 switch(type){
		 case 100:
			 name = "GOLD_UNKNOWN";
			 break;
		 case 101:
			 name = "GOLD_GLOBAL";
			 break;
		 case 102:
			 name = "GOLD_IRG17";
			 break;
		 case 103:
			 name = "GOLD_IRG18";
			 break;
		 case 104:
			 name = "GOLD_IRG20";
			 break;
		 case 105:
			 name = "GOLD_IRG22";
			 break;
		 case 106:
			 name = "GOLD_IRG24";
			 break;
			 
		 case 200:
			 name = "IRC_UNKNOWN";
			 break;
		 case 201:
			 name = "IRC_OLD";
			 break;
		 case 202:
			 name = "IRC_NEW";
			 break;
		 case 203:
			 name = "IRC_2";
			 break;
		 case 204:
			 name = "IRC_4";
			 break;
		 case 205:
			 name = "IRC_Gram";
			 break;
			 
		 case 300:
			 name = "Metal_UNKNOWN";
			 break;
		 case 301:
			 name = "Silver";
			 break;
		 case 302:
			 name = "Platinum";
			 break;
		 case 303:
			 name = "Palladium";
			 break;
		 case 304:
			 name = "Aluminum";
			 break;
		 case 305:
			 name = "Copper";
			 break;
		 case 306:
			 name = "Nickel";
			 break;
		 case 307:
			 name = "Lead";
			 break;
		 case 308:
			 name = "Zinc";
			 break;
		 case 309:
			 name = "Oil";
			 break;
			 
		 case 400:
			 name = "IR_UNKNOWN";
			 break;
		 case 401:
			 name = "IR_dollar";
			 break;
		 case 402:
			 name = "IR_Euro";
			 break;
		 case 403:
			 name = "IR_Pound";
			 break;
		 case 404:
			 name = "IR_UAE_Dirham";
			 break;
		 case 405:
			 name = "IR_Turkish_Lira";
			 break;
		 case 406:
			 name = "IR_Thai_Baht";
			 break;
		 case 407:
			 name = "IR_Indian_Rupee";
			 break;
		 case 408:
			 name = "IR_Mlysi_Ringgit";
			 break;
		 case 409:
			 name = "IR_ China_Yuan";
			 break;
		
		 case 500:
			 name = "$_Unknown";
			 break;
		 case 501:
			 name = "$_Euro";
			 break;
		 case 502:
			 name = "$_Pound";
			 break;
		 case 503:
			 name = "$_Canadian$";
			 break;
		 case 504:
			 name = "$_Japan_100Yen";
			 break;
		 case 505:
			 name = "$_Swiss_Franc";
			 break;
		 case 506:
			 name = "$_Australian&";
			 break;
		 case 507:
			 name = "$_newZealand$";
			 break;
		 case 508:
			 name = "$_Indian_Rupee";
			 break;
		 case 509:
			 name = "$_Swedish_Krona";
			 break;
		 case 510:
			 name = "$_China_Yuan";
			 break;
		 case 511:
			 name = "$_Mlysi_Ringgit";
			 break;
		 case 512:
			 name = "$_Turkish_Lira";
			 break;
		 case 513:
			 name = "$_HongKong&";
			 break;
		 case 514:
			 name = "$_Azrbjan_Manat";
			 break;
		 case 515:
			 name = "$_Thai_Baht";
			 break;
		
		 }
		 return name;
	 }

	 public boolean isgeneralType(){
		 return GOLD_ALL.equals(this) || IRC_ALL.equals(this) || METAL_ALL.equals(this)
				 || IR_ALL.equals(this) || $_ALL.equals(this);
	 }
	 
	 public List<Long> getItemCodes(){
		 if(GOLD_ALL.equals(this))
			 return GOLDS;
		 if(IRC_ALL.equals(this))
			 return GULDENS;
		 if(METAL_ALL.equals(this))
			 return METALS;
		 if(IR_ALL.equals(this))
			 return CUREXCHGS;
		 if($_ALL.equals(this))
			 return DOLLAREXCHGS;
		 return new ArrayList<Long>(){{add((long) this.hashCode());}};
	 }
}
