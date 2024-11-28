package vaulsys.protocols.PaymentSchemes.MasterCard;

import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MasterCard87ProtocolDialog implements ProtocolDialog {

    private Map<String, Map<Integer, Character>> msgElementDefinition;
    public MasterCard87ProtocolDialog() {
        msgElementDefinition = new HashMap<String, Map<Integer, Character>>();


        //Message Element Definition for 0800
        msgElementDefinition.put("0800", new HashMap<Integer, Character>());
        msgElementDefinition.get("0800").put(2, 'M'); //verify this Field is being sent by MDS Simulator
        msgElementDefinition.get("0800").put(7, 'M');
        msgElementDefinition.get("0800").put(11, 'M');
        msgElementDefinition.get("0800").put(33, 'M');
        msgElementDefinition.get("0800").put(48, 'C');
        msgElementDefinition.get("0800").put(63, 'C');
        msgElementDefinition.get("0800").put(70, 'M');
        msgElementDefinition.get("0800").put(96, 'C');
        msgElementDefinition.get("0800").put(100, 'C');
        msgElementDefinition.get("0800").put(126, 'C'); //Switch Private Data
        msgElementDefinition.get("0800").put(127, 'C'); //Private Data

        //Message Element Definition for 0810
        msgElementDefinition.put("0810", new HashMap<Integer, Character>());
        msgElementDefinition.get("0810").put(2, 'M'); //verify this Field is being sent by MDS Simulator
        msgElementDefinition.get("0810").put(7, 'M');
        msgElementDefinition.get("0810").put(11, 'M');
        msgElementDefinition.get("0810").put(33, 'M');
        msgElementDefinition.get("0810").put(39, 'M');
        msgElementDefinition.get("0810").put(48, 'C');
        msgElementDefinition.get("0810").put(63, 'M');
        msgElementDefinition.get("0810").put(70, 'M');
        msgElementDefinition.get("0810").put(96, 'C');
        msgElementDefinition.get("0810").put(100, 'C');
        msgElementDefinition.get("0810").put(126, 'C'); //Switch Private Data
        msgElementDefinition.get("0810").put(127, 'C'); //Private Data

        //Message Element Definition for 0820
        msgElementDefinition.put("0820", new HashMap<Integer, Character>());
        msgElementDefinition.get("0820").put(2, 'C'); //Raza adding for KeyExchange Advice
        msgElementDefinition.get("0820").put(7, 'M');
        msgElementDefinition.get("0820").put(11, 'M');
        msgElementDefinition.get("0820").put(15, 'C');
        msgElementDefinition.get("0820").put(33, 'C');
        msgElementDefinition.get("0820").put(48, 'C');
        msgElementDefinition.get("0820").put(53, 'C');
        msgElementDefinition.get("0820").put(63, 'C'); //Raza adding for KeyExchange Advice
        msgElementDefinition.get("0820").put(70, 'M');
        msgElementDefinition.get("0820").put(96, 'C');
        msgElementDefinition.get("0820").put(100, 'C');
        msgElementDefinition.get("0820").put(128, 'C');

        //Message Element Definition for 0830
        msgElementDefinition.put("0830", new HashMap<Integer, Character>());
        msgElementDefinition.get("0830").put(7, 'M');
        msgElementDefinition.get("0830").put(11, 'M');
        msgElementDefinition.get("0830").put(15, 'C');
        msgElementDefinition.get("0830").put(33, 'C');
        msgElementDefinition.get("0830").put(39, 'M');
        msgElementDefinition.get("0830").put(48, 'C');
        msgElementDefinition.get("0830").put(53, 'C');
        msgElementDefinition.get("0830").put(70, 'M');
        msgElementDefinition.get("0830").put(100, 'C');
        msgElementDefinition.get("0830").put(128, 'C');

        //Message Element Definition for 0200
        msgElementDefinition.put("0200", new HashMap<Integer, Character>());
        msgElementDefinition.get("0200").put(2, 'M');
        msgElementDefinition.get("0200").put(3, 'M');
        msgElementDefinition.get("0200").put(4, 'C');
        msgElementDefinition.get("0200").put(5, 'C'); //Amount Settlement
        msgElementDefinition.get("0200").put(6, 'C'); //Amount CardHolderBilling
        msgElementDefinition.get("0200").put(7, 'M');
        msgElementDefinition.get("0200").put(9, 'C'); //Conversion Rate Settlement
        msgElementDefinition.get("0200").put(10, 'C'); //Conversion Rate Card Holder Billing
        msgElementDefinition.get("0200").put(11, 'M');
        msgElementDefinition.get("0200").put(12, 'M');
        msgElementDefinition.get("0200").put(13, 'M');
        msgElementDefinition.get("0200").put(14, 'C'); //Date Expiration
        msgElementDefinition.get("0200").put(15, 'C'); //Date Settlement
        msgElementDefinition.get("0200").put(16, 'C'); //Date Conversion
        msgElementDefinition.get("0200").put(18, 'C'); //Merchant Type Conditional as it will be filled by TPSP
        //msgElementDefinition.get("0200").put(19, 'M'); //Not Required
        msgElementDefinition.get("0200").put(22, 'M');
        //msgElementDefinition.get("0200").put(23, 'C'); //Card Sequence Number
        msgElementDefinition.get("0200").put(25, 'M'); //POS Condition Code
        msgElementDefinition.get("0200").put(26, 'C'); //POS PIN Capture Code
        //msgElementDefinition.get("0200").put(28, 'C'); //Amount Transaction Fee
        msgElementDefinition.get("0200").put(32, 'M'); //Acquiring Institution Identification Code
        msgElementDefinition.get("0200").put(33, 'C'); //Need Validation -- Forwarding Institution Identification Code
        msgElementDefinition.get("0200").put(35, 'M'); //Track-2 Data
        //msgElementDefinition.get("0200").put(36, 'C'); //Track-3 Data
        msgElementDefinition.get("0200").put(37, 'M');
        msgElementDefinition.get("0200").put(38, 'C'); //Authorization Identification Response reuired for PreAuth Txns.
        //msgElementDefinition.get("0200").put(39, 'C'); //Response Code not reuired in request message
        msgElementDefinition.get("0200").put(41, 'M'); //Card Acceptor Terminal Identification
        msgElementDefinition.get("0200").put(42, 'M'); //Card Acceptor Identification Code
        msgElementDefinition.get("0200").put(43, 'M'); //Card Acceptor Name Location
        //msgElementDefinition.get("0200").put(45, 'C'); //Track-1 Data
        //msgElementDefinition.get("0200").put(48, 'C'); //Additional Data
        msgElementDefinition.get("0200").put(49, 'M'); //Currency Code Transaction
        msgElementDefinition.get("0200").put(50, 'C'); //Currency Code Settlement
        msgElementDefinition.get("0200").put(51, 'C'); //Currency Code, Card Holder Billing
        msgElementDefinition.get("0200").put(52, 'M'); //Personal Identification (PIN) Data
        //msgElementDefinition.get("0200").put(53, 'C'); //Not Required
        //msgElementDefinition.get("0200").put(54, 'C'); //Additional Amounts
        //msgElementDefinition.get("0200").put(55, 'C'); //ICC Related Data
        //msgElementDefinition.get("0200").put(60, 'C'); //Additional EMV Information
        //msgElementDefinition.get("0200").put(61, 'C'); //Not Required
        msgElementDefinition.get("0200").put(90, 'C'); //Original Data Element for PreAuth Txns
        msgElementDefinition.get("0200").put(100, 'C'); //Receiving Institution Identification Number
        //msgElementDefinition.get("0200").put(121, 'C'); //Not Reuired
        //msgElementDefinition.get("0200").put(122, 'C'); //Not Reuired
        //msgElementDefinition.get("0200").put(128, 'C'); //Not Reuired

        //Message Element Definition for 0210
        msgElementDefinition.put("0210", new HashMap<Integer, Character>());
        msgElementDefinition.get("0210").put(2, 'M');
        msgElementDefinition.get("0210").put(3, 'M');
        msgElementDefinition.get("0210").put(4, 'C');
        msgElementDefinition.get("0210").put(5, 'C'); //Amount Settlement
        msgElementDefinition.get("0210").put(6, 'C'); //Amount CardHolderBilling
        msgElementDefinition.get("0210").put(7, 'M');
        msgElementDefinition.get("0210").put(9, 'C'); //Conversion Rate Settlement
        msgElementDefinition.get("0210").put(11, 'M');
        msgElementDefinition.get("0210").put(12, 'M');
        msgElementDefinition.get("0210").put(13, 'M');
        msgElementDefinition.get("0210").put(14, 'C'); //Date Expiration
        msgElementDefinition.get("0210").put(15, 'C'); //Date Settlement
        msgElementDefinition.get("0210").put(16, 'C'); //Date Conversion
        msgElementDefinition.get("0210").put(18, 'C'); //Merchant Type Conditional as it will be filled by TPSP
        //msgElementDefinition.get("0210").put(19, 'M'); //Not Required
        //msgElementDefinition.get("0210").put(22, 'M');
        //msgElementDefinition.get("0210").put(23, 'C'); //Card Sequence Number
        msgElementDefinition.get("0210").put(25, 'M'); //POS Condition Code
        //msgElementDefinition.get("0210").put(26, 'C'); //POS PIN Capture Code
        //msgElementDefinition.get("0210").put(28, 'C'); //Amount Transaction Fee
        msgElementDefinition.get("0210").put(32, 'M'); //Acquiring Institution Identification Code
        msgElementDefinition.get("0210").put(33, 'C'); //Need Validation -- Forwarding Institution Identification Code
        //msgElementDefinition.get("0210").put(35, 'M'); //Track-2 Data
        //msgElementDefinition.get("0210").put(36, 'C'); //Track-3 Data
        msgElementDefinition.get("0210").put(37, 'M');
        msgElementDefinition.get("0210").put(38, 'M'); //Authorization Identification Response
        msgElementDefinition.get("0210").put(39, 'M'); //Response Code
        msgElementDefinition.get("0210").put(41, 'M'); //Card Acceptor Terminal Identification
        msgElementDefinition.get("0210").put(42, 'C'); //Card Acceptor Identification Code
        //msgElementDefinition.get("0210").put(45, 'C'); //Track-1 Data
        //msgElementDefinition.get("0210").put(48, 'C'); //Additional Data
        msgElementDefinition.get("0210").put(49, 'M'); //Currency Code Transaction
        msgElementDefinition.get("0210").put(50, 'C'); //Currency Code Settlement
        msgElementDefinition.get("0210").put(51, 'C'); //Currency Code, Card Holder Billing
        //msgElementDefinition.get("0210").put(53, 'C'); //Not Required
        msgElementDefinition.get("0210").put(54, 'C'); //Additional Amounts for Balance
        //msgElementDefinition.get("0210").put(55, 'C'); //ICC Related Data
        //msgElementDefinition.get("0210").put(60, 'C'); //Additional EMV Information
        //msgElementDefinition.get("0210").put(61, 'C'); //Not Required
        msgElementDefinition.get("0210").put(100, 'C'); //Receiving Institution Identification Number
        //msgElementDefinition.get("0210").put(121, 'C'); //Not Reuired
        //msgElementDefinition.get("0210").put(122, 'C'); //Not Reuired
        //msgElementDefinition.get("0210").put(128, 'C'); //Not Reuired

        //Message Element Definition for 0100
        msgElementDefinition.put("0100", new HashMap<Integer, Character>());
        msgElementDefinition.get("0100").put(2, 'M');
        msgElementDefinition.get("0100").put(3, 'M');
        msgElementDefinition.get("0100").put(4, 'C');
        msgElementDefinition.get("0100").put(5, 'C'); //Amount Settlement
        msgElementDefinition.get("0100").put(6, 'C'); //Amount CardHolderBilling
        msgElementDefinition.get("0100").put(7, 'M');
        msgElementDefinition.get("0100").put(9, 'C'); //Conversion Rate Settlement
        msgElementDefinition.get("0100").put(10, 'C'); //Conversion Rate Card Holder Billing
        msgElementDefinition.get("0100").put(11, 'M');
        msgElementDefinition.get("0100").put(12, 'M');
        msgElementDefinition.get("0100").put(13, 'M');
        msgElementDefinition.get("0100").put(14, 'C'); //Date Expiration
        msgElementDefinition.get("0100").put(15, 'C'); //Date Settlement
        msgElementDefinition.get("0100").put(16, 'C'); //Date Conversion
        msgElementDefinition.get("0100").put(18, 'C'); //Merchant Type Conditional as it will be filled by TPSP
        //msgElementDefinition.get("0100").put(19, 'M'); //Not Required
        msgElementDefinition.get("0100").put(22, 'M');
        //msgElementDefinition.get("0100").put(23, 'C'); //Card Sequence Number
        msgElementDefinition.get("0100").put(25, 'C'); //POS Condition Code
        msgElementDefinition.get("0100").put(26, 'C'); //POS PIN Capture Code
        //msgElementDefinition.get("0100").put(28, 'C'); //Amount Transaction Fee
        msgElementDefinition.get("0100").put(32, 'M'); //Acquiring Institution Identification Code
        msgElementDefinition.get("0100").put(33, 'C'); //Need Validation -- Forwarding Institution Identification Code
        msgElementDefinition.get("0100").put(35, 'M'); //Track-2 Data
        //msgElementDefinition.get("0100").put(36, 'C'); //Track-3 Data
        msgElementDefinition.get("0100").put(37, 'M');
        msgElementDefinition.get("0100").put(38, 'C'); //Authorization Identification Response reuired for PreAuth Txns.
        //msgElementDefinition.get("0100").put(39, 'C'); //Response Code not reuired in request message
        msgElementDefinition.get("0100").put(41, 'M'); //Card Acceptor Terminal Identification
        msgElementDefinition.get("0100").put(42, 'M'); //Card Acceptor Identification Code
        msgElementDefinition.get("0100").put(43, 'M'); //Card Acceptor Name Location
        //msgElementDefinition.get("0100").put(45, 'C'); //Track-1 Data
        msgElementDefinition.get("0100").put(48, 'C'); //Additional Data
        msgElementDefinition.get("0100").put(49, 'M'); //Currency Code Transaction
        msgElementDefinition.get("0100").put(50, 'C'); //Currency Code Settlement
        msgElementDefinition.get("0100").put(51, 'C'); //Currency Code, Card Holder Billing
        msgElementDefinition.get("0100").put(52, 'C'); //Personal Identification (PIN) Data
        msgElementDefinition.get("0100").put(53, 'C'); //For MS Purchase ONLINE PIN
        //msgElementDefinition.get("0100").put(54, 'C'); //Additional Amounts
        //msgElementDefinition.get("0100").put(55, 'C'); //ICC Related Data
        //msgElementDefinition.get("0100").put(60, 'C'); //Additional EMV Information
        msgElementDefinition.get("0100").put(61, 'C'); //Not Required
        msgElementDefinition.get("0100").put(63, 'C'); //Not Required
        msgElementDefinition.get("0100").put(64, 'C'); //Not Required
        msgElementDefinition.get("0100").put(90, 'C'); //Original Data Element for PreAuth Txns
        msgElementDefinition.get("0100").put(100, 'C'); //Receiving Institution Identification Number
        //msgElementDefinition.get("0100").put(121, 'C'); //Not Reuired
        //msgElementDefinition.get("0100").put(122, 'C'); //Not Reuired
        //msgElementDefinition.get("0100").put(128, 'C'); //Not Reuired

        //Message Element Definition for 0110
        msgElementDefinition.put("0110", new HashMap<Integer, Character>());
        msgElementDefinition.get("0110").put(2, 'M');
        msgElementDefinition.get("0110").put(3, 'M');
        msgElementDefinition.get("0110").put(4, 'C');
        msgElementDefinition.get("0110").put(5, 'C'); //Amount Settlement
        msgElementDefinition.get("0110").put(6, 'C'); //Amount CardHolderBilling
        msgElementDefinition.get("0110").put(7, 'M');
        msgElementDefinition.get("0110").put(9, 'C'); //Conversion Rate Settlement
        msgElementDefinition.get("0110").put(10, 'C'); //Conversion Rate Card Holder Billing
        msgElementDefinition.get("0110").put(11, 'M');
        msgElementDefinition.get("0110").put(12, 'M');
        msgElementDefinition.get("0110").put(13, 'M');
        msgElementDefinition.get("0110").put(14, 'C'); //Date Expiration
        msgElementDefinition.get("0110").put(15, 'C'); //Date Settlement
        msgElementDefinition.get("0110").put(16, 'C'); //Date Conversion
        msgElementDefinition.get("0110").put(18, 'C'); //Merchant Type Conditional as it will be filled by TPSP
        //msgElementDefinition.get("0110").put(19, 'M'); //Not Required
        //msgElementDefinition.get("0110").put(22, 'M');
        //msgElementDefinition.get("0110").put(23, 'C'); //Card Sequence Number
        msgElementDefinition.get("0110").put(25, 'M'); //POS Condition Code
        //msgElementDefinition.get("0110").put(26, 'C'); //POS PIN Capture Code
        //msgElementDefinition.get("0110").put(28, 'C'); //Amount Transaction Fee
        msgElementDefinition.get("0110").put(32, 'M'); //Acquiring Institution Identification Code
        msgElementDefinition.get("0110").put(33, 'C'); //Need Validation -- Forwarding Institution Identification Code
        //msgElementDefinition.get("0110").put(35, 'M'); //Track-2 Data
        //msgElementDefinition.get("0110").put(36, 'C'); //Track-3 Data
        msgElementDefinition.get("0110").put(37, 'M');
        msgElementDefinition.get("0110").put(38, 'M'); //Authorization Identification Response
        msgElementDefinition.get("0110").put(39, 'M'); //Response Code
        msgElementDefinition.get("0110").put(41, 'M'); //Card Acceptor Terminal Identification
        msgElementDefinition.get("0110").put(42, 'C'); //Card Acceptor Identification Code
        //msgElementDefinition.get("0110").put(45, 'C'); //Track-1 Data
        //msgElementDefinition.get("0110").put(48, 'C'); //Additional Data
        msgElementDefinition.get("0110").put(49, 'M'); //Currency Code Transaction
        msgElementDefinition.get("0110").put(50, 'C'); //Currency Code Settlement
        msgElementDefinition.get("0110").put(51, 'C'); //Currency Code, Card Holder Billing
        //msgElementDefinition.get("0110").put(53, 'C'); //Not Required
        msgElementDefinition.get("0110").put(54, 'C'); //Additional Amounts for Balance
        //msgElementDefinition.get("0110").put(55, 'C'); //ICC Related Data
        //msgElementDefinition.get("0110").put(60, 'C'); //Additional EMV Information
        //msgElementDefinition.get("0110").put(61, 'C'); //Not Required
        msgElementDefinition.get("0110").put(63, 'C'); //Network Data
        msgElementDefinition.get("0110").put(64, 'C'); //Message Authentication Code
        msgElementDefinition.get("0110").put(100, 'C'); //Receiving Institution Identification Number
        //msgElementDefinition.get("0110").put(121, 'C'); //Not Reuired
        //msgElementDefinition.get("0110").put(122, 'C'); //Not Reuired
        //msgElementDefinition.get("0110").put(128, 'C'); //Not Reuired


        //Message Element Definition for 0420
        msgElementDefinition.put("0420", new HashMap<Integer, Character>());
        msgElementDefinition.get("0420").put(2, 'M');
        msgElementDefinition.get("0420").put(3, 'M');
        msgElementDefinition.get("0420").put(4, 'M');
        msgElementDefinition.get("0420").put(5, 'C');
        msgElementDefinition.get("0420").put(6, 'C'); //Amount Card Holder Billing
        msgElementDefinition.get("0420").put(7, 'M');
        msgElementDefinition.get("0420").put(9, 'C');
        msgElementDefinition.get("0420").put(10, 'C');
        msgElementDefinition.get("0420").put(11, 'M');
        msgElementDefinition.get("0420").put(12, 'M');
        msgElementDefinition.get("0420").put(13, 'M');
        msgElementDefinition.get("0420").put(15, 'C');
        msgElementDefinition.get("0420").put(16, 'C');
        msgElementDefinition.get("0420").put(18, 'C');
        //msgElementDefinition.get("0420").put(19, 'C');
        msgElementDefinition.get("0420").put(22, 'M');
        //msgElementDefinition.get("0420").put(23, 'C');
        msgElementDefinition.get("0420").put(25, 'M'); //POS Condition Code -- For PreAuth Txns.
        //msgElementDefinition.get("0420").put(26, 'C');
        msgElementDefinition.get("0420").put(32, 'M');
        msgElementDefinition.get("0420").put(33, 'C');
        //msgElementDefinition.get("0420").put(35, 'M');
        msgElementDefinition.get("0420").put(37, 'M');
        msgElementDefinition.get("0420").put(38, 'C'); //Adding Authorization Identification Response as Mandatory
        msgElementDefinition.get("0420").put(39, 'C'); //Adding Response Code as Mandatory
        msgElementDefinition.get("0420").put(41, 'M');
        msgElementDefinition.get("0420").put(42, 'M');
        msgElementDefinition.get("0420").put(43, 'M');
        //msgElementDefinition.get("0420").put(44, 'C');
        msgElementDefinition.get("0420").put(49, 'M');
        msgElementDefinition.get("0420").put(50, 'C');
        msgElementDefinition.get("0420").put(51, 'C');
        //msgElementDefinition.get("0420").put(54, 'C'); //Additional Amounts conditional
        //msgElementDefinition.get("0420").put(55, 'C');
        //msgElementDefinition.get("0420").put(60, 'C');
        msgElementDefinition.get("0420").put(90, 'M');
        msgElementDefinition.get("0420").put(100, 'C');
        //msgElementDefinition.get("0420").put(121, 'C');
        //msgElementDefinition.get("0420").put(122, 'C');
        //msgElementDefinition.get("0420").put(128, 'C');

        //Message Element Definition for 0430
        msgElementDefinition.put("0430", new HashMap<Integer, Character>());
        msgElementDefinition.get("0430").put(2, 'M');
        msgElementDefinition.get("0430").put(3, 'M');
        msgElementDefinition.get("0430").put(4, 'M');
        msgElementDefinition.get("0430").put(5, 'C');
        msgElementDefinition.get("0430").put(6, 'C'); //Amount Card Holder Billing
        msgElementDefinition.get("0430").put(7, 'M');
        //msgElementDefinition.get("0430").put(9, 'C');
        msgElementDefinition.get("0430").put(10, 'C');
        msgElementDefinition.get("0430").put(11, 'M');
        msgElementDefinition.get("0430").put(12, 'M');
        msgElementDefinition.get("0430").put(13, 'M');
        msgElementDefinition.get("0430").put(15, 'C');
        msgElementDefinition.get("0430").put(16, 'C');
        msgElementDefinition.get("0430").put(18, 'C');
        //msgElementDefinition.get("0430").put(19, 'C');
        //msgElementDefinition.get("0430").put(22, 'M');
        //msgElementDefinition.get("0430").put(23, 'C');
        msgElementDefinition.get("0430").put(25, 'M');
        //msgElementDefinition.get("0430").put(26, 'C');
        msgElementDefinition.get("0430").put(32, 'M');
        msgElementDefinition.get("0430").put(33, 'C');
        msgElementDefinition.get("0430").put(37, 'M');
        msgElementDefinition.get("0430").put(38, 'C'); //Authorization Identification Response is conditional in Reversal Reply.
        msgElementDefinition.get("0430").put(39, 'M'); //Adding Response Code as Conditional
        msgElementDefinition.get("0430").put(41, 'M');
        msgElementDefinition.get("0430").put(42, 'M');
        //msgElementDefinition.get("0430").put(43, 'M');
        msgElementDefinition.get("0430").put(44, 'C');
        msgElementDefinition.get("0430").put(49, 'M');
        msgElementDefinition.get("0430").put(50, 'C');
        msgElementDefinition.get("0430").put(51, 'C');
        //msgElementDefinition.get("0430").put(54, 'C'); //Additional Amounts conditional
        //msgElementDefinition.get("0430").put(55, 'C');
        //msgElementDefinition.get("0430").put(60, 'C');
        //msgElementDefinition.get("0430").put(90, 'M');
        msgElementDefinition.get("0430").put(100, 'C');
        //msgElementDefinition.get("0430").put(121, 'C');
        //msgElementDefinition.get("0430").put(122, 'C');
        //msgElementDefinition.get("0430").put(128, 'C');

        //Message Element Definition for 0220
        msgElementDefinition.put("0220", new HashMap<Integer, Character>());
        msgElementDefinition.get("0220").put(2, 'M');
        msgElementDefinition.get("0220").put(3, 'M');
        msgElementDefinition.get("0220").put(4, 'M');
        msgElementDefinition.get("0220").put(5, 'C');
        msgElementDefinition.get("0220").put(6, 'C');
        msgElementDefinition.get("0220").put(7, 'M');
        //msgElementDefinition.get("0220").put(9, 'C');
        msgElementDefinition.get("0220").put(10, 'C');
        msgElementDefinition.get("0220").put(11, 'M');
        msgElementDefinition.get("0220").put(12, 'M');
        msgElementDefinition.get("0220").put(13, 'M');
        msgElementDefinition.get("0220").put(14, 'C');
        msgElementDefinition.get("0220").put(15, 'C');
        msgElementDefinition.get("0220").put(16, 'C');
        msgElementDefinition.get("0220").put(18, 'C');
        //msgElementDefinition.get("0220").put(19, 'M');
        msgElementDefinition.get("0220").put(22, 'M');
        msgElementDefinition.get("0220").put(23, 'C');
        msgElementDefinition.get("0220").put(25, 'M');
        msgElementDefinition.get("0220").put(32, 'M');
        msgElementDefinition.get("0220").put(33, 'C');
        msgElementDefinition.get("0220").put(35, 'C');
        msgElementDefinition.get("0220").put(36, 'C');
        msgElementDefinition.get("0220").put(37, 'M');
        msgElementDefinition.get("0220").put(38, 'C');
        msgElementDefinition.get("0220").put(41, 'M');
        msgElementDefinition.get("0220").put(42, 'M');
        msgElementDefinition.get("0220").put(43, 'M');
        msgElementDefinition.get("0220").put(45, 'C');
        msgElementDefinition.get("0220").put(48, 'C');
        msgElementDefinition.get("0220").put(49, 'M');
        msgElementDefinition.get("0220").put(50, 'C');
        msgElementDefinition.get("0220").put(51, 'C');
        msgElementDefinition.get("0220").put(60, 'C');
        msgElementDefinition.get("0220").put(90, 'C');
        msgElementDefinition.get("0220").put(100, 'C');
        //msgElementDefinition.get("0220").put(121, 'C');
        //msgElementDefinition.get("0220").put(122, 'C');
        //msgElementDefinition.get("0220").put(128, 'C');

        //Message Element Definition for 0230
        msgElementDefinition.put("0230", new HashMap<Integer, Character>());
        msgElementDefinition.get("0230").put(2, 'M');
        msgElementDefinition.get("0230").put(3, 'M');
        msgElementDefinition.get("0230").put(4, 'M');
        msgElementDefinition.get("0230").put(5, 'C');
        msgElementDefinition.get("0230").put(6, 'C');
        msgElementDefinition.get("0230").put(7, 'M');
        //msgElementDefinition.get("0230").put(9, 'C');
        msgElementDefinition.get("0230").put(10, 'C');
        msgElementDefinition.get("0230").put(11, 'M');
        msgElementDefinition.get("0230").put(12, 'M');
        msgElementDefinition.get("0230").put(13, 'M');
        msgElementDefinition.get("0230").put(15, 'C');
        msgElementDefinition.get("0230").put(16, 'C');
        msgElementDefinition.get("0230").put(18, 'C');
        //msgElementDefinition.get("0230").put(19, 'M');
        msgElementDefinition.get("0230").put(23, 'C');
        msgElementDefinition.get("0230").put(25, 'M');
        msgElementDefinition.get("0230").put(32, 'M');
        msgElementDefinition.get("0230").put(33, 'C');
        msgElementDefinition.get("0230").put(37, 'M');
        msgElementDefinition.get("0230").put(38, 'C');
        msgElementDefinition.get("0230").put(39, 'M');
        msgElementDefinition.get("0230").put(41, 'M');
        msgElementDefinition.get("0230").put(42, 'M');
        msgElementDefinition.get("0230").put(44, 'C');
        msgElementDefinition.get("0230").put(49, 'M');
        msgElementDefinition.get("0230").put(50, 'C');
        msgElementDefinition.get("0230").put(51, 'C');
        msgElementDefinition.get("0230").put(57, 'C');
        msgElementDefinition.get("0230").put(60, 'C');
        msgElementDefinition.get("0230").put(100, 'C');
        //msgElementDefinition.get("0230").put(121, 'C');
        //msgElementDefinition.get("0230").put(122, 'C');
        //msgElementDefinition.get("0230").put(128, 'C');

        //Message Element Definition for 0600
        msgElementDefinition.put("0600", new HashMap<Integer, Character>());
        msgElementDefinition.get("0600").put(4, 'M');
        msgElementDefinition.get("0600").put(7, 'M');
        msgElementDefinition.get("0600").put(11, 'M');
        msgElementDefinition.get("0600").put(32, 'M');
        msgElementDefinition.get("0600").put(33, 'C');
        msgElementDefinition.get("0600").put(49, 'M');
        msgElementDefinition.get("0600").put(51, 'M');
        msgElementDefinition.get("0600").put(70, 'M');

        //Message Element Definition for 0610
        msgElementDefinition.put("0610", new HashMap<Integer, Character>());
        msgElementDefinition.get("0610").put(4, 'M');
        msgElementDefinition.get("0610").put(6, 'M');
        msgElementDefinition.get("0610").put(7, 'M');
        msgElementDefinition.get("0610").put(10, 'M');
        msgElementDefinition.get("0610").put(11, 'M');
        msgElementDefinition.get("0610").put(16, 'M');
        msgElementDefinition.get("0610").put(32, 'M');
        msgElementDefinition.get("0610").put(33, 'C');
        msgElementDefinition.get("0610").put(39, 'M');
        msgElementDefinition.get("0610").put(49, 'M');
        msgElementDefinition.get("0610").put(51, 'M');
        msgElementDefinition.get("0610").put(70, 'M');

        //Message Element Definition for 0620
        msgElementDefinition.put("0620", new HashMap<Integer, Character>());
        msgElementDefinition.get("0620").put(2, 'M');
        msgElementDefinition.get("0620").put(3, 'C');
        msgElementDefinition.get("0620").put(4, 'C');
        msgElementDefinition.get("0620").put(7, 'M');
        msgElementDefinition.get("0620").put(11, 'M');
        msgElementDefinition.get("0620").put(12, 'M');
        msgElementDefinition.get("0620").put(13, 'M');
        msgElementDefinition.get("0620").put(15, 'C');
        msgElementDefinition.get("0620").put(18, 'C');
        msgElementDefinition.get("0620").put(19, 'C');
        msgElementDefinition.get("0620").put(22, 'C');
        msgElementDefinition.get("0620").put(23, 'C');
        msgElementDefinition.get("0620").put(25, 'C');
        msgElementDefinition.get("0620").put(32, 'M');
        msgElementDefinition.get("0620").put(33, 'C');
        msgElementDefinition.get("0620").put(37, 'C');
        msgElementDefinition.get("0620").put(38, 'C');
        msgElementDefinition.get("0620").put(39, 'M');
        msgElementDefinition.get("0620").put(41, 'M');
        msgElementDefinition.get("0620").put(42, 'M');
        msgElementDefinition.get("0620").put(43, 'M');
        msgElementDefinition.get("0620").put(48, 'C');
        msgElementDefinition.get("0620").put(49, 'C');
        msgElementDefinition.get("0620").put(55, 'C');
        msgElementDefinition.get("0620").put(60, 'C');
        msgElementDefinition.get("0620").put(70, 'M');
        msgElementDefinition.get("0620").put(90, 'M');
        msgElementDefinition.get("0620").put(100, 'C');

        //Message Element Definition for 0630
        msgElementDefinition.put("0630", new HashMap<Integer, Character>());
        msgElementDefinition.get("0630").put(2, 'M');
        msgElementDefinition.get("0630").put(3, 'C');
        msgElementDefinition.get("0630").put(4, 'C');
        msgElementDefinition.get("0630").put(7, 'M');
        msgElementDefinition.get("0630").put(11, 'M');
        msgElementDefinition.get("0630").put(12, 'C');
        msgElementDefinition.get("0630").put(13, 'C');
        msgElementDefinition.get("0630").put(15, 'M');
        msgElementDefinition.get("0630").put(23, 'C');
        msgElementDefinition.get("0630").put(25, 'C');
        msgElementDefinition.get("0630").put(32, 'M');
        msgElementDefinition.get("0630").put(33, 'C');
        msgElementDefinition.get("0630").put(37, 'C');
        msgElementDefinition.get("0630").put(39, 'M');
        msgElementDefinition.get("0630").put(41, 'C');
        msgElementDefinition.get("0630").put(42, 'C');
        msgElementDefinition.get("0630").put(60, 'C');
        msgElementDefinition.get("0630").put(70, 'M');
        msgElementDefinition.get("0630").put(100, 'M');
    }
    transient Logger logger = Logger.getLogger(MasterCard87ProtocolDialog.class);

	//Raza MasterCard commenitng start
    //private static final int[] msg200 = new int[]{2, 3, 4, 6, 7,/*10,*/11, 12, 13, 14, /*15,*/ 17,/*18,*/25, 32, 33, 35, 37, 41, 42, 43, 48, 49, 51, 52, 100/*,64*/};	//Mirkamali(Task154)
    //private static final int[] msg100 = new int[]{2, 3, 7,/*10,*/11, 12, 13, 15, 17,/*18,*/25, 32, 33, 37, 41, 42, 48, /*51,*/100/*,64*/};
    //private static final int[] msg210 = new int[]{2, 3, 4, 6, 7, 11, 12, 13, 15, 32, 33, 35, 37, 38, 39, 41, 43, 44, 49, 51, 54, 100/*,64*/};
    //private static final int[] msg400 = new int[]{2, 3, 4, 6, 7, 10, 11, 12, 13, /*15,*/ 17, 32, 33, 35, 37, 38, 39, 41, 42, 43, 48, 49, 51, 90, 95, 100/*,128*/};
    //private static final int[] msg410 = new int[]{2, 3, 4, 6, 7, 11, 12, 13, 15, 32, 33, 37, 39, 42, 48, 51, 54/*,64*/, 100};
    //private static final int[] msg800 = new int[]{7, 11, 15, 32, 33, 48, 53, 70, 96, 128};
    //private static final int[] msg810 = new int[]{7, 11, 15, 32, 33, 39, 48, 70, 96, 128};
    //private static final int[] msg500 = new int[]{7, 11, 15, 17, 32, 33, 50, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 97, 99, 124};
	//Raza MasterCard commenting end
    
    @Override
    public Ifx refine(Ifx ifx) {
        return ifx;
    }

    @Override
    public ProtocolMessage refine(ProtocolMessage protocolMessage) {

        ISOMsg isoMsg = (ISOMsg) protocolMessage;
        ArrayList<Integer> removedFields = new ArrayList<Integer>();
        ArrayList<Integer> neededFields = new ArrayList<Integer>();
        int[] msg = null;

        try {
            String mtiStr = isoMsg.getMTI();
            //System.out.println("Received MTI [" + mtiStr + "]"); //Raza TEMP

            //System.out.println("Field 100 [" + isoMsg.getString(100) + "]"); //Raza TEMP

            //if(mtiStr.length() == 3) //For MTI when it was int of length 3
            //{
            //  mtiStr = "0" + mtiStr;
            //  System.out.println("Mapped MTI [" + mtiStr + "]");
            //}

            for (int i = 2; i <= 128; i++) { //field counter
                //System.out.println("MasterCard87ProtocolDialog Iteration [" + i + "]"); //Raza TEMP
                if (isoMsg.hasField(i) && !msgElementDefinition.get(mtiStr).containsKey(i)) {
                    isoMsg.unset(i);//unset fld i
                    //System.out.println("Removing Field [" + i + "] as Not Required");
                    //System.out.println("isoMsg.getString(i) [" + isoMsg.getString(i) + "]"); //Raza TEMP
                    removedFields.add(i);
                } else if (!isoMsg.hasField(i) && msgElementDefinition.get(mtiStr).containsKey(i)) {
                    if (msgElementDefinition.get(mtiStr).get(i) == 'M') {
                        //System.out.println("Adding Field [" + i + "] as Needed Mandatory");
                        neededFields.add(i);
                    }
                }
            }

            /*if(mtiStr == "0210") {
                System.out.println("Making Remove Field size - for Testing"); //Raza TEMP
                removedFields.add(1); //Raza TEMP
            }*/


            if (removedFields.size() != 0 || neededFields.size() != 0) {
                if (removedFields.size() != 0) {
                    logger.error("Message does have fields " + removedFields.toString() + " but it should not. Error occurred.");
                }
                if (neededFields.size() != 0) {
                    logger.error("Message doesn't have fields " + neededFields.toString() + " but it should have. Error occurred..");
                }

                //set message status
                isoMsg.setMessageStatus(ISOMsg.INVALID);
                //isoMsg.setResponseMTI();
            }
            else
            {
                logger.info("Message fields validated successfully!");
            }
			/* Raza MasterCard commenitng start
            Integer mti = Integer.parseInt(mtiStr);
            switch (mti) {
            	case 100:
            	case 101:
            		msg = msg100;
            		break;
                case 200:
                case 201:
                    msg = msg200;
                    break;
                case 210:
                case 211:
                    msg = msg210;
                    break;
                case 400:
                case 420:
                    msg = msg400;
                    break;
                case 410:
                case 430:
                    msg = msg410;
                    break;
                case 800:
                case 820:
                	msg = msg800;
                	break;
                case 810:
                case 830:
                	msg = msg810;
                	break;
                case 500:
                case 502:
                case 520:
                case 522:
                	msg = msg500;
                default:
                    break;
            }*/ //Raza MasterCard commenting end

//            ArrayList<Integer> removedFields = new ArrayList<Integer>();
//            ArrayList<Integer> neededFields = new ArrayList<Integer>();

//            int k = 0; 
            
//            for (int i:notInMsg200) {
//            	if(isoMsg.hasField(i)){
//                  isoMsg.unset(i);
//                  removedFields.add(i);
//            	}
//            }
            //Raza MasterCard commenting start
            /*int k=0;
            for (int i = 2; i < 128; i++) {
            	if(k < msg.length && i == msg[k]){
            		k++;
            	}else{
            		if(isoMsg.hasField(i)){
	            		isoMsg.unset(i);
//	            		removedFields.add(i);
            		}
            	}
            }*/
            //Raza MasterCard commenting end
//            for (int i = 2; i < 64; i++) { 
//                if (isoMsg.hasField(i) && (k >= msg.length || msg[k] != i)) {//msg has fld i but msg says no
//                    isoMsg.unset(i);
//                    removedFields.add(i);
//                }
////                if (!isoMsg.hasField(i) && (k < msg.length && msg[k] == i)) {
////                    boolean result = setField(isoMsg, i);//set fld i
////                    neededFields.add(i);
////                }
//                
//                if (!isoMsg.hasField(i) && (k >= msg.length || msg[k] != i)) {//msg does not have fld i and msg says no
//                } else {////msg has fld i and msg says yes
//                    k++;
//                }
//            }
//
//            int i = 90;
//            if (isoMsg.hasField(i) && (k >= msg.length || msg[k] != i)) {//msg has fld i but msg says no
//                isoMsg.unset(i);//unset fld i
//                removedFields.add(i);
//            }
////            if (!isoMsg.hasField(i) && (k < msg.length && msg[k] == i)) {
////                boolean result = setField(isoMsg, i);//set fld i
////                neededFields.add(i);
////            }
//            
//            if (!isoMsg.hasField(i) && (k >= msg.length || msg[k] != i)) {//msg does not have fld i and msg says no
//            } else {////msg has fld i and msg says yes
//                k++;
//            }
//            
//            i = 95;
//            if (isoMsg.hasField(i) && (k >= msg.length || msg[k] != i)) {//msg has fld i but msg says no
//                isoMsg.unset(i);//unset fld i
//                removedFields.add(i);
//            }
////            if (!isoMsg.hasField(i) && (k < msg.length && msg[k] == i)) {
////                //msg does not have fld i but msg says yes
////                boolean result = setField(isoMsg, i);//set fld i
////                neededFields.add(i);
////            }
//            
//            if (!isoMsg.hasField(i) && (k >= msg.length || msg[k] != i)) {//msg does not have fld i and msg says no
//            } else {////msg has fld i and msg says yes
//                k++;
//            }
//                
//            i = 100;
//            if (isoMsg.hasField(i) && (k >= msg.length || msg[k] != i)) {//msg has fld i but msg says no
//            	logger.error("Message has field " + i + " but msg says no. msg.length "+msg.length+" K:"+k+"msg[k]:"+msg[k]);
//
//            	isoMsg.unset(i);//unset fld i
//            	removedFields.add(i);
//            }
////            if (!isoMsg.hasField(i) && (k < msg.length && msg[k] == i)) {
////            	//msg does not have fld i but msg says yes
////            	boolean result = setField(isoMsg, i);//set fld i
////            	neededFields.add(i);
////            }
//            
//            if (!isoMsg.hasField(i) && (k >= msg.length || msg[k] != i)) {//msg does not have fld i and msg says no
//            } else {////msg has fld i and msg says yes
//            	k++;
//            }
            
//            if (neededFields.size() != 0)
//                logger.warn("Message doesn't have fields " + neededFields.toString() + " but it should have. Switch didn't add anything.");
            
//            if (removedFields.size() != 0)
//                logger.warn("Message does    have fields " + removedFields.toString() + " but it should not. Switch removed them.");

            return protocolMessage;
        } catch (Exception ex) {
            ex.printStackTrace();
        	return null;
        }
    }
//    private boolean setField(ISOMsg msg, int fldno) throws Exception {
//        return false;
//    }

    ////Raza Adding for Field traslation start
    @Override
    public ProtocolMessage TranslateToFanap(ProtocolMessage protocolMessage) throws Exception
    {
        //ISOMsg inISOMsg = (ISOMsg) protocolMessage;

        logger.info("Translating incoming message from MasterCard...");

        //System.out.println("Mapping Fields for MasterCard...");
        try {
            if (((ISOMsg)protocolMessage).isResponse()) {
                ((ISOMsg)protocolMessage).unset(12); //Time-Local-Tran
                ((ISOMsg)protocolMessage).unset(13); //Date-Loc-Tran
                ((ISOMsg)protocolMessage).unset(14); //Date-Expiry
                ((ISOMsg)protocolMessage).unset(25); //POS-Cond-Code
                ((ISOMsg)protocolMessage).unset(42); //Card-Accept-Id-Code
                ((ISOMsg)protocolMessage).unset(64); //Message-Auth-Code
                ((ISOMsg)protocolMessage).unset(100); //Receiving-Inst-Id-Code
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        logger.info("Translating incoming message from MasterCard Done..!");

        return protocolMessage;
    }

    @Override
    public ProtocolMessage TranslateFromFanap(ProtocolMessage protocolMessage) throws Exception
    {
        //ISOMsg inISOMsg = (ISOMsg) protocolMessage;
        logger.info("Translating outgoing message for MasterCard...");
        //System.out.println("Mapping Fields for MasterCard...");

        try {
            if (((ISOMsg)protocolMessage).isResponse()) {
                ((ISOMsg)protocolMessage).unset(12); //Time-Local-Tran
                ((ISOMsg)protocolMessage).unset(13); //Date-Loc-Tran
                ((ISOMsg)protocolMessage).unset(14); //Date-Expiry
                ((ISOMsg)protocolMessage).unset(25); //POS-Cond-Code
                ((ISOMsg)protocolMessage).unset(42); //Card-Accept-Id-Code
                ((ISOMsg)protocolMessage).unset(64); //Message-Auth-Code
                ((ISOMsg)protocolMessage).unset(100); //Receiving-Inst-Id-Code
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        logger.info("Translating outgoing message for MasterCard Done...");
        return protocolMessage;
    }
    ////Raza Adding for Field traslation end
}
