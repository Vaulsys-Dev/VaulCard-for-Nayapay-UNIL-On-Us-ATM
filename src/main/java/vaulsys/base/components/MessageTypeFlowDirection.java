package vaulsys.base.components;

public class MessageTypeFlowDirection {
    public final static String Authorization = "to Authorization";
    public final static String Financial = "to Financial";
    public final static String Clearing = "to Clearing";
    public final static String Network = "to Network";
    public final static String NotSupported = "to End";
//    public final static String ATM_REVERSAL = "to NDC_Reversal";
    public final static String GENERAL_UI = "to General_UI";
    public final static String NETWORK_UI = "to Network_UI";
    //m.rehman: for wallet transactions
    public final static String Wallet = "to Wallet";
    //m.rehman: for batch transactions
    public final static String Batch = "to Batch";
}
