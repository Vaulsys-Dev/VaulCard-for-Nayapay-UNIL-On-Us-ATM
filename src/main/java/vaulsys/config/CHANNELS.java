package vaulsys.config;

/**
 * Created by a.shehzad on 6/22/2016.
 */

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

/**
 * Created by Asim Shahzad on 4/19/2016.
 */

@Embeddable
public class CHANNELS implements IEnum {

    private static final int channelSHETABOut_Value = 1;
    private static final int channelSHETABIn_Value = 2;
    private static final int channelMelliIn_Value = 3;
    private static final int CMSOut_Value = 4;
    private static final int CMSIn_Value = 5;
    private static final int channelFnpEpayOutA_Value = 6;
    private static final int channelFnpEpayInA_Value = 7;
    private static final int channelPos87InA_Value = 8;
    private static final int channelNDCProcachInA_Value = 9;
    private static final int channelUI_Value = 10;
    private static final int pos87HDLC_Value = 11;
    private static final int pos87HDLC2_Value = 12;
    private static final int posInfotechMeganac_Value = 13;
    private static final int posInfotech_Value = 14;
    private static final int posInfotechHDLC_Value = 15;
    private static final int posInfotechHDLC2_Value = 16;
    private static final int posInfotechGPRS_Value = 17;
    private static final int Apacs70_Value = 18;
    private static final int Apacs70Secure_Value = 19;
    private static final int Apacs70SecureHDLC_Value = 20;
    private static final int Apacs70GPRS_Value = 21;
    private static final int Apacs70NCC_Value = 22;
    private static final int Apacs70NCC2_Value = 23;
    private static final int Apacs70Meganac_Value = 24;
    private static final int pinpadApacs_Value = 25;
    private static final int kioskSanatRayaneh_Value = 26;
    private static final int kioskAderan_Value = 27;
    private static final int cashDeposit_Value = 28;
    private static final int UnionPay_Value = 46;
    private static final int VisaSMS_Value = 47;
	private static final int VisaBaseI_Value = 48;

    public static final CHANNELS channelSHETABOut = new CHANNELS(1);
    public static final CHANNELS channelSHETABIn = new CHANNELS(2);
    public static final CHANNELS channelMelliIn = new CHANNELS(3);
    public static final CHANNELS CMSOut = new CHANNELS(4);
    public static final CHANNELS CMSIn = new CHANNELS(5);
    public static final CHANNELS channelFnpEpayOutA = new CHANNELS(6);
    public static final CHANNELS channelFnpEpayInA = new CHANNELS(7);
    public static final CHANNELS channelPos87InA = new CHANNELS(8);
    public static final CHANNELS channelNDCProcachInA = new CHANNELS(9);
    public static final CHANNELS channelUI = new CHANNELS(10);
    public static final CHANNELS pos87HDLC = new CHANNELS(11);
    public static final CHANNELS pos87HDLC2 = new CHANNELS(12);
    public static final CHANNELS posInfotechMeganac = new CHANNELS(13);
    public static final CHANNELS posInfotech = new CHANNELS(14);
    public static final CHANNELS posInfotechHDLC = new CHANNELS(15);
    public static final CHANNELS posInfotechHDLC2 = new CHANNELS(16);
    public static final CHANNELS posInfotechGPRS = new CHANNELS(17);
    public static final CHANNELS Apacs70 = new CHANNELS(18);
    public static final CHANNELS Apacs70Secure = new CHANNELS(19);
    public static final CHANNELS Apacs70SecureHDLC = new CHANNELS(20);
    public static final CHANNELS Apacs70GPRS = new CHANNELS(21);
    public static final CHANNELS Apacs70NCC = new CHANNELS(22);
    public static final CHANNELS Apacs70NCC2 = new CHANNELS(23);
    public static final CHANNELS Apacs70Meganac = new CHANNELS(24);
    public static final CHANNELS pinpadApacs = new CHANNELS(25);
    public static final CHANNELS kioskSanatRayaneh = new CHANNELS(26);
    public static final CHANNELS kioskAderan = new CHANNELS(27);
    public static final CHANNELS cashDeposit = new CHANNELS(28);
    public static final CHANNELS UnionPay = new CHANNELS(46);
    public static final CHANNELS VisaSMS = new CHANNELS(47);
	public static final CHANNELS VisaBaseI = new CHANNELS(48);

    int CHANNEL_ID;

    public CHANNELS() {
    }

    CHANNELS(int ChannelType) {
        this.CHANNEL_ID = ChannelType;
    }

    public int getChannelType() {
        return this.CHANNEL_ID;
    }
    public void setChannelType(int ChannelType) {
        this.CHANNEL_ID = ChannelType;
    }

    public int hashCode() {
        boolean prime = true;
        byte result = 1;
        int result1 = 31 * result + this.CHANNEL_ID;
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
            CHANNELS other = (CHANNELS)obj;
            return this.CHANNEL_ID == other.CHANNEL_ID;
        }
    }

    public String toString() {
        switch(CHANNEL_ID){
            case channelSHETABOut_Value:
                return "channelSHETABOut";
            case channelSHETABIn_Value:
                return "channelSHETABIn";
            case channelMelliIn_Value:
                return "channelMelliIn";
            case CMSOut_Value:
                return "CMSOut";
            case CMSIn_Value:
                return "CMSIn";
            case channelFnpEpayOutA_Value:
                return "channelFnpEpayOutA";
            case channelFnpEpayInA_Value:
                return "channelFnpEpayInA";
            case channelPos87InA_Value:
                return "channelPos87InA";
            case channelNDCProcachInA_Value:
                return "channelNDCProcachInA";
            case channelUI_Value:
                return "channelUI";
            case pos87HDLC_Value:
                return "pos87HDLC";
            case pos87HDLC2_Value:
                return "pos87HDLC2";
            case posInfotechMeganac_Value:
                return "posInfotechMeganac";
            case posInfotech_Value:
                return "posInfotech";
            case posInfotechHDLC_Value:
                return "posInfotechHDLC";
            case posInfotechHDLC2_Value:
                return "posInfotechHDLC2";
            case posInfotechGPRS_Value:
                return "posInfotechGPRS";
            case Apacs70_Value:
                return "Apacs70";
            case Apacs70Secure_Value:
                return "Apacs70Secure";
            case Apacs70SecureHDLC_Value:
                return "Apacs70SecureHDLC";
            case Apacs70GPRS_Value:
                return "Apacs70GPRS";
            case Apacs70NCC_Value:
                return "Apacs70NCC";
            case Apacs70NCC2_Value:
                return "Apacs70NCC2";
            case Apacs70Meganac_Value:
                return "Apacs70Meganac";
            case pinpadApacs_Value:
                return "pinpadApacs";
            case kioskSanatRayaneh_Value:
                return "kioskSanatRayaneh";
            case kioskAderan_Value:
                return "kioskAderan";
            case cashDeposit_Value:
                return "cashDeposit";
            case UnionPay_Value:
                return "UnionPay";
            case VisaSMS_Value:
                return "VisaSMS";
			case VisaBaseI_Value:
                return "VisaBaseI";
            default:
                return "";
        }

    }

}

