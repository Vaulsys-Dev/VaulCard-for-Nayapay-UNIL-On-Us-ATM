package vaulsys.protocols.mehremihan;

import vaulsys.message.Message;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.enums.NetworkManagementInfo;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.wfe.base.DispatcherException;
import vaulsys.wfe.base.FlowDispatcher;

import static vaulsys.base.components.MessageTypeFlowDirection.*;

public class MehreMihanFlowDispatcher implements FlowDispatcher {
	@Override
	public String dispatch(Message message) throws DispatcherException {
		try {
			if (message.isIncomingMessage()) {

				ProtocolMessage protocolMessage = message.getProtocolMessage();

				if (protocolMessage instanceof ISOMsg) {
					ISOMsg isoMsg = (ISOMsg) protocolMessage;
					String mtiStr = isoMsg.getMTI();
					Integer mtiType = Integer.parseInt(mtiStr.substring(1, 2));
					switch (mtiType) {
						case 1:
						case 2:
						case 4:
							return Financial;
						case 5:
							return Clearing;
						case 8:
							if (NetworkManagementInfo.CUTOVER.getType() == Integer.parseInt(isoMsg.getString(70).trim()))
								return Clearing;
							else
								return Network;
					}
				}
			}
			return NotSupported;

		} catch (ISOException e) {
			throw new DispatcherException(e);
		}
	}


}
