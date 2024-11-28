/**
 * 
 */
package vaulsys.protocols.ndc.encoding;

import groovy.lang.Binding;
import vaulsys.authorization.policy.Bank;
import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.PersianCalendar;
import vaulsys.clearing.cyclecriteria.CycleType;
import vaulsys.entity.OrganizationService;
import vaulsys.entity.impl.Organization;
import vaulsys.modernpayment.onlinebillpayment.OnlineBillPaymentStatus;
import vaulsys.mtn.MTNChargeService;
import vaulsys.protocols.encoding.impl.FarsiConvertor;
import vaulsys.protocols.ifx.imp.BankStatementData;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.ndc.constants.NDCPrinterFlag;
import vaulsys.protocols.ndc.constants.RestrictionOnTrxAndTermType;
import vaulsys.security.component.SecurityComponent;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.atm.ATMConfiguration;
import vaulsys.terminal.atm.ATMProducer;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.util.constants.ASCIIConstants;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
//import org.apache.woden.internal.resolver.SimpleURIResolver;

import com.ghasemkiani.util.icu.PersianDateFormat;
import com.ibm.icu.math.BigDecimal;


public class NDCConvertor extends FarsiConvertor {

	Logger logger = Logger.getLogger(NDCConvertor.class);
	public enum TextDirection{
		LeftToRight, RightToLeft;
	
		@Override
		public String toString() {
			if (this == LeftToRight)
				return "LeftToRight";
			else 
				return "RightToLeft";
		}
	}
	
	public enum Language{
		FarsiLanguage, EnglishLanguage;
		
		@Override
		public String toString() {
			if (this == FarsiLanguage)
				return "fa";
			else 
				return "en";
		}
	}
	
//	public Ifx ifx;
	
	
	public static final String FARSI_RECIEPT_ENCODING = "farsi_reciept";
	public static final String FARSI_EXTENDED_RECIEPT_ENCODING = "farsi_extended_reciept";
	public static final String FARSI_SCREEN_ENCODING = "farsi_screen";
	public static final String FARSI_EXTENDED_SCREEN_ENCODING = "farsi_extended_screen";
	public static final String ENGLISH_ENCODING = "english";
	
	public static final String RECEIPT_CONVERTOR = "receipt_convertor";
	public static final String SCREEN_CONVERTOR = "screen_convertor";
	
	//AldComment Task002 : add below lines in  92.04.05
	public static final byte DASH_CHAR = "-".getBytes()[0];

	public static final byte NCR_RECEIPT_COMMA_CHAR 	 = (byte) 0xdb;
	public static final byte NCR_RECEIPT_STAR_CHAR 	 	 = (byte) 0xdc;
	public static final byte NCR_RECEIPT_SHARP_SIGN_CHAR = (byte) 0xdd;
	public static final byte NCR_RECEIPT_COLON_CHAR 	 = (byte) 0xde;
	public static final byte NCR_RECEIPT_SLASH_CHAR 	 = (byte) 0xdf;
	public static final byte NCR_RECEIPT_DOT_CHAR		 = (byte) 0x24;
	public static final byte[] NCR_RECEIPT_140_STR 	 	 = new byte[] {(byte)0x31, (byte)0x34, (byte)0x30};
	public static final byte[] NCR_RECEIPT_141_STR 	 	 = new byte[] {(byte)0x31, (byte)0x34, (byte)0x31};
	public static final byte[] NCR_RECEIPT_YESOK_STR 	 = new byte[] {"y".getBytes()[0], "e".getBytes()[0], "s".getBytes()[0], NCR_RECEIPT_SLASH_CHAR, "o".getBytes()[0], "k".getBytes()[0]};

	public static final byte NOT_NCR_RECEIPT_STAR_CHAR 	 	 = (byte) 0x2A;
	public static final byte NOT_NCR_RECEIPT_SHARP_SIGN_CHAR = (byte) 0x23;
	public static final byte NOT_NCR_RECEIPT_COLON_CHAR 	 = ':';
	public static final byte NOT_NCR_RECEIPT_SLASH_CHAR 	 = '/';
	public static final byte[] NOT_NCR_RECEIPT_140_STR 	 	 = "140".getBytes();
	public static final byte[] NOT_NCR_RECEIPT_141_STR 	 	 = "141".getBytes();
	public static final byte[] NOT_NCR_RECEIPT_YESOK_STR 	 = "YES/OK".getBytes();
	
//	public Map<String, byte[]> receiptDetailsMap = new HashMap<String, byte[]>();
//	public Map<String, String> bnkFaName = new HashMap<String, String>();
	
	protected char[] farsiCharsIndexes = {
	/* 0 */'-',
	/* 1 */'%',
	/* 2 */'ا',
	/* 3 */'ء',
	/* 4 */'ب',
	/* 5 */'پ',
	/* 6 */'ت',
	/* 7 */'ث',
	/* 8 */'ج',
	/* 9 */'چ',
	/* 10 */'ح',
	/* 11 */'خ',
	/* 12 */'د',
	/* 13 */'ذ',
	/* 14 */'ر',
	/* 15 */'ز',
	/* 16 */'ژ',
	/* 17 */'س',
	/* 18 */'ش',
	/* 19 */'ص',
	/* 20 */'ض',
	/* 21 */'0',
	/* 22 */'1',
	/* 23 */'2',
	/* 24 */'3',
	/* 25 */'4',
	/* 26 */'5',
	/* 27 */'6',
	/* 28 */'7',
	/* 29 */'8',
	/* 30 */'9',
	/* 31 */'ط',
	/* 32 */'ظ',
	/* 33 */'ع',
	/* 34 */'غ',
	/* 35 */'ف',
	/* 36 */'ق',
	/* 37 */'ک',
	/* 38 */'گ',
	/* 39 */'ل',
	/* 40 */'م',
	/* 41 */'ن',
	/* 42 */'و',
	/* 43 */'ه',
	/* 44 */'ی',
	/* 45 */' ', 
	/* 46 */'،',
	/* 47 */',',
	/* 48 */'آ',
   
	/* 49 */'░', /* 176 */   
	/* 50 */'▒', /* 177 */   
	/* 51 */'▓', /* 178 */   
	/* 52 */'│', /* 179 */   
	/* 53 */'┤', /* 180 */   
	/* 54 */'╡', /* 181 */   
	/* 55 */'╢', /* 182 */   
	/* 56 */'╖', /* 183 */   
	/* 57 */'╕', /* 184 */   
	/* 58 */'╣', /* 185 */   
	/* 59 */'║', /* 186 */   
	/* 60 */'╗', /* 187 */   
	/* 61 */'╝', /* 188 */   
	/* 62 */'╜', /* 189 */   
	/* 63 */'╛', /* 190 */   
	/* 64 */'┐', /* 191 */   
	/* 65 */'└', /* 192 */   
	/* 66 */'┴', /* 193 */   
	/* 67 */'┬', /* 194 */   
	/* 68 */'├', /* 195 */   
	/* 69 */'─', /* 196 */   
	/* 70 */'┼', /* 197 */   
	/* 71 */'╞', /* 198 */   
	/* 72 */'╟', /* 199 */   
	/* 73 */'╚', /* 200 */   
	/* 74 */'╔', /* 201 */   
	/* 75 */'╩', /* 202 */   
	/* 76 */'╦', /* 203 */   
	/* 77 */'╠', /* 204 */   
	/* 78 */'═', /* 205 */   
	/* 79 */'╬', /* 206 */   
	/* 80 */'╧', /* 207 */   
	/* 81 */'╨', /* 208 */   
	/* 82 */'╤', /* 209 */   
	/* 83 */'╥', /* 210 */   
	/* 84 */'╙', /* 211 */   
	/* 85 */'╘', /* 212 */
	/* 86 */'╒', /* 213 */
	/* 87 */'╓', /* 214 */
	/* 88 */'╫', /* 215 */
	/* 89 */'╪', /* 216 */
	/* 90 */'┘', /* 217 */
	/* 91 */'┌', /* 218 */
	/* 92 */'█', /* 219 */
	/* 93 */'▄', /* 220 */
	/* 94 */'▌', /* 221 */
	/* 95 */'▐', /* 222 */
	/* 96 */'▀', /* 223 */
	/* 97 */'»', 
	/* 98 */'«', 
	};

	protected int[] farsiCharsCompleter = { -1, -1, -1, -1, /* 0-3 */
	-1, -1, -1, 0x2D, /* 4-7 */
	-1, -1, -1, -1, /* 8-11 */
	-1, -1, -1, -1, -1, /* 12-16 */
	0xB0, 0xB0, 0xB0, 0xB0, /* 17-20 */
//	0x21, 0x21, 0x21, 0x21, /* 17-20 */
	-1, -1, -1, -1, -1, /* 21-25 */
	-1, -1, -1, -1, -1, /* 26-30 */
	-1, -1, -1, -1, /* 31-34 */
	-1, -1, -1, -1, /* 35-38 */
	-1, -1, -1, -1, /* 39-42 */
	-1, -1, -1, -1, -1, -1 ,-1, /* 43-48 */
	-1, -1, -1, -1, /*49-52*/
	-1, -1, -1, -1, /*53-56*/
	-1, -1, -1, -1, /*57-60*/
	-1, -1, -1, -1, /*61-64*/
	-1, -1, -1, -1, /*65-68*/
	-1, -1, -1, -1, /*69-72*/
	-1, -1, -1, -1, /*73-76*/
	-1, -1, -1, -1, /*77-80*/
	-1, -1, -1, -1, /*81-84*/
	-1, -1, -1, -1, /*85-88*/
	-1, -1, -1, -1, /*89-92*/
	-1, -1, -1, -1, /*93-96*/
	-1, -1 /*97-98*/
	}; 

	protected int[][] farsiCharsConnectingInstances = {
	/* 0 */{ 0x2D, 0x2D, 0x2D, 0x2D },
	/* 1 */{ 0x2B, 0x2B, 0x2B, 0x2B },
	/* 2 */{ 0x90, 0x90, 0x91, 0x91 },
	/* 3 */{ 0x8F, 0x8E, 0x8F, 0x8E },
	/* 4 */{ 0x92, 0x93, 0x92, 0x93 }, 
	/* 5 */{ 0x94, 0x95, 0x94, 0x95 },
	/* 6 */{ 0x96, 0x97, 0x96, 0x97 },
	/* 7 */{ 0x98, 0x99, 0x98, 0x99 },
	/* 8 */{ 0x9A, 0x9B, 0x9A, 0x9B },
	/* 9 */{ 0x9C, 0x9D, 0x9C, 0x9D },
	/* 10 */{ 0x9E, 0x9F, 0x9E, 0x9F },
	/* 11 */{ 0xA0, 0xA1, 0xA0, 0xA1 },
	/* 12 */{ 0xA2, 0xA2, 0xA2, 0xA2 },
	/* 13 */{ 0xA3, 0xA3, 0xA3, 0xA3 },
	/* 14 */{ 0xA4, 0xA4, 0xA4, 0xA4 },
	/* 15 */{ 0xA5, 0xA5, 0xA5, 0xA5 },
	/* 16 */{ 0xA6, 0xA6, 0xA6, 0xA6 },
	/* 17 */{ 0xA7, 0xA8, 0xA7, 0xA8 },
	/* 18 */{ 0xA9, 0xAA, 0xA9, 0xAA },
	/* 19 */{ 0xAB, 0xAC, 0xAB, 0xAC },
	/* 20 */{ 0xAD, 0xAE, 0xAD, 0xAE },
	/* 21 */{ 0x80, 0x80, 0x80, 0x80 },
	/* 22 */{ 0x81, 0x81, 0x81, 0x81 },
	/* 23 */{ 0x82, 0x82, 0x82, 0x82 },
	/* 24 */{ 0x83, 0x83, 0x83, 0x83 },
	/* 25 */{ 0x84, 0x84, 0x84, 0x84 },
	/* 26 */{ 0x85, 0x85, 0x85, 0x85 },
	/* 27 */{ 0x86, 0x86, 0x86, 0x86 },
	/* 28 */{ 0x87, 0x87, 0x87, 0x87 },
	/* 29 */{ 0x88, 0x88, 0x88, 0x88 },
	/* 30 */{ 0x89, 0x89, 0x89, 0x89 },
	/* 31 */{ 0xAF, 0xAF, 0xAF, 0xAF },
	/* 32 */{ 0xE0, 0xE0, 0xE0, 0xE0 },
	/* 33 */{ 0xE1, 0xE4, 0xE2, 0xE3 },
	/* 34 */{ 0xE5, 0xE8, 0xE6, 0xE7 },
	/* 35 */{ 0xE9, 0xEA, 0xE9, 0xEA },
	/* 36 */{ 0xEB, 0xEC, 0xEB, 0xEC },
	/* 37 */{ 0xED, 0xEE, 0xED, 0xEE },
	/* 38 */{ 0xEF, 0xF0, 0xEF, 0xF0 },
	/* 39 */{ 0xF1, 0xF3, 0xF1, 0xF3 },
	/* 40 */{ 0xF4, 0xF5, 0xF4, 0xF5 },
	/* 41 */{ 0xF6, 0xF7, 0xF6, 0xF7 },
	/* 42 */{ 0xF8, 0xF8, 0xF8, 0xF8 },
	/* 43 */{ 0xF9, 0xFB, 0xF9, 0xFA },
	/* 44 */{ 0xFD, 0xFE, 0xFC, 0xFE },
	/* 45 */{ 0x20, 0x20, 0x20, 0x20 },
	/* 46 */{ 0x8A, 0x8A, 0x8A, 0x8A },
	/* 47 */{ 0x2C, 0x2C, 0x2C, 0x2C },
	/* 48 */{ 0x8D, 0x8D, 0x8D, 0x8D },

// TODO	
	/* 49 */{ 0x8D, 0x8D, 0x8D, 0x8D }, //'░', /* 176 */   
	/* 50 */{ 0xB1, 0xB1, 0xB1, 0xb1 }, //'▒', /* 177 */   
	/* 51 */{ 0xB2, 0xB2, 0xB2, 0xB2 }, //'▓', /* 178 */   
	/* 52 */{ 0xB3, 0xB3, 0xB3, 0xB3 }, //'│', /* 179 */   
	/* 53 */{ 0xB4, 0xB4, 0xB4, 0xB4 }, //'┤', /* 180 */   
	/* 54 */{ 0xB5, 0xB5, 0xB5, 0xB5 }, //'╡', /* 181 */   
	/* 55 */{ 0xB6, 0xB6, 0xB6, 0xB6 }, //'╢', /* 182 */   
	/* 56 */{ 0xB7, 0xB7, 0xB7, 0xB7 }, //'╖', /* 183 */   
	/* 57 */{ 0xB8, 0xB8, 0xB8, 0xB8 }, //'╕', /* 184 */   
	/* 58 */{ 0xB9, 0xB9, 0xB9, 0xB9 }, //'╣', /* 185 */   
	/* 59 */{ 0xBA, 0xBA, 0xBA, 0xBA }, //'║', /* 186 */   
	/* 60 */{ 0xBB, 0xBB, 0xBB, 0xBB }, //'╗', /* 187 */   
	/* 61 */{ 0xBC, 0xBC, 0xBC, 0xBC }, //'╝', /* 188 */   
	/* 62 */{ 0xBD, 0xBD, 0xBD, 0xBD }, //'╜', /* 189 */   
	/* 63 */{ 0xBE, 0xBE, 0xBE, 0xBE }, //'╛', /* 190 */   
	/* 64 */{ 0xBF, 0xBF, 0xBF, 0xBF }, //'┐', /* 191 */   
	/* 65 */{ 0xC0, 0xC0, 0xC0, 0xC0 }, //'└', /* 192 */   
	/* 66 */{ 0xC1, 0xC1, 0xC1, 0xC1 }, //'┴', /* 193 */   
	/* 67 */{ 0xC2, 0xC2, 0xC2, 0xC2 }, //'┬', /* 194 */   
	/* 68 */{ 0xC3, 0xC3, 0xC3, 0xC3 }, //'├', /* 195 */   
	/* 69 */{ 0xC4, 0xC4, 0xC4, 0xC4 }, //'─', /* 196 */   
	/* 70 */{ 0xC5, 0xC5, 0xC5, 0xC5 }, //'┼', /* 197 */   
	/* 71 */{ 0xC6, 0xC6, 0xC6, 0xC6 }, //'╞', /* 198 */   
	/* 72 */{ 0xC7, 0xC7, 0xC7, 0xC7 }, //'╟', /* 199 */   
	/* 73 */{ 0xC8, 0xC8, 0xC8, 0xC8 }, //'╚', /* 200 */   
	/* 74 */{ 0xC9, 0xC9, 0xC9, 0xC9 }, //'╔', /* 201 */   
	/* 75 */{ 0xCA, 0xCA, 0xCA, 0xCA }, //'╩', /* 202 */   
	/* 76 */{ 0xCB, 0xCB, 0xCB, 0xCB }, //'╦', /* 203 */   
	/* 77 */{ 0xCC, 0xCC, 0xCC, 0xCC }, //'╠', /* 204 */   
	/* 78 */{ 0xCD, 0xCD, 0xCD, 0xCD }, //'═', /* 205 */   
	/* 79 */{ 0xCE, 0xCE, 0xCE, 0xCE }, //'╬', /* 206 */   
	/* 80 */{ 0xCF, 0xCF, 0xCF, 0xCF }, //'╧', /* 207 */   
	/* 81 */{ 0xD0, 0xD0, 0xD0, 0xD0 }, //'╨', /* 208 */   
	/* 82 */{ 0xD1, 0xD1, 0xD1, 0xD1 }, //'╤', /* 209 */   
	/* 83 */{ 0xD2, 0xD2, 0xD2, 0xD2 }, //'╥', /* 210 */   
	/* 84 */{ 0xD3, 0xD3, 0xD3, 0xD3 }, //'╙', /* 211 */   
	/* 85 */{ 0xD4, 0xD4, 0xD4, 0xD4 }, //'╘', /* 212 */
	/* 86 */{ 0xD5, 0xD5, 0xD5, 0xD5 }, //'╒', /* 213 */
	/* 87 */{ 0xD6, 0xD6, 0xD6, 0xD6 }, //'╓', /* 214 */
	/* 88 */{ 0xD7, 0xD7, 0xD7, 0xD7 }, //'╫', /* 215 */
	/* 89 */{ 0xD8, 0xD8, 0xD8, 0xD8 }, //'╪', /* 216 */
	/* 90 */{ 0xD9, 0xD9, 0xD9, 0xD9 }, //'┘', /* 217 */
	/* 91 */{ 0xDA, 0xDA, 0xDA, 0xDA }, //'┌', /* 218 */
	/* 92 */{ 0xDB, 0xDB, 0xDB, 0xDB }, //'█', /* 219 */
	/* 93 */{ 0xDC, 0xDC, 0xDC, 0xDC }, //'▄', /* 220 */
	/* 94 */{ 0xDD, 0xDD, 0xDD, 0xDD }, //'▌', /* 221 */
	/* 95 */{ 0xDE, 0xDE, 0xDE, 0xDE }, //'▐', /* 222 */
	/* 96 */{ 0xDF, 0xDF, 0xDF, 0xDF }, //'▀', /* 223 */
	/* 97 */{ 0xAF, 0xAF, 0xAF, 0xAF }, //'»', /* 223 */
	/* 98 */{ 0xAE, 0xAE, 0xAE, 0xAE }, //'«', /* 223 */
	};

	@Override
	public int[] getFarsiCharsCompleter() {
		return farsiCharsCompleter;
	}

	@Override
	public int[][] getFarsiCharsConnectingInstances() {
		return farsiCharsConnectingInstances;
	}

	@Override
	public char[] getFarsiCharsIndexes() {
		return farsiCharsIndexes;
	}

	public String getEncoding(String totalStr, String subStr) {
		int index = totalStr.indexOf(subStr);
		String str = totalStr.substring(0, index);
		
		index = str.lastIndexOf("[ESC](");
		if (index == -1)
			return "";
		
		return str.substring(index+6, index+6 + 1);
	}

	/*public NDCConvertor getConvertor(String totalStr, String subStr, Map<String, String> encodings, Map<String, String> convertors) {
		int index = totalStr.indexOf(subStr);
		String str = totalStr.substring(0, index);
		
		index = str.lastIndexOf("[ESC](");
		if (index == -1){
			return this;
		}
		String encoding = str.substring(index+6, index+6 + 1);
		return getConvertor(encoding, encodings, convertors);
	}*/
	
	public NDCConvertor getConvertor(String str, Map<String, String> encodings, Map<String, String> convertors) {
		if (!Util.hasText(str) || encodings == null || encodings.isEmpty())
			return this;

		if (str.equals(encodings.get(NDCConvertor.ENGLISH_ENCODING))
			|| str.equals(encodings.get(NDCConvertor.FARSI_RECIEPT_ENCODING))
			|| str.equals(encodings.get(NDCConvertor.FARSI_EXTENDED_RECIEPT_ENCODING)))
//			return (NDCConvertor) GlobalContext.getInstance().getConvertor(convertors.get(NDCConvertor.RECEIPT_CONVERTOR));
			return (NDCConvertor) ProcessContext.get().getConvertor(convertors.get(NDCConvertor.RECEIPT_CONVERTOR));

		if (str.equals("J") || str.equals("6")
				|| str.equals(encodings.get(NDCConvertor.FARSI_SCREEN_ENCODING))
				|| str.equals(encodings.get(NDCConvertor.FARSI_EXTENDED_SCREEN_ENCODING)))
//			return (NDCConvertor) GlobalContext.getInstance().getConvertor(convertors.get(NDCConvertor.SCREEN_CONVERTOR));
			return (NDCConvertor) ProcessContext.get().getConvertor(convertors.get(NDCConvertor.SCREEN_CONVERTOR));


		return this;
	}
	
