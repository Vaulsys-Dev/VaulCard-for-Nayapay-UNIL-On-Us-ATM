package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

public class KeySpec50 extends KeySpecifier {
    KeySpecifier KMC;
    byte[] cardUniqueData;
    byte cardMethod;

    public KeySpec50(KeySpecifier KMC, byte[] cardUniqueData) {
        format = 0x50;
        this.KMC = KMC;
        this.cardUniqueData = cardUniqueData;
        this.cardMethod = 0x01;

    }

    public KeySpec50(byte[] response, int offset) {
        format = 0x50;
        parseByteArray(response, offset);
    }

    public byte[] getByteArray() {

        byte[] kmcArr = KMC.getByteArray();
        byte[] b = new byte[1 + kmcArr.length + 16 + 1];
        int index = 0;
        b[index++] = format;

        System.arraycopy(kmcArr, 0, b, index, kmcArr.length);
        index += kmcArr.length;
//       for (int i = 0; i < kmcArr.length; i++)
//            b[index++] = kmcArr[i];

        System.arraycopy(cardUniqueData, 0, b, index, cardUniqueData.length);
        index += cardUniqueData.length;
//       for (int i = 0; i < cardUniqueData.length; i++)
//             b[index++] = cardUniqueData[i];     

        b[index++] = cardMethod;

        return HSMUtil.buildVarLengthParam(b);
    }

    public int parseByteArray(byte[] response, int offset) {
        MyInteger ofset = new MyInteger(offset);
        HSMUtil.getLengthOfVarField(response, ofset);
        offset = ofset.value;

        if (format != response[offset++]) {
            KMC = null;
            cardUniqueData = null;
            cardMethod = -1;
            return -1;
        }

        KMC = KeyHandler.getKeySpec(response, offset);
        MyInteger ofsetKMC = new MyInteger(offset);
        int lenKMC = HSMUtil.getLengthOfVarField(response, ofsetKMC);
        offset = ofsetKMC.value;
        offset += lenKMC;

        System.arraycopy(response, offset, cardUniqueData, 0, cardUniqueData.length);
        offset += cardUniqueData.length;
//        for ( int i = 0; i < cardUniqueData.length; i++)
//             cardUniqueData[i] = response[offset++];

        cardMethod = response[offset];
        return 1;

    }

}
