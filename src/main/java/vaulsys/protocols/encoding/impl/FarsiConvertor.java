package vaulsys.protocols.encoding.impl;

import vaulsys.protocols.encoding.EncodingConvertor;

import java.util.HashMap;
import java.util.Map;


public abstract class FarsiConvertor implements EncodingConvertor {

	@Override
	public byte[] encode(String s) {
		try {
			String rawFarsi = preProcess(s);
			int[] res = new int[2 * rawFarsi.length()];
			int resLen = 0;
			for (int i = 1; i < rawFarsi.length() - 1; i++) {
				
				try {
					int[] tmpRes = indexInFarsiChars(rawFarsi.charAt(i), isConnectingToNext(rawFarsi.charAt(i - 1)),
							isConnectingToPrev(rawFarsi.charAt(i + 1)));
					if (tmpRes[0]== -1){
						/*res[0] = -1;
						resLen = 1;
						break;*/
						res[resLen++] = indexOfSpace();
					}
					
					for (int j = 0; j < tmpRes.length; j++)
						res[resLen++] = tmpRes[j];
				} catch (Exception e) {
					/*res[0] = -1;
					resLen = 1;
					break;*/
					res[resLen++] = indexOfSpace();
				}
			}
			
			int[] realRes = new int[resLen];
			byte[] realResByte = new byte[resLen];

			System.arraycopy(res, 0, realRes, 0, resLen);

			for (int i = 0; i < realRes.length; ++i)
				realResByte[i] = (byte) realRes[i];

			return realResByte;
		} catch (Exception e) {
			return encode(" ");
		}
	}

	protected String preProcess(String s) {
		if (s == null)
			s = " ";

		s = s.replace("\r", "");
		String rawFarsi = s;
		rawFarsi = ' ' + rawFarsi + ' ';
		return rawFarsi;
	}

	protected int[] indexInFarsiChars(char c, boolean prevConn, boolean nextConn) {
		/*if (c == 'ي')
			c = 'ی';

		if (c == 'ئ')
        	c = 'ء';
		
		if (c == 'ك')
			c = 'ک';

		if (c == '\n')
			c = '%';
*/		
		Character c1 = getMapToStandardChar().get(new Character(c));
		if (c1 != null )
			c = c1.charValue();
			
		
		
		int base_char_idx;
		for (base_char_idx = 0; base_char_idx < getFarsiCharsIndexes().length; base_char_idx++)
			if (getFarsiCharsIndexes()[base_char_idx] == c)
				break;

		int firstChar = -1;
		int secondChar = -1;
		
		if (prevConn && nextConn)
			firstChar = getFarsiCharsConnectingInstances()[base_char_idx][3];
		
		else if (prevConn) {
			firstChar = getFarsiCharsConnectingInstances()[base_char_idx][2];
			if (getFarsiCharsCompleter()[base_char_idx] != -1)
				secondChar = getFarsiCharsCompleter()[base_char_idx];
		} else if (nextConn)
			firstChar = getFarsiCharsConnectingInstances()[base_char_idx][1];
		else {
			firstChar = getFarsiCharsConnectingInstances()[base_char_idx][0];
			if (getFarsiCharsCompleter()[base_char_idx] != -1)
				secondChar = getFarsiCharsCompleter()[base_char_idx];
		}

		int[] res;
		if (secondChar != -1) {
			res = new int[2];
			res[0] = firstChar;
			res[1] = secondChar;
		} else {
			res = new int[1];
			res[0] = firstChar;
		}

		return res;
	}

