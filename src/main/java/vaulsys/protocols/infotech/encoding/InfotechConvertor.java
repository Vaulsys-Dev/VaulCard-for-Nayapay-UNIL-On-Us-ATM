package vaulsys.protocols.infotech.encoding;

import vaulsys.authorization.component.AuthorizationComponent;
import vaulsys.protocols.encoding.impl.FarsiConvertor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class InfotechConvertor extends FarsiConvertor{
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
			/* 49 */':',   
			/* 50 */'»', 
			/* 51 */'«', 
			/* 52 */'؟', 
			/* 53 */'!', 
			/* 54 */')', 
			/* 55 */'(', 
			/* 56 */'+', 
			/* 57 */'÷', 
			/* 58 */'.', 
			/* 59 */'/', 
			/* 60 */'=', 
			/* 61 */']', 
			/* 62 */'[', 
			/* 63 */'}', 
			/* 64 */'{',
			/* 65 */'>',
			/* 66 */'<',
			/* 67 */'×',
			
			
			};

			protected int[] farsiCharsCompleter = { 
			-1, -1, -1, -1, /* 0-3 */
			-1, -1, -1, -1, /* 4-7 */
			-1, -1, -1, -1, /* 8-11 */
			-1, -1, -1, -1, -1, /* 12-16 */
			-1, -1, -1, -1, /* 17-20 */
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
			-1, -1, -1, /*65-67*/
			}; 

			protected int[][] farsiCharsConnectingInstances = {
			/* 0 */{ 0x3B, 0x3B, 0x3B, 0x3B },
			/* 1 */{ 0x61, 0x61, 0x61, 0x61 },
			/* 2 */{ 0x40, 0x40, 0x41, 0x41 },
			/* 3 */{ 0x3F, 0x3F, 0x3F, 0x3F },
			/* 4 */{ 0x42, 0x43, 0x42, 0x43 }, 
			/* 5 */{ 0x44, 0x45, 0x44, 0x45 },
			/* 6 */{ 0x46, 0x47, 0x46, 0x47 },
			/* 7 */{ 0x48, 0x49, 0x48, 0x49 },
			/* 8 */{ 0x4A, 0x4B, 0x4A, 0x4B },
			/* 9 */{ 0x4C, 0x4D, 0x4C, 0x4D },
			/* 10 */{ 0x4E, 0x4F, 0x4E, 0x4F },
			/* 11 */{ 0x50, 0x51, 0x50, 0x51 },
			/* 12 */{ 0x52, 0x52, 0x52, 0x52 },
			/* 13 */{ 0x53, 0x53, 0x53, 0x53 },
			/* 14 */{ 0x54, 0x54, 0x54, 0x54 },
			/* 15 */{ 0x55, 0x55, 0x55, 0x55 },
			/* 16 */{ 0x56, 0x56, 0x56, 0x56 },
			/* 17 */{ 0x57, 0x58, 0x57, 0x58 },
			/* 18 */{ 0x59, 0x5A, 0x59, 0x5A },
			/* 19 */{ 0x5B, 0x5C, 0x5B, 0x5C },
			/* 20 */{ 0x5D, 0x5E, 0x5D, 0x5E },
			/* 21 */{ 0x30, 0x30, 0x30, 0x30 },
			/* 22 */{ 0x31, 0x31, 0x31, 0x31 },
			/* 23 */{ 0x32, 0x32, 0x32, 0x32 },
			/* 24 */{ 0x33, 0x33, 0x33, 0x33 },
			/* 25 */{ 0x34, 0x34, 0x34, 0x34 },
			/* 26 */{ 0x35, 0x35, 0x35, 0x35 },
			/* 27 */{ 0x36, 0x36, 0x36, 0x36 },
			/* 28 */{ 0x37, 0x37, 0x37, 0x37 },
			/* 29 */{ 0x38, 0x38, 0x38, 0x38 },
			/* 30 */{ 0x39, 0x39, 0x39, 0x39 },
			/* 31 */{ 0x5F, 0x5F, 0x5F, 0x5F },
			/* 32 */{ 0x90, 0x90, 0x90, 0x90 },
			/* 33 */{ 0x91, 0x94, 0x92, 0x93 },
			/* 34 */{ 0x95, 0x98, 0x96, 0x97 },
			/* 35 */{ 0x99, 0x9A, 0x99, 0x9A },
			/* 36 */{ 0x9B, 0x9C, 0x9B, 0x9C },
			/* 37 */{ 0x9D, 0x9E, 0x9D, 0x9E },
			/* 38 */{ 0x9F, 0xA0, 0x9F, 0xA0 },
			/* 39 */{ 0xA1, 0xA3, 0xA1, 0xA3 },
			/* 40 */{ 0xA4, 0xA5, 0xA4, 0xA5 },
			/* 41 */{ 0xA6, 0xA7, 0xA6, 0xA7 },
			/* 42 */{ 0xA8, 0xA8, 0xA8, 0xA8 },
			/* 43 */{ 0xA9, 0xAB, 0xA9, 0xAA },
			/* 44 */{ 0xAD, 0xAE, 0xAC, 0xAE },
			/* 45 */{ 0xAF, 0xAF, 0xAF, 0xAF },
			/* 46 */{ 0x3A, 0x3A, 0x3A, 0x3A },
			/* 47 */{ 0x65, 0x65, 0x65, 0x65 },
			/* 48 */{ 0x3D, 0x3D, 0x3D, 0x3D },
			/* 49 */{ 0x69, 0x69, 0x69, 0x69 },
			/* 50 */{ 0x6F, 0x6F, 0x6F, 0x6F }, //'»'
			/* 51 */{ 0x70, 0x70, 0x70, 0x70 }, //'«'
			/* 52 */{ 0x3C, 0x3C, 0x3C, 0x3C }, //'؟'
			/* 53 */{ 0x60, 0x60, 0x60, 0x60 }, //'!'
			/* 54 */{ 0x62, 0x62, 0x62, 0x62 }, //')'
			/* 55 */{ 0x63, 0x63, 0x63, 0x63 }, //'('
			/* 56 */{ 0x64, 0x64, 0x64, 0x64 }, //'+'
			/* 57 */{ 0x66, 0x66, 0x66, 0x66 }, //'÷'
			/* 58 */{ 0x67, 0x67, 0x67, 0x67 }, //'.'
			/* 59 */{ 0x68, 0x68, 0x68, 0x68 }, //'/'
			/* 60 */{ 0x6A, 0x6A, 0x6A, 0x6A }, //'='
			/* 61 */{ 0x6B, 0x6B, 0x6B, 0x6B }, //']'
			/* 62 */{ 0x6C, 0x6C, 0x6C, 0x6C }, //'['
			/* 63 */{ 0x6D, 0x6D, 0x6D, 0x6D }, //'}'
			/* 64 */{ 0x6E, 0x6E, 0x6E, 0x6E }, //'{'
			/* 65 */{ 0x72, 0x72, 0x72, 0x72 }, //'>'
			/* 66 */{ 0x73, 0x73, 0x73, 0x73 }, //'<'
			/* 67 */{ 0x71, 0x71, 0x71, 0x71 }, //'×'
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

	@Override
	public Map<Integer, Character> getIndexesOfFarsiChars() {
		return null;
	}

	@Override
	public boolean isInNumberRange(byte b) {
		return b >= (byte)0x30 && b <= (byte) 0x39;
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
	protected String preProcess(String s) {
		return super.preProcess(s).replace("_", "-");
	}
	
	@Override
	public byte[] encode(String s) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int lang = 1;
		int farsiIndexStart = 0;
		
		try {
			for(int i=0; i<s.length(); i++){
				if( (s.charAt(i) >= 'a' && s.charAt(i) <= 'z') ||
					(s.charAt(i) >= 'A' && s.charAt(i) <= 'Z') ){
					if(lang == 1){
						if(i > 0){
							out.write(super.encode(s.substring(farsiIndexStart, i)));
						}
						out.write(0x1b);
						out.write(0x31);
					}
					farsiIndexStart = i;
					out.write(s.charAt(i));
					lang = 2;
				}else if(lang == 2){
					farsiIndexStart = i;
					out.write(0x1b);
					out.write(0x33);
					lang = 1;
				}
			}
			
			if(lang == 1)
				out.write(super.encode(s.substring(farsiIndexStart, s.length())));
		} catch (IOException e) {
		}

		return out.toByteArray();
	}
}
