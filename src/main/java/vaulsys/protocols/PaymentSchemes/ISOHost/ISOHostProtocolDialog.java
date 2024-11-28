package vaulsys.protocols.PaymentSchemes.ISOHost;

import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * Created by m.rehman on 4/9/2016.
 */

public class ISOHostProtocolDialog implements ProtocolDialog {

    transient Logger logger = Logger.getLogger(ISOHostProtocolDialog.class);

    @Override
    public Ifx refine(Ifx ifx) {
        return ifx;
    }

    @Override
    public ProtocolMessage refine(ProtocolMessage protocolMessage) {

        int[] msg = null;
        ISOMsg isoMsg = (ISOMsg) protocolMessage;

        int[] msg800 = new int[]{7, 11, 12, 13, 24, 33, 70, 100};
        int[] msg810 = new int[]{7, 11, 12, 13, 24, 33, 39, 70, 100};
        int[] msg200 = new int[]{2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 18, 22, 24, 26, 28, 32, 33, 35,
                36, 37, 41, 42, 43, 45, 49, 50, 51, 100, 102, 103};
        int[] msg210 = new int[]{2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 15, 16, 28, 32, 33, 37, 39, 41, 42, 49, 50, 51,
                54, 100, 102, 103};
        int[] msg420 = new int[]{2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 15, 16, 24, 28, 32, 33, 37, 41, 42, 49, 50,
                51, 90, 100};
        int[] msg430 = new int[]{2, 3, 4, 5, 7, 9, 11, 12, 13, 15, 16, 32, 33, 37, 39, 41, 42, 49, 50, 51, 100};

        try {
            String mtiStr = isoMsg.getMTI();
            Integer mti = Integer.parseInt(mtiStr);
            switch (mti) {
                case 200:
                case 220:
                case 221:
                case 224:
                case 225:
                    msg = msg200;
                    break;
                case 210:
                case 230:
                case 234:
                    msg = msg210;
                    break;
                case 420:
                case 421:
                case 424:
                case 425:
                    msg = msg420;
                    break;
                case 430:
                case 434:
                    msg = msg430;
                    break;
                case 800:
                    msg = msg800;
                    break;
                case 810:
                    msg = msg810;
                    break;
                default:
                    break;
            }

            ArrayList<Integer> removedFields = new ArrayList<Integer>();
            ArrayList<Integer> neededFields = new ArrayList<Integer>();

            int k = 0; //
            for (int i = 2; i < 64; i++) { //field counter
                if (isoMsg.hasField(i) && (k >= msg.length || msg[k] != i)) {//msg has fld i but msg says no
                    isoMsg.unset(i);//unset fld i
                    removedFields.add(i);
                }
                if (!isoMsg.hasField(i) && (k < msg.length && msg[k] == i)) {
                    //msg does not have fld i but msg says yes
                    neededFields.add(i);
                }

                if (!isoMsg.hasField(i) && (k >= msg.length || msg[k] != i)) {//msg does not have fld i and msg says no
                } else {////msg has fld i and msg says yes
                    k++;
                }
            }

            if (neededFields.size() != 0)
                logger.warn("Message doesn't have fields " + neededFields.toString() + " but it should have. Switch didn't add anything.");
            if (removedFields.size() != 0)
                logger.warn("Message does    have fields " + removedFields.toString() + " but it should not. Switch removed them.");

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
