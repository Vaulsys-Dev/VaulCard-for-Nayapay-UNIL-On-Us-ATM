package vaulsys.protocols.apacs70.encoding;

import groovy.lang.Binding;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.protocols.encoding.impl.FarsiConvertor;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.impl.Terminal;
import vaulsys.terminal.impl.TerminalSharedFeature;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Apacs70FarsiConvertor extends FarsiConvertor implements FPA {
	public static final Apacs70FarsiConvertor Instance = new Apacs70FarsiConvertor();
	
	@Override
	public byte[] encode(String s) {
		StringBuilder builder = new StringBuilder();
		if(s != null) {
			LinkedList<Character> list = new LinkedList<Character>();
			for(int i=0; i<s.length(); i++) {
				char c = s.charAt(i);
				if((c>='\u0021' && c<='\u007e') || (c>='\u0660' && c<='\u0669') || (c>='\u06f0' && c<='\u06f9'))
					list.addFirst(c);
				else {
					if (list.size() > 0) {
						for (int j = 0; j < list.size(); j++)
							builder.append(list.get(j));
						list.clear();
					}
					builder.append(c);
				}					
			}
			if (list.size() > 0) {
				for (int j = 0; j < list.size(); j++)
					builder.append(list.get(j));
				list.clear();
			}
		}
		return super.encode(builder.toString());
	}

	private char[] farsiCharsIndexes = {
			' ', 	// 0 SPACE
			'♀', 	// 1 SINGLESPACE
			'/', 	// 2 SLASH
/*3 QUESTION_FA*/ '؟',  
			'>', 	// 4 BIGGER
			'<', 	// 5 LITTLE
			'=', 	// 6 EQUAL
			'-', 	// 7 MINUS
			'+', 	// 8 PLUS
			'!', 	// 9 NOT
			'(', 	// 10 PARENTHESE_LEFT
			')', 	// 11 PARENTHESE_RIGHT
			':', 	// 12 COLON
			';', 	// 13 SEMI_COLON
			'.', 	// 14 DOT
			'⌂', 	// 15 IP_DOT
			'`', 	// 16 SINGLE_QOUTA
			'"', 	// 17 DOUBLE_QOUTA
			'~', 	// 18 TILDA
			'@', 	// 19 AT
			'#', 	// 20 SHARP
			'%', 	// 21 PERCENT
			'^', 	// 22 HAT
			'&', 	// 23 AMPERSAND
			'*', 	// 24 MULTIPLY
			'_', 	// 25 UNDER_LINE
			',', 	// 26 COMMA_EN
			'،', 	// 27 COMMA_FA
			'|', 	// 28 OR
			'√', 	// 29 TICK
			'☼', 	// 30 CROSS
			'∙', 	// 31 PASSWORD
			'«', 	// 32 GIUME_LEFT
			'»', 	// 33 GIUME_RIGHT
			
			'0', 	// 34 ZERO
			'1', 	// 35 ONE
			'2', 	// 36 TWO
			'3', 	// 37 THREE
			'4', 	// 38 FOUR
			'5', 	// 39 FIVE
			'6', 	// 40 SIX
			'7', 	// 41 SEVEN
			'8', 	// 42 EIGHT
			'9', 	// 43 NINE
			
			'أ', 	// 44 ALEF_HAMZE
			'ا', 	// 45 ALEF
			'آ', 	// 46 ALEF_AA
			'ب', 	// 47 BE
			'پ', 	// 48 PE
			'ت', 	// 49 TE
			'ث', 	// 50 SE
			'ج', 	// 51 JIM
			'چ', 	// 52 CHE
			'ح', 	// 53 HE_JIMI
			'خ', 	// 54 KHE
			'د', 	// 55 DAAL
			'ذ', 	// 56 ZAAL
			'ر', 	// 57 RE
			'ز', 	// 58 ZE
			'ژ', 	// 59 JHE
			'س', 	// 60 SIN
			'ش', 	// 61 SHIN
			'ص', 	// 62 SAAD
			'ض', 	// 63 ZAAD
			'ط', 	// 64 TAA
			'ظ', 	// 65 ZAA
			'ع', 	// 66 EYN
			'غ', 	// 67 GHEYN
			'ف', 	// 68 FE
			'ق', 	// 69 GHAAF
			'ک', 	// 70 KAAF
			'گ', 	// 71 GAAF
			'ل', 	// 72 LAAM
			'م', 	// 73 MIM
			'ن', 	// 74 NOON
			'و', 	// 75 VAAV
			'ه', 	// 76 HE_2CH
			'ی', 	// 77 YE
			'ئ', 	// 78 HAMZE
			
			'a', 	// 79 A
			'b', 	// 80 B
			'c', 	// 81 C
			'd', 	// 82 D
			'e', 	// 83 E
			'f', 	// 84 F
			'g', 	// 85 G
			'h', 	// 86 H
			'i', 	// 87 I
			'j', 	// 88 J
			'k', 	// 89 K
			'l', 	// 90 L
			'm', 	// 91 M
			'n', 	// 92 N
			'o', 	// 93 O
			'p', 	// 94 P
			'q', 	// 95 Q
			'r', 	// 96 R
			's', 	// 97 S
			't', 	// 98 T
			'u', 	// 99 U
			'v', 	// 100 V
			'w', 	// 101 W
			'x', 	// 102 X
			'y', 	// 103 Y
			'z', 	// 104 Z
			
			'A', 	// 105 A_C
			'B', 	// 106 B_C
			'C', 	// 107 C_C
			'D', 	// 108 D_C
			'E', 	// 109 E_C
			'F', 	// 110 F_C
			'G', 	// 111 G_C
			'H', 	// 112 H_C
			'I', 	// 113 I_C
			'J', 	// 114 J_C
			'K', 	// 115 K_C
			'L', 	// 116 L_C
			'M', 	// 117 M_C
			'N', 	// 118 N_C
			'O', 	// 119 O_C
			'P', 	// 120 P_C
			'Q', 	// 121 Q_C
			'R', 	// 122 R_C
			'S', 	// 123 S_C
			'T', 	// 124 T_C
			'U', 	// 125 U_C
			'V', 	// 126 V_C
			'W', 	// 127 W_C
			'X', 	// 128 X_C
			'Y', 	// 129 Y_C
			'Z', 	// 130 Z_C
			'?', 	// 131 QUESTION_EN
			'¶', 	// 132 ENTER
			'→',  // 133: RIGHT_ALIGNMENT 
			'↔', 	// 134: CENTER_ALIGNMENT 
			'←', 	// 135: LEFT_ALIGNMENT

			'\u0660',	// 136: ZERO_FA
			'\u0661',	// 137: ONE_FA
			'\u0662',	// 138: TWO_FA
			'\u0663',	// 139: THREE_FA
			'\u0664',	// 140: FOUR_FA
			'\u0665',	// 141: FIVE_FA
			'\u0666',	// 142: SIX_FA
			'\u0667',	// 143: SEVEN_FA
			'\u0668',	// 144: EIGHT_FA
			'\u0669',	// 149: NINE_FA

			'\u06f0',	// 150: ZERO_FA
			'\u06f1',	// 151: ONE_FA
			'\u06f2',	// 152: TWO_FA
			'\u06f3',	// 153: THREE_FA
			'\u06f4',	// 154: FOUR_FA
			'\u06f5',	// 155: FIVE_FA
			'\u06f6',	// 156: SIX_FA
			'\u06f7',	// 157: SEVEN_FA
			'\u06f8',	// 158: EIGHT_FA
			'\u06f9'	// 159: NINE_FA
	};

	private int[] farsiCharsCompleter = { 
			-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
			-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
			-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
			-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
			-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
			-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
			-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
			-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
			-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
			-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
			-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
			-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
			-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
			-1,-1, -1, -1, -1, -1
	};

	// A, F, L, M
	private int[][] farsiCharsConnectingInstances = {
				{SPACE, SPACE, SPACE, SPACE},	// 0: SPACE
				{SINGLESPACE, SINGLESPACE, SINGLESPACE, SINGLESPACE},			// 1: SINGLESPACE
				{SLASH, SLASH, SLASH, SLASH},			// 2: SLASH
				{QUESTION_FA, QUESTION_FA, QUESTION_FA, QUESTION_FA},			// 3: QUESTION_FA
				{BIGGER, BIGGER, BIGGER, BIGGER},			// 4: BIGGER
				{LITTLE, LITTLE, LITTLE, LITTLE},			// 5: LITTLE
				{EQUAL, EQUAL, EQUAL, EQUAL},			// 6: EQUAL
				{MINUS, MINUS, MINUS, MINUS},			// 7: MINUS
				{PLUS, PLUS, PLUS, PLUS},			// 8: PLUS
				{NOT, NOT, NOT, NOT},			// 9: NOT
				{PARENTHESE_LEFT_EN, PARENTHESE_LEFT_EN, PARENTHESE_LEFT_EN, PARENTHESE_LEFT_EN}, 		// 10: PARENTHESE_LEFT
				{PARENTHESE_RIGHT_EN, PARENTHESE_RIGHT_EN, PARENTHESE_RIGHT_EN, PARENTHESE_RIGHT_EN}, 		// 11: PARENTHESE_RIGHT
				{COLON, COLON, COLON, COLON}, 		// 12: COLON
				{SEMI_COLON, SEMI_COLON, SEMI_COLON, SEMI_COLON}, 		// 13: SEMI_COLON
				{DOT, DOT, DOT, DOT}, 		// 14: DOT
				{IP_DOT, IP_DOT, IP_DOT, IP_DOT}, 		// 15: IP_DOT
				{SINGLE_QOUTA, SINGLE_QOUTA, SINGLE_QOUTA, SINGLE_QOUTA}, 		// 16: SINGLE_QOUTA
				{DOUBLE_QOUTA, DOUBLE_QOUTA, DOUBLE_QOUTA, DOUBLE_QOUTA}, 		// 17: DOUBLE_QOUTA
				{TILDA, TILDA, TILDA, TILDA}, 		// 18: TILDA
				{AT, AT, AT, AT}, 		// 19: AT
				{SHARP, SHARP, SHARP, SHARP}, 		// 20: SHARP
				{PERCENT, PERCENT, PERCENT, PERCENT}, 		// 21: PERCENT
				{HAT, HAT, HAT, HAT}, 		// 22: HAT
				{AMPERSAND, AMPERSAND, AMPERSAND, AMPERSAND}, 		// 23: AMPERSAND
				{MULTIPLY, MULTIPLY, MULTIPLY, MULTIPLY}, 		// 24: MULTIPLY
				{UNDER_LINE, UNDER_LINE, UNDER_LINE, UNDER_LINE}, 		// 25: UNDER_LINE
				{COMMA_EN, COMMA_EN, COMMA_EN, COMMA_EN}, 		// 26: COMMA_EN
				{COMMA_FA, COMMA_FA, COMMA_FA, COMMA_FA}, 		// 27: COMMA_FA
				{OR, OR, OR, OR}, 		// 28: OR
				{TICK, TICK, TICK, TICK}, 		// 29: TICK
				{CROSS, CROSS, CROSS, CROSS}, 		// 30: CROSS
				{PASSWORD, PASSWORD, PASSWORD, PASSWORD}, 		// 31: PASSWORD
				{GIUME_LEFT, GIUME_LEFT, GIUME_LEFT, GIUME_LEFT}, 		// 32: GIUME_LEFT
				{GIUME_RIGHT, GIUME_RIGHT, GIUME_RIGHT, GIUME_RIGHT}, 		// 33: GIUME_RIGHT

				{ZERO_EN, ZERO_EN, ZERO_EN, ZERO_EN}, 		// 34: ZERO
				{ONE_EN, ONE_EN, ONE_EN, ONE_EN}, 		// 35: ONE
				{TWO_EN, TWO_EN, TWO_EN, TWO_EN}, 		// 36: TWO
				{THREE_EN, THREE_EN, THREE_EN, THREE_EN}, 		// 37: THREE
				{FOUR_EN, FOUR_EN, FOUR_EN, FOUR_EN}, 		// 38: FOUR
				{FIVE_EN, FIVE_EN, FIVE_EN, FIVE_EN}, 		// 39: FIVE
				{SIX_EN, SIX_EN, SIX_EN, SIX_EN}, 		// 40: SIX
				{SEVEN_EN, SEVEN_EN, SEVEN_EN, SEVEN_EN}, 		// 41: SEVEN
				{EIGHT_EN, EIGHT_EN, EIGHT_EN, EIGHT_EN}, 		// 42: EIGHT
				{NINE_EN, NINE_EN, NINE_EN, NINE_EN}, 		// 43: NINE

				// A, F, L, M
				{ALEF_HAMZE_A, ALEF_HAMZE_F, ALEF_HAMZE_L, ALEF_HAMZE_M}, 		// 44: ALEF_HAMZE
				{ALEF_A, ALEF_F, ALEF_L, ALEF_M}, 		// 45: ALEF
				{ALEF_AA_A, ALEF_AA_F, ALEF_AA_L, ALEF_AA_M}, 		// 46: ALEF_AA
				{BE_A, BE_F, BE_L, BE_M}, 		// 47: BE
				{PE_A, PE_F, PE_L, PE_M}, 		// 48: PE
				{TE_A, TE_F, TE_L, TE_M}, 		// 49: TE
				{SE_A, SE_F, SE_L, SE_M}, 		// 50: SE
				{JIM_A, JIM_F, JIM_L, JIM_M}, 		// 51: JIM
				{CHE_A, CHE_F, CHE_L, CHE_M}, 		// 52: CHE
				{HE_JIMI_A, HE_JIMI_F, HE_JIMI_L, HE_JIMI_M}, 		// 53: HE_JIMI
				{KHE_A, KHE_F, KHE_L, KHE_M}, 		// 54: KHE
				{DAAL_A, DAAL_F, DAAL_L, DAAL_M}, 		// 55: DAAL
				{ZAAL_A, ZAAL_F, ZAAL_L, ZAAL_M}, 		// 56: ZAAL
				{RE_A, RE_F, RE_L, RE_M}, 		// 57: RE
				{ZE_A, ZE_F, ZE_L, ZE_M}, 	// 58: ZE
				{JHE_A, JHE_F, JHE_L, JHE_M}, 	// 59: JHE
				{SIN_A, SIN_F, SIN_L, SIN_M}, 	// 60: SIN
				{SHIN_A, SHIN_F, SHIN_L, SHIN_M}, 	// 61: SHIN
				{SAAD_A, SAAD_F, SAAD_L, SAAD_M}, 	// 62: SAAD
				{ZAAD_A, ZAAD_F, ZAAD_L, ZAAD_M}, 	// 63: ZAAD
				{TAA_A, TAA_F, TAA_L, TAA_M}, 	// 64: TAA
				{ZAA_A, ZAA_F, ZAA_L, ZAA_M}, 	// 65: ZAA
				{EYN_A, EYN_F, EYN_L, EYN_M}, 	// 66: EYN
				{GHEYN_A, GHEYN_F, GHEYN_L, GHEYN_M}, 	// 67: GHEYN
				{FE_A, FE_F, FE_L, FE_M}, 	// 68: FE
				{GHAAF_A, GHAAF_F, GHAAF_L, GHAAF_M}, 	// 69: GHAAF
				{KAAF_A, KAAF_F, KAAF_L, KAAF_M}, 	// 70: KAAF
				{GAAF_A, GAAF_F, GAAF_L, GAAF_M}, 	// 71: GAAF
				{LAAM_A, LAAM_F, LAAM_L, LAAM_M}, 	// 72: LAAM
				{MIM_A, MIM_F, MIM_L, MIM_M}, 	// 73: MIM
				{NOON_A, NOON_F, NOON_L, NOON_M}, 	// 74: NOON
				{VAAV_A, VAAV_F, VAAV_L, VAAV_M}, 	// 75: VAAV
				{HE_2CH_A, HE_2CH_F, HE_2CH_L, HE_2CH_M}, 	// 76: HE_2CH
				{YE_A, YE_F, YE_L, YE_M}, 	// 77: YE
				{HAMZE_A, HAMZE_F, HAMZE_L, HAMZE_M},	// 78: HAMZE
				
				{A, A, A, A},	// 79: A
				{B, B, B, B},	// 80: B
				{C, C, C, C},	// 81: C
				{D, D, D, D},	// 82: D
				{E, E, E, E},	// 83: E
				{F, F, F, F},	// 84: F
				{G, G, G, G},	// 85: G
				{H, H, H, H},	// 86: H
				{I, I, I, I},	// 87: I
				{J, J, J, J},	// 88: J
				{K, K, K, K},	// 89: K
				{L, L, L, L},	// 90: L
				{M, M, M, M},	// 91: M
				{N, N, N, N},	// 92: N
				{O, O, O, O},	// 93: O
				{P, P, P, P},	// 94: P
				{Q, Q, Q, Q},	// 95: Q
				{R, R, R, R},	// 96: R
				{S, S, S, S},	// 97: S
				{T, T, T, T},	// 98: T
				{U, U, U, U},	// 99: U
				{V, V, V, V},	// 100: V
				{W, W, W, W},	// 101: W
				{X, X, X, X},	// 102: X
				{Y, Y, Y, Y},	// 103: Y
				{Z, Z, Z, Z},	// 104: Z
				
				{A_C, A_C, A_C, A_C},	// 105: A_C
				{B_C, B_C, B_C, B_C},	// 106: B_C
				{C_C, C_C, C_C, C_C},	// 107: C_C
				{D_C, D_C, D_C, D_C},	// 108: D_C
				{E_C, E_C, E_C, E_C},	// 109: E_C
				{F_C, F_C, F_C, F_C},	// 110: F_C
				{G_C, G_C, G_C, G_C},	// 111: G_C
				{H_C, H_C, H_C, H_C},	// 112: H_C
				{I_C, I_C, I_C, I_C},	// 113: I_C
				{J_C, J_C, J_C, J_C},	// 114: J_C
				{K_C, K_C, K_C, K_C},	// 115: K_C
				{L_C, L_C, L_C, L_C},	// 116: L_C
				{M_C, M_C, M_C, M_C},	// 117: M_C
				{N_C, N_C, N_C, N_C},	// 118: N_C
				{O_C, O_C, O_C, O_C},	// 119: O_C
				{P_C, P_C, P_C, P_C},	// 120: P_C
				{Q_C, Q_C, Q_C, Q_C},	// 121: Q_C
				{R_C, R_C, R_C, R_C},	// 122: R_C
				{S_C, S_C, S_C, S_C},	// 123: S_C
				{T_C, T_C, T_C, T_C},	// 124: T_C
				{U_C, U_C, U_C, U_C},	// 125: U_C
				{V_C, V_C, V_C, V_C},	// 126: V_C
				{W_C, W_C, W_C, W_C},	// 127: W_C
				{X_C, X_C, X_C, X_C},	// 128: X_C
				{Y_C, Y_C, Y_C, Y_C},	// 129: Y_C
				{Z_C, Z_C, Z_C, Z_C},	// 130: Z_C
				
				{QUESTION_EN, QUESTION_EN, QUESTION_EN, QUESTION_EN},	// 131: QUESTION_EN
				{ENTER, ENTER, ENTER, ENTER}, 	// 132: ENTER
				{RIGHT_ALIGNMENT, RIGHT_ALIGNMENT, RIGHT_ALIGNMENT, RIGHT_ALIGNMENT}, 	// 133: RIGHT_ALIGNMENT '→'
				{CENTER_ALIGNMENT, CENTER_ALIGNMENT, CENTER_ALIGNMENT, CENTER_ALIGNMENT}, 	// 134: RIGHT_ALIGNMENT '↔'
				{LEFT_ALIGNMENT, LEFT_ALIGNMENT, LEFT_ALIGNMENT, LEFT_ALIGNMENT}, 	// 135: RIGHT_ALIGNMENT '←'

				{ZERO_FA, ZERO_FA, ZERO_FA, ZERO_FA},
				{ONE_FA, ONE_FA, ONE_FA, ONE_FA},
				{TWO_FA, TWO_FA, TWO_FA, TWO_FA},
				{THREE_FA, THREE_FA, THREE_FA, THREE_FA},
				{FOUR_FA, FOUR_FA, FOUR_FA, FOUR_FA},
				{FIVE_FA, FIVE_FA, FIVE_FA, FIVE_FA},
				{SIX_FA, SIX_FA, SIX_FA, SIX_FA},
				{SEVEN_FA, SEVEN_FA, SEVEN_FA, SEVEN_FA},
				{EIGHT_FA, EIGHT_FA, EIGHT_FA, EIGHT_FA},
				{NINE_FA, NINE_FA, NINE_FA, NINE_FA},

				{ZERO_FA, ZERO_FA, ZERO_FA, ZERO_FA},
				{ONE_FA, ONE_FA, ONE_FA, ONE_FA},
				{TWO_FA, TWO_FA, TWO_FA, TWO_FA},
				{THREE_FA, THREE_FA, THREE_FA, THREE_FA},
				{FOUR_FA, FOUR_FA, FOUR_FA, FOUR_FA},
				{FIVE_FA, FIVE_FA, FIVE_FA, FIVE_FA},
				{SIX_FA, SIX_FA, SIX_FA, SIX_FA},
				{SEVEN_FA, SEVEN_FA, SEVEN_FA, SEVEN_FA},
				{EIGHT_FA, EIGHT_FA, EIGHT_FA, EIGHT_FA},
				{NINE_FA, NINE_FA, NINE_FA, NINE_FA}
	 };
	
	private Map<Integer, Character> indexesOfFarsiChars ;
	    
	private void init() {
		indexesOfFarsiChars = new HashMap<Integer, Character>();
		
		// 0  
		indexesOfFarsiChars.put(0, ' ');
		// 1 ♀
		indexesOfFarsiChars.put(1, '♀');
		// 2 /
		indexesOfFarsiChars.put(2, '/');
		// 3 ؟
		indexesOfFarsiChars.put(3, '؟');
		// 4 >
		indexesOfFarsiChars.put(4, '>');
		// 5 <
		indexesOfFarsiChars.put(5, '<');
		// 6 =
		indexesOfFarsiChars.put(6, '=');
		// 7 -
		indexesOfFarsiChars.put(7, '-');
		// 8 +
		indexesOfFarsiChars.put(8, '+');
		// 9 !
		indexesOfFarsiChars.put(9, '!');
		// 10 (
		indexesOfFarsiChars.put(10, '(');
		// 11 )
		indexesOfFarsiChars.put(11, ')');
		// 12 :
		indexesOfFarsiChars.put(12, ':');
		// 13 ;
		indexesOfFarsiChars.put(13, ';');
		// 14 .
		indexesOfFarsiChars.put(14, '.');
		// 15 ⌂
		indexesOfFarsiChars.put(15, '⌂');
		// 16 `
		indexesOfFarsiChars.put(16, '`');
		// 17 "
		indexesOfFarsiChars.put(17, '"');
		// 18 ~
		indexesOfFarsiChars.put(18, '~');
		// 19 @
		indexesOfFarsiChars.put(19, '@');
		// 20 #
		indexesOfFarsiChars.put(20, '#');
		// 21 %
		indexesOfFarsiChars.put(21, '%');
		// 22 ^
		indexesOfFarsiChars.put(22, '^');
		// 23 &
		indexesOfFarsiChars.put(23, '&');
		// 24 *
		indexesOfFarsiChars.put(24, '*');
		// 25 _
		indexesOfFarsiChars.put(25, '_');
		// 26 ,
		indexesOfFarsiChars.put(26, ',');
		// 27 ،
		indexesOfFarsiChars.put(27, '،');
		// 28 |
		indexesOfFarsiChars.put(28, '|');
		// 29 √
		indexesOfFarsiChars.put(29, '√');
		// 30 ☼
		indexesOfFarsiChars.put(30, '☼');
		// 31 ∙
		indexesOfFarsiChars.put(31, '∙');
		// 32 «
		indexesOfFarsiChars.put(32, '«');
		// 33 »
		indexesOfFarsiChars.put(33, '»');
		// 34 0
		indexesOfFarsiChars.put(34, '0');
		// 35 1
		indexesOfFarsiChars.put(35, '1');
		// 36 2
		indexesOfFarsiChars.put(36, '2');
		// 37 3
		indexesOfFarsiChars.put(37, '3');
		// 38 4
		indexesOfFarsiChars.put(38, '4');
		// 39 5
		indexesOfFarsiChars.put(39, '5');
		// 40 6
		indexesOfFarsiChars.put(40, '6');
		// 41 7
		indexesOfFarsiChars.put(41, '7');
		// 42 8
		indexesOfFarsiChars.put(42, '8');
		// 43 9
		indexesOfFarsiChars.put(43, '9');
		// 44 أ
		indexesOfFarsiChars.put(47, 'أ');
		indexesOfFarsiChars.put(44, 'أ');
		indexesOfFarsiChars.put(46, 'أ');
		indexesOfFarsiChars.put(45, 'أ');
		// 45 ا
		indexesOfFarsiChars.put(51, 'ا');
		indexesOfFarsiChars.put(48, 'ا');
		indexesOfFarsiChars.put(50, 'ا');
		indexesOfFarsiChars.put(49, 'ا');
		// 46 آ
		indexesOfFarsiChars.put(55, 'آ');
		indexesOfFarsiChars.put(52, 'آ');
		indexesOfFarsiChars.put(54, 'آ');
		indexesOfFarsiChars.put(53, 'آ');
		// 47 ب
		indexesOfFarsiChars.put(59, 'ب');
		indexesOfFarsiChars.put(56, 'ب');
		indexesOfFarsiChars.put(58, 'ب');
		indexesOfFarsiChars.put(57, 'ب');
		// 48 پ
		indexesOfFarsiChars.put(63, 'پ');
		indexesOfFarsiChars.put(60, 'پ');
		indexesOfFarsiChars.put(62, 'پ');
		indexesOfFarsiChars.put(61, 'پ');
		// 49 ت
		indexesOfFarsiChars.put(67, 'ت');
		indexesOfFarsiChars.put(64, 'ت');
		indexesOfFarsiChars.put(66, 'ت');
		indexesOfFarsiChars.put(65, 'ت');
		// 50 ث
		indexesOfFarsiChars.put(71, 'ث');
		indexesOfFarsiChars.put(68, 'ث');
		indexesOfFarsiChars.put(70, 'ث');
		indexesOfFarsiChars.put(69, 'ث');
		// 51 ج
		indexesOfFarsiChars.put(75, 'ج');
		indexesOfFarsiChars.put(72, 'ج');
		indexesOfFarsiChars.put(74, 'ج');
		indexesOfFarsiChars.put(73, 'ج');
		// 52 چ
		indexesOfFarsiChars.put(79, 'چ');
		indexesOfFarsiChars.put(76, 'چ');
		indexesOfFarsiChars.put(78, 'چ');
		indexesOfFarsiChars.put(77, 'چ');
		// 53 ح
		indexesOfFarsiChars.put(83, 'ح');
		indexesOfFarsiChars.put(80, 'ح');
		indexesOfFarsiChars.put(82, 'ح');
		indexesOfFarsiChars.put(81, 'ح');
		// 54 خ
		indexesOfFarsiChars.put(87, 'خ');
		indexesOfFarsiChars.put(84, 'خ');
		indexesOfFarsiChars.put(86, 'خ');
		indexesOfFarsiChars.put(85, 'خ');
		// 55 د
		indexesOfFarsiChars.put(91, 'د');
		indexesOfFarsiChars.put(88, 'د');
		indexesOfFarsiChars.put(90, 'د');
		indexesOfFarsiChars.put(89, 'د');
		// 56 ذ
		indexesOfFarsiChars.put(95, 'ذ');
		indexesOfFarsiChars.put(92, 'ذ');
		indexesOfFarsiChars.put(94, 'ذ');
		indexesOfFarsiChars.put(93, 'ذ');
		// 57 ر
		indexesOfFarsiChars.put(99, 'ر');
		indexesOfFarsiChars.put(96, 'ر');
		indexesOfFarsiChars.put(98, 'ر');
		indexesOfFarsiChars.put(97, 'ر');
		// 58 ز
		indexesOfFarsiChars.put(103, 'ز');
		indexesOfFarsiChars.put(100, 'ز');
		indexesOfFarsiChars.put(102, 'ز');
		indexesOfFarsiChars.put(101, 'ز');
		// 59 ژ
		indexesOfFarsiChars.put(107, 'ژ');
		indexesOfFarsiChars.put(104, 'ژ');
		indexesOfFarsiChars.put(106, 'ژ');
		indexesOfFarsiChars.put(105, 'ژ');
		// 60 س
		indexesOfFarsiChars.put(111, 'س');
		indexesOfFarsiChars.put(108, 'س');
		indexesOfFarsiChars.put(110, 'س');
		indexesOfFarsiChars.put(109, 'س');
		// 61 ش
		indexesOfFarsiChars.put(115, 'ش');
		indexesOfFarsiChars.put(112, 'ش');
		indexesOfFarsiChars.put(114, 'ش');
		indexesOfFarsiChars.put(113, 'ش');
		// 62 ص
		indexesOfFarsiChars.put(119, 'ص');
		indexesOfFarsiChars.put(116, 'ص');
		indexesOfFarsiChars.put(118, 'ص');
		indexesOfFarsiChars.put(117, 'ص');
		// 63 ض
		indexesOfFarsiChars.put(123, 'ض');
		indexesOfFarsiChars.put(120, 'ض');
		indexesOfFarsiChars.put(122, 'ض');
		indexesOfFarsiChars.put(121, 'ض');
		// 64 ط
		indexesOfFarsiChars.put(127, 'ط');
		indexesOfFarsiChars.put(124, 'ط');
		indexesOfFarsiChars.put(126, 'ط');
		indexesOfFarsiChars.put(125, 'ط');
		// 65 ظ
		indexesOfFarsiChars.put(131, 'ظ');
		indexesOfFarsiChars.put(128, 'ظ');
		indexesOfFarsiChars.put(130, 'ظ');
		indexesOfFarsiChars.put(129, 'ظ');
		// 66 ع
		indexesOfFarsiChars.put(135, 'ع');
		indexesOfFarsiChars.put(132, 'ع');
		indexesOfFarsiChars.put(134, 'ع');
		indexesOfFarsiChars.put(133, 'ع');
		// 67 غ
		indexesOfFarsiChars.put(139, 'غ');
		indexesOfFarsiChars.put(136, 'غ');
		indexesOfFarsiChars.put(138, 'غ');
		indexesOfFarsiChars.put(137, 'غ');
		// 68 ف
		indexesOfFarsiChars.put(143, 'ف');
		indexesOfFarsiChars.put(140, 'ف');
		indexesOfFarsiChars.put(142, 'ف');
		indexesOfFarsiChars.put(141, 'ف');
		// 69 ق
		indexesOfFarsiChars.put(147, 'ق');
		indexesOfFarsiChars.put(144, 'ق');
		indexesOfFarsiChars.put(146, 'ق');
		indexesOfFarsiChars.put(145, 'ق');
		// 70 ک
		indexesOfFarsiChars.put(151, 'ک');
		indexesOfFarsiChars.put(148, 'ک');
		indexesOfFarsiChars.put(150, 'ک');
		indexesOfFarsiChars.put(149, 'ک');
		// 71 گ
		indexesOfFarsiChars.put(155, 'گ');
		indexesOfFarsiChars.put(152, 'گ');
		indexesOfFarsiChars.put(154, 'گ');
		indexesOfFarsiChars.put(153, 'گ');
		// 72 ل
		indexesOfFarsiChars.put(159, 'ل');
		indexesOfFarsiChars.put(156, 'ل');
		indexesOfFarsiChars.put(158, 'ل');
		indexesOfFarsiChars.put(157, 'ل');
		// 73 م
		indexesOfFarsiChars.put(163, 'م');
		indexesOfFarsiChars.put(160, 'م');
		indexesOfFarsiChars.put(162, 'م');
		indexesOfFarsiChars.put(161, 'م');
		// 74 ن
		indexesOfFarsiChars.put(167, 'ن');
		indexesOfFarsiChars.put(164, 'ن');
		indexesOfFarsiChars.put(166, 'ن');
		indexesOfFarsiChars.put(165, 'ن');
		// 75 و
		indexesOfFarsiChars.put(171, 'و');
		indexesOfFarsiChars.put(168, 'و');
		indexesOfFarsiChars.put(170, 'و');
		indexesOfFarsiChars.put(169, 'و');
		// 76 ه
		indexesOfFarsiChars.put(175, 'ه');
		indexesOfFarsiChars.put(172, 'ه');
		indexesOfFarsiChars.put(174, 'ه');
		indexesOfFarsiChars.put(173, 'ه');
		// 77 ی
		indexesOfFarsiChars.put(179, 'ی');
		indexesOfFarsiChars.put(176, 'ی');
		indexesOfFarsiChars.put(178, 'ی');
		indexesOfFarsiChars.put(177, 'ی');
		// 78 ئ
		indexesOfFarsiChars.put(183, 'ئ');
		indexesOfFarsiChars.put(180, 'ئ');
		indexesOfFarsiChars.put(182, 'ئ');
		indexesOfFarsiChars.put(181, 'ئ');
		// 79 a
		indexesOfFarsiChars.put(184, 'a');
		// 80 b
		indexesOfFarsiChars.put(185, 'b');
		// 81 c
		indexesOfFarsiChars.put(186, 'c');
		// 82 d
		indexesOfFarsiChars.put(187, 'd');
		// 83 e
		indexesOfFarsiChars.put(188, 'e');
		// 84 f
		indexesOfFarsiChars.put(189, 'f');
		// 85 g
		indexesOfFarsiChars.put(190, 'g');
		// 86 h
		indexesOfFarsiChars.put(191, 'h');
		// 87 i
		indexesOfFarsiChars.put(192, 'i');
		// 88 j
		indexesOfFarsiChars.put(193, 'j');
		// 89 k
		indexesOfFarsiChars.put(194, 'k');
		// 90 l
		indexesOfFarsiChars.put(195, 'l');
		// 91 m
		indexesOfFarsiChars.put(196, 'm');
		// 92 n
		indexesOfFarsiChars.put(197, 'n');
		// 93 o
		indexesOfFarsiChars.put(198, 'o');
		// 94 p
		indexesOfFarsiChars.put(199, 'p');
		// 95 q
		indexesOfFarsiChars.put(200, 'q');
		// 96 r
		indexesOfFarsiChars.put(201, 'r');
		// 97 s
		indexesOfFarsiChars.put(202, 's');
		// 98 t
		indexesOfFarsiChars.put(203, 't');
		// 99 u
		indexesOfFarsiChars.put(204, 'u');
		// 100 v
		indexesOfFarsiChars.put(205, 'v');
		// 101 w
		indexesOfFarsiChars.put(206, 'w');
		// 102 x
		indexesOfFarsiChars.put(207, 'x');
		// 103 y
		indexesOfFarsiChars.put(208, 'y');
		// 104 z
		indexesOfFarsiChars.put(209, 'z');
		// 105 A
		indexesOfFarsiChars.put(210, 'A');
		// 106 B
		indexesOfFarsiChars.put(211, 'B');
		// 107 C
		indexesOfFarsiChars.put(212, 'C');
		// 108 D
		indexesOfFarsiChars.put(213, 'D');
		// 109 E
		indexesOfFarsiChars.put(214, 'E');
		// 110 F
		indexesOfFarsiChars.put(215, 'F');
		// 111 G
		indexesOfFarsiChars.put(216, 'G');
		// 112 H
		indexesOfFarsiChars.put(217, 'H');
		// 113 I
		indexesOfFarsiChars.put(218, 'I');
		// 114 J
		indexesOfFarsiChars.put(219, 'J');
		// 115 K
		indexesOfFarsiChars.put(220, 'K');
		// 116 L
		indexesOfFarsiChars.put(221, 'L');
		// 117 M
		indexesOfFarsiChars.put(222, 'M');
		// 118 N
		indexesOfFarsiChars.put(223, 'N');
		// 119 O
		indexesOfFarsiChars.put(224, 'O');
		// 120 P
		indexesOfFarsiChars.put(225, 'P');
		// 121 Q
		indexesOfFarsiChars.put(226, 'Q');
		// 122 R
		indexesOfFarsiChars.put(227, 'R');
		// 123 S
		indexesOfFarsiChars.put(228, 'S');
		// 124 T
		indexesOfFarsiChars.put(229, 'T');
		// 125 U
		indexesOfFarsiChars.put(230, 'U');
		// 126 V
		indexesOfFarsiChars.put(231, 'V');
		// 127 W
		indexesOfFarsiChars.put(232, 'W');
		// 128 X
		indexesOfFarsiChars.put(233, 'X');
		// 129 Y
		indexesOfFarsiChars.put(234, 'Y');
		// 130 Z
		indexesOfFarsiChars.put(235, 'Z');
		// 131 ?
		indexesOfFarsiChars.put(236, '?');
		// 132 ¶
		indexesOfFarsiChars.put(237, '¶');
		// 133 →
		indexesOfFarsiChars.put(238, '→');
		// 134 ↔
		indexesOfFarsiChars.put(239, '↔');
		// 135 ←
		indexesOfFarsiChars.put(240, '←');
	}

	@Override
	public Map<Integer, Integer> getEndingChars() {
		return new HashMap<Integer, Integer>();
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
	protected int indexOfSpace() {
		return getFarsiCharsConnectingInstances()[0][0];
	}

	@Override
	public boolean isInNumberRange(byte b) {
		return b >= (byte)34 && b <= (byte) 43;
	}

	@Override
	public boolean isSpecialCharacter(int code) {
		if (code == 237 || code == 238 || code == 239 || code== 240)
			return true;
		return false;
	}

	@Override
	public String processSpecialCharacter(int code) {
		if (code == 237)
			return "\r\n";
		
		return ""; 
	}

	@Override
	protected String preProcess(String s) {
		s = super.preProcess(s);
		s = s.replaceAll("\n", "¶");
		s = s.replaceAll("\\[L\\]", "→");
		s = s.replaceAll("\\[R\\]", "←");
		s = s.replaceAll("\\[C\\]", "↔");
		s = s.replaceAll("null", "");
		s = s.replaceAll("NULL", "");
		return s;
	}
	
	public byte[] convert(String s, Ifx ifx, FinancialEntity fe, Terminal terminal) {
		s = convertStr(s, ifx, fe, terminal);
		return encode(s);
	}
	
	public String convertStr(String s ,Ifx ifx, FinancialEntity fe, Terminal terminal){
		if (!Util.hasText(s))
			s = " ";
		else {
			String dailyMessage = "";
			TerminalSharedFeature tsf = terminal.getParentGroup().getSharedFeature();
			if (tsf != null && Util.hasText(tsf.getDailyMessage()))
				dailyMessage = tsf.getDailyMessage();
			Binding binding = new Binding();
			binding.setProperty("ifx", ifx);
			binding.setProperty("trm", terminal);
			binding.setProperty("fe", fe);
			binding.setProperty("dailyMsg", dailyMessage);
			Object run = GlobalContext.getInstance().evaluateScript(s, binding);
			s = run.toString();
		}
		return s;
	}

	@Override
	protected boolean isConnectingToNext(char c) {
		if(c=='↔' || c=='¶' || c=='→' || c=='←')
			return false;
		return super.isConnectingToNext(c);
	}
	
	@Override
	protected boolean isConnectingToPrev(char c) {
		if(c=='↔' || c=='¶' || c=='→' || c=='←')
			return false;
		return super.isConnectingToPrev(c);
	}
	
	private Map<Character, Character> mapToStandardChar = new HashMap<Character, Character>() {
		{
			put('ي', 'ی');
//			put('ئ', 'ء');
			put('ك', 'ک');
			put('\n', '%');
		}
	};
	
	@Override
	public Map<Character, Character> getMapToStandardChar() {
		return mapToStandardChar;
	}
}
