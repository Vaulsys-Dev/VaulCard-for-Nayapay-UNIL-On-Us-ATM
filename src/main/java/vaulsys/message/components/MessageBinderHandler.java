package vaulsys.message.components;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.eft.base.terminalTypeProcessor.TerminalTypeProcessMap;
import vaulsys.eft.base.terminalTypeProcessor.TerminalTypeProcessor;
import vaulsys.message.Message;
import vaulsys.message.exception.MessageBindingException;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class MessageBinderHandler extends BaseHandler {
	private static final Logger logger = Logger.getLogger(MessageBinderHandler.class);

	public static final MessageBinderHandler Instance = new MessageBinderHandler();

	private MessageBinderHandler() {
	}

	@Override
	public void execute(ProcessContext processContext) throws Exception {
		Message inputMessage = null;
		try {
			inputMessage = processContext.getInputMessage();
			TerminalTypeProcessor binderProcessor = TerminalTypeProcessMap.getMessageBinderProcessor(inputMessage.getIfx());
			binderProcessor.bindMessage(inputMessage);
		} catch (Exception ex) {
			throw new MessageBindingException(ex);
		}
	}
}
