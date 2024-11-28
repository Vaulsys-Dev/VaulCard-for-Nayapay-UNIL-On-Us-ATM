package vaulsys.security.hsm.eracom.RemoteATM;

import vaulsys.security.hsm.eracom.KeySpec.KeyHandler;
import vaulsys.security.hsm.eracom.KeySpec.KeySpecifier;
import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

public class Generate_RSA_Key_Pair_Resp {
    public KeySpecifier PK;
    public KeySpecifier SK;

    public Generate_RSA_Key_Pair_Resp(byte[] result, int offset) {

        if (result[offset++] != 0)
            return;

        PK = KeyHandler.getKeySpec(result, offset);

        MyInteger ofset1 = new MyInteger(offset);
        int length = HSMUtil.getLengthOfVarField(result, ofset1);
        offset = ofset1.value;

        offset += length;

        PK = KeyHandler.getKeySpec(result, offset);
/*        
        MyInteger ofset2 = new MyInteger(offset);
        int length2 = HSMUtil.getLengthOfVarField(result, ofset2);
        offset = ofset2.value;
        
        offset += length2;
*/
    }
}
