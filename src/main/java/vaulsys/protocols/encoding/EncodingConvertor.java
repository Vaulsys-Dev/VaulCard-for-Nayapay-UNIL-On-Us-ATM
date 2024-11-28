/**
 * 
 */
package vaulsys.protocols.encoding;

public interface EncodingConvertor {
	 public byte[] encode(String s);
	 
	 public String decode(byte[] bytes);
	 
	 public byte[] finalize(byte[] converted, String encoding, String extendedEncoding);
}
