package vaulsys.security.hsm.eracom.Transfer;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;


public class HSMKeyExport {

    public byte returnCode;
    public byte[] encryp_Key;
    public byte[] kvc;

    public HSMKeyExport(byte[] result, int offset) {

        if ((returnCode = result[offset++]) != 0) {
            //an error occured so all the variables are set to zero
            encryp_Key = null;
            kvc = null;
            return;
        }

        MyInteger ofset = new MyInteger(offset);
        int length = HSMUtil.getLengthOfVarField(result, ofset);
        offset = ofset.value;

        encryp_Key = new byte[length];

        for (int counter = 0; counter < length; counter++)
            encryp_Key[counter] = result[offset++];

        kvc = new byte[3];
        for (int i = 0; i < kvc.length; i++)
            kvc[i] = result[offset++];
    }

    public String toString() {
        String stroutput = "";
        stroutput += "return code:+" + returnCode + "encryp_Key: " + encryp_Key + " kvc: " + kvc;
        return stroutput;
    }


}
