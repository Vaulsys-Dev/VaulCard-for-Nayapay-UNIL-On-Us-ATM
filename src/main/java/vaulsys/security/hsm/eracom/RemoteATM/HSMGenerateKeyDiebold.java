package vaulsys.security.hsm.eracom.RemoteATM;

import vaulsys.security.hsm.eracom.KeySpec.KeyHandler;
import vaulsys.security.hsm.eracom.KeySpec.KeySpecifier;
import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

public class HSMGenerateKeyDiebold {
    public byte returnCode;
    public byte[] keytokenB1;
    public byte[] hostrandomNonce;
    public KeySpecifier keySpec;

    public HSMGenerateKeyDiebold(byte[] result, int offset) {

        if ((returnCode = result[offset++]) != 0) {
            //an error occured so all the variables are set to zero
            keytokenB1 = hostrandomNonce = null;
            keySpec = null;
            return;
        }

        MyInteger ofset1 = new MyInteger(offset);
        int len1 = HSMUtil.getLengthOfVarField(result, ofset1);
        offset = ofset1.value;
        keytokenB1 = new byte[len1];
        for (int i = 0; i < len1; i++)
            keytokenB1[i] = result[offset++];

        MyInteger ofset2 = new MyInteger(offset);
        int len2 = HSMUtil.getLengthOfVarField(result, ofset2);
        offset = ofset1.value;
        hostrandomNonce = new byte[len2];

        for (int i = 0; i < len2; i++)
            hostrandomNonce[i] = result[offset++];

        keySpec = KeyHandler.getKeySpec(result, offset);

    }

    public String toString() {
        String stroutput = "";
        stroutput +=
                "keytokenB1: " + keytokenB1 + " hostrandomNonce: " + hostrandomNonce + " keySpec: " +
                        keySpec;
        return stroutput;
    }


}