	//after Task019 : 92.05.09
	public byte[] convert(NDCPrinterFlag printerFlag, String str, Ifx ifx) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if (str != null) {
			if (printerFlag != null) {
				ATMTerminal atm = null;
				if (ifx != null) {
					if (!(ifx.getEndPointTerminal() instanceof ATMTerminal)){
						atm = TerminalService.findTerminal(ATMTerminal.class, Long.parseLong(ifx.getTerminalId()));
					}else{
						atm = (ATMTerminal) ifx.getEndPointTerminal();
					}
				}
				
				//TASK Task141 [39391] - NCR
				if (atm.getProducer() != null && atm.getProducer().equals(ATMProducer.NCR) && NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY.equals(printerFlag)) {  //AldTODO Task141
					out.write(0x1b);out.write(0x5b);out.write(0x30);out.write(0x32);out.write(0x34);out.write(0x73);
				}				

				if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR) || !NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY.equals(printerFlag))
					out.write(printerFlag.getCode());
			}


			if (ifx != null && ifx.getThirdPartyName() == null && 
					(ISOFinalMessageType.isBillPaymentMessage(ifx.getIfxType()) || ISOFinalMessageType.isPrepareBillPaymentRqMessage(ifx.getIfxType())) &&
					ifx.getBillCompanyCode() != null){
				//Mirkamali(Task158)
//				if (ifx.getBillOrgType().equals(OrganizationType.UNDEFINED)) {
					Organization org = OrganizationService.findOrganizationByCompanyCode(ifx.getBillCompanyCode(), ifx.getBillOrgType());
					if (org != null) {
						ifx.setThirdPartyName(org.getName());
						ifx.setThirdPartyNameEn(org.getNameEn());
						
						if(!Util.hasText(org.getName()))
							ifx.setThirdPartyName(ifx.getBillOrgType().toString());
						if(!Util.hasText(org.getNameEn()))
							ifx.setThirdPartyNameEn(ifx.getBillOrgType().toStringEnglish());
						
					} else {
						ifx.setThirdPartyName(ifx.getBillOrgType().toString());
						ifx.setThirdPartyNameEn(ifx.getBillOrgType().toStringEnglish());
						
					}
//				} else {
//					ifx.setThirdPartyName(ifx.getBillOrgType().toString());
//					ifx.setThirdPartyNameEn(ifx.getBillOrgType().toStringEnglish());
//				}
			}

			return convert(str, ifx, out);
		}
		return null;
	}
	
	public byte[] convert(String str, Ifx ifx, ByteArrayOutputStream out) throws IOException {
		try {
		if (str != null) {
			int index;
			int indexP;
			String[] split = str.split("[\\[]");
			ATMTerminal atm = null;
//			Integer lineLength = null; 
//			Integer leftMargin = null;
//			String bnkFarsiName = "";
//			String bnkFarsiMount = "";
//			String bnkEnglishName = "";
//			String bnkEnglishMount = "";

			
//			Map<String, String> encodings = new HashMap<String, String>();
//			Map<String, String> convertors = new HashMap<String, String>();
			
			NDCConvertor convertor = this;
			String encoding = "";
			String extendedEncoding = "";
			ATMConfiguration atmConfiguration = null;
			
			if(ifx != null){
				//TODO check this condition?! what does it mean?!!
				// FIXME a lazyInitializeException is thrown!
//				if( !(TerminalType.ATM.equals(ifx.getEndPointTerminal().getTerminalType()))){
				if (!(ifx.getEndPointTerminal() instanceof ATMTerminal)){
					atm = TerminalService.findTerminal(ATMTerminal.class, Long.parseLong(ifx.getTerminalId()));
				}else{
					atm = (ATMTerminal) ifx.getEndPointTerminal();
				}
				
				atmConfiguration = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
				
				
//				lineLength = atmConfiguration.getReceiptLineLength();
//				leftMargin = atmConfiguration.getReceiptLeftMargin();
//				bnkFarsiName = atmConfiguration.getBnkFarsiName();
//				bnkFarsiMount = atmConfiguration.getBnkFarsiMount();
//				bnkEnglishName = atmConfiguration.getBnkEnglishName();
//				bnkEnglishMount = atmConfiguration.getBnkEnglishMount();
				
//				encodings = atmConfiguration.getEncodingMap();
//				convertors = atmConfiguration.getConvertorsMap();
//				encodings.put(NDCConvertor.FARSI_RECIEPT_ENCODING, atm.getOwnOrParentConfiguration().getFarsi_reciept_encoding());
//				encodings.put(NDCConvertor.FARSI_EXTENDED_RECIEPT_ENCODING,atm.getOwnOrParentConfiguration().getFarsi_extended_reciept_encoding());
//				encodings.put(NDCConvertor.FARSI_SCREEN_ENCODING,atm.getOwnOrParentConfiguration().getFarsi_screen_encoding());
//				encodings.put(NDCConvertor.FARSI_EXTENDED_SCREEN_ENCODING,atm.getOwnOrParentConfiguration().getFarsi_extended_screen_encoding());
//				encodings.put(NDCConvertor.ENGLISH_ENCODING,atm.getOwnOrParentConfiguration().getEnglish_encoding());
				
//				encodings.put(NDCConvertor.FARSI_RECIEPT_ENCODING, atmConfiguration.getFarsi_reciept_encoding());
//				encodings.put(NDCConvertor.FARSI_EXTENDED_RECIEPT_ENCODING, atmConfiguration.getFarsi_extended_reciept_encoding());
//				encodings.put(NDCConvertor.FARSI_SCREEN_ENCODING, atmConfiguration.getFarsi_screen_encoding());
//				encodings.put(NDCConvertor.FARSI_EXTENDED_SCREEN_ENCODING, atmConfiguration.getFarsi_extended_screen_encoding());
//				encodings.put(NDCConvertor.ENGLISH_ENCODING, atmConfiguration.getEnglish_encoding());
				
//				convertors.put(NDCConvertor.RECEIPT_CONVERTOR,atm.getOwnOrParentConfiguration().getReceiptConvertor());
//				convertors.put(NDCConvertor.SCREEN_CONVERTOR, atm.getOwnOrParentConfiguration().getScreenConvertor());
				
//				convertors.put(NDCConvertor.RECEIPT_CONVERTOR, atmConfiguration.getReceiptConvertor());
//				convertors.put(NDCConvertor.SCREEN_CONVERTOR, atmConfiguration.getScreenConvertor());
				
				

				
				/*********************/
			}
			if (Util.hasText(str)){
				int indexOfConvertor = str.indexOf("[ESC](");
				if (indexOfConvertor != -1){
					encoding = str.substring(indexOfConvertor+6, indexOfConvertor+6 + 1);
					if(atmConfiguration != null){
						convertor =  getConvertor(encoding, /*encodings*/atmConfiguration.getEncodingMap(), /*convertors*/ atmConfiguration.getConvertorsMap());
						if (encoding.equals(/*encodings.get(FARSI_RECIEPT_ENCODING)*/atmConfiguration.getEncodingMap().get(FARSI_RECIEPT_ENCODING))){
							extendedEncoding = /*encodings.get(FARSI_EXTENDED_RECIEPT_ENCODING*/atmConfiguration.getEncodingMap().get(FARSI_EXTENDED_RECIEPT_ENCODING);
						} else if (encoding.equals(/*encodings.get(FARSI_SCREEN_ENCODING)*/atmConfiguration.getEncodingMap().get(FARSI_SCREEN_ENCODING))){
							extendedEncoding = /*encodings.get(FARSI_EXTENDED_SCREEN_ENCODING)*/atmConfiguration.getEncodingMap().get(FARSI_EXTENDED_SCREEN_ENCODING);
						}
					}
				}
			}
			boolean isGroovyProcessingNeeded;
			
			for (String item : split) {
				index = item.indexOf("]");
				indexP = item.indexOf("(")==-1 ? index : item.indexOf("(");
				byte[] data = null;

				if (index != -1) {
					if (item.contains("GR")) {
						isGroovyProcessingNeeded = true;
						String groovyStr = item;
						
//						if (groovyStr.contains("dateFormat(") && !groovyStr.contains("convertor.dateFormat(")){
//							groovyStr = groovyStr.replace("dateFormat(", "convertor.dateFormat(");
//						}
//						
//						if (groovyStr.contains("appPanFa(")){
//							groovyStr = groovyStr.replace("appPanFa(", "convertor.appPanFormatFa(encodings, ");
//						}
//						
//						if (groovyStr.contains("appPanEn(")){
//							groovyStr = groovyStr.replace("appPanEn(", "convertor.appPanFormatEn(");
//						}
//						
//						if (groovyStr.contains("printAppPan") && !groovyStr.contains("convertor.printAppPan(")){
//							groovyStr = groovyStr.replace("printAppPan(", "convertor.printAppPan(");
//							
//						}
//						
//						if (groovyStr.contains("trimLeftZeros(") && !groovyStr.contains("convertor.trimLeftZeros(")){
//							groovyStr = groovyStr.replace("trimLeftZeros(", "convertor.trimLeftZeros(");
//						}
//						if (groovyStr.contains("format(") && !groovyStr.contains("convertor.format(")){
//							groovyStr = groovyStr.replace("format(", "convertor.format(");
//						}
//						if (groovyStr.contains("realChargeCredit2F(")){
//							groovyStr = groovyStr.replace("realChargeCredit2F(", "convertor.realChargeCredit('"+Language.FarsiLanguage+"', '"+encodings.get(FARSI_RECIEPT_ENCODING)+"', encodings, atm,");
//						}
//						if (groovyStr.contains("realChargeCredit2E(")){
//							groovyStr = groovyStr.replace("realChargeCredit2E(", "convertor.realChargeCredit('"+Language.EnglishLanguage+"', '"+encodings.get(ENGLISH_ENCODING)+"', encodings, atm,");
//						}
//						if (groovyStr.contains("decode(") && !groovyStr.contains("convertor.decode(")){
//							groovyStr = groovyStr.replace("decode(", "convertor.decode(");
//						}
//						if (groovyStr.contains("amount2F(")) {
//							groovyStr = groovyStr.replace("amount2F(", "convertor.accBalAvailable('"+Language.FarsiLanguage+"', '"+encodings.get(FARSI_RECIEPT_ENCODING)+"', encodings, atm,");
//						}
//						if (groovyStr.contains("amount2Fscr(")) {
//							groovyStr = groovyStr.replace("amount2Fscr(", "convertor.accBalAvailable('"+Language.FarsiLanguage+"', '"+encodings.get(FARSI_SCREEN_ENCODING)+"', encodings, atm,");
//						}
//						if (groovyStr.contains("amount2E(")) {
//							groovyStr = groovyStr.replace("amount2E(", "convertor.accBalAvailable('"+Language.EnglishLanguage+"', '"+encodings.get(ENGLISH_ENCODING)+"', encodings, atm,");
//						}
//						if (groovyStr.contains("amount2Escr(")) {
//							groovyStr = groovyStr.replace("amount2Escr(", "convertor.accBalAvailable('"+Language.EnglishLanguage+"', '"+encodings.get(ENGLISH_ENCODING)+"', encodings, atm,");
//						}
//						if (groovyStr.contains("partialDispense(") && !groovyStr.contains("convertor.partialDispense()")) {
//							groovyStr = groovyStr.replace("partialDispense(", "convertor.partialDispense(atm,");
//						}
//						if (groovyStr.contains("c2F(")){
//							groovyStr = groovyStr.replace("c2F(", "convertor.convert2Farsi(");
//						}
//						if (groovyStr.contains("c2E(")){
//							groovyStr = groovyStr.replace("c2E(", "convertor.convertToEnglish(");
//						}
//						
//						if (groovyStr.contains("putLF(") && !groovyStr.contains("convertor.putLF(")){
//							groovyStr = groovyStr.replace("putLF(", "convertor.putLF(" );
//						}
//						if (groovyStr.contains("test(") && !groovyStr.contains("convertor.test(")){
//							groovyStr = groovyStr.replace("test(", "convertor.test(" );
//						}
//						
//						if (groovyStr.contains("bankStatementTableFa(") && !groovyStr.contains("convertor.bankStatementTableFa(")){
//							groovyStr = groovyStr.replace("bankStatementTableFa(", "convertor.bankStatementTableFa(ifx, " + lineLength  + ", " + leftMargin + ", encodings");
//						}
//
//						if (groovyStr.contains("bankStatementTableEn(") && !groovyStr.contains("convertor.bankStatementTableEn(")){
//							groovyStr = groovyStr.replace("bankStatementTableEn(", "convertor.bankStatementTableEn(ifx, " + lineLength  + ", " + leftMargin );
//						}
//						
//						if (groovyStr.contains("datePersianFormat(") && !groovyStr.contains("convertor.datePersianFormat(")){
//							groovyStr = groovyStr.replace("datePersianFormat(", "convertor.datePersianFormat(" + lineLength  + ", " + leftMargin + ", ");
//						}
//						
//						if (groovyStr.contains("dateEnglishFormat(") && !groovyStr.contains("convertor.dateEnglishFormat(")){
//							groovyStr = groovyStr.replace("dateEnglishFormat(", "convertor.dateEnglishFormat(" + lineLength  + ", " + leftMargin + ", ");
//						}
//						
//						if (groovyStr.contains("subsidiaryState2F(")) {
//							groovyStr = groovyStr.replace("subsidiaryState2F(", "convertor.subsidiaryStatement2Farsi(");
//						}
//						
//						if (groovyStr.contains("accountFormat(") && !groovyStr.contains("convertor.accountFormat(")){
//							groovyStr = groovyStr.replace("accountFormat(", "convertor.accountFormat(text_dir, ");
//						}
//						
//						if (groovyStr.contains("bnkName2F(")){
//							groovyStr = groovyStr.replace("bnkName2F(", "convertor.bnkFarsiName('"+bnkFarsiName+"'");
//						}
//						
//						if (groovyStr.contains("bnkName2E(")){
//							groovyStr = groovyStr.replace("bnkName2E(", "convertor.bnkEnglishName('"+bnkEnglishName+"'");
//						}
//						
//						if (groovyStr.contains("bnkMount2F(")){
//							groovyStr = groovyStr.replace("bnkMount2F(", "convertor.bnkFarsiMount('"+bnkFarsiMount+"'");
//						}
//						
//						if (groovyStr.contains("bnkMount2E(")){
//							groovyStr = groovyStr.replace("bnkMount2E(", "convertor.bnkEnglishMount('"+bnkEnglishMount+"'");
//						}
//						
//						if (groovyStr.contains("right(")){
//							groovyStr = groovyStr.replace("right(", "convertor.alignTextRight(text_dir," + lineLength  + ", " + leftMargin + ", ");
//						}
//						
//						if (groovyStr.contains("center(") && !groovyStr.contains("convertor.center")){
//							groovyStr = groovyStr.replace("center(", "convertor.center(text_dir,"+ lineLength  + ", " + leftMargin + ", ");
//						}
//						
//						if (groovyStr.contains("justify(") && !groovyStr.contains("convertor.justify(")){
//							groovyStr = groovyStr.replace("justify(", "convertor.justify(text_dir,"+ lineLength  + ", " + leftMargin + ", ");
//						}
//
//						if (groovyStr.contains("hr(")){
//							groovyStr = groovyStr.replace("hr(", "convertor.horizontalLine(" + lineLength  + ", " + leftMargin + ", ");
//						}
//
//						if (groovyStr.contains("safeEn(")&& !groovyStr.contains("convertor.safeEn(")){
//							groovyStr = groovyStr.replace("safeEn(", "convertor.safeEn(");
//						}
						
						//Mirkamali(Task175): Restriction
						if (groovyStr.contains("typeOfRestriction_Farsi(")&& !groovyStr.contains("convertor.typeOfRestriction_Farsi(")){
							groovyStr = groovyStr.replace("typeOfRestriction_Farsi(", "convertor.typeOfRestriction_Farsi(");
						}
						if (groovyStr.contains("cycleTypeOfRestriction_Farsi(")&& !groovyStr.contains("convertor.cycleTypeOfRestriction_Farsi(")){
							groovyStr = groovyStr.replace("cycleTypeOfRestriction_Farsi(", "convertor.cycleTypeOfRestriction_Farsi(");
						}
						if (groovyStr.contains("typeOfRestriction_English(")&& !groovyStr.contains("convertor.typeOfRestriction_English(")){
							groovyStr = groovyStr.replace("typeOfRestriction_English(", "convertor.typeOfRestriction_English(");
						}
						if (groovyStr.contains("cycleTypeOfRestriction_English(")&& !groovyStr.contains("convertor.cycleTypeOfRestriction_English(")){
							groovyStr = groovyStr.replace("cycleTypeOfRestriction_English(", "convertor.cycleTypeOfRestriction_English(");
						}
						//gholami
//						if(groovyStr.contains("bankStatementScreenTableFa(")){
////							atmConfiguration
//							Map<String, String> encodings = atmConfiguration.getEncodingMap();
////							convertors = atmConfiguration.getConvertorsMap();
////							encodings.put(NDCConvertor.FARSI_RECIEPT_ENCODING, atm.getOwnOrParentConfiguration().getFarsi_reciept_encoding());
////							encodings.put(NDCConvertor.FARSI_EXTENDED_RECIEPT_ENCODING,atm.getOwnOrParentConfiguration().getFarsi_extended_reciept_encoding());
////							encodings.put(NDCConvertor.FARSI_SCREEN_ENCODING,atm.getOwnOrParentConfiguration().getFarsi_screen_encoding());
//							encodings.put(NDCConvertor.FARSI_EXTENDED_SCREEN_ENCODING,atmConfiguration.getFarsi_extended_screen_encoding());
//							data = bankStatementScreenTableFa(ifx);
////							bankStatementTableFa
//							isGroovyProcessingNeeded = false;
//						}
						if(groovyStr.contains("simpleBalanceReceiptFa(")){
							data = simpleBalanceReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
						} 
						else if(groovyStr.contains("simpleCharTable(")){
							data = simpleCharTable(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;

						}
						else if (groovyStr.contains("simpleBalanceJournal(")){
							data = simpleBalanceJournal(ifx);
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleBankStatementReceiptFa(")){
							data = simpleBankStatementReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleThirdPartyPaymentReceiptFa(")){
							data = simpleThirdPartyPaymentReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
							
						}
						else if(groovyStr.contains("simpleThirdPartyPaymentJournal(")){
							data = simpleThirdPartyPaymentJournal(ifx);
							isGroovyProcessingNeeded = false;
							
						}
						//Mirkamali(Task175): Restriction
						else if(groovyStr.contains("simpleRestrictionReceiptFa(")) {
							 data = simpleRestrictionReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							 isGroovyProcessingNeeded = false;
						}
						else if(groovyStr.contains("simpleRestrictionJournal(")) {
							data = simpleRestrictionJournal(ifx);
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleBankStatementJournalReceipt(")){
							data = simpleBankStatementJournal(ifx);
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleBillPaymentReceiptFa(")){
							data = simpleBillPaymentReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleBillPaymentJournal(")){
							data = simpleBillPaymentJournal(ifx);
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleTransferReceiptFa(")){
							data = simpleTransferReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
						}
						else if(groovyStr.contains("simpleTransferJournal(")){
							data = simpleTransferJournal(ifx);
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleChangePinReceiptFa(")){
							data = simpleChangePinReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleChangePinJournal(")){
							data = simpleChangePinJournal(ifx);
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleChangeInternetPinReceiptFa(")){
							data = simpleChangeInternetPinReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleChangeInternetPinJournal(")){
							data = simpleChangeInternetPinJournal(ifx);
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleCreditStatementDataReceiptFa(")){
							data = simpleCreditStatementDataReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleCreditStatementDataJournal(")){
							data = simpleCreditStatementDataJournal(ifx);
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleNotAcceptableRequestReceiptFa(")){
							data = simpleNotAcceptableRequestReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleWithdrawalReceiptFa(")){
							data = simpleWithdrawalReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleWithdrawalJournal(")){
							data = simpleWithdrawalJournal(ifx);
							isGroovyProcessingNeeded = false;
						}
						//Mirkamali(Task179): Currency ATM
						else if (groovyStr.contains("simpleWithdrawalCurReceiptFa(")){
							data = simpleWithdrawalCurReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleWithdrawalCurJournal(")){
							data = simpleWithdrawalCurJournal(ifx);
							isGroovyProcessingNeeded = false;
						}
						/*****************************************************************************************/
						else if (groovyStr.contains("simplePartialDispenceReceiptFa(")){
							data = simplePartialDispenceReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simplePartialDispenceErrorReceiptFa(")){
							data = simplePartialDispenceErrorReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simplePartialDispenceJournal(")){
							data = simplePartialDispenceJournal(ifx);
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simplePurchaseMTNChargeReceiptFa(")){
							data = simplePurchaseMTNChargeReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
						}
//						else if (groovyStr.contains("simplePurchaseMTNChargeJournal(")){
//							logger.debug("groovyStr: " + groovyStr);
//							data = simplePurchaseMTNChargeJournal(ifx);
//							isGroovyProcessingNeeded = false;
//						}
						else if (groovyStr.contains("simplePurchaseMCIChargeReceiptFa(")){
							data = simplePurchaseMCIChargeReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simplePurchaseRightelChargeReceiptFa(")){
							data = simplePurchaseRightelChargeReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simplePurchaseTaliaChargeReceiptFa(")){
							data = simplePurchaseTaliaChargeReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simplePurchaseChargeJournal(")){
							data = simplePurchaseChargeJournal(ifx);
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleOnlineBillPaymentReceiptFa(")){
							data = simpleOnlineBillPaymentReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleOnlineBillPaymentJournal(")){
							data = simpleOnlineBillPaymentJournal(ifx);
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleOnlineBillPaymentTrackingReceiptFa(")){
							data = simpleOnlineBillPaymentTrackingReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
						}
						else if (groovyStr.contains("simpleCardCaptureReceiptFa(")){
							data = simpleCardCaptureReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
							
						} else if (groovyStr.contains("simpleTransferToAccountReceiptFa(")) {
							data = simpleTransferToAccountReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
							
						} else if (groovyStr.contains("simpleShebaReceiptFa(")) { 
							
							data = simpleShebaReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							isGroovyProcessingNeeded = false;
							
						//TASK Task019 : Receipt Option
						} else if (groovyStr.contains("simpleShebaJournal(")) { 
							data = simpleShebaJournal(ifx);
							isGroovyProcessingNeeded = false;
							
						}	
						
						//TASK Task081 : ATM Saham Feature
						 else if (groovyStr.contains("simpleStockReceiptFa(")) { 
							 	data = simpleStockReceiptFa(atm, ifx, atmConfiguration.getEncodingMap());
							 	isGroovyProcessingNeeded = false;
						 }
						//TASK Task081 : ATM Saham Feature
						 else if (groovyStr.contains("simpleStockReceiptEn(")) { 
								data = simpleStockReceiptEn(atm, ifx, atmConfiguration.getEncodingMap());
								isGroovyProcessingNeeded = false;
						 }						
						//TASK Task081 : ATM Saham Feature
						 else if (groovyStr.contains("simpleStockJournal(")) { 
								data = simpleStockJournal(ifx);
								isGroovyProcessingNeeded = false;
						}
						
						if(isGroovyProcessingNeeded){
							Binding scriptBinding = new Binding();
							
							scriptBinding.setProperty("ifx", ifx);
								
							scriptBinding.setProperty("atm", atm);
							scriptBinding.setProperty("convertor", convertor);
							
							if (groovyStr.contains("text_dir")) {
								if (getEncoding(str, item).equals(/*encodings*/atmConfiguration.getEncodingMap().get(NDCConvertor.ENGLISH_ENCODING))) {
									scriptBinding.setProperty("text_dir", TextDirection.LeftToRight);
								} else
									scriptBinding.setProperty("text_dir", TextDirection.RightToLeft);
							}
							
							if (groovyStr.contains("encodings")) {
								scriptBinding.setProperty("encodings", /*encodings*/atmConfiguration.getEncodingMap());
							}
							
							groovyStr = groovyStr.replaceAll("GR ", "");
							groovyStr = groovyStr.substring(0, groovyStr.length()-1);

							Object run = GlobalContext.getInstance().evaluateScript(groovyStr, scriptBinding);
							
							if (run == null)
								data = " ".getBytes();
							else if (run instanceof String) {
								data = ((String) run).getBytes();
							} else if (run instanceof byte[]){
								data = (byte[]) run;
							} else {
								data = run.toString().getBytes();
							}
						}
					} else if (item.substring(0, indexP).length() > 5) {
						data = item.substring(5, indexP).getBytes();
					} else if(item.startsWith("0x")){
						String value = item.substring(2, index);
						data = Hex.decode(value);
					}else{
						String value = item.substring(0, index);
						data = new byte[] { ASCIIConstants.getValue(value) };
					}
					
//					logger.debug("Reciept: "+new String(Hex.encode(data)));
					if (data != null) {
						out.write(convertor.finalize(data, encoding, extendedEncoding));
						out.write(item.substring(index+1).getBytes());
					}
					
				} else {
					if (item.equals(split[0])) {
						if (item.length() > 0 && str.startsWith("["))
							out.write("[".getBytes());
					} else
						out.write("[".getBytes());
					out.write(item.getBytes());
				}
			}
			return out.toByteArray();
		}
		} catch(Exception e) {
			logger.error("Exceprtion in convert receipt, " + e, e);
		}
		return null;
	}
	//	private byte[] convert2Farsi(NDCConvertor convertor, OrganizationType data) {
//		 return convert2Farsi(convertor, data.toString().getBytes());
//	}
	
	public byte[] convert2Farsi(OrganizationType data) {
		if (data == null)
			return " ".getBytes();
		return convert2Farsi(data.toString().getBytes());
	}
	public byte[] convert2Farsi(OnlineBillPaymentStatus onbill){
		if(onbill == null)
			return " ".getBytes();
		return convert2Farsi(onbill.toString().getBytes());
	}
	
	/*private byte[] convert2Farsi(NDCConvertor convertor, Long data) {
		return convert2Farsi(convertor, data.toString().getBytes());
	}*/
	
	public byte[] convert2Farsi(Long data) {
		if (data == null)
			return " ".getBytes();
		return convert2Farsi(data.toString().getBytes());
	}
	
	/*protected byte[] convert2Farsi(NDCConvertor convertor, String data) {
		return convert2Farsi(convertor, data.getBytes());
	}*/
	
	public byte[] convert2Farsi(String data) {
		if (!Util.hasText(data))
			data = " ";
		return convert2Farsi(data.getBytes());
//		return convert2Farsi(data.replace(",", "و").getBytes());//TASK Task002 : Transfer Card To Account && TASK Task012 : Transfer Card To Card Recipt Bug
		//AldComment Task002 : Older version before add Transfer Card To Account feature 	return convert2Farsi(data.getBytes());
	}
/*public byte[] convert2Farsi(NDCConvertor convertor, byte[] data) {
		if (data != null) {
			byte[] convert;
			String value = new String(data);
			if (convertor ==null)
				convertor = this;
				
			convert = convertor.encode(value);
			if (convert != null){
				data = reverse(convert);
				data = reverseNumbers(data);
			}
		}
		return data;
	}*/

	public byte[] accNumbertConvert2Farsi(String data){
		if (!Util.hasText(data))
			data = " ";
		return accNumbertConvert2Farsi(data.getBytes());
	}
	public byte[] accNumbertConvert2Farsi(byte[] data){
		if (data != null) {
			byte[] convert;
			String value = new String(data);
			data = super.encode(value);
		}
		return data;
	}
	public byte[] convert2Farsi(byte[] data) {
		if (data != null) {
			byte[] convert;
			String value = new String(data);
			convert = super.encode(value);
			if (convert != null){
				data = reverse(convert);
				data = reverseNumbers(data);
			}
		}
		return data;
	}

	public byte[] subsidiaryStatement2Farsi(String subsidiaryAcct, String statement){
		if (!Util.hasText(subsidiaryAcct))
			statement =""; 
		return statement.getBytes();
	}
	
	public byte[] subsidiaryStatement2EnglishForNCR(String subsidiaryAcct, String statement){
		if (!Util.hasText(subsidiaryAcct))
			statement ="";

		return convertToEnglishForNCR(statement);
	}
	
	public byte[] accountFormat(TextDirection text_direction, String data) {
		if (data == null)
			data = "";

		if (!data.contains(".")){
			if (Util.hasText(data) && data.length()==18){
//				if (TextDirection.LeftToRight.equals(text_direction))
					data = Util.trimLeftZeros(data.substring(0, 4))+"-"+ Util.trimLeftZeros(data.substring(4, 7))+"-"+ Util.trimLeftZeros(data.substring(7,15))+"-"+ Util.trimLeftZeros(data.substring(15));
//				else
//					data = Util.trimLeftZeros(data.substring(15))+"-"+ Util.trimLeftZeros(data.substring(7,15))+"-"+ Util.trimLeftZeros(data.substring(4, 7))+ "-"+ Util.trimLeftZeros(data.substring(0, 4));
			}
		}else{
			data = data.replaceAll("\\.", "-");
		}
		
		return data.getBytes();
	}
	
	protected byte[] reverse(byte[] convert) {
		if (convert == null)
			return null;
		byte[] result = new byte[convert.length];
		int j = 0;
		for (int i = convert.length - 1; i >= 0; i--, j++) {
			result[j] = convert[i];
		}
		return result;
	}

	protected byte[] reverseNumbers(byte[] convert){
		if (convert == null)
			return null;

		byte[] result = new byte[convert.length];
		int j=0;
		int k=0;
		
		for(int i=0; i<convert.length; i++){
			if(isInNumberRange(convert[i])){
				k++;
			}else{
				if( k!=0 ){
					for(int c=0; c<k; c++){
						result[j] = convert[i-1-c];
						j++;
					}
					k = 0;
				}
				result[j] = convert[i];
				j++;
			}
		}
		
		if( k!=0 ){
			for(int c=0; c<k; c++){
				result[j] = convert[convert.length-1-c];
				j++;
			}
			k = 0;
		}

		return result;
	}
	
	@Override
	public Map<Integer, Character> getIndexesOfFarsiChars() {
		return null;
	}
	

	public byte[] dateEnglishFormat(int receiptLineLength, int receiptLeftMargin, DateTime objDateTime) {
//		MyDateFormat dateFormatYYYYMMDDKK = new MyDateFormat("yyyy/MM/dd HH:mm:ss");
		String strDate = MyDateFormatNew.format("yyyy/MM/dd HH:mm:ss", objDateTime.toDate());
		String[] strDt = strDate.split(" ");
		byte[] bDate = justify(TextDirection.LeftToRight, receiptLineLength, receiptLeftMargin, strDt[1].getBytes(), strDt[0].getBytes());
		return bDate;
	}

	public byte[] dateEnglishNCRFormat(int receiptLineLength, int receiptLeftMargin, DateTime objDateTime) {
//		MyDateFormat dateFormatYYYYMMDDKK = new MyDateFormat("yyyy/MM/dd HH:mm:ss");
		String strDate = MyDateFormatNew.format("yyyy/MM/dd HH:mm:ss", objDateTime.toDate());
		String[] strDt = strDate.split(" ");
		byte[] bDate = justify(TextDirection.LeftToRight, receiptLineLength, receiptLeftMargin, strDt[1].getBytes(), strDt[0].getBytes());
		for (int i = 0; i < bDate.length; i++) {
			if ((char) bDate[i] == '/') {
				bDate[i] = NCR_RECEIPT_SLASH_CHAR;
			} else if ((char) bDate[i] == ':') {
				bDate[i] = NCR_RECEIPT_COLON_CHAR;
			}
		}

		return bDate;
	}
	
	/*
	 * public byte[] datePersianFormat(int receiptLineLength, int receiptLeftMargin, DateTime objDateTime){
		PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd HHmmss");
		String strDate = dateFormatPers.format(objDateTime.toDate());
		
		String[] strDt = strDate.split(" ");
		byte[] convertedDt = convert2Farsi(strDt[0]);
		byte[] convertedTime = convert2Farsi(strDt[1]);
		
		byte[] goodDt = new byte[2*7+convertedDt.length];
		int index=0;
		int i = 0;
		goodDt[index++] = convertedDt[i++]; 
		goodDt[index++] = convertedDt[i++]; 
		goodDt[index++] = convertedDt[i++]; 
		goodDt[index++] = convertedDt[i++];
		goodDt[index++] = ASCIIConstants.ESC; goodDt[index++] = '('; goodDt[index++] = '1';
		goodDt[index++] = '/';  
		goodDt[index++] = ASCIIConstants.ESC; goodDt[index++] = '('; goodDt[index++] = '7';
		goodDt[index++] = convertedDt[i++]; 
		goodDt[index++] = convertedDt[i++];
		goodDt[index++] = ASCIIConstants.ESC; goodDt[index++] = '('; goodDt[index++] = '1';
		goodDt[index++] = '/'; 
		goodDt[index++] = ASCIIConstants.ESC; goodDt[index++] = '('; goodDt[index++] = '7';
		goodDt[index++] = convertedDt[i++]; 
		goodDt[index++] = convertedDt[i++];


		byte[] goodTime = new byte[2*7+convertedTime.length];
		index=0;
		i=0;
		goodTime[index++] = convertedTime[i++]; 
		goodTime[index++] = convertedTime[i++];
		goodTime[index++] = ASCIIConstants.ESC; goodTime[index++] = '('; goodTime[index++] = '1';
		goodTime[index++] = ':'; 
		goodTime[index++] = ASCIIConstants.ESC; goodTime[index++] = '('; goodTime[index++] = '7';
		goodTime[index++] = convertedTime[i++]; 
		goodTime[index++] = convertedTime[i++];
		goodTime[index++] = ASCIIConstants.ESC; goodTime[index++] = '('; goodTime[index++] = '1';
		goodTime[index++] = ':';
		goodTime[index++] = ASCIIConstants.ESC; goodTime[index++] = '('; goodTime[index++] = '7';
		goodTime[index++] = convertedTime[i++]; 
		goodTime[index++] = convertedTime[i++];

		byte[] bDate = justify(TextDirection.RightToLeft, receiptLineLength, receiptLeftMargin, goodTime, goodDt);
		return bDate;
	}*/
	public byte[] datePersianFormat(int receiptLineLength, int receiptLeftMargin, ATMTerminal atm, DateTime objDateTime){
		byte[] goodDt;
		byte[] goodTime;

		if(ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId()).getReceiptDetailsMap().containsKey("P$"+objDateTime.getDayDate())){
			goodDt = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId()).getReceiptDetailsMap().get("P$"+objDateTime.getDayDate());
		}else{
			PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
			String strDate = dateFormatPers.format(objDateTime.toDate());

//			String[] strDt = strDate.split(" ");
			byte[] convertedDt = convert2Farsi(strDate);

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				goodDt = new byte[2*7+convertedDt.length];
			} else {
				goodDt = new byte[2+convertedDt.length];
			}
			int index=0;
			int i = 0;
			goodDt[index++] = convertedDt[i++];
			goodDt[index++] = convertedDt[i++];
			goodDt[index++] = convertedDt[i++];
			goodDt[index++] = convertedDt[i++];
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				goodDt[index++] = ASCIIConstants.ESC; goodDt[index++] = '('; goodDt[index++] = '1';
			}
			goodDt[index++] = (byte) ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_SLASH_CHAR : NCR_RECEIPT_SLASH_CHAR);
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				goodDt[index++] = ASCIIConstants.ESC; goodDt[index++] = '('; goodDt[index++] = '7';
			}
			goodDt[index++] = convertedDt[i++];
			goodDt[index++] = convertedDt[i++];
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				goodDt[index++] = ASCIIConstants.ESC; goodDt[index++] = '('; goodDt[index++] = '1';
			}
			goodDt[index++] = (byte) ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_SLASH_CHAR : NCR_RECEIPT_SLASH_CHAR);
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				goodDt[index++] = ASCIIConstants.ESC; goodDt[index++] = '('; goodDt[index++] = '7';
			}
			goodDt[index++] = convertedDt[i++];
			goodDt[index++] = convertedDt[i++];

			ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId()).setReceiptDetailsMap("P$"+objDateTime.getDayDate(), goodDt);
		}
		

//		if(receiptDetailsMap.containsKey("S$"+objDateTime.getDayTime())){
//			goodTime = receiptDetailsMap.get("S$"+objDateTime.getDayTime());
//		}else{
			String strTime = objDateTime.getDayTime().toString2();
			byte[] convertedTime = convert2Farsi(strTime);

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				goodTime = new byte[2*7+convertedTime.length];
			} else {
				goodTime = new byte[2+convertedTime.length];
			}
			int index=0;
			int i=0;
			goodTime[index++] = convertedTime[i++];
			goodTime[index++] = convertedTime[i++];
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				goodTime[index++] = ASCIIConstants.ESC; goodTime[index++] = '('; goodTime[index++] = '1';
			}
			goodTime[index++] = (byte) ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_COLON_CHAR : NCR_RECEIPT_COLON_CHAR);
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				goodTime[index++] = ASCIIConstants.ESC; goodTime[index++] = '('; goodTime[index++] = '7';
			}
			goodTime[index++] = convertedTime[i++];
			goodTime[index++] = convertedTime[i++];
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				goodTime[index++] = ASCIIConstants.ESC; goodTime[index++] = '('; goodTime[index++] = '1';
			}
			goodTime[index++] = (byte) ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_COLON_CHAR : NCR_RECEIPT_COLON_CHAR);
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				goodTime[index++] = ASCIIConstants.ESC; goodTime[index++] = '('; goodTime[index++] = '7';
			}
			goodTime[index++] = convertedTime[i++];
			goodTime[index++] = convertedTime[i++];

//			receiptDetailsMap.put("S$"+objDateTime.getDayTime(), goodTime);
//		}


		byte[] bDate = justify(TextDirection.RightToLeft, receiptLineLength, receiptLeftMargin, goodTime, goodDt);
		return bDate;
	}
	
	public String dateFormat(DateTime objDateTime){
		DayDate dayDate = objDateTime.getDayDate();
		String dateStr = dayDate.toString();
		dateStr = dateStr.replaceAll("/", "-");
		return dateStr + " " + objDateTime.getDayTime().toString().replaceAll(":", "-");
	}

	public byte[] appPanFormatFa(Map<String, String> encodings, ATMTerminal atm, String appPan) {
		byte[] starsTr;
		if (atm.getProducer() != null && atm.getProducer().equals(ATMProducer.NCR)) {
			starsTr = new byte[] {NCR_RECEIPT_STAR_CHAR, NCR_RECEIPT_STAR_CHAR, DASH_CHAR, NCR_RECEIPT_STAR_CHAR, NCR_RECEIPT_STAR_CHAR, NCR_RECEIPT_STAR_CHAR, NCR_RECEIPT_STAR_CHAR, DASH_CHAR};
		} else {
			starsTr = "**-****-".getBytes();
		}

		if (Util.hasText(appPan) && appPan.contains("\\.")){
			return accountFormat(TextDirection.RightToLeft, appPan);
		}

		ByteArrayOutputStream result = new ByteArrayOutputStream();
		/**What is this for????Test it....***/
//		if (Util.hasText(appPan) && appPan.contains("\\.")){
//			return accountFormat(TextDirection.RightToLeft, appPan);
//		}
		String key = appPan.substring(0,4) + "-" + appPan.substring(4,6) + new String(starsTr);
		try {
			if(ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId()).getReceiptDetailsMap().containsKey(key)){
				result.write(ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId()).getReceiptDetailsMap().get(key));
			}else{
				result.write(convert2Farsi(appPan.substring(0, 4)));
				result.write(convert2Farsi(appPan.substring(4, 6)+"-"));
				if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
					result.write(ASCIIConstants.ESC);
					result.write(("("+encodings.get(NDCConvertor.ENGLISH_ENCODING)).getBytes());
				}
				result.write(starsTr);
				ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId()).setReceiptDetailsMap(key, result.toByteArray());
			}

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				result.write(ASCIIConstants.ESC);
				result.write(("("+encodings.get(NDCConvertor.FARSI_RECIEPT_ENCODING)).getBytes());
			}

			result.write(convert2Farsi(appPan.substring(appPan.length()-4)));

		}catch (IOException e) {
			logger.error(e);
		}

		return result.toByteArray();
	}
	
	public byte[] printAppPan(String appPan){
		if (Util.hasText(appPan) && appPan.contains("."))
			return accountFormat(TextDirection.RightToLeft, appPan);
		return appPan.getBytes();
	}
	
	/************Mirkamali(Task175): MaxAmtOnTransaction****************/
	public byte[] typeOfRestriction_Farsi(String bufferB){
		return RestrictionOnTrxAndTermType.getFarsiName(bufferB).getBytes();
	}
	
	public byte[] typeOfRestriction_English(String bufferB){
		return RestrictionOnTrxAndTermType.getEnglishName(bufferB).getBytes();
	}
	
	public String cycleTypeOfRestriction_Farsi(String bufferC) {
		return new CycleType().getFarsiName(bufferC);
	}
	
	public String cycleTypeOfRestriction_English(String bufferC) {
		return new CycleType().getEnglishName(bufferC);
	}
	/*******************************************************************/
	public String appPanFormatEn(String appPan) {
		String starsTr = "**-****-";

		if (Util.hasText(appPan) && appPan.contains(".")){
			return appPan.replaceAll("\\.", "-");
		}
//		return "****-****-****-"+appPan.substring(appPan.length()-4);
		return appPan.substring(0, 4) + "-" + appPan.substring(4, 6) + starsTr + appPan.substring(appPan.length()-4);

	}

	public byte[] appPanFormatEnForNCR(String appPan) {
		if (Util.hasText(appPan) && appPan.contains(".")){
			return appPan.replaceAll("\\.", "-").getBytes();
		}

		byte[] starsTr = new byte[] {NCR_RECEIPT_STAR_CHAR, NCR_RECEIPT_STAR_CHAR, DASH_CHAR, NCR_RECEIPT_STAR_CHAR, NCR_RECEIPT_STAR_CHAR, NCR_RECEIPT_STAR_CHAR, NCR_RECEIPT_STAR_CHAR, DASH_CHAR};

		ByteArrayOutputStream result = new ByteArrayOutputStream();

		try {
			result.write(appPan.substring(0, 4).getBytes());
			result.write("-".getBytes());
			result.write(appPan.substring(4, 6).getBytes());
			result.write(starsTr);
			result.write(appPan.substring(appPan.length() - 4).getBytes());
		} catch (IOException e) {
			logger.error(e);
		}

		return result.toByteArray();
	}

	public String trimLeftZeros(String s) {
		return Util.trimLeftZeros(s);
	}
	
	public byte[] accBalAvailable(String lang, String convertor, Map<String, String> encodings, ATMTerminal atm, Long amt, int maxCh) {
		return accBalAvailable(lang, convertor, encodings, atm, amt.toString(), null, maxCh);
	}

	public byte[] accBalAvailable(String lang, String convertor, Map<String, String> encodings, ATMTerminal atm, String amt, int maxCh) {
		return accBalAvailable(lang, convertor, encodings, atm, amt, null, maxCh);
	}

	public byte[] accBalAvailable(String lang, String convertor, Map<String, String> encodings, ATMTerminal atm, String amt, Ifx ifx, int maxCh) {
		if (!Util.hasText(amt))
			return new byte[]{};
		convertor = "("+convertor;

		String debitCredit = amt.substring(0, 1).toUpperCase();
		String realAmount = amt;
		if ("D".equals(debitCredit)) {
			debitCredit = "-";
			realAmount = amt.substring(1);

		} else if ("C".equals(debitCredit)) {
			debitCredit = "";
			debitCredit = "";
			realAmount = amt.substring(1);

		} else {
			debitCredit = "";
			realAmount = amt;
		}

		if (ifx != null) {
			Long longAmount = Long.parseLong(realAmount);
			longAmount += ifx.getAuth_Amt();
			longAmount -= ATMTerminalService.getDispenseAmount(atm, ifx);
			realAmount = longAmount.toString();
		}
		
		String rialStr;
		
		if(Language.FarsiLanguage.toString()/*faLang*/.equals(lang.toLowerCase())){
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				rialStr = convertor.equals(("("+encodings.get(NDCConvertor.FARSI_RECIEPT_ENCODING))) ? " ریال" :"" ;
				return replaceComma(convertor, reverseNumbers(convert2Farsi(format(amountFormatFarsi(Util.trimLeftZeros(realAmount))+debitCredit + rialStr, maxCh+5, "RIGHT"))), encodings.get(ENGLISH_ENCODING));
			} else {
				rialStr = !convertor.equals(("("+encodings.get(NDCConvertor.FARSI_SCREEN_ENCODING))) ? " ریال" :"" ;
				return replaceCommaForNCR(reverseNumbers(convert2Farsi(format(amountFormatFarsi(Util.trimLeftZeros(realAmount)) + debitCredit + rialStr, maxCh+5, "RIGHT"))));
			}
		}else{
			rialStr = convertor.equals(("("+encodings.get(NDCConvertor.ENGLISH_ENCODING))) ? " RIALS" : "";
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				return format(amountFormat(debitCredit+Util.trimLeftZeros(realAmount)) + rialStr, maxCh+5, "RIGHT").getBytes();
			} else {
				amt = format(amountFormat(debitCredit+Util.trimLeftZeros(realAmount)) + rialStr, maxCh+5, "RIGHT");
				return replaceCommaForNCR(convertToEnglishForNCR(amt));
			}
		}
	}
	
	//Mirkamali(Task179): Currency ATM
	public byte[] accBalAvailableCurrency(String lang, String convertor, Map<String, String> encodings, ATMTerminal atm, Long amt, int maxCh) {
		return accBalAvailableCurrency(lang, convertor, encodings, atm, amt.toString(), null, maxCh);
	}
	
	//Mirkamali(Task179): Currency ATM
	public byte[] accBalAvailableCurrency(String lang, String convertor, Map<String, String> encodings, ATMTerminal atm, String amt, Ifx ifx, int maxCh) {
		if (!Util.hasText(amt))
			return new byte[]{};
		convertor = "("+convertor;

		String debitCredit = amt.substring(0, 1).toUpperCase();
		String realAmount = amt;
		if ("D".equals(debitCredit)) {
			debitCredit = "-";
			realAmount = amt.substring(1);

		} else if ("C".equals(debitCredit)) {
			debitCredit = "";
			debitCredit = "";
			realAmount = amt.substring(1);

		} else {
			debitCredit = "";
			realAmount = amt;
		}

		if (ifx != null) {
			Long longAmount = Long.parseLong(realAmount);
			longAmount += ifx.getAuth_Amt();
			longAmount -= ATMTerminalService.getDispenseAmount(atm, ifx);
			realAmount = longAmount.toString();
		}
		
		String rialStr;
		
		String curNameFa = " ریال";
		String curNameEn = " RIALS";
		if(atm.getCurrency() != null && Util.hasText(atm.getCurrency().getCurNameFa()))
			curNameFa = " " + atm.getCurrency().getCurNameFa();
		if(atm.getCurrency() != null && Util.hasText(atm.getCurrency().getCurNameEn()))
			curNameEn = " " + atm.getCurrency().getCurNameEn();
		
		if(Language.FarsiLanguage.toString()/*faLang*/.equals(lang.toLowerCase())){
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				rialStr = convertor.equals(("("+encodings.get(NDCConvertor.FARSI_RECIEPT_ENCODING))) ? curNameFa :"" ;
				return replaceComma(convertor, reverseNumbers(convert2Farsi(format(amountFormatFarsi(Util.trimLeftZeros(realAmount))+debitCredit + rialStr, maxCh+5, "RIGHT"))), encodings.get(ENGLISH_ENCODING));
			} else {
				rialStr = !convertor.equals(("("+encodings.get(NDCConvertor.FARSI_SCREEN_ENCODING))) ? curNameFa :"" ;
				return replaceCommaForNCR(reverseNumbers(convert2Farsi(format(amountFormatFarsi(Util.trimLeftZeros(realAmount)) + debitCredit + rialStr, maxCh+5, "RIGHT"))));
			}
		}else{
			rialStr = convertor.equals(("("+encodings.get(NDCConvertor.ENGLISH_ENCODING))) ? curNameEn : "";
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				return format(amountFormat(debitCredit+Util.trimLeftZeros(realAmount)) + rialStr, maxCh+5, "RIGHT").getBytes();
			} else {
				amt = format(amountFormat(debitCredit+Util.trimLeftZeros(realAmount)) + rialStr, maxCh+5, "RIGHT");
				return replaceCommaForNCR(convertToEnglishForNCR(amt));
			}
		}
	}
	
	protected byte[] replaceComma(String convertor, byte[] number,String englishEncoding) {
		byte[] comma = convert2Farsi(",");
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		
		for(int i=0; i<number.length; i++){ 
			if(number[i] == comma[0]){
				result.write(ASCIIConstants.ESC);
				try {
					result.write(("("+englishEncoding).getBytes());
					result.write(",".getBytes());
					result.write(ASCIIConstants.ESC);
					result.write(convertor.getBytes());
				} catch (IOException e) {
					logger.error(e);
				}
			}else{
				result.write(number[i]);
			}
		}
		
		return result.toByteArray();		
	}

	protected byte[] replaceCommaForNCR(byte[] number) {
		byte[] comma = convert2Farsi(",");
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		for(int i=0; i<number.length; i++){
			if(number[i] == comma[0]){
				result.write(NCR_RECEIPT_COMMA_CHAR);
			}else{
				result.write(number[i]);
			}
		}

		return result.toByteArray();
	}
	
	public byte[] realChargeCredit(String lang, String convertor, Map<String, String> encodings, ATMTerminal atm, Long credit, Long companyCode, int maxCh) {
		return accBalAvailable(lang, convertor, encodings, atm, MTNChargeService.getRealChargeCredit(credit, companyCode).toString(), null, maxCh);
	}
	
	public Long partialDispense(ATMTerminal atm, Ifx ifx) {
		return ATMTerminalService.getDispenseAmount(atm, ifx);
	}
	
	protected String amountFormat(String amount) {
		int length = amount.length();
		
		String result = amount;
		if (length > 3) {
			int left = length%3;
			result = amount.substring(0, left);
			for (int i = left; i < length; i += 3) {
				if (Util.hasText(result))
					result += ",";
				result += amount.substring(i, i+3);
			}
		}
		
		return result;
	}

	protected String amountFormatFarsi(String amount) {
		int length = amount.length();

		String result = amount;
		if (length > 3) {
			int left = length%3;
			result = amount.substring(0, left);
			for (int i = left; i < length; i += 3) {
				if (Util.hasText(result))
					result += ",";
				result += amount.substring(i, i+3);
			}
		}
		
		String resultFa = ""; 
		for(int i=result.length()-1; i>=0; i--)
			resultFa += result.charAt(i);
		
		return resultFa;
	}

	public String format(OrganizationType s, int maxCh, String just) {
		return format(s.toString(), maxCh, just);
	}
	
	public String format(Long s, int maxCh, String just) {
		return format(s.toString(), maxCh, just);
	}
	
	public String format(String s, int maxCh, String just) {
		int justify = StringFormat.JUST_CENTER;
		if (just.contains("LEFT"))
			justify = StringFormat.JUST_LEFT;
		else if (just.contains("RIGHT"))
			justify = StringFormat.JUST_RIGHT;
		return StringFormat.formatNew(maxCh, justify, s);
	}

	
	public byte[] decode(String data) {
		byte[] actualPIN = null;
		try {
			actualPIN = SecurityComponent.rsaDecrypt(Hex.decode(data));
		} catch (Exception e) {
		}
		return actualPIN;
	}
	
	public byte[] alignTextRight(TextDirection direction, Integer receiptLineLength, Integer receiptLeftMargin, String str){
		if (str == null){
			return new byte[]{0x20};
		}	
		return alignTextRight(direction, receiptLineLength, receiptLeftMargin, str.getBytes());
	}
	public byte[] oldalignTextRight(TextDirection direction, Integer receiptLineLength, Integer receiptLeftMargin, byte[] str){
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		int realStrLength = str.length;
		int numBeforeLineLen = 0;
		int numAfterLineLen = 0;
		int lineLength = receiptLineLength - receiptLeftMargin; 
		
		if (str == null){
			result.write(0x20);
			return result.toByteArray();
		}
		
		for(int i=0; i<str.length; i++){
			if(str[i] == ASCIIConstants.ESC){
				if(i+1 < str.length && str[i+1] == (byte)'('){
					realStrLength -=3;
					if(i<=lineLength)
						numBeforeLineLen++;
					else
						numAfterLineLen++;
				}
			}
		}
			
		try {
			if (realStrLength <= lineLength) {
				int spaceLength = lineLength - realStrLength;
				result.write(addSpace(spaceLength));
				result.write(str);
				return result.toByteArray();
			} else if (realStrLength > lineLength) {
				byte[] tmpStr = new byte[lineLength+3*numBeforeLineLen];
				if(TextDirection.LeftToRight.equals(direction)){
					System.arraycopy(str, 0, tmpStr, 0, tmpStr.length);
				}else{
					System.arraycopy(str, str.length-tmpStr.length, tmpStr, 0, tmpStr.length);
				}
				
				int indexOfLastWord = findIndexOfLastSpace(direction, tmpStr);
				// str.substring(0, receiptLineLength).lastIndexOf(" ");

				if (indexOfLastWord < 1)
					indexOfLastWord = lineLength+3*numBeforeLineLen;

				byte[] tmp;

				tmp = new byte[tmpStr.length - indexOfLastWord];
				System.arraycopy(tmpStr, indexOfLastWord, tmp, 0, tmpStr.length - indexOfLastWord);
				result.write(alignTextRight(direction, receiptLineLength, receiptLeftMargin, tmp));

				result.write(ASCIIConstants.LF);
				tmp = new byte[str.length-(tmpStr.length - indexOfLastWord)];
				System.arraycopy(str, 0, tmp, 0, tmp.length);
				result.write(alignTextRight(direction, receiptLineLength, receiptLeftMargin, tmp));
				return result.toByteArray();
			}
		} catch (IOException e) {
			logger.error(e);
		}
		
		return str;
	}
	public byte[] alignTextRight(TextDirection direction, Integer receiptLineLength, Integer receiptLeftMargin, byte[] str){
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		int realStrLength = str.length;
		int numBeforeLineLen = 0;
		int numAfterLineLen = 0;
		int lineLength = receiptLineLength - receiptLeftMargin; 
		
		if (str == null){
			result.write(0x20);
			return result.toByteArray();
		}
		
		for(int i=0; i<str.length; i++){
			if(str[i] == ASCIIConstants.ESC){
				if(i+1 < str.length && str[i+1] == (byte)'('){
					realStrLength -=3;
					if(i<=lineLength)
						numBeforeLineLen++;
					else
						numAfterLineLen++;
				}
			}
		}
			
		try {
			if (realStrLength <= lineLength) {
				int num = deleteUnNecessarySpaces(str);
				if(num !=0){
					byte[] newstr = new byte[str.length-num];
					System.arraycopy(str, num, newstr, 0, str.length-num);
					int spaceLength = lineLength - realStrLength;
//					if(spaceLength > 0)
					result.write(addSpace(spaceLength));
					result.write(newstr);
					return result.toByteArray();
				}
				int spaceLength = lineLength - realStrLength;
				result.write(addSpace(spaceLength));
				result.write(str);
				return result.toByteArray();
			} else if (realStrLength > lineLength) {
				byte[] tmpStr = new byte[lineLength+3*numBeforeLineLen];
				byte[] tmpStr2 = new byte[lineLength+3*numBeforeLineLen];
				
				int j=0;
				for(int i = str.length-1; i>str.length-tmpStr.length-1; i--){
					if(j < tmpStr.length)
						tmpStr2[j]=str[i];
					j++;						
				}
				int kk2=tmpStr2.length-1;
				for(int kk=0; kk < tmpStr2.length; kk++){
					tmpStr[kk] = tmpStr2[kk2];
					kk2--;
				}
				tmpStr2=deletZeros(tmpStr);	
					
				int indexOfLastWord = oldfindIndexOfLastSpace(direction, tmpStr2);
				byte[] tmp ;
				byte[] tmp2; 
				if (indexOfLastWord < 1)
					indexOfLastWord = lineLength+3*numBeforeLineLen;
				
				if(str[str.length-tmpStr2.length-1] == 0x20 && indexOfLastWord>=1){
					result.write(alignTextRight(direction, receiptLineLength, receiptLeftMargin, tmpStr2));
					indexOfLastWord=0;
				} else if(str[str.length-tmpStr2.length-1] != 0x20){
					if (tmpStr2.length- indexOfLastWord == 0)
						indexOfLastWord = 0;
					tmp = new byte[tmpStr2.length - indexOfLastWord-1];
					tmp2 = new byte[tmpStr2.length - indexOfLastWord-1];

					j=0;
					for(int i = tmpStr2.length-1; i>indexOfLastWord; i--){
						if(j < tmpStr2.length- indexOfLastWord)
							tmp2[j]=tmpStr2[i];
						j++;						
					}
					kk2 = tmp2.length-1;
					for(int kk=0; kk < tmp2.length; kk++){
						tmp[kk] = tmp2[kk2];
						kk2--;
					}
					tmp2=deletZeros(tmp);
					result.write(alignTextRight(direction, receiptLineLength, receiptLeftMargin, tmp2));
				}
				result.write(ASCIIConstants.LF);
//				tmp = new byte[str.length-(tmpStr.length - indexOfLastWord)];
//				tmp2= new byte[str.length-(tmpStr.length-indexOfLastWord)];
				
//				System.arraycopy(str, 0, tmp, 0, tmp.length);
//				tmp = new byte[realStrLength - indexOfLastWord-1];	
//				tmp2 = new byte[realStrLength - indexOfLastWord-1];
				
				tmp = new byte[str.length-tmpStr2.length+indexOfLastWord];
				tmp2= new byte[str.length-tmpStr2.length+indexOfLastWord];
				j=0;
				for(int i = str.length-tmpStr2.length-1+indexOfLastWord; i>=0; i--){//double check this one
					if(j < tmp.length)
						tmp2[j]=str[i];
					j++;
						
				}
				kk2 = tmp2.length-1;
				for(int kk=0; kk < tmp2.length; kk++){
					tmp[kk] = tmp2[kk2];
					kk2--;
				}
				tmp2=deletZeros(tmp);
				result.write(alignTextRight(direction, receiptLineLength, receiptLeftMargin, tmp2));
				return result.toByteArray();
			}
		} catch (IOException e) {
			logger.error(e);
		}
		
		return str;
	}
	
	protected byte[] deletZeros(byte[] st){
		int counter=0;
		for(int i=0;i<st.length;i++){
			if(st[i]== 0)	
				counter++;
			else break;
		}
		byte[]newSt = new byte[st.length-counter];
		for(int i=0;i<st.length-counter && i+counter <= st.length-1;i++)
			newSt[i]=st[i+counter];
		return newSt;
		
		
	}
	protected int findIndexOfLastSpace(TextDirection direction, byte[] tmpStr){
		if(TextDirection.RightToLeft.equals(direction)){
			for (int i=tmpStr.length; i> 0; i--)
				if (tmpStr[i-1] == 0x20)
					return i-1;
		}else{
			for (int i=0; i< tmpStr.length; i++)
				if (tmpStr[i] == 0x20)
					return i;
		}
		return -1;
	}
	
	protected int findIndexOfFirstSpace(TextDirection direction, byte[] tmpStr){
		if(TextDirection.RightToLeft.equals(direction)){
			for(int i=0;i<tmpStr.length-1;i++)
				if(tmpStr[i] == 0x20)
					return i;
		}else{
			for(int i=tmpStr.length;i>0;i--)
				if(tmpStr[i-1] == 0x20)
					return i-1;
		}
		return -1;
	}
	
	
	protected int oldfindIndexOfLastSpace(TextDirection direction, byte[] tmpStr) {
		if (TextDirection.LeftToRight.equals(direction)){
			for (int i=tmpStr.length; i> 0; i--)
				if (tmpStr[i-1] == 0x20)
					return i-1;
		}else{
			for (int i=0; i< tmpStr.length; i++)
				if (tmpStr[i] == 0x20)
					return i;
		}
		return -1;
	}

	//for English receipt
	public byte[] center(TextDirection direction, Integer receiptLineLength, Integer receiptLeftMargin, String str){
		if (str == null){
			return new byte[]{0x20};
		}	
		
		return oldcenter(direction, receiptLineLength, receiptLeftMargin, str.getBytes());
	}
	
	
	public int deleteUnNecessarySpaces(byte[] str){
		int numberOfUnNecessarySpaces = 0;
		int j = 0;
		if(str != null && str.length > 0 && str[j] == 32){
			while(str[j]==32){
				numberOfUnNecessarySpaces++;
				j++;
			}
			
		}
		return numberOfUnNecessarySpaces;
	}
	//for Farsi receipt
	public byte[] center(TextDirection direction, Integer receiptLineLength, Integer receiptLeftMargin, byte[] str){
		String a = str.toString();
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		int realStrLength = str.length;
		int numBeforeLineLen = 0;
		int numAfterLineLen = 0;
		int lineLength = receiptLineLength- receiptLeftMargin;
		
		if (str == null){
			result.write(0x20);
			return result.toByteArray();
		}	
		
		for(int i=0; i<str.length; i++){
			if(str[i] == ASCIIConstants.LF){
				if(i+1 < str.length && str[i+1] == (byte)'('){
					realStrLength -=3;
					if(i<=lineLength)
						numBeforeLineLen++;
					else
						numAfterLineLen++;
				}
			}
		}

		try {
			
			if (realStrLength <= lineLength) {
				int num =deleteUnNecessarySpaces(str);
				
				if(num != 0){
					byte[] newstr = new byte[str.length-num];
					System.arraycopy(str, num, newstr, 0, str.length-num);
					int spaceLength = (lineLength - newstr.length)/2;
					if(spaceLength > 0)
						result.write(addSpace(spaceLength));
					result.write(newstr);
					return result.toByteArray();
				}
				int spaceLength = (lineLength - realStrLength)/2;
				if(spaceLength > 0)
					result.write(addSpace(spaceLength));
				result.write(str);
				return result.toByteArray();
			} else if (realStrLength > lineLength) {
				byte[] tmpStr = new byte[lineLength+3*numBeforeLineLen];
				byte[] tmpStr2 = new byte[lineLength+3*numBeforeLineLen];
				
				
				int j=0;
				for(int i = str.length-1; i>0; i--){
					if(j < lineLength+3*numBeforeLineLen)
						tmpStr2[j]=str[i];
					j++;						
				}
				
				int kk2=tmpStr2.length-1;
				for(int kk=0; kk < tmpStr2.length; kk++){
					tmpStr[kk] = tmpStr2[kk2];
					kk2--;
				}
				
				// Changed By Torki : 91/09/13
				//int indexOfLastWord = findIndexOfLastSpace(direction, tmpStr);
				int indexOfLastWord = lineLength - findIndexOfFirstSpace(direction, tmpStr);
				if (indexOfLastWord < 1)//ino felan dast nazadam
					indexOfLastWord = lineLength+3*numBeforeLineLen;

				byte[] tmp = new byte[indexOfLastWord];
				byte[] tmp2 = new byte[indexOfLastWord];
				
				j=0;
				for(int i = str.length-1; i>0; i--){
					if(j < indexOfLastWord)
						tmp2[j]=str[i];
					j++;						
				}
				
				kk2 = tmp2.length-1;
				for(int kk=0; kk < tmp2.length; kk++){
					tmp[kk] = tmp2[kk2];
					kk2--;
				}
				
				result.write(center(direction, receiptLineLength, receiptLeftMargin, tmp));
				result.write(ASCIIConstants.LF);
			
				tmp = new byte[realStrLength - indexOfLastWord];	
				tmp2 = new byte[realStrLength - indexOfLastWord];
				j=0;
				for(int i = str.length-indexOfLastWord-1; i>=0; i--){
					if(j < realStrLength-indexOfLastWord)
						tmp2[j]=str[i];
					j++;
						
				}
				
				kk2 = tmp2.length-1;
				for(int kk=0; kk < tmp2.length; kk++){
					tmp[kk] = tmp2[kk2];
					kk2--;
				}
				
				result.write(center(direction, receiptLineLength, receiptLeftMargin, tmp));
				return result.toByteArray();
			}
		} catch (IOException e) {
			logger.error(e);
		}
		return str;
		
	}
	
	
	public byte[] oldcenter(TextDirection direction, Integer receiptLineLength, Integer receiptLeftMargin, byte[] str){
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		int realStrLength = str.length;
		int numBeforeLineLen = 0;
		int numAfterLineLen = 0;
		int lineLength = receiptLineLength- receiptLeftMargin;
		
		if (str == null){
			result.write(0x20);
			return result.toByteArray();
		}	
		
		for(int i=0; i<str.length; i++){
			if(str[i] == ASCIIConstants.ESC){
				if(i+1 < str.length && str[i+1] == (byte)'('){
					realStrLength -=3;
					if(i<=lineLength)
						numBeforeLineLen++;
					else
						numAfterLineLen++;
				}
			}
		}

		try {
			if (realStrLength <= lineLength) {
				int spaceLength = (lineLength - realStrLength)/2;
				if(spaceLength > 0)
					result.write(addSpace(spaceLength));
				result.write(str);
				return result.toByteArray();
			} else if (realStrLength > lineLength) {
				byte[] tmpStr = new byte[lineLength+3*numBeforeLineLen];
				System.arraycopy(str, 0, tmpStr, 0, lineLength+3*numBeforeLineLen);
				int indexOfLastWord = findIndexOfLastSpace(direction, tmpStr);
				if (indexOfLastWord < 1)
					indexOfLastWord = lineLength+3*numBeforeLineLen;

				byte[] tmp = new byte[indexOfLastWord];
				System.arraycopy(str, 0, tmp, 0, indexOfLastWord);

				result.write(oldcenter(direction, receiptLineLength, receiptLeftMargin, tmp));
				result.write(ASCIIConstants.LF);
				tmp = new byte[realStrLength - indexOfLastWord];
				System.arraycopy(str, indexOfLastWord + 1, tmp, 0, realStrLength - indexOfLastWord -1);
				result.write(oldcenter(direction, receiptLineLength, receiptLeftMargin, tmp));
				return result.toByteArray();
			}
		} catch (IOException e) {
			logger.error(e);
		}
		return str;
	}
	
	public byte[] justify(TextDirection direction, Integer receiptLineLength, Integer receiptLeftMargin, String str1, Long str2){
		return justify(direction, receiptLineLength, receiptLeftMargin, str1.getBytes(), str2.toString().getBytes());
	}
	public byte[] justify(TextDirection direction, Integer receiptLineLength, Integer receiptLeftMargin, String str1, String str2){
		return justify(direction, receiptLineLength, receiptLeftMargin, str1.getBytes(), str2.getBytes());
	}
	public byte[] justify(TextDirection direction, Integer receiptLineLength, Integer receiptLeftMargin, Long str1, String str2){
		return justify(direction, receiptLineLength, receiptLeftMargin, str1.toString().getBytes(), str2.getBytes());
	}
	public byte[] justify(TextDirection direction, Integer receiptLineLength, Integer receiptLeftMargin, byte[] str1, Long str2){
		return justify(direction, receiptLineLength, receiptLeftMargin, str1, str2.toString().getBytes());
	}
	public byte[] justify(TextDirection direction, Integer receiptLineLength, Integer receiptLeftMargin, byte[] str1, String str2){
		return justify(direction, receiptLineLength, receiptLeftMargin, str1, str2.getBytes());
	}
	public byte[] justify(TextDirection direction, Integer receiptLineLength, Integer receiptLeftMargin, String str1, byte[] str2){
		return justify(direction, receiptLineLength, receiptLeftMargin, str1.getBytes(), str2);
	}	
	public byte[] justify(TextDirection direction, Integer receiptLineLength, Integer receiptLeftMargin, Long str1, byte[] str2){
		return justify(direction, receiptLineLength, receiptLeftMargin, str1.toString().getBytes(), str2);
	}
	public byte[] justify(TextDirection direction, Integer receiptLineLength, Integer receiptLeftMargin, byte[] str1, byte[] str2){
		if (str1 == null){
			return justify(direction, receiptLineLength, receiptLeftMargin, new byte[]{}, str2);
		}else if (str2 == null){
			return alignTextRight(direction, receiptLineLength, receiptLeftMargin, str1);
		}
		
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		int realStr1Length = str1.length;
		int realStr2Length = str2.length;
		
		for(int i=0; i<str1.length; i++){
			if(str1[i] == ASCIIConstants.ESC){
				if(i+1 < str1.length && str1[i+1] == (byte)'('){
					realStr1Length -=3;
				}
			}
		}
		
		for(int i=0; i<str2.length; i++){
			if(str2[i] == ASCIIConstants.ESC){
				if(i+1 < str2.length && str2[i+1] == (byte)'('){
					realStr2Length -=3;
				}
			}
		}

		try {
			int lineLength = receiptLineLength- receiptLeftMargin;
			if (realStr1Length+realStr2Length < lineLength ){
				int spaceLength = lineLength - realStr1Length - realStr2Length;
				result.write(str2);
				result.write(addSpace(spaceLength));
				result.write(str1);
			}else {
				if(direction.equals(TextDirection.LeftToRight)){
					result.write(str2);
					result.write(ASCIIConstants.LF);
					result.write(alignTextRight(direction, receiptLineLength, receiptLeftMargin, str1));
//					result.write(str1);
				}
				else if(direction.equals(TextDirection.RightToLeft)){
					result.write(alignTextRight(direction, receiptLineLength,receiptLeftMargin, str1));
					result.write(ASCIIConstants.LF);
					result.write(str2);
				}
			}
		} catch (IOException e) {
			logger.error(e);
		}
		return result.toByteArray();
	}
	
	
	protected byte[] addSpace(int length){
		if(length == 0)
			return new ByteArrayOutputStream().toByteArray();
		
		byte[] result = new byte[(length/16 +1)*2];
		for (int i =0; i< 2*(length/15); i+=2){
			result[i]= ASCIIConstants.SO;
			result[i+1]= "?".getBytes()[0];			
		}
		
		int tmp = length % 15;
		switch (tmp) {
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
			result[result.length-2]= ASCIIConstants.SO;
			result[result.length-1]= (byte)(0x30+tmp);
			break;
		case 10:
			result[result.length-2]= ASCIIConstants.SO;
			result[result.length-1]= ":".getBytes()[0];
			break;
		case 11:
			result[result.length-2]= ASCIIConstants.SO;
			result[result.length-1]= ";".getBytes()[0];
			break;
		case 12:
			result[result.length-2]= ASCIIConstants.SO;
			result[result.length-1]= "<".getBytes()[0];
			break;
		case 13:
			result[result.length-2]= ASCIIConstants.SO;
			result[result.length-1]= "=".getBytes()[0];
			break;
		case 14:
			result[result.length-2]= ASCIIConstants.SO;
			result[result.length-1]= ">".getBytes()[0];
			break;
		}
		
		return result;
	}

	public byte[] horizontalLine(Integer receiptLineLength, Integer receiptLeftMargin ,ATMTerminal atm, Integer c){
		String elem = receiptLineLength + "$" + receiptLeftMargin + "$" + c;
		
		
		if(ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId()).getReceiptDetailsMap().containsKey(elem))
			return ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId()).getReceiptDetailsMap().get(elem);
		
		byte[] out = new byte[receiptLineLength-receiptLeftMargin+1];
		for(int i=0; i<receiptLineLength-receiptLeftMargin-1; i++)
			out[i] = c.byteValue();
		
		out[out.length-2] = ASCIIConstants.CR;
		out[out.length-1] = ASCIIConstants.LF;
		ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId()).setReceiptDetailsMap(elem, out);
		return out;
	}

	public byte[] horizontalLine(Integer length, Integer c){
		byte[] out = new byte[length];
		for(int i=0; i<length; i++)
			out[i] = c.byteValue();
		
		return out;
	}

	public byte[] putLF(Integer num){
		byte[] b = new byte[num.intValue()];
		for(int i=0; i<num; i++)
			b[i] = ASCIIConstants.LF;
		
		
		return b;
	}
	
	public byte[] convertToEnglish(OrganizationType data){
		return data.toStringEnglish().getBytes();
	}

	public byte[] convertToEnglishForNCR(OrganizationType data){
		return data.toStringEnglish().getBytes();
	}

	public byte[] convertToEnglishForNCRScreen(OrganizationType data){
		return data.toStringEnglish().getBytes();
	}
	
	public byte[] convertToEnglish(String in){
		// TASK Task125[26306] : ThirdPatyName Bug
		if (!Util.hasText(in))
			in = "";
		
		return convertToEnglish(in.getBytes());
	}
	
	public byte[] convertToEnglishForNCR(String in){
		// TASK Task125[26306] : ThirdPatyName Bug
		if (!Util.hasText(in))
			in = "";
		
		return convertToEnglishForNCR(in.getBytes());
	}
	public byte[] convertToEnglishForNCRScreen(String in){
		// TASK Task125[26306] : ThirdPatyName Bug
		if (!Util.hasText(in))
			in = "";
		
		return convertToEnglishForNCRScreen(in.getBytes());
	}
	
	public byte[] convertToEnglish(Long in){
		return convertToEnglish(in.toString().getBytes());
	}
	
	public byte[] convertToEnglishForNCR(Long in){
		return convertToEnglishForNCR(in.toString().getBytes());
	}
	public byte[] convertToEnglishForNCRScreen(Long in){
		return convertToEnglishForNCRScreen(in.toString().getBytes());
	}
	
	public byte[] convertToEnglish(byte[] in){
		// TASK Task125[26306] : ThirdPatyName Bug
		if (in == null) {
			return null;
		}
		
		byte[] result = new byte[in.length + 6];

		int index = 0;
		result[index++] = ASCIIConstants.ESC; result[index++] = '('; result[index++] = '1';
		for(; index-3<in.length; index++)
			result[index] = in[index-3];
		
		result[index++] = ASCIIConstants.ESC; result[index++] = '('; result[index++] = '7';
		
		return result;
}
	
	public byte[] convertToEnglishForNCR(byte[] in){
		// TASK Task125[26306] : ThirdPatyName Bug
		if (in == null) {
			return null;
		}
		
		int index = 0;
		for(; index<in.length; index++) {
			if (in[index] >= 0x41 && in[index] <= 0x5a)
				in[index] = (byte) (in[index] + 0x20);
			else if (in[index] == 0x23)
				in[index] = NCR_RECEIPT_SHARP_SIGN_CHAR;
			else if (in[index] == 0x2A)
				in[index] = NCR_RECEIPT_STAR_CHAR;
			else if (in[index] == 0x2F)
				in[index] = NCR_RECEIPT_SLASH_CHAR;
			else if (in[index] == 0x2E)
				in[index] = NCR_RECEIPT_DOT_CHAR;
		}

		return in;
	}
	public byte[] convertToEnglishForNCRScreen(byte[] in){
		// TASK Task125[26306] : ThirdPatyName Bug
		if (in == null) {
			return null;
		}
		
		byte[] result = new byte[in.length + 6];

		int index = 0;
		result[index++] = ASCIIConstants.ESC; result[index++] = '('; result[index++] = '1';
		for(; index-3<in.length; index++)
			result[index] = in[index-3];

		result[index++] = ASCIIConstants.ESC; result[index++] = '('; result[index++] = '6';

		return result;
	}
	
	public byte[] test() {
		byte[] b = new byte[256];
		int index = 0;
		for (int i=0x80; i<=0xfe && index<256; i++, index+=2) {
			b[index] = (byte) i;
			b[index+1] = (byte) 0x20;
		}
		
		return b;
	}

	@Override
	public boolean isInNumberRange(byte b) {
		return (b >= (byte)0x30 && b <= (byte)0x39) || (b >= (byte)0x80 && b <= (byte)0x89);
	}
	
	public byte[] bankStatementTableFa(Ifx ifx, Integer receiptLineLength, Integer receiptLeftMargin, Map<String, String> encodings){
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		if (ifx.getBankStatementData() == null || ifx.getBankStatementData().size() == 0)
			return result.toByteArray();

		try {
			ATMTerminal atm;
			if (!(ifx.getEndPointTerminal() instanceof ATMTerminal)){
				atm = TerminalService.findTerminal(ATMTerminal.class, Long.parseLong(ifx.getTerminalId()));
			}else{
				atm = (ATMTerminal) ifx.getEndPointTerminal();
			}

			char farsi_rec = ' ';
			char english_rec = ' ';

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				farsi_rec = encodings.get(FARSI_RECIEPT_ENCODING).charAt(0);
				english_rec = encodings.get(ENGLISH_ENCODING).charAt(0);
			}


			//header line-top border
			result.write(charSetSwitchToEnglish(english_rec, atm));
			result.write(0xda);
			result.write(0xc4);
			result.write(horizontalLine(11, 0xc4));
			result.write(0xc2);
			result.write(horizontalLine(8, 0xc4));
			result.write(0xc2);
			result.write(horizontalLine(10, 0xc4));
			result.write(0xc2);
			result.write(horizontalLine(2, 0xc4));
			result.write(0xbf);
			result.write(ASCIIConstants.LF);
			//end: header line-top border

			result.write(0xb3);
			result.write(0x20);
			result.write(charSetSwitchToPersian(farsi_rec, atm));
			result.write(convert2Farsi(format("مبلغ", 11, "LEFT").getBytes()));
			result.write(charSetSwitchToEnglish(english_rec, atm));
			result.write(0xb3);
			result.write(charSetSwitchToPersian(farsi_rec, atm));
			result.write(convert2Farsi(format("زمان", 8, "LEFT").getBytes()));
			result.write(charSetSwitchToEnglish(english_rec, atm));
			result.write(0xb3);
			result.write(charSetSwitchToPersian(farsi_rec, atm));
			result.write(convert2Farsi(format("تاریخ", 10, "LEFT").getBytes()));
			result.write(charSetSwitchToEnglish(english_rec, atm));
			result.write(0xb3);
			result.write(charSetSwitchToPersian(farsi_rec, atm));
			result.write(0x20);
			result.write(0x20);
			result.write(charSetSwitchToEnglish(english_rec, atm));
			result.write(0xb3);
			result.write(ASCIIConstants.LF);

			//header line-bottom border
			result.write(0xc3);
			result.write(0xc4);
			result.write(horizontalLine(11, 0xc4));
			result.write(0xc5);
			result.write(horizontalLine(8, 0xc4));
			result.write(0xc5);
			result.write(horizontalLine(10, 0xc4));
			result.write(0xc5);
			result.write(horizontalLine(2, 0xc4));
			result.write(0xb4);
			result.write(ASCIIConstants.LF);
			//end: header line-bottom border

			Integer row = 1;
			for (BankStatementData data : ifx.getBankStatementData()){
				result.write(0xb3);
				if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
					result.write(data.getTrnType().equalsIgnoreCase("D")? '-' : '+');
				} else {
					result.write(data.getTrnType().equalsIgnoreCase("D")? 0xb2 : 0xb1);
				}
				result.write(charSetSwitchToPersian(farsi_rec, atm));
				result.write(convert2Farsi(format(Math.abs(data.getAmount()), 11, "LEFT").getBytes()));
				result.write(charSetSwitchToEnglish(english_rec, atm));
				result.write(0xb3);
				result.write(charSetSwitchToPersian(farsi_rec, atm));
				String strTime = data.getTrxDt().getDayTime().toString();
				result.write(convert2Farsi(strTime.substring(0, 2).getBytes()));
				result.write(charSetSwitchToEnglish(english_rec, atm));
				result.write((byte) ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_COLON_CHAR : NCR_RECEIPT_COLON_CHAR));
				result.write(charSetSwitchToPersian(farsi_rec, atm));
				result.write(convert2Farsi(strTime.substring(3, 5).getBytes()));
				result.write(charSetSwitchToEnglish(english_rec, atm));
				result.write((byte) ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_COLON_CHAR : NCR_RECEIPT_COLON_CHAR));
				result.write(charSetSwitchToPersian(farsi_rec, atm));
				result.write(convert2Farsi(strTime.substring(6, 8).getBytes()));
				result.write(charSetSwitchToEnglish(english_rec, atm));
				result.write(0xb3);
				result.write(charSetSwitchToPersian(farsi_rec, atm));
