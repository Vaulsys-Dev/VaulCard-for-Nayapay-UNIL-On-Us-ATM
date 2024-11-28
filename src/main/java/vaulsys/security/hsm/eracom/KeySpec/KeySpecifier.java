package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;


public abstract class KeySpecifier {
    public int length;
    public byte format;

    public abstract byte[] getByteArray();

    public abstract int parseByteArray(byte[] response, int offset);

    @Override
    public String toString() {
        return HSMUtil.byteToString(new byte[]{format});
    }
}
