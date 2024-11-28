package vaulsys.protocols.pos87.encoding;

import vaulsys.protocols.encoding.impl.FarsiConvertor;

import java.util.HashMap;
import java.util.Map;


public class HasinFarsiConvertor extends FarsiConvertor{

    private char[] farsiCharsIndexes = {
    		/*0*/'!',
    		/*1*/'"',
    		/*2*/'.',
    		/*3*/'-',
    		/*4*/'%',
    		/*5*/':',
    		/*6*/'?',
    		/*7*/',',
    		/*8*/'/',
    		/*9*/'آ',
    		/*10*/'أ',
    		/*11*/'ا',
    		/*12*/'ء',
    		/*13*/'ؤ',
    		/*14*/'ب',
    		/*15*/'پ',
    		/*16*/'ت',
    		/*17*/'ث',
    		/*18*/'ج', 
    		/*19*/'چ',
    		/*20*/'ح',
    		/*21*/'خ',
    		/*22*/'د',
    		/*23*/'ذ',
    		/*24*/'ر',
    		/*25*/'ز', 
    		/*26*/'ژ',
    		/*27*/'س',
    		/*28*/'ش', 
    		/*29*/'ص',
    		/*30*/'ض',
    		/*31*/'0',
    		/*32*/'1',
    		/*33*/'2',
    		/*34*/'3',
    		/*35*/'4',
    		/*36*/'5',
    		/*37*/'6',
    		/*38*/'7',
    		/*39*/'8',
    		/*40*/'9',
    		/*41*/'ط',
    		/*42*/'ظ',
    		/*43*/'ع',
    		/*44*/'غ',
    		/*45*/'ف',
    		/*46*/'ق',
    		/*47*/'ک',
    		/*48*/'گ',
    		/*49*/'ل',
    		/*50*/'~',
    		/*51*/'م',
    		/*52*/'ن',
    		/*53*/'و',
    		/*54*/'ه',
    		/*55*/'ی',
    		/*56*/' ',
    		/*57*/'*',
    		/*58*/')',
    		/*59*/'(',
    		/*60*/'A',
    		/*61*/'B',
            'C', 'D', 'E', 'F'
			,'G', 'H', 'I', 'J', 'K', 'L'
			,'M', 'N', 'O', 'P', 'Q', 'R'
			,'S', 'T', 'U', 'V', 'W', 'X'
			,'Y', 'Z'
			, 'a', 'b', 'c', 'd','e', 'f', 'g', 'h', 'i', 'j'
			,'k', 'l', 'm', 'n', 'o', 'p'
			,'q', 'r', 's', 't', 'u', 'v'
			,'w', 'x', 'y', 'z'};
    