//				String strDate = data.getTrxDt().getDayDate().toString();
				DayDate persianDayDate = PersianCalendar.getPersianDayDate(data.getTrxDt().toDate());
				String strDate = persianDayDate.toString();
				result.write(convert2Farsi(strDate.substring(0, 4).getBytes()));
				result.write(charSetSwitchToEnglish(english_rec, atm));
				result.write((byte) ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_SLASH_CHAR : NCR_RECEIPT_SLASH_CHAR));
				result.write(charSetSwitchToPersian(farsi_rec, atm));
				result.write(convert2Farsi(strDate.substring(5, 7).getBytes()));
				result.write(charSetSwitchToEnglish(english_rec, atm));
				result.write((byte) ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_SLASH_CHAR : NCR_RECEIPT_SLASH_CHAR));
				result.write(charSetSwitchToPersian(farsi_rec, atm));
				result.write(convert2Farsi(strDate.substring(8, 10).getBytes()));
				result.write(charSetSwitchToEnglish(english_rec, atm));
				result.write(0xb3);
				result.write(charSetSwitchToPersian(farsi_rec, atm));
				result.write(convert2Farsi(format(row.toString(), 2, "LEFT").getBytes()));
				result.write(charSetSwitchToEnglish(english_rec, atm));
				result.write(0xb3);
				result.write(ASCIIConstants.LF);
				row++;
			}

			for(int i=row; i<11; i++){
				result.write(charSetSwitchToEnglish(english_rec, atm));
				result.write(0xb3);
				result.write(0x20);
//				result.write(0xc2);
				result.write(horizontalLine(11, 0x20));
				result.write(0xb3);
				result.write(horizontalLine(8, 0x20));
				result.write(0xb3);
				result.write(horizontalLine(10, 0x20));
				result.write(0xb3);
				result.write(charSetSwitchToPersian(farsi_rec, atm));
				result.write(convert2Farsi(format(String.valueOf(i), 2, "LEFT").getBytes()));
				result.write(charSetSwitchToEnglish(english_rec, atm));
				result.write(0xb3);
				result.write(ASCIIConstants.LF);
			}

			//Last line
			result.write(0xc0);
			result.write(0xc4);
