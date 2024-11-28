package vaulsys.protocols.ndc.handler;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.message.Message;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCTerminalToNetworkMsg;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class NDCReversalMessageHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(NDCReversalMessageHandler.class);

    public static final NDCReversalMessageHandler Instance = new NDCReversalMessageHandler();

    private NDCReversalMessageHandler(){
    }

    @Override
    public void execute(ProcessContext processContext) throws Exception {
        logger.debug("resolving the output message of reference transaction of NDC msg ...");
        try {
            Transaction transaction = processContext.getTransaction();
            Message inputMessage = transaction.getInputMessage();
            NDCTerminalToNetworkMsg ndcMessage = (NDCTerminalToNetworkMsg) inputMessage.getProtocolMessage();

            Terminal endPointTerminal = inputMessage.getEndPointTerminal();
    		logger.debug("NDCReversalMessageHandler EndPointTerminal: " + endPointTerminal.getCode());

    		ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, ndcMessage.getLogicalUnitNumber());
    		logger.debug("NDCReversalMessageHandler Terminal: " + atm.getCode());

            transaction.addOutputMessage(atm.getLastTransaction().getOutputMessage());
            GeneralDao.Instance.saveOrUpdate(transaction);
        } catch (Exception ex) {
            throw ex;
        }
    }
}
