package vaulsys.terminal;

import vaulsys.persistence.GeneralDao;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.terminal.impl.PINPADTerminal;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.KIOSKCardPresentTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.util.Util;
import vaulsys.util.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

public class POSTerminalService {
	private static Logger logger = Logger.getLogger(POSTerminalService.class);

    public static void addDefaultKeySetForTerminal(Terminal selectedTerminal) throws Exception {
    	
        if (selectedTerminal == null)
            return;

        String serialno = "";
        if (selectedTerminal instanceof POSTerminal)
        	serialno = ((POSTerminal)selectedTerminal).getSerialno();
        else if (selectedTerminal instanceof PINPADTerminal)
        	serialno = ((PINPADTerminal)selectedTerminal).getSerialno();
	else if (selectedTerminal instanceof KIOSKCardPresentTerminal)
                serialno = ((KIOSKCardPresentTerminal)selectedTerminal).getSerialno();
        

        if(!Util.hasText(serialno))
        	return;
        
        try {
//        	String serialno = selectedTerminal.getSerialno();
        	if(serialno.length() > 15)
        		serialno = serialno.substring(serialno.length()-15, serialno.length());
            byte[] serialNumberBytesIncomplete = serialno.getBytes();
            byte[] serialNumberBytes = new byte[16];

            for (int i = 0; i < serialNumberBytes.length; ++i)
                serialNumberBytes[i] = 0;

            int j = serialNumberBytesIncomplete.length-1;
            for (int i = serialNumberBytes.length-2; i>=0 && j >= 0; i--,j--)
            	serialNumberBytes[i] = serialNumberBytesIncomplete[j];

            //            for (int i = 5; i < 15; ++i)
//                serialNumberBytes[i] = serialNumberBytesIncomplete[i - 5];

            serialNumberBytes[15] = 0;

            byte[] masterKeyBytes = {1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8};
            byte[] xorResult = XOR(serialNumberBytes, masterKeyBytes);

            byte[] newBigKey = Hex.decode(SHA1(xorResult));
            byte[] newMACKey = new byte[8];
            byte[] newPINKey = new byte[8];

            for (int i = 0; i < 8; i++) {
                newMACKey[i] = newBigKey[2 * i];
                newPINKey[i] = newBigKey[2 * i + 1];
            }

            SecureDESKey MPKKey = SecurityComponent.importKey(SMAdapter.LENGTH_DES, KeyType.TYPE_TAK, newMACKey, null, false);
            SecureDESKey PPKKey = SecurityComponent.importKey(SMAdapter.LENGTH_DES, KeyType.TYPE_TPK, newPINKey, null, false);
            selectedTerminal.addSecureKey(MPKKey);
            selectedTerminal.addSecureKey(PPKKey);
            
            GeneralDao.Instance.saveOrUpdate(MPKKey);
            GeneralDao.Instance.saveOrUpdate(PPKKey);
            GeneralDao.Instance.saveOrUpdate(selectedTerminal);
        } catch(Exception e) {
        	logger.error(e,e);
        }
    }

    private static byte[] XOR(byte[] buffer1, byte[] buffer2) {

        byte[] result = new byte[buffer1.length];

        short n = (short) 0;
        while (n < buffer1.length) {
            result[n] = (byte) (buffer1[n] ^ buffer2[n]);
            n++;
        }
        return result;
    }

    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String SHA1(byte[] src) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");
        byte[] sha1hash = new byte[40];
        md.update(src);
        sha1hash = md.digest();
        return convertToHex(sha1hash);
    }
}