//			result.write(0xc5);
			result.write(horizontalLine(11, 0xc4));
			result.write(0xc1);
			result.write(horizontalLine(8, 0xc4));
			result.write(0xc1);
			result.write(horizontalLine(10, 0xc4));
			result.write(0xc1);
			result.write(horizontalLine(2, 0xc4));
			result.write(0xd9);
			result.write(ASCIIConstants.LF);

			result.write(charSetSwitchToPersian(farsi_rec, atm));
		} catch (IOException e) {
			logger.error(e);
		}

		return result.toByteArray();
	}
	
	public byte[] bankStatementScreenTableFa(Ifx ifx,int columnStatement){

		//start
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		ATMTerminal atm;
		if (!(ifx.getEndPointTerminal() instanceof ATMTerminal)){
			atm = TerminalService.findTerminal(ATMTerminal.class, Long.parseLong(ifx.getTerminalId()));
		}else{
			atm = (ATMTerminal) ifx.getEndPointTerminal();
		}
		try{
//			result.write("---------------------------------------------------------------------------------------------------------------\r\n".getBytes());
			BankStatementData data = null;
			
			if(columnStatement == 4){//value col
				data = ifx.getBankStatementData().get(ifx.getStatementRowNumber());
				
				if(atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR))
					result.write(data.getTrnType().equalsIgnoreCase("D")? convert2Farsi("-") : convert2Farsi(" "));
				else
					result.write(data.getTrnType().equalsIgnoreCase("D")? convertToEnglish("-") : convertToEnglish(" "));
					
				result.write(data.getAmount().toString().getBytes());
				ifx.setStatementRowNumber(ifx.getStatementRowNumber() + 1);
			}else if(columnStatement == 3){//time col
				data = ifx.getBankStatementData().get(ifx.getStatementRowNumber());
				String strTime = data.getTrxDt().getDayTime().toString();
				result.write(convert2Farsi(strTime.substring(0, 2)));
				result.write(convert2Farsi(strTime.substring(3, 5)));
				
			}else if(columnStatement == 2){//date col
				data = ifx.getBankStatementData().get(ifx.getStatementRowNumber());
				DayDate persianDayDate = PersianCalendar.getPersianDayDate(data.getTrxDt().toDate());
				String strDate = persianDayDate.toString();
				result.write(convert2Farsi(strDate.substring(0, 4)));
				result.write(convert2Farsi(strDate.substring(5, 7)));
				result.write(convert2Farsi(strDate.substring(8, 10)));
				
			}else if(columnStatement == 1){//row number col
				data = ifx.getBankStatementData().get(ifx.getStatementRowNumber());
				result.write(convert2Farsi(String.valueOf(ifx.getStatementRowNumber()+1)));
			}else if(columnStatement == 5 && ifx.getBankStatementData().size() == ifx.getStatementRowNumber()){//balance row
				
				if(atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)){
					result.write(convert2Farsi("ریال"));
		    		result.write(convert2Farsi(" "));
		    		String balanceText=ifx.getAcctBalLedgerAmt();
		    		balanceText=balanceText.replaceAll("\\D+", "");
		    		BigDecimal balance=new BigDecimal(balanceText);
		    		result.write(convert2Farsi(balance.toString()));
		    		result.write(convert2Farsi(" "));
		    		result.write(convert2Farsi("موجودی"));
				}else{
		    		String balanceText=ifx.getAcctBalLedgerAmt();
		    		balanceText=balanceText.replaceAll("\\D+", "");
		    		BigDecimal balance=new BigDecimal(balanceText);
		    		result.write(convert2Farsi(balance.toString()));
				}

	    		ifx.setStatementRowNumber(0);
			}
			

			return result.toByteArray();
		}catch(Exception e){
			return new byte[]{};
		}
		
	}

	public byte[] bankStatementScreenTableEn(Ifx ifx,int columnStatement){

		//start
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		ATMTerminal atm;
		if (!(ifx.getEndPointTerminal() instanceof ATMTerminal)){
			atm = TerminalService.findTerminal(ATMTerminal.class, Long.parseLong(ifx.getTerminalId()));
		}else{
			atm = (ATMTerminal) ifx.getEndPointTerminal();
		}
		try{
//			result.write("---------------------------------------------------------------------------------------------------------------\r\n".getBytes());
			BankStatementData data = null;
			
			if(columnStatement == 4){//value col
				data = ifx.getBankStatementData().get(ifx.getStatementRowNumber());
				result.write(data.getTrnType().equalsIgnoreCase("D")? "-".getBytes() : convertToEnglish(" "));
				result.write(convertToEnglish(data.getAmount().toString()));
				ifx.setStatementRowNumber(ifx.getStatementRowNumber() + 1);
			}else if(columnStatement == 3){//time col
				data = ifx.getBankStatementData().get(ifx.getStatementRowNumber());
				String strTime = data.getTrxDt().getDayTime().toString();
				result.write(convertToEnglish(strTime.substring(0, 2)));
				result.write(convertToEnglish(strTime.substring(3, 5)));
				
			}else if(columnStatement == 2){//date col
				data = ifx.getBankStatementData().get(ifx.getStatementRowNumber());
				DayDate gregorianDayDate = data.getTrxDt().getDayDate();
				String strDate = gregorianDayDate.toString();
				result.write(convertToEnglish(strDate.substring(0, 4)));
				result.write(convertToEnglish(strDate.substring(5, 7)));
				result.write(convertToEnglish(strDate.substring(8, 10)));
				
			}else if(columnStatement == 1){//row number col
				data = ifx.getBankStatementData().get(ifx.getStatementRowNumber());
				result.write(convertToEnglish(String.valueOf(ifx.getStatementRowNumber()+1)));
			}else if(columnStatement == 5 && ifx.getBankStatementData().size() == ifx.getStatementRowNumber()){//balance row
				
	    		result.write(convertToEnglish("BALANCE"));
	    		result.write(convertToEnglish(" "));
	    		String balanceText=ifx.getAcctBalLedgerAmt();
	    		balanceText=balanceText.replaceAll("\\D+", "");
	    		BigDecimal balance=new BigDecimal(balanceText);
	    		result.write(convertToEnglish(balance.toString()));
	    		result.write(convertToEnglish(" "));
	    		result.write(convertToEnglish("IRR"));
	    		ifx.setStatementRowNumber(0);
			}
			
			return result.toByteArray();
		}catch(Exception e){
			return new byte[]{};
		}
		
	}
	
	public byte[] bankStatementTableEn(Ifx ifx, Integer receiptLineLength, Integer receiptLeftMargin){
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		if (ifx.getBankStatementData() == null || ifx.getBankStatementData().size() == 0)
			return result.toByteArray();

		try {
			ATMTerminal atm;
			if (!(ifx.getEndPointTerminal() instanceof ATMTerminal)){
				atm = TerminalService.findTerminal(ATMTerminal.class, Long.parseLong(ifx.getTerminalId()));
			}else{
				atm = (ATMTerminal) ifx.getEndPointTerminal();
			}

			//header line-top border
			result.write(0xda);
			result.write(0xc4);
			result.write(horizontalLine(1, 0xc4));
			result.write(0xc2);
			result.write(horizontalLine(10, 0xc4));
			result.write(0xc2);
			result.write(horizontalLine(8, 0xc4));
			result.write(0xc2);
			result.write(horizontalLine(12, 0xc4));
			result.write(0xbf);
			result.write(ASCIIConstants.LF);
			//end: header line-top border

			result.write(0xb3);
			result.write(0x20);
			result.write(0x20);
			result.write(0xb3);
			result.write((format((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? "Date" : "date", 10, "LEFT").getBytes()));
			result.write(0xb3);
			result.write((format((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? "Time" : "time", 8, "LEFT").getBytes()));
			result.write(0xb3);
			result.write((format((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? "Amount" : "amount", 12, "LEFT").getBytes()));
			result.write(0xb3);
			result.write(ASCIIConstants.LF);

			//header line-bottom border
			result.write(0xc3);
			result.write(0xc4);
			result.write(horizontalLine(1, 0xc4));
			result.write(0xc5);
			result.write(horizontalLine(10, 0xc4));
			result.write(0xc5);
			result.write(horizontalLine(8, 0xc4));
			result.write(0xc5);
			result.write(horizontalLine(12, 0xc4));
			result.write(0xb4);
			result.write(ASCIIConstants.LF);
			//end: header line-bottom border

			Integer row = 1;
			for (BankStatementData data : ifx.getBankStatementData()){
				result.write(0xb3);
				result.write((format(row.toString(), 2, "LEFT").getBytes()));
				result.write(0xb3);
				String strDate = data.getTrxDt().getDayDate().toString();
				result.write((strDate.substring(0, 4).getBytes()));
				result.write((byte) ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_SLASH_CHAR : NCR_RECEIPT_SLASH_CHAR));
				result.write((strDate.substring(5, 7).getBytes()));
				result.write((byte) ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_SLASH_CHAR : NCR_RECEIPT_SLASH_CHAR));
				result.write((strDate.substring(8, 10).getBytes()));
				result.write(0xb3);
				String strTime = data.getTrxDt().getDayTime().toString();
				result.write((strTime.substring(0, 2).getBytes()));
				result.write((byte) ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_COLON_CHAR : NCR_RECEIPT_COLON_CHAR));
				result.write((strTime.substring(3, 5).getBytes()));
				result.write((byte) ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_COLON_CHAR : NCR_RECEIPT_COLON_CHAR));
				result.write((strTime.substring(6, 8).getBytes()));
				result.write(0xb3);
				result.write((format(Math.abs(data.getAmount()), 11, "LEFT").getBytes()));
				if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
					result.write(data.getTrnType().equalsIgnoreCase("D")? '-' : '+');
				} else {
					result.write(data.getTrnType().equalsIgnoreCase("D")? 0xb2 : 0xb1);
				}
				result.write(0xb3);
				result.write(ASCIIConstants.LF);
				row++;
			}

			for(int i=row; i<11; i++){
				result.write(0xb3);
				result.write((format(String.valueOf(i), 2, "LEFT").getBytes()));
				result.write(0xb3);
				result.write(horizontalLine(10, 0x20));
				result.write(0xb3);
				result.write(horizontalLine(8, 0x20));
				result.write(0xb3);
				result.write(horizontalLine(12, 0x20));
				result.write(0xb3);
				result.write(ASCIIConstants.LF);
			}

			//Last line
			result.write(0xc0);
			result.write(0xc4);
//			result.write(0xc5);
			result.write(horizontalLine(1, 0xc4));
			result.write(0xc1);
			result.write(horizontalLine(10, 0xc4));
			result.write(0xc1);
			result.write(horizontalLine(8, 0xc4));
			result.write(0xc1);
			result.write(horizontalLine(12, 0xc4));
			result.write(0xd9);
			result.write(ASCIIConstants.LF);

		} catch (IOException e) {
			logger.error(e);
		}

		return result.toByteArray();
	}
	
	public byte[] bnkFarsiName(String bnkFarsiName){
		return bnkFarsiName.getBytes();
	}

	public byte[] bnkFarsiMount(String bnkFarsiMount){
		return bnkFarsiMount.getBytes();
	}
	
	public String bnkEnglishName(String bnkEnglishName){
		return bnkEnglishName;
	}
	
	public String bnkEnglishNameForNCR(String bnkEnglishName){
		return bnkEnglishName.toLowerCase();
	}
	
	public String bnkEnglishMount(String bnkEnglishMount){
		return bnkEnglishMount;
	}
	
	public String bnkEnglishMountForNCR(String bnkEnglishMount){
		return bnkEnglishMount.toLowerCase();
	}
	
	public String safeEn(String str){
		if(str == null || str == "")
			return "";
		
		return str.toUpperCase();
	}
	
	
	protected byte[] charSetSwitchToEnglish(char charSet, ATMTerminal atm){
		if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
			return new byte[] {ASCIIConstants.ESC, '(', (byte) charSet};
		} else {
			return new byte[] {};
		}
	}

	protected byte[] charSetSwitchToPersian(char charSet, ATMTerminal atm){
		if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
			return new byte[] {ASCIIConstants.ESC, '(', (byte) charSet};
		} else {
			return new byte[] {};
		}
	}

	@Override
	protected int indexOfSpace() {
		return getFarsiCharsConnectingInstances()[45][0];
	}

	@Override
	public Map<Integer, Integer> getEndingChars() {
		return new HashMap<Integer, Integer>();
	}

	@Override
	public boolean isSpecialCharacter(int code) {
		return false;
	}

	@Override
	public String processSpecialCharacter(int code) {
		return "";
	}

	@Override
	public byte[] finalize(byte[] converted, String encoding, String extendedEncoding) {
		if (converted == null || converted.length == 0)
			return converted;

		if (!Util.hasText(extendedEncoding) || extendedEncoding.equals(encoding)) {
			return converted;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String characterSet = encoding;
		
		for (int i = 0; i < converted.length; i++) {
			if (converted[i] < 0) {
				if (characterSet.equals(encoding)) {
					out.write(ASCIIConstants.ESC);
					out.write('(');
					out.write(extendedEncoding.charAt(0));
					characterSet = extendedEncoding;
				}
				out.write((byte) (converted[i] - 0x7F + 0x20));
			} else {
				if (!characterSet.equals(encoding)) {
					out.write(ASCIIConstants.ESC);
					out.write('(');
					out.write(encoding.charAt(0));
					characterSet = encoding;
				}
				out.write(converted[i]);
			}
		}
		if (!characterSet.equals(encoding)) {
			out.write(ASCIIConstants.ESC);
			out.write('(');
			out.write(encoding.charAt(0));
			characterSet = encoding;
		}
		return out.toByteArray();
	}
	
	private byte[] simpleCardCaptureReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings/*, NDCConvertor convertor*/){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		byte[] temp;
		try{
			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			
			out.write(generateHeader(atm, ifx, encodings, config, lineLength, leftMargin, textDir));
			
//			if(config.getreceiptDetailsMap().containsKey("رسید ضبط کارت" + "$center$" + lineLength + "$" + leftMargin))
			if(config.getReceiptDetailsMap().containsKey("رسید ضبط کارت" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید ضبط کارت"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید ضبط کارت"));
				out.write(temp);
//				receiptDetailsMap.put("رسید ضبط کارت"+"$center$" + lineLength + "$" + leftMargin, temp);
				config.setReceiptDetailsMap("رسید ضبط کارت"+"$center$" + lineLength + "$" + leftMargin, temp);
			}			
			
			out.write(ASCIIConstants.LF);
//			String st = "[ESC](7[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//					"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//					"[GR center(GR c2F('رسید ضبط کارت'))][LF]" ;
			
			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));
			
			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("شماره کارت")){
				temp = convert2Farsi("شماره کارت");
				config.setReceiptDetailsMap("شماره کارت", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("کارت شما به دلایل امنیتی ضبط شد")){
				temp = convert2Farsi("کارت شما به دلایل امنیتی ضبط شد");
				config.setReceiptDetailsMap("کارت شما به دلایل امنیتی ضبط شد", temp);		
			}
			out.write(center(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("کارت شما به دلایل امنیتی ضبط شد")));
			
			out.write(putLF(15));
//			st +="[ESC](7[GR datePersianFormat(ifx.receivedDt)][LF][LF]" +
//					"[GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))][LF][LF]" +
//					"[GR center(GR c2F('کارت شما به دلایل امنیتی ضبط شد'))]" +
//					"[GR putLF(15)]" ;
			
			out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));
			
			out.write(ASCIIConstants.FF);
//			st +="[ESC](7[GR center(GR GR c2F(GR bnkMount2F()) )][FF]";
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	private byte[] simpleNotAcceptableRequestReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings/*, NDCConvertor convertor*/){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		byte[] temp;
		try{
			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			
			out.write(generateHeader(atm, ifx, encodings, config, lineLength, leftMargin, textDir));
			
			if(config.getReceiptDetailsMap().containsKey("رسید مشكل امنيتي" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید مشكل امنيتي"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید مشكل امنيتي"));
				out.write(temp);
				config.setReceiptDetailsMap("رسید مشكل امنيتي"+"$center$" + lineLength + "$" + leftMargin, temp);		
			}			
			
//			out.write(center(textDir, lineLength, leftMargin, convert2Farsi("رسید مشكل امنيتي")));
			out.write(ASCIIConstants.LF);
		
//			String st = "[ESC](7[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//					"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//					"[GR center(GR c2F('رسید مشكل امنيتي'))][LF]" ;
			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));
			
			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("شماره کارت")){
				temp = convert2Farsi("شماره کارت");
				config.setReceiptDetailsMap("شماره کارت", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("درخواست شما به دلايل امنيتي قابل اجرا نميباشد")){
				temp = convert2Farsi("درخواست شما به دلايل امنيتي قابل اجرا نميباشد");
				config.setReceiptDetailsMap("درخواست شما به دلايل امنيتي قابل اجرا نميباشد", temp);		
			}
			out.write(center(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("درخواست شما به دلايل امنيتي قابل اجرا نميباشد")));
			
			out.write(putLF(15));
			
			
//			st +="[ESC](7[GR datePersianFormat(ifx.receivedDt)][LF][LF]" +
//					"[GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))][LF][LF]" +
//					"[GR center(GR c2F('درخواست شما به دلايل امنيتي قابل اجرا نميباشد'))]" +
//					"[GR putLF(15)]" ;
			
			
			out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));

			out.write(ASCIIConstants.FF);
//			st +="[ESC](7[GR center(GR GR c2F(GR bnkMount2F()) )][FF]";
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	private byte[] simpleCreditStatementDataJournal(Ifx ifx){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			out.write(ASCIIConstants.LF);
			out.write("Credit Statement:".toUpperCase().getBytes()); //Task141 NCR
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("REF:".getBytes());
			out.write(ifx.getSrc_TrnSeqCntr().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("PAN:".getBytes());
			out.write(ifx.getActualAppPAN().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TIME:".getBytes());
			out.write(ifx.getReceivedDt().toString().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TERMINAL:".getBytes());
			out.write(ifx.getTerminalId().getBytes());
	
//			String st = "[LF]Credit Statement:[SO]1" +
//					"REF:[GR ifx.Src_TrnSeqCntr][SO]1" +
//					"PAN:[GR ifx.actualAppPAN][SO]1" +
//					"TIME:[GR ifx.receivedDt][SO]1" +
//					"TERMINAL:[GR ifx.TerminalId]";
			
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	private byte[] simpleCreditStatementDataReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings/*, NDCConvertor convertor*/){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		byte[] temp;
		try{
			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			String lang = Language.FarsiLanguage.toString();
			
			out.write(generateHeader(atm, ifx, encodings, config, lineLength, leftMargin, textDir));
			
			if(config.getReceiptDetailsMap().containsKey("اطلاعات آخرین صورتحساب کارت اعتباری" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("اطلاعات آخرین صورتحساب کارت اعتباری"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("اطلاعات آخرین صورتحساب کارت اعتباری"));
				out.write(temp);
				config.setReceiptDetailsMap("اطلاعات آخرین صورتحساب کارت اعتباری"+"$center$" + lineLength + "$" + leftMargin, temp);		
			}			
			
			out.write(ASCIIConstants.LF);
//			String st = "[ESC](7[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//					"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//					"[GR center(GR c2F('اطلاعات آخرین صورتحساب کارت اعتباری'))][LF]" ;
			
			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));
			
			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("شماره کارت")){
				temp = convert2Farsi("شماره کارت");
				config.setReceiptDetailsMap("شماره کارت", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));

			
			if(!config.getReceiptDetailsMap().containsKey("مبلغ تراکنش ها")){
				temp = convert2Farsi("مبلغ تراکنش ها");
				config.setReceiptDetailsMap("مبلغ تراکنش ها", temp);		
			}			
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مبلغ تراکنش ها"), accBalAvailable(lang, "7", encodings, atm, ifx.getCreditTotalTransactionAmount(), 15)));
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("کارمزدها")){
				temp = convert2Farsi("کارمزدها");
				config.setReceiptDetailsMap("کارمزدها", temp);		
			}	
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("کارمزدها"), accBalAvailable(lang, "7", encodings, atm, ifx.getCreditTotalFeeAmount(), 12)));
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("جریمه دیرکرد")){
				temp = convert2Farsi("جریمه دیرکرد");
				config.setReceiptDetailsMap("جریمه دیرکرد", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("جریمه دیرکرد"), accBalAvailable(lang, "7", encodings, atm, ifx.getCreditInterest(), 12)));
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("مانده اعتبار")){
				temp = convert2Farsi("مانده اعتبار");
				config.setReceiptDetailsMap("مانده اعتبار", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مانده اعتبار"), accBalAvailable(lang, "7", encodings, atm, ifx.getCreditOpenToBuy(), 15)));
			
			out.write(putLF(6));
			
			out.write(justify(textDir, lineLength, leftMargin, convert2Farsi("مبلغ قابل پرداخت"), accBalAvailable(lang, "7", encodings, atm, ifx.getCreditStatementAmount(), 15)));
			out.write(ASCIIConstants.LF);
//			st += "[ESC](7[GR datePersianFormat(ifx.receivedDt)][LF][LF]" +
//					"[GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))][LF]" +
//					"[GR justify(GR c2F('مبلغ تراکنش ها'), GR amount2F(ifx.CreditTotalTransactionAmount, 15))][LF]" +
//					"[GR justify(GR c2F('کارمزدها'), GR amount2F(ifx.CreditTotalFeeAmount, 12))][LF]" +
//					"[GR justify(GR c2F('جریمه دیرکرد'), GR amount2F(ifx.CreditInterest, 12))][LF][LF]" +
//					"[GR justify(GR c2F('مانده اعتبار'), GR amount2F(ifx.CreditOpenToBuy, 15))]" +
//					"[GR putLF(6)]" +
//					"[GR justify(GR c2F('مبلغ قابل پرداخت'), GR amount2F(ifx.CreditStatementAmount, 15))][LF]" ;
			
			out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));
			
			out.write(ASCIIConstants.FF);
//			st += "[ESC](7[GR center(GR GR c2F(GR bnkMount2F()) )][FF]";
		}catch(IOException e){
				logger.error(e,e);
				return new byte[]{0x20};
			}
			return out.toByteArray();
	}
	
	private byte[] simpleChangeInternetPinJournal(Ifx ifx){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			out.write(ASCIIConstants.LF);
			out.write("Pin2 Change:".toUpperCase().getBytes()); //Task141 NCR
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("REF:".getBytes());
			out.write(ifx.getSrc_TrnSeqCntr().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("PAN:".getBytes());
			out.write(ifx.getActualAppPAN().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TIME:".getBytes());
			out.write(ifx.getReceivedDt().toString().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TERMINAL:".getBytes());
			out.write(ifx.getTerminalId().getBytes());
	
//			String st = "[LF]Pin2 Change:[SO]1" +
//					"REF:[GR ifx.Src_TrnSeqCntr][SO]1" +
//					"PAN:[GR ifx.actualAppPAN][SO]1" +
//					"TIME:[GR ifx.receivedDt][SO]1" +
//					"TERMINAL:[GR ifx.TerminalId]";
			
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	private byte[] simpleChangeInternetPinReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings/*, NDCConvertor convertor*/){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		byte[] temp;
		try{
			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			
			out.write(generateHeader(atm, ifx, encodings, config, lineLength, leftMargin, textDir));
			
			if(config.getReceiptDetailsMap().containsKey("رسید تغییر رمز اینترنتی" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید تغییر رمز اینترنتی"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید تغییر رمز اینترنتی"));
				out.write(temp);
				config.getReceiptDetailsMap().put("رسید تغییر رمز اینترنتی"+"$center$" + lineLength + "$" + leftMargin, temp);		
			}			
			
			out.write(ASCIIConstants.LF);
		
//			String st = "[ESC](7[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//					"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//					"[GR center(GR c2F('رسید تغییر رمز اینترنتی'))][LF]" ;
			
			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));
			
			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("رمز شما با موفقیت تغییر یافت")){
				temp = convert2Farsi("رمز شما با موفقیت تغییر یافت");
				config.setReceiptDetailsMap("رمز شما با موفقیت تغییر یافت", temp);
			}
			out.write(center(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("رمز شما با موفقیت تغییر یافت")));
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("شماره کارت")){
				temp = convert2Farsi("شماره کارت");
				config.setReceiptDetailsMap("شماره کارت", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));

			if(!config.getReceiptDetailsMap().containsKey("کد اعتبارسنجی دوم")){
				temp = convert2Farsi("کد اعتبارسنجی دوم");
				config.setReceiptDetailsMap("کد اعتبارسنجی دوم", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("کد اعتبارسنجی دوم"), convert2Farsi(ifx.getCVV2())));
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("تاریخ انقضای کارت")){
				temp = convert2Farsi("تاریخ انقضای کارت");
				config.setReceiptDetailsMap("تاریخ انقضای کارت", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("تاریخ انقضای کارت"), convert2Farsi(ifx.getExpDt())));
			
			out.write(putLF(8));
