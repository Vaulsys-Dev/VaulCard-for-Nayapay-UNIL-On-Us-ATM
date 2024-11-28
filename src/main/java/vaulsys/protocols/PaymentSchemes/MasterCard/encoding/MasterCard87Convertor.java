/**
 * 
 */
package vaulsys.protocols.PaymentSchemes.MasterCard.encoding;

import vaulsys.protocols.encoding.impl.FarsiConvertor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author norouzi
 * 
 */
public class MasterCard87Convertor extends FarsiConvertor {

	public char[] farsiCharsIndexes = { 
			/*0*/'-',
			/*1*/'؟',
			/*2*/'ا',
			/*3*/'ء',
			/*4*/'ب',
			/*5*/'پ',
			/*6*/'ت',
			/*7*/'ث', 
			/*8*/'ج',
			/*9*/'چ', 
			/*10*/'ح',
			/*11*/'خ',
			/*12*/'د',
			/*13*/'ذ',
			/*14*/'ر',
			/*15*/'ز',
			/*16*/'ژ',
			/*17*/'س',
			/*18*/'ش',
			/*19*/'ص',
			/*20*/'ض',
			/*21*/'0',
			/*22*/'1',
			/*23*/'2',
			/*24*/'3',
			/*25*/'4',
			/*26*/'5',
			/*27*/'6',
			/*28*/'7',
			/*29*/'8',
			/*30*/'9',
			/*31*/'ط',
			/*32*/'ظ',
			/*33*/'ع', 
			/*34*/'غ',
			/*35*/'ف',
			/*36*/'ق',
			/*37*/'ک',
			/*38*/'گ',
			/*39*/'ل',
			/*40*/'م',
			/*41*/'ن',
			/*42*/'و',
			/*43*/'ه',
			/*44*/'ی',
			/*45*/' ',
			/*46*/'آ',
			/*47*/'،'};

	public int[] farsiCharsCompleter = { 
			-1, -1, -1, -1, /*0-3*/
			-1, -1, -1, -1, /*4-7*/
			-1, -1, -1, -1, /*8-11*/
			-1, -1, -1, -1, -1, /*12-16*/
			-1, -1, -1, -1, /*17-20*/
			-1, -1, -1, -1, -1, /*21-25*/
			-1, -1, -1, -1, -1, /*26-30*/
			-1, -1, -1, -1, /*31-34*/
			-1, -1, -1, -1, /*35-38*/
			-1, -1,	-1, -1, /*39-42*/
			-1, -1, -1, -1, -1 }; /*43-47*/

	public int[][] farsiCharsConnectingInstances = {
			/*0*/{ 139, 139, 139, 139 }, 
			/*1*/{ 140, 140, 140, 140 },
			/*2*/{ 144, 144, 145, 145 }, 
			/*3*/{ 143, 142, 143, 142 }, 
			/*4*/{ 146, 147, 146, 147 },
			/*5*/{ 148, 149, 148, 149 }, 
			/*6*/{ 150, 151, 150, 151 }, 
			/*7*/{ 152, 153, 152, 153 },
			/*8*/{ 154, 155, 154, 155 }, 
			/*9*/{ 156, 157, 156, 157 }, 
			/*10*/{ 158, 159, 158, 159 },
			/*11*/{ 160, 161, 160, 161 }, 
			/*12*/{ 162, 162, 162, 162 }, 
			/*13*/{ 163, 163, 163, 163 },
			/*14*/{ 164, 164, 164, 164 }, 
			/*15*/{ 165, 165, 165, 165 }, 
			/*16*/{ 166, 166, 166, 166 },
			/*17*/{ 167, 168, 167, 168 }, 
			/*18*/{ 169, 170, 169, 170 }, 
			/*19*/{ 171, 172, 171, 172 },
			/*20*/{ 173, 174, 173, 174 }, 
			/*21*/{ 128, 128, 128, 128 }, 
			/*22*/{ 129, 129, 129, 129 },
			/*23*/{ 130, 130, 130, 130 },
			/*24*/{ 131, 131, 131, 131 },
			/*25*/{ 132, 132, 132, 132 },
			/*26*/{ 133, 133, 133, 133 }, 
			/*27*/{ 134, 134, 134, 134 }, 
			/*28*/{ 135, 135, 135, 135 },
			/*29*/{ 136, 136, 136, 136 }, 
			/*30*/{ 137, 137, 137, 137 }, 
			/*31*/{ 175, 175, 175, 175 }, 
			/*32*/{ 224, 224, 224, 224 }, 
			/*33*/{ 225, 228, 226, 227 }, 
			/*34*/{ 229, 232, 230, 231 },
			/*35*/{ 233, 234, 233, 234 }, 
			/*36*/{ 235, 236, 235, 236 }, 
			/*37*/{ 237, 238, 237, 238 },
			/*38*/{ 239, 240, 239, 240 }, 
			/*39*/{ 241, 243, 241, 243 }, 
			/*40*/{ 244, 245, 244, 245 },
			/*41*/{ 246, 247, 246, 247 }, 
			/*42*/{ 248, 248, 248, 248 }, 
			/*43*/{ 249, 251, 249, 250 },
			/*44*/{ 253, 254, 252, 254 }, 
			/*45*/{ 32, 32, 32, 32 },
			/*46*/{ 141, 141, 141, 141 },
			/*47*/{ 138, 138, 138, 138 }
			};

	
	private Map<Integer, Character> indexesOfFarsiChars;
	private Map<Integer, Integer> endingChars;
	
