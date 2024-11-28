package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

public class KeySpec51 extends KeySpecifier {

    KeySpecifier KMC;
    byte[] cardUniqueData;
    byte cardMethod;
    byte[] sessionData;
    byte sessionMethod;

    public KeySpec51(KeySpecifier KMC, byte[] cardUniqueData,
                     byte[] sessionData, int sessionMethod) {
        format = 0x51;
        this.KMC = KMC;
        this.cardUniqueData = cardUniqueData;
        this.cardMethod = 0x01;
        this.sessionData = sessionData;
        this.sessionMethod = (byte) sessionMethod;

    }

    public KeySpec51(byte[] response, int offset) {
        format = 0x51;
        parseByteArray(response, offset);
    }

    public byte[] getByteArray() {

        byte[] kmcArr = KMC.getByteArray();
        byte[] b = new byte[kmcArr.length + 16 + 1 + 16 + 1];
        int index = 0;

        b[index++] = format;

        System.arraycopy(kmcArr, 0, b, index, kmcArr.length);
        index += kmcArr.length;
//        for (int i = 0; i < kmcArr.length; i++)
//            b[index++] = kmcArr[i];

        System.arraycopy(cardUniqueData, 0, b, index, cardUniqueData.length);
        index += cardUniqueData.length;
//        for (int i = 0; i < cardUniqueData.length; i++)
//            b[index++] = cardUniqueData[i];

        b[index++] = cardMethod;

        System.arraycopy(sessionData, 0, b, index, sessionData.length);
        index += sessionData.length;
//        for (int i = 0; i < sessionData.length; i++)
//            b[index++] = sessionData[i];

        b[index++] = sessionMethod;

        return HSMUtil.buildVarLengthParam(b);


    }

    public int parseByteArray(byte[] response, int offset) {
        MyInteger ofset = new MyInteger(offset);
        HSMUtil.getLengthOfVarField(response, ofset);
        offset = ofset.value;

        if (format != response[offset++]) {
            KMC = null;
            cardUniqueData = sessionData = null;
            cardMethod = sessionMethod = -1;
            return -1;
        }

        KMC = KeyHandler.getKeySpec(response, offset);
        MyInteger ofsetKMC = new MyInteger(offset);
        int lenKMC = HSMUtil.getLengthOfVarField(response, ofsetKMC);
        offset = ofsetKMC.value;
        offset += lenKMC;

        System.arraycopy(response, offset, cardUniqueData, 0, cardUniqueData.length);
        offset += cardUniqueData.length;
//        for ( int i = 0 ;i < cardUniqueData.length ; i++)
//             cardUniqueData[i] = response[offset++];

        cardMethod = response[offset++];

        System.arraycopy(response, offset, sessionData, 0, sessionData.length);
        offset += sessionData.length;
//        for ( int i = 0 ;i < sessionData.length ; i++)
//             sessionData[i] = response[offset++];

        sessionMethod = response[offset];

        return 1;
    }
}
