package vaulsys.config;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

/**
 * Created by a.shehzad on 5/9/2016.
 */
@Embeddable
public class MessageTypes implements IEnum {
    private static final int AUTHORIZATION_REQUEST_VALUE = 100;
    private static final int AUTHORIZATION_RESPONSE_VALUE = 110;
    private static final int FINANCIAL_REQUEST_VALUE = 200;
    private static final int FINANCIAL_ADVICE_VALUE = 220;
    private static final int FINANCIAL_ADVICE_REPEAT_VALUE = 221;
    private static final int FINANCIAL_RESPONSE_VALUE = 210;
    private static final int FINANCIAL_ADVICE_RESPONSE_VALUE = 230;
    private static final int REVERSAL_REQUEST_VALUE = 400;
    private static final int REVERSAL_REQUEST_REPEAT_VALUE = 401;
    private static final int REVERSAL_ADVICE_VALUE = 420;
    private static final int REVERSAL_ADVICE_REPEAT_VALUE = 421;
    private static final int REVERSAL_RESPONSE_VALUE = 410;
    private static final int REVERSAL_ADVICE_RESPONSE_VALUE = 430;
    private static final int NETWORK_MANAGEMENT_REQUEST_VALUE = 800;
    private static final int NETWORK_MANAGEMENT_RESPONSE_VALUE = 810;
    private static final int NETWORK_MANAGEMENT_ADVICE_VALUE = 820;
    private static final int NETWORK_MANAGEMENT_ADVICE_RESPONSE_VALUE = 830;


    public static final MessageTypes AUTHORIZATION_REQUEST = new MessageTypes(AUTHORIZATION_REQUEST_VALUE);
    public static final MessageTypes AUTHORIZATION_RESPONSE = new MessageTypes(AUTHORIZATION_RESPONSE_VALUE);
    public static final MessageTypes FINANCIAL_REQUEST = new MessageTypes(FINANCIAL_REQUEST_VALUE);
    public static final MessageTypes FINANCIAL_ADVICE = new MessageTypes(FINANCIAL_ADVICE_VALUE);
    public static final MessageTypes FINANCIAL_ADVICE_REPEAT = new MessageTypes(FINANCIAL_ADVICE_REPEAT_VALUE);
    public static final MessageTypes FINANCIAL_RESPONSE = new MessageTypes(FINANCIAL_RESPONSE_VALUE);
    public static final MessageTypes FINANCIAL_ADVICE_RESPONSE = new MessageTypes(FINANCIAL_ADVICE_RESPONSE_VALUE);
    public static final MessageTypes REVERSAL_REQUEST = new MessageTypes(REVERSAL_REQUEST_VALUE);
    public static final MessageTypes REVERSAL_REQUEST_REPEAT = new MessageTypes(REVERSAL_REQUEST_REPEAT_VALUE);
    public static final MessageTypes REVERSAL_ADVICE = new MessageTypes(REVERSAL_ADVICE_VALUE);
    public static final MessageTypes REVERSAL_ADVICE_REPEAT = new MessageTypes(REVERSAL_ADVICE_REPEAT_VALUE);
    public static final MessageTypes REVERSAL_RESPONSE = new MessageTypes(REVERSAL_RESPONSE_VALUE);
    public static final MessageTypes REVERSAL_ADVICE_RESPONSE = new MessageTypes(REVERSAL_ADVICE_RESPONSE_VALUE);
    public static final MessageTypes NETWORK_MANAGEMENT_REQUEST = new MessageTypes(NETWORK_MANAGEMENT_REQUEST_VALUE);
    public static final MessageTypes NETWORK_MANAGEMENT_RESPONSE = new MessageTypes(NETWORK_MANAGEMENT_RESPONSE_VALUE);
    public static final MessageTypes NETWORK_MANAGEMENT_ADVICE = new MessageTypes(NETWORK_MANAGEMENT_ADVICE_VALUE);
    public static final MessageTypes NETWORK_MANAGEMENT_ADVICE_RESPONSE = new MessageTypes(NETWORK_MANAGEMENT_ADVICE_RESPONSE_VALUE);

    private int MTI;

    public MessageTypes() {
    }

    public MessageTypes(int MessageType) {
        this.MTI = MessageType;
    }

    public int getType() {
        return MTI;
    }

    public int hashCode() {
        return MTI;
    }

    public boolean equals(Object obj) {
        if(obj != null && obj instanceof MessageTypes) {
            MessageTypes that = (MessageTypes)obj;
            return this.MTI == that.MTI;
        } else {
            return false;
        }
    }

    public String toString() {
        switch(MTI) {
            case AUTHORIZATION_REQUEST_VALUE:
                return "0100";
            case AUTHORIZATION_RESPONSE_VALUE:
                return "0110";
            case FINANCIAL_REQUEST_VALUE:
                return "0200";
            case FINANCIAL_ADVICE_VALUE:
                return "0220";
            case FINANCIAL_ADVICE_REPEAT_VALUE:
                return "0221";
            case FINANCIAL_RESPONSE_VALUE:
                return "0210";
            case FINANCIAL_ADVICE_RESPONSE_VALUE:
                return "0230";
            case REVERSAL_REQUEST_VALUE:
                return "0400";
            case REVERSAL_REQUEST_REPEAT_VALUE:
                return "0401";
            case REVERSAL_ADVICE_VALUE:
                return "0420";
            case REVERSAL_ADVICE_REPEAT_VALUE:
                return "0421";
            case REVERSAL_RESPONSE_VALUE:
                return "0410";
            case REVERSAL_ADVICE_RESPONSE_VALUE:
                return "0430";
            case NETWORK_MANAGEMENT_REQUEST_VALUE:
                return "0800";
            case NETWORK_MANAGEMENT_RESPONSE_VALUE:
                return "0810";
            case NETWORK_MANAGEMENT_ADVICE_VALUE:
                return "0820";
            case NETWORK_MANAGEMENT_ADVICE_RESPONSE_VALUE:
                return "0830";
            default:
                return "";
        }
    }
}