	public void init(){
		indexesOfFarsiChars = new HashMap<Integer, Character>();
		endingChars = new HashMap<Integer, Integer>();
/*		indexesOfFarsiChars.put(128, '0');
		indexesOfFarsiChars.put(129, '1');
		indexesOfFarsiChars.put(130, '2');
		indexesOfFarsiChars.put(131, '3');
		indexesOfFarsiChars.put(132, '4');
		indexesOfFarsiChars.put(133, '5');
		indexesOfFarsiChars.put(134, '6');
		indexesOfFarsiChars.put(135, '7');
		indexesOfFarsiChars.put(136, '8');
		indexesOfFarsiChars.put(137, '9');
		indexesOfFarsiChars.put(138, '،');
    	indexesOfFarsiChars.put(139, '-');
    	indexesOfFarsiChars.put(140, '؟');
    	indexesOfFarsiChars.put(141, 'آ');
    	indexesOfFarsiChars.put(142, 'ﺋ');
    	indexesOfFarsiChars.put(143, 'ء');
    	indexesOfFarsiChars.put(144, 'ا');
    	indexesOfFarsiChars.put(145, 'ﺎ');
    	indexesOfFarsiChars.put(146, 'ب');
    	indexesOfFarsiChars.put(147, 'ﺑ');
    	indexesOfFarsiChars.put(148, 'پ');
    	indexesOfFarsiChars.put(149, 'ﭘ');
    	indexesOfFarsiChars.put(150, 'ت');
    	indexesOfFarsiChars.put(151, 'ﺗ');
    	indexesOfFarsiChars.put(152, 'ث');
    	indexesOfFarsiChars.put(153, 'ﺛ');
    	indexesOfFarsiChars.put(154, 'ج');
    	indexesOfFarsiChars.put(155, 'ﺟ');
    	indexesOfFarsiChars.put(156, 'چ');
    	indexesOfFarsiChars.put(157, 'ﭼ');
    	indexesOfFarsiChars.put(158, 'ح');
    	indexesOfFarsiChars.put(159, 'ﺣ');
    	indexesOfFarsiChars.put(160, 'خ');
    	indexesOfFarsiChars.put(161, 'ﺧ');
    	indexesOfFarsiChars.put(162, 'د');
    	indexesOfFarsiChars.put(163, 'ذ');
    	indexesOfFarsiChars.put(164, 'ر');
    	indexesOfFarsiChars.put(165, 'ز');
    	indexesOfFarsiChars.put(166, 'ژ');
    	indexesOfFarsiChars.put(167, 'س');
    	indexesOfFarsiChars.put(168, 'ﺳ');
    	indexesOfFarsiChars.put(169, 'ش');
    	indexesOfFarsiChars.put(170, 'ﺷ');
    	indexesOfFarsiChars.put(171, 'ص');
    	indexesOfFarsiChars.put(172, 'ﺻ');
    	indexesOfFarsiChars.put(173, 'ض');
    	indexesOfFarsiChars.put(174, 'ﺿ');
    	indexesOfFarsiChars.put(175, 'ط');
    	indexesOfFarsiChars.put(224, 'ظ');
    	indexesOfFarsiChars.put(225, 'ع');
    	indexesOfFarsiChars.put(226, 'ﻊ');
    	indexesOfFarsiChars.put(227, 'ﻌ');
    	indexesOfFarsiChars.put(228, 'ﻋ');
    	indexesOfFarsiChars.put(229, 'غ');
    	indexesOfFarsiChars.put(230, 'ﻎ');
    	indexesOfFarsiChars.put(231, 'ﻐ');
    	indexesOfFarsiChars.put(232, 'ﻏ');
    	indexesOfFarsiChars.put(233, 'ف');
    	indexesOfFarsiChars.put(234, 'ﻓ');
    	indexesOfFarsiChars.put(235, 'ق');
    	indexesOfFarsiChars.put(236, 'ﻗ');
    	indexesOfFarsiChars.put(237, 'ﻙ');
    	indexesOfFarsiChars.put(238, 'ﻛ');
    	indexesOfFarsiChars.put(239, 'گ');
    	indexesOfFarsiChars.put(240, 'ﮔ');
    	indexesOfFarsiChars.put(241, 'ل');
    	indexesOfFarsiChars.put(242, 'ﻻ');
    	indexesOfFarsiChars.put(243, 'ﻟ');
    	indexesOfFarsiChars.put(244, 'م');
    	indexesOfFarsiChars.put(245, 'ﻣ');
    	indexesOfFarsiChars.put(246, 'ن');
    	indexesOfFarsiChars.put(247, 'ﻧ');
    	indexesOfFarsiChars.put(248, 'و');
    	indexesOfFarsiChars.put(249, 'ه');
    	indexesOfFarsiChars.put(250, 'ﻬ');
    	indexesOfFarsiChars.put(251, 'ﻫ');
    	indexesOfFarsiChars.put(252, 'ﻰ');
    	indexesOfFarsiChars.put(253, 'ی');
    	indexesOfFarsiChars.put(254, 'ﻳ');
    	indexesOfFarsiChars.put(32, ' ');
    	*/
		indexesOfFarsiChars.put(128, '0');
		indexesOfFarsiChars.put(129, '1');
		indexesOfFarsiChars.put(130, '2');
		indexesOfFarsiChars.put(131, '3');
		indexesOfFarsiChars.put(132, '4');
		indexesOfFarsiChars.put(133, '5');
		indexesOfFarsiChars.put(134, '6');
		indexesOfFarsiChars.put(135, '7');
		indexesOfFarsiChars.put(136, '8');
		indexesOfFarsiChars.put(137, '9');
		indexesOfFarsiChars.put(138, '،');
    	indexesOfFarsiChars.put(139, '-');
    	indexesOfFarsiChars.put(140, '؟');
    	indexesOfFarsiChars.put(141, 'آ');
//    	indexesOfFarsiChars.put(142, 'ﺋ');
    	indexesOfFarsiChars.put(142, 'ئ');
    	indexesOfFarsiChars.put(143, 'ء');
    	indexesOfFarsiChars.put(144, 'ا');
    	indexesOfFarsiChars.put(145, 'ا');
    	indexesOfFarsiChars.put(146, 'ب');
    	indexesOfFarsiChars.put(147, 'ب');
    	indexesOfFarsiChars.put(148, 'پ');
    	indexesOfFarsiChars.put(149, 'پ');
    	indexesOfFarsiChars.put(150, 'ت');
    	indexesOfFarsiChars.put(151, 'ت');
    	indexesOfFarsiChars.put(152, 'ث');
    	indexesOfFarsiChars.put(153, 'ث');
    	indexesOfFarsiChars.put(154, 'ج');
    	indexesOfFarsiChars.put(155, 'ج');
    	indexesOfFarsiChars.put(156, 'چ');
    	indexesOfFarsiChars.put(157, 'چ');
    	indexesOfFarsiChars.put(158, 'ح');
    	indexesOfFarsiChars.put(159, 'ح');
    	indexesOfFarsiChars.put(160, 'خ');
    	indexesOfFarsiChars.put(161, 'خ');
    	indexesOfFarsiChars.put(162, 'د');
    	indexesOfFarsiChars.put(163, 'ذ');
    	indexesOfFarsiChars.put(164, 'ر');
    	indexesOfFarsiChars.put(165, 'ز');
    	indexesOfFarsiChars.put(166, 'ژ');
    	indexesOfFarsiChars.put(167, 'س');
    	indexesOfFarsiChars.put(168, 'س');
    	indexesOfFarsiChars.put(169, 'ش');
    	indexesOfFarsiChars.put(170, 'ش');
    	indexesOfFarsiChars.put(171, 'ص');
    	indexesOfFarsiChars.put(172, 'ص');
    	indexesOfFarsiChars.put(173, 'ض');
    	indexesOfFarsiChars.put(174, 'ض');
    	indexesOfFarsiChars.put(175, 'ط');
    	indexesOfFarsiChars.put(224, 'ظ');
    	indexesOfFarsiChars.put(225, 'ع');
    	indexesOfFarsiChars.put(226, 'ع');
    	indexesOfFarsiChars.put(227, 'ع');
    	indexesOfFarsiChars.put(228, 'ع');
    	indexesOfFarsiChars.put(229, 'غ');
    	indexesOfFarsiChars.put(230, 'غ');
    	indexesOfFarsiChars.put(231, 'غ');
    	indexesOfFarsiChars.put(232, 'غ');
    	indexesOfFarsiChars.put(233, 'ف');
    	indexesOfFarsiChars.put(234, 'ف');
    	indexesOfFarsiChars.put(235, 'ق');
    	indexesOfFarsiChars.put(236, 'ق');
    	
    	indexesOfFarsiChars.put(237, 'ک');
    	indexesOfFarsiChars.put(238, 'ک');
    	
//    	indexesOfFarsiChars.put(237, 'ﻙ');
    	
//    	indexesOfFarsiChars.put(237, 'ک');
//    	indexesOfFarsiChars.put(238, 'ک');
//    	
    	
    	indexesOfFarsiChars.put(239, 'گ');
    	indexesOfFarsiChars.put(240, 'گ');
    	indexesOfFarsiChars.put(241, 'ل');
    	indexesOfFarsiChars.put(242, 'ﻻ');
    	indexesOfFarsiChars.put(243, 'ل');
    	indexesOfFarsiChars.put(244, 'م');
    	indexesOfFarsiChars.put(245, 'م');
    	indexesOfFarsiChars.put(246, 'ن');
    	indexesOfFarsiChars.put(247, 'ن');
    	indexesOfFarsiChars.put(248, 'و');
    	indexesOfFarsiChars.put(249, 'ه');
    	indexesOfFarsiChars.put(250, 'ه');
    	indexesOfFarsiChars.put(251, 'ه');
    	indexesOfFarsiChars.put(252, 'ی');
    	indexesOfFarsiChars.put(253, 'ی');
    	indexesOfFarsiChars.put(254, 'ی');
    	indexesOfFarsiChars.put(32, ' ');
    	
    	endingChars.put(146, 146);
    	endingChars.put(148, 148);
    	endingChars.put(150, 150);
    	endingChars.put(152, 152);
    	endingChars.put(154, 154);
    	endingChars.put(156, 156);
    	endingChars.put(158, 158);
    	endingChars.put(160, 160);
    	endingChars.put(167, 167);
    	endingChars.put(169, 169);
    	endingChars.put(171, 171);
    	endingChars.put(173, 173);
    	endingChars.put(225, 225);
    	endingChars.put(226, 226);
    	endingChars.put(229, 229);
    	endingChars.put(230, 230);
    	endingChars.put(233, 233);
    	endingChars.put(235, 235);
    	endingChars.put(237, 237);
    	endingChars.put(239, 239);
    	endingChars.put(241, 241);
    	endingChars.put(244, 244);
    	endingChars.put(246, 246);
    	endingChars.put(249, 249);
    	endingChars.put(252, 252);
    	endingChars.put(253, 253);
    }
	
	
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

