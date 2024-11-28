package vaulsys.protocols.ndc.base.TerminalToNetwork.solicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCSolicitedStatusMsg;
import vaulsys.protocols.ndc.constants.NDCMessageClassSolicitedUnSokicited;
import vaulsys.protocols.ndc.constants.NDCMessageClassTerminalToNetwork;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.util.MyInteger;

public class NDCSolicitedStatusEncryptorInitialisationDataMsg extends NDCSolicitedStatusMsg {
	
	private String encryptorInformation;
	private char infoIdentifier;

	public NDCSolicitedStatusEncryptorInitialisationDataMsg(byte[] rawdata, int index) throws NotParsedBinaryToProtocolException {
        solicited = NDCMessageClassSolicitedUnSokicited.SOLICITED_MESSAGE;
        messageType = NDCMessageClassTerminalToNetwork.ENCRYPTOR_INITIALISATION_DATA;

        MyInteger offset = new MyInteger(index);
        logicalUnitNumber = Long.valueOf(NDCParserUtils.readUntilFS(rawdata, offset));
        NDCParserUtils.readFS(rawdata, offset);
        NDCParserUtils.readFS(rawdata, offset);
        
        infoIdentifier = (char) rawdata[offset.value++];
        
        NDCParserUtils.readFS(rawdata, offset);
        
        switch (infoIdentifier) {
		case '1': // EPP Serial Number and Signature
			break;
		
		case '2': // EPP Public Key and Signature
			break;

		case '3': // New KVV for key just loaded or	reactivated
			newKVV(rawdata, offset.value);
			break;

		case '4': // Keys Status
			allKVVs(rawdata, offset.value);
			break;

		case '5': // Key Loaded
			break;

		case '6': // Key Entry Mode
			break;

		default:
			throw new NotParsedBinaryToProtocolException("Invalid NDC Encryptor Initialisation Data message identifier");
		}
        
	}
	
	public String getEncryptorInformation() {
		return encryptorInformation;
	}

	private void newKVV(byte[] rawdata, int index) throws NotParsedBinaryToProtocolException {
		int len = rawdata.length - index;
		if(len!=6 && len!=72)
			throw new NotParsedBinaryToProtocolException("New KVV information length should be 6 or 72");
		
		StringBuilder builder = new StringBuilder();
		for(; index < rawdata.length; index++)
			builder.append((char)rawdata[index]);
		encryptorInformation = builder.toString();
	}
	
	private void allKVVs(byte[] rawdata, int index) throws NotParsedBinaryToProtocolException {
		StringBuilder builder = new StringBuilder();
		for(; index < rawdata.length; index++)
			builder.append((char)rawdata[index]);
		encryptorInformation = builder.toString();
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("Information Identifier:\t\t");
        switch (infoIdentifier) {
			case '1': // EPP Serial Number and Signature
				builder.append("EPP Serial Number and Signature");
				break;
			
			case '2': // EPP Public Key and Signature
				builder.append("EPP Public Key and Signature");
				break;
	
			case '3': // New KVV for key just loaded or	reactivated
				builder.append("New KVV for key just loaded or	reactivated");
				break;
	
			case '4': // Keys Status
				builder.append("Keys Status");
				break;
	
			case '5': // Key Loaded
				builder.append("Key Loaded");
				break;
	
			case '6': // Key Entry Mode
				builder.append("Key Entry Mode");
				break;
        }
        builder.append("\r\n");
		builder.append("Encryptor Information:\t\t").append(encryptorInformation).append("\r\n");
		if(infoIdentifier=='4' && encryptorInformation.length()>=24) {
			builder.append("\tMaster Key KVV:\t\t").append(getMasterKeyKVV()).append("\r\n");
			builder.append("\tCommunications Key KVV:\t\t").append(getCommKeyKVV()).append("\r\n");
			builder.append("\tMAC Key KVV:\t\t").append(getMacKeyKVV()).append("\r\n");
			builder.append("\tB Key KVV:\t\t").append(getBKeyKVV()).append("\r\n");
		}
        return builder.toString();
	}
	
	public String getMasterKeyKVV() {
		return encryptorInformation.substring(0, 6);
	}
	
	public String getCommKeyKVV() {
		return encryptorInformation.substring(6, 12);
	}
	
	public String getMacKeyKVV() {
		return encryptorInformation.substring(12, 18);
	}
	
	public String getBKeyKVV() {
		return encryptorInformation.substring(18, 24);
	}
}
