package vaulsys.entity.impl;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

/**
 * Created by a.shehzad on 4/27/2016.
 */

@Embeddable
public class NetworkType implements IEnum {

    private static final int LOCAL_Value = 1;
    private static final int VISA_Value = 2;
    private static final int MASTERCARD_Value = 3;
    private static final int CUP_Value = 4;

    public static final NetworkType LOCAL = new NetworkType(1);
    public static final NetworkType VISA = new NetworkType(2);
    public static final NetworkType MASTERCARD = new NetworkType(3);
    public static final NetworkType CUP = new NetworkType(4);



    int NETWORK_TYPE;

    public NetworkType() {
    }

    NetworkType(int NetworkType) {
        this.NETWORK_TYPE = NetworkType;
    }

    public int getNetworkType() {
        return this.NETWORK_TYPE;
    }
    public void setNetworkType(int NetworkType) {
        this.NETWORK_TYPE = NetworkType;
    }

    public int hashCode() {
        boolean prime = true;
        byte result = 1;
        int result1 = 31 * result + this.NETWORK_TYPE;
        return result1;
    }

    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        } else if(obj == null) {
            return false;
        } else if(this.getClass() != obj.getClass()) {
            return false;
        } else {
            NetworkType other = (NetworkType)obj;
            return this.NETWORK_TYPE == other.NETWORK_TYPE;
        }
    }

    public String toString() {
        switch (NETWORK_TYPE) {
            case LOCAL_Value:
                return "Local";
            case VISA_Value:
                return "VISA";
            case MASTERCARD_Value:
                return "MASTER CARD";
            case CUP_Value:
                return "CUP";
            default:
                return "";
        }
    }

}


