package vaulsys.security.hsm.eracom.RemoteATM;

public class HSMGenerateMD5Hash {
    public byte returnCode;
    public byte[] bitCount;
    public byte[] hashValue;

    public HSMGenerateMD5Hash(byte[] result, int offset) {

        if ((returnCode = result[offset++]) != 0) {
            //an error occured so all the variables are set to zero
            bitCount = hashValue = null;
            return;
        }

        bitCount = new byte[8];
        for (int i = 0; i < bitCount.length; i++)
            bitCount[i] = result[offset++];

        hashValue = new byte[16];
        for (int i = 0; i < bitCount.length; i++)
            hashValue[i] = result[offset++];

    }

    public String toString() {
        String stroutput = "";
        stroutput +=
                "return code:+" + returnCode + "bitCount: " + bitCount + " hashValue: " +
                        hashValue;
        return stroutput;
    }


}
