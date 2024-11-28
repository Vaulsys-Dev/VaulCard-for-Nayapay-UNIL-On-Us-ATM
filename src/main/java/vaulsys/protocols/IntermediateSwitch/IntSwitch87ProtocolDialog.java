package vaulsys.protocols.IntermediateSwitch;

import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;

import org.apache.log4j.Logger;

public class IntSwitch87ProtocolDialog implements ProtocolDialog {

    transient Logger logger = Logger.getLogger(IntSwitch87ProtocolDialog.class);

    private static final int[] msg200 = new int[]{2, 3, 4, 6, 7,/*10,*/11, 12, 13, 14, /*15,*/ 17,/*18,*/25, 32, 33, 35, 37, 41, 42, 43, 48, 49, 51, 52, 100/*,64*/};	//Mirkamali(Task154)
    private static final int[] msg100 = new int[]{2, 3, 7,/*10,*/11, 12, 13, 15, 17,/*18,*/25, 32, 33, 37, 41, 42, 48, /*51,*/100/*,64*/};
    private static final int[] msg210 = new int[]{2, 3, 4, 6, 7, 11, 12, 13, 15, 32, 33, 35, 37, 38, 39, 41, 43, 44, 49, 51, 54, 100/*,64*/};
    private static final int[] msg400 = new int[]{2, 3, 4, 6, 7, 10, 11, 12, 13, /*15,*/ 17, 32, 33, 35, 37, 38, 39, 41, 42, 43, 48, 49, 51, 90, 95, 100/*,128*/};
    private static final int[] msg410 = new int[]{2, 3, 4, 6, 7, 11, 12, 13, 15, 32, 33, 37, 39, 42, 48, 51, 54/*,64*/, 100};
    private static final int[] msg800 = new int[]{7, 11, 15, 32, 33, 48, 53, 70, 96, 128};
    private static final int[] msg810 = new int[]{7, 11, 15, 32, 33, 39, 48, 70, 96, 128};
    private static final int[] msg500 = new int[]{7, 11, 15, 17, 32, 33, 50, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 97, 99, 124};
    
    @Override
    public Ifx refine(Ifx ifx) {
        return ifx;
    }

    @Override
    public ProtocolMessage refine(ProtocolMessage protocolMessage) {

        ISOMsg isoMsg = (ISOMsg) protocolMessage;

        
        int[] msg = null;

        try {
            String mtiStr = isoMsg.getMTI();
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
            }

//            ArrayList<Integer> removedFields = new ArrayList<Integer>();
//            ArrayList<Integer> neededFields = new ArrayList<Integer>();

//            int k = 0; 
            
//            for (int i:notInMsg200) {
//            	if(isoMsg.hasField(i)){
//                  isoMsg.unset(i);
//                  removedFields.add(i);
//            	}
//            }
            int k=0;
            for (int i = 2; i < 128; i++) {
            	if(k < msg.length && i == msg[k]){
            		k++;
            	}else{
            		if(isoMsg.hasField(i)){
	            		isoMsg.unset(i);
//	            		removedFields.add(i);
            		}
            	}
            }
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
        //logger.info("Translating incoming message from IntSwitch...");
        return protocolMessage;
    }

    @Override
    public ProtocolMessage TranslateFromFanap(ProtocolMessage protocolMessage) throws Exception
    {
        //logger.info("Translating outgoing message for IntSwitch...");
        return protocolMessage;
    }
    ////Raza Adding for Field traslation end
}