//			st+="[ESC](7[GR datePersianFormat(ifx.receivedDt)][LF][LF]" +
//					"[GR center(GR c2F('رمز شما با موفقیت تغییر یافت'))][LF][LF]" +
//					"[GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))][LF]" +
//					"[GR justify(GR c2F('کد اعتبارسنجی دوم'), GR c2F(ifx.CVV2))][LF]" +
//					"[GR justify(GR c2F('تاریخ انقضای کارت'), GR c2F(ifx.ExpDt))]" +
//					"[GR putLF(8)]" ;
			
			
			out.write(generateFooter(atm, config, encodings,lineLength, leftMargin, textDir));
			
			out.write(ASCIIConstants.FF);
//					st+="[ESC](7[GR center(GR GR c2F(GR bnkMount2F()) )][FF]";
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	
	private byte[] simpleChangePinJournal(Ifx ifx){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			out.write(ASCIIConstants.LF);
			out.write("Pin Change:".toUpperCase().getBytes()); //Task141 NCR
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("REF:".getBytes());
			out.write(ifx.getSrc_TrnSeqCntr().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("PAN:".getBytes());
			out.write(ifx.getActualAppPAN().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TIME:".getBytes());
			out.write(ifx.getReceivedDt().toString().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TERMINAL:".getBytes());
			out.write(ifx.getTerminalId().getBytes());

//			String st = "[LF]Pin Change:[SO]1" +
//					"REF:[GR ifx.Src_TrnSeqCntr][SO]1" +
//					"PAN:[GR ifx.actualAppPAN][SO]1" +
//					"TIME:[GR ifx.receivedDt][SO]1" +
//					"TERMINAL:[GR ifx.TerminalId]";
			
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	private byte[] simpleChangePinReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings/*, NDCConvertor convertor*/){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		byte[] temp;
		try{
			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			
			out.write(generateHeader(atm, ifx, encodings, config, lineLength, leftMargin, textDir));
			
			if(config.getReceiptDetailsMap().containsKey("رسید تغییر رمز" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید تغییر رمز"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید تغییر رمز"));
				out.write(temp);
				config.setReceiptDetailsMap("رسید تغییر رمز"+"$center$" + lineLength + "$" + leftMargin, temp);		
			}			
			
			out.write(ASCIIConstants.LF);
//			String st = "[ESC](7[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//					"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//					"[GR center(GR c2F('رسید تغییر رمز'))][LF]" ;
			
			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));
			
			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("رمز شما با موفقیت تغییر یافت")){
				temp = convert2Farsi("رمز شما با موفقیت تغییر یافت");
				config.setReceiptDetailsMap("رمز شما با موفقیت تغییر یافت", temp);
			}
			out.write(center(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("رمز شما با موفقیت تغییر یافت")));
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("شماره کارت")){
				temp = convert2Farsi("شماره کارت");
				config.setReceiptDetailsMap("شماره کارت", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
			
			out.write(putLF(7));
//			st += "[ESC](7[GR datePersianFormat(ifx.receivedDt)][LF][LF]" +
//							"[GR center(GR c2F('رمز شما با موفقیت تغییر یافت'))][LF][LF]" +
//							"[GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))]" +
//							"[GR putLF(7)]" ;
			
			out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));
			
			out.write(ASCIIConstants.FF);
			
