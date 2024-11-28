package vaulsys.security.hsm.eracom.zka;

public class ZKA_PIN_TRANS_Resp {
    public byte[] ePPKo;
    public byte[] RNDo;

    public ZKA_PIN_TRANS_Resp(byte[] result, int offset) {

        if (result[offset++] != 0)
            return;
        ePPKo = new byte[8];
        RNDo = new byte[16];
        for (int i = 0; i < 8; i++)
            ePPKo[i] = result[offset++];
        for (int i = 0; i < 16; i++)
            RNDo[i] = result[offset++];
    }
}
