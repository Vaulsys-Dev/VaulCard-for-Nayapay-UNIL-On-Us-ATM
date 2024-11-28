package vaulsys.protocols.cmsnew;

import static vaulsys.base.components.MessageTypeFlowDirection.Clearing;
import static vaulsys.base.components.MessageTypeFlowDirection.Financial;
import static vaulsys.base.components.MessageTypeFlowDirection.Network;
import static vaulsys.base.components.MessageTypeFlowDirection.NotSupported;
import vaulsys.message.Message;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.cms.utils.CMSMapperUtil;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.wfe.base.DispatcherException;
import vaulsys.wfe.base.FlowDispatcher;

import org.apache.log4j.Logger;

public class CMSNewFlowDispatcher implements FlowDispatcher {
    transient static Logger logger = Logger.getLogger(CMSNewFlowDispatcher.class);

	@Override
	public String dispatch(Message message) throws DispatcherException {
		try {
			if (message.isIncomingMessage()) {

				ProtocolMessage protocolMessage = message.getProtocolMessage();

				if (protocolMessage instanceof CMSMessage) {
					CMSMessage cmsMsg = (CMSMessage) protocolMessage;
							
					IfxType ifxType = CMSMapperUtil.ToIfxType.get(cmsMsg.ifx);

					//m.rehman: separating BI from financial incase of limit
					//if (ISOFinalMessageType.isFinancialMessage(ifxType)
					if (ISOFinalMessageType.isFinancialMessage(ifxType, false)
						|| ISOFinalMessageType.isTransferCheckAccountMessage(ifxType)
						|| ISOFinalMessageType.isDepositChechAccountMessage(ifxType)
						|| ISOFinalMessageType.isGetAccountMessage(ifxType)
						|| ISOFinalMessageType.isChangePinBlockMessage(ifxType)
						|| ISOFinalMessageType.isBankStatementMessage(ifxType)
						|| ISOFinalMessageType.isCreditCardStatementMessage(ifxType)
						|| ISOFinalMessageType.isTransferToacChechAccountMessage(ifxType)
						|| ISOFinalMessageType.isShebaMessage(ifxType)
						)
						return Financial;
					else if (ISOFinalMessageType.isNetworkMessage(ifxType))
						return Network;
					else if (ISOFinalMessageType.isClearingMessage(ifxType))
						return Clearing;
				}
			}
			return NotSupported;

		} catch (Exception e) {
			logger.error(e);
			throw new DispatcherException(e);
		}
	}

}