//			st+="[ESC](7[GR center(GR GR c2F(GR bnkMount2F()) )][FF]";
			
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	private byte[] simpleTransferJournal(Ifx ifx){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			out.write(ASCIIConstants.LF);
			out.write("Transfer:".toUpperCase().getBytes()); //Task141 NCR
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write(ifx.getAuth_Amt().toString().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("REF:".getBytes());
			out.write(ifx.getSrc_TrnSeqCntr().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("PAN:".getBytes());
			out.write(ifx.getActualAppPAN().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TIME:".getBytes());
			out.write(ifx.getReceivedDt().toString().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TERMINAL:".getBytes());
			out.write(ifx.getTerminalId().getBytes());

//			String st = "[LF]Transfer:[SO]1" +
//					"[GR ifx.Auth_Amt][SO]1" +
//					"REF:[GR ifx.Src_TrnSeqCntr][SO]1" +
//					"PAN:[GR ifx.actualAppPAN][SO]1" +
//					"TIME:[GR ifx.receivedDt][SO]1" +
//					"TERMINAL:[GR ifx.TerminalId]";
			
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	private byte[] simpleTransferReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings/*, NDCConvertor convertor*/){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		byte[] temp;
		try{
			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			String lang = Language.FarsiLanguage.toString();
			
			out.write(generateHeader(atm, ifx, encodings, config, lineLength, leftMargin, textDir));
			
			if(config.getReceiptDetailsMap().containsKey("رسید انتقال وجه" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید انتقال وجه"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید انتقال وجه"));
				out.write(temp);
				config.setReceiptDetailsMap("رسید انتقال وجه"+"$center$" + lineLength + "$" + leftMargin, temp);		
			}			
			out.write(ASCIIConstants.LF);
//			String st = "[ESC](7[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//					"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//					"[GR center(GR c2F('رسید انتقال وجه'))][LF]" ;
			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));
					
			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("شماره پیگیری")){
				temp = convert2Farsi("شماره پیگیری");
				config.setReceiptDetailsMap("شماره پیگیری", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره پیگیری"), convert2Farsi(ifx.getSrc_TrnSeqCntr())));
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("مبلغ")){
				temp = convert2Farsi("مبلغ");
				config.setReceiptDetailsMap("مبلغ", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مبلغ"), accBalAvailable(lang, "7", encodings, atm, ifx.getAuth_Amt(), 15)));
			
			//TASK Task029 : Print Bank Name
			out.write(alignTextRight(textDir, lineLength, leftMargin, convert2Farsi("از "+ BankNameFa(encodings, atm, Util.longValueOf(ifx.getDestBankId())))));

			out.write(ASCIIConstants.LF);			
			
			if(!config.getReceiptDetailsMap().containsKey("از کارت")){
				temp = convert2Farsi("از کارت");
				config.setReceiptDetailsMap("از کارت", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("از کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
			out.write(ASCIIConstants.LF);
			
			//TASK Task029 : Print Bank Name
			out.write(alignTextRight(textDir, lineLength, leftMargin, convert2Farsi("به "+ BankNameFa(encodings, atm, Util.longValueOf(ifx.getRecvBankId())))));
			out.write(ASCIIConstants.LF);			
			

			//TASK Task029 : Print Bank Name
			if(!config.getReceiptDetailsMap().containsKey("به کارت")){
				temp = convert2Farsi("به کارت");
				config.setReceiptDetailsMap("به کارت", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("به کارت"), appPanFormatFa(encodings, atm, ifx.getActualSecondAppPan())));
			out.write(ASCIIConstants.LF);
			
			out.write(alignTextRight(textDir, lineLength, leftMargin, convert2Farsi("به نام "+ifx.getCardHolderName()+ " "+ ifx.getCardHolderFamily()+ " "+ "انتقال یافت")));
			
			out.write(putLF(10));
			
			out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));

			//TASK Task040 : DailyMessage
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			out.write(generateDailyMessage(atm, config,encodings, lineLength, leftMargin, textDir)); 
			
			out.write(ASCIIConstants.FF);
			
			/**** Print Bank Name: end ****/
			
			
			/*
			if(!config.getReceiptDetailsMap().containsKey("از کارت شماره")){
				temp = convert2Farsi("از کارت شماره");
				config.setReceiptDetailsMap("از کارت شماره", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("از کارت شماره"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("به کارت شماره")){
				temp = convert2Farsi("به کارت شماره");
				config.setReceiptDetailsMap("به کارت شماره", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("به کارت شماره"), appPanFormatFa(encodings, atm, ifx.getActualSecondAppPan())));
			out.write(ASCIIConstants.LF);
			
			out.write(alignTextRight(textDir, lineLength, leftMargin, convert2Farsi("به نام "+ifx.getCardHolderName()+ " "+ ifx.getCardHolderFamily()+ " "+ "انتقال یافت")));
			
			out.write(putLF(10));
//			st+="[ESC](7[GR datePersianFormat(ifx.receivedDt)][LF][LF]" +
//					"[GR justify(GR c2F('شماره پیگیری'), GR c2F(ifx.Src_TrnSeqCntr))][LF]" +
//					"[GR justify(GR c2F('مبلغ'), GR amount2F(ifx.Auth_Amt, 15))][LF]" +
//					"[GR justify(GR c2F('از کارت شماره'), GR appPanFa(ifx.actualAppPAN))][LF]" +
//					"[GR justify(GR c2F('به کارت شماره'), GR appPanFa(ifx.actualSecondAppPan))][LF]" +
//					"[GR right(GR c2F('به نام ' + ifx.CardHolderName + ' ' +  ifx.CardHolderFamily + ' ' + ' انتقال یافت'))]" +
//					"[GR putLF(10)]" ;
			
			out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));
			
			out.write(ASCIIConstants.FF);
			
//			st+="[ESC](7[GR center(GR GR c2F(GR bnkMount2F()) )][FF]";
 * 
 */
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	private byte[] simpleBillPaymentJournal(Ifx ifx){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			out.write(ASCIIConstants.LF);
			out.write("Bill Payment:".toUpperCase().getBytes()); //Task141 : NCR
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write(ifx.getAuth_Amt().toString().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("REF:".getBytes());
			out.write(ifx.getSrc_TrnSeqCntr().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("PAN:".getBytes());
			out.write(ifx.getActualAppPAN().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TIME:".getBytes());
			out.write(ifx.getReceivedDt().toString().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TERMINAL:".getBytes());
			out.write(ifx.getTerminalId().getBytes());

//			String st= "[LF]Bill Payment:[SO]1" +
//					"[GR ifx.Auth_Amt][SO]1" +
//					"REF:[GR ifx.Src_TrnSeqCntr][SO]1" +
//					"PAN:[GR ifx.actualAppPAN][SO]1" +
//					"TIME:[GR ifx.receivedDt][SO]1" +
//					"TERMINAL:[GR ifx.TerminalId]";
			
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
		
	}
	private byte[] simpleBillPaymentReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings /*, NDCConvertor convertor*/){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		byte[] temp;
		try{
			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			String lang = Language.FarsiLanguage.toString();
			
			out.write(generateHeader(atm, ifx, encodings, config, lineLength, leftMargin, textDir));
			
			if(config.getReceiptDetailsMap().containsKey("رسید پرداخت قبض" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید پرداخت قبض"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید پرداخت قبض"));
				out.write(temp);
				config.setReceiptDetailsMap("رسید پرداخت قبض"+"$center$" + lineLength + "$" + leftMargin, temp);		
			}
			
			out.write(ASCIIConstants.LF);
			
//			String st = "[ESC](7[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//					"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//					"[GR center(GR c2F('رسید پرداخت قبض'))][LF]" ;
			
			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));
			
//			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
//			out.write(ASCIIConstants.LF);
//			out.write(ASCIIConstants.LF);
//			
//			
//			if(!config.getReceiptDetailsMap().containsKey("شماره کارت")){
//				temp = convert2Farsi("شماره کارت");
//				config.setReceiptDetailsMap("شماره کارت", temp);		
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
//			out.write(ASCIIConstants.LF);
//			
//			
//			if(!config.getReceiptDetailsMap().containsKey("شماره پیگیری")){
//				temp = convert2Farsi("شماره پیگیری");
//				config.setReceiptDetailsMap("شماره پیگیری", temp);
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره پیگیری"), convert2Farsi(ifx.getSrc_TrnSeqCntr())));
//			out.write(ASCIIConstants.LF);
			
			out.write(generateDateAndAppPanAndTrn_Seq(atm, config, encodings, lineLength, leftMargin, textDir, ifx));
			
			if(!config.getReceiptDetailsMap().containsKey("شناسه قبض")){
				temp = convert2Farsi("شناسه قبض");
				config.setReceiptDetailsMap("شناسه قبض", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شناسه قبض"), convert2Farsi(ifx.getBillID())));
			out.write(ASCIIConstants.LF);
			
			
			if(!config.getReceiptDetailsMap().containsKey("شناسه پرداخت")){
				temp = convert2Farsi("شناسه پرداخت");
				config.setReceiptDetailsMap("شناسه پرداخت", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شناسه پرداخت"), convert2Farsi(ifx.getBillPaymentID())));
			out.write(ASCIIConstants.LF);
			
			// TASK Task134 [28737] :  Allign Orgaization in ATM Receipt
			// AldComment Montazere tasmim ....
			if(!config.getReceiptDetailsMap().containsKey("مبلغ")){
			temp = convert2Farsi("مبلغ");
			config.setReceiptDetailsMap("مبلغ", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مبلغ"), accBalAvailable(lang, "7", encodings, atm, ifx.getAuth_Amt(), 15)));

			//سازمان
			out.write(alignTextRight(textDir, lineLength, leftMargin, convert2Farsi(ifx.getThirdPartyName())));
			out.write(ASCIIConstants.LF);
			
			//before Task134
//			if(!config.getReceiptDetailsMap().containsKey("سازمان")){
//				temp = convert2Farsi("سازمان");
//				config.setReceiptDetailsMap("سازمان", temp);
//			}
////			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("سازمان"), convert2Farsi(ifx.getBillOrgType())));
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("سازمان"), convert2Farsi(ifx.getThirdPartyName())));
//			out.write(ASCIIConstants.LF);			
			
//			if(!config.getReceiptDetailsMap().containsKey("مبلغ")){
//				temp = convert2Farsi("مبلغ");
//				config.setReceiptDetailsMap("مبلغ", temp);
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مبلغ"), accBalAvailable(lang, "7", encodings, atm, ifx.getAuth_Amt(), 15)));
			//end befor			
			
			
			out.write(putLF(8));
//			st += "[ESC](7[GR datePersianFormat(ifx.receivedDt)][LF][LF]" +
//					"[GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))][LF]" +
//					"[GR justify(GR c2F('شماره پیگیری'), GR c2F(ifx.Src_TrnSeqCntr))][LF]" +
//					"[GR justify(GR c2F('شناسه قبض'), GR c2F(ifx.BillID))][LF]" +
//					"[GR justify(GR c2F('شناسه پرداخت'), GR c2F(ifx.BillPaymentID))][LF]" +
//					"[GR justify(GR c2F('سازمان'), GR c2F(ifx.BillOrgType))][LF]" +
//					"[GR justify(GR c2F('مبلغ'), GR amount2F(ifx.Auth_Amt, 15))]" +
//					"[GR putLF(8)]";
			
			out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));
			
			out.write(ASCIIConstants.FF);
//			st +="[ESC](7[GR center(GR GR c2F(GR bnkMount2F()) )][FF]";
		}catch(IOException e) {
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	private byte[] simpleBankStatementJournal(Ifx ifx){
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			out.write(ASCIIConstants.LF);
			out.write("Statement:".toUpperCase().getBytes()); //Task141 : NCR
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("REF:".getBytes());
			out.write(ifx.getSrc_TrnSeqCntr().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("PAN:".getBytes());
			out.write(ifx.getActualAppPAN().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TIME:".getBytes());
			out.write(ifx.getReceivedDt().toString().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TERMINAL:".getBytes());
			out.write(ifx.getTerminalId().getBytes());

//			String st ="[LF]Statement:[SO]1" +
//					"REF:[GR ifx.Src_TrnSeqCntr][SO]1" +
//					"PAN:[GR ifx.actualAppPAN][SO]1" +
//					"TIME:[GR ifx.receivedDt][SO]1" +
//					"TERMINAL:[GR ifx.TerminalId]";
			
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	
	private byte[] simpleCharTable(ATMTerminal atm, Ifx ifx, Map<String, String> encodings){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		byte[] temp;
		ByteArrayOutputStream table = new ByteArrayOutputStream();

		try{
//			for(int c=1; c<=7; c++){
//			table.write(ASCIIConstants.ESC);
//			table.write("3".getBytes());

//				table.write(0x31);
//				table.write(ASCIIConstants.CR);
//				table.write(ASCIIConstants.LF);
//				table.write(ASCIIConstants.CR);
//				table.write(ASCIIConstants.LF);
//
//				table.write(ASCIIConstants.ESC);
//				table.write(")7".getBytes());
////				table.write(0x31);
//				table.write(0x20);
//				table.write(0x20);
//				for(int j=0; j<16; j++){
//					table.write(0x30+(byte) (j%10));
//				}
//				table.write(ASCIIConstants.CR);
//				table.write(ASCIIConstants.LF);
//
//				for(int i=2; i<16; i++){
//					table.write(0x30+(byte) (i%10));
//					table.write(0x20);
//
//					for(int j=0; j<16; j++){
//						table.write((byte)(i*16+j));
//					}
//					table.write(ASCIIConstants.CR);
//					table.write(ASCIIConstants.LF);
//				}
////			}
//
//			table.write(ASCIIConstants.FF);


//			for(int i=0 ; i<16; i++){
//				for(int j=0; j<16; j++){
//					if(j==1)
//						table.write(0x20);
//					else if(i == 0 && j== 0)
//						table.write(0x20);
//					else if(j == 0)
//						table.write(30 + (i-1));
//					else if (i==0 && j>=2){
//						table.write(0x30);
//					}
//					else if( j >= 11  && i==0){
//						table.write(( String.valueOf(30+j-11)).getBytes());
//					}
//					else if(j>=2 && i>0){
//						table.write((String.valueOf(i-1) + String.valueOf(j-2)).getBytes());
//
//					}
//
//				}
//				table.write(ASCIIConstants.LF);
//			}

			for(int j=0; j<16; j++){
				table.write(0x30+(byte) (j%10));
			}
			table.write(ASCIIConstants.CR);
			table.write(ASCIIConstants.LF);

			for(int i=2; i<16; i++){
				table.write(0x30+(byte) (i%10));
				table.write(0x20);

				for(int j=0; j<16; j++){
					table.write((byte)(i*16+j));
				}
				table.write(ASCIIConstants.CR);
				table.write(ASCIIConstants.LF);
			}

			table.write(ASCIIConstants.FF);

		}catch(Exception e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return table.toByteArray();
	}
	
	private byte[] simpleBankStatementReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings/*, NDCConvertor convertor*/){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		byte[] temp;
		try{
			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			String lang = Language.FarsiLanguage.toString();
			
			out.write(generateHeader(atm, ifx, encodings, config, lineLength, leftMargin, textDir));
			
			if(config.getReceiptDetailsMap().containsKey("رسید صورتحساب" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید صورتحساب"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید صورتحساب"));
				out.write(temp);
				config.setReceiptDetailsMap("رسید صورتحساب"+"$center$" + lineLength + "$" + leftMargin, temp);		
			}
			
			out.write(ASCIIConstants.LF);
			
//			String st = "[ESC](7[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//					"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//					"[GR center(GR c2F('رسید صورتحساب'))][LF]" ;
			
			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));

			
			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("شماره کارت")){
				temp = convert2Farsi("شماره کارت");
				config.setReceiptDetailsMap("شماره کارت", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
			out.write(ASCIIConstants.LF);
			
			out.write(justify(textDir, lineLength, leftMargin, convert2Farsi(subsidiaryStatement2Farsi(ifx.getSubsidiaryAccFrom(),"شماره حساب")), accNumbertConvert2Farsi(accountFormat(textDir, ifx.getSubsidiaryAccFrom()))));
			out.write(ASCIIConstants.LF);
			
			out.write(bankStatementTableFa(ifx, lineLength, leftMargin, encodings));
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("مانده حساب")){
				temp = convert2Farsi("مانده حساب");
				config.setReceiptDetailsMap("مانده حساب", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مانده حساب"), accBalAvailable(lang, "7", encodings, atm, ifx.getAcctBalLedgerAmt(), 15)));
			
			out.write(putLF(2));			
			temp = center(textDir, lineLength, leftMargin, convert2Farsi("پس ازرویت رسید،آن رامعدوم نمایید"));
			out.write(temp) ; 
			
			out.write(putLF(2));
			
//			st+="[ESC](7[GR datePersianFormat(ifx.receivedDt)][LF][LF][GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))][LF]" +
//					"[GR justify(GR c2F( GR subsidiaryState2F(ifx.subsidiaryAccFrom, 'شماره حساب')), GR (accountFormat(ifx.subsidiaryAccFrom)))][LF]" +
//					"[GR bankStatementTableFa()][LF]" +
//					"[GR justify(GR c2F('مانده حساب'), GR amount2F(ifx.AcctBalLedgerAmt, 15))]" +
//					"[GR putLF(2)]" ;

			out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));

			out.write(ASCIIConstants.FF);
//			st += "[ESC](7[GR center(GR GR c2F(GR bnkMount2F()) )][FF]";
			
		}catch (IOException e) {
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	private byte[] simpleThirdPartyPaymentJournal(Ifx ifx){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			out.write(ASCIIConstants.LF);
			out.write("ThirdPartyPayment:".toUpperCase().getBytes()); //Task141 : NCR
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write(ifx.getAuth_Amt().toString().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("REF:".getBytes());
			out.write(ifx.getSrc_TrnSeqCntr().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("PAN:".getBytes());
			out.write(ifx.getActualAppPAN().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TIME:".getBytes());
			out.write(ifx.getReceivedDt().toString().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TERMINAL:".getBytes());
			out.write(ifx.getTerminalId().getBytes());

			
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	
	//Mirkamali(Task175): Restriction
	private byte[] simpleRestrictionJournal(Ifx ifx){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			out.write(ASCIIConstants.LF);
			out.write("Restriction:".toUpperCase().getBytes()); //Task141 : NCR
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write(ifx.getAuth_Amt().toString().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("REF:".getBytes());
			out.write(ifx.getSrc_TrnSeqCntr().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("PAN:".getBytes());
			out.write(ifx.getActualAppPAN().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TIME:".getBytes());
			out.write(ifx.getReceivedDt().toString().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TERMINAL:".getBytes());
			out.write(ifx.getTerminalId().getBytes());

			
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	
	
	private byte[] simpleBalanceJournal(Ifx ifx){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			out.write(ASCIIConstants.LF);
			out.write("Balance:".toUpperCase().getBytes());//Task141 : NCR
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			out.write(ifx.getAuth_Amt().toString().getBytes());			
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("REF:".getBytes());
			out.write(ifx.getSrc_TrnSeqCntr().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("PAN:".getBytes());
			out.write(ifx.getActualAppPAN().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TIME:".getBytes());
			out.write(ifx.getReceivedDt().toString().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TERMINAL:".getBytes());
			out.write(ifx.getTerminalId().getBytes());

//			String st = "[LF]Balance:[SO]1[GR ifx.Auth_Amt][SO]1" +
//					"REF:[GR ifx.Src_TrnSeqCntr][SO]1" +
//					"PAN:[GR ifx.actualAppPAN][SO]1" +
//					"TIME:[GR ifx.receivedDt][SO]1" +
//					"TERMINAL:[GR ifx.TerminalId]";
			
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	private byte[] simpleBalanceReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings/*,NDCConvertor convertor*/){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			String lang = Language.FarsiLanguage.toString();
			byte[] temp;
			
			out.write(generateHeader(atm, ifx,encodings, config, lineLength, leftMargin, textDir));
			
			if(config.getReceiptDetailsMap().containsKey("رسید موجودی" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید موجودی"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید موجودی"));
				out.write(temp);
				config.setReceiptDetailsMap("رسید موجودی"+"$center$" + lineLength + "$" + leftMargin, temp);		
			}
			
			out.write(ASCIIConstants.LF);
			
			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));
			
//			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
//			
//			out.write(ASCIIConstants.LF);
//			out.write(ASCIIConstants.LF);
//			
//			if(!config.getReceiptDetailsMap().containsKey("شماره کارت")){
//				temp = convert2Farsi("شماره کارت");
//				config.setReceiptDetailsMap("شماره کارت", temp);		
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
//			out.write(ASCIIConstants.LF);
//			
//			if(!config.getReceiptDetailsMap().containsKey("شماره پیگیری")){
//				temp = convert2Farsi("شماره پیگیری");
//				config.setReceiptDetailsMap("شماره پیگیری", temp);		
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره پیگیری"), convert2Farsi(ifx.getSrc_TrnSeqCntr())));
//			out.write(ASCIIConstants.LF);
			
			out.write(generateDateAndAppPanAndTrn_Seq(atm, config, encodings, lineLength, leftMargin, textDir, ifx));
			
			if(!config.getReceiptDetailsMap().containsKey("مانده حساب")){
				temp = convert2Farsi("مانده حساب");
				config.setReceiptDetailsMap("مانده حساب", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مانده حساب"), accBalAvailable(lang, "7", encodings, atm, ifx.getAcctBalLedgerAmt(), 15)));
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("قابل برداشت")){
				temp = convert2Farsi("قابل برداشت");
				config.setReceiptDetailsMap("قابل برداشت", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("قابل برداشت"), accBalAvailable(lang, "7", encodings, atm, ifx.getAcctBalAvailableAmt(), 15)));
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("شماره حساب")){
				temp = convert2Farsi("شماره حساب");
				config.setReceiptDetailsMap("شماره حساب", temp);		
			}
			if(Util.hasText(ifx.getSubsidiaryAccFrom())){
				out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره حساب"), accNumbertConvert2Farsi(accountFormat(textDir, ifx.getSubsidiaryAccFrom()))));
			}
			
			out.write(putLF(10));
			
//			st +="[ESC](7[GR datePersianFormat(ifx.receivedDt)][LF][LF][GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))][LF][GR justify(GR c2F('شماره پیگیری'), GR c2F(ifx.Src_TrnSeqCntr))][LF]"+
//			"[GR justify(GR c2F('مانده حساب'), GR amount2F(ifx.AcctBalLedgerAmt, 15))][LF]"+
//			"[GR justify(GR c2F('قابل برداشت'), GR amount2F(ifx.AcctBalAvailableAmt, 15))][LF]"+
//			"[GR justify(GR c2F( GR subsidiaryState2F(ifx.subsidiaryAccFrom, 'شماره حساب')), GR (accountFormat(ifx.subsidiaryAccFrom)))]"+
//			"[GR putLF(10)][ESC]" ;
			
			
			out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));
			out.write(ASCIIConstants.FF);

//			st+="[ESC](7[GR center(GR GR c2F(GR bnkMount2F()) )][FF]";
//			[ESC](7[GR justify(GR GR c2F(GR bnkName2F()),GR GR c2F(GR bnkMount2F()) )][FF]
			
		} catch (IOException e) {
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	
	private byte[] simpleWithdrawalJournal(Ifx ifx){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			out.write(ASCIIConstants.LF);
			out.write("Withdrawal:".toUpperCase().getBytes()); //Task141 : NCR
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write(ifx.getAuth_Amt().toString().getBytes());			
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("REF:".getBytes());
			out.write(ifx.getSrc_TrnSeqCntr().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("PAN:".getBytes());
			out.write(ifx.getActualAppPAN().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TIME:".getBytes());
			out.write(ifx.getReceivedDt().toString().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TERMINAL:".getBytes());
			out.write(ifx.getTerminalId().getBytes());
			
//			String st = "[LF]Withdrawal:[SO]1" +
//					"[GR ifx.Auth_Amt][SO]1" +
//					"REF:[GR ifx.Src_TrnSeqCntr][SO]1" +
//					"PAN:[GR ifx.actualAppPAN][SO]1" +
//					"TIME:[GR ifx.receivedDt][SO]1" +
//					"TERMINAL:[GR ifx.TerminalId]";
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	
	
	//Mirkamali(Task179): Currency ATM
	private byte[] simpleWithdrawalCurJournal(Ifx ifx){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			out.write(ASCIIConstants.LF);
			out.write("WithdrawalCurrency:".toUpperCase().getBytes()); //Task141 : NCR
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write(ifx.getAuth_Amt().toString().getBytes());			
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("REF:".getBytes());
			out.write(ifx.getSrc_TrnSeqCntr().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("PAN:".getBytes());
			out.write(ifx.getActualAppPAN().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TIME:".getBytes());
			out.write(ifx.getReceivedDt().toString().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TERMINAL:".getBytes());
			out.write(ifx.getTerminalId().getBytes());
			
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	
	private byte[] simpleWithdrawalReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings){//partialDispense use this method too!
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			String lang = Language.FarsiLanguage.toString();
			byte[] temp;

			out.write(generateHeader(atm, ifx, encodings, config, lineLength, leftMargin, textDir));

			if(config.getReceiptDetailsMap().containsKey("رسید برداشت وجه" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید برداشت وجه"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید برداشت وجه"));
				out.write(temp);
				config.setReceiptDetailsMap("رسید برداشت وجه"+"$center$" + lineLength + "$" + leftMargin, temp);
			}

			out.write(ASCIIConstants.LF);

//			String st = "[ESC](7" +
//					"[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//					"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//					"[GR center(GR c2F('رسید برداشت وجه')) ][LF]" ;


			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));


//			st += "[ESC](1" +
//			"[GR hr(0xcd)]" +
//			"[ESC](7" +
//			"[GR datePersianFormat(ifx.receivedDt)][LF][LF]" ;

//			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
//			out.write(ASCIIConstants.LF);
//			out.write(ASCIIConstants.LF);
//
//
//
//
//			if(!config.getReceiptDetailsMap().containsKey("شماره کارت")){
//				temp = convert2Farsi("شماره کارت");
//				config.setReceiptDetailsMap("شماره کارت", temp);
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
//			out.write(ASCIIConstants.LF);
//
//			if(!config.getReceiptDetailsMap().containsKey("شماره پیگیری")){
//				temp = convert2Farsi("شماره پیگیری");
//				config.setReceiptDetailsMap("شماره پیگیری", temp);
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره پیگیری"), convert2Farsi(ifx.getSrc_TrnSeqCntr())));
//			out.write(ASCIIConstants.LF);

			out.write(generateDateAndAppPanAndTrn_Seq(atm, config, encodings, lineLength, leftMargin, textDir, ifx));

			if(!config.getReceiptDetailsMap().containsKey("مبلغ")){
				temp = convert2Farsi("مبلغ");
				config.setReceiptDetailsMap("مبلغ", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مبلغ"), accBalAvailable(lang, "7", encodings, atm, ifx.getAuth_Amt(), 15)));
			out.write(ASCIIConstants.LF);

			
			if(!config.getReceiptDetailsMap().containsKey("مانده حساب")){
				temp = convert2Farsi("مانده حساب");
				config.setReceiptDetailsMap("مانده حساب", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مانده حساب"), accBalAvailable(lang, "7", encodings, atm, ifx.getAcctBalLedgerAmt(), 15)));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("قابل برداشت")){
				temp = convert2Farsi("قابل برداشت");
				config.setReceiptDetailsMap("قابل برداشت", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("قابل برداشت"), accBalAvailable(lang, "7", encodings, atm, ifx.getAcctBalAvailableAmt(), 15)));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("شماره حساب")){
				temp = convert2Farsi("شماره حساب");
				config.setReceiptDetailsMap("شماره حساب", temp);
			}
			if(Util.hasText(ifx.getSubsidiaryAccFrom())){
				out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره حساب"), accNumbertConvert2Farsi(accountFormat(textDir, ifx.getSubsidiaryAccFrom()))));
			}

			out.write(putLF(10));

			out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));
			
			//TASK Task040 : DailyMessage
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			out.write(generateDailyMessage(atm, config,encodings, lineLength, leftMargin, textDir));			

			out.write(ASCIIConstants.FF);

//			st += "[GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))][LF]" +
//					"[GR justify(GR c2F('شماره پیگیری'), GR c2F(ifx.Src_TrnSeqCntr))][LF]" +
//					"[GR justify(GR c2F('مبلغ'), GR amount2F(ifx.Auth_Amt, 15))][LF]" +
//					"[GR justify(GR c2F('مانده حساب'), GR amount2F(ifx.AcctBalLedgerAmt, 15))][LF]" +
//					"[GR justify(GR c2F('قابل برداشت'), GR amount2F(ifx.AcctBalAvailableAmt, 15))][LF]" +
//					"[GR justify(GR c2F( GR subsidiaryState2F(ifx.subsidiaryAccFrom, 'شماره حساب')), GR (accountFormat(ifx.subsidiaryAccFrom)))]" +
//					"[GR putLF(8)]" +
//					"[ESC](1[GR hr(0xcd)]" +
//					"[ESC](7[GR center(GR GR c2F(GR bnkMount2F()) )][FF]";

		} catch (IOException e) {
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	
	//Mirkamali(Task179): Currency ATM
	private byte[] simpleWithdrawalCurReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings){//partialDispense use this method too!
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			String lang = Language.FarsiLanguage.toString();
			byte[] temp;

			out.write(generateHeader(atm, ifx, encodings, config, lineLength, leftMargin, textDir));

			if(config.getReceiptDetailsMap().containsKey("رسید برداشت ارز" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید برداشت ارز"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید برداشت ارز"));
				out.write(temp);
				config.setReceiptDetailsMap("رسید برداشت ارز"+"$center$" + lineLength + "$" + leftMargin, temp);
			}

			out.write(ASCIIConstants.LF);

			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));

			out.write(generateDateAndAppPanAndTrn_Seq(atm, config, encodings, lineLength, leftMargin, textDir, ifx));

			if(!config.getReceiptDetailsMap().containsKey("مبلغ به ارز")){
				temp = convert2Farsi("مبلغ به ارز");
				config.setReceiptDetailsMap("مبلغ به ارز", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مبلغ به ارز"), accBalAvailableCurrency(lang, "7", encodings, atm, ifx.getReal_Amt(), 15)));
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("نرخ تبدل ارز")){
				temp = convert2Farsi("نرخ تبدیل ارز");
				config.setReceiptDetailsMap("نرخ تبدیل ارز", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("نرخ تبدیل ارز"), accBalAvailable(lang, "7", encodings, atm, ifx.getAuth_CurRate(), 15)));
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("کارمزد")){
				temp = convert2Farsi("کارمزد");
				config.setReceiptDetailsMap("کارمزد", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("کارمزد"), accBalAvailable(lang, "7", encodings, atm, ifx.getTotalFeeAmt(), 15)));
			out.write(ASCIIConstants.LF);
			
			
			if(!config.getReceiptDetailsMap().containsKey("مبلغ")){
				temp = convert2Farsi("مبلغ");
				config.setReceiptDetailsMap("مبلغ", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مبلغ"), accBalAvailable(lang, "7", encodings, atm, ifx.getAuth_Amt(), 15)));
			out.write(ASCIIConstants.LF);
			
			
			if(!config.getReceiptDetailsMap().containsKey("مانده حساب")){
				temp = convert2Farsi("مانده حساب");
				config.setReceiptDetailsMap("مانده حساب", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مانده حساب"), accBalAvailable(lang, "7", encodings, atm, ifx.getAcctBalLedgerAmt(), 15)));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("قابل برداشت")){
				temp = convert2Farsi("قابل برداشت");
				config.setReceiptDetailsMap("قابل برداشت", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("قابل برداشت"), accBalAvailable(lang, "7", encodings, atm, ifx.getAcctBalAvailableAmt(), 15)));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("شماره حساب")){
				temp = convert2Farsi("شماره حساب");
				config.setReceiptDetailsMap("شماره حساب", temp);
			}
			if(Util.hasText(ifx.getSubsidiaryAccFrom())){
				out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره حساب"), accNumbertConvert2Farsi(accountFormat(textDir, ifx.getSubsidiaryAccFrom()))));
			}

			out.write(putLF(10));

			out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));
			
			//TASK Task040 : DailyMessage
//			out.write(ASCIIConstants.LF);
//			out.write(ASCIIConstants.LF);
//			out.write(generateDailyMessage(atm, config,encodings, lineLength, leftMargin, textDir));			

			out.write(ASCIIConstants.FF);

//			st += "[GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))][LF]" +
//					"[GR justify(GR c2F('شماره پیگیری'), GR c2F(ifx.Src_TrnSeqCntr))][LF]" +
//					"[GR justify(GR c2F('مبلغ'), GR amount2F(ifx.Auth_Amt, 15))][LF]" +
//					"[GR justify(GR c2F('مانده حساب'), GR amount2F(ifx.AcctBalLedgerAmt, 15))][LF]" +
//					"[GR justify(GR c2F('قابل برداشت'), GR amount2F(ifx.AcctBalAvailableAmt, 15))][LF]" +
//					"[GR justify(GR c2F( GR subsidiaryState2F(ifx.subsidiaryAccFrom, 'شماره حساب')), GR (accountFormat(ifx.subsidiaryAccFrom)))]" +
//					"[GR putLF(8)]" +
//					"[ESC](1[GR hr(0xcd)]" +
//					"[ESC](7[GR center(GR GR c2F(GR bnkMount2F()) )][FF]";

		} catch (IOException e) {
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}

	private byte[] simplePartialDispenceReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		try {
			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			String lang = Language.FarsiLanguage.toString();
			byte[] temp;

			out.write(generateHeader(atm, ifx, encodings, config, lineLength, leftMargin, textDir));

			if(config.getReceiptDetailsMap().containsKey("رسید برداشت وجه" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید برداشت وجه"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید برداشت وجه"));
				out.write(temp);
				config.setReceiptDetailsMap("رسید برداشت وجه"+"$center$" + lineLength + "$" + leftMargin, temp);
			}

			out.write(ASCIIConstants.LF);

//			String st = "[ESC](7" +
//					"[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//					"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//					"[GR center(GR c2F('رسید برداشت وجه')) ][LF]" ;


			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));


//			st += "[ESC](1" +
//			"[GR hr(0xcd)]" +
//			"[ESC](7" +
//			"[GR datePersianFormat(ifx.receivedDt)][LF][LF]" ;

//			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
//			out.write(ASCIIConstants.LF);
//			out.write(ASCIIConstants.LF);
//
//
//
//
//			if(!config.getReceiptDetailsMap().containsKey("شماره کارت")){
//				temp = convert2Farsi("شماره کارت");
//				config.setReceiptDetailsMap("شماره کارت", temp);
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
//			out.write(ASCIIConstants.LF);
//
//			if(!config.getReceiptDetailsMap().containsKey("شماره پیگیری")){
//				temp = convert2Farsi("شماره پیگیری");
//				config.setReceiptDetailsMap("شماره پیگیری", temp);
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره پیگیری"), convert2Farsi(ifx.getSrc_TrnSeqCntr())));
//			out.write(ASCIIConstants.LF);

			out.write(generateDateAndAppPanAndTrn_Seq(atm, config, encodings, lineLength, leftMargin, textDir, ifx));


			if(!config.getReceiptDetailsMap().containsKey("مبلغ")){
				temp = convert2Farsi("مبلغ");
				config.setReceiptDetailsMap("مبلغ", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مبلغ"), accBalAvailable(lang, "7", encodings, atm, ifx.getAuth_Amt(), 15)));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("مانده حساب")){
				temp = convert2Farsi("مانده حساب");
				config.setReceiptDetailsMap("مانده حساب", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مانده حساب"), accBalAvailable(lang, "7", encodings, atm, ifx.getAcctBalLedgerAmt(), 15)));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("قابل برداشت")){
				temp = convert2Farsi("قابل برداشت");
				config.setReceiptDetailsMap("قابل برداشت", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("قابل برداشت"), accBalAvailable(lang, "7", encodings, atm, ifx.getAcctBalAvailableAmt(), 15)));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("شماره حساب")){
				temp = convert2Farsi("شماره حساب");
				config.setReceiptDetailsMap("شماره حساب", temp);
			}
			if(Util.hasText(ifx.getSubsidiaryAccFrom())){
				out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره حساب"), accNumbertConvert2Farsi(accountFormat(textDir, ifx.getSubsidiaryAccFrom()))));
			}

			out.write(putLF(10));

			out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));

			out.write(ASCIIConstants.FF);

//			st += "[GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))][LF]" +
//					"[GR justify(GR c2F('شماره پیگیری'), GR c2F(ifx.Src_TrnSeqCntr))][LF]" +
//					"[GR justify(GR c2F('مبلغ'), GR amount2F(ifx.Auth_Amt, 15))][LF]" +
//					"[GR justify(GR c2F('مانده حساب'), GR amount2F(ifx.AcctBalLedgerAmt, 15))][LF]" +
//					"[GR justify(GR c2F('قابل برداشت'), GR amount2F(ifx.AcctBalAvailableAmt, 15))][LF]" +
//					"[GR justify(GR c2F( GR subsidiaryState2F(ifx.subsidiaryAccFrom, 'شماره حساب')), GR (accountFormat(ifx.subsidiaryAccFrom)))]" +
//					"[GR putLF(8)]" +
//					"[ESC](1[GR hr(0xcd)]" +
//					"[ESC](7[GR center(GR GR c2F(GR bnkMount2F()) )][FF]";

		} catch (IOException e) {
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	private byte[] simplePartialDispenceJournal(Ifx ifx){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			out.write(ASCIIConstants.LF);
			out.write("Partial Widthdrawal:".toUpperCase().getBytes()); //Task141 : NCR
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write(ifx.getAuth_Amt().toString().getBytes());			
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("REF:".getBytes());
			out.write(ifx.getSrc_TrnSeqCntr().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("PAN:".getBytes());
			out.write(ifx.getActualAppPAN().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TIME:".getBytes());
			out.write(ifx.getReceivedDt().toString().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TERMINAL:".getBytes());
			out.write(ifx.getTerminalId().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("Step:".getBytes());
			out.write(ifx.getCurrentStep());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("Total Steps:".getBytes());
			out.write(ifx.getTotalStep());
			
//			String st ="[LF]Partial Widthdrawal:[SO]1" +
//					"[GR ifx.Auth_Amt][SO]1" +
//					"REF:[GR ifx.Src_TrnSeqCntr][SO]1" +
//					"PAN:[GR ifx.actualAppPAN][SO]1" +
//					"TIME:[GR ifx.receivedDt][SO]1" +
//					"TERMINAL:[GR ifx.TerminalId][SO]1" +
//					"Step:[GR ifx.currentStep][SO]1" +
//					"Total Steps:[GR ifx.totalStep]";
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
		
	}
	private byte[] simplePartialDispenceErrorReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		try {
			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			String lang = Language.FarsiLanguage.toString();
			byte[] temp;
			
			out.write(generateHeader(atm, ifx, encodings, config, lineLength, leftMargin, textDir));
			
			if(config.getReceiptDetailsMap().containsKey("رسید برداشت وجه" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید برداشت وجه"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید برداشت وجه"));
				out.write(temp);
				config.setReceiptDetailsMap("رسید برداشت وجه"+"$center$" + lineLength + "$" + leftMargin, temp);		
			}
			
			out.write(ASCIIConstants.LF);
//			String st = "[ESC](7" +
//				"[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//				"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//				"[GR center(GR c2F('رسید برداشت وجه')) ][LF]" ;
		
			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));

//			st += "[ESC](1" +
//			"[GR hr(0xcd)]" +
//			"[ESC](7" +
//			"[GR datePersianFormat(ifx.receivedDt)][LF][LF]" ;
			
//			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
//			out.write(ASCIIConstants.LF);
//			out.write(ASCIIConstants.LF);
//			
//			
//			if(!config.getReceiptDetailsMap().containsKey("شماره کارت")){
//				temp = convert2Farsi("شماره کارت");
//				config.setReceiptDetailsMap("شماره کارت", temp);		
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
//			out.write(ASCIIConstants.LF);
//			
//			if(!config.getReceiptDetailsMap().containsKey("شماره پیگیری")){
//				temp = convert2Farsi("شماره پیگیری");
//				config.setReceiptDetailsMap("شماره پیگیری", temp);		
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره پیگیری"), convert2Farsi(ifx.getSrc_TrnSeqCntr())));
//			out.write(ASCIIConstants.LF);
			
			out.write(generateDateAndAppPanAndTrn_Seq(atm, config, encodings, lineLength, leftMargin, textDir, ifx));
			
//			out.write(justify(textDir, lineLength, leftMargin, convert2Farsi("مبلغ"), accBalAvailable(lang, "7", encodings, atm, partialDispense(atm, ifx), 15)));
//			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("مبلغ")){
				temp = convert2Farsi("مبلغ");
				config.setReceiptDetailsMap("مبلغ", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مبلغ"), accBalAvailable(lang, "7", encodings, atm, partialDispense(atm, ifx), 15)));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("مانده حساب")){
				temp = convert2Farsi("مانده حساب");
				config.setReceiptDetailsMap("مانده حساب", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مانده حساب"), accBalAvailable(lang, "7", encodings, atm, ifx.getAcctBalLedgerAmt(), ifx, 15)));
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("قابل برداشت")){
				temp = convert2Farsi("قابل برداشت");
				config.setReceiptDetailsMap("قابل برداشت", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("قابل برداشت"), accBalAvailable(lang, "7", encodings, atm, ifx.getAcctBalAvailableAmt(), ifx, 15)));
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("شماره حساب")){
				temp = convert2Farsi("شماره حساب");
				config.setReceiptDetailsMap("شماره حساب", temp);		
			}
			if(Util.hasText(ifx.getSubsidiaryAccFrom())){
				out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره حساب"), accNumbertConvert2Farsi(accountFormat(textDir, ifx.getSubsidiaryAccFrom()))));
			}
			
			out.write(putLF(8));
			
			
//			st += "[GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))][LF]" +
//					"[GR justify(GR c2F('شماره پیگیری'), GR c2F(ifx.Src_TrnSeqCntr))][LF]" +
//					"[GR justify(GR c2F('مبلغ'), GR amount2F(partialDispense(ifx),15))][LF]" +
//					"[GR justify(GR c2F('مانده حساب'), GR amount2F(ifx.AcctBalLedgerAmt, ifx, 15))][LF]" +
//					"[GR justify(GR c2F('قابل برداشت'), GR amount2F(ifx.AcctBalAvailableAmt, ifx, 15))][LF]" +
//					"[GR justify(GR c2F( GR subsidiaryState2F(ifx.subsidiaryAccFrom, 'شماره حساب')), GR (accountFormat(ifx.subsidiaryAccFrom)))]" +
//					"[GR putLF(8)]";
			
			out.write(generateFooter(atm, config, encodings, lineLength, leftMargin, textDir));
			out.write(ASCIIConstants.FF);
			
//			st += "[ESC](1" +
//					"[GR hr(0xcd)]" +
//					"[ESC](7" +
//					"[GR center(GR GR c2F(GR bnkMount2F()) )][FF]";
		} catch (IOException e) {
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	private byte[] simplePurchaseChargeJournal(Ifx ifx){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			out.write(ASCIIConstants.LF);
			out.write("Charge:".toUpperCase().getBytes()); //Task141 : NCR
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write(ifx.getAuth_Amt().toString().getBytes());			
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("REF:".getBytes());
			out.write(ifx.getSrc_TrnSeqCntr().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("PAN:".getBytes());
			out.write(ifx.getActualAppPAN().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TIME:".getBytes());
			out.write(ifx.getReceivedDt().toString().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TERMINAL:".getBytes());
			out.write(ifx.getTerminalId().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("SERNO:".getBytes());
			out.write(ifx.getChargeData().getCharge().getCardSerialNo().toString().getBytes());
			
//			String st = "[LF]Charge:[SO]1" +
//					"[GR ifx.Auth_Amt][SO]1" +
//					"REF:[GR ifx.Src_TrnSeqCntr][SO]1" +
//					"PAN:[GR ifx.actualAppPAN][SO]1" +
//					"TIME:[GR ifx.receivedDt][SO]1" +
//					"TERMINAL:[GR ifx.TerminalId][SO]1" +
//					"SERNO:[GR ifx.getChargeData().getCharge().getCardSerialNo()]";
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	private byte[] simplePurchaseMTNChargeReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		try {
			byte starChar = (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_STAR_CHAR : NCR_RECEIPT_STAR_CHAR;
			byte sharpChar = (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_SHARP_SIGN_CHAR : NCR_RECEIPT_SHARP_SIGN_CHAR;
			byte[] str140 = (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_140_STR : NCR_RECEIPT_140_STR;
			byte[] yesOK = (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_YESOK_STR : NCR_RECEIPT_YESOK_STR;

			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			String lang = Language.FarsiLanguage.toString();
			byte[] temp;

			out.write(generateHeader(atm, ifx,encodings, config, lineLength, leftMargin, textDir));

			if(config.getReceiptDetailsMap().containsKey("رسید خرید شارژ ایرانسل" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید خرید شارژ ایرانسل"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید خرید شارژ ایرانسل"));
				out.write(temp);
				config.setReceiptDetailsMap("رسید خرید شارژ ایرانسل"+"$center$" + lineLength + "$" + leftMargin, temp);
			}
			out.write(ASCIIConstants.LF);

//			String st = "[ESC](7" +
//					"[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//					"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//					"[GR center(c2F('رسید خرید شارژ ایرانسل')) ][LF]" ;


			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));

//			st += "[ESC](1" +
//			"[GR hr(0xcd)]" +
//			"[ESC](7" +
//			"[GR datePersianFormat(ifx.receivedDt)][LF][LF]" ;

//			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
//			out.write(ASCIIConstants.LF);
//			out.write(ASCIIConstants.LF);
//
//
//			if(!config.getReceiptDetailsMap().containsKey("شماره کارت")){
//				temp = convert2Farsi("شماره کارت");
//				config.setReceiptDetailsMap("شماره کارت", temp);
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
//			out.write(ASCIIConstants.LF);
//
//			if(!config.getReceiptDetailsMap().containsKey("شماره پیگیری")){
//				temp = convert2Farsi("شماره پیگیری");
//				config.setReceiptDetailsMap("شماره پیگیری", temp);
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره پیگیری"), convert2Farsi(ifx.getSrc_TrnSeqCntr())));
//			out.write(ASCIIConstants.LF);

			out.write(generateDateAndAppPanAndTrn_Seq(atm, config, encodings, lineLength, leftMargin, textDir, ifx));

			if(!config.getReceiptDetailsMap().containsKey("مانده حساب")){
				temp = convert2Farsi("مانده حساب");
				config.setReceiptDetailsMap("مانده حساب", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مانده حساب"), accBalAvailable(lang, "7", encodings, atm, ifx.getAcctBalLedgerAmt(), 15)));
			out.write(ASCIIConstants.LF);

			out.write(center(textDir, lineLength, leftMargin, "-----------"));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("مبلغ")){
				temp = convert2Farsi("مبلغ");
				config.setReceiptDetailsMap("مبلغ", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مبلغ"), accBalAvailable(lang, "7", encodings, atm, ifx.getAuth_Amt(), 15)));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("ارزش واقعی برای مکالمه")){
				temp = convert2Farsi("ارزش واقعی برای مکالمه");
				config.setReceiptDetailsMap("ارزش واقعی برای مکالمه", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("ارزش واقعی برای مکالمه"), realChargeCredit(lang, "7", encodings, atm, ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7)));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("شماره رمز")){
				temp = convert2Farsi("شماره رمز");
				config.setReceiptDetailsMap("شماره رمز", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره رمز"), convert2Farsi(decode(ifx.getChargeData().getCharge().getCardPIN()))));
			out.write(ASCIIConstants.LF);

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) 
			    out.write(justify(textDir, lineLength, leftMargin, convertToEnglish(""), convertToEnglish(decode(ifx.getChargeData().getCharge().getCardPIN()))));
			else
				out.write(justify(textDir, lineLength, leftMargin, convertToEnglishForNCR(""), convertToEnglishForNCR(decode(ifx.getChargeData().getCharge().getCardPIN()))));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("شماره سریال شارژ")){
				temp = convert2Farsi("شماره سریال شارژ");
				config.setReceiptDetailsMap("شماره سریال شارژ", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره سریال شارژ"), convert2Farsi(ifx.getChargeData().getCharge().getCardSerialNo())));
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
//			st +="[GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))][LF]" +
//					"[GR justify(GR c2F('شماره پیگیری'), GR c2F(ifx.Src_TrnSeqCntr))][LF]" +
//					"[GR justify(GR c2F('مانده حساب'), GR amount2F(ifx.AcctBalLedgerAmt, 15))][LF]" +
//					"[GR center('-----------')][LF]" +
//					"[GR justify(GR c2F('مبلغ'), GR amount2F(ifx.Auth_Amt, 15))][LF]" +
//					"[GR justify(c2F('ارزش واقعی برای مکالمه'), realChargeCredit2F(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7))][LF]" +
//					"[GR justify(c2F('شماره رمز'), c2F(decode(ifx.getChargeData().getCharge().getCardPIN())))][LF]" +
//					"[GR justify(c2E(''),c2E(decode(ifx.getChargeData().getCharge().getCardPIN())))][LF]" +
//					"[GR justify(c2F('شماره سریال شارژ'), c2F(ifx.getChargeData().getCharge().getCardSerialNo()))][LF][LF]" ;


			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.ENGLISH_ENCODING)).getBytes());
			}

			out.write(starChar);
			out.write(str140);
			out.write(starChar);

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.FARSI_RECIEPT_ENCODING)).getBytes());
			}

			out.write(convert2Farsi("رمز"));

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.ENGLISH_ENCODING)).getBytes());
			}

			out.write(sharpChar);
			out.write(yesOK);

			out.write(ASCIIConstants.SO);
			out.write(0x39);

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.FARSI_RECIEPT_ENCODING)).getBytes());
			}

			out.write(convert2Farsi("نحوه استفاده"));
			out.write(ASCIIConstants.LF);

			out.write(center(textDir, lineLength, leftMargin, convert2Farsi("این شارژ قابل انتقال نیست")));
			out.write(ASCIIConstants.LF);

			out.write(center(textDir, lineLength, leftMargin, convert2Farsi("شماره امداد مشتریان ایرانسل 140")));
			out.write(ASCIIConstants.LF);

			out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));

			out.write(ASCIIConstants.FF);
//			st += "[ESC](1*140*" +
//					"[ESC](7" +
//					"[GR c2F('رمز')]" +
//					"[ESC](1" +
//					"#YES/OK" +
//					"[SO]9:" +
//					"[ESC](7" +
//					"[GR c2F('نحوه استفاده')][LF]" +
//					"[GR center(GR c2F('این شارژ قابل انتقال نیست')) ][LF]" +
//					"[GR center(GR c2F('شماره امداد مشتریان ایرانسل 140')) ][LF]" +
//					"[ESC](1" +
//					"[GR hr(0xcd)]" +
//					"[ESC](7" +
//					"[GR center(GR GR c2F(GR bnkMount2F()) )]" +
//					"[FF]";
		} catch (IOException e) {
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}

	private byte[] simplePurchaseRightelChargeReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		try {
			byte starChar = (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_STAR_CHAR : NCR_RECEIPT_STAR_CHAR;
			byte sharpChar = (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_SHARP_SIGN_CHAR : NCR_RECEIPT_SHARP_SIGN_CHAR;
			byte[] str141 = (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_141_STR : NCR_RECEIPT_141_STR;
			byte[] yesOK = (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_YESOK_STR : NCR_RECEIPT_YESOK_STR;

			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			String lang = Language.FarsiLanguage.toString();
			byte[] temp;

			out.write(generateHeader(atm, ifx,encodings, config, lineLength, leftMargin, textDir));

			if(config.getReceiptDetailsMap().containsKey("رسید خرید شارژ رایتل" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید خرید شارژ رایتل"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید خرید شارژ رایتل"));
				out.write(temp);
				config.setReceiptDetailsMap("رسید خرید شارژ رایتل"+"$center$" + lineLength + "$" + leftMargin, temp);
			}
			out.write(ASCIIConstants.LF);

//			String st = "[ESC](7" +
//					"[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//					"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//					"[GR center(c2F('رسید خرید شارژ رایتل')) ][LF]" ;


			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));

//			st += "[ESC](1" +
//			"[GR hr(0xcd)]" +
//			"[ESC](7" +
//			"[GR datePersianFormat(ifx.receivedDt)][LF][LF]" ;

//			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
//			out.write(ASCIIConstants.LF);
//			out.write(ASCIIConstants.LF);
//
//
//			if(!config.getReceiptDetailsMap().containsKey("شماره کارت")){
//				temp = convert2Farsi("شماره کارت");
//				config.setReceiptDetailsMap("شماره کارت", temp);
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
//			out.write(ASCIIConstants.LF);
//
//			if(!config.getReceiptDetailsMap().containsKey("شماره پیگیری")){
//				temp = convert2Farsi("شماره پیگیری");
//				config.setReceiptDetailsMap("شماره پیگیری", temp);
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره پیگیری"), convert2Farsi(ifx.getSrc_TrnSeqCntr())));
//			out.write(ASCIIConstants.LF);

			out.write(generateDateAndAppPanAndTrn_Seq(atm, config, encodings, lineLength, leftMargin, textDir, ifx));

			if(!config.getReceiptDetailsMap().containsKey("مانده حساب")){
				temp = convert2Farsi("مانده حساب");
				config.setReceiptDetailsMap("مانده حساب", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مانده حساب"), accBalAvailable(lang, "7", encodings, atm, ifx.getAcctBalLedgerAmt(), 15)));
			out.write(ASCIIConstants.LF);

			out.write(center(textDir, lineLength, leftMargin, "-----------"));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("مبلغ")){
				temp = convert2Farsi("مبلغ");
				config.setReceiptDetailsMap("مبلغ", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مبلغ"), accBalAvailable(lang, "7", encodings, atm, ifx.getAuth_Amt(), 15)));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("ارزش واقعی برای مکالمه")){
				temp = convert2Farsi("ارزش واقعی برای مکالمه");
				config.setReceiptDetailsMap("ارزش واقعی برای مکالمه", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("ارزش واقعی برای مکالمه"), realChargeCredit(lang, "7", encodings, atm, ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7)));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("شماره رمز")){
				temp = convert2Farsi("شماره رمز");
				config.setReceiptDetailsMap("شماره رمز", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره رمز"), convert2Farsi(decode(ifx.getChargeData().getCharge().getCardPIN()))));
			out.write(ASCIIConstants.LF);
			
			//TASK Task141 [39391] - NCR
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) 
			    out.write(justify(textDir, lineLength, leftMargin, convertToEnglish(""), convertToEnglish(decode(ifx.getChargeData().getCharge().getCardPIN()))));
			else
				out.write(justify(textDir, lineLength, leftMargin, convertToEnglishForNCR(""), convertToEnglishForNCR(decode(ifx.getChargeData().getCharge().getCardPIN()))));
			//before Task141 out.write(justify(textDir, lineLength, leftMargin, convertToEnglish(""), convertToEnglish(decode(ifx.getChargeData().getCharge().getCardPIN()))));
			
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("شماره سریال شارژ")){
				temp = convert2Farsi("شماره سریال شارژ");
				config.setReceiptDetailsMap("شماره سریال شارژ", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره سریال شارژ"), convert2Farsi(ifx.getChargeData().getCharge().getCardSerialNo())));
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
//			st +="[GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))][LF]" +
//					"[GR justify(GR c2F('شماره پیگیری'), GR c2F(ifx.Src_TrnSeqCntr))][LF]" +
//					"[GR justify(GR c2F('مانده حساب'), GR amount2F(ifx.AcctBalLedgerAmt, 15))][LF]" +
//					"[GR center('-----------')][LF]" +
//					"[GR justify(GR c2F('مبلغ'), GR amount2F(ifx.Auth_Amt, 15))][LF]" +
//					"[GR justify(c2F('ارزش واقعی برای مکالمه'), realChargeCredit2F(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7))][LF]" +
//					"[GR justify(c2F('شماره رمز'), c2F(decode(ifx.getChargeData().getCharge().getCardPIN())))][LF]" +
//					"[GR justify(c2E(''),c2E(decode(ifx.getChargeData().getCharge().getCardPIN())))][LF]" +
//					"[GR justify(c2F('شماره سریال شارژ'), c2F(ifx.getChargeData().getCharge().getCardSerialNo()))][LF][LF]" ;


			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.ENGLISH_ENCODING)).getBytes());
			}

			out.write(starChar);
			out.write(str141);
			out.write(starChar);

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.FARSI_RECIEPT_ENCODING)).getBytes());
			}

			out.write(convert2Farsi("رمز"));

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.ENGLISH_ENCODING)).getBytes());
			}

			out.write(sharpChar);
			out.write(yesOK);

			out.write(ASCIIConstants.SO);
			out.write(0x39);

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.FARSI_RECIEPT_ENCODING)).getBytes());
			}

			out.write(convert2Farsi("نحوه استفاده"));
			out.write(ASCIIConstants.LF);

//			out.write(center(textDir, lineLength, leftMargin, convert2Farsi("این شارژ قابل انتقال نیست")));
//			out.write(ASCIIConstants.LF);

			out.write(center(textDir, lineLength, leftMargin, convert2Farsi("شماره تلفن پشتیبانی رایتل 0212920")));
			out.write(ASCIIConstants.LF);

			out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));

			out.write(ASCIIConstants.FF);
