package vaulsys.clearing.report;

import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;

import java.util.HashMap;
import java.util.Map;

public class ShetabReportConstants {

	public static Map<TerminalType, String> TerminalTypeToAcqReportTermType;
    public  static Map<String, IfxType> shetabTrnTypeToIfxType;
    public  static Map<String, TerminalType> shetabTermTypeToTerminalType;
    public  static Map<TrnType, String> TrnTypeToAcqReportType;
    public 	static Map<IfxType, String> ifxTypeToShetabTrnType;
    public  static Map<String, String> statementCodeToShetabTrnType;
    public  static Map<String, TrnType> statementCodeToTrnType;
    
    public static Map<TrnType, String> TrnTypeForSaderat;

    static {
        TerminalTypeToAcqReportTermType = new HashMap<TerminalType, String>();
        TerminalTypeToAcqReportTermType.put(TerminalType.ATM, "ATM");
        TerminalTypeToAcqReportTermType.put(TerminalType.POS, "POS");
        TerminalTypeToAcqReportTermType.put(TerminalType.VRU, "VRU");
        TerminalTypeToAcqReportTermType.put(TerminalType.PINPAD, "PAD");
        TerminalTypeToAcqReportTermType.put(TerminalType.MOBILE, "MOB");
        TerminalTypeToAcqReportTermType.put(TerminalType.INTERNET, "INT");
//        TerminalTypeToAcqReportTermType.put(TerminalType.INFOKIOSK, "IKT");
        TerminalTypeToAcqReportTermType.put(TerminalType.KIOSK_CARD_PRESENT, "IKT");	//Mirkamali(Task147)
        

        shetabTrnTypeToIfxType = new HashMap<String, IfxType>();
        shetabTrnTypeToIfxType.put("W", IfxType.WITHDRAWAL_RS);
        shetabTrnTypeToIfxType.put("BI", IfxType.BAL_INQ_RS);
        shetabTrnTypeToIfxType.put("BP", IfxType.BILL_PMT_RS);
        shetabTrnTypeToIfxType.put("P", IfxType.PURCHASE_RS);
        shetabTrnTypeToIfxType.put("TT", IfxType.TRANSFER_TO_ACCOUNT_RS);
        shetabTrnTypeToIfxType.put("TF", IfxType.TRANSFER_FROM_ACCOUNT_RS);
        shetabTrnTypeToIfxType.put("T", IfxType.TRANSFER_RS);
        shetabTrnTypeToIfxType.put("CH", IfxType.PURCHASE_CHARGE_RS);
        
        shetabTermTypeToTerminalType = new HashMap<String, TerminalType>();
        shetabTermTypeToTerminalType.put("INT", TerminalType.INTERNET);
        shetabTermTypeToTerminalType.put("ATM", TerminalType.ATM);
        shetabTermTypeToTerminalType.put("POS", TerminalType.POS);
        shetabTermTypeToTerminalType.put("PAD", TerminalType.PINPAD);
        shetabTermTypeToTerminalType.put("VRU", TerminalType.VRU);
        shetabTermTypeToTerminalType.put("MOB", TerminalType.MOBILE);
//        shetabTermTypeToTerminalType.put("IKT", TerminalType.INFOKIOSK);
        shetabTermTypeToTerminalType.put("IKT", TerminalType.KIOSK_CARD_PRESENT);	//Mirkamali(Task148)
        
        TrnTypeToAcqReportType = new HashMap<TrnType, String>();
        TrnTypeToAcqReportType.put(TrnType.WITHDRAWAL, "WD");
        TrnTypeToAcqReportType.put(TrnType.PURCHASE, "PU");
        TrnTypeToAcqReportType.put(TrnType.BILLPAYMENT, "BP");
        TrnTypeToAcqReportType.put(TrnType.DECREMENTALTRANSFER, "TF");
        TrnTypeToAcqReportType.put(TrnType.INCREMENTALTRANSFER, "TT");
        TrnTypeToAcqReportType.put(TrnType.PURCHASECHARGE, "CH");
        TrnTypeToAcqReportType.put(TrnType.TRANSFER, "T ");
        TrnTypeToAcqReportType.put(TrnType.RETURN, "RF");
        
        ifxTypeToShetabTrnType = new HashMap<IfxType, String>();
        ifxTypeToShetabTrnType.put(IfxType.WITHDRAWAL_RS, "W");
        ifxTypeToShetabTrnType.put(IfxType.BAL_INQ_RS, "BI");
        ifxTypeToShetabTrnType.put(IfxType.BILL_PMT_RS, "BP");
        ifxTypeToShetabTrnType.put(IfxType.PURCHASE_RS, "P");
        ifxTypeToShetabTrnType.put(IfxType.TRANSFER_TO_ACCOUNT_RS, "TT");
        ifxTypeToShetabTrnType.put(IfxType.TRANSFER_FROM_ACCOUNT_RS, "TF");
        ifxTypeToShetabTrnType.put(IfxType.TRANSFER_RS, "T");

        
        ifxTypeToShetabTrnType = new HashMap<IfxType, String>();
        ifxTypeToShetabTrnType.put(IfxType.WITHDRAWAL_RS, "W");
        ifxTypeToShetabTrnType.put(IfxType.BAL_INQ_RS, "BI");
        ifxTypeToShetabTrnType.put(IfxType.BILL_PMT_RS, "BP");
        ifxTypeToShetabTrnType.put(IfxType.PURCHASE_RS, "P");
        ifxTypeToShetabTrnType.put(IfxType.TRANSFER_TO_ACCOUNT_RS, "TT");
        ifxTypeToShetabTrnType.put(IfxType.TRANSFER_FROM_ACCOUNT_RS, "TF");
        ifxTypeToShetabTrnType.put(IfxType.TRANSFER_RS, "T");
        
        statementCodeToShetabTrnType = new HashMap<String, String>();
        statementCodeToShetabTrnType.put("141", "W");
        statementCodeToShetabTrnType.put("251", "T");
        statementCodeToShetabTrnType.put("252", "TF");
        statementCodeToShetabTrnType.put("253", "TT");
        statementCodeToShetabTrnType.put("438", "BP");
        statementCodeToShetabTrnType.put("439", "BP");
        statementCodeToShetabTrnType.put("440", "BP");
        statementCodeToShetabTrnType.put("441", "BP");
        statementCodeToShetabTrnType.put("442", "BP");
        statementCodeToShetabTrnType.put("443", "BP");
        statementCodeToShetabTrnType.put("444", "P");
        statementCodeToShetabTrnType.put("448", "P");
        statementCodeToShetabTrnType.put("458", "BI");
        statementCodeToShetabTrnType.put("446", "W");
        // statementCodeToTrnType.put("188", TrnType.PURCHASECHARGE);
        
        statementCodeToTrnType = new HashMap<String, TrnType>();
        statementCodeToTrnType.put("141", TrnType.WITHDRAWAL);
        statementCodeToTrnType.put("251", TrnType.TRANSFER);
        statementCodeToTrnType.put("252", TrnType.DECREMENTALTRANSFER);
        statementCodeToTrnType.put("253", TrnType.INCREMENTALTRANSFER);
        statementCodeToTrnType.put("438", TrnType.BILLPAYMENT);
        statementCodeToTrnType.put("439", TrnType.BILLPAYMENT);
        statementCodeToTrnType.put("440", TrnType.BILLPAYMENT);
        statementCodeToTrnType.put("441", TrnType.BILLPAYMENT);
        statementCodeToTrnType.put("442", TrnType.BILLPAYMENT);
        statementCodeToTrnType.put("443", TrnType.BILLPAYMENT);
        statementCodeToTrnType.put("444", TrnType.PURCHASE);
        statementCodeToTrnType.put("448", TrnType.PURCHASE);
        statementCodeToTrnType.put("458", TrnType.BALANCEINQUIRY);
        statementCodeToTrnType.put("446", TrnType.WITHDRAWAL);
        statementCodeToTrnType.put("188", TrnType.PURCHASECHARGE);
        
        TrnTypeForSaderat = new HashMap<TrnType, String>();
        TrnTypeForSaderat.put(TrnType.PURCHASE, "PU");
        TrnTypeForSaderat.put(TrnType.BILLPAYMENT, "BP");
        
    }


}
