package vaulsys.protocols.PaymentSchemes.VisaSMS;

import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by HP on 11/23/2016.
 */
public class VisaSMSProtocolDialog implements ProtocolDialog {
    private Map<String, Map<Integer, Character>> msgElementDefinition;

    transient Logger logger = Logger.getLogger(VisaSMSProtocolDialog.class);

    VisaSMSProtocolDialog() {
        msgElementDefinition = new HashMap<String, Map<Integer, Character>>();

        //Message Element Definition for 0200 - Financial Transactions Request
        msgElementDefinition.put("0200", new HashMap<Integer, Character>());
        msgElementDefinition.get("0200").put(2, 'M');
        msgElementDefinition.get("0200").put(3, 'M');
        msgElementDefinition.get("0200").put(4, 'C');
        msgElementDefinition.get("0200").put(7, 'M');
        msgElementDefinition.get("0200").put(11, 'M');
        msgElementDefinition.get("0200").put(12, 'M');
        msgElementDefinition.get("0200").put(13, 'M');
        msgElementDefinition.get("0200").put(14, 'C');
        msgElementDefinition.get("0200").put(18, 'M');
        msgElementDefinition.get("0200").put(19, 'C');
        msgElementDefinition.get("0200").put(20, 'C');
        msgElementDefinition.get("0200").put(22, 'M');
        msgElementDefinition.get("0200").put(25, 'M');
        msgElementDefinition.get("0200").put(26, 'C');
        msgElementDefinition.get("0200").put(28, 'C');
        msgElementDefinition.get("0200").put(32, 'M');
        msgElementDefinition.get("0200").put(33, 'C');
        msgElementDefinition.get("0200").put(35, 'M');
        msgElementDefinition.get("0200").put(37, 'M');
        msgElementDefinition.get("0200").put(41, 'M');
        msgElementDefinition.get("0200").put(42, 'M');
        msgElementDefinition.get("0200").put(43, 'M');
        msgElementDefinition.get("0200").put(48, 'C');
        msgElementDefinition.get("0200").put(49, 'C');
        msgElementDefinition.get("0200").put(52, 'M');
        msgElementDefinition.get("0200").put(53, 'M');
        msgElementDefinition.get("0200").put(62, 'C');
        msgElementDefinition.get("0200").put(63, 'M');
        msgElementDefinition.get("0200").put(102, 'C');

        //Message Element Definition for 0210 - Financial Transactions Response
        msgElementDefinition.put("0210", new HashMap<Integer, Character>());
        msgElementDefinition.get("0210").put(2, 'M');
        msgElementDefinition.get("0210").put(3, 'M');
        msgElementDefinition.get("0210").put(4, 'C');
        msgElementDefinition.get("0210").put(7, 'M');
        msgElementDefinition.get("0210").put(11, 'M');
        msgElementDefinition.get("0210").put(15, 'M');
        msgElementDefinition.get("0210").put(19, 'C');
        msgElementDefinition.get("0210").put(25, 'M');
        msgElementDefinition.get("0210").put(32, 'M');
        msgElementDefinition.get("0210").put(37, 'M');
        msgElementDefinition.get("0210").put(38, 'C');
        msgElementDefinition.get("0210").put(39, 'M');
        msgElementDefinition.get("0210").put(41, 'M');
        msgElementDefinition.get("0210").put(42, 'M');
        msgElementDefinition.get("0210").put(49, 'C');
        msgElementDefinition.get("0210").put(62, 'C');
        msgElementDefinition.get("0210").put(63, 'M');
        msgElementDefinition.get("0210").put(102, 'C');

        //Message Element Definition for 0220
        msgElementDefinition.put("0220", new HashMap<Integer, Character>());
        msgElementDefinition.get("0220").put(2, 'C');
        msgElementDefinition.get("0220").put(3, 'M');
        msgElementDefinition.get("0220").put(4, 'M');
        msgElementDefinition.get("0220").put(5, 'C');
        msgElementDefinition.get("0220").put(6, 'C');
        msgElementDefinition.get("0220").put(7, 'M');
        msgElementDefinition.get("0220").put(9, 'C');
        msgElementDefinition.get("0220").put(10, 'C');
        msgElementDefinition.get("0220").put(11, 'M');
        msgElementDefinition.get("0220").put(12, 'C');
        msgElementDefinition.get("0220").put(13, 'C');
        msgElementDefinition.get("0220").put(14, 'C');
        msgElementDefinition.get("0220").put(15, 'C');
        msgElementDefinition.get("0220").put(16, 'C');
        msgElementDefinition.get("0220").put(18, 'C');
        msgElementDefinition.get("0220").put(19, 'C');
        msgElementDefinition.get("0220").put(22, 'C');
        msgElementDefinition.get("0220").put(25, 'C');
        msgElementDefinition.get("0220").put(28, 'C');
        msgElementDefinition.get("0220").put(32, 'M');
        msgElementDefinition.get("0220").put(33, 'C');
        msgElementDefinition.get("0220").put(37, 'M');
        msgElementDefinition.get("0220").put(38, 'C');
        msgElementDefinition.get("0220").put(41, 'C');
        msgElementDefinition.get("0220").put(42, 'C');
        msgElementDefinition.get("0220").put(43, 'C');
        msgElementDefinition.get("0220").put(48, 'C');
        msgElementDefinition.get("0220").put(49, 'C');
        msgElementDefinition.get("0220").put(50, 'C');
        msgElementDefinition.get("0220").put(51, 'C');
        msgElementDefinition.get("0220").put(62, 'C');
        msgElementDefinition.get("0220").put(63, 'M');
        msgElementDefinition.get("0220").put(90, 'C');
        msgElementDefinition.get("0220").put(100, 'C');
        msgElementDefinition.get("0220").put(102, 'C');

        //Message Element Definition for 0230
        msgElementDefinition.put("0230", new HashMap<Integer, Character>());
        msgElementDefinition.get("0230").put(2, 'C');
        msgElementDefinition.get("0230").put(3, 'M');
        msgElementDefinition.get("0230").put(7, 'M');
        msgElementDefinition.get("0230").put(11, 'M');
        msgElementDefinition.get("0230").put(15, 'M');
        msgElementDefinition.get("0230").put(19, 'C');
        msgElementDefinition.get("0230").put(25, 'C');
        msgElementDefinition.get("0230").put(32, 'M');
        msgElementDefinition.get("0230").put(37, 'M');
        msgElementDefinition.get("0230").put(39, 'C');
        msgElementDefinition.get("0230").put(41, 'C');
        msgElementDefinition.get("0230").put(42, 'C');
        msgElementDefinition.get("0230").put(48, 'C');
        msgElementDefinition.get("0230").put(62, 'C');
        msgElementDefinition.get("0230").put(63, 'M');
        msgElementDefinition.get("0230").put(90, 'C');
        msgElementDefinition.get("0230").put(100, 'C');
        msgElementDefinition.get("0230").put(102, 'C');

        //Message Element Definition for 0302 - Two Mandatory fields 91 and 101 are missing.
        msgElementDefinition.put("0302", new HashMap<Integer, Character>());
        msgElementDefinition.get("0302").put(2, 'M');
        msgElementDefinition.get("0302").put(7, 'M');
        msgElementDefinition.get("0302").put(11, 'M');
        msgElementDefinition.get("0302").put(19, 'C');
        msgElementDefinition.get("0302").put(37, 'M');
        msgElementDefinition.get("0302").put(63, 'M');

        //Message Element Definition for 0312 - Two Mandatory fields 91 and 101 are missing.
        msgElementDefinition.put("0312", new HashMap<Integer, Character>());
        msgElementDefinition.get("0312").put(2, 'M');
        msgElementDefinition.get("0312").put(7, 'M');
        msgElementDefinition.get("0312").put(11, 'M');
        msgElementDefinition.get("0312").put(15, 'M');
        msgElementDefinition.get("0312").put(19, 'C');
        msgElementDefinition.get("0312").put(37, 'M');
        msgElementDefinition.get("0312").put(39, 'M');
        msgElementDefinition.get("0312").put(48, 'C');
        msgElementDefinition.get("0312").put(63, 'C');

        //Message Element Definition for 0420 - Financial Reversals Request
        msgElementDefinition.put("0420", new HashMap<Integer, Character>());
        msgElementDefinition.get("0420").put(2, 'M');
        msgElementDefinition.get("0420").put(3, 'M');
        msgElementDefinition.get("0420").put(4, 'M');
        msgElementDefinition.get("0420").put(7, 'M');
        msgElementDefinition.get("0420").put(11, 'M');
        msgElementDefinition.get("0420").put(12, 'M');
        msgElementDefinition.get("0420").put(13, 'M');
        msgElementDefinition.get("0420").put(14, 'C');
        msgElementDefinition.get("0420").put(18, 'M');
        msgElementDefinition.get("0420").put(19, 'C');
        msgElementDefinition.get("0420").put(22, 'M');
        msgElementDefinition.get("0420").put(25, 'M');
        msgElementDefinition.get("0420").put(28, 'M');
        msgElementDefinition.get("0420").put(32, 'M');
        msgElementDefinition.get("0420").put(33, 'C');
        msgElementDefinition.get("0420").put(37, 'M');
        msgElementDefinition.get("0420").put(38, 'C');
        msgElementDefinition.get("0420").put(41, 'M');
        msgElementDefinition.get("0420").put(42, 'M');
        msgElementDefinition.get("0420").put(43, 'M');
        msgElementDefinition.get("0420").put(48, 'C');
        msgElementDefinition.get("0420").put(49, 'M');
        msgElementDefinition.get("0420").put(62, 'C');
        msgElementDefinition.get("0420").put(63, 'M');
        msgElementDefinition.get("0420").put(90, 'M');
        msgElementDefinition.get("0420").put(102, 'C');

        //Message Element Definition for 0430 - Financial Reversals Request Response
        msgElementDefinition.put("0430", new HashMap<Integer, Character>());
        msgElementDefinition.get("0430").put(2, 'M');
        msgElementDefinition.get("0430").put(3, 'M');
        msgElementDefinition.get("0430").put(4, 'C');
        msgElementDefinition.get("0430").put(5, 'C');
        msgElementDefinition.get("0430").put(7, 'M');
        msgElementDefinition.get("0430").put(9, 'C');
        msgElementDefinition.get("0430").put(11, 'M');
        msgElementDefinition.get("0430").put(12, 'C');
        msgElementDefinition.get("0430").put(13, 'C');
        msgElementDefinition.get("0430").put(15, 'M');
        msgElementDefinition.get("0430").put(16, 'C');
        msgElementDefinition.get("0430").put(18, 'C');
        msgElementDefinition.get("0430").put(19, 'C');
        msgElementDefinition.get("0430").put(23, 'C');
        msgElementDefinition.get("0430").put(25, 'M');
        msgElementDefinition.get("0430").put(32, 'M');
        msgElementDefinition.get("0430").put(33, 'C');
        msgElementDefinition.get("0430").put(37, 'M');
        msgElementDefinition.get("0430").put(39, 'M');
        msgElementDefinition.get("0430").put(41, 'M');
        msgElementDefinition.get("0430").put(42, 'M');
        msgElementDefinition.get("0430").put(48, 'C');
        msgElementDefinition.get("0430").put(62, 'C');
        msgElementDefinition.get("0430").put(63, 'M');
        msgElementDefinition.get("0430").put(90, 'M');
        msgElementDefinition.get("0430").put(102, 'C');

        //Message Element Definition for 0422
        msgElementDefinition.put("0422", new HashMap<Integer, Character>());
        msgElementDefinition.get("0422").put(2, 'C');
        msgElementDefinition.get("0422").put(3, 'M');
        msgElementDefinition.get("0422").put(4, 'M');
        msgElementDefinition.get("0422").put(7, 'M');
        msgElementDefinition.get("0422").put(11, 'M');
        msgElementDefinition.get("0422").put(13, 'C');
        msgElementDefinition.get("0422").put(18, 'C');
        msgElementDefinition.get("0422").put(19, 'C');
        msgElementDefinition.get("0422").put(25, 'C');
        msgElementDefinition.get("0422").put(28, 'C');
        msgElementDefinition.get("0422").put(32, 'M');
        msgElementDefinition.get("0422").put(33, 'C');
        msgElementDefinition.get("0422").put(37, 'M');
        msgElementDefinition.get("0422").put(38, 'C');
        msgElementDefinition.get("0422").put(41, 'C');
        msgElementDefinition.get("0422").put(42, 'C');
        msgElementDefinition.get("0422").put(43, 'C');
        msgElementDefinition.get("0422").put(48, 'M');
        msgElementDefinition.get("0422").put(49, 'M');
        msgElementDefinition.get("0422").put(62, 'C');
        msgElementDefinition.get("0422").put(63, 'M');
        msgElementDefinition.get("0422").put(90, 'C');

        //Message Element Definition for 0432
        msgElementDefinition.put("0432", new HashMap<Integer, Character>());
        msgElementDefinition.get("0432").put(2, 'C');
        msgElementDefinition.get("0432").put(3, 'M');
        msgElementDefinition.get("0432").put(7, 'M');
        msgElementDefinition.get("0432").put(11, 'M');
        msgElementDefinition.get("0432").put(15, 'M');
        msgElementDefinition.get("0432").put(19, 'C');
        msgElementDefinition.get("0432").put(25, 'C');
        msgElementDefinition.get("0432").put(32, 'M');
        msgElementDefinition.get("0432").put(33, 'C');
        msgElementDefinition.get("0432").put(37, 'M');
        msgElementDefinition.get("0432").put(39, 'M');
        msgElementDefinition.get("0432").put(41, 'C');
        msgElementDefinition.get("0432").put(42, 'C');
        msgElementDefinition.get("0432").put(48, 'C');
        msgElementDefinition.get("0432").put(62, 'C');
        msgElementDefinition.get("0432").put(63, 'M');
        msgElementDefinition.get("0432").put(90, 'C');

        //Message Element Definition for 0600
        msgElementDefinition.put("0600", new HashMap<Integer, Character>());
        msgElementDefinition.get("0600").put(7, 'M');
        msgElementDefinition.get("0600").put(11, 'M');
        msgElementDefinition.get("0600").put(33, 'M');
        msgElementDefinition.get("0600").put(37, 'M');
        msgElementDefinition.get("0600").put(43, 'C');
        msgElementDefinition.get("0600").put(48, 'M');
        msgElementDefinition.get("0600").put(63, 'M');
        msgElementDefinition.get("0600").put(70, 'M');
        msgElementDefinition.get("0600").put(100, 'M');

        //Message Element Definition for 0610
        msgElementDefinition.put("0610", new HashMap<Integer, Character>());
        msgElementDefinition.get("0610").put(7, 'M');
        msgElementDefinition.get("0610").put(11, 'M');
        msgElementDefinition.get("0610").put(15, 'M');
        msgElementDefinition.get("0610").put(37, 'M');
        msgElementDefinition.get("0610").put(39, 'M');
        msgElementDefinition.get("0610").put(48, 'C');
        msgElementDefinition.get("0610").put(63, 'M');
        msgElementDefinition.get("0610").put(70, 'M');
        msgElementDefinition.get("0610").put(100, 'M');

        //Message Element Definition for 0620
        msgElementDefinition.put("0620", new HashMap<Integer, Character>());
        msgElementDefinition.get("0620").put(7, 'M');
        msgElementDefinition.get("0620").put(11, 'M');
        msgElementDefinition.get("0620").put(15, 'M');
        msgElementDefinition.get("0620").put(33, 'M');
        msgElementDefinition.get("0620").put(37, 'M');
        msgElementDefinition.get("0620").put(43, 'C');
        msgElementDefinition.get("0620").put(48, 'M');
        msgElementDefinition.get("0620").put(63, 'M');
        msgElementDefinition.get("0620").put(70, 'M');
        msgElementDefinition.get("0620").put(100, 'M');

        //Message Element Definition for 0630
        msgElementDefinition.put("0630", new HashMap<Integer, Character>());
        msgElementDefinition.get("0630").put(7, 'M');
        msgElementDefinition.get("0630").put(11, 'M');
        msgElementDefinition.get("0630").put(15, 'M');
        msgElementDefinition.get("0630").put(37, 'M');
        msgElementDefinition.get("0630").put(39, 'M');
        msgElementDefinition.get("0630").put(63, 'M');
        msgElementDefinition.get("0630").put(70, 'M');
        msgElementDefinition.get("0630").put(100, 'M');

        //Message Element Definition for 0800 - Network Management Request
        msgElementDefinition.put("0800", new HashMap<Integer, Character>());
        msgElementDefinition.get("0800").put(7, 'M');
        msgElementDefinition.get("0800").put(11, 'M');
        msgElementDefinition.get("0800").put(33, 'C');
        msgElementDefinition.get("0800").put(37, 'C');
        msgElementDefinition.get("0800").put(39, 'C');
        msgElementDefinition.get("0800").put(48, 'C');
        msgElementDefinition.get("0800").put(53, 'C');
        msgElementDefinition.get("0800").put(63, 'C');
        msgElementDefinition.get("0800").put(70, 'M');
        msgElementDefinition.get("0800").put(96, 'C');

        //Message Element Definition for 0810 - Network Management Response
        msgElementDefinition.put("0810", new HashMap<Integer, Character>());
        msgElementDefinition.get("0810").put(7, 'M');
        msgElementDefinition.get("0810").put(11, 'M');
        msgElementDefinition.get("0810").put(33, 'C');
        msgElementDefinition.get("0810").put(37, 'C');
        msgElementDefinition.get("0810").put(39, 'C');
        msgElementDefinition.get("0810").put(63, 'M');
        msgElementDefinition.get("0810").put(70, 'M');
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
        //logger.info("Translating incoming message from VisaSMS...");
        return protocolMessage;
    }

    @Override
    public ProtocolMessage TranslateFromFanap(ProtocolMessage protocolMessage) throws Exception
    {
        //logger.info("Translating outgoing message for VisaSMS...");
        return protocolMessage;
    }
    ////Raza Adding for Field traslation end
}
