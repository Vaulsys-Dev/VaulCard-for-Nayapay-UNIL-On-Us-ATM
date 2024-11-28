package vaulsys.security.hsm.eracom.EFTTerminal;

public class KeyMailerResp {
    public byte returnCode;
    public byte[] key;

    public KeyMailerResp(byte[] result, int offset) {
        if ((returnCode = result[offset++]) != 0) {
            key = null;
        }

        int length =
                (result[offset] >= 0 ? result[offset++] : result[offset++] + 256);
        key = new byte[length];
        for (int i = 0; i < length; i++)
            key[i] = result[offset++];
    }
}
