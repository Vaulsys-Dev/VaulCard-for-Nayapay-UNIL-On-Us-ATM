package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

public class KeySpec19 extends KeySpecifier {

    public byte dataSpecType;
    public byte encryptedPayload;
    public byte KMId;
    public byte[] payloadLength;
    public byte[] pad1;
    public byte[] bitmap;
    public byte[] authenticationCode;

    public KeySpec19(byte[] bitmap, byte[] authenticationCode) {
        format = 0x19;
        this.dataSpecType = 0x02;
        this.encryptedPayload = 0x00;
        this.KMId = 0x00;
        this.payloadLength[0] = 0x00;
        this.payloadLength[1] = 0x08;
        this.pad1[0] = 0x00;
        this.pad1[1] = 0x00;
        this.bitmap = bitmap;
        this.authenticationCode = authenticationCode;

    }

    public KeySpec19(byte[] response, int offset) {
        format = (byte) 0x19;
        parseByteArray(response, offset);
    }

    public byte[] getByteArray() {

        byte[] b = new byte[1 + 1 + 1 + 1 + 2 + 2 + 8 + 8];
        int index = 0;

        b[index++] = format;
        b[index++] = dataSpecType;
        b[index++] = encryptedPayload;
        b[index++] = KMId;
        System.arraycopy(payloadLength, 0, b, index, payloadLength.length);
        index += payloadLength.length;
//        b[index++] = payloadLength[0];
//        b[index++] = payloadLength[1];
        System.arraycopy(pad1, 0, b, index, pad1.length);
        index += pad1.length;
//        b[index++] = pad1[0];
//        b[index++] = pad1[1];

        System.arraycopy(bitmap, 0, b, index, bitmap.length);
        index += bitmap.length;
//        for(int i = 0 ; i < bitmap.length ; i++ )
//           b[i + 8] =bitmap[i];           

        System.arraycopy(authenticationCode, 0, b, index, authenticationCode.length);
//        for(int i = 0 ; i < authenticationCode.length ; i++ )
//           b[i + 8 + bitmap.length] =authenticationCode[i];

        return HSMUtil.buildVarLengthParam(b);

    }

    public int parseByteArray(byte[] response, int offset) {

        MyInteger ofset = new MyInteger(offset);
        HSMUtil.getLengthOfVarField(response, ofset);
        offset = ofset.value;

        if (format != response[offset++]) {
            dataSpecType = encryptedPayload = KMId = -1;
            payloadLength = pad1 = bitmap = authenticationCode = null;
            return -1;
        }

        dataSpecType = response[offset++];
        encryptedPayload = response[offset++];
        KMId = response[offset++];
        System.arraycopy(response, offset, payloadLength, 0, payloadLength.length);
        offset += payloadLength.length;
//        payloadLength[0] = response[offset++]; 
//        payloadLength[1] = response[offset++];
        System.arraycopy(response, offset, pad1, 0, pad1.length);
        offset += pad1.length;
//        pad1[0] = response[offset++];
//        pad1[1] = response[offset++];

        System.arraycopy(response, offset, bitmap, 0, bitmap.length);
        offset += bitmap.length;
//        for(int i = 0 ; i < 8 ;i++ ) 
//          bitmap[i] = response[offset++];

        System.arraycopy(response, offset, authenticationCode, 0, authenticationCode.length);
//        for(int i = 0 ; i < 8 ; i++)
//         authenticationCode[i] = response[offset++];

        return 1;
    }
}
