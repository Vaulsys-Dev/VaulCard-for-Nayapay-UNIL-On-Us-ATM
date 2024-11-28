package vaulsys.protocols.iso8583v93;

import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import org.apache.log4j.Logger;

public class ISO8583v93ProtocolDialog implements ProtocolDialog {

    transient Logger logger = Logger.getLogger(ISO8583v93ProtocolDialog.class);

    @Override
    public Ifx refine(Ifx ifx) {
        return ifx;
    }

    @Override
    public ProtocolMessage refine(ProtocolMessage protocolMessage) throws Exception {
        ISOMsg isoMsg = (ISOMsg) protocolMessage;

        int[] msg200 = new int[]{2, 3, 4, 6, 7, 10, 11, 12, 13, 14, 17, 25, 32, 33, 35, 37, 41, 42, 43, 48, 49, 51, 52, 100/*,64*/};
        int[] msg210 = new int[]{3, 4, 6, 7, 11, 12, 13, 15, 32, 33, 35, 37, 38, 39, 41, 43, 44, 49, 51, 54/*,64*/};
        int[] msg400 = new int[]{2, 3, 4, 6, 7, 10, 11, 12, 13, 17, 32, 33, 35, 37, 38, 39, 41, 42, 43, 48, 49, 51, 90, 95/*,128*/};
        int[] msg410 = new int[]{2, 3, 4, 6, 7, 11, 12, 13, 15, 32, 33, 37, 39, 42, 48, 51, 54/*,64*/};
        int[] msg = null;
        try {
            String mtiStr = isoMsg.getMTI();
            Integer mti = Integer.parseInt(mtiStr);
            switch (mti) {
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
                default:
                    break;
            }


            int k = 0; //
            for (int i = 2; i < 64; i++) { //field counter
                if (isoMsg.hasField(i) && msg[k] != i) {//msg has fld i but msg says no
                    logger.warn("Message has field " + i + " but msg says no.");
                    isoMsg.unset(i);//unset fld i
                }
                if (!isoMsg.hasField(i) && msg[k] == i) {
                    //msg does not have fld i but msg says yes
                    boolean result = setField(isoMsg, i);//set fld i
                    logger.warn("Message doesn't have field " + i + " but msg says yes, result:" + result);

                }
                if (!isoMsg.hasField(i) && msg[k] != i) {//msg does not have fld i and msg says no
                } else {////msg has fld i and msg says yes
                    k++;
                }
            }

            return protocolMessage;
        } catch (Exception ex) {
        }

        return null;
    }

    private boolean setField(ISOMsg msg, int fldno) throws Exception {

        return false;
        /*
          switch (fldno) {
          case 10:
              msg.set(fldno, "1");
              return true;
          case 49:
          case 51:
              msg.set(fldno, "364");
              return true;
          case 2:
          case 3:
          case 11:
          case 41:
              return false;
              //throw new Exception("Fatal Error in refining ISOMsg.");
          case 96:
              msg.set(fldno, "00000000000000000000000000000000");
              return true;
          case 4:
              msg.set(fldno, "2");
              return true;
          default:
              msg.set(fldno, "0");
              return false;
          }
          */
    }

    ////Raza Adding for Field traslation start
    @Override
    public ProtocolMessage TranslateToFanap(ProtocolMessage protocolMessage) throws Exception
    {
        //logger.info("Translating incoming message from ISO8583v93...");
        return protocolMessage;
    }

    @Override
    public ProtocolMessage TranslateFromFanap(ProtocolMessage protocolMessage) throws Exception
    {
        //logger.info("Translating outgoing message for ISO8583v93...");
        return protocolMessage;
    }
    ////Raza Adding for Field traslation end
}
