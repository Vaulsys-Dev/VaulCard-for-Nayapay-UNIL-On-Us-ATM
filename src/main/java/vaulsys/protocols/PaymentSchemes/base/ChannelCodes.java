package vaulsys.protocols.PaymentSchemes.base;

/**
 * Created by m.rehman on 4/26/2016.
 */
public class ChannelCodes {

	//m.rehman: Euronet Integration, re-arrange channel codes
    public static final String ONELINK = "0003";
    public static final String VISA_BASE_I = "0007";
    public static final String EURONET = "0010";
    public static final String NAC = "0020"; //Raza adding for KEENU
    public static final String VISA_SMS = "0030";
    public static final String MASTERCARD = "0040";
    public static final String UNION_PAY = "0046";
    public static final String NAYAPAY = "9990"; //Raza Nayapay -- Update This
    public static final String ASKARI = "9991"; //Raza Nayapay Askari -- Update This
    public static final String WALLET = "9992"; //Raza Nayapay WalletCMS -- Update This
    public static final String SWITCH = "9999"; //Raza Nayapay -- Update This

    // Asim Shahzad, Date : 18th Jan 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 2)
    public static final String HSM = "0001";
    // ========================================================================================================

    //m.rehman: 16-09-2021 - VP-NAP-202109092 / VG-NAP-202109101 - Non financial Transactions on VaulGuard
    public static final String WALLETADMINPORTAL = "9979";
    public static final String WALLETSUPPORTPORTAL = "9980";
    public static final String WALLETMERCHANTPORTALFINANCIAL = "9981";
    public static final String WALLETMERCHANTPORTALNONFINANCIAL = "9982";
    public static final String WALLETAPPNONFINANCIAL = "9983";
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
    public static final String WALLETAPPSTATEMENT = "9978";
	
    //m.rehman: 15-11-2021, Nayapay Optimization - Channel wise process segregation
    public static final String WALLETEURONETISO = "9977";
    public static final String WALLET1LINKISO = "9976";
    public static final String WALLETMEZNBIOATM = "9975";
    public static final String WALLETAKBLOTC = "9974";
    public static final String WALLETCARDINALCOMMERCE = "9973";
    public static final String WALLET1LINKBILLERUBPS = "9972";
    public static final String WALLETMEZNLINKBANK = "9971";
    public static final String WALLETHABBLINKBANK = "9970";
    public static final String WALLETFAYSLINKBANK = "9969";
    public static final String WALLETJSBLLINKBANK = "9968";
    public static final String WALLETUI = "9967";

    public static final String NAYAPAY1LINKISO = "9966";
    public static final String NAYAPAYJSBLLINKBANK = "9965";
    public static final String NAYAPAYMEZNLINKBANK = "9964";
    public static final String NAYAPAYHABBLINKBANK = "9963";
    public static final String NAYAPAYFAYSLINKBANK = "9962";
    public static final String NAYAPAYCARDINALCOMMERCE = "9961";

    public static final String WALLETMERCHANTSETTLEMENT = "9960";
    ///////////////////////////////////////////////////////////////////////////////

    //m.rehman: 10-12-2021, VP-NAP-202111291 / VC-NAP-202111291 / VG-NAP-202111291 ==> [ Details Required ] ==> Meezan ATM On-Us Withdrawal/Balance Inquiry and Reversal
    public static final String MEZNONUSATM = "9959";
    public static final String WALLETMEZNONUSATM = "9958";
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Huzaifa: 11/08/2023: FW: NAP-P5-23 ==> [ Logging email ] ==> Segregation of ATM On Us Channels Bank - UBL & BAFL
    public static final String ALFHONUSATM = "9928";
    public static final String WALLETALFHONUSATM = "9927";// TODO: Huzaifa Drop entry in DB

    // Huzaifa: 11/08/2023: FW: NAP-P5-23 ==> [ Logging email ] ==> Segregation of ATM On Us Channels Bank - UBL & BAFL
    public static final String UNILONUSATM = "9926";
    public static final String WALLETUNILONUSATM = "9925";// TODO: Huzaifa Drop entry in DB


    // Added By : Asim Shahzad, Date : 24th Nov 2016, Desc : For VISA SMS channel handling
    private static final int UNION_PAY_VALUE = 1;
    private static final int VISA_SMS_VALUE = 2;
    private static final int VISA_BASE_I_VALUE = 3;
    public static final int ONELINK_VALUE = 4;
    private static final int NAC_VALUE = 5;

    //public static final ChannelCodes UNION_PAY = new ChannelCodes(UNION_PAY_VALUE); //Raza commenting
    //public static final ChannelCodes VISA_SMS = new ChannelCodes(VISA_SMS_VALUE); //Raza commenting
    //public static final ChannelCodes VISA_BASE_I = new ChannelCodes(VISA_BASE_I_VALUE); //Raza commenting

    private int ChannelCode;

    public ChannelCodes() {
    }

    public ChannelCodes(int ChannelCode) {
        this.ChannelCode = ChannelCode;
    }

    public String toString() {
        switch(ChannelCode) {
            case UNION_PAY_VALUE:
                return "UnionPay";
            case VISA_SMS_VALUE:
                return "VisaSMS";
            case VISA_BASE_I_VALUE:
                return "VisaBaseI";
            case ONELINK_VALUE:
                return "NAC";
            case NAC_VALUE: //Raza adding for KEENU
                return "NAC";
            default:
                return "";
        }
    }
    // End
}
