package vaulsys.protocols.ndc.base;

import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.constants.NDCMessageClass;

public abstract class NDCMsg implements Cloneable, ProtocolMessage {

    public NDCMessageClass messageType;
    public Long logicalUnitNumber;

    public Long getLogicalUnitNumber() {
//        return 111L;
        return logicalUnitNumber;
//        return Long.valueOf(logicalUnitNumber.toString().substring(3, 9));
//        return Long.valueOf(logicalUnitNumber.toString().substring(0, 3));
    }

    @Override
    public String toString() {
    	return "msgType:\t\t" + messageType + "\r\n" + "LUNO:\t\t" + logicalUnitNumber + "\r\n";
    }

    public abstract Ifx toIfx() throws Exception;
    
    public abstract byte[] toBinary() throws Exception;
}
