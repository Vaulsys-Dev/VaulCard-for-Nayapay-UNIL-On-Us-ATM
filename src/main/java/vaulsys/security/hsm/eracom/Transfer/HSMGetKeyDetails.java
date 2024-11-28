package vaulsys.security.hsm.eracom.Transfer;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;


public class HSMGetKeyDetails {
    public byte returnCode;
    public byte parity;
    public byte[] kvc;

    public HSMGetKeyDetails(byte[] result, int offset) {

        if ((returnCode = result[offset++]) != 0) {
            //an error occured so all the variables are set to zero
            parity = 0;
            kvc = null;
            return;
        }

        parity = result[offset++];

        MyInteger ofset = new MyInteger(offset);
        int length = HSMUtil.getLengthOfVarField(result, ofset);
        offset = ofset.value;
        kvc = new byte[length];

        for (int i = 0; i < kvc.length; i++)
            kvc[i] = result[offset++];


    }

    public String toString() {
        String stroutput = "";
        stroutput +=
                "return code:+" + returnCode + "parity: " + parity +
                        " kvc: " + kvc;
        return stroutput;
    }


}

