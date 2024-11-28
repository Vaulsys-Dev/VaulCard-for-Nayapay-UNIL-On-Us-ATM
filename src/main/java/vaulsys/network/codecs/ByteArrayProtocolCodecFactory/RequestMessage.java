package vaulsys.network.codecs.ByteArrayProtocolCodecFactory;

import java.util.List;

/**
 * Created by Raza on 03-Aug-18.
 */
public class RequestMessage {

    private byte[] CumulativeBytes;
    private int msglength;
    private int lastmsgcount;


    public static List<RequestMessage> pendingmsglist;
    public static RequestMessage rm;

    public RequestMessage(byte[] in)
    {
        CumulativeBytes = in;
        msglength = 0;
        setLastmsgcount(0);
    }

    public byte[] getCumulativeBytes() {
        return CumulativeBytes;
    }

    public void setCumulativeBytes(byte[] cumulativeBytes) {
        CumulativeBytes = cumulativeBytes;
    }

    public int getMsglength() {
        return msglength;
    }

    public void setMsglength(int msglength) {
        this.msglength = msglength;
    }

    public int getLastmsgcount() {
        return lastmsgcount;
    }

    public void setLastmsgcount(int lastmsgcount) {
        if(this.lastmsgcount == 0) {
            this.lastmsgcount = lastmsgcount;
        }
        else
        {
            this.lastmsgcount += lastmsgcount;
        }
    }
}
