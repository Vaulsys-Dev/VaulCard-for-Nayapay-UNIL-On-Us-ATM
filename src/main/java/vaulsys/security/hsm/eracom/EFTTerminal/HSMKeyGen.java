package vaulsys.security.hsm.eracom.EFTTerminal;

import vaulsys.security.hsm.eracom.KeySpec.KeySpecKVC;
import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;


public class HSMKeyGen {
    public byte returnCode;
    public byte numKey;
    public byte[] encrypSessionKey;
    public KeySpecKVC keyKVC;

    public HSMKeyGen(byte[] result, int offset) {

        if ((returnCode = result[offset++]) != 0) {
            //an error occured so all the variables are set to zero
            numKey = 0;
            encrypSessionKey = null;
            keyKVC = null;
            return;
        }

        numKey = result[offset++];

        MyInteger ofset = new MyInteger(offset);
        int length = HSMUtil.getLengthOfVarField(result, ofset);
        offset = ofset.value;
        encrypSessionKey = new byte[length];

        for (int counter = 0; counter < length; counter++)
            encrypSessionKey[counter] = result[offset++];

        keyKVC = new KeySpecKVC(result, new MyInteger(offset), 3);


    }

    public String toString() {
        String stroutput = "";
        stroutput +=
                "return code:+" + returnCode + "numKey: " + numKey + " encrypSessionKey: " +
                        encrypSessionKey + "keyKVC:" + keyKVC;
        return stroutput;
    }

}