//			st += "[ESC](1*141*" +
//					"[ESC](7" +
//					"[GR c2F('رمز')]" +
//					"[ESC](1" +
//					"#YES/OK" +
//					"[SO]9:" +
//					"[ESC](7" +
//					"[GR c2F('نحوه استفاده')][LF]" +
//					"[GR center(GR c2F('این شارژ قابل انتقال نیست')) ][LF]" +
//					"[GR center(GR c2F('شماره امداد مشتریان رایتل 140')) ][LF]" +
//					"[ESC](1" +
//					"[GR hr(0xcd)]" +
//					"[ESC](7" +
//					"[GR center(GR GR c2F(GR bnkMount2F()) )]" +
//					"[FF]";
		} catch (IOException e) {
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	
	private byte[] simplePurchaseTaliaChargeReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
			byte starChar = (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_STAR_CHAR : NCR_RECEIPT_STAR_CHAR;
			byte sharpChar = (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_SHARP_SIGN_CHAR : NCR_RECEIPT_SHARP_SIGN_CHAR;
			byte[] str140 = (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_140_STR : NCR_RECEIPT_140_STR;
			byte[] yesOK = (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_YESOK_STR : NCR_RECEIPT_YESOK_STR;

			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			String lang = Language.FarsiLanguage.toString();
			byte[] temp;

			out.write(generateHeader(atm, ifx,encodings, config, lineLength, leftMargin, textDir));

			if(config.getReceiptDetailsMap().containsKey("رسید خرید شارژ تاليا" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید خرید شارژ تاليا"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید خرید شارژ تاليا"));
				out.write(temp);
				config.setReceiptDetailsMap("رسید خرید شارژ تاليا"+"$center$" + lineLength + "$" + leftMargin, temp);
			}
			out.write(ASCIIConstants.LF);

//			String st = "[ESC](7" +
//					"[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//					"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//					"[GR center(c2F('رسید خرید شارژ رایتل')) ][LF]" ;


			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));

//			st += "[ESC](1" +
//			"[GR hr(0xcd)]" +
//			"[ESC](7" +
//			"[GR datePersianFormat(ifx.receivedDt)][LF][LF]" ;

//			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
//			out.write(ASCIIConstants.LF);
//			out.write(ASCIIConstants.LF);
//
//
//			if(!config.getReceiptDetailsMap().containsKey("شماره کارت")){
//				temp = convert2Farsi("شماره کارت");
//				config.setReceiptDetailsMap("شماره کارت", temp);
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
//			out.write(ASCIIConstants.LF);
//
//			if(!config.getReceiptDetailsMap().containsKey("شماره پیگیری")){
//				temp = convert2Farsi("شماره پیگیری");
//				config.setReceiptDetailsMap("شماره پیگیری", temp);
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره پیگیری"), convert2Farsi(ifx.getSrc_TrnSeqCntr())));
//			out.write(ASCIIConstants.LF);

			out.write(generateDateAndAppPanAndTrn_Seq(atm, config, encodings, lineLength, leftMargin, textDir, ifx));

			if(!config.getReceiptDetailsMap().containsKey("مانده حساب")){
				temp = convert2Farsi("مانده حساب");
				config.setReceiptDetailsMap("مانده حساب", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مانده حساب"), accBalAvailable(lang, "7", encodings, atm, ifx.getAcctBalLedgerAmt(), 15)));
			out.write(ASCIIConstants.LF);

			out.write(center(textDir, lineLength, leftMargin, "-----------"));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("مبلغ")){
				temp = convert2Farsi("مبلغ");
				config.setReceiptDetailsMap("مبلغ", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مبلغ"), accBalAvailable(lang, "7", encodings, atm, ifx.getAuth_Amt(), 15)));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("ارزش واقعی برای مکالمه")){
				temp = convert2Farsi("ارزش واقعی برای مکالمه");
				config.setReceiptDetailsMap("ارزش واقعی برای مکالمه", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("ارزش واقعی برای مکالمه"), realChargeCredit(lang, "7", encodings, atm, ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7)));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("شماره رمز")){
				temp = convert2Farsi("شماره رمز");
				config.setReceiptDetailsMap("شماره رمز", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره رمز"), convert2Farsi(decode(ifx.getChargeData().getCharge().getCardPIN()))));
			out.write(ASCIIConstants.LF);
			
			//TASK Task141 [39391] - NCR
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) 
			    out.write(justify(textDir, lineLength, leftMargin, convertToEnglish(""), convertToEnglish(decode(ifx.getChargeData().getCharge().getCardPIN()))));
			else
				out.write(justify(textDir, lineLength, leftMargin, convertToEnglishForNCR(""), convertToEnglishForNCR(decode(ifx.getChargeData().getCharge().getCardPIN()))));
			// before Task141 out.write(justify(textDir, lineLength, leftMargin, convertToEnglish(""), convertToEnglish(decode(ifx.getChargeData().getCharge().getCardPIN()))));
			
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("شماره سریال شارژ")){
				temp = convert2Farsi("شماره سریال شارژ");
				config.setReceiptDetailsMap("شماره سریال شارژ", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره سریال شارژ"), convert2Farsi(ifx.getChargeData().getCharge().getCardSerialNo())));
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
//			st +="[GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))][LF]" +
//					"[GR justify(GR c2F('شماره پیگیری'), GR c2F(ifx.Src_TrnSeqCntr))][LF]" +
//					"[GR justify(GR c2F('مانده حساب'), GR amount2F(ifx.AcctBalLedgerAmt, 15))][LF]" +
//					"[GR center('-----------')][LF]" +
//					"[GR justify(GR c2F('مبلغ'), GR amount2F(ifx.Auth_Amt, 15))][LF]" +
//					"[GR justify(c2F('ارزش واقعی برای مکالمه'), realChargeCredit2F(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7))][LF]" +
//					"[GR justify(c2F('شماره رمز'), c2F(decode(ifx.getChargeData().getCharge().getCardPIN())))][LF]" +
//					"[GR justify(c2E(''),c2E(decode(ifx.getChargeData().getCharge().getCardPIN())))][LF]" +
//					"[GR justify(c2F('شماره سریال شارژ'), c2F(ifx.getChargeData().getCharge().getCardSerialNo()))][LF][LF]" ;

			


			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.ENGLISH_ENCODING)).getBytes());
			}

			out.write(starChar);
			out.write(str140);
			out.write(starChar);

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.FARSI_RECIEPT_ENCODING)).getBytes());
			}

			out.write(convert2Farsi("رمز"));

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.ENGLISH_ENCODING)).getBytes());
			}

			out.write(sharpChar);
			out.write(yesOK);

			out.write(ASCIIConstants.SO);
			out.write(0x39);

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.FARSI_RECIEPT_ENCODING)).getBytes());
			}

			out.write(convert2Farsi("نحوه استفاده"));
			out.write(ASCIIConstants.LF);

//			out.write(center(textDir, lineLength, leftMargin, convert2Farsi("این شارژ قابل انتقال نیست")));
//			out.write(ASCIIConstants.LF);

			out.write(center(textDir, lineLength, leftMargin, convert2Farsi("شماره پشتیبانی تاليا 09329990000")));
			out.write(ASCIIConstants.LF);

			out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));

			out.write(ASCIIConstants.FF);
//			st += "[ESC](1*141*" +
//					"[ESC](7" +
//					"[GR c2F('رمز')]" +
//					"[ESC](1" +
//					"#YES/OK" +
//					"[SO]9:" +
//					"[ESC](7" +
//					"[GR c2F('نحوه استفاده')][LF]" +
//					"[GR center(GR c2F('این شارژ قابل انتقال نیست')) ][LF]" +
//					"[GR center(GR c2F('شماره امداد مشتریان رایتل 140')) ][LF]" +
//					"[ESC](1" +
//					"[GR hr(0xcd)]" +
//					"[ESC](7" +
//					"[GR center(GR GR c2F(GR bnkMount2F()) )]" +
//					"[FF]";
		} catch (IOException e) {
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	
//	private byte[] simplePurchaseMCIChargeJournal(Ifx ifx){
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		try{
//			out.write(ASCIIConstants.LF);
//			out.write("Charge:".getBytes());
//			out.write(ASCIIConstants.SO);
//			out.write(0x31);
//			
//			out.write(ifx.getAuth_Amt().toString().getBytes());			
//			out.write(ASCIIConstants.SO);
//			out.write(0x31);
//			
//			out.write("REF:".getBytes());
//			out.write(ifx.getSrc_TrnSeqCntr().getBytes());
//			out.write(ASCIIConstants.SO);
//			out.write(0x31);
//			
//			out.write("PAN:".getBytes());
//			out.write(ifx.getActualAppPAN().getBytes());
//			out.write(ASCIIConstants.SO);
//			out.write(0x31);
//			
//			out.write("TIME:".getBytes());
//			out.write(ifx.getReceivedDt().toString().getBytes());
//			out.write(ASCIIConstants.SO);
//			out.write(0x31);
//			
//			out.write("TERMINAL:".getBytes());
//			out.write(ifx.getTerminalId().getBytes());
//			out.write(ASCIIConstants.SO);
//			out.write(0x31);
//			
//			out.write("SERNO:".getBytes());
//			out.write(ifx.getChargeData().getCharge().getCardSerialNo().toString().getBytes());
//			
//			String st ="[LF]Charge:[SO]1" +
//					"[GR ifx.Auth_Amt][SO]1" +
//					"REF:[GR ifx.Src_TrnSeqCntr][SO]1" +
//					"PAN:[GR ifx.actualAppPAN][SO]1" +
//					"TIME:[GR ifx.receivedDt][SO]1" +
//					"TERMINAL:[GR ifx.TerminalId][SO]1" +
//					"SERNO:[GR ifx.getChargeData().getCharge().getCardSerialNo()]";
//			
//		}catch(IOException e){
//			logger.error(e,e);
//			return new byte[]{0x20};
//		}
//		return out.toByteArray();
//	}
	private byte[] simplePurchaseMCIChargeReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		try {
			byte starChar = (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_STAR_CHAR : NCR_RECEIPT_STAR_CHAR;
			byte sharpChar = (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_SHARP_SIGN_CHAR : NCR_RECEIPT_SHARP_SIGN_CHAR;
			byte[] str140 = (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_140_STR : NCR_RECEIPT_140_STR;
			byte[] yesOK = (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ? NOT_NCR_RECEIPT_YESOK_STR : NCR_RECEIPT_YESOK_STR;

			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			String lang = Language.FarsiLanguage.toString();
			byte[] temp;


			out.write(generateHeader(atm, ifx, encodings, config, lineLength, leftMargin, textDir));

			if(config.getReceiptDetailsMap().containsKey("رسید خرید شارژ همراه اول" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید خرید شارژ همراه اول"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید خرید شارژ همراه اول"));
				out.write(temp);
				config.setReceiptDetailsMap("رسید خرید شارژ همراه اول"+"$center$" + lineLength + "$" + leftMargin, temp);
			}
			out.write(ASCIIConstants.LF);

			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));

//			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
//			out.write(ASCIIConstants.LF);
//			out.write(ASCIIConstants.LF);
//
//			if(!config.getReceiptDetailsMap().containsKey("شماره کارت")){
//				temp = convert2Farsi("شماره کارت");
//				config.setReceiptDetailsMap("شماره کارت", temp);
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
//			out.write(ASCIIConstants.LF);
//
//			if(!config.getReceiptDetailsMap().containsKey("شماره پیگیری")){
//				temp = convert2Farsi("شماره پیگیری");
//				config.setReceiptDetailsMap("شماره پیگیری", temp);
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره پیگیری"), convert2Farsi(ifx.getSrc_TrnSeqCntr())));
//			out.write(ASCIIConstants.LF);

			out.write(generateDateAndAppPanAndTrn_Seq(atm, config, encodings, lineLength, leftMargin, textDir, ifx));

			if(!config.getReceiptDetailsMap().containsKey("مانده حساب")){
				temp = convert2Farsi("مانده حساب");
				config.setReceiptDetailsMap("مانده حساب", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مانده حساب"), accBalAvailable(lang, "7", encodings, atm, ifx.getAcctBalLedgerAmt(), 15)));
			out.write(ASCIIConstants.LF);

			out.write(center(textDir, lineLength, leftMargin, "-----------"));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("مبلغ")){
				temp = convert2Farsi("مبلغ");
				config.setReceiptDetailsMap("مبلغ", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مبلغ"), accBalAvailable(lang, "7", encodings, atm, ifx.getAuth_Amt(), 15)));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("ارزش واقعی برای مکالمه")){
				temp = convert2Farsi("ارزش واقعی برای مکالمه");
				config.setReceiptDetailsMap("ارزش واقعی برای مکالمه", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("ارزش واقعی برای مکالمه"), realChargeCredit(lang, "7", encodings, atm, ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7)));
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("شماره رمز")){
				temp = convert2Farsi("شماره رمز");
				config.setReceiptDetailsMap("شماره رمز", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره رمز"), convert2Farsi(decode(ifx.getChargeData().getCharge().getCardPIN()))));
			out.write(ASCIIConstants.LF);

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) 
			    out.write(justify(textDir, lineLength, leftMargin, convertToEnglish(""), convertToEnglish(decode(ifx.getChargeData().getCharge().getCardPIN()))));
			else
				out.write(justify(textDir, lineLength, leftMargin, convertToEnglishForNCR(""), convertToEnglishForNCR(decode(ifx.getChargeData().getCharge().getCardPIN()))));
			
			out.write(ASCIIConstants.LF);

			if(!config.getReceiptDetailsMap().containsKey("شماره سریال شارژ")){
				temp = convert2Farsi("شماره سریال شارژ");
				config.setReceiptDetailsMap("شماره سریال شارژ", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره سریال شارژ"), convert2Farsi(ifx.getChargeData().getCharge().getCardSerialNo())));
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.ENGLISH_ENCODING)).getBytes());
			}

			out.write(starChar);
			out.write(str140);
			out.write(starChar);
			out.write(sharpChar);

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.FARSI_RECIEPT_ENCODING)).getBytes());
			}

			out.write(convert2Farsi("رمز"));

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.ENGLISH_ENCODING)).getBytes());
			}

			out.write(sharpChar);
			out.write(yesOK);

			out.write(ASCIIConstants.SO);
			out.write(0x38);

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.FARSI_RECIEPT_ENCODING)).getBytes());
			}

			out.write(convert2Farsi("نحوه استفاده"));
			out.write(ASCIIConstants.LF);

			out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));

			out.write(ASCIIConstants.FF);

//			String st = "[ESC](7[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//					"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//					"[GR center(c2F('رسید خرید شارژ همراه اول')) ][LF]" +
//					"[ESC](1[GR hr(0xcd)][ESC](7" +
//					"[GR datePersianFormat(ifx.receivedDt)][LF][LF]" +
//					"[GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))][LF]" +
//					"[GR justify(GR c2F('شماره پیگیری'), GR c2F(ifx.Src_TrnSeqCntr))][LF]" +
//					"[GR justify(GR c2F('مانده حساب'), GR amount2F(ifx.AcctBalLedgerAmt, 15))][LF]" +
//					"[GR center('-----------')][LF]" +
//					"[GR justify(GR c2F('مبلغ'), GR amount2F(ifx.Auth_Amt, 15))][LF]" +
//					"[GR justify(c2F('ارزش واقعی برای مکالمه'), realChargeCredit2F(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7))][LF]" +
//					"[GR justify(c2F('شماره رمز'), c2F(decode(ifx.getChargeData().getCharge().getCardPIN())))][LF]" +
//					"[GR justify(c2E(''),c2E(decode(ifx.getChargeData().getCharge().getCardPIN())))][LF]" +
//					"[GR justify(c2F('شماره سریال شارژ'), c2F(ifx.getChargeData().getCharge().getCardSerialNo()))][LF][LF]" +
//					"[ESC](1" +
//					"*140*#" +
//					"[ESC](7" +
//					"[GR c2F('رمز')]" +
//					"[ESC](1#YES/OK" +
//					"[SO]8:[ESC](7[GR c2F('نحوه استفاده')][LF]" +
//					"[ESC](1[GR hr(0xcd)][ESC](7[GR center(GR GR c2F(GR bnkMount2F()) )][FF]";
		}catch(IOException e){
			logger.debug(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	
	private byte[] simpleOnlineBillPaymentJournal (Ifx ifx){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			out.write(ASCIIConstants.LF);
			out.write("TicketPurchase:".toUpperCase().getBytes()); //Task141 : NCR
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write(ifx.getAuth_Amt().toString().getBytes());			
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("REF:".getBytes());
			out.write(ifx.getSrc_TrnSeqCntr().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("PAN:".getBytes());
			out.write(ifx.getActualAppPAN().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TIME:".getBytes());
			out.write(ifx.getReceivedDt().toString().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("TERMINAL:".getBytes());
			out.write(ifx.getTerminalId().getBytes());
			out.write(ASCIIConstants.SO);
			out.write(0x31);
			
			out.write("OnlineRefNum:".getBytes());
			out.write(ifx.getOnlineBillPaymentRefNum().getBytes());
			
//			String st = "[LF]Ticket Purchase:[SO]1" +
//					"[GR ifx.Auth_Amt][SO]1" +
//					"REF:[GR ifx.Src_TrnSeqCntr][SO]1" +
//					"PAN:[GR ifx.actualAppPAN][SO]1" +
//					"TIME:[GR ifx.receivedDt][SO]1" +
//					"TERMINAL:[GR ifx.TerminalId][SO]1" +
//					"OnlineRefNum:[ifx.OnlineBillPaymentRefNum]";
		}catch(IOException e){
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
		
	}
	private byte[] simpleOnlineBillPaymentReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		try {
			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			String lang = Language.FarsiLanguage.toString();
			byte[] temp;

			out.write(generateHeader(atm, ifx, encodings, config, lineLength, leftMargin, textDir));
			
			if(config.getReceiptDetailsMap().containsKey("رسید پرداخت قبض نوپاد" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید پرداخت قبض نوپاد"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید پرداخت قبض نوپاد"));
				out.write(temp);
				config.setReceiptDetailsMap("رسید پرداخت قبض نوپاد"+"$center$" + lineLength + "$" + leftMargin, temp);		
			}
			out.write(ASCIIConstants.LF);
			
			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));
			
//			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
//			out.write(ASCIIConstants.LF);
//			out.write(ASCIIConstants.LF);
//			
//			if(!config.getReceiptDetailsMap().containsKey("شماره کارت")){
//				temp = convert2Farsi("شماره کارت");
//				config.setReceiptDetailsMap("شماره کارت", temp);		
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
//			out.write(ASCIIConstants.LF);
//			
//			if(!config.getReceiptDetailsMap().containsKey("شماره پیگیری")){
//				temp = convert2Farsi("شماره پیگیری");
//				config.setReceiptDetailsMap("شماره پیگیری", temp);		
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره پیگیری"), convert2Farsi(ifx.getSrc_TrnSeqCntr())));
//			out.write(ASCIIConstants.LF);
			
			out.write(generateDateAndAppPanAndTrn_Seq(atm, config, encodings, lineLength, leftMargin, textDir, ifx));
			
			if(!config.getReceiptDetailsMap().containsKey("توضیحات")){
				temp = convert2Farsi("توضیحات");
				config.setReceiptDetailsMap("توضیحات", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("توضیحات"), convert2Farsi(ifx.getOnlineBillPaymentDescription())));
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("شماره قبض نوپاد")){
				temp = convert2Farsi("شماره قبض نوپاد");
				config.setReceiptDetailsMap("شماره قبض نوپاد", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره قبض نوپاد"), convert2Farsi(ifx.getOnlineBillPaymentRefNum())));
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("مبلغ")){
				temp = convert2Farsi("مبلغ");
				config.setReceiptDetailsMap("مبلغ", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مبلغ"), accBalAvailable(lang, "7", encodings, atm, ifx.getAuth_Amt(), 15)));
			
			out.write(putLF(8));
			
			out.write(generateFooter(atm, config, encodings, lineLength, leftMargin, textDir));
			
			out.write(ASCIIConstants.FF);
			
//			String st = "[ESC](7[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//					"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//					"[GR center(GR c2F('رسید پرداخت قبض نوپاد'))][LF]" +
//					"[ESC](1[GR hr(0xcd)][ESC](7" +
//					"[GR datePersianFormat(ifx.receivedDt)][LF][LF]" +
//					"[GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))][LF]" +
//					"[GR justify(GR c2F('شماره پیگیری'), GR c2F(ifx.Src_TrnSeqCntr))][LF]" +
//					"[GR justify(GR c2F('توضیحات'), GR c2F(ifx.OnlineBillPaymentDescription))][LF]" +
//					"[GR justify(GR c2F('شماره قبض نوپاد'), GR c2F(ifx.OnlineBillPaymentRefNum))][LF]" +
//					"[GR justify(GR c2F('مبلغ'), GR amount2F(ifx.Auth_Amt, 15))]" +
//					"[GR putLF(8)]" +
//					"[ESC](1[GR hr(0xcd)][ESC](7[GR center(GR GR c2F(GR bnkMount2F()) )]" +
//					"[FF]";
		}catch(IOException e ){
			logger.debug(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();	
	}
	private byte[] simpleThirdPartyPaymentReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String>encodings){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		byte[] temp;
		try{
			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			String lang = Language.FarsiLanguage.toString();
			
			
			out.write(generateHeader(atm, ifx, encodings, config, lineLength, leftMargin, textDir));
			
			if(config.getReceiptDetailsMap().containsKey("رسید پرداخت" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید پرداخت"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید پرداخت"));
				out.write(temp);
				config.setReceiptDetailsMap("رسید پرداخت"+"$center$" + lineLength + "$" + leftMargin, temp);		
			}
			
			out.write(ASCIIConstants.LF);
			
//			String st = "[ESC](7[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//					"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//					"[GR center(GR c2F('رسید پرداخت'))][LF]" ;
			
			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));
			
			out.write(generateDateAndAppPanAndTrn_Seq(atm, config, encodings, lineLength, leftMargin, textDir, ifx));
			
			
			if(!config.getReceiptDetailsMap().containsKey("شماره حساب")){
				temp = convert2Farsi("شماره حساب");
				config.setReceiptDetailsMap("شماره حساب", temp);		
			}
			if(Util.hasText(ifx.getSubsidiaryAccFrom())){
				out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره حساب"), accNumbertConvert2Farsi(accountFormat(textDir, ifx.getSubsidiaryAccFrom()))));
			}
			
			if(!config.getReceiptDetailsMap().containsKey("مبلغ")){
				temp = convert2Farsi("مبلغ");
				config.setReceiptDetailsMap("مبلغ", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مبلغ"), accBalAvailable(lang, "7", encodings, atm, ifx.getAuth_Amt(), 15)));
			
//			if(!config.getReceiptDetailsMap().containsKey("از کارت شماره")){
//				temp = convert2Farsi("از کارت شماره");
//				config.setReceiptDetailsMap("از کارت شماره", temp);
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("از کارت شماره"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
//			out.write(ASCIIConstants.LF);
			
//			if(!config.getReceiptDetailsMap().containsKey("به موسسه")){
//				temp = convert2Farsi("به موسسه");
//				config.setReceiptDetailsMap("به موسسه", temp);
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("به موسسه"), appPanFormatFa(encodings, atm, ifx.getActualSecondAppPan())));
//			out.write(ASCIIConstants.LF);
			
			out.write(alignTextRight(textDir, lineLength, leftMargin, convert2Farsi("به موسسه "+ifx.getThirdPartyName()+ " "+ "پرداخت شد")));
			
			out.write(putLF(10));
			
			out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));
			
			out.write(ASCIIConstants.FF);
//			st +="[ESC](7[GR center(GR GR c2F(GR bnkMount2F()) )][FF]";
		}catch(IOException e) {
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	
	//Mirkamali(Task175): Restriction
	private byte[] simpleRestrictionReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String>encodings){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		byte[] temp;
		try{
			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			String lang = Language.FarsiLanguage.toString();
			
			
			out.write(generateHeader(atm, ifx, encodings, config, lineLength, leftMargin, textDir));
			
			if(config.getReceiptDetailsMap().containsKey("رسید اعمال محدودیت" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید اعمال محدودیت"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید اعمال محدودیت"));
				out.write(temp);
				config.setReceiptDetailsMap("رسید اعمال محدودیت"+"$center$" + lineLength + "$" + leftMargin, temp);		
			}
			
			out.write(ASCIIConstants.LF);
			
//			String st = "[ESC](7[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//					"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//					"[GR center(GR c2F('رسید پرداخت'))][LF]" ;
			
			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));
			
			out.write(generateDateAndAppPanAndTrn_Seq(atm, config, encodings, lineLength, leftMargin, textDir, ifx));
			
			
			if(!config.getReceiptDetailsMap().containsKey("تراکنش")){
				temp = convert2Farsi("تراکنش");
				config.setReceiptDetailsMap("تراکنش", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("تراکنش"), convert2Farsi(RestrictionOnTrxAndTermType.getFarsiName(ifx.getBufferB()))/*.getBytes()*/));
			
			/*if(!config.getReceiptDetailsMap().containsKey("ترمینال")){
				temp = convert2Farsi("ترمینال");
				config.setReceiptDetailsMap("ترمینال", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("ترمینال"), " "));
			*/
			if(!config.getReceiptDetailsMap().containsKey("نوع محدودیت")){
				temp = convert2Farsi("نوع محدودیت");
				config.setReceiptDetailsMap("نوع محدودیت", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("نوع محدودیت"),convert2Farsi( new CycleType().getFarsiName(ifx.getBufferC()))/*.getBytes()*/));
			
			if(!config.getReceiptDetailsMap().containsKey("مبلغ")){
				temp = convert2Farsi("مبلغ");
				config.setReceiptDetailsMap("مبلغ", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مبلغ"), accBalAvailable(lang, "7", encodings, atm, ifx.getAuth_Amt(), 15)));
			
			out.write(putLF(10));
			
			out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));
			
			out.write(ASCIIConstants.FF);