    private int[] farsiCharsCompleter = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, 16, 16, 16, 16, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, 32, 32, 32,
            32, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, 16, -1, 16, 16, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1};

    private int[][] farsiCharsConnectingInstances = {
    		{0, 0, 0, 0}, 
    		{1, 1, 1, 1}, 
    		{2, 2, 2, 2},
            {3, 3, 3, 3}, 
            {4, 4, 4, 4},
            {5, 5, 5, 5},
            {6, 6, 6, 6},
            {7, 7, 7, 7},
            {8, 8, 8, 8},
            {9, 9, 9, 9}, 
            /*10*/{10, 10, 14, 14},
            /*11*/{11, 11, 12, 12},
            /*12*/{13, 17, 13, 17}, 
            /*13*/{15, 15, 15, 15},
            /*14*/{18, 19, 18, 18},
            /*15*/{20, 21, 20, 20},
            /*16*/{22, 23, 22, 22},
            /*17*/{24, 25, 24, 24},
            /*18*/{26, 27, 26, 27},
            /*19*/{28, 29, 28, 29},
            /*20*/{30, 31, 30, 31},
            /*21*/{33, 34, 33, 34},
            /*22*/{35, 35, 35, 35}, 
            /*23*/{36, 36, 36, 36},
            /*24*/{37, 37, 37, 37}, 
            /*25*/{38, 38, 38, 38},
            /*26*/{39, 39, 39, 39},
            /*27*/{40, 41, 40, 41},
            /*28*/{42, 43, 42, 43},
            /*29*/{44, 45, 44, 45},
            /*30*/{46, 47, 46, 47}, 
            /*31*/{80, 80, 80, 80},
            /*32*/{81, 81, 81, 81},
            /*33*/{82, 82, 82, 82},
            /*34*/{83, 83, 83, 83},
            /*35*/{84, 84, 84, 84},
            /*36*/{85, 85, 85, 85},
            /*37*/{86, 86, 86, 86},
            /*38*/{87, 87, 87, 87},
            /*39*/{88, 88, 88, 88},
            /*40*/{89, 89, 89, 89}, 
            /*41*/{96, 96, 96, 96}, 
            /*42*/{97, 97, 97, 97},
            /*43*/{98, 101, 99, 100},
            /*44*/{102, 105, 103, 104},
            /*45*/{106, 106, 106, 106},
            /*46*/{107, 108, 107, 108},
            /*47*/{109, 109, 109, 109},
            /*48*/{110, 110, 110, 110},
            /*49*/{111, 113, 111, 113},
            /*50*/{112, 112, 112, 112},
            /*51*/{114, 115, 114, 115},
            /*52*/{116, 117, 116, 117},
            /*53*/{118, 118, 118, 118},
            /*54*/{119, 122, 120, 121},
            /*55*/{123, 125, 124, 126},
            /*56*/{127, 127, 127, 127}, 
            /*57*/{128, 128, 128, 128},
            /*58*/{94, 94, 94, 94},
            /*59*/{93, 93, 93, 93},
            /*60*/{ 129, 129, 129, 129 },
            /*61*/{ 130, 130, 130, 130 },
            { 131, 131, 131, 131 },
            { 132, 132, 132, 132 },
            { 133, 133, 133, 133 },
            { 134, 134, 134, 134 },
            { 135, 135, 135, 135 },
            { 136, 136, 136, 136 },
            { 137, 137, 137, 137 },
            { 138, 138, 138, 138 },
            { 139, 139, 139, 139 },
            { 140, 140, 140, 140 },
            { 141, 141, 141, 141 },
            { 142, 142, 142, 142 },
            { 143, 143, 143, 143 },
            { 144, 144, 144, 144 },
            { 145, 145, 145, 145 },
            { 146, 146, 146, 146 },
            { 147, 147, 147, 147 },
            { 148, 148, 148, 148 },
            { 149, 149, 149, 149 },
            { 150, 150, 150, 150 },
            { 151, 151, 151, 151 },
            { 152, 152, 152, 152 },
            { 153, 153, 153, 153 },
            { 154, 154, 154, 154 },
            { 155, 155, 155, 155 },
            { 156, 156, 156, 156 },
            { 157, 157, 157, 157 },
            { 158, 158, 158, 158 },
            { 159, 159, 159, 159 },
            { 160, 160, 160, 160 },
            { 161, 161, 161, 161 },
            { 162, 162, 162, 162 },
            { 163, 163, 163, 163 },
            { 164, 164, 164, 164 },
            { 165, 165, 165, 165 },
            { 166, 166, 166, 166 },
            { 167, 167, 167, 167 },
            { 168, 168, 168, 168 },
            { 169, 169, 169, 169 },
            { 170, 170, 170, 170 },
            { 171, 171, 171, 171 },
            { 172, 172, 172, 172 },
            { 173, 173, 173, 173 },
            { 174, 174, 174, 174 },
            { 175, 175, 175, 175 },
            { 176, 176, 176, 176 },
            { 177, 177, 177, 177 },
            { 178, 178, 178, 178 },
            { 179, 179, 179, 179 },
            { 180, 180, 180, 180 }
            };


    private Map<Integer, Character> indexesOfFarsiChars ;
    
    private void init(){
    	indexesOfFarsiChars = new HashMap<Integer, Character>();
    	indexesOfFarsiChars.put(0, '!');
    	indexesOfFarsiChars.put(1, '"');
    	indexesOfFarsiChars.put(2, '.');
    	indexesOfFarsiChars.put(3, '-');
    	indexesOfFarsiChars.put(4, '%');
    	indexesOfFarsiChars.put(5, ':');
    	indexesOfFarsiChars.put(6, '?');
    	indexesOfFarsiChars.put(7, ',');
    	indexesOfFarsiChars.put(8, '/');
    	indexesOfFarsiChars.put(9, 'آ');
    	indexesOfFarsiChars.put(10, 'أ');
    	indexesOfFarsiChars.put(14, 'أ');
    	indexesOfFarsiChars.put(11, 'ا');
    	indexesOfFarsiChars.put(12, 'ا');
    	indexesOfFarsiChars.put(13, 'ء');
    	indexesOfFarsiChars.put(17, 'ء');
    	indexesOfFarsiChars.put(15, 'ؤ');
    	indexesOfFarsiChars.put(18, 'ب');
    	indexesOfFarsiChars.put(19, 'ب');
    	indexesOfFarsiChars.put(20, 'پ');
    	indexesOfFarsiChars.put(21, 'پ');
    	indexesOfFarsiChars.put(22, 'ت');
    	indexesOfFarsiChars.put(23, 'ت');
    	indexesOfFarsiChars.put(24, 'ث');
    	indexesOfFarsiChars.put(25, 'ث');
    	indexesOfFarsiChars.put(26, 'ج');
    	indexesOfFarsiChars.put(27, 'ج');
    	indexesOfFarsiChars.put(28, 'چ');
    	indexesOfFarsiChars.put(29, 'چ');
    	indexesOfFarsiChars.put(30, 'ح');
    	indexesOfFarsiChars.put(31, 'ح');
    	indexesOfFarsiChars.put(33, 'خ');
    	indexesOfFarsiChars.put(34, 'خ');
    	indexesOfFarsiChars.put(35, 'د');
    	indexesOfFarsiChars.put(36, 'ذ');
    	indexesOfFarsiChars.put(37, 'ر');
    	indexesOfFarsiChars.put(38, 'ز');
    	indexesOfFarsiChars.put(39, 'ژ');
    	indexesOfFarsiChars.put(40, 'س');
    	indexesOfFarsiChars.put(41, 'س');
    	indexesOfFarsiChars.put(42, 'ش');
    	indexesOfFarsiChars.put(43, 'ش');
    	indexesOfFarsiChars.put(44, 'ص');
    	indexesOfFarsiChars.put(45, 'ص');
    	indexesOfFarsiChars.put(46, 'ض');
    	indexesOfFarsiChars.put(47, 'ض');
    	indexesOfFarsiChars.put(80, '0');
    	indexesOfFarsiChars.put(81, '1');
    	indexesOfFarsiChars.put(82, '2');
    	indexesOfFarsiChars.put(83, '3');
    	indexesOfFarsiChars.put(84, '4');
    	indexesOfFarsiChars.put(85, '5');
    	indexesOfFarsiChars.put(86, '6');
    	indexesOfFarsiChars.put(87, '7');
    	indexesOfFarsiChars.put(88, '8');
    	indexesOfFarsiChars.put(89, '9');
    	indexesOfFarsiChars.put(96, 'ط');
    	indexesOfFarsiChars.put(97, 'ظ');
    	indexesOfFarsiChars.put(98, 'ع');
    	indexesOfFarsiChars.put(99, 'ع');
    	indexesOfFarsiChars.put(100, 'ع');
    	indexesOfFarsiChars.put(101, 'ع');
    	indexesOfFarsiChars.put(102, 'غ');
    	indexesOfFarsiChars.put(103, 'غ');
    	indexesOfFarsiChars.put(104, 'غ');
    	indexesOfFarsiChars.put(105, 'غ');
    	indexesOfFarsiChars.put(106, 'ف');
    	indexesOfFarsiChars.put(107, 'ق');
    	indexesOfFarsiChars.put(108, 'ق');
    	indexesOfFarsiChars.put(109, 'ک');
    	indexesOfFarsiChars.put(110, 'گ');
    	indexesOfFarsiChars.put(111, 'ل');
    	indexesOfFarsiChars.put(113, 'ل');
    	indexesOfFarsiChars.put(112, '~');
    	indexesOfFarsiChars.put(114, 'م');
    	indexesOfFarsiChars.put(115, 'م');
    	indexesOfFarsiChars.put(116, 'ن');
    	indexesOfFarsiChars.put(117, 'ن');
    	indexesOfFarsiChars.put(118, 'و');
    	indexesOfFarsiChars.put(119, 'ه');
    	indexesOfFarsiChars.put(120, 'ه');
    	indexesOfFarsiChars.put(121, 'ه');
    	indexesOfFarsiChars.put(122, 'ه');
    	indexesOfFarsiChars.put(123, 'ی');
    	indexesOfFarsiChars.put(124, 'ی');
    	indexesOfFarsiChars.put(125, 'ی');
    	indexesOfFarsiChars.put(126, 'ی');
    	indexesOfFarsiChars.put(127, ' ');
    	indexesOfFarsiChars.put(128, '*');
    	indexesOfFarsiChars.put(94, ')');
    	indexesOfFarsiChars.put(93, '(');
    	indexesOfFarsiChars.put(129, 'A');
    	indexesOfFarsiChars.put(130, 'B');
    	indexesOfFarsiChars.put(131, 'C');
    	indexesOfFarsiChars.put(132, 'D');
    	indexesOfFarsiChars.put(133, 'E');
    	indexesOfFarsiChars.put(134, 'F');
    	indexesOfFarsiChars.put(135, 'G');
    	indexesOfFarsiChars.put(136, 'H');
    	indexesOfFarsiChars.put(137, 'I');
    	indexesOfFarsiChars.put(138, 'J');
    	indexesOfFarsiChars.put(139, 'K');
    	indexesOfFarsiChars.put(140, 'L');
    	indexesOfFarsiChars.put(141, 'M');
    	indexesOfFarsiChars.put(142, 'N');
    	indexesOfFarsiChars.put(143, 'O');
    	indexesOfFarsiChars.put(144, 'P');
    	indexesOfFarsiChars.put(145, 'Q');
    	indexesOfFarsiChars.put(146, 'R');
    	indexesOfFarsiChars.put(147, 'S');
    	indexesOfFarsiChars.put(148, 'T');
    	indexesOfFarsiChars.put(149, 'U');
    	indexesOfFarsiChars.put(150, 'V');
    	indexesOfFarsiChars.put(151, 'W');
    	indexesOfFarsiChars.put(152, 'X');
    	indexesOfFarsiChars.put(153, 'Y');
    	indexesOfFarsiChars.put(154, 'Z');
    	indexesOfFarsiChars.put(155, 'a');
    	indexesOfFarsiChars.put(156, 'b');
    	indexesOfFarsiChars.put(157, 'c');
    	indexesOfFarsiChars.put(158, 'd');
    	indexesOfFarsiChars.put(159, 'e');
    	indexesOfFarsiChars.put(160, 'f');
    	indexesOfFarsiChars.put(161, 'g');
    	indexesOfFarsiChars.put(162, 'h');
    	indexesOfFarsiChars.put(163, 'i');
    	indexesOfFarsiChars.put(164, 'j');
    	indexesOfFarsiChars.put(165, 'k');
    	indexesOfFarsiChars.put(166, 'l');
    	indexesOfFarsiChars.put(167, 'm');
    	indexesOfFarsiChars.put(168, 'n');
    	indexesOfFarsiChars.put(169, 'o');
    	indexesOfFarsiChars.put(170, 'p');
    	indexesOfFarsiChars.put(171, 'q');
    	indexesOfFarsiChars.put(172, 'r');
    	indexesOfFarsiChars.put(173, 's');
    	indexesOfFarsiChars.put(174, 't');
    	indexesOfFarsiChars.put(175, 'u');
    	indexesOfFarsiChars.put(176, 'v');
    	indexesOfFarsiChars.put(177, 'w');
    	indexesOfFarsiChars.put(178, 'x');
    	indexesOfFarsiChars.put(179, 'y');
    	indexesOfFarsiChars.put(180, 'z');
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


	@Override
	public Map<Integer, Character> getIndexesOfFarsiChars() {
		if (indexesOfFarsiChars == null)
			init();
		return indexesOfFarsiChars;
	}

	@Override
	public boolean isInNumberRange(byte b) {
		return b >= (byte)80 && b <= (byte) 89;
	}


	@Override
	protected int indexOfSpace() {
		return getFarsiCharsConnectingInstances()[56][0];
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
}
