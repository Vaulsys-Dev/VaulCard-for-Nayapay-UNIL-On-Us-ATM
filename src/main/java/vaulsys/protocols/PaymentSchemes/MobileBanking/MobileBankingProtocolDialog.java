package vaulsys.protocols.PaymentSchemes.MobileBanking;

import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.imp.Ifx;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by m.rehman on 4/9/2016.
 */

public class MobileBankingProtocolDialog implements ProtocolDialog {

    private Map<String, Map<Integer, Character>> msgElementDefinition;

    transient Logger logger = Logger.getLogger(MobileBankingProtocolDialog.class);

    MobileBankingProtocolDialog() {
        msgElementDefinition = new HashMap<String, Map<Integer, Character>>();

        //Message Element Definition for 0800
        msgElementDefinition.put("0800", new HashMap<Integer, Character>());
        msgElementDefinition.get("0800").put(2, 'C');
        msgElementDefinition.get("0800").put(7, 'M');
        msgElementDefinition.get("0800").put(11, 'M');
        msgElementDefinition.get("0800").put(24, 'M');
        msgElementDefinition.get("0800").put(70, 'M');

        //Message Element Definition for 0810
        msgElementDefinition.put("0810", new HashMap<Integer, Character>());
        msgElementDefinition.get("0800").put(2, 'C');
        msgElementDefinition.get("0810").put(7, 'M');
        msgElementDefinition.get("0810").put(11, 'M');
        msgElementDefinition.get("0810").put(24, 'M');
        msgElementDefinition.get("0810").put(39, 'M');
        msgElementDefinition.get("0810").put(70, 'M');

        //Message Element Definition for 0820
        msgElementDefinition.put("0820", new HashMap<Integer, Character>());
        msgElementDefinition.get("0820").put(7, 'M');
        msgElementDefinition.get("0820").put(11, 'M');
        msgElementDefinition.get("0820").put(15, 'M');
        msgElementDefinition.get("0820").put(24, 'M');
        msgElementDefinition.get("0820").put(48, 'C');
        msgElementDefinition.get("0820").put(70, 'M');

        //Message Element Definition for 0830
        msgElementDefinition.put("0830", new HashMap<Integer, Character>());
        msgElementDefinition.get("0830").put(7, 'M');
        msgElementDefinition.get("0830").put(11, 'M');
        msgElementDefinition.get("0830").put(24, 'M');
        msgElementDefinition.get("0830").put(39, 'M');
        msgElementDefinition.get("0830").put(70, 'M');

        //Message Element Definition for 0200
        msgElementDefinition.put("0200", new HashMap<Integer, Character>());
        msgElementDefinition.get("0200").put(2, 'M');
        msgElementDefinition.get("0200").put(3, 'M');
        msgElementDefinition.get("0200").put(4, 'C');
        msgElementDefinition.get("0200").put(5, 'C');
        msgElementDefinition.get("0200").put(6, 'C');
        msgElementDefinition.get("0200").put(7, 'M');
        msgElementDefinition.get("0200").put(9, 'C');
        msgElementDefinition.get("0200").put(11, 'M');
        msgElementDefinition.get("0200").put(12, 'M');
        msgElementDefinition.get("0200").put(13, 'M');
        msgElementDefinition.get("0200").put(14, 'C');
        msgElementDefinition.get("0200").put(15, 'M');
        msgElementDefinition.get("0200").put(16, 'C');
        msgElementDefinition.get("0200").put(18, 'M');
        msgElementDefinition.get("0200").put(22, 'M');
        msgElementDefinition.get("0200").put(23, 'C');
        msgElementDefinition.get("0200").put(24, 'M');
        msgElementDefinition.get("0200").put(26, 'C');
        msgElementDefinition.get("0200").put(28, 'C');
        msgElementDefinition.get("0200").put(32, 'M');
        msgElementDefinition.get("0200").put(33, 'C');
        msgElementDefinition.get("0200").put(35, 'C');
        msgElementDefinition.get("0200").put(36, 'C');
        msgElementDefinition.get("0200").put(37, 'M');
        msgElementDefinition.get("0200").put(41, 'M');
        msgElementDefinition.get("0200").put(42, 'C');
        msgElementDefinition.get("0200").put(43, 'M');
        msgElementDefinition.get("0200").put(45, 'C');
        msgElementDefinition.get("0200").put(47, 'C');
        msgElementDefinition.get("0200").put(48, 'C');
        msgElementDefinition.get("0200").put(49, 'C');
        msgElementDefinition.get("0200").put(50, 'C');
        msgElementDefinition.get("0200").put(52, 'C');
        msgElementDefinition.get("0200").put(55, 'C');
        msgElementDefinition.get("0200").put(60, 'C');
        msgElementDefinition.get("0200").put(102, 'C');
        msgElementDefinition.get("0200").put(103, 'C');
        msgElementDefinition.get("0200").put(120, 'C');

        //Message Element Definition for 0210
        msgElementDefinition.put("0210", new HashMap<Integer, Character>());
        msgElementDefinition.get("0210").put(2, 'M');
        msgElementDefinition.get("0210").put(3, 'M');
        msgElementDefinition.get("0210").put(4, 'C');
        msgElementDefinition.get("0210").put(5, 'C');
        msgElementDefinition.get("0210").put(6, 'C');
        msgElementDefinition.get("0210").put(7, 'M');
        msgElementDefinition.get("0210").put(9, 'C');
        msgElementDefinition.get("0210").put(11, 'M');
        msgElementDefinition.get("0210").put(12, 'M');
        msgElementDefinition.get("0210").put(13, 'M');
        msgElementDefinition.get("0210").put(14, 'C');
        msgElementDefinition.get("0210").put(15, 'M');
        msgElementDefinition.get("0210").put(16, 'C');
        msgElementDefinition.get("0210").put(18, 'M');
        msgElementDefinition.get("0210").put(24, 'M');
        msgElementDefinition.get("0210").put(28, 'C');
        msgElementDefinition.get("0210").put(32, 'M');
        msgElementDefinition.get("0210").put(33, 'M');
        msgElementDefinition.get("0210").put(37, 'M');
        msgElementDefinition.get("0210").put(38, 'M');
        msgElementDefinition.get("0210").put(39, 'M');
        msgElementDefinition.get("0210").put(41, 'M');
        msgElementDefinition.get("0210").put(42, 'C');
        msgElementDefinition.get("0210").put(49, 'C');
        msgElementDefinition.get("0210").put(50, 'C');
        msgElementDefinition.get("0210").put(54, 'C');
        msgElementDefinition.get("0210").put(55, 'C');
        msgElementDefinition.get("0210").put(60, 'C');
        msgElementDefinition.get("0210").put(102, 'C');
        msgElementDefinition.get("0210").put(103, 'C');
        msgElementDefinition.get("0210").put(120, 'C');

        //Message Element Definition for 0220
        msgElementDefinition.put("0220", new HashMap<Integer, Character>());
        msgElementDefinition.get("0220").put(2, 'M');
        msgElementDefinition.get("0220").put(3, 'M');
        msgElementDefinition.get("0220").put(4, 'M');
        msgElementDefinition.get("0220").put(5, 'C');
        msgElementDefinition.get("0220").put(6, 'C');
        msgElementDefinition.get("0220").put(7, 'M');
        msgElementDefinition.get("0220").put(9, 'C');
        msgElementDefinition.get("0220").put(11, 'M');
        msgElementDefinition.get("0220").put(12, 'M');
        msgElementDefinition.get("0220").put(13, 'M');
        msgElementDefinition.get("0220").put(14, 'C');
        msgElementDefinition.get("0220").put(15, 'C');
        msgElementDefinition.get("0220").put(16, 'C');
        msgElementDefinition.get("0220").put(18, 'M');
        msgElementDefinition.get("0220").put(22, 'M');
        msgElementDefinition.get("0220").put(24, 'M');
        msgElementDefinition.get("0220").put(32, 'M');
        msgElementDefinition.get("0220").put(33, 'M');
        msgElementDefinition.get("0220").put(35, 'C');
        msgElementDefinition.get("0220").put(36, 'C');
        msgElementDefinition.get("0220").put(37, 'M');
        msgElementDefinition.get("0220").put(38, 'C');
        msgElementDefinition.get("0220").put(41, 'M');
        msgElementDefinition.get("0220").put(42, 'C');
        msgElementDefinition.get("0220").put(43, 'M');
        msgElementDefinition.get("0220").put(45, 'C');
        msgElementDefinition.get("0220").put(47, 'C');
        msgElementDefinition.get("0220").put(48, 'C');
        msgElementDefinition.get("0220").put(49, 'M');
        msgElementDefinition.get("0220").put(50, 'C');
        msgElementDefinition.get("0220").put(52, 'C');
        msgElementDefinition.get("0220").put(102, 'C');
        msgElementDefinition.get("0220").put(103, 'C');
        msgElementDefinition.get("0220").put(120, 'C');

        //Message Element Definition for 0230
        msgElementDefinition.put("0230", new HashMap<Integer, Character>());
        msgElementDefinition.get("0230").put(2, 'M');
        msgElementDefinition.get("0230").put(3, 'M');
        msgElementDefinition.get("0230").put(4, 'M');
        msgElementDefinition.get("0230").put(5, 'C');
        msgElementDefinition.get("0230").put(6, 'C');
        msgElementDefinition.get("0230").put(7, 'M');
        msgElementDefinition.get("0230").put(9, 'C');
        msgElementDefinition.get("0230").put(11, 'M');
        msgElementDefinition.get("0230").put(12, 'M');
        msgElementDefinition.get("0230").put(13, 'M');
        msgElementDefinition.get("0230").put(14, 'C');
        msgElementDefinition.get("0230").put(15, 'C');
        msgElementDefinition.get("0230").put(16, 'C');
        msgElementDefinition.get("0230").put(18, 'M');
        msgElementDefinition.get("0230").put(22, 'C');
        msgElementDefinition.get("0230").put(24, 'M');
        msgElementDefinition.get("0230").put(32, 'M');
        msgElementDefinition.get("0230").put(33, 'M');
        msgElementDefinition.get("0230").put(37, 'M');
        msgElementDefinition.get("0230").put(38, 'C');
        msgElementDefinition.get("0230").put(39, 'M');
        msgElementDefinition.get("0230").put(41, 'M');
        msgElementDefinition.get("0230").put(43, 'C');
        msgElementDefinition.get("0230").put(49, 'M');
        msgElementDefinition.get("0230").put(50, 'C');
        msgElementDefinition.get("0230").put(54, 'C');
        msgElementDefinition.get("0230").put(102, 'C');
        msgElementDefinition.get("0230").put(103, 'C');
        msgElementDefinition.get("0230").put(120, 'C');

        //Message Element Definition for 0420
        msgElementDefinition.put("0420", new HashMap<Integer, Character>());
        msgElementDefinition.get("0420").put(2, 'M');
        msgElementDefinition.get("0420").put(3, 'M');
        msgElementDefinition.get("0420").put(4, 'M');
        msgElementDefinition.get("0420").put(5, 'C');
        msgElementDefinition.get("0420").put(7, 'M');
        msgElementDefinition.get("0420").put(9, 'C');
        msgElementDefinition.get("0420").put(11, 'M');
        msgElementDefinition.get("0420").put(12, 'M');
        msgElementDefinition.get("0420").put(13, 'M');
        msgElementDefinition.get("0420").put(15, 'M');
        msgElementDefinition.get("0420").put(16, 'C');
        msgElementDefinition.get("0420").put(23, 'C');
        msgElementDefinition.get("0420").put(24, 'M');
        msgElementDefinition.get("0420").put(28, 'C');
        msgElementDefinition.get("0420").put(32, 'M');
        msgElementDefinition.get("0420").put(33, 'C');
        msgElementDefinition.get("0420").put(37, 'M');
        msgElementDefinition.get("0420").put(41, 'M');
        msgElementDefinition.get("0420").put(42, 'C');
        msgElementDefinition.get("0420").put(49, 'M');
        msgElementDefinition.get("0420").put(50, 'C');
        msgElementDefinition.get("0420").put(54, 'C');
        msgElementDefinition.get("0420").put(90, 'M');
        msgElementDefinition.get("0420").put(95, 'C');

        //Message Element Definition for 0421
        msgElementDefinition.put("0421", new HashMap<Integer, Character>());
        msgElementDefinition.get("0421").put(2, 'M');
        msgElementDefinition.get("0421").put(3, 'M');
        msgElementDefinition.get("0421").put(4, 'M');
        msgElementDefinition.get("0421").put(5, 'C');
        msgElementDefinition.get("0421").put(7, 'M');
        msgElementDefinition.get("0421").put(9, 'C');
        msgElementDefinition.get("0421").put(11, 'M');
        msgElementDefinition.get("0421").put(12, 'M');
        msgElementDefinition.get("0421").put(13, 'M');
        msgElementDefinition.get("0421").put(15, 'M');
        msgElementDefinition.get("0421").put(16, 'C');
        msgElementDefinition.get("0421").put(23, 'C');
        msgElementDefinition.get("0421").put(24, 'M');
        msgElementDefinition.get("0421").put(28, 'C');
        msgElementDefinition.get("0421").put(32, 'M');
        msgElementDefinition.get("0421").put(33, 'C');
        msgElementDefinition.get("0421").put(37, 'M');
        msgElementDefinition.get("0421").put(41, 'M');
        msgElementDefinition.get("0421").put(42, 'C');
        msgElementDefinition.get("0421").put(49, 'M');
        msgElementDefinition.get("0421").put(50, 'C');
        msgElementDefinition.get("0421").put(54, 'C');
        msgElementDefinition.get("0421").put(90, 'M');
        msgElementDefinition.get("0421").put(95, 'C');

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
        msgElementDefinition.get("0430").put(24, 'M');
        msgElementDefinition.get("0430").put(28, 'C');
        msgElementDefinition.get("0430").put(32, 'M');
        msgElementDefinition.get("0430").put(33, 'M');
        msgElementDefinition.get("0430").put(37, 'M');
        msgElementDefinition.get("0430").put(39, 'M');
        msgElementDefinition.get("0430").put(41, 'M');
        msgElementDefinition.get("0430").put(42, 'C');
        msgElementDefinition.get("0430").put(49, 'M');
        msgElementDefinition.get("0430").put(50, 'C');
        msgElementDefinition.get("0430").put(95, 'C');
    }

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
        logger.info("Translating incoming message from MobileBanking...");
        return protocolMessage;
    }

    @Override
    public ProtocolMessage TranslateFromFanap(ProtocolMessage protocolMessage) throws Exception
    {
        logger.info("Translating outgoing message for MobileBanking...");
        return protocolMessage;
    }
    ////Raza Adding for Field traslation end
}
