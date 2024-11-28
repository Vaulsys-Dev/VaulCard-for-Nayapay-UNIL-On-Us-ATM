package vaulsys.security.hsm.eracom.Transfer;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

public class HsmRetrieveKey {
    public byte returnCode;
    public byte keyType;
    public String keySpec;
    public int kvc;

    public HsmRetrieveKey(byte[] result, int offset) {
        if ((returnCode = result[offset++]) != 0) {
            //an error occured so all the variables are set to zero
            keyType = -1;
            keySpec = "";
            kvc = -1;
            return;
        }

        keyType = result[offset++];

        MyInteger ofset = new MyInteger(offset);
        int length = HSMUtil.getLengthOfVarField(result, ofset);
        offset = ofset.value;

        for (int counter = 0; counter < length; counter++)
            keySpec += (char) result[offset++];

        kvc = result[offset++];

    }

    public String toString() {
        String stroutput = "";
        stroutput +=
                "keyType: " + keyType + " keySpec: " + keySpec + " kvc: " +
                        kvc;


        return stroutput;
    }
}
