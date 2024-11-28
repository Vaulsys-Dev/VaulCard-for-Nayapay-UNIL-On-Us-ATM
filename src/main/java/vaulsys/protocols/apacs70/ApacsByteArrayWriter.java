package vaulsys.protocols.apacs70;

import vaulsys.protocols.apacs70.base.ApacsConstants;
import vaulsys.util.ConfigUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

public class ApacsByteArrayWriter {
	private static final Logger logger = Logger.getLogger(ApacsByteArrayWriter.class);
	private static final byte PAD = 48;
	private ByteArrayOutputStream out = new ByteArrayOutputStream();
	private int fieldNo = 0;

	public void write(Integer i, int maxLen) throws IOException {
		if(i != null)
			write(i.toString(), maxLen);
	}

	public void write(Long l, int maxLen) throws IOException {
		if(l != null)
			write(l.toString(), maxLen);
	}

	public void write(String str, int maxLen) throws IOException {
		fieldNo ++;
		if(str != null) {
			if(str.length() > maxLen) {
				logger.error(String.format("Field[%s]: max length[%s], but received length[%s]", fieldNo, maxLen, str.length()));
				writeTruncate(str.getBytes(), maxLen);
			}
			else
				out.write(str.getBytes());
		}
	}

	public void write(byte[] b, int maxLen) throws IOException {
		fieldNo ++;
		if(b != null && b.length > 0) {
			if(b.length > maxLen) {
				logger.error(String.format("Field[%s]: max length[%s], but received length[%s]", fieldNo, maxLen, b.length));
				writeTruncate(b, maxLen);
			}
			else
				out.write(b);
		}
	}

	public void writeTruncate(byte[] b, int maxLen) throws IOException {
		if(b != null && b.length > 0) {
			int max = b.length > maxLen ? maxLen : b.length;
			out.write(b, 0, max);
		}
	}

	public void writePadded(Integer i, int len, boolean padNull) throws IOException {
		writePadded(i != null ? i.toString() : null, len, padNull);
	}

	public void writePadded(Long l, int len, boolean padNull) throws IOException {
		writePadded(l != null ? l.toString() : null, len, padNull);
	}

	public void writePadded(String str, int len, boolean padNull) throws IOException {
		fieldNo ++;
		if(str != null) {
			if(str.length() > len) {
				logger.error(String.format("Field[%s]: max length[%s], but received length[%s]", fieldNo, len, str.length()));
				writeTruncate(str.getBytes(), len);
			}
			else {
				for(int i=str.length(); i<len; i++)
					out.write(PAD);
				out.write(str.getBytes());
			}
		}
		else if(ConfigUtil.getBoolean(ConfigUtil.APACS_ALWAYS_PAD_NULL) || padNull) {
			for(int i=0; i<len; i++)
				out.write(PAD);
		}
		else
			logger.warn("ApacsByteArrayWriter pad null value!");
	}

	public void write(byte b) {
		out.write(b);
	}

	public void write(ApacsConstants cns) {
		out.write(cns.getByte());
	}

	public byte[] toByteArray() {
		return out.toByteArray();
	}
}
