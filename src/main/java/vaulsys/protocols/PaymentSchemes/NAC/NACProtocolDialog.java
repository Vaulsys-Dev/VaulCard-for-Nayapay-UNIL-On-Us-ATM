package vaulsys.protocols.PaymentSchemes.NAC;

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

public class NACProtocolDialog implements ProtocolDialog {

    private Map<String, Map<Integer, Character>> msgElementDefinition;

    transient Logger logger = Logger.getLogger(NACProtocolDialog.class);

    NACProtocolDialog() {
        msgElementDefinition = new HashMap<String, Map<Integer, Character>>();

        //Message Element Definition for 0100
        msgElementDefinition.put("0100", new HashMap<Integer, Character>());
        msgElementDefinition.get("0100").put(2, 'C');
        msgElementDefinition.get("0100").put(3, 'M');
        msgElementDefinition.get("0100").put(4, 'M');
        msgElementDefinition.get("0100").put(6, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0100").put(8, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0100").put(9, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0100").put(11, 'M');
        msgElementDefinition.get("0100").put(14, 'C');
        msgElementDefinition.get("0100").put(21, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0100").put(22, 'M');
        msgElementDefinition.get("0100").put(23, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0100").put(24, 'M');
        msgElementDefinition.get("0100").put(25, 'C');
        msgElementDefinition.get("0100").put(30, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0100").put(35, 'C');
        msgElementDefinition.get("0100").put(37, 'C');
        msgElementDefinition.get("0100").put(41, 'M');
        msgElementDefinition.get("0100").put(42, 'C');
        msgElementDefinition.get("0100").put(44, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0100").put(45, 'C');
        msgElementDefinition.get("0100").put(48, 'C');
        msgElementDefinition.get("0100").put(51, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0100").put(52, 'C');
        msgElementDefinition.get("0100").put(53, 'C');
        msgElementDefinition.get("0100").put(55, 'C');
        msgElementDefinition.get("0100").put(61, 'C');
        msgElementDefinition.get("0100").put(62, 'C');
        msgElementDefinition.get("0100").put(63, 'C');
        msgElementDefinition.get("0100").put(64, 'C');

        //Message Element Definition for 0110
        msgElementDefinition.put("0110", new HashMap<Integer, Character>());
        msgElementDefinition.get("0110").put(3, 'M');
        msgElementDefinition.get("0110").put(4, 'C');
        msgElementDefinition.get("0110").put(11, 'M');
        msgElementDefinition.get("0110").put(12, 'M');
        msgElementDefinition.get("0110").put(13, 'M');
        msgElementDefinition.get("0110").put(24, 'M');
        msgElementDefinition.get("0110").put(37, 'M');
        msgElementDefinition.get("0110").put(38, 'M');
        msgElementDefinition.get("0110").put(39, 'M');
        msgElementDefinition.get("0110").put(41, 'M');
        msgElementDefinition.get("0110").put(53, 'C');
        msgElementDefinition.get("0110").put(54, 'C');
        msgElementDefinition.get("0110").put(55, 'C');
        msgElementDefinition.get("0110").put(63, 'C');
        msgElementDefinition.get("0110").put(64, 'C');
        
        //Message Element Definition for 0200
        msgElementDefinition.put("0200", new HashMap<Integer, Character>());
        msgElementDefinition.get("0200").put(2, 'C'); //Raza changing for KEENU
        msgElementDefinition.get("0200").put(3, 'M');
        msgElementDefinition.get("0200").put(4, 'C');
        msgElementDefinition.get("0200").put(6, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0200").put(8, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0200").put(9, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0200").put(11, 'M');
        msgElementDefinition.get("0200").put(12, 'C');
        msgElementDefinition.get("0200").put(13, 'C');
        msgElementDefinition.get("0200").put(14, 'C');
        msgElementDefinition.get("0200").put(21, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0200").put(22, 'M');
        msgElementDefinition.get("0200").put(23, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0200").put(24, 'M');
        msgElementDefinition.get("0200").put(25, 'C');
        msgElementDefinition.get("0200").put(30, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0200").put(35, 'C');
        msgElementDefinition.get("0200").put(37, 'C');
        msgElementDefinition.get("0200").put(41, 'M');
        msgElementDefinition.get("0200").put(42, 'M');
        msgElementDefinition.get("0200").put(44, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0200").put(45, 'C');
        msgElementDefinition.get("0200").put(48, 'C');
        msgElementDefinition.get("0200").put(51, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0200").put(52, 'C');
        msgElementDefinition.get("0200").put(53, 'C');
        msgElementDefinition.get("0200").put(54, 'C');
        msgElementDefinition.get("0200").put(55, 'C');
        msgElementDefinition.get("0200").put(60, 'C');
        msgElementDefinition.get("0200").put(61, 'C');
        msgElementDefinition.get("0200").put(62, 'C');
        msgElementDefinition.get("0200").put(63, 'C');
        msgElementDefinition.get("0200").put(64, 'C');

        //Message Element Definition for 0210
        msgElementDefinition.put("0210", new HashMap<Integer, Character>());
        msgElementDefinition.get("0210").put(3, 'M');
        msgElementDefinition.get("0210").put(4, 'C');
        msgElementDefinition.get("0210").put(11, 'M');
        msgElementDefinition.get("0210").put(12, 'M');
        msgElementDefinition.get("0210").put(13, 'M');
        msgElementDefinition.get("0210").put(14, 'C');
        msgElementDefinition.get("0210").put(24, 'M');
        msgElementDefinition.get("0210").put(37, 'M');
        msgElementDefinition.get("0210").put(38, 'C');
        msgElementDefinition.get("0210").put(39, 'M');
        msgElementDefinition.get("0210").put(41, 'M');
        msgElementDefinition.get("0210").put(48, 'C');
        msgElementDefinition.get("0210").put(53, 'C');
        msgElementDefinition.get("0210").put(55, 'C');
        msgElementDefinition.get("0210").put(63, 'C');
        msgElementDefinition.get("0210").put(64, 'C');

        //Message Element Definition for 0220
        msgElementDefinition.put("0220", new HashMap<Integer, Character>());
        msgElementDefinition.get("0220").put(2, 'C');
        msgElementDefinition.get("0220").put(3, 'M');
        msgElementDefinition.get("0220").put(4, 'M');
        msgElementDefinition.get("0220").put(6, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0220").put(8, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0220").put(9, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0220").put(11, 'M');
        msgElementDefinition.get("0220").put(12, 'M');
        msgElementDefinition.get("0220").put(13, 'M');
        msgElementDefinition.get("0220").put(14, 'C');
        msgElementDefinition.get("0220").put(21, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0220").put(22, 'C');
        msgElementDefinition.get("0220").put(23, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0220").put(24, 'M');
        msgElementDefinition.get("0220").put(25, 'M');
        msgElementDefinition.get("0220").put(30, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0220").put(37, 'M');
        msgElementDefinition.get("0220").put(38, 'M');
        msgElementDefinition.get("0220").put(39, 'C');
        msgElementDefinition.get("0220").put(41, 'M');
        msgElementDefinition.get("0220").put(42, 'M');
        msgElementDefinition.get("0220").put(44, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0220").put(51, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0220").put(53, 'C');
        msgElementDefinition.get("0220").put(54, 'C');
        msgElementDefinition.get("0220").put(60, 'C');
        msgElementDefinition.get("0220").put(61, 'C');
        msgElementDefinition.get("0220").put(62, 'C');
        msgElementDefinition.get("0220").put(63, 'C');
        msgElementDefinition.get("0220").put(64, 'C');

        //Message Element Definition for 0230
        msgElementDefinition.put("0230", new HashMap<Integer, Character>());
        msgElementDefinition.get("0230").put(3, 'M');
        msgElementDefinition.get("0230").put(4, 'C');
        msgElementDefinition.get("0230").put(11, 'C');
        msgElementDefinition.get("0230").put(12, 'C');
        msgElementDefinition.get("0230").put(13, 'C');
        msgElementDefinition.get("0230").put(24, 'M');
        msgElementDefinition.get("0230").put(37, 'M');
        msgElementDefinition.get("0230").put(39, 'M');
        msgElementDefinition.get("0230").put(41, 'M');
        msgElementDefinition.get("0230").put(53, 'C');
        msgElementDefinition.get("0230").put(63, 'C');
        msgElementDefinition.get("0230").put(64, 'C');

        //Message Element Definition for 0320
        msgElementDefinition.put("0320", new HashMap<Integer, Character>());
        msgElementDefinition.get("0320").put(2, 'C');
        msgElementDefinition.get("0320").put(3, 'M');
        msgElementDefinition.get("0320").put(4, 'M');
        //msgElementDefinition.get("0320").put(6, 'C'); //Raza adding for KEENU
        //msgElementDefinition.get("0220").put(8, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0320").put(11, 'M');
        msgElementDefinition.get("0320").put(12, 'M');
        msgElementDefinition.get("0320").put(13, 'M');
        msgElementDefinition.get("0320").put(22, 'M');
        msgElementDefinition.get("0320").put(24, 'M');
        msgElementDefinition.get("0320").put(25, 'M');
        msgElementDefinition.get("0320").put(37, 'M');
        msgElementDefinition.get("0320").put(38, 'M');
        msgElementDefinition.get("0320").put(41, 'M');
        msgElementDefinition.get("0320").put(42, 'M');
        msgElementDefinition.get("0320").put(54, 'C');
        msgElementDefinition.get("0320").put(60, 'C');
        msgElementDefinition.get("0320").put(62, 'C');

        //Message Element Definition for 0330
        msgElementDefinition.put("0330", new HashMap<Integer, Character>());
        msgElementDefinition.get("0330").put(3, 'M');
        msgElementDefinition.get("0330").put(4, 'C');
        msgElementDefinition.get("0330").put(11, 'C');
        msgElementDefinition.get("0330").put(12, 'C');
        msgElementDefinition.get("0330").put(13, 'C');
        msgElementDefinition.get("0330").put(24, 'M');
        msgElementDefinition.get("0330").put(37, 'M');
        msgElementDefinition.get("0330").put(39, 'M');
        msgElementDefinition.get("0330").put(41, 'M');

        //Message Element Definition for 0400
        msgElementDefinition.put("0400", new HashMap<Integer, Character>());
        msgElementDefinition.get("0400").put(2, 'C');
        msgElementDefinition.get("0400").put(3, 'M');
        msgElementDefinition.get("0400").put(4, 'M');
        msgElementDefinition.get("0400").put(6, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0400").put(8, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0400").put(9, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0400").put(11, 'M');
        msgElementDefinition.get("0400").put(12, 'C');
        msgElementDefinition.get("0400").put(13, 'C');
        msgElementDefinition.get("0400").put(14, 'C');
        msgElementDefinition.get("0400").put(16, 'C');
        msgElementDefinition.get("0400").put(21, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0400").put(22, 'C');
        msgElementDefinition.get("0400").put(23, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0400").put(24, 'M');
        msgElementDefinition.get("0400").put(25, 'M');
        msgElementDefinition.get("0400").put(30, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0400").put(35, 'C');
        msgElementDefinition.get("0400").put(37, 'C');
        msgElementDefinition.get("0400").put(41, 'M');
        msgElementDefinition.get("0400").put(42, 'M');
        msgElementDefinition.get("0400").put(44, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0400").put(45, 'C');
        msgElementDefinition.get("0400").put(51, 'C'); //Raza adding for KEENU
        msgElementDefinition.get("0400").put(52, 'C');
        msgElementDefinition.get("0400").put(53, 'C');
        msgElementDefinition.get("0400").put(55, 'C');
        msgElementDefinition.get("0400").put(61, 'C');
        msgElementDefinition.get("0400").put(62, 'C');
        msgElementDefinition.get("0400").put(63, 'C');
        msgElementDefinition.get("0400").put(64, 'C');

        //Message Element Definition for 0410
        msgElementDefinition.put("0410", new HashMap<Integer, Character>());
        msgElementDefinition.get("0410").put(3, 'M');
        msgElementDefinition.get("0410").put(4, 'C');
        msgElementDefinition.get("0410").put(11, 'M');
        msgElementDefinition.get("0410").put(12, 'M');
        msgElementDefinition.get("0410").put(13, 'M');
        msgElementDefinition.get("0410").put(24, 'M');
        msgElementDefinition.get("0410").put(37, 'M');
        msgElementDefinition.get("0410").put(37, 'C');
        msgElementDefinition.get("0410").put(39, 'M');
        msgElementDefinition.get("0410").put(41, 'M');
        msgElementDefinition.get("0410").put(53, 'C');
        msgElementDefinition.get("0410").put(63, 'C');
        msgElementDefinition.get("0410").put(64, 'C');

        //Message Element Definition for 0500
        msgElementDefinition.put("0500", new HashMap<Integer, Character>());
        msgElementDefinition.get("0500").put(3, 'M');
        msgElementDefinition.get("0500").put(11, 'M');
        msgElementDefinition.get("0500").put(24, 'M');
        msgElementDefinition.get("0500").put(41, 'M');
        msgElementDefinition.get("0500").put(42, 'M');
        msgElementDefinition.get("0500").put(60, 'C');
        msgElementDefinition.get("0500").put(63, 'C');

        //Message Element Definition for 0510
        msgElementDefinition.put("0510", new HashMap<Integer, Character>());
        msgElementDefinition.get("0510").put(3, 'M');
        msgElementDefinition.get("0510").put(11, 'C');
        msgElementDefinition.get("0510").put(12, 'C');
        msgElementDefinition.get("0510").put(13, 'C');
        msgElementDefinition.get("0510").put(24, 'M');
        msgElementDefinition.get("0510").put(37, 'M');
        msgElementDefinition.get("0510").put(39, 'M');
        msgElementDefinition.get("0510").put(41, 'M');
        msgElementDefinition.get("0510").put(63, 'C');

        //Message Element Definition for 0800
        msgElementDefinition.put("0800", new HashMap<Integer, Character>());
        msgElementDefinition.get("0800").put(3, 'M');
        msgElementDefinition.get("0800").put(11, 'M');
        msgElementDefinition.get("0800").put(24, 'M');
        msgElementDefinition.get("0800").put(41, 'C');
        msgElementDefinition.get("0800").put(53, 'C');
        msgElementDefinition.get("0800").put(60, 'C');
        msgElementDefinition.get("0800").put(63, 'C');
        msgElementDefinition.get("0800").put(64, 'C');

        //Message Element Definition for 0810
        msgElementDefinition.put("0810", new HashMap<Integer, Character>());
        msgElementDefinition.get("0800").put(3, 'C');
        msgElementDefinition.get("0810").put(11, 'M');
        msgElementDefinition.get("0810").put(12, 'M');
        msgElementDefinition.get("0810").put(13, 'M');
        msgElementDefinition.get("0810").put(24, 'M');
        msgElementDefinition.get("0810").put(39, 'M');
        msgElementDefinition.get("0810").put(41, 'C');
        msgElementDefinition.get("0810").put(53, 'C');
        msgElementDefinition.get("0810").put(62, 'C');
        msgElementDefinition.get("0810").put(63, 'C');
        msgElementDefinition.get("0810").put(64, 'C');
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
        logger.info("Translating incoming message from NAC...");
        return protocolMessage;
    }

    @Override
    public ProtocolMessage TranslateFromFanap(ProtocolMessage protocolMessage) throws Exception
    {
        logger.info("Translating outgoing message for NAC...");
        return protocolMessage;
    }
    ////Raza Adding for Field traslation end
}
