package vaulsys.protocols.encoding.impl;

import java.util.Map;

public class UTF8FarsiConvertor extends FarsiConvertor {

	@Override
	public byte[] encode(String s) {
		return s.getBytes();
	}
	
	@Override
	public String decode(byte[] bytes) {
			return new String(bytes);
	}
	
	@Override
	public int[] getFarsiCharsCompleter() {
		return null;
	}

	@Override
	public int[][] getFarsiCharsConnectingInstances() {
		return null;
	}

	@Override
	public char[] getFarsiCharsIndexes() {
		return null;
	}

	@Override
	public Map<Integer, Character> getIndexesOfFarsiChars() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInNumberRange(byte b) {
		return b >= '0' && b <= '9';
	}

	@Override
	protected int indexOfSpace() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Map<Integer, Integer> getEndingChars() {
		return null;
	}

	@Override
	public boolean isSpecialCharacter(int code) {
		return false;
	}

	@Override
	public String processSpecialCharacter(int code) {
		return "";
	}
	
}
