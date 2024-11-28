package vaulsys.protocols.pos87;

import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Pos87ProtocolDialog implements ProtocolDialog {

    transient Logger logger = Logger.getLogger(Pos87ProtocolDialog.class);

    @Override
    public Ifx refine(Ifx ifx) {
        return ifx;
    }

    @Override
    public ProtocolMessage refine(ProtocolMessage protocolMessage) {

        ISOMsg isoMsg = (ISOMsg) protocolMessage;
        Pos87RequiredFieldsRepository requiredFields = new Pos87RequiredFieldsRepository();

        try {
            String mtiStr = isoMsg.getMTI();
            Integer mti = Integer.parseInt(mtiStr);
            int[] msg = requiredFields.getRequiredFields(mti);
            ArrayList<Integer> neededFileds = new ArrayList<Integer>();
            
            ArrayList<Integer> removedFields = new ArrayList<Integer>();

            int k = 0; //
        	Integer firstField = 2;

        	Set<Integer> msgFields = isoMsg.getFieldNumbers();
        	List<Integer> tmpMsgFields = new ArrayList<Integer>(); 
        	tmpMsgFields.addAll(msgFields);

        	for(int i=0; i<msg.length; i++){
        		Integer integer = new Integer(msg[i]);
        		if (tmpMsgFields.contains(integer)) {
					tmpMsgFields.remove(integer);
				}else
					neededFileds.add(new Integer(msg[i]));
        	}
        	
			for(Integer i:tmpMsgFields){
				if( i < firstField)
					continue;
				
				isoMsg.unset(i);
				removedFields.add(i);
			}
        	
			for (Integer integer : neededFileds) {
				if (integer.equals(54))
					isoMsg.set(54, "0000000C0000000000000000000C000000000000");
			}
			
            if (removedFields.size() != 0)
                logger.warn("Message has fields " + removedFields.toString() + " but it should not. Switch removed them...");

            return protocolMessage;
        } catch (Exception ex) {
                return null;
        }
    }

    ////Raza Adding for Field traslation start
    @Override
    public ProtocolMessage TranslateToFanap(ProtocolMessage protocolMessage) throws Exception
    {
        //logger.info("Translating incoming message from Pos87...");
        return protocolMessage;
    }

    @Override
    public ProtocolMessage TranslateFromFanap(ProtocolMessage protocolMessage) throws Exception
    {
        //logger.info("Translating outgoing message for Pos87...");
        return protocolMessage;
    }
    ////Raza Adding for Field traslation end
}
