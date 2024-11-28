package vaulsys.protocols.PaymentSchemes.UnionPay;

import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by m.rehman on 4/9/2016.
 */

public class UnionPayProtocolDialog implements ProtocolDialog {

    private Map<String, Map<Integer, Character>> msgElementDefinition;

    transient Logger logger = Logger.getLogger(UnionPayProtocolDialog.class);

    UnionPayProtocolDialog() {
        msgElementDefinition = new HashMap<String, Map<Integer, Character>>();

        //load entries from db
        //LoadFromDb();

        //Message Element Definition for 0820
        msgElementDefinition.put("0820", new HashMap<Integer, Character>());
        msgElementDefinition.get("0820").put(7, 'M');
        msgElementDefinition.get("0820").put(11, 'M');
        msgElementDefinition.get("0820").put(15, 'C');
        msgElementDefinition.get("0820").put(33, 'C');
        msgElementDefinition.get("0820").put(48, 'C');
        msgElementDefinition.get("0820").put(53, 'C');
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

        //Message Element Definition for 0800
        msgElementDefinition.put("0800", new HashMap<Integer, Character>());
        msgElementDefinition.get("0800").put(7, 'M');
        msgElementDefinition.get("0800").put(11, 'M');
        msgElementDefinition.get("0800").put(48, 'C');
        msgElementDefinition.get("0800").put(53, 'C');
        msgElementDefinition.get("0800").put(70, 'M');
        msgElementDefinition.get("0800").put(96, 'C');
        msgElementDefinition.get("0800").put(100, 'C');
        msgElementDefinition.get("0800").put(128, 'C');

        //Message Element Definition for 0810
        msgElementDefinition.put("0810", new HashMap<Integer, Character>());
        msgElementDefinition.get("0810").put(7, 'M');
        msgElementDefinition.get("0810").put(11, 'M');
        msgElementDefinition.get("0810").put(39, 'M');
        msgElementDefinition.get("0810").put(53, 'C');
        msgElementDefinition.get("0810").put(70, 'M');
        msgElementDefinition.get("0810").put(100, 'C');
        msgElementDefinition.get("0810").put(128, 'C');

        //Message Element Definition for 0200
        msgElementDefinition.put("0200", new HashMap<Integer, Character>());
        msgElementDefinition.get("0200").put(2, 'M');
        msgElementDefinition.get("0200").put(3, 'M');
        msgElementDefinition.get("0200").put(4, 'C');
        msgElementDefinition.get("0200").put(5, 'C');
        msgElementDefinition.get("0200").put(6, 'C');
        msgElementDefinition.get("0200").put(7, 'M');
        msgElementDefinition.get("0200").put(9, 'C');
        msgElementDefinition.get("0200").put(10, 'C');
        msgElementDefinition.get("0200").put(11, 'M');
        msgElementDefinition.get("0200").put(12, 'M');
        msgElementDefinition.get("0200").put(13, 'M');
        msgElementDefinition.get("0200").put(14, 'C');
        msgElementDefinition.get("0200").put(15, 'C');
        msgElementDefinition.get("0200").put(16, 'C');
        msgElementDefinition.get("0200").put(18, 'M');
        msgElementDefinition.get("0200").put(19, 'M');
        msgElementDefinition.get("0200").put(22, 'M');
        msgElementDefinition.get("0200").put(23, 'C');
        msgElementDefinition.get("0200").put(25, 'M');
        msgElementDefinition.get("0200").put(26, 'C');
        msgElementDefinition.get("0200").put(28, 'C');
        msgElementDefinition.get("0200").put(32, 'C');
        msgElementDefinition.get("0200").put(33, 'M');
        msgElementDefinition.get("0200").put(35, 'C');
        msgElementDefinition.get("0200").put(36, 'C');
        msgElementDefinition.get("0200").put(37, 'M');
        msgElementDefinition.get("0200").put(38, 'C');
        msgElementDefinition.get("0200").put(41, 'M');
        msgElementDefinition.get("0200").put(42, 'M');
        msgElementDefinition.get("0200").put(43, 'M');
        msgElementDefinition.get("0200").put(45, 'C');
        msgElementDefinition.get("0200").put(48, 'C');
        msgElementDefinition.get("0200").put(49, 'C');
        msgElementDefinition.get("0200").put(50, 'C');
        msgElementDefinition.get("0200").put(51, 'C');
        msgElementDefinition.get("0200").put(52, 'C');
        msgElementDefinition.get("0200").put(53, 'C');
        msgElementDefinition.get("0200").put(55, 'C');
        msgElementDefinition.get("0200").put(60, 'C');
        msgElementDefinition.get("0200").put(61, 'C');
        msgElementDefinition.get("0200").put(90, 'C');
        msgElementDefinition.get("0200").put(100, 'C');
        msgElementDefinition.get("0200").put(121, 'C');
        msgElementDefinition.get("0200").put(122, 'C');
        msgElementDefinition.get("0200").put(128, 'C');

        //Message Element Definition for 0210
        msgElementDefinition.put("0210", new HashMap<Integer, Character>());
        msgElementDefinition.get("0210").put(2, 'M');
        msgElementDefinition.get("0210").put(3, 'M');
        msgElementDefinition.get("0210").put(4, 'C');
        msgElementDefinition.get("0210").put(5, 'C');
        msgElementDefinition.get("0210").put(6, 'C');
        msgElementDefinition.get("0210").put(7, 'M');
        msgElementDefinition.get("0210").put(9, 'C');
        msgElementDefinition.get("0210").put(10, 'C');
        msgElementDefinition.get("0210").put(11, 'M');
        msgElementDefinition.get("0210").put(12, 'M');
        msgElementDefinition.get("0210").put(13, 'M');
        msgElementDefinition.get("0210").put(14, 'C');
        msgElementDefinition.get("0210").put(15, 'C');
        msgElementDefinition.get("0210").put(16, 'C');
        msgElementDefinition.get("0210").put(18, 'M');
        msgElementDefinition.get("0210").put(19, 'M');
        msgElementDefinition.get("0210").put(23, 'C');
        msgElementDefinition.get("0210").put(25, 'M');
        msgElementDefinition.get("0210").put(28, 'C');
        msgElementDefinition.get("0210").put(32, 'M');
        msgElementDefinition.get("0210").put(33, 'M');
        msgElementDefinition.get("0210").put(37, 'M');
        msgElementDefinition.get("0210").put(38, 'C');
        msgElementDefinition.get("0210").put(39, 'M');
        msgElementDefinition.get("0210").put(41, 'M');
        msgElementDefinition.get("0210").put(42, 'M');
        msgElementDefinition.get("0210").put(44, 'C');
        msgElementDefinition.get("0210").put(49, 'C');
        msgElementDefinition.get("0210").put(50, 'C');
        msgElementDefinition.get("0210").put(51, 'C');
        msgElementDefinition.get("0210").put(54, 'C');
        msgElementDefinition.get("0210").put(55, 'C');
        msgElementDefinition.get("0210").put(57, 'C');
        msgElementDefinition.get("0210").put(60, 'C');
        msgElementDefinition.get("0210").put(61, 'C');
        msgElementDefinition.get("0210").put(100, 'C');
        msgElementDefinition.get("0210").put(121, 'C');
        msgElementDefinition.get("0210").put(122, 'C');
        msgElementDefinition.get("0210").put(123, 'C');
        msgElementDefinition.get("0210").put(128, 'C');

        //Message Element Definition for 0100
        msgElementDefinition.put("0100", new HashMap<Integer, Character>());
        msgElementDefinition.get("0100").put(2, 'M');
        msgElementDefinition.get("0100").put(3, 'M');
        msgElementDefinition.get("0100").put(4, 'M');
        msgElementDefinition.get("0100").put(6, 'C');
        msgElementDefinition.get("0100").put(7, 'M');
        msgElementDefinition.get("0100").put(10, 'C');
        msgElementDefinition.get("0100").put(11, 'M');
        msgElementDefinition.get("0100").put(12, 'M');
        msgElementDefinition.get("0100").put(13, 'M');
        msgElementDefinition.get("0100").put(14, 'C');
        msgElementDefinition.get("0100").put(15, 'C');
        msgElementDefinition.get("0100").put(18, 'M');
        msgElementDefinition.get("0100").put(19, 'M');
        msgElementDefinition.get("0100").put(22, 'M');
        msgElementDefinition.get("0100").put(23, 'C');
        msgElementDefinition.get("0100").put(25, 'M');
        msgElementDefinition.get("0100").put(26, 'C');
        msgElementDefinition.get("0100").put(32, 'M');
        msgElementDefinition.get("0100").put(33, 'M');
        msgElementDefinition.get("0100").put(35, 'C');
        msgElementDefinition.get("0100").put(36, 'C');
        msgElementDefinition.get("0100").put(37, 'M');
        msgElementDefinition.get("0100").put(38, 'C');
        msgElementDefinition.get("0100").put(41, 'M');
        msgElementDefinition.get("0100").put(42, 'M');
        msgElementDefinition.get("0100").put(43, 'M');
        msgElementDefinition.get("0100").put(45, 'C');
        msgElementDefinition.get("0100").put(48, 'C');
        msgElementDefinition.get("0100").put(49, 'M');
        msgElementDefinition.get("0100").put(51, 'C');
        msgElementDefinition.get("0100").put(52, 'C');
        msgElementDefinition.get("0100").put(53, 'C');
        msgElementDefinition.get("0100").put(55, 'C');
        msgElementDefinition.get("0100").put(60, 'C');
        msgElementDefinition.get("0100").put(61, 'C');
        msgElementDefinition.get("0100").put(90, 'C');
        msgElementDefinition.get("0100").put(100, 'C');
        msgElementDefinition.get("0100").put(121, 'C');
        msgElementDefinition.get("0100").put(122, 'C');
        msgElementDefinition.get("0100").put(128, 'C');

        //Message Element Definition for 0110
        msgElementDefinition.put("0110", new HashMap<Integer, Character>());
        msgElementDefinition.get("0110").put(2, 'M');
        msgElementDefinition.get("0110").put(3, 'M');
        msgElementDefinition.get("0110").put(4, 'M');
        msgElementDefinition.get("0110").put(6, 'C');
        msgElementDefinition.get("0110").put(7, 'M');
        msgElementDefinition.get("0110").put(10, 'C');
        msgElementDefinition.get("0110").put(11, 'M');
        msgElementDefinition.get("0110").put(12, 'M');
        msgElementDefinition.get("0110").put(13, 'M');
        msgElementDefinition.get("0110").put(14, 'M');
        msgElementDefinition.get("0110").put(15, 'M');
        msgElementDefinition.get("0110").put(18, 'M');
        msgElementDefinition.get("0110").put(19, 'M');
        msgElementDefinition.get("0110").put(23, 'C');
        msgElementDefinition.get("0110").put(25, 'M');
        msgElementDefinition.get("0110").put(32, 'M');
        msgElementDefinition.get("0110").put(33, 'M');
        msgElementDefinition.get("0110").put(37, 'M');
        msgElementDefinition.get("0110").put(38, 'C');
        msgElementDefinition.get("0110").put(39, 'M');
        msgElementDefinition.get("0110").put(41, 'M');
        msgElementDefinition.get("0110").put(42, 'M');
        msgElementDefinition.get("0110").put(44, 'C');
        msgElementDefinition.get("0110").put(49, 'C');
        msgElementDefinition.get("0110").put(51, 'C');
        msgElementDefinition.get("0110").put(54, 'C');
        msgElementDefinition.get("0110").put(55, 'C');
        msgElementDefinition.get("0110").put(57, 'C');
        msgElementDefinition.get("0110").put(60, 'C');
        msgElementDefinition.get("0110").put(61, 'C');
        msgElementDefinition.get("0110").put(100, 'M');
        msgElementDefinition.get("0110").put(121, 'C');
        msgElementDefinition.get("0110").put(122, 'C');
        msgElementDefinition.get("0110").put(123, 'C');
        msgElementDefinition.get("0110").put(128, 'C');

        //Message Element Definition for 0420
        msgElementDefinition.put("0420", new HashMap<Integer, Character>());
        msgElementDefinition.get("0420").put(2, 'M');
        msgElementDefinition.get("0420").put(3, 'M');
        msgElementDefinition.get("0420").put(4, 'M');
        msgElementDefinition.get("0420").put(5, 'C');
        msgElementDefinition.get("0420").put(6, 'C');
        msgElementDefinition.get("0420").put(7, 'M');
        msgElementDefinition.get("0420").put(9, 'C');
        msgElementDefinition.get("0420").put(10, 'C');
        msgElementDefinition.get("0420").put(11, 'M');
        msgElementDefinition.get("0420").put(12, 'M');
        msgElementDefinition.get("0420").put(13, 'M');
        msgElementDefinition.get("0420").put(15, 'C');
        msgElementDefinition.get("0420").put(16, 'C');
        msgElementDefinition.get("0420").put(18, 'M');
        msgElementDefinition.get("0420").put(19, 'C');
        msgElementDefinition.get("0420").put(22, 'M');
        msgElementDefinition.get("0420").put(23, 'C');
        msgElementDefinition.get("0420").put(25, 'M');
        msgElementDefinition.get("0420").put(32, 'M');
        msgElementDefinition.get("0420").put(33, 'M');
        msgElementDefinition.get("0420").put(37, 'M');
        msgElementDefinition.get("0420").put(38, 'C');
        msgElementDefinition.get("0420").put(41, 'M');
        msgElementDefinition.get("0420").put(42, 'M');
        msgElementDefinition.get("0420").put(43, 'M');
        msgElementDefinition.get("0420").put(44, 'C');
        msgElementDefinition.get("0420").put(49, 'C');
        msgElementDefinition.get("0420").put(50, 'C');
        msgElementDefinition.get("0420").put(51, 'C');
        msgElementDefinition.get("0420").put(55, 'C');
        msgElementDefinition.get("0420").put(60, 'C');
        msgElementDefinition.get("0420").put(90, 'C');
        msgElementDefinition.get("0420").put(100, 'C');
        msgElementDefinition.get("0420").put(121, 'C');
        msgElementDefinition.get("0420").put(122, 'C');
        msgElementDefinition.get("0420").put(128, 'C');

        //Message Element Definition for 0430
        msgElementDefinition.put("0430", new HashMap<Integer, Character>());
        msgElementDefinition.get("0430").put(2, 'M');
        msgElementDefinition.get("0430").put(3, 'M');
        msgElementDefinition.get("0430").put(4, 'M');
        msgElementDefinition.get("0430").put(5, 'C');
        msgElementDefinition.get("0430").put(7, 'M');
        msgElementDefinition.get("0430").put(9, 'C');
        msgElementDefinition.get("0430").put(11, 'M');
        msgElementDefinition.get("0430").put(12, 'M');
        msgElementDefinition.get("0430").put(13, 'M');
        msgElementDefinition.get("0430").put(15, 'M');
        msgElementDefinition.get("0430").put(16, 'C');
        msgElementDefinition.get("0430").put(18, 'M');
        msgElementDefinition.get("0430").put(19, 'C');
        msgElementDefinition.get("0430").put(23, 'C');
        msgElementDefinition.get("0430").put(25, 'M');
        msgElementDefinition.get("0430").put(32, 'M');
        msgElementDefinition.get("0430").put(33, 'M');
        msgElementDefinition.get("0430").put(37, 'M');
        msgElementDefinition.get("0430").put(39, 'M');
        msgElementDefinition.get("0430").put(41, 'M');
        msgElementDefinition.get("0430").put(42, 'M');
        msgElementDefinition.get("0430").put(49, 'C');
        msgElementDefinition.get("0430").put(50, 'C');
        msgElementDefinition.get("0430").put(55, 'C');
        msgElementDefinition.get("0430").put(60, 'C');
        msgElementDefinition.get("0430").put(100, 'M');
        msgElementDefinition.get("0430").put(121, 'C');
        msgElementDefinition.get("0430").put(122, 'C');
        msgElementDefinition.get("0430").put(123, 'C');
        msgElementDefinition.get("0430").put(128, 'C');

        //Message Element Definition for 0220
        msgElementDefinition.put("0220", new HashMap<Integer, Character>());
        msgElementDefinition.get("0220").put(2, 'M');
        msgElementDefinition.get("0220").put(3, 'M');
        msgElementDefinition.get("0220").put(4, 'M');
        msgElementDefinition.get("0220").put(5, 'C');
        msgElementDefinition.get("0220").put(6, 'C');
        msgElementDefinition.get("0220").put(7, 'M');
        msgElementDefinition.get("0220").put(9, 'C');
        msgElementDefinition.get("0220").put(10, 'C');
        msgElementDefinition.get("0220").put(11, 'M');
        msgElementDefinition.get("0220").put(12, 'M');
        msgElementDefinition.get("0220").put(13, 'M');
        msgElementDefinition.get("0220").put(14, 'C');
        msgElementDefinition.get("0220").put(15, 'C');
        msgElementDefinition.get("0220").put(16, 'C');
        msgElementDefinition.get("0220").put(18, 'M');
        msgElementDefinition.get("0220").put(19, 'M');
        msgElementDefinition.get("0220").put(22, 'M');
        msgElementDefinition.get("0220").put(23, 'C');
        msgElementDefinition.get("0220").put(25, 'M');
        msgElementDefinition.get("0220").put(32, 'M');
        msgElementDefinition.get("0220").put(33, 'M');
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
        msgElementDefinition.get("0220").put(121, 'C');
        msgElementDefinition.get("0220").put(122, 'C');
        msgElementDefinition.get("0220").put(128, 'C');

        //Message Element Definition for 0230
        msgElementDefinition.put("0230", new HashMap<Integer, Character>());
        msgElementDefinition.get("0230").put(2, 'M');
        msgElementDefinition.get("0230").put(3, 'M');
        msgElementDefinition.get("0230").put(4, 'M');
        msgElementDefinition.get("0230").put(5, 'C');
        msgElementDefinition.get("0230").put(6, 'C');
        msgElementDefinition.get("0230").put(7, 'M');
        msgElementDefinition.get("0230").put(9, 'C');
        msgElementDefinition.get("0230").put(10, 'C');
        msgElementDefinition.get("0230").put(11, 'M');
        msgElementDefinition.get("0230").put(12, 'M');
        msgElementDefinition.get("0230").put(13, 'M');
        msgElementDefinition.get("0230").put(15, 'M');
        msgElementDefinition.get("0230").put(16, 'C');
        msgElementDefinition.get("0230").put(18, 'M');
        msgElementDefinition.get("0230").put(19, 'M');
        msgElementDefinition.get("0230").put(23, 'C');
        msgElementDefinition.get("0230").put(25, 'M');
        msgElementDefinition.get("0230").put(32, 'M');
        msgElementDefinition.get("0230").put(33, 'M');
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
        msgElementDefinition.get("0230").put(121, 'C');
        msgElementDefinition.get("0230").put(122, 'C');
        msgElementDefinition.get("0230").put(128, 'C');

        //Message Element Definition for 0600
        msgElementDefinition.put("0600", new HashMap<Integer, Character>());
        msgElementDefinition.get("0600").put(4, 'M');
        msgElementDefinition.get("0600").put(7, 'M');
        msgElementDefinition.get("0600").put(11, 'M');
        msgElementDefinition.get("0600").put(32, 'M');
        msgElementDefinition.get("0600").put(33, 'M');
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
        msgElementDefinition.get("0610").put(33, 'M');
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
        msgElementDefinition.get("0620").put(33, 'M');
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
        msgElementDefinition.get("0630").put(33, 'M');
        msgElementDefinition.get("0630").put(37, 'C');
        msgElementDefinition.get("0630").put(39, 'M');
        msgElementDefinition.get("0630").put(41, 'C');
        msgElementDefinition.get("0630").put(42, 'C');
        msgElementDefinition.get("0630").put(60, 'C');
        msgElementDefinition.get("0630").put(70, 'M');
        msgElementDefinition.get("0630").put(100, 'M');
    }

