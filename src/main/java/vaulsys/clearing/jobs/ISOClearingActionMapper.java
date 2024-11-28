package vaulsys.clearing.jobs;

import vaulsys.clearing.base.ClearingAction;
import vaulsys.clearing.base.ClearingActionMapper;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;

public class ISOClearingActionMapper implements ClearingActionMapper {

	public static final ISOClearingActionMapper Instance = new ISOClearingActionMapper();
	
	private ISOClearingActionMapper(){}
	
    public ClearingAction findAction(int messageType) {
        //switch (messageType) {
        String MessageType = messageType + "";

        if((MessageType.equals(ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_87)) ||
                (MessageType.equals(ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_93)) ||
                (MessageType.equals(ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_87)) ||
                (MessageType.equals(ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_93))) {
            return ClearingAction.COUTOVER_RESPONSE;
        }
        else if((MessageType.equals(ISOMessageTypes.NETWORK_MANAGEMENT_RESPONSE_87)) ||
                (MessageType.equals(ISOMessageTypes.NETWORK_MANAGEMENT_RESPONSE_93)) ||
                (MessageType.equals(ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_RESPONSE_87)) ||
                (MessageType.equals(ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_RESPONSE_93))) {
            return ClearingAction.RECONCILEMNET_REQUEST;
        }
        else if((MessageType.equals(ISOMessageTypes.ACQUIRER_RECON_REQUEST_87)) ||
                (MessageType.equals(ISOMessageTypes.ACQUIRER_RECON_REQUEST_93)) ||
                (MessageType.equals(ISOMessageTypes.ACQUIRER_RECON_ADVICE_87)) ||
                (MessageType.equals(ISOMessageTypes.ACQUIRER_RECON_ADVICE_93))) {
            return ClearingAction.ACQUIRER_RECONCILEMNET_RESPONSE;
        }
        else if((MessageType.equals(ISOMessageTypes.ISSUER_RECON_REQUEST_87)) ||
                (MessageType.equals(ISOMessageTypes.ISSUER_RECON_REQUEST_93)) ||
                (MessageType.equals(ISOMessageTypes.ISSUER_RECON_ADVICE_87)) ||
                (MessageType.equals(ISOMessageTypes.ISSUER_RECON_ADVICE_93)))   {
            return ClearingAction.ISSUER_RECONCILEMNET_RESPONSE;
        }
        else if((MessageType.equals(ISOMessageTypes.ISSUER_RECON_RESPONSE_87)) ||
                (MessageType.equals(ISOMessageTypes.ISSUER_RECON_RESPONSE_93)) ||
                (MessageType.equals(ISOMessageTypes.ISSUER_RECON_ADVICE_RESPONSE_87)) ||
                (MessageType.equals(ISOMessageTypes.ISSUER_RECON_ADVICE_RESPONSE_93)))
        {
            return ClearingAction.ISSUER_FINALIZE_RECONCILEMNET;
        }
        else if((MessageType.equals(ISOMessageTypes.ACQUIRER_RECON_RESPONSE_87)) ||
                (MessageType.equals(ISOMessageTypes.ACQUIRER_RECON_RESPONSE_93)) ||
                (MessageType.equals(ISOMessageTypes.ACQUIRER_RECON_ADVICE_RESPONSE_87)) ||
                (MessageType.equals(ISOMessageTypes.ACQUIRER_RECON_ADVICE_RESPONSE_93)))    {
            return ClearingAction.ACQUIRER_FINALIZE_RECONCILEMNET;
        }

        //}
        return ClearingAction.UNKNOWN;
    }

    public ClearingAction findAction(String messageType) { //Raza for KEENU
        //switch (messageType) {
        //String MessageType = messageType + "";

        if((messageType.equals(ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_87)) ||
                (messageType.equals(ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_93)) ||
                (messageType.equals(ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_87)) ||
                (messageType.equals(ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_93))) {
            return ClearingAction.COUTOVER_RESPONSE;
        }
        else if((messageType.equals(ISOMessageTypes.NETWORK_MANAGEMENT_RESPONSE_87)) ||
                (messageType.equals(ISOMessageTypes.NETWORK_MANAGEMENT_RESPONSE_93)) ||
                (messageType.equals(ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_RESPONSE_87)) ||
                (messageType.equals(ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_RESPONSE_93))) {
            return ClearingAction.RECONCILEMNET_REQUEST;
        }
        else if((messageType.equals(ISOMessageTypes.ACQUIRER_RECON_REQUEST_87)) ||
                (messageType.equals(ISOMessageTypes.ACQUIRER_RECON_REQUEST_93)) ||
                (messageType.equals(ISOMessageTypes.ACQUIRER_RECON_ADVICE_87)) ||
                (messageType.equals(ISOMessageTypes.ACQUIRER_RECON_ADVICE_93))) {
            return ClearingAction.ACQUIRER_RECONCILEMNET_RESPONSE;
        }
        else if((messageType.equals(ISOMessageTypes.ISSUER_RECON_REQUEST_87)) ||
                (messageType.equals(ISOMessageTypes.ISSUER_RECON_REQUEST_93)) ||
                (messageType.equals(ISOMessageTypes.ISSUER_RECON_ADVICE_87)) ||
                (messageType.equals(ISOMessageTypes.ISSUER_RECON_ADVICE_93)))   {
            return ClearingAction.ISSUER_RECONCILEMNET_RESPONSE;
        }
        else if((messageType.equals(ISOMessageTypes.ISSUER_RECON_RESPONSE_87)) ||
                (messageType.equals(ISOMessageTypes.ISSUER_RECON_RESPONSE_93)) ||
                (messageType.equals(ISOMessageTypes.ISSUER_RECON_ADVICE_RESPONSE_87)) ||
                (messageType.equals(ISOMessageTypes.ISSUER_RECON_ADVICE_RESPONSE_93)))
        {
            return ClearingAction.ISSUER_FINALIZE_RECONCILEMNET;
        }
        else if((messageType.equals(ISOMessageTypes.ACQUIRER_RECON_RESPONSE_87)) ||
                (messageType.equals(ISOMessageTypes.ACQUIRER_RECON_RESPONSE_93)) ||
                (messageType.equals(ISOMessageTypes.ACQUIRER_RECON_ADVICE_RESPONSE_87)) ||
                (messageType.equals(ISOMessageTypes.ACQUIRER_RECON_ADVICE_RESPONSE_93)))    {
            return ClearingAction.ACQUIRER_FINALIZE_RECONCILEMNET;
        }

        //}
        return ClearingAction.UNKNOWN;
    }
}