	protected boolean isConnectingToNext(char c) {
		if (c == '!' || c == '"' || c == '*' || c == '%' || c == ':' || c == '?' || c == ',' || c == '/' || c == 'آ'
				|| c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7'
				|| c == '8' || c == '9' || c == 'آ' || c == 'أ' || c == 'ا' || c == 'ؤ' || c == 'د' || c == 'ذ'
				|| c == 'ر' || c == 'ز' || c == 'ژ' || c == 'و' || c == ' ' || c == '~' || c == '.' || c == '-'
				|| c ==	')' || c== '(' || c=='،'
            	|| c=='A' ||
                c=='B' || c=='C' || c=='D' || c=='E' || c=='F' || c=='G' || c=='H' || c=='I' || c=='J' ||
                c=='K' || c=='L' || c=='M' || c=='N' || c=='O' || c=='P' || c=='Q' || c=='R' || c=='S' ||
                c=='T' || c=='U' || c=='V' || c=='W' || c=='X' || c=='Y' || c=='Z' || c=='a' || c=='b' ||
                c=='c' || c=='d' || c=='e' || c=='f' || c=='g' || c=='h' || c=='i' || c=='j' || c=='k' ||
                c=='l' || c=='m' || c=='n' || c=='o' || c=='p' || c=='q' || c=='r' || c=='s' || c=='t' ||
                c=='u' || c=='v' || c=='w' || c=='x' || c=='y' || c=='z'
					)
			return false;
		else
			return true;
	}

	protected boolean isConnectingToPrev(char c) {
        if (c == '!' || c == '"' || c == '*' || c == '%' || c == ':' || c == '?' || c == ',' || c == '/' || c == 'آ'
                || c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7'
                || c == '8' || c == '9' || c == ' ' || c == '-' || c == '.'
                || c ==	')' || c== '('|| c=='،'
            	|| c=='A' ||
                c=='B' || c=='C' || c=='D' || c=='E' || c=='F' || c=='G' || c=='H' || c=='I' || c=='J' ||
                c=='K' || c=='L' || c=='M' || c=='N' || c=='O' || c=='P' || c=='Q' || c=='R' || c=='S' ||
                c=='T' || c=='U' || c=='V' || c=='W' || c=='X' || c=='Y' || c=='Z' || c=='a' || c=='b' ||
                c=='c' || c=='d' || c=='e' || c=='f' || c=='g' || c=='h' || c=='i' || c=='j' || c=='k' ||
                c=='l' || c=='m' || c=='n' || c=='o' || c=='p' || c=='q' || c=='r' || c=='s' || c=='t' ||
                c=='u' || c=='v' || c=='w' || c=='x' || c=='y' || c=='z'
        )
            return false;
        else
            return true;
    }
	
	@Override
	public byte[] finalize(byte[] converted, String encoding, String extendedEncoding){
		return converted;
	}

	
	@Override
	public String decode(byte[] bytes) {
		String result = "";
		Map<Integer, Character> indexOfFarsiChars = getIndexesOfFarsiChars();
		Map<Integer, Integer> endingChars = getEndingChars();
		
		for (byte b :bytes){
			int code = ((int)b <0)? b+ 256 : b;
			if(code == indexOfSpace() && result.endsWith(" "))
				continue;
			if(isSpecialCharacter(code)){
				result += processSpecialCharacter(code);
			}else{
				if(indexOfFarsiChars.get(code) == null)
					continue;
				else
					result += indexOfFarsiChars.get(code);
			}
			if(endingChars.containsKey(code))
				result += indexOfFarsiChars.get(indexOfSpace());
		}
		
		if(result.endsWith(" "))
			result = result.substring(0, result.length()-1);
			
		return result;
	}
	
	
	abstract public  String processSpecialCharacter(int code);
	
	abstract public boolean isSpecialCharacter(int code);
	
	abstract public Map<Integer, Integer> getEndingChars();

	abstract public char[] getFarsiCharsIndexes();
	
	abstract public int[][] getFarsiCharsConnectingInstances();
	
	abstract public int[] getFarsiCharsCompleter();
	
	abstract public Map<Integer, Character> getIndexesOfFarsiChars();
	
	abstract public boolean isInNumberRange(byte b);
	
	abstract protected int indexOfSpace();
	
	private Map<Character, Character> mapToStandardChar = new HashMap<Character, Character>() {
		{
			put('ي', 'ی');
			put('ئ', 'ء');
			put('ك', 'ک');
			put('\n', '%');
		}
	};
	
	public Map<Character, Character> getMapToStandardChar() {
		return mapToStandardChar;
	}

	
}