	public byte[] convert(Character printerFlag, String str) throws IOException {
		return super.encode(str);
	}

	@Override
	public byte[] finalize(byte[] converted, String encoding, String extendedEncoding) {
		if (converted == null)
			return "00".getBytes();
		int len = Math.min(converted.length, 33);
		byte[] finalbyte = new byte[len+2];
		finalbyte[0] = (byte) ('0' + (len / 10));
		finalbyte[1] = (byte) ('0' + (len % 10));
		System.arraycopy(converted, 0, finalbyte, 2, len);
		return finalbyte;
	}

	@Override
	public Map<Integer, Character> getIndexesOfFarsiChars() {
		if (indexesOfFarsiChars == null)
			init();
		return indexesOfFarsiChars;
	}

	@Override
	public boolean isInNumberRange(byte b) {
		return b >= (byte)128 && b <= (byte) 137;
	}


	@Override
	protected int indexOfSpace() {
		return getFarsiCharsConnectingInstances()[45][0];
	}


	@Override
	public Map<Integer, Integer> getEndingChars() {
		if (endingChars == null)
			init();
		
		return endingChars;		
	}


	@Override
	public boolean isSpecialCharacter(int code) {
		return (code == 242); //لا
	}


	@Override
	public String processSpecialCharacter(int code) {
		String result = "";
		if(code == 242){
			result += indexesOfFarsiChars.get(241); //ل
			result += indexesOfFarsiChars.get(144); //ا
		}
			
		return result;
	}
}