    /*
    private void LoadFromDb() {
        Map<String, Object> dbParam;
        List<ISOMessageDefinition> messageDefList;
        String query, messageType;

        try {
            dbParam = new HashMap<String, Object>();

            dbParam.put("channelId", ChannelCodes.UNION_PAY);
            query = "from " + ISOMessageDefinition.class.getName() + " i " +
                    "where i.channelId = :channelId order by messageType";
            messageDefList = GeneralDao.Instance.find(query, dbParam);

            for (ISOMessageDefinition imdf : messageDefList) {
                messageType = imdf.getMessageType();
                if (!msgElementDefinition.containsKey(messageType))
                    msgElementDefinition.put(messageType, new HashMap<String, String>());
                msgElementDefinition.get(messageType).put(imdf.getFieldId(), imdf.getRequire());
            }

        } catch (Exception e) {
            logger.error("Unable to load Message Definition for with error: " + e.getMessage());
        }
    }
    */

    @Override
    public Ifx refine(Ifx ifx) {
        return ifx;
    }

    @Override
    public ProtocolMessage refine(ProtocolMessage protocolMessage) throws Exception {

        ISOMsg isoMsg = (ISOMsg) protocolMessage;
        String mti = isoMsg.getMTI();

        ArrayList<Integer> removedFields = new ArrayList<Integer>();
        ArrayList<Integer> neededFields = new ArrayList<Integer>();

        try {
            for (int i = 2; i < 128; i++) { //field counter
                if (isoMsg.hasField(i) && !msgElementDefinition.get(mti).containsKey(i)) {
                    if (isoMsg.getDirection() == ISOMsg.OUTGOING)
                        isoMsg.unset(i);    //unset fld i
                    removedFields.add(i);
                }
                else if (!isoMsg.hasField(i) && msgElementDefinition.get(mti).containsKey(i)) {
                    if (msgElementDefinition.get(mti).get(i) == 'M') {
                        neededFields.add(i);
                    }
                }
            }

            if (removedFields.size() != 0 || neededFields.size() != 0) {
                if (removedFields.size() != 0) {
                    logger.error("Message does have fields " + removedFields.toString() + " but it should not. Error occurred.");
                }
                if (neededFields.size() != 0) {
                    logger.error("Message doesn't have fields " + neededFields.toString() + " but it should have. Error occurred..");
                }
                //set message status
                isoMsg.setMessageStatus(ISOMsg.INVALID);
            }

            return protocolMessage;
        } catch (Exception ex) {
            if (true)
                return null;
        }

        return null;
    }

    ////Raza Adding for Field traslation start
    @Override
    public ProtocolMessage TranslateToFanap(ProtocolMessage protocolMessage) throws Exception
    {
        //logger.info("Translating incoming message from UnionPay...");
        return protocolMessage;
    }

    @Override
    public ProtocolMessage TranslateFromFanap(ProtocolMessage protocolMessage) throws Exception
    {
        //logger.info("Translating outgoing message for UnionPay...");
        return protocolMessage;
    }
    ////Raza Adding for Field traslation end
}