//			st +="[ESC](7[GR center(GR GR c2F(GR bnkMount2F()) )][FF]";
		}catch(IOException e) {
			logger.error(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	private byte[] simpleOnlineBillPaymentTrackingReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
		try {
			Integer lineLength = config.getReceiptLineLength();
			Integer leftMargin = config.getReceiptLeftMargin();
			TextDirection textDir = TextDirection.RightToLeft;
			String lang = Language.FarsiLanguage.toString();
			byte[] temp;

			out.write(generateHeader(atm, ifx, encodings, config, lineLength, leftMargin, textDir));
			
			if(config.getReceiptDetailsMap().containsKey("رسید پیگیری پرداخت قبض نوپاد" + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("رسید پیگیری پرداخت قبض نوپاد"+ "$center$" + lineLength + "$" + leftMargin));
			else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید پیگیری پرداخت قبض نوپاد"));
				out.write(temp);
				config.setReceiptDetailsMap("رسید پیگیری پرداخت قبض نوپاد"+"$center$" + lineLength + "$" + leftMargin, temp);		
			}
			out.write(ASCIIConstants.LF);
			
			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));
			
//			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
//			out.write(ASCIIConstants.LF);
//			out.write(ASCIIConstants.LF);
//			
//			if(!config.getReceiptDetailsMap().containsKey("شماره کارت")){
//				temp = convert2Farsi("شماره کارت");
//				config.setReceiptDetailsMap("شماره کارت", temp);		
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
//			out.write(ASCIIConstants.LF);
//			
//			if(!config.getReceiptDetailsMap().containsKey("شماره پیگیری")){
//				temp = convert2Farsi("شماره پیگیری");
//				config.setReceiptDetailsMap("شماره پیگیری", temp);		
//			}
//			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره پیگیری"), convert2Farsi(ifx.getSrc_TrnSeqCntr())));
//			out.write(ASCIIConstants.LF);
			out.write(generateDateAndAppPanAndTrn_Seq(atm, config, encodings, lineLength, leftMargin, textDir, ifx));
			
			if(!config.getReceiptDetailsMap().containsKey("توضیحات")){
				temp = convert2Farsi("توضیحات");
				config.setReceiptDetailsMap("توضیحات", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("توضیحات"), convert2Farsi(ifx.getOnlineBillPaymentDescription())));
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("شماره قبض نوپاد")){
				temp = convert2Farsi("شماره قبض نوپاد");
				config.setReceiptDetailsMap("شماره قبض نوپاد", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره قبض نوپاد"), convert2Farsi(ifx.getOnlineBillPaymentRefNum())));
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("وضعیت")){
				temp = convert2Farsi("وضعیت");
				config.setReceiptDetailsMap("وضعیت", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("وضعیت"), convert2Farsi(ifx.getOnlineBillPaymentData().getOnlineBillPayment().getPaymentStatus())));
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("مبلغ")){
				temp = convert2Farsi("مبلغ");
				config.setReceiptDetailsMap("مبلغ", temp);
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مبلغ"), accBalAvailable(lang, "7", encodings, atm, ifx.getAuth_Amt(), 15)));
			
			out.write(putLF(8));
			
			out.write(generateFooter(atm, config, encodings, lineLength, leftMargin, textDir));
			
			out.write(ASCIIConstants.FF);
			
//			String st= "[ESC](7[GR center(GR c2F(GR bnkName2F())) ][LF][LF]" +
//					"[GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))][LF][LF]" +
//					"[GR center(GR c2F('رسید پیگیری پرداخت قبض نوپاد'))][LF]" +
//					"[ESC](1[GR hr(0xcd)][ESC](7" +
//					"[GR datePersianFormat(ifx.receivedDt)][LF][LF]" +
//					"[GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))][LF]" +
//					"[GR justify(GR c2F('شماره پیگیری'), GR c2F(ifx.Src_TrnSeqCntr))][LF]" +
//					"[GR justify(GR c2F('توضیحات'), GR c2F(ifx.OnlineBillPaymentDescription))][LF]" +
//					"[GR justify(GR c2F('شماره قبض نوپاد'), GR c2F(ifx.OnlineBillPaymentRefNum))][LF]" +
//					"[GR justify(GR c2F('وضعیت'), GR c2F(ifx.onlineBillPaymentData.onlineBillPayment.paymentStatus))][LF]" +
//					"[GR justify(GR c2F('مبلغ'), GR amount2F(ifx.Auth_Amt, 15))]" +
//					"[GR putLF(8)]" +
//					"[ESC](1[GR hr(0xcd)][ESC](7[GR center(GR GR c2F(GR bnkMount2F()) )][FF]";
		}catch(IOException e){
			logger.debug(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	private byte[] generateHorizontalLine(Map<String, String> encodings, Integer lineLength, Integer leftMargin, ATMTerminal atm){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.ENGLISH_ENCODING)).getBytes());
			}

			out.write(horizontalLine(lineLength, leftMargin, atm , 0xcd));

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.FARSI_RECIEPT_ENCODING)).getBytes());
			}

		}catch(IOException e){
			logger.debug(e,e);
			return new byte[]{0x20};

		}
		return out.toByteArray();
	}
	
	private byte[] generateHeader(ATMTerminal atm,Ifx ifx,Map<String, String> encodings,ATMConfiguration config, Integer lineLength, Integer leftMargin, TextDirection textDir){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] temp;
		try{

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.FARSI_RECIEPT_ENCODING)).getBytes());
			}
			
			if(config.getReceiptDetailsMap().containsKey(config.getBnkFarsiName() + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get(config.getBnkFarsiName() + "$center$" + lineLength + "$" + leftMargin));
			else {
				temp = center(textDir, lineLength, leftMargin, convert2Farsi(bnkFarsiName(config.getBnkFarsiName())));
				out.write(temp);
				config.setReceiptDetailsMap(config.getBnkFarsiName() + "$center$" + lineLength + "$" + leftMargin, temp);
			}
				
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			
			if(config.getReceiptDetailsMap().containsKey(ifx.getTerminalId() + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get(ifx.getTerminalId() + "$center$" + lineLength + "$" + leftMargin));
			else {
				temp = center(textDir, lineLength, leftMargin, convert2Farsi("خودپرداز "+atm.getOwner().getName()+" "+ifx.getTerminalId()));
				out.write(temp);
				config.setReceiptDetailsMap(ifx.getTerminalId() + "$center$" + lineLength + "$" + leftMargin, temp);
			}
			
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			
		}catch(IOException e){
			logger.debug(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	
	//TASK Task057 : Topup
	//TASK Task081 : ATM Saham feautre
	private byte[] generateHeaderEn(ATMTerminal atm,Ifx ifx,Map<String, String> encodings,ATMConfiguration config, Integer lineLength, Integer leftMargin, TextDirection textDir){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] temp;
		try{
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.ENGLISH_ENCODING)).getBytes());
			}
			
			if(config.getReceiptDetailsMap().containsKey(config.getBnkEnglishName() + "$center$" + lineLength + "$" + leftMargin))  
				out.write(config.getReceiptDetailsMap().get(config.getBnkEnglishName() + "$center$" + lineLength + "$" + leftMargin));  
			else {
				if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
					temp = center(textDir, lineLength, leftMargin, bnkEnglishName(config.getBnkEnglishName()));
				} else {
					temp = center(textDir, lineLength, leftMargin, convertToEnglishForNCR(bnkEnglishName(config.getBnkEnglishName())));
				}
				out.write(temp);
				config.setReceiptDetailsMap(config.getBnkEnglishName() + "$center$" + lineLength + "$" + leftMargin, temp);
			}
				
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			
			if(config.getReceiptDetailsMap().containsKey("En"+ifx.getTerminalId() + "$center$" + lineLength + "$" + leftMargin))
				out.write(config.getReceiptDetailsMap().get("En"+ifx.getTerminalId() + "$center$" + lineLength + "$" + leftMargin));
			else {
				if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
					temp = center(textDir, lineLength, leftMargin, "ATM"+atm.getOwner().getNameEn()+" "+ifx.getTerminalId());
				}
				else {
					temp = center(textDir, lineLength, leftMargin, convertToEnglishForNCR("ATM"+atm.getOwner().getNameEn()+" "+ifx.getTerminalId()));
				}
				out.write(temp);
				config.setReceiptDetailsMap("En"+ifx.getTerminalId() + "$center$" + lineLength + "$" + leftMargin, temp);
			}
			
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			
		}catch(IOException e){
			logger.debug(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	
	private byte[] generateFooter(ATMTerminal atm,ATMConfiguration config,Map<String, String> encodings, Integer lineLength, Integer leftMargin, TextDirection textDir){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] temp;
		try{
			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));
			
			if(config.getReceiptDetailsMap().containsKey(config.getBnkFarsiName()+"$"+config.getBnkFarsiMount() + "$center$" + lineLength + "$" + leftMargin)){
				out.write(config.getReceiptDetailsMap().get(config.getBnkFarsiName()+"$"+config.getBnkFarsiMount() + "$center$" + lineLength + "$" + leftMargin));
			}else{
				temp = justify(textDir, lineLength, leftMargin, convert2Farsi(config.getBnkFarsiName()), convert2Farsi(config.getBnkFarsiMount()));
				out.write(temp);
				config.setReceiptDetailsMap(config.getBnkFarsiName()+"$"+config.getBnkFarsiMount() + "$center$" + lineLength + "$" + leftMargin, temp);
			}
		}catch(IOException e){
			logger.debug(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	
	//TASK Task081 : ATM Saham Feature
	private byte[] generateFooterEn(ATMTerminal atm,ATMConfiguration config,Map<String, String> encodings, Integer lineLength, Integer leftMargin, TextDirection textDir){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] temp;
		try{
			
			out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));

			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
			out.write(ASCIIConstants.ESC);
			out.write(("("+encodings.get(NDCConvertor.ENGLISH_ENCODING)).getBytes());
		}
			
			if(config.getReceiptDetailsMap().containsKey(config.getBnkEnglishName()+"$"+config.getBnkEnglishMount() + "$center$" + lineLength + "$" + leftMargin)){
				out.write(config.getReceiptDetailsMap().get(config.getBnkEnglishName()+"$"+config.getBnkEnglishMount() + "$center$" + lineLength + "$" + leftMargin));
			}else{
				if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
					temp = justify(textDir, lineLength, leftMargin, config.getBnkEnglishName(), config.getBnkEnglishMount());
				}
				else{
					temp = justify(textDir, lineLength, leftMargin, convertToEnglishForNCR(config.getBnkEnglishName()), convertToEnglishForNCR(config.getBnkEnglishMount()));
				}
				out.write(temp);
				config.setReceiptDetailsMap(config.getBnkEnglishName()+"$"+config.getBnkEnglishMount() + "$center$" + lineLength + "$" + leftMargin, temp);
			}
		}catch(IOException e){
			logger.debug(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}	
	
	//TASK Task040 : DailyMessage
	private byte[] generateDailyMessage(ATMTerminal atm,ATMConfiguration config,Map<String, String> encodings, Integer lineLength, Integer leftMargin, TextDirection textDir){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] temp;
		try{
			if(config.getReceiptDetailsMap().containsKey(atm.getSafeDailyMessage() + "$center$" + lineLength + "$" + leftMargin)){
				out.write(config.getReceiptDetailsMap().get(atm.getSafeDailyMessage() + "$center$" + lineLength + "$" + leftMargin));
			}else{
				temp = center(textDir, lineLength, leftMargin, convert2Farsi(atm.getSafeDailyMessage()));
				out.write(temp);
				config.setReceiptDetailsMap(atm.getSafeDailyMessage() + "$center$" + lineLength + "$" + leftMargin, temp);
			}
		}catch(IOException e){
			logger.debug(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}	
	
	
	
	private byte[] generateDateAndAppPanAndTrn_Seq(ATMTerminal atm, ATMConfiguration config,Map<String, String> encodings, Integer lineLength, Integer leftMargin, TextDirection textDir,Ifx ifx){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] temp;
		try{
			out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("شماره کارت")){
				temp = convert2Farsi("شماره کارت");
				config.setReceiptDetailsMap("شماره کارت", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("شماره پیگیری")){
				temp = convert2Farsi("شماره پیگیری");
				config.setReceiptDetailsMap("شماره پیگیری", temp);		
			}
			out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره پیگیری"), convert2Farsi(ifx.getSrc_TrnSeqCntr())));
			out.write(ASCIIConstants.LF);
		}catch(IOException e){
			logger.debug(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}
	
	//TASK Task057 : Topup
	//TASK Task081 : ATM Saham Feature
	private byte[] generateDateAndAppPanAndTrn_SeqEn(ATMTerminal atm, ATMConfiguration config,Map<String, String> encodings, Integer lineLength, Integer leftMargin, TextDirection textDir,Ifx ifx){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] temp;
		try{
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(ASCIIConstants.ESC);
				out.write(("("+encodings.get(NDCConvertor.ENGLISH_ENCODING)).getBytes());
			}
			
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(dateEnglishFormat(lineLength, leftMargin,  ifx.getReceivedDt()));
			} else {
				out.write(dateEnglishNCRFormat(lineLength, leftMargin,  ifx.getReceivedDt()));
			}
			out.write(ASCIIConstants.LF);
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("Card No.")){
				if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
					temp = "Card No.".getBytes();
				}
				else {
					temp = convertToEnglishForNCR("Card No.");
				}
				config.setReceiptDetailsMap("Card No.", temp);		
			}
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(justify(textDir, lineLength, leftMargin,appPanFormatEn(ifx.getActualAppPAN()),  config.getReceiptDetailsMap().get("Card No.")));
			} else {
				out.write(justify(textDir, lineLength, leftMargin, appPanFormatEnForNCR(ifx.getActualAppPAN()),config.getReceiptDetailsMap().get("Card No.")));
			}
			out.write(ASCIIConstants.LF);
			
			if(!config.getReceiptDetailsMap().containsKey("Reference No.")){
				if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
					temp = "Reference No.".getBytes();
				}
				else {
					temp = convertToEnglishForNCR("Reference No.");
				}
				config.setReceiptDetailsMap("Reference No.", temp);		
			}
			if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
				out.write(justify(textDir, lineLength, leftMargin, ifx.getSrc_TrnSeqCntr(), config.getReceiptDetailsMap().get("Reference No.")));
			} else {
				out.write(justify(textDir, lineLength, leftMargin, convertToEnglishForNCR(ifx.getSrc_TrnSeqCntr()), config.getReceiptDetailsMap().get("Reference No.")));
			}
			out.write(ASCIIConstants.LF);
		}catch(IOException e){
			logger.debug(e,e);
			return new byte[]{0x20};
		}
		return out.toByteArray();
	}	
	
	
	//TASK Task001 : Add Sheba
	//TASK Task141 : NCR
	public byte[] sheba2Farsi(ATMTerminal atm,String shebaCode)
	{
		if (!Util.hasText(shebaCode))
			return new byte[]{};		
		String prefix = shebaCode.substring(0,2).toUpperCase();
		String postfix = shebaCode.substring(2,shebaCode.length()).toUpperCase();
		byte[] prefixBytes = null;
		
		//TASK Task141 [39391] - NCR
		if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) 
			prefixBytes = convertToEnglish(prefix);
		else
			prefixBytes = convertToEnglishForNCR(prefix);
		
		//before Task141 byte[] prefixBytes = convertToEnglish(prefix);
		
		byte[] postfixBytes = convert2Farsi(postfix);
		byte[] result = new byte[prefixBytes.length+postfix.length()];
		System.arraycopy(prefixBytes, 0, result, 0, prefixBytes.length);
		System.arraycopy(postfixBytes, 0, result, prefixBytes.length, postfixBytes.length);
		return result;
	} 
	
		/*** Transfer Card To Account ***/
	public byte[] accountScreenFormatFa(String appPan, String startPos) {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		String row = startPos.substring(0, 1);

		char[] cols = { 'D', 'I', 'O' };
		// char[] cols = {'G','K','0'};
		try {
			if (Util.hasText(appPan)) {
				String[] appPanParts = appPan.split("[.]");
				for (int appPanPartsIndex = 0; appPanPartsIndex < appPanParts.length; appPanPartsIndex++) { //AldComment Task002 :  && appPanPartsIndex <= cols.length check shavad
					result.write(ASCIIConstants.SI);
					result.write((row + cols[appPanPartsIndex]).getBytes());
					result.write(appPanParts[appPanPartsIndex].getBytes());
				}
			} else {
				result.write("".getBytes());

			}
		} catch (IOException e) {
			logger.error("Exception in writting screenFa..." + e, e);
		}
		return result.toByteArray();
	}
		
		//TASK Task002 : Transfer Card To Account
	public byte[] accountScreenFormatEn(String appPan, String startPos) {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		String row = startPos.substring(0, 1);

		char[] cols = { 'D', 'I', 'O' };
		// char[] cols = {'G','K','0'};
		try {
			if (Util.hasText(appPan)) {
				String[] appPanParts = appPan.split("[.]");
				for (int appPanPartsIndex = 0; appPanPartsIndex < appPanParts.length; appPanPartsIndex++) { // AldComment Task002: && appPanPartsIndex <= cols.length check shavad
					result.write(ASCIIConstants.SI);
					result.write((row + cols[appPanPartsIndex]).getBytes());
					result.write(appPanParts[appPanPartsIndex].getBytes());
				}
			} else {
				result.write("".getBytes());
			}
		} catch (IOException e) {
			logger.error("Exception in writting screenEn..." + e, e);
		}
		return result.toByteArray();

	}
		
		//TASK Task002 : Transfer Card To Account
		//AldComment Task002 : 92.04.05
		public byte[] accountFormatEnForNCR(String appPan) {
			if (Util.hasText(appPan) && appPan.contains(".")){
				ByteArrayOutputStream result = new ByteArrayOutputStream();
				String[] appPanParts = appPan.split("[.]");
				try
				{
					for (int i= 0;i<appPanParts.length;i++)
					{
						if (i != 2)
						{
							for (char ch : appPanParts[i].toCharArray() )
								result.write(new byte[]{NCR_RECEIPT_STAR_CHAR});
						}
						else {
								result.write(appPanParts[i].getBytes());
						}
						
						if (i < appPanParts.length-1 ){
								result.write(new byte[]{NCR_RECEIPT_DOT_CHAR});

						}
					}
				} catch (IOException e) {
					logger.error("Exception in writting accountEnNCR..." + e, e);
				}
				
				return result.toByteArray();
			}
			else
			{
				byte[] starsTr = new byte[] {NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,
											NCR_RECEIPT_DOT_CHAR,
											NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,
											NCR_RECEIPT_DOT_CHAR,
											NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,
											NCR_RECEIPT_DOT_CHAR,
											NCR_RECEIPT_STAR_CHAR
											};
				return starsTr;
			}	
					
		}	
		
		
		//TASK Task002 : Transfer Card To Account
		public String accountFormatEn(String appPan) {
			if (Util.hasText(appPan) && appPan.contains(".")){
				StringBuilder result  = new StringBuilder();
				String[] appPanParts = appPan.split("[.]");
				for (int i= 0;i<appPanParts.length;i++)
				{
					if (i != 2)
						result.append(StringFormat.formatNew(appPanParts[i].length(), StringFormat.JUST_LEFT, "*",'*'));
					else
						result.append(appPanParts[i]);
					
					if (i < appPanParts.length-1 )
						result.append('.');
				}
				return result.toString();
			}
			else
			{
				return "***.****.********.*";//AldTODO Taks002 : Correct this !!!
			}	
		}
		
		//TASK Task002 : Transfer Card To Account
		public byte[] accountFormatFa(Map<String, String> encodings, ATMTerminal atm, String appPan) {
			ByteArrayOutputStream result = new ByteArrayOutputStream();			
			if (Util.hasText(appPan) && appPan.contains(".")){
				String[] appPanParts = appPan.split("[.]");
				try {
					if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
						result.write(ASCIIConstants.ESC);
						result.write(("("+encodings.get(NDCConvertor.ENGLISH_ENCODING)).getBytes());
						result.write((StringFormat.formatNew(appPanParts[0].length(), StringFormat.JUST_LEFT, "*",'*')+".").getBytes());
						if (appPanParts.length >= 2)
							result.write((StringFormat.formatNew(appPanParts[1].length(), StringFormat.JUST_LEFT, "*",'*')+".").getBytes());
						result.write(ASCIIConstants.ESC);
						result.write(("("+encodings.get(NDCConvertor.FARSI_RECIEPT_ENCODING)).getBytes());
					}
					else
					{
						//AldTODO Critical
						for (char ch : appPanParts[0].toCharArray())
							result.write(new byte[] {NCR_RECEIPT_STAR_CHAR});
						result.write(new byte[] {NCR_RECEIPT_DOT_CHAR});
						if (appPanParts.length >= 2){
							for (char ch : appPanParts[1].toCharArray())
								result.write(new byte[] {NCR_RECEIPT_STAR_CHAR});
							result.write(new byte[] {NCR_RECEIPT_DOT_CHAR});
						}
					}
					if (appPanParts.length >= 3)
						result.write(convert2Farsi(appPanParts[2].toUpperCase()));
					
					
					if (appPanParts.length >= 4){
						if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
							result.write(ASCIIConstants.ESC);
							result.write(("("+encodings.get(NDCConvertor.ENGLISH_ENCODING)).getBytes());
							result.write(("."+StringFormat.formatNew(appPanParts[3].length(), StringFormat.JUST_LEFT, "*",'*')).getBytes());
							result.write(ASCIIConstants.ESC);
							result.write(("("+encodings.get(NDCConvertor.FARSI_RECIEPT_ENCODING)).getBytes());
						}
						else
						{
							result.write(new byte[] {NCR_RECEIPT_DOT_CHAR});
							for (char ch : appPanParts[3].toCharArray())
								result.write(new byte[] {NCR_RECEIPT_STAR_CHAR});
						}

					}
				} catch (IOException e) {
					logger.error("Exception in writting accountFa..." + e, e);
				}
				
				return result.toByteArray();


			}
			else
			{
				try
				{ //AldTODO Taks002 : Correct this !!!
					if (atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) {
						result.write(ASCIIConstants.ESC);
						result.write(("("+encodings.get(NDCConvertor.ENGLISH_ENCODING)).getBytes());
						result.write("***.****.********.*".getBytes());
						result.write(ASCIIConstants.ESC);
						result.write(("("+encodings.get(NDCConvertor.FARSI_RECIEPT_ENCODING)).getBytes());
					}
					else
					{
						byte[] starsTr = new byte[] {NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,
								NCR_RECEIPT_DOT_CHAR,
								NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,
								NCR_RECEIPT_DOT_CHAR,
								NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,NCR_RECEIPT_STAR_CHAR,
								NCR_RECEIPT_DOT_CHAR,
								NCR_RECEIPT_STAR_CHAR
								};
						result.write(starsTr);
					}
				} catch (IOException e) {
					logger.error("Exception in writting accountFa..." + e, e);
				}
				return result.toByteArray();
			}
		}
		
		//TASK Task002 : Transfer to Account 
		//AldComment Task002 : add 92.04.02
		private byte[] simpleTransferToAccountReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings/*, NDCConvertor convertor*/){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
			byte[] temp;
			try{
				Integer lineLength = config.getReceiptLineLength();
				Integer leftMargin = config.getReceiptLeftMargin();
				TextDirection textDir = TextDirection.RightToLeft;
				String lang = Language.FarsiLanguage.toString();
				
				out.write(generateHeader(atm, ifx, encodings, config, lineLength, leftMargin, textDir));
				
				if(config.getReceiptDetailsMap().containsKey("رسید انتقال به سپرده" + "$center$" + lineLength + "$" + leftMargin))
					out.write(config.getReceiptDetailsMap().get("رسید انتقال به سپرده"+ "$center$" + lineLength + "$" + leftMargin));
				else{
					temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید انتقال به سپرده"));
					out.write(temp);
					config.setReceiptDetailsMap("رسید انتقال به سپرده"+"$center$" + lineLength + "$" + leftMargin, temp);		
				}			
				out.write(ASCIIConstants.LF);

				out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));
						
				out.write(datePersianFormat(lineLength, leftMargin, atm, ifx.getReceivedDt()));
				out.write(ASCIIConstants.LF);
				out.write(ASCIIConstants.LF);
				
				if(!config.getReceiptDetailsMap().containsKey("شماره پیگیری")){
					temp = convert2Farsi("شماره پیگیری");
					config.setReceiptDetailsMap("شماره پیگیری", temp);
				}
				out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره پیگیری"), convert2Farsi(ifx.getSrc_TrnSeqCntr())));
				out.write(ASCIIConstants.LF);
				
				if(!config.getReceiptDetailsMap().containsKey("مبلغ")){
					temp = convert2Farsi("مبلغ");
					config.setReceiptDetailsMap("مبلغ", temp);
				}
				out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("مبلغ"), accBalAvailable(lang, "7", encodings, atm, ifx.getAuth_Amt(), 15)));
				
				//TASK Task029 : Print Bank Name
				out.write(alignTextRight(textDir, lineLength, leftMargin, convert2Farsi("از "+ BankNameFa(encodings, atm, Util.longValueOf(ifx.getDestBankId()))))); //AldTODO Task029
				out.write(ASCIIConstants.LF);	
				
				if(!config.getReceiptDetailsMap().containsKey("از کارت")){
					temp = convert2Farsi("از کارت");
					config.setReceiptDetailsMap("از کارت", temp);
				}
				out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("از کارت"), appPanFormatFa(encodings, atm, ifx.getActualAppPAN())));
				out.write(ASCIIConstants.LF);
				
				//TASK Task029 : Print Bank Name
				out.write(alignTextRight(textDir, lineLength, leftMargin, convert2Farsi("به "+ BankNameFa(encodings, atm, Util.longValueOf(ifx.getRecvBankId())))));//AldTODO Task029
				out.write(ASCIIConstants.LF);
				
				if(!config.getReceiptDetailsMap().containsKey("به سپرده")){
					temp = convert2Farsi("به سپرده");
					config.setReceiptDetailsMap("به سپرده", temp);
				}
				out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("به سپرده"), accountFormatFa(encodings, atm, ifx.getActualSecondAppPan())));
				out.write(ASCIIConstants.LF);
				
				out.write(alignTextRight(textDir, lineLength, leftMargin, convert2Farsi("به نام "+ifx.getCardHolderName()+ " "+ ifx.getCardHolderFamily()+ " "+ "انتقال یافت")));
				
				out.write(putLF(10));
				
				out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));
				
				//TASK Task040 : DailyMessage
				out.write(ASCIIConstants.LF);
				out.write(ASCIIConstants.LF);
				out.write(generateDailyMessage(atm, config,encodings, lineLength, leftMargin, textDir)); 				
				
				out.write(ASCIIConstants.FF);
				
			}catch(IOException e){
				logger.error(e,e);
				return new byte[]{0x20};
			}
			return out.toByteArray();
			

		}
		
		//TASK Task019 : Receipt Option
		private byte[] simpleShebaJournal(Ifx ifx){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try{
				out.write(ASCIIConstants.LF);
				out.write("Sheba:".toUpperCase().getBytes()); //Task141 : NCR
				out.write(ASCIIConstants.SO);
				out.write(0x31);
				
				out.write("REF:".getBytes());
				out.write(ifx.getSrc_TrnSeqCntr().getBytes());
				out.write(ASCIIConstants.SO);
				out.write(0x31);
				
				out.write("PAN:".getBytes());
				out.write(ifx.getActualAppPAN().getBytes());
				out.write(ASCIIConstants.SO);
				out.write(0x31);
				
				out.write("TIME:".getBytes());
				out.write(ifx.getReceivedDt().toString().getBytes());
				out.write(ASCIIConstants.SO);
				out.write(0x31);
				
				out.write("TERMINAL:".getBytes());
				out.write(ifx.getTerminalId().getBytes());

//				String st= "[LF]Bill Payment:[SO]1" +
//						"[GR ifx.Auth_Amt][SO]1" +
//						"REF:[GR ifx.Src_TrnSeqCntr][SO]1" +
//						"PAN:[GR ifx.actualAppPAN][SO]1" +
//						"TIME:[GR ifx.receivedDt][SO]1" +
//						"TERMINAL:[GR ifx.TerminalId]";
				
			}catch(IOException e){
				logger.error(e,e);
				return new byte[]{0x20};

			}
			return out.toByteArray();
			
		}
		
		private byte[] simpleShebaReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings/*,NDCConvertor convertor*/){

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());

			try {
				Integer lineLength = config.getReceiptLineLength();
				Integer leftMargin = config.getReceiptLeftMargin();
				TextDirection textDir = TextDirection.RightToLeft;
				String lang = Language.FarsiLanguage.toString();
				byte[] temp;
				
				out.write(generateHeader(atm, ifx,encodings, config, lineLength, leftMargin, textDir));
				
				if(config.getReceiptDetailsMap().containsKey("رسید شبا" + "$center$" + lineLength + "$" + leftMargin))
					out.write(config.getReceiptDetailsMap().get("رسید شبا"+ "$center$" + lineLength + "$" + leftMargin));
				else{
					temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید شبا"));
					out.write(temp);
					config.setReceiptDetailsMap("رسید شبا"+"$center$" + lineLength + "$" + leftMargin, temp);		
				}
				
				out.write(ASCIIConstants.LF);
				
				out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));
				
				out.write(generateDateAndAppPanAndTrn_Seq(atm, config, encodings, lineLength, leftMargin, textDir, ifx));
				
				if(!config.getReceiptDetailsMap().containsKey("شماره شبا")){
					temp = convert2Farsi("شماره شبا");
					config.setReceiptDetailsMap("شماره شبا", temp);		
				}
				out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("شماره شبا"), sheba2Farsi(atm,ifx.getShebaCode()))); //TASK Task141 : NCR
				out.write(ASCIIConstants.LF);
				
				out.write(putLF(10));
				
				out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));
				out.write(ASCIIConstants.FF);
				
			} catch (IOException e) {
				logger.error(e,e);
				return new byte[]{0x20};
			}
			return out.toByteArray();
		}
		
		//TASK Task081 : ATM Saham Feature
		private byte[] simpleStockReceiptFa(ATMTerminal atm, Ifx ifx, Map<String, String> encodings/*,NDCConvertor convertor*/){

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());

			try {
				Integer lineLength = config.getReceiptLineLength();
				Integer leftMargin = config.getReceiptLeftMargin();
				TextDirection textDir = TextDirection.RightToLeft;
				String lang = Language.FarsiLanguage.toString();
				byte[] temp;
				
				out.write(generateHeader(atm, ifx,encodings, config, lineLength, leftMargin, textDir));
				if(config.getReceiptDetailsMap().containsKey("رسید اعلام سهام بانک پاسارگاد" + "$center$" + lineLength + "$" + leftMargin))
					out.write(config.getReceiptDetailsMap().get("رسید اعلام سهام بانک پاسارگاد"+ "$center$" + lineLength + "$" + leftMargin));
				else{
					temp = center(textDir, lineLength, leftMargin, convert2Farsi("رسید اعلام سهام بانک پاسارگاد"));
					out.write(temp);
					config.setReceiptDetailsMap("رسید اعلام سهام بانک پاسارگاد"+"$center$" + lineLength + "$" + leftMargin, temp);		
				}
				
				out.write(ASCIIConstants.LF);
				
				out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));
				
				out.write(generateDateAndAppPanAndTrn_Seq(atm, config, encodings, lineLength, leftMargin, textDir, ifx));

				if (ifx.getStockCode() != null && !ifx.getStockCode().equals("0")){
					if(!config.getReceiptDetailsMap().containsKey("کد سهام")){
						temp = convert2Farsi("کد سهام");
						config.setReceiptDetailsMap("کد سهام", temp);		
					}
					//AldTODO Task081 : stock2Farsi ???
					out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("کد سهام"), convert2Farsi(ifx.getStockCode())));
					out.write(ASCIIConstants.LF);
					
					if(!config.getReceiptDetailsMap().containsKey("تعداد سهام")){
						temp = convert2Farsi("تعداد سهام");
						config.setReceiptDetailsMap("تعداد سهام", temp);		
					}
					//AldTODO Task081 : Check Shavad
					out.write(justify(textDir, lineLength, leftMargin, config.getReceiptDetailsMap().get("تعداد سهام"), convert2Farsi(ifx.getStockCount())));
					out.write(ASCIIConstants.LF);	
					
					//TASK Task106 - (2745) Saham New Peygham
					out.write(putLF(2));
					out.write(center(textDir, lineLength, leftMargin, convert2Farsi("سهامدار گرامی تعداد سهام درج شده مربوط به معاملات روز قبل میباشد")));
					out.write(ASCIIConstants.LF);					
				} else {
					out.write(putLF(1));;//ye khat paentar
					out.write(center(textDir, lineLength, leftMargin, convert2Farsi("سهامی برای این شماره کارت ثبت نشده است")));
					out.write(ASCIIConstants.LF);
				}
				
				out.write(putLF(10));
				
				out.write(generateFooter(atm, config,encodings, lineLength, leftMargin, textDir));
				out.write(ASCIIConstants.FF);
				
			} catch (IOException e) {
				logger.error(e,e);
				return new byte[]{0x20};
			}
			return out.toByteArray();
		}
		
		//TASK Task081 : ATM Saham feature
		private byte[] simpleStockReceiptEn(ATMTerminal atm, Ifx ifx, Map<String, String> encodings){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ATMConfiguration config = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
			try {

//				 headerEn
//					+ (!atmType.equals(ATMType.NCR) ? "[LF][LF][GR center('Stock') ]" : "[LF][LF][GR center(GR c2NCRE('Stock')) ]")
//					+ newLine
//					+ lineEn
//					+ receivedDateEn
////					+ newLine + newLine + newLine
//					+ newLine + newLine
//					+ formatAppPanEn
////					+ newLine + newLine 
//					+ newLine  
//					+ seqCntrEn 
////					+ newLine + newLine 
//					+ newLine
//					+ "[GR center('-----------')]"
////					+ newLine + newLine 
//					+ newLine  
//					+ (!atmType.equals(ATMType.NCR) ?  "[GR justify(ifx.getTopupData().getCellPhoneNumber(),'Mobile No')]"
//					  : "[GR justify(c2E(ifx.getTopupData().getCellPhoneNumber()),GR c2NCRE('Mobile No'))]")
//					+ newLine  
//					+ amountEn
//					+ newLine
//					+ (!atmType.equals(ATMType.NCR) ?  "[GR center('IranCell HelpDesk Phone Number 140') ]"
//					  : "[GR center(GR c2NCRE('IranCell HelpDesk Phone Number 140')) ]")
//					+ newLine
//					+ footerEn;			
				
				Integer lineLength = config.getReceiptLineLength();
				Integer leftMargin = config.getReceiptLeftMargin();
				TextDirection textDir = TextDirection.LeftToRight;
				String lang = Language.EnglishLanguage.toString();
				byte[] temp;

				out.write(generateHeaderEn(atm, ifx,encodings, config, lineLength, leftMargin, textDir));

				if(config.getReceiptDetailsMap().containsKey("Bank Pasargad Stock Receipt" + "$center$" + lineLength + "$" + leftMargin))
					out.write(config.getReceiptDetailsMap().get("Bank Pasargad Stock Receipt"+ "$center$" + lineLength + "$" + leftMargin));
				else{
					if ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) )
						temp = center(textDir, lineLength, leftMargin, "Bank Pasargad Stock Receipt");
					else
						temp = center(textDir, lineLength, leftMargin, convertToEnglishForNCR("Bank Pasargad Stock Receipt"));
						
					out.write(temp);
					config.setReceiptDetailsMap("Bank Pasargad Stock Receipt"+"$center$" + lineLength + "$" + leftMargin, temp);
				}
				out.write(ASCIIConstants.LF);

				out.write(generateHorizontalLine(encodings, lineLength, leftMargin, atm));

				out.write(generateDateAndAppPanAndTrn_SeqEn(atm, config, encodings, lineLength, leftMargin, textDir, ifx));

				out.write(center(textDir, lineLength, leftMargin, "-----------"));
				out.write(ASCIIConstants.LF);
				
				//AldTODO Task081 : 
		
				
				if (ifx.getStockCode() != null && !ifx.getStockCode().equals("0")){
					if(!config.getReceiptDetailsMap().containsKey("Stock code")){
						if ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ){
							temp = "Stock code".getBytes();
						}
						else {
							temp = convertToEnglishForNCR("Stock code");
						}
						config.setReceiptDetailsMap("Stock code", temp);
					}
					if ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) )
						out.write(justify(textDir, lineLength, leftMargin,ifx.getStockCode().toString(), config.getReceiptDetailsMap().get("Stock code") )); 
					else
						out.write(justify(textDir, lineLength, leftMargin,convertToEnglishForNCR(ifx.getStockCode().toString()), config.getReceiptDetailsMap().get("Stock code") )); //935.... -> 0935....
						
					out.write(ASCIIConstants.LF);
					
					if(!config.getReceiptDetailsMap().containsKey("Stock count")){
						if ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ){
							temp = "Stock count".getBytes();
						}
						else {
							temp = convertToEnglishForNCR("Stock count");
						}
						config.setReceiptDetailsMap("Stock count", temp);
					}
					if ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) )
						out.write(justify(textDir, lineLength, leftMargin,ifx.getStockCount().toString(), config.getReceiptDetailsMap().get("Stock count") )); 
					else
						out.write(justify(textDir, lineLength, leftMargin,convertToEnglishForNCR(ifx.getStockCount().toString()), config.getReceiptDetailsMap().get("Stock count") )); //935.... -> 0935....
						
					out.write(ASCIIConstants.LF);	
					
					//TASK Task106 - (2745) Saham New Peygham
					out.write(putLF(2));
					if ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) ) {
						out.write(center(textDir, lineLength, leftMargin, "Dear stackholder,"));
	 					out.write(ASCIIConstants.LF); 				
						out.write(center(textDir, lineLength, leftMargin, "this stock count is for yesterday."));
					}
					else {
						out.write(center(textDir, lineLength, leftMargin, convertToEnglishForNCR("Dear stackholder,")));
	 					out.write(ASCIIConstants.LF); 				
						out.write(center(textDir, lineLength, leftMargin, convertToEnglishForNCR("this stock count is for yesterday.")));
					}
					out.write(ASCIIConstants.LF); 						
 				} else {
 					if ((atm.getProducer() == null || !atm.getProducer().equals(ATMProducer.NCR)) )
 						out.write(center(textDir, lineLength, leftMargin, "This card.NO doesn't have any stock"));
 					else
 						out.write(center(textDir, lineLength, leftMargin, convertToEnglishForNCR("This card.NO doesn't have any stock")));
 					out.write(ASCIIConstants.LF); 				
 				}
				
							
				

				out.write(putLF(10));

				out.write(generateFooterEn(atm, config,encodings, lineLength, leftMargin, textDir));

				out.write(ASCIIConstants.FF);
			} catch (IOException e) {
				logger.error(e,e);
				return new byte[]{0x20};
			}
			return out.toByteArray();
		}
		
		//TASK Task081 : ATM Saham Feature
		private byte[] simpleStockJournal(Ifx ifx){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try{
				out.write(ASCIIConstants.LF);
				out.write("Stock:".toUpperCase().getBytes()); //Task141 : NCR
				out.write(ASCIIConstants.SO);
				out.write(0x31);
				
				out.write("REF:".getBytes());
				out.write(ifx.getSrc_TrnSeqCntr().getBytes());
				out.write(ASCIIConstants.SO);
				out.write(0x31);
				
				out.write("PAN:".getBytes());
				out.write(ifx.getActualAppPAN().getBytes());
				out.write(ASCIIConstants.SO);
				out.write(0x31);
				
				out.write("TIME:".getBytes());
				out.write(ifx.getReceivedDt().toString().getBytes());
				out.write(ASCIIConstants.SO);
				out.write(0x31);
				
				out.write("TERMINAL:".getBytes());
				out.write(ifx.getTerminalId().getBytes());

//				String st= "[LF]Bill Payment:[SO]1" +
//						"[GR ifx.Auth_Amt][SO]1" +
//						"REF:[GR ifx.Src_TrnSeqCntr][SO]1" +
//						"PAN:[GR ifx.actualAppPAN][SO]1" +
//						"TIME:[GR ifx.receivedDt][SO]1" +
//						"TERMINAL:[GR ifx.TerminalId]";
				
			}catch(IOException e){
				logger.error(e,e);
				return new byte[]{0x20};

			}
			return out.toByteArray();
			
		}
		
				
		//TASK Task029 : Print Bank Name
		public String BankNameFa(Map<String, String> encodings, ATMTerminal atm, Long bin) {
			if (bin == null)
				return "";
			Bank bank = ProcessContext.get().getBank(bin.intValue());
			return bank.getName() != null ? bank.getName() : "";
		}	
		
		//TASK Task029 : Print Bank Name
		public String BankNameEn(Long bin) {
			if (bin == null)
				return "";
			Bank bank = ProcessContext.get().getBank(bin.intValue());
			return bank.getNameEn() != null ? bank.getNameEn() : "";
		}		
		
}
