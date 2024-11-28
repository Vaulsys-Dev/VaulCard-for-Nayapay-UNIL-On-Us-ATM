package vaulsys.protocols.PaymentSchemes.ISOHost;

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

public class ISOHostProtocolDialog_new implements ProtocolDialog {

    private Map<String, Map<Integer, Character>> msgElementDefinition;

    transient Logger logger = Logger.getLogger(ISOHostProtocolDialog_new.class);

    ISOHostProtocolDialog_new() {
        InitializeMessageElementDefinition();
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
                    logger.error("Message does have fields " + removedFields.toString() + " but it should not. Removing fields.");
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

    private void InitializeMessageElementDefinition () {
        msgElementDefinition = new HashMap<String, Map<Integer, Character>>();

        //Message Element Definition for 0800
        msgElementDefinition.put("0800", new HashMap<Integer, Character>());
        msgElementDefinition.get("0800").put(7, 'M');
        msgElementDefinition.get("0800").put(11, 'M');
        msgElementDefinition.get("0800").put(12, 'M');
        msgElementDefinition.get("0800").put(13, 'M');
        msgElementDefinition.get("0800").put(24, 'C');
        msgElementDefinition.get("0800").put(33, 'C');
        msgElementDefinition.get("0800").put(70, 'C');
        msgElementDefinition.get("0800").put(100, 'C');

        //Message Element Definition for 0810
        msgElementDefinition.put("0810", new HashMap<Integer, Character>());
        msgElementDefinition.get("0810").put(7, 'M');
        msgElementDefinition.get("0810").put(11, 'M');
        msgElementDefinition.get("0810").put(12, 'M');
        msgElementDefinition.get("0810").put(13, 'M');
        msgElementDefinition.get("0800").put(24, 'C');
        msgElementDefinition.get("0810").put(33, 'C');
        msgElementDefinition.get("0810").put(39, 'M');
        msgElementDefinition.get("0810").put(70, 'C');
        msgElementDefinition.get("0810").put(100, 'C');

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
        msgElementDefinition.get("0200").put(13, 'C');
        msgElementDefinition.get("0200").put(14, 'C');
        msgElementDefinition.get("0200").put(15, 'C');
        msgElementDefinition.get("0200").put(16, 'C');
        msgElementDefinition.get("0200").put(18, 'M');
        msgElementDefinition.get("0200").put(22, 'M');
        msgElementDefinition.get("0200").put(24, 'M');
        msgElementDefinition.get("0200").put(26, 'C');
        msgElementDefinition.get("0200").put(28, 'C');
        msgElementDefinition.get("0200").put(32, 'M');
        msgElementDefinition.get("0200").put(33, 'M');
        msgElementDefinition.get("0200").put(35, 'C');
        msgElementDefinition.get("0200").put(36, 'C');
        msgElementDefinition.get("0200").put(37, 'M');
        msgElementDefinition.get("0200").put(41, 'M');
        msgElementDefinition.get("0200").put(42, 'M');
        msgElementDefinition.get("0200").put(43, 'C');
        msgElementDefinition.get("0200").put(45, 'C');
        msgElementDefinition.get("0200").put(49, 'C');
        msgElementDefinition.get("0200").put(50, 'C');
        msgElementDefinition.get("0200").put(51, 'C');
        msgElementDefinition.get("0200").put(100, 'C');
        msgElementDefinition.get("0200").put(102, 'C');
        msgElementDefinition.get("0200").put(103, 'C');

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
        msgElementDefinition.get("0210").put(15, 'C');
        msgElementDefinition.get("0210").put(16, 'C');
        msgElementDefinition.get("0210").put(28, 'C');
        msgElementDefinition.get("0210").put(32, 'M');
        msgElementDefinition.get("0210").put(33, 'M');
        msgElementDefinition.get("0210").put(37, 'M');
        msgElementDefinition.get("0210").put(39, 'M');
        msgElementDefinition.get("0210").put(41, 'M');
        msgElementDefinition.get("0210").put(42, 'M');
        msgElementDefinition.get("0210").put(49, 'C');
        msgElementDefinition.get("0210").put(50, 'C');
        msgElementDefinition.get("0200").put(51, 'C');
        msgElementDefinition.get("0210").put(54, 'C');
        msgElementDefinition.get("0210").put(100, 'C');
        msgElementDefinition.get("0210").put(102, 'C');
        msgElementDefinition.get("0210").put(103, 'C');

        //Message Element Definition for 0220
        msgElementDefinition.put("0220", new HashMap<Integer, Character>());
        msgElementDefinition.get("0220").put(2, 'M');
        msgElementDefinition.get("0220").put(3, 'M');
        msgElementDefinition.get("0220").put(4, 'C');
        msgElementDefinition.get("0220").put(5, 'C');
        msgElementDefinition.get("0220").put(6, 'C');
        msgElementDefinition.get("0220").put(7, 'M');
        msgElementDefinition.get("0220").put(9, 'C');
        msgElementDefinition.get("0220").put(10, 'C');
        msgElementDefinition.get("0220").put(11, 'M');
        msgElementDefinition.get("0220").put(12, 'M');
        msgElementDefinition.get("0220").put(13, 'C');
        msgElementDefinition.get("0220").put(14, 'C');
        msgElementDefinition.get("0220").put(15, 'C');
        msgElementDefinition.get("0220").put(16, 'C');
        msgElementDefinition.get("0220").put(18, 'M');
        msgElementDefinition.get("0220").put(22, 'M');
        msgElementDefinition.get("0220").put(24, 'M');
        msgElementDefinition.get("0220").put(26, 'C');
        msgElementDefinition.get("0220").put(28, 'C');
        msgElementDefinition.get("0220").put(32, 'M');
        msgElementDefinition.get("0220").put(33, 'M');
        msgElementDefinition.get("0220").put(35, 'C');
        msgElementDefinition.get("0220").put(36, 'C');
        msgElementDefinition.get("0220").put(37, 'M');
        msgElementDefinition.get("0220").put(41, 'M');
        msgElementDefinition.get("0220").put(42, 'M');
        msgElementDefinition.get("0220").put(43, 'C');
        msgElementDefinition.get("0220").put(45, 'C');
        msgElementDefinition.get("0220").put(49, 'C');
        msgElementDefinition.get("0220").put(50, 'C');
        msgElementDefinition.get("0200").put(51, 'C');
        msgElementDefinition.get("0220").put(100, 'C');
        msgElementDefinition.get("0220").put(102, 'C');
        msgElementDefinition.get("0220").put(103, 'C');

        //Message Element Definition for 0221
        msgElementDefinition.put("0221", new HashMap<Integer, Character>());
        msgElementDefinition.get("0221").put(2, 'M');
        msgElementDefinition.get("0221").put(3, 'M');
        msgElementDefinition.get("0221").put(4, 'C');
        msgElementDefinition.get("0221").put(5, 'C');
        msgElementDefinition.get("0221").put(6, 'C');
        msgElementDefinition.get("0221").put(7, 'M');
        msgElementDefinition.get("0221").put(9, 'C');
        msgElementDefinition.get("0221").put(10, 'C');
        msgElementDefinition.get("0221").put(11, 'M');
        msgElementDefinition.get("0221").put(12, 'M');
        msgElementDefinition.get("0221").put(13, 'C');
        msgElementDefinition.get("0221").put(14, 'C');
        msgElementDefinition.get("0221").put(15, 'C');
        msgElementDefinition.get("0221").put(16, 'C');
        msgElementDefinition.get("0221").put(18, 'M');
        msgElementDefinition.get("0221").put(22, 'M');
        msgElementDefinition.get("0221").put(24, 'M');
        msgElementDefinition.get("0221").put(26, 'C');
        msgElementDefinition.get("0221").put(28, 'C');
        msgElementDefinition.get("0221").put(32, 'M');
        msgElementDefinition.get("0221").put(33, 'M');
        msgElementDefinition.get("0221").put(35, 'C');
        msgElementDefinition.get("0221").put(36, 'C');
        msgElementDefinition.get("0221").put(37, 'M');
        msgElementDefinition.get("0221").put(41, 'M');
        msgElementDefinition.get("0221").put(42, 'M');
        msgElementDefinition.get("0221").put(43, 'C');
        msgElementDefinition.get("0221").put(45, 'C');
        msgElementDefinition.get("0221").put(49, 'C');
        msgElementDefinition.get("0221").put(50, 'C');
        msgElementDefinition.get("0200").put(51, 'C');
        msgElementDefinition.get("0221").put(100, 'C');
        msgElementDefinition.get("0221").put(102, 'C');
        msgElementDefinition.get("0221").put(103, 'C');

        //Message Element Definition for 0230
        msgElementDefinition.put("0230", new HashMap<Integer, Character>());
        msgElementDefinition.get("0230").put(2, 'M');
        msgElementDefinition.get("0230").put(3, 'M');
        msgElementDefinition.get("0230").put(4, 'C');
        msgElementDefinition.get("0230").put(5, 'C');
        msgElementDefinition.get("0230").put(6, 'C');
        msgElementDefinition.get("0230").put(7, 'M');
        msgElementDefinition.get("0230").put(9, 'C');
        msgElementDefinition.get("0230").put(10, 'C');
        msgElementDefinition.get("0230").put(11, 'M');
        msgElementDefinition.get("0230").put(12, 'M');
        msgElementDefinition.get("0230").put(15, 'C');
        msgElementDefinition.get("0230").put(16, 'C');
        msgElementDefinition.get("0230").put(28, 'C');
        msgElementDefinition.get("0230").put(32, 'M');
        msgElementDefinition.get("0230").put(33, 'M');
        msgElementDefinition.get("0230").put(37, 'M');
        msgElementDefinition.get("0230").put(39, 'M');
        msgElementDefinition.get("0230").put(41, 'M');
        msgElementDefinition.get("0230").put(42, 'M');
        msgElementDefinition.get("0230").put(49, 'C');
        msgElementDefinition.get("0230").put(50, 'C');
        msgElementDefinition.get("0200").put(51, 'C');
        msgElementDefinition.get("0230").put(54, 'C');
        msgElementDefinition.get("0230").put(100, 'C');
        msgElementDefinition.get("0230").put(102, 'C');
        msgElementDefinition.get("0230").put(103, 'C');

        //Message Element Definition for 0224 (LORO)
        msgElementDefinition.put("0224", new HashMap<Integer, Character>());
        msgElementDefinition.get("0224").put(2, 'M');
        msgElementDefinition.get("0224").put(3, 'M');
        msgElementDefinition.get("0224").put(4, 'C');
        msgElementDefinition.get("0224").put(5, 'C');
        msgElementDefinition.get("0224").put(6, 'C');
        msgElementDefinition.get("0224").put(7, 'M');
        msgElementDefinition.get("0224").put(9, 'C');
        msgElementDefinition.get("0224").put(10, 'C');
        msgElementDefinition.get("0224").put(11, 'M');
        msgElementDefinition.get("0224").put(12, 'M');
        msgElementDefinition.get("0224").put(13, 'C');
        msgElementDefinition.get("0224").put(14, 'C');
        msgElementDefinition.get("0224").put(15, 'C');
        msgElementDefinition.get("0224").put(16, 'C');
        msgElementDefinition.get("0224").put(18, 'M');
        msgElementDefinition.get("0224").put(22, 'M');
        msgElementDefinition.get("0224").put(24, 'M');
        msgElementDefinition.get("0224").put(26, 'C');
        msgElementDefinition.get("0224").put(28, 'C');
        msgElementDefinition.get("0224").put(32, 'M');
        msgElementDefinition.get("0224").put(33, 'M');
        msgElementDefinition.get("0224").put(35, 'C');
        msgElementDefinition.get("0224").put(36, 'C');
        msgElementDefinition.get("0224").put(37, 'M');
        msgElementDefinition.get("0224").put(41, 'M');
        msgElementDefinition.get("0224").put(42, 'M');
        msgElementDefinition.get("0224").put(43, 'C');
        msgElementDefinition.get("0224").put(45, 'C');
        msgElementDefinition.get("0224").put(49, 'C');
        msgElementDefinition.get("0224").put(50, 'C');
        msgElementDefinition.get("0200").put(51, 'C');
        msgElementDefinition.get("0224").put(100, 'C');
        msgElementDefinition.get("0224").put(102, 'C');
        msgElementDefinition.get("0224").put(103, 'C');

        //Message Element Definition for 0225 (LORO Repeat)
        msgElementDefinition.put("0225", new HashMap<Integer, Character>());
        msgElementDefinition.get("0225").put(2, 'M');
        msgElementDefinition.get("0225").put(3, 'M');
        msgElementDefinition.get("0225").put(4, 'C');
        msgElementDefinition.get("0225").put(5, 'C');
        msgElementDefinition.get("0225").put(6, 'C');
        msgElementDefinition.get("0225").put(7, 'M');
        msgElementDefinition.get("0225").put(9, 'C');
        msgElementDefinition.get("0225").put(10, 'C');
        msgElementDefinition.get("0225").put(11, 'M');
        msgElementDefinition.get("0225").put(12, 'M');
        msgElementDefinition.get("0225").put(13, 'C');
        msgElementDefinition.get("0225").put(14, 'C');
        msgElementDefinition.get("0225").put(15, 'C');
        msgElementDefinition.get("0225").put(16, 'C');
        msgElementDefinition.get("0225").put(18, 'M');
        msgElementDefinition.get("0225").put(22, 'M');
        msgElementDefinition.get("0225").put(24, 'M');
        msgElementDefinition.get("0225").put(26, 'C');
        msgElementDefinition.get("0225").put(28, 'C');
        msgElementDefinition.get("0225").put(32, 'M');
        msgElementDefinition.get("0225").put(33, 'M');
        msgElementDefinition.get("0225").put(35, 'C');
        msgElementDefinition.get("0225").put(36, 'C');
        msgElementDefinition.get("0225").put(37, 'M');
        msgElementDefinition.get("0225").put(41, 'M');
        msgElementDefinition.get("0225").put(42, 'M');
        msgElementDefinition.get("0225").put(43, 'C');
        msgElementDefinition.get("0225").put(45, 'C');
        msgElementDefinition.get("0225").put(49, 'C');
        msgElementDefinition.get("0225").put(50, 'C');
        msgElementDefinition.get("0200").put(51, 'C');
        msgElementDefinition.get("0225").put(100, 'C');
        msgElementDefinition.get("0225").put(102, 'C');
        msgElementDefinition.get("0225").put(103, 'C');

        //Message Element Definition for 0234 (LORO Response)
        msgElementDefinition.put("0234", new HashMap<Integer, Character>());
        msgElementDefinition.get("0234").put(2, 'M');
        msgElementDefinition.get("0234").put(3, 'M');
        msgElementDefinition.get("0234").put(4, 'C');
        msgElementDefinition.get("0234").put(5, 'C');
        msgElementDefinition.get("0234").put(6, 'C');
        msgElementDefinition.get("0234").put(7, 'M');
        msgElementDefinition.get("0234").put(9, 'C');
        msgElementDefinition.get("0234").put(10, 'C');
        msgElementDefinition.get("0234").put(11, 'M');
        msgElementDefinition.get("0234").put(12, 'M');
        msgElementDefinition.get("0234").put(15, 'C');
        msgElementDefinition.get("0234").put(16, 'C');
        msgElementDefinition.get("0234").put(28, 'C');
        msgElementDefinition.get("0234").put(32, 'M');
        msgElementDefinition.get("0234").put(33, 'M');
        msgElementDefinition.get("0234").put(37, 'M');
        msgElementDefinition.get("0234").put(39, 'M');
        msgElementDefinition.get("0234").put(41, 'M');
        msgElementDefinition.get("0234").put(42, 'M');
        msgElementDefinition.get("0234").put(49, 'C');
        msgElementDefinition.get("0234").put(50, 'C');
        msgElementDefinition.get("0200").put(51, 'C');
        msgElementDefinition.get("0234").put(54, 'C');
        msgElementDefinition.get("0234").put(100, 'C');
        msgElementDefinition.get("0234").put(102, 'C');
        msgElementDefinition.get("0234").put(103, 'C');

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
        msgElementDefinition.get("0420").put(24, 'M');
        msgElementDefinition.get("0420").put(28, 'C');
        msgElementDefinition.get("0420").put(32, 'M');
        msgElementDefinition.get("0420").put(33, 'M');
        msgElementDefinition.get("0420").put(37, 'M');
        msgElementDefinition.get("0420").put(41, 'M');
        msgElementDefinition.get("0420").put(42, 'M');
        msgElementDefinition.get("0420").put(49, 'C');
        msgElementDefinition.get("0420").put(50, 'C');
        msgElementDefinition.get("0420").put(51, 'C');
        msgElementDefinition.get("0420").put(90, 'C');
        msgElementDefinition.get("0420").put(100, 'C');

        //Message Element Definition for 0421
        msgElementDefinition.put("0421", new HashMap<Integer, Character>());
        msgElementDefinition.get("0421").put(2, 'M');
        msgElementDefinition.get("0421").put(3, 'M');
        msgElementDefinition.get("0421").put(4, 'M');
        msgElementDefinition.get("0421").put(5, 'C');
        msgElementDefinition.get("0421").put(6, 'C');
        msgElementDefinition.get("0421").put(7, 'M');
        msgElementDefinition.get("0421").put(9, 'C');
        msgElementDefinition.get("0421").put(10, 'C');
        msgElementDefinition.get("0421").put(11, 'M');
        msgElementDefinition.get("0421").put(12, 'M');
        msgElementDefinition.get("0421").put(13, 'M');
        msgElementDefinition.get("0421").put(15, 'C');
        msgElementDefinition.get("0421").put(16, 'C');
        msgElementDefinition.get("0421").put(24, 'M');
        msgElementDefinition.get("0421").put(28, 'C');
        msgElementDefinition.get("0421").put(32, 'M');
        msgElementDefinition.get("0421").put(33, 'C');
        msgElementDefinition.get("0421").put(37, 'M');
        msgElementDefinition.get("0421").put(41, 'M');
        msgElementDefinition.get("0421").put(42, 'M');
        msgElementDefinition.get("0421").put(49, 'C');
        msgElementDefinition.get("0421").put(50, 'C');
        msgElementDefinition.get("0421").put(51, 'C');
        msgElementDefinition.get("0421").put(90, 'C');
        msgElementDefinition.get("0421").put(100, 'C');

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
        msgElementDefinition.get("0430").put(32, 'M');
        msgElementDefinition.get("0430").put(33, 'M');
        msgElementDefinition.get("0430").put(37, 'M');
        msgElementDefinition.get("0430").put(39, 'M');
        msgElementDefinition.get("0430").put(41, 'M');
        msgElementDefinition.get("0430").put(42, 'M');
        msgElementDefinition.get("0430").put(49, 'M');
        msgElementDefinition.get("0430").put(50, 'C');
        msgElementDefinition.get("0430").put(51, 'C');
        msgElementDefinition.get("0430").put(100, 'M');

        //Message Element Definition for 0424 (LORO Reversal)
        msgElementDefinition.put("0424", new HashMap<Integer, Character>());
        msgElementDefinition.get("0424").put(2, 'M');
        msgElementDefinition.get("0424").put(3, 'M');
        msgElementDefinition.get("0424").put(4, 'M');
        msgElementDefinition.get("0424").put(5, 'C');
        msgElementDefinition.get("0424").put(6, 'C');
        msgElementDefinition.get("0424").put(7, 'M');
        msgElementDefinition.get("0424").put(9, 'C');
        msgElementDefinition.get("0424").put(10, 'C');
        msgElementDefinition.get("0424").put(11, 'M');
        msgElementDefinition.get("0424").put(12, 'M');
        msgElementDefinition.get("0424").put(13, 'M');
        msgElementDefinition.get("0424").put(15, 'C');
        msgElementDefinition.get("0424").put(16, 'C');
        msgElementDefinition.get("0424").put(24, 'M');
        msgElementDefinition.get("0424").put(28, 'C');
        msgElementDefinition.get("0424").put(32, 'M');
        msgElementDefinition.get("0424").put(33, 'C');
        msgElementDefinition.get("0424").put(37, 'M');
        msgElementDefinition.get("0424").put(41, 'M');
        msgElementDefinition.get("0424").put(42, 'M');
        msgElementDefinition.get("0424").put(49, 'C');
        msgElementDefinition.get("0424").put(50, 'C');
        msgElementDefinition.get("0424").put(51, 'C');
        msgElementDefinition.get("0424").put(90, 'C');
        msgElementDefinition.get("0424").put(100, 'C');

        //Message Element Definition for 0425 (LORO Reversal Repeat)
        msgElementDefinition.put("0425", new HashMap<Integer, Character>());
        msgElementDefinition.get("0425").put(2, 'M');
        msgElementDefinition.get("0425").put(3, 'M');
        msgElementDefinition.get("0425").put(4, 'M');
        msgElementDefinition.get("0425").put(5, 'C');
        msgElementDefinition.get("0425").put(6, 'C');
        msgElementDefinition.get("0425").put(7, 'M');
        msgElementDefinition.get("0425").put(9, 'C');
        msgElementDefinition.get("0425").put(10, 'C');
        msgElementDefinition.get("0425").put(11, 'M');
        msgElementDefinition.get("0425").put(12, 'M');
        msgElementDefinition.get("0425").put(13, 'M');
        msgElementDefinition.get("0425").put(15, 'C');
        msgElementDefinition.get("0425").put(16, 'C');
        msgElementDefinition.get("0425").put(24, 'M');
        msgElementDefinition.get("0425").put(28, 'C');
        msgElementDefinition.get("0425").put(32, 'M');
        msgElementDefinition.get("0425").put(33, 'C');
        msgElementDefinition.get("0425").put(37, 'M');
        msgElementDefinition.get("0425").put(41, 'M');
        msgElementDefinition.get("0425").put(42, 'M');
        msgElementDefinition.get("0425").put(49, 'C');
        msgElementDefinition.get("0425").put(50, 'C');
        msgElementDefinition.get("0425").put(51, 'C');
        msgElementDefinition.get("0425").put(90, 'C');
        msgElementDefinition.get("0425").put(100, 'C');

        //Message Element Definition for 0434
        msgElementDefinition.put("0434", new HashMap<Integer, Character>());
        msgElementDefinition.get("0434").put(2, 'M');
        msgElementDefinition.get("0434").put(3, 'M');
        msgElementDefinition.get("0434").put(4, 'M');
        msgElementDefinition.get("0434").put(5, 'C');
        msgElementDefinition.get("0434").put(7, 'M');
        msgElementDefinition.get("0434").put(9, 'C');
        msgElementDefinition.get("0434").put(11, 'M');
        msgElementDefinition.get("0434").put(12, 'M');
        msgElementDefinition.get("0434").put(13, 'M');
        msgElementDefinition.get("0434").put(15, 'M');
        msgElementDefinition.get("0434").put(16, 'C');
        msgElementDefinition.get("0434").put(32, 'M');
        msgElementDefinition.get("0434").put(33, 'M');
        msgElementDefinition.get("0434").put(37, 'M');
        msgElementDefinition.get("0434").put(39, 'M');
        msgElementDefinition.get("0434").put(41, 'M');
        msgElementDefinition.get("0434").put(42, 'M');
        msgElementDefinition.get("0434").put(49, 'M');
        msgElementDefinition.get("0434").put(50, 'C');
        msgElementDefinition.get("0434").put(51, 'C');
        msgElementDefinition.get("0434").put(100, 'M');
    }

    ////Raza Adding for Field traslation start
    @Override
    public ProtocolMessage TranslateToFanap(ProtocolMessage protocolMessage) throws Exception
    {
        //logger.info("Translating incoming message from ISOHost...");
        return protocolMessage;
    }

    @Override
    public ProtocolMessage TranslateFromFanap(ProtocolMessage protocolMessage) throws Exception
    {
        //logger.info("Translating outgoing message for ISOHost...");
        return protocolMessage;
    }
    ////Raza Adding for Field traslation end
}
