package vaulsys.terminal.atm.action.config;

import vaulsys.message.Message;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.impl.ATMTerminal;

public abstract class ConfigurationState extends AbstractState {

    /*
    public NDCMsg process(NDCMsg inputMessage) {
        NDCWriteCommandMsg ndcMsg = null;
        try {
            ndcMsg = prepareMessage();
            ndcMsg.logicalUnitNumber = "000";
            byte[] configData = prepareConfigurationData();

            ndcMsg.messageType = NDCConstants.WRITE_COMMAND; //'3';
            ndcMsg.messageSequenceNumber = "000";
            ndcMsg.writeIdentifier = '1';
            ndcMsg.allConfigData = configData;
            ndcMsg.MAC = "";
            return ndcMsg;
        } catch (Exception e) {
            return null;
        }
    }

    protected abstract NDCWriteCommandMsg prepareMessage() throws Exception;

    protected abstract byte[] prepareConfigurationData() throws Exception;
    
    protected ConfigElement getConfigElement() {
        throw new UnsupportedOperationException("Must be impelemented.");
    }

    protected void writeData(OutputStream out, AtomicATMConfigData configData) throws IOException {
        throw new UnsupportedOperationException("Must be impelemented.");
    }
    */

    @Override
    protected Ifx createOutgoingIfx(Message outputMsg, ATMTerminal atm) {
    	return null;
    }
}
